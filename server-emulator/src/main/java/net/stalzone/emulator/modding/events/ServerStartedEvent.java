package net.stalzone.emulator.modding.events;

/**
 * Fired exactly once, at the very end of {@code SingleplayerServer}'s constructor (part 89) --
 * every other core system (including every loaded mod's own {@code onLoad}) has already run by
 * the time this fires. Carries no fields; its type alone is the signal. A plain final class rather
 * than a {@code record}, matching this project's own Java 11 compatibility rule (see part 83's
 * {@code Stream.toList()} fix).
 */
public final class ServerStartedEvent {

    @Override
    public String toString() {
        return "ServerStartedEvent{}";
    }
}
