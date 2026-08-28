package dev.codecraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** Output-only log view: follows new output, but scrolls back with the wheel. Not an interactive widget on purpose. */
final class ConsolePanel {
	private static final int MAX_LINES = 500;

	private final Font font;
	private final List<Entry> lines = new ArrayList<>();
	private int x, y, width, height;
	/** How many lines up from the bottom the view is scrolled. 0 follows the newest output. */
	private int scroll;

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
		scroll = 0;
	}

	/** Jump to the first line -- lesson explanations are read downwards, not tailed like output. */
	void showTop() {
		scroll = Math.max(0, lines.size() - maxVisible());
	}

	boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		boolean inside = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		int overflow = lines.size() - maxVisible();
		if (!inside || overflow <= 0) {
			return false;
		}
		scroll = Math.max(0, Math.min(overflow, scroll + (int) delta));
		return true;
	}

	private int lineHeight() {
		return font.lineHeight + 2;
	}

	private int maxVisible() {
		return Math.max(1, (height - 8) / lineHeight());
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
		scroll = 0; // new output pulls the view back to the bottom
	}

	void render(GuiGraphics graphics) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3A3A40);
		graphics.fill(x, y, x + width, y + height, 0xFF0C0C10);
		int lineHeight = lineHeight();
		int maxVisible = maxVisible();
		int overflow = Math.max(0, lines.size() - maxVisible);
		scroll = Math.min(scroll, overflow);
		int start = overflow - scroll;
		int end = Math.min(lines.size(), start + maxVisible);
		int drawY = y + 4;
		for (int i = start; i < end; i++) {
			Entry entry = lines.get(i);
			graphics.drawString(font, entry.line(), x + 4, drawY, entry.color(), true);
			drawY += lineHeight;
		}

		if (overflow > 0) {
			int barHeight = Math.max(12, height * maxVisible / lines.size());
			int barY = y + (height - barHeight) * start / overflow;
			graphics.fill(x + width - 3, barY, x + width - 1, barY + barHeight, 0xFF5A5A66);
		}
	}

	private record Entry(FormattedCharSequence line, int color) {
	}
}
