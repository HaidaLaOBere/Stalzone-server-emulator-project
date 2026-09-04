package net.stalzone.emulator.session;

import net.stalzone.emulator.world.MilestoneMap;

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
 */
public final class PlayerState {

    public final String name;
    public String zoneName;
    public double x;
    public double y;
    public double z;
    public final Inventory inventory = new Inventory();

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
