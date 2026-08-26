public class Lesson {
    public static void main(String[] args) {
        int x = Playground.playerX();
        int y = Playground.playerY();
        int z = Playground.playerZ();
        System.out.println("You are at " + x + ", " + y + ", " + z);

        // A chunk is 16x16. Integer division throws away the remainder,
        // which is exactly what you want to snap to a chunk corner.
        System.out.println("Chunk corner: X " + (x / 16) * 16 + ", Z " + (z / 16) * 16);
        System.out.println("You are " + (x % 16) + " blocks into that chunk.");

        int reach = 2 + 1;
        Playground.placeBlock(reach, 0, 0, "diamond_block");
        Playground.placeBlock(-reach, 0, 0, "emerald_block");
        Playground.placeBlock(0, 0, reach * 2, "redstone_block");
    }
}
