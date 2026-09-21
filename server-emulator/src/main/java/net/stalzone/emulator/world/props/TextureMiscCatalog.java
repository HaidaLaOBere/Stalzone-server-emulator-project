package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real assets under {@code modassets/assets/stalker/textures/} for 6 small, unrelated
 * leaf trees swept together in part 96 (19 files total): {@code waypoints/} (9), {@code br/} (5),
 * {@code blizzard_overlay/} (2), {@code new_effects_texture/} (1), {@code mining/} (1), {@code map/}
 * (1). These are the smallest real top-level folders under {@code textures/} (4,066 files total) --
 * the 3 large ones ({@code gui/} 2,124, {@code items/} 903, {@code armor/} 731) and the 2 mid-sized
 * ones ({@code loadscreen/} 186, {@code particles/} 95) are deliberately left for future dedicated
 * parts, following this project's established "small stuff swept together, big stuff standalone"
 * split (parts 54-57/93/95). {@link #load} is given the shared {@code textures/} root and walks only
 * a fixed whitelist of 6 known folder names ({@link #KNOWN_FOLDERS}), each recursed fully into one
 * flat "folder" bucket -- it does NOT see or re-index {@code gui/}/{@code items/}/{@code armor/}/
 * {@code loadscreen/}/{@code particles/}, nor the 8 real loose root-level files ({@code
 * ampula_emissive.ol}, {@code ampula_lekarstvo_diff.ol}, {@code backpack_big.mic}, {@code
 * backpack_medium.mic}, {@code backpack_small.mic}, {@code particlesFlat.inf}, {@code
 * particlesFlat.map}, {@code particlesFlat.ol}) -- the same folder-only-walk root/loose-file gap
 * {@link StalkerShopCatalog} and {@link CurrencyCatalog} disclose but deliberately do not index.
 * Squarely game content, not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code waypoints/}</b> (9 files) -- CONFIRMED. {@code comand mark}/{@code comand mark2}/
 * {@code comand mark3} (real on-disk spelling, "comand" not "command"), {@code personal mark}, {@code
 * quest}, {@code select quest}, {@code finish quest}, {@code finish select quest}, {@code death} --
 * PDA/map waypoint marker icons. Confirmed via {@code en.lang}'s {@code
 * core.handbook.id.squad_marks.title=Squad markers} / {@code core.handbook.id.squad_marks.main=If you
 * are part of a squad, then when you place a waypoint on the map you can choose whether to make it a
 * squad marker. Squad waypoints are visible to all squad members.} -- a real squad/personal waypoint
 * marker system, directly matching this folder's {@code comand}/{@code personal} mark icon pair (the
 * numbered {@code comand mark2}/{@code mark3} variants plausibly per-squad-member color/number
 * variants, not confirmed further) alongside generic quest-marker icons.
 *
 * <p><b>{@code br/}</b> (5 files) -- {@code br_layer_0}/{@code br_layer_1}/{@code br_layer_2}/{@code
 * br_layer_flow} plus {@code electricity}. CONFIRMED as Battle Royale mode assets via {@code
 * en.lang}'s extensive real {@code br_*} namespace: {@code general.achievement.id.br_win.desc=Win in
 * Battle Royale}, {@code .br_zone_death.desc=Die outside the Zone in Battle Royale}, plus a {@code
 * go.br_ammo}/{@code go.br_armor1lvl}/{@code go.br_weapon1lvl}/{@code go.br_med}/{@code go.br_build}
 * BR loot-crate item set. The numbered {@code layer_0/1/2} plus {@code layer_flow} shader-layer
 * naming plausibly renders BR's shrinking-zone storm boundary (the {@code .br_zone_death} achievement
 * confirms a real in/out-of-zone boundary exists), with {@code electricity} plausibly an electrified-
 * boundary effect texture -- not confirmed beyond the shared {@code br_} prefix and folder grouping.
 *
 * <p><b>{@code blizzard_overlay/}</b> (2 files) -- {@code pd} and {@code svalka}. CONFIRMED as
 * per-location map overlay textures for the already-confirmed Blizzard winter seasonal event (part
 * 95's {@code models/blizzard/frozen}, {@code currency.type.id.blizzard_23.name=Frozen Bones}): {@code
 * en.lang}'s {@code map.location.id.pd.name=The Path of Fools} and {@code
 * map.location.id.svalka.name=Scrapyard} are two real, distinct map locations, each getting its own
 * named overlay texture here -- plausibly a snow/frost visual treatment applied to specific map
 * locations while the Blizzard event's storm system is active there.
 *
 * <p><b>{@code mining/}</b> (1 file, {@code display.ol}) -- plausibly a UI/display texture for the
 * real metal-detector "Digging" mechanic CONFIRMED via {@code en.lang}'s {@code
 * core.handbook.id.mining.title=Digging} / {@code core.handbook.id.mining.main=Sometimes, useful items
 * are buried in piles of dirt and junk. To find them, all you need is a metal detector. ... In active
 * mode, you can determine the direction and distance to the buried loot based on the frequency and
 * tone of the signal} -- the metal detector's active/passive-mode signal display, not confirmed
 * further beyond the shared "mining" name and single-file plausibility.
 *
 * <p><b>{@code new_effects_texture/}</b> (1 file, {@code leaf_atl_1.mic}) and <b>{@code map/}</b> (1
 * file, {@code map_icons.ol}) -- real but generic (a leaf-particle atlas texture; a PDA/map icon
 * atlas texture); neither has an {@code en.lang} match specific enough to name further.
 *
 * <p>Of the 8 real loose root-level files this class deliberately does not index: {@code
 * ampula_emissive.ol}/{@code ampula_lekarstvo_diff.ol} ("lekarstvo" = Russian for "medicine") are
 * CONFIRMED via {@code en.lang}'s {@code item.quest.whirlpool_medicine.name=Ampoule With Medication} /
 * {@code .additional_stats_tip=Ampoule with an unknown medicinal solution} -- a real Whirlpool-anomaly
 * quest item texture; {@code backpack_big}/{@code backpack_medium}/{@code backpack_small.mic} match
 * the real {@code go.small_backpack_base.name=Small backpack} / {@code
 * go.medium_backpack_base.name=Medium backpack} / {@code go.big_backpack_base.name=Large backpack}
 * item namespace (generic backpack-size UI icons); {@code particlesFlat.inf}/{@code .map}/{@code .ol}
 * is a real generic particle-texture atlas set (the same {@code .inf}/{@code .map}/{@code .ol} texture-
 * atlas trio convention seen elsewhere in this project), disclosed here in Javadoc only, per the
 * {@link StalkerShopCatalog}/{@link CurrencyCatalog} root/loose-file precedent, rather than modeled in
 * code.
 *
 * <p>Pure cataloguing, like parts 54-57/93/95 -- no new {@code GameLoop}/{@code SingleplayerServer}
 * wiring, so no live {@code Main}-entry-point run was needed. {@code textures/}'s 5 larger real
 * top-level folders ({@code gui/} 2,124, {@code items/} 903, {@code armor/} 731, {@code loadscreen/}
 * 186, {@code particles/} 95) remain the largest real open leads under {@code textures/} for future
 * parts; {@code stalker/}'s other remaining large top-level tree ({@code sound/} 10,500 files) also
 * remains an open lead.
 */
public final class TextureMiscCatalog {

    /** The fixed whitelist of small leaf-tree folder names this class exclusively loads. */
    public static final List<String> KNOWN_FOLDERS = List.of(
            "waypoints", "br", "blizzard_overlay", "new_effects_texture", "mining", "map");

    private final Map<String, List<String>> filesByFolder;

    private TextureMiscCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param texturesRoot the real {@code .../modassets/assets/stalker/textures} folder (the shared parent, not a dedicated one). */
    public static TextureMiscCatalog load(Path texturesRoot) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        for (String folderName : KNOWN_FOLDERS) {
            Path folderRoot = texturesRoot.resolve(folderName);
            if (!Files.isDirectory(folderRoot)) {
                continue;
            }
            List<String> files = new ArrayList<>();
            collectFiles(folderRoot, folderRoot, files);
            Collections.sort(files);
            if (!files.isEmpty()) {
                filesByFolder.put(folderName, files);
            }
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(texturesRoot + ": none of the known small texture folders were found");
        }

        return new TextureMiscCatalog(filesByFolder);
    }

    private static void collectFiles(Path folderRoot, Path dir, List<String> out) throws IOException {
        try (var entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    collectFiles(folderRoot, entry, out);
                } else {
                    out.add(folderRoot.relativize(entry).toString().replace('\\', '/'));
                }
            }
        }
    }

    /** Real known-small-tree folder names actually found under the given root, alphabetical. */
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
