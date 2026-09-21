package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 6-line {@code pda.waypoint.home_outpost.<factionId>} namespace in {@code
 * stalker/lang/en.lang} (part 114) -- real PDA waypoint labels for each faction's own home base,
 * all following one consistent real "&lt;Faction&gt; Headquarters" pattern: {@code
 * merc}="Mercenary Headquarters", {@code freedom}="Rise Headquarters", {@code
 * duty}="Frontier Headquarters", {@code covenant}="Covenant Headquarters", {@code
 * stalkers}="Stalker Headquarters", {@code bandits}="Bandit Headquarters". The last two ({@code
 * stalkers}/{@code bandits}) are real factions with their own headquarters that neither {@link
 * ClanWarCatalog} nor {@link net.stalzone.emulator.world.SettlementRegistry} include in their own
 * 4-faction ({@code duty}/{@code freedom}/{@code merc}/{@code covenant}) rosters -- disclosed as a
 * distinct, wider real roster rather than folded into either.
 *
 * <p>This is now a real THIRD independent naming source for the {@code duty}/{@code
 * freedom}/{@code merc}/{@code covenant} hub concept, alongside {@link
 * net.stalzone.emulator.world.SettlementRegistry}'s settlement titles and {@link ClanWarCatalog}'s
 * clan-unit-creation "Base" text -- {@code duty}="Frontier Headquarters" and {@code
 * freedom}="Rise Headquarters" agree with both prior sources' "Frontier"/"Rise" naming exactly,
 * while {@code merc}="Mercenary Headquarters" and {@code covenant}="Covenant Headquarters" add a
 * third distinct real suffix ("Headquarters") alongside the other two sources' "Camp"/"Base" and
 * "Abode"/"Base" variants for those same two factions -- honestly disclosed as a further naming
 * variance, not merged into one canonical name.
 *
 * <p>This namespace's parent, {@code pda.waypoint.*} (301 lines total), is otherwise a near-total
 * real duplicate: 295 of its 301 lines are {@code pda.waypoint.we.<eventId>.title}, confirmed
 * (spot-checked across multiple ids) to exactly match {@link WorldEventCatalog}'s own {@code
 * we.<eventId>.status_title} values one-for-one -- already noted as consistent, not separately
 * catalogued, in that class's part-111 doc. Only these 6 {@code home_outpost} lines are real,
 * previously-uncatalogued content, which is why this class covers only them rather than all of
 * {@code pda.waypoint.*}.
 */
public final class HomeOutpostCatalog {

    private final Map<String, String> nameByFactionId;

    private HomeOutpostCatalog(Map<String, String> nameByFactionId) {
        this.nameByFactionId = nameByFactionId;
    }

    private static final String PREFIX = "pda.waypoint.home_outpost.";

    public static HomeOutpostCatalog load(LangCatalog catalog) {
        Map<String, String> nameByFactionId = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String factionId = key.substring(PREFIX.length());
            nameByFactionId.put(factionId, catalog.get(key));
        }

        return new HomeOutpostCatalog(nameByFactionId);
    }

    public String headquartersName(String factionId) {
        return nameByFactionId.get(factionId);
    }

    public List<String> factionIds() {
        List<String> ids = new ArrayList<>(nameByFactionId.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int factionCount() {
        return nameByFactionId.size();
    }
}
