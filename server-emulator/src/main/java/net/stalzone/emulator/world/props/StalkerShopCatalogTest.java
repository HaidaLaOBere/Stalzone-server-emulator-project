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
 * Regression tests for {@link StalkerShopCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/shop/} folder (part 73), live-verified on
 * device: a representative subset of each of the 3 real top-level folders ({@code models}, {@code
 * sound}, {@code textures}), plus the real loose root-level files that {@code load}'s folder-only walk
 * deliberately does not see. Only real game-content names are used, per this project's privacy rule --
 * never a real player username.
 */
public final class StalkerShopCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- representative real case-type subfolders + loose faction/seasonal files
            "models/case_bp2024/a_box_for_coupons_cheap_Diffuse.ol",
            "models/case_bp2024/a_box_for_coupons_rich_Diffuse.ol",
            "models/case_bp2025/summer25_box_hideout.mcsb",
            "models/case_bp2025/summer25_tickets.mcsb",
            "models/case_collector/case_collector.mcsb",
            "models/case_collector/case_collector_diff.ol",
            "models/case_functional/case_functional.mcsb",
            "models/case_functional/autumn25/case_diff.ol",
            "models/case_long/case.mcsb",
            "models/case_long/frontline_1/ssop_1_diff.ol",
            "models/case_long/frontline_2/ssop_2_diff.ol",
            "models/case_long/frontline_3/ssop_3_diff.ol",
            "models/case_long/frontline_4/ssop_4_diff.ol",
            "models/case_long/divisions/divisions_diff.ol",
            "models/case_long/divisions/divisions_nrm.ol",
            "models/case_long/divisions/divisions_spek.ol",
            "models/case_long/test/Cont_2_BaseColor.ol",
            "models/case_ny25/case1_diff.ol",
            "models/case_ny25/case2_diff.ol",
            "models/case_ops/case_ops.mcsb",
            "models/case_ops/Case_oper_Diffuse.ol",
            "models/case_plastik/case_plastic_diff.ol",
            "models/case_plastik/case_plastik_nrm.ol",
            "models/case_styles/case_styles.mcsb",
            "models/case_twitch/case_twitch.mcsb",
            "models/case_twitch/twitch_diff.ol",
            "models/case_wooden/case_wooden.mcsb",
            "models/case_wooden/case_wood_nrm.ol",
            "models/caseny24/case1_diff.ol",
            "models/caseny24/case2_diff.ol",
            "models/podarki/podarok1.mcsb",
            "models/podarki/podarok2.mcsb",
            "models/podarki/podarok3.mcsb",
            "models/podarki/podarok4.mcsb",
            "models/podarki/podarok5.mcsb",
            "models/faction_covenant.ol",
            "models/faction_duty.ol",
            "models/faction_freedom.ol",
            "models/faction_merc.ol",
            "models/faction_top.ol",
            "models/faction_weapon.ol",
            "models/halloween.ol",
            "models/hw23.ol",
            "models/blizzard.ol",
            "models/ravent.ol",
            "models/parts.ol",
            "models/case_plastic.mcsb",
            "models/case_wooden.mcsb",
            // sound/ -- representative real dated event-case subfolders + treasury minigame
            "sound/cases/case_open_level_1_.ogg",
            "sound/cases/case_twitch_drop_opening.ogg",
            "sound/cases/roulette_spin.ogg",
            "sound/cases/roulette_stop.ogg",
            "sound/cases/autumn26/autumn_25_caseopen_1_.ogg",
            "sound/cases/blizzard23/drop_wooden.ogg",
            "sound/cases/dmz/dmz_item_1_.ogg",
            "sound/cases/dmz/dmz_item_6_.ogg",
            "sound/cases/electrostorm/drop_metallic.ogg",
            "sound/cases/ny24/drop_wooden_drawer_big.ogg",
            "sound/cases/operationscase25/drop_plastic.ogg",
            "sound/cases/ravent23/drop_wooden_drawer_small.ogg",
            "sound/cases/reichstag26/reichstag_2026_case_open_level_1_.ogg",
            "sound/cases/reichstag26/reichstag_2026_case_open_level_6_.ogg",
            "sound/cases/spring25/exclusive_gear_spring25_drop.ogg",
            "sound/cases/spring26/icon_appearance1.ogg",
            "sound/cases/summer26/lootbox_summer25_tickets.ogg",
            "sound/itempacks/equipment_ammunition_popup.ogg",
            "sound/treasury/cards_reroll.ogg",
            "sound/treasury/cards_eliminate.ogg",
            "sound/treasury/cards_select_shops_open_gold.ogg",
            "sound/treasury/cards_select_shops_open_silver.ogg",
            // textures/ -- ads + gui background rotation
            "textures/ads/wow.mic",
            "textures/gui/total_atlas.mic",
            "textures/gui/background/base_fhd.mic",
            "textures/gui/background/highlight_fhd.mic",
            "textures/gui/background/autumn26/base_fhd.mic",
            "textures/gui/background/dmz/base_fhd.mic",
            "textures/gui/background/divisions/base_fhd.mic",
            "textures/gui/background/frontline/base_fhd.mic",
            "textures/gui/background/newyear/base_fhd.mic",
            "textures/gui/background/reichstag/base_fhd.mic",
            "textures/gui/background/winter26/base_fhd.mic",
    };

    /** Real loose root-level files -- NOT indexed by the folder-only {@code load} walk. */
    private static final String[] LOOSE_ROOT_FILES = {
            "box1.mcsb",
            "box2.mcsb",
            "box_diff.ol",
            "box_nrm.ol",
            "box_spek.ol",
            "box_jiden_diff.ol",
            "box_jiden_nrm.ol",
            "box_jiden_spek.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerShopCatalog.load finds exactly the 3 real top-level folders", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertEquals(3, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("foldersContaining: the 13 real case-type names resolve under models", () -> {
            StalkerShopCatalog catalog = loadFixture();
            for (String caseName : new String[]{
                    "case_bp2024", "case_bp2025", "case_collector", "case_functional", "case_long",
                    "case_ny25", "case_ops", "case_plastik", "case_styles", "case_twitch",
                    "case_wooden", "caseny24", "podarki",
            }) {
                assertTrue(catalog.foldersContaining(caseName).contains("models"), caseName);
            }
        }));

        out.add(test("foldersContaining: real S.T.A.L.K.E.R. faction names (duty, freedom) resolve under models", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("faction_duty").contains("models"), "faction_duty");
            assertTrue(catalog.foldersContaining("faction_freedom").contains("models"), "faction_freedom");
        }));

        out.add(test("foldersContaining: reichstag26 is a sixth independent Reichstag-event corroboration", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("reichstag_2026").contains("sound"), "reichstag_2026");
        }));

        out.add(test("foldersContaining: case_twitch_drop_opening cross-references the real case_twitch model folder", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("case_twitch").contains("models"), "case_twitch under models");
            assertTrue(catalog.foldersContaining("twitch_drop_opening").contains("sound"), "twitch_drop_opening under sound");
        }));

        out.add(test("foldersContaining: frontline/divisions resolve under both textures (background art) and models (real case-skin tiers, case_long/)", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("frontline").contains("textures"), "frontline under textures");
            assertTrue(catalog.foldersContaining("divisions").contains("textures"), "divisions under textures");
            assertTrue(catalog.foldersContaining("frontline").contains("models"), "frontline under models");
            assertTrue(catalog.foldersContaining("divisions").contains("models"), "divisions under models");
        }));

        out.add(test("foldersContaining: case_long's real leftover test/ dev artifact is a fresh cut-content lead", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("Cont_2_BaseColor").contains("models"), "Cont_2_BaseColor");
        }));

        out.add(test("filesIn: podarki is the complete real 5-file gift-box fixture", () -> {
            StalkerShopCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("models");
            long podarokCount = files.stream().filter(f -> f.startsWith("podarki/podarok")).count();
            assertEquals(5, (int) podarokCount, "podarok model count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            StalkerShopCatalog catalog = loadFixtureWithLooseRootFiles();
            for (String looseFile : LOOSE_ROOT_FILES) {
                assertTrue(catalog.foldersContaining(looseFile).isEmpty(), looseFile + " not indexed");
            }
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count unaffected by loose root files");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerShopCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerShopCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkershop-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerShopCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerShopCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkershop-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerShopCatalog.load(dir);
    }

    private static StalkerShopCatalog loadFixtureWithLooseRootFiles() throws Exception {
        Path dir = Files.createTempDirectory("stalkershop-fixture-loose");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        for (String looseFile : LOOSE_ROOT_FILES) {
            Path file = dir.resolve(looseFile);
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerShopCatalog.load(dir);
    }
}
