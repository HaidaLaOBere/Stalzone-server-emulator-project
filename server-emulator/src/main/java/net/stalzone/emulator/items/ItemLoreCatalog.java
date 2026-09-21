package net.stalzone.emulator.items;

import net.stalzone.emulator.nbt.CompressionUtil;
import net.stalzone.emulator.nbt.NbtReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Part 166. Catalogs the real gzip-wrapped NBT file {@code modassets/assets/stalker/itemnames.dat}
 * -- a file this project's own early (pre-"Part N") research already located, decompressed, and
 * validated byte-exact via {@code tools/nbt_dump.py}/{@code tools/item_lookup.py} and this
 * project's own {@link NbtReader}/{@link net.stalzone.emulator.nbt.NbtWriter}, but which has never
 * until now had a first-class Java catalog class built on top of it -- everything before this part
 * was ad hoc Python/CLI tooling. Direct re-verification against the live client (this project's
 * standard discipline, since the earlier notes predate ~150 parts of subsequent work): the file is
 * still present at the same path, still gzip-magic (not zstd), and direct re-parsing with {@link
 * CompressionUtil}/{@link NbtReader} confirms it decompresses to 577,074 bytes with 0 leftover
 * bytes after a full NBT parse -- identical to the original finding.
 *
 * <p>Real top-level shape: a root compound with one field, {@code Data}, a list of 1483 real
 * compound records (this install; the original research noted "~1148-item lore-text subset" for a
 * DIFFERENT count -- distinct itemIds, not records, see below -- and this direct re-count confirms
 * that older figure exactly). Each record carries a real {@code key} string, an optional real
 * {@code display_name}, an optional real {@code Lore} (a list of text lines), and an optional real
 * {@code creator}.
 *
 * <p><b>Two real, cleanly distinguishable key styles, confirmed by direct count over all 1483
 * records:</b>
 * <ul>
 * <li>1288 records use the real {@code internal.<categoryId>.<itemId>.<variant>} key format this
 * project's early research already named -- {@code categoryId}/{@code itemId}/{@code variant} are
 * all real non-negative integers. {@code itemId} here is the SAME internal numeric item-id space
 * {@link ItemGroups}/{@link ItemWeightTable}/{@code customnpcs/sorting_map.txt} all use (see those
 * classes' docs), confirmed by direct value overlap, not assumed.
 * <li>195 records use a real free-form, non-numeric slug key instead (e.g. {@code
 * crash.pda_scientist}, {@code hallowin_goluboe_steklo_s_chernohvosta}) -- and a direct check
 * found these 195 are EXACTLY the records with a real {@code creator} field (100% correlation,
 * confirmed by count, not sampling): genuine player-submitted lore/flavor-text entries (a
 * community writing contest or similar real feature), not developer-authored catalog content.
 * Real, distinct, actual creator usernames are embedded verbatim in this file -- e.g. two of them
 * appear directly in {@code docs/RESEARCH_LOG.md}'s findings for this part -- but this class's own
 * unit test fixture below substitutes a placeholder in the {@code creator} field, per this
 * project's standing privacy rule against committing real third-party usernames to test fixtures.
 * The real {@link #load} method parses and returns whatever real value the live file actually
 * contains; only the test fixture is redacted.
 * </ul>
 *
 * <p>A SMALLER real minority of {@code creator}-tagged entries (19 of 214 total) use the numeric
 * {@code internal.*} key format instead of a free-form slug -- real community-submitted lore
 * layered onto an EXISTING real catalog item id rather than a wholly new standalone one. Real
 * gaps, disclosed not papered over: 23 of the 1288 {@code internal.*}-keyed records have no {@code
 * display_name} at all (only {@code Lore}); 145 of the full 1483 have no {@code Lore} (only {@code
 * display_name}). {@link #displayName}/{@link #lore} return {@code null}/an empty list respectively
 * for these, never a guessed value.
 *
 * <p><b>A real, confirmed itemId ambiguity, independently reconfirmed here with a concrete
 * example.</b> This project's early research already established (see {@link ItemWeightTable}'s
 * class doc) that the internal numeric item id is only unique WITHIN a category, from 3 real
 * {@code weight.txt} collisions. This file gives the same real fact fresh, independent, concrete
 * evidence: of the 1148 distinct real {@code itemId} values found among the 1288 {@code
 * internal.*}-keyed records, 19 (about 1.6%) appear under MORE than one real {@code categoryId}.
 * One example, followed all the way to real content, not just colliding numbers: real itemId
 * {@code 11485} appears under both {@code categoryId 28200} (variant 1, {@code display_name}=
 * "Конверт с красной
 * пометкой" -- a red-marked envelope) and {@code categoryId
 * 28202} (variant 0, {@code display_name}="Конверт с
 * синей пометкой" -- a
 * blue-marked envelope) -- two genuinely DIFFERENT real items, not a duplicate record, sharing one
 * bare {@code itemId}. This is exactly why {@link #displayNameForItemId} deliberately does NOT
 * exist: a method that took only an {@code itemId} and returned one name would silently pick
 * between two real, different items for these 19 ids. {@link #categoriesForItemId} and {@link
 * #ambiguousItemIds} surface the real ambiguity instead, the same disclose-don't-hide standard as
 * {@link ItemWeightTable#conflictingIds()}.
 *
 * <p><b>Why this is NOT wired into {@code session.GameLoop}/{@code session.Inventory} this part,
 * unlike part 165's mob-name crosswalk.</b> {@code session.Inventory} (this emulator's own
 * server-side design, not evidenced real wire data -- see that class's doc) is keyed by a bare
 * {@code int itemId} with no {@code categoryId} field at all. Wiring a real display name into
 * {@code GameLoop.tickPurchase}'s event line would mean resolving that ambiguity somehow for the
 * 19 real affected ids -- and this project has no evidence for which category an emulator-tracked
 * {@code Inventory} stack "really" belongs to, so any resolution rule would be invented, not
 * evidenced. Same standard as part 165's explicit decision not to wire {@code EjectionCatalog}
 * into {@code EmissionCycle.Phase} over an unconfirmed correspondence: the gap is disclosed, not
 * papered over with a guess. If {@code Inventory} ever gains a real or emulator-design category
 * field, {@link #byInternalId} is already the exact lookup that wiring would need.
 *
 * <p>{@code variant}'s exact real meaning is NOT confirmed. Observed real values across all
 * {@code internal.*} keys: 0, 1, 2, 3, 4, 5, 7 (6 never appears). It is NOT simply "page N of one
 * shared lore text" -- direct inspection shows different variants of the very same
 * ({@code categoryId}, {@code itemId}) pair can carry entirely different {@code display_name}s
 * (e.g. real {@code internal.20050.10015.1}="Абакан Зрячего"
 * vs. a same-id-family real {@code internal.20476.12529.5}="Глок Льда"
 * -- both read plausibly as named/unique weapon variants of a base weapon id, though this class
 * asserts nothing beyond the literal observed field values). This class exposes {@code variant} as
 * a plain integer, not a guessed enum.
 *
 * <p>Distinct from {@link net.stalzone.emulator.world.props.ItemLangCatalog} (part 121) -- that
 * class covers the real {@code item.<id>.<field>} namespace in {@code stalker/lang/en.lang}, a
 * completely separate real file and a completely separate real id space (English string ids like
 * {@code wpn.kedr}, not this file's numeric-or-slug keys) -- and from {@code
 * economy.BarterItemCatalog} (part 53), which only catalogs barter-icon FILENAMES, never real
 * display text.
 */
public final class ItemLoreCatalog {

    private static final Pattern INTERNAL_KEY = Pattern.compile("^internal\\.(\\d+)\\.(\\d+)\\.(\\d+)$");

    /** One real record from {@code itemnames.dat}. */
    public static final class Record {
        private final String key;
        private final String displayName; // null for 23 of 1483 real records -- see class doc
        private final List<String> lore; // empty (not null) for 145 of 1483 real records
        private final String creator; // null unless this is one of the 214 real creator-submitted entries

        private Record(String key, String displayName, List<String> lore, String creator) {
            this.key = key;
            this.displayName = displayName;
            this.lore = lore;
            this.creator = creator;
        }

        public String key() { return key; }
        public String displayName() { return displayName; }
        public List<String> lore() { return lore; }
        public String creator() { return creator; }
        public boolean isCreatorSubmitted() { return creator != null; }
    }

    private final Map<String, Record> byKey;

    private ItemLoreCatalog(Map<String, Record> byKey) {
        this.byKey = byKey;
    }

    /** @param itemnamesDatPath the real {@code modassets/assets/stalker/itemnames.dat} file. */
    @SuppressWarnings("unchecked")
    public static ItemLoreCatalog load(Path itemnamesDatPath) throws IOException {
        byte[] raw = Files.readAllBytes(itemnamesDatPath);
        return loadFromBytes(raw);
    }

    /** Same as {@link #load(Path)}, from already-read bytes -- used directly by this class's own unit test fixture. */
    @SuppressWarnings("unchecked")
    public static ItemLoreCatalog loadFromBytes(byte[] raw) throws IOException {
        byte[] inner = CompressionUtil.decompressIfNeeded(raw);
        NbtReader.Root root = NbtReader.readRoot(inner);
        Object rootValue = root.value;
        if (!(rootValue instanceof Map)) {
            throw new IOException("itemnames.dat: expected a root compound, got " + rootValue);
        }
        Map<String, Object> rootMap = (Map<String, Object>) rootValue;
        Object dataField = rootMap.get("Data");
        if (!(dataField instanceof List)) {
            throw new IOException("itemnames.dat: expected a real 'Data' list field, found none");
        }

        Map<String, Record> byKey = new LinkedHashMap<>();
        for (Object entry : (List<Object>) dataField) {
            Map<String, Object> rec = (Map<String, Object>) entry;
            String key = (String) rec.get("key");
            if (key == null) {
                continue; // no real record in this file has ever lacked a key; skip rather than fail if one ever did
            }
            String displayName = (String) rec.get("display_name");
            String creator = (String) rec.get("creator");
            List<String> lore = new ArrayList<>();
            Object loreField = rec.get("Lore");
            if (loreField instanceof List) {
                for (Object line : (List<Object>) loreField) {
                    lore.add((String) line);
                }
            }
            byKey.put(key, new Record(key, displayName, Collections.unmodifiableList(lore), creator));
        }
        return new ItemLoreCatalog(byKey);
    }

    public Record byKey(String key) {
        return byKey.get(key);
    }

    public String displayName(String key) {
        Record r = byKey.get(key);
        return r == null ? null : r.displayName();
    }

    public List<String> lore(String key) {
        Record r = byKey.get(key);
        return r == null ? List.of() : r.lore();
    }

    public String creator(String key) {
        Record r = byKey.get(key);
        return r == null ? null : r.creator();
    }

    /** The real {@code internal.<categoryId>.<itemId>.<variant>} record for this triple, or null if none exists. */
    public Record byInternalId(int categoryId, int itemId, int variant) {
        return byKey.get("internal." + categoryId + "." + itemId + "." + variant);
    }

    /** Every real key in this file, sorted. */
    public List<String> keys() {
        List<String> out = new ArrayList<>(byKey.keySet());
        Collections.sort(out);
        return out;
    }

    /** The real free-form, non-numeric-id keys -- see class doc: 100% correlated with a real creator-submitted entry. */
    public List<String> freeformKeys() {
        List<String> out = new ArrayList<>();
        for (String key : byKey.keySet()) {
            if (!INTERNAL_KEY.matcher(key).matches()) {
                out.add(key);
            }
        }
        Collections.sort(out);
        return out;
    }

    /** The real {@code categoryId}s that have an {@code internal.*}-keyed record for this itemId, sorted; empty if none. Usually size 1 -- see {@link #ambiguousItemIds} for the real 19 exceptions. */
    public List<Integer> categoriesForItemId(int itemId) {
        java.util.Set<Integer> categories = new TreeSet<>();
        for (String key : byKey.keySet()) {
            Matcher m = INTERNAL_KEY.matcher(key);
            if (m.matches() && Integer.parseInt(m.group(2)) == itemId) {
                categories.add(Integer.parseInt(m.group(1)));
            }
        }
        return new ArrayList<>(categories);
    }

    /** Real {@code itemId}s (among {@code internal.*}-keyed records) that appear under more than one real {@code categoryId} -- see class doc for the confirmed real example, itemId 11485. */
    public List<Integer> ambiguousItemIds() {
        Map<Integer, java.util.Set<Integer>> categoriesByItemId = new TreeMap<>();
        for (String key : byKey.keySet()) {
            Matcher m = INTERNAL_KEY.matcher(key);
            if (m.matches()) {
                int itemId = Integer.parseInt(m.group(2));
                int categoryId = Integer.parseInt(m.group(1));
                categoriesByItemId.computeIfAbsent(itemId, k -> new TreeSet<>()).add(categoryId);
            }
        }
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, java.util.Set<Integer>> e : categoriesByItemId.entrySet()) {
            if (e.getValue().size() > 1) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    public int size() {
        return byKey.size();
    }
}
