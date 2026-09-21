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
 * Regression tests for {@link PdaMapCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/pda/} folder (part 64), live-verified on
 * device: representative subsets of {@code global_map/} and {@code map/}, the complete real content
 * of all 9 {@code map-<zone>} folders (each is tiny -- 1 to 31 real files), a representative subset
 * of {@code map_overlay/inside/}, and the complete real {@code sound/} (2 files) and {@code
 * textures/} (10 files) folders. Only real game-content names are used, per this project's privacy
 * rule -- never a real player username.
 */
public final class PdaMapCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // global_map/ -- representative subset (36 real files total, x:-4..4, y:-2..1)
            "global_map/r.-4.-2.ol",
            "global_map/r.4.1.ol",
            "global_map/r.0.0.ol",
            // map/ -- representative subset (944 real files total, x:-23..132, y:-14..12)
            "map/r.-23.-14.ol",
            "map/r.132.12.ol",
            "map/r.-2.-2.ol",
            "map/r.11.-5.ol",
            "map/r.11.-6.ol",
            "map/r.12.-5.ol",
            "map/r.12.-6.ol",
            // the 9 real map-<zone> folders, complete real content each
            "map-arena_raven/r.5.-7.ol",
            "map-dungeon_bolota_tutorial/r.6.-7.ol",
            "map-kolizei/r.-2.-2.ol",
            "map-nbolota/r.-6.1.ol",
            "map-nbolota/r.-6.2.ol",
            "map-nbolota/r.-7.1.ol",
            "map-nbolota/r.-7.2.ol",
            "map-nbolota/r.-8.1.ol",
            "map-nbolota/r.-8.2.ol",
            "map-new_bar/r.-3.-10.ol",
            "map-prestige/r.-10.-10.ol",
            "map-prestige/r.-10.-11.ol",
            "map-prestige/r.-11.-10.ol",
            "map-prestige/r.-11.-11.ol",
            "map-reichstag/r.11.-5.ol",
            "map-reichstag/r.11.-6.ol",
            "map-reichstag/r.12.-5.ol",
            "map-reichstag/r.12.-6.ol",
            "map-sovetpunk/r.13.-10.ol",
            "map-sovetpunk/r.13.-11.ol",
            "map-sovhoz_overlay/-1.3.mic",
            "map-sovhoz_overlay/-1.4.mic",
            "map-sovhoz_overlay/0.3.mic",
            "map-sovhoz_overlay/1.6.mic",
            // map_overlay/inside/ -- representative subset (975 real files total, x:-23..18, y:-11..12)
            "map_overlay/inside/-1.-1.mic",
            "map_overlay/inside/-1.-10.mic",
            "map_overlay/inside/-23.-11.mic",
            "map_overlay/inside/18.12.mic",
            // sound/ -- the complete real 2-file folder
            "sound/radar_new_player.ogg",
            "sound/radar_player_left.ogg",
            // textures/ -- the complete real 10-file folder
            "textures/achievements.mic",
            "textures/av_background.mic",
            "textures/failure_overlay.mic",
            "textures/line.ol",
            "textures/main_background.mic",
            "textures/mini_map.mic",
            "textures/mini_map_ny.mic",
            "textures/mini_map_rn.mic",
            "textures/territory_pattern.ol",
            "textures/waypoints.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("PdaMapCatalog.load finds exactly the 14 real top-level folders", () -> {
            PdaMapCatalog catalog = loadFixture();
            assertEquals(14, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"global_map", "map", "map-arena_raven",
                    "map-dungeon_bolota_tutorial", "map-kolizei", "map-nbolota", "map-new_bar",
                    "map-prestige", "map-reichstag", "map-sovetpunk", "map-sovhoz_overlay",
                    "map_overlay", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            PdaMapCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("foldersContaining: map-reichstag's 2x2 tile block sits inside the shared map/ grid", () -> {
            PdaMapCatalog catalog = loadFixture();
            List<String> reichstag = catalog.foldersContaining("r.11.-5");
            assertEquals(2, reichstag.size(), "r.11.-5 folder count (map/ and map-reichstag/)");
            assertTrue(reichstag.contains("map"), "r.11.-5 present under map/");
            assertTrue(reichstag.contains("map-reichstag"), "r.11.-5 present under map-reichstag/");
        }));

        out.add(test("foldersContaining: map-prestige's real 2x2 tile block is a fresh cross-reference for the recurring prestige lead", () -> {
            PdaMapCatalog catalog = loadFixture();
            List<String> prestige = catalog.foldersContaining("r.-11.-11");
            assertEquals(1, prestige.size(), "r.-11.-11 folder count");
            assertTrue(prestige.contains("map-prestige"), "r.-11.-11 present under map-prestige");
        }));

        out.add(test("filesIn: map-sovhoz_overlay is the complete real 4-file fixture, all .mic not .ol", () -> {
            PdaMapCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("map-sovhoz_overlay");
            assertEquals(4, files.size(), "map-sovhoz_overlay fixture file count");
            for (String file : files) {
                assertTrue(file.endsWith(".mic"), file + " ends with .mic, not .ol");
            }
        }));

        out.add(test("filesIn: sound holds exactly the real radar-ping pair", () -> {
            PdaMapCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("sound");
            assertEquals(2, files.size(), "sound file count");
            assertTrue(files.contains("radar_new_player.ogg"), "radar_new_player.ogg present");
            assertTrue(files.contains("radar_player_left.ogg"), "radar_player_left.ogg present");
        }));

        out.add(test("foldersContaining: mini_map_ny is a real seasonal New Year mini-map variant under textures", () -> {
            PdaMapCatalog catalog = loadFixture();
            List<String> ny = catalog.foldersContaining("mini_map_ny");
            assertEquals(1, ny.size(), "mini_map_ny folder count");
            assertTrue(ny.contains("textures"), "mini_map_ny lives under textures");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            PdaMapCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            PdaMapCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("PdaMapCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("pda-map-fixture-empty");
                dir.toFile().deleteOnExit();
                PdaMapCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static PdaMapCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("pda-map-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return PdaMapCatalog.load(dir);
    }
}
