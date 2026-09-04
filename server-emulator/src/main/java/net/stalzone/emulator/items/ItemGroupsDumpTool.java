package net.stalzone.emulator.items;

import java.io.IOException;
import java.nio.file.Path;

/**
 * CLI demo for ItemGroups, same spirit as the other *DumpTool classes. Loads the real
 * modassets/assets/bundle/item_groups.txt, prints every group name with its entry count, and
 * (if item ids are given on the command line) which group(s) each id belongs to.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.items.ItemGroupsDumpTool <item_groups.txt> [itemId...]
 */
public final class ItemGroupsDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: ItemGroupsDumpTool <item_groups.txt> [itemId...]");
            System.exit(2);
        }

        Path itemGroupsTxt = Path.of(args[0]);
        ItemGroups groups = ItemGroups.load(itemGroupsTxt);
        System.out.println("Loaded " + groups.size() + " item groups from " + itemGroupsTxt);
        System.out.println();
        for (String name : groups.groupNames()) {
            System.out.println("  " + name + " (" + groups.rangesOf(name).size() + " range/id entries)");
        }

        if (args.length > 1) {
            System.out.println();
            System.out.println("Requested lookups:");
            for (int i = 1; i < args.length; i++) {
                int id = Integer.parseInt(args[i]);
                System.out.println("  " + id + " -> " + groups.groupsOf(id));
            }
        }
    }
}
