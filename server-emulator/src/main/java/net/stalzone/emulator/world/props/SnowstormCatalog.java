package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 18-line {@code snowstorm.notf.<phase>_<n>} namespace in {@code
 * stalker/lang/en.lang} (part 150, the thirtieth concrete namespace built from part 123's
 * follow-up census) -- real, richly-written winter-storm announcer flavor text, all sharing one
 * id ({@code notf}), modeled with the established generic last-dot-split (id, field) table.
 *
 * <p><b>A real, richer winter-storm text pool alongside part 141's {@link EjectionCatalog}
 * winter group.</b> {@code EjectionCatalog}'s {@code ejection.winter.notification_prepare.<n>}/
 * {@code .notification_start.<n>} (4 short lines total) already confirmed a real seasonal
 * "winter storm" announcer event. This standalone {@code snowstorm.*} namespace is a separate,
 * MUCH larger and more narratively developed real text pool for what reads as the same real
 * event: 10 real {@code start_<n>} multi-sentence announcer variants (1-10) and 8 real {@code
 * end_<n>} variants (1-8), each written in a distinct in-character narrator voice (a
 * radio-operator persona referencing "the weather tower," a broken thermometer, and their own
 * malfunctioning equipment). Disclosed as a further, richer real text pool for the same general
 * real winter-storm mechanic, not merged with {@code EjectionCatalog}'s shorter pair, since no
 * shared id/key structure ties the two namespaces together directly.
 *
 * <p>One real line contains real, unfiltered profanity as authored by the client (part of {@code
 * end_4}'s text, "fix the ... radio!") -- disclosed here only as the fact that it exists,
 * preserved verbatim in the actual fixture/test data per this project's standing rule 1 (real
 * content is not corrected or sanitized), full text available directly in {@code en.lang}.
 */
public final class SnowstormCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private SnowstormCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "snowstorm.";

    public static SnowstormCatalog load(LangCatalog catalog) {
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
        return new SnowstormCatalog(fieldsById);
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
