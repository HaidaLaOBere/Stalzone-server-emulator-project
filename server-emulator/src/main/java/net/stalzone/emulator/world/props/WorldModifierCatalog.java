package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 138-line {@code world.*} namespace in {@code stalker/lang/en.lang} (part 130,
 * the seventh concrete namespace built from part 123's follow-up census) -- real Arena-mode
 * "world modifier" buff/debuff descriptions. Named {@code WorldModifierCatalog}, not {@code
 * WorldCatalog}, to avoid any confusion with the unrelated already-covered {@code we.*}
 * namespace ({@link WorldEventCatalog}, part 111, "World Events") -- checked and confirmed a
 * genuinely separate real prefix and real feature, not a naming variant of the same system.
 *
 * <p>Confirmed by direct count to be a single real group with zero flat exceptions: all 138 lines
 * fit {@code world.modifier.<id>.name}/{@code .description} (69 real modifiers x 2 fields).
 * Several real ids share the {@code ravent_level_*} prefix (e.g. {@code
 * ravent_level_electra_activity}="Proto-Discharge Activity", {@code
 * ravent_level_lighter_activity}="Proto-Lighter Activity", {@code
 * ravent_level_funnel_activity}="Proto-Whirlpool Activity") -- real anomaly-spawn-rate modifiers
 * tying directly to the still-uncovered top-level {@code ravent.*} namespace from part 123's
 * census (Arena-mode wave/round names), not modeled by this class.
 */
public final class WorldModifierCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private WorldModifierCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "world.modifier.";

    public static WorldModifierCatalog load(LangCatalog catalog) {
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
        return new WorldModifierCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String description(String id) { return field(id, "description"); }

    /** Real modifier ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
