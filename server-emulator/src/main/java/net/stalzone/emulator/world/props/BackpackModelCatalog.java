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
 * Catalogs the real backpack/container model assets under
 * {@code modassets/assets/stalker/models/backpacks/} (part 56) -- 65 real top-level FOLDERS on
 * this install, confirmed by direct directory listing, holding 385 real files between them (154
 * {@code .mcsb} meshes and 231 {@code .ol} diffuse/normal/specular/emissive textures -- only two
 * extensions, and unlike {@link ArtefactModelCatalog}'s part-55 find, ZERO loose files sit outside
 * the 65 named subfolders, so this class's {@link #totalFileCount()} needs no disclosed gap: the
 * real {@code find}-confirmed root total (385) and the walked total agree exactly. Same generic,
 * no-hardcoding design as {@link ArmorModelCatalog}/{@link ArtefactModelCatalog}:
 * {@link #foldersContaining(String)} is a reusable substring search, so narrative claims below are
 * checked programmatically, not hand-counted.
 *
 * <p><b>Two real naming families, splitting the 65 folders cleanly</b>: 30 folders are
 * {@code bag_}-prefixed (backbomb, dmz26, errand, flamethrower, march, mbss, mbss_old, npa,
 * npa_old, placeholders, router, sack, sports, sports_quest, trader, trader1-11, transformer,
 * trizip, tv, zivcas) and 33 are {@code cont_}-prefixed ("container" -- the bulkier, non-worn
 * storage props: apiary, bale, barrel, bathyscaphe, bear, bidon, cocoon, cold, dubak, faraday,
 * fridge, getter, hellboy, hive, honeycomb, iu2, kega, kolotun, krab5, krab6, ksm, kzs1-5, murmur,
 * overtone, pathfinder, pouch, psychocell, tasche, zivcas), plus 2 folders outside either family:
 * {@code clan/} and {@code corpse_bags/}. 30 + 33 + 2 = 65, verified programmatically against the
 * live real folder list via {@link #totalFolderCount()} and a prefix count -- an initial hand-count
 * of this same breakdown (26/37) undercounted both families and omitted {@code cont_cocoon}/
 * {@code cont_kolotun} entirely; this class doc reports the corrected, tool-verified figures.
 *
 * <p><b>A real structural convention not seen in the armor or artefact sweeps</b>: most meshes in
 * this tree ship in TWO forms -- a worn/carried mesh (e.g. {@code kega.mcsb}) and a separate
 * {@code _ground} mesh (e.g. {@code kega_ground.mcsb}, the model as it appears dropped in the
 * world) -- confirmed real and widespread via {@link #foldersContaining(String)
 * foldersContaining("_ground")} rather than assumed from a handful of examples. Textures are
 * NOT duplicated per state; only the mesh has a dedicated ground variant.
 *
 * <p><b>A real, reusable {@code style_<season>} subfolder convention, distinct from part 55's
 * per-artefact {@code n_art} folders</b>: {@code cont_bear/style_autumn25/}, {@code
 * cont_hive/style_autumn25/}, and {@code cont_pathfinder/style_autumn26/} each hold a dedicated
 * seasonal texture/mesh variant of their parent container. {@code style_autumn26} is real, on-disk,
 * literally year-suffixed evidence of Autumn '26 content -- the first "26"-dated SEASON name this
 * project has found anywhere (part 55's dated folders topped out at {@code ny26}, New Year '26);
 * whether it has shipped or is upcoming relative to any specific date is not evidenced by the file
 * alone. {@code bag_dmz26/} is a second, independent real 2026-dated find in this same sweep -- a
 * DMZ-mode bag with {@code bake} and {@code frost} finish variants (each its own diff/emi/nrm) plus
 * a crossover texture pair, {@code bag_trizip_dmz26_diff.ol}/{@code _nrm.ol}, tying it to the
 * separately-cataloged {@code bag_trizip/} folder.
 *
 * <p><b>Real "kept the old version" content, in THREE distinct forms found in this one tree</b>:
 * sibling {@code _old} folders ({@code bag_npa_old/} -- identical file set to {@code bag_npa/} plus
 * one extra {@code npa_spek.ol} the current folder lacks; {@code bag_mbss_old/} -- a byte-for-byte
 * identical filename set to {@code bag_mbss/}, no content difference detectable from names alone);
 * and, new to this project, nested {@code old/} SUBFOLDERS inside an otherwise-current folder
 * ({@code cont_kzs1/old/} and {@code cont_bidon/old/}, each holding a prior texture revision of
 * that container, e.g. {@code cont_bidon/old/kzs3_diff.ol} sitting alongside the real, separately
 * cataloged {@code cont_kzs3/} folder's own current textures). Three different real retention
 * conventions for old assets, none previously seen together in one folder before this sweep.
 *
 * <p><b>Real cross-references to prior parts, extending established findings into equipment</b>:
 * {@code bag_flamethrower/} carries {@code flamethrower_reich_diff.ol} and {@code
 * flamethrower_rkka_diff.ol} -- real evidence tying part 54's Reichstag/RKKA WWII-event armor
 * roster to a matching backpack skin, structurally an outlier itself (no plain diffuse, no
 * {@code _ground} mesh -- only the two WWII-faction variants exist). {@code cont_zivcas/} and
 * {@code cont_murmur/} extend the established Zivcas (parts 34/53/54) and Murmur (parts 36/54)
 * faction cross-references from armor into containers.
 *
 * <p><b>Other real, disclosed-as-unconfirmed finds</b>: a same-tree bee-themed cluster --
 * {@code cont_apiary/}, {@code cont_hive/}, {@code cont_honeycomb/} -- three independently-named
 * real folders on one insect/beekeeping theme, unconfirmed as a single set (no shared string ties
 * them together beyond the theme itself). {@code cont_hellboy/hellboydonate_diff.ol} is a real,
 * literal "donate" naming -- the first evidence in this project of a monetization/donation-tied
 * cosmetic skin variant, distinct from every other found reward/seasonal system. {@code bag_tv/}
 * holds only {@code tv.mcsb} -- the sole folder in this tree with a mesh and NO texture file of any
 * kind, a real disclosed gap. {@code bag_sports/} and {@code bag_sports_quest/} are two differently
 * NAMED folders whose internal file names are identical ({@code sports.mcsb}, {@code
 * sports_diff.ol}, ...) -- the same real duplication pattern as {@code bag_mbss_old}, here read as
 * a quest-specific reuse of one asset rather than an old/new revision. {@code bag_trader/} (one
 * shared mesh, 10 real numbered diffuse skins {@code trader0_diff.ol}-{@code trader9_diff.ol}) sits
 * alongside 11 SEPARATE, fully-independent {@code bag_trader1/} through {@code bag_trader11/}
 * folders (each its own dedicated mesh+texture set) -- a real naming duplication across two
 * different "trader" bag systems, the same class of oddity as part 55's {@code karusel/art_ostov}
 * vs {@code ostov}, unknown if related in-game. {@code clan/} holds {@code clan_supply.mcsb} plus
 * {@code clan_tent_medic}/{@code clan_tent_supply} (each a full diff/nrm/spek set) -- real evidence
 * of clan camp/tent props with medic and supply roles, corroborating the clans system already
 * inferred from keybindings and prefs data (parts 42/45). {@code corpse_bags/} holds real
 * {@code big}/{@code medium}/{@code small} size-variant meshes with no textures of their own,
 * plausibly a death/loot-drop bag prop, unconfirmed by any log. A recurring {@code u}/{@code d}
 * suffix letter appears across several container names ({@code bidonu}, {@code kzs1u}/{@code
 * kzs1d}, {@code kzs3u}, {@code kzs4d}, {@code kzs5d}, {@code honeycombu}, {@code cocoonu},
 * {@code bear4d}/{@code bear5u}/{@code bear6d}, {@code kegaue}), read here as a plausible camo or
 * finish variant marker (e.g. urban/digital), NOT confirmed by any file or log this project has
 * found -- the same hedge level as part 53's barter-icon zone-code reading before corroboration.
 */
public final class BackpackModelCatalog {

    private final Map<String, List<String>> filesByFolder;

    private BackpackModelCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param backpacksDir the real {@code .../stalker/models/backpacks} folder. */
    public static BackpackModelCatalog load(Path backpacksDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(backpacksDir)) {
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
            throw new IOException(backpacksDir + ": no backpack model folders found");
        }

        return new BackpackModelCatalog(filesByFolder);
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

    /** Real top-level backpack/container model folder names, alphabetical. */
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
