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
 * Catalogs the real assets under {@code modassets/assets/stalkermobs/} (part 69) -- 1,247 files
 * across 4 real top-level folders, no loose root files: {@code configs} (35), {@code models} (394),
 * {@code sound} (816), {@code textures} (2). Squarely game content (mutant models/sounds/AI configs),
 * not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code configs/}</b> (35 real {@code .smm} files) has the exact same real 35-file count as the
 * mutant AI config set this project's own {@code MobRegistry} (part 20) already loads from a different
 * path -- disclosed as consistent with, but NOT confirmed byte-identical to, that existing dataset; no
 * diff was attempted this part. The real base-mutant name list itself is unchanged from what {@code
 * MobRegistry} already knows (boar, chimera, cybergiant, dog/doge, drone, flesh, fpv, krovosos, lurker,
 * pseudodog, pseudogigant, psidog, puppet, rudolf, snork, spirit, thief_rat, tick, tsarevich/tsarevna,
 * tumbler, tushkan, wisp, several with a real {@code _clone} variant) -- what this part adds is the
 * FIRST catalog of the actual model/sound assets tied to those names, which {@code MobRegistry} never
 * loaded (it reads only the {@code .smm} config values).
 *
 * <p><b>{@code models/}</b> (394 files, 25 real per-mutant subfolders) turned up three real, striking
 * findings via direct content inspection, not just folder-name inference: {@code cybergiant/} (9 files)
 * has real filenames literally reading {@code psevdogigant_boss_att_diff.ol}/{@code
 * psevdogigant_boss_body_diff.ol} -- direct, first-hand confirmation that "cybergiant" is specifically a
 * BOSS-tier reskin of the existing "pseudogigant" mutant, not a separate creature design. {@code
 * tsarevich/} (16 files) has a real {@code crown_Diffuse.ol}/{@code crown_Normal.ol}/{@code
 * crown_Specular.ol} texture set alongside a real {@code frost_boar_diff.ol} -- confirming "tsarevich"
 * ("Tsar's son", Russian) is a crowned, winter-themed boar mutant (its real sibling {@code tsarevna/},
 * 17 files, "Tsar's daughter", was not individually inspected but is presumably the same royal-boar
 * theme). {@code rudolf/} (13 files) has real {@code Rudolf_Angry}/{@code Rudolf_Base}/{@code
 * Rudolf_Medium} Diffuse/Emissive/Normal/Specular texture tiers -- a real seasonal (Christmas-reindeer-
 * themed) mutant with distinct aggression-state texture variants, joining this project's existing
 * seasonal-event roster (Halloween, New Year, winter) with a specifically Christmas-flavored creature.
 * {@code doge/} (7 files) has real {@code act_skeleton}/{@code act_skeleton_fresh}/{@code
 * act_skeleton_tox}/{@code act_skeleton_tox_emiss} texture variants -- a skeletal, toxin-themed dog
 * mutant distinct from the base {@code dog/} model, disclosed as a striking real NAME (identical to the
 * well-known internet meme) but with no evidence connecting the two beyond the name itself.
 *
 * <p><b>{@code sound/}</b> (816 files, 31 real subfolders) roughly mirrors the model roster with real
 * per-mutant attack/idle/death sound sets, plus a real {@code inside_*} variant for 10 of those mutants
 * (a distinct, quieter/muffled sound set, plausibly for a body-cam or up-close/inside-anomaly
 * perspective, not confirmed). One real subfolder here has NO matching {@code models/} entry: {@code
 * jellyfish/} (3 files) -- a fresh, unconfirmed mutant-name lead with sound data but no model asset
 * found in this tree (its model may live elsewhere, not searched this part).
 *
 * <p><b>{@code textures/}</b> (2 files, both under a real {@code particles/} subfolder) -- {@code
 * distortion.mic}, {@code funnel_eye.mic} -- are small, generic mutant-ability VFX textures, not
 * individually cross-referenced against any specific mutant this part.
 */
public final class StalkerMobsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerMobsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerMobsDir the real {@code .../modassets/assets/stalkermobs} folder. */
    public static StalkerMobsCatalog load(Path stalkerMobsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalkerMobsDir)) {
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
            throw new IOException(stalkerMobsDir + ": no stalkermobs asset folders found");
        }

        return new StalkerMobsCatalog(filesByFolder);
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

    /** Real top-level stalkermobs-asset folder names, alphabetical. */
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
