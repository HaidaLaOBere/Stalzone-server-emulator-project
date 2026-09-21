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
 * Regression tests for {@link SeasonsCatalog}, using a fixture built from real {@code
 * seasons.<id>.title} lines drawn directly from {@code stalker/lang/en.lang} (part 145,
 * live-verified on device) -- a real bare-month-year title, a real multi-month span using an en
 * dash, and the real duplicate "Off-Season" pair.
 */
public final class SeasonsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "seasons.january2024.title=January 2024",
            "seasons.june2024.title=June",
            "seasons.july-august2024.title=July–August",
            "seasons.cw_chill_0.title=Off-Season",
            "seasons.cw_chill_1.title=Off-Season"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("seasons-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SeasonsCatalog loadFixture() throws IOException {
        return SeasonsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real bare month-year title and a real bare-month-only title resolve", () -> {
            SeasonsCatalog catalog = loadFixture();
            assertEquals("January 2024", catalog.title("january2024"), "january2024 title");
            assertEquals("June", catalog.title("june2024"), "june2024 title (bare month, no year)");
        }));

        out.add(test("a real multi-month span using an en dash resolves", () -> {
            SeasonsCatalog catalog = loadFixture();
            assertEquals("July–August", catalog.title("july-august2024"), "july-august2024 title");
        }));

        out.add(test("the real duplicate Off-Season pair resolves as distinct ids sharing literal text, ids() is sorted, and unknown ids resolve to null rather than throwing", () -> {
            SeasonsCatalog catalog = loadFixture();
            assertEquals("Off-Season", catalog.title("cw_chill_0"), "cw_chill_0 title");
            assertEquals("Off-Season", catalog.title("cw_chill_1"), "cw_chill_1 title");
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "5 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.title("not_a_real_season"), "unknown season id");
        }));

        return out;
    }
}
