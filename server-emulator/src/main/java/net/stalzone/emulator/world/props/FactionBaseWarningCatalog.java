package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs five real, separate top-level {@code en.lang} prefixes (part 123's follow-up census
 * found these as the first concrete namespace built from that survey): {@code
 * merc.protected_areas.alert.notification.<n>}, {@code duty.*}, {@code freedom.*}, {@code
 * covenant.*}, {@code bandit.*} (5 real lines each, 25 total) -- each faction's own set of
 * in-character guard warning lines for trespassing near its base, indexed 0-4.
 *
 * <p>This is a DIFFERENT, richer real system from {@link StalkerLangCatalog}'s already-covered
 * {@code stalker.protected_areas.*} (part 119, 18 lines): that one is a single GENERIC
 * overlay/notification/owner system shared across 12 real protected areas regardless of which
 * faction controls them. These five standalone prefixes instead give each faction its own
 * distinct voice for the same "you're trespassing" beat -- confirmed by direct content
 * comparison, not assumed from the shared {@code protected_areas} substring in both key
 * families.
 *
 * <p>Four of the five faction ids corroborate {@link
 * net.stalzone.emulator.world.SettlementRegistry}'s part-105 real display-name crosswalk from a
 * second, independent angle: {@code merc}="Mercenary Camp" (guard line: "You're approaching the
 * Mercenary base, stranger"), {@code duty}="Frontier Base" (guard line: "Frontier base perimeter
 * breached"), {@code freedom}="Rise Base" (guard line: "You're not respecting Rise's
 * boundaries"), {@code covenant}="Covenant Abode" (guard lines lean hard into an ecclesiastical
 * register -- "Unbeliever! You are on Covenant land!", "The Lord sees all!", "Repent and leave
 * Covenant territory" -- consistent with {@link net.stalzone.emulator.world.props.CnpsCatalog}'s
 * part-107 finding of a real Covenant reputation ladder running Acolyte through Apostle). The
 * fifth, {@code bandit}, has NO entry in {@link
 * net.stalzone.emulator.world.SettlementRegistry#ALL_KNOWN_SETTLEMENTS} at all -- bandits get
 * their own real guard dialogue ("Oi, dumbass! You got the wrong base or something?") despite
 * apparently having no formal registered "settlement" of their own in this client build, a real
 * asymmetry disclosed as-is rather than assumed to be an oversight on this project's part.
 *
 * <p>Every one of the 25 real lines uses the exact same shape, so this class is modeled with one
 * flat {@code Map<String, List<String>>} keyed by faction id rather than five separate fields.
 */
public final class FactionBaseWarningCatalog {

    private final Map<String, List<String>> notificationsByFactionId;

    private FactionBaseWarningCatalog(Map<String, List<String>> notificationsByFactionId) {
        this.notificationsByFactionId = notificationsByFactionId;
    }

    /** The five real faction ids this project has confirmed carry this warning-line family. */
    public static final List<String> KNOWN_FACTION_IDS =
            List.of("merc", "duty", "freedom", "covenant", "bandit");

    private static final String MIDDLE = ".protected_areas.alert.notification.";

    public static FactionBaseWarningCatalog load(LangCatalog catalog) {
        Map<String, List<String>> notificationsByFactionId = new LinkedHashMap<>();
        for (String factionId : KNOWN_FACTION_IDS) {
            String prefix = factionId + MIDDLE;
            Map<Integer, String> byIndex = new TreeMap<>();
            for (String key : catalog.keysWithPrefix(prefix)) {
                int idx = Integer.parseInt(key.substring(prefix.length()));
                byIndex.put(idx, catalog.get(key));
            }
            if (!byIndex.isEmpty()) {
                notificationsByFactionId.put(factionId, new ArrayList<>(byIndex.values()));
            }
        }
        return new FactionBaseWarningCatalog(notificationsByFactionId);
    }

    /** The real ordered (index 0, 1, 2, ...) warning lines for this faction, or an empty list if unknown. */
    public List<String> notifications(String factionId) {
        List<String> lines = notificationsByFactionId.get(factionId);
        return lines == null ? List.of() : lines;
    }

    /** Real faction ids that actually have at least one warning line, sorted. */
    public List<String> factionIds() {
        List<String> out = new ArrayList<>(notificationsByFactionId.keySet());
        Collections.sort(out);
        return out;
    }
}
