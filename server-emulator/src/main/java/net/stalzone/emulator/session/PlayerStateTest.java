package net.stalzone.emulator.session;

import net.stalzone.emulator.progression.PlayerClass;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link PlayerState}'s part-83 fields ({@link PlayerState#playerClass},
 * {@link PlayerState#unlockedAbilities}) -- pure Java logic, no real files needed. {@code
 * PlayerState} itself has no broader dedicated test file (same standing as {@link Inventory}):
 * its position/zone behavior is exercised end-to-end via {@code GameLoopDemo} against real
 * staged files. These two fields are simple enough in-memory state to cover directly here.
 */
public final class PlayerStateTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a fresh PlayerState has no class selected and no abilities unlocked", () -> {
            PlayerState player = new PlayerState("demo", "bar", 0, 0, 0);
            assertNull(player.playerClass, "no class selected by default -- no evidence one is required");
            assertTrue(player.unlockedAbilities.isEmpty(), "no abilities unlocked by default");
        }));

        out.add(test("playerClass can be assigned any real evidenced class", () -> {
            PlayerState player = new PlayerState("demo", "bar", 0, 0, 0);
            player.playerClass = PlayerClass.SHAMAN;
            assertEquals(PlayerClass.SHAMAN, player.playerClass, "assigned class");
        }));

        out.add(test("unlockedAbilities accepts real named abilities and reports containment", () -> {
            PlayerState player = new PlayerState("demo", "bar", 0, 0, 0);
            player.unlockedAbilities.add(PrestigeAbility.VORTEX_GRENADE);
            assertTrue(player.unlockedAbilities.contains(PrestigeAbility.VORTEX_GRENADE), "just-added ability present");
            assertFalse(player.unlockedAbilities.contains(PrestigeAbility.PHASE_SHIFT), "un-added ability absent");
        }));

        return out;
    }
}
