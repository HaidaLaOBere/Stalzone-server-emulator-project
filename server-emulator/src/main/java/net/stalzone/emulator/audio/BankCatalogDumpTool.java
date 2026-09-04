package net.stalzone.emulator.audio;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * CLI demo for BankCatalog, same spirit as the other *DumpTool classes. Loads the real
 * {@code banks/} folder, and -- if a logs folder is given -- cross-references real
 * {@code client-*.log} files to split the catalog into confirmed-live vs. unconfirmed.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.audio.BankCatalogDumpTool <banks dir> [logs dir]
 */
public final class BankCatalogDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: BankCatalogDumpTool <banks dir> [logs dir]");
            System.exit(2);
        }

        Path banksDir = Path.of(args[0]);
        BankCatalog catalog = BankCatalog.load(banksDir);
        System.out.println("Total real .bank files: " + catalog.size());

        if (args.length < 2) {
            return;
        }
        Path logsDir = Path.of(args[1]);
        Set<String> confirmedLive = BankCatalog.loadConfirmedLiveBankIds(logsDir);

        List<String> live = catalog.confirmedLiveBankIds(confirmedLive);
        List<String> unconfirmed = catalog.unconfirmedBankIds(confirmedLive);
        List<String> missing = catalog.confirmedLiveButMissingFromDisk(confirmedLive);

        System.out.println("Confirmed live (seen in a real client-*.log \"Loading bank\" line): " + live.size());
        System.out.println("Unconfirmed (real file, no log evidence yet): " + unconfirmed.size());
        System.out.println("Confirmed-live but missing from disk (should be 0): " + missing.size());
        if (!missing.isEmpty()) {
            System.out.println("  " + missing);
        }

        System.out.println();
        System.out.println("First 20 confirmed-live: " + live.subList(0, Math.min(20, live.size())));
    }
}
