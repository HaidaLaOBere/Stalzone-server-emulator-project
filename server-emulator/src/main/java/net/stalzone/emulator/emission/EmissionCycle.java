package net.stalzone.emulator.emission;

import net.stalzone.emulator.config.ServerConfig;

import java.util.Random;

/**
 * Wires the real S.T.A.L.K.E.R.-series "emission"/"blowout" mechanic (Russian "Выброс") into an
 * actual tick-driven cycle -- the strongest single real-mechanic confirmation this project has
 * found, from part 80's real (untranslated, dev test/sample) handbook articles under {@code
 * modassets/assets/_localized/en/stalkerguide/handbook/articles/ejection/}, extending part 79's
 * bare {@code ejection/} audio lead into a full, evidenced 4-stage design. See {@code
 * docs/RESEARCH_LOG.md} part 80 for the full quote and translation.
 *
 * <p><b>What is REAL, evidenced data here (do not change these without new evidence):</b> the real
 * text says emissions occur "regularly, approximately once every 2-4 hours, with a maximum 4-hour
 * gap between events" -- {@link #REAL_MIN_INTERVAL_SECONDS}/{@link #REAL_MAX_INTERVAL_SECONDS}.
 * The real warning phase gives players "about 2 minutes to reach a safe place" before the event
 * turns deadly -- {@link #REAL_WARNING_SECONDS}. After an emission ends, real artefacts begin
 * spawning, "most active in the first 30 minutes", stopping "completely after an hour and a half"
 * -- {@link #REAL_ARTEFACT_HIGH_ACTIVITY_SECONDS} and {@link #REAL_ARTEFACT_WINDOW_TOTAL_SECONDS}.
 *
 * <p><b>What is NOT evidenced (this emulator's own design choice, disclosed):</b> the real handbook
 * text never states how LONG the deadly, must-be-sheltered phase itself lasts (only that survival
 * outside a shelter during it is impossible) -- {@link #EMULATOR_ACTIVE_PHASE_SECONDS} is this
 * project's own arbitrary placeholder for that one missing duration, same disclosure standard as
 * {@code mobs/SpawnScheduler}'s {@code BASE_MEAN_SECONDS_BETWEEN_SPAWNS}.
 *
 * <p>{@link ServerConfig#emissionRateMultiplier} scales the real 2-4 hour interval the same way
 * {@code spawnRateMultiplier} scales {@code SpawnScheduler}'s cadence -- exactly the kind of "full
 * control over ... spawn rates"-style knob this project exists to provide, layered on top of real
 * evidenced timing rather than replacing it.
 *
 * <p>This class tracks phase and timing only -- it has no world/player-position awareness (nothing
 * in this project's evidence ties emissions to a specific place, and no real shelter-marker
 * coordinate data has ever been found), so "is this player actually in a shelter" is left entirely
 * to the caller, same pattern as {@code GameLoop.tickHazardCheck}'s caller-supplied hazard position.
 *
 * <p>As of part 88, {@link #setEmissionRateMultiplier} lets an operator retune this live, without
 * a restart. Same "applies going forward, not retroactively" semantics as {@code
 * SpawnScheduler#setSpawnRateMultiplier}: it changes the multiplier future calls to {@link
 * #nextRealInterval} use, but does not rescale a {@code waitTimer}/{@code phaseTimer} already
 * counting down when it is called.
 */
public final class EmissionCycle {

    /** The 4 real named stages this project's evidence supports, in real cycle order. */
    public enum Phase { WAITING, WARNING, ACTIVE, ARTEFACT_WINDOW }

    // --- REAL, evidenced constants (part 80, docs/RESEARCH_LOG.md) ---
    public static final double REAL_MIN_INTERVAL_SECONDS = 2 * 3600.0;
    public static final double REAL_MAX_INTERVAL_SECONDS = 4 * 3600.0;
    public static final double REAL_WARNING_SECONDS = 120.0;
    public static final double REAL_ARTEFACT_HIGH_ACTIVITY_SECONDS = 30 * 60.0;
    public static final double REAL_ARTEFACT_WINDOW_TOTAL_SECONDS = 90 * 60.0;

    // --- EMULATOR-ORIGINAL, NOT evidenced -- see class doc ---
    public static final double EMULATOR_ACTIVE_PHASE_SECONDS = 180.0;

    private final Random random;
    private double emissionRateMultiplier;
    private Phase phase = Phase.WAITING;
    private double waitTimer;
    private double phaseTimer;

    public EmissionCycle(ServerConfig config, Random random) {
        this.random = random;
        this.emissionRateMultiplier = config.emissionRateMultiplier;
        this.waitTimer = nextRealInterval();
    }

    /** The emission-rate multiplier currently in effect (the constructor's {@code ServerConfig} value, or a live override via {@link #setEmissionRateMultiplier}). */
    public double emissionRateMultiplier() {
        return emissionRateMultiplier;
    }

    /**
     * Live-retunes the emission-rate multiplier (part 88 -- an operator/console action, e.g.
     * {@code ConsoleDriver}'s {@code tune} command; not new evidence). Takes effect starting with
     * the NEXT real interval this cycle computes (i.e. the next time it returns to {@code
     * WAITING}) -- it does not rescale a countdown already in progress.
     */
    public void setEmissionRateMultiplier(double emissionRateMultiplier) {
        this.emissionRateMultiplier = emissionRateMultiplier;
    }

    private double nextRealInterval() {
        double multiplier = emissionRateMultiplier > 0 ? emissionRateMultiplier : 0.0001; // guard against div-by-zero from a misconfigured 0.0
        double real = REAL_MIN_INTERVAL_SECONDS + random.nextDouble() * (REAL_MAX_INTERVAL_SECONDS - REAL_MIN_INTERVAL_SECONDS);
        return real / multiplier;
    }

    /** The current real-named stage. Starts at {@code WAITING}. */
    public Phase phase() {
        return phase;
    }

    /** Real disclosed fact: survival outside a shelter is impossible during {@code ACTIVE}. */
    public boolean shelterRequired() {
        return phase == Phase.ACTIVE;
    }

    /** True during the real 30-minute high-activity sub-window at the start of {@code ARTEFACT_WINDOW}. */
    public boolean artefactSpawnBoostActive() {
        return phase == Phase.ARTEFACT_WINDOW && phaseTimer > (REAL_ARTEFACT_WINDOW_TOTAL_SECONDS - REAL_ARTEFACT_HIGH_ACTIVITY_SECONDS);
    }

    /**
     * Advances the cycle by {@code deltaSeconds}, correctly handling a delta large enough to cross
     * more than one real phase boundary in a single call (e.g. a coarse-grained caller ticking in
     * large steps). Returns the new {@link Phase} if at least one transition happened this call,
     * or null if the cycle is still mid-phase.
     */
    public Phase tick(double deltaSeconds) {
        Phase changedTo = null;
        double remaining = deltaSeconds;
        while (remaining > 0) {
            double current = phase == Phase.WAITING ? waitTimer : phaseTimer;
            if (remaining < current) {
                if (phase == Phase.WAITING) {
                    waitTimer -= remaining;
                } else {
                    phaseTimer -= remaining;
                }
                remaining = 0;
            } else {
                remaining -= current;
                changedTo = advancePhase();
            }
        }
        return changedTo;
    }

    private Phase advancePhase() {
        switch (phase) {
            case WAITING:
                phase = Phase.WARNING;
                phaseTimer = REAL_WARNING_SECONDS;
                break;
            case WARNING:
                phase = Phase.ACTIVE;
                phaseTimer = EMULATOR_ACTIVE_PHASE_SECONDS;
                break;
            case ACTIVE:
                phase = Phase.ARTEFACT_WINDOW;
                phaseTimer = REAL_ARTEFACT_WINDOW_TOTAL_SECONDS;
                break;
            case ARTEFACT_WINDOW:
            default:
                phase = Phase.WAITING;
                waitTimer = nextRealInterval();
                break;
        }
        return phase;
    }
}
