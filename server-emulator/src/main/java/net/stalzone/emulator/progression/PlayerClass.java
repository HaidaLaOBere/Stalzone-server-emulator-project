package net.stalzone.emulator.progression;

/**
 * STALZONE's real, evidenced playable-class roster (part 66, {@code
 * world/props/WeaponAssetCatalog}): six real class names found across two independent asset
 * angles under {@code modassets/assets/weapons/} -- {@code sound/class_hunter/}/{@code
 * class_shaman/}/{@code class_tank/} each hold a real per-class {@code attack_N.ogg} set, and
 * {@code models/weapons/ability_items/tubus/} separately has real per-class emissive texture
 * variants ({@code assault_emi.ol}/{@code scout_emi.ol}/{@code support_emi.ol}) confirming the
 * other three. No other class names have been found anywhere in this project as of part 83.
 *
 * <p><b>What this enum does NOT claim.</b> Only the six NAMES are real, evidenced data. Nothing
 * found anywhere in this project's evidence says: what stat differences (if any) exist between
 * classes, whether a player is limited to one class at a time or can switch freely, how a class is
 * selected/unlocked, or which of {@link PrestigeAbility}'s real named abilities (if any) are
 * class-specific versus available to everyone via the separate "prestige" progression system --
 * the {@code class_*}/{@code prestige_abil} real asset folders that name these two rosters are
 * themselves two SEPARATE real folders with no cross-reference found tying a specific ability to a
 * specific class. This project therefore does not invent such a mapping (rule 1: never turn an
 * unverified assumption into a spec). This enum exposes the real roster only -- a caller ({@code
 * PlayerState}) can record which class a player has selected, but nothing about what that
 * selection actually changes mechanically is simulated here, since no evidence exists for it.
 */
public enum PlayerClass {
    HUNTER,
    SHAMAN,
    TANK,
    ASSAULT,
    SCOUT,
    SUPPORT
}
