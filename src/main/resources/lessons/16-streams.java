import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Lesson {
    public static void main(String[] args) {
        // Scan the ground in a line ahead of you.
        List<String> ground = new ArrayList<>();
        for (int dz = 1; dz <= 12; dz++) {
            ground.add(Playground.getBlock(0, -1, dz));
        }
        System.out.println("scanned: " + ground);

        long gaps = ground.stream()
                .filter(block -> block.equals("air"))
                .count();
        System.out.println("gaps ahead: " + gaps);

        // Distinct block ids, tidied up and sorted - one sentence instead of a loop and a Set.
        List<String> kinds = ground.stream()
                .filter(block -> !block.equals("air"))
                .map(block -> block.replace("_", " "))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("solid ground is made of: " + kinds);

        // IntStream generates the numbers, so there is no counter to get wrong.
        IntStream.rangeClosed(1, 12)
                .filter(dz -> Playground.getBlock(0, -1, dz).equals("air"))
                .forEach(dz -> Playground.placeBlock(0, -1, dz, "oak_planks"));

        Playground.say("Bridged " + gaps + " gaps with a three-line pipeline.");
    }
}
