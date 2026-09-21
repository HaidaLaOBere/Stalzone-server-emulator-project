package net.stalzone.emulator.world.props;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link PatchAssetsCatalog}, built from the complete real 68-file listing
 * of {@code bin_global/patchassets/assets/} (part 105, live-verified on device).
 */
public final class PatchAssetsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "bundle/anim_casts.xeon",
            "bundle/boxes.xeon",
            "bundle/configs.xeon",
            "bundle/effects.xeon",
            "bundle/icons.xeon",
            "bundle/materials.xeon",
            "bundle/shaders.xeon",
            "bundle/small_images.xeon",
            "bundle/texmeta.xeon",
            "bundle/textures.sign",
            "highpoly/character/animations/pack.mcal",
            "highpoly/character/animationspov/pack.mcal",
            "metadata",
            "pda/map/r.-1.-35.ol",
            "pda/map/r.-2.62.ol",
            "pda/map/r.-46.1.ol",
            "pda/map/r.-86.-15.ol",
            "pda/map/r.0.-38.ol",
            "pda/map/r.1.-13.ol",
            "pda/map/r.1.-15.ol",
            "pda/map/r.1.-16.ol",
            "pda/map/r.1.-18.ol",
            "pda/map/r.2.-19.ol",
            "pda/map/r.3.-12.ol",
            "pda/map/r.3.-13.ol",
            "pda/map/r.3.-16.ol",
            "pda/map/r.3.-17.ol",
            "pda/map/r.3.-18.ol",
            "pda/map/r.4.-12.ol",
            "pda/map/r.4.-13.ol",
            "pda/map/r.4.-14.ol",
            "pda/map/r.4.-16.ol",
            "pda/map/r.4.-17.ol",
            "pda/map/r.4.-18.ol",
            "pda/map/r.4.-19.ol",
            "pda/map/r.4.-20.ol",
            "pda/map/r.5.-14.ol",
            "pda/map/r.5.-16.ol",
            "pda/map/r.5.-17.ol",
            "pda/map/r.5.-19.ol",
            "pda/map/r.6.-12.ol",
            "pda/map/r.6.-15.ol",
            "pda/map/r.6.-16.ol",
            "pda/map/r.6.-17.ol",
            "pda/map/r.6.-18.ol",
            "pda/map/r.6.-19.ol",
            "pda/map/r.7.-17.ol",
            "pda/map/r.7.-18.ol",
            "pda/map/r.7.-19.ol",
            "pda/map/r.7.-20.ol",
            "pda/map/r.7.57.ol",
            "pda/map/r.8.-19.ol",
            "pda/map/r.8.-20.ol",
            "stalcraft/textures/blockMap.ta",
            "stalcraft/textures/blockMap.texarr",
            "stalcraft/textures/hrblockMap.ta",
            "stalker/slices/bar_sever.mcws",
            "stalker/slices/covenant.mcws",
            "stalker/slices/csky.mcws",
            "stalker/slices/duty.mcws",
            "stalker/slices/foton2.mcws",
            "stalker/slices/freedom.mcws",
            "stalker/slices/garage.mcws",
            "stalker/slices/lodochnaya.mcws",
            "stalker/slices/merc.mcws",
            "stalker/slices/rostok.mcws",
            "stalker/slices/savezone_agro.mcws",
            "stalker/slices/savezone_td.mcws",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load finds all 6 real top-level folder keys incl. ROOT_KEY", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(6, catalog.totalFolderCount(), "folder count");
            List<String> folders = catalog.folderNames();
            assertTrue(folders.contains(PatchAssetsCatalog.ROOT_KEY), "contains ROOT_KEY");
            assertTrue(folders.contains("bundle"), "contains bundle");
            assertTrue(folders.contains("highpoly"), "contains highpoly");
            assertTrue(folders.contains("pda"), "contains pda");
            assertTrue(folders.contains("stalcraft"), "contains stalcraft");
            assertTrue(folders.contains("stalker"), "contains stalker");
        }));

        out.add(test("load finds all 68 real files total", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(68, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("ROOT_KEY holds exactly the loose metadata file", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            List<String> rootFiles = catalog.filesIn(PatchAssetsCatalog.ROOT_KEY);
            assertEquals(1, rootFiles.size(), "root file count");
            assertEquals("metadata", rootFiles.get(0), "root file name");
        }));

        out.add(test("bundle/ has exactly 10 real files", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(10, catalog.filesIn("bundle").size(), "bundle file count");
        }));

        out.add(test("pda/ has exactly 40 real map-tile override files", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(40, catalog.filesIn("pda").size(), "pda file count");
        }));

        out.add(test("stalker/ has exactly 12 real slice files, all under slices/", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stalker");
            assertEquals(12, files.size(), "stalker file count");
            for (String file : files) {
                assertTrue(file.startsWith("slices/"), "under slices/: " + file);
            }
        }));

        out.add(test("stalker/slices/ carries 12 of SettlementRegistry's 15 real settlement ids", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stalker");
            String[] expectedIds = {
                    "bar_sever", "covenant", "csky", "duty", "foton2", "freedom",
                    "garage", "lodochnaya", "merc", "rostok", "savezone_agro", "savezone_td",
            };
            for (String id : expectedIds) {
                assertTrue(files.contains("slices/" + id + ".mcws"), "carries real slice for settlement id " + id);
            }
        }));

        out.add(test("foldersContaining finds the real csky level-slice override", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("csky");
            assertEquals(1, matches.size(), "match count");
            assertEquals("stalker", matches.get(0), "matching folder");
        }));

        out.add(test("foldersContaining is case-insensitive and returns empty for a substring nothing matches", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("ROSTOK").contains("stalker"), "case-insensitive match");
            assertTrue(catalog.foldersContaining("not_a_real_substring_xyz").isEmpty(), "no match");
        }));

        out.add(test("stalcraft/ has exactly 3 real texture-array files", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(3, catalog.filesIn("stalcraft").size(), "stalcraft file count");
        }));

        out.add(test("highpoly/ has exactly 2 real animation-pack files", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertEquals(2, catalog.filesIn("highpoly").size(), "highpoly file count");
        }));

        out.add(test("filesIn returns empty (not null) for an unknown folder name", () -> {
            PatchAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.filesIn("not_a_real_folder").isEmpty(), "unknown folder");
        }));

        out.add(test("load throws IOException on an empty directory", () -> {
            Path dir = Files.createTempDirectory("patch-assets-empty-fixture");
            dir.toFile().deleteOnExit();
            boolean threw = false;
            try {
                PatchAssetsCatalog.load(dir);
            } catch (java.io.IOException expected) {
                threw = true;
            }
            assertTrue(threw, "load() on an empty directory should throw IOException");
        }));

        return out;
    }

    private static PatchAssetsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("patch-assets-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return PatchAssetsCatalog.load(dir);
    }
}
