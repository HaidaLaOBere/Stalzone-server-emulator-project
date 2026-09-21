package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 18-line {@code industries.logistics.<tier>.benefits.supply_loot_upgrade.desc}
 * namespace in {@code stalker/lang/en.lang} (part 151, the thirty-first concrete namespace built
 * from part 123's follow-up census) -- a real settlement "Logistics" upgrade-tier benefit list,
 * every one of the 18 real lines ending in the identical literal {@code
 * .benefits.supply_loot_upgrade.desc} tail, modeled with the established flat PREFIX/SUFFIX
 * strip (same shape as {@link TileCatalog}/{@link DeliveryCatalog}), keyed by the real numeric
 * tier level itself (as a string id).
 *
 * <p>The real 18 tier levels present are NOT consecutive: {@code 2, 3, 4, 5, 6, 8, 10, 12, 14,
 * 15, 17, 19, 20, 21, 22, 23, 25, 30} -- real gaps at, e.g., 1, 7, 9, 11, 13, 16, 18, 24, and
 * 26-29, disclosed as-is (this project does not assume every intermediate tier exists just
 * because the surrounding ones do). Each real tier's text names a supply-drop loot upgrade for a
 * settlement's care-package/resupply system (e.g. {@code 6}="Morphine added", {@code
 * 12}="Epinephrine added", {@code 21}="First aid kits are now Guide's", {@code 30}="First aid
 * kits are now Emergency ones") -- real medicine/first-aid item names that corroborate this
 * project's existing loot/medicine item evidence (e.g. {@code Epinephrine}/{@code Morphine}
 * already appear in part 149's {@link IncidentCatalog} care-package text).
 */
public final class IndustriesCatalog {

    private final Map<String, String> descByTier;

    private IndustriesCatalog(Map<String, String> descByTier) {
        this.descByTier = descByTier;
    }

    private static final String PREFIX = "industries.logistics.";
    private static final String SUFFIX = ".benefits.supply_loot_upgrade.desc";

    public static IndustriesCatalog load(LangCatalog catalog) {
        Map<String, String> descByTier = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (!key.endsWith(SUFFIX)) {
                continue;
            }
            String tier = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
            descByTier.put(tier, catalog.get(key));
        }
        return new IndustriesCatalog(descByTier);
    }

    public String desc(String tier) {
        return descByTier.get(tier);
    }

    /** Real tier ids with a description, sorted lexicographically (not numerically). */
    public List<String> tiers() {
        List<String> out = new ArrayList<>(descByTier.keySet());
        Collections.sort(out);
        return out;
    }
}
