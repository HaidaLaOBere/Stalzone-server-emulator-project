package net.stalzone.emulator.items;

import java.nio.file.Path;

/**
 * CLI demo for {@link ItemTiers}. Loads the real {@code gloomycore/item_tiers.txt} and prints
 * its tier names in real file order with their 0-based ordinal.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.items.ItemTiersDumpTool <item_tiers.txt>
 */
public final class ItemTiersDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: ItemTiersDumpTool <item_tiers.txt>");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        ItemTiers tiers = ItemTiers.load(path);
        System.out.println("Loaded " + tiers.size() + " tier(s) from " + path);
        for (String tier : tiers.tiers()) {
            System.out.println("  [" + tiers.ordinalOf(tier) + "] " + tier);
        }
    }
}
