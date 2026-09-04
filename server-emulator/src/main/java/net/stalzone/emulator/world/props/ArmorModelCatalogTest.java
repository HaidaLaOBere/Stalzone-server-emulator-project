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
 * Regression tests for {@link ArmorModelCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code stalker/models/armor/} folder (part 54): a
 * same-level "_sanitar" suffix variant ({@code zarya}), a dedicated-mesh "_sanitar" variant
 * ({@code kz3b}, which has its own {@code kz3b_sanitar.mcsb} rather than only a texture pair),
 * the real Twins boss body/head models, the real {@code lipa_mask} file tying {@code zarya} to
 * the "Lipa" tutorial character, and one folder with no sanitar variant at all ({@code hunter}) to
 * confirm {@link ArmorModelCatalog#foldersContaining} doesn't over-match.
 */
public final class ArmorModelCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "zarya/zarya.mcsb",
            "zarya/zarya_diff.ol",
            "zarya/zarya_sanitar_diff.ol",
            "zarya/zarya_sanitar_nrm.ol",
            "zarya/lipa_mask.mcsb",
            "kz3b/kz3b.mcsb",
            "kz3b/kz3b_sanitar.mcsb",
            "kz3b/kz3b_sanitar_diff.ol",
            "twins/left_twin.mcsb",
            "twins/right_twin.mcsb",
            "twins/twins_heads_diff.ol",
            "hunter/hunter.mcsb",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ArmorModelCatalog.load finds every real folder in the fixture", () -> {
            ArmorModelCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("zarya"), "zarya present");
            assertTrue(catalog.folderNames().contains("kz3b"), "kz3b present");
            assertTrue(catalog.folderNames().contains("twins"), "twins present");
            assertTrue(catalog.folderNames().contains("hunter"), "hunter present");
        }));

        out.add(test("filesIn: returns every real nested file path, including a same-level suffix pair", () -> {
            ArmorModelCatalog catalog = loadFixture();
            List<String> zaryaFiles = catalog.filesIn("zarya");
            assertEquals(5, zaryaFiles.size(), "zarya file count");
            assertTrue(zaryaFiles.contains("zarya_sanitar_diff.ol"), "zarya has a sanitar texture");
            assertTrue(zaryaFiles.contains("lipa_mask.mcsb"), "zarya has the real lipa_mask file");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            ArmorModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("foldersContaining: finds real 'sanitar' variants in both suffix and dedicated-mesh form", () -> {
            ArmorModelCatalog catalog = loadFixture();
            List<String> sanitarFolders = catalog.foldersContaining("sanitar");
            assertEquals(2, sanitarFolders.size(), "sanitar folder count");
            assertTrue(sanitarFolders.contains("zarya"), "zarya has a sanitar variant");
            assertTrue(sanitarFolders.contains("kz3b"), "kz3b has a sanitar variant");
            assertTrue(!sanitarFolders.contains("hunter"), "hunter correctly has no sanitar variant");
            assertTrue(!sanitarFolders.contains("twins"), "twins correctly has no sanitar variant");
        }));

        out.add(test("foldersContaining: is case-insensitive and matches a single real file", () -> {
            ArmorModelCatalog catalog = loadFixture();
            List<String> lipaFolders = catalog.foldersContaining("LIPA");
            assertEquals(1, lipaFolders.size(), "lipa folder count");
            assertTrue(lipaFolders.contains("zarya"), "zarya matches case-insensitive LIPA search");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            ArmorModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            ArmorModelCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("ArmorModelCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("armor-fixture-empty");
                dir.toFile().deleteOnExit();
                ArmorModelCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static ArmorModelCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("armor-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return ArmorModelCatalog.load(dir);
    }
}
