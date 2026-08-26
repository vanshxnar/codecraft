# CodeCraft

Learn Java **inside** Minecraft. CodeCraft is a Fabric mod that puts a real code editor in the
game window — no alt-tabbing to a browser — and runs what you write against the world you are
standing in. Write a `for` loop, hit Run, and watch a tower go up in front of you.

The curriculum teaches Java from zero, but it assumes you already know Minecraft: lessons talk
in chunks, block ids and coordinates rather than abstract exercises.

## Requirements

- Minecraft Java Edition **1.21.1**
- [Fabric Loader](https://fabricmc.net/use/) 0.19.3+ and [Fabric API](https://modrinth.com/mod/fabric-api)
- A **JDK** 21 (not just a JRE) — CodeCraft compiles your code at runtime and needs a real compiler

## Using it

Join a singleplayer world, then press **G** or run **`/codecraft`** to open the editor.

- Pick a lesson on the left; its explanation appears in the console panel.
- Edit the code in the middle pane and press **Run ▶**.
- `System.out.println(...)` output, compiler errors and stack traces stream into the console.
- Lessons you complete are ticked off and remembered per player.

## The Playground API

Lesson code calls `Playground` to touch the world. Every position is a **relative offset** from
where you are standing, so `(0, 1, 0)` is the block above your head.

| Call | What it does |
| --- | --- |
| `Playground.say(text)` | Chat message to yourself |
| `Playground.showTitle(text)` | Flash text above the hotbar |
| `Playground.placeBlock(dx, dy, dz, id)` | Place a block, e.g. `"stone"` |
| `Playground.breakBlock(dx, dy, dz)` | Replace a block with air |
| `Playground.getBlock(dx, dy, dz)` | Read a block id back out of the world |
| `Playground.spawnEntity(dx, dy, dz, id)` | Spawn a mob, e.g. `"cow"` |
| `Playground.teleportPlayer(dx, dy, dz)` | Move yourself |
| `Playground.particles(dx, dy, dz, id, n)` | Spray particles |
| `Playground.giveItem(id, count)` | Put an item in your inventory |
| `Playground.playSound(id)` | Play a sound at your feet |
| `Playground.playerX/Y/Z()` | Your current world coordinates |
| `Playground.isDay()` | Whether it is daytime |

## Adding lessons

Lessons are data, not code. Each one is a pair of files in `src/main/resources/lessons/`:

- `NN-name.json` — id, order, title, topic and the explanation paragraphs
- `NN-name.java` — the starter code, as a real `.java` file so it stays readable

Add both, then list the `.json` in `lessons/index.json`. Nothing else needs to change.

## Building

```
./gradlew build      # produces the mod jar in build/libs
./gradlew runClient  # launches a dev client with the mod loaded
```

### Dev self-test

Dropping a file named `codecraft-selftest.txt` into the run directory arms an automated smoke
test: it opens the editor, exercises `/codecraft`, compiles and runs a Playground program, and
saves a screenshot to `run/screenshots/`. It does nothing without that marker file, and it
exists so GUI and compiler changes can be verified without driving the client by hand.

## How it works, briefly

Your code is compiled in memory with `javax.tools.JavaCompiler` and run on a timeboxed daemon
thread. Because a Fabric mod's classes are loaded by Knot rather than from `java.class.path`,
javac cannot find `Playground` on a normal classpath — so the compiler is handed those classes
directly out of the mod's own classloader. The whole `Playground` API is deliberately free of
Minecraft types for the same reason; the Minecraft-aware implementation sits behind an interface.

## Scope and limitations

- **Singleplayer only.** World calls run against the integrated server.
- **Not sandboxed.** Submitted code runs with the game's permissions and only has a timeout
  guarding it. This is built for a single learner running their own code locally, not for a
  public server accepting code from strangers.
- A tight infinite loop cannot be force-killed; it is abandoned as a daemon thread, and you
  will be told so.

## Licence and trademarks

MIT. Not affiliated with or endorsed by Mojang or Microsoft. CodeCraft ships no Minecraft
assets — it uses only the public Fabric modding API against a copy of the game you already own.
