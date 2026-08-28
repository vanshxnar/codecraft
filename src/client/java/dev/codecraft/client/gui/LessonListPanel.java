package dev.codecraft.client.gui;

import dev.codecraft.lessons.Lesson;
import dev.codecraft.lessons.LessonLevel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Click-to-select lesson list, grouped by level and scrollable. Driven manually from EditorScreen. */
final class LessonListPanel {
	private static final int ROW_HEIGHT = 18;
	private static final int HEADER_HEIGHT = 14;
	private static final int SCROLL_STEP = ROW_HEIGHT;

	/** One line in the list: either a level heading or a lesson. */
	private record Row(LessonLevel header, Lesson lesson) {
		int height() {
			return header != null ? HEADER_HEIGHT : ROW_HEIGHT;
		}
	}

	private final Font font;
	private final Consumer<Lesson> onSelect;
	private List<Row> rows = List.of();
	private Set<String> completed = Set.of();
	private String selectedId;
	private int x, y, width, height;
	private int scroll;
	private int contentHeight;

	LessonListPanel(Font font, List<Lesson> lessons, Consumer<Lesson> onSelect) {
		this.font = font;
		this.onSelect = onSelect;
		setLessons(lessons);
	}

	void setLessons(List<Lesson> lessons) {
		List<Row> built = new ArrayList<>();
		LessonLevel seen = null;
		for (Lesson lesson : lessons) {
			if (lesson.level() != seen) {
				seen = lesson.level();
				built.add(new Row(seen, null));
			}
			built.add(new Row(null, lesson));
		}
		rows = List.copyOf(built);
		contentHeight = rows.stream().mapToInt(Row::height).sum();
		scroll = 0;
	}

	void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		clampScroll();
	}

	void setCompleted(Set<String> completed) {
		this.completed = completed;
	}

	void setSelected(String lessonId) {
		this.selectedId = lessonId;
		scrollIntoView(lessonId);
	}

	private boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	boolean mouseClicked(double mouseX, double mouseY) {
		if (!contains(mouseX, mouseY)) {
			return false;
		}
		int rowTop = y - scroll;
		for (Row row : rows) {
			int rowBottom = rowTop + row.height();
			if (row.lesson() != null && mouseY >= rowTop && mouseY < rowBottom) {
				selectedId = row.lesson().id();
				onSelect.accept(row.lesson());
				return true;
			}
			rowTop = rowBottom;
		}
		return true; // swallow clicks inside the panel so they don't fall through to the editor
	}

	boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (!contains(mouseX, mouseY) || contentHeight <= height) {
			return false;
		}
		scroll -= (int) (delta * SCROLL_STEP);
		clampScroll();
		return true;
	}

	private void clampScroll() {
		scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - height)));
	}

	/** Keep the selected lesson on screen -- switching tracks or reopening can land it out of view. */
	private void scrollIntoView(String lessonId) {
		int rowTop = 0;
		for (Row row : rows) {
			if (row.lesson() != null && row.lesson().id().equals(lessonId)) {
				if (rowTop < scroll) {
					scroll = rowTop;
				} else if (rowTop + row.height() > scroll + height) {
					scroll = rowTop + row.height() - height;
				}
				clampScroll();
				return;
			}
			rowTop += row.height();
		}
	}

	void render(GuiGraphics graphics) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF3A3A40);
		graphics.fill(x, y, x + width, y + height, 0xFF0C0C10);
		graphics.enableScissor(x, y, x + width, y + height);

		int rowTop = y - scroll;
		for (Row row : rows) {
			int rowBottom = rowTop + row.height();
			if (rowBottom > y && rowTop < y + height) {
				if (row.header() != null) {
					graphics.drawString(font, levelLabel(row.header()), x + 6, rowTop + 4, 0xFF8A8A96, true);
				} else {
					Lesson lesson = row.lesson();
					boolean selected = lesson.id().equals(selectedId);
					boolean done = completed.contains(lesson.id());
					if (selected) {
						graphics.fill(x, rowTop, x + width, rowBottom, 0x804A90D9);
					}
					int color = selected ? 0xFFFFFFFF : done ? 0xFF7FD97F : 0xFFCCCCCC;
					String prefix = done ? "✓ " : "";
					graphics.drawString(font, prefix + lesson.title(), x + 6, rowTop + 5, color, true);
				}
			}
			rowTop = rowBottom;
		}
		graphics.disableScissor();

		if (contentHeight > height) {
			int barHeight = Math.max(12, height * height / contentHeight);
			int barY = y + (height - barHeight) * scroll / (contentHeight - height);
			graphics.fill(x + width - 3, barY, x + width - 1, barY + barHeight, 0xFF5A5A66);
		}
	}

	private static String levelLabel(LessonLevel level) {
		return switch (level) {
			case FUNDAMENTALS -> "FUNDAMENTALS";
			case CORE -> "CORE JAVA";
			case ADVANCED -> "ADVANCED";
		};
	}
}
