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
 * Regression tests for {@link ClanWarCatalog}, using a fixture built from real {@code clans.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 108, live-verified on device) --
 * covering the faction base-requirement crosswalk, the clan-level chrono-installation text, the
 * FFA/tournament/fight_site map-base rosters (including the real "Moria House" four-way naming),
 * and the open-world base-capture reward text.
 */
public final class ClanWarCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "clans.unit_creation.duty.creation_rules_test_fail.desc=Requires level 3 reputation at the Frontier Base",
            "clans.unit_creation.freedom.creation_rules_test_fail.desc=Requires level 3 reputation at the Rise Base",
            "clans.unit_creation.merc.creation_rules_test_fail.desc=Requires level 3 reputation at the Mercenary Base",
            "clans.unit_creation.covenant.creation_rules_test_fail.desc=Requires level 3 reputation at the Covenant Base",
            "clans.level.first_level_addition.desc=Special Deliveries Level 1",
            "clans.level.second_level_addition.desc=Special Deliveries Level 2\\nAvailable chrono installations: 5",
            "clans.level.third_level_addition.desc=Special Deliveries Level 3\\nAvailable chrono installations: 10",
            "clans.ffa_roza_vetrov.base_1.name=Red Moria House",
            "clans.ffa_roza_vetrov.base_2.name=White Moria House",
            "clans.ffa_roza_vetrov.base_3.name=Green Moria House",
            "clans.ffa_roza_vetrov.base_4.name=Blue Moria House",
            "clans.ffa_roza_vetrov.base_5.name=Collapse",
            "clans.ffa_nizina.base_4.name=Apple Tower",
            "clans.tournament.brawl.name=Brawl",
            "clans.tournament.1v1.name=Tournament",
            "clans.tournament.tournament_nizina.name=The Lowland",
            "clans.tournament.tournament_nizina2.name=The Lowland 2",
            "clans.tournament.base_capture_west.name=Bunker",
            "clans.tournament.base_capture_west.open_world_base.name=Bunker",
            "clans.tournament.base_capture_west.open_world_base.bonuses=- Fast travel to the base once every 10 minutes\\n- Storage size 8x8\\n- Consumables trader\\n- Resource buyer\\n- Mechanic for equipment repairs\\n- Generator for device charging",
            "clans.fight_site.nizina.base_4.name=Tower Apple"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("clanwar-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ClanWarCatalog loadFixture() throws IOException {
        return ClanWarCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("factionBaseRequirement resolves the real four-faction crosswalk", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Requires level 3 reputation at the Frontier Base", catalog.factionBaseRequirement("duty"), "duty");
            assertEquals("Requires level 3 reputation at the Rise Base", catalog.factionBaseRequirement("freedom"), "freedom");
            assertEquals("Requires level 3 reputation at the Mercenary Base", catalog.factionBaseRequirement("merc"), "merc");
            assertEquals("Requires level 3 reputation at the Covenant Base", catalog.factionBaseRequirement("covenant"), "covenant");
        }));

        out.add(test("levelAdditionDescription resolves the real chrono-installation progression text", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Special Deliveries Level 1", catalog.levelAdditionDescription("first_level_addition"), "level 1");
            assertTrue(catalog.levelAdditionDescription("second_level_addition").contains("chrono installations: 5"), "level 2");
            assertTrue(catalog.levelAdditionDescription("third_level_addition").contains("chrono installations: 10"), "level 3");
        }));

        out.add(test("ffaBaseName resolves the real four-way Moria House naming on roza_vetrov", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Red Moria House", catalog.ffaBaseName("ffa_roza_vetrov", "base_1"), "red");
            assertEquals("White Moria House", catalog.ffaBaseName("ffa_roza_vetrov", "base_2"), "white");
            assertEquals("Green Moria House", catalog.ffaBaseName("ffa_roza_vetrov", "base_3"), "green");
            assertEquals("Blue Moria House", catalog.ffaBaseName("ffa_roza_vetrov", "base_4"), "blue");
            assertEquals("Collapse", catalog.ffaBaseName("ffa_roza_vetrov", "base_5"), "collapse");
        }));

        out.add(test("tournamentName resolves real map display names distinct from internal ids", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Brawl", catalog.tournamentName("brawl"), "brawl");
            assertEquals("Tournament", catalog.tournamentName("1v1"), "1v1");
            assertEquals("The Lowland", catalog.tournamentName("tournament_nizina"), "nizina");
            assertEquals("The Lowland 2", catalog.tournamentName("tournament_nizina2"), "nizina2");
        }));

        out.add(test("openWorldBaseName/Bonuses resolve the real base-capture reward text", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Bunker", catalog.openWorldBaseName("base_capture_west"), "name");
            assertTrue(catalog.openWorldBaseBonuses("base_capture_west").contains("Fast travel to the base"), "bonus 1");
            assertTrue(catalog.openWorldBaseBonuses("base_capture_west").contains("Generator for device charging"), "bonus 6");
        }));

        out.add(test("fightSiteBaseName resolves the real word-order variant of the ffa base name", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertEquals("Apple Tower", catalog.ffaBaseName("ffa_nizina", "base_4"), "ffa word order");
            assertEquals("Tower Apple", catalog.fightSiteBaseName("nizina", "base_4"), "fight_site word order");
        }));

        out.add(test("ffaMapIds/tournamentIds/fightSiteMapIds are sorted and contain the real fixture ids", () -> {
            ClanWarCatalog catalog = loadFixture();
            List<String> ffaIds = catalog.ffaMapIds();
            assertTrue(ffaIds.contains("ffa_roza_vetrov"), "ffa contains roza_vetrov");
            assertTrue(ffaIds.contains("ffa_nizina"), "ffa contains nizina");
            for (int i = 1; i < ffaIds.size(); i++) {
                assertTrue(ffaIds.get(i - 1).compareTo(ffaIds.get(i)) < 0, "ffa sorted");
            }
            List<String> tournamentIds = catalog.tournamentIds();
            assertTrue(tournamentIds.contains("brawl"), "tournament contains brawl");
            List<String> fightSiteIds = catalog.fightSiteMapIds();
            assertEquals(1, fightSiteIds.size(), "fight_site map count");
            assertEquals("nizina", fightSiteIds.get(0), "fight_site contains nizina");
        }));

        out.add(test("unknown ids resolve to null across all lookups", () -> {
            ClanWarCatalog catalog = loadFixture();
            assertNull(catalog.factionBaseRequirement("not_a_real_faction"), "unknown faction");
            assertNull(catalog.ffaBaseName("ffa_roza_vetrov", "base_99"), "unknown ffa base");
            assertNull(catalog.tournamentName("not_a_real_tournament"), "unknown tournament");
            assertNull(catalog.openWorldBaseName("not_a_real_tournament"), "unknown open world base");
            assertNull(catalog.fightSiteBaseName("nizina", "base_99"), "unknown fight_site base");
        }));

        return out;
    }
}
