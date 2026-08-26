public class Lesson {
    public static void main(String[] args) {
        String[] palette = {
            "red_concrete",
            "orange_concrete",
            "yellow_concrete",
            "lime_concrete",
            "light_blue_concrete"
        };

        System.out.println("palette holds " + palette.length + " colours");

        // Index positions run 0 .. length-1
        for (int i = 0; i < palette.length; i++) {
            Playground.placeBlock(4, i + 1, 0, palette[i]);
        }

        // A for-each loop, for when you do not need the index
        for (String colour : palette) {
            System.out.println("used " + colour);
        }

        Playground.say("Rainbow tower up.");
    }
}
