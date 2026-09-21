package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 12-line SINGULAR {@code season.<year>/<seasonName>.tabTitle}/{@code .title}
 * namespace in {@code stalker/lang/en.lang} (part 158, the thirty-ninth concrete namespace built
 * from part 123's follow-up census), named distinct from the already-covered PLURAL {@link
 * SeasonsCatalog} (part 145, a real month-by-month live-service event-name index) and from {@code
 * shop.season.*} (a separate real id space, not modeled here) -- both explicitly disclosed as
 * distinct real id spaces back in part 123's own census, confirmed disjoint by content: this
 * namespace names 6 real QUARTERLY thematic season titles rather than month labels. Real ids
 * embed a literal {@code /} character ({@code 25/spring}, {@code 26/winter}, etc.), handled by
 * the established generic last-dot-split (id, field) table without special-casing, the same
 * handling already used for part 152's {@link ChallengeCatalog} {@code wild_north/&lt;n&gt;} ids.
 *
 * <p><b>Real shape: 6 ids x 2 fields = 12.</b> The 6 real quarterly seasons present: {@code
 * 25/spring}="Back to the Inside Out", {@code 25/summer}="MURMUR OF THE ZONE", {@code
 * 25/autumn}="HORRORS OF THE ZONE", {@code 26/winter}="SUB-ZERO ZONE", {@code
 * 26/spring}="TEMPERED STEEL", {@code 26/summer}="ANTICYCLONE". For every one of the 6 real ids,
 * {@code .tabTitle} and {@code .title} carry byte-identical text -- two real, separately-named UI
 * text slots that happen to always agree in this snapshot, disclosed as an observed real
 * duplication rather than assumed to be structurally guaranteed.
 *
 * <p><b>Two real, independently-confirmed crosswalks.</b> {@code 25/spring}="Back to the Inside
 * Out" directly names the real real-world season (Spring 2025) for part 139's {@link
 * ItemOriginsCatalog}/part 154's {@link InsideCatalog} "Back to the Inside Out" ranked event --
 * previously only known by event name, now dated. {@code 26/winter}="SUB-ZERO ZONE" is an EXACT
 * real text match for part 121's {@link ItemLangCatalog} real {@code
 * item.customisation.collection.winter26}="Sub-Zero Zone" cosmetic-collection crosswalk --
 * independently confirming that collection id's real season.
 */
public final class SeasonCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private SeasonCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "season.";

    public static SeasonCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new SeasonCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
