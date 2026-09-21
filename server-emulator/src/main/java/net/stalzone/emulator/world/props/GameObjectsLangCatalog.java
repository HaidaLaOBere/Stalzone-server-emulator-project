package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 21-line {@code gameobjects.*} namespace in {@code stalker/lang/en.lang} (part
 * 148, the twenty-eighth concrete namespace built from part 123's follow-up census), modeled with
 * the established generic last-dot-split (id, field) table.
 *
 * <p>Named {@code GameObjectsLangCatalog}, not {@code GameObjectsCatalog}, to avoid confusion with
 * this project's unrelated file-based {@link GameObjectsCatalog} (part 74, real map-decoration
 * asset files and a battle-royale loot-crate taxonomy under {@code modassets/assets/gameobjects/})
 * -- this class instead covers real interaction-VERB tooltip text (e.g. "press E to..."-style
 * prompts), a completely different real content pocket that merely shares the {@code
 * gameobjects} English word.
 *
 * <p><b>Real shape, reconciled exactly: 20 + 1 = 21.</b> {@code
 * gameobjects.action_tip.go_quest.<id>} (20 lines, all sharing id={@code action_tip.go_quest})
 * are real quest-object interaction prompts (e.g. {@code ex_chrism}="Hang the chrism", {@code
 * dig_hole}="Dig a hole", {@code cook_soup}="Cook soup"). Three real ids ({@code
 * dmz_primanka1}/{@code dmz_primanka2}/{@code dmz_primanka3}) share the identical literal text
 * "Place bait", a real DMZ-mode bait-placement interaction, disclosed as-is. One real id ({@code
 * 33627}) is a literal decimal quest/object identifier used directly as the field name rather
 * than a descriptive token, same pattern already seen for literal-digit field names elsewhere in
 * this project (e.g. part 140's {@code daily_bonus.description.summer25.1}). {@code
 * gameobjects.action_tip.safe.requires_key}="Requires" (1 line, a separate id={@code
 * action_tip.safe}) is a real safe/lockbox key-requirement prompt fragment.
 */
public final class GameObjectsLangCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private GameObjectsLangCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "gameobjects.";

    public static GameObjectsLangCatalog load(LangCatalog catalog) {
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
        return new GameObjectsLangCatalog(fieldsById);
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
