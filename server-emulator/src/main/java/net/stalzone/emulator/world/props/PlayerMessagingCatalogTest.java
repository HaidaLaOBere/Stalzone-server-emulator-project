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
 * Regression tests for {@link PlayerMessagingCatalog}, using a fixture built from real {@code
 * mail.message.*}/{@code bonuscode.*} lines drawn directly from {@code stalker/lang/en.lang}
 * (part 163, live-verified on device) -- including the real STALCRAFT-to-STALZONE lineage text.
 */
public final class PlayerMessagingCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "mail.message.season_reward.rewarded.member.title=Season Results",
            "mail.message.season_reward.rewarded.member.text=Crimson Casings earned this season: {sleeves}",
            "mail.message.season_reward.rewarded.leader.title=Reward to the subdivision leader",
            "mail.message.season_reward.no_reward.member.title=Season Results",
            "bonuscode.stalzone.old=Here's a little something to mark the transition to STALZONE.",
            "bonuscode.stalzone2026.old=for all the time you've spent with us, first in STALCRAFT and now in STALZONE.",
            "bonuscode.stalzone2026.newbies=this year STALZONE turns twelve. That's right — TWELVE YEARS."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("player-messaging-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PlayerMessagingCatalog loadFixture() throws IOException {
        return PlayerMessagingCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real season-reward mail fields resolve under their own ids, including the real Crimson Casings currency reference", () -> {
            PlayerMessagingCatalog catalog = loadFixture();
            assertEquals("Season Results", catalog.mailField("season_reward.rewarded.member", "title"), "rewarded.member title");
            assertTrue(catalog.mailField("season_reward.rewarded.member", "text").contains("Crimson Casings"), "rewarded.member text references real Crimson Casings currency");
            assertEquals("Reward to the subdivision leader", catalog.mailField("season_reward.rewarded.leader", "title"), "rewarded.leader title");
        }));

        out.add(test("the real STALCRAFT-to-STALZONE lineage text and anniversary text resolve exactly as authored", () -> {
            PlayerMessagingCatalog catalog = loadFixture();
            assertTrue(catalog.bonusCodeField("stalzone", "old").contains("transition to STALZONE"), "real STALZONE transition text");
            assertTrue(catalog.bonusCodeField("stalzone2026", "old").contains("STALCRAFT"), "real STALCRAFT predecessor reference");
            assertTrue(catalog.bonusCodeField("stalzone2026", "newbies").contains("TWELVE YEARS"), "real twelve-year anniversary text");
        }));

        out.add(test("mailIds()/bonusCodeIds() are sorted and unknown lookups resolve to null rather than throwing", () -> {
            PlayerMessagingCatalog catalog = loadFixture();
            List<String> mailIds = catalog.mailIds();
            assertEquals(3, mailIds.size(), "3 real mail ids in fixture");
            assertTrue(mailIds.get(0).compareTo(mailIds.get(mailIds.size() - 1)) <= 0, "mailIds sorted ascending");
            List<String> bonusCodeIds = catalog.bonusCodeIds();
            assertEquals(2, bonusCodeIds.size(), "2 real bonus code ids in fixture");
            assertNull(catalog.mailField("not_a_real_id", "title"), "unknown mail id");
            assertNull(catalog.bonusCodeField("not_a_real_id", "old"), "unknown bonus code id");
        }));

        return out;
    }
}
