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
 * Regression tests for WaypointList/Waypoint (part 42), using the EXACT real JSON captured from
 * a real, live install's {@code config/waypoints.cfg_old} (one real death-marker), plus the
 * real empty-array shape the live file itself had at the same moment.
 */
public final class WaypointListTest {

    private static final String REAL_DEATH_MARKER_JSON =
            "[{\"pos\":{\"x\":-5275.422,\"y\":65.75,\"z\":1522.7283},\"name\":\"\",\"icon_index\":0,"
                    + "\"color\":-1,\"can_position_float\":false,\"time\":1788350972745,\"type\":\"death\"}]";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("WaypointList.load parses the real, one-entry death-marker JSON", () -> {
            List<Waypoint> waypoints = loadFixture(REAL_DEATH_MARKER_JSON);
            assertEquals(1, waypoints.size(), "waypoint count");
        }));

        out.add(test("Waypoint fields match the real captured death-marker exactly", () -> {
            Waypoint w = loadFixture(REAL_DEATH_MARKER_JSON).get(0);
            assertEquals(-5275.422, w.x(), 0.001, "x");
            assertEquals(65.75, w.y(), 0.001, "y");
            assertEquals(1522.7283, w.z(), 0.001, "z");
            assertEquals("", w.name(), "name (real value was empty string)");
            assertEquals(0, w.iconIndex(), "iconIndex");
            assertEquals(-1, w.color(), "color (real packed value)");
            assertFalse(w.canPositionFloat(), "canPositionFloat");
            assertEquals(1788350972745L, w.timeMillis(), "timeMillis");
            assertEquals("death", w.type(), "type");
        }));

        out.add(test("WaypointList.load parses the real empty-array shape (the live file had this at the same moment)", () -> {
            List<Waypoint> waypoints = loadFixture("[]");
            assertTrue(waypoints.isEmpty(), "empty waypoints.cfg should parse to an empty list");
        }));

        return out;
    }

    private static List<Waypoint> loadFixture(String json) throws Exception {
        Path tmp = Files.createTempFile("waypoints-fixture", ".cfg");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, json, StandardCharsets.UTF_8);
        return WaypointList.load(tmp);
    }
}
