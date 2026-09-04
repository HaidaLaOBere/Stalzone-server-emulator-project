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
 * Regression tests for ClientOptions (part 44), using a trimmed real snippet of an actual
 * {@code config/options.json} from a real, live install -- real key names and real values,
 * including the 4 real keys confirmed (see ClientOptions class doc) to hold either a raw
 * IEEE-754 float32 bit pattern ({@code admin_hotbar_scale}, {@code admin_inv_icon_scale},
 * {@code world_outliner_width}) or a packed ARGB color ({@code world_outliner_color}).
 */
public final class ClientOptionsTest {

    private static final String FIXTURE_JSON = "{"
            + "\"show_driver_notification\":true,"
            + "\"shown_disclaimer\":false,"
            + "\"language\":\"en\","
            + "\"mouse_sensitivity\":15.0,"
            + "\"fov\":90.0,"
            + "\"admin_hotbar_scale\":1075838976,"
            + "\"admin_inv_icon_scale\":1065353216,"
            + "\"world_outliner_width\":1073741824,"
            + "\"world_outliner_color\":1711276032"
            + "}";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ClientOptions.load parses the fixture's 9 real-named keys", () -> {
            ClientOptions o = loadFixture();
            assertEquals(9, o.size(), "key count");
        }));

        out.add(test("getBoolean: real key show_driver_notification is true", () -> {
            ClientOptions o = loadFixture();
            assertTrue(o.getBoolean("show_driver_notification", false), "show_driver_notification");
            assertFalse(o.getBoolean("shown_disclaimer", true), "shown_disclaimer");
        }));

        out.add(test("getBoolean: missing key returns the given default, not a crash", () -> {
            ClientOptions o = loadFixture();
            assertTrue(o.getBoolean("does_not_exist", true), "missing key default");
        }));

        out.add(test("getString: real key language is \"en\"", () -> {
            ClientOptions o = loadFixture();
            assertEquals("en", o.getString("language", null), "language");
        }));

        out.add(test("getDouble/getInt: real sensitivity and fov values round-trip", () -> {
            ClientOptions o = loadFixture();
            assertEquals(15.0, o.getDouble("mouse_sensitivity", -1), "mouse_sensitivity");
            assertEquals(90, o.getInt("fov", -1), "fov as int");
        }));

        out.add(test("getFloatBits: real admin_inv_icon_scale (0x3f800000) decodes to exactly 1.0f", () -> {
            ClientOptions o = loadFixture();
            assertEquals(1.0f, o.getFloatBits("admin_inv_icon_scale"), 0.0f, "admin_inv_icon_scale as float bits");
        }));

        out.add(test("getFloatBits: real admin_hotbar_scale (0x40200000) decodes to exactly 2.5f", () -> {
            ClientOptions o = loadFixture();
            assertEquals(2.5f, o.getFloatBits("admin_hotbar_scale"), 0.0f, "admin_hotbar_scale as float bits");
        }));

        out.add(test("getFloatBits: real world_outliner_width (0x40000000) decodes to exactly 2.0f", () -> {
            ClientOptions o = loadFixture();
            assertEquals(2.0f, o.getFloatBits("world_outliner_width"), 0.0f, "world_outliner_width as float bits");
        }));

        out.add(test("getArgbComponents: real world_outliner_color (0x66000000) decodes to alpha=102, black RGB", () -> {
            ClientOptions o = loadFixture();
            int[] argb = o.getArgbComponents("world_outliner_color");
            assertEquals(4, argb.length, "argb component count");
            assertEquals(102, argb[0], "alpha");
            assertEquals(0, argb[1], "red");
            assertEquals(0, argb[2], "green");
            assertEquals(0, argb[3], "blue");
        }));

        out.add(test("has/keys: real key set matches what was loaded, unknown key absent", () -> {
            ClientOptions o = loadFixture();
            assertTrue(o.has("fov"), "fov should be present");
            assertFalse(o.has("does_not_exist"), "unknown key should be absent");
            assertEquals(9, o.keys().size(), "keys() size");
        }));

        return out;
    }

    private static ClientOptions loadFixture() throws Exception {
        Path tmp = Files.createTempFile("options-fixture", ".json");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return ClientOptions.load(tmp);
    }
}
