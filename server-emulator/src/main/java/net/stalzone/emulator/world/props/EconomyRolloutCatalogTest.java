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
 * Regression tests for {@link EconomyRolloutCatalog}, using a fixture built from real {@code
 * banner.*}/{@code barter.category.*}/{@code main_menu_popup.act1_ticket_warning.*} lines drawn
 * directly from {@code stalker/lang/en.lang} (part 161, live-verified on device).
 */
public final class EconomyRolloutCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "banner.information.barter_upgrade_compensation.title=«BARTER AND MODIFICATIONS REWORK»",
            "banner.information.barter_upgrade_compensation.info_block.1.title=CHANGES",
            "banner.information.barter_upgrade_compensation.info_block.1.link.1.name=Find out more about the changes",
            "banner.information.barter_upgrade_compensation.info_block.2.title=COMPENSATION",
            "barter.category.armor=Suits",
            "barter.category.modules=Attachments",
            "main_menu_popup.act1_ticket_warning.title=ACT TICKETS REMOVAL — JUNE 4",
            "main_menu_popup.act1_ticket_warning.text=1 ticket = 50 coins."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("economy-rollout-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static EconomyRolloutCatalog loadFixture() throws IOException {
        return EconomyRolloutCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real deeply-nested banner ids resolve without special-casing", () -> {
            EconomyRolloutCatalog catalog = loadFixture();
            assertTrue(catalog.bannerField("information.barter_upgrade_compensation", "title").contains("REWORK"), "banner base title");
            assertEquals("CHANGES", catalog.bannerField("information.barter_upgrade_compensation.info_block.1", "title"), "info_block.1 title");
            assertEquals("Find out more about the changes", catalog.bannerField("information.barter_upgrade_compensation.info_block.1.link.1", "name"), "info_block.1.link.1 name");
            assertEquals("COMPENSATION", catalog.bannerField("information.barter_upgrade_compensation.info_block.2", "title"), "info_block.2 title");
        }));

        out.add(test("real barter category names and the Act 1 Ticket warning fields resolve", () -> {
            EconomyRolloutCatalog catalog = loadFixture();
            assertEquals("Suits", catalog.barterCategory("armor"), "armor category");
            assertEquals("Attachments", catalog.barterCategory("modules"), "modules category");
            assertTrue(catalog.act1TicketWarningField("title").contains("JUNE 4"), "act1 ticket warning title");
            assertTrue(catalog.act1TicketWarningField("text").contains("50 coins"), "act1 ticket warning text");
        }));

        out.add(test("bannerIds() is sorted and unknown lookups resolve to null rather than throwing", () -> {
            EconomyRolloutCatalog catalog = loadFixture();
            List<String> ids = catalog.bannerIds();
            assertEquals(4, ids.size(), "4 real banner ids in fixture (base, info_block.1, info_block.1.link.1, info_block.2)");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.bannerField("not_a_real_id", "title"), "unknown banner id");
            assertNull(catalog.barterCategory("not_a_real_category"), "unknown barter category");
        }));

        return out;
    }
}
