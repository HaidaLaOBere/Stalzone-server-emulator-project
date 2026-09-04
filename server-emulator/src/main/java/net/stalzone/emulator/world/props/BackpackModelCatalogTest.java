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
 * Regression tests for {@link BackpackModelCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code stalker/models/backpacks/} folder (part 56): a
 * real "kept the old version" sibling-folder pair ({@code bag_npa}/{@code bag_npa_old}, where the
 * {@code _old} folder carries one real extra file, {@code npa_spek.ol}, the current folder lacks),
 * a real bare-mesh-no-texture gap ({@code bag_tv}, {@code tv.mcsb} only), a real
 * {@code style_<season>} nested subfolder ({@code cont_hive/style_autumn25/}), and a real nested
 * {@code old/} subfolder holding a prior texture revision ({@code cont_kzs1/old/}).
 */
public final class BackpackModelCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "bag_npa/npa.mcsb",
            "bag_npa/npa_diff.ol",
            "bag_npa/npa_ground.mcsb",
            "bag_npa/npa_nrm.ol",
            "bag_npa_old/npa.mcsb",
            "bag_npa_old/npa_diff.ol",
            "bag_npa_old/npa_ground.mcsb",
            "bag_npa_old/npa_nrm.ol",
            "bag_npa_old/npa_spek.ol",
            "bag_tv/tv.mcsb",
            "cont_hive/hive.mcsb",
            "cont_hive/hive_diff.ol",
            "cont_hive/hive_emi.ol",
            "cont_hive/hive_ground.mcsb",
            "cont_hive/hive_nrm.ol",
            "cont_hive/style_autumn25/hive_autumn25.mcsb",
            "cont_hive/style_autumn25/hive_autumn25_diff.ol",
            "cont_hive/style_autumn25/hive_autumn25_nrm.ol",
            "cont_hive/style_autumn25/animation_atlas.ol",
            "cont_hive/style_autumn25/animation_atlas_emi.ol",
            "cont_hive/style_autumn25/glass_nrm.ol",
            "cont_kzs1/kzs1.mcsb",
            "cont_kzs1/kzs1_diff.ol",
            "cont_kzs1/kzs1_ground.mcsb",
            "cont_kzs1/kzs1_nrm.ol",
            "cont_kzs1/kzs1u_diff.ol",
            "cont_kzs1/old/kzs1_diff.ol",
            "cont_kzs1/old/kzs1_nrm.ol",
            "cont_kzs1/old/kzs1_spek.ol",
            "cont_kzs1/old/kzs1d_diff.ol",
            "clan/clan_supply.mcsb",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("BackpackModelCatalog.load finds every real folder in the fixture", () -> {
            BackpackModelCatalog catalog = loadFixture();
            assertEquals(6, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("bag_npa"), "bag_npa present");
            assertTrue(catalog.folderNames().contains("bag_npa_old"), "bag_npa_old present");
            assertTrue(catalog.folderNames().contains("bag_tv"), "bag_tv present");
            assertTrue(catalog.folderNames().contains("cont_hive"), "cont_hive present");
            assertTrue(catalog.folderNames().contains("cont_kzs1"), "cont_kzs1 present");
            assertTrue(catalog.folderNames().contains("clan"), "clan present");
        }));

        out.add(test("filesIn: an _old sibling folder carries one real extra file the current folder lacks", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> npa = catalog.filesIn("bag_npa");
            List<String> npaOld = catalog.filesIn("bag_npa_old");
            assertEquals(4, npa.size(), "bag_npa file count");
            assertEquals(5, npaOld.size(), "bag_npa_old file count");
            assertTrue(npaOld.contains("npa_spek.ol"), "npa_spek.ol only in the _old folder");
            assertTrue(!npa.contains("npa_spek.ol"), "npa_spek.ol correctly absent from the current folder");
        }));

        out.add(test("filesIn: a bare mesh folder has no matching texture file at all", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> tv = catalog.filesIn("bag_tv");
            assertEquals(1, tv.size(), "bag_tv file count");
            assertTrue(tv.contains("tv.mcsb"), "tv.mcsb is the sole file");
        }));

        out.add(test("filesIn: a style_<season> nested subfolder path is relative to the parent folder itself", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> hive = catalog.filesIn("cont_hive");
            assertEquals(11, hive.size(), "cont_hive file count");
            assertTrue(hive.contains("style_autumn25/hive_autumn25.mcsb"), "nested seasonal mesh path");
            assertTrue(hive.contains("style_autumn25/animation_atlas.ol"), "nested seasonal atlas texture path");
            assertTrue(hive.contains("hive.mcsb"), "base mesh still present at folder root");
        }));

        out.add(test("filesIn: a nested old/ subfolder holding a prior texture revision", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> kzs1 = catalog.filesIn("cont_kzs1");
            assertEquals(9, kzs1.size(), "cont_kzs1 file count");
            assertTrue(kzs1.contains("old/kzs1_spek.ol"), "nested old/ revision path");
            assertTrue(kzs1.contains("kzs1u_diff.ol"), "current u-suffixed variant still present at root");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            BackpackModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("foldersContaining: finds the widespread _ground mesh convention, case-insensitively", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> groundFolders = catalog.foldersContaining("GROUND");
            assertEquals(4, groundFolders.size(), "_ground folder count");
            assertTrue(groundFolders.contains("bag_npa"), "bag_npa has a _ground mesh");
            assertTrue(groundFolders.contains("bag_npa_old"), "bag_npa_old has a _ground mesh");
            assertTrue(groundFolders.contains("cont_hive"), "cont_hive has a _ground mesh");
            assertTrue(groundFolders.contains("cont_kzs1"), "cont_kzs1 has a _ground mesh");
            assertTrue(!groundFolders.contains("bag_tv"), "bag_tv correctly has no _ground mesh");
            assertTrue(!groundFolders.contains("clan"), "clan correctly has no _ground mesh");
        }));

        out.add(test("foldersContaining: matches a nested style_<season> subfolder file by substring", () -> {
            BackpackModelCatalog catalog = loadFixture();
            List<String> seasonal = catalog.foldersContaining("style_autumn25");
            assertEquals(1, seasonal.size(), "style_autumn25 folder count");
            assertTrue(seasonal.contains("cont_hive"), "cont_hive has the autumn25 style variant");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            BackpackModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            BackpackModelCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("BackpackModelCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("backpack-fixture-empty");
                dir.toFile().deleteOnExit();
                BackpackModelCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static BackpackModelCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("backpack-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return BackpackModelCatalog.load(dir);
    }
}
