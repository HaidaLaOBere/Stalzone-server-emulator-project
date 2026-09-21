package net.stalzone.emulator.progression;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression test for {@link QuestStatus} (part 85) -- pure Java logic, no real files needed.
 * Pins the exact 3 real modeled lifecycle states (of the 4 real notification {@code type} values
 * this project has evidence for -- {@code mail_reward} deliberately excluded, see this enum's
 * class doc).
 */
public final class QuestStatusTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("QuestStatus has exactly the 3 real modeled lifecycle states", () -> {
            QuestStatus[] values = QuestStatus.values();
            assertEquals(3, values.length, "status count");
            assertEquals(QuestStatus.NEW, values[0], "status 0");
            assertEquals(QuestStatus.FINISHED, values[1], "status 1");
            assertEquals(QuestStatus.FAILED, values[2], "status 2");
        }));

        return out;
    }
}
