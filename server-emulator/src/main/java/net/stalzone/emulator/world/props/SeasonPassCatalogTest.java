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
 * Regression tests for {@link SeasonPassCatalog}, using a fixture built from real {@code
 * seasonpass.*} lines drawn directly from {@code stalker/lang/en.lang} (part 140, live-verified on
 * device) -- the real {@code task.category.<id>} double-use quirk (a leaf label that is ALSO a
 * prefix for {@code .desc}), the real "FROST TROOPERS" category name, and the real "Phil" NPC
 * quest-condition text.
 */
public final class SeasonPassCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "seasonpass.task.category.event=FROST TROOPERS",
            "seasonpass.task.category.event.desc=Tasks in the FROST TROOPERS category include activities in a special game mode",
            "seasonpass.task.category.event.completionTextInfo=FROST TROOPERS EVENT",
            "seasonpass.task.category.event.conditionTextInfo=VISIT A SAFE ZONE ON THE ROADSIDE",
            "seasonpass.task.category.operations=OPERATIONS",
            "seasonpass.task.category.operations.conditionTextInfo=GET PHIL'S QUEST AT THE BAR",
            "seasonpass.season_trader.name=Exchanger",
            "seasonpass.bp.option.bonus50.title=+ 50 LEVELS",
            "seasonpass.bp.option.bonus50.description=Get the Season Pass and +50 Levels for it",
            "seasonpass.daily_bonus.description.summer25.1=Up to {cellsAmount} times per day, by completing any daily quests from the categories, you can earn even more season points."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("seasonpass-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SeasonPassCatalog loadFixture() throws IOException {
        return SeasonPassCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real task.category double-use quirk resolves both the leaf label and its nested .desc as separate ids", () -> {
            SeasonPassCatalog catalog = loadFixture();
            assertEquals("FROST TROOPERS", catalog.field("task.category", "event"), "task.category leaf label 'event'");
            assertEquals("Tasks in the FROST TROOPERS category include activities in a special game mode",
                    catalog.field("task.category.event", "desc"), "task.category.event.desc, a distinct nested id");
            assertEquals("FROST TROOPERS EVENT", catalog.field("task.category.event", "completionTextInfo"), "task.category.event.completionTextInfo");
            assertEquals("VISIT A SAFE ZONE ON THE ROADSIDE", catalog.field("task.category.event", "conditionTextInfo"), "task.category.event.conditionTextInfo");
        }));

        out.add(test("the real 'Phil' NPC quest-condition text and season_trader/bp.option fields resolve", () -> {
            SeasonPassCatalog catalog = loadFixture();
            assertEquals("GET PHIL'S QUEST AT THE BAR", catalog.field("task.category.operations", "conditionTextInfo"), "operations conditionTextInfo names Phil");
            assertEquals("Exchanger", catalog.field("season_trader", "name"), "season_trader name");
            assertEquals("+ 50 LEVELS", catalog.field("bp.option.bonus50", "title"), "bp.option.bonus50 title");
            assertEquals("Get the Season Pass and +50 Levels for it", catalog.field("bp.option.bonus50", "description"), "bp.option.bonus50 description");
        }));

        out.add(test("a real digit-suffixed daily_bonus field resolves, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            SeasonPassCatalog catalog = loadFixture();
            assertEquals("Up to {cellsAmount} times per day, by completing any daily quests from the categories, you can earn even more season points.",
                    catalog.field("daily_bonus.description.summer25", "1"), "daily_bonus.description.summer25 field '1'");
            List<String> ids = catalog.ids();
            assertTrue(ids.size() > 0, "at least one real id in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
            assertNull(catalog.field("season_trader", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
