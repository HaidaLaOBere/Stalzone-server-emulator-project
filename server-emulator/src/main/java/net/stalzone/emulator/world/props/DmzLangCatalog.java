package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real {@code dmz.*} (78 lines) and {@code dmz2026.*} (17 lines) namespaces in
 * {@code stalker/lang/en.lang} together (part 135, the fourteenth/fifteenth concrete namespaces
 * built from part 123's follow-up census) -- two real, related DMZ-mode content generations. Part
 * 119's {@code StalkerLangCatalog} explicitly flagged these two prefixes for a direct "Hook"-
 * ability text compare once built; this class performs that compare (see below).
 *
 * <p>Both are confirmed by direct count to be single real groups with zero flat exceptions,
 * modeled with the established last-dot-split scheme, kept in two SEPARATE tables (not merged
 * into one) since they are two real distinct top-level prefixes: {@code dmz.<id>.<field>}
 * (28 {@code name} + 22 {@code description} + 14 {@code hint} + 14 {@code header} = 78 exactly)
 * splits into {@code ability.*} (44 lines: one real human "soldier" ability, {@code
 * active.soldier_hook}="Hook", plus a full 21-ability real "zombie" playable-class kit -- 12
 * active, 9 passive -- tying to {@link PartyMissionsCatalog}'s (part 133) {@code
 * kill_entity_living.zombies}/{@code dmz_zombie_scenario} ids), {@code leagues.<n>.name} (6
 * lines, a real previously-uncatalogued DMZ ranked-ladder: "Baron's Guard", "Market Elite",
 * "Market Agents", "Black Raiders", "Operatives", "Recruits"), {@code tutorial.<id>.header}/
 * {@code .hint} (28 lines, 14 real tutorial tips with embedded {@code [map:waypoint_*]} tags).
 * {@code dmz2026.<id>.<field>} (7 {@code description} + 2 {@code title} + 2 {@code name} + 2
 * {@code desc} + 4 single-digit level labels = 17 exactly) splits into {@code
 * ability.active.hook.*} (the real 2026-generation player Hook ability), {@code
 * calldown.respawn.name} (1 line, "Reinforcement"), {@code industry.equipment.level.<n>} (4 flat
 * "LV n" labels), {@code trader_terminal_requirement.level_<n>.description} (4 lines),
 * {@code chest.requirement.<tier>.description} (2 lines), {@code game_mode.<id>.title}/{@code
 * .desc} (4 lines, 2 real modes: {@code solo}="SCOUT", {@code party}="TROOPER GROUP").
 *
 * <p><b>The flagged Hook compare.</b> Three real "Hook" grappling-ability texts exist across the
 * two namespaces. {@code dmz.ability.active.soldier_hook} and {@code
 * dmz2026.ability.active.hook} are BYTE-IDENTICAL ("You throw a grappling hook in the direction
 * of view. When it latches onto a surface, the hook pulls you in.") -- real evidence the 2026
 * player-facing Hook ability is a direct carryover of the original `dmz` soldier ability, not a
 * redesign. {@code dmz.ability.active.zombie_hook} is a real NEAR-identical but NOT byte-identical
 * variant ("in the direction of your view" -- an extra "your" -- vs. "in the direction of view"
 * for the other two) -- a genuine minor real client text inconsistency between the human and
 * zombie versions of the same ability, disclosed as-is per rule 1, not corrected or merged.
 */
public final class DmzLangCatalog {

    private final Map<String, Map<String, String>> dmzFieldsById;
    private final Map<String, Map<String, String>> dmz2026FieldsById;

    private DmzLangCatalog(Map<String, Map<String, String>> dmzFieldsById,
                            Map<String, Map<String, String>> dmz2026FieldsById) {
        this.dmzFieldsById = dmzFieldsById;
        this.dmz2026FieldsById = dmz2026FieldsById;
    }

    private static final String DMZ_PREFIX = "dmz.";
    private static final String DMZ2026_PREFIX = "dmz2026.";

    private static Map<String, Map<String, String>> loadGeneric(LangCatalog catalog, String prefix) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(prefix)) {
            String rest = key.substring(prefix.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return fieldsById;
    }

    public static DmzLangCatalog load(LangCatalog catalog) {
        return new DmzLangCatalog(loadGeneric(catalog, DMZ_PREFIX), loadGeneric(catalog, DMZ2026_PREFIX));
    }

    public String dmzField(String id, String field) {
        Map<String, String> fields = dmzFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String dmz2026Field(String id, String field) {
        Map<String, String> fields = dmz2026FieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real {@code dmz.*} ids with at least one field, sorted. */
    public List<String> dmzIds() {
        List<String> out = new ArrayList<>(dmzFieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real {@code dmz2026.*} ids with at least one field, sorted. */
    public List<String> dmz2026Ids() {
        List<String> out = new ArrayList<>(dmz2026FieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
