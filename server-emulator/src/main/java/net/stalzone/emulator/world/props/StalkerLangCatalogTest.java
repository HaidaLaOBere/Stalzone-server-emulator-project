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
 * Regression tests for {@link StalkerLangCatalog}, using a fixture built from real {@code
 * stalker.*} lines drawn directly from {@code stalker/lang/en.lang} (part 119, live-verified on
 * device) -- covering each of the namespace's 14 real groups.
 */
public final class StalkerLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "stalker.quickcommands.approv/mark/position=Roger",
            "stalker.quickcommands.approv/mark/item=I'll take it",
            "stalker.quickcommands.happynewyear=Happy New Year!",
            "stalker.quickcommands.stalkers.approv.0=Great!",
            "stalker.quickcommands.stalkers.approv.1=Attaboy, stalker!",
            "stalker.quickcommands.hunter.approv.0=You almost lived up to my expectations.",
            "stalker.navigation.transition.agroprom-dungeon_mozglyak.name=Weakling's Den",
            "stalker.ui_guide.text.click_lmb_to_continue=Press [key:LMB] to continue.",
            "stalker.ui_guide.infection_output.1.header=Quick Access",
            "stalker.hud.narrative_tooltip.poster_visdom=Sounds everywhere\\nThe voice of wise books",
            "stalker.hud.breakthrough.team.attackers=RED ARMY",
            "stalker.hud.breakthrough.team.defenders=WEHRMACHT",
            "stalker.guide.default.tutorial_heal.1.header=Interaction",
            "stalker.guide.default.tutorial_heal.1.text=Press [#key.interact] to open the crate",
            "stalker.guide.conditional.some_topic.1.header=Some Header",
            "stalker.gui.death_screen.damage_type.freeze.label=Frost Damage",
            "stalker.clans.tournament.brawl.name=Brawl",
            "stalker.clans.mail.tournament.brawl.fight_win=Reward for the {stage} phase.",
            "stalker.alliance.gui.description.short.stalkers=Stalkers are adventurers who are eager to find the hidden treasures of the Zone.",
            "stalker.alliance.gui.description.full.duty=\"There is nothing valuable or human in the Zone.\" (Davydov, Frontier Commander)",
            "stalker.alliance.quest_join=Join",
            "stalker.alliance.duty.quest_join=Join Frontier",
            "stalker.alliance.duty.quest_leave=Leave Frontier",
            "stalker.protected_areas.alert.overlay.0=You should leave the confines of the enemy base.",
            "stalker.protected_areas.alert.notification.0=Oi, scofflaw! Watch where you're going!",
            "stalker.protected_areas.alert.notification.1=I'm warning you, scofflaw: you can't go any further.",
            "stalker.protected_areas.bar_stalker.owner=Stalkers",
            "stalker.battlefield.team_name.redarmy=Red Army",
            "stalker.battlefield.team_name.vermacht=Wehrmacht",
            "stalker.dmz2026.stat.scenario_completion_factor.name=Main task completion",
            "stalker.conditional_guide.bleeding.1.header=Bleeding",
            "stalker.conditional_guide.bleeding.1.text=Use bandages to stop bleeding.",
            "stalker.artefact_properties.factor.bleeding_protection=Bleeding protection",
            "stalker.artefact_properties.factor.bleeding_protection.desc=Affects the strength and duration of incoming bleeding effects.",
            "stalker.player_params.effect.category.blessed_power=Power of the Blessed"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("stalker-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static StalkerLangCatalog loadFixture() throws IOException {
        return StalkerLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("quickCommandLine resolves ordered voice-pack phrase variants", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Great!", catalog.quickCommandLine("stalkers", "approv", 0), "stalkers approv 0");
            assertEquals("Attaboy, stalker!", catalog.quickCommandLine("stalkers", "approv", 1), "stalkers approv 1");
            assertEquals(2, catalog.quickCommandLineCount("stalkers", "approv"), "stalkers approv count");
            assertEquals("You almost lived up to my expectations.",
                    catalog.quickCommandLine("hunter", "approv", 0), "hunter approv 0");
        }));

        out.add(test("quickCommandFlat resolves the real flat exceptions with embedded slashes", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Roger", catalog.quickCommandFlat("approv/mark/position"), "mark position");
            assertEquals("I'll take it", catalog.quickCommandFlat("approv/mark/item"), "mark item");
            assertEquals("Happy New Year!", catalog.quickCommandFlat("happynewyear"), "happynewyear");
        }));

        out.add(test("transitionName resolves a real navigation connector", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Weakling's Den", catalog.transitionName("agroprom", "dungeon_mozglyak"), "transition");
        }));

        out.add(test("generic (id, field) tables resolve for ui_guide/guide/gui/clans", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Press [key:LMB] to continue.", catalog.uiGuideField("text", "click_lmb_to_continue"), "ui_guide flat");
            assertEquals("Quick Access", catalog.uiGuideField("infection_output.1", "header"), "ui_guide nested");
            assertEquals("Interaction", catalog.guideField("default.tutorial_heal.1", "header"), "guide default");
            assertEquals("Some Header", catalog.guideField("conditional.some_topic.1", "header"), "guide conditional distinct");
            assertEquals("Frost Damage", catalog.guiField("death_screen.damage_type.freeze", "label"), "gui nested");
            assertEquals("Brawl", catalog.clansField("tournament.brawl", "name"), "clans tournament name");
            assertEquals("Reward for the {stage} phase.", catalog.clansField("mail.tournament.brawl", "fight_win"), "clans mail");
        }));

        out.add(test("hud narrative tooltip and breakthrough team names resolve", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Sounds everywhere\\nThe voice of wise books", catalog.narrativeTooltipText("poster_visdom"), "poster");
            assertEquals("RED ARMY", catalog.breakthroughTeamName("attackers"), "breakthrough attackers");
            assertEquals("WEHRMACHT", catalog.breakthroughTeamName("defenders"), "breakthrough defenders");
        }));

        out.add(test("alliance description/quest-label accessors resolve, including the flat generic label", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertTrue(catalog.allianceDescription("stalkers", "short").startsWith("Stalkers are adventurers"), "short desc");
            assertTrue(catalog.allianceDescription("duty", "full").contains("Davydov"), "full desc quote");
            assertEquals("Join", catalog.allianceGenericQuestJoinLabel(), "generic quest_join");
            assertEquals("Join Frontier", catalog.allianceQuestLabel("duty", "quest_join"), "duty quest_join");
            assertEquals("Leave Frontier", catalog.allianceQuestLabel("duty", "quest_leave"), "duty quest_leave");
        }));

        out.add(test("protected_areas alert lists and owner map resolve", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals(1, catalog.protectedAreaAlertOverlay().size(), "overlay size");
            assertEquals(2, catalog.protectedAreaAlertNotification().size(), "notification size");
            assertEquals("Stalkers", catalog.protectedAreaOwner("bar_stalker"), "bar_stalker owner");
        }));

        out.add(test("battlefield/dmz2026/conditional_guide/artefact_properties/player_params resolve", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertEquals("Red Army", catalog.battlefieldTeamName("redarmy"), "battlefield redarmy");
            assertEquals("Wehrmacht", catalog.battlefieldTeamName("vermacht"), "battlefield vermacht");
            assertEquals("Main task completion", catalog.dmz2026StatName("scenario_completion_factor"), "dmz2026 stat");
            assertEquals("Bleeding", catalog.conditionalGuideField("bleeding.1", "header"), "conditional_guide header");
            assertEquals("Bleeding protection", catalog.artefactPropertyLabel("bleeding_protection"), "artefact label");
            assertEquals("Affects the strength and duration of incoming bleeding effects.",
                    catalog.artefactPropertyDesc("bleeding_protection"), "artefact desc");
            assertEquals("Power of the Blessed",
                    catalog.playerParamsEffectCategoryLabel("blessed_power"), "player_params label");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            StalkerLangCatalog catalog = loadFixture();
            assertNull(catalog.quickCommandLine("not_real", "approv", 0), "unknown voice pack");
            assertNull(catalog.transitionName("not", "real"), "unknown transition");
            assertNull(catalog.uiGuideField("not_real", "field"), "unknown ui_guide id");
            assertNull(catalog.allianceDescription("not_real", "short"), "unknown alliance faction");
            assertNull(catalog.protectedAreaOwner("not_real"), "unknown protected area");
            assertEquals(0, catalog.quickCommandCategories("not_real").size(), "unknown voice pack categories");
        }));

        return out;
    }
}
