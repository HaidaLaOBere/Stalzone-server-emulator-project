package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for QuestProgress (part 42), using a trimmed real snippet of an actual
 * {@code config/quests.cfg} from a real, live install -- real quest ids, including the real
 * {@code hiddenQuests} entry (27091) that also appears in {@code readQuests}, exactly as
 * observed (see part 40's cross-validation against a real {@code sd2} save file).
 */
public final class QuestProgressTest {

    private static final String FIXTURE_JSON =
            "{\"readQuests\":[26018,27621,27091,33389],\"hiddenQuests\":[27091]}";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("QuestProgress.load parses real-shaped readQuests/hiddenQuests arrays", () -> {
            QuestProgress p = loadFixture();
            assertEquals(4, p.readQuestIds().size(), "readQuests count");
            assertEquals(1, p.hiddenQuestIds().size(), "hiddenQuests count");
        }));

        out.add(test("isRead/isHidden: real quest 27091 is BOTH read and hidden, as observed on a real install", () -> {
            QuestProgress p = loadFixture();
            assertTrue(p.isRead(27091), "27091 should be read");
            assertTrue(p.isHidden(27091), "27091 should be hidden");
        }));

        out.add(test("isRead/isHidden: a quest id present in neither list is neither", () -> {
            QuestProgress p = loadFixture();
            assertFalse(p.isRead(999999), "unknown id should not be read");
            assertFalse(p.isHidden(999999), "unknown id should not be hidden");
        }));

        out.add(test("isRead: a quest id in readQuests but not hiddenQuests (26018) is read, not hidden", () -> {
            QuestProgress p = loadFixture();
            assertTrue(p.isRead(26018), "26018 should be read");
            assertFalse(p.isHidden(26018), "26018 should not be hidden");
        }));

        return out;
    }

    private static QuestProgress loadFixture() throws Exception {
        Path tmp = Files.createTempFile("quests-fixture", ".cfg");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return QuestProgress.load(tmp);
    }
}
