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
 * Catalogs the real assets under {@code modassets/assets/customitems/} (part 70) -- 25,123 files, this
 * project's largest single tree, across 2 real top-level folders ({@code models} 22,798, {@code
 * textures} 2,324) plus 1 real loose root file ({@code empty.txt}, a real disclosed placeholder).
 * Given the scale, this class's own folder-only walk stops at those 2 top-level folders (the same
 * generic pattern as every other {@code world/props/*Catalog} class in this project), but a direct,
 * live listing of {@code models/blocks/}'s 82 real immediate subfolders (this tree's dominant
 * component by far) turned up the richest single batch of cross-references to prior parts' findings
 * this project has produced in one sweep. Squarely game content (world-building props, quest/event
 * objects, deprecated furniture), not an EXENS anti-cheat/launcher asset, so stays in scope under
 * rule 2.
 *
 * <p><b>{@code models/}</b> (22,798 files: 3 real loose files -- {@code btr.ol} [a real BTR-series
 * Soviet/Russian armored-personnel-carrier vehicle model], {@code sharek.mcsb} [a real, unexplained
 * fresh name lead], {@code untitled.mcsb} [a real, self-disclosed placeholder name] -- plus 4 real
 * subfolders) is overwhelmingly dominated by {@code blocks/} (21,024 files, 82 real subfolders -- this
 * project's single largest subfolder anywhere), a world-building/level-design prop library, not a
 * player-placeable voxel system despite the folder's name. A genuine, clearly-non-generic subset of
 * those 82 subfolders directly cross-references a wide swath of this project's prior findings, all
 * confirmed by direct file listing rather than name inference alone:
 *
 * <ul>
 * <li><b>{@code barricade/}</b> (38 files, real {@code barekad}/{@code barikada_army} placeable
 * barricade models) is this project's first real placeable-prop evidence for part 66's named
 * "Barricade" prestige ability, alongside part 67's plausible {@code stalcraft/models/barricade.mcsb}
 * lead -- now with a whole dedicated 38-file model set rather than a single file.</li>
 * <li><b>{@code prestige/}</b> (68 files) is a real battery/charger/discharger power-generator prop
 * set ({@code battery}, {@code charger}/{@code charger_working_emi}/{@code charger_empty_emi}, {@code
 * discharger}) -- a fresh, unconfirmed lead into a base-power or device-charging mechanic tied to the
 * "prestige" namespace, distinct from the named-ability system parts 66/68 already found.</li>
 * <li><b>{@code reichstag/}</b> (43 files, real {@code sand_wall_1}/{@code sand_wall_2} sandbag-wall
 * models) is a FOURTH independent tree corroborating the real Reichstag/WWII event, after part 61's
 * VFX, part 64's PDA map tiles, and part 66's weapon-sound roster.</li>
 * <li><b>{@code zipline/}</b> (6 files, real {@code Carabine}/{@code CarabineFull}/{@code Carbine}
 * models) confirms a real zipline traversal mechanic uses an actual carabiner-clip prop, corroborated
 * by a matching real {@code textures/zipline/} (5 files).</li>
 * <li><b>{@code teleport/}</b> (1 file, {@code block.mcsb}) is a real teleport-pad prop, plausibly
 * tied to part 66's named "Personal Teleport" prestige ability (not confirmed by a direct name
 * match).</li>
 * <li><b>{@code turret/}</b> (6 files) is a real placeable turret prop, plausibly tied to part 66's
 * {@code models/weapons/ability_items/woofer/} deployable (not confirmed by a direct name match).</li>
 * <li><b>{@code z_old_models/}</b> (177 files, real named subfolders {@code bathroom}, {@code
 * computer}, {@code freski_old} ["old frescoes"], {@code plita} ["stove"], {@code tablichki_old} ["old
 * signs/plaques"], {@code transformator}, {@code tv_plazma}, {@code verstak}/{@code verstak_food}
 * ["workbench"], {@code vodokachka} ["water tower"], {@code wires}, {@code zabor} ["fence"]) is a real,
 * self-disclosed DEPRECATED furniture/fixture set -- a direct hit on this project's original
 * "possible cut content" interest, though this is superseded/replaced content (an "_old" naming
 * convention), not necessarily content cut from the shipped game entirely.</li>
 * <li><b>{@code quest_botwin/}</b> (128 files, real industrial/lab props incl. {@code
 * Industrial_Steam_Generator} and {@code afvtoklav} ["autoclave"]) names a fresh, unconfirmed quest
 * lead, "Botwin", with a substantial dedicated asset set.</li>
 * <li><b>{@code test_iznanka/}</b> (5 files, real {@code tank_test.mcsb}/{@code zarya_test.mcsb}
 * ["zarya" = "dawn", Russian, a fresh unexplained lead]) is a real, self-disclosed test folder --
 * "iznanka" ("the other side"/"inside out", Russian) directly cross-references part 68's real {@code
 * insideout} NPC-faction lead, though the exact relationship between the two is not resolved this
 * part.</li>
 * <li>Real subfolder names confirmed to exist by direct listing but NOT individually inventoried this
 * part (disclosed as open leads, grouped by theme): mutant/environment ({@code dek_mutants}, {@code
 * mutant_nest}, {@code encaged}, {@code snork}), seasonal ({@code halloween}, {@code newyear}, {@code
 * newyear24} [a real, freshly-dated 2024 New Year prop set -- {@code configurator.mcsb} and its
 * variants -- distinct from part 67's 2026-dated cosmetic packs], {@code snowman}, {@code
 * snowstorm_supply}), storage/security ({@code safe}, {@code stashes}, {@code personal_storage},
 * {@code signals}, {@code chain_alarm}, {@code claymore}), narrative/faction ({@code clanbazi}, {@code
 * dungeon}, {@code operations}/{@code operations_tv}/{@code operations_w} [cross-referencing part 25's
 * real "operation_lesta" mission finding]), and vehicles ({@code vehicles}, {@code excavator_test},
 * {@code bridge}).</li>
 * </ul>
 *
 * <p><b>{@code models/items/}</b> (1,707 files, 15 real subfolders incl. a real {@code barter/}
 * folder) is mostly loose per-item files -- real consumable/quest-item models such as {@code
 * antidot_item.mcsb}/{@code antirad_item.mcsb} (real anti-radiation items, corroborating this
 * project's existing {@code DetectorConfig} rad-hazard data), a real bandage set ({@code bint.mcsb}/
 * {@code bint2.mcsb}, "bint" = "bandage", Russian), and a real {@code boar_head.mcsb} -- a hunting-
 * trophy item, a fresh, unconfirmed lead into a trophy/collection mechanic. {@code models/trace/} (58
 * files, real {@code .mcvd} damage-decal/trace variants of several {@code blocks/} props, e.g. {@code
 * claymore.mcvd}, {@code barekad.mcvd}) and {@code models/wire/} (6 files, a real generic wire prop
 * set) were confirmed to exist but not individually inventoried this part.
 *
 * <p><b>{@code textures/}</b> (2,324 files: 3 real subfolders, {@code items} 2,316 [20 real
 * subfolders, 449 real loose files], {@code curve} 3, {@code zipline} 5) mirrors {@code models/items/}
 * and {@code blocks/zipline/} respectively; not individually inventoried beyond the zipline
 * cross-reference above.
 */
public final class CustomItemsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private CustomItemsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param customItemsDir the real {@code .../modassets/assets/customitems} folder. */
    public static CustomItemsCatalog load(Path customItemsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(customItemsDir)) {
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
            throw new IOException(customItemsDir + ": no customitems asset folders found");
        }

        return new CustomItemsCatalog(filesByFolder);
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

    /** Real top-level customitems-asset folder names, alphabetical. */
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
