package net.stalzone.emulator.modding.events;

/**
 * Fired once per real iteration of {@code SingleplayerServer.run()}'s tick loop (part 89),
 * regardless of whether that iteration produced any printed line. A plain final class rather than
 * a {@code record}, matching this project's own Java 11 compatibility rule (see part 83's
 * {@code Stream.toList()} fix and {@code loot/LootDrop}'s established style for a simple,
 * accessor-based data holder).
 */
public final class ServerTickEvent {

    private final long tickNumber;

    public ServerTickEvent(long tickNumber) {
        this.tickNumber = tickNumber;
    }

    public long tickNumber() {
        return tickNumber;
    }

    @Override
    public String toString() {
        return "ServerTickEvent{" + tickNumber + "}";
    }
}
