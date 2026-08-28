package dev.codecraft.client.gui;

import dev.codecraft.lessons.LessonRepository;
import dev.codecraft.lessons.Track;
import dev.codecraft.progress.TrackStore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Asked once, the first time the editor is opened: how much Java the learner already knows.
 *
 * The answer only sets where the lesson list starts, and the editor has a button to change it,
 * so there is no wrong choice to be stuck with.
 */
public final class TrackSelectScreen extends Screen {
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_SPACING = 42;

	public TrackSelectScreen() {
		super(Component.literal("Choose your starting point"));
	}

	@Override
	protected void init() {
		int y = this.height / 2 - (Track.values().length * ROW_SPACING) / 2 + 6;
		for (Track track : Track.values()) {
			int buttonY = y;
			addRenderableWidget(Button.builder(Component.literal(track.label()), btn -> choose(track))
					.bounds((this.width - BUTTON_WIDTH) / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
					.build());
			y += ROW_SPACING;
		}
	}

	private void choose(Track track) {
		TrackStore.choose(track);
		this.minecraft.setScreen(new EditorScreen());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xFF1A1A1E);

		int titleY = this.height / 2 - (Track.values().length * ROW_SPACING) / 2 - 30;
		drawCentered(graphics, "CodeCraft", titleY, 0xFFFFFFFF);
		drawCentered(graphics, "How much Java do you already know?", titleY + 14, 0xFFCCCCCC);

		super.render(graphics, mouseX, mouseY, partialTick);

		// Blurbs go after super.render so they sit on top of nothing -- the buttons are drawn there.
		int y = this.height / 2 - (Track.values().length * ROW_SPACING) / 2 + 6;
		for (Track track : Track.values()) {
			int count = LessonRepository.forTrack(track).size();
			drawCentered(graphics, track.blurb() + "  (" + count + " lessons)", y + BUTTON_HEIGHT + 3, 0xFF8A8A96);
			y += ROW_SPACING;
		}

		drawCentered(graphics, "You can change this any time from the editor.", this.height - 24, 0xFF6A6A76);
	}

	private void drawCentered(GuiGraphics graphics, String text, int y, int color) {
		graphics.drawString(this.font, text, (this.width - this.font.width(text)) / 2, y, color, true);
	}

	/** See EditorScreen: the vanilla blur pass smears everything drawn before the widgets. */
	@Override
	protected void renderBlurredBackground(float partialTick) {
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
