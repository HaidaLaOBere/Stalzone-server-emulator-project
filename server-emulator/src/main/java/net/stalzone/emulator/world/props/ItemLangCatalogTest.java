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
 * Regression tests for {@link ItemLangCatalog}, using a fixture built from real {@code item.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 121, live-verified on device) --
 * covering a real firearm name, a deeply nested quest-reward lore id, and the real seasonal
 * customisation-collection crosswalk.
 */
public final class ItemLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "item.wpn.kedr.name=PP-91 KEDR",
            "item.wpn.mp5.name=HK MP5",
            "item.wpn.aks74.name=AKS-74",
            "item.wpn.aks74.description=A compact Soviet assault rifle.",
            "item.custom.internal.16022.12177.0.lore=§rSix! That thing has six eyes!",
            "item.customisation.collection.summer24=ZIVCAS Corporation",
            "item.customisation.collection.winter26=Sub-Zero Zone",
            "item.customisation.collection.triumph26=Triumphs. Faction Race 2026",
            "item.warning.stolen=This item does not belong to you. Sale price reduced",
            "item.amm.temporary=Ammunition stats are temporary"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("item-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ItemLangCatalog loadFixture() throws IOException {
        return ItemLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("name/description resolve together for a real dual-field weapon id", () -> {
            ItemLangCatalog catalog = loadFixture();
            assertEquals("AKS-74", catalog.name("wpn.aks74"), "aks74 name");
            assertEquals("A compact Soviet assault rifle.", catalog.description("wpn.aks74"), "aks74 description");
            assertEquals("PP-91 KEDR", catalog.name("wpn.kedr"), "kedr name");
            assertEquals("HK MP5", catalog.name("wpn.mp5"), "mp5 name");
        }));

        out.add(test("lore resolves for a real deeply-nested compound id", () -> {
            ItemLangCatalog catalog = loadFixture();
            assertEquals("§rSix! That thing has six eyes!", catalog.lore("custom.internal.16022.12177.0"), "deep lore");
        }));

        out.add(test("real seasonal customisation-collection crosswalk resolves", () -> {
            ItemLangCatalog catalog = loadFixture();
            assertEquals("ZIVCAS Corporation", catalog.field("customisation.collection", "summer24"), "summer24");
            assertEquals("Sub-Zero Zone", catalog.field("customisation.collection", "winter26"), "winter26");
            assertEquals("Triumphs. Faction Race 2026", catalog.field("customisation.collection", "triumph26"), "triumph26");
        }));

        out.add(test("flat-looking real single-field ids resolve via the same generic split", () -> {
            ItemLangCatalog catalog = loadFixture();
            assertEquals("This item does not belong to you. Sale price reduced",
                    catalog.field("warning", "stolen"), "warning.stolen");
            assertEquals("Ammunition stats are temporary", catalog.field("amm", "temporary"), "amm.temporary");
        }));

        out.add(test("ids/fieldNames are sorted and reflect the real fixture data", () -> {
            ItemLangCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(7, ids.size(), "7 real item ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertEquals(2, catalog.fieldNames("wpn.aks74").size(), "aks74 has name+description");
            assertEquals(3, catalog.fieldNames("customisation.collection").size(), "3 seasonal collection fields");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            ItemLangCatalog catalog = loadFixture();
            assertNull(catalog.name("wpn.not_a_real_weapon"), "unknown weapon name");
            assertNull(catalog.field("wpn.aks74", "not_a_real_field"), "unknown field");
            assertEquals(0, catalog.fieldNames("wpn.not_a_real_weapon").size(), "unknown weapon field names");
        }));

        return out;
    }
}
