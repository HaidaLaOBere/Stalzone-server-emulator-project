package net.stalzone.emulator.economy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for VendorArchive, same spirit as the other *DumpTool classes. Loads a real
 * config/vendor_archive.dat, prints its size/currencies/traders, per-character purchase counts,
 * and (if an item id is given) every price actually paid for that item.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.economy.VendorArchiveDumpTool <vendor_archive.dat> [itemId]
 */
public final class VendorArchiveDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: VendorArchiveDumpTool <vendor_archive.dat> [itemId]");
            System.exit(2);
        }

        Path archivePath = Path.of(args[0]);
        VendorArchive archive = VendorArchive.load(archivePath);
        System.out.println("Loaded " + archive.size() + " purchase records from " + archivePath);
        System.out.println("Characters: " + archive.usernames());
        System.out.println("Currencies: " + archive.currencies());
        System.out.println("Trader ids: " + archive.traderIds());

        System.out.println();
        for (VendorArchive.CharacterArchive ca : archive.archives()) {
            System.out.println(ca.username() + ": " + ca.allPurchases().size() + " purchases across "
                    + ca.traderIds().size() + " traders " + ca.traderIds());
        }

        if (args.length > 1) {
            int itemId = Integer.parseInt(args[1]);
            List<Long> prices = archive.pricesPaidFor(itemId);
            System.out.println();
            System.out.println("Prices paid for item id " + itemId + " (" + prices.size() + " purchases): " + prices);
        }
    }
}
