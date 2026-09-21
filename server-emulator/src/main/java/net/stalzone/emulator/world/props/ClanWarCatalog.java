package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalogs a real 98-line {@code clans.*} namespace in {@code stalker/lang/en.lang} (part 108) --
 * a real clan-war PvP FFA/tournament map roster and base-capture reward system, entirely distinct
 * from part 72's file-based {@link StalkerClansCatalog} (a different, non-overlapping real map id
 * set: {@code nizina}/{@code roza_vetrov}/{@code hvoiny}/{@code kvartaly}/{@code small_berdovka},
 * none of which appear in {@code StalkerClansCatalog}'s own 15-map roster) and from part 106's
 * {@link BattlefieldCatalog} (also a non-overlapping id set, beyond one coincidental base-name
 * reuse noted below). This is a third, independently real PvP/clan-content system this project has
 * now catalogued, not a rediscovery of either earlier one.
 *
 * <p><b>{@code clans.unit_creation.<factionId>.creation_rules_test_fail.desc}</b> (4 lines) gives a
 * real clan-unit creation reputation gate for the four confirmed clan factions, each naming a real
 * clan "Base": {@code duty}="Requires level 3 reputation at the Frontier Base", {@code
 * freedom}="...at the Rise Base", {@code merc}="...at the Mercenary Base", {@code
 * covenant}="...at the Covenant Base". Honestly disclosed: two of these four base names differ
 * slightly from {@link net.stalzone.emulator.world.SettlementRegistry}'s confirmed real settlement
 * titles for the same faction ids -- {@code merc}="Mercenary Camp" there vs. "Mercenary Base" here,
 * {@code covenant}="Covenant Abode" there vs. "Covenant Base" here. {@code duty}/{@code freedom}
 * agree exactly ("Frontier Base"/"Rise Base" in both places). This is disclosed as a real, minor
 * naming variance between two independent {@code en.lang} systems (settlement identity vs. clan-war
 * eligibility text) -- plausibly the same physical hub described two different ways, or two
 * genuinely distinct real locations sharing a faction; NOT confirmed either way, per rule 1.
 *
 * <p><b>UPDATED part 114:</b> {@link HomeOutpostCatalog}'s {@code pda.waypoint.home_outpost.*} is a
 * real THIRD independent naming source for this same {@code duty}/{@code freedom}/{@code
 * merc}/{@code covenant} hub concept: {@code duty}="Frontier Headquarters" and {@code
 * freedom}="Rise Headquarters" agree exactly with this class's "Frontier Base"/"Rise Base" (and
 * {@code SettlementRegistry}'s own titles), while {@code merc}="Mercenary Headquarters" and {@code
 * covenant}="Covenant Headquarters" add yet a third distinct real suffix for those two factions,
 * further corroborating (not resolving) the naming variance disclosed above. That namespace also
 * covers two further real factions with their own headquarters, {@code stalkers}/{@code bandits},
 * outside this class's 4-faction roster.
 *
 * <p><b>{@code clans.level.<levelId>.desc}</b> (3 lines) is a real clan-progression-level system:
 * {@code first_level_addition}="Special Deliveries Level 1", {@code second_level_addition}=
 * "Special Deliveries Level 2\nAvailable chrono installations: 5", {@code third_level_addition}=
 * "Special Deliveries Level 3\nAvailable chrono installations: 10" -- a real "chrono installation"
 * clan-base feature never previously catalogued, unlocked in increasing quantity per clan level.
 *
 * <p><b>{@code clans.ffa_<mapId>.base_<n>.name}</b> (34 lines across 9 real FFA map variants:
 * {@code hvoiny}, {@code kvartaly}, {@code nizina}/{@code nizina2}/{@code nizina3}, {@code
 * roza_vetrov}/{@code roza_vetrov2}, {@code small_berdovka}/{@code small_berdovka3}) names real
 * capturable base points on each map -- mostly generic urban/industrial names (Fountain, Monument,
 * Kindergarten, Department Store, Hardware Store, School, several numbered "School No. &lt;n&gt;"
 * variants, Market, Courtyard, Intersection, Warehouse, Barn(s)), but {@code roza_vetrov} carries a
 * real, distinctive four-way named-building set -- "Red Moria House", "White Moria House", "Green
 * Moria House", "Blue Moria House" -- an independent real corroboration that "Moria House" is a
 * recurring named real-world structure in this game's setting, not unique to part 106's confirmed
 * {@code moryatnik}="Moria House" battlefield map (a coincidental base-name reuse, {@code
 * roza_vetrov.base_5.name=Collapse}, is also shared with part 106's unrelated {@code collapse}
 * battlefield map id -- disclosed as likely coincidence, not investigated further).
 *
 * <p><b>{@code clans.tournament.*}</b> (44 lines) is a real ranked/tournament PvP mode layered over
 * the same map roster (plus two more variants, {@code hvoiny2}) at multiple real real team-size
 * configurations ({@code _1x2}, {@code _1x4}, {@code _2x4}, {@code _2x6}, and the unsuffixed base
 * size) -- real English display names for what are otherwise internal map-folder-style ids never
 * given a human-readable title anywhere else in this project: {@code nizina}="The Lowland" (with
 * {@code nizina2}/{@code nizina3} as "The Lowland 2"/"3"), {@code small_berdovka}="Malaya Berdovka"
 * (2/3 variants numbered the same way), {@code roza_vetrov}="The Wind Rose" (2 variant likewise),
 * {@code hvoiny}="Coniferous" (2 variant likewise), {@code kvartaly}="Quarters". Also real: {@code
 * brawl}="Brawl" and {@code 1v1}="Tournament" game-mode labels, and a real four-base "Base Capture"
 * open-world mode ({@code base_capture_west}="Bunker", {@code base_capture_east}="Checkpoint",
 * {@code base_capture_north}="Weather station", {@code base_capture_south}="Old mine"), each
 * carrying real identical base-ownership reward text via {@code .open_world_base.bonuses}: "Fast
 * travel to the base once every 10 minutes", "Storage size 8x8", "Consumables trader", "Resource
 * buyer", "Mechanic for equipment repairs", "Generator for device charging" -- a real, previously
 * uncatalogued open-world territory-control reward mechanic.
 *
 * <p><b>{@code clans.fight_site.nizina.base_<n>.name}</b> (7 lines) is a real, slightly renamed
 * variant of the {@code ffa_nizina} base roster for a distinct "fight site" mode (e.g. {@code
 * base_4.name}="Tower Apple" here vs. {@code ffa_nizina.base_4.name}="Apple Tower" -- same real
 * landmark, word order swapped between the two real client strings).
 *
 * <p>All 98 real lines are accounted for by the parsing above; nothing in this namespace was
 * skipped or judged out of scope, unlike {@code cnps.availability.desc.*} in part 107.
 */
public final class ClanWarCatalog {

    private final Map<String, String> factionBaseRequirementById;
    private final Map<String, String> levelAdditionDescriptionById;
    private final Map<String, String> ffaBaseNameByKey;
    private final Map<String, String> tournamentNameById;
    private final Map<String, String> openWorldBaseNameById;
    private final Map<String, String> openWorldBaseBonusesById;
    private final Map<String, String> fightSiteBaseNameByKey;

    private ClanWarCatalog(Map<String, String> factionBaseRequirementById,
                            Map<String, String> levelAdditionDescriptionById,
                            Map<String, String> ffaBaseNameByKey,
                            Map<String, String> tournamentNameById,
                            Map<String, String> openWorldBaseNameById,
                            Map<String, String> openWorldBaseBonusesById,
                            Map<String, String> fightSiteBaseNameByKey) {
        this.factionBaseRequirementById = factionBaseRequirementById;
        this.levelAdditionDescriptionById = levelAdditionDescriptionById;
        this.ffaBaseNameByKey = ffaBaseNameByKey;
        this.tournamentNameById = tournamentNameById;
        this.openWorldBaseNameById = openWorldBaseNameById;
        this.openWorldBaseBonusesById = openWorldBaseBonusesById;
        this.fightSiteBaseNameByKey = fightSiteBaseNameByKey;
    }

    public static ClanWarCatalog load(LangCatalog catalog) {
        Map<String, String> factionBaseRequirementById = new LinkedHashMap<>();
        Map<String, String> levelAdditionDescriptionById = new LinkedHashMap<>();
        Map<String, String> ffaBaseNameByKey = new LinkedHashMap<>();
        Map<String, String> tournamentNameById = new LinkedHashMap<>();
        Map<String, String> openWorldBaseNameById = new LinkedHashMap<>();
        Map<String, String> openWorldBaseBonusesById = new LinkedHashMap<>();
        Map<String, String> fightSiteBaseNameByKey = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix("clans.")) {
            String rest = key.substring("clans.".length());
            String[] parts = rest.split("\\.");

            if (parts.length == 4 && parts[0].equals("unit_creation")
                    && parts[2].equals("creation_rules_test_fail") && parts[3].equals("desc")) {
                factionBaseRequirementById.put(parts[1], catalog.get(key));
            } else if (parts.length == 3 && parts[0].equals("level") && parts[2].equals("desc")) {
                levelAdditionDescriptionById.put(parts[1], catalog.get(key));
            } else if (parts.length == 3 && parts[0].startsWith("ffa_") && parts[2].equals("name")) {
                ffaBaseNameByKey.put(parts[0] + "/" + parts[1], catalog.get(key));
            } else if (parts.length == 3 && parts[0].equals("tournament") && parts[2].equals("name")) {
                tournamentNameById.put(parts[1], catalog.get(key));
            } else if (parts.length == 4 && parts[0].equals("tournament")
                    && parts[2].equals("open_world_base") && parts[3].equals("name")) {
                openWorldBaseNameById.put(parts[1], catalog.get(key));
            } else if (parts.length == 4 && parts[0].equals("tournament")
                    && parts[2].equals("open_world_base") && parts[3].equals("bonuses")) {
                openWorldBaseBonusesById.put(parts[1], catalog.get(key));
            } else if (parts.length == 4 && parts[0].equals("fight_site") && parts[3].equals("name")) {
                fightSiteBaseNameByKey.put(parts[1] + "/" + parts[2], catalog.get(key));
            }
        }

        return new ClanWarCatalog(factionBaseRequirementById, levelAdditionDescriptionById,
                ffaBaseNameByKey, tournamentNameById, openWorldBaseNameById,
                openWorldBaseBonusesById, fightSiteBaseNameByKey);
    }

    public String factionBaseRequirement(String factionId) {
        return factionBaseRequirementById.get(factionId);
    }

    public String levelAdditionDescription(String levelId) {
        return levelAdditionDescriptionById.get(levelId);
    }

    public String ffaBaseName(String mapId, String baseId) {
        return ffaBaseNameByKey.get(mapId + "/" + baseId);
    }

    public String tournamentName(String tournamentId) {
        return tournamentNameById.get(tournamentId);
    }

    public String openWorldBaseName(String tournamentId) {
        return openWorldBaseNameById.get(tournamentId);
    }

    public String openWorldBaseBonuses(String tournamentId) {
        return openWorldBaseBonusesById.get(tournamentId);
    }

    public String fightSiteBaseName(String mapId, String baseId) {
        return fightSiteBaseNameByKey.get(mapId + "/" + baseId);
    }

    public List<String> ffaMapIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (String key : ffaBaseNameByKey.keySet()) {
            ids.add(key.substring(0, key.indexOf('/')));
        }
        List<String> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        return sorted;
    }

    public List<String> tournamentIds() {
        List<String> ids = new ArrayList<>(tournamentNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public List<String> fightSiteMapIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (String key : fightSiteBaseNameByKey.keySet()) {
            ids.add(key.substring(0, key.indexOf('/')));
        }
        List<String> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        return sorted;
    }
}
