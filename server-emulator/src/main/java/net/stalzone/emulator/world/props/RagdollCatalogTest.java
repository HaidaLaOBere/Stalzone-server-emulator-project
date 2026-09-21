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
 * Regression tests for {@link RagdollCatalog}, using a fixture directory tree that is the COMPLETE real
 * {@code modassets/assets/ragdolls/} folder (part 76, 41 files under the single real {@code models/}
 * top-level folder, small enough to fixture in full), live-verified on device. Only real game-content
 * names are used, per this project's privacy rule -- never a real player username.
 */
public final class RagdollCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- the complete real 41-file folder: 27 real named mutant/NPC ragdoll.mcsb files
            "models/boar/ragdoll.mcsb",
            "models/cat/ragdoll.mcsb",
            "models/chimera/ragdoll.mcsb",
            "models/cybergiant/ragdoll.mcsb",
            "models/dog/ragdoll.mcsb",
            "models/doge/ragdoll.mcsb",
            "models/drone/ragdoll.mcsb",
            "models/flesh/ragdoll.mcsb",
            "models/fpv/ragdoll.mcsb",
            "models/krovosos/ragdoll.mcsb",
            "models/lurker/ragdoll.mcsb",
            "models/pseudodog/ragdoll.mcsb",
            "models/pseudogigant/ragdoll.mcsb",
            "models/psidog/ragdoll.mcsb",
            "models/psidog_clone/ragdoll.mcsb",
            "models/puppet/ragdoll.mcsb",
            "models/rudolf/ragdoll.mcsb",
            "models/snork/ragdoll.mcsb",
            "models/spirit/ragdoll.mcsb",
            "models/steve/ragdoll.mcsb",
            "models/thief_rat/ragdoll.mcsb",
            "models/tick/ragdoll.mcsb",
            "models/tsarevich/ragdoll.mcsb",
            "models/tsarevna/ragdoll.mcsb",
            "models/tumbler/ragdoll.mcsb",
            "models/tushkan/ragdoll.mcsb",
            "models/wisp/ragdoll.mcsb",
            // models/tex/ -- the complete real 10-file corpse-bag texture set
            "models/tex/corpse_bag_1.ol",
            "models/tex/corpse_bag_2.ol",
            "models/tex/corpse_bag_3.ol",
            "models/tex/corpse_bag_4.ol",
            "models/tex/corpse_bag_5.ol",
            "models/tex/corpse_bag_6.ol",
            "models/tex/corpse_bag_7.ol",
            "models/tex/corpse_bag_8.ol",
            "models/tex/corpse_bag_9.ol",
            "models/tex/corpse_bag_nrm.ol",
            // 4 loose files directly in models/
            "models/corpse_bag.mcsb",
            "models/corpse_bag.mcvd",
            "models/corpse_pile.mcsb",
            "models/corpse_pile.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("RagdollCatalog.load finds exactly the 1 real top-level folder", () -> {
            RagdollCatalog catalog = loadFixture();
            assertEquals(1, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("models"), "models present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (matches the real 41-file total)", () -> {
            RagdollCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(41, catalog.totalFileCount(), "real total file count");
        }));

        out.add(test("filesIn: models has exactly 27 real ragdoll.mcsb entries, one per named mutant/NPC", () -> {
            RagdollCatalog catalog = loadFixture();
            long ragdollCount = catalog.filesIn("models").stream()
                    .filter(f -> f.endsWith("/ragdoll.mcsb")).count();
            assertEquals(27, (int) ragdollCount, "ragdoll.mcsb count");
        }));

        out.add(test("foldersContaining: krovosos, snork, and tushkan are real canonical S.T.A.L.K.E.R.-series mutant names", () -> {
            RagdollCatalog catalog = loadFixture();
            for (String mutant : new String[]{"krovosos", "snork", "tushkan"}) {
                assertTrue(catalog.foldersContaining(mutant).contains("models"), mutant);
            }
        }));

        out.add(test("foldersContaining: steve has a real ragdoll -- a third confirmation it is a functional entity", () -> {
            RagdollCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("steve/ragdoll").contains("models"), "steve/ragdoll");
        }));

        out.add(test("foldersContaining: tsarevna is a fresh female-counterpart lead to part 69's tsarevich", () -> {
            RagdollCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("tsarevna").contains("models"), "tsarevna");
            assertTrue(catalog.foldersContaining("tsarevich").contains("models"), "tsarevich");
        }));

        out.add(test("foldersContaining: cybergiant/doge/rudolf/tsarevich cross-reference part 69's StalkerMobsCatalog reskins", () -> {
            RagdollCatalog catalog = loadFixture();
            for (String mutant : new String[]{"cybergiant", "doge", "rudolf", "tsarevich"}) {
                assertTrue(catalog.foldersContaining(mutant).contains("models"), mutant);
            }
        }));

        out.add(test("filesIn: models/tex is the complete real 10-file corpse-bag texture set", () -> {
            RagdollCatalog catalog = loadFixture();
            long texCount = catalog.filesIn("models").stream()
                    .filter(f -> f.startsWith("tex/")).count();
            assertEquals(10, (int) texCount, "tex/ file count");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            RagdollCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            RagdollCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("RagdollCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("ragdolls-fixture-empty");
                dir.toFile().deleteOnExit();
                RagdollCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static RagdollCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("ragdolls-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return RagdollCatalog.load(dir);
    }
}
