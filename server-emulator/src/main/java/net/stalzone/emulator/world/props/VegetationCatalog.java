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
 * Catalogs the real assets under {@code modassets/assets/vegetation/} (part 75) -- 42 files across 2
 * real top-level folders, no loose root files: {@code models} (38), {@code textures} (4). Squarely game
 * content (grass/reed decoration props), not an EXENS anti-cheat/launcher asset, so stays in scope under
 * rule 2. Small enough to fixture in full.
 *
 * <p><b>{@code models/}</b> (38 files) has 10 loose shipped grass props ({@code grass.ol}/{@code
 * grass2.ol}/{@code grass3.ol}, real {@code grass_0.obj}-{@code grass_2.obj} Wavefront-OBJ-format
 * models -- a different real 3D format from every other {@code .mcsb}-format model in this project --
 * and {@code grass_a_0.mcsb}/{@code grass_a_1.mcsb}/{@code grass_a_2.mcsb}/{@code grass_b_0.mcsb}) and a
 * real {@code wrk/} ("work") subfolder (28 files) that is this project's richest direct evidence of the
 * internal art-development pipeline: real {@code .3ds} files ({@code grass01.3ds}-{@code grass03.3ds},
 * the native 3D Studio Max format, distinct from both {@code .obj} and this game's own {@code .mcsb}),
 * real {@code .obj}/{@code .mtl} pairs ({@code Grass_01}-{@code Grass_03}), and a real chain of
 * superseded intermediate save iterations for the same asset ({@code grass_a_0.mcsa_old}, {@code
 * grass_a_0.mcsa_veryold}, {@code grass_a_0.mcsaold} -- three distinct old-version extensions for one
 * model) -- plus a file literally named {@code unused.mcsb}, this project's most direct, self-disclosed
 * piece of cut-content evidence yet (stronger than part 70's inferred {@code z_old_models/} naming, since
 * this filename states its own status outright).
 *
 * <p><b>{@code textures/}</b> (4 files, {@code blocks/reed_0.mic}-{@code reed_3.mic}) names real reed
 * ("sugar cane"-style) vegetation-block textures -- {@code reed} is itself a real Minecraft block-family
 * name, a further echo of the user's Minecraft-origin account (parts 63, 67, 68).
 */
public final class VegetationCatalog {

    private final Map<String, List<String>> filesByFolder;

    private VegetationCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param vegetationDir the real {@code .../modassets/assets/vegetation} folder. */
    public static VegetationCatalog load(Path vegetationDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(vegetationDir)) {
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
            throw new IOException(vegetationDir + ": no vegetation asset folders found");
        }

        return new VegetationCatalog(filesByFolder);
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

    /** Real top-level vegetation-asset folder names, alphabetical. */
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
