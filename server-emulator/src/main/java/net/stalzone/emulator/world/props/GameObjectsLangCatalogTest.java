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
 * Regression tests for {@link GameObjectsLangCatalog}, using a fixture built from real {@code
 * gameobjects.*} lines drawn directly from {@code stalker/lang/en.lang} (part 148, live-verified
 * on device) -- real quest-object interaction prompts sharing one id, the real duplicate "Place
 * bait" DMZ group, and the real literal-digit id.
 */
public final class GameObjectsLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "gameobjects.action_tip.go_quest.ex_chrism=Hang the chrism",
            "gameobjects.action_tip.go_quest.dig_hole=Dig a hole",
            "gameobjects.action_tip.go_quest.dmz_primanka1=Place bait",
            "gameobjects.action_tip.go_quest.dmz_primanka2=Place bait",
            "gameobjects.action_tip.go_quest.33627=Restart the scanner",
            "gameobjects.action_tip.safe.requires_key=Requires"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("gameobjects-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static GameObjectsLangCatalog loadFixture() throws IOException {
        return GameObjectsLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real quest-object interaction prompts resolve under one shared id", () -> {
            GameObjectsLangCatalog catalog = loadFixture();
            assertEquals("Hang the chrism", catalog.field("action_tip.go_quest", "ex_chrism"), "ex_chrism prompt");
            assertEquals("Dig a hole", catalog.field("action_tip.go_quest", "dig_hole"), "dig_hole prompt");
        }));

        out.add(test("the real duplicate 'Place bait' DMZ group and the real literal-digit id resolve", () -> {
            GameObjectsLangCatalog catalog = loadFixture();
            assertEquals("Place bait", catalog.field("action_tip.go_quest", "dmz_primanka1"), "dmz_primanka1 prompt");
            assertEquals("Place bait", catalog.field("action_tip.go_quest", "dmz_primanka2"), "dmz_primanka2 prompt");
            assertEquals("Restart the scanner", catalog.field("action_tip.go_quest", "33627"), "literal-digit id 33627 prompt");
        }));

        out.add(test("the real safe key-requirement prompt resolves as a separate id, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            GameObjectsLangCatalog catalog = loadFixture();
            assertEquals("Requires", catalog.field("action_tip.safe", "requires_key"), "action_tip.safe requires_key");
            List<String> ids = catalog.ids();
            assertEquals(2, ids.size(), "2 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "x"), "unknown id");
            assertNull(catalog.field("action_tip.go_quest", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
