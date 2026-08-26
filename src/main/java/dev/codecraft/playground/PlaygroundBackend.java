package dev.codecraft.playground;

/**
 * The world-interaction operations lesson code can perform.
 *
 * Deliberately free of any Minecraft type: javac has to read this interface (and
 * {@link PlaygroundRegistry}) when it compiles lesson code, and under Fabric's Knot
 * classloader the Minecraft classes are not reachable from a plain compiler classpath.
 * Keeping the whole API surface to primitives and Strings means the compiler never has
 * to resolve a game class. The Minecraft-aware implementation lives client-side.
 */
public interface PlaygroundBackend {
	void say(String message);

	void showTitle(String message);

	void placeBlock(int dx, int dy, int dz, String blockId);

	void breakBlock(int dx, int dy, int dz);

	String getBlock(int dx, int dy, int dz);

	void spawnEntity(int dx, int dy, int dz, String entityId);

	void teleportPlayer(int dx, int dy, int dz);

	void particles(int dx, int dy, int dz, String particleId, int count);

	void giveItem(String itemId, int count);

	void playSound(String soundId);

	int playerX();

	int playerY();

	int playerZ();

	boolean isDay();
}
