package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 16-line {@code blizzard.*} namespace in {@code stalker/lang/en.lang} (part
 * 155, the thirty-fifth concrete namespace built from part 123's follow-up census), modeled with
 * the established generic last-dot-split (id, field) table.
 *
 * <p><b>Real shape, reconciled exactly: 11 + 3 + 2 = 16.</b> {@code blizzard.event.<field>} (11
 * lines, one shared id {@code event}) gives real announcer/outcome notification text for a
 * two-sided PvPvE base-defense world event: preparation/action start text, four distinct
 * win/defeat outcome variants split by side ({@code end_human_win}/{@code end_human_defeat}/
 * {@code end_hunter_win}/{@code end_hunter_defeat}), structure-damage notifications for two real
 * defended structure types ({@code fireplace_destroyed}, {@code crystal_destroyed}, each with a
 * matching {@code one_&lt;x&gt;_left} warning), and an {@code end_notification} storm-departure
 * warning. {@code blizzard.event.props.class.&lt;id&gt;.name} (3 lines) names 3 real playable
 * classes for the "hunter" side: {@code hunter}="Hunter", {@code shaman}="Shaman", {@code
 * tank}="Thug". {@code blizzard.base.&lt;id&gt;} (2 lines) names 2 real defended bases: {@code
 * stalker}="Stalker Base", {@code bandit}="Bandit Base".
 *
 * <p><b>A real narrative pairing, disclosed not merged, with two independently-confirmed
 * namespaces.</b> The real text reads as a two-sided event: a "human" side (Stalker/Bandit Base)
 * defends real "heating installations" against "mutants"/"the adepts," while a real "Frost
 * Fighters" faction defends real "Crystals" against the Blizzard's own "hunter"/"shaman"/"thug"
 * forces (referred to as "heretics" from the hunter side's own notification text). This real
 * "Frost Fighters" name is the SAME real faction name already confirmed by part 111's {@link
 * WorldEventCatalog} seasonal rescue event {@code we.ny25}="Help the Frost Fighters" -- an
 * independent real corroboration that this Blizzard base-defense event and that seasonal rescue
 * event share a real in-fiction faction, disclosed as a narrative link only, no shared id/key
 * structure ties the two namespaces together directly. Separately, {@code
 * end_human_defeat_notification}'s real reference to "the Sovkhoz... swarming with their spawn"
 * independently reconfirms part 116's {@link net.stalzone.emulator.world.LocationNameRegistry}
 * real {@code location.sovhoz.name=Sovkhoz of Shevchenko} as this event's real home location.
 *
 * <p>Not merged with part 150's {@link SnowstormCatalog}: that namespace is real winter-storm
 * announcer flavor text with no base-defense/faction/class content at all, a genuinely distinct
 * real winter-themed system, disclosed as a thematic pairing only.
 */
public final class BlizzardCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private BlizzardCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "blizzard.";

    public static BlizzardCatalog load(LangCatalog catalog) {
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
        return new BlizzardCatalog(fieldsById);
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
