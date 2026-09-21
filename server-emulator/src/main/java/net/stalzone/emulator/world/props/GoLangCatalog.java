package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 2,613-line {@code go.*} namespace in {@code stalker/lang/en.lang} (part 120)
 * -- named {@code GoLangCatalog} rather than {@code GameObjectCatalog}/{@code GameObjectsCatalog}
 * to avoid confusion with the existing, unrelated, file-based {@link GameObjectsCatalog} (part 74,
 * models the real {@code modassets/assets/gameobjects/} asset folder). {@code go} most plausibly
 * abbreviates "Game Object" given this namespace's content (in-world interactable props, hazards,
 * and installations, each with a display name and sometimes flavor text) -- disclosed as a
 * plausible reading of the prefix, NOT a client-confirmed expansion, per rule 1.
 *
 * <p>A direct count established this is the SECOND namespace this project has found (after part
 * 116's {@link ChallengesCatalog}) with zero real flat exceptions: every one of the 2,613 lines
 * fits the shape {@code go.<id>.<field>}, letting the whole namespace be modeled with a single
 * generic {@code (id, field)} table -- 2,413 distinct real object ids, carrying one of 10 real
 * field names ({@code name}: 2,402 lines; {@code additional_stats_tip}: 88; {@code hint}: 71;
 * {@code tip_action}/{@code description}: 17 each; {@code lore}: 12; {@code notify}/{@code
 * activation_text}: 2 each; {@code text}/{@code desc}: 1 each).
 *
 * <p>The {@code misc.<id>} sub-family (326 of the 2,413 ids) is itself a real mixed bag: anomaly-
 * research/artifact-extraction installations (e.g. {@code misc.extractor}="Anomaly Research
 * Installation", {@code misc.artefact_extractor}="Noo-converter"), and a large real {@code
 * misc.reichstag_*} tail naming actual WWII vehicles, props, and soldier roles for the Reichstag
 * mode -- real named tanks/planes ({@code reichstag_t34}="Т-34-85" [sic, Cyrillic Т in the English
 * client text], {@code reichstag_panther}="Panther", {@code reichstag_tiger}="Tiger", {@code
 * reichstag_junkers87}="Junkers Ju 87") and real dated soldier-role labels ({@code
 * reichstag_rkka_sniper}="Red Army Sniper. Winter 1941", {@code
 * reichstag_wehrmacht_officer}="Wehrmacht Officer. Eastern Front"). Disclosed per the part-109
 * standing note as further uncounted entries in the long Reichstag corroboration chain.
 *
 * <p>Other real id families found by sampling rather than exhaustively enumerated here (the whole
 * point of the single generic table is that no enumeration is needed to use the data): {@code
 * exp.<id>} (real explosive/hazard props -- {@code claymore}="M18 Claymore", {@code
 * claymore_reich}="Trip Wire Mine", {@code stretch}="RGD-5 Trip Wire", {@code psymine}="Psy-mine");
 * {@code ops.<id>} (real "Phase Battery" props in Red/Green/Blue, tying to part 113's {@link
 * OperationsCatalog} "breach"/"focus"/"sea_alienation" Operations); {@code clan.<id>} (real
 * clan-base building props -- gabions, barricades -- plus a {@code clan.extractor}="Chronodevice"
 * distinct from the {@code misc} extractors above); {@code reward_beacon.<id>.<subId>} (real
 * stockpile-reward prop names); and a very large real long tail of {@code tutor_*}/{@code
 * tutor_hint_<n>}/{@code tutor_descent_<n>} etc. ids (in-world tutorial-popup trigger objects,
 * each with a {@code hint}/{@code name} pair, e.g. {@code tutor_hint_0}: hint="Press [#key.jump] to
 * jump.", name="Hint (Jumping)").
 */
public final class GoLangCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private GoLangCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "go.";

    public static GoLangCatalog load(LangCatalog catalog) {
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
        return new GoLangCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String description(String id) { return field(id, "description"); }
    public String lore(String id) { return field(id, "lore"); }
    public String hint(String id) { return field(id, "hint"); }

    /** Real object ids that have at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
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
}
