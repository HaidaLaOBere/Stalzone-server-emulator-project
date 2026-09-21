package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 29-line {@code seasons.<id>.title} namespace in {@code stalker/lang/en.lang}
 * (part 145, the twenty-fifth concrete namespace built from part 123's follow-up census) -- a
 * real month-by-month live-service event-name index, every one of the 29 real lines ending in
 * the literal {@code .title} tail, modeled with the established flat PREFIX/SUFFIX strip.
 *
 * <p>This is, as of the {@code en.lang} snapshot this class was built from, a near-continuous
 * real historical timeline running from {@code november2023}="November 2023" through {@code
 * august2026}="August 2026" -- distinct from (singular) {@code season.*} and from {@code
 * shop.season.*}, both separate real id spaces per part 123's own census disclosure, not modeled
 * here. Real formatting is inconsistent client-side, disclosed as-is, not corrected: most ids
 * carry a bare month+year ("January 2024"), a few carry only the bare month ({@code
 * june2024}="June"), some are real multi-month SPANS ({@code july-august2024}="July&#8211;August",
 * {@code december-january2024}="December-January", using either an en dash or a literal hyphen
 * inconsistently), and two real ids ({@code cw_chill_0}/{@code cw_chill_1}) share the literal
 * title "Off-Season" rather than any month name.
 */
public final class SeasonsCatalog {

    private final Map<String, String> titleById;

    private SeasonsCatalog(Map<String, String> titleById) {
        this.titleById = titleById;
    }

    private static final String PREFIX = "seasons.";
    private static final String SUFFIX = ".title";

    public static SeasonsCatalog load(LangCatalog catalog) {
        Map<String, String> titleById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (!key.endsWith(SUFFIX)) {
                continue;
            }
            String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
            titleById.put(id, catalog.get(key));
        }
        return new SeasonsCatalog(titleById);
    }

    public String title(String id) {
        return titleById.get(id);
    }

    /** Real ids with a title, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(titleById.keySet());
        Collections.sort(out);
        return out;
    }
}
