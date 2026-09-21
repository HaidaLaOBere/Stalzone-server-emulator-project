package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 41-line {@code savepoints.id.<savepointId>.name} namespace in {@code
 * stalker/lang/en.lang} (part 142, the twenty-second concrete namespace built from part 123's
 * follow-up census) -- real respawn-point display labels, every one of the 41 real lines ending
 * in the literal {@code .name} tail, modeled with the established flat PREFIX/SUFFIX strip (same
 * shape as {@link TileCatalog}/{@link DeliveryCatalog}).
 *
 * <p><b>A real confirmed corroboration with {@link net.stalzone.emulator.world.LocationNameRegistry}
 * and this project's Ataman's-HQ/settlement evidence chain.</b> {@code
 * savepoints.id.fallback_stalkers.name}="Ataman's HQ" and {@code
 * savepoints.id.fallback_bandits.name}="Boathouse" reuse the EXACT same real display names as
 * {@code location.atamans_hq.name}/{@code location.boat_station.name} (part 105's {@code
 * LocationNameRegistry} Javadoc, itself cross-confirmed against {@code settlement.id.csky.title}
 * in part 123's census) -- a real, evidenced "which faction currently holds this named location"
 * fallback-respawn convention, not a new independent finding.
 *
 * <p>Other real ids: 5 real {@code kontrolnaya_tochka_<n>} ("Checkpoint_1".."Checkpoint_5") plus
 * 4 more real {@code poligon_kontrolnaya_tochka[_<n>]} ids all literally named plain
 * "Checkpoint" -- a real client naming collision across genuinely different savepoint ids,
 * disclosed not corrected. Several real {@code isolator_room_<n>}/{@code isolator_lobby} ids
 * share the literal name "Elevator" for the same reason. {@code school_1}/{@code school_2} both
 * ="School beginning" and {@code school_end_1}/{@code school_end_2} both ="School ending" -- two
 * further real duplicate-name pairs, same disclosed-not-corrected pattern.
 */
public final class SavepointsCatalog {

    private final Map<String, String> nameById;

    private SavepointsCatalog(Map<String, String> nameById) {
        this.nameById = nameById;
    }

    private static final String PREFIX = "savepoints.id.";
    private static final String SUFFIX = ".name";

    public static SavepointsCatalog load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (!key.endsWith(SUFFIX)) {
                continue;
            }
            String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
            nameById.put(id, catalog.get(key));
        }
        return new SavepointsCatalog(nameById);
    }

    public String name(String id) {
        return nameById.get(id);
    }

    /** Real ids with a name, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(nameById.keySet());
        Collections.sort(out);
        return out;
    }
}
