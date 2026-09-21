package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.loot.LootDrop;
import net.stalzone.emulator.mobs.MobInstance;

import java.util.List;

/**
 * Fired by {@code GameLoop}'s private {@code handleDeath} (part 89) for every real mob kill,
 * whether or not a loot table was configured for that kill ({@code loot} is an empty list, never
 * null, when none was) -- see {@code ModEventBus}'s class doc for why event firing is deliberately
 * decoupled from console output. A plain final class rather than a {@code record}, matching this
 * project's own Java 11 compatibility rule (see part 83's {@code Stream.toList()} fix and
 * {@code loot/LootDrop}'s established style for a simple, accessor-based data holder).
 */
public final class MobKilledEvent {

    private final MobInstance mob;
    private final List<LootDrop> loot;

    public MobKilledEvent(MobInstance mob, List<LootDrop> loot) {
        this.mob = mob;
        this.loot = loot;
    }

    public MobInstance mob() {
        return mob;
    }

    public List<LootDrop> loot() {
        return loot;
    }

    @Override
    public String toString() {
        return "MobKilledEvent{" + mob + " loot=" + loot + "}";
    }
}
