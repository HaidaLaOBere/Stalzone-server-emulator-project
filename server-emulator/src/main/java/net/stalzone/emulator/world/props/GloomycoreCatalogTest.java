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
 * Regression tests for {@link GloomycoreCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/gloomycore/} folder (part 63),
 * live-verified on device: a representative {@code banks/} subset, the complete real 37-folder/
 * representative-subset {@code colliders/} set (including the two real unconfirmed leads {@code
 * steve} and {@code shnir}), the complete real 16-top-level-file plus a representative subfolder
 * subset of {@code fonts/}, the complete real 5-file {@code lang/}, the complete real 8-file {@code
 * models/}, a representative {@code sky/} and {@code sound/} subset, and the complete real 5-file
 * {@code textures/gui/}. Only real game-content names are used, per this project's privacy rule --
 * never a real player username.
 */
public final class GloomycoreCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // banks/ -- representative subset (474 real files total, FMOD binaries, name-only evidence)
            "banks/Master.bank",
            "banks/Master.strings.bank",
            "banks/A.bank",
            "banks/abilities.bank",
            "banks/worldEvents.bank",
            "banks/2_ahav.bank",
            "banks/2_ahav_en.bank",
            "banks/agitatsii_afon.bank",
            "banks/agitatsii_afon_en.bank",
            "banks/verbovshik_kondor.bank",
            "banks/vodovorot_sobitiy.bank",
            "banks/voennie_v_peshere.bank",
            "banks/tutorial_3.bank",
            "banks/tutorial_3_en.bank",
            // colliders/ -- real mob roster corroboration (real files are all genericaly named
            // "collider.mcvd" inside each mob's own folder -- verified directly, NOT <mobname>.mcvd)
            // + the 2 unconfirmed leads, of which "steve" turned out to hold real rich boss/
            // prototype-flavored content rather than a placeholder (see class doc)
            "colliders/boar/collider.mcvd",
            "colliders/chimera/collider.mcvd",
            "colliders/cybergiant/collider.mcvd",
            "colliders/krovosos/collider.mcvd",
            "colliders/pseudogigant/collider.mcvd",
            "colliders/rudolf/collider.mcvd",
            "colliders/tsarevich/collider.mcvd",
            "colliders/tsarevna/collider.mcvd",
            "colliders/tushkan/collider.mcvd",
            "colliders/tushkan_clone/collider.mcvd",
            "colliders/shnir/collider.mcvd",
            "colliders/steve/collider.mcvd",
            "colliders/steve/collider_forge_boss_proto.mcvd",
            "colliders/steve/collider_technar.mcvd",
            "colliders/steve/collider_test_apostle_percentdamagespot.mcvd",
            "colliders/steve/collider_test_percentdamagespot.mcvd",
            "colliders/steve/collider_heavy_murmur_25129_1.mcvd",
            // fonts/ -- complete real 16 top-level files
            "fonts/Capture_it.ttf",
            "fonts/Europe-Book-Edited.otf",
            "fonts/FuturaPT-Medium.ttf",
            "fonts/Golos_Text_Italic.ttf",
            "fonts/Golos_Text_Regular.ttf",
            "fonts/Montserrat-Bold.ttf",
            "fonts/SourceHanSansHW-VF.ttf",
            "fonts/SourceHanSansHW-VFImGui.ttf",
            "fonts/Tahoma.ttf",
            "fonts/Tahoma_Bold.ttf",
            "fonts/braille.ttf",
            "fonts/dict-ranges.txt",
            "fonts/dict.txt",
            "fonts/fa-regular-400.ttf",
            "fonts/fa-solid-900.ttf",
            "fonts/murmur.otf",
            // fonts/ -- representative subfolder subset (44 real files total across msdf/sdf/slug/ttf)
            "fonts/msdf/braille.mic",
            "fonts/msdf/Capture_IT.mic",
            "fonts/sdf/tahoma.fnt",
            "fonts/sdf/tahoma.ol",
            "fonts/slug/Capture_it.slug",
            "fonts/ttf/capture_it_16.bfnt",
            "fonts/ttf/capture_it_16_x2.0.bfnt",
            // handbook/ -- representative subset (43 real files total under img/)
            "handbook/img/anomalies/batut.mic",
            "handbook/img/anomalies/blackhole.mic",
            "handbook/img/anomalies/circus.mic",
            "handbook/img/anomalies/electra.mic",
            "handbook/img/anomalies/karousel.mic",
            "handbook/img/guide/ammo.mic",
            "handbook/img/guide/hunger.mic",
            "handbook/img/locations/agro.mic",
            "handbook/img/locations/armsklad.mic",
            "handbook/img/locations/bar.mic",
            "handbook/img/locations/bolota.mic",
            // lang/ -- the complete real 5-file folder
            "lang/en.lang",
            "lang/es.lang",
            "lang/fr.lang",
            "lang/ko.lang",
            "lang/ru.lang",
            // models/ -- the complete real 8-file folder
            "models/.mayaSwatches/steve.dds_hcm.swatch",
            "models/jump_spot/jump_spot.mcsb",
            "models/jump_spot/jump_spot.ol",
            "models/spawn_spot/material_spot.mcsb",
            "models/spawn_spot/spawn_spot.mcsb",
            "models/wepoint/aipoint.ol",
            "models/wepoint/wb.ol",
            "models/wepoint/wepoint.mcsb",
            // sky/ -- representative subset (101 real files total)
            "sky/halloween2022/halloween2022.ol",
            "sky/halloween2022/halloween2022.properties",
            "sky/mogilnik/black.properties",
            "sky/mogilnik/day.properties",
            "sky/fake_ejection/fake_ejection.properties",
            "sky/skybox/Day_ClearSky_01.ol",
            "sky/skybox/Day_Cloudy_01.ol",
            "sky/terrain_depth.ol",
            "sky/terrain_mask.ol",
            // sound/ -- representative subset (104 real files total)
            "sound/click.ogg",
            "sound/click_insert.ogg",
            "sound/deaf1.ogg",
            "sound/deaf15.ogg",
            "sound/damage/death1.ogg",
            "sound/damage/death25.ogg",
            // textures/gui/ -- the complete real 5-file folder
            "textures/gui/buttons.mic",
            "textures/gui/daily_reward.mic",
            "textures/gui/hud_cooldown.mic",
            "textures/gui/inventory.mic",
            "textures/gui/widgets.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("GloomycoreCatalog.load finds exactly the 9 real top-level folders", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertEquals(9, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"banks", "colliders", "fonts", "handbook", "lang", "models", "sky", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("gloomycore-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            String[] looseRootFiles = {"RU_ru.txt", "egs_dlc_ids.txt", "steam_dlc_ids.txt", "mcmtlList.txt"};
            for (String loose : looseRootFiles) {
                Path file = dir.resolve(loose);
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            GloomycoreCatalog catalog = GloomycoreCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(),
                    "loose root files must not be counted");
        }));

        out.add(test("foldersContaining: mob roster corroboration -- rudolf, tsarevich, tushkan resolve to colliders", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("rudolf").contains("colliders"), "rudolf");
            assertTrue(catalog.foldersContaining("tsarevich").contains("colliders"), "tsarevich");
            assertTrue(catalog.foldersContaining("tushkan").contains("colliders"), "tushkan");
        }));

        out.add(test("foldersContaining: the two unconfirmed leads (steve, shnir) are real colliders entries", () -> {
            GloomycoreCatalog catalog = loadFixture();
            List<String> steve = catalog.foldersContaining("steve");
            assertEquals(2, steve.size(), "steve appears under both colliders/ and models/ (.mayaSwatches)");
            assertTrue(steve.contains("colliders"), "steve under colliders");
            assertTrue(steve.contains("models"), "steve under models (.mayaSwatches)");
            List<String> shnir = catalog.foldersContaining("shnir");
            assertEquals(1, shnir.size(), "shnir folder count");
            assertTrue(shnir.contains("colliders"), "shnir under colliders");
        }));

        out.add(test("foldersContaining: halloween2022 is a real seasonal-event sky preset", () -> {
            GloomycoreCatalog catalog = loadFixture();
            List<String> halloween = catalog.foldersContaining("halloween2022");
            assertEquals(1, halloween.size(), "halloween2022 folder count");
            assertTrue(halloween.contains("sky"), "halloween2022 lives under sky");
        }));

        out.add(test("foldersContaining: deaf ringing-effect sounds and death sounds both live under sound", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("deaf1").contains("sound"), "deaf1");
            assertTrue(catalog.foldersContaining("death1").contains("sound"), "death1");
        }));

        out.add(test("foldersContaining: SourceHanSansHW corroborates CJK/Korean font coverage under fonts", () -> {
            GloomycoreCatalog catalog = loadFixture();
            List<String> han = catalog.foldersContaining("sourcehansans");
            assertEquals(1, han.size(), "SourceHanSansHW folder count (case-insensitive)");
            assertTrue(han.contains("fonts"), "SourceHanSansHW lives under fonts");
        }));

        out.add(test("filesIn: textures/gui is the sole real textures/ content, all 5 files", () -> {
            GloomycoreCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("textures");
            assertEquals(5, files.size(), "textures file count");
            assertTrue(files.contains("gui/buttons.mic"), "buttons.mic present");
            assertTrue(files.contains("gui/widgets.mic"), "widgets.mic present");
        }));

        out.add(test("filesIn: lang holds exactly the real 5-locale set", () -> {
            GloomycoreCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("lang");
            assertEquals(5, files.size(), "lang file count");
            for (String locale : new String[]{"en.lang", "es.lang", "fr.lang", "ko.lang", "ru.lang"}) {
                assertTrue(files.contains(locale), locale + " present");
            }
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            GloomycoreCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("GloomycoreCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("gloomycore-fixture-empty");
                dir.toFile().deleteOnExit();
                GloomycoreCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static GloomycoreCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("gloomycore-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return GloomycoreCatalog.load(dir);
    }
}
