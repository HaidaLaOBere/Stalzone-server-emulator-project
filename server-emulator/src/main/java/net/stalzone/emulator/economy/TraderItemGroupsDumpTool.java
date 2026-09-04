package net.stalzone.emulator.economy;

import net.stalzone.emulator.items.ItemGroups;

import java.nio.file.Path;

/**
 * CLI demo for {@link TraderItemGroups}. Loads the real {@code logged_trader_item_groups.txt}
 * and, if a real {@code item_groups.txt} path is also given, cross-checks every name against it
 * (see {@link TraderItemGroups}'s class doc for what that comparison did/didn't confirm).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.economy.TraderItemGroupsDumpTool <logged_trader_item_groups.txt> [item_groups.txt]
 */
public final class TraderItemGroupsDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: TraderItemGroupsDumpTool <logged_trader_item_groups.txt> [item_groups.txt]");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        TraderItemGroups groups = TraderItemGroups.load(path);
        System.out.println("Loaded " + groups.size() + " trader-logged group name(s) from " + path);
        for (String name : groups.groupNames()) {
            System.out.println("  " + name);
        }

        if (args.length > 1) {
            ItemGroups itemGroups = ItemGroups.load(Path.of(args[1]));
            System.out.println();
            System.out.println("Cross-check against " + itemGroups.size() + " real item_groups.txt categories:");
            for (String name : groups.groupNames()) {
                boolean present = itemGroups.groupNames().contains(name);
                System.out.println("  " + name + " -> " + (present ? "present in item_groups.txt" : "NOT in item_groups.txt"));
            }
        }
    }
}
