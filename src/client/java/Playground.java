import dev.codecraft.playground.PlaygroundBackend;
import dev.codecraft.playground.PlaygroundRegistry;

/**
 * Your bridge from Java into the Minecraft world.
 *
 * Deliberately in the default (unnamed) package so lesson code can call Playground.xyz(...)
 * with no import -- a learner on lesson 1 shouldn't need to know what a package is yet.
 * Every position is a relative offset from where you're standing: (0, 1, 0) is the block
 * directly above your head, (0, -1, 0) is the one under your feet.
 */
public final class Playground {
	private Playground() {
	}

	/** Sends a chat message to yourself. */
	public static void say(String message) {
		backend().say(message);
	}

	/** Flashes a message above your hotbar. */
	public static void showTitle(String message) {
		backend().showTitle(message);
	}

	/** Places a block, e.g. placeBlock(0, 1, 2, "stone"). */
	public static void placeBlock(int dx, int dy, int dz, String blockId) {
		backend().placeBlock(dx, dy, dz, blockId);
	}

	/** Replaces a block with air. */
	public static void breakBlock(int dx, int dy, int dz) {
		backend().breakBlock(dx, dy, dz);
	}

	/** Reads back what's at a position, e.g. "stone", "grass_block" or "air". */
	public static String getBlock(int dx, int dy, int dz) {
		return backend().getBlock(dx, dy, dz);
	}

	/** Spawns a mob, e.g. spawnEntity(0, 0, 3, "cow"). */
	public static void spawnEntity(int dx, int dy, int dz, String entityId) {
		backend().spawnEntity(dx, dy, dz, entityId);
	}

	public static void teleportPlayer(int dx, int dy, int dz) {
		backend().teleportPlayer(dx, dy, dz);
	}

	/** Sprays particles, e.g. particles(0, 2, 0, "happy_villager", 20). */
	public static void particles(int dx, int dy, int dz, String particleId, int count) {
		backend().particles(dx, dy, dz, particleId, count);
	}

	/** Puts an item in your inventory, e.g. giveItem("diamond", 3). */
	public static void giveItem(String itemId, int count) {
		backend().giveItem(itemId, count);
	}

	/** Plays a sound at your feet, e.g. playSound("entity.player.levelup"). */
	public static void playSound(String soundId) {
		backend().playSound(soundId);
	}

	/** Your current world X coordinate. */
	public static int playerX() {
		return backend().playerX();
	}

	public static int playerY() {
		return backend().playerY();
	}

	public static int playerZ() {
		return backend().playerZ();
	}

	/** True if it's currently daytime in the world. */
	public static boolean isDay() {
		return backend().isDay();
	}

	private static PlaygroundBackend backend() {
		PlaygroundBackend backend = PlaygroundRegistry.get();
		if (backend == null) {
			throw new IllegalStateException(
					"Playground isn't connected to a world yet -- join a singleplayer world and try again.");
		}
		return backend;
	}
}
