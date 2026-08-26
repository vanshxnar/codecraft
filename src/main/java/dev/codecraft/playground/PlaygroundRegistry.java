package dev.codecraft.playground;

/**
 * Holds the active {@link PlaygroundBackend}.
 *
 * This indirection exists because {@code Playground} lives in the default package (so lesson
 * code can call it without an import), and Java forbids a class in a named package from
 * referencing one in the default package. The client registers its backend here instead.
 */
public final class PlaygroundRegistry {
	private static volatile PlaygroundBackend backend;

	private PlaygroundRegistry() {
	}

	public static void set(PlaygroundBackend newBackend) {
		backend = newBackend;
	}

	public static PlaygroundBackend get() {
		return backend;
	}
}
