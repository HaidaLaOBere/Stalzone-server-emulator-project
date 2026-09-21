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
 * Regression tests for {@link WorldModifierCatalog}, using a fixture built from real {@code
 * world.modifier.*} lines drawn directly from {@code stalker/lang/en.lang} (part 130,
 * live-verified on device) -- real Arena-mode modifiers tying to the still-uncovered {@code
 * ravent.*} namespace.
 */
public final class WorldModifierCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "world.modifier.ravent_level_electra_activity.name=Proto-Discharge Activity",
            "world.modifier.ravent_level_electra_activity.description=Proto-Discharges appear in the Arena",
            "world.modifier.ravent_level_funnel_activity.name=Proto-Whirlpool Activity",
            "world.modifier.ravent_level_funnel_activity.description=Proto-Whirlpools appear in the Arena"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("world-modifier-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static WorldModifierCatalog loadFixture() throws IOException {
        return WorldModifierCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real modifier's name/description resolve together", () -> {
            WorldModifierCatalog catalog = loadFixture();
            assertEquals("Proto-Discharge Activity", catalog.name("ravent_level_electra_activity"), "electra name");
            assertEquals("Proto-Discharges appear in the Arena", catalog.description("ravent_level_electra_activity"), "electra desc");
        }));

        out.add(test("a second real modifier resolves independently", () -> {
            WorldModifierCatalog catalog = loadFixture();
            assertEquals("Proto-Whirlpool Activity", catalog.name("ravent_level_funnel_activity"), "funnel name");
        }));

        out.add(test("ids() is sorted and unknown ids resolve to null rather than throwing", () -> {
            WorldModifierCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(2, ids.size(), "2 real modifier ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(1)) < 0, "sorted ascending");
            assertNull(catalog.name("not_a_real_modifier"), "unknown modifier id");
        }));

        return out;
    }
}
