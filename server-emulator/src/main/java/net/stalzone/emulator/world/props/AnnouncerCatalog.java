package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 175. Catalogs the real 67-line {@code bundle.notf.*} namespace in {@code
 * stalker/lang/en.lang} -- a real live-commentary/"Commentator" announcer-line system for the DMZ
 * raid modes, plus a real "Admin" announcer variant for an April Fools'-Day event. Found via a
 * fresh full-file top-level-prefix census while auditing the namespaces below {@code banner} (part
 * 174's closing note). Named {@code AnnouncerCatalog}, deliberately NOT {@code BundleCatalog} --
 * that name is already taken by the completely unrelated real {@code modassets/assets/bundle/}
 * FILE-asset catalog (part 78); this is real {@code en.lang} TEXT under the top-level {@code
 * bundle.} prefix, a coincidental name collision at the top-level-prefix layer only, checked and
 * confirmed distinct before naming this class.
 *
 * <p>Confirmed by direct count to split cleanly into two real shapes with no leftover lines (62 +
 * 5 = 67): {@code bundle.notf.<modeId>.annotator.<eventId>} (62 lines -- real per-event announcer
 * lines, {@code modeId} one of {@code dmz} (59 lines), {@code dmz2026} (1 line, just {@code
 * title}), {@code fools_day2026} (2 lines)) and {@code bundle.notf.dmz2026.<field>} (5 lines -- a
 * real, separate "FROST TROOPERS" operation-queue info block, NOT under {@code .annotator.}).
 *
 * <p><b>A real, disclosed naming quirk:</b> of the 59 real lines under {@code
 * bundle.notf.dmz.annotator.*}, only 30 use genuinely legacy-DMZ-flavored event ids ({@code
 * world_dmz_*}/{@code party_dmz_*}/{@code player_dmz_*}, plus the mode's own {@code title}); the
 * other 29 use {@code dmz2026}-flavored event ids ({@code world_dmz2026_*}/{@code
 * party_dmz2026_*}/{@code player_dmz2026_*}) despite living under the {@code dmz} mode prefix, not
 * a separate {@code dmz2026} mode prefix -- the real client author nested the 2026 raid's announcer
 * text inside the legacy mode's namespace rather than giving it its own, unlike the sibling {@code
 * bundle.notf.dmz2026.annotator.title} (1 line) and the non-annotator {@code
 * bundle.notf.dmz2026.*} info block (5 lines) which DO use the separate {@code dmz2026} prefix.
 * This class models the real data exactly as keyed -- both id families resolve under {@code
 * modeId="dmz"} -- rather than re-bucketing the 2026-flavored ids under a guessed {@code dmz2026}
 * grouping the client itself does not use.
 *
 * <p>Real content highlights: {@code dmz.annotator.title}={@code dmz2026.annotator.title}=
 * "Commentator" (the announcer's real on-screen role name); {@code
 * fools_day2026.annotator.title}="Admin" with {@code .session_intro}="Attention! The server rules
 * are being read" (a real April Fools'-Day admin-roleplay announcer variant, distinct from the
 * raid commentator). 7 real named boss-kill lines, previously uncatalogued by this project under
 * any name (checked -- no existing class names these): {@code boss_death_dochide}="Dr. Hyde
 * killed!", {@code _grenade}="Bomber killed!", {@code _infected}="Toxic is killed!", {@code
 * _objora}="Guzzler killed!", {@code _wizard}="Warlock is killed!", {@code _rhino}="Rhino killed!",
 * {@code _aid}="Hades is killed!". Several real event ids repeat byte-identical text across the
 * legacy/2026 id families despite the different ids (disclosed, not merged): {@code
 * world_dmz_evacuation_started}={@code world_dmz2026_auto_evacuation_started}="The last helicopter
 * leaves in a minute!"; {@code party_dmz_scenario_completed}={@code
 * party_dmz2026_scenario_completed}="Mission completed!"; {@code player_dmz_evacuated}={@code
 * player_dmz2026_evacuated}="Evacuation completed! See you next time!"; {@code
 * player_dmz_killed}={@code player_dmz2026_killed}="It's better to die on your feet than live on
 * your knees..." (a real philosophical death quote, kept for both id families). By contrast {@code
 * player_dmz_left_game}="A member quit the game" vs. {@code
 * player_dmz2026_left_game}="Participant left the game" is a real, genuinely DIFFERENT wording for
 * the same real event across the two families -- not a duplicate, disclosed separately.
 *
 * <p>The non-annotator {@code bundle.notf.dmz2026.*} info block's {@code title}="FROST TROOPERS"
 * is a THIRD independent real corroboration of this operation name, alongside part 135's {@link
 * DmzLangCatalog} ({@code dmz2026.*}) and part 140's {@link SeasonPassCatalog} ({@code
 * seasonpass.task.category.event}="FROST TROOPERS"). {@code info.start}/{@code .end}/{@code
 * .join}/{@code .leave} are real operation-queue lifecycle messages ("Access to the Frost Troopers
 * operation is granted"/"...is closed"/"Squad leader has joined the queue..."/"You have left the
 * queue...: {reason}").
 */
public final class AnnouncerCatalog {

    private static final String PREFIX = "bundle.notf.";
    private static final String ANNOTATOR_INFIX = ".annotator.";

    private final Map<String, Map<String, String>> announcerLineByModeThenEventId;
    private final Map<String, Map<String, String>> infoFieldByModeThenField;

    private AnnouncerCatalog(Map<String, Map<String, String>> announcerLineByModeThenEventId,
                              Map<String, Map<String, String>> infoFieldByModeThenField) {
        this.announcerLineByModeThenEventId = announcerLineByModeThenEventId;
        this.infoFieldByModeThenField = infoFieldByModeThenField;
    }

    public static AnnouncerCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> announcerLineByModeThenEventId = new LinkedHashMap<>();
        Map<String, Map<String, String>> infoFieldByModeThenField = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int infixIndex = rest.indexOf(ANNOTATOR_INFIX);
            if (infixIndex >= 0) {
                String modeId = rest.substring(0, infixIndex);
                String eventId = rest.substring(infixIndex + ANNOTATOR_INFIX.length());
                announcerLineByModeThenEventId.computeIfAbsent(modeId, k -> new LinkedHashMap<>())
                        .put(eventId, catalog.get(key));
            } else {
                int firstDot = rest.indexOf('.');
                if (firstDot < 0) {
                    continue;
                }
                String modeId = rest.substring(0, firstDot);
                String field = rest.substring(firstDot + 1);
                infoFieldByModeThenField.computeIfAbsent(modeId, k -> new LinkedHashMap<>())
                        .put(field, catalog.get(key));
            }
        }

        return new AnnouncerCatalog(announcerLineByModeThenEventId, infoFieldByModeThenField);
    }

    /** The real announcer line for this mode/event pair (e.g. {@code ("dmz", "world_dmz_airdrop")}), or null. */
    public String announcerLine(String modeId, String eventId) {
        Map<String, String> events = announcerLineByModeThenEventId.get(modeId);
        return events == null ? null : events.get(eventId);
    }

    /** Real mode ids that have at least one real announcer line ({@code dmz}, {@code dmz2026}, {@code fools_day2026}), sorted. */
    public List<String> announcerModeIds() {
        List<String> out = new ArrayList<>(announcerLineByModeThenEventId.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real event ids with an announcer line under this mode, sorted. */
    public List<String> announcerEventIds(String modeId) {
        Map<String, String> events = announcerLineByModeThenEventId.get(modeId);
        if (events == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>(events.keySet());
        Collections.sort(out);
        return out;
    }

    /** The real non-annotator info-block field for this mode (currently only real {@code dmz2026} data exists), or null. */
    public String infoField(String modeId, String field) {
        Map<String, String> fields = infoFieldByModeThenField.get(modeId);
        return fields == null ? null : fields.get(field);
    }

    /** Total real lines resolved across both shapes -- 67 against the real, full file. */
    public int size() {
        int total = 0;
        for (Map<String, String> m : announcerLineByModeThenEventId.values()) {
            total += m.size();
        }
        for (Map<String, String> m : infoFieldByModeThenField.values()) {
            total += m.size();
        }
        return total;
    }
}
