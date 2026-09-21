package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the complete real {@code modassets/assets/stalker/textures/items/} folder (part 100) --
 * 903 files: 133 real consumable/artefact textures loose at the folder's own root, plus 14 real named
 * subfolders (some nested one level deeper, e.g. {@code armor/reichstag/}, {@code artefacts/ravent/}).
 * Like {@link TextureArmorCatalog} (part 99) and {@link TextureParticlesCatalog} (part 98), this root has
 * a large, non-trivial set of loose files (133, more than 10 of the 14 subfolders individually hold), so
 * {@link #load} reuses the same synthetic {@link #ROOT_KEY} deviation rather than discarding them.
 *
 * <p>This is this project's richest single gameplay-item {@code en.lang} crosswalk to date, spanning
 * crafting, weather events, and a previously only thinly-evidenced boss mode:
 *
 * <ul>
 *   <li>{@code art.mod_calibration.ol}/{@code art.mod_change.ol}/{@code art.mod_change_2.ol}/
 *       {@code art.mod_reroll.ol}/{@code art.mod_shuffle.ol} are CONFIRMED real artefact-modification
 *       consumables (Calibrating Agent, Changing Serum, Special Changing Serum, Reset Essence,
 *       Destabilizer) -- a previously-undisclosed real artefact re-rolling/crafting economy sitting on
 *       top of part 71's confirmed named anomaly-type roster.</li>
 *   <li>The {@code inside/} subfolder is CONFIRMED to be the real consumable set for part 68/70/98's
 *       long-disclosed but thinly-evidenced "insideout" lead: {@code inside_amulet.ol} resolves to
 *       "Clergyman's Amulet", {@code inside_grace.ol} to "Clergyman's Prayer" (both naming a real NPC,
 *       "Clergyman"), {@code inside_oblivion.ol} to "Oblivion", {@code inside_insight.ol} to "Poet's
 *       Inspiration", and root-level {@code amalgam.ol} to a real substance used to "activate the spatial
 *       rifts that stalkers use to leave the Inside Out" -- this upgrades "Inside Out" from a bare boss
 *       -invasion mode name into a real pocket-dimension/extraction-style PvE mode with its own NPC and
 *       its own teleporter-evacuation mechanic.</li>
 *   <li>Root-level {@code proto_electra.ol}/{@code proto_karousel.ol}/{@code proto_kisel.ol}/
 *       {@code proto_lighter.ol}/{@code proto_trampoline.ol}/{@code proto_voronka.ol} are disclosed
 *       (icon-name-only, no literal {@code en.lang} key match found) prototype-artefact icons whose
 *       names directly echo part 71's confirmed named anomaly roster (Electra/Carousel/Kissel/
 *       Trampoline/Funnel -- "voronka" is Russian for "funnel") and part 98's confirmed "Lighter"
 *       anomaly; {@code proto_teleport.ol} and {@code proto_snowstorm.ol} are similarly disclosed but
 *       not resolved to a specific existing anomaly name.</li>
 *   <li>{@code en.lang}'s {@code snowstorm.notf.start_1}..{@code start_10}/{@code end_1}..{@code end_8}
 *       CONFIRM a full third real severe-weather event distinct from part 80/81's Emission/Blowout and
 *       part 98's Electrostorm -- a cold-front storm with its own frost-artefact reward window, and a
 *       real "Snowstorm Squad" military waypoint tie (part 100, this class); {@code blizzard23/stark.ol}
 *       and {@code blizzard23/grog.ol} are CONFIRMED real Blizzard-event consumables ("STARK Anabolic
 *       Steroid", "Grog") whose flavor text names a "Frost Fighter" title, corroborating part 95's
 *       confirmed "Frost Troopers" mechanic.</li>
 *   <li>{@code med_apt_reich.ol} is CONFIRMED ("Military First-Aid Kit") a TWELFTH independent Reichstag
 *       corroboration; {@code backpacks/bag_flamethrower_reich.ol}/{@code bag_flamethrower_rkka.ol} is a
 *       THIRTEENTH (RKKA = Raboche-Krestyanskaya Krasnaya Armiya, the real historical Red Army initialism,
 *       further corroborating the Soviet side of the event); and {@code armor/reichstag/}'s full matched
 *       12-skin two-faction roster (six {@code reich_*} German ranks mirrored by six {@code rkka_*}
 *       Soviet ranks: capitan/corporal/engineer/gunner/sniper/stormtrooper on each side) is a FOURTEENTH
 *       and the richest single Reichstag corroboration this project has found -- a full class-parity
 *       cosmetic roster for both historical sides of the event.</li>
 *   <li>{@code stashes/ravent.ol}, {@code artefacts/ravent/}, and {@code currency.type.id.ravent_23}
 *       ("Crow Coupons") CONFIRM a dated (2023) event-currency/stash/artefact-skin family directly tied
 *       to part 93's "Crow Arena"/"Arena of Slavs" event by its raven/crow-themed name.</li>
 *   <li>{@code armorplates/ceramic1.ol}/{@code composite1.ol} (through tier 5) are CONFIRMED real
 *       ("Ceramic Plate I", "Composite Plate I", ...) -- a previously-undisclosed modular armor-plate
 *       insert/tier system distinct from the base {@code armor/} skin roster.</li>
 *   <li>{@code styles_background/} and {@code stashes/} reuse part 95's confirmed 6-tier rank-badge names
 *       (LEGEND/MASTER/NEWBIE/PICKLOCK/STALKER/VETERAN) as item-background/stash-skin cosmetics, a
 *       further corroboration of that same reputation ladder at the item-icon level.</li>
 *   <li>{@code dlyatestov/} (Russian for "for tests") is a further self-disclosed leftover developer-test
 *       folder, joining parts 70/75/78's similar finds -- despite the folder name, its 5 files
 *       ({@code BADY.ol}/{@code BADYe.ol}/{@code SAS.ol}/{@code painblocker.ol}/{@code stimulyator.ol})
 *       are CONFIRMED real shipped medical items ("Battery Energy Drink", "Battery Tonic", "Pain Killer",
 *       "Strength Stimulator"), not cut content.</li>
 *   <li>{@code craft/} is a CONFIRMED real cooking/crafting-recipe-result item set (soups, cured meats,
 *       wine, pickles, condensed milk) including joke items ({@code tinfoil_hat.ol}), disclosed but not
 *       pursued further; {@code collectible/}'s {@code cubemelon.ol}/{@code saltweed.ol}/
 *       {@code sweettooth.ol} are CONFIRMED real "Pulp" harvestable resources found in anomalous/
 *       contaminated zones, a real gathering-resource system.</li>
 *   <li>{@code ny23/} is a further real 2023-dated seasonal item set (following part 67's already-
 *       confirmed 2023 case timeline), disclosed but not cross-referenced further.</li>
 * </ul>
 */
public final class TextureItemsCatalog {

    /** Synthetic folder key for the 133 real loose files at {@code items/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private TextureItemsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param itemsDir the real {@code .../modassets/assets/stalker/textures/items} folder. */
    public static TextureItemsCatalog load(Path itemsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(itemsDir)) {
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
            throw new IOException(itemsDir + ": no item asset folders/files found");
        }

        return new TextureItemsCatalog(filesByFolder);
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

    public List<String> folderNames() {
        return new ArrayList<>(filesByFolder.keySet());
    }

    public List<String> filesIn(String folderName) {
        List<String> files = filesByFolder.get(folderName);
        return files == null ? Collections.emptyList() : Collections.unmodifiableList(files);
    }

    public List<String> foldersContaining(String substring) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesByFolder.entrySet()) {
            for (String file : entry.getValue()) {
                if (file.contains(substring)) {
                    out.add(entry.getKey());
                    break;
                }
            }
        }
        return out;
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
