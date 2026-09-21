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
 * Regression tests for {@link GoLangCatalog}, using a fixture built from real {@code go.*} lines
 * drawn directly from {@code stalker/lang/en.lang} (part 120, live-verified on device) --
 * covering ordinary two-field objects, a lore-bearing installation, the real Reichstag WWII prop
 * tail, and a tutorial-hint object.
 */
public final class GoLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "go.tutor_hint_0.hint=Press [#key.jump] to jump.",
            "go.tutor_hint_0.name=Hint (Jumping)",
            "go.misc.extractor.lore=An installation for harvesting and converting anomalous energy.",
            "go.misc.extractor.name=Anomaly Research Installation",
            "go.misc.reichstag_t34.name=Т-34-85",
            "go.misc.reichstag_rkka_sniper.name=Red Army Sniper. Winter 1941",
            "go.exp.claymore.name=M18 Claymore",
            "go.exp.claymore_reich.name=Trip Wire Mine",
            "go.exp.claymore_reich.desc=A concealed anti-personnel trap triggered by tension on a wire.",
            "go.ops.phase_battery_red.name=Red Phase Battery",
            "go.clan.extractor.name=Chronodevice"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("go-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static GoLangCatalog loadFixture() throws IOException {
        return GoLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("hint/name resolve together for a real tutorial-trigger object", () -> {
            GoLangCatalog catalog = loadFixture();
            assertEquals("Press [#key.jump] to jump.", catalog.hint("tutor_hint_0"), "tutor_hint_0 hint");
            assertEquals("Hint (Jumping)", catalog.name("tutor_hint_0"), "tutor_hint_0 name");
        }));

        out.add(test("lore/name resolve together for a real compound-id installation", () -> {
            GoLangCatalog catalog = loadFixture();
            assertEquals("Anomaly Research Installation", catalog.name("misc.extractor"), "misc.extractor name");
            assertTrue(catalog.lore("misc.extractor").startsWith("An installation"), "misc.extractor lore");
        }));

        out.add(test("Reichstag WWII prop names resolve without asserting a corroboration ordinal", () -> {
            GoLangCatalog catalog = loadFixture();
            assertEquals("Т-34-85", catalog.name("misc.reichstag_t34"), "reichstag t34");
            assertEquals("Red Army Sniper. Winter 1941", catalog.name("misc.reichstag_rkka_sniper"), "reichstag sniper");
        }));

        out.add(test("field resolves generically across different top-level id groups", () -> {
            GoLangCatalog catalog = loadFixture();
            assertEquals("M18 Claymore", catalog.name("exp.claymore"), "exp claymore name");
            assertEquals("Trip Wire Mine", catalog.name("exp.claymore_reich"), "exp claymore_reich name");
            assertEquals("A concealed anti-personnel trap triggered by tension on a wire.",
                    catalog.field("exp.claymore_reich", "desc"), "exp claymore_reich desc");
            assertEquals("Red Phase Battery", catalog.name("ops.phase_battery_red"), "ops phase battery");
            assertEquals("Chronodevice", catalog.name("clan.extractor"), "clan extractor, distinct from misc.extractor");
        }));

        out.add(test("ids/fieldNames are sorted and reflect the real fixture data", () -> {
            GoLangCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(8, ids.size(), "8 real object ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertEquals(2, catalog.fieldNames("tutor_hint_0").size(), "tutor_hint_0 has hint+name");
            assertEquals(1, catalog.fieldNames("ops.phase_battery_red").size(), "phase battery has only name");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            GoLangCatalog catalog = loadFixture();
            assertNull(catalog.name("not_a_real_object"), "unknown object name");
            assertNull(catalog.field("misc.extractor", "not_a_real_field"), "unknown field");
            assertEquals(0, catalog.fieldNames("not_a_real_object").size(), "unknown object field names");
        }));

        return out;
    }
}
