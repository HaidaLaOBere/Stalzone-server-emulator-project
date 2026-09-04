package net.stalzone.emulator.economy;

import java.util.Collections;
import java.util.Map;

/**
 * One purchase record from {@code config/vendor_archive.dat} -- an item this account actually
 * bought from a trader, with what it cost. See {@link VendorArchive} for the file format and how
 * this was validated.
 */
public final class VendorPurchase {

    private final int itemId;
    private final int count;
    private final int damage;
    private final String currency;
    private final long price;
    /** Raw parsed "tag" compound, or null if this stack had no tag at all (1,726/2,603 real entries didn't). */
    private final Map<String, Object> tag;

    VendorPurchase(int itemId, int count, int damage, String currency, long price, Map<String, Object> tag) {
        this.itemId = itemId;
        this.count = count;
        this.damage = damage;
        this.currency = currency;
        this.price = price;
        this.tag = tag;
    }

    /** The internal numeric item id (same id space as {@code itemnames.dat}/{@code weight.txt}/{@code item_groups.txt}). */
    public int itemId() {
        return itemId;
    }

    public int count() {
        return count;
    }

    public int damage() {
        return damage;
    }

    /** Real values seen: {@code "money"} (2,574/2,603), {@code "sc_birthday_21"} (23), {@code "season_pass_ticket_3"} (6). */
    public String currency() {
        return currency;
    }

    public long price() {
        return price;
    }

    public boolean hasTag() {
        return tag != null;
    }

    /**
     * The raw parsed tag compound (unmodifiable), or null if this stack had no tag. Exposed
     * generically rather than as fixed fields because the real data's tag schema is richer and
     * more varied than any one purchase shows -- see {@link VendorArchive}'s class doc for the
     * full set of keys found. Use this for anything beyond {@link #upgradeBonus()}/{@link
     * #uuidMost()}/{@link #uuidLeast()}, which are the only three keys common/understood enough
     * to warrant a typed accessor.
     */
    public Map<String, Object> rawTag() {
        return tag == null ? null : Collections.unmodifiableMap(tag);
    }

    /**
     * The item's upgrade-bonus value, or null if this stack's tag doesn't have one (494/877
     * real tagged stacks did; the other 383 tagged stacks simply lack the key). Every non-null
     * value found in this account's real archive is exactly 0.0 -- disclosed honestly rather
     * than assumed meaningful: this could mean no purchased item in this dataset has ever had a
     * nonzero upgrade applied at time of purchase, or that the value is set later during
     * gameplay rather than at purchase time. Not enough evidence to say which.
     */
    public Double upgradeBonus() {
        if (tag == null) return null;
        Object v = tag.get("upgrade_bonus");
        return v instanceof Number ? ((Number) v).doubleValue() : null;
    }

    /**
     * The high 64 bits of what reads as a per-stack UUID (paired with {@link #uuidLeast()}),
     * present in 877/877 real tag blocks whenever a tag exists at all -- i.e. unlike
     * upgrade_bonus, u1/u2 are effectively mandatory once a stack has a tag. Named "u1" in the
     * real data, not "uuidMost" -- this project doesn't know the client's own name for it, only
     * that the two-long-pair shape matches the standard Minecraft NBT UUID-storage convention
     * (UUIDMost/UUIDLeast), which is an inference, not confirmed from client source.
     */
    public Long uuidMost() {
        if (tag == null) return null;
        Object v = tag.get("u1");
        return v instanceof Number ? ((Number) v).longValue() : null;
    }

    /** The low 64 bits of the same per-stack identifier. See {@link #uuidMost()}. */
    public Long uuidLeast() {
        if (tag == null) return null;
        Object v = tag.get("u2");
        return v instanceof Number ? ((Number) v).longValue() : null;
    }
}
