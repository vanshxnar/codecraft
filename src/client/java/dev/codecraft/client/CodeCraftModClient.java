package dev.codecraft.client;

import com.mojang.brigadier.Command;
import dev.codecraft.client.gui.EditorScreen;
import dev.codecraft.client.playground.ClientPlaygroundBackend;
import dev.codecraft.playground.PlaygroundRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CodeCraftModClient implements ClientModInitializer {
	private static final KeyMapping OPEN_EDITOR = new KeyMapping(
			"key.codecraft.open_editor", GLFW.GLFW_KEY_G, "key.categories.codecraft");

	/**
	 * Set by /codecraft, consumed on the next client tick.
	 *
	 * Opening the screen inline from the command doesn't work: Minecraft#execute runs the task
	 * immediately when it's already on the render thread, and ChatScreen closes itself (setScreen(null))
	 * right after dispatching the command -- which would wipe out the editor we just opened. Waiting
	 * for the next tick puts us safely after that.
	 */
	private static volatile boolean openRequested;

	@Override
	public void onInitializeClient() {
		PlaygroundRegistry.set(new ClientPlaygroundBackend());
		KeyBindingHelper.registerKeyBinding(OPEN_EDITOR);
		SelfTest.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_EDITOR.consumeClick()) {
				openRequested = true;
			}
			if (openRequested) {
				openRequested = false;
				openEditor(client);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("codecraft")
						.executes(context -> {
							openRequested = true;
							return Command.SINGLE_SUCCESS;
						})));
	}

	private static void openEditor(Minecraft client) {
		if (client.player != null && client.screen == null) {
			client.setScreen(new EditorScreen());
		}
	}
}
