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
 * Regression tests for {@link TriumphCatalog}, using a fixture built from the real, complete
 * {@code triumph.2026.*} namespace (part 171, live-verified against the full {@code en.lang}
 * file -- all 4 real lines, verbatim; no privacy concerns, this namespace contains no
 * player-submitted or account-identifying text).
 */
public final class TriumphCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "triumph.2026.reward_type.reward_level_10=STATUE",
            "triumph.2026.reward_type.reward_level_30=CUSTOMIZATION",
            "triumph.2026.mail.message.reward.title=Reward for completing the Triumphs:",
            "triumph.2026.mail.message.reward.text=Your reward for completing {count} Triumphs"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("triumph-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static TriumphCatalog loadFixture() throws IOException {
        return TriumphCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real 4-line namespace loads exactly, size() and keys() confirm no more/no fewer", () -> {
            TriumphCatalog catalog = loadFixture();
            assertEquals(4, catalog.size(), "4 real lines in the fixture, matching the live-verified full-file count");
            assertEquals(4, catalog.keys().size(), "keys() surfaces all 4 real key suffixes");
        }));

        out.add(test("rewardTypeForLevel resolves the real embedded-level keys and nothing else", () -> {
            TriumphCatalog catalog = loadFixture();
            assertEquals("STATUE", catalog.rewardTypeForLevel(10), "real level-10 reward type");
            assertEquals("CUSTOMIZATION", catalog.rewardTypeForLevel(30), "real level-30 reward type");
            assertNull(catalog.rewardTypeForLevel(20), "no real level-20 entry exists in this namespace");
        }));

        out.add(test("mailRewardTitle/mailRewardText resolve the real single-instance mail fields", () -> {
            TriumphCatalog catalog = loadFixture();
            assertEquals("Reward for completing the Triumphs:", catalog.mailRewardTitle(), "real mail reward title");
            assertEquals("Your reward for completing {count} Triumphs", catalog.mailRewardText(), "real mail reward text, real {count} placeholder preserved verbatim");
        }));

        out.add(test("field() resolves by exact real key suffix, and unknown keys resolve to null rather than a guess", () -> {
            TriumphCatalog catalog = loadFixture();
            assertEquals("STATUE", catalog.field("reward_type.reward_level_10"), "field() matches the typed accessor for the same real key");
            assertNull(catalog.field("not_a_real_key"), "unknown real key resolves to null");
        }));

        return out;
    }
}
