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
 * Regression tests for {@link MapLangCatalog}, using a fixture built from real {@code map.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 126, live-verified on device) --
 * one real line from each of the five groups (location, teleport, global.region, safezone, page),
 * including the real {@code cs}="Clear Sky Base" safezone find and the odd real inverted {@code
 * page.id.east.name}="West" label.
 */
public final class MapLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "map.location.id.bolota.name=The Swamps",
            "map.location.id.kordon.name=Roadside",
            "map.teleport.id.bolota-kordon.name=The Swamps—The Roadside",
            "map.global.region.sever.name=North Zone",
            "map.global.region.sever.desc=Full-fledged colonization of the Northern territories began relatively recently.",
            "map.global.region.dmz.name=Boral",
            "map.safezone.id.bar.name=Pervomaisky Factory",
            "map.safezone.id.cs.name=Clear Sky Base",
            "map.page.id.east.name=West",
            "map.page.id.limansk.name=Lyubech-3"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("map-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static MapLangCatalog loadFixture() throws IOException {
        return MapLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("location names resolve and ids() is sorted", () -> {
            MapLangCatalog catalog = loadFixture();
            assertEquals("The Swamps", catalog.locationName("bolota"), "bolota");
            assertEquals("Roadside", catalog.locationName("kordon"), "kordon");
            List<String> ids = catalog.locationIds();
            assertEquals(2, ids.size(), "2 real location ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(1)) < 0, "sorted ascending");
        }));

        out.add(test("a real named fast-travel route label resolves", () -> {
            MapLangCatalog catalog = loadFixture();
            assertEquals("The Swamps—The Roadside", catalog.teleportName("bolota-kordon"), "bolota-kordon route");
        }));

        out.add(test("real global-region name/desc resolve, a third distinct 'region' concept", () -> {
            MapLangCatalog catalog = loadFixture();
            assertEquals("North Zone", catalog.globalRegionName("sever"), "sever region name");
            assertTrue(catalog.globalRegionDesc("sever").startsWith("Full-fledged colonization"), "sever region desc");
            assertEquals("Boral", catalog.globalRegionName("dmz"), "dmz region name (real DMZ-mode island)");
        }));

        out.add(test("real safezone names resolve, including the Clear Sky Base find", () -> {
            MapLangCatalog catalog = loadFixture();
            assertEquals("Pervomaisky Factory", catalog.safezoneName("bar"), "bar safezone (matches SettlementRegistry)");
            assertEquals("Clear Sky Base", catalog.safezoneName("cs"), "cs safezone -- distinct from settlement id csky");
        }));

        out.add(test("real page labels resolve, including the odd inverted 'east'=West line", () -> {
            MapLangCatalog catalog = loadFixture();
            assertEquals("West", catalog.pageName("east"), "east page label is literally West, disclosed as-is");
            assertEquals("Lyubech-3", catalog.pageName("limansk"), "limansk page label matches the global region name");
        }));

        out.add(test("unknown ids across every group resolve to null/empty rather than throwing", () -> {
            MapLangCatalog catalog = loadFixture();
            assertNull(catalog.locationName("not_a_real_location"), "unknown location");
            assertNull(catalog.teleportName("not-a-real-route"), "unknown route");
            assertNull(catalog.globalRegionName("not_a_real_region"), "unknown region");
            assertNull(catalog.safezoneName("not_a_real_safezone"), "unknown safezone");
            assertNull(catalog.pageName("not_a_real_page"), "unknown page");
        }));

        return out;
    }
}
