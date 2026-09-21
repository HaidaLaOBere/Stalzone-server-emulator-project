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
 * Regression tests for {@link FrontlineCatalog}, using a fixture built from real {@code
 * frontline.*} lines drawn directly from {@code stalker/lang/en.lang} (part 129, live-verified on
 * device, briefing paragraphs truncated to their real opening sentence for fixture brevity) --
 * covering two briefing series in order, a real calldown item's full field set, the item missing
 * a {@code name}, the DMZ26 tie-in quest, and the bonus-event completion template.
 */
public final class FrontlineCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "frontline.briefing_s_0.text=Murmur swept through the South of the Zone like a true \"Blitzkrieg,\" setting up its psi-generators.",
            "frontline.briefing_s_1.text=Reporting. We have reliable data that Murmur is using psi-active devices to influence our soldiers.",
            "frontline.briefing_n2_0.text=We control the Swamps. The SSPP Subdivisions have taken strategically important positions.",
            "frontline.calldown.rbk250.name=RBK-250",
            "frontline.calldown.rbk250.desc=A cluster munition RBK-250 is launched at the call point.",
            "frontline.calldown.rbk250.prop1=LOW PROJECTILE DAMAGE",
            "frontline.calldown.rbk250.prop2=LARGE NUMBER OF PROJECTILES",
            "frontline.calldown.rbk250.prop3=LARGE COVERAGE AREA",
            "frontline.calldown.fab100.desc=A FAB-100 is sent to the coordinates, dealing high damage over an area.",
            "frontline.calldown.fab100.prop1=HIGH DAMAGE",
            "frontline.calldown_quest_dmz26.name=Thousand Leagues Cargo 200",
            "frontline.calldown_quest_dmz26.desc=Thousand Leagues Cargo 200",
            "frontline.bonus_event.completion=The \"{squad}\" squad has destroyed Murmur Radio suppressors - the command is increasing the mission's overall payout"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("frontline-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static FrontlineCatalog loadFixture() throws IOException {
        return FrontlineCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real briefing series' paragraphs resolve in index order", () -> {
            FrontlineCatalog catalog = loadFixture();
            List<String> s = catalog.briefingText("s");
            assertEquals(2, s.size(), "2 real 's' series briefing paragraphs in fixture");
            assertTrue(s.get(0).startsWith("Murmur swept through the South"), "s briefing index 0");
            assertTrue(s.get(1).startsWith("Reporting. We have reliable data"), "s briefing index 1");
        }));

        out.add(test("the 's' and 'n2' series are genuinely separate, not merged", () -> {
            FrontlineCatalog catalog = loadFixture();
            assertEquals(1, catalog.briefingText("n2").size(), "1 real 'n2' series paragraph in fixture");
            assertTrue(catalog.briefingText("n2").get(0).startsWith("We control the Swamps"), "n2 briefing index 0");
            assertEquals(0, catalog.briefingText("s2").size(), "no 's2' data in this fixture");
        }));

        out.add(test("a real calldown item's full field set (name/desc/prop1-3) resolves", () -> {
            FrontlineCatalog catalog = loadFixture();
            assertEquals("RBK-250", catalog.calldownName("rbk250"), "rbk250 name");
            assertTrue(catalog.calldownDesc("rbk250").startsWith("A cluster munition RBK-250"), "rbk250 desc");
            assertEquals("LOW PROJECTILE DAMAGE", catalog.calldownField("rbk250", "prop1"), "rbk250 prop1");
            assertEquals("LARGE COVERAGE AREA", catalog.calldownField("rbk250", "prop3"), "rbk250 prop3");
        }));

        out.add(test("a real calldown item genuinely missing its own name field resolves null, not a fallback", () -> {
            FrontlineCatalog catalog = loadFixture();
            assertNull(catalog.calldownName("fab100"), "fab100 has no real .name line in this client build");
            assertTrue(catalog.calldownDesc("fab100").startsWith("A FAB-100 is sent"), "fab100 desc still resolves");
        }));

        out.add(test("the DMZ26 tie-in quest and bonus-event completion template resolve, distinct from the calldown roster", () -> {
            FrontlineCatalog catalog = loadFixture();
            assertEquals("Thousand Leagues Cargo 200", catalog.calldownQuestDmz26Field("name"), "dmz26 quest name");
            assertEquals("Thousand Leagues Cargo 200", catalog.calldownQuestDmz26Field("desc"), "dmz26 quest desc");
            assertTrue(catalog.bonusEventField("completion").contains("Murmur Radio suppressors"), "bonus event completion");
        }));

        out.add(test("calldownIds()/briefingSeriesIds() are sorted and unknown ids resolve to null/empty", () -> {
            FrontlineCatalog catalog = loadFixture();
            List<String> ids = catalog.calldownIds();
            assertEquals(2, ids.size(), "2 real calldown ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "calldownIds sorted ascending");
            }
            assertNull(catalog.calldownName("not_a_real_calldown"), "unknown calldown id");
            assertEquals(0, catalog.briefingText("not_a_real_series").size(), "unknown briefing series");
        }));

        return out;
    }
}
