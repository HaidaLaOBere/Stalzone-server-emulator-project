package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 1,011-line {@code challenges.*} namespace in {@code stalker/lang/en.lang}
 * (part 116) -- real in-game challenge/achievement task text: what to do ({@code name}), flavor
 * text ({@code description}), a UI hint ({@code hint}), and a separate achievement title ({@code
 * label}), spanning nearly every season and live-service event this project has confirmed
 * (Reichstag, Encaged, DMZ26/"Frost Troopers", the New Year tree, Triumph leaderboards, and more).
 *
 * <p>Every one of the 1,011 real lines fits one single shape: {@code challenges.<id>.<field>},
 * where {@code field} is always the terminal simple token ({@code name}/{@code description}/{@code
 * hint}/{@code label}, plus a handful of real one-off exceptions covered below) and {@code id} is
 * everything before it -- including embedded {@code /} path segments (e.g. {@code
 * season.summer26/pg_challenges/g4/8}) and, for several real ids, embedded dots of its own (e.g.
 * {@code season.summer26/pg_challenges/g4/8.na}, a distinct real sub-id from {@code
 * season.summer26/pg_challenges/g4/8} itself -- disclosed as two separate real ids rather than
 * merged, since nothing confirms {@code .na} means "no-amount"/abbreviated variant beyond the
 * naming pattern itself). This lets the WHOLE namespace be modeled with a single generic {@code
 * (id, field)} table via {@link #field}, confirmed by direct count to have zero flat exceptions
 * (every real line has at least one dot after the {@code challenges.} prefix) -- unlike {@link
 * WorldEventCatalog} (part 111) or {@link OperationsCatalog} (part 113), which each needed a
 * separate flat-line carve-out.
 *
 * <p>Five real one-off field names round out the small tail beyond {@code name}/{@code
 * description}/{@code hint}/{@code label}: {@code newyear24.first_week}/{@code
 * .second_week}/{@code .third_week} (3 real week-label lines filed directly on the {@code
 * newyear24} group id itself, not under any numbered sub-challenge), {@code
 * achievements.complete=Complete the \{key\} achievement} (a real template string, id {@code
 * achievements}, field {@code complete}), and {@code upg_motif.fmg9_bureau.desc} (the one real id
 * in this whole namespace using the short field name {@code desc} instead of {@code
 * description}) -- all handled by the same generic split, no special-casing required.
 *
 * <p>{@code reichstag/1}-{@code reichstag/8} (8 lines, real Assault-on-the-Reichstag session-mode
 * challenge tasks -- "Kill \{amount\} players while playing the Assaulter/Scout/Engineer/Machine
 * Gunner class", "Score \{amount\} capture/defense points") is disclosed per this project's part-109
 * standing note as simply another entry in the long Reichstag corroboration chain, with no new
 * ordinal number asserted.
 *
 * <p>The real {@code label} field (60 lines) surfaces this project's densest single block of
 * pop-culture-reference achievement titles found in one namespace: {@code
 * summer26/pg_challenges/g1/7.label=Roadside Picnic} (the real 1972 Strugatsky brothers novel the
 * original S.T.A.L.K.E.R. game series itself draws from), {@code g4/1.label=Hacksaw Ridge} (a real
 * film title), {@code g2/4.na.label=Welcome to the Dungeon}, {@code g3/3.na.label=The Cave of
 * Wonders} (Aladdin), {@code g3/7.na.label=Ohio Final Boss} (a real internet-meme reference), {@code
 * g1/3.label=Money Changer}/{@code .na.label=No ATMs in the Zone} -- disclosed as real client text,
 * not investigated further per rule 1.
 */
public final class ChallengesCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ChallengesCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "challenges.";

    public static ChallengesCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                // No real line in this namespace lacks a field suffix (confirmed by direct count),
                // but skip defensively rather than guess if a future content update adds one.
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new ChallengesCatalog(fieldsById);
    }

    /** Real raw field value for {@code id}/{@code field} (e.g. {@code "name"}, {@code "label"}), or null. */
    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String description(String id) { return field(id, "description"); }
    public String hint(String id) { return field(id, "hint"); }
    public String label(String id) { return field(id, "label"); }

    /** Real challenge/group ids that have at least one real field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real field-name keys present for this id, or empty if the id is unknown. */
    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }
}
