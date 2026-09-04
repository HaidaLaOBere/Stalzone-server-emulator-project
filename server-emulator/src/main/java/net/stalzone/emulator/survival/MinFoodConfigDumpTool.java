package net.stalzone.emulator.survival;

import java.nio.file.Path;

/**
 * CLI demo for {@link MinFoodConfig}, same spirit as the other {@code *DumpTool} classes. Loads
 * the real {@code modassets/assets/stalker/min_food.txt} and prints its one confirmed value --
 * see {@link MinFoodConfig}'s class doc for what is and isn't evidenced beyond that.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.survival.MinFoodConfigDumpTool <min_food.txt>
 */
public final class MinFoodConfigDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: MinFoodConfigDumpTool <min_food.txt>");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        MinFoodConfig config = MinFoodConfig.load(path);
        System.out.println("Loaded " + path + ": " + config);
        System.out.println("minFood() = " + config.minFood()
                + " (real confirmed value -- see MinFoodConfig's class doc for what is/isn't known about its meaning)");
    }
}
