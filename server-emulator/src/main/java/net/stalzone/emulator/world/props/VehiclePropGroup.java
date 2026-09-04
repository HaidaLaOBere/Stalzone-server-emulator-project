package net.stalzone.emulator.world.props;

import java.util.Collections;
import java.util.List;

/**
 * One real folder under {@code vehicles/} -- either {@code vehicles/<category>} (for
 * {@code boats}/{@code heli}) or {@code vehicles/cars/<subCategory>} (cars are one level
 * deeper, into 5 real subcategories). See {@link VehiclePropCatalog} for how these are
 * discovered and why {@code modelFolders} and {@code looseFileNames} are kept separate rather
 * than merged by a guessed naming rule.
 */
public final class VehiclePropGroup {

    private final String category;
    private final String subCategory;
    private final List<String> modelFolders;
    private final List<String> looseFileNames;

    public VehiclePropGroup(String category, String subCategory,
                             List<String> modelFolders, List<String> looseFileNames) {
        this.category = category;
        this.subCategory = subCategory;
        this.modelFolders = Collections.unmodifiableList(modelFolders);
        this.looseFileNames = Collections.unmodifiableList(looseFileNames);
    }

    public String category() {
        return category;
    }

    /** Null for {@code boats}/{@code heli} -- only {@code cars} is subdivided on this install. */
    public String subCategory() {
        return subCategory;
    }

    /**
     * Real subdirectory names directly under this group, EXCLUDING the small, explicitly-known
     * set of non-vehicle auxiliary folders {@link VehiclePropCatalog} disclosed while building
     * this loader ({@code addons} under {@code cars/cargo_cars}; {@code crashed_parts} and
     * {@code warmup} under {@code heli}). Each remaining name is a real named vehicle model
     * (e.g. {@code zil131}, {@code t72}, {@code mi6}).
     */
    public List<String> modelFolders() {
        return modelFolders;
    }

    /**
     * Real files sitting directly in this folder (not inside any subdirectory) -- e.g.
     * {@code heli}'s {@code mi8_dek_*.mcsb}/{@code .mcvd} files, which exist ONLY as loose files
     * here with no {@code mi8} subfolder of their own (unlike {@code mi6}, which IS a subfolder).
     * Deliberately NOT grouped into a guessed "this loose file belongs to that model folder"
     * relationship -- see {@link VehiclePropCatalog}'s javadoc for why.
     */
    public List<String> looseFileNames() {
        return looseFileNames;
    }

    public String label() {
        return subCategory == null ? category : category + "/" + subCategory;
    }
}
