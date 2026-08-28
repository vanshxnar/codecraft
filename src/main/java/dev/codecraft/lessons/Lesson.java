package dev.codecraft.lessons;

import java.util.List;

public record Lesson(
		String id,
		int order,
		String title,
		String topic,
		LessonLevel level,
		List<String> explanation,
		String starterCode,
		boolean usesPlayground
) {
}
