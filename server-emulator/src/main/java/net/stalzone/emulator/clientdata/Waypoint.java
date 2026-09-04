package net.stalzone.emulator.clientdata;

import java.util.Map;

/**
 * One entry from the real local client-state file {@code config/waypoints.cfg} -- a plain JSON
 * array, sitting in the same real {@code config/} directory the per-character save files (part
 * 40) and {@link QuestProgress} come from. Real shape, confirmed from a real, live install:
 * <pre>
 *   {"pos": {"x": &lt;float&gt;, "y": &lt;float&gt;, "z": &lt;float&gt;}, "name": "",
 *    "icon_index": 0, "color": -1, "can_position_float": false,
 *    "time": &lt;epoch millis&gt;, "type": "death"}
 * </pre>
 *
 * <p><b>Only ONE real {@code type} value has been observed: {@code "death"}</b> (two separate
 * real occurrences, different sessions, both auto-generated where the account's character last
 * died). {@code name} was empty (`""`) both times. Whether player-PLACED custom waypoints use
 * this same file/schema with a different {@code type} is NOT evidenced either way -- this class
 * does not assume {@code type} is a closed enum, {@link #type()} stays a raw string.
 * {@code color} is a packed real value (real observed value {@code -1}, i.e. all bits set) --
 * consistent with a standard ARGB int packed into a signed 32-bit field (matching the
 * {@code headerColor: 16777215} pattern already seen in real {@code sd2} notification payloads,
 * part 40), but this project has no evidence confirming the exact channel layout, so this class
 * exposes the raw signed int rather than decoding it into r/g/b/a.
 *
 * <p><b>Confirmed to be a transient, actively-rewritten file, not a durable log</b>: on a real,
 * live install, the SAME real death-waypoint that was present in {@code waypoints.cfg} in an
 * earlier session had, by a later session, moved to {@code waypoints.cfg_old} (with a fresh,
 * later {@code time} and different coordinates -- i.e. the account's character died again in the
 * meantime) while the live {@code waypoints.cfg} itself was back down to an empty {@code []} --
 * the same current/{@code _old}-backup rotation pattern already confirmed for the per-character
 * save files (part 40), applied here to a top-level {@code config/} file too.
 */
public final class Waypoint {

    private final double x;
    private final double y;
    private final double z;
    private final String name;
    private final int iconIndex;
    private final int color;
    private final boolean canPositionFloat;
    private final long timeMillis;
    private final String type;

    Waypoint(double x, double y, double z, String name, int iconIndex, int color,
             boolean canPositionFloat, long timeMillis, String type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.iconIndex = iconIndex;
        this.color = color;
        this.canPositionFloat = canPositionFloat;
        this.timeMillis = timeMillis;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    static Waypoint fromRaw(Map<String, Object> raw) {
        Map<String, Object> pos = raw.get("pos") instanceof Map ? (Map<String, Object>) raw.get("pos") : Map.of();
        return new Waypoint(
                doubleOf(pos.get("x")), doubleOf(pos.get("y")), doubleOf(pos.get("z")),
                raw.get("name") instanceof String ? (String) raw.get("name") : "",
                (int) doubleOf(raw.get("icon_index")),
                (int) doubleOf(raw.get("color")),
                Boolean.TRUE.equals(raw.get("can_position_float")),
                (long) doubleOf(raw.get("time")),
                raw.get("type") instanceof String ? (String) raw.get("type") : null);
    }

    private static double doubleOf(Object v) {
        return v instanceof Double ? (Double) v : 0.0;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public String name() { return name; }
    public int iconIndex() { return iconIndex; }

    /** Packed real color value, real observed value {@code -1} -- see class doc for why this isn't decoded into channels. */
    public int color() { return color; }

    public boolean canPositionFloat() { return canPositionFloat; }
    public long timeMillis() { return timeMillis; }

    /** Raw string, only {@code "death"} confirmed real so far -- see class doc. */
    public String type() { return type; }

    @Override
    public String toString() {
        return String.format("Waypoint{%s @ (%.1f, %.1f, %.1f), t=%d}", type, x, y, z, timeMillis);
    }
}
