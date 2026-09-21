package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 95-line {@code party_missions.*} namespace in {@code stalker/lang/en.lang}
 * (part 133, the twelfth concrete namespace built from part 123's follow-up census) -- a real
 * co-op squad "party mission" objective-tracker system: mission NAME labels plus STATUS-TEXT
 * progress templates (real literal {@code {marker}}/{@code {completed}}/{@code {required}}/
 * {@code {name}}/{@code {status}} placeholder tokens, not substituted by this class).
 *
 * <p>Confirmed by direct count to split into a generic table plus one real flat exception
 * (51 {@code name} + 43 {@code status_text} + 1 {@code name_format} = 95 exactly): almost every
 * line fits {@code party_missions.<id>.<field>} via the established last-dot-split scheme, except
 * {@code party_missions.name_format={name} {status}} itself, which has no second dot at all and
 * is kept as its own flat value, the same generic-table-plus-flat-carve-out shape already
 * established by {@link WorldEventCatalog}/{@link OperationsCatalog}/{@link CheckpointsCatalog}.
 *
 * <p>Real ids cross-reference three already-covered event systems by name: {@code
 * complete_world_event.frontline_*} ids reuse Frontline wave/objective vocabulary (part 130's
 * `world.modifier`/part 111's `WorldEventCatalog` territory), {@code complete_world_event.dmz_*}
 * ids tie to the DMZ/DMZ-2026 thread (part 129's `FrontlineCatalog` `calldown_quest_dmz26`), and
 * several ids reference {@code encaged} (part 122's `GuiLangCatalog` `encaged.*` tutorial-card
 * group). A separate real id family, {@code bolota/frontline_<eventId>}, embeds a literal {@code
 * /} character (a real structural quirk already seen with {@code StalkerLangCatalog}'s (part 119)
 * `approv/mark/<type>` ids and {@code BpSeasonCatalog}'s (part 131) `24/spring`-style ids) and
 * reuses the SAME tail vocabulary as the parallel {@code complete_world_event.frontline_<eventId>}
 * ids -- two real distinct id shapes over overlapping event-name vocabulary, kept as separate real
 * ids rather than merged, since the client itself never merges them.
 */
public final class PartyMissionsCatalog {

    private final Map<String, Map<String, String>> fieldsById;
    private final String nameFormat;

    private PartyMissionsCatalog(Map<String, Map<String, String>> fieldsById, String nameFormat) {
        this.fieldsById = fieldsById;
        this.nameFormat = nameFormat;
    }

    private static final String PREFIX = "party_missions.";
    private static final String NAME_FORMAT_KEY = PREFIX + "name_format";

    public static PartyMissionsCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.equals(NAME_FORMAT_KEY)) {
                continue;
            }
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new PartyMissionsCatalog(fieldsById, catalog.get(NAME_FORMAT_KEY));
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String statusText(String id) { return field(id, "status_text"); }

    /** The one real flat exception, {@code party_missions.name_format={name} {status}}. */
    public String nameFormat() { return nameFormat; }

    /** Real ids with at least one field, sorted (several embed a literal {@code /}). */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
