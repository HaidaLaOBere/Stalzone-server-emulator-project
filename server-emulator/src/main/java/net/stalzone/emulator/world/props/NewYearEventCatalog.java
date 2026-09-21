package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real {@code ny.*} (34 lines) and {@code ny_toys.*} (13 lines) namespaces in {@code
 * stalker/lang/en.lang} together (part 157, the thirty-seventh/thirty-eighth concrete namespaces
 * built from part 123's follow-up census) -- real UI text for a "X-Mas Tree 2025" New Year
 * seasonal event, kept in two separate tables (not merged into one) since they are two real
 * distinct top-level prefixes, the same two-tables-in-one-class shape already used by part 135's
 * {@link DmzLangCatalog}.
 *
 * <p><b>{@code ny.*} (34 lines), reconciled exactly: 31 + 3.</b> 31 real lines fit the established
 * generic last-dot-split (id, field) table: {@code leagues.&lt;id&gt;.name}/{@code .short_name}/
 * {@code .reward_text} (18 lines) is a real 6-tier leaderboard ranked ladder ({@code
 * ice}="Ice League", {@code golden}="Golden League", {@code silver}="Silver League", {@code
 * bronze}="Bronze League", {@code wood}="Wooden League", {@code snow}="Snow League"); {@code
 * tutorial.&lt;id&gt;.header}/{@code .hint} (10 lines, 5 real tutorial cards) covers the Christmas
 * tree, decoration merging, Battle Pass, challenges, and the "Pine Cones" event currency; {@code
 * rewardmap.tier&lt;n&gt;.name} (3 lines) names 3 real generic reward tiers. The remaining 3 real
 * lines have no further dot after the {@code ny.} prefix ({@code leaderboardRewardTitle}, {@code
 * leaderboardRewardText}, {@code reward_text_without_league}) and are modeled as a separate flat
 * carve-out via {@link #flatField}, the same generic-table-plus-flat-carve-out shape already used
 * by part 143's {@link PremiumCatalog} and part 156's {@link OutpostModeCatalog}.
 *
 * <p><b>A real client copy-paste bug, disclosed not corrected.</b> {@code
 * ny.leagues.snow.reward_text} is BYTE-IDENTICAL to {@code ny.leagues.wood.reward_text} --
 * including the literal text "you have managed to reach the Wooden League!" inside the {@code
 * snow} league's own reward text, where a correct, distinct Snow League message is clearly
 * intended. Preserved and disclosed exactly as authored per this project's standing rule 1.
 *
 * <p><b>A third real recurrence of the "leagues" ranked-ladder naming convention.</b> Part 135's
 * {@link DmzLangCatalog} ({@code dmz.leagues.&lt;n&gt;}: "Baron's Guard" through "Recruits") and
 * part 154's {@link InsideCatalog} ({@code inside.leagues.&lt;n&gt;}: "Legend of the Inside Out"
 * through "Shadow") already gave this project two real, independently-named ranked-ladder
 * systems under a {@code leagues} key. This standalone {@code ny.leagues.*} is a real THIRD,
 * again independently-named ("Ice"/"Golden"/"Silver"/"Bronze"/"Wooden"/"Snow") ranked-ladder
 * system -- disclosed as a recurring real client naming convention for seasonal/competitive
 * ladders, not merged with either prior system, since none of the three share tier names, ids, or
 * tier counts beyond all having 6.
 *
 * <p><b>{@code ny_toys.&lt;id&gt;} (13 lines)</b> is a real flat {@code Map&lt;String,String&gt;}
 * of New Year decoration SET/category display names ({@code setA}="Red Decorations" through
 * {@code setM}="Digger's Decorations", a real non-alphabetical real id sequence skipping {@code
 * setI} and inserting {@code setQ} out of order, disclosed as-is). This is a real, previously
 * uncatalogued CATEGORY layer sitting directly above the already-confirmed part-121 {@link
 * ItemLangCatalog} real {@code item.name.ny_toys.&lt;id&gt;} namespace (273 individual real
 * decoration item display names) -- this class's {@code ny_toys.*} gives the real 13 set names
 * those 273 individual items are grouped under, a genuinely distinct top-level prefix from {@code
 * item.name.ny_toys.*}, disclosed as a further corroboration/completion, not a new independent
 * item roster.
 */
public final class NewYearEventCatalog {

    private final Map<String, Map<String, String>> fieldsById;
    private final Map<String, String> flatFields;
    private final Map<String, String> toySetNameById;

    private NewYearEventCatalog(Map<String, Map<String, String>> fieldsById, Map<String, String> flatFields,
            Map<String, String> toySetNameById) {
        this.fieldsById = fieldsById;
        this.flatFields = flatFields;
        this.toySetNameById = toySetNameById;
    }

    private static final String NY_PREFIX = "ny.";
    private static final String NY_TOYS_PREFIX = "ny_toys.";

    public static NewYearEventCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        Map<String, String> flatFields = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(NY_PREFIX)) {
            String rest = key.substring(NY_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                flatFields.put(rest, catalog.get(key));
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, String> toySetNameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(NY_TOYS_PREFIX)) {
            String setId = key.substring(NY_TOYS_PREFIX.length());
            toySetNameById.put(setId, catalog.get(key));
        }

        return new NewYearEventCatalog(fieldsById, flatFields, toySetNameById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** One of the 3 real zero-dot top-level {@code ny.*} fields. */
    public String flatField(String field) {
        return flatFields.get(field);
    }

    /** A real decoration set/category display name, e.g. {@code setA} -&gt; "Red Decorations". */
    public String toySetName(String setId) {
        return toySetNameById.get(setId);
    }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real decoration set ids, sorted. */
    public List<String> toySetIds() {
        List<String> out = new ArrayList<>(toySetNameById.keySet());
        Collections.sort(out);
        return out;
    }
}
