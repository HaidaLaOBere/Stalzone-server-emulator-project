package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 64-line {@code orders.*} namespace in {@code stalker/lang/en.lang} (part
 * 137, the seventeenth concrete namespace built from part 123's follow-up census) -- a real
 * per-faction/DMZ squad-"order" objective system, confirmed distinct from part 133's {@link
 * PartyMissionsCatalog} (a different real co-op mission-tracker namespace, `party_missions.*`,
 * with no shared ids checked between the two).
 *
 * <p>Confirmed by direct count to be a single real group with zero flat exceptions: all 64 lines
 * fit {@code orders.<id>.<field>} via the established last-dot-split scheme (40 lines with one
 * dot in the remainder, 19 with two, 5 with three). Real ids split into 4 rough families:
 * per-faction repeating objectives (e.g. {@code destroy_ammunition_covenant_1}/{@code _2}/
 * {@code _3}, {@code visit_positions_covenant}/{@code _duty}/{@code _freedom}/{@code _merc}),
 * a real DMZ-tied objective set ({@code dmz_collect_objects_<n>}, {@code dmz_destroy_objects_
 * <n>}, {@code dmz_visit_positions_<n>}, {@code dmz_kill_player}), a real internal test/QA
 * objective set ({@code *_party_test}/{@code *_single_test} ids, left uninterpreted rather than
 * guessed at), and 4 real shared UI-label groups ({@code map_marker}, {@code objective}, {@code
 * reward}, {@code waypoint}).
 *
 * <p>A real structural quirk, not normalized: {@code orders.map_marker.title} is itself both a
 * real leaf VALUE ("A Mission from the Command") and a real prefix for a further-nested key,
 * {@code orders.map_marker.title.description} ("Follow the command's instructions") -- the same
 * quirk recurs at {@code orders.dmz_kill_player.source_map_marker.title}/{@code .title.
 * description}. Handled correctly without special-casing by this class's generic last-dot split,
 * since {@code map_marker.title} and {@code map_marker} are simply two different real ids in the
 * same table.
 */
public final class OrdersCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private OrdersCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "orders.";

    public static OrdersCatalog load(LangCatalog catalog) {
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
        return new OrdersCatalog(fieldsById);
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
