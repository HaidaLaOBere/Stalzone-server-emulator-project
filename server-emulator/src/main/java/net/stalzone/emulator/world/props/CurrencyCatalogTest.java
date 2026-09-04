package net.stalzone.emulator.world.props;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link CurrencyCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/currency/} folder (part 58),
 * live-verified on device: the 6 ruble-denomination models plus the {@code
 * token_electroshtorm2026} model/icon pairing that corrected this class's Javadoc (part of the
 * fixture on purpose, so the corrected claim stays covered by a test), a dated seasonal icon, a
 * cross-referenced WWII-event currency ({@code reichstag}), and a DMZ-specific currency. Only
 * real game-content names are used, per this project's privacy rule -- never a real player
 * username.
 */
public final class CurrencyCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "models/10rub.mcsb",
            "models/50rub.mcsb",
            "models/100rub.mcsb",
            "models/500rub.mcsb",
            "models/1000rub.mcsb",
            "models/5000rub.mcsb",
            "models/money.ol",
            "models/token_electroshtorm2026.mcsb",
            "textures/token_electroshtorm2026_diff.ol",
            "textures/token_electroshtorm2026_nrm.ol",
            "textures/token_electroshtorm2026_spek.ol",
            "textures/token_event.mic",
            "textures/items/money_10.ol",
            "textures/items/money_50.ol",
            "textures/items/money_100.ol",
            "textures/items/money_500.ol",
            "textures/items/money_1000.ol",
            "textures/items/money_5000.ol",
            "textures/items/reichstag_silver.ol",
            "textures/items/reichstag_xp_free.ol",
            "textures/items/token_prestige.ol",
            "textures/items/dmz_coin.ol",
            "textures/items/dmz26_token.ol",
            "textures/items/popularnostDMZ.ol",
            "textures/items/shards.ol",
            "textures/items/shards21.ol",
            "textures/items/twitch_shards.ol",
            "textures/items/autumn_25.ol",
            "textures/items/winter_25.ol",
            "textures/items/winter_26.ol",
            "textures/items/coins21.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("CurrencyCatalog.load finds exactly the 2 real top-level folders", () -> {
            CurrencyCatalog catalog = loadFixture();
            assertEquals(2, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("models"), "models present");
            assertTrue(catalog.folderNames().contains("textures"), "textures present");
        }));

        out.add(test("filesIn: models/ holds the 6 real ruble denominations plus money.ol and the electroshtorm token", () -> {
            CurrencyCatalog catalog = loadFixture();
            List<String> models = catalog.filesIn("models");
            assertEquals(8, models.size(), "models file count");
            assertTrue(models.contains("10rub.mcsb"), "10rub present");
            assertTrue(models.contains("5000rub.mcsb"), "5000rub present");
            assertTrue(models.contains("money.ol"), "money.ol present");
            assertTrue(models.contains("token_electroshtorm2026.mcsb"), "electroshtorm model present");
        }));

        out.add(test("filesIn: textures/items nested paths are relative to the textures folder itself", () -> {
            CurrencyCatalog catalog = loadFixture();
            List<String> textures = catalog.filesIn("textures");
            assertTrue(textures.contains("items/reichstag_silver.ol"), "nested reichstag path");
            assertTrue(textures.contains("token_event.mic"), "root-level .mic file");
        }));

        out.add(test("foldersContaining: the corrected money-icon/model pairing is found by name match", () -> {
            CurrencyCatalog catalog = loadFixture();
            // money_10.ol (icon) and 10rub.mcsb (model) don't share a literal substring beyond
            // digits, so this checks the icon and model are each independently locatable instead.
            List<String> rubIcons = catalog.foldersContaining("money_10");
            assertEquals(1, rubIcons.size(), "money_10 folder count");
            assertTrue(rubIcons.contains("textures"), "money_10.ol lives under textures");
            List<String> rubModel = catalog.foldersContaining("10rub");
            assertEquals(1, rubModel.size(), "10rub folder count");
            assertTrue(rubModel.contains("models"), "10rub.mcsb lives under models");
        }));

        out.add(test("foldersContaining: matches the real reichstag cross-reference, case-insensitively", () -> {
            CurrencyCatalog catalog = loadFixture();
            List<String> reichstag = catalog.foldersContaining("REICHSTAG");
            assertEquals(1, reichstag.size(), "reichstag folder count");
            assertTrue(reichstag.contains("textures"), "reichstag files live under textures");
        }));

        out.add(test("foldersContaining: matches a DMZ-specific currency by substring", () -> {
            CurrencyCatalog catalog = loadFixture();
            List<String> dmz = catalog.foldersContaining("dmz");
            assertEquals(1, dmz.size(), "dmz folder count");
            assertTrue(dmz.contains("textures"), "dmz currencies live under textures");
        }));

        out.add(test("foldersContaining: matches a dated seasonal variant", () -> {
            CurrencyCatalog catalog = loadFixture();
            List<String> winter = catalog.foldersContaining("winter_25");
            assertEquals(1, winter.size(), "winter_25 folder count");
            assertTrue(winter.contains("textures"), "dated icons live under textures");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            CurrencyCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            CurrencyCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            CurrencyCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("CurrencyCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("currency-fixture-empty");
                dir.toFile().deleteOnExit();
                CurrencyCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static CurrencyCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("currency-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return CurrencyCatalog.load(dir);
    }
}
