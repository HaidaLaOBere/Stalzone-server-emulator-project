package net.stalzone.emulator.hazards;

import java.util.Collections;
import java.util.List;

/**
 * One distance-tier block from {@code modassets/assets/stalker/detectors.txt} -- the real config
 * that drives the client's Geiger-counter-style proximity detector (radiation/"rad", chemical/
 * "thr", biological/"bio" hazard fields). As the player gets closer to a hazard source, the
 * client picks the tier whose {@link #startDistance()} is the largest value still &lt;= the
 * actual distance, and plays {@link #soundNames()} on a {@link #periodBase()}-second cycle
 * (rad/thr) or with pitch scaled between {@link #pitchMin()}/{@link #pitchMax()} (bio) -- exactly
 * the classic S.T.A.L.K.E.R.-series detector beep-faster-as-you-get-closer behavior. Confirmed
 * real by direct inspection (part 22): {@code period_base} for "rad" strictly decreases as
 * {@code start_distance} decreases (12, 8, 5, 3, 2, 1 seconds for distances 5..0) -- closer really
 * does mean faster beeping in the source data itself, not just by assumption.
 */
public final class DetectorTier {

    /** "rad", "thr" (chemical/thermal), or "bio" -- the three hazard types this file defines. */
    public final String type;
    public final int startDistance;
    private final List<String> soundNames;
    public final double periodBase;
    public final double periodRandom;
    /** Only non-null for "bio" tiers -- the only type with pitch fields in the source file. */
    public final Double pitchMin;
    public final Double pitchMax;

    DetectorTier(String type, int startDistance, List<String> soundNames, double periodBase,
                 double periodRandom, Double pitchMin, Double pitchMax) {
        this.type = type;
        this.startDistance = startDistance;
        this.soundNames = soundNames;
        this.periodBase = periodBase;
        this.periodRandom = periodRandom;
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
    }

    /** The raw {@code sound_names} value split on {@code @} (the source file's own separator, e.g. "geiger_1_@geiger_2_@..."). */
    public List<String> soundNames() {
        return Collections.unmodifiableList(soundNames);
    }

    @Override
    public String toString() {
        return "DetectorTier{" + type + " dist=" + startDistance + " periodBase=" + periodBase
                + (pitchMin != null ? " pitch=[" + pitchMin + "," + pitchMax + "]" : "") + "}";
    }
}
