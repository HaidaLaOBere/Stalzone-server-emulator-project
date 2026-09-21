package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 114-line {@code tile.*} namespace in {@code stalker/lang/en.lang} (part 130,
 * the ninth concrete namespace built from part 123's follow-up census) -- a real level-design
 * material/flooring-tile display-name table, keyed by a real numeric tile id (e.g. {@code
 * 3500}="Stones (Floor)", {@code 3501}="dull yellow tile", {@code 3503}="wall"). Confirmed by
 * direct count to be a single real flat shape with zero flat exceptions: all 114 lines fit {@code
 * tile.<numericId>.name}, one field only.
 *
 * <p>Real capitalization/formatting is inconsistent across entries (compare {@code
 * 3500}="Stones (Floor)" with {@code 3501}="dull yellow tile") -- disclosed as a genuine real
 * client inconsistency, not normalized by this class.
 */
public final class TileCatalog {

    private final Map<String, String> nameById;

    private TileCatalog(Map<String, String> nameById) {
        this.nameById = nameById;
    }

    private static final String PREFIX = "tile.";
    private static final String SUFFIX = ".name";

    public static TileCatalog load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.endsWith(SUFFIX)) {
                String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
                nameById.put(id, catalog.get(key));
            }
        }
        return new TileCatalog(nameById);
    }

    public String name(String id) { return nameById.get(id); }

    /** Real tile ids with a disclosed name, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(nameById.keySet());
        Collections.sort(out);
        return out;
    }
}
