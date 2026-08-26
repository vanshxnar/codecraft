package dev.codecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.codecraft.client.gui.EditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CodeCraftModClient implements ClientModInitializer {
	private static final KeyMapping OPEN_EDITOR = new KeyMapping(
			"key.codecraft.open_editor", GLFW.GLFW_KEY_G, "key.categories.codecraft");

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(OPEN_EDITOR);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_EDITOR.consumeClick()) {
				openEditor(client);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
						.literal("codecraft")
						.executes(context -> {
							// Deferred: opening a screen synchronously inside a chat-submitted command's
							// executes() gets immediately clobbered by the chat screen's own close-screen
							// call later in that same call stack. Queuing it runs after that stack unwinds.
							Minecraft client = Minecraft.getInstance();
							client.execute(() -> openEditor(client));
							return com.mojang.brigadier.Command.SINGLE_SUCCESS;
						})));
	}

	private static void openEditor(Minecraft client) {
		if (client.player != null && client.screen == null) {
			client.setScreen(new EditorScreen());
		}
	}
}
