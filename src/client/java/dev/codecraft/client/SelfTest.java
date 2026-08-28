package dev.codecraft.client;

import dev.codecraft.CodeCraftMod;
import dev.codecraft.client.gui.EditorScreen;
import dev.codecraft.client.gui.TrackSelectScreen;
import dev.codecraft.lessons.Lesson;
import dev.codecraft.lessons.LessonRepository;
import dev.codecraft.lessons.Track;
import dev.codecraft.progress.TrackStore;
import dev.codecraft.exec.JavaRunner;
import dev.codecraft.exec.OutputSink;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Development-only smoke test, triggered by dropping a "codecraft-selftest.txt" marker in the
 * game directory. Opens the editor, compiles a Playground program, and grabs a screenshot so
 * the GUI and the compile pipeline can be checked without a human driving the client.
 */
@Environment(EnvType.CLIENT)
public final class SelfTest {
	private static final String SOURCE = """
			public class Lesson {
			    public static void main(String[] args) {
			        System.out.println("stdout works");
			        Playground.say("Playground.say works");
			        Playground.placeBlock(0, 3, 0, "gold_block");
			        System.out.println("block above me is now: " + Playground.getBlock(0, 3, 0));
			        System.out.println("player at " + Playground.playerX() + "," + Playground.playerY() + "," + Playground.playerZ());
			        System.out.println("isDay=" + Playground.isDay());
			    }
			}
			""";

	private static final String SEP = "\n    ";

	private static Path marker;
	private static int ticks;
	private static boolean finished;

	private SelfTest() {
	}

	public static void init() {
		marker = FabricLoader.getInstance().getGameDir().resolve("codecraft-selftest.txt");
		if (!Files.exists(marker)) {
			return;
		}
		CodeCraftMod.LOGGER.info("[selftest] marker found, arming");
		ClientTickEvents.END_CLIENT_TICK.register(SelfTest::tick);
	}

	private static void tick(Minecraft client) {
		if (finished || client.player == null || client.level == null) {
			return;
		}
		ticks++;
		if (ticks == 20) {
			for (Track track : Track.values()) {
				CodeCraftMod.LOGGER.info("[selftest] track {} -> {} lessons", track.label(),
						LessonRepository.forTrack(track).size());
			}
			CodeCraftMod.LOGGER.info("[selftest] opening track picker");
			client.setScreen(new TrackSelectScreen());
		} else if (ticks == 30) {
			grab(client, "codecraft-selftest-track.png");
		} else if (ticks == 45) {
			TrackStore.choose(Track.NEW_TO_CODE);
			CodeCraftMod.LOGGER.info("[selftest] chose {}, opening editor", TrackStore.chosen());
			client.setScreen(new EditorScreen());
		} else if (ticks == 60) {
			runCompileTest();
		} else if (ticks == 100) {
			// Exercise /codecraft through the real client-command pipeline, not by calling
			// openEditor directly -- the bug this guards against was the chat screen closing
			// over the editor, which only shows up on the genuine path.
			client.setScreen(null);
			if (client.getConnection() != null) {
				client.getConnection().sendCommand("codecraft");
			}
		} else if (ticks == 110) {
			CodeCraftMod.LOGGER.info("[selftest] after /codecraft, screen is: {}",
					client.screen == null ? "none" : client.screen.getClass().getSimpleName());
		} else if (ticks == 130) {
			// Losing window focus auto-opens the pause screen over ours, so re-assert the editor
			// right before the grab -- otherwise we photograph the vanilla menu instead.
			if (!(client.screen instanceof EditorScreen)) {
				client.setScreen(new EditorScreen());
			}
		} else if (ticks == 140) {
			CodeCraftMod.LOGGER.info("[selftest] guiScale={} forceUnicode={} windowGui={}x{}",
					client.options.guiScale().get(), client.options.forceUnicodeFont().get(),
					client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
			grab(client, "codecraft-selftest.png");
		} else if (ticks == 145 && client.screen != null) {
			// Scroll the lesson list to the bottom so the screenshot proves clipping and the
			// ADVANCED section both render.
			client.screen.mouseScrolled(60, 120, 0, -12);
		} else if (ticks == 150) {
			grab(client, "codecraft-selftest-scrolled.png");
		} else if (ticks == 160) {
			// Last, and on its own: JavaRunner swaps System.out while a program runs, so this must
			// not overlap the Playground smoke test above.
			runEveryLesson();
		} else if (ticks == 170) {
			finished = true;
			try {
				Files.deleteIfExists(marker);
			} catch (IOException e) {
				CodeCraftMod.LOGGER.warn("[selftest] could not remove marker", e);
			}
			CodeCraftMod.LOGGER.info("[selftest] DONE");
		}
	}

	/**
	 * Compile and actually run every shipped lesson.
	 *
	 * Compiling alone would miss the failure that matters most here: a lesson that makes so many
	 * Playground calls it trips the five-second runner timeout. Elapsed time is logged per lesson
	 * so a slow one is obvious before a learner finds it.
	 */
	private static void runEveryLesson() {
		Thread thread = new Thread(() -> {
			int failed = 0;
			for (Lesson lesson : LessonRepository.all()) {
				StringBuilder errors = new StringBuilder();
				AtomicBoolean ok = new AtomicBoolean();
				long startedAt = System.currentTimeMillis();
				JavaRunner.run(lesson.starterCode(), new OutputSink() {
					@Override
					public void onOutput(String line) {
					}

					@Override
					public void onError(String line) {
						errors.append(SEP).append(line);
					}

					@Override
					public void onFinished(boolean success) {
						ok.set(success);
					}
				});
				long elapsed = System.currentTimeMillis() - startedAt;
				if (ok.get() && errors.isEmpty()) {
					CodeCraftMod.LOGGER.info("[selftest] lesson {} ok in {}ms", lesson.id(), elapsed);
				} else {
					failed++;
					CodeCraftMod.LOGGER.error("[selftest] lesson {} FAILED after {}ms:{}", lesson.id(), elapsed, errors);
				}
			}
			CodeCraftMod.LOGGER.info("[selftest] lesson run check: {} lessons, {} failed",
					LessonRepository.all().size(), failed);
		}, "codecraft-selftest-lessons");
		thread.setDaemon(true);
		thread.start();
	}

	private static void grab(Minecraft client, String name) {
		CodeCraftMod.LOGGER.info("[selftest] capturing {}: screen is {}", name,
				client.screen == null ? "none" : client.screen.getClass().getSimpleName());
		Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(),
				component -> CodeCraftMod.LOGGER.info("[selftest] screenshot: {}", component.getString()));
	}

	private static void runCompileTest() {
		CodeCraftMod.LOGGER.info("[selftest] compiling Playground program");
		Thread thread = new Thread(() -> JavaRunner.run(SOURCE, new OutputSink() {
			@Override
			public void onOutput(String line) {
				CodeCraftMod.LOGGER.info("[selftest] out: {}", line);
			}

			@Override
			public void onError(String line) {
				CodeCraftMod.LOGGER.error("[selftest] err: {}", line);
			}

			@Override
			public void onFinished(boolean success) {
				CodeCraftMod.LOGGER.info("[selftest] compile+run success={}", success);
			}
		}), "codecraft-selftest");
		thread.setDaemon(true);
		thread.start();
	}
}
