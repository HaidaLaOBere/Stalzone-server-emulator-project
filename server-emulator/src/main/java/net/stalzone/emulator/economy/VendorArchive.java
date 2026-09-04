package net.stalzone.emulator.economy;

import net.stalzone.emulator.nbt.CompressionUtil;
import net.stalzone.emulator.nbt.NbtReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads {@code config/vendor_archive.dat} -- a real, per-account, zstd+NBT save file (same
 * format family as every other local save file this project has parsed, see {@code nbt/}) that
 * records every item this account has ever bought from a trader: which trader, what item, how
 * much it cost, and in what currency. {@link net.stalzone.emulator.nbt.CompressionUtil}'s own doc
 * comment already named this exact file as a real zstd-magic-bytes example before this class was
 * written -- this class is the first thing to actually load it end-to-end.
 *
 * <p><b>Real structure</b> (root compound, no name): {@code archives} (list, one per character on
 * this account) -> {@code username} (string) + {@code traders} (list) -> {@code traderId} (string)
 * + {@code items} (list) -> {@code stack} (compound: {@code id}/{@code Count_i}/{@code Damage}/
 * optional {@code tag}) + {@code currency} (string) + {@code price} (number).
 *
 * <p><b>New real content surfaced by this file, not found anywhere else in this project</b>:
 * this account has 3 characters -- {@code AveghiliaBoy}, {@code WolfZero}, {@code Buji}. 14 real
 * trader ids across them: {@code buyer_foton2}/{@code supplier_foton2}, {@code buyer_lodochnaya}/
 * {@code supplier_lodochnaya}, {@code buyer_merc}/{@code supplier_merc}, {@code buyer_rostok}/
 * {@code supplier_rostok}, {@code arsenal_assistant_trader}, {@code compensation_trader},
 * {@code trader_seasonal}, {@code trader_starievshik}. {@code foton2} was already a known real
 * zone (map_cache roster, part 15); {@code lodochnaya} ("boathouse", Russian) and {@code rostok}
 * (a real named location from the original S.T.A.L.K.E.R. series, distinct from any of the 46
 * zones in {@code map_cache/5.0/} as of part 26) are new named locations not found anywhere else
 * in this project -- worth a future map_cache/log cross-check, not attempted yet. {@code merc}
 * likely ties to the existing {@code base_merc}/{@code outpost_merc} zones, not confirmed.
 * {@code buyer_*}/{@code supplier_*} read as a buy-from-player/sell-to-player pair per location,
 * not two different NPCs. Three real currencies: {@code money} (2,574/2,603 real purchases),
 * {@code sc_birthday_21} (23, an event currency) and {@code season_pass_ticket_3} (6, battle-pass
 * currency) -- the first direct evidence of more than one currency in this project. Real prices
 * ranged 0-360,000 across 2,603 purchases of 825 distinct items; 188 were price 0 (not
 * investigated further -- plausibly {@code compensation_trader}/event-unlock freebies, not
 * confirmed which).
 *
 * <p><b>Real, disclosed tag-schema quirk</b>: an item stack's optional {@code tag} compound is
 * NOT just the {@code upgrade_bonus}/{@code u1}/{@code u2} keys seen in the first entries checked
 * -- a full sweep of all 877 real tag blocks in this account's archive also found
 * {@code repairs_amount}, {@code currentVisualDefault}, {@code currentMotifDefault} (11/11/4
 * occurrences, always {@code 0} in this data), and {@code stash_durability} (11 occurrences, real
 * varying values: 180/420/900). {@code u1}/{@code u2} (a two-long pair matching the standard
 * Minecraft NBT UUID-storage convention, not confirmed as the client's own name for it) are
 * present in 877/877 tag blocks whenever a tag exists at all; {@code upgrade_bonus} is present in
 * only 494/877 -- genuinely optional even within a tag, and every value found is exactly 0.0 (see
 * {@link VendorPurchase#upgradeBonus()}'s doc for what that honestly does and doesn't imply).
 * Because the real schema is this varied, {@link VendorPurchase#rawTag()} exposes the whole
 * parsed compound rather than this class inventing a fixed set of fields for a schema it hasn't
 * fully seen.
 *
 * <p>This is real historical evidence of prices actually paid, not an authoritative price list --
 * no canonical vendor price/stock config has been found anywhere in this project. Still useful as
 * a genuine economic-tuning signal (per-item observed price ranges) and, as above, as a new
 * source of real named locations and currencies.
 */
public final class VendorArchive {

    private final List<CharacterArchive> archives;

    private VendorArchive(List<CharacterArchive> archives) {
        this.archives = archives;
    }

    /** One character's full purchase history, grouped by trader in file order. */
    public static final class CharacterArchive {
        private final String username;
        private final Map<String, List<VendorPurchase>> purchasesByTrader;

        CharacterArchive(String username, Map<String, List<VendorPurchase>> purchasesByTrader) {
            this.username = username;
            this.purchasesByTrader = purchasesByTrader;
        }

        public String username() {
            return username;
        }

        public List<String> traderIds() {
            return new ArrayList<>(purchasesByTrader.keySet());
        }

        /** Purchases from one trader, in file order, or empty if this character never bought from that trader. */
        public List<VendorPurchase> purchasesFrom(String traderId) {
            return purchasesByTrader.getOrDefault(traderId, List.of());
        }

        /** Every purchase this character has made, across all traders, in file order (traders in file order, items in file order within each). */
        public List<VendorPurchase> allPurchases() {
            List<VendorPurchase> all = new ArrayList<>();
            for (List<VendorPurchase> perTrader : purchasesByTrader.values()) {
                all.addAll(perTrader);
            }
            return all;
        }
    }

    @SuppressWarnings("unchecked")
    public static VendorArchive load(Path vendorArchiveDatPath) throws IOException {
        byte[] raw = Files.readAllBytes(vendorArchiveDatPath);
        byte[] nbtBytes = CompressionUtil.decompressIfNeeded(raw);
        NbtReader.Root root = NbtReader.readRoot(nbtBytes);

        if (!(root.value instanceof Map)) {
            throw new IOException(vendorArchiveDatPath + ": root tag is not a compound");
        }
        Map<String, Object> rootMap = (Map<String, Object>) root.value;
        Object archivesObj = rootMap.get("archives");
        if (!(archivesObj instanceof List)) {
            throw new IOException(vendorArchiveDatPath + ": missing or non-list \"archives\" at root");
        }

        List<CharacterArchive> archives = new ArrayList<>();
        for (Object archiveObj : (List<Object>) archivesObj) {
            requireCompound(archiveObj, vendorArchiveDatPath, "archives[]");
            Map<String, Object> archiveMap = (Map<String, Object>) archiveObj;
            String username = requireString(archiveMap, "username", vendorArchiveDatPath);

            Object tradersObj = archiveMap.get("traders");
            if (!(tradersObj instanceof List)) {
                throw new IOException(vendorArchiveDatPath + ": archive for \"" + username
                        + "\" has missing or non-list \"traders\"");
            }

            Map<String, List<VendorPurchase>> purchasesByTrader = new LinkedHashMap<>();
            for (Object traderObj : (List<Object>) tradersObj) {
                requireCompound(traderObj, vendorArchiveDatPath, "traders[]");
                Map<String, Object> traderMap = (Map<String, Object>) traderObj;
                String traderId = requireString(traderMap, "traderId", vendorArchiveDatPath);

                Object itemsObj = traderMap.get("items");
                if (!(itemsObj instanceof List)) {
                    throw new IOException(vendorArchiveDatPath + ": trader \"" + traderId
                            + "\" (username \"" + username + "\") has missing or non-list \"items\"");
                }

                List<VendorPurchase> purchases = new ArrayList<>();
                for (Object itemObj : (List<Object>) itemsObj) {
                    requireCompound(itemObj, vendorArchiveDatPath, "items[]");
                    purchases.add(parsePurchase((Map<String, Object>) itemObj, vendorArchiveDatPath, username, traderId));
                }
                // Real data can list the same traderId more than once for one username (not observed
                // on this install, but not ruled out either) -- append rather than overwrite so no
                // purchase is silently dropped.
                purchasesByTrader.computeIfAbsent(traderId, k -> new ArrayList<>()).addAll(purchases);
            }

            archives.add(new CharacterArchive(username, purchasesByTrader));
        }

        return new VendorArchive(archives);
    }

    @SuppressWarnings("unchecked")
    private static VendorPurchase parsePurchase(Map<String, Object> itemMap, Path path, String username, String traderId) throws IOException {
        Object stackObj = itemMap.get("stack");
        if (!(stackObj instanceof Map)) {
            throw new IOException(path + ": item entry (username \"" + username + "\", trader \"" + traderId
                    + "\") has missing or non-compound \"stack\"");
        }
        Map<String, Object> stackMap = (Map<String, Object>) stackObj;

        int itemId = requireInt(stackMap, "id", path);
        int count = requireInt(stackMap, "Count_i", path);
        int damage = requireInt(stackMap, "Damage", path);

        Map<String, Object> tag = null;
        Object tagObj = stackMap.get("tag");
        if (tagObj != null) {
            if (!(tagObj instanceof Map)) {
                throw new IOException(path + ": stack for item id " + itemId + " has non-compound \"tag\"");
            }
            tag = (Map<String, Object>) tagObj;
        }

        String currency = requireString(itemMap, "currency", path);
        long price = requireLong(itemMap, "price", path);

        return new VendorPurchase(itemId, count, damage, currency, price, tag);
    }

    private static void requireCompound(Object o, Path path, String where) throws IOException {
        if (!(o instanceof Map)) {
            throw new IOException(path + ": expected a compound at " + where + ", got " + (o == null ? "null" : o.getClass()));
        }
    }

    private static String requireString(Map<String, Object> map, String key, Path path) throws IOException {
        Object v = map.get(key);
        if (!(v instanceof String)) {
            throw new IOException(path + ": missing or non-string \"" + key + "\" (got "
                    + (v == null ? "null" : v.getClass()) + ")");
        }
        return (String) v;
    }

    private static int requireInt(Map<String, Object> map, String key, Path path) throws IOException {
        Object v = map.get(key);
        if (!(v instanceof Number)) {
            throw new IOException(path + ": missing or non-numeric \"" + key + "\" (got "
                    + (v == null ? "null" : v.getClass()) + ")");
        }
        return ((Number) v).intValue();
    }

    private static long requireLong(Map<String, Object> map, String key, Path path) throws IOException {
        Object v = map.get(key);
        if (!(v instanceof Number)) {
            throw new IOException(path + ": missing or non-numeric \"" + key + "\" (got "
                    + (v == null ? "null" : v.getClass()) + ")");
        }
        return ((Number) v).longValue();
    }

    public List<CharacterArchive> archives() {
        return archives;
    }

    public List<String> usernames() {
        List<String> names = new ArrayList<>();
        for (CharacterArchive a : archives) names.add(a.username());
        return names;
    }

    /** The named character's archive, or null if this account has no character by that name. */
    public CharacterArchive archiveFor(String username) {
        for (CharacterArchive a : archives) {
            if (a.username().equals(username)) return a;
        }
        return null;
    }

    /** Every distinct currency string seen across every character/trader/item, in first-seen order. */
    public Set<String> currencies() {
        Set<String> out = new LinkedHashSet<>();
        for (CharacterArchive a : archives) {
            for (VendorPurchase p : a.allPurchases()) {
                out.add(p.currency());
            }
        }
        return out;
    }

    /** Every distinct traderId seen across every character, in first-seen order. */
    public Set<String> traderIds() {
        Set<String> out = new LinkedHashSet<>();
        for (CharacterArchive a : archives) {
            out.addAll(a.traderIds());
        }
        return out;
    }

    /** Total purchase-record count across every character and trader. */
    public int size() {
        int n = 0;
        for (CharacterArchive a : archives) {
            n += a.allPurchases().size();
        }
        return n;
    }

    /** Every price actually paid for this item id, across every character/trader, in file order. Empty if this item id was never bought. */
    public List<Long> pricesPaidFor(int itemId) {
        List<Long> out = new ArrayList<>();
        for (CharacterArchive a : archives) {
            for (VendorPurchase p : a.allPurchases()) {
                if (p.itemId() == itemId) {
                    out.add(p.price());
                }
            }
        }
        return out;
    }
}
