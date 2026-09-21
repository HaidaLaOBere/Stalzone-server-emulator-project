package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 24-line {@code hint.<id>.header}/{@code .text} namespace in {@code
 * stalker/lang/en.lang} (part 147, the twenty-seventh concrete namespace built from part 123's
 * follow-up census) -- real in-world contextual hint-card copy (12 real ids, each with a header
 * and a body text), modeled with the established generic last-dot-split (id, field) table.
 *
 * <p>Real finds: {@code hint.pda.*}, {@code hint.bolt.*}, {@code hint.wpn_mod.*}, {@code
 * hint.personal_storage.*} are real key-binding tutorial prompts, each body text referencing a
 * real {@code [key:...]} client keybind-template token. {@code
 * hint.signals_Zheltuha.header}="Fight without firearms!" is a real signal-hunt-location-specific
 * hint tied to the real "Zheltuha" location, already independently confirmed by this project's
 * {@link StalkerSoundMiscCatalog} (a real {@code zheltuha/} sound-asset folder) -- disclosed as a
 * further real corroboration of that location name, not a new finding. Three real ids ({@code
 * destroy_wood_board}, {@code search_stash}, {@code destroy_lock}) share the identical literal
 * header text "Tip", disclosed as-is, not corrected. Several real body texts use a literal em
 * dash with no following space ({@code "Remember&#8212;certain planks..."}), a real client
 * punctuation convention preserved verbatim.
 */
public final class HintCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private HintCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "hint.";

    public static HintCatalog load(LangCatalog catalog) {
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
        return new HintCatalog(fieldsById);
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
