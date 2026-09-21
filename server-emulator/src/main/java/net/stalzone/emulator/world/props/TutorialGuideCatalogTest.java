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
 * Regression tests for {@link TutorialGuideCatalog}, using a fixture built from real key=value
 * lines drawn directly from {@code stalker/lang/en.lang} (part 168, live-verified on device) --
 * a representative slice of both the current {@code stalkerguide.*} and legacy {@code
 * stalkerguide2019.*} namespaces, covering the real overlap and the real disclosed asymmetry.
 */
public final class TutorialGuideCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "stalkerguide.quest.walkaround.header=Moving",
            "stalkerguide.quest.getup.header=Ascent",
            "stalkerguide.quest.getup.text=Press [key:{keyName}] to stand up.",
            "stalkerguide.quest.weaponuse.3.desc.killed=Destroy {total number} enemies",
            "stalkerguide.controlpoints.control_point.ammo_crate=Hover over the ammo crate and hold [#key.interact] to replenish your ammo",
            "stalkerguide2019.quest.getup.header=Stand Up",
            "stalkerguide2019.quest.getup.text=Press [key:{keyName}] to stand up.",
            "stalkerguide2019.quest.weaponuse.3.desc.killed=Targets killed ({killed number}/{total number})",
            "stalkerguide2019.quest.medkit_pre.text=Hover over an item and press [key:{keyName}] to pick it up.",
            "stalkerguide2019.quest.medkit.desc.use=Use a first-aid kit on yourself"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("tutorial-guide-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static TutorialGuideCatalog loadFixture() throws IOException {
        return TutorialGuideCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real current and stalkerguide2019 text resolve independently by exact key suffix, including a real byte-identical shared line", () -> {
            TutorialGuideCatalog catalog = loadFixture();
            assertEquals("Ascent", catalog.current("quest.getup.header"), "real current getup header");
            assertEquals("Stand Up", catalog.legacy2019("quest.getup.header"), "real stalkerguide2019 getup header -- different real wording for the same step");
            assertEquals(catalog.current("quest.getup.text"), catalog.legacy2019("quest.getup.text"), "this particular shared step really is byte-identical between the two namespaces");
            assertNull(catalog.current("not_a_real_key"), "unknown key resolves to null, not a guess");
        }));

        out.add(test("the real 'stalkerguide2019' prefix never collides with the current 'stalkerguide.' prefix, despite the shared leading substring", () -> {
            TutorialGuideCatalog catalog = loadFixture();
            assertEquals(5, catalog.currentKeys().size(), "5 real current-namespace keys in the fixture");
            assertEquals(5, catalog.legacy2019Keys().size(), "5 real stalkerguide2019 keys in the fixture");
            assertTrue(!catalog.currentKeys().contains("2019.quest.getup.header"), "no cross-contamination between the two real prefixes");
        }));

        out.add(test("onlyInCurrent/onlyInLegacy2019/inBothVersions surface the real disclosed asymmetry exactly, not a guessed merge", () -> {
            TutorialGuideCatalog catalog = loadFixture();
            assertEquals(2, catalog.onlyInCurrent().size(), "2 real current-only keys in the fixture, matching the live-verified full-file count");
            assertTrue(catalog.onlyInCurrent().contains("controlpoints.control_point.ammo_crate"), "real ammo_crate step exists only in the current namespace");
            assertTrue(catalog.onlyInCurrent().contains("quest.walkaround.header"), "real walkaround header exists only in the current namespace -- stalkerguide2019's own walkaround step never got one");
            assertTrue(!catalog.onlyInCurrent().contains("quest.getup.header"), "a shared key (even with differing text) is not 'only in current'");

            assertTrue(catalog.onlyInLegacy2019().contains("quest.medkit_pre.text"), "real medkit_pre step exists only in stalkerguide2019");
            assertTrue(catalog.onlyInLegacy2019().contains("quest.medkit.desc.use"), "real medkit.desc.use step exists only in stalkerguide2019");

            List<String> both = catalog.inBothVersions();
            assertTrue(both.contains("quest.getup.header") && both.contains("quest.getup.text") && both.contains("quest.weaponuse.3.desc.killed"), "real shared step ids, whether or not their text actually matches");
            assertTrue(!both.contains("controlpoints.control_point.ammo_crate") && !both.contains("quest.medkit_pre.text"), "the two real asymmetric keys are correctly excluded from the shared set");
        }));

        return out;
    }
}
