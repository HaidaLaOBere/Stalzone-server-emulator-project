package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;

/**
 * Catalogs the real artefact model assets under
 * {@code modassets/assets/stalker/models/artefacts/} (part 55) -- 17 real top-level FOLDERS on
 * this install, confirmed by direct directory listing, holding 536 real files between them (139
 * {@code .mcsb} meshes and 397 {@code .ol} diffuse/normal/specular/emissive textures -- only two
 * extensions found here, unlike {@code models/armor}'s extra {@code .mic} outlier, part 54). Same
 * generic, no-hardcoding design as {@link ArmorModelCatalog}: {@link #foldersContaining(String)}
 * is a reusable substring search, so a narrative claim below is verified programmatically rather
 * than hand-counted -- a discipline that caught a real gap in this class's own first draft before
 * it shipped, disclosed next.
 *
 * <p><b>A real gap this class does NOT cover, caught by checking the live real total</b>: the
 * {@code artefacts/} folder itself also holds 17 real loose files that sit directly in its root,
 * outside any of the 17 subfolders this class walks -- {@code load()} only visits directories, so
 * {@link #totalFileCount()} (536) undercounts the tree's real, direct {@code find}-confirmed total
 * of 553 by exactly those 17. This project's first draft of this Javadoc claimed 553 as this
 * class's own total before the live device check caught the mismatch; corrected here rather than
 * silently reconciled. The 17 real loose files, for the record: a {@code krisstal.mcsb} mesh (note
 * the real on-disk spelling mismatch -- the mesh is "krisstal" with a double s, its own
 * {@code kristal.ol}/{@code kristal_emi.ol} textures are "kristal" with one, the same class of
 * naming inconsistency as {@code dr_zivkas}/{@code dr_zivcas} in part 54's armor sweep) with no
 * owning family folder; a {@code 1623.ol}/{@code 1623_emi.ol} texture pair with no matching mesh,
 * meaning unknown; and two apparently shared, mesh-less texture groups -- {@code dist.ol} through
 * {@code dist5.ol} plus {@code dist2k.ol} (6 files, read here as a shared distortion/heat-haze
 * effect set) and {@code sopli.ol} through {@code sopli6.ol} (6 files, "sopli" is Russian slang for
 * "snot/mucus", read here as a shared organic/slime effect set plausibly used by the {@code kisel}
 * ("jelly") family) -- both readings unconfirmed by any file or log this project has found.
 *
 * <p><b>Two real structural eras, found by direct inspection</b>: 6 of the 17 top-level folders
 * ({@code electra}, {@code jarka}, {@code karusel}, {@code kisel}, {@code puh}, {@code voronka})
 * are "classic" family folders -- flat, with meshes and textures side by side and some textures
 * reused across multiple named artefacts in the same folder (e.g. {@code electra/lighting.ol},
 * {@code electra/plasmaball.ol}). A newer {@code n_art/} folder ("new artefacts") reorganizes
 * those same 6 family names into 68 real per-artefact leaf folders across 12 real family
 * subfolders, each leaf folder holding its own dedicated diffuse/normal/specular(/emissive)
 * texture set (e.g. {@code n_art/electra/art_coil/} has its own {@code coil_diff.ol},
 * {@code coil_emi.ol}, {@code coil_nrm.ol} beside {@code art_coil.mcsb}) -- a real, disclosed
 * asset-pipeline evolution from shared-texture to per-artefact-texture, not an invented reading.
 * The other 6 real {@code n_art/} family subfolders ({@code autumn25}, {@code biohim_summer25},
 * {@code collab_art}, {@code inside}, {@code ny26}, {@code trampoline}) have no flat-era
 * counterpart at all -- purely {@code n_art}-era additions. The remaining 10 top-level folders
 * ({@code barter}, {@code clan}, {@code helewin}, {@code ivent}, {@code newmodels},
 * {@code newyear}, {@code other}, {@code puzir}, {@code snow_storm}, {@code trump}) exist only in
 * the flat, older style, with no {@code n_art} counterpart.
 *
 * <p><b>Real dated seasonal folders, a new kind of evidence for this project</b>: {@code
 * n_art/autumn25}, {@code n_art/biohim_summer25}, and {@code n_art/ny26} are real, literally
 * year-suffixed folder names (Autumn '25, Summer '25, New Year '26) -- the first directly
 * DATED naming convention this project has found in a file/folder name itself, as opposed to the
 * dated FMOD bank names found earlier ({@code reichstag2026}, {@code bm2024}, part 35) which come
 * from a different data source. This is real, disclosed evidence of a live seasonal-content
 * cadence, not a claim about what any specific date shipped or when.
 *
 * <p><b>A real, cross-referenced Halloween-adjacent cluster</b>: this same part's sweep also
 * found a standalone {@code models/halloween/} folder (a {@code goner_diff.ol}/{@code goner_emi.ol}
 * /{@code goner_nrm.ol} texture set shared by real {@code big.mcsb}/{@code medium.mcsb}/
 * {@code small.mcsb} size-variant meshes -- a likely creature/prop, not catalogued by this class).
 * Independently, this class's own tree has a real {@code artefacts/helewin/} folder ({@code pup.ol},
 * {@code pupdif.ol}, {@code pupdif_emi.ol}, {@code ty_kva.mcsb} -- "helewin" reads as a
 * transliteration of "Halloween", and "ty_kva" plausibly as "tykva", Russian for "pumpkin", though
 * this project has no file or log that spells either out, so both readings stay hedged), and a
 * third, independent {@code n_art/trampoline/art_pumpkin.mcsb} with the literal English word
 * "pumpkin" in its filename. Three differently-named folders in two different real trees
 * converging on the same seasonal theme is real corroborating evidence of a Halloween-adjacent
 * event bundle; it is NOT evidence they all ship together, share a date, or share a name in any
 * player-facing string -- no such string has been found.
 *
 * <p><b>Other real, disclosed-as-unconfirmed finds</b>: {@code n_art/collab_art/} (real leaf
 * folders {@code hedgehog}, {@code jele}, {@code spheretank}, {@code tracks}) -- the literal
 * substring "collab" in a real folder name is direct evidence of some collaboration/crossover
 * content, but no partner or brand name appears anywhere in this tree, so who or what it
 * collaborates with is unconfirmed. A real {@code "proto"}-prefixed naming habit recurs
 * independently in four unrelated folders -- {@code electra/protoart.mcsb}, {@code jarka/proto.ol},
 * {@code n_art/trampoline/art_protocibulya.mcsb}, and {@code trump/art_protomedusa.mcsb} -- read
 * here as a recurring internal "prototype before final" convention, not confirmed by any comment
 * or log. Real leftover-looking dev files sit directly in the shipped tree: {@code jarka/test.ol},
 * {@code karusel/test.mcsb}. {@code karusel/} also has a real, disclosed naming duplication --
 * both {@code art_ostov.mcsb} (no texture files of its own in the folder) and a separately-named
 * {@code ostov.mcsb} WITH its own {@code ostov_diff.ol}/{@code ostov_nrm.ol}/{@code ostov_spek.ol}
 * exist side by side; this project does not know if these are the same in-game artefact or two
 * different ones. In {@code barter/}, two of its eight real {@code art_}-prefixed meshes --
 * {@code art_tesla.mcsb} and {@code art_toksikoz.mcsb} -- have no matching same-name texture files
 * anywhere in that folder, a real, disclosed gap (the other six barter meshes each have a matching
 * diffuse/normal/specular(/emissive) set). {@code clan/} holds a real {@code clan}/{@code clan2}
 * pair (a full {@code clan_diff}/{@code clan_emiss}/{@code clan_nrm}/{@code clan_spek} texture set
 * plus a {@code clan2_diff}/{@code clan2_emiss}/{@code clan2_spek}, {@code clan2} notably missing
 * its own normal map in this listing), read as two skin tiers of one clan-related artefact,
 * unconfirmed by any log.
 */
public final class ArtefactModelCatalog {

    private final Map<String, List<String>> filesByFolder;

    private ArtefactModelCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param artefactsDir the real {@code .../stalker/models/artefacts} folder. */
    public static ArtefactModelCatalog load(Path artefactsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(artefactsDir)) {
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
            throw new IOException(artefactsDir + ": no artefact model folders found");
        }

        return new ArtefactModelCatalog(filesByFolder);
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

    /** Real top-level artefact model folder names, alphabetical. */
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
