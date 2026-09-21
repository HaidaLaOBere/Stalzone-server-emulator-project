package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs three real tiny namespaces from {@code stalker/lang/en.lang} together (part 160, the
 * forty-first/forty-second/forty-third concrete namespaces built from part 123's follow-up
 * census, the first of several small "grab-bag" parts covering what remained of that census),
 * kept as separate tables in one class: real content-creator/account badge labels, footer help
 * links, and an in-game review prompt.
 *
 * <p><b>{@code profile_icon.&lt;id&gt;.text}</b> (10 lines, flat PREFIX/SUFFIX strip, one field
 * only) names 10 real social-media/community profile badges: {@code tg}="Telegram", {@code
 * youtube}="YouTuber", {@code admin}="Developer", {@code verified}="Verified Account", {@code
 * helper}="Developer" (a real duplicate of {@code admin}'s text), {@code vk}="VK Community",
 * {@code twitch}="Twitch Streamer", {@code tiktok}="TikTok Blogger", {@code chzzk}="TikTok
 * Blogger", {@code soop}="TikTok Blogger". The real {@code chzzk}/{@code soop} ids are two
 * DIFFERENT, real, dedicated Korean live-streaming platform brand names (distinct services, not
 * TikTok) that the client nonetheless labels with the identical generic "TikTok Blogger" text as
 * {@code tiktok} itself -- a real content oddity disclosed exactly as found, not corrected.
 *
 * <p><b>{@code menulinks.help.&lt;id&gt;}</b> (3 lines, flat PREFIX strip) names 3 real main-menu
 * footer links: {@code forum}="Forum", {@code support}="Support", {@code
 * credits}="About the developers".
 *
 * <p><b>{@code review.notf.&lt;field&gt;}</b> (2 lines, one real id) is a real post-session
 * review-prompt notification: {@code title}="How do you like the game?"/{@code
 * description}="Tell us what you think!".
 */
public final class AccountBadgeCatalog {

    private final Map<String, String> badgeTextById;
    private final Map<String, String> helpLinkById;
    private final Map<String, String> reviewPromptFields;

    private AccountBadgeCatalog(Map<String, String> badgeTextById, Map<String, String> helpLinkById,
            Map<String, String> reviewPromptFields) {
        this.badgeTextById = badgeTextById;
        this.helpLinkById = helpLinkById;
        this.reviewPromptFields = reviewPromptFields;
    }

    private static final String BADGE_PREFIX = "profile_icon.";
    private static final String BADGE_SUFFIX = ".text";
    private static final String HELP_LINK_PREFIX = "menulinks.help.";
    private static final String REVIEW_PREFIX = "review.notf.";

    public static AccountBadgeCatalog load(LangCatalog catalog) {
        Map<String, String> badgeTextById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(BADGE_PREFIX)) {
            if (!key.endsWith(BADGE_SUFFIX)) {
                continue;
            }
            String id = key.substring(BADGE_PREFIX.length(), key.length() - BADGE_SUFFIX.length());
            badgeTextById.put(id, catalog.get(key));
        }

        Map<String, String> helpLinkById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(HELP_LINK_PREFIX)) {
            String id = key.substring(HELP_LINK_PREFIX.length());
            helpLinkById.put(id, catalog.get(key));
        }

        Map<String, String> reviewPromptFields = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(REVIEW_PREFIX)) {
            String field = key.substring(REVIEW_PREFIX.length());
            reviewPromptFields.put(field, catalog.get(key));
        }

        return new AccountBadgeCatalog(badgeTextById, helpLinkById, reviewPromptFields);
    }

    /** A real profile badge's display text, e.g. {@code tg} -&gt; "Telegram". */
    public String badgeText(String id) {
        return badgeTextById.get(id);
    }

    /** A real footer help link's display text, e.g. {@code forum} -&gt; "Forum". */
    public String helpLink(String id) {
        return helpLinkById.get(id);
    }

    /** A real review-prompt field ({@code title}/{@code description}). */
    public String reviewPromptField(String field) {
        return reviewPromptFields.get(field);
    }

    /** Real badge ids, sorted. */
    public List<String> badgeIds() {
        List<String> out = new ArrayList<>(badgeTextById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real help-link ids, sorted. */
    public List<String> helpLinkIds() {
        List<String> out = new ArrayList<>(helpLinkById.keySet());
        Collections.sort(out);
        return out;
    }
}
