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
 * Regression tests for {@link ItemOriginsCatalog}, using a fixture built from real {@code
 * item_origins.*} lines drawn directly from {@code stalker/lang/en.lang} (part 139,
 * live-verified on device) -- the real dmz_league/inside_league crosswalk pair for the same
 * "picklock" tier codename, a real nested 2-segment id, and the real arsenal corroboration.
 */
public final class ItemOriginsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "item_origins.dmz_league_picklock.name=reward for entering the Market Recruits league in the Black Raid event ranking (2024)",
            "item_origins.inside_league_picklock.name=reward for entering the Shadow league in the Back to the Inside Out event ranking (2025)",
            "item_origins.bag.murmur.name=- Ranked reward for the Black Raid event (Fall 2024)",
            "item_origins.arsenal.name=Arsenal Workshop",
            "item_origins.season_shop.item_origin_tip=purchase in the Shop for Season currency"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("item-origins-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ItemOriginsCatalog loadFixture() throws IOException {
        return ItemOriginsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the same real 'picklock' tier codename resolves two genuinely different real league texts", () -> {
            ItemOriginsCatalog catalog = loadFixture();
            String dmzText = catalog.name("dmz_league_picklock");
            String insideText = catalog.name("inside_league_picklock");
            assertTrue(dmzText.contains("Market Recruits"), "dmz_league_picklock names the real Market Recruits league");
            assertTrue(insideText.contains("Shadow"), "inside_league_picklock names the real Shadow league");
            assertTrue(!dmzText.equals(insideText), "the two picklock-tier texts are genuinely different real events");
        }));

        out.add(test("a real nested 2-segment id and the real arsenal corroboration resolve", () -> {
            ItemOriginsCatalog catalog = loadFixture();
            assertEquals("- Ranked reward for the Black Raid event (Fall 2024)", catalog.name("bag.murmur"), "bag.murmur name");
            assertEquals("Arsenal Workshop", catalog.name("arsenal"), "arsenal name");
            assertEquals("purchase in the Shop for Season currency", catalog.field("season_shop", "item_origin_tip"), "season_shop item_origin_tip");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            ItemOriginsCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "5 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.name("not_a_real_origin"), "unknown origin id");
            assertNull(catalog.field("arsenal", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
