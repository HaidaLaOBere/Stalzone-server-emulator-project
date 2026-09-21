package net.stalzone.emulator.session;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.economy.VendorArchive;
import net.stalzone.emulator.emission.EmissionCycle;
import net.stalzone.emulator.hazards.DetectorConfig;
import net.stalzone.emulator.hazards.DetectorTier;
import net.stalzone.emulator.items.ItemWeightTable;
import net.stalzone.emulator.items.WeaponEntry;
import net.stalzone.emulator.items.WeaponRegistry;
import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.loot.LootDrop;
import net.stalzone.emulator.loot.LootRoller;
import net.stalzone.emulator.loot.LootTables;
import net.stalzone.emulator.mobs.MobConfig;
import net.stalzone.emulator.mobs.MobHealthTable;
import net.stalzone.emulator.mobs.MobInstance;
import net.stalzone.emulator.mobs.SpawnScheduler;
import net.stalzone.emulator.modding.ModEventBus;
import net.stalzone.emulator.modding.events.AbilityUseEvent;
import net.stalzone.emulator.modding.events.EmissionPhaseChangedEvent;
import net.stalzone.emulator.modding.events.MobKilledEvent;
import net.stalzone.emulator.modding.events.MobSpawnedEvent;
import net.stalzone.emulator.modding.events.PurchaseEvent;
import net.stalzone.emulator.modding.events.QuestUpdatedEvent;
import net.stalzone.emulator.movement.MovementSimulator;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.world.WorldModel;
import net.stalzone.emulator.world.props.WorldFlavorCatalog;

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
 * with evidenced numbers in the first place. As of part 81, {@link #tickEmission} drives a real,
 * evidenced "emission"/"blowout" cycle via {@link #setEmissionCycle} -- see {@link
 * EmissionCycle}'s class doc for exactly which of its timing constants are real (part 80's
 * handbook-article finding) versus this emulator's own disclosed placeholder for the one
 * duration that finding did not give a real number for. Like the loot table and health table,
 * this is opt-in: nothing calls {@link #tickEmission} automatically, and {@link #tick} does not
 * include it -- a caller wires it in explicitly, since (unlike spawns/combat/movement) real
 * evidence never ties the emission cycle to a per-tick cadence a generic convenience method
 * could assume. As of part 82, {@link #tickPurchase} lets a {@link PlayerState} spend its real,
 * evidenced {@link Wallet} currency ({@code money}/{@code sc_birthday_21}/
 * {@code season_pass_ticket_3}, part 27) to buy into its real {@link Inventory} item-id space --
 * see that method's doc for exactly what is real (the currency ids, and, if {@link
 * #setVendorArchive} is used, real historical prices actually paid) versus entirely
 * caller-supplied policy (which items cost what, same disclosed-gap standard as {@link
 * #tickHazardCheck}'s hazard coordinates, since no canonical vendor price list has been found).
 * As of part 83, {@link #tickAbilityUse} lets a {@link PlayerState} invoke one of its real, named
 * {@link PrestigeAbility} unlocks (part 66) -- unlike every other tick method above, it does NOT
 * simulate any numeric effect (no blast radius, duration, cooldown, etc.), since no such data
 * exists anywhere in this project's evidence for any of the 11 real named abilities -- see that
 * method's own doc and {@link PrestigeAbility}'s class doc for why simulating one would mean
 * inventing gameplay wholesale rather than disclosing a placeholder on top of real data.
 *
 * <p>As of part 88, {@link #setSpawnRateMultiplier}/{@link #setLootRateMultiplier}/{@link
 * #setDamageMultiplier}/{@link #setEmissionRateMultiplier} let an operator (e.g. {@code
 * ConsoleDriver}'s {@code tune} command) actually retune all four of this project's headline
 * "spawn rates or damage values" knobs on a LIVE running server, without a restart. Before this
 * part, three of the four ({@code spawnRateMultiplier}/{@code lootRateMultiplier}/{@code
 * emissionRateMultiplier}) were silently dead once the server was running: {@link SpawnScheduler},
 * {@link LootRoller}, and {@link EmissionCycle} each captured their multiplier ONCE, from {@link
 * ServerConfig}, at construction time, and never read the field again -- editing {@code
 * server.properties} after startup, or the field directly, had zero effect. Only {@code
 * damageMultiplier} already worked live, since {@link WeaponRegistry} re-reads it on every call.
 *
 * <p>As of part 89, {@link #setEventBus} opts this loop into firing real simulation events (mob
 * spawn/kill, purchase, ability use, quest update, emission phase change -- see {@code
 * net.stalzone.emulator.modding.events}) to a {@link ModEventBus} -- this project's own answer to
 * the original ask's "later modding support (similar to what SPT team has done)". A no-op
 * (nothing fires) until this is called, same opt-in pattern already established for {@link
 * #setLootTables}/{@link #setMobHealthTable}/{@link #setEmissionCycle}. Every event carries
 * exactly the same real data its corresponding printed {@link EventSink} line already does -- a
 * mod receives structured objects instead of having to parse a log line back apart.
 *
 * <p>As of part 90, {@link #setLangCatalog} opts {@link #tickAbilityUse} into appending a real,
 * human-written ability name/description from {@code stalker/lang/en.lang} (see {@link
 * LangCatalog} and {@code progression.PrestigeAbility#langKey}) to its event line when the invoked
 * ability has a confirmed crosswalk -- same opt-in, no-op-until-called pattern as {@link
 * #setVendorArchive}. It does not change what {@link #tickAbilityUse} simulates (still nothing
 * numeric); it only makes the reported line carry the ability's real name instead of just its Java
 * enum constant.
 *
 * <p>As of part 165 -- the first {@code GameLoop} wiring since part 90's, after 74 parts (91-164)
 * spent entirely on further {@code en.lang}/asset content investigation with no new engine
 * hookups -- {@link #setEntityCatalog} opts {@link #tickSpawns} into the same treatment for real
 * mob display names: {@link WorldFlavorCatalog#entityNameForMobConfigId} (part 162/165, extended
 * part 178) gives a confirmed, hardcoded real crosswalk (found while investigating this exact
 * integration, not before) from {@link MobConfig#configId()} to a real {@code en.lang entity.*}
 * display name, for 4 of the real mob configs this project has actual evidence for out of the
 * full 35-config roster. Same standard as part 90: purely cosmetic (the event line's internal
 * codenames gain a real human-readable name alongside them), no new numeric simulation.
 *
 * <p>As of part 178, after another 12 parts (166-177) of further pure content cataloguing with no
 * new engine hookups, this project directly staged and read the real {@code
 * stalkermobs/configs/*.smm} files on the device (rather than relying only on already-written
 * Javadoc summaries of them) specifically looking for a 4th real {@link
 * WorldFlavorCatalog#entityNameForMobConfigId} crosswalk entry among the 32 real mob configs part
 * 165 left unmatched. Found one: {@code base_thief_rat.smm}'s own real {@code mobName} field is
 * {@code "tushkan_thief"}, matching {@code entity.tushkanchik_vor.name}="Rat-Robber" by the same
 * real transliteration-variance standard already used for {@code tsarevich}/{@code carevich}. Also
 * checked {@code base_puppet.smm} against the one other real unmatched {@code entity.*} name,
 * {@code kukla}="Doll", and found no supporting evidence (its {@code mobName} is plainly
 * {@code "puppet"}, and its sounds reuse the unrelated {@code tushkan} rat family) -- disclosed as
 * a checked, inconclusive lead, not wired, per rule 1. This project's ~56 `en.lang`-catalog
 * classes from parts 124-164, plus parts 166-177, are otherwise pure display-text data with no real
 * evidenced STRUCTURE (timings, triggers, numeric tables) this loop could hang further tick
 * behavior on without inventing gameplay wholesale, which per rule 1 this project does not do --
 * see {@link PrestigeAbility}'s class doc for the fullest existing explanation of exactly that
 * line.
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
    private EmissionCycle emissionCycle; // null until setEmissionCycle() is called -- tickEmission() is a no-op until then
    private VendorArchive vendorArchive; // null until setVendorArchive() is called -- tickPurchase() reports no real-price context until then
    private ModEventBus events; // null until setEventBus() is called -- event firing is a no-op until then, part 89
    private LangCatalog langCatalog; // null until setLangCatalog() is called -- tickAbilityUse() reports no real name/description until then, part 90
    private WorldFlavorCatalog entityCatalog; // null until setEntityCatalog() is called -- tickSpawns() reports no real display name until then, part 165

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
     * Opts this loop into driving a real "emission"/"blowout" cycle (part 81) -- see
     * {@link EmissionCycle}'s class doc for exactly which of its timing constants are real
     * evidenced data. {@link #tickEmission} is a no-op until this is called.
     */
    public void setEmissionCycle(EmissionCycle emissionCycle) {
        this.emissionCycle = emissionCycle;
    }

    /** The emission cycle set via {@link #setEmissionCycle}, or null if none has been configured. */
    public EmissionCycle emissionCycle() {
        return emissionCycle;
    }

    /**
     * Opts this loop into surfacing real historical price context (part 27) whenever
     * {@link #tickPurchase} is called for an item id this account's real {@link VendorArchive}
     * shows was actually bought before. Purely informational -- see {@link #tickPurchase}'s doc
     * for why the price a purchase is made at always stays entirely caller-supplied, never looked
     * up, validated, or overridden from this archive.
     */
    public void setVendorArchive(VendorArchive vendorArchive) {
        this.vendorArchive = vendorArchive;
    }

    /**
     * Opts this loop into firing real simulation events to {@code events} (part 89) -- see this
     * class's top-level doc and {@code net.stalzone.emulator.modding.ModEventBus}'s class doc.
     * Nothing fires (every {@code tickXxx} method behaves exactly as it did before this method
     * existed) until this is called.
     */
    public void setEventBus(ModEventBus events) {
        this.events = events;
    }

    /**
     * Opts {@link #tickAbilityUse} into appending a real ability name/description from {@code
     * catalog} (part 90) -- see this class's top-level doc and {@link
     * net.stalzone.emulator.progression.PrestigeAbility#langKey}. No-op (event line unchanged)
     * until this is called, and still a no-op for any ability {@code langKey()} returns null for.
     */
    public void setLangCatalog(LangCatalog catalog) {
        this.langCatalog = catalog;
    }

    /** The {@link LangCatalog} set via {@link #setLangCatalog}, or null if none has been configured. */
    public LangCatalog langCatalog() {
        return langCatalog;
    }

    /**
     * Opts {@link #tickSpawns} into appending a real mob display name from {@code catalog} (part
     * 165, extended part 178) -- see this class's top-level doc and {@link
     * WorldFlavorCatalog#entityNameForMobConfigId} for exactly which 4 real mob configs (out of
     * the full real 35-config roster) this project has an actual confirmed crosswalk for. No-op
     * (event line unchanged) until this is called, and still a no-op for any {@code configId}
     * that method returns null for.
     */
    public void setEntityCatalog(WorldFlavorCatalog catalog) {
        this.entityCatalog = catalog;
    }

    /** The {@link WorldFlavorCatalog} set via {@link #setEntityCatalog}, or null if none has been configured. */
    public WorldFlavorCatalog entityCatalog() {
        return entityCatalog;
    }

    /**
     * The real spawn-rate multiplier currently in effect (part 88) -- see {@link
     * #setSpawnRateMultiplier} for how to change it live.
     */
    public double spawnRateMultiplier() {
        return spawner.spawnRateMultiplier();
    }

    /**
     * Live-retunes {@link #tickSpawns}' spawn cadence (part 88) without a restart -- an
     * operator/console action (e.g. {@code ConsoleDriver}'s {@code tune} command), not new
     * evidence. Exactly the "full control over ... spawn rates" knob this project's own top-level
     * goal names, finally reachable while the server is actually running: before this part,
     * {@link ServerConfig#spawnRateMultiplier} was only ever read ONCE, at {@link SpawnScheduler}
     * construction time -- editing that field afterward silently did nothing, since nothing
     * re-read it. This method also keeps {@code config.spawnRateMultiplier} itself in sync, so it
     * reads correctly afterward (e.g. via {@code status}), even though the live component no
     * longer depends on that field being re-read. See {@link SpawnScheduler#setSpawnRateMultiplier}
     * for exactly when the change takes effect.
     */
    public void setSpawnRateMultiplier(double multiplier) {
        config.spawnRateMultiplier = multiplier;
        spawner.setSpawnRateMultiplier(multiplier);
    }

    /**
     * The real loot-rate multiplier currently in effect (part 88) -- see {@link
     * #setLootRateMultiplier} for how to change it live.
     */
    public double lootRateMultiplier() {
        return lootRoller.lootRateMultiplier();
    }

    /**
     * Live-retunes loot rolls (part 88) without a restart -- same rationale and same
     * config-field-was-only-ever-read-once problem as {@link #setSpawnRateMultiplier}, for the
     * third of this project's three headline "spawn rates or damage values"-style knobs (part
     * 48's {@link LootRoller}). See {@link LootRoller#setLootRateMultiplier} for exactly when the
     * change takes effect (the very next {@link LootRoller#roll}).
     */
    public void setLootRateMultiplier(double multiplier) {
        config.lootRateMultiplier = multiplier;
        lootRoller.setLootRateMultiplier(multiplier);
    }

    /**
     * The real damage multiplier currently in effect (part 88) -- see {@link
     * #setDamageMultiplier}'s doc for why this one already worked live before this part.
     */
    public double damageMultiplier() {
        return config.damageMultiplier;
    }

    /**
     * Live-retunes weapon damage (part 88). Unlike {@link #setSpawnRateMultiplier}/{@link
     * #setLootRateMultiplier}, {@link WeaponRegistry} was ALREADY live-tunable before this part --
     * it holds a direct {@link ServerConfig} reference and re-reads {@code damageMultiplier} on
     * every {@link WeaponRegistry#effectiveDamage} call rather than caching it at construction, so
     * simply assigning {@code config.damageMultiplier} was always enough. This method exists so
     * {@code ConsoleDriver}'s {@code tune} command has one consistent API for all four "spawn
     * rates or damage values" knobs, not because a code gap needed closing here.
     */
    public void setDamageMultiplier(double multiplier) {
        config.damageMultiplier = multiplier;
    }

    /**
     * The real emission-rate multiplier currently in effect, or null if {@link #setEmissionCycle}
     * was never called (part 88) -- see {@link #setEmissionRateMultiplier} for how to change it
     * live.
     */
    public Double emissionRateMultiplier() {
        return emissionCycle == null ? null : emissionCycle.emissionRateMultiplier();
    }

    /**
     * Live-retunes {@link #tickEmission}'s real 2-4 hour cycle (part 88) without a restart -- same
     * rationale and same config-field-was-only-ever-read-once problem as {@link
     * #setSpawnRateMultiplier}, for the fourth of this project's real-mechanic-backed tuning
     * knobs (part 81's {@link EmissionCycle}). Returns false (and still records the value in
     * {@code config.emissionRateMultiplier} for when a cycle IS later configured) if {@link
     * #setEmissionCycle} was never called -- there is no live {@link EmissionCycle} to apply it
     * to. See {@link EmissionCycle#setEmissionRateMultiplier} for exactly when a successful change
     * takes effect.
     */
    public boolean setEmissionRateMultiplier(double multiplier) {
        config.emissionRateMultiplier = multiplier;
        if (emissionCycle == null) {
            return false;
        }
        emissionCycle.setEmissionRateMultiplier(multiplier);
        return true;
    }

    /**
     * Advances the configured {@link EmissionCycle} (a no-op, reporting nothing, if
     * {@link #setEmissionCycle} was never called) and reports a real evidenced phase transition
     * if one happened this tick -- see that class's doc for exactly what each real named phase
     * means and which of its durations are real evidenced data versus this emulator's own
     * disclosed placeholder.
     */
    public void tickEmission(double deltaSeconds, EventSink sink) {
        if (emissionCycle == null) {
            return;
        }
        EmissionCycle.Phase changedTo = emissionCycle.tick(deltaSeconds);
        if (changedTo != null) {
            if (sink != null) {
                sink.onEvent(String.format("emission: phase -> %s%s", changedTo,
                        changedTo == EmissionCycle.Phase.ACTIVE ? " -- shelter required, survival in the open is impossible (real, part 80)"
                                : changedTo == EmissionCycle.Phase.ARTEFACT_WINDOW ? " -- real post-emission artefact-spawn window begins"
                                : ""));
            }
            if (events != null) {
                events.fire(new EmissionPhaseChangedEvent(changedTo));
            }
        }
    }

    /**
     * Simulates {@code player} buying {@code count} of item {@code itemId} from {@code traderId}
     * for {@code price} per unit in {@code currency}. If {@code player}'s real {@link Wallet}
     * doesn't have {@code price * count} of {@code currency} available, this reports the shortfall
     * and changes nothing, returning {@code false}; otherwise it debits the wallet, adds
     * {@code count} of {@code itemId} to {@code player}'s real {@link Inventory} (part 21's real
     * item-id space), and returns {@code true}.
     *
     * <p>{@code currency} should normally be one of {@link Wallet}'s real evidenced ids ({@code
     * money}/{@code sc_birthday_21}/{@code season_pass_ticket_3}, part 27), and {@code traderId}
     * one of the 14 real trader ids {@link VendorArchive}'s class doc names -- but neither is
     * enforced here, since an operator may legitimately configure others (nothing in this
     * project's evidence says either list is closed to exactly what one account's archive
     * happened to contain).
     *
     * <p>{@code traderId}/{@code itemId}/{@code price} are entirely CALLER-supplied, the same
     * disclosed-gap pattern as {@code hazardX}/{@code hazardZ} in {@link #tickHazardCheck} or
     * {@code distanceMeters} in the range-aware {@code tickCombat} overload: {@link VendorArchive}
     * is real evidence of purchases actually made in the past (which traders, which items, what
     * was actually paid), not a canonical price list -- no such config has been found anywhere in
     * this project, see that class's own doc. If {@link #setVendorArchive} has been called and
     * this account's real archive shows at least one past purchase of {@code itemId}, this method
     * reports that real min/avg/max price actually paid alongside the transaction, purely as
     * informational context -- it never substitutes for, validates, or overrides the
     * caller-supplied {@code price}.
     */
    public boolean tickPurchase(PlayerState player, String traderId, int itemId, int count, String currency, long price, EventSink sink) {
        if (count <= 0 || price < 0) {
            if (sink != null) {
                sink.onEvent(String.format("purchase: invalid request (count=%d, price=%d) -- no effect", count, price));
            }
            return false;
        }
        long totalCost = price * count; // caller-supplied per-unit price, same per-stack simplification VendorArchive's own real "price" field uses
        if (!player.wallet.debit(currency, totalCost)) {
            if (sink != null) {
                sink.onEvent(String.format("purchase: %s cannot afford %dx item %d from %s (%d %s needed, have %d) -- no effect",
                        player.name, count, itemId, traderId, totalCost, currency, player.wallet.balanceOf(currency)));
            }
            if (events != null) {
                events.fire(new PurchaseEvent(player, traderId, itemId, count, currency, totalCost, false));
            }
            return false;
        }
        player.inventory.add(itemId, count);
        if (sink != null) {
            String context = "";
            if (vendorArchive != null) {
                List<Long> realPrices = vendorArchive.pricesPaidFor(itemId);
                if (!realPrices.isEmpty()) {
                    long min = Collections.min(realPrices);
                    long max = Collections.max(realPrices);
                    double avg = realPrices.stream().mapToLong(Long::longValue).average().orElse(0.0);
                    context = String.format(" (real price history, part 27: min=%d avg=%.0f max=%d over %d past purchase(s))",
                            min, avg, max, realPrices.size());
                }
            }
            sink.onEvent(String.format("purchase: %s bought %dx item %d from %s for %d %s -- wallet now %d %s%s",
                    player.name, count, itemId, traderId, totalCost, currency, player.wallet.balanceOf(currency), currency, context));
        }
        if (events != null) {
            events.fire(new PurchaseEvent(player, traderId, itemId, count, currency, totalCost, true));
        }
        return true;
    }

    /**
     * Records that {@code player} invoked one of their real, unlocked {@link PrestigeAbility}
     * names (part 66) -- checks {@link PlayerState#unlockedAbilities}, reports whether the ability
     * was actually available, and returns whether it "fired" (unlocked) or not. Deliberately does
     * NOT simulate any numeric in-game effect -- no blast radius, teleport distance, phase-shift
     * duration, cooldown, or resource cost exists anywhere in this project's evidence for any of
     * the 11 real named abilities (see {@link PrestigeAbility}'s class doc); the real evidence is
     * exhaustively just the 11 names themselves. This method exists so the real ability roster has
     * SOME reachable integration point in the actual game loop (an operator/mod can build real
     * effects on top of it once evidence -- or an explicit emulator-original design decision -- is
     * available) without this project inventing gameplay mechanics it cannot evidence today.
     *
     * <p>Unlike {@link #tickCombat}/{@link #tickPurchase}, a "successful" call here changes no
     * state at all beyond the event line -- there is nothing evidenced to change.
     */
    public boolean tickAbilityUse(PlayerState player, PrestigeAbility ability, EventSink sink) {
        boolean unlocked = player.unlockedAbilities.contains(ability);
        if (sink != null) {
            String realName = langCatalog != null ? ability.realDisplayName(langCatalog) : null;
            String namePart = realName != null ? String.format(" \"%s\" (real name, part 90)", realName) : "";
            sink.onEvent(unlocked
                    ? String.format("ability: %s uses %s%s (real named ability, part 66 -- no simulated effect, see PrestigeAbility's doc)",
                            player.name, ability, namePart)
                    : String.format("ability: %s tried to use %s%s but has not unlocked it -- no effect", player.name, ability, namePart));
        }
        if (events != null) {
            events.fire(new AbilityUseEvent(player, ability, unlocked));
        }
        return unlocked;
    }

    /**
     * Records a real per-quest lifecycle transition (part 85) -- {@code questId} is a real quest
     * id, {@code questTitle} is the real title if the caller has one (pass null if not; {@code
     * config/quests.cfg} alone never supplies one, only a real {@code sd2} notification does, see
     * {@code QuestLog}'s class doc), {@code status} is one of {@link
     * net.stalzone.emulator.progression.QuestStatus}'s 3 real modeled lifecycle states. Like
     * {@link #tickPurchase}'s {@code traderId}/{@code itemId}/{@code price}, all three are
     * entirely CALLER-supplied -- this method has no canonical quest database to validate against
     * (none exists anywhere in this project's evidence, only the notification log of events that
     * already happened), it just records what the caller reports and updates {@link
     * PlayerState#questLog} accordingly. Simulates no reward or other consequence of a quest
     * finishing -- no reward-item/currency data has been found tied to a quest completion
     * anywhere in this project's evidence, only the notification toast itself (same standing as
     * {@link #tickAbilityUse}: a real, reachable integration point, not invented gameplay).
     */
    public void tickQuestEvent(PlayerState player, int questId, String questTitle,
                                QuestStatus status, EventSink sink) {
        player.questLog.recordEvent(questId, questTitle, status);
        if (sink != null) {
            String titlePart = questTitle != null && !questTitle.isBlank() ? " \"" + questTitle + "\"" : "";
            sink.onEvent(String.format("quest: %s -> %s quest %d%s (part 40/85 real evidence -- no reward simulated)",
                    player.name, status, questId, titlePart));
        }
        if (events != null) {
            events.fire(new QuestUpdatedEvent(player, questId, questTitle, status));
        }
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
     * this tick). As of part 165, if {@link #setEntityCatalog} has been called and this spawn's
     * real {@code configId} is one of the 3 this project has a confirmed {@code en.lang} display
     * name crosswalk for (see {@link WorldFlavorCatalog#entityNameForMobConfigId}), the event line
     * also carries that real human-readable name (e.g. "Boar Tsarevich" alongside the internal
     * {@code base_tsarevich}/{@code boar_tsarevich} codenames) -- purely cosmetic, same
     * no-new-numeric-effect standard as {@link #tickAbilityUse}'s part-90 name lookup.
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
            String realName = entityCatalog != null ? entityCatalog.entityNameForMobConfigId(spawned.configId()) : null;
            String namePart = realName != null ? String.format(" \"%s\" (real name, part 165)", realName) : "";
            sink.onEvent(String.format("spawn: %s (%s)%s -- %.0f/%.0f hp (emulator placeholder, see MobInstance doc)",
                    spawned.mobName(), spawned.configId(), namePart, instance.health(), instance.maxHealth()));
        }
        if (events != null) {
            events.fire(new MobSpawnedEvent(instance));
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
        List<LootDrop> drops = Collections.emptyList();
        if (lootTables != null) {
            drops = lootRoller.roll(lootTables.forMob(target.config().configId()));
            if (sink != null) {
                if (drops.isEmpty()) {
                    sink.onEvent("loot: " + target.config().mobName() + " dropped nothing");
                } else {
                    for (LootDrop drop : drops) {
                        sink.onEvent("loot: " + target.config().mobName() + " dropped " + drop);
                    }
                }
            }
        }
        if (events != null) {
            events.fire(new MobKilledEvent(target, drops));
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
