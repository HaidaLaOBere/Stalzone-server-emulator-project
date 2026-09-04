package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Loads the real local client-state file {@code config/options.json} -- plain JSON, a flat object
 * of client graphics/audio/UI/input settings. 159 real top-level keys confirmed on a real, live
 * install (booleans, strings, and numbers -- JSON doesn't distinguish int/float, so every number
 * comes back as {@code double} via {@link #getDouble}/{@link #getInt}). Same current/{@code _old}
 * rotation pattern already confirmed for the per-character save files and for {@code
 * config/quests.cfg}/{@code config/waypoints.cfg} applies to this file too (both copies were
 * byte-identical at the time this class was validated, which is the expected state between writes,
 * not evidence against the rotation pattern -- see {@link QuestProgress}/{@link WaypointList} for
 * where a real mid-session divergence between the two copies WAS observed).
 *
 * <p><b>Real, byte-level-confirmed finding, not previously documented anywhere in this project</b>:
 * four real keys hold values many orders of magnitude larger than every other numeric field in the
 * file ({@code admin_hotbar_scale=1075838976}, {@code admin_inv_icon_scale=1065353216}, {@code
 * world_outliner_width=1073741824}, {@code world_outliner_color=1711276032}). Reinterpreting each
 * as raw big-endian bytes shows:
 * <ul>
 *   <li>{@code admin_hotbar_scale} = {@code 0x40200000} = the exact IEEE-754 binary32 bit pattern
 *       of {@code 2.5f}</li>
 *   <li>{@code admin_inv_icon_scale} = {@code 0x3f800000} = the exact IEEE-754 binary32 bit pattern
 *       of {@code 1.0f} (a universally-known constant -- this is not a coincidental match)</li>
 *   <li>{@code world_outliner_width} = {@code 0x40000000} = the exact IEEE-754 binary32 bit pattern
 *       of {@code 2.0f}</li>
 * </ul>
 * All three decode to small, round, plausible UI-scale multipliers (1x/2x/2.5x), strong evidence
 * this is a real float-as-raw-int32-bits serialization used for these specific fields (see {@link
 * #getFloatBits}). {@code world_outliner_color}, by contrast, does NOT decode to a clean float
 * ({@code 0x66000000} -&gt; ~1.5e23) but its bytes read as a plausible packed ARGB color instead:
 * alpha byte {@code 0x66} (102/255, i.e. a semi-transparent overlay) over RGB {@code 000000}
 * (black) -- see {@link #getArgbComponents}. <b>This is confirmed only for these 4 real keys on
 * this real file, not asserted as a general rule for every large-int option value</b>, per rule 1 --
 * {@link #getFloatBits} and {@link #getArgbComponents} are generic bit-reinterpret utilities a
 * caller can apply to any key, they don't themselves claim any other key uses either encoding.
 *
 * <p>This class only reads and exposes these real values -- it does not validate, clamp, or apply
 * any of them to anything; there is no rendering/audio engine in this project for a graphics or
 * volume setting to affect.
 */
public final class ClientOptions {

    private final Map<String, Object> values;

    private ClientOptions(Map<String, Object> values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public static ClientOptions load(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof Map)) {
            throw new IOException(path + ": expected a top-level JSON object, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }
        return new ClientOptions(new LinkedHashMap<>((Map<String, Object>) parsed));
    }

    public int size() {
        return values.size();
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    /** Every real option key in this file, exactly as it appears in the JSON, sorted. */
    public Set<String> keys() {
        return Collections.unmodifiableSet(new TreeSet<>(values.keySet()));
    }

    /** The raw parsed value (Double / Boolean / String / null) for a key, or null if absent. */
    public Object getRaw(String key) {
        return values.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object v = values.get(key);
        return (v instanceof Boolean) ? (Boolean) v : defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        Object v = values.get(key);
        return (v instanceof Double) ? (Double) v : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        Object v = values.get(key);
        return (v instanceof Double) ? ((Double) v).intValue() : defaultValue;
    }

    public String getString(String key, String defaultValue) {
        Object v = values.get(key);
        return (v instanceof String) ? (String) v : defaultValue;
    }

    /**
     * Reinterprets a numeric key's raw value as the raw bit pattern of an IEEE-754 binary32 float
     * (i.e. {@code Float.intBitsToFloat}, not a numeric cast). Confirmed correct for the real keys
     * {@code admin_hotbar_scale}, {@code admin_inv_icon_scale}, and {@code world_outliner_width} --
     * see class doc. Returns {@code NaN} if the key is absent or not numeric.
     */
    public float getFloatBits(String key) {
        Object v = values.get(key);
        if (!(v instanceof Double)) {
            return Float.NaN;
        }
        int bits = ((Double) v).intValue();
        return Float.intBitsToFloat(bits);
    }

    /**
     * Reinterprets a numeric key's raw value as a packed 32-bit ARGB color and returns {@code
     * {alpha, red, green, blue}}, each 0-255. Confirmed plausible for the real key {@code
     * world_outliner_color} (alpha=102, black RGB) -- see class doc. Returns all zeros if the key
     * is absent or not numeric.
     */
    public int[] getArgbComponents(String key) {
        Object v = values.get(key);
        if (!(v instanceof Double)) {
            return new int[] {0, 0, 0, 0};
        }
        int bits = ((Double) v).intValue();
        int a = (bits >>> 24) & 0xFF;
        int r = (bits >>> 16) & 0xFF;
        int g = (bits >>> 8) & 0xFF;
        int b = bits & 0xFF;
        return new int[] {a, r, g, b};
    }

    @Override
    public String toString() {
        return "ClientOptions{" + values.size() + " keys}";
    }
}
