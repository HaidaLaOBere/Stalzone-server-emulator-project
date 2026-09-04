package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;

/**
 * Catalogs the real narrative/tutorial cutscene assets under {@code modassets/assets/stalkerguide/}
 * (part 59) -- 4 real top-level FOLDERS ({@code sound}, {@code stream}, {@code textures}, {@code
 * video}), confirmed by direct directory listing, holding all 157 real files with NO loose
 * root-vs-walked gap: every file sits inside one of the four folders. Despite the folder's name
 * ("guide"), this is real STORY/cutscene content -- named characters, branching outcomes, and
 * faction-arc videos -- not an anti-cheat or account-system asset, in line with this project's
 * standing focus on game content and possible cut content.
 *
 * <p><b>{@code video/}</b> (99 files): real {@code .mp4} cutscenes, almost all shipped as a
 * {@code _720}/{@code _1080} dual-resolution pair -- except 4 real single-resolution files with no
 * suffix at all: {@code honey_badger}, {@code memagent}, {@code reichstag_atmosphere} (a FIFTH real
 * Reichstag/RKKA WWII-event cross-reference in this project, after armor part 54, backpacks part
 * 56, meds part 57, and currency part 58 -- now a dedicated cutscene, not just an item name), and
 * {@code vozduhan} ("vozdukh" is Russian for "air"; the exact meaning of the {@code -an} suffix is
 * unconfirmed).
 *
 * <p><b>Real named faction/story-arc start/end video PAIRS, live-confirmed to exist as real dual
 * dual-resolution {@code .mp4} pairs</b>: {@code duty_start}/{@code duty_end} (the first real
 * evidence in this project of the "Duty" faction, a name carried over from the base S.T.A.L.K.E.R.
 * series but never before seen in any STALZONE asset, log, or config this project has catalogued),
 * {@code zarya_start}/{@code zarya_end} (corroborating the {@code zarya} armor set found in part
 * 53's barter-item sweep -- now confirmed to have its own dedicated story arc, not just a cosmetic
 * item), {@code zavet_start}/{@code zavet_end} ("zavet" is Russian for "covenant"/"testament", a
 * named story arc with no prior appearance in this project), and {@code merc_start}/{@code
 * merc_end} (corroborating the {@code merc}-prefixed trader ids and zone names found in parts
 * 26/27/53). None of these 8 files, nor the 4 single-resolution files above, have a matching audio
 * track in {@code stream/video/} -- disclosed below as a real, systematic gap.
 *
 * <p><b>{@code stream/}</b> (44 files): 4 real ambient/transition tracks at its own root ({@code
 * elevator_ride.ogg}, {@code getup_anim.ogg}, {@code wind_ext.ogg}, {@code wind_int.ogg}), plus a
 * {@code stream/video/} subfolder of 40 real {@code .ogg} audio tracks that pair by name with a
 * cutscene in {@code video/} above. A live cross-check by filename found this pairing is NOT
 * total in either direction: 38 names are shared, but 2 real audio tracks -- {@code
 * frontline_intro}, {@code inside} -- exist in {@code stream/video/} with NO matching video file
 * anywhere in {@code video/}, and (the complementary gap) 14 real videos -- the 8 start/end pair
 * files and 4 single-resolution files named above, plus {@code cinemat} and {@code
 * floating_away_2} -- exist in {@code video/} with no separate audio track of their own in {@code
 * stream/video/} (plausibly because their audio is muxed directly into the {@code .mp4}, not
 * evidenced by these file listings alone).
 *
 * <p><b>Real named characters and locations recurring across multiple cutscene names, all
 * corroborated by at least 2 independent files each</b>: a character "Vlasov" with two branching
 * outcomes ({@code vlasov_death}, {@code vlasov_escape}); a character/boss "Shaman" with four real
 * cutscene beats ({@code shaman_death}, {@code shaman_flies}, {@code morg_shaman_fight}, {@code
 * morg_shaman_meet}) -- the {@code morg_shaman_*} pair directly corroborates part 55's {@code
 * shamanfight} artefact-model folder, confirming "Shaman" is a real in-fiction character/boss
 * encounter, not just an artefact name; a location "Morg" (plausibly "morgue") with 5 real
 * cutscenes of its own ({@code morg}, {@code morg_death}, {@code morg_fires_roof}, plus the two
 * shaman ones above); and a location/character "Lipa" ("linden tree" in Russian) with 4 real
 * cutscenes ({@code lipa_1}, {@code lipa_2}, {@code lipa_3}, {@code lipa_lodka} -- "lodka" is
 * Russian for "boat") plus a fifth, {@code tutorial_lipa_tonnel} ("tonnel" = "tunnel"), tying Lipa
 * into the tutorial sequence below.
 *
 * <p><b>A real 4-part tutorial sequence</b>: {@code tutorial_intro}, {@code
 * tutorial_lipa_tonnel}, {@code tutorial_ejection}, {@code tutorial_outro} -- all 4 confirmed
 * present as real dual-resolution video pairs with matching {@code stream/video/} audio tracks.
 * Separately, 3 real numbered generic-story cutscenes exist as their own family: {@code sstory1},
 * {@code sstory2}, {@code sstory3}.
 *
 * <p><b>Other real cutscene names, disclosed with no further evidence of their in-fiction
 * context</b>: {@code ball}, {@code ball2}, {@code Boat} (capitalized, unlike every other file in
 * this tree), {@code broken}, {@code club}, {@code inside}, {@code frontline_intro}, {@code
 * grenade_throw}, {@code heli_crash}, {@code helicopter}, {@code knifestab}, {@code meet}, {@code
 * oracle}, {@code phits}, {@code ppayalnik}, {@code pshoker}, {@code pshot}, {@code shepot}
 * (Russian for "whisper"), {@code slayd} (plausibly "slide"), {@code floating_away}/{@code
 * floating_away_2}, {@code cinemat} (plausibly a generic "cinematic" placeholder name).
 *
 * <p><b>{@code sound/}</b> (11 files): 3 real elevator sound effects at its own root ({@code
 * elevator_bell_synth.ogg}, {@code elevator_close.ogg}, {@code elevator_open.ogg} -- pairing with
 * {@code stream/elevator_ride.ogg} above to suggest a real elevator sequence somewhere in this
 * guide/tutorial flow), one generic {@code hint.ogg}, and a real {@code glitch_monitor/} subfolder
 * of 7 numbered variants ({@code glitch_monitor_1.ogg} through {@code _7.ogg}) -- a real
 * "glitched monitor/screen" audio effect with 7 randomized takes, unconfirmed as to which UI
 * moment triggers it.
 *
 * <p><b>{@code textures/gui/}</b> (3 files): {@code advbanner.mic}, {@code banner.mic}, {@code
 * palka.mic} ("palka" is Russian for "stick"/"pole") -- real GUI banner textures for this
 * guide/tutorial system, using the same {@code .mic} shader/effect-role extension this project
 * first saw in part 55's {@code shamanfight/shield/perlin.mic} and later in part 58's {@code
 * token_event.mic}.
 */
public final class StalkerGuideCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerGuideCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerGuideDir the real {@code .../stalkerguide} folder. */
    public static StalkerGuideCatalog load(Path stalkerGuideDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalkerGuideDir)) {
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
            throw new IOException(stalkerGuideDir + ": no stalkerguide asset folders found");
        }

        return new StalkerGuideCatalog(filesByFolder);
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

    /** Real top-level stalkerguide asset folder names, alphabetical. */
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
