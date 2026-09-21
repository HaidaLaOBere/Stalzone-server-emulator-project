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
 * Regression tests for {@link Spring26ChallengeCatalog}, using a fixture built from real key=value
 * lines drawn directly from {@code stalker/lang/en.lang} (part 167, live-verified on device) -- a
 * representative slice of all 5 real id groups in the 41-line {@code spring26/challenges/*}
 * namespace.
 */
public final class Spring26ChallengeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "spring26/challenges/ships_artefacts_episode/1.name=Find the Jelly artifact",
            "spring26/challenges/tanks_artefacts_episode/1.name=Find the Link artifact",
            "spring26/challenges/reichstag/1.name=Reach class level {amount} with the Assaulter class in the Assault on the Reichstag mode",
            "spring26/challenges/reichstag/9.name=Play {amount} matches in the Assault on the Reichstag mode",
            "spring26/challenges/fools_day/3.name=Why was I given a butterfly knife?",
            "spring26/challenges/season/insideout/1.name=Clear out 10 events in the Inside Out zones without dying",
            "spring26/challenges/season/insideout/9.name=Claim the reward when the centrifuge is at maximum load at Forge-11"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("spring26-challenge-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static Spring26ChallengeCatalog loadFixture() throws IOException {
        return Spring26ChallengeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real bare spring26/challenges/* names resolve across all 5 real id groups", () -> {
            Spring26ChallengeCatalog catalog = loadFixture();
            assertEquals(7, catalog.ids().size(), "7 real ids in the fixture");
            assertEquals("Find the Jelly artifact", catalog.name("ships_artefacts_episode/1"), "real artifact-hunt name, confirms the item.art.lesta_jele=\"Jelly\" crosswalk");
            assertEquals("Find the Link artifact", catalog.name("tanks_artefacts_episode/1"), "real artifact-hunt name, confirms the item.art.lesta_tracks=\"Link\" crosswalk");
            assertTrue(catalog.name("reichstag/1").contains("Reichstag"), "real reichstag challenge name -- another entry in the standing corroboration chain, no new ordinal per part 109's note");
            assertEquals("Why was I given a butterfly knife?", catalog.name("fools_day/3"), "real, intentionally comedic April Fools' copy preserved verbatim");
            assertNull(catalog.name("not_a_real_id"), "unknown id resolves to null, not a guess");
        }));

        out.add(test("idsInGroup resolves real sub-families, including the slash-embedded season/insideout group", () -> {
            Spring26ChallengeCatalog catalog = loadFixture();
            List<String> reichstagIds = catalog.idsInGroup("reichstag");
            assertEquals(2, reichstagIds.size(), "2 real reichstag ids in the fixture");
            assertTrue(reichstagIds.contains("reichstag/1") && reichstagIds.contains("reichstag/9"), "both real reichstag ids present");

            List<String> insideoutIds = catalog.idsInGroup("season/insideout");
            assertEquals(2, insideoutIds.size(), "2 real season/insideout ids in the fixture, group id itself contains a real '/'");
            assertTrue(catalog.name("season/insideout/9").contains("Forge-11"), "real Forge-11 location name, ties to this project's existing Inside Out event coverage");

            assertEquals(0, catalog.idsInGroup("no_such_group").size(), "an unknown group resolves to an empty list, not null");
        }));

        out.add(test("fieldNames() confirms 'name' is the only real field in this namespace, per this class's own doc", () -> {
            Spring26ChallengeCatalog catalog = loadFixture();
            assertEquals(List.of("name"), catalog.fieldNames("reichstag/1"), "only real field suffix in this namespace");
            assertEquals(List.of(), catalog.fieldNames("not_a_real_id"), "unknown id has no fields, not null");
        }));

        return out;
    }
}
