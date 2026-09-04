package net.stalzone.emulator.session;

import net.stalzone.emulator.items.ItemWeightTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A player's carried items, keyed by the client's internal numeric item id (the same id space
 * {@link ItemWeightTable}/{@code ItemGroups}/{@code itemnames.dat} all use -- see
 * {@code recon_findings.md}'s "Item ID systems" note). This is this emulator's own design for
 * holding that state server-side; STALZONE's real inventory wire representation is still
 * unknown (payloads are encrypted, see docs/RESEARCH_LOG.md part 7) -- nothing here claims to
 * match it.
 *
 * <p>The one piece of real, evidenced behavior this class enforces is the carry-weight cap:
 * {@link ItemWeightTable#characterMaxWeight()} is a real value read from
 * {@code modassets/assets/stalker/weight.txt} (50.0 on this install, part 21) and
 * {@link #totalWeight(ItemWeightTable)} sums each stack's weight from that same real table.
 * Item ids with no entry in the table (only 415 of the ~9,350+ ids {@code ItemGroups} covers
 * have a known weight, part 21/23) contribute 0 to the total -- an honest, disclosed gap, not a
 * silent assumption that unweighted items are weightless.
 */
public final class Inventory {

    private final Map<Integer, Integer> stacks = new LinkedHashMap<>();

    public void add(int itemId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive: " + count);
        }
        stacks.merge(itemId, count, Integer::sum);
    }

    /** Removes up to {@code count} of this item id; returns how many were actually removed (0 if the id isn't carried). */
    public int remove(int itemId, int count) {
        Integer have = stacks.get(itemId);
        if (have == null) {
            return 0;
        }
        int removed = Math.min(have, count);
        if (removed >= have) {
            stacks.remove(itemId);
        } else {
            stacks.put(itemId, have - removed);
        }
        return removed;
    }

    public int countOf(int itemId) {
        return stacks.getOrDefault(itemId, 0);
    }

    public List<Integer> itemIds() {
        return new ArrayList<>(stacks.keySet());
    }

    public int distinctStackCount() {
        return stacks.size();
    }

    /**
     * Real weight sum, in {@code weight.txt}'s own units, using {@code table.weightOf(id)} for
     * every carried stack (0 contribution for ids the real table doesn't cover -- see class doc).
     */
    public double totalWeight(ItemWeightTable table) {
        double total = 0;
        for (Map.Entry<Integer, Integer> e : stacks.entrySet()) {
            Double w = table.weightOf(e.getKey());
            if (w != null) {
                total += w * e.getValue();
            }
        }
        return total;
    }

    /** True if {@link #totalWeight} exceeds the real {@code character_max} cap from {@code weight.txt}. */
    public boolean isOverweight(ItemWeightTable table) {
        return totalWeight(table) > table.characterMaxWeight();
    }
}
