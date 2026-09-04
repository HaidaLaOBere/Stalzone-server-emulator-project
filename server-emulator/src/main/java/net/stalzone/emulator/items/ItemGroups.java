package net.stalzone.emulator.items;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code modassets/assets/bundle/item_groups.txt} -- a real category map for the client's
 * internal numeric item id space (the same space as {@code itemnames.dat}, {@code weight.txt},
 * and {@code customnpcs/sorting_map.txt} -- see docs/RESEARCH_LOG.md's "Item ID systems"
 * section). Confirmed real format by direct inspection (part 23): CRLF-terminated lines
 * (final line has no trailing newline), each {@code groupName=token,token,...} where a token is
 * either a single id ({@code 16131}) or an inclusive range ({@code 20000-20999}). 41 real
 * groups on this install, naming real gameplay categories: {@code weapons} (20000-20999),
 * {@code melee_weapons}, {@code armor}, {@code ammo}, {@code artifacts}, {@code medicine},
 * {@code lootboxes}/{@code lootbox_keys}, {@code clan_stuff}, {@code barter}, and more.
 *
 * <p><b>Real, disclosed data quirk -- not a parsing bug</b>: 11 item ids belong to more than one
 * group on this install (e.g. id 16613 is in both {@code lootboxes} and {@code lootbox_keys};
 * several {@code weapons}/{@code upgrades}/{@code customitems_stuff}/{@code donate_protectors}
 * ids also appear in {@code barter}). This is consistent with {@code barter} being a curated
 * cross-cutting list of specifically tradeable items layered on top of the primary categories,
 * not a disjoint category of its own -- so {@link #groupsOf(int)} returns every matching group
 * rather than picking one.
 *
 * <p>Cross-validated (part 23) against {@code customnpcs/sorting_map.txt} (part 21/22's sibling
 * find): several of that file's id groupings land inside a single {@code item_groups.txt} range
 * (e.g. sorting_map.txt's first line, ids 16600-16607/16610, all fall inside this file's
 * {@code crates} group, 16600-16611) -- two independently-found real files agreeing on the same
 * category boundary.
 */
public final class ItemGroups {

    private final List<String> groupNames;
    private final Map<String, List<int[]>> rangesByGroup; // each int[] is {startInclusive, endInclusive}

    private ItemGroups(List<String> groupNames, Map<String, List<int[]>> rangesByGroup) {
        this.groupNames = groupNames;
        this.rangesByGroup = rangesByGroup;
    }

    public static ItemGroups load(Path itemGroupsTxtPath) throws IOException {
        List<String> lines = Files.readAllLines(itemGroupsTxtPath, StandardCharsets.UTF_8);

        List<String> groupNames = new ArrayList<>();
        Map<String, List<int[]>> rangesByGroup = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = stripTrailingCr(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                throw new IOException(itemGroupsTxtPath + ": unparseable line (no '='): \"" + line + "\"");
            }
            String name = line.substring(0, eq);
            String rest = line.substring(eq + 1);
            if (rangesByGroup.containsKey(name)) {
                throw new IOException(itemGroupsTxtPath + ": duplicate group name \"" + name + "\"");
            }

            List<int[]> ranges = new ArrayList<>();
            for (String token : rest.split(",")) {
                int dash = token.indexOf('-');
                int start, end;
                if (dash > 0) {
                    start = Integer.parseInt(token.substring(0, dash));
                    end = Integer.parseInt(token.substring(dash + 1));
                    if (start > end) {
                        throw new IOException(itemGroupsTxtPath + ": inverted range \"" + token
                                + "\" in group \"" + name + "\"");
                    }
                } else {
                    start = end = Integer.parseInt(token);
                }
                ranges.add(new int[] { start, end });
            }

            groupNames.add(name);
            rangesByGroup.put(name, ranges);
        }

        return new ItemGroups(groupNames, rangesByGroup);
    }

    private static String stripTrailingCr(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    /** All group names, in file order. */
    public List<String> groupNames() {
        return Collections.unmodifiableList(groupNames);
    }

    public int size() {
        return groupNames.size();
    }

    /** True if this item id falls inside any range/single-id entry of this group. False (not an error) if the group name is unknown. */
    public boolean contains(String group, int itemId) {
        List<int[]> ranges = rangesByGroup.get(group);
        if (ranges == null) {
            return false;
        }
        for (int[] r : ranges) {
            if (itemId >= r[0] && itemId <= r[1]) {
                return true;
            }
        }
        return false;
    }

    /** Every group this item id belongs to, in file order. Usually size 1, occasionally more -- see class doc comment. Empty if the id isn't in any group. */
    public List<String> groupsOf(int itemId) {
        List<String> matches = new ArrayList<>();
        for (String name : groupNames) {
            if (contains(name, itemId)) {
                matches.add(name);
            }
        }
        return matches;
    }

    /** This group's raw ranges (each a {startInclusive, endInclusive} pair; a single id is represented as {id, id}), in file order. Empty list if the group name is unknown. */
    public List<int[]> rangesOf(String group) {
        List<int[]> ranges = rangesByGroup.get(group);
        return ranges != null ? Collections.unmodifiableList(ranges) : Collections.emptyList();
    }
}
