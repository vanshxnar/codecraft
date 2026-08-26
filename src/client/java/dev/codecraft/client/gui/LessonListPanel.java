package dev.codecraft.client.gui;

import dev.codecraft.lessons.Lesson;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Click-to-select lesson list. Not a Minecraft widget -- driven manually from EditorScreen. */
final class LessonListPanel {
	private static final int ROW_HEIGHT = 18;

	private final Font font;
	private final List<Lesson> lessons;
	private final Consumer<Lesson> onSelect;
	private Set<String> completed = Set.of();
	private String selectedId;
	private int x, y, width, height;

	LessonListPanel(Font font, List<Lesson> lessons, Consumer<Lesson> onSelect) {
		this.font = font;
		this.lessons = lessons;
		this.onSelect = onSelect;
	}

	void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	void setCompleted(Set<String> completed) {
		this.completed = completed;
	}

	void setSelected(String lessonId) {
		this.selectedId = lessonId;
	}

	boolean mouseClicked(double mouseX, double mouseY) {
		if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
			return false;
		}
		int index = (int) ((mouseY - y) / ROW_HEIGHT);
		if (index >= 0 && index < lessons.size()) {
			Lesson lesson = lessons.get(index);
			selectedId = lesson.id();
			onSelect.accept(lesson);
			return true;
		}
		return false;
	}

	void render(GuiGraphics graphics) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3A3A40);
		graphics.fill(x, y, x + width, y + height, 0xFF0C0C10);
		int rowY = y;
		for (Lesson lesson : lessons) {
			if (rowY + ROW_HEIGHT > y + height) {
				break;
			}
			boolean selected = lesson.id().equals(selectedId);
			boolean done = completed.contains(lesson.id());
			if (selected) {
				graphics.fill(x, rowY, x + width, rowY + ROW_HEIGHT, 0x804A90D9);
			}
			int color = selected ? 0xFFFFFFFF : done ? 0xFF7FD97F : 0xFFCCCCCC;
			String prefix = done ? "✓ " : "";
			graphics.drawString(font, prefix + lesson.title(), x + 6, rowY + 5, color, false);
			rowY += ROW_HEIGHT;
		}
	}
}
