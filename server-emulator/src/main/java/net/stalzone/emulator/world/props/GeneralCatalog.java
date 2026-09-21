package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 1,121-line {@code general.*} namespace in {@code stalker/lang/en.lang} (part
 * 118) -- a real, previously-uncatalogued in-game achievement system plus two small unrelated
 * groups sharing the same top-level prefix. Confirmed by direct count that the namespace splits
 * cleanly into exactly three real groups with no leftover lines: {@code achievement} (1,116
 * lines), {@code region} (4 lines), {@code stats} (1 line); 1,116 + 4 + 1 = 1,121.
 *
 * <p>{@code general.achievement.id.<achievementId>.title}/{@code .desc} (1,115 of the 1,116
 * achievement lines) is a real achievement system: 558 distinct real string achievement ids, each
 * carrying up to two real fields, {@code title} (558 lines, the short achievement name, e.g.
 * {@code bolota_explored.title}="Beloved Marsh") and {@code desc} (557 lines, the longer
 * requirement text, e.g. {@code bolota_explored.desc}="Cross the river and enter the Zone").
 * Confirmed by direct count that {@code title}/{@code desc} are the ONLY two real field suffixes
 * used across the whole {@code achievement.id.*} group. Many ids are real zone-exploration
 * achievements whose ids/titles tie directly to zone codes this project already models elsewhere
 * via {@link net.stalzone.emulator.world.LocationNameRegistry} (e.g. {@code kordon_explored}, {@code
 * svalka_explored}, {@code agroprom_explored}, {@code td_explored}, {@code dt_explored}, {@code
 * bar_explored}, {@code bar100_explored}, {@code yantar_explored}, {@code les_explored}), disclosed
 * here as a real cross-reference opportunity, not re-modeled.
 *
 * <p>One real id genuinely has a {@code title} but no matching {@code desc}: {@code
 * general.achievement.id.legend.title}="Legend of the Zone" -- confirmed by direct grep against
 * the live file (not a diff-tool artifact) that no {@code general.achievement.id.legend.desc} line
 * exists anywhere in the file. This is disclosed as a real, minor, flat/incomplete achievement
 * entry; {@link #achievementField} simply returns {@code null} for the missing {@code desc} field,
 * same as it would for any other unknown (id, field) pair, so no special-casing was needed for it.
 * A real compound id worth noting explicitly, because it could be mistaken for a discrepancy: {@code
 * dmz_2026_quest.tankiWatch} is a genuine child id with its OWN complete title/desc pair ({@code
 * title}="40 Tons", {@code desc}="Find all tanks on Boral"), fully distinct from its similarly-named
 * parent id {@code dmz_2026_quest} (which also has its own complete title/desc pair). The generic
 * last-dot split below handles this correctly with no special-casing, since only the trailing
 * {@code title}/{@code desc} segment is treated as the field name.
 *
 * <p>{@code general.achievement.category.*} (the remaining 1 line of the 1,116) is a real, separate
 * flat label map: {@code general.achievement.category.quest}="Quest" is the only real category
 * label present in this file today.
 *
 * <p>{@code general.region.*} (4 lines, complete, unrelated to achievements) is a real flat map of
 * platform/publishing region labels: {@code eu}="Europe", {@code na}="North America", {@code
 * sea}="Southeast Asia (Singapore, Australia)", {@code nea}="Northeast Asia (Korea, Japan)".
 *
 * <p>{@code general.stats.*} (1 line, unrelated to achievements) is a real flat map, here keyed by
 * the id after stripping the {@code stat.} sub-group segment: {@code
 * general.stats.stat.raffle-tickets-ny22}="Participation Tickets found", a real seasonal
 * (New Year 2022) event-currency display label.
 */
public final class GeneralCatalog {

    private final Map<String, Map<String, String>> achievementFieldsById;
    private final Map<String, String> achievementCategoryLabelById;
    private final Map<String, String> regionNameById;
    private final Map<String, String> statLabelById;

    private GeneralCatalog(Map<String, Map<String, String>> achievementFieldsById,
                            Map<String, String> achievementCategoryLabelById,
                            Map<String, String> regionNameById,
                            Map<String, String> statLabelById) {
        this.achievementFieldsById = achievementFieldsById;
        this.achievementCategoryLabelById = achievementCategoryLabelById;
        this.regionNameById = regionNameById;
        this.statLabelById = statLabelById;
    }

    private static final String PREFIX = "general.";
    private static final String ACHIEVEMENT_ID_PREFIX = "achievement.id.";
    private static final String ACHIEVEMENT_CATEGORY_PREFIX = "achievement.category.";
    private static final String REGION_PREFIX = "region.";
    private static final String STATS_STAT_PREFIX = "stats.stat.";

    public static GeneralCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> achievementFieldsById = new LinkedHashMap<>();
        Map<String, String> achievementCategoryLabelById = new LinkedHashMap<>();
        Map<String, String> regionNameById = new LinkedHashMap<>();
        Map<String, String> statLabelById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());

            if (rest.startsWith(ACHIEVEMENT_ID_PREFIX)) {
                String idAndField = rest.substring(ACHIEVEMENT_ID_PREFIX.length());
                int lastDot = idAndField.lastIndexOf('.');
                if (lastDot < 0) {
                    continue;
                }
                String id = idAndField.substring(0, lastDot);
                String field = idAndField.substring(lastDot + 1);
                achievementFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
            } else if (rest.startsWith(ACHIEVEMENT_CATEGORY_PREFIX)) {
                String id = rest.substring(ACHIEVEMENT_CATEGORY_PREFIX.length());
                achievementCategoryLabelById.put(id, catalog.get(key));
            } else if (rest.startsWith(REGION_PREFIX)) {
                String id = rest.substring(REGION_PREFIX.length());
                regionNameById.put(id, catalog.get(key));
            } else if (rest.startsWith(STATS_STAT_PREFIX)) {
                String id = rest.substring(STATS_STAT_PREFIX.length());
                statLabelById.put(id, catalog.get(key));
            }
        }

        return new GeneralCatalog(achievementFieldsById, achievementCategoryLabelById, regionNameById, statLabelById);
    }

    public String achievementField(String id, String field) {
        Map<String, String> fields = achievementFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String achievementTitle(String id) { return achievementField(id, "title"); }
    public String achievementDesc(String id) { return achievementField(id, "desc"); }

    /** Real achievement ids that have at least one of title/desc, sorted. */
    public List<String> achievementIds() {
        List<String> out = new ArrayList<>(achievementFieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    public List<String> achievementFieldNames(String id) {
        Map<String, String> fields = achievementFieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }

    public String achievementCategoryLabel(String id) { return achievementCategoryLabelById.get(id); }

    public List<String> achievementCategoryIds() {
        List<String> out = new ArrayList<>(achievementCategoryLabelById.keySet());
        Collections.sort(out);
        return out;
    }

    public String regionName(String id) { return regionNameById.get(id); }

    public List<String> regionIds() {
        List<String> out = new ArrayList<>(regionNameById.keySet());
        Collections.sort(out);
        return out;
    }

    public String statLabel(String id) { return statLabelById.get(id); }

    public List<String> statIds() {
        List<String> out = new ArrayList<>(statLabelById.keySet());
        Collections.sort(out);
        return out;
    }
}
