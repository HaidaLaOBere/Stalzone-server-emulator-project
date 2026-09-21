package net.stalzone.emulator.session;

import net.stalzone.emulator.clientdata.QuestProgress;
import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link QuestLog} (part 85) -- pure Java logic against a small fixture
 * matching {@code config/quests.cfg}'s real shape (real quest ids reused from this project's own
 * fixtures elsewhere, see {@link QuestProgress}'s own tests) plus in-memory {@link
 * #recordEvent}/{@link QuestStatus} calls -- no real client files needed for these specifically.
 */
public final class QuestLogTest {

    private static final String FIXTURE_JSON = "{\"readQuests\":[27091,27621,33388],\"hiddenQuests\":[27091]}";

    private static QuestProgress loadFixture() throws Exception {
        Path path = Files.createTempFile("questlog-test", ".cfg");
        Files.writeString(path, FIXTURE_JSON, StandardCharsets.UTF_8);
        return QuestProgress.load(path);
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a fresh QuestLog knows and hides nothing", () -> {
            QuestLog log = new QuestLog();
            assertTrue(log.knownQuestIds().isEmpty(), "no known quests by default");
            assertTrue(log.hiddenQuestIds().isEmpty(), "no hidden quests by default");
            assertNull(log.titleOf(27091), "no title known by default");
            assertNull(log.statusOf(27091), "no status known by default");
        }));

        out.add(test("seedFrom(QuestProgress) populates known/hidden ids from the real quests.cfg shape, no titles", () -> {
            QuestLog log = new QuestLog();
            log.seedFrom(loadFixture());
            assertTrue(log.isKnown(27091), "27091 is in the fixture's readQuests");
            assertTrue(log.isKnown(27621), "27621 is in the fixture's readQuests");
            assertTrue(log.isHidden(27091), "27091 is in the fixture's hiddenQuests");
            assertTrue(!log.isHidden(27621), "27621 is not in the fixture's hiddenQuests");
            assertTrue(!log.isKnown(99999), "an unrelated id is not known");
            assertNull(log.titleOf(27091), "quests.cfg alone never supplies a title");
        }));

        out.add(test("recordEvent marks the id known and records title/status", () -> {
            QuestLog log = new QuestLog();
            log.recordEvent(27091, "For the Good of the Mercenaries", QuestStatus.NEW);
            assertTrue(log.isKnown(27091), "recordEvent marks the id known");
            assertEquals("For the Good of the Mercenaries", log.titleOf(27091), "recorded title");
            assertEquals(QuestStatus.NEW, log.statusOf(27091), "recorded status");
        }));

        out.add(test("recordEvent with a null title leaves a previously-recorded title untouched", () -> {
            QuestLog log = new QuestLog();
            log.recordEvent(27091, "An Important Delivery", QuestStatus.NEW);
            log.recordEvent(27091, null, QuestStatus.FINISHED);
            assertEquals("An Important Delivery", log.titleOf(27091), "title preserved across a null-title event");
            assertEquals(QuestStatus.FINISHED, log.statusOf(27091), "status still updates");
        }));

        out.add(test("recordEvent overwrites status on repeated real transitions (new -> failed -> new -> finished)", () -> {
            QuestLog log = new QuestLog();
            log.recordEvent(27091, "For the Good of the Mercenaries", QuestStatus.NEW);
            log.recordEvent(27091, null, QuestStatus.FAILED);
            log.recordEvent(27091, null, QuestStatus.NEW);
            log.recordEvent(27091, null, QuestStatus.FINISHED);
            assertEquals(QuestStatus.FINISHED, log.statusOf(27091), "final status after the real cycle");
        }));

        out.add(test("markKnown (part 87 restore helper) marks known without touching title/status/hidden", () -> {
            QuestLog log = new QuestLog();
            log.markKnown(27621);
            assertTrue(log.isKnown(27621), "markKnown marks known");
            assertTrue(!log.isHidden(27621), "markKnown does not mark hidden");
            assertNull(log.titleOf(27621), "markKnown does not set a title");
            assertNull(log.statusOf(27621), "markKnown does not set a status");
        }));

        out.add(test("markHidden (part 87 restore helper) is independent of known, same as seedFrom treats the real sets", () -> {
            QuestLog log = new QuestLog();
            log.markHidden(27091);
            assertTrue(log.isHidden(27091), "markHidden marks hidden");
            assertTrue(!log.isKnown(27091), "markHidden alone does not imply known");
        }));

        return out;
    }
}
