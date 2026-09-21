package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 49-line {@code seasonpass.*} namespace in {@code stalker/lang/en.lang} (part
 * 140, the twentieth concrete namespace built from part 123's follow-up census), modeled with the
 * established generic last-dot-split (id, field) table. Reconciled exactly: 12 real {@code
 * task.category.<id>.desc} + 9 {@code .completionTextInfo} + 6 {@code .conditionTextInfo} + 4
 * {@code title} + 4 {@code description} + 14 single-occurrence fields (the 12 real {@code
 * task.category.<id>} leaf LABELS themselves, plus {@code season_trader.name} and {@code
 * daily_bonus.description.summer25.1}) = 49.
 *
 * <p><b>The real {@code task.category.<categoryId>} double-use quirk.</b> Same shape already
 * handled cleanly (no special-casing needed) in part 137's {@link OrdersCatalog} {@code
 * map_marker.title}: {@code seasonpass.task.category.base}="BASIC OPEN WORLD ACTIVITIES" is
 * simultaneously a leaf VALUE (id={@code task.category}, field={@code base}) and a PREFIX for
 * {@code seasonpass.task.category.base.desc} (id={@code task.category.base}, field={@code desc}).
 * The generic last-dot split resolves both without conflict. All 12 real task categories: {@code
 * base}="BASIC OPEN WORLD ACTIVITIES", {@code worldevent}="CLEARING EVENTS", {@code
 * hard}="COMPLEX ACTIVITIES", {@code battlefields}="SESSION BATTLES", {@code clans}="SUBDIVISION
 * BATTLES", {@code event}="FROST TROOPERS", {@code wildnorth}="WILD LOCATIONS", {@code
 * operations}="OPERATIONS", {@code ny26}="X-MAS TREE", {@code insideout}="INSIDE OUT
 * PERCOLATION", {@code reichstag}="ASSAULT ON THE REICHSTAG", {@code ssop_help}="SSPP CONTRACTS".
 *
 * <p>Real cross-namespace corroborations, all disclosed not merged: {@code event}="FROST TROOPERS"
 * directly corroborates part 135's {@link DmzLangCatalog} {@code dmz2026.*} namespace and part
 * 138's {@link ChallengesCatalog} Javadoc, which already named "DMZ26/'Frost Troopers'" as a real
 * live-service event; {@code reichstag}/{@code reichstag.desc} are further uncounted entries in
 * the long part-109 Reichstag corroboration chain (also reconfirmed by {@link
 * StalkerLangCatalog}'s {@code stalker.gui.seasonpass.event.reichstag.industry.*} reward-UI find);
 * {@code ny26}="X-MAS TREE" ties to part 139's {@link ItemOriginsCatalog} {@code ny_tree_reward}
 * find. {@code operations.conditionTextInfo}="GET PHIL'S QUEST AT THE BAR" names a real NPC
 * "Phil" not previously catalogued anywhere in this project (checked via grep across every
 * existing {@code world.props} catalog) -- disclosed as a genuinely new find, not corroborated
 * elsewhere yet. {@code insideout.desc} and {@code clans.desc} are byte-identical real text
 * ("...involve activities between competing subdivisions. The list of assignments is updated
 * daily.") -- a real client copy-paste artifact, disclosed as-is and not corrected.
 *
 * <p>A real narrative finding: {@code event.title}="ATTACK ON PERIMETER" and its {@code
 * event.description} read as a climactic beat for the long-running SSPP/Murmur conflict thread
 * (parts 105/107/109/123/126/129/131/135) -- "The defense of the Perimeter is over. After
 * prolonged combat, the Murmur vanished as swiftly as appeared... a huge crater remained where the
 * military town once stood... No one knows what will happen next. Is this the end of the story or
 * a lull before something bigger?" -- flagged for future parts as a possible conclusion/pivot
 * point, not asserted as definitive since the client text itself leaves it open.
 *
 * <p>The real {@code bp.option.<id>.title}/{@code .description} group (3 ids: {@code base}="Get
 * the Season Pass" tier variants; {@code bonus50}/{@code bonus20}="+ 50/20 LEVELS") is the
 * Season-Pass-purchase-tier UI text. {@code season_trader.name}="Exchanger" is a real NPC/vendor
 * name tied to the Season Pass currency exchange flow. {@code
 * daily_bonus.description.summer25.1} is a real seasonal (summer25) daily-bonus tooltip, its
 * trailing literal digit {@code 1} landing as an ordinary field name under the generic scheme.
 */
public final class SeasonPassCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private SeasonPassCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "seasonpass.";

    public static SeasonPassCatalog load(LangCatalog catalog) {
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
        return new SeasonPassCatalog(fieldsById);
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
