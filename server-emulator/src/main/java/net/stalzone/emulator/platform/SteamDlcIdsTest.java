package net.stalzone.emulator.platform;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link SteamDlcIds}, using the real 24 Steam AppIDs and the real EGS
 * catalog-item id read directly from the real {@code gloomycore/steam_dlc_ids.txt}/{@code
 * egs_dlc_ids.txt} files (part 63), live-verified on device. These are public store-platform
 * identifiers, not personal data, so used verbatim per this project's privacy rule.
 */
public final class SteamDlcIdsTest {

    private static final String[] REAL_STEAM_APP_IDS = {
            "2490360", "2659320", "2659310", "2659300", "2659290", "2659280",
            "2742650", "2742670", "2742680", "2742690", "2742700",
            "2898540", "2898550", "2898560",
            "3086480", "3086490", "3086470", "3086460",
            "3291560", "3291570",
            "3403640", "3403650",
            "4207960", "4908670",
    };

    private static final String REAL_EGS_ID = "2b1a4a2aa9e1485cb007d62e3f3965f8";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("SteamDlcIds.load reads exactly the real 24 Steam AppIDs", () -> {
            SteamDlcIds ids = loadFixture();
            assertEquals(24, ids.steamAppIdCount(), "Steam AppID count");
            assertEquals(REAL_STEAM_APP_IDS.length, ids.steamAppIds().size(), "fixture/loaded size match");
        }));

        out.add(test("hasSteamAppId: real IDs confirmed via a live Steam Storefront API check are present", () -> {
            SteamDlcIds ids = loadFixture();
            // 4908670 = "STALZONE -- Memories of STALCRAFT: X", real direct evidence of the rename itself.
            assertTrue(ids.hasSteamAppId("4908670"), "4908670 (Memories of STALCRAFT: X) present");
            // 2490360 = "STALCRAFT: X Crow Arena 2023 Battle Pack", real evidence of pre-rename DLC history.
            assertTrue(ids.hasSteamAppId("2490360"), "2490360 (Crow Arena 2023 Battle Pack) present");
            assertTrue(ids.hasSteamAppId("4207960"), "4207960 (3rd Anniversary) present");
        }));

        out.add(test("hasSteamAppId: an unrelated id is genuinely absent", () -> {
            SteamDlcIds ids = loadFixture();
            assertTrue(!ids.hasSteamAppId("1818450"), "the base game's own AppID is not in the DLC list");
            assertTrue(!ids.hasSteamAppId("0"), "id 0 is not present");
        }));

        out.add(test("egsCatalogItemId: real 32-char hex GUID read verbatim, no dashes", () -> {
            SteamDlcIds ids = loadFixture();
            assertEquals(REAL_EGS_ID, ids.egsCatalogItemId(), "EGS catalog-item id");
            assertEquals(32, ids.egsCatalogItemId().length(), "EGS id length");
        }));

        out.add(test("SteamDlcIds.load rejects a Steam ids file with no non-blank lines", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("steam-dlc-ids-fixture-empty");
                dir.toFile().deleteOnExit();
                Path steamFile = dir.resolve("steam_dlc_ids.txt");
                Files.writeString(steamFile, "\n\n");
                steamFile.toFile().deleteOnExit();
                Path egsFile = dir.resolve("egs_dlc_ids.txt");
                Files.writeString(egsFile, REAL_EGS_ID);
                egsFile.toFile().deleteOnExit();
                SteamDlcIds.load(steamFile, egsFile);
            }, "blank-only steam ids file");
        }));

        out.add(test("SteamDlcIds.load rejects an empty EGS id file", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("steam-dlc-ids-fixture-empty-egs");
                dir.toFile().deleteOnExit();
                Path steamFile = dir.resolve("steam_dlc_ids.txt");
                Files.writeString(steamFile, String.join("\n", REAL_STEAM_APP_IDS));
                steamFile.toFile().deleteOnExit();
                Path egsFile = dir.resolve("egs_dlc_ids.txt");
                Files.writeString(egsFile, "");
                egsFile.toFile().deleteOnExit();
                SteamDlcIds.load(steamFile, egsFile);
            }, "empty egs id file");
        }));

        return out;
    }

    private static SteamDlcIds loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("steam-dlc-ids-fixture");
        dir.toFile().deleteOnExit();
        Path steamFile = dir.resolve("steam_dlc_ids.txt");
        Files.writeString(steamFile, String.join("\n", REAL_STEAM_APP_IDS) + "\n");
        steamFile.toFile().deleteOnExit();
        Path egsFile = dir.resolve("egs_dlc_ids.txt");
        Files.writeString(egsFile, REAL_EGS_ID + "\n");
        egsFile.toFile().deleteOnExit();
        return SteamDlcIds.load(steamFile, egsFile);
    }
}
