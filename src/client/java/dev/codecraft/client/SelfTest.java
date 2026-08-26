package dev.codecraft.client;

import dev.codecraft.CodeCraftMod;
import dev.codecraft.client.gui.EditorScreen;
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
			CodeCraftMod.LOGGER.info("[selftest] opening editor");
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
			CodeCraftMod.LOGGER.info("[selftest] capturing screen: {}",
					client.screen == null ? "none" : client.screen.getClass().getName());
			Screenshot.grab(client.gameDirectory, "codecraft-selftest.png", client.getMainRenderTarget(),
					component -> CodeCraftMod.LOGGER.info("[selftest] screenshot: {}", component.getString()));
		} else if (ticks == 160) {
			finished = true;
			try {
				Files.deleteIfExists(marker);
			} catch (IOException e) {
				CodeCraftMod.LOGGER.warn("[selftest] could not remove marker", e);
			}
			CodeCraftMod.LOGGER.info("[selftest] DONE");
		}
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
