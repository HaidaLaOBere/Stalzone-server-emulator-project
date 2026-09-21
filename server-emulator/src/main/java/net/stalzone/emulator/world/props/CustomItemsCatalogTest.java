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
 * Regression tests for {@link CustomItemsCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/customitems/} folder (part 70), live-verified on
 * device: representative subsets of {@code models/blocks/}'s standout cross-reference subfolders
 * (barricade, prestige, reichstag, zipline, teleport, z_old_models, quest_botwin, test_iznanka), the 3
 * real loose {@code models/} files, a {@code models/items/} subset, and a {@code textures/} subset.
 * Only real game-content names are used, per this project's privacy rule -- never a real player
 * username.
 */
public final class CustomItemsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- 3 real loose files
            "models/btr.ol",
            "models/sharek.mcsb",
            "models/untitled.mcsb",
            // models/blocks/barricade/ -- representative subset (38 real files total)
            "models/blocks/barricade/barekad.mcsb",
            "models/blocks/barricade/barekad_tess1.mcsb",
            "models/blocks/barricade/barikada_army.mcsb",
            "models/blocks/barricade/barikada_army_2.mcsb",
            // models/blocks/prestige/ -- representative subset (68 real files total)
            "models/blocks/prestige/battery.mcsb",
            "models/blocks/prestige/charger.mcsb",
            "models/blocks/prestige/charger_working_emi.ol",
            "models/blocks/prestige/discharger.mcsb",
            // models/blocks/reichstag/ -- representative subset (43 real files total)
            "models/blocks/reichstag/sand_wall_1_2.mcsb",
            "models/blocks/reichstag/sand_wall_1_diff.ol",
            "models/blocks/reichstag/sand_wall_2_2.mcsb",
            // models/blocks/zipline/ -- the complete real 6-file folder
            "models/blocks/zipline/Carabine.mcsb",
            "models/blocks/zipline/CarabineFull.mcsb",
            "models/blocks/zipline/carabine_BaseColor.ol",
            "models/blocks/zipline/carabine_Normal.ol",
            "models/blocks/zipline/Carbine.mcsb",
            // models/blocks/teleport/ -- the complete real 1-file folder
            "models/blocks/teleport/block.mcsb",
            // models/blocks/z_old_models/ -- representative subset (177 real files total)
            "models/blocks/z_old_models/bathroom/bathroom_1.mcsb",
            "models/blocks/z_old_models/freski_old/fresk1.ol",
            "models/blocks/z_old_models/tablichki_old/tablichka1.ol",
            "models/blocks/z_old_models/vodokachka/vodokachka.mcsb",
            // models/blocks/quest_botwin/ -- representative subset (128 real files total)
            "models/blocks/quest_botwin/Industrial_Steam_Generator_BaseColor.ol",
            "models/blocks/quest_botwin/afvtoklav_BaseColor.ol",
            // models/blocks/test_iznanka/ -- the complete real 5-file folder
            "models/blocks/test_iznanka/Base_BaseColor.ol",
            "models/blocks/test_iznanka/Base_Emissive.ol",
            "models/blocks/test_iznanka/Base_Normal.ol",
            "models/blocks/test_iznanka/tank_test.mcsb",
            "models/blocks/test_iznanka/zarya_test.mcsb",
            // models/items/ -- representative subset (1,707 real files total)
            "models/items/antidot_item.mcsb",
            "models/items/antirad_item.mcsb",
            "models/items/bint.mcsb",
            "models/items/bint2.mcsb",
            "models/items/boar_head.mcsb",
            "models/items/barter/barter_item_1.mcsb",
            // models/trace/ -- representative subset (58 real files total)
            "models/trace/claymore.mcvd",
            "models/trace/barekad.mcvd",
            // models/wire/ -- the complete real 6-file folder
            "models/wire/wire.mcsb",
            "models/wire/wire.mcvd",
            "models/wire/wire10.mcsb",
            "models/wire/wire3.mcsb",
            "models/wire/wire6.mcsb",
            "models/wire/wire_new.ol",
            // textures/zipline/ -- representative subset (5 real files total)
            "textures/zipline/carabine_texture_1.ol",
            // textures/curve/ -- representative subset (3 real files total)
            "textures/curve/curve_1.ol",
            // textures/items/ -- representative subset
            "textures/items/item_icon_1.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("CustomItemsCatalog.load finds exactly the 2 real top-level folders", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertEquals(2, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("customitems-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            Path loose = dir.resolve("empty.txt");
            Files.writeString(loose, "");
            loose.toFile().deleteOnExit();
            CustomItemsCatalog catalog = CustomItemsCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "loose root file must not be counted");
        }));

        out.add(test("foldersContaining: barricade models give part 66's prestige ability a real placeable prop", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("barikada_army").contains("models"), "barikada_army");
        }));

        out.add(test("foldersContaining: reichstag sandbag walls are a fourth independent event corroboration", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("sand_wall").contains("models"), "sand_wall");
        }));

        out.add(test("foldersContaining: zipline's real carabiner-clip prop resolves under models", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("Carabine").contains("models"), "Carabine");
        }));

        out.add(test("foldersContaining: the real z_old_models deprecated furniture set resolves under models", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("vodokachka").contains("models"), "vodokachka");
        }));

        out.add(test("foldersContaining: test_iznanka cross-references part 68's insideout NPC-faction lead", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("zarya_test").contains("models"), "zarya_test");
        }));

        out.add(test("filesIn: wire is the complete real 6-file fixture", () -> {
            CustomItemsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("models");
            long wireCount = files.stream().filter(f -> f.startsWith("wire/")).count();
            assertEquals(6, (int) wireCount, "wire file count");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            CustomItemsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("CustomItemsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("customitems-fixture-empty");
                dir.toFile().deleteOnExit();
                CustomItemsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static CustomItemsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("customitems-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return CustomItemsCatalog.load(dir);
    }
}
