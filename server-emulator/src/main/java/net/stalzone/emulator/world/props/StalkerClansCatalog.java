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
 * Catalogs the real assets under {@code modassets/assets/stalkerclans/} (part 72) -- 153 files across
 * 5 real top-level folders, no loose root files: {@code battlefields} (15), {@code models} (17),
 * {@code sound} (9), {@code stream} (1), {@code textures} (111). Squarely game content (a clan-war PvP
 * mode's maps/props/audio), not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 * This is this project's first direct evidence of a full clan-war PvP mode with capture-the-flag
 * mechanics, and a rich real map roster directly matching well-known S.T.A.L.K.E.R.-series locations.
 *
 * <p><b>{@code battlefields/}</b> (15 real {@code .mic} files, one per named clan-war map) is a rich
 * real battlefield-map roster, several names directly matching well-known real S.T.A.L.K.E.R.-series
 * locations -- {@code kordon} (a real, iconic starting-zone location name), {@code pripyat} (the real
 * Chernobyl-adjacent city the whole franchise is set around), {@code svalka} ("junkyard/dump", Russian,
 * a real classic S.T.A.L.K.E.R. location) -- alongside {@code reich} (a real, FIFTH independent tree
 * corroborating the Reichstag/WWII event, after parts 61, 64, 66, and 70), and custom real map names
 * {@code bridges}, {@code hospital}, {@code idiotway}, {@code jail}, {@code kishka} ("gut/intestine",
 * Russian, plausibly a narrow-corridor map), {@code kolizei} ("coliseum"), {@code krot} ("mole"), {@code
 * moryatnik} (a real, unexplained fresh name lead), {@code peklo} ("hell/inferno", Russian/Ukrainian),
 * {@code td} (RESOLVED part 105: real name "Black Willows", see {@link
 * net.stalzone.emulator.world.LocationNameRegistry}), {@code yama} ("pit").
 *
 * <p><b>UPDATED part 106:</b> {@code moryatnik} is RESOLVED -- {@code en.lang}'s real {@code
 * battlefield.id.moryatnik.name=Moria House} gives it an English display title (see {@link
 * net.stalzone.emulator.world.props.BattlefieldCatalog}, which also cross-references this class's
 * full {@code battlefields/} roster against that namespace directly and discloses where the two
 * real sources agree and disagree). A distinct real map id, {@code reich_breakthrough}, is a NINTH
 * independent Reichstag corroboration there, with real named historical Berlin landmark capture
 * points (Moltke Bridge, Brandenburg Gate, the Interior Ministry Building, Victory Column, Bunker).
 *
 * <p><b>UPDATED part 108:</b> a completely separate real {@code en.lang} namespace, {@code
 * clans.*} (98 lines, see {@link net.stalzone.emulator.world.props.ClanWarCatalog}), was checked
 * directly against this class's own 15-map {@code battlefields/} roster and found to be a
 * genuinely distinct, non-overlapping real clan-war map system (FFA/tournament maps like {@code
 * nizina}/{@code roza_vetrov}/{@code hvoiny}/{@code kvartaly}/{@code small_berdovka}, none of which
 * match any id here) -- disclosed so a future part doesn't mistake the two for the same roster
 * just because both are real and both say "clan(s)".
 *
 * <p><b>{@code models/}</b> (17 files, no subfolders) confirms real capture-the-flag PvP mechanics:
 * {@code flag.mcsb}/{@code flag_down.ol}/{@code flag_up.ol} (a real raised/lowered flag-state prop
 * set), {@code clanchest.mcsb} (a real clan loot-chest prop), 3 real placeable-turret tiers ({@code
 * turrel1}/{@code turrel2}/{@code turrel3.mcsb}, each with a matching {@code .ol} texture), {@code
 * explosive.mcsb}/{@code explosive.ol} (a real placeable explosive prop), and real {@code br_root}/
 * {@code br_spawn} folders (1 file each) whose {@code br_} prefix is directly corroborated by {@code
 * sound/}'s own {@code br_cinematic_missle_strike.ogg}/{@code br_wall.ogg} as "battle royale" --
 * {@code br_wall.ogg} plausibly the real shrinking-play-zone sound cue for that mode, a classic
 * battle-royale mechanic not previously confirmed by name in this project.
 *
 * <p><b>{@code sound/}</b> (9 files) further corroborates the clan-war/BR angle: {@code
 * fight_preparation_start.ogg} (a real pre-match cue), {@code transmit_start.ogg}/{@code
 * transmit_end.ogg} (a real radio-transmission cue pair), and a real {@code gg_player_level_up.ogg}/
 * {@code gg_player_level_down.ogg} pair (a real clan/player ranking-change cue, {@code gg} plausibly
 * "clan" or "gang" -- not confirmed) alongside a real {@code gg_player_katana.ogg}/{@code
 * gg_player_katana_other.ogg} pair, cross-referencing the existing real Katana melee-weapon type (part
 * 65's {@code character/casts/CharacterCharacterMeleeLayerKatana.sai}).
 *
 * <p><b>{@code stream/}</b> (1 file, {@code EvacuationBackground.ogg}) is a real named "Evacuation"
 * background-music track, plausibly tied to a match-ending extraction/evacuation phase, not
 * investigated further. <b>{@code textures/}</b> (111 files, 4 real subfolders: {@code gui}, {@code
 * items}, {@code logo}, {@code logo-dds}) confirms a real customizable clan logo/emblem system: {@code
 * logo/} and its DDS-format mirror {@code logo-dds/} both hold real preset emblem names (e.g. {@code
 * alpha.ol}, {@code anarchy.ol}, {@code antarctica.ol}), a real selectable clan-emblem catalog rather
 * than a single generic placeholder icon, though the full preset list was not individually inventoried
 * this part. {@code gui/} has a real {@code atlas_battlefields_sections.mic} (cross-referencing the
 * {@code battlefields/} roster above) and {@code atlas_killlog.mic}/{@code battle_drop.mic}, the latter
 * plausibly a battle-royale-style airdrop UI cue.
 */
public final class StalkerClansCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerClansCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerClansDir the real {@code .../modassets/assets/stalkerclans} folder. */
    public static StalkerClansCatalog load(Path stalkerClansDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalkerClansDir)) {
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
            throw new IOException(stalkerClansDir + ": no stalkerclans asset folders found");
        }

        return new StalkerClansCatalog(filesByFolder);
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

    /** Real top-level stalkerclans-asset folder names, alphabetical. */
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
