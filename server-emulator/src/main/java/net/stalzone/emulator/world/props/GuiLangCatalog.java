package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the remaining 211 lines of the real 402-line top-level {@code gui.*} namespace in
 * {@code stalker/lang/en.lang} (part 122) -- the {@code gui.operation.how_to_play} sub-namespace
 * (191 of the 402 lines) was already fully modeled by part 113's {@link OperationsCatalog}, and
 * this class deliberately excludes it rather than re-parsing it. Together with {@link
 * OperationsCatalog} this now closes the top-level {@code gui.*} prefix in full. (Not to be
 * confused with {@link StalkerLangCatalog}'s already-fully-covered {@code stalker.gui.*}, a
 * different real namespace under a different top-level prefix.)
 *
 * <p>A direct count established this remainder is a FOURTH namespace this project has found with
 * zero real flat exceptions: every one of the 211 lines fits {@code gui.<id>.<field>}, letting it
 * be modeled with the same single generic {@code (id, field)} table -- 74 distinct real ids across
 * 17 real event/season/mode groups, dominated by real marketing-banner text (`title`: 58 lines,
 * `desc`: 50, `headerLabel`/`actionText`: 9 each, `endTimeLabel`: 7, plus numbered
 * `title1`-`title4`/`desc1`-`desc4` sub-blocks for multi-feature banners).
 *
 * <p>13 of the 17 groups (`nytree`, `encaged`, `summer25_event`, `summer25`, `streamer_battle`,
 * `battlefields`, `autumn24_event`, `winter25`, `summer24`, `spring25`, `autumn24`, `spring24`,
 * `newyear24`) are real in-client marketing banners for seasons and events -- headline/description
 * text plus numbered feature call-out blocks (`block1.title`/`block1.desc`, etc.) and action
 * buttons (`event`/`season`/`patchnotes`). The season banners corroborate this project's existing
 * seasonal-event timeline (parts 109/115/121) with, in several cases, richer narrative copy than
 * those sources carried on their own (e.g. `summer25.banner.desc`'s full "MURMUR OF THE ZONE"
 * announcement text, `spring24.banner.title`="Arena of Slavs" with its own lengthy in-universe
 * flavor text). One real line is disclosed as-is despite being empty: {@code
 * spring25.banner.desc=} has no text at all in this client build -- a real, if odd, blank value,
 * not a missing key.
 *
 * <p>The remaining 4 groups are distinct real UI text pockets: {@code encaged.*} (a real "Encaged"
 * mode tutorial-card system -- class-select, mission, and enemy-warning cards, including a real
 * `hunter_desc.wh`="Can see survivors and equipment through walls" enemy-ability tag); {@code
 * pause.*} (a real pause-menu text grab-bag: a clan-season-rewards ruleset and a 4-tier arcade
 * rank ladder, Rookie/Soldier/Experienced/Specialist); {@code case.treasury.bonus.<tier>.*} (a real
 * 3-tier loot-case pity-system explanation -- legend/silver/bronze tiers, each with an explicit
 * drop-chance-increases-per-opening rule); {@code bureau.*} (real reward-type labels distinguishing
 * a unique numbered/effect-bearing weapon reward from a plain one).
 */
public final class GuiLangCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private GuiLangCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "gui.";
    private static final String ALREADY_COVERED_PREFIX = "gui.operation.how_to_play.";

    public static GuiLangCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.startsWith(ALREADY_COVERED_PREFIX)) {
                continue;
            }
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new GuiLangCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String title(String id) { return field(id, "title"); }
    public String desc(String id) { return field(id, "desc"); }

    /** Real ids that have at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
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
