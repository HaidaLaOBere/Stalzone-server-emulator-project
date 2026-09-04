package net.stalzone.emulator.items;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ItemTiers}, using a fixture matching the real file's exact
 * confirmed content (part 51).
 */
public final class ItemTiersTest {

    private static final String FIXTURE = "novice\r\nstalker\r\nveteran\r\nmaster";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ItemTiers.load parses the real file's exact 4 confirmed tier names, in order", () -> {
            ItemTiers tiers = loadFixture();
            assertEquals(4, tiers.size(), "tier count");
            assertEquals(List.of("novice", "stalker", "veteran", "master"), tiers.tiers(), "tier names/order");
        }));

        out.add(test("ordinalOf: returns each real tier's 0-based file-order position", () -> {
            ItemTiers tiers = loadFixture();
            assertEquals(0, tiers.ordinalOf("novice"), "novice ordinal");
            assertEquals(3, tiers.ordinalOf("master"), "master ordinal");
        }));

        out.add(test("ordinalOf: returns -1 for an unknown tier name", () -> {
            ItemTiers tiers = loadFixture();
            assertEquals(-1, tiers.ordinalOf("legendary"), "unknown tier ordinal");
        }));

        out.add(test("ItemTiers.load rejects an empty file", () -> {
            assertThrows(java.io.IOException.class, () -> {
                Path tmp = Files.createTempFile("item-tiers-fixture-empty", ".txt");
                tmp.toFile().deleteOnExit();
                Files.writeString(tmp, "", StandardCharsets.UTF_8);
                ItemTiers.load(tmp);
            }, "empty file should throw IOException");
        }));

        return out;
    }

    private static ItemTiers loadFixture() throws Exception {
        Path tmp = Files.createTempFile("item-tiers-fixture", ".txt");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE, StandardCharsets.UTF_8);
        return ItemTiers.load(tmp);
    }
}
