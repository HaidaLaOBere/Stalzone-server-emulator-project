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
 * Regression tests for {@link AccountBadgeCatalog}, using a fixture built from real {@code
 * profile_icon.*}/{@code menulinks.help.*}/{@code review.notf.*} lines drawn directly from
 * {@code stalker/lang/en.lang} (part 160, live-verified on device).
 */
public final class AccountBadgeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "profile_icon.tg.text=Telegram",
            "profile_icon.admin.text=Developer",
            "profile_icon.helper.text=Developer",
            "profile_icon.tiktok.text=TikTok Blogger",
            "profile_icon.chzzk.text=TikTok Blogger",
            "menulinks.help.forum=Forum",
            "menulinks.help.credits=About the developers",
            "review.notf.title=How do you like the game?",
            "review.notf.description=Tell us what you think!"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("account-badge-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static AccountBadgeCatalog loadFixture() throws IOException {
        return AccountBadgeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real badge text resolves, including the real chzzk/tiktok shared 'TikTok Blogger' text oddity", () -> {
            AccountBadgeCatalog catalog = loadFixture();
            assertEquals("Telegram", catalog.badgeText("tg"), "tg badge");
            assertEquals(catalog.badgeText("tiktok"), catalog.badgeText("chzzk"), "chzzk shares tiktok's real badge text despite being a distinct platform");
            assertEquals(catalog.badgeText("admin"), catalog.badgeText("helper"), "admin/helper share the real 'Developer' text");
        }));

        out.add(test("real footer help links and review-prompt fields resolve", () -> {
            AccountBadgeCatalog catalog = loadFixture();
            assertEquals("Forum", catalog.helpLink("forum"), "forum help link");
            assertEquals("About the developers", catalog.helpLink("credits"), "credits help link");
            assertEquals("How do you like the game?", catalog.reviewPromptField("title"), "review prompt title");
        }));

        out.add(test("badgeIds()/helpLinkIds() are sorted, and unknown lookups resolve to null rather than throwing", () -> {
            AccountBadgeCatalog catalog = loadFixture();
            List<String> badgeIds = catalog.badgeIds();
            assertEquals(5, badgeIds.size(), "5 real badge ids in fixture");
            assertTrue(badgeIds.get(0).compareTo(badgeIds.get(badgeIds.size() - 1)) <= 0, "badgeIds sorted ascending");
            List<String> helpLinkIds = catalog.helpLinkIds();
            assertEquals(2, helpLinkIds.size(), "2 real help link ids in fixture");
            assertNull(catalog.badgeText("not_a_real_id"), "unknown badge id");
            assertNull(catalog.helpLink("not_a_real_id"), "unknown help link id");
            assertNull(catalog.reviewPromptField("not_a_real_field"), "unknown review prompt field");
        }));

        return out;
    }
}
