public class Lesson {
    // Your own exception type: a normal class that extends Exception.
    static class BuildException extends Exception {
        BuildException(String message) {
            super(message);
        }
    }

    static void buildTower(String heightText, String block) throws BuildException {
        int height = Integer.parseInt(heightText); // throws NumberFormatException on junk
        if (height > 16) {
            throw new BuildException("refusing to build " + height + " blocks high");
        }
        for (int y = 1; y <= height; y++) {
            Playground.placeBlock(0, y, 2, block);
        }
        Playground.say("Built a tower " + height + " high.");
    }

    static void attempt(String heightText) {
        try {
            buildTower(heightText, "stone_bricks");
        } catch (NumberFormatException e) {
            System.out.println("\"" + heightText + "\" is not a number: " + e.getMessage());
        } catch (BuildException e) {
            System.out.println("build refused: " + e.getMessage());
        } finally {
            // finally always runs - failure or success.
            System.out.println("finished attempt with input \"" + heightText + "\"");
        }
    }

    public static void main(String[] args) {
        attempt("6");        // works
        attempt("tall");     // NumberFormatException
        attempt("40");       // our own BuildException

        // Unhandled exceptions end the program - uncomment to see the stack trace in the console.
        // int[] small = new int[2];
        // System.out.println(small[5]);

        Playground.say("Three attempts, one tower, and the program survived all three.");
    }
}
