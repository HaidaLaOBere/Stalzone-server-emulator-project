package net.stalzone.emulator.session;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.hazards.DetectorConfig;
import net.stalzone.emulator.hazards.DetectorTier;
import net.stalzone.emulator.items.ItemWeightTable;
import net.stalzone.emulator.items.WeaponEntry;
import net.stalzone.emulator.items.WeaponRegistry;
import net.stalzone.emulator.loot.LootDrop;
import net.stalzone.emulator.loot.LootRoller;
import net.stalzone.emulator.loot.LootTables;
import net.stalzone.emulator.mobs.MobConfig;
import net.stalzone.emulator.mobs.MobHealthTable;
import net.stalzone.emulator.mobs.MobInstance;
import net.stalzone.emulator.mobs.SpawnScheduler;
import net.stalzone.emulator.movement.MovementSimulator;
import net.stalzone.emulator.world.WorldModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Ties this project's real, validated data layers together into an actual tick loop -- the
 * single most-repeated "Not yet done" line in this project since part 18
 * ("nothing ... constructs a WorldModel/WeaponRegistry/MobRegistry/... yet"). This class is what
 * finally constructs and calls into them together.
 *
 * <p>This is a LOCAL simulation loop, not a networked one: it does not speak STALZONE's real
 * wire protocol (still encrypted/unread, see docs/RESEARCH_LOG.md part 7, and gated behind
 * explicit user go-ahead per the project's standing rules). It drives one {@link PlayerState}
 * through simulated time using real per-tick data from {@link MovementSimulator} (real speed
 * factors), {@link WorldModel} (real cached terrain, used to keep the player's Y on the ground),
 * {@link DetectorConfig} (real hazard-beep timing), and {@link SpawnScheduler} (mob roster is
 * real; the spawn cadence itself is this emulator's own design -- see that class's doc for why).
 * Exactly matches the project's original ask: a singleplayer-capable server with "full control
 * over what can be done, like spawn rates or damage values" -- this is the loop those knobs now
 * actually reach. As of part 30, "damage values" was wired for flat (range-independent) hits:
 * {@link #tickCombat} lets a spawned {@link MobInstance} actually take real, evidenced
 * {@link WeaponRegistry} damage (see that method's doc, and {@link MobInstance}'s doc, for
 * exactly what is real data vs. this emulator's own necessary placeholder -- real .smm mob
 * configs have no health field at all). As of part 41, a distance-aware {@link #tickCombat}
 * overload also applies the real per-weapon range/damage-dropoff curve -- see that overload's doc
 * for exactly which part of that curve is now OFFICIALLY confirmed (part 39, EXBO's own public
 * docs) versus this project's own disclosed linear-interpolation assumption for the one remaining
 * undocumented segment. As of part 48, a mob kill can also roll loot via {@link #setLootTables}
 * -- {@code loot/LootRoller} finally wires up {@link ServerConfig#lootRateMultiplier}, the third
 * and last of the project's headline "spawn rates or damage values"-style tuning knobs, though
 * (unlike the other two) there is no real drop-rate data behind it to scale -- see {@code
 * loot/LootTable}'s class doc for why. As of part 49, {@link #tickSpawns} can also apply a
 * per-mob {@code maxHealth} override via {@link #setMobHealthTable} -- {@link MobHealthTable}
 * is the same kind of emulator-original opt-in tuning as the loot table, sitting on top of
 * {@link MobInstance#DEFAULT_MAX_HEALTH} rather than replacing it (a mob id with no override
 * still uses the flat default), since real .smm mob configs have no health field to override
 * with evidenced numbers in the first place.
 */
public final class GameLoop {

    private final WorldModel world;
    private final MovementSimulator movement;
    private final DetectorConfig detectors;
    private final SpawnScheduler spawner;
    private final ItemWeightTable weightTable;
    private final WeaponRegistry weapons;
    private final ServerConfig config;
    private final List<MobInstance> aliveMobs = new ArrayList<>();
    private final LootRoller lootRoller;
    private LootTables lootTables; // null until setLootTables() is called -- no loot rolls happen until an operator opts in
    private MobHealthTable healthTable; // null until setMobHealthTable() is called -- MobInstance.DEFAULT_MAX_HEALTH is used until then

    /** Fired once per tick with a human-readable event line; the CLI demo just prints these. */
    public interface EventSink {
        void onEvent(String line);
    }

    public GameLoop(WorldModel world, MovementSimulator movement, DetectorConfig detectors,
                     net.stalzone.emulator.mobs.MobRegistry mobRegistry, ServerConfig config, ItemWeightTable weightTable,
                     WeaponRegistry weapons) {
        this.world = world;
        this.movement = movement;
        this.detectors = detectors;
        this.spawner = new SpawnScheduler(mobRegistry, config, new Random(42)); // fixed seed: reproducible demo runs
        this.weightTable = weightTable;
        this.weapons = weapons;
        this.config = config;
        this.lootRoller = new LootRoller(config, new Random(99)); // fixed seed: reproducible demo runs, same pattern as spawner
    }

    /**
     * Opts this loop into rolling loot on mob kills (part 48) -- see {@code loot/LootTable}'s
     * class doc for why this is emulator-original functionality, not reverse-engineered data.
     * Loot rolling is a no-op (nothing to roll) until this is called.
     */
    public void setLootTables(LootTables lootTables) {
        this.lootTables = lootTables;
    }

    /**
     * Opts this loop into per-mob {@code maxHealth} overrides on spawn -- see
     * {@link MobHealthTable}'s class doc for why this is emulator-original tuning, not
     * reverse-engineered data. Until this is called, every spawned {@link MobInstance} uses
     * the flat {@link MobInstance#DEFAULT_MAX_HEALTH}, same as before this method existed.
     */
    public void setMobHealthTable(MobHealthTable healthTable) {
        this.healthTable = healthTable;
    }

    /**
     * Advances {@code player} by {@code deltaSeconds} of simulated time, moving them
     * {@code deltaSeconds * blocksPerSecond(mode)} blocks toward (targetX, targetZ) at the mode's
     * real speed, snapping Y to the real cached ground height ({@link WorldModel#highestSolidY})
     * when the column is loaded (falls back to holding Y steady when it isn't -- an honest
     * "unknown terrain" case, same limitation {@code WorldModel} already discloses).
     */
    public void tickMovement(PlayerState player, double targetX, double targetZ,
                              MovementSimulator.MoveMode mode, double deltaSeconds, EventSink sink) {
        double dx = targetX - player.x;
        double dz = targetZ - player.z;
        double remaining = Math.sqrt(dx * dx + dz * dz);
        double maxStep = movement.blocksPerSecond(mode) * deltaSeconds;
        double step = Math.min(remaining, maxStep);

        if (remaining > 1e-9) {
            player.x += dx / remaining * step;
            player.z += dz / remaining * step;
        }

        int groundY = world.highestSolidY((int) Math.round(player.x), (int) Math.round(player.z));
        if (groundY >= 0) {
            player.y = groundY + 1; // stand on top of the highest solid block, not inside it
        }
        // else: column not loaded/cached -- honestly leave Y as-is rather than guessing (WorldModel's own disclosed limitation).
        // Real, observed side effect (part 29 demo run): in a cave/dungeon zone, "highest solid
        // block" is sometimes a cave ceiling/overhang rather than a walkable floor -- WorldModel
        // has no floor-vs-ceiling concept (part 18's own doc already discloses this), so Y can
        // jump sharply between ticks near overhangs. Not a bug here; a genuine limitation this
        // class inherits and does not try to paper over.

        if (sink != null) {
            sink.onEvent(String.format("move[%s]: %s (moved %.2f blocks, %.1f blocks/s)", mode, player, step, movement.blocksPerSecond(mode)));
        }
    }

    /**
     * Checks {@code player}'s real inventory weight against the real cap and reports it. Doesn't
     * block or drop items -- that policy (what happens when overweight) isn't evidenced from any
     * source, so this only surfaces the fact, same "disclose don't hide" standard as
     * {@code ItemWeightTable.conflictingIds()}.
     */
    public void tickInventoryCheck(PlayerState player, EventSink sink) {
        double weight = player.inventory.totalWeight(weightTable);
        boolean over = weight > weightTable.characterMaxWeight();
        if (sink != null) {
            sink.onEvent(String.format("inventory: %.2f / %.1f weight (%d stacks)%s",
                    weight, weightTable.characterMaxWeight(), player.inventory.distinctStackCount(),
                    over ? " -- OVERWEIGHT" : ""));
        }
    }

    /**
     * Checks {@code player}'s distance to a hazard source and, if within any real detector tier's
     * range, reports the tier's real beep timing. {@code hazardX}/{@code hazardZ}/{@code hazardType}
     * are caller-supplied -- this project has never found real anomaly/hazard PLACEMENT data
     * (coordinates) anywhere, only the detector's own response-to-distance behavior (part 22), so
     * the hazard's location here is necessarily a demo scenario, not extracted real data.
     */
    public void tickHazardCheck(PlayerState player, double hazardX, double hazardZ, String hazardType, EventSink sink) {
        double distance = player.horizontalDistanceTo(hazardX, hazardZ);
        DetectorTier tier = detectors.tierFor(hazardType, distance);
        if (sink != null) {
            if (tier != null) {
                sink.onEvent(String.format("detector[%s]: %.1f blocks away, tier startDistance=%d, next beep ~%.1fs%s",
                        hazardType, distance, tier.startDistance, tier.periodBase,
                        tier.pitchMin != null ? String.format(" pitch=[%.1f,%.1f]", tier.pitchMin, tier.pitchMax) : ""));
            } else {
                sink.onEvent(String.format("detector[%s]: %.1f blocks away -- out of range, silent", hazardType, distance));
            }
        }
    }

    /**
     * Ticks the spawn scheduler; if one fires, wraps the real {@link MobConfig} in a new
     * {@link MobInstance} (an emulator-tracked health pool -- see that class's doc), using
     * {@link #setMobHealthTable}'s per-mob override if one has been configured for this mob's
     * real {@code configId}, or {@link MobInstance#DEFAULT_MAX_HEALTH} otherwise (part 49). Adds
     * it to {@link #aliveMobs()}, reports a spawn event, and returns it (null if nothing spawned
     * this tick).
     */
    public MobInstance tickSpawns(double deltaSeconds, EventSink sink) {
        MobConfig spawned = spawner.tick(deltaSeconds);
        if (spawned == null) {
            return null;
        }
        double maxHealth = healthTable != null
                ? healthTable.maxHealthFor(spawned.configId())
                : MobInstance.DEFAULT_MAX_HEALTH;
        MobInstance instance = new MobInstance(spawned, maxHealth);
        aliveMobs.add(instance);
        if (sink != null) {
            sink.onEvent(String.format("spawn: %s (%s) -- %.0f/%.0f hp (emulator placeholder, see MobInstance doc)",
                    spawned.mobName(), spawned.configId(), instance.health(), instance.maxHealth()));
        }
        return instance;
    }

    /** Every currently-alive (not yet killed) mob this loop has spawned so far, oldest first. */
    public List<MobInstance> aliveMobs() {
        return Collections.unmodifiableList(aliveMobs);
    }

    /**
     * Where a shot lands, for {@link #tickCombat(PlayerState, WeaponEntry, MobInstance, HitLocation, EventSink)}.
     * {@code BODY} applies a firearm's flat {@code effectiveDamage}; {@code HEAD}/{@code LIMB}
     * apply the real per-weapon {@code headshotMultiplier}/{@code limbMultiplier} on top of it
     * (part 30 -- see {@code WeaponEntry}'s doc for the string-encoding quirk those two real
     * fields have). Which location an attack hits is entirely the caller's decision -- this
     * emulator has no hit-detection/aim model, evidenced or otherwise; a caller (this class's own
     * CLI demo included) picks a location and this method just applies the matching real number.
     */
    public enum HitLocation { BODY, HEAD, LIMB }

    /** Internal pairing of a resolved damage amount with a human-readable description of how it was computed. */
    /**
     * Removes a freshly-dead mob from play and, if {@link #setLootTables} has been called, rolls
     * and reports its loot (part 48). Called from both {@code tickCombat} overloads.
     */
    private void handleDeath(MobInstance target, EventSink sink) {
        aliveMobs.remove(target);
        if (lootTables == null) {
            return;
        }
        List<LootDrop> drops = lootRoller.roll(lootTables.forMob(target.config().configId()));
        if (sink == null) {
            return;
        }
        if (drops.isEmpty()) {
            sink.onEvent("loot: " + target.config().mobName() + " dropped nothing");
        } else {
            for (LootDrop drop : drops) {
                sink.onEvent("loot: " + target.config().mobName() + " dropped " + drop);
            }
        }
    }

    private static final class DamageResolution {
        final double amount;
        final String how;
        DamageResolution(double amount, String how) {
            this.amount = amount;
            this.how = how;
        }
    }

    /**
     * Shared by both {@code tickCombat} overloads: resolves the real, range-INDEPENDENT damage
     * number for one hit -- exactly the part-30 behavior, unchanged. See
     * {@link #tickCombat(PlayerState, WeaponEntry, MobInstance, HitLocation, EventSink)}'s doc for
     * the full rationale (multiplier fallback, melee midpoint, vs.-mutant multiplier). Returns
     * null if {@code weapon} has neither a firearm damage field nor a melee damage range.
     */
    private DamageResolution resolveFlatDamage(WeaponEntry weapon, HitLocation location) {
        Double damage;
        String how;
        if (location == HitLocation.HEAD && weapons.effectiveHeadshotDamage(weapon, config) != null) {
            damage = weapons.effectiveHeadshotDamage(weapon, config);
            how = String.format("firearm HEADSHOT x%.2f", weapon.headshotMultiplier());
        } else if (location == HitLocation.LIMB && weapons.effectiveLimbDamage(weapon, config) != null) {
            damage = weapons.effectiveLimbDamage(weapon, config);
            how = String.format("firearm LIMB x%.2f", weapon.limbMultiplier());
        } else {
            damage = weapons.effectiveDamage(weapon, config);
            how = "firearm";
        }
        if (damage == null) {
            double[] melee = weapons.effectiveMeleeCommonDamage(weapon, config);
            if (melee != null) {
                damage = (melee[0] + melee[1]) / 2.0; // disclosed simplification: midpoint of the real light-attack range
                how = "melee(light attack, midpoint)";
                Double mutantMultiplier = weapon.meleeMutantDamageMultiplier();
                if (mutantMultiplier != null) {
                    // Every combat target in this emulator is a MobInstance wrapping a real
                    // stalkermobs config -- a mutant by this dataset's own definition -- so this
                    // is the real per-weapon multiplier applied in the only context that exists,
                    // not a guessed-at scenario. See this method's doc.
                    damage = damage * mutantMultiplier;
                    how = String.format("melee(light attack, midpoint) vs-mutant x%.2f", mutantMultiplier);
                }
            }
        }
        return damage == null ? null : new DamageResolution(damage, how);
    }

    /**
     * Resolves one attack from {@code player}'s {@code weapon} against {@code target}, using the
     * real, evidenced {@link WeaponRegistry#effectiveDamage}/{@code effectiveMelee*Damage}/
     * {@code effectiveHeadshotDamage}/{@code effectiveLimbDamage} numbers (this server's
     * {@code damageMultiplier} already applied, see {@link ServerConfig}) against {@code target}'s
     * emulator-tracked health pool -- see {@link MobInstance}'s doc for why that pool itself is a
     * disclosed placeholder, not evidenced data.
     *
     * <p>{@code location} only changes anything for a firearm, and only if the weapon record has
     * the matching real multiplier field ({@code headshotMultiplier}/{@code limbMultiplier}); if
     * it doesn't, this falls back to the flat {@code effectiveDamage} rather than failing --
     * roughly half the firearm dataset (62 of 337 total records) has no multiplier fields at all.
     * {@code location} is ignored for melee weapons: nothing in this dataset ties melee damage to
     * a hit location, only to attack type (common/strong), which this method does not vary either
     * -- it always uses the real "common" (light-attack) range, and within that range takes the
     * exact midpoint rather than rolling a random value, since nothing in this project's evidence
     * says how (or whether) the real game randomizes within the range.
     *
     * <p><b>No range/falloff model is applied by THIS overload</b> -- {@code
     * WeaponEntry.damageFalloff()} exposes the real curve, but this method always treats every
     * shot as if fired at {@code startDamage} range (0m). Use the distance-aware overload,
     * {@link #tickCombat(PlayerState, WeaponEntry, MobInstance, double, HitLocation, EventSink)},
     * when the caller has a real or demo distance to apply the curve against; this flat overload
     * stays available for callers that don't (or that want the always-0m behavior on purpose).
     *
     * <p>A melee weapon's real {@code meleeMutantDamageMultiplier} IS applied on top of the
     * midpoint, unlike {@code meleeCriticalHitMultiplier} (still unwired -- the dataset gives no
     * crit-chance field, so applying it would mean inventing when a hit "crits", which this
     * project's evidence rules forbid). Applying the vs.-mutant multiplier isn't a similar guess:
     * every {@code target} this method can ever be called with is a {@link MobInstance}, and every
     * {@code MobInstance} wraps a real {@code stalkermobs} config (part 20) -- i.e. a mutant by
     * this dataset's own definition. There is no other kind of combat target in this emulator, so
     * "is this hit against a mutant" is not a caller assumption here, it's the only case that
     * exists.
     *
     * <p>Returns {@code false} (and reports why) without changing any state if {@code target} is
     * null or already dead, or if {@code weapon} has neither a firearm damage field nor a melee
     * damage range (e.g. a device/attachment record) -- {@code true} otherwise.
     */
    public boolean tickCombat(PlayerState player, WeaponEntry weapon, MobInstance target, HitLocation location, EventSink sink) {
        if (target == null || target.isDead()) {
            if (sink != null) {
                sink.onEvent("combat: no valid (alive) target");
            }
            return false;
        }

        DamageResolution resolution = resolveFlatDamage(weapon, location);
        if (resolution == null) {
            if (sink != null) {
                sink.onEvent(String.format("combat: %s has no damage data (device/attachment?) -- no effect", weapon.name()));
            }
            return false;
        }

        double applied = target.applyDamage(resolution.amount);
        if (sink != null) {
            sink.onEvent(String.format("combat: %s's %s hits %s for %.1f (%s) -- %.1f/%.1f hp%s",
                    player.name, weapon.name(), target.config().mobName(), applied, resolution.how,
                    target.health(), target.maxHealth(), target.isDead() ? " -- DEAD" : ""));
        }
        if (target.isDead()) {
            handleDeath(target, sink);
        }
        return true;
    }

    /**
     * Same as {@link #tickCombat(PlayerState, WeaponEntry, MobInstance, HitLocation, EventSink)},
     * but for a firearm with a real falloff curve ({@code weapon.damageFalloff() != null}, 268 of
     * 337 records), applies {@link WeaponRegistry#effectiveDamageAtDistance} at {@code
     * distanceMeters} INSTEAD of the flat {@code effectiveDamage} before any headshot/limb
     * multiplier is applied on top -- i.e. a shot at long range now does less damage, a shot
     * inside {@code damageDecreaseStart} does full damage, exactly as real, per-weapon data says.
     *
     * <p><b>What part of that curve is officially confirmed vs. this project's own assumption
     * (part 39, restated here since it now affects an actual combat outcome, not just a dump-tool
     * printout):</b> the flat-at-{@code startDamage}-then-flat-at-{@code endDamage}-then-zero
     * SHAPE and every boundary distance's meaning is official, from EXBO's own public API docs.
     * The one thing that is NOT official is the exact interpolation curve strictly between {@code
     * damageDecreaseStart} and {@code damageDecreaseEnd} -- {@code
     * effectiveDamageAtDistance}/{@code WeaponEntry.damageAtDistanceLinearAssumption} use plain
     * linear interpolation there, disclosed as an assumption in that method's own doc, not
     * verified against real captured in-game numbers. Wiring it into combat here does not change
     * that disclosure; it just means a hit whose distance falls in that one segment now applies
     * an explicitly-flagged assumption instead of not applying the curve at all -- the same kind
     * of disclosed simplification this project already accepts for the melee midpoint pick above.
     *
     * <p>{@code distanceMeters} is entirely CALLER-supplied, same pattern as {@code
     * hazardX}/{@code hazardZ} in {@link #tickHazardCheck}: {@link MobInstance} has no tracked
     * world position (nothing in this project's evidence gives mobs one -- see that class's own
     * doc on why even its health pool is a placeholder), so there is no real per-shot distance
     * this method could compute itself. A caller with real or simulated player/mob positions
     * passes the distance it already has; this class does not invent one.
     *
     * <p>Weapons with NO falloff curve (69 of 337 records) and melee weapons behave IDENTICALLY
     * to the flat overload -- {@code distanceMeters} is simply not applicable to them (no range
     * field exists for melee in this dataset at all), so this method falls back to {@link
     * #resolveFlatDamage} rather than inventing a range effect for data that doesn't have one.
     */
    public boolean tickCombat(PlayerState player, WeaponEntry weapon, MobInstance target,
                               double distanceMeters, HitLocation location, EventSink sink) {
        if (target == null || target.isDead()) {
            if (sink != null) {
                sink.onEvent("combat: no valid (alive) target");
            }
            return false;
        }

        DamageResolution resolution;
        if (weapon.damageFalloff() != null) {
            double distanceBase = weapons.effectiveDamageAtDistance(weapon, distanceMeters, config);
            double amount = distanceBase;
            String how;
            if (location == HitLocation.HEAD && weapon.headshotMultiplier() != null) {
                amount = amount * weapon.headshotMultiplier();
                how = String.format("firearm HEADSHOT x%.2f @ %.0fm (range-adjusted, part 39 official boundaries / part 39 ramp-assumption)",
                        weapon.headshotMultiplier(), distanceMeters);
            } else if (location == HitLocation.LIMB && weapon.limbMultiplier() != null) {
                amount = amount * weapon.limbMultiplier();
                how = String.format("firearm LIMB x%.2f @ %.0fm (range-adjusted)", weapon.limbMultiplier(), distanceMeters);
            } else {
                how = String.format("firearm @ %.0fm (range-adjusted)", distanceMeters);
            }
            resolution = new DamageResolution(amount, how);
        } else {
            // No falloff curve on this record (or it's melee, which never has one) -- identical
            // to the flat overload, distance is not applicable, not silently ignored-but-assumed-zero.
            resolution = resolveFlatDamage(weapon, location);
        }

        if (resolution == null) {
            if (sink != null) {
                sink.onEvent(String.format("combat: %s has no damage data (device/attachment?) -- no effect", weapon.name()));
            }
            return false;
        }

        double applied = target.applyDamage(resolution.amount);
        if (sink != null) {
            sink.onEvent(String.format("combat: %s's %s hits %s for %.1f (%s) -- %.1f/%.1f hp%s",
                    player.name, weapon.name(), target.config().mobName(), applied, resolution.how,
                    target.health(), target.maxHealth(), target.isDead() ? " -- DEAD" : ""));
        }
        if (target.isDead()) {
            handleDeath(target, sink);
        }
        return true;
    }

    /** Convenience overload: a {@link HitLocation#BODY} attack (no headshot/limb multiplier, no range falloff -- see {@link #tickCombat(PlayerState, WeaponEntry, MobInstance, HitLocation, EventSink)}). */
    public boolean tickCombat(PlayerState player, WeaponEntry weapon, MobInstance target, EventSink sink) {
        return tickCombat(player, weapon, target, HitLocation.BODY, sink);
    }

    /** Convenience: runs every tick type above once, in order, for one simulated second. */
    public void tick(PlayerState player, double targetX, double targetZ, MovementSimulator.MoveMode mode,
                      double hazardX, double hazardZ, String hazardType, double deltaSeconds, EventSink sink) {
        tickMovement(player, targetX, targetZ, mode, deltaSeconds, sink);
        tickInventoryCheck(player, sink);
        tickHazardCheck(player, hazardX, hazardZ, hazardType, sink);
        tickSpawns(deltaSeconds, sink);
    }

    /** Collects every event line from a fixed-size run into a list instead of streaming to a sink -- for tests/tools that want the whole log. */
    public List<String> runCollectingEvents(PlayerState player, double targetX, double targetZ, MovementSimulator.MoveMode mode,
                                             double hazardX, double hazardZ, String hazardType, double deltaSeconds, int tickCount) {
        List<String> events = new ArrayList<>();
        for (int i = 0; i < tickCount; i++) {
            events.add("--- tick " + (i + 1) + " ---");
            tick(player, targetX, targetZ, mode, hazardX, hazardZ, hazardType, deltaSeconds, events::add);
        }
        return events;
    }
}
