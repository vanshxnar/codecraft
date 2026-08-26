public class Lesson {
    static int size = 7;
    static int wallHeight = 4;

    static void floor(String block) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Playground.placeBlock(x, 0, z, block);
            }
        }
    }

    static void walls(String block) {
        for (int y = 1; y <= wallHeight; y++) {
            for (int i = 0; i < size; i++) {
                Playground.placeBlock(i, y, 0, block);
                Playground.placeBlock(i, y, size - 1, block);
                Playground.placeBlock(0, y, i, block);
                Playground.placeBlock(size - 1, y, i, block);
            }
        }
    }

    static void roof(String block) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Playground.placeBlock(x, wallHeight + 1, z, block);
            }
        }
    }

    static void doorway() {
        Playground.breakBlock(size / 2, 1, 0);
        Playground.breakBlock(size / 2, 2, 0);
    }

    static void windows() {
        Playground.placeBlock(2, 3, 0, "glass");
        Playground.placeBlock(4, 3, 0, "glass");
        Playground.placeBlock(0, 3, 3, "glass");
        Playground.placeBlock(size - 1, 3, 3, "glass");
    }

    static void lights() {
        Playground.placeBlock(1, wallHeight, 1, "sea_lantern");
        Playground.placeBlock(size - 2, wallHeight, size - 2, "sea_lantern");
    }

    public static void main(String[] args) {
        Playground.showTitle("Building your base...");

        floor("stone_bricks");
        walls("oak_planks");
        roof("dark_oak_slab");
        doorway();
        windows();
        lights();

        Playground.particles(size / 2, wallHeight + 2, size / 2, "happy_villager", 40);
        Playground.playSound("entity.player.levelup");
        Playground.giveItem("diamond", 5);
        Playground.say("Base complete - walk in through the door!");

        System.out.println("Now change size, wallHeight, or the block names and run it again.");
    }
}
