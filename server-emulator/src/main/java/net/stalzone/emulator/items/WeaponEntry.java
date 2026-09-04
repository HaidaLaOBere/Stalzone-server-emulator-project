package net.stalzone.emulator.items;

import java.util.Map;

/**
 * One weapon record from data/weapons/weapons.json, kept as its raw parsed JSON fields
 * (a Map, from JsonReader) plus typed convenience accessors for the fields tuning logic
 * actually needs. Deliberately NOT a fixed-field DTO: the dataset has two different
 * provenances (stalcraftdb.net / stalzone.wiki, see data/weapons/README.md) with slightly
 * different field coverage per weapon class (firearms have "damage"; melee weapons have
 * "meleeDamageMinCommon" etc. instead; devices have neither) -- a rigid DTO would either
 * lose fields or need nullable getters for everything anyway, so this just wraps the map
 * and exposes typed lookups for the fields callers actually use.
 *
 * <p><b>Real data-format quirk, found while wiring combat (part 30):</b> a cluster of fields
 * that read as plain numbers -- {@code headshotMultiplier}, {@code limbMultiplier},
 * {@code overheatAfterShots}, {@code meleeCriticalHitMultiplier},
 * {@code meleeMutantDamageMultiplier} -- are, consistently across every one of the 337 real
 * records that has them, encoded as JSON STRINGS (e.g. {@code "headshotMultiplier": "1.4"}), not
 * JSON numbers. Every other numeric field in this dataset (including the {@code damageFalloff}
 * sub-fields) is a genuine JSON number. This is confirmed by checking the type of every occurrence
 * of each field name across the full 337-record file, not just one sample -- it's a real, uniform
 * quirk of this dataset's two source scrapes (see data/weapons/README.md), not malformed data or a
 * parser bug. The accessors below for these fields parse the string, same as any other typed
 * accessor here, they just have to unwrap one extra layer to do it.
 */
public final class WeaponEntry {

    private final Map<String, Object> fields;

    WeaponEntry(Map<String, Object> fields) {
        this.fields = fields;
    }

    /** Direct access to every field this record has, exactly as parsed from JSON (mixed types). */
    public Map<String, Object> rawFields() {
        return fields;
    }

    public String id() {
        return stringField("id");
    }

    public String name() {
        return stringField("name");
    }

    public String category() {
        return stringField("category");
    }

    public String rank() {
        return stringField("rank");
    }

    /** "stalzone.wiki" for records from the second scrape pass, null for the original stalcraftdb.net records. */
    public String source() {
        return stringField("source");
    }

    /** Firearm base damage per hit, or null if this record has none (melee weapons and devices don't). */
    public Double damage() {
        return doubleField("damage");
    }

    public Double magazineCapacity() {
        return doubleField("magazineCapacity");
    }

    public Double rateOfFireRpm() {
        return doubleField("rateOfFireRpm");
    }

    /** Melee light-attack damage range, or null if this isn't a melee weapon record. */
    public Double meleeDamageMinCommon() {
        return doubleField("meleeDamageMinCommon");
    }

    public Double meleeDamageMaxCommon() {
        return doubleField("meleeDamageMaxCommon");
    }

    /** Melee heavy/"strong"-attack damage range, or null if this isn't a melee weapon record. */
    public Double meleeDamageMinStrong() {
        return doubleField("meleeDamageMinStrong");
    }

    public Double meleeDamageMaxStrong() {
        return doubleField("meleeDamageMaxStrong");
    }

    public boolean isMelee() {
        return meleeDamageMinCommon() != null;
    }

    public boolean isFirearm() {
        return damage() != null;
    }

    /**
     * Real per-weapon headshot damage multiplier (e.g. {@code 1.4}), present on 275 of 337
     * records -- see this class's doc for why the source field is a JSON string, not a number.
     * Null if the record has none (melee weapons and devices don't carry this field).
     */
    public Double headshotMultiplier() {
        return stringAsDoubleField("headshotMultiplier");
    }

    /** Real per-weapon limb-hit damage multiplier, same string-encoding quirk as {@link #headshotMultiplier()}. */
    public Double limbMultiplier() {
        return stringAsDoubleField("limbMultiplier");
    }

    /** Real per-weapon shot count before overheating, where the dataset defines one (string-encoded, e.g. "51.0"). */
    public Double overheatAfterShots() {
        return stringAsDoubleField("overheatAfterShots");
    }

    /** Real per-melee-weapon reach (blocks/meters, unit not confirmed) for a light ("common") attack. */
    public Double meleeReachCommon() {
        return doubleField("meleeReachCommon");
    }

    /** Real per-melee-weapon reach for a heavy ("strong") attack. */
    public Double meleeReachStrong() {
        return doubleField("meleeReachStrong");
    }

    /** Real per-melee-weapon armor-piercing value (exact game-mechanic meaning not confirmed from this dataset alone). */
    public Double meleePiercing() {
        return doubleField("meleePiercing");
    }

    /** Real per-melee-weapon percent chance of a named "bloodlust" mechanic (mechanic's exact effect not confirmed). */
    public Double meleeBloodlustChancePct() {
        return doubleField("meleeBloodlustChancePct");
    }

    /** Real per-melee-weapon critical-hit multiplier (string-encoded, same quirk as {@link #headshotMultiplier()}). */
    public Double meleeCriticalHitMultiplier() {
        return stringAsDoubleField("meleeCriticalHitMultiplier");
    }

    /** Real per-melee-weapon damage multiplier specifically vs. mutants (string-encoded, same quirk as {@link #headshotMultiplier()}). */
    public Double meleeMutantDamageMultiplier() {
        return stringAsDoubleField("meleeMutantDamageMultiplier");
    }

    /**
     * Real per-firearm range/damage-dropoff curve, present on 268 of 337 records: base damage
     * ({@code startDamage}, matches {@link #damage()}) holds until {@code damageDecreaseStart}
     * meters, then decreases to {@code endDamage} by {@code damageDecreaseEnd}, with
     * {@code maxDistance} as the weapon's outer effective range. All five sub-fields are real
     * JSON numbers (unlike the string-encoded fields above).
     *
     * <p><b>Update, part 39</b>: this exact five-field shape and every boundary's meaning is now
     * OFFICIALLY documented, not just inferred from field names -- EXBO's own public API docs
     * (<a href="https://eapi.stalzone.net/items.html">eapi.stalzone.net/items.html</a>, the
     * "Damage Ramp Block" info-block type, fetched during this project's public-source research
     * phase, part 36-39) define {@code startDamage} as "weapon damage at distance of 0 meters",
     * {@code damageDecreaseStart} as "distance in meters at which damage starts to decrease",
     * {@code damageDecreaseEnd} as "distance in meters at which damage stops decreasing",
     * {@code endDamage} as "weapon damage at distance defined by damageDecreaseEnd", and
     * {@code maxDistance} as "max distance in meters after which weapon deals no damage" --
     * word-for-word matching this dataset's own field names, confirming the shape this project
     * had already inferred. What the official docs do NOT specify is the interpolation method
     * for the ramp strictly between {@code damageDecreaseStart} and {@code damageDecreaseEnd} --
     * see {@link #damageAtDistanceLinearAssumption} for a disclosed, non-official assumption
     * about that one remaining gap. This raw accessor still returns the five real numbers
     * unmodified; null if the record has no falloff curve.
     */
    public double[] damageFalloff() {
        Object raw = fields.get("damageFalloff");
        if (!(raw instanceof Map)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> f = (Map<String, Object>) raw;
        Double startDamage = numberField(f, "startDamage");
        Double decreaseStart = numberField(f, "damageDecreaseStart");
        Double endDamage = numberField(f, "endDamage");
        Double decreaseEnd = numberField(f, "damageDecreaseEnd");
        Double maxDistance = numberField(f, "maxDistance");
        if (startDamage == null || decreaseStart == null || endDamage == null || decreaseEnd == null || maxDistance == null) {
            return null;
        }
        return new double[] { startDamage, decreaseStart, endDamage, decreaseEnd, maxDistance };
    }

    /**
     * Base damage at a given distance, built on top of {@link #damageFalloff()}'s five real
     * numbers. The flat-before/ramp/flat-after/zero-beyond SHAPE below is now the OFFICIALLY
     * documented behavior (see {@link #damageFalloff()}'s part 39 update) -- not this method's
     * own invention:
     * <ul>
     *   <li>{@code 0 <= distance <= damageDecreaseStart}: {@code startDamage} (flat, official)</li>
     *   <li>{@code damageDecreaseStart < distance < damageDecreaseEnd}: linearly interpolated --
     *       <b>this one segment's shape is NOT officially documented</b> (EXBO's docs describe
     *       the block only as a "damage ramp" with no formula). Linear is the conventional
     *       default for a two-point ramp and matches "ramp" terminology, but is NOT verified
     *       against real captured in-game damage numbers at intermediate distances -- that would
     *       need a controlled capture per this project's rule 1, not done. The method name says
     *       so on purpose.</li>
     *   <li>{@code damageDecreaseEnd <= distance < maxDistance}: {@code endDamage} (flat, official)</li>
     *   <li>{@code distance >= maxDistance}: {@code 0} (official: "deals no damage")</li>
     * </ul>
     *
     * <p><b>Real, confirmed edge case, not a bug</b>: exactly 1 of the 268 real falloff records
     * ({@code knmdv}, "Gauss Rifle") has {@code damageDecreaseStart == damageDecreaseEnd == 200}
     * AND {@code startDamage == endDamage == 247} -- i.e. genuinely flat damage (247) across its
     * whole 0-250m range with no ramp segment at all, thematically consistent with a railgun-style
     * weapon. The boundary conditions here ({@code <=}/{@code >=}, not strict) handle a
     * zero-width ramp correctly without a division-by-zero special case -- confirmed by testing
     * this exact record, not assumed safe.
     *
     * @return null if this record has no falloff curve (69 of 337 records).
     * @throws IllegalArgumentException if distanceMeters is negative.
     */
    public Double damageAtDistanceLinearAssumption(double distanceMeters) {
        if (distanceMeters < 0) {
            throw new IllegalArgumentException("distanceMeters must be >= 0, got " + distanceMeters);
        }
        double[] curve = damageFalloff();
        if (curve == null) {
            return null;
        }
        double startDamage = curve[0];
        double decreaseStart = curve[1];
        double endDamage = curve[2];
        double decreaseEnd = curve[3];
        double maxDistance = curve[4];

        if (distanceMeters >= maxDistance) {
            return 0.0;
        }
        if (distanceMeters <= decreaseStart) {
            return startDamage;
        }
        if (distanceMeters >= decreaseEnd) {
            return endDamage;
        }
        double t = (distanceMeters - decreaseStart) / (decreaseEnd - decreaseStart);
        return startDamage + t * (endDamage - startDamage);
    }

    private String stringField(String key) {
        Object v = fields.get(key);
        return v instanceof String ? (String) v : null;
    }

    private Double doubleField(String key) {
        Object v = fields.get(key);
        return v instanceof Double ? (Double) v : null;
    }

    /** For the fields disclosed in this class's doc that are real numbers encoded as JSON strings. */
    private Double stringAsDoubleField(String key) {
        Object v = fields.get(key);
        if (!(v instanceof String)) {
            return null;
        }
        try {
            return Double.parseDouble((String) v);
        } catch (NumberFormatException e) {
            return null; // honestly report "no usable value" rather than guessing
        }
    }

    private Double numberField(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Double ? (Double) v : null;
    }

    @Override
    public String toString() {
        return "WeaponEntry{" + id() + " \"" + name() + "\" (" + category() + ")}";
    }
}
