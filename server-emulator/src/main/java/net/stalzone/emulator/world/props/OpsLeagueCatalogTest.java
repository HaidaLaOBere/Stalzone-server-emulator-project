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
 * Regression tests for {@link OpsLeagueCatalog}, using a fixture built from the real, complete
 * {@code ops.leagues.*} namespace (part 170, live-verified on device against the full {@code
 * en.lang} file -- all 10 real lines, verbatim; no privacy concerns, this namespace contains no
 * player-submitted or account-identifying text).
 */
public final class OpsLeagueCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "ops.leagues.1.name=Absolute Unity",
            "ops.leagues.2.name=Ascendant",
            "ops.leagues.3.name=Revenant",
            "ops.leagues.4.name=Acolyte",
            "ops.leagues.5.name=Adept",
            "ops.leagues.1.reward_text=Absolute Unity\\n\\nYou|We|All are absolutely one!\\n\\nUnity has taken a change in your face. You have proven yourself worthy|capable|aware. Wait — we will be near, to offer|take away.",
            "ops.leagues.2.reward_text=Ascendant\\n\\nYou have stepped onto the path of ascension toward Unity.\\n\\nYou are becoming a part|block|component of a great algorithm, where everyone matters. You are closer to the truth|verity|meaning.",
            "ops.leagues.3.reward_text=Revenant\\n\\nYou have found yourself, Revenant.\\n\\nYou died|crossed to the other side and returned new|different|changed. Rise as the strongest among equals|the unified.",
            "ops.leagues.4.reward_text=Acolyte\\n\\nYou have become an Acolyte.\\n\\nWe welcome you, worthy enemy|friend|rival, into our ranks. You will gain wisdom|understanding|knowledge. You will become one of all.",
            "ops.leagues.5.reward_text=Adept\\n\\nYou have become an Adept.\\n\\nYou have joined us, though you do not yet understand it. Awareness will come soon; the understanding of this blessing will follow quickly."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("ops-league-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OpsLeagueCatalog loadFixture() throws IOException {
        return OpsLeagueCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real 10-line namespace resolves to 5 real ids, each with real name + reward_text", () -> {
            OpsLeagueCatalog catalog = loadFixture();
            assertEquals(5, catalog.size(), "5 real tier ids in the fixture, matching the live-verified full-file count");
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "ids() surfaces all 5 real ids");
            assertTrue(ids.contains("1") && ids.contains("5"), "real ids 1 and 5 are both present");
        }));

        out.add(test("real tier names resolve by real numeric id", () -> {
            OpsLeagueCatalog catalog = loadFixture();
            assertEquals("Absolute Unity", catalog.name("1"), "real id 1 tier name");
            assertEquals("Ascendant", catalog.name("2"), "real id 2 tier name");
            assertEquals("Revenant", catalog.name("3"), "real id 3 tier name");
            assertEquals("Acolyte", catalog.name("4"), "real id 4 tier name");
            assertEquals("Adept", catalog.name("5"), "real id 5 tier name");
        }));

        out.add(test("real reward_text passages resolve and repeat the real tier name at their start", () -> {
            OpsLeagueCatalog catalog = loadFixture();
            assertTrue(catalog.rewardText("1").startsWith("Absolute Unity"), "real id 1 reward_text opens with its own tier name");
            assertTrue(catalog.rewardText("5").contains("you do not yet understand it"), "real id 5 reward_text's real initiate-toned line");
            assertTrue(catalog.rewardText("1").contains("absolutely one"), "real id 1 reward_text's real union-toned line");
        }));

        out.add(test("fieldNames confirms name/reward_text are the only two real fields, sorted", () -> {
            OpsLeagueCatalog catalog = loadFixture();
            assertEquals(List.of("name", "reward_text"), catalog.fieldNames("3"), "real field names for a known id, sorted");
            assertTrue(catalog.fieldNames("not_a_real_id").isEmpty(), "unknown real id has no real field names");
        }));

        out.add(test("unknown id/field resolve to null rather than a guess, and this class asserts no tier order", () -> {
            OpsLeagueCatalog catalog = loadFixture();
            assertNull(catalog.name("6"), "no real id 6 in this namespace");
            assertNull(catalog.field("1", "not_a_real_field"), "unknown real field resolves to null");
            assertNull(catalog.name("not_a_real_id"), "unknown real id resolves to null");
        }));

        return out;
    }
}
