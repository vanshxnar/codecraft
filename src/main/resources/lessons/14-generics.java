import java.util.ArrayList;
import java.util.List;

public class Lesson {
    // T is a placeholder for a type that whoever uses Plan gets to choose.
    static class Plan<T> {
        private final List<T> steps = new ArrayList<>();

        void add(T step) {
            steps.add(step);
        }

        T get(int index) {
            return steps.get(index);
        }

        int size() {
            return steps.size();
        }
    }

    // A generic method: returns exactly the type the list holds.
    static <T> T last(List<T> items) {
        return items.get(items.size() - 1);
    }

    public static void main(String[] args) {
        Plan<String> blocks = new Plan<>();
        blocks.add("stone");
        blocks.add("stone_bricks");
        blocks.add("gold_block");

        Plan<Integer> heights = new Plan<>();
        heights.add(1);
        heights.add(2);
        heights.add(3);

        // No casts anywhere: the compiler already knows what comes out of each Plan.
        for (int i = 0; i < blocks.size(); i++) {
            String block = blocks.get(i);
            int height = heights.get(i);
            Playground.placeBlock(i * 2, height, 3, block);
            System.out.println("placed " + block + " at height " + height);
        }

        // blocks.add(42); // uncomment: the compiler stops you before the game ever runs.

        List<String> palette = List.of("glass", "sea_lantern", "quartz_block");
        System.out.println("last in palette: " + last(palette));
        Playground.say("Generics: one class, any type, still type-safe.");
    }
}
