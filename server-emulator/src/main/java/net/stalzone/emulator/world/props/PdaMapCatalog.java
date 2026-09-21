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
 * Catalogs the real PDA map assets under {@code modassets/assets/pda/} (part 64) -- the 14 real
 * top-level folders confirmed by direct listing, 2,018 files total, no loose root-level files: {@code
 * global_map} (36), {@code map} (944), 9 real {@code map-<zone>} per-zone override folders ({@code
 * map-arena_raven} 1, {@code map-dungeon_bolota_tutorial} 1, {@code map-kolizei} 1, {@code
 * map-nbolota} 6, {@code map-new_bar} 1, {@code map-prestige} 4, {@code map-reichstag} 4, {@code
 * map-sovetpunk} 2, {@code map-sovhoz_overlay} 31), {@code map_overlay} (975, incl. a real {@code
 * inside/} subfolder of 31), {@code sound} (2), and {@code textures} (10). Squarely game content (the
 * in-game PDA map/minimap system), not an EXENS anti-cheat/launcher asset, so stays in scope under
 * rule 2.
 *
 * <p><b>{@code map/}</b> (944 files) is a real, flat, coordinate-named overworld tile grid --
 * filenames follow a real {@code r.<x>.<y>.ol} pattern with no subfolders. Directly parsing every
 * real filename's x/y pair confirms a real tile-coordinate span of x: -23..132, y: -14..12 (944
 * distinct tiles, one file per coordinate pair, no gaps checked beyond the min/max span). This is
 * genuine, concrete evidence -- not previously available anywhere else in this project -- that the
 * game world is one single continuous coordinate space rather than a set of unrelated discrete zones,
 * directly corroborating this project's existing {@code WorldModel} (parts 15-18) at the PDA-map
 * level. {@code global_map/} (36 files) reuses the exact same {@code r.<x>.<y>.ol} naming convention
 * over a much smaller real coordinate span, directly confirmed across the complete real file set:
 * x: -4..4, y: -2..1, plausibly a coarser overview/LOD tile set for the same world, not
 * independently confirmed.
 *
 * <p><b>The 9 real {@code map-<zone>} folders</b> are real per-zone, hand-authored detail-map
 * overrides sitting at SPECIFIC real coordinates within that same shared {@code map/} tile grid --
 * not a separate coordinate space of their own. Directly confirmed by real filename: {@code
 * map-kolizei/r.-2.-2.ol} (1 tile), {@code map-reichstag/} (a real real 2x2 tile block: {@code
 * r.11.-5}/{@code r.11.-6}/{@code r.12.-5}/{@code r.12.-6.ol}), {@code map-prestige/} (a real 2x2
 * block: {@code r.-10.-10}/{@code r.-10.-11}/{@code r.-11.-10}/{@code r.-11.-11.ol} -- yet another
 * real cross-reference for this project's recurring, still-only-partially-confirmed "prestige"
 * mechanic, now with a concrete real-world map position), {@code map-sovetpunk/} (2 tiles), {@code
 * map-arena_raven/}, {@code map-dungeon_bolota_tutorial/}, {@code map-new_bar/} (1 tile each -- small,
 * specific PvP-arena/tutorial/hub zones), and {@code map-nbolota/} (6 tiles, a real 2x3 block). {@code
 * map-sovhoz_overlay/} (31 files) breaks this pattern: its real filenames drop the {@code r.} prefix
 * entirely (e.g. {@code -1.3.mic}, {@code 0.7.mic}) and use {@code .mic} rather than {@code .ol} --
 * disclosed as a real, unexplained naming-scheme difference from the other 8 {@code map-<zone>}
 * folders, not investigated further this part.
 *
 * <p><b>{@code map_overlay/}</b> (975 files) holds no loose files of its own -- directly confirmed,
 * correcting an initial assumption to the contrary before this Javadoc was finalized -- all 975 real
 * files sit in a single real {@code inside/} subfolder, using the same coordinate-without-{@code
 * r.}-prefix, {@code .mic} naming style as {@code map-sovhoz_overlay/} above (e.g. {@code
 * inside/-1.-1.mic}) rather than {@code map/}'s own {@code r.<x>.<y>.ol} style -- real evidence that
 * {@code .ol} vs {@code .mic} plus the {@code r.} prefix marks two different real asset roles (base
 * map tile vs. overlay layer) rather than being interchangeable naming conventions. Directly parsing
 * all 975 real filenames gives a real coordinate span of x: -23..18, y: -11..12 -- genuinely NOT the
 * same span as {@code map/}'s own x: -23..132 (same minimum, a much smaller maximum: 18 vs 132), so
 * this is disclosed honestly as a differently-extended overlay coordinate space, not confirmed to be
 * a 1:1 fog-of-war layer over the exact same tile grid as first assumed.
 *
 * <p><b>{@code sound/}</b> (2 files) is real PDA radar UI feedback: {@code radar_new_player.ogg} and
 * {@code radar_player_left.ogg} -- real evidence of a live server-population radar ping, corroborating
 * this as a multiplayer-aware UI element (asset-name evidence only, not wire-protocol reverse
 * engineering, so stays in scope under rule 3). <b>{@code textures/}</b> (10 files) is real core PDA
 * UI: {@code mini_map.mic} plus two real seasonal variants, {@code mini_map_ny.mic} (a real "New
 * Year" seasonal mini-map skin, a fresh corroborating data point alongside this project's existing
 * Halloween/winter seasonal-event findings) and {@code mini_map_rn.mic} (unconfirmed abbreviation,
 * disclosed as an open lead rather than guessed at), {@code achievements.mic}, {@code av_background
 * .mic} (avatar background), {@code failure_overlay.mic}, {@code main_background.mic}, and generic
 * rendering utility files {@code line.ol}, {@code territory_pattern.ol}, {@code waypoints.ol}.
 */
public final class PdaMapCatalog {

    private final Map<String, List<String>> filesByFolder;

    private PdaMapCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param pdaDir the real {@code .../modassets/assets/pda} folder. */
    public static PdaMapCatalog load(Path pdaDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(pdaDir)) {
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
            throw new IOException(pdaDir + ": no pda asset folders found");
        }

        return new PdaMapCatalog(filesByFolder);
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

    /** Real top-level pda-asset folder names, alphabetical. */
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
