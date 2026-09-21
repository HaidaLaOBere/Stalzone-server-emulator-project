package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 197-line {@code core.*} namespace in {@code stalker/lang/en.lang} (part 128,
 * the fifth concrete namespace built from part 123's follow-up census) -- almost entirely a real
 * in-game "Handbook" help/wiki feature ({@code core.handbook.*}, 196 lines), plus one real
 * stray tooltip line under a different, misspelled top-level id ({@code core.tooltop.*} -- a real
 * client typo for "tooltip", disclosed as-is rather than silently corrected).
 *
 * <p>Confirmed by direct count that {@code core.handbook.*} splits into 6 real groups with no
 * leftover lines (94 + 71 + 18 + 6 + 6 + 1 = 196): {@code id.<entryId>.title}/{@code .main} (94
 * lines, 47 real wiki-style entries -- see below), {@code category.<path>} (71, flat category
 * display labels, several real paths nested several dots deep, e.g. {@code
 * category.articles.article.arthunt}="Zone Phenomena"), {@code tag.<tagId>} (18, flat filter-tag
 * labels: GENERAL, ZONE, SURVIVAL, PvP, RESOURCES, BARTERING, TRADING, GROUP ACTIVITIES, RESEARCH,
 * ARTHUNT, CRAFTING, THREATS, OPEN WORLD, TASKS, FACTIONS, SEASON, CHARACTER PROGRESSION, plus a
 * real leftover {@code testtag}="TEST TAG"), {@code subcategory.<path>} (6, flat, only under the
 * {@code articles.article.*} category tree), {@code report.option_<n>} (6, flat -- real "report
 * this article" feedback reasons for a content-moderation feature: too complicated, missing info,
 * outdated, formatting issues, factual errors, overloaded with text), {@code
 * notification.custom_1}="Custom notification description" (1, a real but generic-sounding
 * placeholder-style string, disclosed as-is).
 *
 * <p>The 47 real {@code id.<entryId>} wiki entries are a mixed bestiary-plus-mechanics glossary,
 * each with a {@code title} and a full {@code main} body (often several paragraphs). Confirmed
 * real titles for the entries that read as mutant-bestiary ids: {@code boar}="Boar", {@code
 * chimera}="Fiend", {@code krovosos}="Ghoul" (this client's own English title for what the id's
 * Russian root literally means "bloodsucker" -- a real localization choice, not this project's
 * translation), {@code snork}="Skitter" (directly corroborating part 123's census find {@code
 * territories.snork.name}="Skitter Territory"), {@code tushkan}="Rat", {@code psidog}="Psy-Hound",
 * {@code pseudogigant}="Behemoth", {@code pseudodog}="Wild Hound", {@code dog}="Mutt", {@code
 * kisel}="Aspic". One non-mutant entry sampled in full: {@code voronka}="Whirlpool", a real
 * gravitational-anomaly lore entry with a full multi-paragraph description and a listed real
 * artifact roster (Sponge, Prima, Shrimp, Golden Prima, Amberite, Cryogen, Dark Crystal).
 *
 * <p>{@code core.tooltop.locitem.loc_type.frontline}="Conflict zone" is the one real line outside
 * {@code handbook.*} -- a location-type tooltip label for the Frontline game mode, filed under a
 * real client typo ({@code tooltop}, not {@code tooltip}) rather than this project's own error.
 */
public final class HandbookCatalog {

    private final Map<String, Map<String, String>> articleFieldsById;
    private final Map<String, String> categoryLabelByPath;
    private final Map<String, String> subcategoryLabelByPath;
    private final Map<String, String> tagLabelById;
    private final Map<String, String> reportOptionById;
    private final Map<String, String> notificationById;
    private final String frontlineLocTypeTooltip;

    private HandbookCatalog(
            Map<String, Map<String, String>> articleFieldsById,
            Map<String, String> categoryLabelByPath,
            Map<String, String> subcategoryLabelByPath,
            Map<String, String> tagLabelById,
            Map<String, String> reportOptionById,
            Map<String, String> notificationById,
            String frontlineLocTypeTooltip) {
        this.articleFieldsById = articleFieldsById;
        this.categoryLabelByPath = categoryLabelByPath;
        this.subcategoryLabelByPath = subcategoryLabelByPath;
        this.tagLabelById = tagLabelById;
        this.reportOptionById = reportOptionById;
        this.notificationById = notificationById;
        this.frontlineLocTypeTooltip = frontlineLocTypeTooltip;
    }

    private static final String ID_PREFIX = "core.handbook.id.";
    private static final String CATEGORY_PREFIX = "core.handbook.category.";
    private static final String SUBCATEGORY_PREFIX = "core.handbook.subcategory.";
    private static final String TAG_PREFIX = "core.handbook.tag.";
    private static final String REPORT_PREFIX = "core.handbook.report.";
    private static final String NOTIFICATION_PREFIX = "core.handbook.notification.";
    private static final String FRONTLINE_TOOLTIP_KEY = "core.tooltop.locitem.loc_type.frontline";

    public static HandbookCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> articleFieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(ID_PREFIX)) {
            String rest = key.substring(ID_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            articleFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, String> categoryLabelByPath = flatMap(catalog, CATEGORY_PREFIX);
        Map<String, String> subcategoryLabelByPath = flatMap(catalog, SUBCATEGORY_PREFIX);
        Map<String, String> tagLabelById = flatMap(catalog, TAG_PREFIX);
        Map<String, String> reportOptionById = flatMap(catalog, REPORT_PREFIX);
        Map<String, String> notificationById = flatMap(catalog, NOTIFICATION_PREFIX);
        String frontlineLocTypeTooltip = catalog.get(FRONTLINE_TOOLTIP_KEY);

        return new HandbookCatalog(articleFieldsById, categoryLabelByPath, subcategoryLabelByPath,
                tagLabelById, reportOptionById, notificationById, frontlineLocTypeTooltip);
    }

    private static Map<String, String> flatMap(LangCatalog catalog, String prefix) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(prefix)) {
            out.put(key.substring(prefix.length()), catalog.get(key));
        }
        return out;
    }

    public String articleTitle(String id) { return articleField(id, "title"); }
    public String articleMain(String id) { return articleField(id, "main"); }

    public String articleField(String id, String field) {
        Map<String, String> fields = articleFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real wiki-entry ids with at least one field, sorted. */
    public List<String> articleIds() {
        List<String> out = new ArrayList<>(articleFieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    public String categoryLabel(String path) { return categoryLabelByPath.get(path); }
    public String subcategoryLabel(String path) { return subcategoryLabelByPath.get(path); }
    public String tagLabel(String id) { return tagLabelById.get(id); }
    public String reportOption(String id) { return reportOptionById.get(id); }
    public String notification(String id) { return notificationById.get(id); }

    /** The real {@code core.tooltop.locitem.loc_type.frontline} location-type tooltip, or null. */
    public String frontlineLocTypeTooltip() { return frontlineLocTypeTooltip; }
}
