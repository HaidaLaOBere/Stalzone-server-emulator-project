package net.stalzone.emulator.session;

import net.stalzone.emulator.progression.PlayerClass;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.world.MilestoneMap;

import java.util.EnumSet;
import java.util.Set;

/**
 * Server-side state for one connected (or, for now, simulated) player: position, current zone,
 * and inventory. This is the piece every "Not yet done" note since part 18 has been pointing
 * at -- "there's still no player, no session ... no position tracking" -- and it is this
 * emulator's own design for holding that state, NOT a reverse-engineered copy of STALZONE's
 * real player-state representation (which is still unknown; the wire payload is encrypted, see
 * docs/RESEARCH_LOG.md part 7). Only the systems it delegates to for behavior
 * ({@link net.stalzone.emulator.items.ItemWeightTable} via {@link Inventory}, real map names via
 * {@link MilestoneMap}) are evidenced; the shape of this class itself is not a protocol claim.
 *
 * <p>Position is plain world-block coordinates in the same system {@code WorldModel} already
 * uses (part 18) -- (x, y, z) as doubles so sub-block movement can be simulated, with y as the
 * vertical (up) axis per {@code ChunkHeader}'s established 16-band-per-chunk convention.
 *
 * <p>As of part 82, {@link #wallet} holds this player's real, evidenced multi-currency balances
 * (part 27) the same way {@link #inventory} holds carried items -- see {@link Wallet}'s class doc.
 *
 * <p>As of part 83, {@link #playerClass} (nullable -- no evidence says a class must be selected at
 * creation, or ever) records which of {@link PlayerClass}'s real six evidenced class names this
 * player has chosen, and {@link #unlockedAbilities} tracks which of {@link PrestigeAbility}'s 11
 * real named abilities they have. Both are real, evidenced NAMES (part 66); see those two enums'
 * own class docs for exactly why neither comes with any simulated stat/effect behavior -- no
 * numeric class or ability data has been found anywhere in this project.
 *
 * <p>As of part 85, {@link #questLog} holds this player's real, evidenced quest-progress
 * bookkeeping (real quest ids, and real titles/lifecycle status once a real notification event
 * has been recorded for one) -- see {@link QuestLog}'s own class doc for the two real evidence
 * sources it combines and exactly what it deliberately does not simulate.
 */
public final class PlayerState {

    public final String name;
    public String zoneName;
    public double x;
    public double y;
    public double z;
    public final Inventory inventory = new Inventory();
    public final Wallet wallet = new Wallet();
    public PlayerClass playerClass; // null until the player selects one -- no evidence this is required
    public final Set<PrestigeAbility> unlockedAbilities = EnumSet.noneOf(PrestigeAbility.class);
    public final QuestLog questLog = new QuestLog();

    public PlayerState(String name, String zoneName, double x, double y, double z) {
        this.name = name;
        this.zoneName = zoneName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** True only if {@code zoneName} is one of the 46 real, confirmed zones (part 26). Doesn't imply the zone is currently loaded by this server. */
    public boolean isInKnownZone() {
        for (String known : MilestoneMap.ALL_KNOWN_MAPS) {
            if (known.equals(zoneName)) {
                return true;
            }
        }
        return false;
    }

    public double horizontalDistanceTo(double otherX, double otherZ) {
        double dx = x - otherX;
        double dz = z - otherZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public String toString() {
        return String.format("%s @ %s (%.1f, %.1f, %.1f)", name, zoneName, x, y, z);
    }
}
