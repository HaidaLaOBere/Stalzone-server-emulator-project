package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 98-line {@code bpseason.*} namespace in {@code stalker/lang/en.lang} (part
 * 131, the tenth concrete namespace built from part 123's follow-up census) -- real Battle Pass
 * seasonal-event "how to play" mechanics text plus in-client promotional ad-card copy. Named
 * {@code BpSeasonCatalog} after the real literal prefix, distinct from {@link ShopCatalog}'s
 * already-covered Battle Pass BANNER/marketing timeline (part 115).
 *
 * <p>Confirmed by direct count to be a single real group with zero flat exceptions: all 98 lines
 * fit {@code bpseason.<id>.<field>} via a last-dot split (the same generic scheme already used by
 * {@link GuiLangCatalog}/{@link WorldEventCatalog}), even though several real ids embed a literal
 * {@code /} character (e.g. {@code 24/spring}, {@code 26/winter}) -- a genuine real structural
 * quirk, not normalized, already seen once before with {@code StalkerLangCatalog}'s (part 119)
 * {@code approv/mark/<type>} ids.
 *
 * <p>Splits into 3 real groups: {@code <year>/<season>.event.*} (58 lines across 7 real season
 * ids -- {@code 24/spring}, {@code 24/autumn}, {@code 25/winter}, {@code 25/spring}, {@code
 * 25/summer}, {@code 26/spring}, {@code 26/winter} -- each a real 4-block "how to play" mechanics
 * breakdown for that season's headline PvE/PvP event mode; only {@code 24/spring} additionally
 * carries a root {@code event.title}/{@code event.description}, the other 6 have only their 4
 * blocks), {@code ads.*} (37 lines, real in-client Battle Pass promotional ad-card copy: weapon/
 * style level-reward previews, a "SEASON PASS" upsell card, recurring-reward and item-pack
 * callouts -- one real line is disclosed as genuinely empty rather than missing, {@code
 * ads.ad_item_pack_1.description=}), {@code rewards.*} (3 lines, flat reward-category labels:
 * {@code motif}="Motif", {@code style}/{@code style_and_motif}="Style").
 *
 * <p>Two real cross-namespace finds worth disclosing rather than silently modeling away:
 * {@code 24/spring.event.title}/{@code .description} ("Arena of Slavs" / the "Max Crow returns to
 * the Zone..." blurb) are BYTE-IDENTICAL to {@code GuiLangCatalog}'s already-covered {@code
 * spring24.banner.title}/{@code .desc} (part 122) -- a real verbatim client duplication of the
 * same marketing copy across two differently-shaped namespaces ({@code 24/spring} vs {@code
 * spring24}), confirmed genuinely distinct where it matters: each namespace's own block-level
 * breakdown is unique (gui's {@code banner.block1-3} are marketing feature callouts -- "Case with
 * styles", "Season Styles", "Season Pass" -- while this class's {@code event.howtoplay.block1-4}
 * are the actual gameplay-mechanics explanation -- "Protect the Idol of Perun", "Buy equipment",
 * "Test your Strength", "Get Rewards"). Separately, {@code 25/summer}'s 4 real blocks name two
 * NPCs never seen before ("Kondratenko", "Zaharov") and describe choosing between "southern and
 * northern directions" with SSPP artillery/aircraft support -- a real independent corroboration of
 * part 129's {@code FrontlineCatalog} South/North campaign-direction structure from a completely
 * separate namespace. {@code 26/spring}'s 4 real blocks are explicitly Reichstag-mode "how to
 * play" text -- disclosed per the standing note as simply another entry in the long Reichstag
 * corroboration chain, no new ordinal asserted.
 */
public final class BpSeasonCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private BpSeasonCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "bpseason.";

    public static BpSeasonCatalog load(LangCatalog catalog) {
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
        return new BpSeasonCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real ids with at least one field, sorted (several embed a literal {@code /}). */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
