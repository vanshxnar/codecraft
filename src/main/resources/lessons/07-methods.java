public class Lesson {
    // A method: a named block of code you can call as often as you like.
    static void pillar(int x, int z, int height, String block) {
        for (int y = 1; y <= height; y++) {
            Playground.placeBlock(x, y, z, block);
        }
        Playground.placeBlock(x, height + 1, z, "sea_lantern");
    }

    // Methods can hand a value back with 'return'.
    static int blocksNeeded(int height) {
        return height + 1;
    }

    public static void main(String[] args) {
        pillar(3, 0, 4, "quartz_pillar");
        pillar(-3, 0, 4, "quartz_pillar");
        pillar(0, 4, 6, "quartz_pillar");

        System.out.println("a 6-tall pillar costs " + blocksNeeded(6) + " blocks");
        Playground.say("Three pillars, one method.");
    }
}
