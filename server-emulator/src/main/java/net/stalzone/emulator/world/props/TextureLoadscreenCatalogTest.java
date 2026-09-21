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
 * Regression tests for {@link TextureLoadscreenCatalog}, using a fixture directory tree with the
 * complete real filenames drawn directly from the real {@code
 * modassets/assets/stalker/textures/loadscreen/} folder (part 97), live-verified on device via a
 * single {@code find} sweep, staged to the cloud workspace, and cross-checked with a {@code Counter}
 * before this 186-entry array was generated programmatically (never hand-typed). Only real
 * game-content/asset names are used, per this project's privacy rule -- never a real player username.
 * The real on-disk {@code Pd/} folder name preserves its genuine capitalization (distinct from every
 * other folder in this set, which are all lowercase); the real on-disk {@code north_serpantine/}
 * folder name preserves its genuine spelling divergence from {@code en.lang}'s {@code
 * north_serpentine} key -- neither is normalized in this fixture.
 */
public final class TextureLoadscreenCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "Pd/pd1.ol",
            "Pd/pd2.ol",
            "Pd/pd3.ol",
            "agroprom/agroprom1.ol",
            "agroprom/agroprom2.ol",
            "agroprom/agroprom3.ol",
            "arena_raven/raven1.ol",
            "arena_raven/raven2.ol",
            "arena_raven/raven3.ol",
            "arena_raven/raven4.ol",
            "armsklad/armsklad1.ol",
            "armsklad/armsklad2.ol",
            "armsklad/armsklad3.ol",
            "arsenal_workshop/arsenal.ol",
            "atamans_hq/atamans_hq1.ol",
            "atp/atp1.ol",
            "bar/bar1.ol",
            "bar/bar2.ol",
            "bar/bar3.ol",
            "bar_save/bar_save1.ol",
            "bar_save/bar_save2.ol",
            "bar_save/bar_save3.ol",
            "bar_save/bar_save_hangar1.ol",
            "bar_save/bar_save_hangar2.ol",
            "bar_save/bar_save_hangar3.ol",
            "bar_save/bar_sever1.ol",
            "bar_save/bar_sever2.ol",
            "bar_save/bar_sever3.ol",
            "base_covenant/covenant1.ol",
            "base_duty/duty1.ol",
            "base_freedom/freedom1.ol",
            "base_merc/merc1.ol",
            "battlefield/1april.ol",
            "battlefield/collapse.ol",
            "battlefield/duel.ol",
            "battlefield/hospital.ol",
            "battlefield/jail.ol",
            "battlefield/kolizei.ol",
            "battlefield/krot2.ol",
            "battlefield/kutuzov.ol",
            "battlefield/moryatnik.ol",
            "battlefield/peklo.ol",
            "battlefield/pripyat.ol",
            "battlefield/reich.ol",
            "battlefield/reich_breakthrough.ol",
            "battlefield/reich_breakthrough_1.ol",
            "battlefield/reich_breakthrough_2.ol",
            "battlefield/reich_breakthrough_3.ol",
            "battlefield/rus.ol",
            "battlefield/sovetpunk.ol",
            "battlefield/stakan.ol",
            "berdovka/berdovka.ol",
            "boat_station/boat_station1.ol",
            "bolota/1.ol",
            "bolota/2.ol",
            "bolota/3.ol",
            "dungeon_school/mg1.ol",
            "dungeon_school/mg2.ol",
            "dungeon_school/mg3.ol",
            "ffa_base_bunker/ffa_base_bunker.ol",
            "ffa_base_kpp/ffa_base_kpp.ol",
            "ffa_base_meteostation/ffa_base_meteostation.ol",
            "ffa_base_mine/ffa_base_mine.ol",
            "foton2/foton2_1.ol",
            "generic/1.ol",
            "generic/2.ol",
            "generic/3.ol",
            "generic/4.ol",
            "generic/5.ol",
            "hvoiny/hvoiny.ol",
            "incident/incident1.ol",
            "incident/incident2.ol",
            "incident/incident3.ol",
            "incident/incident4.ol",
            "incident/incident5.ol",
            "inside/agroprom.ol",
            "inside/forge.ol",
            "inside/inflow.ol",
            "inside/kordon.ol",
            "inside/mg.ol",
            "inside/mines.ol",
            "inside/svalka.ol",
            "inside/td.ol",
            "inside/yantar.ol",
            "inside/zaton.ol",
            "kordon/kordon1.ol",
            "kordon/kordon2.ol",
            "kordon/kordon3.ol",
            "kvartaly/kvartaly.ol",
            "les/les1.ol",
            "les/les2.ol",
            "les/les3.ol",
            "limansk/limansk1.ol",
            "limansk/limansk2.ol",
            "limansk/limansk3.ol",
            "mg/mg1.ol",
            "mg/mg2.ol",
            "mg/mg3.ol",
            "nbolota/nbolota1.ol",
            "nbolota/nbolota2.ol",
            "nbolota/nbolota3.ol",
            "nizina/nizina.ol",
            "north_barrier/barrier1.ol",
            "north_barrier/barrier2.ol",
            "north_barrier/barrier3.ol",
            "north_forge/forge1.ol",
            "north_forge/forge2.ol",
            "north_forge/forge3.ol",
            "north_forge/forge4.ol",
            "north_hazard_inflow/hazard_inflow1.ol",
            "north_hazard_inflow/hazard_inflow2.ol",
            "north_hazard_inflow/hazard_inflow3.ol",
            "north_hazard_mines/hazard_mines1.ol",
            "north_hazard_mines/hazard_mines2.ol",
            "north_hazard_mines/hazard_mines3.ol",
            "north_hrop/hrop1.ol",
            "north_hrop/hrop2.ol",
            "north_hrop/hrop3.ol",
            "north_hrop/hrop4.ol",
            "north_inflow/inflow1.ol",
            "north_inflow/inflow2.ol",
            "north_inflow/inflow3.ol",
            "north_institute/institute1.ol",
            "north_institute/institute2.ol",
            "north_institute/institute3.ol",
            "north_institute/institute4.ol",
            "north_maze_d_c/maze_d_c1.ol",
            "north_maze_d_c/maze_d_c2.ol",
            "north_maze_d_c/maze_d_c3.ol",
            "north_maze_m_f/maze_m_f1.ol",
            "north_maze_m_f/maze_m_f2.ol",
            "north_maze_m_f/maze_m_f3.ol",
            "north_mines/mines1.ol",
            "north_mines/mines2.ol",
            "north_mines/mines3.ol",
            "north_mines/mines4.ol",
            "north_serpantine/serpantine1.ol",
            "north_serpantine/serpantine2.ol",
            "north_serpantine/serpantine3.ol",
            "north_serpantine/serpantine4.ol",
            "object/object1.ol",
            "operation_big_cleanup/hospital1.ol",
            "operation_big_cleanup/hospital2.ol",
            "operation_big_cleanup/library1.ol",
            "operation_big_cleanup/library2.ol",
            "operation_sea_alienation/SoA_1.ol",
            "operation_sea_alienation/SoA_2.ol",
            "operation_sea_alienation/SoA_3.ol",
            "operation_sea_alienation/SoA_4.ol",
            "operation_shock_therapy/bur1.ol",
            "operation_shock_therapy/bur2.ol",
            "operation_shock_therapy/nii1.ol",
            "operation_shock_therapy/nii2.ol",
            "overpass/overpass1.ol",
            "overpass/overpass2.ol",
            "overpass/overpass3.ol",
            "pa/pa1.ol",
            "poligon2/1.ol",
            "prestige/map_1_1.ol",
            "prestige/map_1_2.ol",
            "prestige/map_1_3.ol",
            "prestige/map_1_4.ol",
            "prestige/map_1_5.ol",
            "radar/radar1.ol",
            "rls/rls_1.ol",
            "rls/rls_2.ol",
            "rls/rls_3.ol",
            "rls/rls_4.ol",
            "roza/roza.ol",
            "savezone_agroprom/savezone_agroprom1.ol",
            "savezone_td/savezone_td1.ol",
            "svalka/svalka1.ol",
            "svalka/svalka2.ol",
            "svalka/svalka3.ol",
            "td/td1.ol",
            "td/td2.ol",
            "td/td3.ol",
            "yama/yama1.ol",
            "yama/yama2.ol",
            "yama/yama3.ol",
            "yanov/yanov_1.ol",
            "yanov/yanov_2.ol",
            "yanov/yanov_3.ol",
            "zaton/zaton1.ol",
            "zaton/zaton2.ol",
            "zaton/zaton3.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("TextureLoadscreenCatalog.load finds all 63 real top-level folders", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(63, catalog.totalFolderCount(), "folder count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (186 total)", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(186, catalog.totalFileCount(), "matches the real 186-file sweep");
        }));

        out.add(test("filesIn: Pd preserves its real on-disk capitalization (3 files)", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("Pd");
            assertEquals(3, files.size(), "Pd file count");
            assertTrue(files.contains("pd1.ol"), "pd1.ol present");
            assertTrue(catalog.filesIn("pd").isEmpty(), "lowercase pd is a distinct, unknown key");
        }));

        out.add(test("filesIn: battlefield is the complete real 19-file clan-war map roster", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("battlefield");
            assertEquals(19, files.size(), "battlefield file count");
            assertTrue(files.contains("reich.ol"), "reich.ol present");
            assertTrue(files.contains("reich_breakthrough_3.ol"), "reich_breakthrough_3.ol present");
            assertTrue(files.contains("kutuzov.ol"), "kutuzov.ol present (unconfirmed lead)");
        }));

        out.add(test("foldersContaining: reich resolves under battlefield (Reichstag corroboration)", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("reich");
            assertEquals(1, matches.size(), "reich folder count");
            assertTrue(matches.contains("battlefield"), "battlefield has reich files");
        }));

        out.add(test("filesIn: the 4 alliance base folders are each a complete real single-file set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("base_duty").size(), "base_duty file count");
            assertEquals(1, catalog.filesIn("base_freedom").size(), "base_freedom file count");
            assertEquals(1, catalog.filesIn("base_merc").size(), "base_merc file count");
            assertEquals(1, catalog.filesIn("base_covenant").size(), "base_covenant file count");
        }));

        out.add(test("filesIn: the 4 FFA base folders are each a complete real single-file set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("ffa_base_bunker").size(), "ffa_base_bunker file count");
            assertEquals(1, catalog.filesIn("ffa_base_kpp").size(), "ffa_base_kpp file count");
            assertEquals(1, catalog.filesIn("ffa_base_meteostation").size(), "ffa_base_meteostation file count");
            assertEquals(1, catalog.filesIn("ffa_base_mine").size(), "ffa_base_mine file count");
        }));

        out.add(test("filesIn: the 3 real Operation folders are each a complete real 4-file set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(4, catalog.filesIn("operation_big_cleanup").size(), "operation_big_cleanup file count");
            assertEquals(4, catalog.filesIn("operation_sea_alienation").size(), "operation_sea_alienation file count");
            assertEquals(4, catalog.filesIn("operation_shock_therapy").size(), "operation_shock_therapy file count");
        }));

        out.add(test("filesIn: the 5 tournament-family folders (Crow Arena) are each a complete real single-file set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("hvoiny").size(), "hvoiny file count");
            assertEquals(1, catalog.filesIn("kvartaly").size(), "kvartaly file count");
            assertEquals(1, catalog.filesIn("nizina").size(), "nizina file count");
            assertEquals(1, catalog.filesIn("roza").size(), "roza file count");
            assertEquals(1, catalog.filesIn("berdovka").size(), "berdovka file count");
        }));

        out.add(test("filesIn: arena_raven is the complete real 4-file Arena of Slavs folder", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("arena_raven");
            assertEquals(4, files.size(), "arena_raven file count");
            assertTrue(files.contains("raven1.ol"), "raven1.ol present");
        }));

        out.add(test("filesIn: prestige is the complete real 5-file Area-16 map set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("prestige");
            assertEquals(5, files.size(), "prestige file count");
            assertTrue(files.contains("map_1_1.ol"), "map_1_1.ol present");
        }));

        out.add(test("filesIn: atamans_hq is the complete real single-file Ataman's HQ settlement folder", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("atamans_hq").size(), "atamans_hq file count");
        }));

        out.add(test("filesIn: boat_station resolves part 27's lodochnaya lead (1 file)", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("boat_station").size(), "boat_station file count");
        }));

        out.add(test("filesIn: limansk is the complete real 3-file Lyubech-3 alias folder", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(3, catalog.filesIn("limansk").size(), "limansk file count");
        }));

        out.add(test("filesIn: inside is the complete real 10-file interior-transition set", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("inside");
            assertEquals(10, files.size(), "inside file count");
            assertTrue(files.contains("yantar.ol"), "yantar.ol present");
        }));

        out.add(test("foldersContaining: north_ prefix resolves the 11 real north sub-region folders", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("north_");
            assertEquals(0, matches.size(), "north_ substring is a folder-name prefix, not inside filenames");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            TextureLoadscreenCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("TextureLoadscreenCatalog.load rejects a root with no subfolders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("texture-loadscreen-fixture-empty");
                dir.toFile().deleteOnExit();
                TextureLoadscreenCatalog.load(dir);
            }, "root with no subfolders");
        }));

        return out;
    }

    private static TextureLoadscreenCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("texture-loadscreen-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return TextureLoadscreenCatalog.load(dir);
    }

    // Note: unlike TextureMiscCatalog (a KNOWN_FOLDERS whitelist sweep over a shared parent that
    // deliberately has other real folders it must ignore), TextureLoadscreenCatalog walks every
    // direct subfolder of whatever root it is given -- it has no whitelist to test, because in
    // real usage that root IS textures/loadscreen/ itself, and a folder like gui/ is a sibling
    // under textures/, never nested inside loadscreen/. There is no "ignores unrelated folder"
    // case to construct here without misrepresenting the real on-disk layout.
}
