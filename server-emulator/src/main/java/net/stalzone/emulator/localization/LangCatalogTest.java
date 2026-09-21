package net.stalzone.emulator.localization;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link LangCatalog}, using a fixture file built from real {@code key=value}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 90, live-verified on device) --
 * a representative slice of the real {@code prestige.ability.*} namespace (see {@link
 * net.stalzone.emulator.progression.PrestigeAbility}) plus one real {@code battlefield.id.*} entry
 * and one blank line, matching the file's real shape.
 */
public final class LangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "prestige.ability.tank_barricade.name=Barricade",
            "prestige.ability.tank_barricade.description=Deploy a defensive barricade that provides cover. It can be destroyed by any player. You can have up to {max_objects number} barricades deployed at the same time.\\n\\nCharges: {max_charges number}. Recharge time: {recharge_time number} seconds.",
            "prestige.ability.scout_mine.name=Mine",
            "prestige.ability.scout_stun.name=Tranquilizer",
            "",
            "battlefield.id.reich.name=Assault on the Reichstag"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load parses every non-blank key=value line", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            assertEquals(5, catalog.size(), "key count (blank line skipped)");
        }));

        out.add(test("get returns the exact real value for a known key", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            assertEquals("Barricade", catalog.get("prestige.ability.tank_barricade.name"), "tank_barricade display name");
            assertEquals("Assault on the Reichstag", catalog.get("battlefield.id.reich.name"), "reich battlefield name");
        }));

        out.add(test("get returns null (not throw) for an unknown key", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            assertEquals(null, catalog.get("nonexistent.key"), "unknown key");
        }));

        out.add(test("values are stored raw -- a literal backslash-n escape is not unescaped", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            String description = catalog.get("prestige.ability.tank_barricade.description");
            assertTrue(description.contains("\\n\\n"), "literal backslash-n pair preserved: " + description);
            assertTrue(description.contains("{max_charges number}"), "template placeholder preserved verbatim: " + description);
        }));

        out.add(test("keysWithPrefix: finds exactly the 3 real prestige.ability.*.name entries", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            List<String> keys = catalog.keysWithPrefix("prestige.ability.");
            long nameKeys = keys.stream().filter(k -> k.endsWith(".name")).count();
            assertEquals(3, (int) nameKeys, "prestige.ability.*.name count");
        }));

        out.add(test("keysWithPrefix: returns empty (not null) for a prefix nothing matches", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            assertEquals(0, catalog.keysWithPrefix("nonexistent.prefix.").size(), "no-match count");
        }));

        out.add(test("keysContaining: battlefield.id.reich.name is found by substring 'reich' (8th Reichstag corroboration)", () -> {
            LangCatalog catalog = LangCatalog.load(writeFixture());
            assertTrue(catalog.keysContaining("reich").contains("battlefield.id.reich.name"), "reich key present");
        }));

        out.add(test("load rejects a line with no '=' at all", () -> {
            assertThrows(IOException.class, () -> {
                Path file = Files.createTempFile("lang-catalog-bad", ".lang");
                file.toFile().deleteOnExit();
                Files.writeString(file, "this line has no equals sign\n");
                LangCatalog.load(file);
            }, "malformed line");
        }));

        out.add(test("load rejects an empty file", () -> {
            assertThrows(IOException.class, () -> {
                Path file = Files.createTempFile("lang-catalog-empty", ".lang");
                file.toFile().deleteOnExit();
                Files.writeString(file, "");
                LangCatalog.load(file);
            }, "empty file");
        }));

        out.add(test("load tolerates an exact-duplicate key re-declaration", () -> {
            Path file = Files.createTempFile("lang-catalog-dup", ".lang");
            file.toFile().deleteOnExit();
            Files.writeString(file, "a.key=same value\na.key=same value\n");
            LangCatalog catalog = LangCatalog.load(file);
            assertEquals(1, catalog.size(), "duplicate collapses to one entry");
        }));

        out.add(test("load rejects a key re-declared with a DIFFERENT value", () -> {
            assertThrows(IOException.class, () -> {
                Path file = Files.createTempFile("lang-catalog-conflict", ".lang");
                file.toFile().deleteOnExit();
                Files.writeString(file, "a.key=value one\na.key=value two\n");
                LangCatalog.load(file);
            }, "conflicting re-declaration");
        }));

        return out;
    }
}
