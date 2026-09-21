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
 * Regression tests for {@link PartyMissionsCatalog}, using a fixture built from real {@code
 * party_missions.*} lines drawn directly from {@code stalker/lang/en.lang} (part 133,
 * live-verified on device) -- real mission name/status-text pairs, the one real flat
 * `name_format` exception, and a real id embedding a literal `/`.
 */
public final class PartyMissionsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "party_missions.name_format={name} {status}",
            "party_missions.soldier_scenario_1.name=First Squad",
            "party_missions.kill_any_turned_player.status_text={marker} Kill people {completed}/{required}",
            "party_missions.complete_world_event.dmz_trading_terminal.status_text={marker} Destroy the vending machine: {completed}/{required}",
            "party_missions.bolota/frontline_camp_third.name=Frontline Camp"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("party-missions-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PartyMissionsCatalog loadFixture() throws IOException {
        return PartyMissionsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real flat name_format exception resolves distinct from the generic table", () -> {
            PartyMissionsCatalog catalog = loadFixture();
            assertEquals("{name} {status}", catalog.nameFormat(), "name_format literal template");
            assertEquals("First Squad", catalog.name("soldier_scenario_1"), "soldier_scenario_1 name");
        }));

        out.add(test("real status_text templates and an id embedding a literal '/' resolve", () -> {
            PartyMissionsCatalog catalog = loadFixture();
            assertEquals("{marker} Kill people {completed}/{required}", catalog.statusText("kill_any_turned_player"), "kill_any_turned_player status_text");
            assertEquals("{marker} Destroy the vending machine: {completed}/{required}", catalog.statusText("complete_world_event.dmz_trading_terminal"), "dmz_trading_terminal status_text");
            assertEquals("Frontline Camp", catalog.name("bolota/frontline_camp_third"), "bolota/frontline_camp_third name");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            PartyMissionsCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real generic-table ids in fixture (name_format excluded)");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.name("not_a_real_mission"), "unknown mission id");
            assertNull(catalog.statusText("soldier_scenario_1"), "soldier_scenario_1 has no real status_text in fixture");
        }));

        return out;
    }
}
