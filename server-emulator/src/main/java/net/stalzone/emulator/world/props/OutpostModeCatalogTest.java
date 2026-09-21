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
 * Regression tests for {@link OutpostModeCatalog}, using a fixture built from real {@code
 * outpost.*} lines drawn directly from {@code stalker/lang/en.lang} (part 156, live-verified on
 * device) -- the real capture-the-point PvP mode's role/reward/outcome text.
 */
public final class OutpostModeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "outpost.attacker.desc=Capture the outpost",
            "outpost.defender.desc=Defend the outpost",
            "outpost.attacker_mail_reward.title=Reward {rank}",
            "outpost.attacker_mail_reward.desc=Your share of the spoils from the outpost operation",
            "outpost.defender_mail_reward.desc=Your share of the spoils from the outpost operation",
            "outpost.headquarters_spawn.name=Headquarters",
            "outpost.winner_text.title=You won the battle for the outpost",
            "outpost.loser_text.title=You lost the battle for the outpost",
            "outpost.rewards_low_desc=for 1 outpost capture",
            "outpost.rewards_medium_desc=for 1 outpost capture",
            "outpost.rewards_high_desc=for 1 outpost capture"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("outpost-mode-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OutpostModeCatalog loadFixture() throws IOException {
        return OutpostModeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real role objective and mail-reward text resolve, including the real shared reward .desc between both sides", () -> {
            OutpostModeCatalog catalog = loadFixture();
            assertEquals("Capture the outpost", catalog.field("attacker", "desc"), "attacker role objective");
            assertEquals("Defend the outpost", catalog.field("defender", "desc"), "defender role objective");
            assertEquals(catalog.field("attacker_mail_reward", "desc"), catalog.field("defender_mail_reward", "desc"), "both sides share identical real reward desc text");
        }));

        out.add(test("real headquarters spawn and win/loss outcome text resolve", () -> {
            OutpostModeCatalog catalog = loadFixture();
            assertEquals("Headquarters", catalog.field("headquarters_spawn", "name"), "headquarters spawn name");
            assertTrue(catalog.field("winner_text", "title").contains("won"), "winner text");
            assertTrue(catalog.field("loser_text", "title").contains("lost"), "loser text");
        }));

        out.add(test("the real zero-dot reward-tier fields resolve via the flat carve-out, ids()/rewardTiers() are sorted, and unknown lookups resolve to null rather than throwing", () -> {
            OutpostModeCatalog catalog = loadFixture();
            assertEquals("for 1 outpost capture", catalog.rewardDesc("low"), "low reward tier");
            assertEquals("for 1 outpost capture", catalog.rewardDesc("medium"), "medium reward tier");
            assertEquals("for 1 outpost capture", catalog.rewardDesc("high"), "high reward tier");
            List<String> tiers = catalog.rewardTiers();
            assertEquals(3, tiers.size(), "3 real reward tiers");
            assertTrue(tiers.get(0).compareTo(tiers.get(tiers.size() - 1)) <= 0, "reward tiers sorted ascending");
            List<String> ids = catalog.ids();
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "ids sorted ascending");
            assertNull(catalog.field("not_a_real_id", "desc"), "unknown id");
            assertNull(catalog.rewardDesc("not_a_real_tier"), "unknown reward tier");
        }));

        return out;
    }
}
