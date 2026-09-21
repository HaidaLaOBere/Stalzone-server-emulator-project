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
 * Catalogs the real assets under {@code modassets/assets/anomalies/} (part 71) -- 263 files across 4
 * real top-level folders ({@code models} 66, {@code sound} 173, {@code textures} 22, {@code trace} 1)
 * plus 1 real loose root file ({@code morse_alphabet.txt}). Squarely game content (anomaly-hazard
 * models/sounds/VFX), not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2. This part
 * gives this project's existing {@code DetectorConfig} (part 22, 16 real rad/thr/bio hazard TIERS) its
 * first real NAMED anomaly-type roster from an independent asset tree.
 *
 * <p><b>{@code morse_alphabet.txt}</b> (81 real lines) has the same near-identical real line count as
 * the {@code morse_alphabet.txt} this project's own {@code MorseAlphabet} (part 24, 82 real entries)
 * already loads from a different path -- disclosed as consistent with, but NOT confirmed
 * byte-identical to, that dataset; the 1-line difference was not investigated. {@code sound/morse/} (2
 * files, {@code 0_.ogg}/{@code 1_.ogg}) is a small, real morse-tone audio sample set, distinct from the
 * text table itself.
 *
 * <p><b>{@code sound/}</b> (173 files) names a real, rich anomaly-type roster via its own real
 * subfolder-less loose filenames -- {@code blackhole}, {@code carousel} (incl. a real {@code
 * carousel_bolt.ogg} bolt-throw cue), {@code circusidle} (cross-referencing part 63's real {@code
 * gloomycore/handbook/circus.mic}), {@code coach}, {@code electra}/{@code protoelectra} (a real
 * "proto" variant, an unconfirmed weaker/precursor anomaly-tier lead), {@code funnel} (incl. a real
 * {@code funnel_bolt.ogg}, {@code funnel_node_active.ogg}), {@code icecrack}, {@code kissel}/{@code
 * kisel} (both spellings appear, "kissel" = a real Russian fruit-jelly drink, a well-known real
 * S.T.A.L.K.E.R.-series anomaly name), {@code mirror}, {@code stasis}/{@code statis} (both spellings
 * appear), {@code steam}, {@code tesla}, {@code trampoline}, and {@code warmwaveidle}. This is a
 * genuine, first-time NAMED corroboration of the anomaly-type roster underlying {@code
 * DetectorConfig}'s existing rad/thr/bio hazard-tier data, which never named specific anomaly types.
 * Real subfolders add further detail: {@code resources/} (23 files) is dominated by a real {@code
 * chronodevice_*} sound set ({@code idle}/{@code loading}/{@code loading_end}/{@code mining}/{@code
 * start}/{@code stop}/{@code destruction}) -- a fresh, unconfirmed named artefact-extraction-device
 * lead, plausibly tied to this folder's sibling real {@code models/extractor/} (23 files, not
 * individually inventoried this part). {@code traders/} (5 files, real {@code monolit.ogg} [cross-
 * referencing part 68's real "Monolit"/Monolith faction], {@code trader_v2_}/{@code v3_}/{@code
 * v4_.ogg}, {@code zombie.ogg}) is a real distorted-radio-chatter ambience set, plausibly heard near
 * anomaly fields. {@code random_radio/} (30 files, real {@code random_2} through {@code random_19.ogg}
 * and more) is a real generic ambient radio-static set. {@code scanner/} (4) and {@code
 * signal_noise/} (3) are small real detector/signal-adjacent sets, not individually inventoried.
 *
 * <p><b>{@code models/}</b> (66 files: 4 real loose files -- {@code bolt.mcsb}/{@code bolt_thrown.mcsb}
 * [the real thrown-bolt anomaly-detection prop already implied by {@code DetectorConfig}'s existence],
 * {@code box.mcsb}, {@code empty.mcsb} [a real, self-disclosed placeholder] -- plus 6 real subfolders)
 * includes {@code extractor/} (23 files, the largest real subfolder here, cross-referencing {@code
 * sound/resources/}'s chronodevice set above), {@code node/} (10), {@code kisel/} (9), {@code
 * dist_meshes/} (9, real distortion-effect meshes), {@code zastoy/} (6, "stagnation", Russian -- a
 * real, distinct anomaly-name lead not seen among the {@code sound/} names above, with its own real
 * {@code trace/zastoy.mcvd} decal), and {@code signal_point/} (2, the smallest real subfolder here).
 *
 * <p><b>{@code textures/}</b> (22 files, all loose, no subfolders) is small anomaly-VFX texture data
 * (real {@code distortion.mic}, {@code light_beam}/{@code light_beam1}/{@code light_beam6.mic}, {@code
 * scanners_b}/{@code scanners_b_bg}/{@code scanners_b_scan}/{@code scanners_b_scan_anim.mic}), not
 * individually cross-referenced against a specific anomaly type this part.
 */
public final class AnomalyCatalog {

    private final Map<String, List<String>> filesByFolder;

    private AnomalyCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param anomaliesDir the real {@code .../modassets/assets/anomalies} folder. */
    public static AnomalyCatalog load(Path anomaliesDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(anomaliesDir)) {
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
            throw new IOException(anomaliesDir + ": no anomaly asset folders found");
        }

        return new AnomalyCatalog(filesByFolder);
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

    /** Real top-level anomaly-asset folder names, alphabetical. */
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
