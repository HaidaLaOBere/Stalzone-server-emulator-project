package net.stalzone.emulator.progression;

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
 * Regression test for {@link PrestigeAbility} (part 83; {@link PrestigeAbility#langKey}/{@link
 * PrestigeAbility#realDisplayName}/{@link PrestigeAbility#realDescription} added part 90) -- pure
 * Java logic, no real files needed beyond a small real-content {@link LangCatalog} fixture. Pins
 * the exact real, evidenced 11-ability roster from part 66 (9 from {@code prestige_abil/}, 1 from
 * {@code prestige_tranq/}, 1 from {@code prestige_med/}) so an accidental future edit is caught
 * immediately rather than silently drifting from the real evidence.
 */
public final class PrestigeAbilityTest {

    private static LangCatalog fixtureCatalog() throws IOException {
        Path file = Files.createTempFile("prestige-ability-lang-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, String.join("\n",
                "prestige.ability.tank_barricade.name=Barricade",
                "prestige.ability.tank_barricade.description=Deploy a defensive barricade that provides cover.",
                "prestige.ability.scout_mine.name=Mine"
        ) + "\n");
        return LangCatalog.load(file);
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("PrestigeAbility has exactly the 11 real evidenced abilities from part 66", () -> {
            PrestigeAbility[] values = PrestigeAbility.values();
            assertEquals(11, values.length, "ability count");
            assertEquals(PrestigeAbility.BARRICADE, values[0], "ability 0");
            assertEquals(PrestigeAbility.LOW_GRAVITY, values[1], "ability 1");
            assertEquals(PrestigeAbility.PERSONAL_TELEPORT, values[2], "ability 2");
            assertEquals(PrestigeAbility.PHASE_SHIFT, values[3], "ability 3");
            assertEquals(PrestigeAbility.PORTAL_GRENADE, values[4], "ability 4");
            assertEquals(PrestigeAbility.PUSH_GRENADE, values[5], "ability 5");
            assertEquals(PrestigeAbility.SCANNER, values[6], "ability 6");
            assertEquals(PrestigeAbility.SCOUT_MINE, values[7], "ability 7");
            assertEquals(PrestigeAbility.VORTEX_GRENADE, values[8], "ability 8");
            assertEquals(PrestigeAbility.TRANQUILIZER, values[9], "ability 9");
            assertEquals(PrestigeAbility.PRESTIGE_MED, values[10], "ability 10");
        }));

        out.add(test("langKey: exactly 8 of 11 abilities have a confirmed crosswalk", () -> {
            int confirmed = 0;
            for (PrestigeAbility ability : PrestigeAbility.values()) {
                if (ability.langKey() != null) {
                    confirmed++;
                }
            }
            assertEquals(8, confirmed, "confirmed langKey count");
        }));

        out.add(test("langKey: PHASE_SHIFT, VORTEX_GRENADE, PRESTIGE_MED are deliberately unconfirmed (null)", () -> {
            assertEquals(null, PrestigeAbility.PHASE_SHIFT.langKey(), "PHASE_SHIFT");
            assertEquals(null, PrestigeAbility.VORTEX_GRENADE.langKey(), "VORTEX_GRENADE");
            assertEquals(null, PrestigeAbility.PRESTIGE_MED.langKey(), "PRESTIGE_MED");
        }));

        out.add(test("realDisplayName: BARRICADE resolves its real confirmed lang entry", () -> {
            LangCatalog catalog = fixtureCatalog();
            assertEquals("Barricade", PrestigeAbility.BARRICADE.realDisplayName(catalog), "BARRICADE display name");
        }));

        out.add(test("realDescription: BARRICADE resolves its real confirmed lang entry", () -> {
            LangCatalog catalog = fixtureCatalog();
            assertEquals("Deploy a defensive barricade that provides cover.",
                    PrestigeAbility.BARRICADE.realDescription(catalog), "BARRICADE description");
        }));

        out.add(test("realDisplayName: an unconfirmed ability returns null without touching the catalog", () -> {
            LangCatalog catalog = fixtureCatalog();
            assertEquals(null, PrestigeAbility.PHASE_SHIFT.realDisplayName(catalog), "PHASE_SHIFT has no crosswalk");
        }));

        out.add(test("realDisplayName: a confirmed ability absent from a smaller catalog returns null, not throw", () -> {
            LangCatalog catalog = fixtureCatalog(); // has no scout_stun (TRANQUILIZER) entry
            assertEquals(null, PrestigeAbility.TRANQUILIZER.realDisplayName(catalog), "TRANQUILIZER missing from fixture");
        }));

        return out;
    }
}
