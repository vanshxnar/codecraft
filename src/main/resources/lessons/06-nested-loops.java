public class Lesson {
    public static void main(String[] args) {
        int size = 7;
        int placed = 0;

        // A loop inside a loop sweeps a 2D area:
        // every x, and for each x, every z.
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Playground.placeBlock(x, 0, z, "polished_andesite");
                placed++;
            }
        }

        System.out.println("placed " + placed + " blocks (" + size + " x " + size + ")");
        Playground.say("Platform built.");
    }
}
