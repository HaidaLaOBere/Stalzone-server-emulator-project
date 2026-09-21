package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 20-line {@code incident.*} namespace in {@code stalker/lang/en.lang} (part
 * 149, the twenty-ninth concrete namespace built from part 123's follow-up census), modeled with
 * the established generic last-dot-split (id, field) table.
 *
 * <p><b>Real shape, reconciled exactly: 1 + 1 + 18 = 20.</b> {@code
 * incident.battlepack.description} (1 line) and {@code incident.weekly_drop.description} (1
 * line) are real care-package loot-list tooltips, each a real multi-line, {@code \n}-delimited
 * bulleted item list (spare parts, tools, catalysts, armor/weapon component packs, and a choice
 * of medicine). {@code incident.game_class.sample.<id>.name}/{@code .desc} (18 lines, 9 real
 * ids: {@code support}, {@code raider}, {@code stormtrooper}, {@code druid}, {@code sniper},
 * {@code marksman}, {@code healer}, {@code grenadier}, {@code scout}) is a real 9-entry
 * "sample" class roster with a full display name and flavor description for each.
 *
 * <p><b>A real, but cautiously disclosed, name overlap with {@link OperationsCatalog}.</b> {@code
 * incident.game_class.sample.stormtrooper.name}="Assault Trooper" is textually IDENTICAL to
 * {@link OperationsCatalog}'s already-covered {@code mode_classes} text, which names Operations
 * mode's 3-class roster as "Assault Trooper/Support/Operative". This namespace's {@code
 * stormtrooper} id may therefore be the real internal id for that Operations-mode class -- but
 * this {@code incident.game_class.sample} roster has 9 entries, not 3, and most of its other real
 * ids/names ({@code raider}, {@code druid}="Hunter", {@code marksman}="Shooter", {@code
 * healer}="Chemist", {@code grenadier}) have no corresponding entry anywhere in the real Operations
 * 3-class text, so this is disclosed as a suggestive single-name match, not asserted as the same
 * roster (rule 1). Separately, 2 of these 9 real ids ({@code scout}, {@code support}) are also
 * bare-word matches for 2 of {@link net.stalzone.emulator.progression.PlayerClass}'s real 6-member
 * enum (part 66) -- again disclosed as a recurring real class-id vocabulary across at least three
 * independent real rosters (this one, Operations, and {@code PlayerClass}), never merged into one.
 */
public final class IncidentCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private IncidentCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "incident.";

    public static IncidentCatalog load(LangCatalog catalog) {
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
        return new IncidentCatalog(fieldsById);
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
