package dev.codecraft.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.codecraft.CodeCraftMod;
import dev.codecraft.lessons.Track;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Remembers which experience track the learner picked.
 *
 * Unlike lesson progress this is not per-player: it describes the person at the keyboard, so one
 * file for the installation is right, and it lets the editor know on first open whether it still
 * needs to ask.
 */
public final class TrackStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static Track cached;
	private static boolean loaded;

	private TrackStore() {
	}

	private static Path file() {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve("codecraft");
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			CodeCraftMod.LOGGER.error("Could not create config directory", e);
		}
		return dir.resolve("track.json");
	}

	/** The chosen track, or null if the learner has never been asked. */
	public static synchronized Track chosen() {
		if (loaded) {
			return cached;
		}
		loaded = true;
		Path file = file();
		if (!Files.exists(file)) {
			return null;
		}
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
			cached = Track.parse(obj.has("track") ? obj.get("track").getAsString() : null, null);
		} catch (IOException | RuntimeException e) {
			CodeCraftMod.LOGGER.error("Failed to read track preference", e);
		}
		return cached;
	}

	/** The chosen track, falling back to the beginner track when nothing has been picked yet. */
	public static Track chosenOrDefault() {
		Track track = chosen();
		return track != null ? track : Track.NEW_TO_CODE;
	}

	public static synchronized void choose(Track track) {
		cached = track;
		loaded = true;
		JsonObject obj = new JsonObject();
		obj.addProperty("track", track.name());
		try (Writer writer = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
			GSON.toJson(obj, writer);
		} catch (IOException e) {
			CodeCraftMod.LOGGER.error("Failed to save track preference", e);
		}
	}
}
