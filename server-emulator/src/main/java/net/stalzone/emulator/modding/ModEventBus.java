package net.stalzone.emulator.modding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A minimal typed publish/subscribe bus -- this project's own event-dispatch mechanism for mods
 * (part 89), not an external framework import (same "no dependency access" constraint documented
 * in {@code testing/Assertions}'s doc and the README's "Why no Netty" section). {@code GameLoop}
 * and {@code SingleplayerServer} {@link #fire} real simulation events (mob spawn/kill, purchase,
 * ability use, quest update, emission phase change, server tick/start/stop -- see {@code
 * net.stalzone.emulator.modding.events}) at the same points a {@code ConsoleDriver} operator or
 * {@code GameLoopDemo} already sees them as printed lines; a {@link ServerMod} subscribes with
 * {@link #on} to receive the SAME structured data instead of having to parse those lines back out
 * of a log.
 *
 * <p>A listener that throws is caught and logged (via the sink this bus was built with), never
 * propagated -- one mod's bug must not stop dispatch to any OTHER listener registered for the
 * same event, and must not crash the real simulation tick that fired it. This mirrors the "one
 * typo does not kill the console thread" discipline {@code ConsoleDriver.handle} already applies
 * to bad operator input, applied here to third-party mod code instead.
 *
 * <p>Dispatch is by the event object's EXACT runtime type ({@link Object#getClass()}), not by
 * supertype/interface matching -- every event this project fires is a small, final {@code record}
 * (see {@code net.stalzone.emulator.modding.events}), so there is no subtype hierarchy for a
 * listener to accidentally miss or double-receive through.
 */
public final class ModEventBus {

    private final Map<Class<?>, List<Consumer<Object>>> listenersByType = new HashMap<>();
    private final Consumer<String> errorSink;

    public ModEventBus(Consumer<String> errorSink) {
        this.errorSink = errorSink;
    }

    /** Registers {@code listener} to be called with every future event of exactly type {@code eventType} passed to {@link #fire}. Multiple listeners may register for the same type; all are called, in registration order. */
    @SuppressWarnings("unchecked")
    public <T> void on(Class<T> eventType, Consumer<T> listener) {
        listenersByType.computeIfAbsent(eventType, key -> new ArrayList<>()).add((Consumer<Object>) listener);
    }

    /** Calls every listener registered for exactly {@code event}'s runtime type, in registration order. A no-op if none are registered. A listener that throws a {@link RuntimeException} is caught, logged, and does not stop the remaining listeners from running. */
    public void fire(Object event) {
        List<Consumer<Object>> listeners = listenersByType.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Consumer<Object> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                errorSink.accept("mod event listener threw handling " + event.getClass().getSimpleName() + ": " + e);
            }
        }
    }

    /** Number of listeners currently registered for exactly {@code eventType}. Mainly for tests. */
    public int listenerCount(Class<?> eventType) {
        List<Consumer<Object>> listeners = listenersByType.get(eventType);
        return listeners == null ? 0 : listeners.size();
    }
}
