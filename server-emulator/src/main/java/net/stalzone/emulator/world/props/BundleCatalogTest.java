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
 * Regression tests for {@link BundleCatalog}, using a fixture directory tree with real filenames drawn
 * directly from the real {@code modassets/assets/bundle/} folder (part 78), live-verified on device:
 * the complete real 1-file {@code levels/} folder, plus the real 8 loose root-level files (used only to
 * verify {@code load}'s folder-only walk genuinely does not see them). Only real game-content names are
 * used, per this project's privacy rule -- never a real player username.
 */
public final class BundleCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // levels/ -- the complete real 1-file folder
            "levels/br_location.txt",
    };

    /** Real loose root-level files -- NOT indexed by the folder-only {@code load} walk. */
    private static final String[] LOOSE_ROOT_FILES = {
            "item_groups.txt",
            "item_groups_barter.txt",
            "items.xeon",
            "logged_trader_item_groups.txt",
            "nvds.yolo",
            "render_configs.xeon",
            "sound_duration.csv",
            "speech_duration.csv",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("BundleCatalog.load finds exactly the 1 real top-level folder", () -> {
            BundleCatalog catalog = loadFixture();
            assertEquals(1, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("levels"), "levels present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            BundleCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: levels is the complete real 1-file battle-royale-marker folder", () -> {
            BundleCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("levels");
            assertEquals(1, files.size(), "levels file count");
            assertTrue(files.contains("br_location.txt"), "br_location.txt present");
        }));

        out.add(test("foldersContaining: br_location resolves under levels", () -> {
            BundleCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("br_location").contains("levels"), "br_location");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            BundleCatalog catalog = loadFixtureWithLooseRootFiles();
            for (String looseFile : LOOSE_ROOT_FILES) {
                assertTrue(catalog.foldersContaining(looseFile).isEmpty(), looseFile + " not indexed");
            }
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count unaffected by loose root files");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            BundleCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            BundleCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("BundleCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("bundle-fixture-empty");
                dir.toFile().deleteOnExit();
                BundleCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static BundleCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("bundle-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return BundleCatalog.load(dir);
    }

    private static BundleCatalog loadFixtureWithLooseRootFiles() throws Exception {
        Path dir = Files.createTempDirectory("bundle-fixture-loose");
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
        return BundleCatalog.load(dir);
    }
}
