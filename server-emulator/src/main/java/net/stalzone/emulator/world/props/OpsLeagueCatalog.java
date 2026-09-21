package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 170. Catalogs a real, previously-uncatalogued 10-line namespace found by continuing the
 * fresh full-file {@code en.lang} re-scan (parts 167-169): {@code ops.leagues.<id>.<field>} -- a
 * real 5-tier ranked-ladder system. Confirmed via {@code grep -c "^ops\."} against the full real
 * file: exactly 10 lines, no other real {@code ops.*} content exists anywhere in the client.
 *
 * <p>Real tier names, by real numeric id: {@code 1}="Absolute Unity", {@code 2}="Ascendant",
 * {@code 3}="Revenant", {@code 4}="Acolyte", {@code 5}="Adept". Each real id also has a real
 * {@code reward_text} -- a short flavor passage repeating the tier name, written entirely in a
 * cultic/hive-mind register ("Unity", "the truth", "you will become one of all", "the
 * understanding of this blessing will follow quickly") distinct in tone from this project's three
 * other already-confirmed real "leagues" ranked-ladder namespaces ({@link DmzLangCatalog}, part
 * 135; {@link InsideCatalog}, part 154; {@link NewYearEventCatalog}, part 157, which explicitly
 * called itself the third recurrence) -- this is a real 4th recurrence of that naming convention,
 * confirmed by the shared literal {@code leagues} path segment, not merely a naming coincidence
 * asserted from similarity alone.
 *
 * <p><b>Tier ordering is NOT confirmed by direct evidence and this class does not assert one.</b>
 * The real numeric ids alone do not disclose which direction is "up": reading the five real
 * {@code reward_text} passages in numeric order (5, 4, 3, 2, 1) traces a plausible
 * initiate-to-ultimate narrative arc -- id 5 ("Adept") reads as a bare joining ("you do not yet
 * understand it... awareness will come soon"), id 4 ("Acolyte") as gaining standing ("welcome you
 * ... into our ranks"), id 3 ("Revenant") as a transformative rebirth ("died... and returned...
 * changed"), id 2 ("Ascendant") as approaching the whole ("becoming a part... of a great
 * algorithm"), and id 1 ("Absolute Unity") as final union ("You|We|All are absolutely one!") --
 * which would make numeric id 5 the LOWEST/initiate tier and id 1 the HIGHEST/final tier, the
 * REVERSE of a naive "1 is first/lowest" reading. This reading is plausible but unproven: nothing
 * in the real client text says a player's numeric id increases or decreases with rank, so this
 * class exposes only the literal real {@code (id, field)} data, deliberately without a {@code
 * rank()}/{@code tierOrder()} method that would assert either direction as fact.
 *
 * <p>Modeled with the project's established generic last-dot-split {@code (id, field)} table
 * after stripping the literal {@code ops.leagues.} prefix, giving ids {@code "1"}-{@code "5"} and
 * fields {@code name}/{@code reward_text} (the only two real fields observed).
 */
public final class OpsLeagueCatalog {

    private static final String PREFIX = "ops.leagues.";

    private final Map<String, Map<String, String>> fieldsById;

    private OpsLeagueCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    public static OpsLeagueCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue; // no real line in this namespace has ever lacked a field suffix
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new OpsLeagueCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) {
        return field(id, "name");
    }

    public String rewardText(String id) {
        return field(id, "reward_text");
    }

    /** Real ids that have at least one real field, in file order ({@code "1"}-{@code "5"} as it happens, since the real file lists them that way) -- not asserted as a tier order, see class doc. */
    public List<String> ids() {
        return new ArrayList<>(fieldsById.keySet());
    }

    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }

    public int size() {
        return fieldsById.size();
    }
}
