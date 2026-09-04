package net.stalzone.emulator.survival;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link MinFoodConfig}'s loader, using a fixture matching the real file's
 * exact confirmed byte content ({@code "10"}, no trailing newline -- see part 50 / that class's
 * doc for the real-file evidence this fixture is modeled on).
 */
public final class MinFoodConfigTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("MinFoodConfig.load parses the real file's exact confirmed content (\"10\", no trailing newline)", () -> {
            MinFoodConfig config = loadFixture("10");
            assertEquals(10, config.minFood(), "minFood");
        }));

        out.add(test("MinFoodConfig.load trims surrounding whitespace/newlines (tolerant of a differently-saved copy)", () -> {
            MinFoodConfig config = loadFixture(" 10\r\n");
            assertEquals(10, config.minFood(), "minFood with surrounding whitespace");
        }));

        out.add(test("MinFoodConfig.load rejects non-numeric content", () -> {
            assertThrows(java.io.IOException.class, () -> loadFixture("not_a_number"),
                    "non-numeric content should throw IOException");
        }));

        out.add(test("MinFoodConfig.load rejects empty content", () -> {
            assertThrows(java.io.IOException.class, () -> loadFixture(""),
                    "empty content should throw IOException");
        }));

        out.add(test("MinFoodConfig.toString() includes the parsed value", () -> {
            MinFoodConfig config = loadFixture("10");
            assertTrue(config.toString().contains("10"), "toString should mention the value");
        }));

        return out;
    }

    private static MinFoodConfig loadFixture(String content) throws Exception {
        Path tmp = Files.createTempFile("min-food-fixture", ".txt");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        return MinFoodConfig.load(tmp);
    }
}
