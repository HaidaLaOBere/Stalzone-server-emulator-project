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
 * Regression tests for {@link LoadscreenCatalog}, using a fixture built from real {@code
 * loadscreens.*} lines drawn directly from {@code stalker/lang/en.lang} (part 110, live-verified
 * on device) -- covering the real 5-tier danger classification, the real per-location
 * name/desc/feature crosswalk, the cross-namespace naming variances for {@code poligon2}/{@code
 * battlefield_krot2_open}, and the new real {@code battlefield_promzona_open} battlefield map.
 */
public final class LoadscreenCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "loadscreens.type.peace.name=Peaceful location",
            "loadscreens.type.peace.desc=Only players from enemy factions can be killed in this location",
            "loadscreens.type.wild.name=Wild location",
            "loadscreens.type.wild.desc=There are no laws in this location. You can kill anyone with impunity",
            "loadscreens.location.yanov.name=Uranus Mill",
            "loadscreens.location.yanov.desc=The area around the Uranus Mill was first explored by stalkers in the early 2020s.",
            "loadscreens.location.yanov.feature.0=Near the bucket excavator, you can find a scientific bunker.",
            "loadscreens.location.yanov.feature.1=Stories about the underpass under the mill are known to many.",
            "loadscreens.location.yanov.feature.2=The only thing stopping diggers is the difficulty of transporting scrap.",
            "loadscreens.location.poligon2.name=Reactor 4",
            "loadscreens.location.battlefield_krot2_open.name=Mole-2 System",
            "loadscreens.location.battlefield_promzona_open.name=Industrial Zone",
            "loadscreens.location.zaton.name=Stillwaters",
            "loadscreens.battlefield_breakthrough_reich.name=Reichstag"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("loadscreen-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static LoadscreenCatalog loadFixture() throws IOException {
        return LoadscreenCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("typeName/typeDescription resolve the real peace and wild danger tiers", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertEquals("Peaceful location", catalog.typeName("peace"), "peace name");
            assertTrue(catalog.typeDescription("peace").contains("enemy factions"), "peace desc");
            assertEquals("Wild location", catalog.typeName("wild"), "wild name");
            assertTrue(catalog.typeDescription("wild").contains("no laws"), "wild desc");
        }));

        out.add(test("locationName/locationDescription resolve the real yanov entry", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertEquals("Uranus Mill", catalog.locationName("yanov"), "yanov name");
            assertTrue(catalog.locationDescription("yanov").contains("early 2020s"), "yanov desc");
        }));

        out.add(test("locationFeatures resolves the real numbered feature call-outs in order", () -> {
            LoadscreenCatalog catalog = loadFixture();
            List<String> features = catalog.locationFeatures("yanov");
            assertEquals(3, features.size(), "feature count");
            assertTrue(features.get(0).contains("bucket excavator"), "feature 0");
            assertTrue(features.get(1).contains("underpass"), "feature 1");
            assertTrue(features.get(2).contains("scrap"), "feature 2");
        }));

        out.add(test("locationFeatures returns empty (not null) for a location with no feature lines", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertEquals(0, catalog.locationFeatures("zaton").size(), "zaton has no features in fixture");
            assertEquals(0, catalog.locationFeatures("not_a_real_location").size(), "unknown location");
        }));

        out.add(test("poligon2/battlefield_krot2_open resolve their real, distinctly-named loadscreen variants", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertEquals("Reactor 4", catalog.locationName("poligon2"), "poligon2 loadscreen name");
            assertEquals("Mole-2 System", catalog.locationName("battlefield_krot2_open"), "krot2 loadscreen name");
        }));

        out.add(test("battlefield_promzona_open resolves the new real 24th battlefield map name", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertEquals("Industrial Zone", catalog.locationName("battlefield_promzona_open"), "promzona");
        }));

        out.add(test("locationIds/typeIds are sorted and contain the real fixture ids", () -> {
            LoadscreenCatalog catalog = loadFixture();
            List<String> locationIds = catalog.locationIds();
            assertTrue(locationIds.contains("yanov"), "contains yanov");
            for (int i = 1; i < locationIds.size(); i++) {
                assertTrue(locationIds.get(i - 1).compareTo(locationIds.get(i)) < 0, "sorted");
            }
            List<String> typeIds = catalog.typeIds();
            assertTrue(typeIds.contains("peace"), "contains peace");
            assertTrue(typeIds.contains("wild"), "contains wild");
        }));

        out.add(test("unknown ids resolve to null/empty across all lookups", () -> {
            LoadscreenCatalog catalog = loadFixture();
            assertNull(catalog.typeName("not_a_real_type"), "unknown type");
            assertNull(catalog.locationName("not_a_real_location"), "unknown location name");
            assertNull(catalog.locationDescription("not_a_real_location"), "unknown location desc");
        }));

        return out;
    }
}
