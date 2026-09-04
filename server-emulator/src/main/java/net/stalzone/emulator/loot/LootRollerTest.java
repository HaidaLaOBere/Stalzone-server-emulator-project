package net.stalzone.emulator.loot;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link LootRoller} -- the class that finally wires
 * {@link ServerConfig#lootRateMultiplier} into real behavior (part 48). These test the emulator's
 * OWN roll semantics (documented in {@link LootRoller}'s class doc), not reverse-engineered data
 * -- see {@link LootTable}'s class doc for why no such real data exists to test against.
 */
public final class LootRollerTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("roll: a null table returns an empty list, not a crash", () -> {
            LootRoller roller = new LootRoller(new ServerConfig(), new Random(1));
            assertTrue(roller.roll(null).isEmpty(), "null table");
        }));

        out.add(test("roll: a table with no entries returns an empty list", () -> {
            LootRoller roller = new LootRoller(new ServerConfig(), new Random(1));
            LootTable empty = new LootTable(1.0, List.of());
            assertTrue(roller.roll(empty).isEmpty(), "empty entries");
        }));

        out.add(test("roll: dropChance=0.0 never drops, across many rolls with different seeds", () -> {
            LootTable table = new LootTable(0.0, List.of(new LootEntry("x", 1, 1, 1.0)));
            for (int seed = 0; seed < 20; seed++) {
                LootRoller roller = new LootRoller(new ServerConfig(), new Random(seed));
                assertTrue(roller.roll(table).isEmpty(), "dropChance=0.0 should never drop, seed=" + seed);
            }
        }));

        out.add(test("roll: dropChance=1.0 with lootRateMultiplier=1.0 always drops, across many rolls with different seeds", () -> {
            LootTable table = new LootTable(1.0, List.of(new LootEntry("x", 5, 5, 1.0)));
            for (int seed = 0; seed < 20; seed++) {
                ServerConfig config = new ServerConfig();
                LootRoller roller = new LootRoller(config, new Random(seed));
                List<LootDrop> drops = roller.roll(table);
                assertEquals(1, drops.size(), "dropChance=1.0 should always drop exactly one entry, seed=" + seed);
                assertEquals("x", drops.get(0).itemId(), "dropped itemId");
                assertEquals(5, drops.get(0).count(), "fixed-count entry should always roll exactly 5");
            }
        }));

        out.add(test("roll: lootRateMultiplier=0.0 suppresses drops even when dropChance=1.0", () -> {
            LootTable table = new LootTable(1.0, List.of(new LootEntry("x", 1, 1, 1.0)));
            ServerConfig config = new ServerConfig();
            config.lootRateMultiplier = 0.0;
            LootRoller roller = new LootRoller(config, new Random(7));
            assertTrue(roller.roll(table).isEmpty(), "lootRateMultiplier=0.0 should suppress all drops");
        }));

        out.add(test("roll: lootRateMultiplier clamps the effective chance at 1.0 rather than over-rolling", () -> {
            // dropChance=0.5 * lootRateMultiplier=3.0 would be 1.5 unclamped -- effectiveChance
            // must clamp to 1.0, i.e. this should behave identically to the dropChance=1.0 test above.
            LootTable table = new LootTable(0.5, List.of(new LootEntry("x", 1, 1, 1.0)));
            ServerConfig config = new ServerConfig();
            config.lootRateMultiplier = 3.0;
            for (int seed = 0; seed < 20; seed++) {
                LootRoller roller = new LootRoller(config, new Random(seed));
                assertEquals(1, roller.roll(table).size(), "clamped chance should always drop, seed=" + seed);
            }
        }));

        out.add(test("roll: count is always within [minCount, maxCount], across many rolls", () -> {
            LootTable table = new LootTable(1.0, List.of(new LootEntry("x", 2, 6, 1.0)));
            for (int seed = 0; seed < 50; seed++) {
                LootRoller roller = new LootRoller(new ServerConfig(), new Random(seed));
                int count = roller.roll(table).get(0).count();
                assertTrue(count >= 2 && count <= 6, "count " + count + " out of range [2,6], seed=" + seed);
            }
        }));

        out.add(test("roll: with multiple entries, every configured itemId is reachable across many rolls", () -> {
            LootTable table = new LootTable(1.0, List.of(
                    new LootEntry("a", 1, 1, 1.0),
                    new LootEntry("b", 1, 1, 1.0)));
            java.util.Set<String> seen = new java.util.HashSet<>();
            LootRoller roller = new LootRoller(new ServerConfig(), new Random(42));
            for (int i = 0; i < 200; i++) {
                seen.add(roller.roll(table).get(0).itemId());
            }
            assertTrue(seen.contains("a"), "item \"a\" should be reachable across 200 rolls");
            assertTrue(seen.contains("b"), "item \"b\" should be reachable across 200 rolls");
        }));

        return out;
    }
}
