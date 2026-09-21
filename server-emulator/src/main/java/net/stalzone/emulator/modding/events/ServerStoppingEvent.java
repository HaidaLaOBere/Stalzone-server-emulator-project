package net.stalzone.emulator.modding.events;

/**
 * Fired exactly once, at the very start of {@code SingleplayerServer.close()} (part 89) -- before
 * any mod is unloaded and before the session is saved, so a mod can still see a fully-live {@code
 * GameLoop}/{@code PlayerState} while reacting to this event. Carries no fields; its type alone is
 * the signal. A plain final class rather than a {@code record}, matching this project's own Java
 * 11 compatibility rule (see part 83's {@code Stream.toList()} fix).
 */
public final class ServerStoppingEvent {

    @Override
    public String toString() {
        return "ServerStoppingEvent{}";
    }
}
