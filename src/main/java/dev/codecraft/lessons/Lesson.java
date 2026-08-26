package dev.codecraft.lessons;

import java.util.List;

public record Lesson(
		String id,
		int order,
		String title,
		String topic,
		List<String> explanation,
		String starterCode,
		boolean usesPlayground
) {
}
