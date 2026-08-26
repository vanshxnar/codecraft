package dev.codecraft.lessons;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.codecraft.CodeCraftMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lessons ship as JSON resources under /lessons and are listed in /lessons/index.json,
 * since a jar can't be directory-listed via the classloader.
 */
public final class LessonRepository {
	private static final Gson GSON = new Gson();
	private static final Map<String, Lesson> BY_ID = new LinkedHashMap<>();
	private static final List<Lesson> ORDERED = new ArrayList<>();

	private LessonRepository() {
	}

	public static synchronized void load() {
		if (!ORDERED.isEmpty()) {
			return;
		}
		List<String> files = readIndex();
		for (String file : files) {
			Lesson lesson = readLesson(file);
			if (lesson != null) {
				BY_ID.put(lesson.id(), lesson);
				ORDERED.add(lesson);
			}
		}
		ORDERED.sort((a, b) -> Integer.compare(a.order(), b.order()));
	}

	public static List<Lesson> all() {
		return Collections.unmodifiableList(ORDERED);
	}

	public static Lesson byId(String id) {
		return BY_ID.get(id);
	}

	private static List<String> readIndex() {
		try (InputStream in = LessonRepository.class.getResourceAsStream("/lessons/index.json")) {
			if (in == null) {
				CodeCraftMod.LOGGER.error("Missing /lessons/index.json in mod resources");
				return List.of();
			}
			JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
			List<String> out = new ArrayList<>();
			for (JsonElement e : arr) {
				out.add(e.getAsString());
			}
			return out;
		} catch (IOException e) {
			CodeCraftMod.LOGGER.error("Failed to read lesson index", e);
			return List.of();
		}
	}

	private static Lesson readLesson(String file) {
		String path = "/lessons/" + file;
		try (InputStream in = LessonRepository.class.getResourceAsStream(path)) {
			if (in == null) {
				CodeCraftMod.LOGGER.error("Missing lesson resource {}", path);
				return null;
			}
			JsonObject obj = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			List<String> explanation = new ArrayList<>();
			for (JsonElement e : obj.getAsJsonArray("explanation")) {
				explanation.add(e.getAsString());
			}
			return new Lesson(
					obj.get("id").getAsString(),
					obj.get("order").getAsInt(),
					obj.get("title").getAsString(),
					obj.get("topic").getAsString(),
					explanation,
					obj.get("starterCode").getAsString(),
					obj.has("usesPlayground") && obj.get("usesPlayground").getAsBoolean()
			);
		} catch (IOException | RuntimeException e) {
			CodeCraftMod.LOGGER.error("Failed to parse lesson resource {}", path, e);
			return null;
		}
	}
}
