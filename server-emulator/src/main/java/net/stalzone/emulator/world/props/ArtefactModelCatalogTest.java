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
 * Regression tests for {@link ArtefactModelCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code stalker/models/artefacts/} folder (part 55): a
 * flat "classic"-era folder with a real texture-less mesh gap ({@code barter}, where
 * {@code art_tesla.mcsb} has no matching texture set, unlike its sibling {@code art_iskra.mcsb}),
 * a real {@code n_art}-era nested per-artefact folder ({@code n_art/electra/art_coil/}), a real
 * Halloween-adjacent folder ({@code helewin}), and a folder with a real dev-leftover file plus a
 * real naming duplication ({@code karusel}, {@code test.mcsb} and {@code art_ostov.mcsb} vs
 * {@code ostov.mcsb}).
 */
public final class ArtefactModelCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "barter/art_iskra.mcsb",
            "barter/iskra_diff.ol",
            "barter/iskra_emis.ol",
            "barter/iskra_nrm.ol",
            "barter/iskra_spek.ol",
            "barter/art_tesla.mcsb",
            "n_art/electra/art_coil/art_coil.mcsb",
            "n_art/electra/art_coil/coil_diff.ol",
            "n_art/electra/art_coil/coil_emi.ol",
            "n_art/electra/art_coil/coil_nrm.ol",
            "helewin/pup.ol",
            "helewin/pupdif.ol",
            "helewin/pupdif_emi.ol",
            "helewin/ty_kva.mcsb",
            "karusel/art_ostov.mcsb",
            "karusel/ostov.mcsb",
            "karusel/ostov_diff.ol",
            "karusel/test.mcsb",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ArtefactModelCatalog.load finds every real folder in the fixture", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("barter"), "barter present");
            assertTrue(catalog.folderNames().contains("n_art"), "n_art present");
            assertTrue(catalog.folderNames().contains("helewin"), "helewin present");
            assertTrue(catalog.folderNames().contains("karusel"), "karusel present");
        }));

        out.add(test("filesIn: flat folder returns a mesh with no matching texture alongside one that has a full set", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            List<String> barterFiles = catalog.filesIn("barter");
            assertEquals(6, barterFiles.size(), "barter file count");
            assertTrue(barterFiles.contains("art_tesla.mcsb"), "art_tesla present with no texture set");
            assertTrue(barterFiles.contains("iskra_emis.ol"), "iskra has its full texture set");
        }));

        out.add(test("filesIn: n_art nested per-artefact folder path is relative to n_art itself", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            List<String> nArtFiles = catalog.filesIn("n_art");
            assertEquals(4, nArtFiles.size(), "n_art file count");
            assertTrue(nArtFiles.contains("electra/art_coil/art_coil.mcsb"), "nested art_coil mesh path");
            assertTrue(nArtFiles.contains("electra/art_coil/coil_emi.ol"), "nested art_coil emissive texture path");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("foldersContaining: matches a nested n_art file by substring", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            List<String> coilFolders = catalog.foldersContaining("coil");
            assertEquals(1, coilFolders.size(), "coil folder count");
            assertTrue(coilFolders.contains("n_art"), "n_art has the coil artefact");
        }));

        out.add(test("foldersContaining: is case-insensitive and finds a substring across flat and nested folders", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            List<String> emiFolders = catalog.foldersContaining("EMI");
            assertEquals(3, emiFolders.size(), "emi folder count");
            assertTrue(emiFolders.contains("barter"), "barter's iskra_emis.ol matches");
            assertTrue(emiFolders.contains("n_art"), "n_art's coil_emi.ol matches");
            assertTrue(emiFolders.contains("helewin"), "helewin's pupdif_emi.ol matches");
            assertTrue(!emiFolders.contains("karusel"), "karusel correctly has no 'emi' match");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            ArtefactModelCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("ArtefactModelCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("artefact-fixture-empty");
                dir.toFile().deleteOnExit();
                ArtefactModelCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static ArtefactModelCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("artefact-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return ArtefactModelCatalog.load(dir);
    }
}
