package net.stalzone.emulator.audio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Loads the client's sound-duration CSVs -- plain {@code <resourceKey>,<durationMs>} lines, no
 * header. Two real files ship this shape (part 25): {@code bundle/sound_duration.csv} (1,481
 * unique keys: sound effects, ambience, the 15-clip in-game guitar minigame, faction radio
 * chatter, English battle-royale announcer lines -- e.g. `en_landing01`..`en_landing15`,
 * `en_airdrop01`..`en_airdrop05`, `en_evacuation`, confirming a BR match structure with numbered
 * landing zones and airdrops) and {@code bundle/speech_duration.csv} (7,447 unique keys: named
 * NPC dialogue lines, e.g. `steven.sounds.npc.14 new npc.zarya.01_common.idle...`). The two
 * files' key spaces are disjoint (checked: 0 overlap) -- they're independent registries, loaded
 * separately via {@link #load(Path)}, not one combined table.
 *
 * <p>This is asset-timing metadata rather than a "spawn rate"/"damage value" style tunable, but
 * genuinely useful for anything that needs to know how long a scripted audio cue takes -- e.g.
 * not letting a player interrupt a radio broadcast, or timing a quest-dialogue sequence
 * server-side. Also useful simply as a source of real, previously-uncatalogued content: this
 * file revealed a named mission this project hadn't found before, {@code operation_lesta} (5
 * audio-diary parts: intervention/main_caliber/mine/rainbow/water_area), alongside the
 * already-known {@code operation_kutuzov} (part 11).
 *
 * <p><b>Real, disclosed quirk, not a bug</b>: {@code sound_duration.csv} has 15 keys
 * (`guitar_1`..`guitar_15`) that each appear TWICE in the raw file -- confirmed harmless: both
 * occurrences carry the exact same duration value every time, not a genuine conflict. {@link
 * #load(Path)} tolerates an exact-duplicate re-declaration silently, but throws if a key is
 * re-declared with a DIFFERENT value (that would be a genuine ambiguity worth surfacing, the same
 * "disclose an ambiguity, don't hide it" standard as {@code ItemWeightTable}/{@code ItemGroups}
 * -- it just happens that this file's only duplicates are non-conflicting).
 */
public final class SoundDurations {

    private static final Pattern LINE = Pattern.compile("^([^,]+),(\\d+)$");

    private final Map<String, Integer> durationMsByKey;

    private SoundDurations(Map<String, Integer> durationMsByKey) {
        this.durationMsByKey = durationMsByKey;
    }

    public static SoundDurations load(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        Map<String, Integer> durationMsByKey = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            var m = LINE.matcher(line);
            if (!m.matches()) {
                throw new IOException(csvPath + ": unparseable line: \"" + line + "\"");
            }
            String key = m.group(1);
            int durationMs = Integer.parseInt(m.group(2));

            Integer existing = durationMsByKey.get(key);
            if (existing != null && !existing.equals(durationMs)) {
                throw new IOException(csvPath + ": key \"" + key + "\" re-declared with a different "
                        + "duration (" + existing + "ms then " + durationMs + "ms) -- a genuine conflict, "
                        + "not the harmless exact-duplicate pattern this file normally has");
            }
            durationMsByKey.put(key, durationMs);
        }

        return new SoundDurations(durationMsByKey);
    }

    public int size() {
        return durationMsByKey.size();
    }

    /** This resource key's duration in milliseconds, or null if it isn't in the table. */
    public Integer durationMs(String key) {
        return durationMsByKey.get(key);
    }

    public List<String> keys() {
        return new ArrayList<>(durationMsByKey.keySet());
    }

    /** Every key whose name contains this substring (case-sensitive), in file order -- a convenience for exploring the table, e.g. {@code keysContaining("operation_lesta")}. */
    public List<String> keysContaining(String substring) {
        List<String> matches = new ArrayList<>();
        for (String key : durationMsByKey.keySet()) {
            if (key.contains(substring)) {
                matches.add(key);
            }
        }
        return matches;
    }
}
