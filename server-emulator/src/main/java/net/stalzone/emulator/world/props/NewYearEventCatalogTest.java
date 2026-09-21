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
 * Regression tests for {@link NewYearEventCatalog}, using a fixture built from real {@code ny.*}
 * and {@code ny_toys.*} lines drawn directly from {@code stalker/lang/en.lang} (part 157,
 * live-verified on device) -- the real 6-tier leagues ladder (including its real snow/wood
 * copy-paste bug), tutorial cards, and decoration set names.
 */
public final class NewYearEventCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "ny.leaderboardRewardTitle=Reward",
            "ny.reward_text_without_league=Reward for X-Mas Tree 2025 Event",
            "ny.leagues.ice.name=Ice League",
            "ny.leagues.ice.short_name=Ice",
            "ny.leagues.wood.reward_text=...you have managed to reach the Wooden League!...",
            "ny.leagues.snow.name=Snow League",
            "ny.leagues.snow.reward_text=...you have managed to reach the Wooden League!...",
            "ny.tutorial.ny_visit_tree.header=Christmas Tree",
            "ny.tutorial.ny_trader_visit.hint=To purchase themed loot you need Pine Cones.",
            "ny.rewardmap.tier1.name=Common reward",
            "ny_toys.setA=Red Decorations",
            "ny_toys.setQ=Soviet Decorations",
            "ny_toys.setM=Digger's Decorations"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("new-year-event-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static NewYearEventCatalog loadFixture() throws IOException {
        return NewYearEventCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real flat zero-dot ny.* fields resolve via the carve-out, and real leagues ladder entries resolve under the generic table", () -> {
            NewYearEventCatalog catalog = loadFixture();
            assertEquals("Reward", catalog.flatField("leaderboardRewardTitle"), "flat leaderboardRewardTitle");
            assertTrue(catalog.flatField("reward_text_without_league").contains("X-Mas Tree 2025"), "flat reward_text_without_league");
            assertEquals("Ice League", catalog.field("leagues.ice", "name"), "ice league name");
            assertEquals("Ice", catalog.field("leagues.ice", "short_name"), "ice league short_name");
        }));

        out.add(test("the real snow/wood reward_text copy-paste bug is preserved exactly as authored, not corrected", () -> {
            NewYearEventCatalog catalog = loadFixture();
            assertEquals("Snow League", catalog.field("leagues.snow", "name"), "snow league's own real name");
            assertEquals(catalog.field("leagues.wood", "reward_text"), catalog.field("leagues.snow", "reward_text"), "real byte-identical copy-paste bug between wood and snow reward_text");
            assertTrue(catalog.field("leagues.snow", "reward_text").contains("Wooden League"), "snow's reward_text really references the Wooden League due to the bug");
        }));

        out.add(test("real tutorial/rewardmap fields and real ny_toys set names resolve, ids()/toySetIds() are sorted, and unknown lookups resolve to null", () -> {
            NewYearEventCatalog catalog = loadFixture();
            assertEquals("Christmas Tree", catalog.field("tutorial.ny_visit_tree", "header"), "tutorial header");
            assertEquals("Common reward", catalog.field("rewardmap.tier1", "name"), "rewardmap tier1 name");
            assertEquals("Red Decorations", catalog.toySetName("setA"), "ny_toys setA name");
            assertEquals("Soviet Decorations", catalog.toySetName("setQ"), "ny_toys setQ name (real non-alphabetical id)");
            List<String> ids = catalog.ids();
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "ids sorted ascending");
            List<String> toySetIds = catalog.toySetIds();
            assertEquals(3, toySetIds.size(), "3 real ny_toys set ids in fixture");
            assertTrue(toySetIds.get(0).compareTo(toySetIds.get(toySetIds.size() - 1)) <= 0, "toySetIds sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
            assertNull(catalog.flatField("not_a_real_field"), "unknown flat field");
            assertNull(catalog.toySetName("not_a_real_set"), "unknown set id");
        }));

        return out;
    }
}
