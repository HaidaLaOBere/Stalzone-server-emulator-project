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
 * Regression tests for {@link GameObjectsCatalog}, using a fixture directory tree that is the COMPLETE
 * real {@code modassets/assets/gameobjects/} folder (part 74, 80 files, small enough to fixture in
 * full), live-verified on device. Only real game-content names are used, per this project's privacy
 * rule -- never a real player username.
 */
public final class GameObjectsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- the complete real 68-file folder
            // 8 real battle-royale loot-crate categories, 5 files each = 40
            "models/br_ammo/br_ammo.mcsb",
            "models/br_ammo/br_ammo_diff.ol",
            "models/br_ammo/br_ammo_emi.ol",
            "models/br_ammo/br_ammo_nrm.ol",
            "models/br_ammo/br_ammo_spek.ol",
            "models/br_armor1lvl/br_armor1lvl.mcsb",
            "models/br_armor1lvl/br_armor1lvl_diff.ol",
            "models/br_armor1lvl/br_armor1lvl_emi.ol",
            "models/br_armor1lvl/br_armor1lvl_nrm.ol",
            "models/br_armor1lvl/br_armor1lvl_spek.ol",
            "models/br_armor2lvl/br_armor2lvl.mcsb",
            "models/br_armor2lvl/br_armor2lvl_diff.ol",
            "models/br_armor2lvl/br_armor2lvl_emi.ol",
            "models/br_armor2lvl/br_armor2lvl_nrm.ol",
            "models/br_armor2lvl/br_armor2lvl_spek.ol",
            "models/br_build/br_build.mcsb",
            "models/br_build/br_build_diff.ol",
            "models/br_build/br_build_emi.ol",
            "models/br_build/br_build_nrm.ol",
            "models/br_build/br_build_spek.ol",
            "models/br_med/br_med.mcsb",
            "models/br_med/br_med_diff.ol",
            "models/br_med/br_med_emi.ol",
            "models/br_med/br_med_nrm.ol",
            "models/br_med/br_med_spek.ol",
            "models/br_other/br_other.mcsb",
            "models/br_other/br_other_diff.ol",
            "models/br_other/br_other_emi.ol",
            "models/br_other/br_other_nrm.ol",
            "models/br_other/br_other_spek.ol",
            "models/br_weapon1lvl/br_weapon1lvl.mcsb",
            "models/br_weapon1lvl/br_weapon1lvl_diff.ol",
            "models/br_weapon1lvl/br_weapon1lvl_emi.ol",
            "models/br_weapon1lvl/br_weapon1lvl_nrm.ol",
            "models/br_weapon1lvl/br_weapon1lvl_spek.ol",
            "models/br_weapon2lvl/br_weapon2lvl.mcsb",
            "models/br_weapon2lvl/br_weapon2lvl_diff.ol",
            "models/br_weapon2lvl/br_weapon2lvl_emi.ol",
            "models/br_weapon2lvl/br_weapon2lvl_nrm.ol",
            "models/br_weapon2lvl/br_weapon2lvl_spek.ol",
            "models/quest_placement/quest_placement.mcsb",
            "models/switches/test_switch.mcsb",
            // 26 loose files directly in models/
            "models/balk.mcsb",
            "models/dekor_tipa.mcsb",
            "models/dist2.ol",
            "models/marker.ol",
            "models/plane.ol",
            "models/plane_door.mcsb",
            "models/plane_okno1.mcsb",
            "models/plane_okno2.mcsb",
            "models/plane_stena.mcsb",
            "models/plane_stena2.mcsb",
            "models/projector.mcsb",
            "models/projector_diffuse.ol",
            "models/puh1.mcsb",
            "models/puh1_tess1.mcsb",
            "models/puh2.mcsb",
            "models/puh2_tess1.mcsb",
            "models/puh3.mcsb",
            "models/puh3_tess1.mcsb",
            "models/puh_new1.mcsb",
            "models/puh_new2.mcsb",
            "models/red_shar.mcsb",
            "models/red_shar_diff.ol",
            "models/red_shar_emis.ol",
            "models/red_shar_nrm.mic",
            "models/sphere.mcsb",
            "models/sun.mcsb",
            // textures/ -- the complete real 11-file folder
            "textures/patch/frozen_diffuse.ol",
            "textures/patch/frozen_gloss.ol",
            "textures/patch/frozen_noise.ol",
            "textures/patch/frozen_normal.ol",
            "textures/patch/snowed_diffuse.ol",
            "textures/patch/snowed_gloss.ol",
            "textures/patch/snowed_noise.ol",
            "textures/patch/snowed_normal.ol",
            "textures/puh/puh_diff.ol",
            "textures/puh/puh_normal.ol",
            "textures/puh_new/puh_new_diff.ol",
            // trace/ -- the complete real 1-file folder
            "trace/Zabor_betonnyi_dyra_5852.mcvd",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("GameObjectsCatalog.load finds exactly the 3 real top-level folders", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertEquals(3, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "textures", "trace"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (matches the real 80-file total)", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(80, catalog.totalFileCount(), "real total file count");
        }));

        out.add(test("filesIn: models is the complete real 68-file folder", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertEquals(68, catalog.filesIn("models").size(), "models file count");
        }));

        out.add(test("foldersContaining: the 8 real battle-royale loot-crate categories resolve under models", () -> {
            GameObjectsCatalog catalog = loadFixture();
            for (String category : new String[]{
                    "br_ammo", "br_armor1lvl", "br_armor2lvl", "br_build",
                    "br_med", "br_other", "br_weapon1lvl", "br_weapon2lvl",
            }) {
                assertTrue(catalog.foldersContaining(category).contains("models"), category);
            }
        }));

        out.add(test("foldersContaining: this project's first real, concrete BR loot-crate taxonomy extends part 72's br_ prefix lead", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("br_weapon1lvl").contains("models"), "br_weapon1lvl");
            assertTrue(catalog.foldersContaining("br_weapon2lvl").contains("models"), "br_weapon2lvl");
        }));

        out.add(test("foldersContaining: switches/test_switch is a real leftover dev-test artifact", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("test_switch").contains("models"), "test_switch");
        }));

        out.add(test("foldersContaining: puh model props cross-reference their own texture folders", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("puh1").contains("models"), "puh1 under models");
            assertTrue(catalog.foldersContaining("puh_diff").contains("textures"), "puh_diff under textures");
            assertTrue(catalog.foldersContaining("puh_new").contains("models"), "puh_new under models");
            assertTrue(catalog.foldersContaining("puh_new_diff").contains("textures"), "puh_new_diff under textures");
        }));

        out.add(test("filesIn: trace is the complete real 1-file folder", () -> {
            GameObjectsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("trace");
            assertEquals(1, files.size(), "trace file count");
            assertTrue(files.contains("Zabor_betonnyi_dyra_5852.mcvd"), "Zabor_betonnyi_dyra_5852.mcvd present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            GameObjectsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("GameObjectsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("gameobjects-fixture-empty");
                dir.toFile().deleteOnExit();
                GameObjectsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static GameObjectsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("gameobjects-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return GameObjectsCatalog.load(dir);
    }
}
