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
 * Regression tests for {@link TextureMiscCatalog}, using a fixture directory tree with the complete
 * real filenames drawn directly from the real {@code modassets/assets/stalker/textures/} folder's 6
 * small leaf trees swept together in part 96 (19 files total): {@code waypoints/} (9), {@code br/}
 * (5), {@code blizzard_overlay/} (2), {@code new_effects_texture/} (1), {@code mining/} (1), {@code
 * map/} (1), live-verified on device via a single {@code find}-per-folder sweep, staged to the cloud
 * workspace, and cross-checked with a {@code Counter} before this array was generated programmatically
 * (never hand-typed). Only real game-content/asset names are used, per this project's privacy rule --
 * never a real player username. The real on-disk {@code waypoints/} filenames use spaces and the
 * spelling "comand" (not "command"), both preserved verbatim.
 */
public final class TextureMiscCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "blizzard_overlay/pd.mic",
            "blizzard_overlay/svalka.mic",
            "br/br_layer_0.mic",
            "br/br_layer_1.mic",
            "br/br_layer_2.mic",
            "br/br_layer_flow.mic",
            "br/electricity.mic",
            "map/map_icons.ol",
            "mining/display.ol",
            "new_effects_texture/leaf_atl_1.mic",
            "waypoints/comand mark.ol",
            "waypoints/comand mark2.ol",
            "waypoints/comand mark3.ol",
            "waypoints/death.ol",
            "waypoints/finish quest.ol",
            "waypoints/finish select quest.ol",
            "waypoints/personal mark.ol",
            "waypoints/quest.ol",
            "waypoints/select quest.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("TextureMiscCatalog.load finds exactly the 6 known small-tree folders", () -> {
            TextureMiscCatalog catalog = loadFixture();
            assertEquals(6, catalog.totalFolderCount(), "folder count");
            for (String known : TextureMiscCatalog.KNOWN_FOLDERS) {
                assertTrue(catalog.folderNames().contains(known), known + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (19 total)", () -> {
            TextureMiscCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(19, catalog.totalFileCount(), "matches the real 19-file sweep");
        }));

        out.add(test("filesIn: waypoints is the complete real 9-file folder, incl. spaced/'comand' names", () -> {
            TextureMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("waypoints");
            assertEquals(9, files.size(), "waypoints file count");
            assertTrue(files.contains("comand mark.ol"), "comand mark.ol present");
            assertTrue(files.contains("personal mark.ol"), "personal mark.ol present");
            assertTrue(files.contains("finish select quest.ol"), "finish select quest.ol present");
        }));

        out.add(test("filesIn: br is the complete real 5-file Battle Royale folder", () -> {
            TextureMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("br");
            assertEquals(5, files.size(), "br file count");
            assertTrue(files.contains("br_layer_flow.mic"), "br_layer_flow.mic present");
            assertTrue(files.contains("electricity.mic"), "electricity.mic present");
        }));

        out.add(test("filesIn: blizzard_overlay is the complete real 2-file folder (pd, svalka locations)", () -> {
            TextureMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("blizzard_overlay");
            assertEquals(2, files.size(), "blizzard_overlay file count");
            assertTrue(files.contains("pd.mic"), "pd.mic present (The Path of Fools)");
            assertTrue(files.contains("svalka.mic"), "svalka.mic present (Scrapyard)");
        }));

        out.add(test("filesIn: mining and map are each a complete real single-file folder", () -> {
            TextureMiscCatalog catalog = loadFixture();
            assertEquals(1, catalog.filesIn("mining").size(), "mining file count");
            assertTrue(catalog.filesIn("mining").contains("display.ol"), "display.ol present");
            assertEquals(1, catalog.filesIn("map").size(), "map file count");
            assertTrue(catalog.filesIn("map").contains("map_icons.ol"), "map_icons.ol present");
        }));

        out.add(test("foldersContaining: br_layer resolves under br only", () -> {
            TextureMiscCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("br_layer");
            assertEquals(1, matches.size(), "br_layer folder count");
            assertTrue(matches.contains("br"), "br has br_layer files");
        }));

        out.add(test("foldersContaining: svalka resolves under blizzard_overlay only", () -> {
            TextureMiscCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("svalka");
            assertEquals(1, matches.size(), "svalka folder count");
            assertTrue(matches.contains("blizzard_overlay"), "blizzard_overlay has svalka");
        }));

        out.add(test("load ignores a real textures/ folder that is not on the known-small-tree whitelist", () -> {
            TextureMiscCatalog catalog = loadFixtureWithUnrelatedFolder();
            assertEquals(6, catalog.totalFolderCount(), "folder count unaffected by unrelated folder");
            assertTrue(!catalog.folderNames().contains("gui"), "gui not indexed (a future dedicated part)");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            TextureMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            TextureMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("TextureMiscCatalog.load rejects a root with none of the known folders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("texture-misc-fixture-empty");
                dir.toFile().deleteOnExit();
                TextureMiscCatalog.load(dir);
            }, "root with no known folders");
        }));

        return out;
    }

    private static TextureMiscCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("texture-misc-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return TextureMiscCatalog.load(dir);
    }

    private static TextureMiscCatalog loadFixtureWithUnrelatedFolder() throws Exception {
        Path dir = Files.createTempDirectory("texture-misc-fixture-unrelated");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        Path unrelated = dir.resolve("gui/some_atlas.mic");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "");
        unrelated.toFile().deleteOnExit();
        return TextureMiscCatalog.load(dir);
    }
}
