import java.util.ArrayList;
import java.util.List;

public class Lesson {
    public static void main(String[] args) {
        // An array has a fixed size. A List grows as you add to it.
        List<String> scanned = new ArrayList<>();

        for (int dz = 1; dz <= 8; dz++) {
            scanned.add(Playground.getBlock(0, -1, dz));
        }

        System.out.println("scanned " + scanned.size() + " blocks ahead:");
        System.out.println(scanned);

        int bridged = 0;
        for (int i = 0; i < scanned.size(); i++) {
            if (scanned.get(i).equals("air")) {
                Playground.placeBlock(0, -1, i + 1, "oak_planks");
                bridged++;
            }
        }

        Playground.say("Bridged " + bridged + " gaps in the path ahead.");
    }
}
