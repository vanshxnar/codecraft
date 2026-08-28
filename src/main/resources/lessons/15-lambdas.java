import java.util.function.IntPredicate;

public class Lesson {
    // One abstract method, so a lambda can stand in for it.
    interface BlockPicker {
        String pick(int y);
    }

    static void buildColumn(int dx, int height, BlockPicker picker) {
        for (int y = 1; y <= height; y++) {
            Playground.placeBlock(dx, y, 4, picker.pick(y));
        }
    }

    public static void main(String[] args) {
        // Three rules, three lambdas, one build method.
        buildColumn(-3, 8, y -> "stone_bricks");
        buildColumn(0, 8, y -> y % 2 == 0 ? "glass" : "stone");
        buildColumn(3, 8, y -> y < 4 ? "oak_planks" : y < 7 ? "quartz_block" : "sea_lantern");

        // A standard functional interface from the JDK.
        IntPredicate isTall = y -> y >= 6;
        for (int y = 1; y <= 8; y++) {
            if (isTall.test(y)) {
                Playground.particles(0, y, 4, "end_rod", 4);
            }
        }

        // A lambda is a value, so it can live in a variable and be passed on.
        BlockPicker gold = y -> "gold_block";
        buildColumn(6, 3, gold);

        Playground.say("Same loop, four different columns - the rule was the argument.");
    }
}
