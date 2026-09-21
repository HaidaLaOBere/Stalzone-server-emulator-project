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
 * Catalogs the real assets under {@code modassets/assets/bundle/} (part 78) -- 9 files, of which 1 falls
 * under the single real top-level folder this catalog indexes ({@code levels}, 1 file) and 8 are loose
 * root-level files that the folder-only {@link #load} walk deliberately does not see, same as every
 * other catalog in this package. Squarely game content (config/data files, not an EXENS anti-cheat/
 * launcher asset), so stays in scope under rule 2.
 *
 * <p>4 of the 8 loose root files are not new discoveries -- they are the exact real source files
 * already loaded by dedicated classes elsewhere in this project: {@code item_groups.txt} ({@link
 * net.stalzone.emulator.items.ItemGroups}, part 23), {@code logged_trader_item_groups.txt} ({@link
 * net.stalzone.emulator.economy.TraderItemGroups}, part 51), and {@code sound_duration.csv}/{@code
 * speech_duration.csv} ({@link net.stalzone.emulator.audio.SoundDurations}, part 25) -- this catalog
 * confirms {@code bundle/} is the shared real directory all four of those classes' own Javadocs already
 * name as their source, rather than a separate, previously-unknown copy.
 *
 * <p>The other 4 loose files are genuinely new to this project. {@code item_groups_barter.txt} is a
 * real, completely EMPTY file (0 bytes/lines) -- an unused placeholder, not populated data. {@code
 * items.xeon} (352 bytes) and {@code render_configs.xeon}/{@code nvds.yolo} (16 bytes each) are a real,
 * previously-unseen binary {@code .xeon} config format plus one oddly-named {@code .yolo} file sharing
 * that same format -- all three are high-entropy/opaque binary data with no readable structure found by
 * direct inspection, and {@code render_configs.xeon} and {@code nvds.yolo} are byte-for-byte IDENTICAL
 * despite their different names, a real, unexplained duplication not investigated further (no attempt at
 * decryption/decoding was made, consistent with this project's wire-protocol-decryption gate -- these
 * are local files, not captured wire traffic, but the same evidence-only, no-speculation discipline
 * applies).
 *
 * <p><b>{@code levels/}</b> (1 file, {@code br_location.txt}) contains exactly the single word {@code
 * battleroyale} -- a real, minimal existence marker for that mode, distinct from and simpler than the
 * richer {@code br_*} asset trees found in parts 72 and 74.
 */
public final class BundleCatalog {

    private final Map<String, List<String>> filesByFolder;

    private BundleCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param bundleDir the real {@code .../modassets/assets/bundle} folder. */
    public static BundleCatalog load(Path bundleDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(bundleDir)) {
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
            throw new IOException(bundleDir + ": no bundle asset folders found");
        }

        return new BundleCatalog(filesByFolder);
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

    /** Real top-level bundle-asset folder names, alphabetical. */
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
