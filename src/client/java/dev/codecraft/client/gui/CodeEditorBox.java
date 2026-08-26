package dev.codecraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;

/**
 * A MultiLineEditBox that can be scrolled back to the top.
 *
 * setValue() parks the cursor at the end of the text and scrolls there, so loading a lesson
 * would drop you at the closing braces instead of line one. setScrollAmount is protected,
 * hence this subclass.
 */
final class CodeEditorBox extends MultiLineEditBox {
	CodeEditorBox(Font font, int x, int y, int width, int height, Component placeholder, Component message) {
		super(font, x, y, width, height, placeholder, message);
	}

	void showTop() {
		setScrollAmount(0);
	}
}
