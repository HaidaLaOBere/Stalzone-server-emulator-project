package net.stalzone.emulator.session;

import net.stalzone.emulator.clientdata.QuestProgress;
import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.economy.VendorArchive;
import net.stalzone.emulator.emission.EmissionCycle;
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
import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.session.save.PlayerNotification;
import net.stalzone.emulator.session.save.PlayerSaveFile;
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
 * As of part 81, every tick also drives a real "emission"/"blowout" cycle via {@code
 * GameLoop.tickEmission} (part 80's real handbook-article finding, see {@link EmissionCycle}'s
 * class doc for exactly which of its timing constants are real vs. this emulator's own disclosed
 * placeholder). The demo's default {@code emissionRateMultiplier} is deliberately cranked way up
 * from {@code ServerConfig}'s real-timescale default of 1.0 (same reasoning as the demo's
 * {@code spawnRateMultiplier} default) purely so a short CLI run actually shows a phase change or
 * two instead of sitting in the real multi-hour WAITING phase the whole time.
 *
 * As of part 82, the demo player also starts with a disclosed, emulator-original demo balance of
 * real-evidenced {@code money} currency (see {@link Wallet}'s class doc) and makes one demo
 * purchase via {@code GameLoop.tickPurchase} against one of {@link VendorArchive}'s 14 real
 * trader ids. If a real {@code vendor_archive.dat} path is given, it's loaded via {@code
 * GameLoop.setVendorArchive} so that purchase also reports real historical price context (part
 * 27) when one exists for the demo item id -- purely informational, see that method's doc for why
 * the demo price itself stays this demo's own made-up policy either way, same disclosed-gap
 * standard as the demo hazard coordinates and engagement distance below.
 *
 * As of part 83, the demo player also has a real evidenced class and one real evidenced unlocked
 * ability (part 66) and makes two demo calls to {@code GameLoop.tickAbilityUse} -- one for the
 * unlocked ability, one for an ability never unlocked -- showing both outcomes; see that method's
 * doc for why neither call simulates any actual in-game effect.
 *
 * As of part 85, if a real {@code config/quests.cfg} path is given, the demo player's {@code
 * questLog} is seeded from it via {@code QuestLog.seedFrom} (real known/hidden quest ids, part
 * 40); if a real per-character {@code sd2} save-file path is also given, the demo additionally
 * replays that file's real quest notification history through {@code GameLoop.tickQuestEvent}
 * (real quest ids/titles/lifecycle status, part 85) -- see that method's own doc for why no
 * reward/consequence is simulated.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.session.GameLoopDemo \
 *     <dungeon_bolota_tutorial dir> <moving.txt> <detectors.txt> <weight.txt> <mob configs dir> \
 *     <weapons.json> [ticks] [spawnRateMultiplier] [damageMultiplier] [weaponName] [lootTables.json] \
 *     [lootRateMultiplier] [mobHealthTable.json] [emissionRateMultiplier] [vendorArchive.dat] \
 *     [quests.cfg] [sd2 save file]
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
        // Demo default deliberately far above ServerConfig's real-timescale default of 1.0 -- purely
        // so a short CLI run actually crosses a real phase boundary or two (see class doc).
        config.emissionRateMultiplier = args.length > 13 ? Double.parseDouble(args[13]) : 1000.0;
        Path vendorArchiveDat = args.length > 14 ? Path.of(args[14]) : null;
        Path questsCfg = args.length > 15 ? Path.of(args[15]) : null;
        Path sd2SaveFile = args.length > 16 ? Path.of(args[16]) : null;

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

        // Demo wallet balance: emulator-original demo default, NOT real evidence (no starting-balance
        // number for any currency has been found anywhere in this project) -- see Wallet's class doc.
        player.wallet.credit(Wallet.CURRENCY_MONEY, 10_000);

        // Demo class/ability selection: real evidenced names (part 66), but WHICH class/abilities a
        // demo player has is this demo's own arbitrary choice, same standing as the demo hazard
        // placement -- no evidence exists for how a real player actually selects either.
        player.playerClass = net.stalzone.emulator.progression.PlayerClass.HUNTER;
        player.unlockedAbilities.add(net.stalzone.emulator.progression.PrestigeAbility.VORTEX_GRENADE);

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
        EmissionCycle emissionCycle = new EmissionCycle(config, new java.util.Random(7)); // fixed seed: reproducible demo runs, same pattern as spawner/lootRoller
        loop.setEmissionCycle(emissionCycle);
        System.out.println("ServerConfig.emissionRateMultiplier = " + config.emissionRateMultiplier
                + " (real 2-4 hour interval, scaled for this short demo -- see emission/EmissionCycle's class doc)");
        if (vendorArchiveDat != null) {
            VendorArchive vendorArchive = VendorArchive.load(vendorArchiveDat);
            loop.setVendorArchive(vendorArchive);
            System.out.println("Loaded " + vendorArchive.size() + " real historical purchase(s) from " + vendorArchiveDat
                    + " across " + vendorArchive.usernames().size() + " character(s), " + vendorArchive.traderIds().size()
                    + " real trader id(s), " + vendorArchive.currencies().size()
                    + " real currency id(s) (part 27 -- see economy/VendorArchive's class doc)");
        }

        // Demo purchase: exercises GameLoop.tickPurchase (part 82) against one of VendorArchive's
        // real trader ids and one of the same real item ids already carried above -- the price
        // itself is this demo's own made-up policy (500 money/unit), NOT a real vendor price, since
        // no canonical price list exists anywhere in this project (see that method's own doc).
        System.out.println();
        System.out.println("Wallet before purchase: " + player.wallet.balanceOf(Wallet.CURRENCY_MONEY) + " " + Wallet.CURRENCY_MONEY);
        loop.tickPurchase(player, "buyer_rostok", 11352, 1, Wallet.CURRENCY_MONEY, 500, System.out::println);

        // Demo ability use: exercises GameLoop.tickAbilityUse (part 83) against the real named
        // ability roster (part 66) -- one call for an ability the demo player actually unlocked
        // above, one for an ability they never unlocked, showing both real event lines. Neither
        // call simulates any in-game effect -- see that method's own doc for why.
        loop.tickAbilityUse(player, net.stalzone.emulator.progression.PrestigeAbility.VORTEX_GRENADE, System.out::println);
        loop.tickAbilityUse(player, net.stalzone.emulator.progression.PrestigeAbility.PHASE_SHIFT, System.out::println);

        // Demo quest log: exercises QuestLog.seedFrom (part 85) against a real config/quests.cfg
        // if given, and GameLoop.tickQuestEvent against a real sd2 save file's real quest
        // notification history if also given -- both real account-derived data, see QuestLog's
        // and tickQuestEvent's own docs for exactly what's real vs. this demo's own choices.
        if (questsCfg != null) {
            QuestProgress questProgress = QuestProgress.load(questsCfg);
            player.questLog.seedFrom(questProgress);
            System.out.println("Loaded quest progress from " + questsCfg + ": " + questProgress
                    + " (real, live-updated account state, part 40 -- see clientdata/QuestProgress's class doc)");
        }
        if (sd2SaveFile != null) {
            PlayerSaveFile save = PlayerSaveFile.load(sd2SaveFile);
            int replayed = 0;
            for (PlayerNotification notification : save.notifications()) {
                if (!notification.isQuestChannel()) {
                    continue;
                }
                Integer questId = notification.questId();
                if (questId == null) {
                    continue; // real quest_failed/mail_reward entries carry no QuestId -- see QuestStatus's doc
                }
                QuestStatus status = "quest_finished".equals(notification.questNotificationType()) ? QuestStatus.FINISHED
                        : "new".equals(notification.questNotificationType()) ? QuestStatus.NEW
                        : null;
                if (status == null) {
                    continue;
                }
                loop.tickQuestEvent(player, questId, notification.questTitle(), status, System.out::println);
                replayed++;
                if (replayed >= 3) {
                    break; // demo policy: replay only the first few real events, not the whole file
                }
            }
            System.out.println("Replayed " + replayed + " real quest notification event(s) from " + sd2SaveFile
                    + " (part 40 -- see session/save/PlayerNotification's class doc)");
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
            loop.tickEmission(1.0, tickEvents::add);
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
        System.out.println("Final emission phase: " + emissionCycle.phase()
                + " (shelterRequired=" + emissionCycle.shelterRequired()
                + ", artefactSpawnBoostActive=" + emissionCycle.artefactSpawnBoostActive() + ")");
        System.out.println("Final wallet: " + player.wallet.balanceOf(Wallet.CURRENCY_MONEY) + " " + Wallet.CURRENCY_MONEY);
        System.out.println("Final quest log: " + player.questLog.knownQuestIds().size() + " known quest(s), "
                + player.questLog.hiddenQuestIds().size() + " hidden");
    }
}
