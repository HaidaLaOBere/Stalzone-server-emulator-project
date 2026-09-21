package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.emission.EmissionCycle;

/**
 * Fired by {@code GameLoop.tickEmission} (part 89) whenever a real emission/blowout phase
 * transition (part 81) is actually observed, regardless of whether a print sink was supplied to
 * that call. A plain final class rather than a {@code record}, matching this project's own Java 11
 * compatibility rule (see part 83's {@code Stream.toList()} fix and {@code loot/LootDrop}'s
 * established style for a simple, accessor-based data holder).
 */
public final class EmissionPhaseChangedEvent {

    private final EmissionCycle.Phase phase;

    public EmissionPhaseChangedEvent(EmissionCycle.Phase phase) {
        this.phase = phase;
    }

    public EmissionCycle.Phase phase() {
        return phase;
    }

    @Override
    public String toString() {
        return "EmissionPhaseChangedEvent{" + phase + "}";
    }
}
