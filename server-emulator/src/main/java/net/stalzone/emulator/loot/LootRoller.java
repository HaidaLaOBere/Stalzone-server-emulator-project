package net.stalzone.emulator.loot;

import net.stalzone.emulator.config.ServerConfig;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Finally wires {@link ServerConfig#lootRateMultiplier} into real behavior -- until this class,
 * that field was an explicitly-documented unwired placeholder ("no loot-table system exists
 * yet", see {@code ServerConfig}'s own class doc). Same style as {@code mobs/SpawnScheduler}
 * (which did the same thing for {@code spawnRateMultiplier}): the multiplier is captured from
 * {@link ServerConfig} at construction time and a {@link Random} is injected by the caller so
 * demo/test runs stay reproducible with a fixed seed.
 *
 * <p><b>Roll semantics (an emulator design choice, not evidenced data -- see {@link LootTable}'s
 * doc):</b> one roll against {@code table.dropChance() * lootRateMultiplier} (clamped to
 * {@code [0.0, 1.0]}) decides whether anything drops at all; if it does, exactly ONE entry is
 * picked from {@link LootTable#entries()} by relative weight, and its count is rolled uniformly
 * in {@code [minCount, maxCount]}. {@code lootRateMultiplier} scales the drop CHANCE, not the
 * dropped count -- a deliberate, simple choice consistent with how {@code spawnRateMultiplier}
 * scales spawn cadence rather than mob count per spawn.
 *
 * <p>As of part 88, {@link #setLootRateMultiplier} lets an operator retune this live, without a
 * restart -- unlike {@code SpawnScheduler}/{@code EmissionCycle}, a live change here takes effect
 * on the very next {@link #roll}, since the multiplier is read fresh every call rather than baked
 * into a precomputed countdown.
 */
public final class LootRoller {

    private double lootRateMultiplier;
    private final Random random;

    public LootRoller(ServerConfig config, Random random) {
        this.lootRateMultiplier = config.lootRateMultiplier;
        this.random = random;
    }

    /** The loot-rate multiplier currently in effect (the constructor's {@code ServerConfig} value, or a live override via {@link #setLootRateMultiplier}). */
    public double lootRateMultiplier() {
        return lootRateMultiplier;
    }

    /**
     * Live-retunes the loot-rate multiplier (part 88 -- an operator/console action, e.g.
     * {@code ConsoleDriver}'s {@code tune} command; not new evidence). Applies starting with the
     * very next {@link #roll} call.
     */
    public void setLootRateMultiplier(double lootRateMultiplier) {
        this.lootRateMultiplier = lootRateMultiplier;
    }

    /**
     * Rolls one kill's worth of loot against {@code table}. Returns an empty list if {@code
     * table} is null, has no entries, or the drop-chance roll fails.
     */
    public List<LootDrop> roll(LootTable table) {
        if (table == null || table.entries().isEmpty()) {
            return Collections.emptyList();
        }

        double effectiveChance = table.dropChance() * (lootRateMultiplier > 0 ? lootRateMultiplier : 0.0);
        effectiveChance = Math.max(0.0, Math.min(1.0, effectiveChance));

        if (random.nextDouble() >= effectiveChance) {
            return Collections.emptyList();
        }

        LootEntry chosen = pickWeighted(table.entries());
        int range = chosen.maxCount() - chosen.minCount() + 1;
        int count = chosen.minCount() + random.nextInt(range);
        return List.of(new LootDrop(chosen.itemId(), count));
    }

    private LootEntry pickWeighted(List<LootEntry> entries) {
        double totalWeight = 0;
        for (LootEntry e : entries) {
            totalWeight += e.weight();
        }
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (LootEntry e : entries) {
            cumulative += e.weight();
            if (roll < cumulative) {
                return e;
            }
        }
        return entries.get(entries.size() - 1); // floating-point rounding fallback
    }
}
