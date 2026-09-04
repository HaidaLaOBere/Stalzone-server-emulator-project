package net.stalzone.emulator.economy;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link TraderItemGroups}, using a fixture matching the real file's exact
 * confirmed 10 category names and CRLF shape (part 51).
 */
public final class TraderItemGroupsTest {

    private static final String FIXTURE =
            "barter\r\ncases\r\ntoys\r\nweapons\r\narmor\r\nskins\r\nbackpacks\r\nweapon_modules\r\nsignal_stuff\r\nguitar";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("TraderItemGroups.load parses the real file's exact 10 confirmed group names, in order", () -> {
            TraderItemGroups groups = loadFixture();
            assertEquals(10, groups.size(), "group count");
            assertEquals("barter", groups.groupNames().get(0), "first group");
            assertEquals("guitar", groups.groupNames().get(9), "last group (no trailing newline)");
        }));

        out.add(test("contains: true for a real configured group name", () -> {
            TraderItemGroups groups = loadFixture();
            assertTrue(groups.contains("weapon_modules"), "weapon_modules should be present");
        }));

        out.add(test("contains: false for an unconfigured name", () -> {
            TraderItemGroups groups = loadFixture();
            assertFalse(groups.contains("does_not_exist"), "unconfigured name should be absent");
        }));

        out.add(test("TraderItemGroups.load rejects an empty file", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("trader-groups-fixture-empty", ".txt");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "", StandardCharsets.UTF_8);
                TraderItemGroups.load(tmp);
            }, "empty file should throw IOException");
        }));

        return out;
    }

    private static TraderItemGroups loadFixture() throws Exception {
        Path tmp = Files.createTempFile("trader-groups-fixture", ".txt");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE, StandardCharsets.UTF_8);
        return TraderItemGroups.load(tmp);
    }
}
