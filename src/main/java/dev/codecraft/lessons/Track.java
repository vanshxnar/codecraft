package dev.codecraft.lessons;

/**
 * The learner's self-reported experience, which decides where the curriculum starts.
 *
 * Nothing is deleted by picking a track -- it only sets the floor on lesson level, and you can
 * switch at any time from the editor, so starting high and dropping back is one click.
 */
public enum Track {
	NEW_TO_CODE("New to code", "Start from what a variable is.", LessonLevel.FUNDAMENTALS),
	CODED_BEFORE("Coded before", "Know another language -- skip the syntax primer.", LessonLevel.CORE),
	KNOW_JAVA("Know Java", "Straight to collections, generics and streams.", LessonLevel.ADVANCED);

	private final String label;
	private final String blurb;
	private final LessonLevel from;

	Track(String label, String blurb, LessonLevel from) {
		this.label = label;
		this.blurb = blurb;
		this.from = from;
	}

	public String label() {
		return label;
	}

	public String blurb() {
		return blurb;
	}

	public boolean includes(LessonLevel level) {
		return level.rank() >= from.rank();
	}

	public Track next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static Track parse(String value, Track fallback) {
		if (value == null) {
			return fallback;
		}
		for (Track track : values()) {
			if (track.name().equalsIgnoreCase(value)) {
				return track;
			}
		}
		return fallback;
	}
}
