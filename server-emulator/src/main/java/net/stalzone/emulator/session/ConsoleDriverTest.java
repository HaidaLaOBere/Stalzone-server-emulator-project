package net.stalzone.emulator.session;

import net.stalzone.emulator.testing.TestRunner;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ConsoleDriver} (part 86; extended part 88 for {@code tune}, part 89
 * for {@code mods}) covering
 * command PARSING/dispatch robustness only -- blank/unknown/malformed input, and the {@code
 * help}/{@code quit} commands, none of which ever touch the {@link SingleplayerServer} this class
 * would otherwise need (so {@code null} is passed for it here, same as a real instance would be,
 * since these code paths never dereference it). Testing the actual tick-method dispatch
 * (move/attack/buy/quest/tune/...) requires a real, fully-loaded {@link SingleplayerServer}, which
 * itself has no dedicated unit test file for the same reason (see that class's own doc) -- real
 * client zone/mob/weapon data, not a small bundleable fixture. That part is verified end-to-end
 * via a live run instead, same established precedent as {@code SingleplayerServer} and {@code
 * GameLoopDemo}.
 */
public final class ConsoleDriverTest {

    private static ConsoleDriver driverWithNullServer(List<String> captured) {
        return new ConsoleDriver(null, new BufferedReader(new StringReader("")), captured::add);
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a blank line produces no output", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("   ");
            assertTrue(captured.isEmpty(), "blank input line is silently ignored");
        }));

        out.add(test("an unknown command is reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("frobnicate 1 2 3");
            assertEquals(1, captured.size(), "one reported line");
            assertTrue(captured.get(0).contains("unknown command"), "reports it as unknown: " + captured.get(0));
        }));

        out.add(test("command names are case-insensitive", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("HELP");
            assertTrue(!captured.isEmpty(), "HELP (uppercase) is recognized the same as help");
            assertTrue(captured.get(0).toLowerCase().contains("commands"), "prints the command list: " + captured.get(0));
        }));

        out.add(test("help lists every real command name", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("help");
            String joined = String.join("\n", captured);
            for (String expected : new String[]{"status", "mobs", "inventory", "move", "hazard",
                    "attack", "buy", "unlock", "useability", "class", "quest", "save", "tune", "mods", "quit"}) {
                assertTrue(joined.contains(expected), "help output mentions '" + expected + "': " + joined);
            }
        }));

        out.add(test("a malformed numeric argument is caught and reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("move notanumber 2 WALK"); // fails parsing parts[1] before ever touching the (null) server
            assertEquals(1, captured.size(), "one reported line");
            assertTrue(captured.get(0).contains("bad command"), "reports the parse failure: " + captured.get(0));
        }));

        out.add(test("an unknown enum name is caught and reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("move 1 2 SIDEWAYS"); // fails MoveMode.valueOf before ever touching the (null) server
            assertTrue(captured.get(0).contains("bad command"), "reports the enum failure: " + captured.get(0));
        }));

        out.add(test("a missing argument is caught and reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("buy onlyOneArg"); // parts[2] doesn't exist -- ArrayIndexOutOfBoundsException before touching the (null) server
            assertTrue(captured.get(0).contains("bad command"), "reports the missing-argument failure: " + captured.get(0));
        }));

        out.add(test("tune with a missing value argument is caught and reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("tune spawnrate"); // parts[2] doesn't exist -- ArrayIndexOutOfBoundsException before touching the (null) server
            assertTrue(captured.get(0).contains("bad command"), "reports the missing-value failure: " + captured.get(0));
        }));

        out.add(test("tune with a malformed numeric value is caught and reported, not thrown", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            driver.handle("tune spawnrate notanumber"); // fails Double.parseDouble before ever touching the (null) server
            assertTrue(captured.get(0).contains("bad command"), "reports the parse failure: " + captured.get(0));
        }));

        out.add(test("tune with an unrecognized knob name is reported without touching the server", () -> {
            List<String> captured = new ArrayList<>();
            ConsoleDriver driver = driverWithNullServer(captured);
            // "frobRate" parses as a valid number-bearing command but is not a real knob name --
            // doTune must report this WITHOUT calling server.gameLoop() (which would NPE on the
            // null server used in these parsing-only tests), same "validate before dispatch"
            // discipline as every other command in this class.
            driver.handle("tune frobRate 2.0");
            assertEquals(1, captured.size(), "one reported line, no exception propagated");
            assertTrue(captured.get(0).contains("unknown knob"), "reports it as an unknown knob: " + captured.get(0));
        }));

        out.add(test("quit stops run()'s read loop without needing a real server", () -> {
            List<String> captured = new ArrayList<>();
            // "quit" is handled entirely before any tick-method dispatch (see ConsoleDriver.handle);
            // a trailing "help" line would prove the loop kept going if quit didn't actually stop it.
            BufferedReader input = new BufferedReader(new StringReader("quit\nhelp\n"));
            ConsoleDriver driver = new ConsoleDriver(null, input, captured::add);
            driver.run();
            long quitLines = captured.stream().filter(line -> line.contains("stopping this console")).count();
            assertEquals(1L, quitLines, "run() reports stopping exactly once");
            long helpLines = captured.stream().filter(line -> line.toLowerCase().contains("commands (part 86")).count();
            assertEquals(0L, helpLines, "the 'help' line after 'quit' is never read -- the loop already stopped");
        }));

        return out;
    }
}
