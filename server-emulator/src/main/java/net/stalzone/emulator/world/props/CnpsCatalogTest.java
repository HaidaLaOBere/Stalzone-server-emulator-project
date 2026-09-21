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
 * Regression tests for {@link CnpsCatalog}, using a fixture built from real {@code cnps.*} lines
 * drawn directly from {@code stalker/lang/en.lang} (part 107, live-verified on device) -- covering
 * the real zavet/dolg/svoboda/odinochki faction-name resolutions, the settlement reputation-rank
 * ladders (both plain rank titles and quoted bartender dialogue), the dialog tooltip crosswalk, the
 * new named entities, and the real quest names hidden in the availability namespace.
 */
public final class CnpsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "cnps.faction.default.relation.neutral=Neutral",
            "cnps.faction.default.relation.good=Good",
            "cnps.faction.zavet.name=Covenant",
            "cnps.faction.dolg.name=Frontier",
            "cnps.faction.svoboda.name=Rise",
            "cnps.faction.odinochki.name=Loner",
            "cnps.faction.monolit.name=Murmur",
            "cnps.faction.settlement_stalkers_csky.name=Stalker",
            "cnps.faction.settlement_stalkers_csky.relation.0=Unknown",
            "cnps.faction.settlement_stalkers_csky.relation.1=Picklock",
            "cnps.faction.settlement_stalkers_csky.relation.4=Vodyanoy",
            "cnps.faction.settlement_rostok.name=Neutral",
            "cnps.faction.settlement_rostok.relation.0=\"We don't serve your kind\"",
            "cnps.faction.settlement_rostok.relation.6=\"On the house!\"",
            "cnps.faction.settlement_covenant.relation.6=Apostle",
            "cnps.dialog.option.icon.tooltip.bandit.title=BANDIT",
            "cnps.dialog.option.icon.tooltip.bandit.desc=Respond as a bandit",
            "cnps.dialog.option.icon.tooltip.fork.title=PLOT TWIST",
            "cnps.dialog.option.icon.tooltip.fork.desc=Can affect the plot and the reward",
            "cnps.entity.vagant.name=Vagant",
            "cnps.entity.beelzebub.name=Beelzebub",
            "cnps.entity.gerrsvin.name=Herr Swine",
            "cnps.entity.415.name=415",
            "cnps.reward.mail.settlement=Faction Reward\\n\\nYour relationship with the faction has improved to the {level} level. For your achievements, you receive a reward",
            "cnps.reward.mail.arcade=Session Modes Reward\\n\\nFor reaching level {level} in session modes, you receive a special reward",
            "cnps.availability.desc.reputation=Insufficient reputation",
            "cnps.availability.desc.barter.trader_csky.bolota_safezone_2=Ataman's HQ—Picklock (2)",
            "cnps.availability.desc.barter.trader_csky.bolota_safezone_4.att.forend_sks=Complete Trade Affairs",
            "cnps.availability.desc.barter.trader_csky.bolota_safezone_5.att.forend_skstop=Complete Trade Affairs",
            "cnps.availability.desc.barter.trader_merc.x=Complete The Rat Pit"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("cnps-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static CnpsCatalog loadFixture() throws IOException {
        return CnpsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("factionName resolves the real zavet/dolg/svoboda/odinochki/monolit crosswalk", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("Covenant", catalog.factionName("zavet"), "zavet");
            assertEquals("Frontier", catalog.factionName("dolg"), "dolg");
            assertEquals("Rise", catalog.factionName("svoboda"), "svoboda");
            assertEquals("Loner", catalog.factionName("odinochki"), "odinochki");
            assertEquals("Murmur", catalog.factionName("monolit"), "monolit");
        }));

        out.add(test("defaultRelation resolves the real fallback relation vocabulary", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("Neutral", catalog.defaultRelation("neutral"), "neutral");
            assertEquals("Good", catalog.defaultRelation("good"), "good");
        }));

        out.add(test("factionRelationTitle resolves the real csky rank ladder without misreading the .name line", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("Stalker", catalog.factionName("settlement_stalkers_csky"), "csky faction name");
            assertEquals("Unknown", catalog.factionRelationTitle("settlement_stalkers_csky", "0"), "csky level 0");
            assertEquals("Picklock", catalog.factionRelationTitle("settlement_stalkers_csky", "1"), "csky level 1");
            assertEquals("Vodyanoy", catalog.factionRelationTitle("settlement_stalkers_csky", "4"), "csky level 4");
        }));

        out.add(test("factionRelationTitle resolves real quoted bartender dialogue for rostok", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("\"We don't serve your kind\"", catalog.factionRelationTitle("settlement_rostok", "0"), "rostok level 0");
            assertEquals("\"On the house!\"", catalog.factionRelationTitle("settlement_rostok", "6"), "rostok level 6");
        }));

        out.add(test("factionRelationTitle resolves the real Covenant ecclesiastical rank ladder", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("Apostle", catalog.factionRelationTitle("settlement_covenant", "6"), "covenant level 6");
        }));

        out.add(test("dialogTooltipTitle/Description resolve real UI tooltip text", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("BANDIT", catalog.dialogTooltipTitle("bandit"), "bandit title");
            assertEquals("Respond as a bandit", catalog.dialogTooltipDescription("bandit"), "bandit desc");
            assertEquals("PLOT TWIST", catalog.dialogTooltipTitle("fork"), "fork title");
        }));

        out.add(test("entityName resolves Vagant plus new named entities Beelzebub/Herr Swine/415", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals("Vagant", catalog.entityName("vagant"), "vagant");
            assertEquals("Beelzebub", catalog.entityName("beelzebub"), "beelzebub");
            assertEquals("Herr Swine", catalog.entityName("gerrsvin"), "gerrsvin");
            assertEquals("415", catalog.entityName("415"), "415");
        }));

        out.add(test("rewardMail resolves both real reward-mail templates", () -> {
            CnpsCatalog catalog = loadFixture();
            assertTrue(catalog.rewardMail("settlement").contains("Faction Reward"), "settlement reward");
            assertTrue(catalog.rewardMail("arcade").contains("Session Modes Reward"), "arcade reward");
        }));

        out.add(test("realQuestNamesReferenced extracts real quest names, deduplicated and sorted", () -> {
            CnpsCatalog catalog = loadFixture();
            List<String> names = catalog.realQuestNamesReferenced();
            assertEquals(2, names.size(), "distinct quest name count");
            assertEquals("The Rat Pit", names.get(0), "first alphabetically");
            assertEquals("Trade Affairs", names.get(1), "second alphabetically");
        }));

        out.add(test("availabilityDescriptionCount matches the real fixture line count", () -> {
            CnpsCatalog catalog = loadFixture();
            assertEquals(5, catalog.availabilityDescriptionCount(), "availability description count");
        }));

        out.add(test("unknown ids resolve to null across all lookups", () -> {
            CnpsCatalog catalog = loadFixture();
            assertNull(catalog.factionName("not_a_real_faction"), "unknown faction");
            assertNull(catalog.entityName("not_a_real_entity"), "unknown entity");
            assertNull(catalog.factionRelationTitle("settlement_stalkers_csky", "99"), "unknown relation level");
        }));

        return out;
    }
}
