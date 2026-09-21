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
 * Regression tests for {@link StalcraftCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/stalcraft/} folder (part 67), live-verified on
 * device: a representative {@code ctmpatcher/ctm/} subset (incl. the real numeric-folder-named-but-
 * material-file case, {@code 3/dirt.properties}), a representative {@code sound/} subset (the 4 real
 * loose files, plus subsets of {@code step}/{@code stepmobs}/{@code im}/{@code damage}), the complete
 * real 18-file {@code models/} folder (incl. the two real prestige-ability cross-reference files), and
 * a representative {@code textures/} subset (incl. the real {@code exbo.mic}/{@code exbo_dark.mic}
 * branding find and the real 2026-dated {@code ny26_pack}/{@code dmz26_pack} cosmetics). Only real
 * game-content names are used, per this project's privacy rule -- never a real player username.
 */
public final class StalcraftCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // ctmpatcher/ctm/ -- representative subset (115 real material folders total)
            "ctmpatcher/ctm/brick_45/brick_45.properties",
            "ctmpatcher/ctm/brick_gray_mossy/brick_gray_mossy.properties",
            "ctmpatcher/ctm/metal_3630/metal_3630.properties",
            "ctmpatcher/ctm/metal_green/metal_green.properties",
            "ctmpatcher/ctm/quartz_placeholder/quartz_placeholder.properties",
            "ctmpatcher/ctm/152/152.properties",
            "ctmpatcher/ctm/3/dirt.properties",
            "ctmpatcher/ctm/3510/3510.properties",
            "ctmpatcher/ctm/3528/3528.properties",
            // sound/ -- 4 real loose files
            "sound/click.ogg",
            "sound/click_insert.ogg",
            "sound/deaf1.ogg",
            "sound/deaf_fast.ogg",
            // sound/step/ -- representative subset (621 real files total)
            "sound/step/concrete1.ogg",
            "sound/step/concrete2.ogg",
            "sound/step/glass1.ogg",
            "sound/step/ladder1.ogg",
            "sound/step/mesh_fence1.ogg",
            "sound/step/snow1.ogg",
            "sound/step/water1.ogg",
            // sound/stepmobs/ -- representative subset (189 real files total)
            "sound/stepmobs/concrete.ogg",
            "sound/stepmobs/default.ogg",
            "sound/stepmobs/grass1.ogg",
            // sound/im/ -- representative subset (132 real files total)
            "sound/im/climb1.ogg",
            "sound/im/climb_to_climb_on1.ogg",
            "sound/im/prone1.ogg",
            "sound/im/stand_to_crouch1.ogg",
            // sound/damage/ -- representative subset (87 real files total)
            "sound/damage/death1.ogg",
            "sound/damage/hit1.ogg",
            "sound/damage/fallbig1.ogg",
            "sound/damage/fallsmall1.ogg",
            // models/ -- the complete real 18-file folder
            "models/barricade.mcsb",
            "models/blue_diff.ol",
            "models/blue_nrm.ol",
            "models/blue_shelf.mcsb",
            "models/blue_shelf1.mcsb",
            "models/blue_spek.ol",
            "models/botles_d.ol",
            "models/botles_n.ol",
            "models/botles_s.ol",
            "models/bottles.mcsb",
            "models/cabinet.mcsb",
            "models/cash.mcsb",
            "models/concrete_wall.mcsb",
            "models/metal_shelf.mcsb",
            "models/mina.ol",
            "models/mine.mcsb",
            "models/stellage.mcsb",
            "models/urn.mcsb",
            // textures/ -- 1 real loose file
            "textures/empty.ol",
            // textures/gui/ -- the complete real 2-file folder
            "textures/gui/exbo.mic",
            "textures/gui/exbo_dark.mic",
            // textures/items/ -- representative subset
            "textures/items/module_assault.ol",
            "textures/items/module_sniper.ol",
            "textures/items/seasonpass.ol",
            "textures/items/quest_giver.ol",
            "textures/items/customisation/ny26_pack_1.ol",
            "textures/items/customisation/ny26_pack_8.ol",
            "textures/items/customisation/dmz26_pack_1.ol",
            "textures/items/customisation/dmz26_pack_4.ol",
            "textures/items/customisation/snegurka.ol",
            "textures/items/customisation/murzya.ol",
            "textures/items/customisation/backgrounds/bg1.ol",
            "textures/items/customisation/patterns/pattern1.ol",
            "textures/items/customisation/stickers/sticker1.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalcraftCatalog.load finds exactly the 4 real top-level folders", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"ctmpatcher", "models", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("stalcraft-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            String[] looseRootFiles = {"black_texture_placeHolder.ol", "moving.txt"};
            for (String loose : looseRootFiles) {
                Path file = dir.resolve(loose);
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            StalcraftCatalog catalog = StalcraftCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(),
                    "loose root files must not be counted");
        }));

        out.add(test("foldersContaining: a numerically-named ctm folder can still hold a material-named file (3/dirt.properties)", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("dirt").contains("ctmpatcher"), "dirt.properties under ctmpatcher");
        }));

        out.add(test("foldersContaining: barricade and mine models live under models/, cross-referencing part 66's prestige abilities", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("barricade").contains("models"), "barricade.mcsb");
            assertTrue(catalog.foldersContaining("mine.mcsb").contains("models"), "mine.mcsb");
        }));

        out.add(test("foldersContaining: real EXBO branding textures live under textures/", () -> {
            StalcraftCatalog catalog = loadFixture();
            List<String> exbo = catalog.foldersContaining("exbo");
            assertEquals(1, exbo.size(), "exbo folder count");
            assertTrue(exbo.contains("textures"), "exbo lives under textures/");
        }));

        out.add(test("foldersContaining: real 2026-dated seasonal cosmetic packs (ny26, dmz26) live under textures/", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("ny26_pack").contains("textures"), "ny26_pack");
            assertTrue(catalog.foldersContaining("dmz26_pack").contains("textures"), "dmz26_pack");
        }));

        out.add(test("foldersContaining: the real module_* weapon-class item set lives under textures/", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("module_assault").contains("textures"), "module_assault");
            assertTrue(catalog.foldersContaining("module_sniper").contains("textures"), "module_sniper");
        }));

        out.add(test("filesIn: models is the complete real 18-file fixture", () -> {
            StalcraftCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("models");
            assertEquals(18, files.size(), "models file count");
            assertTrue(files.contains("stellage.mcsb"), "stellage.mcsb present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalcraftCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalcraftCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalcraft-fixture-empty");
                dir.toFile().deleteOnExit();
                StalcraftCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalcraftCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalcraft-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalcraftCatalog.load(dir);
    }
}
