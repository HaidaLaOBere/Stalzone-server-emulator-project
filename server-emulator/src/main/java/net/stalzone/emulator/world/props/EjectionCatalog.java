package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 188-line {@code ejection.*} namespace in {@code stalker/lang/en.lang} (part
 * 141, the twenty-first concrete namespace built from part 123's follow-up census, and the
 * largest remaining fully-uncovered one at the time this class was written), modeled with the
 * established generic last-dot-split (id, field) table. "Ejection" is this project's already-
 * flagged (part 79/80, disclosed as unconfirmed) English rendering of the real S.T.A.L.K.E.R.-
 * series "emission"/"blowout" mechanic (Russian "&#1074;&#1099;&#1073;&#1088;&#1086;&#1089;", literally "ejection"/"throw-out"); this
 * namespace is REAL announcer flavor-text for that exact mechanic, giving this project's first
 * {@code en.lang}-sourced text evidence for it, on top of {@code emission/EmissionCycle}'s
 * existing part-80 handbook-article evidence.
 *
 * <p><b>Real shape, reconciled exactly: 180 + 4 + 2 + 2 = 188.</b> {@code
 * ejection.stage.notf.<phase>_<n>} (180 lines) is a real pool of FIVE phase categories --
 * {@code preparation}, {@code starting}, {@code storm}, {@code ending}, {@code deadline} -- each
 * with exactly 36 numbered flavor-text variants (id={@code stage.notf}, field=
 * {@code <phase>_<n>}), read client-side as randomized announcer lines for the main eruption
 * event (e.g. {@code storm_1}="Attention! The eruption is starting. Hang in there, fellas!").
 * {@code ejection.winter.notification_prepare.<n>}/{@code .notification_start.<n>} (4 lines, 2
 * variants each) is a real SEPARATE seasonal "winter storm" announcer pool, textually distinct
 * from the main eruption lines (e.g. "The ecologists are predicting a cold snap"). {@code
 * ejection.fallout.notification_prepare}/{@code .notification_start} (2 lines) is a real THIRD,
 * separate "radioactive fallout" announcer pair (single lines, no numbered pool). {@code
 * ejection.shelter.msg.enter}/{@code .exit} (2 lines) are the real in-shelter status toggle texts
 * ("You are in cover" / "You are not in cover").
 *
 * <p><b>A plausible but NOT asserted correspondence to {@link
 * net.stalzone.emulator.emission.EmissionCycle}'s 4-value {@code Phase} enum.</b> The real 5
 * {@code stage.notf} flavor categories ({@code preparation}/{@code starting}/{@code storm}/
 * {@code ending}/{@code deadline}) outnumber {@code EmissionCycle}'s 4 phases ({@code WAITING}/
 * {@code WARNING}/{@code ACTIVE}/{@code ARTEFACT_WINDOW}), so no direct field-to-enum-constant
 * mapping is claimed here -- disclosed as a real, evidenced text pool that plausibly narrates
 * that same 4-stage cycle (a natural reading: {@code preparation}/{@code deadline} during {@code
 * WARNING}, {@code starting}/{@code storm} during {@code ACTIVE}, {@code ending} at the {@code
 * ACTIVE}-to-{@code ARTEFACT_WINDOW} transition), never wired into {@code EmissionCycle} itself
 * per standing rule 1 (no assumption becomes a protocol/behavior spec without direct evidence of
 * the actual trigger mapping).
 */
public final class EjectionCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private EjectionCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "ejection.";

    public static EjectionCatalog load(LangCatalog catalog) {
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
        return new EjectionCatalog(fieldsById);
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
