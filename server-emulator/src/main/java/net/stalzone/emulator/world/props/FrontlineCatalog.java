package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Catalogs the real 192-line {@code frontline.*} namespace in {@code stalker/lang/en.lang} (part
 * 129, the sixth concrete namespace built from part 123's follow-up census) -- the real
 * "Frontline" large-scale SSPP-vs-Murmur war-campaign game mode. This is the most narratively
 * significant namespace this project has catalogued from a single {@code en.lang} prefix: 60 of
 * its 192 lines are full multi-paragraph campaign BRIEFING TEXT, not just short display labels.
 *
 * <p>Confirmed by direct count to split into 4 real groups with no leftover lines (60 + 129 + 2 +
 * 1 = 192, itself confirmed zero flat exceptions -- every line fits {@code
 * frontline.<id>.<field>}): {@code briefing_<series>_<n>.text} (60 lines, 4 real series -- {@code
 * s} and {@code s2} (10 and 20 entries), {@code n} and {@code n2} (10 and 20 entries), read as two
 * campaign directions (South, North) each with two escalating phases), {@code
 * calldown.<itemId>.<field>} (129 lines, 40 real airstrike/ordnance items), {@code
 * calldown_quest_dmz26.name}/{@code .desc} (2 lines, one real DMZ-2026-tie-in quest, "Thousand
 * Leagues Cargo 200" -- distinct from the {@code calldown.*} item roster, its own real id), {@code
 * bonus_event.completion} (1 line, a real squad-credited bonus-payout message template).
 *
 * <p>The 60 real briefing lines are a substantial, previously-uncaptured piece of this project's
 * running SSPP/Murmur conflict thread (parts 105/107/109/123/126): they name, corroborate, or
 * extend several already-confirmed real locations and threads from an independent source --
 * the Swamps, Roadside, Fire Station, School, Radar Station bunker, {@code Lyubech-3}, {@code
 * Boral} (both from part 126's {@link MapLangCatalog}), and {@code Chernobyl-4}/{@code Ch-4} (the
 * same real former SSPP staging ground part 126 disclosed) -- and introduce two real named
 * elements not seen anywhere else in this project before: a named military operation, "Operation
 * Pole," and two real named archianomalies, "Sauna" and "Evil Well." (Consistent with this
 * project's Reichstag-corroboration discipline, these are disclosed as further real entries in an
 * existing thread, not treated as newly "confirming" anything that needed confirmation.) Full
 * text is kept verbatim in this class's test fixture for regression purposes; only short excerpts
 * are quoted here.
 *
 * <p>{@code calldown.<itemId>.<field>} (40 real items, e.g. {@code fab100}, {@code rbk250}, {@code
 * zab100}, {@code pfm1}, {@code helic5}, {@code flamethrower}, {@code m249}) gives real airstrike
 * flavor text ({@code desc}) and short real property tags ({@code prop1}-{@code prop4}, e.g.
 * "HIGH DAMAGE", "PRECISION TARGETING", "LARGE COVERAGE AREA") -- not every item has every field
 * (39 of 40 have a {@code name}; only 2 real items, {@code flamethrower} and {@code m249}, carry a
 * fourth property).
 */
public final class FrontlineCatalog {

    private final Map<String, List<String>> briefingTextBySeries;
    private final Map<String, Map<String, String>> calldownFieldsById;
    private final Map<String, String> calldownQuestDmz26FieldById;
    private final Map<String, String> bonusEventFieldById;

    private FrontlineCatalog(
            Map<String, List<String>> briefingTextBySeries,
            Map<String, Map<String, String>> calldownFieldsById,
            Map<String, String> calldownQuestDmz26FieldById,
            Map<String, String> bonusEventFieldById) {
        this.briefingTextBySeries = briefingTextBySeries;
        this.calldownFieldsById = calldownFieldsById;
        this.calldownQuestDmz26FieldById = calldownQuestDmz26FieldById;
        this.bonusEventFieldById = bonusEventFieldById;
    }

    private static final String PREFIX = "frontline.";
    private static final String CALLDOWN_PREFIX = "calldown.";
    private static final String CALLDOWN_QUEST_DMZ26_PREFIX = "calldown_quest_dmz26.";
    private static final String BONUS_EVENT_PREFIX = "bonus_event.";
    private static final Pattern BRIEFING = Pattern.compile("^briefing_(s2|n2|s|n)_(\\d+)\\.text$");

    public static FrontlineCatalog load(LangCatalog catalog) {
        Map<String, Map<Integer, String>> briefingByIndexTmp = new LinkedHashMap<>();
        Map<String, Map<String, String>> calldownFieldsById = new LinkedHashMap<>();
        Map<String, String> calldownQuestDmz26FieldById = new LinkedHashMap<>();
        Map<String, String> bonusEventFieldById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            String value = catalog.get(key);

            Matcher briefingMatch = BRIEFING.matcher(rest);
            if (briefingMatch.matches()) {
                String series = briefingMatch.group(1);
                int index = Integer.parseInt(briefingMatch.group(2));
                briefingByIndexTmp.computeIfAbsent(series, k -> new TreeMap<>()).put(index, value);
                continue;
            }

            if (rest.startsWith(CALLDOWN_QUEST_DMZ26_PREFIX)) {
                calldownQuestDmz26FieldById.put(rest.substring(CALLDOWN_QUEST_DMZ26_PREFIX.length()), value);
                continue;
            }

            if (rest.startsWith(CALLDOWN_PREFIX)) {
                String itemRest = rest.substring(CALLDOWN_PREFIX.length());
                int lastDot = itemRest.lastIndexOf('.');
                if (lastDot < 0) {
                    continue;
                }
                String id = itemRest.substring(0, lastDot);
                String field = itemRest.substring(lastDot + 1);
                calldownFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                continue;
            }

            if (rest.startsWith(BONUS_EVENT_PREFIX)) {
                bonusEventFieldById.put(rest.substring(BONUS_EVENT_PREFIX.length()), value);
            }
        }

        Map<String, List<String>> briefingTextBySeries = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, String>> entry : briefingByIndexTmp.entrySet()) {
            briefingTextBySeries.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }

        return new FrontlineCatalog(briefingTextBySeries, calldownFieldsById,
                calldownQuestDmz26FieldById, bonusEventFieldById);
    }

    /** Real ordered briefing paragraphs (index 0, 1, 2, ...) for this series ({@code "s"}, {@code "s2"}, {@code "n"}, {@code "n2"}), or empty. */
    public List<String> briefingText(String series) {
        List<String> lines = briefingTextBySeries.get(series);
        return lines == null ? List.of() : lines;
    }

    /** Real briefing series ids this catalog has data for, sorted. */
    public List<String> briefingSeriesIds() {
        List<String> out = new ArrayList<>(briefingTextBySeries.keySet());
        Collections.sort(out);
        return out;
    }

    public String calldownField(String itemId, String field) {
        Map<String, String> fields = calldownFieldsById.get(itemId);
        return fields == null ? null : fields.get(field);
    }

    public String calldownName(String itemId) { return calldownField(itemId, "name"); }
    public String calldownDesc(String itemId) { return calldownField(itemId, "desc"); }

    /** Real calldown item ids with at least one field, sorted. */
    public List<String> calldownIds() {
        List<String> out = new ArrayList<>(calldownFieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    public String calldownQuestDmz26Field(String field) { return calldownQuestDmz26FieldById.get(field); }

    public String bonusEventField(String field) { return bonusEventFieldById.get(field); }
}
