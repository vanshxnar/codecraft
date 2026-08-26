public class Lesson {
    public static void main(String[] args) {
        String block = "gold_block";
        int height = 3;
        double sprintSpeed = 5.612;
        boolean creative = false;

        Playground.placeBlock(0, height, 0, block);
        Playground.say("Placed " + block + ", " + height + " blocks up.");

        System.out.println("sprint speed: " + sprintSpeed + " blocks/sec");
        System.out.println("creative mode: " + creative);
    }
}
