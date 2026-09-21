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
 * Catalogs the complete real {@code modassets/assets/stalker/textures/gui/} folder (part 101) -- 2,124
 * files: 27 real HUD/overlay textures loose at the folder's own root, plus 69 real named subfolders, the
 * last remaining {@code textures/} folder after parts 96-100 closed out {@code particles/}, {@code armor/},
 * and {@code items/}. Like those parts, {@link #load} reuses the same synthetic {@link #ROOT_KEY} deviation
 * for the loose root files -- 27 is fewer than the biggest subfolders here ({@code shop/} alone holds 843),
 * but still more files than roughly half of the 69 subfolders individually hold, so discarding them would
 * lose real content ({@code airdrop.mic}, {@code safe_zone.mic}, {@code border_influence.mic}, ...).
 * {@code shop/} and several other subfolders (e.g. {@code customisation/}) nest up to several levels deep;
 * the existing recursive {@link #collectFiles} helper (unchanged since part 91) flattens this without
 * modification, e.g. {@code shop/content/.../file.mic}.
 *
 * <p><b>Note on {@link #foldersContaining}'s semantics with this folder.</b> It matches a substring against
 * each folder's own <em>relative</em> filenames (i.e. relative to that subfolder's own root, without the
 * subfolder's own name as a prefix) -- so a folder can be named after a theme without any of its own files
 * literally containing that theme's name. E.g. {@code reichstag/}'s 32 files (event backgrounds, industry
 * icons, logos, ...) never repeat the word "reichstag" in their own filenames, so
 * {@code foldersContaining("reich")} resolves only to {@code customisation/} (via its nested
 * {@code stickers/reichstag/} sticker set) and {@code hideout/} (via {@code presets/reichstag.ol}) -- not
 * to {@code reichstag/} itself. Likewise {@code ravent_shop/}/{@code ravent_scoreboard/}'s own single files
 * are literally named {@code atlas_arena_inventory.mic}/{@code atlas_arena_scoreboard.mic} (an internal
 * "Atlas Arena" file-naming theme, disclosed but with no literal {@code en.lang} key match found), so
 * {@code foldersContaining("ravent")} resolves instead to {@code item_packs/} (a nested {@code ravent/}
 * bundle-image subfolder, including {@code bundle_card_image_gladiator.ol} -- a "gladiator" theme further
 * corroborating the arena-combat framing of part 93/100's "Crow Arena"/"Arena of Slavs") and {@code shop/}
 * (dated {@code *_ravent23.ol} cosmetic styles, corroborating that event's 2023 dating).
 *
 * <p>This is a GUI/menu-icon folder rather than a gameplay-item folder like part 100's {@code items/}, so
 * its {@code en.lang} crosswalk skews toward real game <em>modes</em> and <em>systems</em> named by their
 * top-level folder, rather than individual items. Headline confirmations from this part:
 *
 * <ul>
 *   <li>{@code inside/}'s {@code stalker.hud.inside.phantom.turned.title}="YOU HAVE TURNED INTO A PHANTOM"
 *       and {@code .desc}="Find the Altar of Cleansing to complete the challenge and be reborn!" CONFIRM a
 *       further "Inside Out" mechanic beyond part 100's consumable-set finding: a phantom-transformation
 *       penalty state with its own named landmark, the "Altar of Cleansing", tied to run failure/rebirth.
 *       Root-level {@code turned/} (2 files) is this mechanic's own icon set.</li>
 *   <li>{@code incident/} is CONFIRMED a full real time-limited PvE raid mode, "Dead Time" (internal id
 *       {@code incident}): {@code bundle.notf.incident.info.start}="Scientists have stabilized the temporal
 *       field! You can now take part in operation Dead Time.", with its own terminal/keycard mechanic
 *       ({@code gameobjects.action_tip.incident_terminal.keycard_is_required}), an energy-cell-capture
 *       objective, a named enemy faction ("Kostelevsky group" -- {@code general.stats.stat.incident-part}),
 *       and its own "Incident Battle Pass" ({@code bundle.item_tagger.incident_battlepass}).</li>
 *   <li>{@code prestige/} is CONFIRMED the real "Coliseum" mode: {@code bundle.notf.prestige.info.start}=
 *       "Soothsayer has stabilized the Cyclical Trials arena once again. The Coliseum awaits its
 *       contenders!" -- a named NPC ("Soothsayer") gating a recurring PvP arena, distinct from part 93's
 *       "Crow Arena"/"Arena of Slavs".</li>
 *   <li>{@code dmz/} is CONFIRMED the real "Black Raid" operation and {@code frontline/} the real "Attack
 *       on Perimeter" operation ({@code bundle.notf.dmz.title}/{@code bundle.notf.frontline.title}) -- both
 *       previously known only by their internal folder/id names in this project's asset trees, now with
 *       real in-game display names.</li>
 *   <li>{@code encaged/} is CONFIRMED a real timed event, "Encaged" ({@code bundle.notf.encaged.title}),
 *       with its own reward currency "SHADOW TROPHIES" ({@code stalker.gui.eventhub.experience.encaged.
 *       cases}).</li>
 *   <li>{@code stalcraftshop/} is CONFIRMED a real-world merchandise pre-order tie-in, distinct from every
 *       other in-game shop folder: {@code stalker.gui.stalcraftshop.text_line1}="Seekers, attention!
 *       There's news everyone's been waiting for -- a long-awaited shipment of merch is coming from the
 *       Mainland." with a "PLACE A PRE-ORDER" call to action -- an out-of-game merchandise storefront
 *       surfaced through the in-game UI.</li>
 *   <li>{@code sbattlepass/}'s subfolder names are a real season timeline spanning {@code act1},
 *       {@code spring24} through {@code winter26} (spring/summer/autumn/winter x 2024-2026, 12 dated
 *       seasons plus the initial act) -- the fullest single real content-dating artifact this project has
 *       found, corroborating and extending every prior part's individual seasonal-content dates (e.g. part
 *       67's 2023 case timeline, part 100's {@code ny23}/{@code blizzard23}).</li>
 *   <li>{@code phonograph/} is CONFIRMED a real Hideout/safe-zone music system: collectible vinyl records
 *       obtained via location search, quest completion, or seasonal events ({@code phonograph.gui.
 *       about_description_content}).</li>
 *   <li>{@code repairman/}, {@code bureau/} ("Order from the Mainland" resource-gathering rewards), and
 *       {@code arcades/} (real PvP kill/death/assist stat tracking) are further confirmed real systems,
 *       disclosed but not cross-referenced beyond their direct {@code en.lang} confirmation.</li>
 *   <li>{@code sbattle2026}, {@code protectedareas}, {@code blizzard_banner}, {@code artefacts_compensation
 *       _banner}, {@code recap25}, {@code undead}, and {@code wild location} (note: this real folder name
 *       contains a literal space) found no literal {@code en.lang} key match under their folder names and
 *       are disclosed, plausible by name/context alone, but NOT confirmed.</li>
 * </ul>
 */
public final class TextureGuiCatalog {

    /** Synthetic folder key for the 27 real loose files at {@code gui/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private TextureGuiCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param guiDir the real {@code .../modassets/assets/stalker/textures/gui} folder. */
    public static TextureGuiCatalog load(Path guiDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(guiDir)) {
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
            throw new IOException(guiDir + ": no gui asset folders/files found");
        }

        return new TextureGuiCatalog(filesByFolder);
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
