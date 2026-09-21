package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 169. Catalogs a real, previously-uncatalogued 12-line namespace found by continuing the
 * fresh full-file {@code en.lang} re-scan (parts 167/168): {@code frontlineline.intro.<id>.<field>}
 * -- real multi-paragraph war-campaign narrative briefing text.
 *
 * <p><b>Genuinely distinct from the already-covered {@code frontline.*} (192 lines, part 129,
 * {@link FrontlineCatalog}), not a substring collision or a duplicate.</b> {@code
 * LangCatalog#keysWithPrefix("frontline.")} cannot accidentally match a real {@code
 * frontlineline.*} key -- the real character immediately after {@code frontline} in every one of
 * these 12 real keys is {@code l} (from the doubled "line"), never {@code .} -- confirmed by direct
 * count, not assumed from the names merely looking similar. Content-wise, both namespaces share
 * the same real SSPP-vs-Murmur war-campaign setting, but this namespace introduces three real named
 * characters ({@code Colonel Kondratenko}, {@code Major Makarov}, {@code Zaharov}) that do NOT
 * appear anywhere in {@link FrontlineCatalog}'s own already-covered briefing text -- confirmed by
 * direct search, not assumed -- so this is real NEW narrative content under a real, oddly-doubled
 * prefix spelling, not a re-shipped duplicate of the existing campaign briefings.
 *
 * <p>Real shape: {@code frontlineline.intro.<group>.<stage>.<paragraph>}, e.g. {@code
 * frontlineline.intro.second.stage1.par0}. Modeled with the established generic last-dot-split
 * {@code (id, field)} table after stripping the literal {@code frontlineline.intro.} prefix, giving
 * ids like {@code second.stage1} and fields like {@code par0}/{@code par1}/{@code par2}.
 *
 * <p><b>Real content, confirmed by direct count: 2 real groups ({@code second}, {@code final}) x 2
 * real stages ({@code stage1}, {@code stage2}) x 3 real paragraphs ({@code par0}-{@code par2}) =
 * 12, with zero leftover lines.</b> The real narrative: allied SSPP forces push Murmur forces back
 * past a "Railway Embankment" and hold a "Radar Station bunker" ({@code second.stage1}); a
 * "psy-suppressor" is lost and the "ZIVCAS Scientific Research Institute" works to restore a
 * defensive system while new operations are planned ({@code second.stage2} -- {@code ZIVCAS}
 * further corroborates part 92's already-confirmed real "ZIVCAS Scientific Research Institute",
 * from yet another independent angle); Major Makarov reports the enemy's main forces destroyed but
 * a "military town", "forward command post", and "gravitational beacon" lost, with Colonel
 * Kondratenko and Zaharov's fate unknown ({@code final.stage1}); the affected space "warped, folded
 * into a sphere, and simply... disappeared", the SSPP formally thanks the player and withdraws,
 * closing this real campaign arc ({@code final.stage2}).
 */
public final class FrontlineBriefingCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private FrontlineBriefingCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "frontlineline.intro.";

    public static FrontlineBriefingCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue; // no real line in this namespace has ever lacked a paragraph-field suffix
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new FrontlineBriefingCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String paragraph(String id, int paragraphIndex) {
        return field(id, "par" + paragraphIndex);
    }

    /** Real ids that have at least one real field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** All real paragraphs for {@code id}, in {@code par0, par1, par2, ...} order, stopping at the first missing index. Empty if the id is unknown. */
    public List<String> paragraphs(String id) {
        List<String> out = new ArrayList<>();
        for (int i = 0; ; i++) {
            String p = paragraph(id, i);
            if (p == null) {
                break;
            }
            out.add(p);
        }
        return out;
    }

    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }
}
