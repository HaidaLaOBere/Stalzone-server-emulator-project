package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real PvP/multiplayer map data from {@code stalker/lang/en.lang}'s {@code battlefield} namespace
 * (68 lines; part 90's {@link LangCatalog} counted it and noted one sample line, {@code
 * battlefield.id.reich.name=Assault on the Reichstag}, as an eighth independent Reichstag
 * corroboration, but did not otherwise parse it -- this class is the full investigation, part 106,
 * which also turns up a real, distinct NINTH corroboration via {@code reich_breakthrough}, below).
 *
 * <p><b>Cross-referenced against {@code world/props/StalkerClansCatalog}'s part-72 {@code
 * battlefields/} folder (15 real {@code .mic} map files).</b> Seven ids match exactly -- {@code
 * hospital}, {@code jail}, {@code kolizei}, {@code moryatnik}, {@code peklo}, {@code pripyat},
 * {@code reich} -- too many to be coincidental, confirming this namespace is the real
 * localization/display layer for the SAME battlefield-mode map system part 72 catalogued compiled
 * files for, not a separate mode that happens to reuse a few names. The two sides are honestly a
 * non-identical snapshot, not a perfect 1:1 set, disclosed rather than glossed over: part 72's
 * {@code battlefields/} folder has 7 real ids never seen in {@code en.lang}'s {@code battlefield.*}
 * ({@code bridges}, {@code idiotway}, {@code kishka}, {@code kordon}, {@code svalka}, {@code td},
 * {@code yama} -- plausibly maps retired or renamed since that asset snapshot, or a real id-naming
 * difference this project hasn't resolved), and its {@code krot.mic} has no exact match here (this
 * namespace's closest id is {@code krot2}, plausibly a real sequel/replacement map, not confirmed).
 * Conversely this namespace has real ids/variants part 72's snapshot never had: brand-new maps
 * ({@code collapse}, {@code ship}, {@code stakan}, {@code sovetpunk}, {@code rus}, the {@code
 * 1april} joke map) and real seasonal/mode variants of the shared maps ({@code kolizei_calibre},
 * {@code ship_deathmatch}, {@code peklo_autumn25}, {@code pripyat_summer25}, {@code reich_night},
 * {@code reich_breakthrough}, {@code rus_open_night}) -- consistent with {@code en.lang} simply
 * being the more current/complete of the two real sources.
 *
 * <p>23 real map ids resolve to a real display name. Most are real-world/generic PvP arena names
 * with no Zone-lore tie: {@code collapse}="Collapse", {@code hospital}="Hospital", {@code jail}=
 * "Prison", {@code kolizei}/{@code kolizei_calibre}="Transit", {@code krot2}="Wormhole", {@code
 * moryatnik}="Moria House", {@code ship}="Lotan", {@code stakan}="Glass", {@code peklo}="Inferno"
 * (a real knife-only variant, {@code peklo_autumn25}="[Knives] Inferno", real text: "Leave your gun
 * in the warehouse and show who wields a knife better here!"), {@code sovetpunk}="GOST-17". A real
 * seasonal joke map: {@code 1april}="Petard Server" (real text: "Basically, someone did something
 * there for April Fools").
 *
 * <p>Two maps carry real narrative ties already established elsewhere in this project: {@code
 * pripyat}="Pripyat Streets" (real description {@code battlefield.id.pripyat.description=SSPP is
 * conducting an operation against Murmur}, tying into the confirmed SSPP/Murmur thread, with real
 * named points {@code western_parking}="West Camp", {@code box}="Box", {@code lenin}="Lenin
 * Avenue", {@code garage}="Garage", {@code east_parking}="East Camp"); and {@code reich}="Assault
 * on the Reichstag" (the same real id part 90's {@link LangCatalog} doc already counted as an
 * eighth independent Reichstag corroboration; this class adds its real description text,
 * "Participate in a fierce battle for every inch of land with historical equipment", and real
 * points {@code hall}, {@code entrance}, {@code wing}, but does not claim a new corroboration from
 * the same id/source). A distinct real id, {@code reich_breakthrough} (same display name, a real
 * NINTH independent corroboration since it is a separate map entry from {@code reich}), carries six
 * real named historical Berlin landmarks as capture points -- {@code a}="Moltke Bridge", {@code
 * b}="Brandenburg Gate", {@code c}="The Interior Ministry Building", {@code d}="Victory Column",
 * {@code e}="Bunker", {@code f}="Reichstag" -- this project's first real confirmation that the
 * Reichstag mode is a historically-themed WWII Battle-of-Berlin recreation, not merely a Zone-lore
 * reuse of the name.
 *
 * <p>One map carries an unrelated real fantasy/folklore theme, distinct from core S.T.A.L.K.E.R.
 * Zone lore: {@code rus}="Battlefield", with real opposing team names {@code team1}="Slavs" and
 * {@code team2}="Lizards", and real named points {@code north}="Idol of Perun" (the real Slavic
 * thunder-god), {@code middle}="Center", {@code south}="Lizard Nest" -- disclosed as a real but
 * lore-unconnected PvP theme, not investigated further.
 *
 * <p>Live-verified directly against the real client's {@code en.lang} on 2026-09-11.
 *
 * <p><b>UPDATED part 110:</b> a separate real {@code en.lang} namespace, {@code loadscreens.
 * location.battlefield_<id>_open.name} (see {@link LoadscreenCatalog}), gives {@code krot2} a
 * SECOND, different real display name -- "Mole-2 System" ("krot" is Russian for "mole") -- versus
 * this class's own {@code krot2}="Wormhole", honestly disclosed as a real naming variance rather
 * than resolved one way. The same namespace also reveals a genuinely new real 24th battlefield map
 * this class does not have: {@code battlefield_promzona_open}="Industrial Zone".
 */
public final class BattlefieldCatalog {

    private static final String PREFIX = "battlefield.id.";

    private final Map<String, String> mapNameById;
    private final Map<String, String> mapDescriptionById;
    private final Map<String, String> pointNameByKey;
    private final Map<String, String> teamNameByKey;

    private BattlefieldCatalog(Map<String, String> mapNameById,
                                Map<String, String> mapDescriptionById,
                                Map<String, String> pointNameByKey,
                                Map<String, String> teamNameByKey) {
        this.mapNameById = mapNameById;
        this.mapDescriptionById = mapDescriptionById;
        this.pointNameByKey = pointNameByKey;
        this.teamNameByKey = teamNameByKey;
    }

    public static BattlefieldCatalog load(LangCatalog catalog) {
        Map<String, String> mapNameById = new LinkedHashMap<>();
        Map<String, String> mapDescriptionById = new LinkedHashMap<>();
        Map<String, String> pointNameByKey = new LinkedHashMap<>();
        Map<String, String> teamNameByKey = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            String[] parts = rest.split("\\.");
            if (parts.length == 2 && parts[1].equals("name")) {
                mapNameById.put(parts[0], catalog.get(key));
            } else if (parts.length == 2 && parts[1].equals("description")) {
                mapDescriptionById.put(parts[0], catalog.get(key));
            } else if (parts.length == 4 && parts[1].equals("point") && parts[3].equals("name")) {
                pointNameByKey.put(parts[0] + "/" + parts[2], catalog.get(key));
            } else if (parts.length == 3 && (parts[1].equals("team1") || parts[1].equals("team2"))
                    && parts[2].equals("name")) {
                teamNameByKey.put(parts[0] + "/" + parts[1], catalog.get(key));
            }
        }

        return new BattlefieldCatalog(mapNameById, mapDescriptionById, pointNameByKey, teamNameByKey);
    }

    public String mapName(String mapId) {
        return mapNameById.get(mapId);
    }

    /** The map's real flavor-text description, or {@code null} if this map id has none. */
    public String mapDescription(String mapId) {
        return mapDescriptionById.get(mapId);
    }

    public String pointName(String mapId, String pointId) {
        return pointNameByKey.get(mapId + "/" + pointId);
    }

    /** {@code teamKey} is {@code "team1"} or {@code "team2"}. */
    public String teamName(String mapId, String teamKey) {
        return teamNameByKey.get(mapId + "/" + teamKey);
    }

    public List<String> mapIds() {
        List<String> ids = new ArrayList<>(mapNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int mapCount() {
        return mapNameById.size();
    }
}
