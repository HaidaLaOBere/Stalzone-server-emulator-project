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
 * Regression tests for {@link WorldEventCatalog}, using a fixture built from real {@code we.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 111, live-verified on device) --
 * covering the real faction/mutant camp roster (including the Murmur corroboration), the
 * stalker-vs-bandit clash event, the real mutant-boss lair naming variance, and the New Year 2023
 * seasonal boss roster.
 */
public final class WorldEventCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "we.abstract_camp_bandit.status_title=Bandit Encampment",
            "we.abstract_camp_bandit_large.status_title=Bandit Camp",
            "we.abstract_camp_monolit.status_title=Murmur Squad",
            "we.abstract_camp_monolit_large.status_title=Murmur Base",
            "we.abstract_camp_krovosos.status_title=Ghoul Nest",
            "we.abstract_camp_inside_krovosos.status_title=Inside Out Ghoul Den",
            "we.abstract_clash_st_ba.help_stalkers=Help stalkers in battle",
            "we.abstract_clash_st_ba.help_bandits=Help bandits in battle",
            "we.abstract_clash_st_ba.stalkers_won=The stalkers survived and offer a reward for helping",
            "we.abstract_clash_st_ba.bandits_lost=The bandits died!",
            "we.abstract_clash_st_ba.notification_text=Anyone there? Help!",
            "we.abstract_clash_st_ba.goal=Help allies",
            "we.boss_pseudogigant.status_title=Behemoth's Lair",
            "we.boss_pseudogigant.status_desc=Kill the Boss",
            "we.limansk_pseudogigant_boss.status_title=Giant Rat Den",
            "we.limansk_pseudogigant_boss.goal=Clear out the area",
            "we.ny23_boss_FightClub.status_title=Weather Forecast",
            "we.ny23_boss_shrek.status_title=Krampus",
            "we.ny23_boss_rescue_group.goal=Don't let the scientists die",
            "we.ny23_boss_rescue_group.task_text=Help the scientists",
            "we.ny23_boss_rescue_group.failure_text=The scientists died!",
            "we.ny23_boss_rescue_group.success_text=The scientists survived and offer a reward",
            "we.ny23_boss_rescue_group.notification_text=Anyone there? I need help!",
            "we.shopot_exit_goal=Clear the area and leave the location",
            "we.abstract_rescue_duty.goal=Don't let the Frontier soldiers die",
            "we.abstract_rescue_duty.task_text=Help the Frontier soldiers",
            "we.abstract_rescue_duty.failure_text=The Rise soldiers died!",
            "we.abstract_rescue_duty.success_text=The Rise soldiers survived and offer a reward",
            "we.abstract_rescue_freedom.goal=Don't let the Rise soldiers die",
            "we.abstract_rescue_freedom.task_text=Help the Rise soldiers",
            "we.abstract_rescue_freedom.failure_text=The Rise soldiers died!",
            "we.abstract_rescue_freedom.success_text=The Rise soldiers survived and offer a reward"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("world-event-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static WorldEventCatalog loadFixture() throws IOException {
        return WorldEventCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("statusTitle resolves the real faction camp roster including the Murmur corroboration", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Bandit Encampment", catalog.statusTitle("abstract_camp_bandit"), "bandit");
            assertEquals("Bandit Camp", catalog.statusTitle("abstract_camp_bandit_large"), "bandit large");
            assertEquals("Murmur Squad", catalog.statusTitle("abstract_camp_monolit"), "monolit");
            assertEquals("Murmur Base", catalog.statusTitle("abstract_camp_monolit_large"), "monolit large");
        }));

        out.add(test("statusTitle resolves the real Inside Out mutant camp variant", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Ghoul Nest", catalog.statusTitle("abstract_camp_krovosos"), "krovosos");
            assertEquals("Inside Out Ghoul Den", catalog.statusTitle("abstract_camp_inside_krovosos"), "inside krovosos");
        }));

        out.add(test("the real stalker-vs-bandit clash event resolves its full field set via field()", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Help stalkers in battle", catalog.field("abstract_clash_st_ba", "help_stalkers"), "help_stalkers");
            assertEquals("The stalkers survived and offer a reward for helping", catalog.field("abstract_clash_st_ba", "stalkers_won"), "stalkers_won");
            assertEquals("The bandits died!", catalog.field("abstract_clash_st_ba", "bandits_lost"), "bandits_lost");
            assertEquals("Anyone there? Help!", catalog.notificationText("abstract_clash_st_ba"), "notification");
            assertEquals("Help allies", catalog.goal("abstract_clash_st_ba"), "goal");
        }));

        out.add(test("the same real mutant boss resolves two distinct lair names in two contexts", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Behemoth's Lair", catalog.statusTitle("boss_pseudogigant"), "abstract lair name");
            assertEquals("Giant Rat Den", catalog.statusTitle("limansk_pseudogigant_boss"), "limansk lair name");
        }));

        out.add(test("the real NY23 seasonal boss roster resolves, including a full rescue sub-event chain", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Weather Forecast", catalog.statusTitle("ny23_boss_FightClub"), "FightClub");
            assertEquals("Krampus", catalog.statusTitle("ny23_boss_shrek"), "shrek");
            assertEquals("Don't let the scientists die", catalog.goal("ny23_boss_rescue_group"), "rescue goal");
            assertEquals("The scientists survived and offer a reward", catalog.successText("ny23_boss_rescue_group"), "rescue success");
        }));

        out.add(test("eventIds/fieldNames are sorted and reflect the real fixture data", () -> {
            WorldEventCatalog catalog = loadFixture();
            List<String> ids = catalog.eventIds();
            assertTrue(ids.contains("abstract_camp_bandit"), "contains bandit camp");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted");
            }
            List<String> fields = catalog.fieldNames("abstract_clash_st_ba");
            assertTrue(fields.contains("stalkers_won"), "clash fields contain stalkers_won");
            assertTrue(fields.contains("goal"), "clash fields contain goal");
        }));

        out.add(test("the real flat shopot_exit_goal line is NOT force-parsed into the (id, field) model", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals(0, catalog.fieldNames("shopot_exit_goal").size(), "no fields for the flat key's own text as an id");
        }));

        out.add(test("Part 177: the real abstract_rescue_duty copy-paste bug is captured exactly as found, not silently corrected", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertEquals("Don't let the Frontier soldiers die", catalog.goal("abstract_rescue_duty"), "duty's own goal correctly names Frontier");
            assertEquals("Help the Frontier soldiers", catalog.taskText("abstract_rescue_duty"), "duty's own task_text correctly names Frontier");
            assertEquals("The Rise soldiers died!", catalog.failureText("abstract_rescue_duty"), "duty's failure_text is a real byte-identical copy of freedom's, not Frontier-flavored");
            assertEquals("The Rise soldiers survived and offer a reward", catalog.successText("abstract_rescue_duty"), "duty's success_text is likewise a real byte-identical copy of freedom's");
            assertEquals(catalog.failureText("abstract_rescue_duty"), catalog.failureText("abstract_rescue_freedom"), "confirms the real duplication is byte-exact against the freedom sibling");
            assertEquals(catalog.successText("abstract_rescue_duty"), catalog.successText("abstract_rescue_freedom"), "confirms the real duplication is byte-exact against the freedom sibling");
        }));

        out.add(test("unknown ids/fields resolve to null/empty across all lookups", () -> {
            WorldEventCatalog catalog = loadFixture();
            assertNull(catalog.statusTitle("not_a_real_event"), "unknown event");
            assertNull(catalog.field("abstract_clash_st_ba", "not_a_real_field"), "unknown field on known event");
            assertEquals(0, catalog.fieldNames("not_a_real_event").size(), "unknown event field names");
        }));

        return out;
    }
}
