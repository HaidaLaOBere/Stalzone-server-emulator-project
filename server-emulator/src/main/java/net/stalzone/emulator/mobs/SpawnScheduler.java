package net.stalzone.emulator.mobs;

import net.stalzone.emulator.config.ServerConfig;

import java.util.List;
import java.util.Random;

/**
 * Finally wires {@link ServerConfig#spawnRateMultiplier} into real behavior -- until this class,
 * every "Not yet done" note since part 20 said that field existed but nothing read it.
 *
 * <p><b>Be clear about what is and isn't evidenced here</b>: {@link MobRegistry} (part 20) is a
 * real, validated catalogue of all 35 mutant AI configs -- that part is evidenced. What this
 * project has NEVER found anywhere, across every pass (parts 11, 20, 23, 25, 26), is a real
 * per-zone spawn table -- which mobs spawn in {@code dungeon_bolota_tutorial} vs.
 * {@code agroprom} vs. anywhere else, at what real rate, is not recorded in any {@code
 * modassets}/{@code config} file or log this project has read. So this scheduler does NOT
 * attempt to reproduce STALZONE's real spawn logic (there is no evidence for what that even is)
 * -- it is this emulator's own simple, generic design: pick uniformly at random from the full
 * real 35-mob roster, on a fixed-mean interval scaled by {@code spawnRateMultiplier}. This is
 * exactly the kind of "full control over ... spawn rates" knob the project exists to provide,
 * not a claim about how the real game decides spawns.
 *
 * <p>As of part 88, {@link #setSpawnRateMultiplier} lets an operator retune this live, without a
 * restart -- until then, the multiplier was only ever captured once, at construction time, from
 * whatever {@link ServerConfig#spawnRateMultiplier} held at that moment; editing that field
 * afterward (e.g. from a running {@code ConsoleDriver}) silently did nothing, since nothing
 * re-read it. See {@link #setSpawnRateMultiplier}'s own doc for exactly when a live change takes
 * effect.
 */
public final class SpawnScheduler {

    /** Emulator-chosen placeholder, NOT evidenced -- see class doc. Mean seconds between spawns at spawnRateMultiplier=1.0. */
    public static final double BASE_MEAN_SECONDS_BETWEEN_SPAWNS = 30.0;

    private final MobRegistry registry;
    private double spawnRateMultiplier;
    private final Random random;

    private double secondsUntilNextSpawn;

    public SpawnScheduler(MobRegistry registry, ServerConfig config, Random random) {
        this.registry = registry;
        this.spawnRateMultiplier = config.spawnRateMultiplier;
        this.random = random;
        this.secondsUntilNextSpawn = nextInterval();
    }

    /** The spawn-rate multiplier currently in effect (the constructor's {@code ServerConfig} value, or a live override via {@link #setSpawnRateMultiplier}). */
    public double spawnRateMultiplier() {
        return spawnRateMultiplier;
    }

    /**
     * Live-retunes the spawn-rate multiplier (part 88 -- an operator/console action, e.g.
     * {@code ConsoleDriver}'s {@code tune} command; not new evidence). Takes effect starting with
     * the NEXT interval this scheduler computes after the currently-pending countdown fires next
     * -- it does not retroactively rescale a countdown already in progress at the moment this is
     * called, same "applies going forward, not retroactively" semantics as changing a game's
     * spawn-rate setting mid-session generally would.
     */
    public void setSpawnRateMultiplier(double spawnRateMultiplier) {
        this.spawnRateMultiplier = spawnRateMultiplier;
    }

    private double nextInterval() {
        double multiplier = spawnRateMultiplier > 0 ? spawnRateMultiplier : 0.0001; // guard against div-by-zero from a misconfigured 0.0
        return BASE_MEAN_SECONDS_BETWEEN_SPAWNS / multiplier;
    }

    /**
     * Advances the scheduler by {@code deltaSeconds}. Returns the mob that spawned, or null if
     * no spawn happened this tick. A caller ticking every simulated second should expect roughly
     * one non-null result every {@code BASE_MEAN_SECONDS_BETWEEN_SPAWNS / spawnRateMultiplier}
     * seconds, on average.
     */
    public MobConfig tick(double deltaSeconds) {
        secondsUntilNextSpawn -= deltaSeconds;
        if (secondsUntilNextSpawn > 0) {
            return null;
        }
        secondsUntilNextSpawn += nextInterval();
        List<MobConfig> all = registry.all();
        if (all.isEmpty()) {
            return null;
        }
        return all.get(random.nextInt(all.size()));
    }
}
