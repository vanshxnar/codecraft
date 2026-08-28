import java.util.HashMap;
import java.util.Map;

public class Lesson {
    public static void main(String[] args) {
        // Survey the ground in a 7x7 square around your feet and tally what is down there.
        Map<String, Integer> census = new HashMap<>();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                String block = Playground.getBlock(dx, -1, dz);
                census.put(block, census.getOrDefault(block, 0) + 1);
            }
        }

        System.out.println("ground survey:");
        String commonest = "air";
        for (Map.Entry<String, Integer> entry : census.entrySet()) {
            System.out.println("  " + entry.getKey() + " x" + entry.getValue());
            if (entry.getValue() > census.getOrDefault(commonest, 0)) {
                commonest = entry.getKey();
            }
        }

        Playground.say("You are standing mostly on " + commonest + ".");
        Playground.say("Found " + census.size() + " different block types.");
    }
}
