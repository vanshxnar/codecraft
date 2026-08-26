public class Lesson {
    public static void main(String[] args) {
        // for (start; keep going while; do after each pass)
        for (int y = 1; y <= 8; y++) {
            Playground.placeBlock(2, y, 0, "stone_bricks");
            System.out.println("placed block " + y);
        }

        Playground.placeBlock(2, 9, 0, "torch");
        Playground.playSound("entity.player.levelup");
        Playground.say("One loop, nine blocks.");
    }
}
