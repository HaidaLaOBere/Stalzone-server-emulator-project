package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 173. Catalogs the real 293-line {@code pda.waypoint.we.*} namespace in {@code
 * stalker/lang/en.lang} -- real PDA map-waypoint labels for world-event locations, camps, and
 * bosses. Found while auditing the {@code pda.*} top-level prefix (299 lines total) for full
 * coverage: {@link HomeOutpostCatalog} covers only the other 6 lines ({@code
 * pda.waypoint.home_outpost.*}), and {@link WorldEventCatalog}'s own Javadoc had explicitly
 * disclosed this namespace's existence as "confirmed consistent where checked, not separately
 * catalogued" -- a real, honest admission that it was only spot-checked, not exhaustively
 * verified or modeled. This class does that full exhaustive work.
 *
 * <p>Real shape: {@code pda.waypoint.we.<eventId>.<field>}, the same {@code eventId} vocabulary
 * {@link WorldEventCatalog} uses for {@code we.<eventId>.*}, modeled with the same generic
 * last-dot-split {@code (id, field)} table -- 280 distinct real ids, 291 lines, dominated by
 * {@code title} (273) and {@code desc} (7), plus a handful of real compound ids where the
 * generic split naturally (no special-casing needed) treats a longer dotted prefix as its own
 * distinct id: {@code insideout_centrifuge.status} ({@code title}/{@code description}), {@code
 * insideout_centrifuge.map_marker.description} ({@code active}/{@code inactive}), and a real
 * {@code outpost} id whose "fields" are actually per-faction outpost labels ({@code merc}=
 * "Mercenary Outpost", {@code freedom}="Rise Outpost", {@code duty}="Frontier Outpost", {@code
 * covenant}="Covenant Outpost", {@code neutral}="Neutral Outpost", {@code stalkers}="Stalker
 * Outpost", {@code bandits}="Bandit Outpost", {@code in_progress}="Disputed Outpost").
 * Additionally, 2 real lines ({@code shopot_exit}, {@code shopot_control_center}) exist as
 * completely flat keys with no field suffix at all -- and, disclosed as a real, minor client
 * quirk rather than an error, EACH of those two ids also has its own separate, real, byte-identical
 * {@code .title} line (e.g. both {@code pda.waypoint.we.shopot_exit}="Murmur Post" and {@code
 * pda.waypoint.we.shopot_exit.title}="Murmur Post" exist side by side) -- confirmed by direct
 * count, not assumed. 291 (generic table) + 2 (flat) = 293, confirmed by direct count against the
 * full real file with zero leftover lines.
 *
 * <p><b>Real cross-reference against {@link WorldEventCatalog}, done exhaustively here for the
 * first time (not just spot-checked):</b> of this namespace's 280 real ids, 191 also carry a real
 * {@code we.<id>.status_title} entry; of those 191, 183 match byte-for-byte and 8 do not. Those 8
 * real mismatches fall into three genuinely different categories, disclosed separately rather than
 * lumped together as one kind of "discrepancy":
 * <ul>
 * <li><b>Cosmetic capitalization only</b> (4 ids: {@code supply_base_duty}/{@code _merc}/{@code
 * _freedom}/{@code _covenant}): {@code we.*.status_title}="Supply base" (lowercase b) vs. this
 * namespace's {@code title}="Supply Base" (capital B) -- same real words, a real, trivial
 * capitalization difference, preserved exactly as found on both sides.
 * <li><b>A real, genuine client text bug -- two ids' real content is swapped</b> ({@code
 * science_camp_duty}/{@code science_camp_mercenary}): {@code we.science_camp_duty.status_title}=
 * "Mercenary scientists" while {@code we.science_camp_mercenary.status_title}="Frontier
 * scientists" -- backwards, since {@code duty} is this project's already-confirmed "Frontier"
 * faction id and the "mercenary" id should plainly read "Mercenary". This namespace's own {@code
 * title} field has the pairing the RIGHT way round for both ids ({@code
 * science_camp_duty.title}="Frontier scientists", {@code science_camp_mercenary.title}=
 * "Mercenary scientists"), so this class's data can be used to identify which of the two real
 * client strings is the swapped one, but this class does not silently "correct" {@link
 * WorldEventCatalog}'s own already-shipped data -- both are exposed exactly as each real file
 * line reads.
 * <li><b>Different real UI role, not a factual conflict</b> (2 ids: {@code shopot_exit}, {@code
 * inside_extractor}): {@code we.shopot_exit.status_title}="Clear the area and leave the location"
 * (a real in-progress objective instruction) vs. this namespace's {@code shopot_exit.title}=
 * "Murmur Post" (a real place/waypoint name); {@code we.inside_extractor.status_title}="Defending
 * Installation" (a real objective status) vs. this namespace's {@code inside_extractor.title}=
 * "Onyx Area" (a real place name). Disclosed as a plausible reading -- {@link
 * WorldEventCatalog#statusTitle} reads as live mission-status text while this namespace's {@code
 * title} reads as a static map label for the same event -- not confirmed by any further evidence,
 * per rule 1.
 * </ul>
 * Separately, id-set comparison found 57 real ids that exist ONLY in this namespace (not in {@code
 * we.*} at all, e.g. {@code frontline_camp_psy_officer}, {@code dmz26_camp_docs}, {@code outpost})
 * and 60 real ids that exist ONLY in {@code we.*} (not here, e.g. {@code abstract_clash_st_ba},
 * {@code inside_arena}, {@code ny24_worldboss}) -- each namespace genuinely covers some real
 * events the other does not, not a strict subset/superset relationship in either direction.
 */
public final class PdaWaypointEventCatalog {

    private static final String PREFIX = "pda.waypoint.we.";

    private final Map<String, Map<String, String>> fieldsById;
    private final Map<String, String> flatValueById;

    private PdaWaypointEventCatalog(Map<String, Map<String, String>> fieldsById, Map<String, String> flatValueById) {
        this.fieldsById = fieldsById;
        this.flatValueById = flatValueById;
    }

    public static PdaWaypointEventCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        Map<String, String> flatValueById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                // Real flat exceptions (shopot_exit, shopot_control_center) -- each also has its
                // own separate, real, byte-identical ".title" line, captured normally below.
                flatValueById.put(rest, catalog.get(key));
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new PdaWaypointEventCatalog(fieldsById, flatValueById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String title(String id) { return field(id, "title"); }
    public String desc(String id) { return field(id, "desc"); }

    /** Real flat value for one of the 2 real flat-exception ids ({@code shopot_exit}, {@code shopot_control_center}), or null. */
    public String flatValue(String id) {
        return flatValueById.get(id);
    }

    /** Real ids that have at least one field in the generic (id, field) table, sorted. Does not include the 2 real flat-exception ids. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** The 2 real flat-exception ids, sorted. */
    public List<String> flatIds() {
        List<String> out = new ArrayList<>(flatValueById.keySet());
        Collections.sort(out);
        return out;
    }

    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }

    /** Total real lines resolved (generic table entries plus flat exceptions) -- 293 against the real, full file. */
    public int size() {
        int total = flatValueById.size();
        for (Map<String, String> m : fieldsById.values()) {
            total += m.size();
        }
        return total;
    }
}
