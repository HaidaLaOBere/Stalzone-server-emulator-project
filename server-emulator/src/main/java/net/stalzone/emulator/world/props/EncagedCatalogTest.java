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
 * Regression tests for {@link EncagedCatalog}, using a fixture built from real {@code
 * encaged.*} lines drawn directly from {@code stalker/lang/en.lang} (part 146, live-verified on
 * device) -- the real 4-class roster, real hunter/survivor scoreboard action labels sharing one
 * id, and the real "iItem" client typo.
 */
public final class EncagedCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "encaged.class.medic.name=MEDIC",
            "encaged.class.scout.name=SCOUT",
            "encaged.score_action.hunter_encaged_survivor=Encaging a survivor",
            "encaged.score_action.survivor_encaged_help=Rescuing a teammate from a cage",
            "encaged.score_action.hunter_infect_folder_picking=Infected iItem picked up"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("encaged-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static EncagedCatalog loadFixture() throws IOException {
        return EncagedCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real 4-class roster resolves (fixture holds 2 of the 4)", () -> {
            EncagedCatalog catalog = loadFixture();
            assertEquals("MEDIC", catalog.field("class.medic", "name"), "class.medic name");
            assertEquals("SCOUT", catalog.field("class.scout", "name"), "class.scout name");
        }));

        out.add(test("real hunter and survivor scoreboard action labels resolve under one shared id", () -> {
            EncagedCatalog catalog = loadFixture();
            assertEquals("Encaging a survivor", catalog.field("score_action", "hunter_encaged_survivor"), "hunter_encaged_survivor label");
            assertEquals("Rescuing a teammate from a cage", catalog.field("score_action", "survivor_encaged_help"), "survivor_encaged_help label");
        }));

        out.add(test("the real 'iItem' client typo is preserved verbatim, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            EncagedCatalog catalog = loadFixture();
            assertEquals("Infected iItem picked up", catalog.field("score_action", "hunter_infect_folder_picking"), "hunter_infect_folder_picking label, real typo preserved");
            List<String> ids = catalog.ids();
            assertEquals(3, ids.size(), "3 real ids in fixture (class.medic, class.scout, score_action)");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
            assertNull(catalog.field("score_action", "not_a_real_action"), "unknown field on a real id");
        }));

        return out;
    }
}
