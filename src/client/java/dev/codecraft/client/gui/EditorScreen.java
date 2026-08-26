package dev.codecraft.client.gui;

import dev.codecraft.exec.JavaRunner;
import dev.codecraft.exec.OutputSink;
import dev.codecraft.lessons.Lesson;
import dev.codecraft.lessons.LessonRepository;
import dev.codecraft.progress.ProgressStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/** The in-game code editor overlay: lesson list, editor, console and Run -- all rendered directly in the game window. */
public final class EditorScreen extends Screen {
	private static final int MARGIN = 8;
	private static final int LIST_WIDTH = 150;
	private static final int CONSOLE_HEIGHT = 110;
	private static final int BUTTON_HEIGHT = 20;
	private static final int TOP_BAR_HEIGHT = 22;

	private LessonListPanel lessonList;
	private ConsolePanel console;
	private CodeEditorBox editorBox;
	private Button runButton;
	private Lesson current;
	private volatile boolean running;

	public EditorScreen() {
		super(Component.literal("CodeCraft"));
	}

	@Override
	protected void init() {
		List<Lesson> lessons = LessonRepository.all();
		lessonList = new LessonListPanel(this.font, lessons, this::selectLesson);
		console = new ConsolePanel(this.font);

		int listY = MARGIN + TOP_BAR_HEIGHT;
		int listHeight = this.height - listY - MARGIN;
		lessonList.setBounds(MARGIN, listY, LIST_WIDTH, listHeight);

		int rightX = MARGIN + LIST_WIDTH + MARGIN;
		int rightWidth = this.width - rightX - MARGIN;

		int editorY = listY;
		int editorHeight = this.height - editorY - BUTTON_HEIGHT - CONSOLE_HEIGHT - MARGIN - 12;
		editorBox = new CodeEditorBox(this.font, rightX, editorY, rightWidth, editorHeight,
				Component.literal("Write your Java code here..."), Component.literal("Code editor"));
		addRenderableWidget(editorBox);

		int buttonY = editorY + editorHeight + 6;
		runButton = Button.builder(Component.literal("Run ▶"), btn -> runCurrentLesson())
				.bounds(rightX + rightWidth - 90, buttonY, 90, BUTTON_HEIGHT)
				.build();
		addRenderableWidget(runButton);

		int consoleY = buttonY + BUTTON_HEIGHT + 6;
		console.setBounds(rightX, consoleY, rightWidth, CONSOLE_HEIGHT);

		Set<String> completed = completedLessons();
		lessonList.setCompleted(completed);

		Lesson toShow = current != null ? current : lessons.isEmpty() ? null : lessons.get(0);
		if (toShow != null) {
			selectLesson(toShow);
		} else {
			console.error("No lessons found -- check the mod's lessons/ resources.");
		}
	}

	private void selectLesson(Lesson lesson) {
		current = lesson;
		lessonList.setSelected(lesson.id());
		editorBox.setValue(lesson.starterCode());
		editorBox.showTop();
		console.clear();
		console.info(lesson.title() + " (" + lesson.topic() + ")");
		for (String paragraph : lesson.explanation()) {
			console.info(paragraph);
		}
		console.info("Press Run to try it.");
	}

	private void runCurrentLesson() {
		if (current == null || running) {
			return;
		}
		running = true;
		runButton.active = false;
		String source = editorBox.getValue();
		Minecraft client = Minecraft.getInstance();
		console.clear();
		console.info("Running " + current.title() + "...");

		OutputSink sink = new OutputSink() {
			@Override
			public void onOutput(String line) {
				client.execute(() -> console.output(line));
			}

			@Override
			public void onError(String line) {
				client.execute(() -> console.error(line));
			}

			@Override
			public void onFinished(boolean success) {
				client.execute(() -> {
					running = false;
					runButton.active = true;
					console.info(success ? "Finished." : "Finished with errors.");
					if (success && client.player != null) {
						ProgressStore.markComplete(client.player.getUUID(), current.id());
						lessonList.setCompleted(completedLessons());
					}
				});
			}
		};
		Thread thread = new Thread(() -> JavaRunner.run(source, sink), "codecraft-compile");
		thread.setDaemon(true);
		thread.start();
	}

	private Set<String> completedLessons() {
		Minecraft client = Minecraft.getInstance();
		return client.player != null ? ProgressStore.completed(client.player.getUUID()) : Set.of();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Deliberately not calling renderBackground(): it triggers Minecraft's blur-the-world-behind-the-GUI
		// pass, which bled through every gap between our panels. A flat fill keeps the whole screen crisp.
		graphics.fill(0, 0, this.width, this.height, 0xFF1A1A1E);
		graphics.drawString(this.font, "CodeCraft", MARGIN, MARGIN, 0xFFFFFFFF, true);
		lessonList.render(graphics);
		console.render(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	/**
	 * Opt out of the vanilla menu blur.
	 *
	 * Minecraft runs this as part of the standard screen path even though our render() never
	 * calls renderBackground(), and the blur post-effect processes the main render target
	 * mid-frame -- which smeared every panel we had already drawn while leaving the widgets
	 * drawn afterwards crisp. We paint our own opaque background, so there is nothing to blur.
	 */
	@Override
	protected void renderBlurredBackground(float partialTick) {
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (lessonList.mouseClicked(mouseX, mouseY)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
