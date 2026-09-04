package net.stalzone.emulator.economy;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real barter-item icon textures under
 * {@code modassets/assets/customitems/textures/items/barter/} (part 53) -- 101 real
 * {@code .ol} files on this install, confirmed by direct directory listing, not just
 * client-log references. Most follow a real, consistent {@code <zone>.<category>[_add].ol}
 * naming pattern (e.g. {@code mg.node.ol}, {@code pd.signal_add.ol}), and the {@code <zone>}
 * token this project found here is what directly, independently corroborates the
 * years-open "mg"/"pd"/"td" real-but-unidentified {@code map_cache/5.0/} zone-code mystery
 * (parts 26/35/36/37): those three exact tokens appear here as barter-item zone prefixes
 * IN THE SAME LIST as already-confirmed real zones -- {@code armsklad}, {@code bar},
 * {@code bolota}, {@code kordon}, {@code les}, {@code svalka} -- placing mg/pd/td in the
 * identical reference class as known real zones, even though their full expanded names are
 * still not spelled out anywhere this project has found. Three zone-shaped prefixes new to
 * this project entirely also appear here -- {@code limansk}, {@code sever}, and
 * {@code blizzard} (the last with only a single real category, {@code npc}) -- but NONE of
 * the three is a real {@code map_cache/5.0/} folder name (checked, part 53), so unlike
 * mg/pd/td their status as an actual playable zone (vs. e.g. a cut/renamed/unused barter
 * category) is NOT confirmed, only that the name itself is real and on-disk.
 *
 * <p>Real category tokens found across the zone-scoped entries: {@code mining}, {@code mob},
 * {@code npc}, {@code node}, {@code signal}, {@code airdrop}, {@code boss}, {@code capture},
 * {@code fallout} -- most zones also have an {@code _add} variant of one or more categories
 * (a real, disclosed, unexplained naming suffix; this class exposes it as a distinct
 * category token rather than guessing at its meaning). Not every zone has every category.
 *
 * <p>4 real filenames are standalone, NOT zone-scoped: {@code battlefields.ol},
 * {@code ffa_token.ol}, {@code fractional_coupon.ol}, {@code triumph_coin.ol} -- these read
 * plausibly as generic PvP-mode/event currencies rather than per-zone collectibles, but
 * this class does not assert that, only that they don't fit the {@code <zone>.<category>.ol}
 * pattern. 11 more are bare internal numeric item ids (e.g. {@code 28703.ol}) with no
 * zone/category encoded in the filename at all -- see
 * {@link net.stalzone.emulator.items.ItemGroups}/{@code tools/item_lookup.py} for this
 * project's existing numeric-id tooling if resolving these further matters later.
 */
public final class BarterItemCatalog {

    private final Map<String, List<String>> zoneCategories;
    private final List<String> standaloneItemNames;
    private final List<String> numericIdItemNames;

    private BarterItemCatalog(
            Map<String, List<String>> zoneCategories,
            List<String> standaloneItemNames,
            List<String> numericIdItemNames) {
        this.zoneCategories = zoneCategories;
        this.standaloneItemNames = standaloneItemNames;
        this.numericIdItemNames = numericIdItemNames;
    }

    /** @param barterDir the real {@code .../textures/items/barter} folder. */
    public static BarterItemCatalog load(Path barterDir) throws IOException {
        Map<String, List<String>> zoneCategories = new TreeMap<>();
        List<String> standalone = new ArrayList<>();
        List<String> numericIds = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(barterDir, "*.ol")) {
            for (Path entry : entries) {
                String name = entry.getFileName().toString();
                String stem = name.substring(0, name.length() - ".ol".length());
                int dot = stem.indexOf('.');
                if (dot < 0) {
                    // No zone/category dot at all: either a standalone named item or a bare numeric id.
                    if (isAllDigits(stem)) {
                        numericIds.add(stem);
                    } else {
                        standalone.add(stem);
                    }
                } else {
                    String zone = stem.substring(0, dot);
                    String category = stem.substring(dot + 1);
                    // A stem can only have ONE dot in every real file found (part 53) -- a second
                    // dot would mean this isn't the <zone>.<category>.ol pattern this class assumes.
                    if (category.indexOf('.') >= 0) {
                        throw new IOException(entry + ": unexpected second '.' in barter item filename");
                    }
                    zoneCategories.computeIfAbsent(zone, z -> new ArrayList<>()).add(category);
                }
            }
        }

        for (List<String> categories : zoneCategories.values()) {
            Collections.sort(categories);
        }
        Collections.sort(standalone);
        Collections.sort(numericIds);

        if (zoneCategories.isEmpty() && standalone.isEmpty() && numericIds.isEmpty()) {
            throw new IOException(barterDir + ": no .ol barter item files found");
        }

        return new BarterItemCatalog(
                new LinkedHashMap<>(zoneCategories), // TreeMap already sorted; copy to a stable, ordered map
                Collections.unmodifiableList(standalone),
                Collections.unmodifiableList(numericIds));
    }

    private static boolean isAllDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Real zone-prefix tokens found in {@code <zone>.<category>.ol} filenames, alphabetical. */
    public List<String> zoneNames() {
        return Collections.unmodifiableList(new ArrayList<>(zoneCategories.keySet()));
    }

    /** Real category tokens for one real zone prefix (includes {@code _add} variants as distinct entries), or empty if unknown. */
    public List<String> categoriesFor(String zone) {
        return Collections.unmodifiableList(zoneCategories.getOrDefault(zone, List.of()));
    }

    /** Real standalone (non-zone-scoped, non-numeric-id) barter item names, e.g. {@code "ffa_token"}. */
    public List<String> standaloneItemNames() {
        return standaloneItemNames;
    }

    /** Real bare internal numeric item ids with no zone/category encoded in the filename, as strings. */
    public List<String> numericIdItemNames() {
        return numericIdItemNames;
    }

    public int zoneCount() {
        return zoneCategories.size();
    }

    public int totalFileCount() {
        int total = standaloneItemNames.size() + numericIdItemNames.size();
        for (List<String> categories : zoneCategories.values()) {
            total += categories.size();
        }
        return total;
    }
}
