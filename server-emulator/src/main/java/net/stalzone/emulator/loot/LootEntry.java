package net.stalzone.emulator.loot;

/**
 * One possible drop inside a {@link LootTable} -- an item id, a count range, and a relative
 * selection weight. See {@link LootTable}'s class doc for why this whole package is emulator-
 * original design, not reverse-engineered data.
 */
public final class LootEntry {

    private final String itemId;
    private final int minCount;
    private final int maxCount;
    private final double weight;

    public LootEntry(String itemId, int minCount, int maxCount, double weight) {
        if (itemId == null || itemId.isEmpty()) {
            throw new IllegalArgumentException("itemId must be non-empty");
        }
        if (minCount < 1 || maxCount < minCount) {
            throw new IllegalArgumentException("invalid count range [" + minCount + "," + maxCount + "]");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive, got " + weight);
        }
        this.itemId = itemId;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.weight = weight;
    }

    public String itemId() {
        return itemId;
    }

    public int minCount() {
        return minCount;
    }

    public int maxCount() {
        return maxCount;
    }

    public double weight() {
        return weight;
    }

    @Override
    public String toString() {
        return "LootEntry{" + itemId + " x[" + minCount + "-" + maxCount + "] weight=" + weight + "}";
    }
}
