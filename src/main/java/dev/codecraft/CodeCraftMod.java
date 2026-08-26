package dev.codecraft;

import dev.codecraft.lessons.LessonRepository;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeCraftMod implements ModInitializer {
	public static final String MOD_ID = "codecraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LessonRepository.load();
		LOGGER.info("CodeCraft loaded: {} lessons ready.", LessonRepository.all().size());
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
