package net.stalzone.emulator.economy;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for {@link BarterItemCatalog}. Loads the real
 * {@code .../textures/items/barter} folder and prints every zone, its real categories, and
 * the standalone/numeric-id items found alongside them.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.economy.BarterItemCatalogDumpTool <barterDir>
 */
public final class BarterItemCatalogDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: BarterItemCatalogDumpTool <barterDir>");
            System.exit(2);
        }

        Path dir = Path.of(args[0]);
        BarterItemCatalog catalog = BarterItemCatalog.load(dir);

        System.out.println("Loaded " + catalog.totalFileCount() + " real barter item file(s) from " + dir);
        System.out.println(catalog.zoneCount() + " real zone prefix(es):");
        for (String zone : catalog.zoneNames()) {
            List<String> categories = catalog.categoriesFor(zone);
            System.out.println("  " + zone + " (" + categories.size() + "): " + String.join(", ", categories));
        }

        System.out.println();
        System.out.println("Standalone (non-zone-scoped) item(s): " + catalog.standaloneItemNames());
        System.out.println("Bare numeric-id item(s): " + catalog.numericIdItemNames());
    }
}
