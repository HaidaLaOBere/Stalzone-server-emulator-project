package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs two real tiny namespaces from {@code stalker/lang/en.lang} together (part 163, the
 * fifty-fourth/fifty-fifth concrete namespaces built from part 123's follow-up census, closing
 * out this project's "grab-bag" sweep of part 123's original census), kept as separate tables in
 * one class: real in-game mail templates and real anniversary bonus-code redemption messages --
 * both real player-facing message text delivered outside normal gameplay UI.
 *
 * <p><b>{@code mail.message.season_reward.&lt;outcome&gt;.&lt;role&gt;.&lt;field&gt;}</b> (6
 * lines) fits the generic last-dot-split (id, field) table, yielding 3 real ids ({@code
 * rewarded.member}, {@code rewarded.leader}, {@code no_reward.member}) each with {@code title}/
 * {@code text} fields. Real content: a clan-subdivision ranked-season results mail, referencing a
 * real, previously-uncatalogued in-fiction currency, "Crimson Casings" (via a {@code {sleeves}}
 * placeholder), plus {@code {clanName}}/{@code {rank}}/{@code {rating}}/{@code
 * {leaderboardPosition}} template placeholders -- real evidence of a clan/subdivision competitive
 * ranking system with its own seasonal rewards, distinct from (but thematically adjacent to) this
 * project's already-confirmed {@link ClanWarCatalog}.
 *
 * <p><b>{@code bonuscode.&lt;id&gt;.&lt;field&gt;}</b> (5 lines) fits the same generic table, 3
 * real ids: {@code dayx} (2 fields: {@code old}, {@code newbies}), {@code stalzone} (1 field:
 * {@code old}), {@code stalzone2026} (2 fields: {@code old}, {@code newbies}). These are real,
 * in-character anniversary/"project birthday" bonus-code redemption letters, signed by real
 * in-fiction dev personas ("Server Sprinkler", "ZIV", "Ziv"). <b>A significant real find for this
 * project's game-history scope (rule 2: game content and cut content are in scope, EXENS
 * launcher/anti-cheat internals are not -- this is public in-fiction dev commentary embedded in
 * game text, squarely the former):</b> {@code bonuscode.stalzone.old}'s real text explicitly
 * frames the game as "the transition to STALZONE" from something prior, and {@code
 * bonuscode.stalzone2026.newbies}'s real text states outright "you've probably learned one
 * thing... STALZONE turns twelve... TWELVE YEARS" and separately {@code
 * bonuscode.stalzone2026.old} thanks players "for all the time you've spent with us, first in
 * STALCRAFT and now in STALZONE" -- direct, first-party client-text confirmation that this game
 * is presented in-universe as a rebrand/successor of an existing, real, separately-named prior
 * project ("STALCRAFT"), with a real "July 10" project-anniversary date. Disclosed exactly as
 * authored, not further investigated or acted on beyond this documentation, per this project's
 * standing scope rules.
 */
public final class PlayerMessagingCatalog {

    private final Map<String, Map<String, String>> mailFieldsById;
    private final Map<String, Map<String, String>> bonusCodeFieldsById;

    private PlayerMessagingCatalog(Map<String, Map<String, String>> mailFieldsById, Map<String, Map<String, String>> bonusCodeFieldsById) {
        this.mailFieldsById = mailFieldsById;
        this.bonusCodeFieldsById = bonusCodeFieldsById;
    }

    private static final String MAIL_PREFIX = "mail.message.";
    private static final String BONUS_CODE_PREFIX = "bonuscode.";

    public static PlayerMessagingCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> mailFieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(MAIL_PREFIX)) {
            String rest = key.substring(MAIL_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            mailFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, Map<String, String>> bonusCodeFieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(BONUS_CODE_PREFIX)) {
            String rest = key.substring(BONUS_CODE_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            bonusCodeFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new PlayerMessagingCatalog(mailFieldsById, bonusCodeFieldsById);
    }

    public String mailField(String id, String field) {
        Map<String, String> fields = mailFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String bonusCodeField(String id, String field) {
        Map<String, String> fields = bonusCodeFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real mail ids with at least one field, sorted. */
    public List<String> mailIds() {
        List<String> out = new ArrayList<>(mailFieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real bonus code ids with at least one field, sorted. */
    public List<String> bonusCodeIds() {
        List<String> out = new ArrayList<>(bonusCodeFieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
