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
 * Regression tests for {@link BlizzardCatalog}, using a fixture built from real {@code
 * blizzard.*} lines drawn directly from {@code stalker/lang/en.lang} (part 155, live-verified on
 * device) -- the real base-defense event notification text, class names, and base names.
 */
public final class BlizzardCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "blizzard.event.preparation_notification=The Blizzard is approaching. It's time to take up positions near the heating installations!",
            "blizzard.event.crystal_destroyed_notification=Frost Fighters have destroyed a Crystal.",
            "blizzard.event.end_human_defeat_notification=The adepts got the upper hand. Now the Sovkhoz is swarming with their spawn. The truce ends in 5 minutes.",
            "blizzard.event.props.class.hunter.name=Hunter",
            "blizzard.event.props.class.shaman.name=Shaman",
            "blizzard.event.props.class.tank.name=Thug",
            "blizzard.base.stalker=Stalker Base",
            "blizzard.base.bandit=Bandit Base"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("blizzard-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static BlizzardCatalog loadFixture() throws IOException {
        return BlizzardCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real event notification text resolves, including the real 'Frost Fighters' faction reference corroborating part 111's WorldEventCatalog we.ny25", () -> {
            BlizzardCatalog catalog = loadFixture();
            assertTrue(catalog.field("event", "preparation_notification").contains("heating installations"), "preparation notification");
            assertTrue(catalog.field("event", "crystal_destroyed_notification").contains("Frost Fighters"), "crystal_destroyed references real Frost Fighters faction");
            assertTrue(catalog.field("event", "end_human_defeat_notification").contains("Sovkhoz"), "end_human_defeat references real Sovkhoz location");
        }));

        out.add(test("real hunter-side class names resolve under their own id space", () -> {
            BlizzardCatalog catalog = loadFixture();
            assertEquals("Hunter", catalog.field("event.props.class.hunter", "name"), "hunter class name");
            assertEquals("Shaman", catalog.field("event.props.class.shaman", "name"), "shaman class name");
            assertEquals("Thug", catalog.field("event.props.class.tank", "name"), "tank class real display name is 'Thug'");
        }));

        out.add(test("real base names resolve, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            BlizzardCatalog catalog = loadFixture();
            assertEquals("Stalker Base", catalog.field("base", "stalker"), "stalker base name");
            assertEquals("Bandit Base", catalog.field("base", "bandit"), "bandit base name");
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "5 real ids in fixture (event, event.props.class.hunter/shaman/tank, base)");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
        }));

        return out;
    }
}
