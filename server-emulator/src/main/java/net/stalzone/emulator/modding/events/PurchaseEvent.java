package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.session.PlayerState;

/**
 * Fired by {@code GameLoop.tickPurchase} (part 89) for every real purchase attempt, both accepted
 * and rejected (see {@code accepted}) -- the same real currency ids/trader id/item id/price
 * {@code tickPurchase} itself already discloses as caller-supplied, not a canonical vendor price
 * list (part 82). A plain final class rather than a {@code record}, matching this project's own
 * Java 11 compatibility rule (see part 83's {@code Stream.toList()} fix and {@code
 * loot/LootDrop}'s established style for a simple, accessor-based data holder).
 */
public final class PurchaseEvent {

    private final PlayerState player;
    private final String traderId;
    private final int itemId;
    private final int count;
    private final String currency;
    private final long totalCost;
    private final boolean accepted;

    public PurchaseEvent(PlayerState player, String traderId, int itemId, int count,
                          String currency, long totalCost, boolean accepted) {
        this.player = player;
        this.traderId = traderId;
        this.itemId = itemId;
        this.count = count;
        this.currency = currency;
        this.totalCost = totalCost;
        this.accepted = accepted;
    }

    public PlayerState player() {
        return player;
    }

    public String traderId() {
        return traderId;
    }

    public int itemId() {
        return itemId;
    }

    public int count() {
        return count;
    }

    public String currency() {
        return currency;
    }

    public long totalCost() {
        return totalCost;
    }

    public boolean accepted() {
        return accepted;
    }

    @Override
    public String toString() {
        return "PurchaseEvent{" + traderId + " item=" + itemId + " x" + count
                + " " + totalCost + " " + currency + " accepted=" + accepted + "}";
    }
}
