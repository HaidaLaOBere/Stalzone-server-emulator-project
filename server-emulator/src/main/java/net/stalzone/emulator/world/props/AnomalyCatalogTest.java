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
 * Regression tests for {@link AnomalyCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/anomalies/} folder (part 71), live-verified on
 * device: a representative {@code sound/} subset covering the real named anomaly-type roster plus the
 * {@code resources}/{@code traders}/{@code morse} subfolders, a representative {@code models/} subset
 * (incl. the complete real 6-file {@code zastoy/} folder), and the complete real 1-file {@code trace/}
 * folder. Only real game-content names are used, per this project's privacy rule -- never a real
 * player username.
 */
public final class AnomalyCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- 4 real loose files
            "models/bolt.mcsb",
            "models/bolt_thrown.mcsb",
            "models/box.mcsb",
            "models/empty.mcsb",
            // models/zastoy/ -- the complete real 6-file folder
            "models/zastoy/zastoy_1.mcsb",
            "models/zastoy/zastoy_2.mcsb",
            "models/zastoy/zastoy_3.mcsb",
            "models/zastoy/zastoy_4.mcsb",
            "models/zastoy/zastoy_5.mcsb",
            "models/zastoy/zastoy_6.mcsb",
            // models/extractor/ -- representative subset (23 real files total)
            "models/extractor/extractor_1.mcsb",
            "models/extractor/extractor_2.mcsb",
            // sound/ -- representative subset of the real named anomaly-type roster
            "sound/blackhole.ogg",
            "sound/carousel.ogg",
            "sound/carousel_bolt.ogg",
            "sound/circusidle.ogg",
            "sound/electra.ogg",
            "sound/protoelectra_hit.ogg",
            "sound/funnel.ogg",
            "sound/funnel_bolt.ogg",
            "sound/kissel.ogg",
            "sound/kissel_hit.ogg",
            "sound/mirror_attack.ogg",
            "sound/stasis_effect.ogg",
            "sound/statis_effect.ogg",
            "sound/steam.ogg",
            "sound/tesla_active.ogg",
            "sound/trampoline.ogg",
            // sound/resources/ -- representative subset (23 real files total)
            "sound/resources/chronodevice_idle.ogg",
            "sound/resources/chronodevice_mining.ogg",
            "sound/resources/chronodevice_start.ogg",
            "sound/resources/chronodevice_stop.ogg",
            // sound/traders/ -- the complete real 5-file folder
            "sound/traders/monolit.ogg",
            "sound/traders/trader_v2_.ogg",
            "sound/traders/trader_v3_.ogg",
            "sound/traders/trader_v4_.ogg",
            "sound/traders/zombie.ogg",
            // sound/morse/ -- the complete real 2-file folder
            "sound/morse/0_.ogg",
            "sound/morse/1_.ogg",
            // textures/ -- representative subset
            "textures/distortion.mic",
            "textures/light_beam.mic",
            "textures/scanners_b.mic",
            // trace/ -- the complete real 1-file folder
            "trace/zastoy.mcvd",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("AnomalyCatalog.load finds exactly the 4 real top-level folders", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "sound", "textures", "trace"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("anomalies-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            Path loose = dir.resolve("morse_alphabet.txt");
            Files.writeString(loose, "");
            loose.toFile().deleteOnExit();
            AnomalyCatalog catalog = AnomalyCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "loose root file must not be counted");
        }));

        out.add(test("foldersContaining: the real named anomaly-type roster (blackhole, kissel, tesla) resolves under sound", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("blackhole").contains("sound"), "blackhole");
            assertTrue(catalog.foldersContaining("kissel").contains("sound"), "kissel");
            assertTrue(catalog.foldersContaining("tesla").contains("sound"), "tesla");
        }));

        out.add(test("foldersContaining: circusidle cross-references part 63's gloomycore/handbook/circus.mic", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("circusidle").contains("sound"), "circusidle");
        }));

        out.add(test("foldersContaining: monolit in traders cross-references part 68's Monolit faction", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("monolit").contains("sound"), "monolit");
        }));

        out.add(test("foldersContaining: the real chronodevice artefact-extraction sounds resolve under sound", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("chronodevice").contains("sound"), "chronodevice");
        }));

        out.add(test("foldersContaining: zastoy has both a real model set and its own trace decal", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("zastoy").contains("models"), "zastoy under models");
            assertTrue(catalog.foldersContaining("zastoy").contains("trace"), "zastoy under trace");
        }));

        out.add(test("filesIn: zastoy model folder is the complete real 6-file fixture", () -> {
            AnomalyCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("models");
            long zastoyCount = files.stream().filter(f -> f.startsWith("zastoy/")).count();
            assertEquals(6, (int) zastoyCount, "zastoy model file count");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            AnomalyCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("AnomalyCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("anomalies-fixture-empty");
                dir.toFile().deleteOnExit();
                AnomalyCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static AnomalyCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("anomalies-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return AnomalyCatalog.load(dir);
    }
}
