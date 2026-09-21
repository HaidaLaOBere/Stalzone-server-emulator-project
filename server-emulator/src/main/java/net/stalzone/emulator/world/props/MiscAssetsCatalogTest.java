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
 * Regression tests for {@link MiscAssetsCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/} folder's 8 small remaining trees (part 79),
 * live-verified on device: the complete real 17-file set across {@code chat}/{@code ejection}/{@code
 * probuilder}/{@code handcuffs}/{@code skinarmor}/{@code party}/{@code misc}/{@code trade}. Only real
 * game-content names are used, per this project's privacy rule -- never a real player username.
 */
public final class MiscAssetsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // chat/ -- the complete real 3-file folder
            "chat/textures/gui/chat_bg.mic",
            "chat/textures/gui/chat_bg_ny.mic",
            "chat/textures/gui/chat_rn.mic",
            // ejection/ -- the complete real 4-file folder
            "ejection/sound/thunder1.ogg",
            "ejection/sound/thunder2.ogg",
            "ejection/sound/thunder3.ogg",
            "ejection/stream/windstorm.ogg",
            // probuilder/ -- the complete real 4-file folder, incl. a loose root-level config file
            "probuilder/smoothing.txt",
            "probuilder/textures/items/door.ol",
            "probuilder/textures/items/hammer.ol",
            "probuilder/textures/items/spraycan.ol",
            // handcuffs/ -- the complete real 2-file folder
            "handcuffs/models/handcuffs.mcsb",
            "handcuffs/models/handcuffs.ol",
            // skinarmor/ -- the complete real 1-file folder
            "skinarmor/textures/items/skin.mic",
            // party/ -- the complete real 1-file folder, a loose root-level config file
            "party/weapon_levels.cfg",
            // misc/ -- the complete real 1-file folder
            "misc/textures/items/barter/triumph_compensation.ol",
            // trade/ -- the complete real 1-file folder
            "trade/textures/gui/trade.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("MiscAssetsCatalog.load finds exactly the 8 known small-tree folders", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertEquals(8, catalog.totalFolderCount(), "folder count");
            for (String known : MiscAssetsCatalog.KNOWN_FOLDERS) {
                assertTrue(catalog.folderNames().contains(known), known + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: probuilder includes its loose root-level smoothing.txt (unlike other catalogs)", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("probuilder");
            assertEquals(4, files.size(), "probuilder file count");
            assertTrue(files.contains("smoothing.txt"), "smoothing.txt present");
        }));

        out.add(test("filesIn: party is the complete real 1-file weapon-tier config folder", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("party");
            assertEquals(1, files.size(), "party file count");
            assertTrue(files.contains("weapon_levels.cfg"), "weapon_levels.cfg present");
        }));

        out.add(test("foldersContaining: ny (seasonal chat background) resolves under chat", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("chat_bg_ny").contains("chat"), "chat_bg_ny");
        }));

        out.add(test("foldersContaining: windstorm resolves under ejection", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("windstorm").contains("ejection"), "windstorm");
        }));

        out.add(test("load ignores a real folder that is not on the known-small-tree whitelist", () -> {
            MiscAssetsCatalog catalog = loadFixtureWithUnrelatedFolder();
            assertEquals(8, catalog.totalFolderCount(), "folder count unaffected by unrelated folder");
            assertTrue(!catalog.folderNames().contains("shop"), "shop not indexed (not in whitelist)");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            MiscAssetsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("MiscAssetsCatalog.load rejects a root with none of the known folders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("misc-assets-fixture-empty");
                dir.toFile().deleteOnExit();
                MiscAssetsCatalog.load(dir);
            }, "root with no known folders");
        }));

        return out;
    }

    private static MiscAssetsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("misc-assets-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return MiscAssetsCatalog.load(dir);
    }

    private static MiscAssetsCatalog loadFixtureWithUnrelatedFolder() throws Exception {
        Path dir = Files.createTempDirectory("misc-assets-fixture-unrelated");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        Path unrelated = dir.resolve("shop/models/not_from_this_catalog.mcsb");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "");
        unrelated.toFile().deleteOnExit();
        return MiscAssetsCatalog.load(dir);
    }
}
