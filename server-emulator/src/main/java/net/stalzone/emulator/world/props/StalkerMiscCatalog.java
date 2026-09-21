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
 * Catalogs the real assets under {@code modassets/assets/stalker/} for 5 small, unrelated leaf trees
 * swept together in part 93 (85 files total): {@code hungerBuffs/} (1), {@code anims/} (5), {@code
 * figma/} (10), {@code slices/} (13), {@code figma_gui/} (56). Each is far too small to justify its own
 * single-purpose catalog class, so -- following part 79's {@link MiscAssetsCatalog} precedent -- this
 * class deliberately deviates from every prior {@code world.props} catalog: instead of walking every
 * immediate top-level subfolder of one dedicated directory, {@link #load} is given the shared {@code
 * modassets/assets/stalker/} root and walks only a fixed whitelist of 5 known folder names ({@link
 * #KNOWN_FOLDERS}), each recursed fully into one "folder" bucket in the returned map. This means {@link
 * #load} does NOT see or index anything else under {@code stalker/} -- in particular not its 7 other,
 * far larger real top-level trees ({@code sound/} 10,500 files, {@code textures/} 4,066, {@code models/}
 * 3,525 -- 2 of whose subfolders were already swept in parts 56-57, {@code stream/} 331, {@code
 * streamrandom/} 240, {@code music/} 105, {@code lang/} 5, already catalogued in part 90 as {@link
 * net.stalzone.emulator.localization.LangCatalog}), all of which remain real, disclosed open leads.
 * Squarely game content, not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code hungerBuffs/}</b> (1 file, {@code kit.mic}) -- a single real icon texture, cross-
 * referencing a real, previously-uncatalogued Hunger/Starvation stat mechanic confirmed in {@code
 * en.lang} ({@code player_effect.hunger.title=Hunger}, {@code core.handbook.id.hunger.title=Starvation},
 * with a real handbook entry: "When your character is full, you get a bonus to some stats... buy food at
 * safe zones... or get it for completing quests... open your inventory, right click and select [Eat]").
 * {@code kit.mic} plausibly renders one of the real named food-kit items also confirmed in {@code
 * en.lang} ({@code item.food.irp}/{@code item.food.irp_usa}, both "A food kit designed for situations
 * when preparing a hot meal isn't an option" -- real IRP/MRE-style ration packs), though no direct
 * filename-to-item-id match was found, so that specific tie stays a plausible, not confirmed, reading.
 *
 * <p><b>{@code anims/}</b> (5 files, all under {@code menu/}) -- real main-menu idle/walk animation
 * clips ({@code idle.mcal}, {@code idle_new.mcal}, {@code menu_idle.mcal}, {@code menu_walk.mcal}), plus
 * a THIRD independent "steve" reference in this project, {@code steve_anim.mcal} -- after part 91's
 * base player rig ({@code steve.mcsb}/{@code .obj}/{@code .ol}/{@code .mic}) and part 76's steve death-
 * ragdoll, this confirms the Minecraft-derived "steve" rig is even what idles and walks on the real MAIN
 * MENU screen, not just in-world -- the strongest possible evidence that "steve" is the literal default/
 * fallback character model at every layer of the client, not a stray leftover collider (part 63).
 *
 * <p><b>{@code figma/}</b> (10 files: {@code figma.bin} + 9 hashed {@code images/*.mic} files) -- a real
 * Figma design-tool export cache (the folder name and internal structure -- one binary index file plus a
 * flat folder of SHA1-hash-named image exports -- match Figma's real "code export"/plugin-cache
 * convention), a real developer-workflow artifact rather than in-game content; the hashed filenames carry
 * no readable names, so this folder is catalogued for completeness but not investigated further.
 *
 * <p><b>{@code slices/}</b> (13 real named world "slice"/pre-fab-block files, {@code .mcws}) -- this
 * part's richest single find. {@code battlefields.mcws} cross-references part 72's {@link
 * StalkerClansCatalog} clan-war battlefield-map roster directly. {@code stalkers_sovhoz.mcws} confirms a
 * real named winter-themed zone, "Sovkhoz of Shevchenko" ({@code location.sovhoz.name}, {@code
 * map.global.region.sovhoz.desc}: "The Sovkhoz has really been through it!... repeatedly turned into
 * test sites for anomalous activity"), with a real Frost-prefixed mutant-boss roster ({@code
 * pda.waypoint.we.sovhoz_boss_pseudo.title=Frost Behemoth}, {@code .sovhoz_boss_chimera.title=Frost
 * Fiend}, {@code .sovhoz_boss_copypig.title=Frost Xerohoof}) and a real achievement ({@code
 * general.achievement.id.sovhoz_explored.title=Great Plains}, desc "Visit Sovkhoz of Shevchenko (before
 * 2019)" -- a real, disclosed 2019 in-lore date reference). {@code sever.mcws} confirms a real named Bar
 * sub-area, "Drunken Geiger Bar" ({@code settlement.id.sever.title}, {@code savepoints.id.savepoint_
 * sever.name}, {@code settlement.id.rostok.level.7.perk.sever=Access to the Drunken Geiger} -- tying
 * {@code sever} directly to part 27's real {@code rostok} location). Seven real per-target TEST maps --
 * {@code testers_base}, {@code testers_covenant}, {@code testers_duty}, {@code testers_freedom}, {@code
 * testers_merc}, {@code testers_rostok}, {@code testers_sever} -- directly cross-reference 4 of part 91's
 * confirmed 6-id playable-alliance roster (covenant/duty/freedom/merc) plus this part's own {@code
 * rostok}/{@code sever} finds, strongly suggesting these are real internal per-faction/per-location
 * developer test levels. {@code derevnya_novichkov.mcws} ("Village of Novices," Russian) has no {@code
 * en.lang} match -- a real, disclosed unconfirmed lead, plausibly a tutorial/starting-area map given the
 * name. {@code north_factions_block.mcws} and {@code bar_save_residential_hangar.mcws} are likewise real,
 * disclosed, unconfirmed leads.
 *
 * <p><b>{@code figma_gui/}</b> (56 files across 13 subfolders, each a Figma-export bucket like {@code
 * figma/} above) -- real UI-mockup exports for several distinct modes/screens. {@code bf_finish_stat}/
 * {@code bf_spawn_select}/{@code bf_stats} confirm {@code bf} = "Battlefield" (part 72's clan-war mode),
 * corroborated by a real 10-entry camo/paint set in {@code en.lang} ({@code
 * item.camo.arm.bf_skin_1.name=Breakdown Paint} through {@code bf_skin_10.name=«Blood Grid» Paint}).
 * {@code clans_hud} (1 real icon, {@code home.ol}) further corroborates the clan-war mode's own HUD.
 * {@code death_screen} confirms a real {@code stalker.gui.death_screen} namespace ({@code
 * .damage_type.freeze.label=Frost Damage}, tying back to {@code slices/}'s Sovkhoz frost theme above).
 * {@code ffa_finish_stat}/{@code ffa_grid}/{@code ffa_spawn_select}/{@code ffa_stats} confirm a real
 * SECOND, previously-uncatalogued PvP mode distinct from part 72's clan-war capture-the-flag: "FFA"
 * (free-for-all), under the same real {@code clans} namespace, with a real 5-map roster ({@code
 * clans.ffa_small_berdovka}, {@code ffa_nizina}, {@code ffa_nizina2}, {@code ffa_nizina3}, {@code
 * ffa_roza_vetrov2}) and real named building/callout points within them (Fountain, Monument,
 * Kindergarten, Department Store, School, Peak 239, Sunshine Tower, Warehouse, Apple Tower, Raspberry
 * Tower, Blue Moria House). {@code ravent_finish}/{@code ravent_round} confirm a real, MAJOR seasonal
 * event, the "Crow Arena" / "Arena of Slavs" ({@code shop.category.ravent.name}, {@code
 * shop.bundle_ravent23} 2023-dated) -- a PvE horde/wave-defense mode built entirely around Slavic
 * mythology, with real named bosses (Baba Yaga, Kikimora, Leshy, Zmey Tugarin, Rat King, Firebird,
 * Gorynich, "Champion of Perun/Khors/Stribog"), a real dedicated event currency ({@code
 * currency.type.id.ravent_23.name=Crow Coupons}), and a rich real multi-tier wave-name roster ({@code
 * ravent.waveTypes.*}, e.g. "Baptism of fire," "Lizard Invasion," "Pigstorm," "Titans of the Zone") --
 * this project's first confirmed Slavic-folklore-themed content pack, distinct from every prior real
 * Christmas/New Year/Halloween seasonal thread. {@code test} and {@code buttons} are generic/placeholder
 * buckets, not investigated further.
 */
public final class StalkerMiscCatalog {

    /** The fixed whitelist of small leaf-tree folder names this class exclusively loads. */
    public static final List<String> KNOWN_FOLDERS = List.of(
            "hungerBuffs", "anims", "figma", "slices", "figma_gui");

    private final Map<String, List<String>> filesByFolder;

    private StalkerMiscCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerRoot the real {@code .../modassets/assets/stalker} folder (the shared parent, not a dedicated one). */
    public static StalkerMiscCatalog load(Path stalkerRoot) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        for (String folderName : KNOWN_FOLDERS) {
            Path folderRoot = stalkerRoot.resolve(folderName);
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
            throw new IOException(stalkerRoot + ": none of the known small asset folders were found");
        }

        return new StalkerMiscCatalog(filesByFolder);
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
