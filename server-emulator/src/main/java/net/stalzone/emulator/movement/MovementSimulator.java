package net.stalzone.emulator.movement;

/**
 * Turns {@link MovementConfig}'s real, validated speed-factor keys into an actual horizontal
 * speed number a game loop can use -- the piece part 23's own doc comment said was still
 * missing ("Nothing in the emulator applies any of these values yet -- there's no player
 * movement simulation").
 *
 * <p><b>What's real here vs. what's this emulator's own choice, kept clearly separate</b>: every
 * factor multiplied together below ({@code move.speed.factor}, {@code move.run.factor},
 * {@code move.sprint.factor}, {@code move.sneak.factor}, {@code move.crawl.factor}) is a real
 * value read straight from {@code modassets/assets/stalcraft/moving.txt} (part 23) -- these are
 * genuine relative speed RATIOS from the client's own tuning file. What that file does NOT
 * contain anywhere is an absolute base speed in blocks/second -- only relative factors. So
 * {@link #BASE_WALK_BLOCKS_PER_SECOND} below is explicitly an emulator-chosen placeholder (a
 * roughly Minecraft-scale walk speed, picked because {@code WorldModel}'s coordinate system is
 * confirmed standard Anvil block units, part 15/18), NOT a claim about STALZONE's real walk
 * speed -- no evidence source for that absolute number exists anywhere in this project.
 * {@link ServerConfig} style docs made this same base-vs-multiplier distinction for
 * {@code damageMultiplier}; this class follows the same honesty rule.
 *
 * <p><b>Real, disclosed quirk found while building this</b>: {@code move.run} (the boolean that
 * gates whether "run" mode is active at all) is {@code false} in the base config, with no
 * per-variant override for any of the 4 real difficulty/mode letters (c/e/h/m) -- i.e. "run" is
 * unconditionally disabled in this real data. {@code move.run.factor:1.4} exists in the file but
 * is dead data under every game mode this file describes. Only {@code WALK} (the implicit
 * baseline, factor 1.0 beyond the global {@code move.speed.factor}), {@code SPRINT}, {@code
 * SNEAK}, and {@code CRAWL} are real, active modes. {@link MoveMode#RUN} is still modeled here
 * (the factor is real data, just currently inert) so a future variant/config change that flips
 * {@code move.run} to true is handled without new code -- but {@link #isModeActive} exposes the
 * real on/off flag so callers can honestly skip it.
 */
public final class MovementSimulator {

    /** Emulator-chosen placeholder baseline, NOT evidenced from STALZONE -- see class doc. */
    public static final double BASE_WALK_BLOCKS_PER_SECOND = 4.3;

    public enum MoveMode {
        WALK(null, null),
        RUN("move.run", "move.run.factor"),
        SPRINT("move.sprint", "move.sprint.factor"),
        SNEAK("move.sneak", "move.sneak.factor"),
        CRAWL("move.crawl", "move.crawl.factor");

        final String enabledKey;
        final String factorKey;

        MoveMode(String enabledKey, String factorKey) {
            this.enabledKey = enabledKey;
            this.factorKey = factorKey;
        }
    }

    private final MovementConfig config;

    public MovementSimulator(MovementConfig config) {
        this.config = config;
    }

    /**
     * Whether this mode is actually enabled in the real config (always true for WALK, the
     * implicit baseline). For RUN/SPRINT/SNEAK/CRAWL this reads the real {@code move.<mode>}
     * boolean key, honoring a per-variant override when one is given.
     */
    public boolean isModeActive(MoveMode mode, Character variant) {
        if (mode == MoveMode.WALK) {
            return true;
        }
        Boolean active = variant != null ? config.asBoolean(mode.enabledKey, variant) : config.asBoolean(mode.enabledKey);
        return Boolean.TRUE.equals(active);
    }

    public boolean isModeActive(MoveMode mode) {
        return isModeActive(mode, null);
    }

    /**
     * This mode's speed multiplier: the real global {@code move.speed.factor} times the real
     * per-mode factor (1.0 for WALK, which has no dedicated factor key). Does NOT check whether
     * the mode is actually active -- call {@link #isModeActive} first if that matters (e.g. RUN
     * is real data but currently inert, see class doc).
     */
    public double speedFactor(MoveMode mode, Character variant) {
        double base = orElse(variant != null ? config.asDouble("move.speed.factor", variant) : config.asDouble("move.speed.factor"), 1.0);
        if (mode == MoveMode.WALK) {
            return base;
        }
        double modeFactor = orElse(variant != null ? config.asDouble(mode.factorKey, variant) : config.asDouble(mode.factorKey), 1.0);
        return base * modeFactor;
    }

    public double speedFactor(MoveMode mode) {
        return speedFactor(mode, null);
    }

    /** {@link #speedFactor} scaled onto the emulator's placeholder base walk speed -- see class doc for what part of this is real vs. chosen. */
    public double blocksPerSecond(MoveMode mode, Character variant) {
        return BASE_WALK_BLOCKS_PER_SECOND * speedFactor(mode, variant);
    }

    public double blocksPerSecond(MoveMode mode) {
        return blocksPerSecond(mode, null);
    }

    private static double orElse(Double value, double fallback) {
        return value != null ? value : fallback;
    }
}
