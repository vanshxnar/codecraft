package dev.codecraft.exec;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiles a single Java source in memory and runs its main() on a timeboxed daemon thread.
 * Scoped for trusted single-player use: there is no security-manager style sandbox, only a
 * best-effort timeout. A tight CPU-bound infinite loop in submitted code cannot be force-killed
 * (Java offers no safe way to do that), it will simply be abandoned as a leaked daemon thread.
 */
public final class JavaRunner {
	private static final Pattern CLASS_NAME = Pattern.compile("public\\s+class\\s+(\\w+)");
	private static final long TIMEOUT_MS = 5000;

	private JavaRunner() {
	}

	public static void run(String source, OutputSink sink) {
		Matcher matcher = CLASS_NAME.matcher(source);
		String className = matcher.find() ? matcher.group(1) : "Lesson";

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			sink.onError("No Java compiler is available in this JVM (CodeCraft needs a JDK, not just a JRE, to run your code).");
			sink.onFinished(false);
			return;
		}

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
		ClassFileManager fileManager = new ClassFileManager(stdFileManager);

		JavaFileObject sourceObject = new StringSource(className, source);
		JavaCompiler.CompilationTask task = compiler.getTask(
				null, fileManager, diagnostics, List.of("-proc:none"), null, List.of(sourceObject));

		boolean compiled = task.call();
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
			if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
				sink.onError("line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));
			}
		}
		if (!compiled) {
			sink.onFinished(false);
			return;
		}

		Thread runner = new Thread(() -> executeCompiled(fileManager, className, sink), "codecraft-run");
		runner.setDaemon(true);
		runner.start();
		try {
			runner.join(TIMEOUT_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (runner.isAlive()) {
			sink.onError("Still running after " + (TIMEOUT_MS / 1000) + "s -- looks like an infinite loop. Close and reopen the editor to reset.");
		}
	}

	private static void executeCompiled(ClassFileManager fileManager, String className, OutputSink sink) {
		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;
		System.setOut(new PrintStream(new LineCollectingStream(sink::onOutput), true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(new LineCollectingStream(sink::onError), true, StandardCharsets.UTF_8));
		boolean success = true;
		try {
			ClassLoader loader = fileManager.getClassLoader(null);
			Class<?> loaded = Class.forName(className, true, loader);
			Method main = loaded.getMethod("main", String[].class);
			main.invoke(null, (Object) new String[0]);
		} catch (InvocationTargetException e) {
			sink.onError(describeThrowable(e.getCause() != null ? e.getCause() : e));
			success = false;
		} catch (NoSuchMethodException e) {
			sink.onError("Class '" + className + "' needs a 'public static void main(String[] args)' method.");
			success = false;
		} catch (Throwable t) {
			sink.onError(describeThrowable(t));
			success = false;
		} finally {
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
		sink.onFinished(success);
	}

	private static String describeThrowable(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getClass().getSimpleName());
		if (t.getMessage() != null) {
			sb.append(": ").append(t.getMessage());
		}
		for (StackTraceElement frame : t.getStackTrace()) {
			String cls = frame.getClassName();
			if (!cls.startsWith("java.") && !cls.startsWith("jdk.") && !cls.startsWith("dev.codecraft.exec")) {
				sb.append("\n    at ").append(frame);
			}
		}
		return sb.toString();
	}
}
