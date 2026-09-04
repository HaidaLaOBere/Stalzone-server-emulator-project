package net.stalzone.emulator.movement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for MovementConfig, same spirit as the other *DumpTool classes. Loads the real
 * modassets/assets/stalcraft/moving.txt and prints its size, every variant letter found, and
 * (if keys are given on the command line) each key's base value plus any per-variant overrides.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.movement.MovementConfigDumpTool <moving.txt> [key...]
 */
public final class MovementConfigDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: MovementConfigDumpTool <moving.txt> [key...]");
            System.exit(2);
        }

        Path movingTxt = Path.of(args[0]);
        MovementConfig config = MovementConfig.load(movingTxt);
        System.out.println("Loaded " + config.size() + " movement settings from " + movingTxt);
        System.out.println("Variant letters in use: " + config.variantLetters());

        String[] defaultKeys = {
                "move.speed.factor", "move.sprint.factor", "move.run.factor", "move.sneak.factor",
                "move.crawl.factor", "move.jump.charge.maximum", "move.config.key.name"
        };
        List<String> keysToPrint = args.length > 1 ? List.of(args).subList(1, args.length) : List.of(defaultKeys);

        System.out.println();
        for (String key : keysToPrint) {
            MovementSetting s = config.get(key);
            if (s == null) {
                System.out.println(key + " -> NOT FOUND");
                continue;
            }
            System.out.println(key + " -> base=" + s.baseValue()
                    + (s.variants().isEmpty() ? "" : " variants=" + s.variants()));
        }
    }
}
