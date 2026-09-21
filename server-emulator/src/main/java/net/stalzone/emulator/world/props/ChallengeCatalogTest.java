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
 * Regression tests for {@link ChallengeCatalog}, using a fixture built from real {@code
 * challenge.*} lines drawn directly from {@code stalker/lang/en.lang} (part 152, live-verified
 * on device) -- real ids with an embedded literal {@code /} character, and the real "episode"
 * story-challenge group.
 */
public final class ChallengeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "challenge.wild_north/1.name=Destroying Murmur Squads",
            "challenge.wild_north/11.name=Boars Hunting",
            "challenge.episode.wild_north/4.name=Get on... a Tractor?",
            "challenge.episode.wild_north/7.name=Find a Wild North artifact"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("challenge-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ChallengeCatalog loadFixture() throws IOException {
        return ChallengeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real ids with an embedded literal '/' character resolve without special-casing", () -> {
            ChallengeCatalog catalog = loadFixture();
            assertEquals("Destroying Murmur Squads", catalog.field("wild_north/1", "name"), "wild_north/1 name");
            assertEquals("Boars Hunting", catalog.field("wild_north/11", "name"), "wild_north/11 name (corroborates StalkerMobsCatalog boar mutant)");
        }));

        out.add(test("the real 'episode' story-challenge group resolves as a distinct, separately-prefixed id space", () -> {
            ChallengeCatalog catalog = loadFixture();
            assertEquals("Get on... a Tractor?", catalog.field("episode.wild_north/4", "name"), "episode.wild_north/4 name");
            assertEquals("Find a Wild North artifact", catalog.field("episode.wild_north/7", "name"), "episode.wild_north/7 name");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            ChallengeCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
            assertNull(catalog.field("wild_north/1", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
