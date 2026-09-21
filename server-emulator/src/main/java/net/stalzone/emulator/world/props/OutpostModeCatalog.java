package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 14-line {@code outpost.*} namespace in {@code stalker/lang/en.lang} (part
 * 156, the thirty-sixth concrete namespace built from part 123's follow-up census), a real
 * capture-the-point PvP mode's UI text -- attacker/defender role descriptions, reward text, and
 * win/loss text. Named {@code OutpostModeCatalog} to disambiguate from the already-covered {@link
 * HomeOutpostCatalog} (part 114, real {@code pda.waypoint.home_outpost.&lt;factionId&gt;} PDA
 * waypoint labels for each faction's own home base) -- genuinely distinct real content, confirmed
 * via direct comparison: no shared keys, no overlapping id vocabulary.
 *
 * <p><b>Real shape, reconciled exactly: 11 + 3 = 14.</b> 11 real lines follow the established
 * generic last-dot-split (id, field) table: {@code attacker.desc}="Capture the outpost"/{@code
 * defender.desc}="Defend the outpost" (the two role objectives), {@code
 * attacker_status.desc}="Attack enemy positions"/{@code defender_status.desc}="Hold your ground"
 * (in-match HUD status text), {@code attacker_mail_reward.title}/{@code .desc} and {@code
 * defender_mail_reward.title}/{@code .desc} (real post-match mail-reward copy, both sides sharing
 * the identical real {@code .desc} text "Your share of the spoils from the outpost operation" --
 * disclosed as a real client copy-paste, not corrected), {@code
 * headquarters_spawn.name}="Headquarters" (the real capturable spawn point), and {@code
 * winner_text.title}/{@code loser_text.title} (real match-outcome banners). The remaining 3 real
 * lines have NO further dot after the {@code outpost.} prefix ({@code rewards_low_desc}/{@code
 * rewards_medium_desc}/{@code rewards_high_desc}, all three literally "for 1 outpost capture") and
 * are therefore modeled as a separate flat carve-out keyed by tier ({@code low}/{@code
 * medium}/{@code high}) via {@link #rewardDescByTier}, the same generic-table-plus-flat-carve-out
 * shape already used by part 143's {@link PremiumCatalog}.
 *
 * <p><b>A real, independently-confirmed corroboration.</b> {@link net.stalzone.emulator.world.
 * MilestoneMap}'s part-26 real map-roster expansion already confirmed a real, cached, visited
 * zone named {@code outpost_merc} (real {@code .mdat} region data on this account's install, not
 * just an asset reference) -- this standalone {@code outpost.*} namespace independently supplies
 * the actual real capture-the-point mode UI text for that exact real Mercenary-affiliated zone,
 * confirming it is a real playable PvP capture map rather than a static location, disclosed as a
 * further corroboration, not a new independent finding.
 */
public final class OutpostModeCatalog {

    private final Map<String, Map<String, String>> fieldsById;
    private final Map<String, String> rewardDescByTier;

    private OutpostModeCatalog(Map<String, Map<String, String>> fieldsById, Map<String, String> rewardDescByTier) {
        this.fieldsById = fieldsById;
        this.rewardDescByTier = rewardDescByTier;
    }

    private static final String PREFIX = "outpost.";
    private static final String REWARDS_PREFIX = "rewards_";
    private static final String REWARDS_SUFFIX = "_desc";

    public static OutpostModeCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        Map<String, String> rewardDescByTier = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            if (rest.startsWith(REWARDS_PREFIX) && rest.endsWith(REWARDS_SUFFIX) && !rest.contains(".")) {
                String tier = rest.substring(REWARDS_PREFIX.length(), rest.length() - REWARDS_SUFFIX.length());
                rewardDescByTier.put(tier, catalog.get(key));
                continue;
            }
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new OutpostModeCatalog(fieldsById, rewardDescByTier);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** The real reward description text for a tier ({@code low}/{@code medium}/{@code high}). */
    public String rewardDesc(String tier) {
        return rewardDescByTier.get(tier);
    }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real reward tiers with a description, sorted. */
    public List<String> rewardTiers() {
        List<String> out = new ArrayList<>(rewardDescByTier.keySet());
        Collections.sort(out);
        return out;
    }
}
