package dev.codecraft.lessons;

/** How much prior programming knowledge a lesson assumes. Lessons declare one; tracks filter on it. */
public enum LessonLevel {
	/** Syntax from absolute zero: what a statement is, what a variable is. */
	FUNDAMENTALS(0),
	/** Structure and data: methods, arrays, lists, classes. Assumes you can read a loop. */
	CORE(1),
	/** The parts of Java you reach for once the basics are reflex. */
	ADVANCED(2);

	private final int rank;

	LessonLevel(int rank) {
		this.rank = rank;
	}

	public int rank() {
		return rank;
	}

	public static LessonLevel parse(String value, LessonLevel fallback) {
		if (value == null) {
			return fallback;
		}
		for (LessonLevel level : values()) {
			if (level.name().equalsIgnoreCase(value)) {
				return level;
			}
		}
		return fallback;
	}
}
