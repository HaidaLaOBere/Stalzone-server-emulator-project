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
 * Regression tests for {@link DeliveryCatalog}, using a fixture built from real {@code
 * delivery.*} lines drawn directly from {@code stalker/lang/en.lang} (part 136, live-verified on
 * device) -- a real premium/non-premium pair sharing a byte-identical name, and a real north
 * sub-zone entry.
 */
public final class DeliveryCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "delivery.abstract_agroprom_bandit.name=Kolos Agrocomplex",
            "delivery.abstract_agroprom_bandit_premium.name=Kolos Agrocomplex",
            "delivery.abstract_north_forge.name=Forge-11",
            "delivery.abstract_wnorth.name=Wild Northern Locations"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("delivery-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static DeliveryCatalog loadFixture() throws IOException {
        return DeliveryCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real premium/non-premium contract pair shares a byte-identical name", () -> {
            DeliveryCatalog catalog = loadFixture();
            String base = catalog.name("abstract_agroprom_bandit");
            String premium = catalog.name("abstract_agroprom_bandit_premium");
            assertEquals("Kolos Agrocomplex", base, "non-premium name");
            assertEquals(base, premium, "premium variant shares the same real name");
        }));

        out.add(test("real north sub-zone contract names resolve independently", () -> {
            DeliveryCatalog catalog = loadFixture();
            assertEquals("Forge-11", catalog.name("abstract_north_forge"), "north_forge name");
            assertEquals("Wild Northern Locations", catalog.name("abstract_wnorth"), "wnorth name");
        }));

        out.add(test("ids() is sorted and unknown ids resolve to null rather than throwing", () -> {
            DeliveryCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real contract ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.name("abstract_not_a_real_contract"), "unknown contract id");
        }));

        return out;
    }
}
