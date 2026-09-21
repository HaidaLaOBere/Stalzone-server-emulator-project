package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 17-line SINGULAR {@code challenge.*} namespace in {@code
 * stalker/lang/en.lang} (part 152, the thirty-second concrete namespace built from part 123's
 * follow-up census), modeled with the established generic last-dot-split (id, field) table.
 *
 * <p>Named {@code ChallengeCatalog}, not {@code ChallengesCatalog}, to avoid confusion with this
 * project's already-covered PLURAL {@code challenges.*} (part 116, 1,011 real lines spanning
 * nearly every season/event this project has confirmed) -- a genuinely different, much smaller
 * real id space that merely shares the English word. Real ids here embed a literal {@code /}
 * character, handled without special-casing by the same last-dot-split approach already proven
 * for parts 131/133's {@code bpseason}/{@code party_missions}.
 *
 * <p><b>Real shape, reconciled exactly: 11 + 6 = 17.</b> {@code
 * challenge.wild_north/&lt;n&gt;.name} (11 lines, n=1-11) names real "Wild North" open-world
 * challenge objectives: 7 real anti-Murmur-faction objectives (destroying squads, camps, control
 * points, antennas, anomalous structures, posts, bases) plus 4 real mutant-hunting objectives
 * ({@code Skitters}, {@code Ghoul}, {@code Piggies}, {@code Boars} Hunting) -- {@code Boars}
 * corroborates this project's already-confirmed {@link StalkerMobsCatalog} boar-mutant evidence.
 * {@code challenge.episode.wild_north/&lt;n&gt;.name} (6 lines) names real "Wild North" STORY
 * episode challenges: n=1,3,4,5,6,7 -- real episode {@code 2} is a genuine gap in the client data,
 * disclosed as-is, not assumed to exist. Real episode titles include a real informally-punctuated
 * one, {@code n=4}="Get on... a Tractor?".
 */
public final class ChallengeCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ChallengeCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "challenge.";

    public static ChallengeCatalog load(LangCatalog catalog) {
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
        return new ChallengeCatalog(fieldsById);
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
