package net.stalzone.emulator.mobs;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link MobHealthTable}'s JSON loader, using a fixture in the same shape as
 * {@code data/mobs/example_mob_health.json} (real mob ids reused from elsewhere in this project's
 * demo data, invented health values -- see that class's doc: this is emulator-original tuning,
 * not reverse-engineered data, since real {@code .smm} files have no health field at all).
 */
public final class MobHealthTableTest {

    private static final String FIXTURE_JSON = "{"
            + "\"base_boar\": 80.0,"
            + "\"base_tushkan\": 40.0"
            + "}";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("MobHealthTable.load parses the fixture's 2 overrides", () -> {
            MobHealthTable table = loadFixture();
            assertEquals(2, table.size(), "override count");
        }));

        out.add(test("maxHealthFor: a configured real mobId returns its override", () -> {
            MobHealthTable table = loadFixture();
            assertEquals(80.0, table.maxHealthFor("base_boar"), 0.0, "base_boar maxHealth");
            assertEquals(40.0, table.maxHealthFor("base_tushkan"), 0.0, "base_tushkan maxHealth");
        }));

        out.add(test("maxHealthFor: an unconfigured mobId falls back to DEFAULT_MAX_HEALTH, not null/zero", () -> {
            MobHealthTable table = loadFixture();
            assertEquals(MobInstance.DEFAULT_MAX_HEALTH, table.maxHealthFor("does_not_exist"), 0.0, "unconfigured mobId");
        }));

        out.add(test("unconfigured: reports real mob ids missing from the table", () -> {
            MobHealthTable table = loadFixture();
            List<String> missing = table.unconfigured(List.of("base_boar", "base_tushkan", "base_flesh"));
            assertEquals(1, missing.size(), "missing count");
            assertEquals("base_flesh", missing.get(0), "missing mobId");
        }));

        out.add(test("MobHealthTable.load rejects a top-level array where an object was expected", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("mob-health-fixture-bad-shape", ".json");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "[]", StandardCharsets.UTF_8);
                MobHealthTable.load(tmp);
            }, "top-level array instead of object should throw IOException");
        }));

        out.add(test("MobHealthTable.load rejects a non-numeric maxHealth value", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("mob-health-fixture-bad-type", ".json");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "{\"base_boar\": \"not a number\"}", StandardCharsets.UTF_8);
                MobHealthTable.load(tmp);
            }, "non-numeric maxHealth should throw IOException");
        }));

        out.add(test("MobHealthTable.load rejects a zero/negative maxHealth value", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("mob-health-fixture-bad-value", ".json");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "{\"base_boar\": 0.0}", StandardCharsets.UTF_8);
                MobHealthTable.load(tmp);
            }, "maxHealth <= 0 should throw IOException");
        }));

        return out;
    }

    private static MobHealthTable loadFixture() throws Exception {
        Path tmp = Files.createTempFile("mob-health-fixture", ".json");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return MobHealthTable.load(tmp);
    }
}
