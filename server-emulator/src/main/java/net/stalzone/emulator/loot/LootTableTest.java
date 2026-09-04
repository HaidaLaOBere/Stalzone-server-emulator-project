package net.stalzone.emulator.loot;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link LootEntry}/{@link LootTable} construction and validation (part 48).
 * This whole package is emulator-original design (see {@link LootTable}'s class doc), so unlike
 * most of this project's tests, these fixtures are NOT real captured data -- they're just
 * structurally-valid example values exercising the validation and accessor logic.
 */
public final class LootTableTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("LootEntry accessors round-trip constructor arguments", () -> {
            LootEntry e = new LootEntry("some_item", 2, 5, 3.0);
            assertEquals("some_item", e.itemId(), "itemId");
            assertEquals(2, e.minCount(), "minCount");
            assertEquals(5, e.maxCount(), "maxCount");
            assertEquals(3.0, e.weight(), 0.0, "weight");
        }));

        out.add(test("LootEntry rejects an empty itemId", () -> {
            assertThrows(IllegalArgumentException.class, () -> new LootEntry("", 1, 1, 1.0),
                    "empty itemId should throw");
        }));

        out.add(test("LootEntry rejects an inverted count range (maxCount < minCount)", () -> {
            assertThrows(IllegalArgumentException.class, () -> new LootEntry("x", 5, 1, 1.0),
                    "maxCount < minCount should throw");
        }));

        out.add(test("LootEntry rejects a non-positive weight", () -> {
            assertThrows(IllegalArgumentException.class, () -> new LootEntry("x", 1, 1, 0.0),
                    "zero weight should throw");
        }));

        out.add(test("LootTable accessors round-trip constructor arguments", () -> {
            LootEntry e1 = new LootEntry("a", 1, 1, 1.0);
            LootEntry e2 = new LootEntry("b", 1, 2, 2.0);
            LootTable t = new LootTable(0.5, List.of(e1, e2));
            assertEquals(0.5, t.dropChance(), 0.0, "dropChance");
            assertEquals(2, t.entries().size(), "entries size");
        }));

        out.add(test("LootTable rejects a negative dropChance", () -> {
            assertThrows(IllegalArgumentException.class, () -> new LootTable(-0.1, List.of()),
                    "negative dropChance should throw");
        }));

        out.add(test("LootTable.entries() returns an independent, unmodifiable view", () -> {
            LootEntry e1 = new LootEntry("a", 1, 1, 1.0);
            LootTable t = new LootTable(0.5, List.of(e1));
            assertThrows(UnsupportedOperationException.class, () -> t.entries().add(e1),
                    "entries() should not be mutable");
        }));

        return out;
    }
}
