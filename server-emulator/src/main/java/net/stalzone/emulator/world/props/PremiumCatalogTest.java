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
 * Regression tests for {@link PremiumCatalog}, using a fixture built from real {@code
 * premium.*} lines drawn directly from {@code stalker/lang/en.lang} (part 143, live-verified on
 * device) -- the real flat {@code desc} exception, the older numbered perk_block layout, and the
 * newer perkBlocks layout's real digit-suffixed {@code details.<n>} field.
 */
public final class PremiumCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "premium.desc=Explore the world of the Zone with STALZONE PREMIUM benefits. Unlock access to:",
            "premium.inline_perk.barter=+25% barter resources from in-game events",
            "premium.perk_block.fillers.title=2. Earnings from events",
            "premium.perk_block.fillers.description=Complete events in the open world and earn §#FFC34F15%§r more resources for sale.",
            "premium.purchaseOptions.best.label=BEST OFFER",
            "premium.perkBlocks.quest.title=TASKS AND CONTRACTS",
            "premium.perkBlocks.quest.details.1=SPECIAL DAILY TASKS AT BASES",
            "premium.perkBlocks.quest.details.2=ADDITIONAL WEEKLY TASKS AT BASES",
            "premium.perkBlocks.quest.desc=Each base offers special daily and weekly tasks, as well as delivery contracts."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("premium-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PremiumCatalog loadFixture() throws IOException {
        return PremiumCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real flat desc top-banner line resolves separately from the generic table", () -> {
            PremiumCatalog catalog = loadFixture();
            assertEquals("Explore the world of the Zone with STALZONE PREMIUM benefits. Unlock access to:", catalog.desc(), "premium.desc");
            assertEquals("+25% barter resources from in-game events", catalog.field("inline_perk", "barter"), "inline_perk barter");
        }));

        out.add(test("the older perk_block layout and the purchase option label resolve", () -> {
            PremiumCatalog catalog = loadFixture();
            assertEquals("2. Earnings from events", catalog.field("perk_block.fillers", "title"), "perk_block.fillers title");
            assertTrue(catalog.field("perk_block.fillers", "description").contains("15%"), "perk_block.fillers description contains real percent");
            assertEquals("BEST OFFER", catalog.field("purchaseOptions.best", "label"), "purchaseOptions.best label");
        }));

        out.add(test("the newer perkBlocks layout's digit-suffixed details field resolves distinct from title/desc, and ids() is sorted with null-safe lookups", () -> {
            PremiumCatalog catalog = loadFixture();
            assertEquals("TASKS AND CONTRACTS", catalog.field("perkBlocks.quest", "title"), "perkBlocks.quest title");
            assertEquals("SPECIAL DAILY TASKS AT BASES", catalog.field("perkBlocks.quest.details", "1"), "perkBlocks.quest.details field 1");
            assertEquals("ADDITIONAL WEEKLY TASKS AT BASES", catalog.field("perkBlocks.quest.details", "2"), "perkBlocks.quest.details field 2");
            List<String> ids = catalog.ids();
            assertTrue(ids.size() > 0, "at least one real id in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "title"), "unknown id");
        }));

        return out;
    }
}
