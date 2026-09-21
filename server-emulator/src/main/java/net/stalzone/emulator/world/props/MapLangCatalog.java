package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 193-line {@code map.*} namespace in {@code stalker/lang/en.lang} (part 126,
 * the third concrete namespace built from part 123's follow-up census). Named {@code
 * MapLangCatalog}, not {@code MapCatalog}, to avoid confusion with this project's unrelated
 * file-based {@link PdaMapCatalog} (part 64, real PDA map ASSET files) and {@link
 * net.stalzone.emulator.world.MilestoneMap} (a different zone-tracking structure) -- both checked
 * before naming this class.
 *
 * <p>Confirmed by direct count to split cleanly into 5 real groups with no leftover lines (81 +
 * 78 + 26 + 6 + 2 = 193): {@code location} (81), {@code teleport} (78), {@code global.region}
 * (26), {@code safezone} (6), {@code page} (2). Four groups use a real {@code .id.<id>.<field>}
 * infix; {@code global} alone uses {@code .region.<id>.<field>} instead. Every real line in every
 * group carries only {@code name} or {@code desc} as its field, so each group is modeled as a
 * generic {@code (id, field)} table.
 *
 * <p>{@code location.id.<id>.name} (81 lines, no {@code desc}) gives real zone-code display names
 * that substantially overlap the already-covered {@code location.<id>.name} family ({@link
 * LocationNameRegistry}, part 105) -- e.g. {@code bolota}="The Swamps", {@code kordon}="The
 * Roadside" match on both sides -- a real corroboration from an independent {@code en.lang}
 * prefix, not treated as a duplicate to skip since this is a genuinely separate real key family.
 * {@code teleport.id.<fromId>-<toId>.name} (78 lines) gives real named fast-travel ROUTE labels
 * (e.g. {@code bolota-kordon.name}="The Swamps—The Roadside"), a real system this project has not
 * catalogued from any other angle before.
 *
 * <p>{@code global.region.<id>.name}/{@code .desc} (26 lines -- a direct count found 13 real
 * world regions, not the smaller number a quick sample first suggested, each with a real
 * multi-sentence lore-flavor {@code desc}) is a THIRD distinct real "region" concept this project
 * has now found, confirmed by direct content comparison rather than assumed identical to the
 * other two: {@link GeneralCatalog}'s {@code general.region.*} (part 118, flat platform/publishing
 * labels like "Europe") and the standalone top-level {@code region.<id>.name} (part 123's census,
 * faction-controlled sub-area labels like "Ataman's HQ") are both unrelated id spaces from this
 * one, which names whole in-fiction geographic regions of the Zone instead. All 13, verbatim:
 * {@code south}="South Zone", {@code sever}="North Zone" (confusingly the same id {@link
 * SettlementRegistry} and {@link CnpsCatalog} use for the "Drunken Geiger Bar" settlement --
 * genuinely two separate real id spaces sharing one short id, disclosed as-is, not merged),
 * {@code limansk}="Lyubech-3", {@code zaton}="Stillwaters", {@code tournament_nizina}="Lowland",
 * {@code tournament_roza_vetrov}="The Wind Rose", {@code tournament_hvoiny}="Coniferous", {@code
 * pripyat}="Pripyat", {@code cnpp}="ChNPP" (real flavor text calls it "the heart of the Zone"),
 * {@code wnorth}="Wild North" (ties to {@link SettlementRegistry}'s own already-disclosed {@code
 * wnorth} settlement id), {@code sovhoz}="Sovkhoz", {@code chernobyl4}="Chernobyl-4" (real lore:
 * a former SSPP staging ground, now a crater, tying into this project's existing SSPP/Murmur
 * conflict thread), {@code dmz}="Boral" (a real named DMZ-mode island, resolving what the bare
 * top-level {@code dmz.*}/{@code dmz2026.*} prefixes from part 123's census never themselves
 * named).
 *
 * <p>{@code safezone.id.<id>.name} (6 lines) gives real safe-hub display names that corroborate
 * {@link net.stalzone.emulator.world.SettlementRegistry}'s (part 105) crosswalk from yet another
 * angle, several under DIFFERENT zone-code ids than that class uses: {@code bar}="Pervomaisky
 * Factory" (matches directly), {@code lodka}="Boathouse" (a shortened real alias for that class's
 * {@code lodochnaya}), {@code atp}="Fire Station" (a real alias for {@code garage}), {@code
 * bvs}="Village School" (a real alias for {@code foton2}), {@code podzemka}="Intersection" (new).
 * The sixth, {@code cs}="Clear Sky Base", is a genuinely NEW real id -- distinct from {@code
 * SettlementRegistry}'s own {@code csky} (already resolved to "Ataman's HQ," which that class's
 * doc says supersedes an earlier speculative "csky abbreviates Clear Sky" guess). This {@code cs}
 * id is a separate, freshly-confirmed real reference to "Clear Sky" as in-fiction content, without
 * reopening or contradicting the already-settled {@code csky} resolution.
 *
 * <p>{@code page.id.<id>.name} (2 lines) is a small real map-UI tab-label pair: {@code
 * east.name}="West" (a real inverted/legacy label, disclosed as-is) and {@code
 * limansk.name}="Lyubech-3" (matching {@code global.region.limansk.name} exactly).
 */
public final class MapLangCatalog {

    private final Map<String, Map<String, String>> locationFieldsById;
    private final Map<String, Map<String, String>> teleportFieldsById;
    private final Map<String, Map<String, String>> globalRegionFieldsById;
    private final Map<String, Map<String, String>> safezoneFieldsById;
    private final Map<String, Map<String, String>> pageFieldsById;

    private MapLangCatalog(
            Map<String, Map<String, String>> locationFieldsById,
            Map<String, Map<String, String>> teleportFieldsById,
            Map<String, Map<String, String>> globalRegionFieldsById,
            Map<String, Map<String, String>> safezoneFieldsById,
            Map<String, Map<String, String>> pageFieldsById) {
        this.locationFieldsById = locationFieldsById;
        this.teleportFieldsById = teleportFieldsById;
        this.globalRegionFieldsById = globalRegionFieldsById;
        this.safezoneFieldsById = safezoneFieldsById;
        this.pageFieldsById = pageFieldsById;
    }

    private static final String LOCATION_PREFIX = "map.location.id.";
    private static final String TELEPORT_PREFIX = "map.teleport.id.";
    private static final String GLOBAL_REGION_PREFIX = "map.global.region.";
    private static final String SAFEZONE_PREFIX = "map.safezone.id.";
    private static final String PAGE_PREFIX = "map.page.id.";

    public static MapLangCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> locationFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> teleportFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> globalRegionFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> safezoneFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> pageFieldsById = new LinkedHashMap<>();

        putAllLastDotSplit(catalog, LOCATION_PREFIX, locationFieldsById);
        putAllLastDotSplit(catalog, TELEPORT_PREFIX, teleportFieldsById);
        putAllLastDotSplit(catalog, GLOBAL_REGION_PREFIX, globalRegionFieldsById);
        putAllLastDotSplit(catalog, SAFEZONE_PREFIX, safezoneFieldsById);
        putAllLastDotSplit(catalog, PAGE_PREFIX, pageFieldsById);

        return new MapLangCatalog(locationFieldsById, teleportFieldsById, globalRegionFieldsById,
                safezoneFieldsById, pageFieldsById);
    }

    private static void putAllLastDotSplit(LangCatalog catalog, String prefix,
                                            Map<String, Map<String, String>> into) {
        for (String key : catalog.keysWithPrefix(prefix)) {
            String rest = key.substring(prefix.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            into.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
    }

    private static String field(Map<String, Map<String, String>> fieldsById, String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    private static List<String> ids(Map<String, Map<String, String>> fieldsById) {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    public String locationName(String id) { return field(locationFieldsById, id, "name"); }
    public List<String> locationIds() { return ids(locationFieldsById); }

    public String teleportName(String routeId) { return field(teleportFieldsById, routeId, "name"); }
    public List<String> teleportIds() { return ids(teleportFieldsById); }

    public String globalRegionName(String id) { return field(globalRegionFieldsById, id, "name"); }
    public String globalRegionDesc(String id) { return field(globalRegionFieldsById, id, "desc"); }
    public List<String> globalRegionIds() { return ids(globalRegionFieldsById); }

    public String safezoneName(String id) { return field(safezoneFieldsById, id, "name"); }
    public List<String> safezoneIds() { return ids(safezoneFieldsById); }

    public String pageName(String id) { return field(pageFieldsById, id, "name"); }
    public List<String> pageIds() { return ids(pageFieldsById); }
}
