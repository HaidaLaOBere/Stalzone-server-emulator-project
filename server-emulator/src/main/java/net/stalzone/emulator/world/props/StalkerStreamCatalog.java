package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real assets under {@code modassets/assets/stalker/} for the 3 sibling top-level audio/
 * music trees swept together in part 94 (676 files total): {@code stream/} (331), {@code streamrandom/}
 * (240), {@code music/} (105). Distinct from part 92's {@link StalkerSoundsCatalog}, which already covers
 * a SEPARATE real tree, {@code modassets/assets/stalkersounds/}. Following part 93's {@link
 * StalkerMiscCatalog} precedent, {@link #load} is given the shared {@code modassets/assets/stalker/} root
 * and walks only a fixed whitelist of 3 known folder names ({@link #KNOWN_FOLDERS}), each recursed fully
 * into one "folder" bucket in the returned map -- unlike {@code StalkerMiscCatalog}'s 5 tiny leaf trees,
 * these 3 are each individually large, so this class does NOT also break them down by their own
 * subfolders; {@link #filesIn} returns every file under a whitelisted top folder as one flat, path-
 * relative list. This means {@link #load} does NOT see or index the 3 other real top-level trees still
 * remaining under {@code stalker/} ({@code sound/} 10,500 files, {@code textures/} 4,066, {@code models/}
 * 3,525 -- 2 of whose subfolders were already swept in parts 56-57), nor {@code lang/} (5, already
 * catalogued in part 90 as {@link net.stalzone.emulator.localization.LangCatalog}). Squarely game content,
 * not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code stream/}</b> (331 files, 23 subfolders + 4 loose root files) -- by far the richest single
 * folder swept this part. {@code ambience/} (188, all loose) is a real per-zone ambient-loop roster: a
 * {@code sc_ambient_<zone>_<n>.ogg} naming convention confirms {@code mg}/{@code pd}/{@code td} are real
 * zone-code prefixes each with their own ambience (corroborating, without fully resolving, part 26's old
 * {@code mg}/{@code pd}/{@code td} zone-code lead), alongside many real named zones (agroprom, bolota,
 * lubech, mogilnik, obochina, pervomayskiy, polesskoe, radar, reichstag, safezone x18, svalkatehniki,
 * yantar, zaton, school, quagmire, sanitars, uran, warp), a {@code sc_vynil_nyguitar} 10-track "vinyl
 * guitar" set distinct from part 93's Village Disco vinyl bundle, {@code insideout_areal_1-3} sitting
 * alongside {@code iznanka_areal_1-3} (further corroborating the {@code insideout}/"Typhoon Den" tie), and
 * {@code vagant_1}/{@code vagant_end}/{@code vlasov_bossfight} tracks corroborating the real Vagant
 * questline and Colonel Vlasov boss encounter. {@code attackonperimeter/} (3, {@code
 * Stalcraft_Pavl_May_attack_on_perimeter_track_1/2.ogg} + a matching main-menu track) confirms the game's
 * literal internal codename is "Stalcraft" (matching this project's existing {@link StalcraftCatalog}) and
 * a real May-dated "Attack on Perimeter" seasonal event. {@code blizzard/} (10, {@code blizzard_NN_NN.ogg}
 * + {@code buran_track_0-4.ogg}, "buran" = Russian for blizzard) is weather-system audio. {@code
 * bossfight/} (4: {@code morg1/2.ogg}, {@code shaman1/2.ogg}) is early evidence -- fully confirmed below by
 * {@code music/bossfight/}'s literal folder co-location -- that Morg and Shaman are one linked encounter.
 * {@code country/} (5: Amazing Grace, This Land Is Your Land, US National Anthem, generic "American
 * Country"/"True Country American Blues") is a real licensed-style Americana set with no {@code en.lang}
 * match found -- a real, disclosed, unconfirmed lead as to where/how it plays (most plausibly an in-world
 * radio easter egg). {@code dmz/} (5) confirms real DMZ-mode fight music, a {@code storm_troopers_*} pair
 * (matching this project's already-confirmed "Frost Troopers" find), and a literal {@code
 * Stalcraft_main_menu_2024_black_raid.ogg} tying the 2024 Black Raid event to a Stalcraft-branded main-
 * menu track. {@code fools_day/} (2) corroborates the confirmed April Fools event. {@code
 * gribnaya_bylina/} (3, "Mushroom Bylina" -- bylina = old Slavic epic legend) is a SECOND real Slavic-
 * mythology-themed seasonal-event thread, distinct from {@code ravent}'s "Arena of Slavs" below. {@code
 * grindanilov/} (3: {@code Grin_Danilov_moi_dozhd/na_reke/vi_vse_odinakovie.ogg}, "My Rain"/"On The
 * River"/"You're All The Same") resolves the {@code danilov en.lang}-grep lead's real source -- a credited
 * musician/band with no {@code en.lang} entry of its own, a real disclosed unconfirmed lead. {@code
 * incident/} (12: 10 numbered {@code announcement_N.ogg} + {@code lab_past_sc.ogg}/{@code
 * lab_present_sc.ogg}) confirms a past/present lab narrative split, kept distinct from the separately-
 * flagged {@code incident.game_class.sample.*} en.lang namespace. {@code insideout/} (2, incl. a 2025-
 * dated {@code Stalcraft_main_menu_2025_inside_out.ogg}) further corroborates the Typhoon Den tie. {@code
 * main/} (5, {@code Ambient1/4/7/8/9.ogg}, gapped) is generic hub ambience. {@code newyear/} (1)
 * corroborates the confirmed New Year event. {@code operations/} (3) is CONFIRMED this part via {@code
 * en.lang}: {@code breach_operation.ogg} -> "BREACH OPERATION" ({@code
 * gui.operation.lobby.introduction_quest_big_cleanup} AND {@code ..._shock_therapy} both display as
 * "BREACH OPERATION" -- two internal quest-key aliases sharing one display name, suggesting operation
 * variants/rotations), {@code focus_operation.ogg} -> real "Focus Operation" ({@code
 * general.stats.stat.focus-completed-ops}, with a real "Clearance level" progression stat), and {@code
 * sea_alienation_operation.ogg} -> the already-confirmed "Sea of Exclusion Operation" -- three distinct
 * real "Operation"-branded PvE modes now confirmed.
 *
 * <p><b>UPDATED part 113:</b> {@link OperationsCatalog}'s {@code gui.operation.how_to_play.*}
 * confirms {@code breach}/{@code focus}/{@code sea_alienation} as the real short internal codenames
 * for these same three operations, matching this class's own {@code
 * introduction_quest_big_cleanup}/{@code ..._shock_therapy}/{@code ..._sea_alienation} quest-key
 * suffixes exactly (id {@code breach} = "Big Clean Up", id {@code focus} = "Shock Therapy"/"Focus",
 * id {@code sea_alienation} = "Sea Alienation") -- also fully catalogues the Operations mode's own
 * tutorial/lore content (enemy roster, named bosses, mechanics) that this class's audio-filename
 * survey could only gesture at.
 *
 * <p>{@code ravent/} (33) + {@code ravent_quest/} (3)
 * flesh out part 93's "Arena of Slavs" find with real audio: a 2024-dated main-menu track, PvE/PvP/duel
 * music variants, {@code wave_begins}/{@code wave_ends} (wave-defense structure), a real in-run shop
 * mechanic ({@code shop_block/draft/sell/shuffle/update}, {@code shot_buying}), a 6-variant "discoball"
 * hit-sound set (a distinctly non-Slavic comedic touch), round/pvp win-lose stingers, and a separate
 * {@code ravent_quest/} boss/PvE music set that is likely a related but distinct quest layer; {@code
 * desktop.ini} is an OS Explorer artifact, not game content (matching {@code StalkerMiscCatalog}'s already-
 * noted outlier pattern). {@code reichstag/} (2: {@code reichstag_sovet.ogg}/{@code
 * reichstag_wehrmacht.ogg}) is a real WWII Soviet-vs-Wehrmacht Reichstag-themed track pair (matching
 * {@code ambience/}'s {@code sc_ambient_reichstag_1/2}), plausibly a Victory Day (May 9) seasonal event.
 * {@code sujet3/} (26) is this part's single richest find, confirming {@code psikaban} ("Psy Boar"), {@code
 * norakabana} ("Boar Nest"), {@code lodochnayastanciya} (the full real "Lodochnaya Stanciya"/"Boathouse
 * Station" name, extending part 27/93's partial resolution), {@code twins} (boss), {@code palach}
 * ("Executioner"), and {@code nina} ("Nina"), plus a NEW real find CONFIRMED this part via {@code
 * en.lang}: {@code zivcas} ({@code zivcas_outpost.ogg}/{@code zivcas_base.ogg}/{@code
 * zivcas_base_calm.ogg}) -> a real in-fiction corporation ({@code anomaly.gui.scanner_high.disclamer}:
 * "The ZIVCAS Corporation is not responsible for user death during special signal searches") that is ALSO
 * a real playable/NPC death-screen faction ({@code stalker.gui.death_screen.faction.zivcas.name=ZIVCAS}).
 * {@code mountain} and {@code zavod} ("factory") appear as further real, disclosed, unconfirmed named
 * locations tied to this questline. {@code terentiy/} (3) corroborates the confirmed Grandpa Terentiy
 * backstory. {@code undead/} (12) is a full previously-uncatalogued real mode discovery: "Undead" (zombie-
 * themed), with a real announcer-line set under {@code dictor/} ({@code evacuation_warning}, {@code
 * humans_left_20_}/{@code humans_left_half}, {@code introduction}, {@code team_mode_enabled}) and a real
 * music set under {@code music/} (evacuation, game, human_fight, intro, lobby, zombie_fight) plus a root
 * {@code undead_idle.ogg}. 4 loose files sit directly under {@code stream/}: {@code "Main Menu
 * Background.ogg"} and {@code "Main Menu Music.ogg"} (capitalized-with-spaces, an outlier naming
 * convention matching {@code StalkerMiscCatalog}'s already-noted root-file outliers), {@code
 * blizzard_loop2.ogg}, and {@code memagent.ogg} (an intriguingly-named file with no {@code en.lang} match
 * -- a real, disclosed, unconfirmed lead, plausibly a meme/easter-egg audio cue).
 *
 * <p><b>{@code streamrandom/}</b> (240 files, 3 subfolders) -- {@code frontline/} (214, this part's
 * largest single subfolder) is a real large-scale PvP mode, matching the real {@code
 * stalker.hud.frontline.*} en.lang namespace ({@code stalker.hud.frontline.intro.title=EFFECT A BREACH}).
 * Its own {@code calldown/} (151: 116 loose {@code calldown_voice_<type>_N.ogg} files covering real named
 * ordnance/support-strike types -- 6p62, acacia, c24, c5, c8, fab100, flamethrower, grad, gvozdika,
 * heal_decor, m249, pfm1, rbk250, respawn, rg6, rpg7, shield_decor, station, supply, universal, zab100 --
 * a real artillery/support "calldown" strike-request system, plus a {@code spsh/} subfolder of 15 further
 * {@code spsh_*.ogg} voice lines and a {@code walkietalkie/} subfolder of 20 more) is this subtree's
 * richest single find. {@code gui/} (8, incl. {@code colonel_click.ogg}/{@code
 * stratagem_choosing.ogg}) confirms a real "Colonel" NPC/commander role and a real stratagem-choice UI.
 * {@code mission/} (6: {@code mission_finished1/2.ogg}, {@code scenario_finished1/2.ogg}, {@code
 * scenario_finished_during_evac1/2.ogg}) confirms a real evac-mission structure. {@code briefing_n/}
 * {@code briefing_n2/}{@code briefing_s/}{@code briefing_s2/} (10+15+10+14 = 49 numbered {@code
 * Nbriefing.ogg} tracks) are real North/South-faction-branded pre-mission briefings, each with a base 0-9
 * set and an extended "2" variant that is gapped (10-14 missing), suggesting a later content pass added
 * tracks 15-19 without renumbering the gap. {@code dmz/} (21, all under its own {@code gui/}) is a real
 * DMZ-mode UI sound-effect set (per-tab hover in/out for adaptation/case/equipment/fame/hook, {@code
 * tab_dossier}/{@code tab_main}/{@code tab_task}, {@code spawn_point}, {@code start_find_game}/{@code
 * starting_search}, {@code upgrade}, {@code converting}, {@code final_screen}) -- corroborating {@code
 * stream/dmz/}'s fight music with a full matchmaking/lobby UI audio layer. {@code nytree/} (5, {@code
 * nytree_music_level1-5.ogg}) corroborates the confirmed X-Mas Tree calendar event with a real 5-level
 * music progression.
 *
 * <p><b>{@code music/}</b> (105 files, all under one {@code bossfight/} folder) -- {@code morgfight/} (41)
 * and {@code shamanfight/} (54) together CONFIRM (upgrading part 93's disclosed-but-unconfirmed cross-
 * naming observation) that Morg and Shaman fight together in one linked 2-phase encounter: BOTH folders
 * contain files for BOTH bosses ({@code morgfight/} holds {@code morg_calling_ambulance}/{@code
 * firing_bullets}/{@code firing_explosives}/{@code remain_20pc_health}/{@code 40pc_health}/{@code
 * using_cover} PLUS {@code shaman_bombers_warning}/{@code destroyed_1st..4th_pillar}/{@code start_fight};
 * {@code shamanfight/} holds {@code shaman_burning_threat}/{@code remain_10/40/70pc_health}/{@code
 * shield_broke}/{@code start_fight} PLUS {@code morg_destroy_pillar}/{@code marked_1st_pillar}/{@code
 * pay_attention}/{@code start_fight}) -- read together this describes a real two-phase fight: Morg attacks
 * (calling in an ambulance/reinforcement, firing bullets/explosives, taking cover) while a shaman defends
 * behind 4 destructible "pillars" and later raises a multi-stage "shield" the player must break, with
 * escalating health-threshold voice barks and mood-variant takes ({@code _calm}, {@code _harsh}, {@code
 * _fast}, {@code _slow}, {@code _scream}, {@code _anguish}) for staged VO delivery. {@code vlasov/} (10)
 * is real Russian-language Colonel Vlasov boss-taunt voice lines whose FILENAMES ARE the literal dialogue
 * text (real, disclosed game-content strings, not player data, quoted per project privacy rules) --
 * resolving the previously-flagged "blessed" narrative thread: Vlasov repeatedly taunts the player as
 * "блаженный" ("blessed"/"holy fool," used as a mocking
 * insult), e.g. "Получи блаженный
 * ублюдок" ("Take that, blessed bastard") and "Ты умрешь
 * блаженный" ("You will die, blessed one"), alongside general
 * boss-taunt lines (defiance, revenge, "I will destroy you").
 */
public final class StalkerStreamCatalog {

    /** The fixed whitelist of large sibling folder names this class exclusively loads. */
    public static final List<String> KNOWN_FOLDERS = List.of("stream", "streamrandom", "music");

    private final Map<String, List<String>> filesByFolder;

    private StalkerStreamCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerRoot the real {@code .../modassets/assets/stalker} folder (the shared parent, not a dedicated one). */
    public static StalkerStreamCatalog load(Path stalkerRoot) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        for (String folderName : KNOWN_FOLDERS) {
            Path folderRoot = stalkerRoot.resolve(folderName);
            if (!Files.isDirectory(folderRoot)) {
                continue;
            }
            List<String> files = new ArrayList<>();
            collectFiles(folderRoot, folderRoot, files);
            Collections.sort(files);
            if (!files.isEmpty()) {
                filesByFolder.put(folderName, files);
            }
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(stalkerRoot + ": none of the known audio/music folders were found");
        }

        return new StalkerStreamCatalog(filesByFolder);
    }

    private static void collectFiles(Path folderRoot, Path dir, List<String> out) throws IOException {
        try (var entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    collectFiles(folderRoot, entry, out);
                } else {
                    out.add(folderRoot.relativize(entry).toString().replace('\\', '/'));
                }
            }
        }
    }

    /** Real known folder names actually found under the given root, alphabetical. */
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
