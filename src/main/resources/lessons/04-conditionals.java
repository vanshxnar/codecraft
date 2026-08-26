public class Lesson {
    public static void main(String[] args) {
        String ground = Playground.getBlock(0, -1, 0);
        System.out.println("You are standing on: " + ground);

        // Text is compared with .equals(...), never with ==
        if (ground.equals("air")) {
            Playground.say("You are in mid-air!");
        } else if (ground.equals("water")) {
            Playground.say("You are swimming.");
        } else {
            Playground.say("Solid ground: " + ground);
        }

        if (Playground.isDay()) {
            Playground.showTitle("Daytime - safe to build");
        } else {
            Playground.showTitle("Night - here is a torch");
            Playground.placeBlock(1, 0, 0, "torch");
        }
    }
}
