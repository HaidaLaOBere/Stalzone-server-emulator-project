package net.stalzone.emulator.modding;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Pure-logic regression tests for {@link ModEventBus} (part 89) -- no real mod JAR or {@link
 * ModLoader} discovery involved, just the typed pub/sub mechanism itself. The real
 * JAR/{@code ServiceLoader}-driven path is exercised only by live end-to-end verification (see
 * docs/RESEARCH_LOG.md part 89), same "no dedicated unit test for a real-file-heavy path"
 * precedent {@link net.stalzone.emulator.mobs.SpawnSchedulerTest} already sets for this project.
 */
public final class ModEventBusTest {

    private static final class Ping {
    }

    private static final class Pong {
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("fire delivers an event to a listener registered for its exact runtime type", () -> {
            ModEventBus bus = new ModEventBus(msg -> { });
            List<Ping> received = new ArrayList<>();
            bus.on(Ping.class, received::add);
            Ping event = new Ping();
            bus.fire(event);
            assertEquals(1, received.size(), "listener received exactly one event");
            assertTrue(received.get(0) == event, "listener received the exact fired instance");
        }));

        out.add(test("fire does not deliver an event to a listener registered for a different type", () -> {
            ModEventBus bus = new ModEventBus(msg -> { });
            List<Object> received = new ArrayList<>();
            bus.on(Pong.class, received::add);
            bus.fire(new Ping());
            assertTrue(received.isEmpty(), "Pong listener never fires for a Ping event");
        }));

        out.add(test("fire with no registered listener for that type is a silent no-op", () -> {
            ModEventBus bus = new ModEventBus(msg -> { throw new AssertionError("errorSink must not be called: " + msg); });
            bus.fire(new Ping()); // no listeners registered at all -- must not throw or call errorSink
        }));

        out.add(test("a listener that throws does not stop a second listener for the same event from running", () -> {
            List<String> errors = new ArrayList<>();
            ModEventBus bus = new ModEventBus(errors::add);
            List<Ping> secondListenerReceived = new ArrayList<>();
            bus.on(Ping.class, event -> { throw new RuntimeException("boom"); });
            bus.on(Ping.class, secondListenerReceived::add);
            bus.fire(new Ping());
            assertEquals(1, secondListenerReceived.size(), "second listener still ran despite the first throwing");
            assertEquals(1, errors.size(), "the throw was reported to errorSink exactly once");
            assertTrue(errors.get(0).contains("boom"), "errorSink message mentions the original exception: " + errors.get(0));
        }));

        out.add(test("listenerCount reflects registrations per exact type, zero for an unregistered type", () -> {
            ModEventBus bus = new ModEventBus(msg -> { });
            assertEquals(0, bus.listenerCount(Ping.class), "no listeners registered yet");
            bus.on(Ping.class, event -> { });
            bus.on(Ping.class, event -> { });
            assertEquals(2, bus.listenerCount(Ping.class), "two Ping listeners registered");
            assertEquals(0, bus.listenerCount(Pong.class), "Pong was never registered");
        }));

        return out;
    }
}
