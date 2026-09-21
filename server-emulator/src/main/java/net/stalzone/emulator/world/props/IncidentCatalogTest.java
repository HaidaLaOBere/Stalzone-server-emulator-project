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
 * Regression tests for {@link IncidentCatalog}, using a fixture built from real {@code
 * incident.*} lines drawn directly from {@code stalker/lang/en.lang} (part 149, live-verified on
 * device) -- the real multi-line care-package loot list, and the real "stormtrooper"/"Assault
 * Trooper" name overlap with {@link OperationsCatalog}.
 */
public final class IncidentCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "incident.weekly_drop.description=— Advanced Spare Parts, 6 units\\n— Advanced Tools, 15 units",
            "incident.game_class.sample.stormtrooper.name=Assault Trooper",
            "incident.game_class.sample.stormtrooper.desc=An all-round soldier who may not have any specific specialization, but is always a great support both in shootouts and in battles with mutants.",
            "incident.game_class.sample.scout.name=Scout",
            "incident.game_class.sample.scout.desc=The scout's high mobility means they can attack the enemy from unexpected sides and use mines to slow down enemy movement or cover withdrawal routes."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("incident-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static IncidentCatalog loadFixture() throws IOException {
        return IncidentCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real multi-line care-package loot-list description resolves with its literal newline escapes intact", () -> {
            IncidentCatalog catalog = loadFixture();
            String desc = catalog.field("weekly_drop", "description");
            assertTrue(desc.contains("Advanced Spare Parts, 6 units"), "weekly_drop description contains real loot line");
            assertTrue(desc.contains("\\n"), "weekly_drop description preserves real literal newline escapes");
        }));

        out.add(test("the real 'stormtrooper'/'Assault Trooper' entry textually matches OperationsCatalog's mode_classes name", () -> {
            IncidentCatalog catalog = loadFixture();
            assertEquals("Assault Trooper", catalog.field("game_class.sample.stormtrooper", "name"), "stormtrooper name matches real Operations-mode class text");
        }));

        out.add(test("the real scout sample entry resolves, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            IncidentCatalog catalog = loadFixture();
            assertEquals("Scout", catalog.field("game_class.sample.scout", "name"), "scout name");
            assertTrue(catalog.field("game_class.sample.scout", "desc").contains("mobility"), "scout desc contains real phrase");
            List<String> ids = catalog.ids();
            assertEquals(3, ids.size(), "3 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
            assertNull(catalog.field("weekly_drop", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
