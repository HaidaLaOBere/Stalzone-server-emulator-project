package net.stalzone.emulator.items;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads data/weapons/weapons.json (337 real weapon records, see that file's own README for
 * provenance) into memory and applies this server's {@link ServerConfig#damageMultiplier} to
 * it. This is the first place in the emulator where the project's headline promise -- "full
 * control over what can be done, like ... damage values" -- is something you can actually run
 * and see change a number, rather than a config field sitting unused (see the comment at the
 * top of ServerConfig.java, which this directly answers).
 *
 * What this is NOT: nothing in the emulator's (nonexistent) game loop calls into this yet --
 * there's no live session that fires a shot and asks this class what damage to apply. This is
 * the data-and-tuning layer that such a system would call, built and validated ahead of having
 * that system, the same way WorldModel was built ahead of having a game loop to place a player
 * in it (see docs/RESEARCH_LOG.md part 18).
 */
public final class WeaponRegistry {

    private final List<WeaponEntry> all;
    private final Map<String, WeaponEntry> byId;
    private final Map<String, WeaponEntry> byLowercaseName;

    private WeaponRegistry(List<WeaponEntry> all, Map<String, WeaponEntry> byId, Map<String, WeaponEntry> byLowercaseName) {
        this.all = all;
        this.byId = byId;
        this.byLowercaseName = byLowercaseName;
    }

    @SuppressWarnings("unchecked")
    public static WeaponRegistry load(Path weaponsJsonPath) throws IOException {
        String text = Files.readString(weaponsJsonPath, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);

        if (!(parsed instanceof List)) {
            throw new IOException(weaponsJsonPath + ": expected a top-level JSON array, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }

        List<Object> rawList = (List<Object>) parsed;
        List<WeaponEntry> all = new ArrayList<>(rawList.size());
        Map<String, WeaponEntry> byId = new LinkedHashMap<>();
        Map<String, WeaponEntry> byLowercaseName = new LinkedHashMap<>();

        for (Object item : rawList) {
            if (!(item instanceof Map)) {
                continue; // skip anything malformed rather than fail the whole load
            }
            WeaponEntry entry = new WeaponEntry((Map<String, Object>) item);
            all.add(entry);
            if (entry.id() != null) {
                byId.put(entry.id(), entry);
            }
            if (entry.name() != null) {
                byLowercaseName.putIfAbsent(entry.name().toLowerCase(), entry);
            }
        }

        return new WeaponRegistry(all, byId, byLowercaseName);
    }

    public int size() {
        return all.size();
    }

    public List<WeaponEntry> all() {
        return Collections.unmodifiableList(all);
    }

    public WeaponEntry byId(String id) {
        return byId.get(id);
    }

    /** Case-insensitive exact name lookup, e.g. "PM" or "ak-12". Returns the first match if a
     *  name is somehow shared by more than one record (not currently known to happen). */
    public WeaponEntry byName(String name) {
        return byLowercaseName.get(name.toLowerCase());
    }

    /**
     * A firearm's base damage-per-hit with this server's {@code damageMultiplier} applied, or
     * null if the entry has no firearm damage field (melee weapons and devices -- use
     * {@link #effectiveMeleeDamage} for melee).
     */
    public Double effectiveDamage(WeaponEntry entry, ServerConfig config) {
        Double base = entry.damage();
        if (base == null) {
            return null;
        }
        return base * config.damageMultiplier;
    }

    /** A melee weapon's [min, max] light-attack damage range with the multiplier applied, or null if not melee. */
    public double[] effectiveMeleeCommonDamage(WeaponEntry entry, ServerConfig config) {
        Double min = entry.meleeDamageMinCommon();
        Double max = entry.meleeDamageMaxCommon();
        if (min == null || max == null) {
            return null;
        }
        return new double[] { min * config.damageMultiplier, max * config.damageMultiplier };
    }

    /** A melee weapon's [min, max] heavy/"strong"-attack damage range with the multiplier applied, or null if not melee. */
    public double[] effectiveMeleeStrongDamage(WeaponEntry entry, ServerConfig config) {
        Double min = entry.meleeDamageMinStrong();
        Double max = entry.meleeDamageMaxStrong();
        if (min == null || max == null) {
            return null;
        }
        return new double[] { min * config.damageMultiplier, max * config.damageMultiplier };
    }

    /**
     * A firearm's effective damage on a headshot: {@link #effectiveDamage} multiplied by the
     * entry's real {@link WeaponEntry#headshotMultiplier()}. Null if either the entry has no
     * firearm damage or no headshot multiplier (275 of 337 records have one -- see
     * {@code WeaponEntry}'s doc). This is a plain multiplication of two real, evidenced numbers,
     * not an invented combat formula.
     */
    public Double effectiveHeadshotDamage(WeaponEntry entry, ServerConfig config) {
        Double base = effectiveDamage(entry, config);
        Double multiplier = entry.headshotMultiplier();
        if (base == null || multiplier == null) {
            return null;
        }
        return base * multiplier;
    }

    /** Same as {@link #effectiveHeadshotDamage} but for a limb hit, using {@link WeaponEntry#limbMultiplier()}. */
    public Double effectiveLimbDamage(WeaponEntry entry, ServerConfig config) {
        Double base = effectiveDamage(entry, config);
        Double multiplier = entry.limbMultiplier();
        if (base == null || multiplier == null) {
            return null;
        }
        return base * multiplier;
    }

    /**
     * {@link WeaponEntry#damageAtDistanceLinearAssumption} with this server's
     * {@code damageMultiplier} applied -- same disclosed linear-ramp assumption as that method
     * (see its javadoc and part 39 for what is and isn't officially confirmed). Null if the
     * entry has no falloff curve.
     */
    public Double effectiveDamageAtDistance(WeaponEntry entry, double distanceMeters, ServerConfig config) {
        Double base = entry.damageAtDistanceLinearAssumption(distanceMeters);
        if (base == null) {
            return null;
        }
        return base * config.damageMultiplier;
    }
}
