package net.stalzone.emulator.items;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for ItemWeightTable, same spirit as the other *DumpTool classes. Loads the real
 * modassets/assets/stalker/weight.txt and prints the default carry-weight cap, the total entry
 * count, every id with a real duplicate-with-different-value conflict (see ItemWeightTable's doc
 * comment), and the weight for any specific item ids given on the command line.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.items.ItemWeightTableDumpTool <weight.txt> [itemId...]
 */
public final class ItemWeightTableDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: ItemWeightTableDumpTool <weight.txt> [itemId...]");
            System.exit(2);
        }

        Path weightTxt = Path.of(args[0]);
        ItemWeightTable table = ItemWeightTable.load(weightTxt);

        System.out.println("Loaded " + table.size() + " item weight entries from " + weightTxt);
        System.out.println("character_max (default carry weight cap): " + table.characterMaxWeight());

        List<Integer> conflicts = table.conflictingIds();
        System.out.println();
        System.out.println("Ids with conflicting duplicate entries (" + conflicts.size() + "):");
        for (int id : conflicts) {
            System.out.println("  " + id + " -> " + table.allValuesOf(id)
                    + " (weightOf() returns the last one: " + table.weightOf(id) + ")");
        }

        if (args.length > 1) {
            System.out.println();
            System.out.println("Requested lookups:");
            for (int i = 1; i < args.length; i++) {
                int id = Integer.parseInt(args[i]);
                Double w = table.weightOf(id);
                System.out.println("  " + id + " -> " + (w != null ? w : "not found"));
            }
        }
    }
}
