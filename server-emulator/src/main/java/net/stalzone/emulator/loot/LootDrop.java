package net.stalzone.emulator.loot;

/** One rolled outcome from {@link LootRoller#roll(LootTable)} -- an item id and a count. */
public final class LootDrop {

    private final String itemId;
    private final int count;

    public LootDrop(String itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public String itemId() {
        return itemId;
    }

    public int count() {
        return count;
    }

    @Override
    public String toString() {
        return "LootDrop{" + itemId + " x" + count + "}";
    }
}
