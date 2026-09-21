package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 16-line {@code inside.*} namespace in {@code stalker/lang/en.lang} (part 154,
 * the thirty-fourth concrete namespace built from part 123's follow-up census), modeled with the
 * established generic last-dot-split (id, field) table.
 *
 * <p><b>Real shape, reconciled exactly: 2 + 2 + 6 + 6 = 16.</b> {@code
 * inside.ability.active.inside_recovery.name}="Burnt Onyx"/{@code .description} (2 lines) is a
 * real active ability usable as a med kit. {@code
 * inside.ability.passive.phantom_cure_buffs.name}="Altar of Cleansing"/{@code .description} (2
 * lines) is a real passive gravity-reduction ability. {@code inside.leagues.&lt;n&gt;.name} (6
 * lines) and {@code inside.leagues.&lt;n&gt;.reward_text} (6 lines) give the real numbered "Back
 * to the Inside Out" ranked-event ladder with full reward-letter flavor text for each rank.
 *
 * <p><b>A CONFIRMED crosswalk, resolving part 139's suggestive one.</b> Part 139's {@link
 * ItemOriginsCatalog} found a real {@code inside_league_&lt;codename&gt;} naming series and
 * inferred which numbered league each of 6 codenames belonged to purely from parallel text
 * content, without ever seeing the actual numbered ladder. This standalone {@code
 * inside.leagues.&lt;n&gt;} namespace IS that real numbered ladder, and its 6 real names match
 * {@code ItemOriginsCatalog}'s inferred mapping exactly: {@code leagues.1}="Legend of the Inside
 * Out" ({@code legend}), {@code leagues.2}="Risen" ({@code master}), {@code
 * leagues.3}="Phantom" ({@code veteran}), {@code leagues.4}="Distorted" ({@code stalker}),
 * {@code leagues.5}="Devastated" ({@code newbie}), {@code leagues.6}="Shadow" ({@code
 * picklock}) -- upgrading part 139's inference to a directly confirmed real crosswalk.
 */
public final class InsideCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private InsideCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "inside.";

    public static InsideCatalog load(LangCatalog catalog) {
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
        return new InsideCatalog(fieldsById);
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
