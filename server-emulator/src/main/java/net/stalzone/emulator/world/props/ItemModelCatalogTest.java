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
 * Regression tests for {@link ItemModelCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code stalker/models/items/} folder (part 57): a real
 * texture-sharing folder where most meshes have no dedicated texture of their own ({@code
 * armorplate}), a real mesh-less-texture item ({@code psiblock_diff.ol} with no matching mesh),
 * and a real cross-naming-convention split ({@code meds/nmeds/}'s capitalized {@code _Diffuse}
 * suffix vs. the lowercase {@code _diff} used everywhere else in this tree).
 */
public final class ItemModelCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "armorplate/common.mcsb",
            "armorplate/durable.mcsb",
            "armorplate/meh.mcsb",
            "armorplate/meh_diff.ol",
            "armorplate/meh_nrm.ol",
            "armorplate/meh_spek.ol",
            "armorplate/armorplate_diff.ol",
            "armorplate/armorplate2_diff.ol",
            "armorplate/armorplate_nrm.ol",
            "food/galet.mcsb",
            "food/galet_diff.ol",
            "food/galet_nrm.ol",
            "food/galet_spek.ol",
            "meds/psiblock_diff.ol",
            "meds/psiblock_spek.ol",
            "meds/nmeds/psiblock.mcsb",
            "meds/nmeds/aptechka.mcsb",
            "meds/nmeds/aptechka_Diffuse.ol",
            "meds/nmeds/aptechka_Normal.ol",
            "meds/nmeds/aptechka_Specular.ol",
            "meds/nmeds/aptechka_color_prestige.ol",
            "meds/memoria/memoria.mcsb",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ItemModelCatalog.load finds every real folder in the fixture", () -> {
            ItemModelCatalog catalog = loadFixture();
            assertEquals(3, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("armorplate"), "armorplate present");
            assertTrue(catalog.folderNames().contains("food"), "food present");
            assertTrue(catalog.folderNames().contains("meds"), "meds present");
        }));

        out.add(test("filesIn: most armorplate meshes have no dedicated texture of their own", () -> {
            ItemModelCatalog catalog = loadFixture();
            List<String> plate = catalog.filesIn("armorplate");
            assertEquals(9, plate.size(), "armorplate file count");
            assertTrue(plate.contains("common.mcsb"), "common mesh present with no own texture");
            assertTrue(plate.contains("meh_spek.ol"), "meh has its own full texture set");
            assertTrue(plate.contains("armorplate2_diff.ol"), "generic root-level texture present");
        }));

        out.add(test("filesIn: a mesh-less texture item has no matching mesh anywhere in its folder", () -> {
            ItemModelCatalog catalog = loadFixture();
            List<String> meds = catalog.filesIn("meds");
            assertTrue(meds.contains("psiblock_diff.ol"), "psiblock_diff.ol present at meds root");
            assertTrue(!meds.contains("psiblock.mcsb"), "no psiblock.mcsb at meds root");
            assertTrue(meds.contains("nmeds/psiblock.mcsb"), "psiblock.mcsb instead lives in nmeds/");
        }));

        out.add(test("filesIn: nmeds nested subfolder path is relative to the parent folder itself", () -> {
            ItemModelCatalog catalog = loadFixture();
            List<String> meds = catalog.filesIn("meds");
            assertTrue(meds.contains("nmeds/aptechka_Diffuse.ol"), "nested capitalized-suffix texture path");
            assertTrue(meds.contains("nmeds/aptechka_color_prestige.ol"), "nested prestige variant path");
            assertTrue(meds.contains("memoria/memoria.mcsb"), "nested bare-mesh-no-texture folder path");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            ItemModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("foldersContaining: finds the capitalized _Diffuse naming convention only in meds", () -> {
            ItemModelCatalog catalog = loadFixture();
            List<String> diffuseFolders = catalog.foldersContaining("_Diffuse");
            assertEquals(1, diffuseFolders.size(), "_Diffuse folder count");
            assertTrue(diffuseFolders.contains("meds"), "meds has the capitalized-suffix nmeds/ files");
        }));

        out.add(test("foldersContaining: matches a nested prestige variant by substring, case-insensitively", () -> {
            ItemModelCatalog catalog = loadFixture();
            List<String> prestige = catalog.foldersContaining("PRESTIGE");
            assertEquals(1, prestige.size(), "prestige folder count");
            assertTrue(prestige.contains("meds"), "meds has the prestige cosmetic variant");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            ItemModelCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            ItemModelCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("ItemModelCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("item-fixture-empty");
                dir.toFile().deleteOnExit();
                ItemModelCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static ItemModelCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("item-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return ItemModelCatalog.load(dir);
    }
}
