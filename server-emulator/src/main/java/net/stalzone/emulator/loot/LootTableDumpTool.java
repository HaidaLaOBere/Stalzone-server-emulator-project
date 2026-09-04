package net.stalzone.emulator.loot;

import net.stalzone.emulator.config.ServerConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * CLI tool for a loot-table JSON file: {@code dump} prints every configured table, {@code
 * simulate} rolls a mob id N times and tallies the outcomes -- useful for an operator/modder
 * tuning {@code dropChance}/{@code weight} values without having to run the full {@code
 * GameLoopDemo}. See {@link LootTable}'s class doc for why this whole package is
 * emulator-original design, not reverse-engineered STALZONE data.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.loot.LootTableDumpTool dump &lt;loot tables.json&gt;
 *   java -cp out net.stalzone.emulator.loot.LootTableDumpTool simulate &lt;loot tables.json&gt; &lt;mobId&gt; &lt;rolls&gt; [lootRateMultiplier]
 */
public final class LootTableDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 2) {
            System.err.println("usage: LootTableDumpTool <dump|simulate> <loot tables.json> [mobId] [rolls] [lootRateMultiplier]");
            System.exit(2);
        }

        String mode = args[0];
        Path path = Path.of(args[1]);
        LootTables tables = LootTables.load(path);

        switch (mode) {
            case "dump": {
                System.out.println("Loaded " + tables.size() + " loot table(s) from " + path);
                for (Map.Entry<String, LootTable> e : tables.all().entrySet()) {
                    System.out.println("  " + e.getKey() + ": " + e.getValue());
                    for (LootEntry entry : e.getValue().entries()) {
                        System.out.println("    " + entry);
                    }
                }
                break;
            }
            case "simulate": {
                if (args.length < 4) {
                    System.err.println("usage: LootTableDumpTool simulate <loot tables.json> <mobId> <rolls> [lootRateMultiplier]");
                    System.exit(2);
                }
                String mobId = args[2];
                int rolls = Integer.parseInt(args[3]);
                double lootRateMultiplier = args.length > 4 ? Double.parseDouble(args[4]) : 1.0;

                LootTable table = tables.forMob(mobId);
                if (table == null) {
                    System.err.println("no loot table configured for mobId \"" + mobId + "\"");
                    System.exit(1);
                }

                ServerConfig config = new ServerConfig();
                config.lootRateMultiplier = lootRateMultiplier;
                LootRoller roller = new LootRoller(config, new Random(1234)); // fixed seed: reproducible CLI runs

                int noDrop = 0;
                Map<String, Integer> itemCounts = new java.util.LinkedHashMap<>();
                for (int i = 0; i < rolls; i++) {
                    List<LootDrop> drops = roller.roll(table);
                    if (drops.isEmpty()) {
                        noDrop++;
                    } else {
                        for (LootDrop d : drops) {
                            itemCounts.merge(d.itemId(), d.count(), Integer::sum);
                        }
                    }
                }

                System.out.println("Simulated " + rolls + " kills of \"" + mobId + "\" (table: " + table
                        + ", lootRateMultiplier=" + lootRateMultiplier + "):");
                System.out.println("  no drop: " + noDrop + " / " + rolls
                        + String.format(" (%.1f%%)", 100.0 * noDrop / rolls));
                for (Map.Entry<String, Integer> e : itemCounts.entrySet()) {
                    System.out.println("  total " + e.getKey() + ": " + e.getValue());
                }
                break;
            }
            default:
                System.err.println("unknown mode \"" + mode + "\" -- expected dump or simulate");
                System.exit(2);
        }
    }
}
