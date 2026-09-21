package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 36-line {@code arcade.*} namespace in {@code stalker/lang/en.lang} (part 144,
 * the twenty-fourth concrete namespace built from part 123's follow-up census), modeled with the
 * established generic last-dot-split (id, field) table.
 *
 * <p><b>Real shape, reconciled exactly: 20 + 8 + 4 + 4 = 36.</b> {@code
 * arcade.customTags.<id>.text}/{@code .desc} (20 lines, 10 real ids) are real match-modifier
 * badge texts for the "Arcade" session-mode lobby: gear-rank caps ({@code stalker}/{@code
 * veteran}), weapon restrictions ({@code no_shotgun}, {@code sniper_only}), a real named
 * "SPARTAK'S CALIBERS" ammo-themed variant, and three real game-mode names ({@code
 * deathmatch}="DEATHMATCH", {@code teamdeathmatch}="TEAM BATTLE", {@code gungame}="ARMS RACE")
 * plus two real timer-template badges ({@code rank_change}/{@code map_change}, both {@code
 * {time} MIN}).
 *
 * <p>{@code arcade.classes.reichstag_<faction>_<role>.name} (8 lines) gives real Reichstag-mode
 * class display names split by the two real WWII factions, {@code rkka} (Red Army) and {@code
 * wehrmacht}, across 4 real roles ({@code assault}/{@code scout}/{@code engineer}/{@code
 * gunner}): "Red Army Assaulter"/"Wehrmacht Assaulter", etc. This is a further, uncounted entry
 * in the long part-109 Reichstag/RKKA corroboration chain (per the standing note, no new ordinal
 * asserted) -- directly alongside {@link GoLangCatalog}'s already-covered {@code
 * reichstag_rkka_sniper}="Red Army Sniper. Winter 1941"/{@code
 * reichstag_wehrmacht_officer}="Wehrmacht Officer. Eastern Front" naming pattern, confirming the
 * same real RKKA/Wehrmacht role-naming convention extends to these 4 additional roles. {@code
 * arcade.classes.reichstag_<role>.description} (4 lines) are real EMPTY placeholder values
 * (faction-neutral, unlike {@code .name}), disclosed as-is per standing rule 1. {@code
 * arcade.classes.reichstag_<role>.progress} (4 lines) are real level-gate unlock texts (e.g.
 * "Requires Assaulter&mdash;Level {targetLevel}").
 */
public final class ArcadeCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ArcadeCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "arcade.";

    public static ArcadeCatalog load(LangCatalog catalog) {
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
        return new ArcadeCatalog(fieldsById);
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
