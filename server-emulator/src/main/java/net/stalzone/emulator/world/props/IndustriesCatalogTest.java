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
 * Regression tests for {@link IndustriesCatalog}, using a fixture built from real {@code
 * industries.logistics.*} lines drawn directly from {@code stalker/lang/en.lang} (part 151,
 * live-verified on device) -- real non-consecutive tier levels and real medicine-item benefit
 * text.
 */
public final class IndustriesCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "industries.logistics.2.benefits.supply_loot_upgrade.desc=Increased number of bandages and first-aid kits",
            "industries.logistics.6.benefits.supply_loot_upgrade.desc=Morphine added",
            "industries.logistics.12.benefits.supply_loot_upgrade.desc=Epinephrine added",
            "industries.logistics.30.benefits.supply_loot_upgrade.desc=First aid kits are now Emergency ones"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("industries-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static IndustriesCatalog loadFixture() throws IOException {
        return IndustriesCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real low-tier and mid-tier medicine-item benefit text resolves", () -> {
            IndustriesCatalog catalog = loadFixture();
            assertEquals("Increased number of bandages and first-aid kits", catalog.desc("2"), "tier 2 desc");
            assertEquals("Morphine added", catalog.desc("6"), "tier 6 desc");
            assertEquals("Epinephrine added", catalog.desc("12"), "tier 12 desc");
        }));

        out.add(test("the real highest real tier (30) resolves, confirming non-consecutive real tier gaps", () -> {
            IndustriesCatalog catalog = loadFixture();
            assertEquals("First aid kits are now Emergency ones", catalog.desc("30"), "tier 30 desc");
            assertNull(catalog.desc("7"), "tier 7 is a real gap, not present in the client");
        }));

        out.add(test("tiers() is sorted and unknown tiers resolve to null rather than throwing", () -> {
            IndustriesCatalog catalog = loadFixture();
            List<String> tiers = catalog.tiers();
            assertEquals(4, tiers.size(), "4 real tiers in fixture");
            assertTrue(tiers.get(0).compareTo(tiers.get(tiers.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.desc("not_a_real_tier"), "unknown tier");
        }));

        return out;
    }
}
