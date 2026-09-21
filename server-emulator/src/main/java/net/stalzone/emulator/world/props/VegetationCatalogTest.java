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
 * Regression tests for {@link VegetationCatalog}, using a fixture directory tree that is the COMPLETE
 * real {@code modassets/assets/vegetation/} folder (part 75, 42 files, small enough to fixture in
 * full), live-verified on device. Only real game-content names are used, per this project's privacy
 * rule -- never a real player username.
 */
public final class VegetationCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- the complete real 38-file folder
            "models/grass.ol",
            "models/grass2.ol",
            "models/grass3.ol",
            "models/grass_0.obj",
            "models/grass_1.obj",
            "models/grass_2.obj",
            "models/grass_a_0.mcsb",
            "models/grass_a_1.mcsb",
            "models/grass_a_2.mcsb",
            "models/grass_b_0.mcsb",
            "models/wrk/Grass_01.mtl",
            "models/wrk/Grass_01.obj",
            "models/wrk/Grass_02.mtl",
            "models/wrk/Grass_02.obj",
            "models/wrk/Grass_03.mtl",
            "models/wrk/Grass_03.obj",
            "models/wrk/foliage_flower_long_grass_diffuse.ol",
            "models/wrk/grass.mic",
            "models/wrk/grass01.3ds",
            "models/wrk/grass01.mic",
            "models/wrk/grass02.3ds",
            "models/wrk/grass02.mic",
            "models/wrk/grass03.3ds",
            "models/wrk/grass_0.ol",
            "models/wrk/grass_1.mic",
            "models/wrk/grass_1.ol",
            "models/wrk/grass_a_0.mcsa_old",
            "models/wrk/grass_a_0.mcsa_veryold",
            "models/wrk/grass_a_0.mcsaold",
            "models/wrk/grass_a_1.mcsa_old",
            "models/wrk/grass_a_1.mcsa_veryold",
            "models/wrk/grass_a__1.mcsb",
            "models/wrk/grass_ao.mic",
            "models/wrk/grass_diff.mic",
            "models/wrk/grass_diff.ol",
            "models/wrk/optical.mic",
            "models/wrk/trees_vetkagreen3.ol",
            "models/wrk/unused.mcsb",
            // textures/ -- the complete real 4-file folder
            "textures/blocks/reed_0.mic",
            "textures/blocks/reed_1.mic",
            "textures/blocks/reed_2.mic",
            "textures/blocks/reed_3.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("VegetationCatalog.load finds exactly the 2 real top-level folders", () -> {
            VegetationCatalog catalog = loadFixture();
            assertEquals(2, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (matches the real 42-file total)", () -> {
            VegetationCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(42, catalog.totalFileCount(), "real total file count");
        }));

        out.add(test("filesIn: models is the complete real 38-file folder", () -> {
            VegetationCatalog catalog = loadFixture();
            assertEquals(38, catalog.filesIn("models").size(), "models file count");
        }));

        out.add(test("filesIn: textures is the complete real 4-file reed-block set", () -> {
            VegetationCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("textures");
            assertEquals(4, files.size(), "textures file count");
            for (String name : new String[]{
                    "blocks/reed_0.mic", "blocks/reed_1.mic", "blocks/reed_2.mic", "blocks/reed_3.mic",
            }) {
                assertTrue(files.contains(name), name + " present");
            }
        }));

        out.add(test("foldersContaining: unused.mcsb is a direct, self-disclosed cut-content artifact", () -> {
            VegetationCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("unused.mcsb").contains("models"), "unused.mcsb");
        }));

        out.add(test("foldersContaining: the wrk/ dev-pipeline folder holds real .3ds and .obj/.mtl format files", () -> {
            VegetationCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("grass01.3ds").contains("models"), "grass01.3ds");
            assertTrue(catalog.foldersContaining("Grass_01.obj").contains("models"), "Grass_01.obj");
            assertTrue(catalog.foldersContaining("Grass_01.mtl").contains("models"), "Grass_01.mtl");
        }));

        out.add(test("foldersContaining: grass_a_0 has 3 distinct old-version save-iteration extensions", () -> {
            VegetationCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("grass_a_0.mcsa_old").contains("models"), "mcsa_old");
            assertTrue(catalog.foldersContaining("grass_a_0.mcsa_veryold").contains("models"), "mcsa_veryold");
            assertTrue(catalog.foldersContaining("grass_a_0.mcsaold").contains("models"), "mcsaold");
        }));

        out.add(test("foldersContaining: reed is a real Minecraft block-family name, cross-referencing the Minecraft-origin account", () -> {
            VegetationCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("reed").contains("textures"), "reed");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            VegetationCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            VegetationCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("VegetationCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("vegetation-fixture-empty");
                dir.toFile().deleteOnExit();
                VegetationCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static VegetationCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("vegetation-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return VegetationCatalog.load(dir);
    }
}
