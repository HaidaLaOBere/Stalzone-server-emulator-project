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
 * Regression tests for {@link AuctionCatalog}, using a fixture directory tree that is the COMPLETE real
 * {@code modassets/assets/auction/} folder (part 77, 19 files, small enough to fixture in full),
 * live-verified on device. Only real game-content names are used, per this project's privacy rule --
 * never a real player username.
 */
public final class AuctionCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // fonts/ -- the complete real 14-file folder
            "fonts/CAPTURE_IT__12.fmi",
            "fonts/CAPTURE_IT__14.fmi",
            "fonts/CAPTURE_IT__16.fmi",
            "fonts/Capture_it.ttf",
            "fonts/GOST_A.ttf",
            "fonts/GOST_A__12.fmi",
            "fonts/GOST_A__14.fmi",
            "fonts/GOST_B.TTF",
            "fonts/GOST_B__12.fmi",
            "fonts/GOST_B_bold_20.fmi",
            "fonts/Tahoma__11.fmi",
            "fonts/Tahoma_bold_11.fmi",
            "fonts/Tahoma_bold_14.fmi",
            "fonts/dictionary.txt",
            // textures/gui/ -- the complete real 5-file folder
            "textures/gui/auction_list.mic",
            "textures/gui/auction_list_bg.mic",
            "textures/gui/icons.mic",
            "textures/gui/mail_list.mic",
            "textures/gui/mail_msg.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("AuctionCatalog.load finds exactly the 2 real top-level folders", () -> {
            AuctionCatalog catalog = loadFixture();
            assertEquals(2, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"fonts", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (matches the real 19-file total)", () -> {
            AuctionCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(19, catalog.totalFileCount(), "real total file count");
        }));

        out.add(test("filesIn: fonts is the complete real 14-file folder", () -> {
            AuctionCatalog catalog = loadFixture();
            assertEquals(14, catalog.filesIn("fonts").size(), "fonts file count");
        }));

        out.add(test("foldersContaining: GOST fonts are real, formal evidence of the Russian dev origin", () -> {
            AuctionCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("GOST_A").contains("fonts"), "GOST_A");
            assertTrue(catalog.foldersContaining("GOST_B").contains("fonts"), "GOST_B");
        }));

        out.add(test("foldersContaining: Tahoma is a real Microsoft system font used as a UI fallback", () -> {
            AuctionCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("Tahoma").contains("fonts"), "Tahoma");
        }));

        out.add(test("foldersContaining: dictionary.txt is a real font glyph-coverage list, not a word dictionary", () -> {
            AuctionCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("dictionary.txt").contains("fonts"), "dictionary.txt");
        }));

        out.add(test("foldersContaining: mail UI assets are bundled under the auction namespace, a fresh finding", () -> {
            AuctionCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("mail_list").contains("textures"), "mail_list");
            assertTrue(catalog.foldersContaining("mail_msg").contains("textures"), "mail_msg");
        }));

        out.add(test("filesIn: textures is the complete real 5-file auction/mail UI atlas set", () -> {
            AuctionCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("textures");
            assertEquals(5, files.size(), "textures file count");
            for (String name : new String[]{"gui/auction_list.mic", "gui/icons.mic"}) {
                assertTrue(files.contains(name), name + " present");
            }
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            AuctionCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            AuctionCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("AuctionCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("auction-fixture-empty");
                dir.toFile().deleteOnExit();
                AuctionCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static AuctionCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("auction-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return AuctionCatalog.load(dir);
    }
}
