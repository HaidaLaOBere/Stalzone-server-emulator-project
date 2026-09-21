package net.stalzone.emulator.session;

import net.stalzone.emulator.clientdata.QuestProgress;
import net.stalzone.emulator.progression.QuestStatus;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Part 85: one player's real, evidenced quest-progress bookkeeping -- the same standing as {@link
 * Wallet} for currency (real ids/structure, this emulator's own ledger design on top). Two real
 * evidence sources feed it, cross-file-validated against each other (part 40, restated on {@link
 * QuestProgress}'s own class doc): {@link QuestProgress} (the real, live-updated {@code
 * config/quests.cfg} -- which quest ids this account's client log currently lists as {@code
 * readQuests}/{@code hiddenQuests}, bare ids only, no title or lifecycle state) and {@code
 * session/save/PlayerNotification}'s real {@code quests} channel (a real {@code sd2} save file's
 * notification history -- real quest TITLES and real lifecycle {@code type} transitions for
 * individual quest ids, see {@link QuestStatus}'s class doc for exactly which of the 4 real
 * notification types are modeled here and why).
 *
 * <p>Deliberately does NOT interpret what "read" or "hidden" mean (neither is confirmed anywhere
 * in this project's evidence -- see {@link QuestProgress}'s own class doc), and does NOT simulate
 * any reward/consequence of a quest finishing (no reward-item/currency data has been found tied
 * to a quest completion anywhere in this project's evidence, only the notification toast itself).
 * This class only tracks and reports real ids, real titles when known, and real lifecycle status
 * when known -- same restraint as {@code progression/PrestigeAbility}'s "expose the roster, don't
 * invent behavior" standard.
 *
 * <p>As of part 87, {@link #markKnown}/{@link #markHidden} exist purely so {@link
 * PlayerStateSave} can restore this state across a {@code SingleplayerServer} restart -- they are
 * not a new evidence source, just persistence plumbing for the same fields above.
 */
public final class QuestLog {

    private final Set<Integer> knownQuestIds = new LinkedHashSet<>();
    private final Set<Integer> hiddenQuestIds = new LinkedHashSet<>();
    private final Map<Integer, String> titleByQuestId = new LinkedHashMap<>();
    private final Map<Integer, QuestStatus> statusByQuestId = new LinkedHashMap<>();

    /**
     * Seeds {@link #knownQuestIds()}/{@link #hiddenQuestIds()} from a real, loaded {@link
     * QuestProgress} (i.e. a real account's {@code config/quests.cfg}) -- the real cross-file
     * relationship part 40 established (every quest id {@code sd2} shows with real metadata also
     * appears in {@code readQuests}). Does not touch title/status maps -- {@code quests.cfg} has
     * no title or lifecycle data, only bare ids.
     */
    public void seedFrom(QuestProgress progress) {
        knownQuestIds.addAll(progress.readQuestIds());
        hiddenQuestIds.addAll(progress.hiddenQuestIds());
    }

    /**
     * Restore-only helper for {@link PlayerStateSave} (part 87) -- marks {@code questId} known
     * without recording any title/status, for restoring an id that was only ever known via a
     * prior {@link #seedFrom} call (i.e. it came from {@code quests.cfg} alone, never from a real
     * notification event). Not real evidence produced by this call itself, just persistence.
     */
    public void markKnown(int questId) {
        knownQuestIds.add(questId);
    }

    /**
     * Restore-only helper for {@link PlayerStateSave} (part 87) -- marks {@code questId} hidden.
     * Deliberately independent of {@link #markKnown}/{@link #knownQuestIds()}, same as {@link
     * #seedFrom} already treats the two real sets ({@code readQuests}/{@code hiddenQuests}) as
     * independent (a hidden id may or may not also be a known/read one, per real observed data,
     * part 40).
     */
    public void markHidden(int questId) {
        hiddenQuestIds.add(questId);
    }

    /** Records a real per-quest lifecycle transition (see {@link QuestStatus}'s class doc for which real notification types this models). Also marks the id known, and records the title if one is given (non-null, non-blank). */
    public void recordEvent(int questId, String questTitle, QuestStatus status) {
        knownQuestIds.add(questId);
        if (questTitle != null && !questTitle.isBlank()) {
            titleByQuestId.put(questId, questTitle);
        }
        statusByQuestId.put(questId, status);
    }

    public boolean isKnown(int questId) {
        return knownQuestIds.contains(questId);
    }

    public boolean isHidden(int questId) {
        return hiddenQuestIds.contains(questId);
    }

    /** Real title, if a {@link #recordEvent} call has ever supplied one for this id -- null otherwise ({@code quests.cfg} alone never supplies titles). */
    public String titleOf(int questId) {
        return titleByQuestId.get(questId);
    }

    /** Most recently recorded real lifecycle status for this id, or null if none has been recorded via {@link #recordEvent}. */
    public QuestStatus statusOf(int questId) {
        return statusByQuestId.get(questId);
    }

    public Set<Integer> knownQuestIds() {
        return new LinkedHashSet<>(knownQuestIds);
    }

    public Set<Integer> hiddenQuestIds() {
        return new LinkedHashSet<>(hiddenQuestIds);
    }
}
