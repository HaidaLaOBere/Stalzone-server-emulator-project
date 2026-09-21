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
 * Regression tests for {@link DmzLangCatalog}, using a fixture built from real {@code dmz.*}/
 * {@code dmz2026.*} lines drawn directly from {@code stalker/lang/en.lang} (part 135,
 * live-verified on device) -- the flagged "Hook" ability compare (byte-identical soldier/2026
 * text, a near-identical zombie variant), a real league name, and a real 2026 game mode.
 */
public final class DmzLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "dmz.ability.active.soldier_hook.name=Hook",
            "dmz.ability.active.soldier_hook.description=You throw a grappling hook in the direction of view. When it latches onto a surface, the hook pulls you in.",
            "dmz.ability.active.zombie_hook.name=Hook",
            "dmz.ability.active.zombie_hook.description=You throw a grappling hook in the direction of your view. When it latches onto a surface, the hook pulls you in.",
            "dmz.leagues.1.name=Baron's Guard",
            "dmz2026.ability.active.hook.name=Hook",
            "dmz2026.ability.active.hook.description=You throw a grappling hook in the direction of view. When it latches onto a surface, the hook pulls you in.",
            "dmz2026.game_mode.solo.title=SCOUT"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("dmz-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static DmzLangCatalog loadFixture() throws IOException {
        return DmzLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real soldier_hook and dmz2026 hook descriptions are byte-identical", () -> {
            DmzLangCatalog catalog = loadFixture();
            String soldierDesc = catalog.dmzField("ability.active.soldier_hook", "description");
            String dmz2026Desc = catalog.dmz2026Field("ability.active.hook", "description");
            assertEquals(soldierDesc, dmz2026Desc, "soldier_hook vs dmz2026 hook description, byte-identical");
        }));

        out.add(test("the real zombie_hook description is a near-identical but NOT byte-identical variant", () -> {
            DmzLangCatalog catalog = loadFixture();
            String soldierDesc = catalog.dmzField("ability.active.soldier_hook", "description");
            String zombieDesc = catalog.dmzField("ability.active.zombie_hook", "description");
            assertTrue(!soldierDesc.equals(zombieDesc), "zombie_hook desc differs from soldier_hook desc");
            assertTrue(zombieDesc.contains("your view"), "zombie_hook desc has the real extra 'your'");
        }));

        out.add(test("a real league name and a real dmz2026 game mode resolve, and ids() are sorted with null-safe lookups", () -> {
            DmzLangCatalog catalog = loadFixture();
            assertEquals("Baron's Guard", catalog.dmzField("leagues.1", "name"), "leagues.1 name");
            assertEquals("SCOUT", catalog.dmz2026Field("game_mode.solo", "title"), "solo game mode title");
            List<String> dmzIds = catalog.dmzIds();
            assertEquals(3, dmzIds.size(), "3 real dmz ids in fixture");
            assertTrue(dmzIds.get(0).compareTo(dmzIds.get(dmzIds.size() - 1)) < 0, "dmzIds sorted ascending");
            List<String> dmz2026Ids = catalog.dmz2026Ids();
            assertEquals(2, dmz2026Ids.size(), "2 real dmz2026 ids in fixture");
            assertNull(catalog.dmzField("not_a_real_id", "name"), "unknown dmz id");
            assertNull(catalog.dmz2026Field("ability.active.hook", "not_a_real_field"), "unknown dmz2026 field");
        }));

        return out;
    }
}
