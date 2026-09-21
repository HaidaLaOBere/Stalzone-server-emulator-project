package net.stalzone.emulator.progression;

/**
 * STALZONE's real, evidenced "prestige" ability roster (part 66, {@code
 * world/props/WeaponAssetCatalog}): 11 real named abilities found under {@code
 * modassets/assets/weapons/sound/} -- 9 from {@code prestige_abil/} ({@link #BARRICADE}, {@link
 * #LOW_GRAVITY}, {@link #PERSONAL_TELEPORT}, {@link #PHASE_SHIFT}, {@link #PORTAL_GRENADE},
 * {@link #PUSH_GRENADE}, {@link #SCANNER}, {@link #SCOUT_MINE}, {@link #VORTEX_GRENADE}), 1 from
 * {@code prestige_tranq/} ({@link #TRANQUILIZER}), and 1 from {@code prestige_med/} ({@link
 * #PRESTIGE_MED}). This is the first and only place in this project that names actual abilities
 * behind the "prestige" mechanic (parts 57/58/60/61 only ever found bare cross-references to the
 * word "prestige" itself); part 79's {@code party/weapon_levels.cfg} weapon-XP tier system is a
 * confirmed DISTINCT real system, not related to this one.
 *
 * <p><b>What this enum deliberately does NOT include: any numeric ability data.</b> No cooldown,
 * damage, radius, duration, mana/resource cost, or unlock-level number has been found anywhere in
 * this project for ANY of these 11 abilities -- the real evidence is exhaustively just these 11
 * folder names (each holding only real audio-cue files, no config data). Unlike {@code
 * emission/EmissionCycle} or {@code loot/LootTable}, where a real evidenced STRUCTURE (a phase
 * sequence, a per-mob table) exists for this emulator to layer disclosed placeholder NUMBERS onto,
 * here there is no real structure at all to layer anything onto -- simulating what any of these
 * abilities actually DO in-game (e.g. a blast radius for {@link #VORTEX_GRENADE}, a duration for
 * {@link #PHASE_SHIFT}) would mean inventing gameplay mechanics wholesale, not disclosing a
 * necessary placeholder on top of real data. Per rule 1, this project does not do that. This enum
 * therefore exposes the real named roster only, for a caller ({@code PlayerState}) to track which
 * abilities a player has unlocked -- see {@code GameLoop#tickAbilityUse} for the one thing this
 * project's evidence DOES support simulating (that an ability was invoked at all), and its own doc
 * for exactly what it does and does not simulate.
 *
 * <p>As of part 90, 8 of these 11 abilities have a CONFIRMED real crosswalk (see {@link #langKey})
 * into {@code stalker/lang/en.lang}'s real {@code prestige.ability.<key>.name}/{@code .description}
 * entries ({@link net.stalzone.emulator.localization.LangCatalog}) -- a real, human-written display
 * name and a real (if not fully numeric) mechanic description, e.g. {@link #BARRICADE}'s real
 * description is "Deploy a defensive barricade that provides cover... Charges: {@code
 * \{max_charges number\}}. Recharge time: {@code \{recharge_time number\}} seconds." This still does
 * NOT give this project real numeric values for those {@code {placeholder}} tokens -- only their
 * NAMES and units ("seconds", "meter radius", "%") -- so {@code GameLoop#tickAbilityUse} still
 * simulates no numeric effect; this is real evidence of ability STRUCTURE (what a cooldown/radius/
 * charge count conceptually is), not evidence of what number it actually is. See {@link #langKey}'s
 * own doc for exactly how each of the 8 was confirmed and why the other 3 were deliberately left
 * unconfirmed rather than guessed.
 */
public enum PrestigeAbility {
    BARRICADE,
    LOW_GRAVITY,
    PERSONAL_TELEPORT,
    PHASE_SHIFT,
    PORTAL_GRENADE,
    PUSH_GRENADE,
    SCANNER,
    SCOUT_MINE,
    VORTEX_GRENADE,
    TRANQUILIZER,
    PRESTIGE_MED;

    /**
     * The real {@code stalker/lang/en.lang} internal ability key (part 90) for this ability's
     * {@code prestige.ability.<key>.name}/{@code .description} entries, or {@code null} if this
     * project has no CONFIRMED crosswalk for it.
     *
     * <p>Each of the 11 real abilities here was originally named (part 66) from a real audio-cue
     * folder/filename codename under {@code weapons/sound/prestige_abil,prestige_tranq,prestige_med/}
     * (e.g. {@code prestige_abil_barricade_draw.ogg} -&gt; codename {@code barricade}) -- a
     * DIFFERENT, independent internal-id scheme from {@code en.lang}'s {@code tank_}/{@code
     * support_}/{@code scout_}-prefixed keys (grouped by which of the 3 real {@code
     * prestige.classes.*} owns the ability, not by ability codename). The two schemes do not share
     * literal ids, so the crosswalk below is confirmed only where the audio codename's own plain-
     * English word(s) appear literally in the lang entry's KEY or its real English display-name
     * VALUE -- the same "independent corroboration from a second real source" evidentiary bar this
     * project has applied since part 63's SteamDlcIds cross-check, not a guess from vibes:
     * <ul>
     * <li>{@link #BARRICADE} ({@code barricade}) -&gt; {@code tank_barricade} (key contains "barricade"; name "Barricade")</li>
     * <li>{@link #LOW_GRAVITY} ({@code lowgravity}) -&gt; {@code tank_low_gravity} (key contains "low_gravity"; name "Low Gravity Grenade")</li>
     * <li>{@link #PERSONAL_TELEPORT} ({@code personalteleport}) -&gt; {@code scout_blink} (name "Personal Teleporter" is a literal, exact match despite the differing key)</li>
     * <li>{@link #PORTAL_GRENADE} ({@code portalgrenade}) -&gt; {@code support_gateway} (name "Portal Grenade" is a literal, exact match despite the differing key)</li>
     * <li>{@link #PUSH_GRENADE} ({@code pushgrenade}) -&gt; {@code tank_push} (key contains "push"; description "knocks back all players" matches a push/shockwave mechanic)</li>
     * <li>{@link #SCANNER} ({@code scanner}) -&gt; {@code support_scanner} (key and name both literally "scanner")</li>
     * <li>{@link #SCOUT_MINE} ({@code scoutmine}) -&gt; {@code scout_mine} (key is a literal, exact match, underscore aside)</li>
     * <li>{@link #TRANQUILIZER} ({@code tranquilizer}) -&gt; {@code scout_stun} (name "Tranquilizer" is a literal, exact match despite the differing key)</li>
     * </ul>
     * Deliberately left {@code null} (not guessed) for the remaining 3, since no {@code en.lang}
     * entry's key or English name shares a literal word with the audio codename: {@link
     * #PHASE_SHIFT} ({@code phaseshift} -- the closest candidate, {@code scout_inside_dimension}
     * "Inside Out Shift", shares only the generic word "shift", not enough on its own), {@link
     * #VORTEX_GRENADE} ({@code vortexgrenade} -- the closest candidate, {@code support_black_hole}
     * "Gravity Grenade", is conceptually similar -- a pull-to-center effect -- but shares no literal
     * word), and {@link #PRESTIGE_MED} (the {@code prestige_med/} audio folder has no distinct
     * per-ability codename at all to match against, only the class-level folder name itself).
     */
    public String langKey() {
        switch (this) {
            case BARRICADE: return "tank_barricade";
            case LOW_GRAVITY: return "tank_low_gravity";
            case PERSONAL_TELEPORT: return "scout_blink";
            case PORTAL_GRENADE: return "support_gateway";
            case PUSH_GRENADE: return "tank_push";
            case SCANNER: return "support_scanner";
            case SCOUT_MINE: return "scout_mine";
            case TRANQUILIZER: return "scout_stun";
            default: return null; // PHASE_SHIFT, VORTEX_GRENADE, PRESTIGE_MED -- no confirmed crosswalk, see javadoc
        }
    }

    /**
     * This ability's real display name from {@code catalog} (part 90), or {@code null} if {@link
     * #langKey} is null (no confirmed crosswalk) or {@code catalog} doesn't have the expected
     * {@code prestige.ability.<key>.name} entry.
     */
    public String realDisplayName(net.stalzone.emulator.localization.LangCatalog catalog) {
        String key = langKey();
        return key == null ? null : catalog.get("prestige.ability." + key + ".name");
    }

    /**
     * This ability's real mechanic description from {@code catalog} (part 90), verbatim -- still
     * containing unresolved {@code {placeholder}} tokens, see this enum's class doc -- or {@code
     * null} under the same conditions as {@link #realDisplayName}.
     */
    public String realDescription(net.stalzone.emulator.localization.LangCatalog catalog) {
        String key = langKey();
        return key == null ? null : catalog.get("prestige.ability." + key + ".description");
    }
}
