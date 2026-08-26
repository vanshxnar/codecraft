public class Lesson {
    // An interface is a promise: whatever implements it provides these methods.
    interface Structure {
        void build(int x, int z);

        String name();
    }

    static class Pillar implements Structure {
        public void build(int x, int z) {
            for (int y = 1; y <= 4; y++) {
                Playground.placeBlock(x, y, z, "quartz_pillar");
            }
        }

        public String name() {
            return "Pillar";
        }
    }

    // Beacon reuses Pillar's build() through super, then adds to it.
    static class Beacon extends Pillar {
        public void build(int x, int z) {
            super.build(x, z);
            Playground.placeBlock(x, 5, z, "sea_lantern");
            Playground.particles(x, 6, z, "end_rod", 15);
        }

        public String name() {
            return "Beacon";
        }
    }

    public static void main(String[] args) {
        Structure[] plan = { new Pillar(), new Beacon(), new Pillar() };

        int x = -4;
        for (Structure piece : plan) {
            piece.build(x, 5);
            System.out.println("built " + piece.name() + " at x=" + x);
            x += 4;
        }

        Playground.say("Same loop, different behaviour. That is polymorphism.");
    }
}
