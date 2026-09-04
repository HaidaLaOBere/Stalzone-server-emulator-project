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
 */
public final class SpawnScheduler {

    /** Emulator-chosen placeholder, NOT evidenced -- see class doc. Mean seconds between spawns at spawnRateMultiplier=1.0. */
    public static final double BASE_MEAN_SECONDS_BETWEEN_SPAWNS = 30.0;

    private final MobRegistry registry;
    private final double spawnRateMultiplier;
    private final Random random;

    private double secondsUntilNextSpawn;

    public SpawnScheduler(MobRegistry registry, ServerConfig config, Random random) {
        this.registry = registry;
        this.spawnRateMultiplier = config.spawnRateMultiplier;
        this.random = random;
        this.secondsUntilNextSpawn = nextInterval();
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
