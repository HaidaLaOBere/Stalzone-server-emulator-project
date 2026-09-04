package net.stalzone.emulator.loot;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link LootTables}' JSON loader (part 48), using a fixture in the same
 * shape as {@code data/loot/example_loot_tables.json} (real mob/item ids reused from elsewhere in
 * this project's demo data, invented drop-chance/weight/count values -- see {@link LootTable}'s
 * class doc: this whole package is emulator-original config, not reverse-engineered data).
 */
public final class LootTablesTest {

    private static final String FIXTURE_JSON = "["
            + "{\"mobId\":\"base_boar\",\"dropChance\":0.5,\"entries\":["
            + "{\"itemId\":\"16340\",\"minCount\":1,\"maxCount\":2,\"weight\":3.0},"
            + "{\"itemId\":\"16347\",\"minCount\":1,\"maxCount\":1,\"weight\":1.0}"
            + "]},"
            + "{\"mobId\":\"base_tushkan\",\"dropChance\":0.25,\"entries\":["
            + "{\"itemId\":\"16340\",\"minCount\":1,\"maxCount\":1,\"weight\":1.0}"
            + "]}"
            + "]";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("LootTables.load parses the fixture's 2 mob tables", () -> {
            LootTables tables = loadFixture();
            assertEquals(2, tables.size(), "table count");
        }));

        out.add(test("forMob: a configured real mobId returns its table with the right shape", () -> {
            LootTables tables = loadFixture();
            LootTable boar = tables.forMob("base_boar");
            assertNotNull(boar, "base_boar table");
            assertEquals(0.5, boar.dropChance(), 0.0, "base_boar dropChance");
            assertEquals(2, boar.entries().size(), "base_boar entries count");
        }));

        out.add(test("forMob: an unconfigured mobId returns null, not an empty table", () -> {
            LootTables tables = loadFixture();
            assertNull(tables.forMob("does_not_exist"), "unconfigured mobId");
        }));

        out.add(test("LootTables.load: entry fields (itemId/minCount/maxCount/weight) round-trip exactly", () -> {
            LootTables tables = loadFixture();
            LootEntry first = tables.forMob("base_boar").entries().get(0);
            assertEquals("16340", first.itemId(), "itemId");
            assertEquals(1, first.minCount(), "minCount");
            assertEquals(2, first.maxCount(), "maxCount");
            assertEquals(3.0, first.weight(), 0.0, "weight");
        }));

        out.add(test("LootTables.load rejects a top-level object where an array was expected", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("loot-fixture-bad", ".json");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "{}", StandardCharsets.UTF_8);
                LootTables.load(tmp);
            }, "top-level object instead of array should throw IOException");
        }));

        return out;
    }

    private static LootTables loadFixture() throws Exception {
        Path tmp = Files.createTempFile("loot-fixture", ".json");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return LootTables.load(tmp);
    }
}
