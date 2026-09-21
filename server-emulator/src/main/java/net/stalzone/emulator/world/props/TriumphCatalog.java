package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 171. Catalogs a real, previously-uncatalogued 4-line namespace found by continuing the
 * fresh full-file {@code en.lang} re-scan (parts 167-170), and the last of the three open leads
 * part 168's own closing note originally flagged: {@code triumph.2026.*} -- real reward-mail text
 * for a "Triumphs" seasonal event. Confirmed via {@code grep -c "^triumph\."} against the full
 * real file: exactly 4 lines, no other real {@code triumph.*} content exists anywhere in the
 * client.
 *
 * <p>Real lines: {@code reward_type.reward_level_10}="STATUE", {@code
 * reward_type.reward_level_30}="CUSTOMIZATION" (two real reward-type names, keyed by what reads as
 * a real numeric completion-level threshold embedded in the key itself -- 10 and 30 -- not by a
 * separate id field), and {@code mail.message.reward.title}="Reward for completing the Triumphs:"
 * / {@code mail.message.reward.text}="Your reward for completing {count} Triumphs" (a real
 * templated in-game mail body, {@code {count}} being a real client-side substitution placeholder,
 * left verbatim rather than interpreted).
 *
 * <p>Real key depth is irregular across the two real sub-groups ({@code
 * reward_type.reward_level_<n>} is 2 real segments after the {@code triumph.2026.} prefix; {@code
 * mail.message.reward.<field>} is 3), so -- following this project's established precedent for
 * this exact situation ({@link TutorialGuideCatalog}, part 168) -- this class does NOT force the
 * generic last-dot-split {@code (id, field)} table onto it. Instead it exposes a flat {@code
 * Map<String,String>} keyed by the full real suffix string after {@code triumph.2026.}, plus two
 * small typed accessors for the two real sub-groups that are honest to their own real shape:
 * {@link #rewardTypeForLevel(int)} (parses the real embedded level number rather than requiring
 * the caller to reconstruct the real key string) and {@link #mailRewardTitle()}/{@link
 * #mailRewardText()} (fixed real single-instance fields, not a table -- there is only ever one
 * real mail-reward title/text pair in this namespace).
 *
 * <p><b>Confirmed real crosswalk, not yet previously written up in any class doc:</b> this
 * namespace's {@code 2026} year tag ties to {@link ItemLangCatalog}'s already-confirmed real
 * {@code item.customisation.collection.triumph26}="Triumphs. Faction Race 2026" cosmetic-collection
 * display name (part 121) -- both real strings independently name the same real "Triumphs" /
 * "Faction Race 2026" seasonal event, from two entirely separate real namespaces (reward-mail text
 * here vs. a cosmetic-collection display name there), corroborating each other rather than being
 * duplicates of the same source line.
 */
public final class TriumphCatalog {

    private static final String PREFIX = "triumph.2026.";
    private static final String REWARD_TYPE_LEVEL_PREFIX = "reward_type.reward_level_";
    private static final String MAIL_REWARD_TITLE_KEY = "mail.message.reward.title";
    private static final String MAIL_REWARD_TEXT_KEY = "mail.message.reward.text";

    private final Map<String, String> fields;

    private TriumphCatalog(Map<String, String> fields) {
        this.fields = fields;
    }

    public static TriumphCatalog load(LangCatalog catalog) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            fields.put(key.substring(PREFIX.length()), catalog.get(key));
        }
        return new TriumphCatalog(fields);
    }

    /** Real raw value for the exact real key suffix (e.g. {@code "mail.message.reward.title"}), or null if unknown. */
    public String field(String keySuffix) {
        return fields.get(keySuffix);
    }

    /** Real reward-type name (e.g. {@code "STATUE"}, {@code "CUSTOMIZATION"}) for the real numeric completion level embedded in the key (e.g. {@code 10}, {@code 30}), or null if this level has no real entry. */
    public String rewardTypeForLevel(int level) {
        return fields.get(REWARD_TYPE_LEVEL_PREFIX + level);
    }

    public String mailRewardTitle() {
        return fields.get(MAIL_REWARD_TITLE_KEY);
    }

    public String mailRewardText() {
        return fields.get(MAIL_REWARD_TEXT_KEY);
    }

    /** Every real key suffix present, sorted. */
    public List<String> keys() {
        List<String> out = new ArrayList<>(fields.keySet());
        Collections.sort(out);
        return out;
    }

    public int size() {
        return fields.size();
    }
}
