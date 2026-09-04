package net.stalzone.emulator.session.save;

import java.util.Map;

/**
 * One entry from a real per-character {@code common} save file's top-level {@code
 * lastOpsDifficultyByMap} list (part 47) -- see {@link PlayerSaveFile} for where this lives.
 * Real schema, confirmed from a real character's real 2-entry sample: {@code {id: "big_cleanup",
 * difficulty: 1}}, {@code {id: "sea_alienation", difficulty: 1}} -- both real internal Operations
 * map identifiers this project hadn't seen documented elsewhere (a third real character's sample
 * had zero entries, i.e. no Operations played yet -- an empty real list, not evidence against the
 * schema). {@code id} is a real NBT {@code TAG_STRING}, {@code difficulty} a real {@code
 * TAG_INT}; only the value {@code 1} has been observed for {@code difficulty} so far, so this
 * project has no evidence for what other difficulty levels exist or what {@code 1} itself means
 * (a raw int, not a guessed enum, per rule 1).
 */
public final class OpsMapDifficulty {

    private final Map<String, Object> fields;

    OpsMapDifficulty(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> rawFields() {
        return fields;
    }

    /** Real internal Operations map id, e.g. {@code big_cleanup}, {@code sea_alienation}. */
    public String mapId() {
        Object v = fields.get("id");
        return (v instanceof String) ? (String) v : null;
    }

    public int difficulty() {
        Object v = fields.get("difficulty");
        return (v instanceof Integer) ? (Integer) v : 0;
    }

    @Override
    public String toString() {
        return "OpsMapDifficulty{mapId=" + mapId() + ", difficulty=" + difficulty() + "}";
    }
}
