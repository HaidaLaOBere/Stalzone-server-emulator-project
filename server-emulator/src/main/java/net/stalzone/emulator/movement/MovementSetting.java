package net.stalzone.emulator.movement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One parsed line from {@code modassets/assets/stalcraft/moving.txt} -- a key, an optional
 * "base" value, and zero or more per-difficulty-variant overrides (see {@link MovementConfig}'s
 * class doc comment for the file format and the 4 real variant letters found: c/e/h/m).
 *
 * <p>A setting can genuinely have no base value at all -- {@code move.config.key.name} is the
 * one real example (its raw value is just {@code e:Easy;h:Hard;m:Medium}, a pure variant-name
 * lookup with nothing before the first variant pair). {@link #baseValue()} returns null for
 * that case, distinct from a setting whose base value is the empty string (several real lines,
 * e.g. {@code move.server.config.survival.user.keys:}, have exactly that -- an intentional
 * empty default, not a missing one).
 */
public final class MovementSetting {

    public final String key;
    private final String baseValue; // null only when the line had no plain token before its variant pairs
    private final Map<Character, String> variants;

    MovementSetting(String key, String baseValue, Map<Character, String> variants) {
        this.key = key;
        this.baseValue = baseValue;
        this.variants = Collections.unmodifiableMap(new LinkedHashMap<>(variants));
    }

    /** The plain (non-variant) value, or null if this line only ever defined variant overrides (see class doc). */
    public String baseValue() {
        return baseValue;
    }

    /** This difficulty/mode variant's override letter -&gt; value pairs, e.g. {"c":"true"}. Empty if this setting has no variants. */
    public Map<Character, String> variants() {
        return variants;
    }

    public boolean hasVariant(char letter) {
        return variants.containsKey(letter);
    }

    /** The value that applies for this variant letter: the variant override if one exists, else {@link #baseValue()} (which may itself be null). */
    public String valueFor(char variant) {
        String override = variants.get(variant);
        return override != null ? override : baseValue;
    }

    public Double asDouble() {
        return parseDouble(baseValue);
    }

    public Double asDouble(char variant) {
        return parseDouble(valueFor(variant));
    }

    public Boolean asBoolean() {
        return parseBoolean(baseValue);
    }

    public Boolean asBoolean(char variant) {
        return parseBoolean(valueFor(variant));
    }

    private static Double parseDouble(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolean(String raw) {
        if ("true".equals(raw)) {
            return Boolean.TRUE;
        }
        if ("false".equals(raw)) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public String toString() {
        return "MovementSetting{" + key + "=" + baseValue + (variants.isEmpty() ? "" : " " + variants) + "}";
    }
}
