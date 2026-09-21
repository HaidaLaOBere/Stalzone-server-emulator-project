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
 * Regression tests for {@link GeneralCatalog}, using a fixture built from real {@code general.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 118, live-verified on device) --
 * covering a real complete title/desc achievement pair, the real {@code legend} id that genuinely
 * has a title but no desc, the real {@code dmz_2026_quest}/{@code dmz_2026_quest.tankiWatch}
 * parent/child compound-id pair, and the real category/region/stats flat groups.
 */
public final class GeneralCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "general.achievement.id.bolota_explored.title=Beloved Marsh",
            "general.achievement.id.bolota_explored.desc=Cross the river and enter the Zone",
            "general.achievement.id.legend.title=Legend of the Zone",
            "general.achievement.id.dmz_2026_quest.title=Bottom",
            "general.achievement.id.dmz_2026_quest.desc=Congratulations! You found a unique murmurer residing in a marine environment! How did it get there...?",
            "general.achievement.id.dmz_2026_quest.tankiWatch.title=40 Tons",
            "general.achievement.id.dmz_2026_quest.tankiWatch.desc=Find all tanks on Boral",
            "general.achievement.category.quest=Quest",
            "general.region.eu=Europe",
            "general.region.na=North America",
            "general.region.sea=Southeast Asia (Singapore, Australia)",
            "general.region.nea=Northeast Asia (Korea, Japan)",
            "general.stats.stat.raffle-tickets-ny22=Participation Tickets found"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("general-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static GeneralCatalog loadFixture() throws IOException {
        return GeneralCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("title/desc resolve together for a real complete achievement", () -> {
            GeneralCatalog catalog = loadFixture();
            assertEquals("Beloved Marsh", catalog.achievementTitle("bolota_explored"), "bolota title");
            assertEquals("Cross the river and enter the Zone", catalog.achievementDesc("bolota_explored"), "bolota desc");
        }));

        out.add(test("legend has a real title but genuinely no desc", () -> {
            GeneralCatalog catalog = loadFixture();
            assertEquals("Legend of the Zone", catalog.achievementTitle("legend"), "legend title");
            assertNull(catalog.achievementDesc("legend"), "legend desc genuinely absent");
        }));

        out.add(test("dmz_2026_quest parent/child compound ids resolve independently", () -> {
            GeneralCatalog catalog = loadFixture();
            assertEquals("Bottom", catalog.achievementTitle("dmz_2026_quest"), "parent title");
            assertTrue(catalog.achievementDesc("dmz_2026_quest").startsWith("Congratulations!"), "parent desc");
            assertEquals("40 Tons", catalog.achievementTitle("dmz_2026_quest.tankiWatch"), "child title");
            assertEquals("Find all tanks on Boral", catalog.achievementDesc("dmz_2026_quest.tankiWatch"), "child desc");
        }));

        out.add(test("achievementCategoryLabel/regionName/statLabel resolve the real flat groups", () -> {
            GeneralCatalog catalog = loadFixture();
            assertEquals("Quest", catalog.achievementCategoryLabel("quest"), "category label");
            assertEquals("Europe", catalog.regionName("eu"), "region eu");
            assertEquals("Northeast Asia (Korea, Japan)", catalog.regionName("nea"), "region nea");
            assertEquals("Participation Tickets found", catalog.statLabel("raffle-tickets-ny22"), "stat label");
        }));

        out.add(test("ids/fieldNames are sorted and reflect the real fixture data", () -> {
            GeneralCatalog catalog = loadFixture();
            List<String> ids = catalog.achievementIds();
            assertEquals(4, ids.size(), "4 real achievement ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertEquals(2, catalog.achievementFieldNames("bolota_explored").size(), "bolota has title+desc");
            assertEquals(1, catalog.achievementFieldNames("legend").size(), "legend has only title");
            assertEquals(4, catalog.regionIds().size(), "4 real region ids");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            GeneralCatalog catalog = loadFixture();
            assertNull(catalog.achievementTitle("not_a_real_achievement"), "unknown achievement title");
            assertNull(catalog.achievementField("bolota_explored", "not_a_real_field"), "unknown field");
            assertNull(catalog.achievementCategoryLabel("not_real"), "unknown category");
            assertNull(catalog.regionName("not_real"), "unknown region");
            assertNull(catalog.statLabel("not_real"), "unknown stat");
            assertEquals(0, catalog.achievementFieldNames("not_a_real_achievement").size(), "unknown achievement field names");
        }));

        return out;
    }
}
