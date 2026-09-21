package net.stalzone.emulator.modding.events;

import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.session.PlayerState;

/**
 * Fired by {@code GameLoop.tickAbilityUse} (part 89) for every real ability-use attempt, whether
 * or not the ability was actually unlocked (see {@code unlocked}) -- {@code tickAbilityUse} itself
 * simulates no numeric effect, see {@link PrestigeAbility}'s own class doc for why. A plain final
 * class rather than a {@code record}, matching this project's own Java 11 compatibility rule (see
 * part 83's {@code Stream.toList()} fix and {@code loot/LootDrop}'s established style for a
 * simple, accessor-based data holder).
 */
public final class AbilityUseEvent {

    private final PlayerState player;
    private final PrestigeAbility ability;
    private final boolean unlocked;

    public AbilityUseEvent(PlayerState player, PrestigeAbility ability, boolean unlocked) {
        this.player = player;
        this.ability = ability;
        this.unlocked = unlocked;
    }

    public PlayerState player() {
        return player;
    }

    public PrestigeAbility ability() {
        return ability;
    }

    public boolean unlocked() {
        return unlocked;
    }

    @Override
    public String toString() {
        return "AbilityUseEvent{" + ability + " unlocked=" + unlocked + "}";
    }
}
