package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 17-line {@code arena.*} namespace in {@code stalker/lang/en.lang} (part 153,
 * the thirty-third concrete namespace built from part 123's follow-up census), modeled with the
 * established generic last-dot-split (id, field) table.
 *
 * <p><b>Real shape, reconciled exactly: 6 + 11 = 17.</b> {@code arena.id.&lt;mapId&gt;.name} (6
 * lines) names 6 real Arena-mode map display names: {@code arena_angar}="Hangar", {@code
 * arena_depo}="Depot", {@code arena_mine}="Mine", {@code arena_sklad}="Warehouse", {@code
 * arena_hospital}="Hospital", {@code arena_peklo}="Inferno" -- corroborates this project's
 * already-confirmed real Arena-mode evidence (part 106's {@link StalkerSoundEventsCatalog}
 * {@code arena/} sound-asset folder). {@code arena.equip_preset.id.&lt;presetId&gt;.name} (11
 * lines) names 11 real Arena-mode loadout presets, including {@code custom}="Your gear" (the
 * player's own equipment, not a preset class).
 *
 * <p><b>Real corroborations and one real internal inconsistency against part 149's {@link
 * IncidentCatalog} "sample" class roster, all disclosed not merged.</b> {@code sniper}="Sniper"
 * and {@code marksman}="Shooter" are EXACT id+display-name matches between this preset roster and
 * {@code IncidentCatalog}'s {@code game_class.sample.*} roster -- the strongest evidence yet that
 * these two rosters share a real underlying id vocabulary. {@code assault}="Assault Trooper" here
 * uses a DIFFERENT real id than {@code IncidentCatalog}'s {@code stormtrooper}="Assault Trooper"
 * for the identical real display name -- a real client naming variance between two systems
 * referencing the same class, disclosed not corrected. Likewise {@code shotgun}="Hunter" here
 * differs in id from {@code IncidentCatalog}'s {@code druid}="Hunter" for the same real display
 * name. Most notably, {@code scout}="Raider" here uses the SAME real id ({@code scout}) as {@code
 * IncidentCatalog}'s {@code scout}="Scout", but the two real display names are genuinely
 * DIFFERENT ("Raider" here vs "Scout" there) -- a real client data inconsistency for the same
 * literal id string, preserved and disclosed exactly as found, never silently corrected (rule 1).
 * The remaining real presets ({@code blower}="Bomber", {@code pistol}="Duelist", {@code
 * pistol2}="Hooligan", {@code knife}="Melee Weapon", {@code fists}="Fist fight") have no
 * corresponding entry in {@code IncidentCatalog}'s roster.
 */
public final class ArenaCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ArenaCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "arena.";

    public static ArenaCatalog load(LangCatalog catalog) {
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
        return new ArenaCatalog(fieldsById);
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
