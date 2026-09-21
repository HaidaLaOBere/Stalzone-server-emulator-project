package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 11-line {@code personal_storage.*} namespace in {@code stalker/lang/en.lang}
 * (part 159, the fortieth concrete namespace built from part 123's follow-up census) -- real
 * per-settlement "Personal Storage" shelf-unlock requirement text, modeled with the established
 * generic last-dot-split (id, field) table, no special-casing needed: {@code
 * availability.settlement.<id>} (10 lines) lands as id={@code availability.settlement},
 * field={@code &lt;settlementId&gt;}; the one flat {@code availability.premium} line lands as a
 * SEPARATE id={@code availability}, field={@code premium} -- two distinct real ids the generic
 * split produces naturally.
 *
 * <p><b>The richest reputation-rank corroboration chain this project has built to date, against
 * part 107's {@link CnpsCatalog} {@code cnps.faction.&lt;settlementId&gt;.relation.&lt;level&gt;}
 * ladders.</b> Every one of the 10 real {@code availability.settlement.*} unlock texts names a
 * real settlement/base plus a real top-of-ladder (or near-top) rank title that EXACTLY matches
 * text already confirmed in {@code CnpsCatalog}: {@code csky}="Ataman's HQ&mdash;Vodyanoy (5)"
 * ({@code settlement_stalkers_csky}'s real level-4 top rank "Vodyanoy"), {@code
 * lodochnaya}="Boathouse&mdash;Fisherman (5)" ({@code settlement_bandits_lodochnaya} level-4
 * "Fisherman"), {@code garage}="Fire Station&mdash;Border Guard (5)" ({@code
 * settlement_stalkers_garage} level-4 "Border Guard"), {@code foton2}="Village
 * School&mdash;Brigadier (5)" ({@code settlement_bandits_foton2} level-4 "Brigadier"), {@code
 * rostok}="The Bar&mdash;On the house! (7)" ({@code settlement_rostok} level-6 quoted bartender
 * line "On the house!"), {@code sever}="Drunken Geiger Bar&mdash;Legend of the Zone (7)" ({@code
 * settlement_sever} level-6 top rank "Legend of the Zone"), {@code
 * alliance_stalkers}="Frontier Base&mdash;General (7)\nRise Base&mdash;Zone Guardian (7)" ({@code
 * settlement_duty} level-6 "General" and {@code settlement_freedom} level-6 "Zone Guardian" --
 * "Frontier"/"Rise" also independently re-confirming {@code CnpsCatalog}'s own {@code
 * dolg}="Frontier"/{@code svoboda}="Rise" faction-name crosswalk), {@code
 * alliance_bandits}="Mercenary Base&mdash;Professional (7)\nCovenant Base&mdash;Apostle (7)"
 * ({@code settlement_merc} level-6 "Professional" and {@code settlement_covenant} level-6
 * "Apostle"), {@code wnorth_10}="Drunken Geiger Bar&mdash;Legend of the Zone (10)" ({@code
 * settlement_wnorth} level-10 top rank, also real-word-identical "Legend of the Zone" to {@code
 * sever}'s own top rank despite the two settlements using different-length ladders -- already a
 * disclosed real cross-settlement coincidence in {@code CnpsCatalog} itself), and {@code
 * wnorth_7}="Drunken Geiger Bar&mdash;Interesting drinking buddy (7)" (a real {@code
 * settlement_wnorth} mid-ladder rank not otherwise sampled by {@code CnpsCatalog}'s own doc, but
 * structurally consistent with that same 0-10 ladder).
 *
 * <p><b>An observed, NOT fully consistent numbering correspondence, disclosed exactly as found.</b>
 * For the four real 0-4 ladders (csky/lodochnaya/garage/foton2, 5 real levels) and the four real
 * 0-6 ladders (rostok/sever/duty/freedom/merc/covenant, 7 real levels), this namespace's real
 * "required reputation (N)" number equals (top real CnpsCatalog level index + 1) in every case
 * checked. But for the real 0-10 {@code settlement_wnorth} ladder (11 real levels), {@code
 * wnorth_10}'s real required number is literally "(10)", equal to the raw top level index, NOT
 * level+1 as the other two groups would predict. This project does not have enough independent
 * evidence to assert a single universal formula relating the two real numbering systems --
 * disclosed as a strong but imperfect correspondence per this project's standing rule 1, not
 * corrected or forced into one rule.
 */
public final class PersonalStorageCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private PersonalStorageCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "personal_storage.";

    public static PersonalStorageCatalog load(LangCatalog catalog) {
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
        return new PersonalStorageCatalog(fieldsById);
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
