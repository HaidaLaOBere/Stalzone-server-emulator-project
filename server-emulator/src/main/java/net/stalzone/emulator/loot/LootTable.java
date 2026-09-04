package net.stalzone.emulator.loot;

import java.util.Collections;
import java.util.List;

/**
 * An emulator-original, operator/modder-editable drop table for one mob id -- a chance that a
 * kill drops anything at all ({@link #dropChance()}), and a weighted set of possible drops
 * ({@link #entries()}) if it does. Rolled by {@link LootRoller}, loaded from a JSON file by
 * {@link LootTables}.
 *
 * <p><b>This is NOT reverse-engineered STALZONE data, and must never be presented as such.</b>
 * This project spent real effort looking for a real client-side loot/drop-rate table (the
 * "163 additional loot configs" log line investigation, docs/RESEARCH_LOG.md part 43) and
 * confirmed one is very likely absent from client files by design -- consistent with the
 * already-repeatedly-confirmed absence of a real per-zone mob spawn table. That absence is
 * exactly why this package exists: the project's own stated goal is "full control over what can
 * be done, like spawn rates or damage values" plus modding support (same standing goal already
 * answered for spawn rate via {@code mobs/SpawnScheduler} and for damage via {@code
 * items/WeaponRegistry}) -- a private/singleplayer server needs SOME loot behavior to be
 * playable, and since the real game gives no evidence to reverse-engineer here, this is original
 * emulator functionality an operator or modder fully owns and edits via plain JSON, not a claim
 * about what STALZONE itself drops.
 */
public final class LootTable {

    private final double dropChance;
    private final List<LootEntry> entries;

    public LootTable(double dropChance, List<LootEntry> entries) {
        if (dropChance < 0) {
            throw new IllegalArgumentException("dropChance must be >= 0, got " + dropChance);
        }
        this.dropChance = dropChance;
        this.entries = List.copyOf(entries);
    }

    /** Base chance (0.0-1.0, before {@link net.stalzone.emulator.config.ServerConfig#lootRateMultiplier} is applied) that a kill drops anything. */
    public double dropChance() {
        return dropChance;
    }

    public List<LootEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String toString() {
        return "LootTable{dropChance=" + dropChance + ", " + entries.size() + " entries}";
    }
}
