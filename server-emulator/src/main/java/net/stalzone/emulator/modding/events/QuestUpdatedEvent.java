package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.session.PlayerState;

/**
 * Fired by {@code GameLoop.tickQuestEvent} (part 89) for every real quest lifecycle update
 * recorded to a player's {@code QuestLog} (part 85) -- {@code tickQuestEvent} itself simulates no
 * reward. A plain final class rather than a {@code record}, matching this project's own Java 11
 * compatibility rule (see part 83's {@code Stream.toList()} fix and {@code loot/LootDrop}'s
 * established style for a simple, accessor-based data holder).
 */
public final class QuestUpdatedEvent {

    private final PlayerState player;
    private final int questId;
    private final String questTitle;
    private final QuestStatus status;

    public QuestUpdatedEvent(PlayerState player, int questId, String questTitle, QuestStatus status) {
        this.player = player;
        this.questId = questId;
        this.questTitle = questTitle;
        this.status = status;
    }

    public PlayerState player() {
        return player;
    }

    public int questId() {
        return questId;
    }

    public String questTitle() {
        return questTitle;
    }

    public QuestStatus status() {
        return status;
    }

    @Override
    public String toString() {
        return "QuestUpdatedEvent{" + questId + " '" + questTitle + "' " + status + "}";
    }
}
