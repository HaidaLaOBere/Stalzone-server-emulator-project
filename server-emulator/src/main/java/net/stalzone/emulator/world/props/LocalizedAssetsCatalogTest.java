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
 * Regression tests for {@link LocalizedAssetsCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/_localized/} folder (part 80),
 * live-verified on device. Given the tree's real scale (8,796 files across 5 locales), this fixture is
 * a representative subset, not the complete tree -- covering the {@code en} ejection/emission handbook
 * articles, the {@code dmz2026}/{@code ravent24} dated audio leads, the {@code streamrandom/reichstag}
 * Soviet/Wermacht calldown set, and one real marker file per locale demonstrating the real per-locale
 * namespace-coverage asymmetry documented in the class Javadoc ({@code en} has all 12 real namespaces;
 * {@code ru} has 11 (no {@code gloomycore}); {@code es}/{@code fr} share the same narrower 9; {@code
 * ko} has only 4). Only real game-content names are used, per this project's privacy rule -- never a
 * real player username.
 */
public final class LocalizedAssetsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // en/ -- ejection/emission handbook articles (real dev test/sample fixtures)
            "en/stalkerguide/handbook/articles/ejection/sample_ejection.md",
            "en/stalkerguide/handbook/articles/ejection/sample_ejection2.md",
            "en/stalkerguide/handbook/articles/ejection/shelters.md",
            "en/stalkerguide/handbook/articles/ejection/map_example.mic",
            "en/stalkerguide/handbook/articles/ejection/notification_card.mic",
            "en/stalkerguide/handbook/articles/arthunt/vybros.md",
            "en/stalkerguide/handbook/articles/arthunt/anomalii.md",
            "en/stalkerguide/handbook/articles/arthunt/artefakty.md",
            // en/ -- dmz2026 dated audio lead (real 2026-dated content, part 67 cross-reference)
            "en/stalker/sound/dmz2026/pers/2b11_strat1.ogg",
            "en/stalker/sound/dmz2026/pers/2b11_strat2.ogg",
            // en/ -- ravent24 dated audio lead (a fresh "lizards" enemy/faction name)
            "en/stalker/sound/ravent24/lizards/begin/start0.ogg",
            "en/stalker/sound/ravent24/lizards/begin/start1.ogg",
            // en/ -- streamrandom/reichstag (7th independent Reichstag corroboration)
            "en/stalker/streamrandom/reichstag/RCCA/calldown_soviet1.ogg",
            "en/stalker/streamrandom/reichstag/Wermacht/calldown_wermacht1.ogg",
            // en/ -- marker files for namespaces only en has (gloomycore, stalcraft, stalkerclans, stalkersounds)
            "en/gloomycore/marker.mic",
            "en/stalcraft/marker.mic",
            "en/stalkerclans/marker.mic",
            "en/stalkersounds/marker.ogg",
            // ru/ -- 11 namespaces (has stalcraft/stalkerclans/stalkersounds, no gloomycore)
            "ru/stalcraft/marker.mic",
            "ru/stalkerclans/marker.mic",
            "ru/stalkersounds/marker.ogg",
            // es/ and fr/ -- the same narrower real 9-namespace set (no stalcraft/stalkerclans/stalkersounds)
            "es/anomalies/marker.mic",
            "es/weapons/marker.mic",
            "fr/anomalies/marker.mic",
            "fr/weapons/marker.mic",
            // ko/ -- the real narrowest 4-namespace set
            "ko/customnpcs/marker.mic",
            "ko/pda/marker.mic",
            "ko/stalker/marker.mic",
            "ko/stalkerguide/marker.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("LocalizedAssetsCatalog.load finds exactly the 5 real locale folders", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertEquals(5, catalog.totalFolderCount(), "folder count");
            for (String locale : new String[] {"en", "es", "fr", "ko", "ru"}) {
                assertTrue(catalog.folderNames().contains(locale), locale + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("foldersContaining: sample_ejection resolves under en (real dev test/sample handbook articles)", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("sample_ejection").contains("en"), "sample_ejection");
        }));

        out.add(test("foldersContaining: vybros (the emission mechanic's real Russian name) resolves under en", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("vybros").contains("en"), "vybros");
        }));

        out.add(test("foldersContaining: 2b11_strat (real 2026-dated DMZ audio) resolves under en", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("2b11_strat").contains("en"), "2b11_strat");
        }));

        out.add(test("foldersContaining: the real Reichstag calldown_soviet/calldown_wermacht set resolves under en", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("calldown_soviet").contains("en"), "calldown_soviet");
            assertTrue(catalog.foldersContaining("calldown_wermacht").contains("en"), "calldown_wermacht");
        }));

        out.add(test("filesIn: en has all 4 namespaces no other locale in this fixture has (gloomycore/stalcraft/stalkerclans/stalkersounds)", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            List<String> enFiles = catalog.filesIn("en");
            assertTrue(enFiles.contains("gloomycore/marker.mic"), "en gloomycore");
            assertTrue(enFiles.contains("stalcraft/marker.mic"), "en stalcraft");
            assertTrue(enFiles.contains("stalkerclans/marker.mic"), "en stalkerclans");
            assertTrue(enFiles.contains("stalkersounds/marker.ogg"), "en stalkersounds");
        }));

        out.add(test("filesIn: ru has stalcraft/stalkerclans/stalkersounds but this fixture has no ru gloomycore marker", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            List<String> ruFiles = catalog.filesIn("ru");
            assertTrue(ruFiles.contains("stalcraft/marker.mic"), "ru stalcraft");
            assertTrue(ruFiles.contains("stalkerclans/marker.mic"), "ru stalkerclans");
            assertTrue(ruFiles.contains("stalkersounds/marker.ogg"), "ru stalkersounds");
            assertTrue(!ruFiles.contains("gloomycore/marker.mic"), "ru has no gloomycore marker in this fixture");
        }));

        out.add(test("filesIn: ko is the real narrowest locale, only 4 namespaces in this fixture", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            List<String> koFiles = catalog.filesIn("ko");
            assertEquals(4, koFiles.size(), "ko fixture file count");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            LocalizedAssetsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("LocalizedAssetsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("localized-fixture-empty");
                dir.toFile().deleteOnExit();
                LocalizedAssetsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static LocalizedAssetsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("localized-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return LocalizedAssetsCatalog.load(dir);
    }
}
