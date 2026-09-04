package net.stalzone.emulator.items;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads {@code modassets/assets/gloomycore/item_tiers.txt} -- a real, tiny, plain-text file
 * (part 51) confirmed by direct inspection to be exactly 4 CRLF-separated lines (no trailing
 * newline): {@code novice}, {@code stalker}, {@code veteran}, {@code master}, in that file order.
 *
 * <p><b>What is and isn't evidenced, per this project's rule 1</b>: the file, its exact content,
 * and its 4-entry, file-ordered shape are directly confirmed real data. What is NOT confirmed is
 * the precise mechanic these 4 names gate -- the filename says "item tiers", and the ordering
 * ({@code novice} first, {@code master} last) reads naturally as an ascending quality/rank
 * progression, consistent with these being the same rank-style vocabulary this project has
 * already seen used for STALKER-genre gear naming, but no other real client file examined by this
 * project (item_groups.txt, itemnames.dat, weapons.json, weight.txt) tags any specific item id
 * with one of these 4 names. So {@link #tiers()} exposes exactly the 4 real strings in their real
 * file order, and {@link #ordinalOf(String)} exposes that order as a 0-based index (useful for a
 * caller that wants to compare "is tier A higher than tier B" using the file's own ordering) --
 * neither invents a meaning for the names beyond what the file itself provides.
 */
public final class ItemTiers {

    private final List<String> tiers;

    private ItemTiers(List<String> tiers) {
        this.tiers = tiers;
    }

    public static ItemTiers load(Path itemTiersTxtPath) throws IOException {
        String text = Files.readString(itemTiersTxtPath, StandardCharsets.UTF_8);
        List<String> tiers = new ArrayList<>();
        for (String rawLine : text.split("\r\n|\n")) {
            String line = rawLine.trim();
            if (!line.isEmpty()) {
                tiers.add(line);
            }
        }
        if (tiers.isEmpty()) {
            throw new IOException(itemTiersTxtPath + ": no tier names found");
        }
        return new ItemTiers(tiers);
    }

    /** All real tier names, in file order (ascending, per the class doc's evidenced-vs-not discussion). */
    public List<String> tiers() {
        return Collections.unmodifiableList(tiers);
    }

    public int size() {
        return tiers.size();
    }

    /** 0-based position of {@code tierName} in file order, or -1 if it isn't one of the real 4 names. */
    public int ordinalOf(String tierName) {
        return tiers.indexOf(tierName);
    }
}
