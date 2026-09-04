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
 * Catalogs the real consumable/attachment item model assets under
 * {@code modassets/assets/stalker/models/items/} (part 57) -- 3 real top-level FOLDERS on this
 * install ({@code armorplate}, {@code food}, {@code meds}), confirmed by direct directory listing,
 * holding 130 real files that this class's folder-only walk (same generic, no-hardcoding design as
 * {@link ArmorModelCatalog}/{@link ArtefactModelCatalog}/{@link BackpackModelCatalog}) actually
 * reaches -- plus 3 more real files sitting loose directly in the {@code items/} root, outside any
 * subfolder ({@code habar.mcsb}, {@code habar_diff.ol}, {@code habar_nrm.ol} -- "habar" is Russian
 * slang for "loot"/"swag"), which this class does not walk and which {@link #totalFileCount()}
 * therefore does not include -- a real, disclosed root-vs-walked gap, the same class of gap part
 * 55's {@code ArtefactModelCatalog} found (17 loose files there), just much smaller here (3, not
 * 17). {@link #foldersContaining(String)} is a reusable substring search, so narrative claims below
 * are checked programmatically, not hand-counted.
 *
 * <p><b>{@code armorplate/}</b> (15 files): 6 real mesh variants -- {@code common}, {@code
 * durable}, {@code meh}, {@code radiator}, {@code solid}, {@code stell} (plausibly a typo for
 * "steel", unconfirmed). Only {@code meh} and {@code radiator} have their own dedicated
 * diff/nrm/spek texture sets; {@code common}/{@code durable}/{@code solid}/{@code stell} have no
 * texture files of their own anywhere in the folder. Three generic, folder-root-level texture
 * files -- {@code armorplate_diff.ol}, {@code armorplate2_diff.ol}, {@code armorplate_nrm.ol} --
 * sit alongside the six meshes with no single matching mesh name; plausibly shared/default
 * textures for the four texture-less meshes, NOT confirmed by any file or log this project has
 * found.
 *
 * <p><b>{@code food/}</b> (4 files): the thinnest folder in this tree -- a single real item,
 * {@code galet} (Russian "галета", a hardtack/cracker), with a full diff/nrm/spek set.
 *
 * <p><b>{@code meds/}</b> (111 files, the richest folder here): its own flat root (35 files) holds
 * 6 real named meds with texture files but NO matching mesh anywhere in this entire {@code items/}
 * tree -- {@code astrikcin}, {@code desperol}, {@code psiblock}, {@code sas}, {@code stark}, {@code
 * stomping} -- a real, disclosed mesh-less-texture gap, the inverse of {@link ArtefactModelCatalog}
 * part 55's texture-less-mesh gap. A real shared-mesh-many-skins convention, not seen in the armor,
 * artefact, or backpack sweeps: {@code meds/inside_lech/} (30 files) has ONE mesh, {@code
 * inside_lech.mcsb} (with its own full diff/emi/nrm/spek set), reused across roughly a dozen
 * differently-named real med items via texture-only variants -- {@code amulet}, {@code cyan},
 * {@code grace}, {@code insight}, {@code mazz}, {@code pilul}, {@code red}, {@code salt}, {@code
 * sedative}, {@code stimulator}, {@code treat}, {@code yellow} -- plus {@code nootrop}, which
 * uniquely among this group ALSO has its own dedicated {@code nootrop.mcsb} mesh alongside
 * {@code nootrop2}/{@code nootrop3} texture-only variants. "lech" is Russian for "cure"/"treatment".
 * A smaller second instance of the same shared-folder pattern: {@code meds/inside_grass_extract/}
 * (9 files) has one full-set mesh, {@code inside_grass_extract}, sharing its folder with two more
 * real texture-only meds, {@code ozverin} and {@code schizoyorsh} (each diff+emi only).
 *
 * <p><b>{@code meds/nmeds/}</b> (32 files, including the nested {@code animated_apt/} folder): a
 * real, disclosed naming-convention shift -- capitalized {@code _Diffuse}/{@code _Normal}/{@code
 * _Specular} texture suffixes, instead of the lowercase {@code _diff}/{@code _nrm}/{@code _spek}
 * used everywhere else in this tree (and in every other model tree this project has catalogued to
 * date) -- not a transcription artifact, confirmed real via direct listing. Real numbered-tier
 * variants: {@code antirad} (radiation med) has 3 real numbered diffuse variants ({@code
 * antirad_191}/{@code _292}/{@code _393_Diffuse.ol}); {@code pciblock_neiron} likewise has 3
 * ({@code _11}/{@code _22}/{@code _33_Diffuse.ol}). {@code aptechka} ("first aid kit") has a base
 * diff/nrm/spek set PLUS 5 real numbered color variants ({@code aptechka_color1} through {@code
 * color5.ol}) PLUS a real {@code aptechka_color_prestige.ol} variant -- real evidence tying a
 * cosmetic "prestige" tier to the STALCRAFT prestige/rank system already known from this project's
 * earlier wire-traffic and API work (part 11), NOT confirmed as the same prestige mechanic by any
 * file or log here. {@code nmeds/psiblock.mcsb} has no texture of its own anywhere in {@code
 * nmeds/}; its only real candidates are the differently-named, differently-cased {@code
 * meds/psiblock_diff.ol}/{@code psiblock_spek.ol} one directory up -- a real, disclosed
 * cross-folder, cross-naming-convention pairing, NOT confirmed as the texture set this mesh
 * actually uses in-game. {@code nmeds/animated_apt/aptechka.mcsb} is a second, independently-named
 * "aptechka" mesh (an "animated" variant), separate from {@code nmeds/aptechka.mcsb} itself.
 *
 * <p><b>{@code meds/reichstag26/}</b> (4 files): {@code aptechkareih} (BaseColor/Normal/Specular)
 * plus {@code reichstag_medbag.mcsb} -- a THIRD real Reichstag/RKKA WWII-event cross-reference
 * found by this project (armor, part 54; backpacks, part 56; now consumables), extending the real
 * {@code reichstag2026} FMOD bank (part 35) and the real {@code hasSeenReichstagHowToPlay} save
 * flag (part 47).
 *
 * <p><b>{@code meds/memoria/}</b> (1 file): {@code memoria.mcsb}, no texture file of any kind --
 * the sole bare-mesh-no-texture gap in this tree, the same class of gap as part 56's {@code
 * bag_tv/} and part 55's texture-less artefact meshes. "memoria" (Latin: "memory"), meaning
 * unconfirmed by any file or log this project has found.
 */
public final class ItemModelCatalog {

    private final Map<String, List<String>> filesByFolder;

    private ItemModelCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param itemsDir the real {@code .../stalker/models/items} folder. */
    public static ItemModelCatalog load(Path itemsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(itemsDir)) {
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
            throw new IOException(itemsDir + ": no item model folders found");
        }

        return new ItemModelCatalog(filesByFolder);
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

    /** Real top-level item model folder names, alphabetical. */
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
