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
 * Regression tests for {@link ArcadeCatalog}, using a fixture built from real {@code arcade.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 144, live-verified on device) --
 * real match-modifier badge text and the real RKKA/Wehrmacht Reichstag class-name pair.
 */
public final class ArcadeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "arcade.customTags.deathmatch.text=DEATHMATCH",
            "arcade.customTags.deathmatch.desc=Intense battle mode, where everyone fights for themselves; the one who makes the most kills in the allotted time wins",
            "arcade.customTags.rank_change.text={time} MIN",
            "arcade.classes.reichstag_rkka_assault.name=Red Army Assaulter",
            "arcade.classes.reichstag_wehrmacht_assault.name=Wehrmacht Assaulter",
            "arcade.classes.reichstag_assault.description=",
            "arcade.classes.reichstag_assault.progress=Requires Assaulter—Level {targetLevel}"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("arcade-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ArcadeCatalog loadFixture() throws IOException {
        return ArcadeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real named game mode badge and a real timer-template badge resolve", () -> {
            ArcadeCatalog catalog = loadFixture();
            assertEquals("DEATHMATCH", catalog.field("customTags.deathmatch", "text"), "deathmatch text");
            assertTrue(catalog.field("customTags.deathmatch", "desc").contains("most kills"), "deathmatch desc contains real phrase");
            assertEquals("{time} MIN", catalog.field("customTags.rank_change", "text"), "rank_change text template");
        }));

        out.add(test("the real RKKA and Wehrmacht Reichstag class names resolve as genuinely different real faction texts", () -> {
            ArcadeCatalog catalog = loadFixture();
            String rkka = catalog.field("classes.reichstag_rkka_assault", "name");
            String wehrmacht = catalog.field("classes.reichstag_wehrmacht_assault", "name");
            assertEquals("Red Army Assaulter", rkka, "rkka assault name");
            assertEquals("Wehrmacht Assaulter", wehrmacht, "wehrmacht assault name");
            assertTrue(!rkka.equals(wehrmacht), "the two faction names are genuinely different");
        }));

        out.add(test("a real empty description placeholder and a real progress unlock text resolve, with sorted/null-safe lookups", () -> {
            ArcadeCatalog catalog = loadFixture();
            assertEquals("", catalog.field("classes.reichstag_assault", "description"), "reichstag_assault description is a real empty placeholder");
            assertTrue(catalog.field("classes.reichstag_assault", "progress").contains("targetLevel"), "reichstag_assault progress contains real template token");
            List<String> ids = catalog.ids();
            assertTrue(ids.size() > 0, "at least one real id in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
        }));

        return out;
    }
}
