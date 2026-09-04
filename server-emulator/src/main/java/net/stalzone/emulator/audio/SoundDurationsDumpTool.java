package net.stalzone.emulator.audio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for SoundDurations, same spirit as the other *DumpTool classes. Loads a real
 * sound/speech-duration CSV, prints its size, and (if a substring is given) every key
 * containing it with its duration.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.audio.SoundDurationsDumpTool <duration.csv> [substring]
 */
public final class SoundDurationsDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: SoundDurationsDumpTool <duration.csv> [substring]");
            System.exit(2);
        }

        Path csvPath = Path.of(args[0]);
        SoundDurations durations = SoundDurations.load(csvPath);
        System.out.println("Loaded " + durations.size() + " duration entries from " + csvPath);

        String substring = args.length > 1 ? args[1] : "operation_lesta";
        List<String> matches = durations.keysContaining(substring);
        System.out.println();
        System.out.println("Keys containing \"" + substring + "\" (" + matches.size() + "):");
        for (String key : matches) {
            System.out.println("  " + key + " -> " + durations.durationMs(key) + "ms");
        }
    }
}
