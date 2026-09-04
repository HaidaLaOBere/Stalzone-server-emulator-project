package net.stalzone.emulator.economy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads {@code modassets/assets/bundle/logged_trader_item_groups.txt} -- a real, plain-text,
 * CRLF-terminated list of 10 real item category names (part 51, no trailing newline after the
 * last entry), confirmed by direct inspection: {@code barter}, {@code cases}, {@code toys},
 * {@code weapons}, {@code armor}, {@code skins}, {@code backpacks}, {@code weapon_modules},
 * {@code signal_stuff}, {@code guitar}.
 *
 * <p>Real, VERIFIED (not assumed) relationship to {@link net.stalzone.emulator.items.ItemGroups}
 * (part 23's 41 real {@code item_groups.txt} categories): all 10 of these names are ALSO real
 * group names in {@code item_groups.txt} -- checked programmatically against that file's real
 * 41-name set while writing this class, not assumed. So this file's real content is consistent
 * with being a curated SUBSET of {@code item_groups.txt}'s categories (the filename,
 * {@code logged_trader_item_groups}, reads naturally as "which of the item-group categories a
 * trader/vendor transaction LOG tracks") -- a plausible reading of the name that this project's
 * own cross-check now actually supports (zero names here are absent from {@code item_groups.txt},
 * which a truly independent/unrelated category list would not guarantee), though per rule 1 this
 * class still does not assert the filename's exact semantic as fact, only as consistent with the
 * verified data. {@link #groupNames()} exposes exactly the 10 real strings in real file order;
 * this class deliberately does NOT try to cross-reference {@code item_groups.txt}'s id ranges
 * itself -- a caller wanting that can load both and compare {@link #groupNames()} against
 * {@code ItemGroups#groupNames()} directly.
 */
public final class TraderItemGroups {

    private final List<String> groupNames;

    private TraderItemGroups(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public static TraderItemGroups load(Path loggedTraderItemGroupsTxtPath) throws IOException {
        String text = Files.readString(loggedTraderItemGroupsTxtPath, StandardCharsets.UTF_8);
        List<String> groupNames = new ArrayList<>();
        for (String rawLine : text.split("\r\n|\n")) {
            String line = rawLine.trim();
            if (!line.isEmpty()) {
                groupNames.add(line);
            }
        }
        if (groupNames.isEmpty()) {
            throw new IOException(loggedTraderItemGroupsTxtPath + ": no group names found");
        }
        return new TraderItemGroups(groupNames);
    }

    /** All real trader-logged group names, in file order. */
    public List<String> groupNames() {
        return Collections.unmodifiableList(groupNames);
    }

    public int size() {
        return groupNames.size();
    }

    public boolean contains(String groupName) {
        return groupNames.contains(groupName);
    }
}
