package dev.codecraft.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.codecraft.CodeCraftMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks which lessons each local player has completed, persisted as one JSON file per player. */
public final class ProgressStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type SET_TYPE = new TypeToken<LinkedHashSet<String>>() {
	}.getType();
	private static final ConcurrentHashMap<UUID, Set<String>> CACHE = new ConcurrentHashMap<>();

	private ProgressStore() {
	}

	private static Path fileFor(UUID player) {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve("codecraft").resolve("progress");
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			CodeCraftMod.LOGGER.error("Could not create progress directory", e);
		}
		return dir.resolve(player + ".json");
	}

	public static synchronized Set<String> completed(UUID player) {
		return CACHE.computeIfAbsent(player, id -> {
			Path file = fileFor(id);
			if (!Files.exists(file)) {
				return new LinkedHashSet<>();
			}
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				Set<String> loaded = GSON.fromJson(reader, SET_TYPE);
				return loaded != null ? loaded : new LinkedHashSet<>();
			} catch (IOException e) {
				CodeCraftMod.LOGGER.error("Failed to read progress for {}", id, e);
				return new LinkedHashSet<>();
			}
		});
	}

	public static synchronized void markComplete(UUID player, String lessonId) {
		Set<String> set = completed(player);
		if (set.add(lessonId)) {
			save(player, set);
		}
	}

	private static void save(UUID player, Set<String> set) {
		Path file = fileFor(player);
		try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			GSON.toJson(set, SET_TYPE, writer);
		} catch (IOException e) {
			CodeCraftMod.LOGGER.error("Failed to save progress for {}", player, e);
		}
	}
}
