package net.stalzone.emulator.mobs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One mob/mutant AI config, parsed from a real {@code modassets/assets/stalkermobs/configs/*.smm}
 * file (confirmed plain JSON -- see docs/RESEARCH_LOG.md part 10, re-verified part 20). Unlike
 * {@code WeaponEntry}, these files carry NO combat numbers (no health, no attack damage) -- each
 * attack's {@code effector} block is present but empty in every file checked, and there is no
 * health/HP field anywhere in the schema. That is a real absence, not a gap in this reader: per
 * this project's evidence rule, this class exposes exactly the fields the files actually contain
 * (behavior flags, faction relations, attack timing/animation, sounds) and does NOT invent a
 * damage or health accessor to mirror WeaponEntry's shape. If real combat-stat data for mutants
 * ever surfaces (the way weapon damage did via a community site, see part 12/17), it belongs in
 * its own accessor added then, backed by its own evidence.
 *
 * What IS here is genuinely useful for "full control over what can be done": per-mob behavior
 * flags (canWalk, canRunAttack, bodyEater, ...) and faction relations (mutantRelations) are
 * exactly the kind of thing a server operator would want to retune -- e.g. making a normally
 * neutral mutant hostile, or disabling a body-eating behavior -- without needing any combat math
 * this reader doesn't have evidence for.
 */
public final class MobConfig {

    private final String configId;
    private final Map<String, Object> fields;

    MobConfig(String configId, Map<String, Object> fields) {
        this.configId = configId;
        this.fields = fields;
    }

    public Map<String, Object> rawFields() {
        return fields;
    }

    /**
     * The source file's stem (e.g. {@code "base_boar"}, {@code "base_boar_clone"}) -- the only
     * reliably unique identifier across this config set. {@code mobName()} looks like it should
     * be one, but it is NOT unique: confirmed by loading all 35 real files, {@code base_boar.smm}
     * and {@code base_boar_clone.smm} both report {@code mobName="boar"}; all three of
     * {@code base_tick.smm}/{@code base_tushkan.smm}/{@code base_tushkan_clone.smm} report
     * {@code mobName="tushkan"}; {@code base_drone.smm} reports {@code mobName="spirit"}, the
     * same value {@code base_spirit.smm} reports. Some of this is deliberate reuse (the
     * {@code base_tsarevich.smm}/{@code base_tsarevna.smm} boss-event configs report
     * {@code mobName="boar_tsarevich"}/{@code "flesh_tsarevna"} -- named variants built on the
     * boar/flesh behavior base, not copy-paste). Use {@code configId} when you need one config
     * per real file; use {@code mobName()} only when you specifically want the (possibly shared)
     * gameplay-facing name.
     */
    public String configId() {
        return configId;
    }

    public String mobName() {
        return stringField("mobName");
    }

    public boolean canWalk() {
        return boolField("canWalk");
    }

    public boolean canRest() {
        return boolField("canRest");
    }

    public boolean canSuddenTurn() {
        return boolField("canSuddenTurn");
    }

    public boolean canRunAttack() {
        return boolField("canRunAttack");
    }

    public boolean hasMovementTurnAnimations() {
        return boolField("hasMovementTurnAnimations");
    }

    /** Whether this mutant eats corpses (a real, named behavior flag in the source file). */
    public boolean bodyEater() {
        return boolField("bodyEater");
    }

    public boolean playIdleSound() {
        return boolField("playIdleSound");
    }

    public boolean playStepSounds() {
        return boolField("playStepSounds");
    }

    public Double idleSoundDelayMin() {
        return doubleField("idleSoundDelayMin");
    }

    public Double idleSoundDelayMax() {
        return doubleField("idleSoundDelayMax");
    }

    public Double maxSoundSources() {
        return doubleField("maxSoundSources");
    }

    /**
     * This mutant's stance toward every other mutant type named in its file, e.g.
     * {@code "controller" -> "ENEMY"}, {@code "boar" -> "ALLY"}. Not every mutant type appears
     * in every file's relation table (e.g. a file may simply omit a type it never reacts to
     * specially) -- callers should treat a missing key as "no special relation defined", not as
     * a defaulted value this reader is choosing.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> mutantRelations() {
        Object raw = fields.get("mutantRelations");
        if (!(raw instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : ((Map<String, Object>) raw).entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map) {
                Object rel = ((Map<String, Object>) v).get("relation");
                if (rel instanceof String) {
                    out.put(e.getKey(), (String) rel);
                }
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Raw {@code _attacks} block: attack name (e.g. {@code "attack#run"}) -> its fields
     * (timeOfAttack, rotationLimit, animationName, movementType, impulse*, effector). Returned
     * raw rather than wrapped in typed accessors because {@code effector} is an opaque, always-
     * empty object in every file checked -- there is nothing concrete yet to type it as.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> attacks() {
        Object raw = fields.get("_attacks");
        return raw instanceof Map ? (Map<String, Object>) raw : Collections.emptyMap();
    }

    /** Event name (e.g. "pain", "attack", "death") -> sound resource key, e.g. "stalkermobs:boar.pain". */
    @SuppressWarnings("unchecked")
    public Map<String, String> sounds() {
        Object raw = fields.get("_sounds");
        if (!(raw instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : ((Map<String, Object>) raw).entrySet()) {
            if (e.getValue() instanceof String) {
                out.put(e.getKey(), (String) e.getValue());
            }
        }
        return Collections.unmodifiableMap(out);
    }

    private String stringField(String key) {
        Object v = fields.get(key);
        return v instanceof String ? (String) v : null;
    }

    private Double doubleField(String key) {
        Object v = fields.get(key);
        return v instanceof Double ? (Double) v : null;
    }

    private boolean boolField(String key) {
        Object v = fields.get(key);
        return v instanceof Boolean && (Boolean) v;
    }

    @Override
    public String toString() {
        return "MobConfig{" + configId + ", mobName=" + mobName() + "}";
    }
}
