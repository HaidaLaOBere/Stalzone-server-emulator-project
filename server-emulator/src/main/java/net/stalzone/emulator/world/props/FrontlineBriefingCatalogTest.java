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
 * Regression tests for {@link FrontlineBriefingCatalog}, using a fixture built from the real,
 * complete {@code frontlineline.intro.*} namespace (part 169, live-verified on device against the
 * full {@code en.lang} file -- all 12 real lines, verbatim, no privacy concerns since this
 * namespace contains no player-submitted or account-identifying text).
 */
public final class FrontlineBriefingCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "frontlineline.intro.second.stage1.par0=With the support of allied SSPP forces, Murmur forces were pushed back beyond the Railway Embankment.",
            "frontlineline.intro.second.stage1.par1=Colonel Kondratenko's units, with the assistance of strike groups from Zaharov's direction, captured and held the Radar Station bunker.",
            "frontlineline.intro.second.stage1.par2=However, even in retreat, the enemy was not broken. Murmur realized that a quick assault on the Perimeter was impossible and switched to defense, mobilizing reserves and constructing a deeply echeloned defense.",
            "frontlineline.intro.second.stage2.par0=A grueling positional war began, complicated by the loss of the critically important asset for the Special Security Perimeter Protection forces — the psy-suppressor.",
            "frontlineline.intro.second.stage2.par1=While military engineers, together with the ZIVCAS Scientific Research Institute, work to restore the defensive system, the command is developing new operations aimed at finally eliminating the Murmur threat in the south of the CEZ.",
            "frontlineline.intro.second.stage2.par2=Yet, the battle in the Swamps proved – Murmur always has aces up its sleeve. And now, as the stakes have risen for both sides, it's time to lay all cards on the table...",
            "frontlineline.intro.final.stage1.par0=Major Makarov, head of the reconnaissance unit speaking.\\n\\nToday, I want to inform you about the results of our latest operation. We managed to destroy the main forces of the Murmur on the border territories with the Perimeter. The remnants have retreated to the North and, according to intelligence reports, are crossing the anomalous barrier.",
            "frontlineline.intro.final.stage1.par1=This was achieved at the cost of tremendous losses, and I want to express my gratitude to each of you for your bravery and dedication. The enemy threat was eliminated, but unfortunately, that is where the good news ends.",
            "frontlineline.intro.final.stage1.par2=The military town, along with the forward command post and the gravitational beacon, was destroyed. The fate of Colonel Kondratenko, as well as Zaharov and their personnel, is unknown.",
            "frontlineline.intro.final.stage2.par0=Survivors outside the kill zone report that the space warped, folded into a sphere, and simply... disappeared. Now, only a crater remains in its place, and the remnants of urban infrastructure are at risk of collapse.",
            "frontlineline.intro.final.stage2.par1=The SSPP forces express their gratitude to you for your help and support. We have defended the Mainland, and you have defended the Zone. However, we must admit that we understand little about this phenomenon—this space within the boundaries of our planet, its logic, and its rules. Every time command is confident that we have all the means for victory, the Zone shows how misguided we are. And we pay for each such mistake with the lives of our soldiers.",
            "frontlineline.intro.final.stage2.par2=In conclusion, I will say this: we have won the battle—a prolonged and difficult one, but not the war. We are withdrawing troops from the Zone back to the Perimeter. We are returning the Zone to you. End of briefing."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("frontline-briefing-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static FrontlineBriefingCatalog loadFixture() throws IOException {
        return FrontlineBriefingCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real ids cover exactly the 2 real groups x 2 real stages, sorted", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "2 real groups (second, final) x 2 real stages (stage1, stage2)");
            assertEquals(List.of("final.stage1", "final.stage2", "second.stage1", "second.stage2"), ids, "real ids, sorted");
        }));

        out.add(test("real paragraphs resolve in par0/par1/par2 order for a real id", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            List<String> paragraphs = catalog.paragraphs("second.stage1");
            assertEquals(3, paragraphs.size(), "real second.stage1 has exactly 3 real paragraphs");
            assertTrue(paragraphs.get(0).contains("Railway Embankment"), "real par0 text");
            assertTrue(paragraphs.get(1).contains("Colonel Kondratenko"), "real par1 text introduces the real named character");
            assertTrue(paragraphs.get(2).contains("deeply echeloned defense"), "real par2 text");
        }));

        out.add(test("real ZIVCAS Scientific Research Institute reference resolves by direct field access, corroborating part 92's already-confirmed real entity", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            assertEquals(true, catalog.field("second.stage2", "par1").contains("ZIVCAS Scientific Research Institute"), "real cross-namespace corroboration");
        }));

        out.add(test("real named characters (Colonel Kondratenko, Major Makarov, Zaharov) appear in the real final-arc briefing text", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            assertTrue(catalog.paragraph("final.stage1", 0).contains("Major Makarov"), "real Major Makarov introduction");
            assertTrue(catalog.paragraph("final.stage1", 2).contains("Colonel Kondratenko") && catalog.paragraph("final.stage1", 2).contains("Zaharov"), "real fate-unknown line names both real characters");
        }));

        out.add(test("unknown id/field resolve to null or empty, not a guess", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            assertNull(catalog.field("second.stage1", "par9"), "no real par9 in this namespace");
            assertNull(catalog.field("not_a_real_group.stage1", "par0"), "unknown real id resolves to null");
            assertTrue(catalog.paragraphs("not_a_real_group.stage1").isEmpty(), "unknown real id has no real paragraphs");
            assertTrue(catalog.fieldNames("not_a_real_group.stage1").isEmpty(), "unknown real id has no real field names");
        }));

        out.add(test("fieldNames surfaces the real par0/par1/par2 fields for a known id, sorted", () -> {
            FrontlineBriefingCatalog catalog = loadFixture();
            assertEquals(List.of("par0", "par1", "par2"), catalog.fieldNames("final.stage2"), "real field names, sorted");
        }));

        return out;
    }
}
