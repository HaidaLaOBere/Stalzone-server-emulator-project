package net.stalzone.emulator.items;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for the real damage-falloff math wired into combat in part 41. Uses the
 * EXACT real {@code weapons.json} field values for two real records already cited in this
 * project's docs -- AK-12 (id {@code g4mdp}) and the Gauss Rifle (id {@code knmdv}, the real
 * zero-width-ramp edge case) -- written to a small temp JSON file rather than requiring the full
 * 337-record real dataset on disk, so this test is self-contained and fast. The AK-12 numbers at
 * 5m/25m below are the SAME numbers an actual {@code GameLoopDemo} run against the real dataset
 * produced (see docs/RESEARCH_LOG.md part 41) -- this test exists so that result stays true after
 * future changes, not just on the day it was manually observed.
 */
public final class WeaponRegistryTest {

    private static final String FIXTURE_JSON = "["
            + "{\"id\":\"g4mdp\",\"name\":\"AK-12\",\"category\":\"weapon/assault_rifle\",\"damage\":37.5,"
            + "\"headshotMultiplier\":\"1.4\",\"limbMultiplier\":\"0.8\","
            + "\"damageFalloff\":{\"startDamage\":37.5,\"damageDecreaseStart\":18,\"endDamage\":29.9,"
            + "\"damageDecreaseEnd\":72,\"maxDistance\":160}},"
            + "{\"id\":\"knmdv\",\"name\":\"Gauss Rifle\",\"category\":\"weapon/sniper_rifle\",\"damage\":247,"
            + "\"damageFalloff\":{\"startDamage\":247,\"damageDecreaseStart\":200,\"endDamage\":247,"
            + "\"damageDecreaseEnd\":200,\"maxDistance\":250}},"
            + "{\"id\":\"zzz01\",\"name\":\"Fake Knife\",\"category\":\"weapon/melee\","
            + "\"meleeDamageMinCommon\":10.0,\"meleeDamageMaxCommon\":20.0}"
            + "]";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("WeaponRegistry.load parses the fixture's 3 real-shaped records", () -> {
            WeaponRegistry reg = loadFixture();
            assertEquals(3, reg.size(), "fixture record count");
        }));

        out.add(test("effectiveDamage: flat AK-12 damage at 1.0x multiplier matches real startDamage", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertEquals(37.5, reg.effectiveDamage(ak12, config(1.0)), 1e-9, "AK-12 flat damage");
        }));

        out.add(test("effectiveDamageAtDistance: AK-12 inside decreaseStart is flat startDamage", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertEquals(37.5, reg.effectiveDamageAtDistance(ak12, 5.0, config(1.0)), 1e-9, "AK-12 @ 5m");
            assertEquals(37.5, reg.effectiveDamageAtDistance(ak12, 18.0, config(1.0)), 1e-9, "AK-12 @ exactly decreaseStart=18m");
        }));

        out.add(test("effectiveDamageAtDistance: AK-12 at 25m matches the real linear-ramp value seen in part 41's live run", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            // t = (25-18)/(72-18) = 0.129629...; 37.5 + t*(29.9-37.5) = 36.5 (confirmed by hand and by a real GameLoopDemo run, part 41)
            assertEquals(36.5, reg.effectiveDamageAtDistance(ak12, 25.0, config(1.0)), 0.05, "AK-12 @ 25m (ramp)");
        }));

        out.add(test("effectiveDamageAtDistance: AK-12 at/after decreaseEnd is flat endDamage", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertEquals(29.9, reg.effectiveDamageAtDistance(ak12, 72.0, config(1.0)), 1e-9, "AK-12 @ exactly decreaseEnd=72m");
            assertEquals(29.9, reg.effectiveDamageAtDistance(ak12, 159.9, config(1.0)), 1e-9, "AK-12 @ just under maxDistance");
        }));

        out.add(test("effectiveDamageAtDistance: AK-12 at/beyond maxDistance deals zero, per EXBO's official docs (part 39)", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertEquals(0.0, reg.effectiveDamageAtDistance(ak12, 160.0, config(1.0)), 1e-9, "AK-12 @ exactly maxDistance");
            assertEquals(0.0, reg.effectiveDamageAtDistance(ak12, 500.0, config(1.0)), 1e-9, "AK-12 far beyond maxDistance");
        }));

        out.add(test("effectiveHeadshotDamage: AK-12 real 1.4x multiplier stacks on top of flat damage", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertEquals(52.5, reg.effectiveHeadshotDamage(ak12, config(1.0)), 1e-9, "AK-12 headshot");
        }));

        out.add(test("damageAtDistanceLinearAssumption: knmdv's real zero-width ramp is flat across its whole range, no div-by-zero", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry knmdv = reg.byId("knmdv");
            assertEquals(247.0, knmdv.damageAtDistanceLinearAssumption(0.0), 1e-9, "knmdv @ 0m");
            assertEquals(247.0, knmdv.damageAtDistanceLinearAssumption(100.0), 1e-9, "knmdv @ 100m (before the zero-width ramp point)");
            assertEquals(247.0, knmdv.damageAtDistanceLinearAssumption(200.0), 1e-9, "knmdv @ exactly 200m (decreaseStart==decreaseEnd)");
            assertEquals(0.0, knmdv.damageAtDistanceLinearAssumption(250.0), 1e-9, "knmdv @ maxDistance");
        }));

        out.add(test("damageAtDistanceLinearAssumption: negative distance is rejected, not silently clamped", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry ak12 = reg.byId("g4mdp");
            assertThrows(IllegalArgumentException.class, () -> ak12.damageAtDistanceLinearAssumption(-1.0),
                    "negative distance should throw");
        }));

        out.add(test("damageFalloff/damageAtDistanceLinearAssumption: melee record with no falloff curve returns null, not a guess", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry knife = reg.byId("zzz01");
            assertNull(knife.damageFalloff(), "melee record's damageFalloff()");
            assertNull(knife.damageAtDistanceLinearAssumption(50.0), "melee record's damageAtDistanceLinearAssumption()");
            assertNull(reg.effectiveDamageAtDistance(knife, 50.0, config(1.0)), "WeaponRegistry.effectiveDamageAtDistance for melee");
        }));

        out.add(test("effectiveMeleeCommonDamage: real light-attack range with a 2.0x server multiplier applied", () -> {
            WeaponRegistry reg = loadFixture();
            WeaponEntry knife = reg.byId("zzz01");
            double[] range = reg.effectiveMeleeCommonDamage(knife, config(2.0));
            assertEquals(20.0, range[0], 1e-9, "melee min * 2.0");
            assertEquals(40.0, range[1], 1e-9, "melee max * 2.0");
        }));

        return out;
    }

    private static ServerConfig config(double damageMultiplier) {
        ServerConfig c = new ServerConfig();
        c.damageMultiplier = damageMultiplier;
        return c;
    }

    private static WeaponRegistry loadFixture() throws Exception {
        Path tmp = Files.createTempFile("weapon-fixture", ".json");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return WeaponRegistry.load(tmp);
    }
}
