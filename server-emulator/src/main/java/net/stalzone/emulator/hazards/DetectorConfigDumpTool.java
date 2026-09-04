package net.stalzone.emulator.hazards;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * CLI demo for DetectorConfig, same spirit as the other *DumpTool classes. Loads the real
 * modassets/assets/stalker/detectors.txt, prints every tier grouped by hazard type, and (if a
 * type + distance are given) resolves which tier would be active at that distance.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.hazards.DetectorConfigDumpTool <detectors.txt> [type distance]
 */
public final class DetectorConfigDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: DetectorConfigDumpTool <detectors.txt> [type distance]");
            System.exit(2);
        }

        Path detectorsTxt = Path.of(args[0]);
        DetectorConfig config = DetectorConfig.load(detectorsTxt);
        System.out.println("Loaded " + config.size() + " detector tiers from " + detectorsTxt);
        System.out.println("Types: " + config.types());
        System.out.println();

        for (String type : config.types()) {
            System.out.println(type + ":");
            for (DetectorTier tier : config.tiersOf(type)) {
                StringBuilder line = new StringBuilder();
                line.append(String.format(Locale.ROOT, "  dist>=%d: periodBase=%.1f periodRandom=%.1f",
                        tier.startDistance, tier.periodBase, tier.periodRandom));
                if (tier.pitchMin != null) {
                    line.append(String.format(Locale.ROOT, " pitch=[%.2f,%.2f]", tier.pitchMin, tier.pitchMax));
                }
                line.append(" sounds=").append(tier.soundNames());
                System.out.println(line);
            }
        }

        if (args.length >= 3) {
            String type = args[1];
            double distance = Double.parseDouble(args[2]);
            DetectorTier active = config.tierFor(type, distance);
            System.out.println();
            System.out.println("At distance " + distance + " from a \"" + type + "\" hazard: "
                    + (active != null ? active : "no reaction yet (farther than every known tier)"));
        }
    }
}
