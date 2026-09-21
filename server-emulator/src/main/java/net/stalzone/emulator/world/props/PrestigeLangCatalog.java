package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 79-line {@code prestige.*} namespace in {@code stalker/lang/en.lang} (part
 * 134, the thirteenth concrete namespace built from part 123's follow-up census) -- the real
 * "Coliseum" / "Cyclical Trials" PvP arena mode, already independently confirmed real by several
 * earlier parts from completely different angles: {@link TextureGuiCatalog}'s {@code
 * bundle.notf.prestige.info.start}="Soothsayer has stabilized the Cyclical Trials arena once
 * again. The Coliseum awaits its..." (naming the same "Soothsayer" entity part 109's {@link
 * CurrencyDisplayCatalog} first surfaced from {@code prestige_dollars}="Patterns"), {@link
 * StalkerClansCatalog}'s {@code kolizei} ("coliseum") map id (part 72), {@link
 * StalkerSoundEventsCatalog}'s {@code arena/} sound-file set, and {@link ClientPrefs}'s real
 * handbook-article path naming the same "Coliseum" session mode. Named {@code
 * PrestigeLangCatalog}, not {@code PrestigeCatalog}, to avoid confusion with the existing
 * asset-derived {@code progression.PrestigeAbility} enum (part 66/90).
 *
 * <p><b>Explicit overlap disclosure.</b> {@code progression.PrestigeAbility.realDisplayName}/
 * {@code realDescription} (part 90) already read this same namespace's {@code
 * prestige.ability.<key>.name}/{@code .description} entries directly by constructed key, but only
 * for 8 of this namespace's 25 real ability ids (the 8 {@code PrestigeAbility} could confirm a
 * literal-word crosswalk for from its own separate asset-derived roster) -- it does not enumerate
 * the namespace, nor cover its other 5 real groups at all. This class is the first full,
 * enumerable catalog of the entire real namespace; it does not replace {@code PrestigeAbility}'s
 * own asset-based evidence chain.
 *
 * <p>Confirmed by direct count to be a single real group with zero flat exceptions: all 79 lines
 * fit {@code prestige.<id>.<field>} via the established last-dot-split scheme. Splits into 6 real
 * groups: {@code ability.<abilityId>.name}/{@code .description} (49 lines, 25 real abilities --
 * one real id, {@code tank_high_gravity_aura}, has a `.description` but no `.name`, an asymmetric
 * field gap already seen elsewhere in this project, e.g. part 118's `GeneralCatalog` `legend` id);
 * {@code classes.<classId>.name}/
 * {@code .desc} (8 lines, 4 real classes: {@code noclass}="NO CLASS", {@code combat}="Assault",
 * {@code scientist}="Support", {@code combined}="Operative" -- two of these DISPLAY NAMES
 * ("Assault"/"Support") share exact real English words with 2 of the asset-derived {@code
 * progression.PlayerClass} roster's 6 names, while the {@code ability.*} group's own internal ids
 * use a DIFFERENT real class-tag-prefix vocabulary ({@code tank_}/{@code scout_}/{@code
 * support_}) sharing literal words with 3 different {@code PlayerClass} names -- two genuinely
 * separate real internal-id schemes for what may be the same underlying class roster, disclosed
 * as-is rather than merged into an unverified crosswalk, per rule 1); {@code
 * howtoplay.tip.<n>.title}/{@code .description} (8 lines, 4 real tips describing a rifts/
 * batteries/generators multi-team objective-capture mechanic never documented anywhere else in
 * this project); {@code rating.no_access.message} (1 line, "Level 2 Drunken Geiger Bar
 * reputation" -- a real reputation-gate tied to {@code SettlementRegistry}'s {@code sever}
 * settlement, part 105); {@code specialization.<classId>.<specId>.name}/{@code .desc} (12 lines,
 * 6 real per-class specializations, e.g. {@code combat.push}="Onslaught", {@code
 * combat.bastion}="Bastion"); {@code usual.no_access.message} (1 line, "Level 3 Bar reputation" --
 * a second, distinct real reputation gate for the same mode's other queue, tied to the standalone
 * "Bar" location already referenced by part 118's `GeneralCatalog` achievement ids).
 */
public final class PrestigeLangCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private PrestigeLangCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "prestige.";

    public static PrestigeLangCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new PrestigeLangCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
