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
 * Catalogs the real assets under {@code modassets/assets/auction/} (part 77) -- 19 files across 2 real
 * top-level folders, no loose root files: {@code fonts} (14), {@code textures} (5). Squarely game
 * content (the auction-house UI's fonts and icon atlases), not an EXENS anti-cheat/launcher asset, so
 * stays in scope under rule 2. Small enough to fixture in full.
 *
 * <p><b>{@code fonts/}</b> (14 files) is a real, self-contained UI font set: {@code Capture_it.ttf}
 * (a stylized display font, plausibly for highlighted prices/bids) with its own {@code CAPTURE_IT__12/
 * 14/16.fmi} rasterized-size metrics; a real Tahoma set ({@code Tahoma__11.fmi}, {@code
 * Tahoma_bold_11/14.fmi} -- a genuine Microsoft system font used as a UI fallback); and real {@code
 * GOST_A}/{@code GOST_B} fonts ({@code GOST_A.ttf}, {@code GOST_B.TTF}, plus {@code .fmi} metrics at
 * several sizes/weights) -- GOST ("Государственный стандарт", "State Standard") is a real Russian/former-
 * Soviet standards body, and GOST-family fonts are a genuine, formal piece of evidence for this
 * project's Russian development origin, distinct from (and stronger than) the informal Russian words
 * found in filenames elsewhere in this project. {@code dictionary.txt} is a real font glyph-coverage
 * list (not a word dictionary) -- the full supported character set: punctuation, digits, and the
 * complete Cyrillic and Latin alphabets in both cases, confirming these UI fonts render both languages.
 *
 * <p><b>{@code textures/gui/}</b> (5 files) has the expected {@code auction_list.mic}/{@code
 * auction_list_bg.mic}/{@code icons.mic} auction-house UI atlases, plus a real, fresh finding: {@code
 * mail_list.mic}/{@code mail_msg.mic} -- in-game MAIL-system UI assets bundled under the same namespace
 * as the auction house, plausibly because auction results/notifications are delivered via in-game mail.
 */
public final class AuctionCatalog {

    private final Map<String, List<String>> filesByFolder;

    private AuctionCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param auctionDir the real {@code .../modassets/assets/auction} folder. */
    public static AuctionCatalog load(Path auctionDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(auctionDir)) {
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
            throw new IOException(auctionDir + ": no auction asset folders found");
        }

        return new AuctionCatalog(filesByFolder);
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

    /** Real top-level auction-asset folder names, alphabetical. */
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
