package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.mobs.MobInstance;

/**
 * Fired by {@code GameLoop.tickSpawns} (part 89) immediately after a real mob spawns, regardless
 * of whether a print sink was supplied to that call -- see {@code ModEventBus}'s class doc for why
 * event firing is deliberately decoupled from console output. A plain final class rather than a
 * {@code record}, matching this project's own Java 11 compatibility rule (see part 83's
 * {@code Stream.toList()} fix and {@code loot/LootDrop}'s established style for a simple,
 * accessor-based data holder).
 */
public final class MobSpawnedEvent {

    private final MobInstance mob;

    public MobSpawnedEvent(MobInstance mob) {
        this.mob = mob;
    }

    public MobInstance mob() {
        return mob;
    }

    @Override
    public String toString() {
        return "MobSpawnedEvent{" + mob + "}";
    }
}
