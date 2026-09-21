package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 29-line STANDALONE top-level {@code encaged.*} namespace in {@code
 * stalker/lang/en.lang} (part 146, the twenty-sixth concrete namespace built from part 123's
 * follow-up census), modeled with the established generic last-dot-split (id, field) table.
 *
 * <p><b>Genuinely distinct from {@link GuiLangCatalog}'s already-covered {@code gui.encaged.*}</b>
 * (that group is a real tutorial-card system -- class-select, mission, and enemy-warning cards).
 * This standalone prefix is instead a real 4-class ROSTER plus a real post-match SCOREBOARD
 * action-label system for the same "Encaged" asymmetric hunter-vs-survivors mode -- confirmed via
 * direct content comparison, not assumed from the shared {@code encaged} substring.
 *
 * <p><b>Real shape, reconciled exactly: 4 + 25 = 29.</b> {@code encaged.class.<id>.name} (4 lines)
 * is the real survivor class roster: {@code medic}="MEDIC", {@code soldier}="MILITARY", {@code
 * engineer}="ENGINEER", {@code scout}="SCOUT". {@code encaged.score_action.<actionId>} (25 lines,
 * all sharing id={@code score_action}) is a real post-match scoreboard action-label list, split
 * across both asymmetric roles: {@code hunter_*} labels (e.g. {@code
 * hunter_encaged_survivor}="Encaging a survivor", {@code
 * hunter_killed_encaged}="Survivor killed after capture") and {@code survivor_*} labels (e.g.
 * {@code survivor_encaged_help}="Rescuing a teammate from a cage", {@code
 * survivor_kite_hunter}="Hunter distraction").
 *
 * <p>One real client typo disclosed as-is, not corrected: {@code
 * hunter_infect_folder_picking}="Infected iItem picked up" (a literal doubled lowercase "i").
 */
public final class EncagedCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private EncagedCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "encaged.";

    public static EncagedCatalog load(LangCatalog catalog) {
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
        return new EncagedCatalog(fieldsById);
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
