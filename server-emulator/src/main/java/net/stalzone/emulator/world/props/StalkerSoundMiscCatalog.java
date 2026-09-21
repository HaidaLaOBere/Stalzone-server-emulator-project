package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs 100 of the 102 real top-level folders (plus the loose root files) under
 * {@code modassets/assets/stalker/sound/} (part 102) -- 3,026 files: 132 real loose audio files at the
 * folder's own root, plus 100 real named subfolders. This is a deliberate WHITELIST sweep, not a complete
 * folder walk: {@code sound/}'s full real shape is 10,500 files across 102 subfolders plus the root, but
 * two subfolders -- {@code events/} (4,352 files) and {@code closeenvironment/} (3,122 files) -- are each
 * individually larger than everything else in this class combined, and are deliberately left for future
 * dedicated parts, following the "small stuff swept together, big stuff standalone" precedent parts 96/97
 * established for {@code textures/}. This is nonetheless this project's largest single whitelist-style
 * sweep to date (prior largest: part 94's 676 files across 3 folders). {@link #load} reuses the same
 * synthetic {@link #ROOT_KEY} deviation established at part 91 and reused every part since for a folder
 * with a large, non-trivial set of loose root files (132 -- more than all but a handful of the 100
 * subfolders individually hold).
 *
 * <p>This tree is {@code stalker/sound/}, entirely distinct from {@link StalkerSoundsCatalog}'s
 * {@code modassets/assets/stalkersounds/} (part 92) and from part 94's {@code StalkerStreamCatalog}'s
 * {@code stream/}/{@code streamrandom/}/{@code music/} (all direct children of {@code stalker/}, not
 * {@code stalker/sound/}). Headline {@code en.lang} confirmations from this part:
 *
 * <ul>
 *   <li>{@code 24bosses/}'s real {@code ghoul_*.ogg} creature sounds (attack/death/detect/
 *       drinking_blood/idle/pain) CONFIRM part 76's real canonical mutant name "krovosos" (previously
 *       named but never given an English display name in this project) is the real "Ghoul" --
 *       {@code entity.krovosos.name=Ghoul} -- with a further real illusory/psionic variant,
 *       {@code entity.krovosos_illuziya.name=Ghoul (illusion)}, tying this creature to part 92's confirmed
 *       Murmur psionic faction.</li>
 *   <li>{@code plumber/}'s {@code provod_turn_*.ogg} ("provod" = "wire", Russian) plus the separate
 *       {@code wire/} folder disclose a real wire/pipe-connection minigame (a {@code plumber_win.ogg}
 *       success cue); {@code runa/}'s {@code morse.ogg} plus four directional {@code radio_*.ogg} cues
 *       disclose a real Morse-code radio minigame corroborating this project's existing confirmed
 *       {@code MorseAlphabet} (an early-part class, part 9-ish). Neither "plumber" nor "runa" literally
 *       appears in {@code en.lang}, so both mechanics are disclosed by file-content inference, not
 *       confirmed by a literal key match.</li>
 *   <li>{@code rlstower/}'s {@code radartowerdown.ogg}/{@code radartoweridle.ogg} and {@code radarcity/}'s/
 *       {@code radartonnel/}'s ambience (incl. {@code radartonnel/snork_screamer.ogg}, corroborating part
 *       76's confirmed "snork" mutant) are real location-ambience sets for the Radar Station/Area-16
 *       complex part 92 already confirmed, extending it from lore text to actual environmental audio.</li>
 *   <li>{@code dungeon_mir_korabley_cave/} ("Mir Korabley" = "World of Ships", Russian) and
 *       {@code dungeon_unnamed_deep_house/} (whose only real content, {@code merc_raid.ogg}/
 *       {@code merc_raid_ambience.ogg}, ties it to the confirmed {@code merc} alliance from part 91) are
 *       two further real named dungeon locations, disclosed but not cross-referenced against any existing
 *       map roster.</li>
 *   <li>{@code voland/} is a further real, previously-uncatalogued named boss/enemy -- not merely
 *       ambience: alongside a real environment set (bed springs, bottle clinks, a repair-complete jingle,
 *       meditation radio, and {@code musicexorci_radio.ogg}, tying it to the separate {@code exorcism/}
 *       folder's door open/close cues), it has its own real {@code voland_battle_phrase_*.ogg} (four
 *       lines) and {@code voland_pain_scream_*.ogg} (four lines) combat voice sets. "Voland" is also the
 *       name of the Devil character in Bulgakov's "The Master and Margarita", a plausible but NOT
 *       confirmed naming inspiration (no literal {@code en.lang} match for "voland" found).</li>
 *   <li>{@code socialstels/}'s {@code blessed_screaming.ogg} directly corroborates part 94's confirmed
 *       Colonel Vlasov "blazhenniy"/"blessed" narrative thread with an actual sound cue, previously known
 *       only from filename-as-dialogue text.</li>
 *   <li>{@code boss_scalolaz/} ("skalolaz" = "climber", Russian) and {@code newnorth_boss/} (all files
 *       prefixed {@code Palach_}, "palach" = "executioner", Russian) are two further real, previously
 *       -uncatalogued named bosses, each with a substantial real voice-line set (attack/monologue/laugh
 *       cues for Skalolaz; a large numbered {@code Palach_N.ogg} line set); neither name has a literal
 *       {@code en.lang} match, so both are disclosed as real bosses by asset evidence, not confirmed
 *       display names.</li>
 *   <li>{@code tochka_nevozvrata26/} ("Tochka Nevozvrata" = "Point of No Return", Russian, 2026-dated) is a
 *       real, previously-uncatalogued 2026 event/mission with its own grenade/radio/scream cue set,
 *       disclosed but with no literal {@code en.lang} match found for its name.</li>
 * </ul>
 */
public final class StalkerSoundMiscCatalog {

    /** Synthetic folder key for the 132 real loose files at {@code sound/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    /** Real top-level subfolders deliberately excluded from this sweep -- left for future dedicated parts. */
    public static final List<String> EXCLUDED_FOLDERS =
            Collections.unmodifiableList(java.util.Arrays.asList("events", "closeenvironment"));

    private final Map<String, List<String>> filesByFolder;

    private StalkerSoundMiscCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /**
     * @param soundDir the real {@code .../modassets/assets/stalker/sound} folder. {@link #EXCLUDED_FOLDERS}
     *     are skipped even if present under {@code soundDir}.
     */
    public static StalkerSoundMiscCatalog load(Path soundDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(soundDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    String folderName = entry.getFileName().toString();
                    if (EXCLUDED_FOLDERS.contains(folderName)) {
                        continue;
                    }
                    List<String> files = new ArrayList<>();
                    collectFiles(entry, entry, files);
                    Collections.sort(files);
                    filesByFolder.put(folderName, files);
                } else {
                    rootFiles.add(entry.getFileName().toString());
                }
            }
        }

        if (!rootFiles.isEmpty()) {
            Collections.sort(rootFiles);
            filesByFolder.put(ROOT_KEY, rootFiles);
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(soundDir + ": no sound asset folders/files found");
        }

        return new StalkerSoundMiscCatalog(filesByFolder);
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

    public List<String> folderNames() {
        return new ArrayList<>(filesByFolder.keySet());
    }

    public List<String> filesIn(String folderName) {
        List<String> files = filesByFolder.get(folderName);
        return files == null ? Collections.emptyList() : Collections.unmodifiableList(files);
    }

    public List<String> foldersContaining(String substring) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesByFolder.entrySet()) {
            for (String file : entry.getValue()) {
                if (file.contains(substring)) {
                    out.add(entry.getKey());
                    break;
                }
            }
        }
        return out;
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
