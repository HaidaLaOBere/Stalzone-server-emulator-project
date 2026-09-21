package net.stalzone.emulator.mobs;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads a plain, operator/modder-editable JSON file of per-mob {@code maxHealth} overrides,
 * keyed by real mob {@code configId} (see {@link MobConfig#configId()} -- e.g.
 * {@code "base_boar"}). Same status and same reason for existing as {@code loot/LootTables}: real
 * STALZONE {@code .smm} mob config files carry NO health/HP field anywhere in their schema --
 * {@link MobConfig}'s own class doc already confirms this absence across all 35 real files. This
 * class does not change that: it is purely this emulator's own opt-in tuning layer on top of
 * {@link MobInstance#DEFAULT_MAX_HEALTH}, exactly the "full control over ... damage values" knob
 * the project exists to expose -- a per-mob health value is not a claim about what the real game
 * actually uses, and a mob id with no entry here simply falls back to
 * {@link MobInstance#DEFAULT_MAX_HEALTH} (see {@link #maxHealthFor(String)}).
 *
 * <p>File shape:
 * <pre>
 * {
 *   "base_boar": 80.0,
 *   "base_tushkan": 40.0
 * }
 * </pre>
 * A flat JSON object of {@code configId -> maxHealth}, deliberately simpler than {@link
 * net.stalzone.emulator.loot.LootTable}'s shape since there is nothing else to configure per mob
 * here yet.
 */
public final class MobHealthTable {

    private final Map<String, Double> byConfigId;

    private MobHealthTable(Map<String, Double> byConfigId) {
        this.byConfigId = byConfigId;
    }

    @SuppressWarnings("unchecked")
    public static MobHealthTable load(Path jsonPath) throws IOException {
        String text = Files.readString(jsonPath, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof Map)) {
            throw new IOException(jsonPath + ": expected a top-level JSON object, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }

        Map<String, Double> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) parsed).entrySet()) {
            Object v = entry.getValue();
            if (!(v instanceof Double)) {
                throw new IOException(jsonPath + ": maxHealth for \"" + entry.getKey()
                        + "\" must be a number, got " + (v == null ? "null" : v.getClass().getSimpleName()));
            }
            double maxHealth = (Double) v;
            if (maxHealth <= 0) {
                throw new IOException(jsonPath + ": maxHealth for \"" + entry.getKey()
                        + "\" must be > 0, got " + maxHealth);
            }
            out.put(entry.getKey(), maxHealth);
        }
        return new MobHealthTable(out);
    }

    public int size() {
        return byConfigId.size();
    }

    public Map<String, Double> all() {
        return Collections.unmodifiableMap(byConfigId);
    }

    /**
     * The configured {@code maxHealth} for a real mob {@code configId}, or
     * {@link MobInstance#DEFAULT_MAX_HEALTH} if none is configured -- so a caller can use this
     * unconditionally without a null check.
     */
    public double maxHealthFor(String mobConfigId) {
        Double v = byConfigId.get(mobConfigId);
        return v != null ? v : MobInstance.DEFAULT_MAX_HEALTH;
    }

    /**
     * Real mob ids (from {@link MobRegistry#all()}) that have no entry in this table, for a
     * dump/audit tool. Uses {@code Collectors.toList()} rather than {@code Stream.toList()}
     * (Java 16+) so this class stays compilable at {@code --release 11}, matching every other
     * class in this project -- a maintenance fix, not a behavior change (part 82's device-side
     * verification run surfaced this as the one thing blocking a full-project {@code --release 11}
     * device build; every individual part's own files had always compiled fine at 11, this was
     * simply never exercised as part of a whole-project 11 build until then).
     */
    public List<String> unconfigured(List<String> allMobConfigIds) {
        return allMobConfigIds.stream().filter(id -> !byConfigId.containsKey(id)).collect(Collectors.toList());
    }
}
