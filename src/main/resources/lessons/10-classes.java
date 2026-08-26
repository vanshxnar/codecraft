public class Lesson {
    // A class is a blueprint. An object is one thing built from it.
    static class Tower {
        String block;
        int height;

        Tower(String block, int height) {
            this.block = block;
            this.height = height;
        }

        void buildAt(int x, int z) {
            for (int y = 1; y <= height; y++) {
                Playground.placeBlock(x, y, z, block);
            }
            Playground.particles(x, height + 1, z, "flame", 12);
        }
    }

    public static void main(String[] args) {
        Tower stone = new Tower("stone_bricks", 5);
        Tower gold = new Tower("gold_block", 3);

        stone.buildAt(5, 0);
        gold.buildAt(0, 5);

        System.out.println("two towers, one class");
        Playground.say("Objects stamp out copies that each hold their own data.");
    }
}
