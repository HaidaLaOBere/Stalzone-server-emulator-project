package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 202-line {@code checkpoints.*} namespace in {@code stalker/lang/en.lang}
 * (part 127, the fourth concrete namespace built from part 123's follow-up census) -- real
 * per-dungeon waypoint/checkpoint display names (entry points, numbered control points, boss-warp
 * points), used to label a dungeon's progress markers on the in-game map/PDA.
 *
 * <p>Confirmed by direct count to split into two real shapes, no leftover lines (196 + 6 = 202):
 * 196 lines fit {@code checkpoints.<dungeonId>.<pointId>}, modeled with a generic {@code (id,
 * field)} table (the {@code dungeonId} as id, the checkpoint slot -- {@code entry}, {@code
 * control}, {@code control_point}, {@code control_point_<n>} (n observed 1-14, non-contiguous in
 * several real dungeons, e.g. {@code dungeon_stels} jumps from {@code control_point_8} straight to
 * {@code control_point_12}), {@code control_warp}, {@code start} -- as field). 6 real lines are
 * flat exceptions with no second dot at all, always a dungeon id ending in a real literal {@code
 * _finish} or standing alone: {@code dungeon_enlightened}="Unknown", {@code
 * dungeon_zemlcap1_finish}="Dugout", {@code dungeon_sklad_frimena_finish}="Warehouse", {@code
 * dungeon_svetliy_stalkers_finish}="Light's Hideout", {@code
 * dungeon_svetliy_bandits_finish}="Light's Hideout" (two real dungeon variants sharing one finish
 * label), {@code dungeon_psisobaka_lobby_finish}="Unknown" -- same generic-table-plus-flat-
 * carve-out shape already established by {@link WorldEventCatalog} (part 111) and {@link
 * OperationsCatalog} (part 113), rather than the "zero flat exceptions" shape of several other
 * {@code *LangCatalog} classes.
 *
 * <p>The literal string "Unknown" is itself a real, disclosed-as-is client value for several
 * checkpoints (not a placeholder this project invented) -- several real dungeons ship with one or
 * more of their control points still unnamed in this client build (e.g. {@code
 * blizzard_boss}'s entire 6-point ladder is "Unknown" except for its id).
 */
public final class CheckpointsCatalog {

    private final Map<String, Map<String, String>> pointNameByDungeonId;
    private final Map<String, String> flatNameById;

    private CheckpointsCatalog(Map<String, Map<String, String>> pointNameByDungeonId,
                                Map<String, String> flatNameById) {
        this.pointNameByDungeonId = pointNameByDungeonId;
        this.flatNameById = flatNameById;
    }

    private static final String PREFIX = "checkpoints.";

    public static CheckpointsCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> pointNameByDungeonId = new LinkedHashMap<>();
        Map<String, String> flatNameById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                flatNameById.put(rest, catalog.get(key));
                continue;
            }
            String dungeonId = rest.substring(0, lastDot);
            String pointId = rest.substring(lastDot + 1);
            pointNameByDungeonId.computeIfAbsent(dungeonId, k -> new LinkedHashMap<>())
                    .put(pointId, catalog.get(key));
        }

        return new CheckpointsCatalog(pointNameByDungeonId, flatNameById);
    }

    /** This dungeon's real display name for this checkpoint slot (e.g. {@code "entry"}, {@code "control_point_3"}), or null. */
    public String pointName(String dungeonId, String pointId) {
        Map<String, String> points = pointNameByDungeonId.get(dungeonId);
        return points == null ? null : points.get(pointId);
    }

    /** Real checkpoint slot ids this dungeon has, sorted. */
    public List<String> pointIds(String dungeonId) {
        Map<String, String> points = pointNameByDungeonId.get(dungeonId);
        if (points == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>(points.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real dungeon ids with at least one modeled checkpoint slot, sorted. */
    public List<String> dungeonIds() {
        List<String> out = new ArrayList<>(pointNameByDungeonId.keySet());
        Collections.sort(out);
        return out;
    }

    /** The real flat (no-slot) display name for this dungeon id, e.g. a {@code _finish} variant, or null. */
    public String flatName(String id) { return flatNameById.get(id); }

    /** Real flat (no-slot) ids, sorted. */
    public List<String> flatIds() {
        List<String> out = new ArrayList<>(flatNameById.keySet());
        Collections.sort(out);
        return out;
    }
}
