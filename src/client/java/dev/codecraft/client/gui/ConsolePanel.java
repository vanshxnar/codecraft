package dev.codecraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** Output-only, scroll-to-bottom log view. Not an interactive widget on purpose. */
final class ConsolePanel {
	private static final int MAX_LINES = 500;

	private final Font font;
	private final List<Entry> lines = new ArrayList<>();
	private int x, y, width, height;

	ConsolePanel(Font font) {
		this.font = font;
	}

	void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	void clear() {
		lines.clear();
	}

	void info(String text) {
		add(text, 0xFFAAAAAA);
	}

	void output(String text) {
		add(text, 0xFFE0E0E0);
	}

	void error(String text) {
		add(text, 0xFFFF6B6B);
	}

	private void add(String text, int color) {
		int wrapWidth = Math.max(20, width - 8);
		for (String rawLine : text.split("\n", -1)) {
			for (FormattedCharSequence seq : font.split(FormattedText.of(rawLine), wrapWidth)) {
				lines.add(new Entry(seq, color));
			}
		}
		while (lines.size() > MAX_LINES) {
			lines.remove(0);
		}
	}

	void render(GuiGraphics graphics) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3A3A40);
		graphics.fill(x, y, x + width, y + height, 0xFF0C0C10);
		int lineHeight = font.lineHeight + 2;
		int maxVisible = Math.max(1, (height - 8) / lineHeight);
		int start = Math.max(0, lines.size() - maxVisible);
		int drawY = y + 4;
		for (int i = start; i < lines.size(); i++) {
			Entry entry = lines.get(i);
			graphics.drawString(font, entry.line(), x + 4, drawY, entry.color(), false);
			drawY += lineHeight;
		}
	}

	private record Entry(FormattedCharSequence line, int color) {
	}
}
