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
 * Regression tests for {@link PrestigeLangCatalog}, using a fixture built from real {@code
 * prestige.*} lines drawn directly from {@code stalker/lang/en.lang} (part 134, live-verified on
 * device) -- real Coliseum-mode ability/class/howtoplay/rating content, including the one real
 * ability id missing its own `.name` field.
 */
public final class PrestigeLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "prestige.ability.throw_respawn_marker.name=Loot Bag",
            "prestige.ability.throw_respawn_marker.description=Technical Ability: Throw a Respawn Marker",
            "prestige.ability.tank_high_gravity_aura.description=Create a high-gravity aura with a radius around yourself.",
            "prestige.classes.combat.name=Assault",
            "prestige.classes.combat.desc=The Assault class specializes in provoking enemies and controlling zones.",
            "prestige.howtoplay.tip.1.title=Activate rifts",
            "prestige.rating.no_access.message=Level 2 Drunken Geiger Bar reputation",
            "prestige.usual.no_access.message=Level 3 Bar reputation"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("prestige-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PrestigeLangCatalog loadFixture() throws IOException {
        return PrestigeLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real ability's name/description resolve together, and the real name-less ability resolves its description only", () -> {
            PrestigeLangCatalog catalog = loadFixture();
            assertEquals("Loot Bag", catalog.field("ability.throw_respawn_marker", "name"), "throw_respawn_marker name");
            assertEquals("Technical Ability: Throw a Respawn Marker", catalog.field("ability.throw_respawn_marker", "description"), "throw_respawn_marker description");
            assertNull(catalog.field("ability.tank_high_gravity_aura", "name"), "tank_high_gravity_aura has no real name");
            assertEquals("Create a high-gravity aura with a radius around yourself.", catalog.field("ability.tank_high_gravity_aura", "description"), "tank_high_gravity_aura description");
        }));

        out.add(test("real class, howtoplay, and reputation-gate groups resolve independently", () -> {
            PrestigeLangCatalog catalog = loadFixture();
            assertEquals("Assault", catalog.field("classes.combat", "name"), "combat class name");
            assertEquals("Activate rifts", catalog.field("howtoplay.tip.1", "title"), "howtoplay tip 1 title");
            assertEquals("Level 2 Drunken Geiger Bar reputation", catalog.field("rating.no_access", "message"), "rating no_access message");
            assertEquals("Level 3 Bar reputation", catalog.field("usual.no_access", "message"), "usual no_access message");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            PrestigeLangCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(6, ids.size(), "6 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("classes.not_a_real_class", "name"), "unknown class id");
        }));

        return out;
    }
}
