package net.stalzone.emulator.mobs;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for MobInstance's disclosed placeholder health pool (part 30) -- pure Java
 * logic, no real files needed. {@link MobConfig}'s package-private constructor is used with a
 * minimal synthetic field map since this test only exercises health-tracking, not real .smm data.
 */
public final class MobInstanceTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("new MobInstance starts at full health, not dead", () -> {
            MobInstance m = fresh(100.0);
            assertEquals(100.0, m.health(), 1e-9, "starting health");
            assertEquals(100.0, m.maxHealth(), 1e-9, "maxHealth");
            assertFalse(m.isDead(), "should not start dead");
        }));

        out.add(test("applyDamage subtracts and returns the amount actually applied", () -> {
            MobInstance m = fresh(100.0);
            double applied = m.applyDamage(30.0);
            assertEquals(30.0, applied, 1e-9, "applied amount");
            assertEquals(70.0, m.health(), 1e-9, "remaining health");
            assertFalse(m.isDead(), "70/100 should not be dead");
        }));

        out.add(test("applyDamage clamps at zero and reports the CLAMPED amount, not the requested amount", () -> {
            MobInstance m = fresh(100.0);
            m.applyDamage(90.0);
            double applied = m.applyDamage(50.0); // only 10 hp actually left
            assertEquals(10.0, applied, 1e-9, "clamped applied amount on overkill");
            assertEquals(0.0, m.health(), 1e-9, "health should floor at zero");
            assertTrue(m.isDead(), "0 hp should be dead");
        }));

        out.add(test("applyDamage with zero or negative amount is a no-op", () -> {
            MobInstance m = fresh(100.0);
            assertEquals(0.0, m.applyDamage(0.0), 1e-9, "zero damage applied");
            assertEquals(0.0, m.applyDamage(-5.0), 1e-9, "negative damage applied");
            assertEquals(100.0, m.health(), 1e-9, "health unchanged");
        }));

        out.add(test("applyDamage on an already-dead mob applies nothing further", () -> {
            MobInstance m = fresh(50.0);
            m.applyDamage(50.0);
            assertTrue(m.isDead(), "should be dead after exact-lethal hit");
            double applied = m.applyDamage(10.0);
            assertEquals(0.0, applied, 1e-9, "no further damage should apply once dead");
        }));

        out.add(test("DEFAULT_MAX_HEALTH convenience constructor matches the documented placeholder value", () -> {
            MobConfig cfg = new MobConfig("base_test", new LinkedHashMap<>());
            MobInstance m = new MobInstance(cfg);
            assertEquals(MobInstance.DEFAULT_MAX_HEALTH, m.maxHealth(), 1e-9, "default max health");
        }));

        return out;
    }

    private static MobInstance fresh(double maxHealth) {
        MobConfig cfg = new MobConfig("base_test", new LinkedHashMap<>());
        return new MobInstance(cfg, maxHealth);
    }
}
