package net.stalzone.emulator.progression;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression test for {@link PlayerClass} (part 83) -- pure Java logic, no real files needed.
 * Pins the exact real, evidenced 6-class roster from part 66 so an accidental future edit (an
 * added/removed/renamed constant) is caught immediately rather than silently drifting from the
 * real evidence this enum is supposed to represent.
 */
public final class PlayerClassTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("PlayerClass has exactly the 6 real evidenced classes from part 66", () -> {
            PlayerClass[] values = PlayerClass.values();
            assertEquals(6, values.length, "class count");
            assertEquals(PlayerClass.HUNTER, values[0], "class 0");
            assertEquals(PlayerClass.SHAMAN, values[1], "class 1");
            assertEquals(PlayerClass.TANK, values[2], "class 2");
            assertEquals(PlayerClass.ASSAULT, values[3], "class 3");
            assertEquals(PlayerClass.SCOUT, values[4], "class 4");
            assertEquals(PlayerClass.SUPPORT, values[5], "class 5");
        }));

        return out;
    }
}
