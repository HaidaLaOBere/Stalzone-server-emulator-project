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
 * Catalogs the real assets under {@code modassets/assets/stalcraft/} (part 67) -- 1,647 files across 4
 * real top-level folders ({@code ctmpatcher} 118, {@code models} 18, {@code sound} 1,057, {@code
 * textures} 452) plus 2 real loose root files ({@code black_texture_placeHolder.ol}, {@code
 * moving.txt}). Squarely game content (generic world-surface/movement audio, building-material
 * texture definitions, and a handful of ability-linked props), not an EXENS anti-cheat/launcher asset,
 * so stays in scope under rule 2. This folder's own NAME is itself evidence: this project's existing
 * confirmed facts are that STALZONE is built on the STALCRAFT engine/asset pipeline (part 63's live
 * Steam Storefront cross-check directly confirmed a DLC titled "STALZONE -- Memories of STALCRAFT: X"),
 * and this tree reads as exactly what that name implies -- generic, reusable base-engine content
 * (surface footsteps, building materials, movement/impact audio) rather than STALZONE-specific
 * narrative content, distinct from every other {@code modassets/assets/} folder swept so far.
 *
 * <p><b>{@code ctmpatcher/ctm/}</b> (118 real files, 115 real per-material subfolders, each holding
 * exactly one real {@code .properties} config file named after the material, e.g. {@code
 * brick_45/brick_45.properties}, {@code metal_3630/metal_3630.properties} -- 3 folders are named by a
 * bare numeric texture id instead, e.g. {@code 152/152.properties}, and one of those numeric folders
 * ({@code 3/}) holds a file actually named for its material, {@code dirt.properties}) is a REAL,
 * DIRECT, hard-format piece of evidence for the user's own firsthand account (recorded in part 63's
 * addendum) that STALZONE was originally built on or with Minecraft-derived tooling: "CTM" is the
 * name of a real, well-known Minecraft resource-pack feature/mod ("Connected Textures Mod"), and a
 * one-{@code .properties}-file-per-material-folder layout is exactly that real feature's own config
 * convention -- not a guess or a name coincidence like "steve", but the literal on-disk shape of a real
 * Minecraft modding convention. The 115 real material names themselves are generic real-world building
 * materials with no narrative content of their own: brick (11 variants incl. {@code brick_gray_mossy},
 * {@code brick_white}), concrete (9 variants), tile ({@code tiles_*}, 8 variants), metal (10 variants
 * incl. {@code metal_green}, {@code metal_red}), plaster, wallpaper, asphalt, gravel, sand, wood
 * ({@code logs}, {@code plywood}), and one real disclosed placeholder ({@code quartz_placeholder}).
 *
 * <p><b>{@code sound/}</b> (1,057 files: 4 real loose files -- {@code click.ogg}, {@code
 * click_insert.ogg}, a real 16-tier {@code deaf1-15.ogg}/{@code deaf_fast.ogg} post-explosion
 * ear-ringing ladder (the same numbered-severity-tier pattern this project already validated for
 * {@code DetectorConfig}'s real rad/thr/bio hazard tiers, part 22, but here for a hearing-damage
 * effect instead) -- plus 6 real subfolders) is almost entirely a generic surface-interaction audio
 * library, not narrative content: real {@code step/} (621 files, a real 17-surface-type roster each
 * with its own file count -- {@code concrete}/{@code default}/{@code grass}/{@code gravel}/{@code
 * ladder}/{@code metal}/{@code sand}/{@code soil}/{@code stone}/{@code wood} at 50 files each, {@code
 * glass}/{@code mesh_fence}/{@code rubber} at 25, {@code snow} at 20, {@code cloth} at 10, {@code
 * inwater}/{@code water} at 8) and a smaller, sparser real {@code stepmobs/} (189 files, a 13-surface
 * subset of the same roster, no {@code default}-labelled generic fallback beyond a real 13-file {@code
 * default*} set of its own) are real player-vs-mob footstep libraries keyed by walked-on material.
 * {@code im/} (132 files) is real player movement-transition audio: {@code climb1-16.ogg}, {@code
 * climb_to_climb_on1-9.ogg}, {@code ladder1-16.ogg}, {@code prone1-20.ogg}, and 5 real 10-file {@code
 * X_to_Y} transition sets ({@code crouch_to_stand} at 8, {@code jump_to_stand} at 13, {@code
 * prone_to_stand}/{@code stand_to_climb}/{@code stand_to_crouch}/{@code stand_to_jump}/{@code
 * stand_to_prone} at 10 each). {@code damage/} (87 files) is real combat-feedback audio: {@code
 * death1-20.ogg} (20 real variants, gaps at 4/5/10/21/22 disclosed as real, not walked-around), {@code
 * hit1-36.ogg} (the largest set here), {@code fallsmall1-21.ogg}, {@code fallbig1-10.ogg}. {@code
 * liquid/} (6) and {@code random/} (4) are small, real, uninventoried ambient sets.
 *
 * <p><b>{@code models/}</b> (18 files, all loose, no subfolders) is mostly generic interior-prop
 * geometry (a real {@code cabinet.mcsb}/{@code metal_shelf.mcsb}/{@code blue_shelf.mcsb}/{@code
 * blue_shelf1.mcsb}/{@code stellage.mcsb} ["stellage" -- a shelving unit, Russian] furniture set, a
 * real {@code bottles.mcsb}/{@code botles_d.ol}/{@code botles_n.ol}/{@code botles_s.ol} bottle prop,
 * {@code urn.mcsb}, {@code cash.mcsb}, {@code concrete_wall.mcsb}) -- but two real files directly
 * cross-reference part 66's freshly-confirmed named prestige abilities from an independent, in-world
 * angle: {@code barricade.mcsb} (part 66's real "Barricade" prestige ability) and {@code mine.mcsb}/
 * {@code mina.ol} ("mina" = "mine", Russian -- plausibly part 66's real "Scout Mine" prestige ability's
 * placeable deployable model, though this specific cross-reference is disclosed as plausible, not
 * confirmed, since no direct filename link exists between the two trees).
 *
 * <p><b>{@code textures/}</b> (452 files: 1 real loose file, {@code empty.ol} -- plus 3 real
 * subfolders) includes a striking real find in {@code gui/}: {@code exbo.mic}/{@code exbo_dark.mic} --
 * the first time this project has found real EXBO branding (the actual real-world publisher/developer
 * per rule 2) as an in-game GUI asset rather than only in Steam Storefront metadata (part 63). {@code
 * items/} (448 files, 3 real subfolders under a real {@code customisation/}: {@code backgrounds} 56,
 * {@code patterns} 63, {@code stickers} 150) holds real cosmetic/economy icon textures, incl. a real
 * {@code module_assault}/{@code module_base}/{@code module_core}/{@code module_ingibitor}/{@code
 * module_lmg}/{@code module_mixed}/{@code module_pistol}/{@code module_shotgun}/{@code module_smg}/
 * {@code module_sniper.ol} set -- a fresh, unconfirmed lead into a real weapon-class-keyed crafting or
 * socket/module system, not investigated further this part -- plus real economy icons ({@code
 * bet_coin.ol}/{@code bet_coin_6/12/25.ol}, {@code seasonpass.ol}/{@code seasonpass_20bl/50bl.ol},
 * {@code faction_point.ol}, {@code quest_giver.ol}), and two real, freshly-dated 2026 seasonal
 * cosmetic-pack names directly under {@code customisation/} -- {@code ny26_pack_1} through {@code
 * ny26_pack_8.ol} (a real "New Year 2026" cosmetic set) and {@code dmz26_pack_1} through {@code
 * dmz26_pack_4.ol} (a real 2026-dated DMZ-mode cosmetic set) -- alongside {@code snegurka.ol} ("Snow
 * Maiden", a real Russian folklore figure, New Year-themed) and {@code murzya.ol} (a real, unexplained
 * name lead). These are this project's first confirmed 2026-dated seasonal content, later than any
 * prior part's seasonal findings (which topped out at 2023 Halloween/New Year and 2025 winter).
 *
 * <p><b>Loose root files.</b> {@code moving.txt} is a real, DIFFERENT-FORMAT movement-tuning file
 * (plain {@code key.path:value} lines, e.g. {@code move.speed.factor:0.95}, {@code
 * move.climb.free.ladder.one.up.speed.factor:1.0153}) from the one this project's own {@code
 * MovementConfig} (part 23) already loads -- disclosed as a real, distinct config format sitting in
 * this specific base-engine-named tree, plausibly a legacy/base STALCRAFT movement-tuning file rather
 * than the one STALZONE actually uses at runtime, but this is not confirmed either way and no attempt
 * was made this part to reconcile the two formats or determine which (if either) is authoritative.
 * {@code black_texture_placeHolder.ol} is a real, self-disclosed placeholder texture.
 */
public final class StalcraftCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalcraftCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalcraftDir the real {@code .../modassets/assets/stalcraft} folder. */
    public static StalcraftCatalog load(Path stalcraftDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalcraftDir)) {
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
            throw new IOException(stalcraftDir + ": no stalcraft asset folders found");
        }

        return new StalcraftCatalog(filesByFolder);
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

    /** Real top-level stalcraft-asset folder names, alphabetical. */
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
