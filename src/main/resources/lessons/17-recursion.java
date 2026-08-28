import java.util.HashSet;
import java.util.Set;

public class Lesson {
    static void branch(int x, int y, int z, int length) {
        if (length <= 0) { // base case: without this it never stops
            Playground.placeBlock(x, y, z, "oak_leaves");
            return;
        }
        for (int i = 0; i < length; i++) {
            Playground.placeBlock(x, y + i, z, "oak_log");
        }
        // Each branch splits into two shorter ones.
        branch(x - 1, y + length, z, length - 2);
        branch(x + 1, y + length, z, length - 2);
    }

    // Spread outward from a spot, one neighbour at a time, remembering where we have been.
    static void floodFill(int dx, int dz, int budget, Set<String> visited) {
        if (budget <= 0 || !visited.add(dx + "," + dz)) {
            return;
        }
        if (!Playground.getBlock(dx, -1, dz).equals("air")) {
            Playground.placeBlock(dx, -1, dz, "smooth_stone");
        }
        floodFill(dx + 1, dz, budget - 1, visited);
        floodFill(dx - 1, dz, budget - 1, visited);
        floodFill(dx, dz + 1, budget - 1, visited);
        floodFill(dx, dz - 1, budget - 1, visited);
    }

    public static void main(String[] args) {
        branch(0, 1, 6, 6);
        System.out.println("tree built by a method that called itself");

        floodFill(0, 0, 4, new HashSet<>());
        System.out.println("paved outward from your feet");

        Playground.say("Two shapes, no coordinates tracked by hand.");
    }
}
