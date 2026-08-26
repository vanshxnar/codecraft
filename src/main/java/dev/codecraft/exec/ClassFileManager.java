package dev.codecraft.exec;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Captures compiled .class bytes in memory, and feeds javac the CodeCraft API classes.
 *
 * The second job matters more than it looks: a Fabric mod's classes are loaded by Knot, not
 * from {@code java.class.path}, so javac's normal classpath scan finds nothing of ours and
 * lesson code referencing Playground would fail with "cannot find symbol". Rather than trying
 * to reconstruct a classpath that works in both dev and a production jar, we hand javac the
 * exact classes it needs, read straight out of our own classloader.
 */
final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	/** Everything lesson code is allowed to reference from CodeCraft. All are Minecraft-free by design. */
	private static final List<String> API_CLASSES = List.of(
			"Playground",
			"dev.codecraft.playground.PlaygroundBackend",
			"dev.codecraft.playground.PlaygroundRegistry");

	private final Map<String, ByteArrayOutputStream> classBytes = new HashMap<>();

	ClassFileManager(StandardJavaFileManager fileManager) {
		super(fileManager);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		classBytes.put(className, bytes);
		return new SimpleJavaFileObject(URI.create("bytes:///" + className.replace('.', '/') + kind.extension), kind) {
			@Override
			public OutputStream openOutputStream() {
				return bytes;
			}
		};
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
		Iterable<JavaFileObject> standard = super.list(location, packageName, kinds, recurse);
		if (location != StandardLocation.CLASS_PATH || !kinds.contains(JavaFileObject.Kind.CLASS)) {
			return standard;
		}
		List<JavaFileObject> combined = new ArrayList<>();
		standard.forEach(combined::add);
		for (String binaryName : API_CLASSES) {
			if (!inPackage(binaryName, packageName, recurse)) {
				continue;
			}
			byte[] bytes = readClassBytes(binaryName);
			if (bytes != null) {
				combined.add(new ApiClassFile(binaryName, bytes));
			}
		}
		return combined;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof ApiClassFile apiFile) {
			return apiFile.binaryName();
		}
		return super.inferBinaryName(location, file);
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		// Parent is our own loader so compiled lesson code resolves Playground (and, through it,
		// the Minecraft-aware backend) at runtime.
		return new MemoryClassLoader(classBytes, ClassFileManager.class.getClassLoader());
	}

	private static boolean inPackage(String binaryName, String packageName, boolean recurse) {
		int lastDot = binaryName.lastIndexOf('.');
		String owner = lastDot < 0 ? "" : binaryName.substring(0, lastDot);
		if (owner.equals(packageName)) {
			return true;
		}
		return recurse && !packageName.isEmpty() && owner.startsWith(packageName + ".");
	}

	private static byte[] readClassBytes(String binaryName) throws IOException {
		String resource = "/" + binaryName.replace('.', '/') + ".class";
		try (InputStream in = ClassFileManager.class.getResourceAsStream(resource)) {
			return in == null ? null : in.readAllBytes();
		}
	}

	/** A compiled class handed to javac from our classloader rather than from a classpath entry. */
	private static final class ApiClassFile extends SimpleJavaFileObject {
		private final String binaryName;
		private final byte[] bytes;

		ApiClassFile(String binaryName, byte[] bytes) {
			super(URI.create("codecraft:///" + binaryName.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
			this.binaryName = binaryName;
			this.bytes = bytes;
		}

		String binaryName() {
			return binaryName;
		}

		@Override
		public InputStream openInputStream() {
			return new ByteArrayInputStream(bytes);
		}
	}

	private static final class MemoryClassLoader extends ClassLoader {
		private final Map<String, ByteArrayOutputStream> classBytes;

		MemoryClassLoader(Map<String, ByteArrayOutputStream> classBytes, ClassLoader parent) {
			super(parent);
			this.classBytes = classBytes;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			ByteArrayOutputStream bytes = classBytes.get(name);
			if (bytes == null) {
				throw new ClassNotFoundException(name);
			}
			byte[] data = bytes.toByteArray();
			return defineClass(name, data, 0, data.length);
		}
	}
}
