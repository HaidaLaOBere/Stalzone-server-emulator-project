package net.stalzone.emulator.mobs;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CLI tool for a mob-health-table JSON file: {@code dump} prints every configured override (and,
 * given a mob registry directory, flags real mob ids that fall back to
 * {@link MobInstance#DEFAULT_MAX_HEALTH}); {@code get} prints the resolved {@code maxHealth} for
 * one mob id. See {@link MobHealthTable}'s class doc for why this whole file is emulator-original
 * tuning, not reverse-engineered STALZONE data.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.mobs.MobHealthTableDumpTool dump &lt;health table.json&gt; [mob configs dir]
 *   java -cp out net.stalzone.emulator.mobs.MobHealthTableDumpTool get &lt;health table.json&gt; &lt;mobId&gt;
 */
public final class MobHealthTableDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 2) {
            System.err.println("usage: MobHealthTableDumpTool <dump|get> <health table.json> [mob configs dir | mobId]");
            System.exit(2);
        }

        String mode = args[0];
        Path path = Path.of(args[1]);
        MobHealthTable table = MobHealthTable.load(path);

        switch (mode) {
            case "dump": {
                System.out.println("Loaded " + table.size() + " mob health override(s) from " + path);
                for (Map.Entry<String, Double> e : table.all().entrySet()) {
                    System.out.println("  " + e.getKey() + ": " + e.getValue());
                }
                if (args.length > 2) {
                    MobRegistry registry = MobRegistry.load(Path.of(args[2]));
                    List<String> allIds = registry.all().stream().map(MobConfig::configId).collect(Collectors.toList());
                    List<String> unconfigured = table.unconfigured(allIds);
                    System.out.println("Real mob ids with no override (fall back to DEFAULT_MAX_HEALTH="
                            + MobInstance.DEFAULT_MAX_HEALTH + "): " + unconfigured.size() + " / " + allIds.size());
                    for (String id : unconfigured) {
                        System.out.println("  " + id);
                    }
                }
                break;
            }
            case "get": {
                if (args.length < 3) {
                    System.err.println("usage: MobHealthTableDumpTool get <health table.json> <mobId>");
                    System.exit(2);
                }
                String mobId = args[2];
                System.out.println(mobId + " -> " + table.maxHealthFor(mobId));
                break;
            }
            default:
                System.err.println("unknown mode \"" + mode + "\" -- expected dump or get");
                System.exit(2);
        }
    }
}
