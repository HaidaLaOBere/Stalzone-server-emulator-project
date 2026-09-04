package net.stalzone.emulator.items;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code modassets/assets/stalker/weight.txt} -- a real, plain-text per-item inventory
 * weight table, keyed by the client's INTERNAL numeric item id (the same id space as
 * {@code itemnames.dat} and save-data records, see docs/RESEARCH_LOG.md's "Item ID systems"
 * section -- NOT the opaque 4-char ids {@code weapons.json} uses). Confirmed real format by
 * direct inspection (part 21): CRLF-terminated lines, {@code //comment} section headers
 * (`//melee`, `//food`, `//armor`, `//other`, `//med`, `//items`, `//ammo`), a single special
 * {@code character_max-50} line (the default carry-weight cap "without bonuses"), and 480
 * {@code <itemId>-<weight>} entries.
 *
 * <p><b>Real, disclosed data quirk -- not a parsing bug</b>: 3 of the 481 lines repeat an item id
 * with a DIFFERENT weight (`16001`: 0.01 then later 0.1; `16075`: 10 then later 1; `16079`: 1.2
 * then later 1). All three first appearances are in the unlabeled section before `//melee` and
 * their second appearances are in the `//items` section. This is consistent with this project's
 * earlier finding (see `itemnames.dat`'s `internal.<categoryId>.<itemId>.<variant>` key format)
 * that the numeric item id is only guaranteed unique WITHIN a category -- `weight.txt`'s flat
 * `id-weight` format has no category field, so these 3 ids are genuinely ambiguous in the source
 * data itself. This class does not silently pick a "correct" value: {@link #weightOf(int)}
 * returns the LAST entry in file order (the simplest, most defensible default for an
 * override-style config, and consistent with how such flat text configs are conventionally
 * applied), but every id with more than one distinct value is also recorded in
 * {@link #conflictingIds()} so a caller -- or a mod author retuning this table -- knows not to
 * trust that id's weight blindly.
 */
public final class ItemWeightTable {

    private final double characterMaxWeight;
    private final Map<Integer, Double> weightById;
    private final Map<Integer, List<Double>> allValuesById;

    private ItemWeightTable(double characterMaxWeight, Map<Integer, Double> weightById, Map<Integer, List<Double>> allValuesById) {
        this.characterMaxWeight = characterMaxWeight;
        this.weightById = weightById;
        this.allValuesById = allValuesById;
    }

    public static ItemWeightTable load(Path weightTxtPath) throws IOException {
        List<String> lines = Files.readAllLines(weightTxtPath, StandardCharsets.UTF_8);

        Double characterMax = null;
        Map<Integer, Double> weightById = new LinkedHashMap<>();
        Map<Integer, List<Double>> allValuesById = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = stripTrailingCr(rawLine).trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            int dash = line.indexOf('-');
            if (dash <= 0 || dash == line.length() - 1) {
                throw new IOException(weightTxtPath + ": unparseable line: \"" + line + "\"");
            }
            String key = line.substring(0, dash);
            String valueText = line.substring(dash + 1);

            if ("character_max".equals(key)) {
                characterMax = Double.parseDouble(valueText);
                continue;
            }

            int itemId;
            try {
                itemId = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw new IOException(weightTxtPath + ": unrecognized key \"" + key + "\" in line: \"" + line + "\"");
            }
            double weight = Double.parseDouble(valueText);

            weightById.put(itemId, weight); // last occurrence wins, see class doc comment
            allValuesById.computeIfAbsent(itemId, k -> new ArrayList<>()).add(weight);
        }

        if (characterMax == null) {
            throw new IOException(weightTxtPath + ": missing required \"character_max-<n>\" line");
        }

        return new ItemWeightTable(characterMax, weightById, allValuesById);
    }

    private static String stripTrailingCr(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    /** The default max carry weight "without bonuses" (the file's own {@code character_max} line). */
    public double characterMaxWeight() {
        return characterMaxWeight;
    }

    /** This item's weight, or null if the table has no entry for it. If the id is a {@link #conflictingIds() conflict}, this is the LAST value in file order. */
    public Double weightOf(int itemId) {
        return weightById.get(itemId);
    }

    /** Every distinct value seen for this id, in file order (size 1 for every non-conflicting id, size 0 if the id isn't in the table at all). */
    public List<Double> allValuesOf(int itemId) {
        List<Double> values = allValuesById.get(itemId);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    /** Item ids that appear more than once in the source file with different weight values -- see the class doc comment. */
    public List<Integer> conflictingIds() {
        List<Integer> conflicts = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> e : allValuesById.entrySet()) {
            long distinct = e.getValue().stream().distinct().count();
            if (distinct > 1) {
                conflicts.add(e.getKey());
            }
        }
        Collections.sort(conflicts);
        return conflicts;
    }

    /** Number of distinct item ids in the table (not counting character_max). */
    public int size() {
        return weightById.size();
    }
}
