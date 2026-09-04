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
 * Catalogs the real armor/character model assets under
 * {@code modassets/assets/stalker/models/armor/} (part 54) -- 192 real top-level folders on this
 * install, confirmed by direct directory listing, holding 2,240 real files total (mostly
 * {@code .mcsb} meshes and {@code .ol} diffuse/normal/emissive textures, matching the mesh/texture
 * format this project already identified in part 15).
 *
 * <p>This class deliberately does NOT hardcode any narrative interpretation of a folder or
 * filename -- {@link #foldersContaining(String)} is a generic, reusable substring search over
 * every real nested filename, so a specific claim (e.g. "9 armor bases have a real 'sanitar'
 * variant") is verified programmatically against the real data rather than hand-counted, which is
 * exactly the class of transcription error this project caught and disclosed in part 53. The
 * narrative reads below are this project's own interpretation of what {@link #foldersContaining}
 * turns up, kept in the Javadoc rather than the code.
 *
 * <p><b>Real cross-references found by direct inspection, part 54</b>: an {@code armor/twins/}
 * folder holds real {@code left_twin.mcsb}/{@code right_twin.mcsb} body meshes, a real
 * {@code left_twin_body}/{@code right_twin_body} diffuse/normal texture pair for each twin, plus a
 * shared {@code twins_heads} diffuse/normal texture pair -- the first real character-model evidence
 * for the "Twins" (bliznetsi) boss encounter this project only had FMOD bank names for before (5
 * dedicated banks, part 35). An {@code armor/nina/} folder (a real {@code nina.mcsb} mesh, a real
 * {@code nina_diff}/{@code nina_nrm} body texture pair, and a separate real {@code face_diff.ol})
 * corroborates part 34's {@code nina_2}-{@code nina_6} subtitle files with an actual in-game model,
 * not just dialogue text. An {@code armor/reichstag/} folder holds TWO real, symmetric unit rosters
 * under distinct prefixes, each with 9 real {@code .mcsb} meshes (corrected here from an earlier
 * hand-counted "6-8 role" draft) -- {@code reich_} ({@code capitan}, {@code corporal},
 * {@code engineer}, {@code gunner}, {@code hands_1}, {@code hands_2}, {@code hands_sniper},
 * {@code sniper}, {@code stormtrooper}) and {@code rkka_} (the real historical Soviet Red Army
 * acronym, Raboche-Krestyanskaya Krasnaya Armiya, the identical 9 roles), each also with a real
 * {@code _1}/{@code _2} diffuse/normal texture pair -- corroborating the {@code reichstag2026} FMOD
 * bank (part 35) and the real {@code hasSeenReichstagHowToPlay} save flag (part 47) with actual
 * opposing-army character models. 8 real {@code murmur_*} folders ({@code bigboy}, {@code dozer},
 * {@code electra}, {@code grenadier}, {@code medic}, {@code ratcatcher}, {@code sniper},
 * {@code technar}) plus a plain {@code murmur} base give the first real in-game armor/model
 * evidence for EXBO's public "Monolit -&gt; Murmur" faction rename (part 36, previously only
 * evidenced by an external patch-notes article, not a STALZONE file). A plain {@code zivcas}
 * folder plus {@code zivcas_commander}/{@code zivcas_fighter}/{@code zivcas_heavy} (and a separate
 * {@code dr_zivcas}, disclosed below) extend part 34's "Zivcas special forces" faction name with a
 * real 4-role armor roster. A real {@code zombie_bm} folder is considerably larger than an initial
 * pass suggested -- corrected here after re-checking the real listing directly: it holds not just
 * {@code ao4}/{@code ao5}/{@code ao6} gas-mask zombie variants but 8 distinct zombie character
 * lines total ({@code ao4}, {@code ao5}, {@code ao6} (itself in two mesh variants), {@code
 * centurion}, {@code ghost} (two mesh variants), {@code goplit}, {@code legion}, {@code mule} (six
 * numbered mesh variants, {@code zombie_mule_1} through {@code zombie_mule_6})), plus a shared
 * {@code zombie_ao_head} and {@code zombie_pvp_head} texture pair. This plausibly, but still NOT
 * confirmed, ties to the {@code bm2024} seasonal-event FMOD bank (part 35) via the shared "bm"
 * token -- no file or log line in this project directly equates the two, so this stays a hedged
 * reading, same standard as the sanitar/Orderlies hypothesis below.
 *
 * <p><b>The real "sanitar" filename-suffix variant (part 53's "8+ armor bases", now an exact,
 * programmatically-verified 9)</b>: {@code bover}, {@code kz3b}, {@code mule}, {@code saturn},
 * {@code seva}, {@code skat10}, {@code smog_old}, {@code voshod_old}, {@code zarya} -- {@code seva}
 * is newly confirmed here (part 53's hand-assembled list of "8+" did not name it explicitly).
 * Structurally inconsistent, disclosed rather than papered over: most bases only add a
 * {@code &lt;base&gt;_sanitar_diff.ol}/{@code _nrm.ol} texture pair over the existing mesh;
 * {@code kz3b} additionally has its own {@code kz3b_sanitar.mcsb} mesh; {@code skat10}'s variant
 * sits inside its own {@code motif_sanitar/} subfolder rather than as a same-level filename
 * suffix; {@code zarya} is the richest of the nine -- it has BOTH a same-level
 * {@code zarya_sanitar_diff.ol}/{@code _nrm.ol} texture pair AND its own {@code zarya/sanitar/}
 * subfolder holding four distinct dedicated meshes ({@code zarya_sanitar}, {@code
 * zarya_balon_sanitar}, {@code zarya_gas_sanitar}, {@code turist_sanitar}) plus its own texture
 * pair, corrected here from an earlier draft that only credited it with the same-level pair. This
 * still only corroborates, not confirms, part 36's hedged "sanitar" = EXBO's public "Orderlies"
 * faction reading -- no direct file/log equation exists.
 *
 * <p><b>Other real, disclosed-as-unconfirmed finds</b>: {@code armor/zarya/} contains
 * {@code lipa_mask.mcsb}/{@code lipa_mask_diff.ol}/{@code lipa_mask_nrm.ol} -- a real, literal mask
 * model tied to the "Lipa" tutorial character (parts 32/34/35), a cross-reference this project had
 * not made before -- plus a separate {@code lipa_diff.ol}/{@code lipa_nrm.ol} texture pair without
 * the "_mask" suffix, an additional real file this project had not previously catalogued. The same
 * folder also has an {@code outlaw_diff.ol}/{@code outlaw_nrm.ol} texture pair -- a real named skin
 * variant new to this project, unconfirmed meaning. A real {@code dr_zivcas}/{@code dr_tank} pair
 * (both with a matching {@code _hw22_diff.ol}/{@code _hw22_emi.ol}/{@code _hw22_nrm.ol} texture
 * variant) share a {@code dr_} prefix plausibly reading as "doctor"/medic, not confirmed by any
 * file or log this project has found. {@code dr_zivcas} additionally carries three real
 * {@code .mic}-extension files ({@code dr_zivkas_hw22_diff.mic}, {@code
 * dr_zivkas_hw22_emiss.mic}, {@code dr_zivkas_hw22_normal.mic}) -- note the real spelling
 * inconsistency in these three filenames only ("zivkas" with a k, versus "zivcas" with a c
 * everywhere else in the folder); this is disclosed as a real, on-disk naming inconsistency in the
 * game's own asset files, not a transcription error introduced by this project, and it is also the
 * first {@code .mic} file extension this project has catalogued (previously only {@code .mcsb}
 * mesh and {@code .ol} texture extensions were seen under {@code models/armor}, part 15/54). The
 * folder list also carries many classic S.T.A.L.K.E.R.-trilogy suit names ({@code seva}, {@code
 * ozk}, {@code exo}, {@code berill}, {@code kz1}-{@code kz4}, {@code skat}, {@code svd}-adjacent
 * {@code skat10}, etc.) -- the same "reuses/reimagines the original game's roster" pattern this
 * project already established for zone names (part 15) and mob names (part 10), now extended to
 * armor.
 */
public final class ArmorModelCatalog {

    private final Map<String, List<String>> filesByFolder;

    private ArmorModelCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param armorDir the real {@code .../stalker/models/armor} folder. */
    public static ArmorModelCatalog load(Path armorDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(armorDir)) {
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
            throw new IOException(armorDir + ": no armor model folders found");
        }

        return new ArmorModelCatalog(filesByFolder);
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

    /** Real top-level armor model folder names, alphabetical. */
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
     * narrative claims (e.g. "which bases have a sanitar variant") this way instead of hardcoding
     * a fixed list.
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
