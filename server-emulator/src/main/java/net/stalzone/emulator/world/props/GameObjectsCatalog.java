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
 * Catalogs the real assets under {@code modassets/assets/gameobjects/} (part 74) -- 80 files across 3
 * real top-level folders, no loose root files: {@code models} (68), {@code textures} (11), {@code
 * trace} (1). Squarely game content (generic map-decoration props plus a real battle-royale loot-crate
 * taxonomy), not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code models/}</b> (68 files) names a real 8-category battle-royale loot-crate taxonomy via 8
 * real {@code br_*} subfolders (5 files each, a shared {@code .mcsb}/diffuse/emissive/normal/specular
 * set): {@code br_ammo}, two real armor tiers {@code br_armor1lvl}/{@code br_armor2lvl}, {@code
 * br_build} (building materials), {@code br_med}, {@code br_other}, and two real weapon tiers {@code
 * br_weapon1lvl}/{@code br_weapon2lvl}. This directly extends part 72's {@code br_} battle-royale-prefix
 * finding (which only had {@code br_root}/{@code br_spawn} spawn markers and 2 sound cues) into a real,
 * concrete loot-crate-type roster for that mode. Also has {@code quest_placement/} (1 file, a generic
 * quest-marker placement model) and {@code switches/test_switch.mcsb} -- a real leftover
 * developer-testing artifact, the same class of fresh cut-content lead as part 70's {@code
 * z_old_models/} and part 73's {@code case_long/test/}. 26 loose files directly in {@code models/} are
 * generic map-decoration props: real building-facade pieces ({@code plane_door}/{@code plane_okno1}/
 * {@code plane_okno2} ["window", Russian] /{@code plane_stena}/{@code plane_stena2} ["wall", Russian]),
 * a {@code projector.mcsb}, a {@code red_shar.mcsb} ("red ball/sphere", Russian) with its own emissive
 * texture, a generic {@code sphere.mcsb}/{@code sun.mcsb}, and 8 real {@code puh*.mcsb} variants ("fluff/
 * fuzz", Russian; plausibly airborne seed-fluff/dandelion vegetation particles, cross-referenced by
 * {@code textures/puh/} and {@code textures/puh_new/} below) plus an unexplained {@code dekor_tipa.mcsb}
 * ("decor_[something]") lead.
 *
 * <p><b>{@code textures/}</b> (11 files) has {@code patch/} (8 files: real {@code frozen_*}/{@code
 * snowed_*} diffuse/gloss/noise/normal texture sets -- generic winter ground-overlay decals) and {@code
 * puh/}/{@code puh_new/} (3 files total, cross-referencing the {@code models/puh*}/{@code puh_new*} props
 * above). <b>{@code trace/}</b> (1 file, {@code Zabor_betonnyi_dyra_5852.mcvd} -- "concrete fence hole",
 * Russian; a real bullet/damage decal for a concrete-fence prop).
 */
public final class GameObjectsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private GameObjectsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param gameObjectsDir the real {@code .../modassets/assets/gameobjects} folder. */
    public static GameObjectsCatalog load(Path gameObjectsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(gameObjectsDir)) {
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
            throw new IOException(gameObjectsDir + ": no gameobjects asset folders found");
        }

        return new GameObjectsCatalog(filesByFolder);
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

    /** Real top-level gameobjects-asset folder names, alphabetical. */
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
