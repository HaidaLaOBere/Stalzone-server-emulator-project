package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 1,064-line {@code quest_contract.*} namespace in {@code stalker/lang/en.lang}
 * (part 117) -- a real, previously-uncatalogued quest-naming namespace, distinct from {@link
 * net.stalzone.emulator.session.QuestLog}/{@link net.stalzone.emulator.clientdata.QuestProgress}
 * (which key by real numeric quest ids from client save data, not these real string ids) and
 * distinct from {@code session/save/PlayerNotification}'s notification-toast titles.
 *
 * <p>717 distinct real string quest ids each carry up to two real fields, {@code name} (668 lines,
 * a short category-style label, e.g. {@code wnorth_sniper.name}="Murmur Assassination") and {@code
 * title} (390 lines, the actual in-PDA quest title, e.g. {@code wnorth_sniper.title}="Murmur
 * Neutralization") -- confirmed by direct count that {@code name}/{@code title} are the ONLY two
 * real field suffixes used across the whole namespace (668 + 390 + 6 real flat exceptions = 1,064).
 *
 * <p>Those 6 real flat exceptions, {@code sever_new_contract1}-{@code sever_new_contract6} (no
 * dotted field suffix at all), are a real roster of named contract-giver labels rather than
 * per-quest name/title pairs: {@code sever_new_contract1}="\"Take And Forget\" Tasks", {@code
 * sever_new_contract2}="Partner's Tasks", {@code sever_new_contract3}="Seva Ford Tasks", {@code
 * sever_new_contract4}="Pu'er Tasks", {@code sever_new_contract5}="Jaeger Tasks", {@code
 * sever_new_contract6}="Curie Tasks" -- four of these ({@code Seva Ford}, {@code Pu'er}, {@code
 * Jaeger}, {@code Curie}) are real named contract-giver NPCs/characters this project had not
 * catalogued anywhere before this part, disclosed as real names only, not further investigated.
 * These are exposed separately via {@link #contractGiverLabel} rather than force-fit into the
 * per-quest {@code (id, field)} table.
 */
public final class QuestContractCatalog {

    private final Map<String, Map<String, String>> fieldsById;
    private final Map<String, String> contractGiverLabelById;

    private QuestContractCatalog(Map<String, Map<String, String>> fieldsById,
                                  Map<String, String> contractGiverLabelById) {
        this.fieldsById = fieldsById;
        this.contractGiverLabelById = contractGiverLabelById;
    }

    private static final String PREFIX = "quest_contract.";

    public static QuestContractCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        Map<String, String> contractGiverLabelById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                // Real flat contract-giver labels (sever_new_contract1-6), not per-quest fields.
                contractGiverLabelById.put(rest, catalog.get(key));
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new QuestContractCatalog(fieldsById, contractGiverLabelById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String title(String id) { return field(id, "title"); }

    /** Real ids that have at least one real name/title field, sorted. */
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

    /** Real flat contract-giver label for one of the 6 {@code sever_new_contractN} ids, or null. */
    public String contractGiverLabel(String id) { return contractGiverLabelById.get(id); }

    public List<String> contractGiverIds() {
        List<String> out = new ArrayList<>(contractGiverLabelById.keySet());
        Collections.sort(out);
        return out;
    }
}
