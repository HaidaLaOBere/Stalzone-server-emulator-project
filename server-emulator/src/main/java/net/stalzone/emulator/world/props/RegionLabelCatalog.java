package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 176. Catalogs the real, tiny 4-line bare top-level {@code region.<factionId>_<zoneId>.name}
 * namespace in {@code stalker/lang/en.lang} -- found while auditing the smaller namespaces below
 * {@code banner} (part 174/175's closing notes). This is the "standalone top-level {@code
 * region.<id>.name}" family that {@link MapLangCatalog}, {@link
 * net.stalzone.emulator.world.SettlementRegistry}, and {@link TextureLoadscreenCatalog} had each
 * already independently quoted verbatim as crosswalk evidence in their own Javadoc (e.g. {@code
 * region.stalkers_bolota.name=Ataman's HQ}), but none of the three had actually modeled as its
 * own parseable class -- confirmed by a direct source-tree search for a literal {@code "region."}
 * bare-prefix load before writing this class, so this closes a real, previously-unmodeled (though
 * partially pre-quoted) gap rather than a wholly blind spot. Deliberately NOT folded into {@link
 * GeneralCatalog}, whose own {@code REGION_PREFIX} constant is scoped to {@code general.region.}
 * (a different, unrelated real key family under the {@code general.} prefix, not this bare
 * top-level one) -- checked directly in that class's source before naming and building this one.
 *
 * <p>All 4 real ids follow the exact same {@code <factionId>_<zoneId>} shape, both halves already
 * confirmed real vocabulary elsewhere in this project: {@code stalkers}/{@code bandits} match the
 * already-confirmed {@code pda.waypoint.we.outpost.stalkers}="Stalker Outpost"/{@code
 * .bandits}="Bandit Outpost" faction-id vocabulary (part 173), and {@code bolota}/{@code kordon}
 * match {@link net.stalzone.emulator.world.LocationNameRegistry}'s already-confirmed real zone
 * codes ("The Swamps"/"The Roadside"). This class exposes {@code factionId(id)}/{@code
 * zoneId(id)} as a first-underscore split on that basis, not as an unevidenced guess.
 *
 * <p>Real content, and the crosswalk each entry adds (all 4 resolve to already-known real
 * settlements, so this namespace names no NEW settlement, but corroborates 4 already-confirmed
 * ones from a fourth independent angle -- after {@link
 * net.stalzone.emulator.world.SettlementRegistry}, {@link
 * net.stalzone.emulator.world.props.SettlementPerkCatalog}, and {@link CnpsCatalog}'s per-
 * settlement rank ladders): {@code stalkers_bolota}="Ataman's HQ" (matches {@code
 * SettlementRegistry}'s {@code csky}, and was already quoted by that class's own doc as one of
 * its two corroborating real strings for the {@code csky}="Ataman's HQ" resolution); {@code
 * bandits_bolota}="Boathouse" (a NEW corroborating angle for {@code SettlementRegistry}'s {@code
 * lodochnaya}="Boathouse", not previously spelled out with this specific real id); {@code
 * stalkers_kordon}="Fire Station" (a NEW angle for {@code garage}="Fire Station"); {@code
 * bandits_kordon}="Village School" (a NEW angle for {@code foton2}="Village School"). Read
 * together, the real data shows the {@code bolota} and {@code kordon} zones each have one
 * "stalkers"-labeled and one "bandits"-labeled named sub-area with DIFFERENT display names (not
 * the same settlement renamed per faction) -- consistent with, not contradicting, the existing
 * settlement roster.
 */
public final class RegionLabelCatalog {

    private static final String PREFIX = "region.";
    private static final String SUFFIX = ".name";

    private final Map<String, String> nameById;

    private RegionLabelCatalog(Map<String, String> nameById) {
        this.nameById = nameById;
    }

    public static RegionLabelCatalog load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (!key.endsWith(SUFFIX)) {
                continue;
            }
            String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
            nameById.put(id, catalog.get(key));
        }
        return new RegionLabelCatalog(nameById);
    }

    public String name(String id) {
        return nameById.get(id);
    }

    /** First-underscore split of a real {@code <factionId>_<zoneId>} id, e.g. {@code "stalkers"} for {@code "stalkers_bolota"}. Null if the id has no underscore. */
    public String factionId(String id) {
        int underscore = id.indexOf('_');
        return underscore < 0 ? null : id.substring(0, underscore);
    }

    /** First-underscore split of a real {@code <factionId>_<zoneId>} id, e.g. {@code "bolota"} for {@code "stalkers_bolota"}. Null if the id has no underscore. */
    public String zoneId(String id) {
        int underscore = id.indexOf('_');
        return underscore < 0 ? null : id.substring(underscore + 1);
    }

    /** Real ids, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(nameById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Total real lines resolved -- 4 against the real, full file. */
    public int size() {
        return nameById.size();
    }
}
