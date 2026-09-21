package net.stalzone.emulator.platform;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * CLI demo for {@link SteamDlcIds}. Loads the real {@code steam_dlc_ids.txt}/{@code
 * egs_dlc_ids.txt} pair and prints the real Steam AppID count plus the real EGS catalog-item id --
 * see the class doc for the live Steam Storefront API cross-check findings this data led to.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.platform.SteamDlcIdsDumpTool <steam_dlc_ids.txt> <egs_dlc_ids.txt>
 */
public final class SteamDlcIdsDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 2) {
            System.err.println("usage: SteamDlcIdsDumpTool <steam_dlc_ids.txt> <egs_dlc_ids.txt>");
            System.exit(2);
        }

        SteamDlcIds ids = SteamDlcIds.load(Path.of(args[0]), Path.of(args[1]));

        System.out.println("Real Steam DLC AppIDs: " + ids.steamAppIdCount());
        System.out.println(ids.steamAppIds());
        System.out.println();
        System.out.println("Real EGS catalog-item id: " + ids.egsCatalogItemId());
    }
}
