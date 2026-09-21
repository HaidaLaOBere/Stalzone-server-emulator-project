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
 * Regression tests for {@link HomeOutpostCatalog}, using a fixture built from the real, complete
 * 6-line {@code pda.waypoint.home_outpost.*} namespace drawn directly from {@code
 * stalker/lang/en.lang} (part 114, live-verified on device).
 */
public final class HomeOutpostCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "pda.waypoint.home_outpost.merc=Mercenary Headquarters",
            "pda.waypoint.home_outpost.freedom=Rise Headquarters",
            "pda.waypoint.home_outpost.duty=Frontier Headquarters",
            "pda.waypoint.home_outpost.covenant=Covenant Headquarters",
            "pda.waypoint.home_outpost.stalkers=Stalker Headquarters",
            "pda.waypoint.home_outpost.bandits=Bandit Headquarters"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("home-outpost-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static HomeOutpostCatalog loadFixture() throws IOException {
        return HomeOutpostCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("headquartersName resolves the real 6-faction roster, including duty/freedom naming agreement", () -> {
            HomeOutpostCatalog catalog = loadFixture();
            assertEquals("Frontier Headquarters", catalog.headquartersName("duty"), "duty");
            assertEquals("Rise Headquarters", catalog.headquartersName("freedom"), "freedom");
            assertEquals("Mercenary Headquarters", catalog.headquartersName("merc"), "merc");
            assertEquals("Covenant Headquarters", catalog.headquartersName("covenant"), "covenant");
        }));

        out.add(test("headquartersName resolves the two factions absent from the 4-faction clan-war roster", () -> {
            HomeOutpostCatalog catalog = loadFixture();
            assertEquals("Stalker Headquarters", catalog.headquartersName("stalkers"), "stalkers");
            assertEquals("Bandit Headquarters", catalog.headquartersName("bandits"), "bandits");
        }));

        out.add(test("factionIds/factionCount reflect the real complete 6-entry fixture, sorted", () -> {
            HomeOutpostCatalog catalog = loadFixture();
            assertEquals(6, catalog.factionCount(), "faction count");
            List<String> ids = catalog.factionIds();
            assertEquals(6, ids.size(), "id list size");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
        }));

        out.add(test("unknown faction id resolves to null", () -> {
            HomeOutpostCatalog catalog = loadFixture();
            assertNull(catalog.headquartersName("not_a_real_faction"), "unknown faction");
        }));

        return out;
    }
}
