package net.stalzone.emulator.emission;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link EmissionCycle} (part 81; extended part 88 for {@link
 * EmissionCycle#setEmissionRateMultiplier}) -- pure Java logic, no real files needed.
 * Real timing constants (the 2-4 hour interval, the ~2 minute warning, the 30/90 minute artefact
 * window) are exercised directly against {@link EmissionCycle}'s own public constants rather than
 * hardcoded again here, so this test stays correct if those constants are ever revised with new
 * evidence. The random per-cycle interval is reproduced deterministically by seeding an
 * independent {@link Random} with the same seed and replicating the exact same formula/call order
 * {@link EmissionCycle} itself uses -- {@code java.util.Random} is a documented, seed-deterministic
 * LCG, so this is a legitimate way to compute the expected value, not a guess.
 */
public final class EmissionCycleTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("new EmissionCycle starts at WAITING, no shelter required, no artefact boost", () -> {
            EmissionCycle cycle = fresh(1L, 1.0);
            assertEquals(EmissionCycle.Phase.WAITING, cycle.phase(), "starting phase");
            assertFalse(cycle.shelterRequired(), "shelter not required while waiting");
            assertFalse(cycle.artefactSpawnBoostActive(), "no artefact boost while waiting");
        }));

        out.add(test("tick by less than the real wait interval makes no transition", () -> {
            long seed = 7L;
            EmissionCycle cycle = fresh(seed, 1.0);
            double interval = expectedInterval(seed, 1.0);
            EmissionCycle.Phase changed = cycle.tick(interval - 1.0);
            assertNull(changed, "no transition yet");
            assertEquals(EmissionCycle.Phase.WAITING, cycle.phase(), "still waiting");
        }));

        out.add(test("tick crossing the real wait interval transitions WAITING -> WARNING", () -> {
            long seed = 7L;
            EmissionCycle cycle = fresh(seed, 1.0);
            double interval = expectedInterval(seed, 1.0);
            cycle.tick(interval - 1.0);
            EmissionCycle.Phase changed = cycle.tick(1.0);
            assertEquals(EmissionCycle.Phase.WARNING, changed, "transitioned to WARNING");
            assertEquals(EmissionCycle.Phase.WARNING, cycle.phase(), "now in WARNING");
            assertFalse(cycle.shelterRequired(), "shelter not required during WARNING");
        }));

        out.add(test("real WARNING duration (2 minutes) transitions WARNING -> ACTIVE, shelter now required", () -> {
            EmissionCycle cycle = atPhase(EmissionCycle.Phase.WARNING);
            EmissionCycle.Phase changed = cycle.tick(EmissionCycle.REAL_WARNING_SECONDS);
            assertEquals(EmissionCycle.Phase.ACTIVE, changed, "transitioned to ACTIVE");
            assertTrue(cycle.shelterRequired(), "shelter required during ACTIVE (real, part 80)");
        }));

        out.add(test("emulator ACTIVE duration transitions ACTIVE -> ARTEFACT_WINDOW, boost immediately active", () -> {
            EmissionCycle cycle = atPhase(EmissionCycle.Phase.ACTIVE);
            EmissionCycle.Phase changed = cycle.tick(EmissionCycle.EMULATOR_ACTIVE_PHASE_SECONDS);
            assertEquals(EmissionCycle.Phase.ARTEFACT_WINDOW, changed, "transitioned to ARTEFACT_WINDOW");
            assertFalse(cycle.shelterRequired(), "shelter no longer required once emission ends");
            assertTrue(cycle.artefactSpawnBoostActive(), "real 30-minute high-activity window just started");
        }));

        out.add(test("artefact boost ends after the real 30-minute high-activity window, phase stays ARTEFACT_WINDOW", () -> {
            EmissionCycle cycle = atPhase(EmissionCycle.Phase.ARTEFACT_WINDOW);
            EmissionCycle.Phase changed = cycle.tick(EmissionCycle.REAL_ARTEFACT_HIGH_ACTIVITY_SECONDS + 1.0);
            assertNull(changed, "still in ARTEFACT_WINDOW, no phase transition");
            assertEquals(EmissionCycle.Phase.ARTEFACT_WINDOW, cycle.phase(), "phase unchanged");
            assertFalse(cycle.artefactSpawnBoostActive(), "boost window (real 30 min) has ended");
        }));

        out.add(test("real 90-minute total artefact window ends ARTEFACT_WINDOW -> WAITING, with a fresh real interval", () -> {
            EmissionCycle cycle = atPhase(EmissionCycle.Phase.ARTEFACT_WINDOW);
            EmissionCycle.Phase changed = cycle.tick(EmissionCycle.REAL_ARTEFACT_WINDOW_TOTAL_SECONDS);
            assertEquals(EmissionCycle.Phase.WAITING, changed, "cycle restarted");
            assertEquals(EmissionCycle.Phase.WAITING, cycle.phase(), "back to WAITING");
        }));

        out.add(test("a single large tick correctly crosses multiple real phase boundaries", () -> {
            long seed = 11L;
            EmissionCycle cycle = fresh(seed, 1.0);
            double interval = expectedInterval(seed, 1.0);
            // Cross WAITING -> WARNING -> ACTIVE in one call.
            double bigDelta = interval + EmissionCycle.REAL_WARNING_SECONDS + 1.0;
            EmissionCycle.Phase changed = cycle.tick(bigDelta);
            assertEquals(EmissionCycle.Phase.ACTIVE, changed, "single tick reached ACTIVE");
            assertTrue(cycle.shelterRequired(), "shelter required after crossing into ACTIVE");
        }));

        out.add(test("emissionRateMultiplier > 1 shortens the real wait interval proportionally", () -> {
            long seed = 42L;
            double baseInterval = expectedInterval(seed, 1.0);
            double scaledInterval = expectedInterval(seed, 2.0);
            assertEquals(baseInterval / 2.0, scaledInterval, 1e-6, "interval halved by a 2x multiplier");
        }));

        out.add(test("setEmissionRateMultiplier (part 88) does not rescale a countdown already in progress", () -> {
            long seed = 123L;
            EmissionCycle cycle = fresh(seed, 1.0);
            assertEquals(1.0, cycle.emissionRateMultiplier(), 1e-9, "starts at the ServerConfig value passed to the constructor");
            double firstInterval = expectedInterval(seed, 1.0); // the interval already committed to at construction
            cycle.setEmissionRateMultiplier(5.0); // retune BEFORE that first interval even elapses
            assertEquals(5.0, cycle.emissionRateMultiplier(), 1e-9, "getter reflects the live override immediately");
            EmissionCycle.Phase stillWaiting = cycle.tick(firstInterval - 1.0);
            assertNull(stillWaiting, "not yet at the ORIGINAL (pre-retune) interval");
            EmissionCycle.Phase changed = cycle.tick(1.0);
            assertEquals(EmissionCycle.Phase.WARNING, changed,
                    "the first transition still fires on the schedule set at construction, despite the retune -- a live retune is not retroactive");
        }));

        out.add(test("setEmissionRateMultiplier (part 88) DOES apply to the next interval this cycle computes", () -> {
            long seed = 321L;
            EmissionCycle cycle = fresh(seed, 1.0);
            Random independent = new Random(seed);
            independent.nextDouble(); // consume the same draw the constructor already made, to reach the SECOND draw below
            // Drive the cycle through one full real loop back to WAITING -- retuning partway
            // through, well before WAITING is reached again, so the NEXT interval computed
            // (when the cycle returns to WAITING) is the one that should reflect the retune.
            double firstInterval = expectedInterval(seed, 1.0);
            cycle.tick(firstInterval); // -> WARNING
            cycle.setEmissionRateMultiplier(10.0);
            cycle.tick(EmissionCycle.REAL_WARNING_SECONDS); // -> ACTIVE
            cycle.tick(EmissionCycle.EMULATOR_ACTIVE_PHASE_SECONDS); // -> ARTEFACT_WINDOW
            EmissionCycle.Phase backToWaiting = cycle.tick(EmissionCycle.REAL_ARTEFACT_WINDOW_TOTAL_SECONDS); // -> WAITING, computes a fresh interval
            assertEquals(EmissionCycle.Phase.WAITING, backToWaiting, "cycle restarted");

            double secondRaw = independent.nextDouble(); // the SAME Random sequence's second draw
            double secondReal = EmissionCycle.REAL_MIN_INTERVAL_SECONDS
                    + secondRaw * (EmissionCycle.REAL_MAX_INTERVAL_SECONDS - EmissionCycle.REAL_MIN_INTERVAL_SECONDS);
            double expectedSecondInterval = secondReal / 10.0; // the retuned multiplier

            EmissionCycle.Phase stillWaiting = cycle.tick(expectedSecondInterval - 1.0);
            assertNull(stillWaiting, "not yet at the retuned second interval");
            EmissionCycle.Phase changedAgain = cycle.tick(1.0);
            assertEquals(EmissionCycle.Phase.WARNING, changedAgain,
                    "the second interval correctly used the live-retuned 10.0 multiplier");
        }));

        return out;
    }

    private static double expectedInterval(long seed, double multiplier) {
        Random r = new Random(seed);
        double real = EmissionCycle.REAL_MIN_INTERVAL_SECONDS
                + r.nextDouble() * (EmissionCycle.REAL_MAX_INTERVAL_SECONDS - EmissionCycle.REAL_MIN_INTERVAL_SECONDS);
        return real / multiplier;
    }

    private static EmissionCycle fresh(long seed, double multiplier) {
        ServerConfig config = new ServerConfig();
        config.emissionRateMultiplier = multiplier;
        return new EmissionCycle(config, new Random(seed));
    }

    /** Drives a fresh cycle forward, tick by exact real boundary, until it reaches {@code target}. */
    private static EmissionCycle atPhase(EmissionCycle.Phase target) {
        long seed = 99L;
        EmissionCycle cycle = fresh(seed, 1.0);
        if (target == EmissionCycle.Phase.WAITING) {
            return cycle;
        }
        cycle.tick(expectedInterval(seed, 1.0)); // -> WARNING
        if (target == EmissionCycle.Phase.WARNING) {
            return cycle;
        }
        cycle.tick(EmissionCycle.REAL_WARNING_SECONDS); // -> ACTIVE
        if (target == EmissionCycle.Phase.ACTIVE) {
            return cycle;
        }
        cycle.tick(EmissionCycle.EMULATOR_ACTIVE_PHASE_SECONDS); // -> ARTEFACT_WINDOW
        return cycle;
    }
}
