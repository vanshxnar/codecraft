package dev.codecraft.exec;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Captures compiled .class bytes in memory instead of writing them to disk. */
final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
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
	public ClassLoader getClassLoader(Location location) {
		return new MemoryClassLoader(classBytes, ClassFileManager.class.getClassLoader());
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
