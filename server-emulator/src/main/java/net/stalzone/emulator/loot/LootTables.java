package net.stalzone.emulator.loot;

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
 * Loads a plain, operator/modder-editable JSON file of {@link LootTable}s keyed by real mob
 * {@code configId} (see {@code mobs/MobConfig#configId()} -- e.g. {@code "base_boar"}). This is
 * emulator-original config, same status as {@code data/weapons/weapons.json}'s tuning fields --
 * see {@link LootTable}'s class doc for why no such file exists as real STALZONE data to load
 * instead. File shape:
 * <pre>
 * [
 *   {
 *     "mobId": "base_boar",
 *     "dropChance": 0.35,
 *     "entries": [
 *       {"itemId": "some_item_id", "minCount": 1, "maxCount": 2, "weight": 3.0},
 *       {"itemId": "another_item_id", "minCount": 1, "maxCount": 1, "weight": 1.0}
 *     ]
 *   }
 * ]
 * </pre>
 * A mob id with no matching entry in the file simply has no configured drops -- {@link
 * #forMob(String)} returns {@code null} rather than a table with zero entries, so a caller can
 * tell "not configured" apart from "configured to always drop nothing" (an empty {@code entries}
 * list with {@code dropChance} > 0 would be a config error, not represented specially here).
 */
public final class LootTables {

    private final Map<String, LootTable> byMobId;

    private LootTables(Map<String, LootTable> byMobId) {
        this.byMobId = byMobId;
    }

    @SuppressWarnings("unchecked")
    public static LootTables load(Path jsonPath) throws IOException {
        String text = Files.readString(jsonPath, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof List)) {
            throw new IOException(jsonPath + ": expected a top-level JSON array, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }

        Map<String, LootTable> out = new LinkedHashMap<>();
        for (Object entryObj : (List<Object>) parsed) {
            if (!(entryObj instanceof Map)) {
                continue;
            }
            Map<String, Object> raw = (Map<String, Object>) entryObj;
            String mobId = stringOf(raw.get("mobId"));
            if (mobId == null) {
                throw new IOException(jsonPath + ": a loot table entry is missing \"mobId\"");
            }
            double dropChance = doubleOf(raw.get("dropChance"), 0.0);

            List<LootEntry> entries = new ArrayList<>();
            Object entriesRaw = raw.get("entries");
            if (entriesRaw instanceof List) {
                for (Object e : (List<Object>) entriesRaw) {
                    if (!(e instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> em = (Map<String, Object>) e;
                    String itemId = stringOf(em.get("itemId"));
                    if (itemId == null) {
                        throw new IOException(jsonPath + ": a loot entry for mobId \"" + mobId + "\" is missing \"itemId\"");
                    }
                    int minCount = (int) doubleOf(em.get("minCount"), 1.0);
                    int maxCount = (int) doubleOf(em.get("maxCount"), minCount);
                    double weight = doubleOf(em.get("weight"), 1.0);
                    entries.add(new LootEntry(itemId, minCount, maxCount, weight));
                }
            }
            out.put(mobId, new LootTable(dropChance, entries));
        }
        return new LootTables(out);
    }

    private static String stringOf(Object v) {
        return (v instanceof String) ? (String) v : null;
    }

    private static double doubleOf(Object v, double defaultValue) {
        return (v instanceof Double) ? (Double) v : defaultValue;
    }

    public int size() {
        return byMobId.size();
    }

    public Map<String, LootTable> all() {
        return Collections.unmodifiableMap(byMobId);
    }

    /** The configured table for a real mob {@code configId}, or {@code null} if none is configured. */
    public LootTable forMob(String mobConfigId) {
        return byMobId.get(mobConfigId);
    }
}
