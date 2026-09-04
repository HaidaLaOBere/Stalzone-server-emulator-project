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
 * Regression tests for KeyBindings (part 42), using a small fixture built from REAL action
 * names/keycodes captured from a real install's {@code config/keybindings} -- both real gameplay
 * binds ({@code key_key.*}) and a couple of the real developer/debug-tool entries this project
 * found (see that class's doc; this fixture does not reproduce all 205, just enough to test the
 * gameplay/non-gameplay split logic against real examples of both).
 */
public final class KeyBindingsTest {

    private static final String FIXTURE_JSON = "{"
            + "\"key_key.attack\":[0],"
            + "\"key_key.use\":[-1],"
            + "\"key_key.prev_item\":[],"
            + "\"key_No clip\":[67],"
            + "\"key_Scene Editor\":[341,86],"
            + "\"key_GameObject Debugger\":[]"
            + "}";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("KeyBindings.load parses the real-shaped fixture", () -> {
            KeyBindings b = loadFixture();
            assertEquals(6, b.size(), "fixture action count");
        }));

        out.add(test("isGameplayAction: real key_key.* actions are gameplay, real non-prefixed ones are not", () -> {
            KeyBindings b = loadFixture();
            assertTrue(b.isGameplayAction("key_key.attack"), "key_key.attack should be gameplay");
            assertFalse(b.isGameplayAction("key_No clip"), "key_No clip should not be gameplay");
            assertFalse(b.isGameplayAction("key_Scene Editor"), "key_Scene Editor should not be gameplay");
        }));

        out.add(test("gameplayActionNames/nonGameplayActionNames partition the fixture correctly", () -> {
            KeyBindings b = loadFixture();
            assertEquals(3, b.gameplayActionNames().size(), "gameplay action count");
            assertEquals(3, b.nonGameplayActionNames().size(), "non-gameplay action count");
            assertTrue(b.nonGameplayActionNames().contains("key_No clip"), "non-gameplay set should contain key_No clip");
            assertTrue(b.nonGameplayActionNames().contains("key_Scene Editor"), "non-gameplay set should contain key_Scene Editor");
        }));

        out.add(test("codesFor/isBound: real negative sentinel keycode (-1) is preserved, not filtered out", () -> {
            KeyBindings b = loadFixture();
            assertEquals(List.of(-1), b.codesFor("key_key.use"), "key_key.use codes");
            assertTrue(b.isBound("key_key.use"), "key_key.use should be considered bound");
        }));

        out.add(test("codesFor/isBound: an unbound real action (empty list) is correctly reported unbound", () -> {
            KeyBindings b = loadFixture();
            assertTrue(b.codesFor("key_key.prev_item").isEmpty(), "key_key.prev_item codes should be empty");
            assertFalse(b.isBound("key_key.prev_item"), "key_key.prev_item should be unbound");
        }));

        out.add(test("codesFor: an action name that doesn't exist returns an empty list, not null", () -> {
            KeyBindings b = loadFixture();
            assertNotNull(b.codesFor("key_key.does_not_exist"), "codesFor should never return null");
            assertTrue(b.codesFor("key_key.does_not_exist").isEmpty(), "unknown action codes should be empty");
        }));

        out.add(test("unboundActionCount: matches the real fixture (2 of 6 actions have empty code lists)", () -> {
            KeyBindings b = loadFixture();
            assertEquals(2, b.unboundActionCount(), "unbound action count");
        }));

        out.add(test("codesFor: real two-code (primary+secondary) binding preserved in order", () -> {
            KeyBindings b = loadFixture();
            assertEquals(List.of(341, 86), b.codesFor("key_Scene Editor"), "key_Scene Editor codes");
        }));

        return out;
    }

    private static KeyBindings loadFixture() throws Exception {
        Path tmp = Files.createTempFile("keybindings-fixture", "");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, FIXTURE_JSON, StandardCharsets.UTF_8);
        return KeyBindings.load(tmp);
    }
}
