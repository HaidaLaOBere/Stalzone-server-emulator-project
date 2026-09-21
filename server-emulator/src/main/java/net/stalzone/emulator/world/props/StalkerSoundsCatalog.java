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
 * Catalogs the real assets under {@code modassets/assets/stalkersounds/} (part 92) -- 805 files
 * across exactly 2 real top-level folders, no loose root files (unlike part 91's {@code
 * stalkerplayer/}, which needed a {@code ROOT_KEY} deviation -- confirmed by direct real-folder
 * inspection that {@code stalkersounds/} has none, so this class follows {@link
 * StalkerClansCatalog}'s plain per-top-level-folder walk instead): {@code music/} (145 files, flat,
 * no subfolders) and {@code stream/} (660 files across a {@code guitar_na/} subfolder and a
 * {@code radio/rock1_radio/{en,ru}/} tree, plus loose files at {@code stream/}'s own root). Squarely
 * game content (music tracks, ambient ogg streams, voiced audio-diary/cassette collectibles), not an
 * EXENS anti-cheat/launcher asset, so stays in scope under rule 2. Cross-referenced against real
 * {@code stalker/lang/en.lang} strings under rule 1's literal-word evidentiary bar; several distinct
 * real narrative threads surfaced, documented below by file-name family.
 *
 * <p><b>Audio-diary / cassette collectible chains (all confirmed real, {@code music/}):</b> {@code
 * smotrikov1/2/3} are a real audio-diary chain ({@code item.quest.smotrikov1.name=Audio Recorder:
 * Greeting}, {@code smotrikov2.name=White Cassette: 42}, {@code smotrikov3.name=White Cassette: 51}).
 * {@code breadcrumbs_audiocassette_*} (all 9 of {@code fundament_7}, {@code lekciya_1}, {@code
 * nepredskazuemost_8}, {@code paradoxteseya_3}, {@code poligon_4}, {@code prestizhno_5}, {@code
 * rashozhdeniye_2}, {@code razvoplosheniye_9}, {@code reabilitaciya_6}) are real, all confirmed with
 * real English titles ("Lecture Cassette", "Divergence Cassette", "Ship of Theseus Cassette",
 * "Area-16 Cassette", "Prestige Cassette", "Rehabilitation Cassette", "Foundation Cassette",
 * "Unpredictability Cassette", "Disembodiment Cassette"), revealing a real "psychoscope"-based
 * personality-cassette narrative mechanic (real "Fierce"/"Silence" personality tags) and a real
 * second named facility, {@code Area-16}, distinct from {@code Area-7}. {@code
 * operatsii_audiodnevnik1-7} are ALL 7 confirmed real, revealing real {@code Area-7} sub-locations
 * (Administration, Laboratory, Mutation Department, Reactor Room) plus 3 cryptic real cassette titles
 * ('Ad astra!', 'Incident', 'Accelerator Laboratory') -- a real science-facility-incident lore thread.
 * {@code audio_diary_dmz_{jiguli,lada,pegas}} are ALL 3 confirmed real ("Military Cassette from Boral
 * 1/2/3"), tying to a real named DMZ region, {@code Boral} ({@code map.global.region.dmz.name=Boral},
 * real lore text describing "a mysterious island... walking dead"). Note {@code
 * location.battleroyale.name} is ALSO real {@code Boral} -- a real, disclosed namespace overlap
 * between the DMZ and battle-royale game modes, stated here without asserting they are necessarily
 * the same in-world place (no further evidence either way).
 *
 * <p><b>The {@code _typoon} cassette family ({@code music/}, ALL 9 confirmed real):</b> {@code
 * freedom_typoon.name=Rise Member Cassette} (real, using the "Rise" name for the {@code freedom}
 * alliance id -- a real new lead on part 91's disclosed Dawn/Rise naming inconsistency, see below),
 * {@code covenant_typoon.name=Covenant Member Cassette}, {@code duty_typoon.name=Frontier Member
 * Cassette} (matches part 91's confirmed Frontier/duty crosswalk), {@code
 * merc_typoon.name=Mercenary Honor Cassette}, {@code zivcas_typoon.name=ZIVCAS Cassette: Typhoon},
 * {@code murmur_typoon.name=Typhoon Cassette: Murmur}, {@code shaiba_typoon.name=Typhoon Cassette:
 * Puck}, {@code polygons_typoon.name=Typhoon Cassette: Areas} (ties to the Area-7/Area-16 thread
 * above), {@code unknown_typoon.name=Unknown Stalker Cassette}. All 9 real descriptions place these
 * on "the body of a &lt;faction&gt; fighter in the Forge area" -- a real "Typhoon" faction/operation
 * distinct from the 6 confirmed playable alliances, fighting in a real "Forge" (also seen as
 * "Forge-11") area. This also confirms two real non-playable, non-alliance factions: {@code ZIVCAS}
 * (a real "ZIVCAS Scientific Research Institute", a government-contracted paramilitary/scientific
 * organization -- {@code cnps.faction.zivcas.name}, multiple real {@code pda.waypoint.we.*_zivcas}
 * entries) and {@code Murmur} (a real hostile armed faction tied to psionic effects and a real "Radar
 * Station" bunker in the "Wild North", per real {@code frontline.briefing_*} text -- explicitly "not
 * allies" with ZIVCAS per that same text). A real {@code item.quest.doklad_dla_typoon} note further
 * mentions "some Dawn members" and "their current leader, Chatsky", alongside "the former leader of
 * Rise, Luka, went missing" -- a real new lead touching part 91's disclosed Dawn/Rise inconsistency,
 * deliberately NOT resolved here (the text is consistent with either two distinct groups or one
 * renamed over time; no further evidence disambiguates it, so this stays an open lead under rule 1).
 *
 * <p><b>Other real {@code music/} cross-references:</b> {@code goblin1-14} tie to a real confirmed
 * boss ({@code item.camo.unique_arm.goblin.name=Leshy}, achievement "Kill Goblin"/"Defeat the
 * Goblin", id {@code rijik_miniboss_2021}). {@code naemnikiaudiodnevnik*} ties to a real ambient track
 * ({@code phonograph.gui.track.ambient_nn_naemniki=Sounds of the Serpentine}) though the audio-diary
 * files themselves have no direct {@code en.lang} match (real disclosed unconfirmed lead). {@code
 * zaryaaudiodnevnik*} ties only to a real, unrelated brand-translation pattern ({@code
 * item.arm.zarya.name=Aurora Jumpsuit}, {@code item.exp.zarya.name=Zarya-3 Flashbang Grenade}) --
 * explicitly does NOT resolve the Dawn/Rise inconsistency above (a hypothesis raised and then
 * dropped for lack of literal-word evidence). {@code zavetaudiodnevnik*}, {@code kasseta_stalkera1-4},
 * {@code kasseta_bunker}, {@code kasseta_voenstala}, {@code kasseta_zorki1-2}, and {@code
 * shlam_cassete1-2} have no {@code en.lang} match found -- real disclosed unconfirmed leads, named
 * here rather than silently dropped, per rule 1.
 *
 * <p><b>{@code stream/}'s own root</b> (loose files, collected here under the {@code stream} folder
 * key alongside its subfolders) includes a real {@code loudspeaker_limansk_1-6.ogg} set, cross-
 * referencing a real repeatable quest-contract type ({@code
 * quest_contract.quest_event_limansk_1-5.name=Camp clear-out assignment}); {@code
 * radioburan_1-17.ogg}, cross-referencing a real confirmed Buran-to-"Blizzard" translation ({@code
 * phonograph.gui.track.buran_track_1-4=Blizzard 1-4}); {@code radiosssr_2-55.ogg} with a real
 * disclosed gap (no {@code radiosssr_1.ogg} exists); {@code radar_tower_1-11.ogg}; {@code
 * bar_npc_one.ogg}; and {@code soundvoid.ogg}. <b>{@code stream/guitar_na/}</b> (27 files, {@code
 * guitar_1-27.ogg}) duplicates {@code stream/}'s own root-level {@code guitar_1-27.ogg} numbering --
 * apparently a regional/duplicate variant, not investigated further. <b>{@code
 * stream/radio/rock1_radio/en/}</b> (111 files, {@code en_radio_host_&lt;N&gt;.ogg}, real sparse
 * numbering 1-210) and <b>{@code stream/radio/rock1_radio/ru/}</b> (405 files: 175 {@code
 * ru_radio_host_&lt;N&gt;.ogg} sparsely numbered 1-324, plus 230 {@code
 * ru_radiorock_song_&lt;N&gt;.ogg} nearly-complete 1-231) confirm a real localized in-world radio
 * station with separate English/Russian DJ voice-line and song-track pools.
 */
public final class StalkerSoundsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerSoundsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerSoundsDir the real {@code .../modassets/assets/stalkersounds} folder. */
    public static StalkerSoundsCatalog load(Path stalkerSoundsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalkerSoundsDir)) {
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
            throw new IOException(stalkerSoundsDir + ": no stalkersounds asset folders found");
        }

        return new StalkerSoundsCatalog(filesByFolder);
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

    /** Real top-level stalkersounds-asset folder names, alphabetical ({@code music}, {@code stream}). */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /**
     * Real file paths (relative to the folder itself) inside one real folder, or empty if unknown.
     * For {@code stream}, this includes nested {@code guitar_na/} and {@code radio/rock1_radio/...}
     * paths alongside the folder's own loose root files.
     */
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
