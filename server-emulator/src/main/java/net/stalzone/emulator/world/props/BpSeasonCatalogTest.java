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
 * Regression tests for {@link BpSeasonCatalog}, using a fixture built from real {@code
 * bpseason.*} lines drawn directly from {@code stalker/lang/en.lang} (part 131, live-verified on
 * device) -- a real id embedding a literal {@code /} character, the real duplicated-with-gui
 * spring24 root title/description, a real ads-group entry, and a real flat rewards label.
 */
public final class BpSeasonCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "bpseason.24/spring.event.title=Arena of Slavs",
            "bpseason.24/spring.event.howtoplay.block1.title=Protect the Idol of Perun",
            "bpseason.24/spring.event.howtoplay.block1.description=Two teams participate in the Arena at the same time.",
            "bpseason.26/spring.event.howtoplay.block1.title=REICHSTAG",
            "bpseason.ads.ad_battlepass.title=SEASON PASS",
            "bpseason.rewards.motif=Motif"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("bpseason-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static BpSeasonCatalog loadFixture() throws IOException {
        return BpSeasonCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real season id embedding a literal '/' resolves its root and nested block fields", () -> {
            BpSeasonCatalog catalog = loadFixture();
            assertEquals("Arena of Slavs", catalog.field("24/spring.event", "title"), "24/spring root title");
            assertEquals("Protect the Idol of Perun", catalog.field("24/spring.event.howtoplay.block1", "title"), "24/spring block1 title");
            assertEquals("Two teams participate in the Arena at the same time.", catalog.field("24/spring.event.howtoplay.block1", "description"), "24/spring block1 description");
        }));

        out.add(test("the real Reichstag-tied 26/spring block and the ads/rewards groups resolve independently", () -> {
            BpSeasonCatalog catalog = loadFixture();
            assertEquals("REICHSTAG", catalog.field("26/spring.event.howtoplay.block1", "title"), "26/spring block1 title");
            assertEquals("SEASON PASS", catalog.field("ads.ad_battlepass", "title"), "ads ad_battlepass title");
            assertEquals("Motif", catalog.field("rewards", "motif"), "rewards motif label");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            BpSeasonCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "5 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "title"), "unknown id");
            assertNull(catalog.field("24/spring.event", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
