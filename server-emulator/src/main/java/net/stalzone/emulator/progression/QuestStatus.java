package net.stalzone.emulator.progression;

/**
 * A real, evidenced per-quest lifecycle state, part 85 -- sourced from {@code
 * session/save/PlayerNotification}'s real {@code quests} channel (part 40), which a real {@code
 * sd2} save file's {@code notifications} list confirms carries a {@code type} field with 4 real
 * values: {@code new}, {@code quest_finished}, {@code quest_failed}, {@code mail_reward}.
 *
 * <p>Only the first three are modeled here -- {@code mail_reward} is deliberately excluded, since
 * {@code PlayerNotification}'s own class doc already establishes it isn't actually a quest
 * lifecycle state at all (it shares the {@code quests} channel but is a distinct reward-toast
 * purpose, and -- like {@code quest_failed} -- carries no real {@code QuestId} in any sample this
 * project has found).
 *
 * <p><b>{@code FAILED} is real but thin evidence</b>: {@code quest_failed} notification entries
 * are confirmed real (part 40), but none seen so far carry a {@code QuestId} field, so {@code
 * PlayerNotification.questId()} returns null for them -- there is no evidence source that
 * mechanically ties a given failure event to a specific quest id. The one real case this project
 * has cross-file evidence for (part 40): {@code config/quests.cfg}'s single {@code hiddenQuests}
 * entry (id 27091) is exactly the quest a real {@code sd2} sample shows cycling {@code
 * new}-&gt;{@code failed}-&gt;{@code new}-&gt;{@code failed}-&gt;...-&gt;{@code finished}, so a
 * caller WITH that kind of independent cross-reference can still record a real {@code FAILED}
 * transition against a specific id via {@code session/GameLoop#tickQuestEvent} -- this enum value
 * exists so that's possible, it just isn't derivable from a single notification payload alone.
 */
public enum QuestStatus {
    NEW,
    FINISHED,
    FAILED
}
