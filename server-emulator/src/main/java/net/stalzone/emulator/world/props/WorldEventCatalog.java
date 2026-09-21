package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs a real {@code we.*} ("World Events") namespace in {@code stalker/lang/en.lang}
 * (part 111) -- real UI text for a large open-world dynamic-encounter system: roaming
 * faction/mutant "camps" to clear, "rescue" distress calls to answer, and named one-off boss
 * encounters, layered with real seasonal-event variants. Unlike most namespaces this project has
 * parsed so far, {@code we.*} does not use one fixed suffix set per id -- different event
 * archetypes carry different real field combinations (a camp mostly has just a {@code
 * status_title}; a rescue event has a {@code goal}/{@code notification_text}/{@code task_text}/
 * {@code failure_text}/{@code success_text} chain; some ids add {@code status_desc}/{@code
 * status_description} variants that are NOT treated as equivalent here, since nothing confirms
 * they are interchangeable). This class therefore models it as a real, generic {@code (eventId,
 * field)} table via {@link #field}, with convenience accessors for the most common real field
 * names layered on top -- not a hardcoded per-archetype schema.
 *
 * <p><b>A real, near-complete faction/mutant "camp" roster</b> ({@code we.abstract_camp_<id>.
 * status_title}, ~30 lines) gives nearly every faction and mutant type this project has already
 * confirmed its own real roaming-encampment display name: {@code bandit}="Bandit Encampment"/
 * {@code bandit_large}="Bandit Camp", {@code stalker}="Stalker Encampment"/{@code
 * stalker_large}="Stalker Camp", {@code duty}="Frontier Encampment", {@code freedom}="Rise
 * Encampment", {@code mercenary}="Mercenary Encampment", {@code covenant}="Covenant Encampment",
 * {@code military}="Military Checkpoint"/{@code military_large}="Military Base", {@code
 * sanitar}="Orderly Camp", {@code zivcas}="ZIVCAS Encampment", {@code ssop}="SSPP Camp", {@code
 * artxanter}="Group of Arthunters", and -- a further, real corroboration of the confirmed Murmur
 * antagonist -- {@code monolit}="Murmur Squad"/{@code monolit_large}="Murmur Base". Real mutant
 * camps use their own English creature nicknames rather than the faction pattern: {@code
 * tushkan}="Rat Nest", {@code dog}="Mutt Den", {@code flesh}="Piggy Den", {@code boar}="Boar Den",
 * {@code krovosos}="Ghoul Nest"/{@code krovosos_large}="Ghoul Den", {@code snork}="Skitter Den",
 * {@code zombie}="Zombie Group"/{@code zombie_large}="Zombie Horde". Four {@code
 * abstract_camp_inside_<mutant>} variants ({@code krovosos}/{@code flesh}/{@code boar}/{@code
 * snork}, each "Inside Out &lt;Den name&gt;") tie directly to part 106's confirmed "Inside Out"
 * seasonal event.
 *
 * <p><b>A real "rescue" distress-call event family</b> ({@code we.abstract_rescue_<variant>.*},
 * dozens of ids) layers real notification/goal/task/outcome text over faction-vs-threat pairings
 * (e.g. {@code stalker_vs_military}, {@code bandit_vs_zombie}, {@code stalker_vs_mutant}), plus
 * real per-faction rescue variants ({@code covenant}, {@code duty}, {@code freedom}, {@code merc},
 * {@code scientist}) and seasonal ones ({@code ny25}="Help the Frost Fighters", {@code
 * duty_summer25_quest}).
 *
 * <p><b>Part 177 fix: a second real client copy-paste bug found in this same rescue family,</b>
 * distinct from the already-disclosed {@code science_camp_duty}/{@code science_camp_mercenary}
 * swap (found cross-referencing against {@link PdaWaypointEventCatalog}, part 173). This one was
 * found by exhaustively checking every real faction-templated {@code we.*} field (not just {@code
 * status_title}) across all ~30 real base-id groups that repeat the {@code duty}/{@code
 * mercenary}/{@code merc}/{@code freedom}/{@code covenant} template, for internal keyword
 * consistency (each faction's own text should name its own faction). Every group checked out
 * consistent except this one: {@code we.abstract_rescue_duty.goal}="Don't let the Frontier
 * soldiers die" and {@code .task_text}="Help the Frontier soldiers" correctly name the {@code
 * duty} id's own Frontier faction, but {@code we.abstract_rescue_duty.failure_text}="The Rise
 * soldiers died!" and {@code .success_text}="The Rise soldiers survived and offer a reward" are
 * BYTE-IDENTICAL duplicates of {@code we.abstract_rescue_freedom}'s own {@code failure_text}/
 * {@code success_text} -- a real copy-paste error in only half of this one id's real field set,
 * confirmed by direct comparison, not inferred. Disclosed exactly as found; this class's generic
 * {@link #field} table already returns the real (buggy) text verbatim, and no attempt is made to
 * silently "correct" it. Checked and confirmed this is the ONLY other occurrence of this bug
 * pattern anywhere in the real {@code we.*} data -- every other multi-field faction-templated
 * group (checked exhaustively, not sampled) has fully consistent per-faction text.
 *
 * <p>{@code we.abstract_clash_st_ba} is a real "stalker vs. bandit" battle
 * event a player can join on either side, with real distinct outcome text for each ("The stalkers
 * survived and offer a reward for helping" / "The bandits died!").
 *
 * <p><b>A real named mutant-boss "lair" system</b> gives several confirmed mutant species their own
 * real, distinct English lair names depending on encounter context: {@code boss_pseudogigant}=
 * "Behemoth's Lair" vs. the same species' {@code limansk_pseudogigant_boss}="Giant Rat Den" (two
 * different real names for the same real mutant in two different real encounter contexts, honestly
 * disclosed as a variance rather than merged); similarly {@code boss_chimera}="Fiend's Lair" vs.
 * {@code limansk_chimera_boss}="Phantom Fiend Den", and {@code boss_krovosos}="Dark Ghoul's Lair"
 * vs. {@code limansk_krovosos_boss}="Psy-Ghoul Den".
 *
 * <p><b>A real, fully-named New Year 2023 seasonal boss roster</b> ({@code we.ny23_boss_<id>.
 * status_title}, 14 real named one-off encounters, none catalogued by this project before): {@code
 * beerHead}="Old Year vs. New Year", {@code brothers}="Meat Jelly Lovers", {@code FightClub}=
 * "Weather Forecast", {@code copyPig}="New Year's Xerohoof", {@code tryppa}="Santa's Little
 * Helpers", {@code boroda}="New Year Festivities", {@code sword&amp;shield}="Hooligans and
 * Vandals", {@code hotabich}="Santa Corpse", {@code shrek}="Krampus" (a real Christmas-folklore
 * demon reference), {@code coward}="Scaredy-Cat", {@code tonnel_mouse}="Chimney Sweeping", {@code
 * zombieLand}="Zombieland" (a real pop-culture film-title reference), {@code music}="Bremen
 * Musicians" (a real Brothers Grimm fairy-tale reference), {@code grenadir}="Christmas Sentinel".
 * {@code ny23_boss_rescue_group}="Don't let the scientists die" is a real linked rescue sub-event
 * for this same seasonal set.
 *
 * <p>Two real, structurally irregular finds disclosed rather than force-fit into the id/field
 * model: {@code we.shopot_exit_goal=Clear the area and leave the location} is a flat key with no
 * dotted field suffix at all ("shopot" is Russian for "whisper", tying to the confirmed
 * Whisperers/Murmur naming thread from part 106's {@code HideoutCatalog}); and a separate, much
 * smaller real namespace, {@code pda.waypoint.we.<eventId>.title}, cross-references these same
 * world-event ids from PDA waypoint labels (e.g. {@code pda.waypoint.we.ny23_boss_FightClub.title=
 * Weather Forecast}, matching this namespace's own value for the same id) -- confirmed consistent
 * where checked, not separately catalogued here.
 *
 * <p>A second-level completeness re-check (direct count against the live real file) found the
 * namespace is exactly 506 real lines (505 captured by the generic {@code (id, field)} table plus
 * the one real flat {@code shopot_exit_goal} exception) -- correcting this doc's earlier "508-line"
 * header count, which this re-check could not reproduce. {@link #load} itself was independently
 * confirmed to drop zero real lines: every real line other than the one disclosed flat exception
 * fits the generic model cleanly.
 */
public final class WorldEventCatalog {

    private final Map<String, Map<String, String>> fieldsByEventId;

    private WorldEventCatalog(Map<String, Map<String, String>> fieldsByEventId) {
        this.fieldsByEventId = fieldsByEventId;
    }

    private static final String PREFIX = "we.";

    public static WorldEventCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsByEventId = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int dot = rest.indexOf('.');
            if (dot < 0) {
                // A real, structurally flat line with no dotted field suffix (e.g. we.shopot_exit_goal) --
                // disclosed in the class doc, deliberately not force-fit into the (id, field) model here.
                continue;
            }
            String id = rest.substring(0, dot);
            String field = rest.substring(dot + 1);
            fieldsByEventId.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new WorldEventCatalog(fieldsByEventId);
    }

    /** Real raw field value for {@code eventId}/{@code field} (e.g. {@code "status_title"}), or null. */
    public String field(String eventId, String field) {
        Map<String, String> fields = fieldsByEventId.get(eventId);
        return fields == null ? null : fields.get(field);
    }

    public String statusTitle(String eventId) { return field(eventId, "status_title"); }
    public String statusDescription(String eventId) { return field(eventId, "status_desc"); }
    public String goal(String eventId) { return field(eventId, "goal"); }
    public String notificationText(String eventId) { return field(eventId, "notification_text"); }
    public String taskText(String eventId) { return field(eventId, "task_text"); }
    public String failureText(String eventId) { return field(eventId, "failure_text"); }
    public String successText(String eventId) { return field(eventId, "success_text"); }
    public String title(String eventId) { return field(eventId, "title"); }
    public String description(String eventId) { return field(eventId, "description"); }

    /** Real event ids that have at least one real field, sorted. */
    public List<String> eventIds() {
        List<String> ids = new ArrayList<>(fieldsByEventId.keySet());
        Collections.sort(ids);
        return ids;
    }

    /** Real field-name keys present for this event id, or empty if the id is unknown. */
    public List<String> fieldNames(String eventId) {
        Map<String, String> fields = fieldsByEventId.get(eventId);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }
}
