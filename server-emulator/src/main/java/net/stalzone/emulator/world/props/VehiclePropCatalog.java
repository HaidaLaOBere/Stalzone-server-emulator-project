package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalogs the real vehicle-model assets under
 * {@code modassets/assets/customitems/models/blocks/vehicles/} -- 3 real top-level categories
 * confirmed on this install ({@code cars}, {@code boats}, {@code heli}), with {@code cars}
 * further split into 5 real subcategories ({@code cargo_cars}, {@code light_cars},
 * {@code military}, {@code other}, {@code toy}). Real, named vehicle models found this way
 * include authentic Soviet-era vehicles -- {@code zil131}/{@code zil131_firetruck}/{@code maz515}
 * (cargo trucks), {@code moskvich_2136}/{@code moskvich_2140}/{@code niva}/{@code uaz_452}/
 * {@code uaz_469}/{@code vaz_2106}/{@code zaz_968} (cars), {@code batm}/{@code brdm}/
 * {@code btr70}/{@code t72}/{@code mir_tankov} (military armor), {@code dragline}/{@code dt75}/
 * {@code laz}/{@code paz}/{@code t40} (industrial/civilian), {@code aist}/{@code boat_wood}/
 * {@code kazanka}/{@code saturn_rusty}/{@code saturn_not_rusty} (boats), and {@code mi6}
 * (helicopter, plus real {@code mi8_dek_*} mesh files that exist only as loose files with no
 * {@code mi8} subfolder of their own -- see below).
 *
 * <p><b>What this catalog is NOT</b>: per part 33's research (docs/RESEARCH_LOG.md), these
 * vehicle models are, with high confidence, environmental set-dressing/wreck props, NOT
 * drivable vehicles -- confirmed by their placement under a folder named {@code la} ("Level
 * Art") alongside hundreds of other unambiguously-decorative categories, a corrected reading of
 * the client's boot-time asset precompute pass (not evidence of in-world spawn placement), zero
 * vehicle-related entries anywhere in the real 79-line {@code keybinds.txt}, and real first-hand
 * player testimony ("I never seen a vehicle in game") gathered during this project. This class
 * exists so that IF this project (or a future mod) ever adds real drivable-vehicle gameplay, the
 * real model names/paths are already cataloged rather than needing to be rediscovered -- it does
 * not itself add any vehicle behavior, physics, or spawn logic.
 *
 * <p><b>Real structural quirk, disclosed rather than papered over</b>: the same real vehicle
 * sometimes has BOTH a same-named subdirectory (holding texture-variant subfolders, e.g.
 * {@code boats/aist/default}, {@code boats/aist/rust}) AND separate loose mesh files one level
 * up sharing a name prefix (e.g. {@code boats/aist.mcsb}). This class does NOT attempt to merge
 * a model folder with its "matching" loose files by guessing a naming convention -- doing so
 * would be exactly the kind of unevidenced assumption this project's rule 1 forbids (e.g. is
 * {@code kazanka_a.mcsb} and {@code kazanka_b.mcsb} two color variants of the SAME boat, or two
 * DIFFERENT real boats that happen to share a prefix? Nothing in this project's evidence answers
 * that). Instead, {@link VehiclePropGroup#modelFolders()} and {@link VehiclePropGroup#looseFileNames()}
 * are exposed as two separate, honest facts about what's really on disk.
 */
public final class VehiclePropCatalog {

    /** Real, confirmed-non-vehicle auxiliary subfolders found while building this loader (part 38). */
    private static final Map<String, Set<String>> EXCLUDED_SUBFOLDERS = Map.of(
            "cars/cargo_cars", Set.of("addons"),
            "heli", Set.of("crashed_parts", "warmup")
    );

    private static final List<String> CAR_SUBCATEGORIES =
            List.of("cargo_cars", "light_cars", "military", "other", "toy");

    private final List<VehiclePropGroup> groups;

    private VehiclePropCatalog(List<VehiclePropGroup> groups) {
        this.groups = groups;
    }

    /** @param vehiclesDir the real {@code .../models/blocks/vehicles} folder. */
    public static VehiclePropCatalog load(Path vehiclesDir) throws IOException {
        List<VehiclePropGroup> groups = new ArrayList<>();

        for (String carSub : CAR_SUBCATEGORIES) {
            Path dir = vehiclesDir.resolve("cars").resolve(carSub);
            if (Files.isDirectory(dir)) {
                groups.add(loadGroup("cars", carSub, dir));
            }
        }
        for (String topCategory : List.of("boats", "heli")) {
            Path dir = vehiclesDir.resolve(topCategory);
            if (Files.isDirectory(dir)) {
                groups.add(loadGroup(topCategory, null, dir));
            }
        }

        return new VehiclePropCatalog(groups);
    }

    private static VehiclePropGroup loadGroup(String category, String subCategory, Path dir) throws IOException {
        String key = subCategory == null ? category : category + "/" + subCategory;
        Set<String> excluded = EXCLUDED_SUBFOLDERS.getOrDefault(key, Set.of());

        List<String> modelFolders = new ArrayList<>();
        List<String> looseFileNames = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry)) {
                    if (!excluded.contains(name)) {
                        modelFolders.add(name);
                    }
                } else {
                    looseFileNames.add(name);
                }
            }
        }
        Collections.sort(modelFolders);
        Collections.sort(looseFileNames);

        return new VehiclePropGroup(category, subCategory, modelFolders, looseFileNames);
    }

    public List<VehiclePropGroup> groups() {
        return Collections.unmodifiableList(groups);
    }

    public VehiclePropGroup group(String category, String subCategory) {
        for (VehiclePropGroup g : groups) {
            if (g.category().equals(category)
                    && (subCategory == null ? g.subCategory() == null : subCategory.equals(g.subCategory()))) {
                return g;
            }
        }
        return null;
    }

    public int totalModelFolderCount() {
        int total = 0;
        for (VehiclePropGroup g : groups) {
            total += g.modelFolders().size();
        }
        return total;
    }
}
