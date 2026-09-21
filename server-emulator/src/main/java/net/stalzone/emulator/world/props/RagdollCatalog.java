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
 * Catalogs the real assets under {@code modassets/assets/ragdolls/} (part 76) -- 41 files under a
 * single real top-level folder, {@code models} (41; no other top-level folders exist, so {@code models}
 * itself is the only entry indexed by {@link #load}, matching this class's own {@code load} pattern).
 * Squarely game content (real per-mutant death-physics props and a corpse/body-bag looting system), not
 * an EXENS anti-cheat/launcher asset, so stays in scope under rule 2. Small enough to fixture in full.
 *
 * <p><b>{@code models/}</b> holds this project's richest single real mutant/NPC-death roster: 27 real
 * named subfolders, each with exactly one {@code ragdoll.mcsb} file -- {@code boar}, {@code cat}, {@code
 * chimera}, {@code cybergiant} (part 69's confirmed pseudogigant-boss reskin), {@code dog}, {@code doge}
 * (part 69), {@code drone}, {@code flesh}, {@code fpv} ("first-person view", plausibly the player's own
 * death-ragdoll model), {@code krovosos} ("bloodsucker", Russian -- a real, canonical S.T.A.L.K.E.R.-
 * series mutant name), {@code lurker}, {@code pseudodog}, {@code pseudogigant}, {@code psidog}/{@code
 * psidog_clone}, {@code puppet}, {@code rudolf} (part 69), {@code snork} (a real, canonical
 * S.T.A.L.K.E.R.-series mutant name), {@code spirit}, {@code steve} (this project's third appearance of
 * "steve" -- after part 63's collider self-correction into a cut/prototype-boss lead -- and the
 * strongest evidence yet that it is/was a functional, killable entity with its own death physics, not
 * just a leftover collider), {@code thief_rat}, {@code tick}, {@code tsarevich} (part 69's crowned
 * winter boar), a fresh {@code tsarevna} ("princess", Russian -- an unconfirmed female counterpart to
 * {@code tsarevich}) lead, {@code tumbler}, {@code tushkan} (a real, canonical S.T.A.L.K.E.R.-series
 * mutant name -- a "flying rat"-type creature), and {@code wisp}. Also has {@code tex/} (10 files: 9
 * numbered {@code corpse_bag_1.ol}-{@code corpse_bag_9.ol} texture variants plus a shared normal map) and
 * 4 loose files ({@code corpse_bag.mcsb}/{@code corpse_bag.mcvd}, {@code corpse_pile.mcsb}/{@code
 * corpse_pile.ol}) -- a real corpse/body-bag looting-and-extraction prop set.
 */
public final class RagdollCatalog {

    private final Map<String, List<String>> filesByFolder;

    private RagdollCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param ragdollsDir the real {@code .../modassets/assets/ragdolls} folder. */
    public static RagdollCatalog load(Path ragdollsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(ragdollsDir)) {
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
            throw new IOException(ragdollsDir + ": no ragdolls asset folders found");
        }

        return new RagdollCatalog(filesByFolder);
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

    /** Real top-level ragdolls-asset folder names, alphabetical. */
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
