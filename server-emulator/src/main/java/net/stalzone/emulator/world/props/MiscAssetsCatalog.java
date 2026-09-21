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
 * Catalogs the real assets under {@code modassets/assets/} for the 8 remaining small, unrelated leaf
 * trees swept together in part 79, closing out the "remaining unswept trees" list from parts 73-78:
 * {@code chat/} (3), {@code ejection/} (4), {@code probuilder/} (4), {@code handcuffs/} (2), {@code
 * skinarmor/} (1), {@code party/} (1), {@code misc/} (1), {@code trade/} (1) -- 17 files total. Each is
 * far too small to justify its own single-purpose catalog class (this project's established one-tree-
 * per-class pattern), so this class deliberately deviates from every prior {@code world.props} catalog:
 * instead of walking every immediate top-level subfolder of one dedicated directory, {@link #load} is
 * given the shared {@code modassets/assets/} root and walks only a fixed whitelist of 8 known folder
 * names ({@link #KNOWN_FOLDERS}), each treated as one "folder" bucket in the returned map and recursed
 * fully (including any loose files directly at that bucket's own root -- unlike the folder-only walk
 * used by every other catalog in this package, since these buckets have no further meaningful
 * subfolder structure worth distinguishing). This means {@link #load} does NOT see or index anything
 * else under {@code modassets/assets/} -- it is not a general-purpose walker over the whole assets root.
 * Squarely game content, not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code chat/}</b> (textures/gui/, 3 files) -- in-game chat-window background textures: {@code
 * chat_bg.mic}, a New Year seasonal variant {@code chat_bg_ny.mic} (this project's now-familiar seasonal-
 * skin convention, seen elsewhere for cases/backgrounds), and {@code chat_rn.mic} -- an unconfirmed
 * abbreviation, disclosed as an open lead rather than guessed at.
 *
 * <p><b>{@code ejection/}</b> (sound/, stream/, 4 files) -- three {@code thunder1-3.ogg} clips plus a
 * 7.2 MB {@code windstorm.ogg} ambience stream. No storm/weather asset tree was found anywhere else in
 * this project. "Ejection" is a plausible (unconfirmed) English rendering of the real S.T.A.L.K.E.R.-
 * series "emission"/"blowout" mechanic (Russian "выброс", literally "ejection" or "throw-out") --
 * periodic deadly psi-storms that are a defining feature of the source series. This is a real, disclosed
 * lead pointing at that mechanic via the folder name plus matching storm-sound content, not a confirmed
 * in-game feature (no emission-timer/shelter config was found to corroborate it further).
 *
 * <p><b>{@code probuilder/}</b> (4 files) -- {@code smoothing.txt}, a real 8-bit neighbor-pattern lookup
 * table for auto-smoothing block/voxel terrain edges (documented in the file's own header comment), plus
 * 3 real hand-tool icon textures ({@code door.ol}, {@code hammer.ol}, {@code spraycan.ol}). The folder
 * name coincides with Unity's real "ProBuilder" mesh-editing plugin, but {@code smoothing.txt}'s actual
 * content is a block-terrain smoothing bitmask table, not ProBuilder mesh data -- disclosed as a naming
 * coincidence, not confirmed Unity-editor tooling. The block/voxel terrain-smoothing content itself is a
 * fresh, concrete corroboration of this project's already-documented Minecraft-derived-engine hypothesis
 * (part 63 addendum, part 67's Connected-Textures-Mod convention).
 *
 * <p><b>{@code handcuffs/}</b> (models/, 2 files) -- {@code handcuffs.mcsb}/{@code handcuffs.ol}, a real
 * working restraint-item model+texture pair, consistent with a capture/arrest mechanic.
 *
 * <p><b>{@code skinarmor/}</b> (textures/items/, 1 file) -- {@code skin.mic}, a single armor-skin/cosmetic
 * texture; too small to say more without further evidence.
 *
 * <p><b>{@code party/}</b> (1 file) -- {@code weapon_levels.cfg}, containing exactly one real line,
 * {@code 25000=NEWBIE}: a weapon-XP threshold (25000) mapped to a named tier ("NEWBIE"). Confirms a real
 * weapon-leveling/tier-naming system distinct from part 66's "prestige" ability system -- only one
 * threshold is present in the real file, so the rest of the tier ladder (if any exists) is not evidenced
 * here.
 *
 * <p><b>{@code misc/}</b> (textures/items/barter/, 1 file) -- {@code triumph_compensation.ol}, a real
 * barter/compensation item texture; "triumph" is not corroborated elsewhere in this project.
 *
 * <p><b>{@code trade/}</b> (textures/gui/, 1 file) -- {@code trade.mic}, a real trade-window GUI texture,
 * a distinct namespace from the already-catalogued {@code shop/} (part 73) and {@code auction/} (part 77)
 * UI trees.
 */
public final class MiscAssetsCatalog {

    /** The fixed whitelist of small leaf-tree folder names this class exclusively loads. */
    public static final List<String> KNOWN_FOLDERS = List.of(
            "chat", "ejection", "probuilder", "handcuffs", "skinarmor", "party", "misc", "trade");

    private final Map<String, List<String>> filesByFolder;

    private MiscAssetsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param assetsRoot the real {@code .../modassets/assets} folder (the shared parent, not a dedicated one). */
    public static MiscAssetsCatalog load(Path assetsRoot) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        for (String folderName : KNOWN_FOLDERS) {
            Path folderRoot = assetsRoot.resolve(folderName);
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
            throw new IOException(assetsRoot + ": none of the known small asset folders were found");
        }

        return new MiscAssetsCatalog(filesByFolder);
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

    /** Real known-small-tree folder names actually found under the given root, alphabetical. */
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
