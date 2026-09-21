package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 87-line {@code ravent.*} namespace in {@code stalker/lang/en.lang} (part 132,
 * the eleventh concrete namespace built from part 123's follow-up census) -- the real player
 * passive-ability roster and enemy wave/round-name roster for the same real Slavic-folklore Arena
 * mode already surfaced twice before from independent angles: part 130's {@link
 * WorldModifierCatalog} `ravent_level_*` spawn-rate modifiers, and part 131's {@link
 * BpSeasonCatalog} `24/spring` "Arena of Slavs" seasonal event (Max Crow, the idol of Perun,
 * bogatyrs). This part is the third and richest angle on that same real mode -- its actual named
 * enemy waves and player perks.
 *
 * <p>Confirmed by direct count to be a single real group with zero flat exceptions: all 87 lines
 * fit {@code ravent.<id>.<field>} via the same last-dot-split generic scheme already used
 * throughout this project. Splits into 2 real shapes: {@code ability.passive.<abilityId>.name}/
 * {@code .desc} (22 lines, 11 real named player passive perks, e.g. {@code gladiator_health}=
 * "Life-Giving Cross", {@code gladiator_damage}="Bogatyr Strength" -- each `.desc` carries real
 * literal tiered-scaling placeholder syntax verbatim, e.g. "increases by %1%10&#167;r/%2%20&#167;
 * r/%3%30&#167;r units", not normalized or parsed further by this class) and {@code
 * waveTypes.<waveId>.name} (65 lines, flat, real named enemy encounter waves: a tiered
 * "Nth_base_n"/"Nth_boss_n" ladder across 5 real difficulty tiers (first/second/third/fourth/
 * fifth), plus 16 real named special "boss_&lt;name&gt;" encounters drawn from genuine Slavic
 * folklore -- {@code boss_perun}, {@code boss_hors}, {@code boss_stribog}, {@code boss_leshiy},
 * {@code boss_kikimora}, {@code boss_tugarin}, {@code boss_gorynych}, {@code boss_kojemyaka} --
 * alongside comedic tier-ladder wave titles ("Slavic Chainsaw Massacre", "Battle of Dogs",
 * "Green serpent", "Pigstorm").
 */
public final class RaventCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private RaventCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "ravent.";

    public static RaventCatalog load(LangCatalog catalog) {
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
        return new RaventCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String desc(String id) { return field(id, "desc"); }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
