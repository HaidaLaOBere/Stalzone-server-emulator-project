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
 * Regression tests for {@link SettlementPerkCatalog}, using a fixture built from real {@code
 * settlement.id.*} lines drawn directly from {@code stalker/lang/en.lang} (part 125, live-verified
 * on device) -- covering the generic {@code general.perk.*} value lookup, a real settlement's
 * title/default_desc/level ladder with its own concrete per-level perk values, the {@code sever}
 * alias's deeper 7-level ladder plus its real cross-settlement {@code rostok} unlock tie, and the
 * {@code battlefields.unavailableLine} one-off.
 */
public final class SettlementPerkCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "settlement.id.general.perk.research_cost_25=25% discount on research",
            "settlement.id.general.perk.art_price_50=+50% to the cost of selling artifacts and mutant parts",
            "settlement.id.csky.title=Ataman's HQ",
            "settlement.id.csky.default_desc=Nobody knows a thing about you.",
            "settlement.id.csky.level.3.desc=You earned the respect of the stalkers in the Swamps.",
            "settlement.id.csky.level.3.perk.art_price=+10% to the cost of selling artifacts and mutant parts",
            "settlement.id.csky.level.5.perk.repair_cost=20% discount on repairs",
            "settlement.id.lodochnaya.level.3.perk.art_price=+20% to the cost of selling artifacts and mutant parts",
            "settlement.id.rostok.level.7.perk.sever=Access to the Drunken Geiger",
            "settlement.id.sever.level.7.desc=North, south--the whole Zone has heard word of your glorious deeds.",
            "settlement.id.battlefields.title=Session battles",
            "settlement.id.battlefields.unavailableLine=Not enough reputation"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("settlement-perk-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SettlementPerkCatalog loadFixture() throws IOException {
        return SettlementPerkCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("general.perk.* resolves as a flat, settlement-agnostic value lookup", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            assertEquals("25% discount on research", catalog.generalPerkDescription("research_cost_25"), "research_cost_25");
            assertEquals("+50% to the cost of selling artifacts and mutant parts",
                    catalog.generalPerkDescription("art_price_50"), "art_price_50");
        }));

        out.add(test("csky's title/default_desc/level desc and its OWN per-level perk value resolve", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            assertEquals("Ataman's HQ", catalog.title("csky"), "csky title");
            assertEquals("Nobody knows a thing about you.", catalog.defaultDesc("csky"), "csky default_desc");
            assertTrue(catalog.levelDesc("csky", 3).startsWith("You earned the respect"), "csky level 3 desc");
            assertEquals("+10% to the cost of selling artifacts and mutant parts",
                    catalog.levelPerk("csky", 3, "art_price"), "csky level 3 art_price");
            assertEquals("20% discount on repairs", catalog.levelPerk("csky", 5, "repair_cost"), "csky level 5 repair_cost");
        }));

        out.add(test("two settlements' own perk values for the same perk id genuinely differ", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            assertEquals("+10% to the cost of selling artifacts and mutant parts",
                    catalog.levelPerk("csky", 3, "art_price"), "csky level 3 art_price");
            assertEquals("+20% to the cost of selling artifacts and mutant parts",
                    catalog.levelPerk("lodochnaya", 3, "art_price"), "lodochnaya level 3 art_price -- different value");
        }));

        out.add(test("the real rostok-to-sever cross-settlement unlock and sever's deeper ladder resolve", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            assertEquals("Access to the Drunken Geiger", catalog.levelPerk("rostok", 7, "sever"), "rostok level 7 sever unlock");
            assertTrue(catalog.levelDesc("sever", 7).startsWith("North, south"), "sever level 7 desc (deeper than the 5-level norm)");
        }));

        out.add(test("battlefields' one-off unavailableLine field resolves via the extra-field map", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            assertEquals("Session battles", catalog.title("battlefields"), "battlefields title");
            assertEquals("Not enough reputation", catalog.extraField("battlefields", "unavailableLine"), "battlefields unavailableLine");
        }));

        out.add(test("settlementIds() is sorted and unknown ids/levels resolve to null/empty rather than throwing", () -> {
            SettlementPerkCatalog catalog = loadFixture();
            List<String> ids = catalog.settlementIds();
            assertEquals(2, ids.size(), "2 real settlement ids with a title in fixture (csky, battlefields)");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertNull(catalog.title("not_a_real_settlement"), "unknown settlement title");
            assertNull(catalog.levelDesc("csky", 99), "unknown level desc");
            assertEquals(0, catalog.levelPerkIds("csky", 99).size(), "unknown level perk ids");
        }));

        return out;
    }
}
