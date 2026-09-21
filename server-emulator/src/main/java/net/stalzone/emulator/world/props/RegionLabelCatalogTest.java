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
 * Regression tests for {@link RegionLabelCatalog}, using the real, complete 4-line {@code
 * region.*} namespace (part 176, live-verified against the full {@code en.lang} file).
 */
public final class RegionLabelCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "region.stalkers_bolota.name=Ataman's HQ",
            "region.bandits_bolota.name=Boathouse",
            "region.stalkers_kordon.name=Fire Station",
            "region.bandits_kordon.name=Village School"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("region-label-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static RegionLabelCatalog loadFixture() throws IOException {
        return RegionLabelCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real names resolve for all 4 real ids, corroborating 4 already-known settlements from a fourth angle", () -> {
            RegionLabelCatalog catalog = loadFixture();
            assertEquals("Ataman's HQ", catalog.name("stalkers_bolota"), "stalkers_bolota -> Ataman's HQ, matching SettlementRegistry's csky");
            assertEquals("Boathouse", catalog.name("bandits_bolota"), "bandits_bolota -> Boathouse, matching lodochnaya");
            assertEquals("Fire Station", catalog.name("stalkers_kordon"), "stalkers_kordon -> Fire Station, matching garage");
            assertEquals("Village School", catalog.name("bandits_kordon"), "bandits_kordon -> Village School, matching foton2");
        }));

        out.add(test("factionId/zoneId split the real compound id on its first underscore", () -> {
            RegionLabelCatalog catalog = loadFixture();
            assertEquals("stalkers", catalog.factionId("stalkers_bolota"), "faction half");
            assertEquals("bolota", catalog.zoneId("stalkers_bolota"), "zone half");
            assertEquals("bandits", catalog.factionId("bandits_kordon"), "faction half 2");
            assertEquals("kordon", catalog.zoneId("bandits_kordon"), "zone half 2");
        }));

        out.add(test("both zones (bolota, kordon) carry one stalkers-labeled and one bandits-labeled entry with genuinely different real names", () -> {
            RegionLabelCatalog catalog = loadFixture();
            assertTrue(!catalog.name("stalkers_bolota").equals(catalog.name("bandits_bolota")), "bolota: two distinct real names, not the same settlement renamed");
            assertTrue(!catalog.name("stalkers_kordon").equals(catalog.name("bandits_kordon")), "kordon: two distinct real names, not the same settlement renamed");
        }));

        out.add(test("ids() is sorted and contains all 4 real ids; unknown lookups resolve to null; size() matches the real full-file count", () -> {
            RegionLabelCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted at " + i);
            }
            assertTrue(ids.contains("stalkers_bolota") && ids.contains("bandits_kordon"), "real ids present");
            assertNull(catalog.name("not_a_real_id"), "unknown id resolves to null");
            assertNull(catalog.factionId("nounderscore"), "no underscore resolves factionId to null");
            assertNull(catalog.zoneId("nounderscore"), "no underscore resolves zoneId to null");
            assertEquals(4, catalog.size(), "4 real lines total, matching the real full file exactly");
        }));

        return out;
    }
}
