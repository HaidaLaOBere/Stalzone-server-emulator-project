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
 * Catalogs the real {@code bin_global/patchassets/assets/} folder (part 105) -- an entirely new
 * real top-level directory this project had never swept before, sitting OUTSIDE {@code
 * modassets/} entirely, alongside the install's {@code bin_global/}/{@code bin_ru/} client
 * binaries. 68 real files across 5 real top-level folders ({@code bundle} 10, {@code highpoly}
 * 2, {@code pda} 40, {@code stalcraft} 3, {@code stalker} 12, the last nested one level into a
 * real {@code slices/} subfolder) plus 1 real loose root file ({@code metadata}, reusing the same
 * {@code ROOT_KEY} deviation parts 91/98/99/100/104 established). Squarely game content (a live
 * incremental patch/delta layer EXBO ships on top of the base client), not an EXENS anti-cheat/
 * launcher asset, so stays in scope under rule 2.
 *
 * <p><b>Confirmed structurally identical, byte-different, to {@code bin_ru/patchassets/assets/}
 * </b> -- directly diffed: the same 68 relative file paths exist under both, but per-file sizes
 * and {@code md5sum}s differ throughout (spot-checked: {@code metadata}, {@code stalker/slices/
 * rostok.mcws}, {@code bundle/configs.xeon} all differ in both size and hash between the two).
 * These are two separate regional (Global vs. RU) client patch-delta builds shipping the exact
 * same real folder/file STRUCTURE with independently-compiled per-region contents -- this class
 * only loads {@code bin_global/}'s copy; {@code bin_ru/}'s is real but not separately catalogued,
 * since its structure is already fully represented here.
 *
 * <p><b>What this class deliberately does NOT do, per rule 5:</b> every real file here is an
 * opaque compiled/packed format this project has no reader for -- {@code .xeon} bundle packages
 * ({@code bundle/}, real magic bytes {@code "SCG1"} at offset 0, i.e. its own distinct packed
 * format, not the same as {@code classes.jar}'s), {@code .mcws} compiled level-slice overlays
 * ({@code stalker/slices/}), {@code .ol} compiled PDA map-tile overlays ({@code pda/map/}), {@code
 * .mcal} compiled animation packs ({@code highpoly/character/}), and {@code .ta}/{@code .texarr}
 * compiled texture arrays ({@code stalcraft/textures/}). {@code metadata} (13.5MB, the folder's
 * only loose root file) is likewise unreadable binary data with no discoverable structure --
 * disclosed for completeness, not decoded, the same "encrypted/packed format, don't touch"
 * boundary this project has always kept for {@code classes.jar} (rule 1/3). This class catalogs
 * real STRUCTURE (which files/folders exist, real file sizes) as a one-time snapshot -- exactly
 * like every other {@code world.props} catalog -- and deliberately never re-extracts this
 * PATCH-LAYER'S contents on the assumption that a later patch changing these exact bytes would
 * mean anything worth re-chasing (rule 5's "ignore live game rebalancing" applies with extra
 * force to a folder that is, by its own path, nothing but a patch delta).
 *
 * <p><b>Real value found here is entirely in the FILENAMES, not the binary contents:</b> the 12
 * real files under {@code stalker/slices/} ({@code bar_sever}, {@code covenant}, {@code csky},
 * {@code duty}, {@code foton2}, {@code freedom}, {@code garage}, {@code lodochnaya}, {@code
 * merc}, {@code rostok}, {@code savezone_agro}, {@code savezone_td}) are exactly 12 of {@link
 * net.stalzone.emulator.world.SettlementRegistry}'s 15 real confirmed settlement ids (part 28) --
 * i.e. this patch delta ships updated PRODUCTION level geometry for those 12 real settlements,
 * not internal test content. This directly upgrades part 93's own {@code StalkerMiscCatalog}
 * finding, which could only say its {@code testers_covenant}/{@code testers_duty}/{@code
 * testers_freedom}/{@code testers_merc}/{@code testers_rostok}/{@code testers_sever} internal
 * test maps "strongly suggest[ed]" real per-faction/per-location developer test levels existed
 * for real shipped zones -- this part's real {@code stalker/slices/} file list is now direct,
 * independent confirmation that those 6 (of these 12) really do correspond to real, currently
 * shipping production settlements, not just a naming coincidence. See {@link
 * net.stalzone.emulator.world.SettlementRegistry#realTitle} (also part 105) for each of these
 * 12 ids' real display title, and {@link net.stalzone.emulator.world.LocationNameRegistry} for
 * the separate, much larger real zone-name namespace this same part found.
 *
 * <p>{@code pda/map/} (40 real {@code r.&lt;x&gt;.&lt;y&gt;.ol} tile-override files) are real
 * incremental updates to part 64's {@link PdaMapCatalog} coordinate grid -- real coordinates,
 * not new zone names. {@code highpoly/character/animations/pack.mcal} and {@code .../
 * animationspov/pack.mcal} are real patch updates to part 62's {@code HighpolyCatalog} animation
 * packs. {@code stalcraft/textures/} (3 files) are real patch updates to part 71's {@code
 * StalcraftCatalog} block-texture arrays. {@code bundle/} (10 real {@code .xeon}/{@code .sign}
 * files -- {@code anim_casts}, {@code boxes}, {@code configs}, {@code effects}, {@code icons},
 * {@code materials}, {@code shaders}, {@code small_images}, {@code texmeta}, {@code textures})
 * are real patch updates to part 78's {@code BundleCatalog}. None of these four folders name any
 * new real content beyond what those four earlier parts already catalogued -- disclosed for
 * completeness, not investigated further.
 */
public final class PatchAssetsCatalog {

    /** Synthetic folder key for the 1 real loose file ({@code metadata}) at this folder's own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private PatchAssetsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param patchAssetsDir the real {@code .../bin_global/patchassets/assets} (or {@code bin_ru/...}) folder. */
    public static PatchAssetsCatalog load(Path patchAssetsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(patchAssetsDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    String folderName = entry.getFileName().toString();
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
            throw new IOException(patchAssetsDir + ": no patch asset folders/files found");
        }

        return new PatchAssetsCatalog(filesByFolder);
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

    /** Real top-level folder names, alphabetical, incl. {@link #ROOT_KEY} (6 total). */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /** Real file paths (relative to the folder itself) inside one real folder, or empty if unknown. */
    public List<String> filesIn(String folderName) {
        return Collections.unmodifiableList(filesByFolder.getOrDefault(folderName, List.of()));
    }

    /**
     * Real folder names that have at least one nested file whose name contains {@code substring}
     * (case-insensitive). Same generic evidentiary-verification helper as every prior catalog
     * class in this package.
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
