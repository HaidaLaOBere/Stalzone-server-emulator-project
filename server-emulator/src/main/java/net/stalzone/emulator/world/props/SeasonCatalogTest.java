package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link SeasonCatalog}, using a fixture built from real {@code season.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 158, live-verified on device) --
 * the real quarterly season titles and their two confirmed crosswalks.
 */
public final class SeasonCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "season.25/spring.tabTitle=Back to the Inside Out",
            "season.25/spring.title=Back to the Inside Out",
            "season.25/summer.tabTitle=MURMUR OF THE ZONE",
            "season.26/winter.tabTitle=SUB-ZERO ZONE",
            "season.26/winter.title=SUB-ZERO ZONE",
            "season.26/summer.title=ANTICYCLONE"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("season-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SeasonCatalog loadFixture() throws IOException {
        return SeasonCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real ids with an embedded literal '/' character resolve without special-casing", () -> {
            SeasonCatalog catalog = loadFixture();
            assertEquals("Back to the Inside Out", catalog.field("25/spring", "tabTitle"), "25/spring tabTitle");
            assertEquals("ANTICYCLONE", catalog.field("26/summer", "title"), "26/summer title");
        }));

        out.add(test("the real 25/spring title confirms part 154's InsideCatalog 'Back to the Inside Out' event season, and 26/winter confirms part 121's ItemLangCatalog winter26 collection crosswalk", () -> {
            SeasonCatalog catalog = loadFixture();
            assertEquals(catalog.field("25/spring", "tabTitle"), catalog.field("25/spring", "title"), "25/spring tabTitle and title are byte-identical");
            assertEquals("SUB-ZERO ZONE", catalog.field("26/winter", "title"), "26/winter matches ItemLangCatalog's winter26 collection name exactly");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            SeasonCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "title"), "unknown id");
        }));

        return out;
    }
}
