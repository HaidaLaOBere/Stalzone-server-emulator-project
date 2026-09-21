package net.stalzone.emulator.session;

import net.stalzone.emulator.clientdata.QuestProgress;
import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.economy.VendorArchive;
import net.stalzone.emulator.emission.EmissionCycle;
import net.stalzone.emulator.hazards.DetectorConfig;
import net.stalzone.emulator.items.ItemWeightTable;
import net.stalzone.emulator.items.WeaponRegistry;
import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.loot.LootTables;
import net.stalzone.emulator.mobs.MobHealthTable;
import net.stalzone.emulator.mobs.MobRegistry;
import net.stalzone.emulator.modding.LoadedMod;
import net.stalzone.emulator.modding.ModEventBus;
import net.stalzone.emulator.modding.ModLoader;
import net.stalzone.emulator.modding.events.ServerStartedEvent;
import net.stalzone.emulator.modding.events.ServerStoppingEvent;
import net.stalzone.emulator.modding.events.ServerTickEvent;
import net.stalzone.emulator.movement.MovementConfig;
import net.stalzone.emulator.movement.MovementSimulator;
import net.stalzone.emulator.world.WorldModel;
import net.stalzone.emulator.world.props.WorldFlavorCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Part 84: the first thing in this project that turns {@code Main} (previously a passive UDP
 * packet logger with zero connection to {@link GameLoop}, see {@code Main}'s pre-part-84 doc
 * history in docs/RESEARCH_LOG.md) into an actually-runnable local simulation -- a genuine
 * continuously-ticking process, not {@code GameLoopDemo}'s fixed-tick-count CLI run.
 *
 * <p><b>What this is.</b> A background world simulation: it loads the same real Milestone-1 zone
 * data, {@code moving.txt}/{@code detectors.txt}/{@code weight.txt}, and real {@code .smm} mob
 * configs that {@code GameLoopDemo} and this project's other dump tools already load and verify
 * against (from underneath {@link ServerConfig#stalzoneInstallDir}, at the real relative paths
 * documented on that field), builds one local {@link PlayerState}, and then repeatedly drives
 * exactly the {@link GameLoop} tick methods that have real evidenced cadence behind them and need
 * no fabricated per-tick input to call honestly: {@link GameLoop#tickSpawns} (real spawn cadence,
 * part 29), {@link GameLoop#tickEmission} (real 2-4 hour emission interval, part 80/81), and
 * {@link GameLoop#tickInventoryCheck} (real weight cap, part ~20s). All of {@link
 * ServerConfig}'s real-wired multipliers ({@code spawnRateMultiplier}, {@code damageMultiplier},
 * {@code lootRateMultiplier}, {@code emissionRateMultiplier}) apply exactly as they do in the
 * demo, since this uses the same {@link GameLoop} instance type and the same {@link ServerConfig}.
 *
 * <p><b>What this deliberately does NOT do.</b> It does not simulate player movement, does not
 * fabricate a hazard/anomaly position, and does not auto-fire combat -- {@code GameLoopDemo}'s
 * own doc already discloses that its scripted walk-to-a-waypoint, its nearby demo "rad" hazard,
 * and its auto-engage-on-spawn combat policy are all this project's own demo conveniences, not
 * evidenced real behavior (no real automated-player-input or real anomaly-placement data exists
 * anywhere in this project's evidence). Baking those same demo conveniences into what's supposed
 * to be the actual server process would misrepresent disclosed demo theatrics as real server
 * behavior, so they're left out here. {@link #player()}, {@link #gameLoop()}, and
 * {@link #weapons()} expose the live {@link PlayerState}/{@link GameLoop}/{@link WeaponRegistry}
 * so a future caller with a real input source -- eventually the real network protocol once the
 * wire encryption is read (docs/RESEARCH_LOG.md part 7), or in the meantime a console/manual
 * driver -- can call {@code tickMovement}/{@code tickHazardCheck}/{@code tickCombat}/
 * {@code tickPurchase}/{@code tickAbilityUse}/{@code tickQuestEvent} against this same running
 * loop. As of part 86, {@link ConsoleDriver} is exactly that console/manual driver -- see its own
 * class doc. As of part 85, the constructor also seeds the local player's real,
 * account-level quest-progress state (their real known/hidden quest ids, via {@link
 * PlayerState#questLog}) from a real {@code config/quests.cfg} if the configured install has one
 * -- see {@link QuestProgress}'s and {@code session/QuestLog}'s class docs; it deliberately does
 * NOT also auto-load any specific character's real per-character quest-notification history
 * ({@code config/<CharacterName>/sd2}), since there's no evidence for which real account
 * character this local player should be treated as -- that stays a future caller's job too, via
 * {@code tickQuestEvent} against {@link #player()}/{@link #gameLoop()}.
 *
 * <p>Mirrors {@code network/UdpListener}'s {@code Runnable, AutoCloseable} shape: {@link #run()}
 * blocks in a loop until {@link #close()} sets the running flag false, pacing itself at {@link
 * ServerConfig#tickIntervalMillis} between ticks (this project's own disclosed pacing choice, not
 * an evidenced real tick rate -- see that field's doc).
 *
 * <p>As of part 87, the constructor also restores {@link #player()}'s state (position, inventory,
 * wallet, class, unlocked abilities, quest log) from {@link ServerConfig#playerSaveFilePath} if
 * that file exists, and {@link #close()} writes it back out -- {@link #saveNow()} does the same on
 * demand. Before this, every restart discarded whatever a {@link ConsoleDriver} session had built
 * up. See {@link PlayerStateSave}'s class doc for the (entirely emulator-original) file format.
 *
 * <p>As of part 89, the constructor also builds a {@link ModEventBus}, wires it into {@link #loop}
 * via {@link GameLoop#setEventBus}, and loads every mod JAR found under {@link
 * ServerConfig#modsDir} via {@link ModLoader#loadAll} -- this project's own answer to the original
 * ask's "later modding support (similar to what SPT team has done)". Mods finish loading (and see
 * a {@link net.stalzone.emulator.modding.events.ServerStartedEvent} fire) before this constructor
 * returns, so by the time {@link #run()}'s tick loop starts, every mod's event listeners are
 * already registered and every startup-time live-tuning call a mod made (part 88's {@code
 * setXxxMultiplier} methods) is already in effect. {@link #close()} fires a {@link
 * net.stalzone.emulator.modding.events.ServerStoppingEvent}, then calls {@link
 * ModLoader#unloadAll} before saving the session -- see {@link #loadedMods()} and {@code
 * ConsoleDriver}'s {@code mods} command for inspecting what's loaded.
 */
public final class SingleplayerServer implements Runnable, AutoCloseable {

    private final ServerConfig config;
    private final GameLoop loop;
    private final PlayerState player;
    private final EmissionCycle emissionCycle;
    private final WeaponRegistry weapons;
    private final Consumer<String> eventSink;
    private final ModEventBus events;
    private final List<LoadedMod> loadedMods;
    private Path playerSavePath; // null if config.playerSaveFilePath is unset -- persistence disabled (part 87)
    private volatile boolean running = true;

    /** True when {@link ServerConfig#stalzoneInstallDir} is set -- the only signal {@code Main} needs to decide whether to start this at all. */
    public static boolean isConfigured(ServerConfig config) {
        return config.stalzoneInstallDir != null && !config.stalzoneInstallDir.isBlank();
    }

    public SingleplayerServer(ServerConfig config, Consumer<String> eventSink) throws IOException {
        if (!isConfigured(config)) {
            throw new IllegalStateException("stalzoneInstallDir is not set -- see ServerConfig's class doc");
        }
        this.config = config;
        this.eventSink = eventSink;

        Path installDir = Path.of(config.stalzoneInstallDir);
        // Real relative paths under a STALZONE install, confirmed against an actual client copy
        // (see ServerConfig's class doc for the full citation) -- same paths GameLoopDemo's own
        // CLI args point at, just derived here instead of typed by hand each run.
        Path zoneDir = installDir.resolve("map_cache").resolve(config.mapCacheVersion).resolve(config.startZone);
        Path movingTxt = installDir.resolve("modassets/assets/stalcraft/moving.txt");
        Path detectorsTxt = installDir.resolve("modassets/assets/stalker/detectors.txt");
        Path weightTxt = installDir.resolve("modassets/assets/stalker/weight.txt");
        Path mobConfigsDir = installDir.resolve("modassets/assets/stalkermobs/configs");
        Path weaponsJson = Path.of(config.weaponsJsonPath);

        requireExists(zoneDir, "zone data (stalzoneInstallDir/map_cache/" + config.mapCacheVersion + "/" + config.startZone + ")");
        requireExists(movingTxt, "moving.txt");
        requireExists(detectorsTxt, "detectors.txt");
        requireExists(weightTxt, "weight.txt");
        requireExists(mobConfigsDir, "mob configs dir");
        requireExists(weaponsJson, "weaponsJsonPath");

        WorldModel world = WorldModel.load(zoneDir, config.startZone);
        MovementConfig moveConfig = MovementConfig.load(movingTxt);
        DetectorConfig detectors = DetectorConfig.load(detectorsTxt);
        ItemWeightTable weightTable = ItemWeightTable.load(weightTxt);
        MobRegistry mobs = MobRegistry.load(mobConfigsDir);
        this.weapons = WeaponRegistry.load(weaponsJson);

        eventSink.accept("Loaded: " + world.regionFileCount() + " region files / " + world.loadedChunkCount() + " chunks; "
                + moveConfig.size() + " movement settings; " + detectors.size() + " detector tiers; "
                + weightTable.size() + " weight entries; " + mobs.size() + " mob configs; "
                + weapons.size() + " weapon records.");

        List<long[]> loadedChunks = world.loadedChunkCoords();
        if (loadedChunks.isEmpty()) {
            throw new IOException("no loaded chunks in " + zoneDir);
        }
        long[] startChunk = loadedChunks.get(0);
        int startX = (int) (startChunk[0] * 16 + 8);
        int startZ = (int) (startChunk[1] * 16 + 8);
        int startY = Math.max(world.highestSolidY(startX, startZ), 0) + 1;

        this.player = new PlayerState("player", config.startZone, startX, startY, startZ);
        eventSink.accept("Start: " + player + " (in known zone: " + player.isInKnownZone() + ")");

        MovementSimulator movement = new MovementSimulator(moveConfig);
        this.loop = new GameLoop(world, movement, detectors, mobs, config, weightTable, weapons);
        this.events = new ModEventBus(eventSink);
        loop.setEventBus(events);

        if (config.lootTablesJsonPath != null) {
            Path lootTablesJson = Path.of(config.lootTablesJsonPath);
            requireExists(lootTablesJson, "lootTablesJsonPath");
            LootTables lootTables = LootTables.load(lootTablesJson);
            loop.setLootTables(lootTables);
            eventSink.accept("Loaded " + lootTables.size() + " loot table(s) from " + lootTablesJson
                    + " (emulator-original -- see loot/LootTable's class doc)");
        }
        if (config.mobHealthTableJsonPath != null) {
            Path mobHealthTableJson = Path.of(config.mobHealthTableJsonPath);
            requireExists(mobHealthTableJson, "mobHealthTableJsonPath");
            MobHealthTable healthTable = MobHealthTable.load(mobHealthTableJson);
            loop.setMobHealthTable(healthTable);
            eventSink.accept("Loaded " + healthTable.size() + " mob health override(s) from " + mobHealthTableJson
                    + " (emulator-original -- see mobs/MobHealthTable's class doc)");
        }
        this.emissionCycle = new EmissionCycle(config, new java.util.Random());
        loop.setEmissionCycle(emissionCycle);

        // Real per-install purchase history (part 27), if this install's client has ever actually
        // bought anything -- purely optional/informational, see GameLoop.tickPurchase's own doc.
        Path vendorArchiveDat = installDir.resolve("config/vendor_archive.dat");
        if (Files.exists(vendorArchiveDat)) {
            VendorArchive vendorArchive = VendorArchive.load(vendorArchiveDat);
            loop.setVendorArchive(vendorArchive);
            eventSink.accept("Loaded " + vendorArchive.size() + " real historical purchase(s) from " + vendorArchiveDat);
        }

        // Real, account-level (not per-character) live quest-progress state (part 40/85), if this
        // install's client has ever logged into an account that has read/hidden any quest. Seeds
        // player.questLog's known/hidden real quest ids -- see QuestLog's class doc. Deliberately
        // does NOT attempt to also auto-load any specific character's real sd2 notification
        // history (config/<CharacterName>/sd2) -- there is no evidence for which real account
        // character, if any, this local singleplayer PlayerState should be treated as; a future
        // caller with that context can still call GameLoop.tickQuestEvent directly via player()/
        // gameLoop(), same as movement/hazard/combat.
        Path questsCfg = installDir.resolve("config/quests.cfg");
        if (Files.exists(questsCfg)) {
            QuestProgress questProgress = QuestProgress.load(questsCfg);
            player.questLog.seedFrom(questProgress);
            eventSink.accept("Loaded quest progress from " + questsCfg + ": " + questProgress
                    + " (real, live-updated account state, part 40 -- see clientdata/QuestProgress's class doc)");
        }

        // Real UI-localization key table (part 90), if this install's client has it -- purely
        // optional/informational, see GameLoop.tickAbilityUse's and LangCatalog's own doc. Only
        // en.lang is loaded; the other 4 real locale files are not wired into anything yet.
        Path langFile = installDir.resolve("modassets/assets/stalker/lang/en.lang");
        if (Files.exists(langFile)) {
            LangCatalog langCatalog = LangCatalog.load(langFile);
            loop.setLangCatalog(langCatalog);
            eventSink.accept("Loaded " + langCatalog.size() + " real localization key(s) from " + langFile
                    + " (part 90 -- see localization/LangCatalog's class doc)");

            // Real mob-display-name crosswalk (part 165), built from the same already-loaded
            // en.lang -- see GameLoop.tickSpawns's and WorldFlavorCatalog#entityNameForMobConfigId's
            // own docs for exactly which 3 real mob configs (out of the full 35-config roster)
            // this project has an actual confirmed crosswalk for.
            WorldFlavorCatalog entityCatalog = WorldFlavorCatalog.load(langCatalog);
            loop.setEntityCatalog(entityCatalog);
            eventSink.accept("Loaded real entity display-name crosswalk from " + langFile
                    + " (part 165 -- see world/props/WorldFlavorCatalog's class doc)");
        }

        // This emulator's own session-persistence file (part 87), NOT real client data -- see
        // PlayerStateSave's class doc. Restores position/inventory/wallet/class/abilities/quest
        // state from a prior run, if config.playerSaveFilePath is set and that file exists; a
        // fresh install with no save file yet just keeps the spawn state built above (PlayerStateSave.load
        // is a no-op when the path doesn't exist, same as ServerConfig.loadOrCreateDefault treating
        // a missing file as "use defaults" rather than an error).
        this.playerSavePath = config.playerSaveFilePath != null ? Path.of(config.playerSaveFilePath) : null;
        if (playerSavePath != null && Files.exists(playerSavePath)) {
            PlayerStateSave.load(player, playerSavePath);
            eventSink.accept("Loaded session save from " + playerSavePath + ": " + player
                    + " (this emulator's own persistence format, part 87 -- see session/PlayerStateSave's class doc)");
        }

        eventSink.accept("ServerConfig: spawnRateMultiplier=" + config.spawnRateMultiplier
                + " damageMultiplier=" + config.damageMultiplier
                + " lootRateMultiplier=" + config.lootRateMultiplier
                + " emissionRateMultiplier=" + config.emissionRateMultiplier
                + " tickIntervalMillis=" + config.tickIntervalMillis);

        // Part 89: load every third-party mod JAR under config.modsDir, if any -- see
        // modding/ModLoader's class doc for the full discovery/fault-isolation design. Mods are
        // loaded LAST in this constructor, after every other core system above is already wired,
        // so a mod's onLoad() sees the fully-set-up GameLoop/ServerConfig it's documented to see.
        this.loadedMods = config.modsDir != null
                ? ModLoader.loadAll(Path.of(config.modsDir), loop, config, events, eventSink)
                : new ArrayList<>();
        if (loadedMods.isEmpty()) {
            eventSink.accept("mods: none loaded"
                    + (config.modsDir != null ? " (" + config.modsDir + " missing/empty, or mod loading is disabled)" : " (modsDir is unset)"));
        }
        events.fire(new ServerStartedEvent());
    }

    private static void requireExists(Path path, String what) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("missing " + what + ": " + path);
        }
    }

    /** The one local player this singleplayer simulation drives. Exposed so a future caller can drive movement/combat/purchases/abilities against the live {@link #gameLoop()}. */
    public PlayerState player() {
        return player;
    }

    /** The live, fully-wired {@link GameLoop} -- every {@code setXxx}/optional system this constructor loaded is already attached. */
    public GameLoop gameLoop() {
        return loop;
    }

    /** The real {@code weapons.json}-backed registry this loop's combat uses -- exposed (part 86) so a caller like {@link ConsoleDriver} can look weapons up by name for {@link GameLoop#tickCombat}. */
    public WeaponRegistry weapons() {
        return weapons;
    }

    /** Every mod that loaded successfully at startup (part 89) -- see {@link ModLoader}'s class doc. Empty if {@link ServerConfig#modsDir} is unset or no mod JARs were found there. */
    public List<LoadedMod> loadedMods() {
        return loadedMods;
    }

    /** The shared event bus {@link #loop} fires real simulation events to and every loaded mod subscribes through (part 89) -- see {@link ModEventBus}'s class doc. */
    public ModEventBus events() {
        return events;
    }

    /**
     * Writes {@link #player()}'s current state to {@link ServerConfig#playerSaveFilePath} right
     * now (part 87) -- a no-op if that field is unset (persistence disabled). {@link #close()}
     * already calls this automatically; exposed as its own method so a caller (e.g. {@link
     * ConsoleDriver}'s {@code save} command) can save on demand without waiting for shutdown.
     */
    public void saveNow() throws IOException {
        if (playerSavePath != null) {
            PlayerStateSave.save(player, playerSavePath);
            eventSink.accept("Saved session to " + playerSavePath);
        }
    }

    @Override
    public void run() {
        long tickNumber = 0;
        while (running) {
            tickNumber++;
            List<String> tickLines = new ArrayList<>();
            // Deliberately NOT calling tickMovement/tickHazardCheck/tickCombat here -- see this
            // class's doc for why (no real automated-input or anomaly-placement evidence exists).
            loop.tickInventoryCheck(player, tickLines::add);
            loop.tickSpawns(1.0, tickLines::add);
            loop.tickEmission(1.0, tickLines::add);
            if (!tickLines.isEmpty()) {
                eventSink.accept("--- tick " + tickNumber + " ---");
                for (String line : tickLines) {
                    eventSink.accept(line);
                }
            }
            events.fire(new ServerTickEvent(tickNumber)); // part 89 -- fired every iteration, regardless of whether this tick produced any printed line
            try {
                Thread.sleep(config.tickIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void close() {
        running = false;
        events.fire(new ServerStoppingEvent());
        ModLoader.unloadAll(loadedMods, eventSink);
        try {
            saveNow();
        } catch (IOException e) {
            eventSink.accept("WARNING: failed to save session on shutdown (" + e.getMessage() + ") -- state from this run was not persisted");
        }
    }
}
