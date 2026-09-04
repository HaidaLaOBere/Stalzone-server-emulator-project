package net.stalzone.emulator.session;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.hazards.DetectorConfig;
import net.stalzone.emulator.items.ItemWeightTable;
import net.stalzone.emulator.items.WeaponEntry;
import net.stalzone.emulator.items.WeaponRegistry;
import net.stalzone.emulator.loot.LootTables;
import net.stalzone.emulator.mobs.MobHealthTable;
import net.stalzone.emulator.mobs.MobInstance;
import net.stalzone.emulator.mobs.MobRegistry;
import net.stalzone.emulator.movement.MovementConfig;
import net.stalzone.emulator.movement.MovementSimulator;
import net.stalzone.emulator.world.WorldModel;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for {@link GameLoop} -- loads the SAME real files this project's other *DumpTool
 * classes validate against (Milestone-1's real {@code dungeon_bolota_tutorial} region data,
 * {@code moving.txt}, {@code detectors.txt}, {@code weight.txt}, all 35 real {@code .smm} mob
 * configs, and all 337 real {@code weapons.json} records), builds one simulated player, and runs
 * a scripted walk toward a demo waypoint while an also-demo "rad" hazard sits nearby and mobs
 * spawn on {@code ServerConfig.spawnRateMultiplier}'s real cadence; once a mob spawns, the demo
 * player fires a real weapon (real {@code WeaponRegistry} damage, {@code damageMultiplier}
 * applied) at it every following tick until it dies, using {@link MobInstance}'s disclosed
 * placeholder health pool (real .smm configs have no health field -- see that class's doc).
 * As of part 41, each engagement also exercises the distance-aware {@code GameLoop.tickCombat}
 * overload -- see the combat loop below for the disclosed, made-up-for-this-demo engagement
 * distance (no real per-shot distance exists anywhere in this project's evidence, same
 * limitation as the hazard-check demo scenario). Prints every tick's events, same pattern as
 * this project's other dump tools.
 *
 * As of part 48, if a loot-table JSON path is given, a kill also rolls loot via {@code
 * GameLoop.setLootTables} -- see {@code loot/LootTable}'s class doc for why that data is
 * emulator-original, not reverse-engineered.
 *
 * As of part 49, if a mob-health-table JSON path is given, spawned mobs use its per-{@code
 * configId} {@code maxHealth} override (via {@code GameLoop.setMobHealthTable}) instead of the
 * flat {@link MobInstance#DEFAULT_MAX_HEALTH} -- see {@code mobs/MobHealthTable}'s class doc
 * for why that data is likewise emulator-original tuning, not reverse-engineered.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.session.GameLoopDemo \
 *     <dungeon_bolota_tutorial dir> <moving.txt> <detectors.txt> <weight.txt> <mob configs dir> \
 *     <weapons.json> [ticks] [spawnRateMultiplier] [damageMultiplier] [weaponName] [lootTables.json] \
 *     [lootRateMultiplier] [mobHealthTable.json]
 */
public final class GameLoopDemo {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 6) {
            System.err.println("usage: GameLoopDemo <zoneDir> <moving.txt> <detectors.txt> <weight.txt> <mobConfigsDir> "
                    + "<weapons.json> [ticks] [spawnRateMultiplier] [damageMultiplier] [weaponName]");
            System.exit(2);
        }

        Path zoneDir = Path.of(args[0]);
        Path movingTxt = Path.of(args[1]);
        Path detectorsTxt = Path.of(args[2]);
        Path weightTxt = Path.of(args[3]);
        Path mobConfigsDir = Path.of(args[4]);
        Path weaponsJson = Path.of(args[5]);
        int ticks = args.length > 6 ? Integer.parseInt(args[6]) : 15;

        ServerConfig config = new ServerConfig();
        config.spawnRateMultiplier = args.length > 7 ? Double.parseDouble(args[7]) : 3.0; // demo: faster than default so the CLI run actually shows a spawn
        config.damageMultiplier = args.length > 8 ? Double.parseDouble(args[8]) : 1.0;
        String weaponName = args.length > 9 ? args[9] : "AK-12";
        Path lootTablesJson = args.length > 10 ? Path.of(args[10]) : null;
        config.lootRateMultiplier = args.length > 11 ? Double.parseDouble(args[11]) : 1.0;
        Path mobHealthTableJson = args.length > 12 ? Path.of(args[12]) : null;

        WorldModel world = WorldModel.load(zoneDir, "dungeon_bolota_tutorial");
        MovementConfig moveConfig = MovementConfig.load(movingTxt);
        DetectorConfig detectors = DetectorConfig.load(detectorsTxt);
        ItemWeightTable weightTable = ItemWeightTable.load(weightTxt);
        MobRegistry mobs = MobRegistry.load(mobConfigsDir);
        WeaponRegistry weapons = WeaponRegistry.load(weaponsJson);

        WeaponEntry weapon = weapons.byName(weaponName);
        if (weapon == null) {
            System.err.println("weapon \"" + weaponName + "\" not found in " + weaponsJson);
            System.exit(1);
        }

        System.out.println("Loaded: " + world.regionFileCount() + " region files / " + world.loadedChunkCount() + " chunks; "
                + moveConfig.size() + " movement settings; " + detectors.size() + " detector tiers; "
                + weightTable.size() + " weight entries; " + mobs.size() + " mob configs; "
                + weapons.size() + " weapon records.");
        System.out.println("Demo weapon: " + weapon.name() + " (id=" + weapon.id() + "), damageMultiplier = " + config.damageMultiplier);
        System.out.println("ServerConfig.spawnRateMultiplier = " + config.spawnRateMultiplier
                + " (this run's whole point: this real config knob now actually changes spawn cadence below)");

        // Pick a REAL loaded chunk to start the player in, rather than an arbitrary/possibly-unloaded coordinate.
        List<long[]> loadedChunks = world.loadedChunkCoords();
        if (loadedChunks.isEmpty()) {
            System.err.println("no loaded chunks in " + zoneDir);
            System.exit(1);
        }
        long[] startChunk = loadedChunks.get(0);
        int startX = (int) (startChunk[0] * 16 + 8); // chunk center
        int startZ = (int) (startChunk[1] * 16 + 8);
        int startY = Math.max(world.highestSolidY(startX, startZ), 0) + 1;

        PlayerState player = new PlayerState("demo_player", "dungeon_bolota_tutorial", startX, startY, startZ);
        System.out.println("Start: " + player + " (in known zone: " + player.isInKnownZone() + ")");

        // Demo inventory: real item ids from weight.txt. 16347 weighs 25 -- three of them (75) trips the real 50-weight cap.
        player.inventory.add(16340, 2);   // 0.4 each
        player.inventory.add(16347, 3);   // 25 each -- deliberately overweight for the demo

        // Demo waypoint: another real loaded chunk if one exists, else just offset from start.
        double targetX = startX + 40;
        double targetZ = startZ + 40;
        if (loadedChunks.size() > 1) {
            long[] other = loadedChunks.get(loadedChunks.size() / 2);
            targetX = other[0] * 16 + 8;
            targetZ = other[1] * 16 + 8;
        }

        // Demo hazard: NOT real anomaly placement data (none exists anywhere in this project, see
        // GameLoop.tickHazardCheck's doc) -- placed a plausible short distance from the start so the
        // "rad" detector tiers actually trigger during this short demo run.
        double hazardX = startX + 3;
        double hazardZ = startZ + 2;

        MovementSimulator movement = new MovementSimulator(moveConfig);
        GameLoop loop = new GameLoop(world, movement, detectors, mobs, config, weightTable, weapons);
        if (lootTablesJson != null) {
            LootTables lootTables = LootTables.load(lootTablesJson);
            loop.setLootTables(lootTables);
            System.out.println("Loaded " + lootTables.size() + " loot table(s) from " + lootTablesJson
                    + "; ServerConfig.lootRateMultiplier = " + config.lootRateMultiplier
                    + " (emulator-original loot config -- see loot/LootTable's class doc)");
        }
        if (mobHealthTableJson != null) {
            MobHealthTable healthTable = MobHealthTable.load(mobHealthTableJson);
            loop.setMobHealthTable(healthTable);
            System.out.println("Loaded " + healthTable.size() + " mob health override(s) from " + mobHealthTableJson
                    + " (emulator-original tuning -- see mobs/MobHealthTable's class doc)");
        }

        // Manual tick loop (rather than runCollectingEvents, part 29's convenience helper) because
        // this demo also needs to react to what tickSpawns() returns each tick: once a mob is
        // alive, fire the demo weapon at it on every subsequent tick until it dies. Combat is
        // intentionally NOT folded into GameLoop.tick()/runCollectingEvents() itself -- whether/who
        // to fire at each tick is a caller decision this class has no evidence to make generically.
        System.out.println();
        int killCount = 0;
        int shotsOnCurrentTarget = 0;
        for (int i = 1; i <= ticks; i++) {
            System.out.println("--- tick " + i + " ---");
            java.util.List<String> tickEvents = new java.util.ArrayList<>();
            loop.tick(player, targetX, targetZ, MovementSimulator.MoveMode.WALK, hazardX, hazardZ, "rad", 1.0, tickEvents::add);
            for (String line : tickEvents) {
                System.out.println(line);
            }

            List<MobInstance> alive = loop.aliveMobs();
            if (!alive.isEmpty()) {
                MobInstance target = alive.get(0); // demo policy: always engage the oldest still-alive spawn
                boolean wasAliveBefore = !target.isDead();
                // Demo policy, not real data: the very first hit on a freshly-spawned target is a
                // headshot (to actually exercise the real headshotMultiplier -- part 30), every
                // following hit on the same target is a plain body shot.
                boolean freshSpawn = target.health() == target.maxHealth();
                GameLoop.HitLocation location = freshSpawn ? GameLoop.HitLocation.HEAD : GameLoop.HitLocation.BODY;
                // Demo engagement distance, NOT real telemetry (see this class's part-41 doc note
                // above): starts close (5m) on a fresh spawn and opens up by 20m per following
                // shot on the same target, so a short demo run actually crosses a real weapon's
                // damageDecreaseStart/damageDecreaseEnd boundaries and visibly shows the applied
                // damage number change tick to tick -- exercising GameLoop's distance-aware
                // tickCombat overload (part 41) rather than the always-0m flat one.
                double demoDistanceMeters = freshSpawn ? 5.0 : 5.0 + shotsOnCurrentTarget * 20.0;
                loop.tickCombat(player, weapon, target, demoDistanceMeters, location, System.out::println);
                shotsOnCurrentTarget = freshSpawn ? 1 : shotsOnCurrentTarget + 1;
                if (wasAliveBefore && target.isDead()) {
                    killCount++;
                }
            }
        }

        System.out.println();
        System.out.println("Final: " + player);
        System.out.println("Mobs killed: " + killCount + ", still alive: " + loop.aliveMobs().size());
    }
}
