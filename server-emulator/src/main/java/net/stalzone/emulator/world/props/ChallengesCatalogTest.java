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
 * Regression tests for {@link ChallengesCatalog}, using a fixture built from real {@code
 * challenges.*} lines drawn directly from {@code stalker/lang/en.lang} (part 116, live-verified on
 * device) -- covering the single generic {@code (id, field)} split across ordinary numbered
 * challenges, deeply nested season sub-ids with the real {@code .na} variant, the real Reichstag
 * tail, and every one-off field-name exception.
 */
public final class ChallengesCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "challenges.season.base/1.name=Completing a task from the announcement board and faction missions",
            "challenges.season.summer26/pg_challenges/g1/7.name=Complete the News for Shaman quest",
            "challenges.season.summer26/pg_challenges/g1/7.na.label=Roadside Picnic",
            "challenges.season.summer26/pg_challenges/g1/7.label=Roadside Picnic",
            "challenges.season.summer26/pg_challenges/g1/7.hint=Follow the South storyline. Quests can be tracked in your PDA\\n(P key)",
            "challenges.reichstag/1.name=Deal {amount} damage",
            "challenges.reichstag/8.name=Complete 1 game",
            "challenges.triumph.triumph2025/1.name=Quantum Batteries obtained from Outposts",
            "challenges.newyear24.first_week=First week",
            "challenges.newyear24.second_week=Second week",
            "challenges.newyear24.third_week=Third week",
            "challenges.achievements.complete=Complete the {key} achievement",
            "challenges.upg_motif.fmg9_bureau.desc=Get money from the trader for selling items",
            "challenges.season.summer25/challenges/s/9.name=Successfully complete a mission using selected 122mm Gvozdika, 152mm Akatsiya, and BM-21 Grad",
            "challenges.season.summer25/challenges/s/9.description=Leave loudly"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("challenges-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ChallengesCatalog loadFixture() throws IOException {
        return ChallengesCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("name resolves an ordinary numbered season challenge", () -> {
            ChallengesCatalog catalog = loadFixture();
            assertTrue(catalog.name("season.base/1").contains("announcement board"), "season.base/1 name");
        }));

        out.add(test("deeply nested season sub-ids resolve, keeping the real .na variant as a distinct id", () -> {
            ChallengesCatalog catalog = loadFixture();
            String id = "season.summer26/pg_challenges/g1/7";
            assertEquals("Complete the News for Shaman quest", catalog.name(id), "g1/7 name");
            assertEquals("Roadside Picnic", catalog.label(id), "g1/7 label");
            assertTrue(catalog.hint(id).contains("PDA"), "g1/7 hint");
            assertEquals("Roadside Picnic", catalog.field(id + ".na", "label"), "g1/7 .na variant is a distinct real id");
        }));

        out.add(test("reichstag challenges resolve without asserting a corroboration ordinal", () -> {
            ChallengesCatalog catalog = loadFixture();
            assertEquals("Deal {amount} damage", catalog.name("reichstag/1"), "reichstag/1");
            assertEquals("Complete 1 game", catalog.name("reichstag/8"), "reichstag/8");
        }));

        out.add(test("triumph leaderboard entry resolves", () -> {
            ChallengesCatalog catalog = loadFixture();
            assertTrue(catalog.name("triumph.triumph2025/1").contains("Quantum Batteries"), "triumph2025/1");
        }));

        out.add(test("real one-off field-name exceptions all resolve via the same generic split", () -> {
            ChallengesCatalog catalog = loadFixture();
            assertEquals("First week", catalog.field("newyear24", "first_week"), "newyear24 first_week");
            assertEquals("Second week", catalog.field("newyear24", "second_week"), "newyear24 second_week");
            assertEquals("Third week", catalog.field("newyear24", "third_week"), "newyear24 third_week");
            assertEquals("Complete the {key} achievement", catalog.field("achievements", "complete"), "achievements complete template");
            assertEquals("Get money from the trader for selling items", catalog.field("upg_motif.fmg9_bureau", "desc"), "upg_motif.fmg9_bureau desc");
        }));

        out.add(test("name/description resolve together for a real dual-field challenge id", () -> {
            ChallengesCatalog catalog = loadFixture();
            String id = "season.summer25/challenges/s/9";
            assertTrue(catalog.name(id).contains("Gvozdika"), "s/9 name");
            assertEquals("Leave loudly", catalog.description(id), "s/9 description");
        }));

        out.add(test("ids/fieldNames are sorted and reflect the real fixture data", () -> {
            ChallengesCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertTrue(ids.contains("reichstag/1"), "ids contains reichstag/1");
            List<String> fields = catalog.fieldNames("season.summer26/pg_challenges/g1/7");
            assertEquals(3, fields.size(), "g1/7 has name/label/hint");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            ChallengesCatalog catalog = loadFixture();
            assertNull(catalog.name("not_a_real_id"), "unknown id");
            assertNull(catalog.field("reichstag/1", "not_a_real_field"), "unknown field on known id");
            assertEquals(0, catalog.fieldNames("not_a_real_id").size(), "unknown id field names");
        }));

        return out;
    }
}
