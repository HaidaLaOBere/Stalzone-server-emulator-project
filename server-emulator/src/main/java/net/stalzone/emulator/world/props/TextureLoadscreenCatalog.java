package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real assets under {@code modassets/assets/stalker/textures/loadscreen/} (part 97) --
 * 186 files across 63 real top-level folders, no loose root files, so this class follows part 92's
 * {@link StalkerSoundsCatalog} plain per-top-level-folder walk rather than part 95/96's whitelist
 * deviation (this IS the complete real folder, not a sweep of several small leftover trees). One of
 * {@code textures/}'s 5 larger real folders part 96 deliberately left uncatalogued (alongside {@code
 * gui/} 2,124, {@code items/} 903, {@code armor/} 731, {@code particles/} 95). Squarely game content
 * (per-location loading-screen images), not an EXENS anti-cheat/launcher asset, so stays in scope
 * under rule 2. Cross-referenced against real {@code stalker/lang/en.lang} strings under rule 1's
 * literal-word evidentiary bar; nearly every folder name resolves to a real {@code
 * loadscreens.location.*} display name, making this class this project's single richest real
 * map/location-name crosswalk to date, and it directly corroborates a large number of previously
 * confirmed and previously-unconfirmed leads from earlier parts.
 *
 * <p><b>Map-location loadscreens (the majority of folders) -- CONFIRMED real display names via
 * {@code loadscreens.location.*}:</b> {@code agroprom}="Kolos Agrocomplex", {@code
 * armsklad}="Polesskoye", {@code bar}="Pervomaisky Factory", {@code bar_save}="The Bar", {@code
 * bolota}="The Swamps", {@code dungeon_school}="Military Base", {@code incident}="Laboratory",
 * {@code kordon}="Roadside", {@code les}="The Forest", {@code mg}="The Dead City", {@code
 * nbolota}="The Graveyard", {@code overpass}="Underground", {@code radar}="Radar", {@code
 * svalka}="Scrapyard" (doubly confirming part 96's {@code blizzard_overlay/svalka.mic} finding),
 * {@code td}="Black Willows" (directly matching part 27's real {@code
 * map.teleport.id.svalka-td.name=The Scrapyard-The Black Willows}), {@code yama}, {@code
 * yanov}="Uranus Mill", {@code zaton}="Stillwaters". {@code Pd} (the only folder in this class that
 * does not use the otherwise-universal all-lowercase naming convention -- a real disclosed on-disk
 * capitalization quirk, not normalized away) resolves case-insensitively to the real {@code en.lang}
 * key {@code pd}="The Path of Fools", further confirming part 96's {@code blizzard_overlay/pd.mic}
 * finding. The {@code north_*}-prefixed folders ({@code
 * north_barrier}="Barrier", {@code north_forge}="Forge-11" -- directly matching part 92's disclosed
 * "Forge" ZIVCAS/Murmur conflict-zone lead, {@code north_hazard_inflow}="Reflection", {@code
 * north_hazard_mines}="Black Detour", {@code north_hrop}="Temple Swamp", {@code
 * north_inflow}="Inflow", {@code north_institute}="Institute", {@code north_maze_d_c}/{@code
 * north_maze_m_f}="Labyrinth", {@code north_mines}="Mines", {@code north_serpantine}="Serpentine"
 * (the real on-disk folder name has a genuine, disclosed spelling divergence from {@code en.lang}'s
 * {@code north_serpentine} key -- not normalized away)) resolve a large, previously-uncatalogued
 * real sub-region roster. {@code savezone_agroprom}="Digger Hut" and {@code savezone_td}="Digger
 * Plot" tie to a real "Digger" faction/role (also {@code loadscreens.location.diggers.name=Digger
 * Base}, not itself backed by a texture folder in this sweep). {@code inside/} (10 files, one per
 * named sub-location: {@code agroprom}/{@code forge}/{@code inflow}/{@code kordon}/{@code mg}/{@code
 * mines}/{@code svalka}/{@code td}/{@code yantar}/{@code zaton}) is a real interior/underground-
 * transition loadscreen set for 10 of the above locations. {@code object}, {@code pa}, {@code
 * poligon2}="Reactor 4", {@code roza}, {@code hvoiny}, {@code kvartaly}, {@code nizina}, {@code
 * berdovka}, and {@code atp}="Fire Station" are real but {@code object}/{@code pa}/{@code roza} (as
 * a bare key) have no direct {@code en.lang} name match found -- disclosed as real, unconfirmed
 * leads rather than guessed.
 *
 * <p><b>{@code battlefield/} (19 files) -- CONFIRMED, directly corroborates part 72's confirmed
 * clan-war Battlefield mode map roster with real display names via {@code battlefield.id.*}:</b>
 * {@code 1april}="Petard Server", {@code collapse}="Collapse", {@code duel}="Duel" (generic,
 * separately named {@code loadscreens.location.arena.name=Duel}), {@code hospital}="Hospital",
 * {@code jail}="Prison", {@code kolizei}="Transit", {@code krot2}="Wormhole", {@code
 * kutuzov}(no direct match), {@code moryatnik}="Moria House", {@code peklo}="Inferno", {@code
 * pripyat}="Pripyat Streets"/"Streets of Pripyat", {@code reich}="Assault on the Reichstag" and
 * {@code reich_breakthrough}(_1/_2/_3) -- a TENTH independent Reichstag corroboration, {@code
 * rus}="Battlefield" (real team names {@code battlefield.id.rus.team1.name=Slavs}, {@code
 * .team2.name=Lizards} -- CONFIRMS part 80's previously-uncorroborated "lizards" enemy/faction name
 * lead as a real Battlefield-mode team name, not a mutant/enemy type), {@code sovetpunk}="GOST-17",
 * {@code stakan}="Glass".
 *
 * <p><b>{@code base_covenant}/{@code base_duty}/{@code base_freedom}/{@code base_merc} (1 file
 * each) -- CONFIRMED as the 4 non-{@code stalkers}/{@code bandits} alliance home-base loadscreens,
 * directly extending part 91's confirmed 6-id playable-alliance roster with real base names:</b>
 * {@code base_duty}="Frontier Base" (matches part 91's confirmed "the Frontier" nickname), {@code
 * base_freedom}="Rise Base" (matches part 92's confirmed "Rise" name for the {@code freedom} id),
 * {@code base_merc}="Mercenary Camp", {@code base_covenant}="Covenant Abode".
 *
 * <p><b>{@code ffa_base_bunker}/{@code ffa_base_kpp}/{@code ffa_base_meteostation}/{@code
 * ffa_base_mine} (1 file each) -- CONFIRMED, directly corroborates part 93's confirmed "FFA"
 * (free-for-all) PvP mode with 4 real base display names:</b> {@code
 * ffa_base_bunker}="Bunker", {@code ffa_base_kpp}="Checkpoint", {@code
 * ffa_base_meteostation}="Weather station", {@code ffa_base_mine}="Mine".
 *
 * <p><b>{@code operation_big_cleanup}/{@code operation_sea_alienation}/{@code
 * operation_shock_therapy} (4 files each) -- CONFIRMED, directly corroborates part 94's 3 confirmed
 * real PvE Operation modes:</b> {@code operation_big_cleanup}="The Big Clean Up Operation", {@code
 * operation_sea_alienation}="Sea of Exclusion Operation", {@code
 * operation_shock_therapy}="Focus Operation".
 *
 * <p><b>UPDATED part 113:</b> these same 3 real names are corroborated a third time, and the
 * Operations mode itself is fully catalogued, by {@link OperationsCatalog}'s {@code
 * gui.operation.how_to_play.category.*} tab-label crosswalk.
 *
 * <p><b>{@code tournament_}-family (via {@code hvoiny}/{@code kvartaly}/{@code nizina}/{@code
 * roza}/{@code berdovka}, 1 file each) -- CONFIRMED real, a previously-uncatalogued PvP mode
 * distinct from both part 72's Battlefield and part 93's FFA, tied directly to part 93's confirmed
 * "Crow Arena" event via {@code general.achievement.id.ravent_quests.desc=Win the tournament in the
 * Crow Arena}:</b> {@code en.lang}'s {@code loadscreens.location.tournament_*} keys resolve real
 * names for all 5 (Coniferous, Quarters, The Lowland, The Wind Rose, Malaya Berdovka); this project's
 * on-disk folder names drop the {@code tournament_} prefix entirely (e.g. real folder {@code hvoiny}
 * vs. real key {@code tournament_hvoiny}), a genuine naming-convention divergence disclosed rather
 * than silently reconciled.
 *
 * <p><b>{@code arena_raven} (4 files) -- CONFIRMED as "Arena of Slavs" via {@code
 * loadscreens.location.arena_raven.name=Arena of Slavs}, directly re-confirming part 93's real
 * Slavic-mythology PvE wave-defense event (a distinct real mode from the {@code tournament_}
 * PvP-arena family above, despite the shared "arena"/"Crow Arena" branding).</b>
 *
 * <p><b>{@code prestige} (5 files, {@code map_1_1}-{@code map_1_5}) -- CONFIRMED as "Area-16" via
 * {@code location.prestige_map_1.name=Area-16}, resolving a real map/location tie for part 92's
 * confirmed {@code Area-16} facility name (previously known only from a cassette-collectible item
 * title, with no location evidence) and linking it to part 83's real {@code PrestigeAbility}
 * progression system by shared naming.</b>
 *
 * <p><b>{@code atamans_hq} (1 file) -- CONFIRMED via a real named settlement in the Swamps region
 * ({@code region.stalkers_bolota.name=Ataman's HQ}, {@code settlement.id.csky.title=Ataman's HQ}),
 * home to a real named leader ("the Ataman") and a real 4-tier reputation ladder (Picklock/Swamp
 * Stalker/Ensign/Vodyanoy) that directly reuses part 95's confirmed {@code PICKLOCK} tier name from
 * a completely different namespace (the {@code dropped_tooltip/stick} rank-badge system), showing
 * "Picklock" is a real, reused low-reputation-tier name across at least two distinct real systems.</b>
 *
 * <p><b>{@code boat_station} (1 file) -- CONFIRMED as "Boathouse" via {@code
 * loadscreens.location.boat_station.name=Boathouse}, directly resolving part 27's disclosed-but-
 * unconfirmed {@code lodochnaya} ("boathouse", Russian) named location from that part's real
 * trading-history data -- the first direct {@code en.lang} confirmation of that lead.</b>
 *
 * <p><b>{@code foton2} (1 file) -- CONFIRMED as "Village School" via {@code
 * loadscreens.location.foton2.name=Village School}, giving a real display name to part 27's {@code
 * foton2} trader-id location for the first time.</b>
 *
 * <p><b>{@code limansk} (3 files) -- CONFIRMED as an alias for "Lyubech-3" via {@code
 * loadscreens.location.limansk.name=Lyubech-3}, matching part 92's real Lyubech-3 mutant-name
 * namespace and confirming {@code limansk}/Lyubech-3 are the same real in-fiction location under two
 * different internal/display names.</b>
 *
 * <p><b>{@code generic} (5 files) -- a real, disclosed placeholder/fallback set, CONFIRMED via
 * {@code loadscreens.location.generic.name=STALZONE} (the game's own literal title used as a generic
 * loading-screen location label, not a named map).</b>
 *
 * <p>Not investigated further: exact per-file image content (this class indexes real filenames only,
 * consistent with every other catalog in this project); the 15 named locations in the real {@code
 * loadscreens.location.*} namespace that have NO corresponding texture folder in this sweep (e.g.
 * {@code izgoi}="Sewer", {@code kishka}="Zone Intestine", {@code yasli}="Marsh") -- real, disclosed,
 * simply outside this class's scope since it only walks {@code textures/loadscreen/}.
 *
 * <p>Pure cataloguing, like parts 54-57/91-96 -- no new {@code GameLoop}/{@code SingleplayerServer}
 * wiring, so no live {@code Main}-entry-point run was needed; verified by 18 new tests against a
 * fixture with the COMPLETE real 186-file listing across all 63 folders, `find`-verified directly on
 * the actual device install, staged to the cloud workspace, and cross-checked with a Python
 * {@code Counter} before the fixture array was generated programmatically (never hand-typed) -- the
 * same technique parts 95-96 established. {@code textures/}'s 4 other larger real top-level folders
 * ({@code gui/} 2,124, {@code items/} 903, {@code armor/} 731, {@code particles/} 95) and {@code
 * stalker/}'s other remaining large top-level tree ({@code sound/} 10,500 files) remain the largest
 * real open leads for future parts.
 */
public final class TextureLoadscreenCatalog {

    private final Map<String, List<String>> filesByFolder;

    private TextureLoadscreenCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param loadscreenDir the real {@code .../modassets/assets/stalker/textures/loadscreen} folder. */
    public static TextureLoadscreenCatalog load(Path loadscreenDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(loadscreenDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    String folderName = entry.getFileName().toString();
                    List<String> files = new ArrayList<>();
                    collectFiles(entry, entry, files);
                    Collections.sort(files);
                    filesByFolder.put(folderName, files);
                }
            }
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(loadscreenDir + ": no loadscreen asset folders found");
        }

        return new TextureLoadscreenCatalog(filesByFolder);
    }

    private static void collectFiles(Path folderRoot, Path dir, List<String> out) throws IOException {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    collectFiles(folderRoot, entry, out);
                } else {
                    out.add(folderRoot.relativize(entry).toString().replace('\\', '/'));
                }
            }
        }
    }

    /** Real top-level loadscreen-asset folder names, alphabetical. */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /** Real file paths (relative to the folder itself) inside one real folder, or empty if unknown. */
    public List<String> filesIn(String folderName) {
        return Collections.unmodifiableList(filesByFolder.getOrDefault(folderName, List.of()));
    }

    /**
     * Real folder names that have at least one nested file whose name contains {@code substring}
     * (case-insensitive). Generic on purpose -- see the class doc for why this project verifies
     * narrative claims this way instead of hardcoding a fixed list.
     */
    public List<String> foldersContaining(String substring) {
        String needle = substring.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesByFolder.entrySet()) {
            for (String file : entry.getValue()) {
                if (file.toLowerCase(Locale.ROOT).contains(needle)) {
                    matches.add(entry.getKey());
                    break;
                }
            }
        }
        Collections.sort(matches);
        return matches;
    }

    public int totalFolderCount() {
        return filesByFolder.size();
    }

    public int totalFileCount() {
        int total = 0;
        for (List<String> files : filesByFolder.values()) {
            total += files.size();
        }
        return total;
    }
}
