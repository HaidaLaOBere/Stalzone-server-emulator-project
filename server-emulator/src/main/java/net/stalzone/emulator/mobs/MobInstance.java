package net.stalzone.emulator.mobs;

/**
 * One spawned, alive mob inside a running {@link net.stalzone.emulator.session.GameLoop} --
 * pairs a real {@link MobConfig} (behavior/faction data straight from a real {@code .smm} file)
 * with a health pool this emulator tracks itself.
 *
 * <p><b>Why a placeholder is needed at all, disclosed up front:</b> real STALZONE {@code .smm}
 * mob config files carry NO health/HP field anywhere in their schema -- confirmed across all 35
 * real files (part 10, re-checked part 20, re-confirmed again while building this class).
 * {@link MobConfig} deliberately does not invent a health accessor to paper over that absence.
 * But part 29's {@code GameLoop} and this part's combat wiring need SOME notion of "how much
 * damage before a mob dies" in order to make the real, evidenced
 * {@link net.stalzone.emulator.items.WeaponRegistry} damage numbers do anything at all once a
 * shot is fired at a live target. {@link #DEFAULT_MAX_HEALTH} is this emulator's own arbitrary
 * choice -- flat across every mob type, because no source anywhere (client code, capture, or
 * public doc) distinguishes a boar's toughness from a flesh's or a controller's. This is exactly
 * the "full control over ... damage values" knob the project exists to expose (a server operator
 * can retune {@code DEFAULT_MAX_HEALTH} or pass a per-instance value), not a claim about what the
 * real game actually uses. Do not read this number as reverse-engineered data -- it isn't.
 */
public final class MobInstance {

    /** Emulator placeholder, NOT evidenced from any STALZONE source -- see class doc. */
    public static final double DEFAULT_MAX_HEALTH = 100.0;

    private final MobConfig config;
    private final double maxHealth;
    private double health;

    public MobInstance(MobConfig config, double maxHealth) {
        this.config = config;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    /** Convenience constructor using {@link #DEFAULT_MAX_HEALTH}. */
    public MobInstance(MobConfig config) {
        this(config, DEFAULT_MAX_HEALTH);
    }

    public MobConfig config() {
        return config;
    }

    public double health() {
        return health;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    /**
     * Subtracts {@code amount} from this mob's health, clamped so it never goes below zero.
     * Returns the amount actually applied (equal to {@code amount} unless this hit would have
     * over-killed an already-nearly-dead mob). Negative/zero amounts are a no-op.
     */
    public double applyDamage(double amount) {
        if (amount <= 0) {
            return 0;
        }
        double before = health;
        health = Math.max(0, health - amount);
        return before - health;
    }

    @Override
    public String toString() {
        return "MobInstance{" + config.configId() + " (" + config.mobName() + "), "
                + String.format("%.1f/%.1f hp", health, maxHealth) + (isDead() ? " DEAD" : "") + "}";
    }
}
