package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 172. Catalogs a real, previously-uncatalogued 67-line bare {@code operation.*} namespace,
 * found by extending this project's fresh-re-scan methodology (parts 167-171) with a full
 * top-level-prefix census against the current, complete {@code en.lang} file -- a real "Operations"
 * game mode: three real player classes, their real abilities, a handful of real named missions, and
 * a small set of real in-mission tasks.
 *
 * <p><b>Genuinely distinct from the existing, confusingly-similarly-named {@link
 * OperationsCatalog} (part 111), confirmed by direct inspection, not assumed from the names looking
 * alike.</b> {@code OperationsCatalog} covers only the real, entirely different {@code
 * gui.operation.how_to_play.*} namespace (a GUI tutorial-tab system) plus a {@code
 * category.<id>} UI-tab-label lookup that happens to resolve the literal string {@code "operation"}
 * to "Operations Mode" -- neither of which parses or exposes any of this class's real 67 lines.
 * Confirmed by direct source search: no existing class in this project builds a literal or dynamic
 * {@code "operation."} bare-prefix key lookup before this one. The real bare {@code operations.}
 * (plural) prefix does not exist anywhere in the client at all -- confirmed via direct count,
 * zero real lines -- so there is no risk of this class colliding with a plural variant either.
 *
 * <p>Real shape, four real sub-groups under the bare {@code operation.} prefix, each with its own
 * dot-depth:
 * <ul>
 * <li>{@code operation.classes.<classId>.<name|desc>} -- 3 real classes x 2 real fields = 6 lines.
 * Real ids and real display names: {@code combat}="Assault", {@code scientist}="Support", {@code
 * combined}="Operative".
 * <li>{@code operation.abilities.<abilityId>.<name|desc>} -- 21 real abilities x 2 real fields = 42
 * lines, the large majority of the namespace. Real ability-id naming loosely groups by class
 * ({@code ops_tank_*}/{@code passive_ops_tank_*} for Assault, {@code ops_ultimate_support_*}/{@code
 * passive_ops_support_*} for Support, {@code ops_scout_*}/{@code passive_ops_scout_*} for
 * Operative), though this class does not assert a strict id-to-class mapping beyond what the ids
 * themselves literally spell out.
 * <li>{@code operation.task.<taskId>.<name|desc>} -- 3 real in-mission tasks x 2 real fields = 6
 * lines ({@code absorber}="Phase Generator", {@code capture}="Communication Hubs Capturing", {@code
 * boss_ratter}="Ratcatcher Boss").
 * <li>{@code operation.<missionId>.<name|desc>} -- 5 real named missions x 2 real fields = 10 lines
 * ({@code big_cleanup}="The Big Clean Up", {@code shock_therapy}="Focus", {@code
 * quest_polygon0}="Area-0", {@code sea_alienation}="Sea of Exclusion", {@code
 * quest_sea_alienation}="Sea of Exclusion" -- the real duplicate "Sea of Exclusion" display name
 * across two different real mission ids is preserved exactly as found, not merged or treated as an
 * error). This sub-group sits directly under the bare {@code operation.} prefix alongside the three
 * dotted sub-namespaces above, so this class explicitly excludes the {@code classes.}/{@code
 * abilities.}/{@code task.}/{@code introduction.} real sub-prefixes when building this particular
 * table, rather than accidentally absorbing them.
 * <li>{@code operation.introduction.description_<missionId>} -- 3 real lines, one real flavor-text
 * paragraph per mission (only 3 of the 5 real missions above have one: {@code big_cleanup}, {@code
 * shock_therapy}, {@code sea_alienation}). Real key shape here is irregular relative to the other
 * three sub-groups -- the mission id is joined by a literal underscore after {@code description_},
 * not a further dot -- so this sub-group is modeled as its own small flat lookup rather than forced
 * into the shared last-dot-split table.
 * </ul>
 *
 * <p><b>Real content, reconciled exactly: 6 (classes) + 42 (abilities) + 6 (task) + 10 (missions) +
 * 3 (introduction) = 67, confirmed by direct count against the full real file with zero leftover
 * lines.</b>
 *
 * <p><b>Real content corroborates two already-established real facts from independent angles, not
 * new lore:</b> both real {@code operation.introduction.description_big_cleanup} and {@code
 * description_shock_therapy} name "Phil from the ZIVCAS Scientific Research Institute corporation"
 * -- {@code ZIVCAS} is just another entry in this project's long-standing chain of independent
 * "ZIVCAS Scientific Research Institute" corroborations (parts 92, 169, and now this one -- no
 * numbered ordinal is asserted for this recurring corroboration, per this project's standing
 * disclosure convention). Separately, real {@code operation.introduction.description_sea_alienation}
 * names "Rise" and "Frontier" alongside "the Covenant" as real factions -- "Rise" and "Frontier" are
 * real already-confirmed display names for the {@code freedom} and {@code duty} faction ids
 * respectively (see {@link ClanWarCatalog}, {@link CnpsCatalog}, {@link
 * FactionBaseWarningCatalog}), and "Covenant" is the real already-confirmed {@code covenant}
 * faction's own display name -- so this passage is a further independent real corroboration of all
 * three, not a new faction reveal.
 */
public final class OperationModeCatalog {

    private static final String PREFIX = "operation.";
    private static final String CLASSES_SUB_PREFIX = "classes.";
    private static final String ABILITIES_SUB_PREFIX = "abilities.";
    private static final String TASK_SUB_PREFIX = "task.";
    private static final String INTRODUCTION_SUB_PREFIX = "introduction.";
    private static final String INTRODUCTION_DESCRIPTION_KEY_PREFIX = "description_";

    private final Map<String, Map<String, String>> classFields;
    private final Map<String, Map<String, String>> abilityFields;
    private final Map<String, Map<String, String>> taskFields;
    private final Map<String, Map<String, String>> missionFields;
    private final Map<String, String> missionIntroDescriptions;

    private OperationModeCatalog(
            Map<String, Map<String, String>> classFields,
            Map<String, Map<String, String>> abilityFields,
            Map<String, Map<String, String>> taskFields,
            Map<String, Map<String, String>> missionFields,
            Map<String, String> missionIntroDescriptions) {
        this.classFields = classFields;
        this.abilityFields = abilityFields;
        this.taskFields = taskFields;
        this.missionFields = missionFields;
        this.missionIntroDescriptions = missionIntroDescriptions;
    }

    public static OperationModeCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> classFields = new LinkedHashMap<>();
        Map<String, Map<String, String>> abilityFields = new LinkedHashMap<>();
        Map<String, Map<String, String>> taskFields = new LinkedHashMap<>();
        Map<String, Map<String, String>> missionFields = new LinkedHashMap<>();
        Map<String, String> missionIntroDescriptions = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            if (rest.startsWith(CLASSES_SUB_PREFIX)) {
                putLastDotSplit(classFields, rest.substring(CLASSES_SUB_PREFIX.length()), catalog.get(key));
            } else if (rest.startsWith(ABILITIES_SUB_PREFIX)) {
                putLastDotSplit(abilityFields, rest.substring(ABILITIES_SUB_PREFIX.length()), catalog.get(key));
            } else if (rest.startsWith(TASK_SUB_PREFIX)) {
                putLastDotSplit(taskFields, rest.substring(TASK_SUB_PREFIX.length()), catalog.get(key));
            } else if (rest.startsWith(INTRODUCTION_SUB_PREFIX)) {
                String introKey = rest.substring(INTRODUCTION_SUB_PREFIX.length());
                if (introKey.startsWith(INTRODUCTION_DESCRIPTION_KEY_PREFIX)) {
                    String missionId = introKey.substring(INTRODUCTION_DESCRIPTION_KEY_PREFIX.length());
                    missionIntroDescriptions.put(missionId, catalog.get(key));
                }
                // no real line in this namespace has ever used any other "introduction." key shape
            } else {
                // the bare operation.<missionId>.<field> table -- everything not in one of the four
                // real dotted sub-namespaces above
                putLastDotSplit(missionFields, rest, catalog.get(key));
            }
        }

        return new OperationModeCatalog(classFields, abilityFields, taskFields, missionFields, missionIntroDescriptions);
    }

    private static void putLastDotSplit(Map<String, Map<String, String>> table, String rest, String value) {
        int lastDot = rest.lastIndexOf('.');
        if (lastDot < 0) {
            return; // no real line in this namespace has ever lacked a field suffix
        }
        String id = rest.substring(0, lastDot);
        String field = rest.substring(lastDot + 1);
        table.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
    }

    private static String field(Map<String, Map<String, String>> table, String id, String field) {
        Map<String, String> fields = table.get(id);
        return fields == null ? null : fields.get(field);
    }

    private static List<String> ids(Map<String, Map<String, String>> table) {
        List<String> out = new ArrayList<>(table.keySet());
        Collections.sort(out);
        return out;
    }

    public String className(String classId) {
        return field(classFields, classId, "name");
    }

    public String classDescription(String classId) {
        return field(classFields, classId, "desc");
    }

    public List<String> classIds() {
        return ids(classFields);
    }

    public String abilityName(String abilityId) {
        return field(abilityFields, abilityId, "name");
    }

    public String abilityDescription(String abilityId) {
        return field(abilityFields, abilityId, "desc");
    }

    public List<String> abilityIds() {
        return ids(abilityFields);
    }

    public String taskName(String taskId) {
        return field(taskFields, taskId, "name");
    }

    public String taskDescription(String taskId) {
        return field(taskFields, taskId, "desc");
    }

    public List<String> taskIds() {
        return ids(taskFields);
    }

    public String missionName(String missionId) {
        return field(missionFields, missionId, "name");
    }

    public String missionDescription(String missionId) {
        return field(missionFields, missionId, "desc");
    }

    public List<String> missionIds() {
        return ids(missionFields);
    }

    /** Real intro flavor-text paragraph for a real mission id, or null -- only 3 of the 5 real missions have one. */
    public String missionIntroDescription(String missionId) {
        return missionIntroDescriptions.get(missionId);
    }

    public List<String> missionIdsWithIntroDescription() {
        List<String> out = new ArrayList<>(missionIntroDescriptions.keySet());
        Collections.sort(out);
        return out;
    }

    /** Total real lines resolved across all five real sub-groups -- 67 against the real, full file. */
    public int size() {
        int total = missionIntroDescriptions.size();
        for (Map<String, String> m : classFields.values()) total += m.size();
        for (Map<String, String> m : abilityFields.values()) total += m.size();
        for (Map<String, String> m : taskFields.values()) total += m.size();
        for (Map<String, String> m : missionFields.values()) total += m.size();
        return total;
    }
}
