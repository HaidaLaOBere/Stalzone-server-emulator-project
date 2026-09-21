package net.stalzone.emulator.config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The "full control over what can happen on your server" knobs the project exists for
 * — spawn rates, damage values, etc. There is still no NETWORKED gameplay logic (the
 * wire protocol is still encrypted/unknown, see docs/RESEARCH_LOG.md part 7), but as of
 * part 48, all three of these fields are genuinely wired into real local simulation
 * code: {@link #damageMultiplier} scales real weapon-stats data in
 * {@code items/WeaponRegistry#effectiveDamage} (part 19), {@link #spawnRateMultiplier}
 * drives real mob-spawn cadence in {@code mobs/SpawnScheduler} (part 29), and {@link
 * #lootRateMultiplier} scales the drop-chance roll in {@code loot/LootRoller} (part 48) --
 * all three reachable from {@code session/GameLoop}. Unlike the other two, there is no real
 * STALZONE loot/drop-rate data to scale in the first place (this project confirmed one is
 * very likely absent from client files entirely, docs/RESEARCH_LOG.md part 43) -- see
 * {@code loot/LootTable}'s class doc for why the loot system it scales is emulator-original
 * design, not reverse-engineered data. As of part 81, {@link #emissionRateMultiplier} scales
 * the real evidenced 2-4 hour "emission"/"blowout" interval in {@code emission/EmissionCycle}
 * (part 80's real handbook-article finding) the same way {@code spawnRateMultiplier} scales
 * mob-spawn cadence -- see that class's doc for exactly which of its constants are real,
 * evidenced timing versus this emulator's own disclosed placeholder. Nothing here should be
 * read as a claim about the real game's actual default values — these are our own emulator's
 * tunable defaults, chosen arbitrarily until we have evidence (a capture, decompiled code, or
 * doc) for what STALZONE itself uses.
 *
 * As of part 84, {@link #stalzoneInstallDir} is the one new field that actually turns this
 * config into a runnable local simulation ({@code session/SingleplayerServer}) instead of just
 * tuning knobs nothing reads yet: when set, {@code Main} loads a real zone's region data, real
 * {@code moving.txt}/{@code detectors.txt}/{@code weight.txt}, and real {@code .smm} mob configs
 * from underneath it, at the same real relative paths this project's own tools/tests already
 * load them from (confirmed against a real STALZONE install: {@code map_cache/<mapCacheVersion>/
 * <startZone>}, {@code modassets/assets/stalcraft/moving.txt}, {@code modassets/assets/stalker/
 * detectors.txt}, {@code modassets/assets/stalker/weight.txt}, {@code modassets/assets/
 * stalkermobs/configs}). {@link #weaponsJsonPath}, {@link #lootTablesJsonPath} and {@link
 * #mobHealthTableJsonPath} deliberately do NOT derive from {@link #stalzoneInstallDir} --
 * {@code weapons.json} is this project's own wiki-sourced dataset (part 19, not a real client
 * file), and the loot/mob-health tables are disclosed emulator-original tuning data (parts 48/49)
 * -- so they default to this project's own bundled {@code data/...} files instead. Leaving
 * {@link #stalzoneInstallDir} unset keeps {@code Main} exactly at its pre-part-84 behavior (UDP
 * packet logging only, no simulation) -- see {@code session/SingleplayerServer}'s class doc for
 * the full design and for what it deliberately does NOT do (no real player movement/combat input
 * exists yet since the wire protocol is still unread).
 *
 * As of part 86, {@link #enableConsoleDriver} (default {@code true}) controls whether {@code Main}
 * also starts {@code session/ConsoleDriver} alongside {@code SingleplayerServer} -- a stdin
 * command console that is, until the real wire protocol is read, this project's only way to
 * actually call {@code GameLoop}'s movement/hazard/combat/purchase/ability/quest tick methods
 * against a real running server rather than only {@code GameLoopDemo}'s disclosed fabricated demo
 * script. See {@code ConsoleDriver}'s own class doc for the full command list and rationale.
 *
 * As of part 87, {@link #playerSaveFilePath} (default {@code data/saves/player.properties}, set
 * to {@code null} to disable) is where {@code SingleplayerServer} persists the local {@code
 * PlayerState} between runs -- before this, every restart discarded any state a {@code
 * ConsoleDriver} session had built up (position, inventory, wallet, class, abilities, quest log).
 * See {@code session/PlayerStateSave}'s class doc for the file format (this emulator's own,
 * explicitly not a copy of any real STALZONE save format).
 *
 * As of part 89, {@link #modsDir} (default {@code mods}) is where {@code SingleplayerServer} looks
 * for third-party mod JARs to load at startup -- this project's own answer to the original ask's
 * "later modding support (similar to what SPT team has done)". A missing or empty directory is a
 * normal "no mods installed" state, same as every other optional field above; set this to
 * {@code null} to skip mod loading entirely. See {@code modding/ModLoader}'s class doc for the
 * full discovery/loading design and {@code examples/mods/} for a buildable example mod.
 */
public final class ServerConfig {

    public String bindAddress = "0.0.0.0";
    // Matches the real client's observed port (89.222.108.193:29450, see RESEARCH_LOG.md)
    // purely so a hosts-file/DNS redirect points at a server listening where the client
    // expects one. Does NOT imply we can speak the real wire protocol yet.
    public int port = 29450;

    // Gameplay tuning placeholders (Phase 3+, not wired to anything yet).
    public double spawnRateMultiplier = 1.0;
    public double damageMultiplier = 1.0;
    public double lootRateMultiplier = 1.0;
    public double emissionRateMultiplier = 1.0;

    public boolean packetLoggingEnabled = true;
    public int packetLogHexBytes = 64;

    // Singleplayer-server bootstrap (part 84). Null = SingleplayerServer never starts; see this
    // class's doc above and session/SingleplayerServer's class doc for the full picture.
    public String stalzoneInstallDir = null;
    // Milestone-1's real zone (docs/PROJECT_PLAN.md), same default GameLoopDemo already uses.
    public String startZone = "dungeon_bolota_tutorial";
    // Real observed map_cache subfolder name in the client version this project has snapshots
    // of (Aug 2026) -- a real STALZONE client detail, not a tunable, but exposed in case a future
    // client version changes it (see this class's doc above for the full real path it's part of).
    public String mapCacheVersion = "5.0";
    // This project's own bundled, wiki-sourced weapon dataset (part 19) -- NOT present in a real
    // client install, so this does not derive from stalzoneInstallDir. Relative paths are
    // resolved against the current working directory, same as configPath itself.
    public String weaponsJsonPath = "data/weapons/weapons.json";
    // Optional emulator-original tuning data (parts 48/49 -- see loot/LootTable's and
    // mobs/MobHealthTable's class docs for why neither is reverse-engineered data). Null =
    // that system stays unconfigured, same as GameLoopDemo when its own optional args are omitted.
    public String lootTablesJsonPath = null;
    public String mobHealthTableJsonPath = null;
    // Real-time seconds between world-simulation ticks. Not an evidenced real tick rate -- this
    // emulator's own disclosed pacing choice, purely so a running server doesn't spin a tight loop.
    public long tickIntervalMillis = 1000;
    // Part 86: whether Main also starts session/ConsoleDriver (reading operator commands from
    // stdin) alongside SingleplayerServer. Only meaningful when stalzoneInstallDir is set -- see
    // ConsoleDriver's own class doc. Default true since it is harmless (idle waiting on stdin)
    // and is this project's only way to actually drive tickMovement/tickHazardCheck/tickCombat/
    // tickPurchase/tickAbilityUse/tickQuestEvent against a real running server today.
    public boolean enableConsoleDriver = true;
    // Part 87: this emulator's own session-persistence file for the local PlayerState (position,
    // inventory, wallet, class, unlocked abilities, quest log) -- NOT a real client file, NOT
    // derived from stalzoneInstallDir (same standing as weaponsJsonPath/lootTablesJsonPath). Set
    // to null to disable persistence entirely (every run always starts fresh, pre-part-87
    // behavior); see session/PlayerStateSave's class doc for the full format and rationale.
    public String playerSaveFilePath = "data/saves/player.properties";
    // Part 89: where SingleplayerServer looks for third-party mod JARs (modding/ModLoader).
    // Relative paths resolve against the current working directory, same as configPath itself.
    // A missing/empty directory is a normal "no mods installed" state, not an error. Set to null
    // to skip mod loading entirely, same "null = feature off" pattern as lootTablesJsonPath.
    public String modsDir = "mods";

    public static ServerConfig loadOrCreateDefault(Path path) throws IOException {
        ServerConfig cfg = new ServerConfig();
        if (Files.exists(path)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                p.load(in);
            }
            cfg.bindAddress = p.getProperty("bindAddress", cfg.bindAddress);
            cfg.port = Integer.parseInt(p.getProperty("port", String.valueOf(cfg.port)));
            cfg.spawnRateMultiplier = Double.parseDouble(
                    p.getProperty("spawnRateMultiplier", String.valueOf(cfg.spawnRateMultiplier)));
            cfg.damageMultiplier = Double.parseDouble(
                    p.getProperty("damageMultiplier", String.valueOf(cfg.damageMultiplier)));
            cfg.lootRateMultiplier = Double.parseDouble(
                    p.getProperty("lootRateMultiplier", String.valueOf(cfg.lootRateMultiplier)));
            cfg.emissionRateMultiplier = Double.parseDouble(
                    p.getProperty("emissionRateMultiplier", String.valueOf(cfg.emissionRateMultiplier)));
            cfg.packetLoggingEnabled = Boolean.parseBoolean(
                    p.getProperty("packetLoggingEnabled", String.valueOf(cfg.packetLoggingEnabled)));
            cfg.packetLogHexBytes = Integer.parseInt(
                    p.getProperty("packetLogHexBytes", String.valueOf(cfg.packetLogHexBytes)));
            cfg.stalzoneInstallDir = p.getProperty("stalzoneInstallDir", cfg.stalzoneInstallDir);
            cfg.startZone = p.getProperty("startZone", cfg.startZone);
            cfg.mapCacheVersion = p.getProperty("mapCacheVersion", cfg.mapCacheVersion);
            cfg.weaponsJsonPath = p.getProperty("weaponsJsonPath", cfg.weaponsJsonPath);
            cfg.lootTablesJsonPath = p.getProperty("lootTablesJsonPath", cfg.lootTablesJsonPath);
            cfg.mobHealthTableJsonPath = p.getProperty("mobHealthTableJsonPath", cfg.mobHealthTableJsonPath);
            cfg.tickIntervalMillis = Long.parseLong(
                    p.getProperty("tickIntervalMillis", String.valueOf(cfg.tickIntervalMillis)));
            cfg.enableConsoleDriver = Boolean.parseBoolean(
                    p.getProperty("enableConsoleDriver", String.valueOf(cfg.enableConsoleDriver)));
            cfg.playerSaveFilePath = p.getProperty("playerSaveFilePath", cfg.playerSaveFilePath);
            cfg.modsDir = p.getProperty("modsDir", cfg.modsDir);
        } else {
            cfg.writeTo(path);
        }
        return cfg;
    }

    public void writeTo(Path path) throws IOException {
        Properties p = new Properties();
        p.setProperty("bindAddress", bindAddress);
        p.setProperty("port", String.valueOf(port));
        p.setProperty("spawnRateMultiplier", String.valueOf(spawnRateMultiplier));
        p.setProperty("damageMultiplier", String.valueOf(damageMultiplier));
        p.setProperty("lootRateMultiplier", String.valueOf(lootRateMultiplier));
        p.setProperty("emissionRateMultiplier", String.valueOf(emissionRateMultiplier));
        p.setProperty("packetLoggingEnabled", String.valueOf(packetLoggingEnabled));
        p.setProperty("packetLogHexBytes", String.valueOf(packetLogHexBytes));
        // stalzoneInstallDir/lootTablesJsonPath/mobHealthTableJsonPath are optional and left out
        // of the written file entirely when unset (Properties.setProperty rejects null values;
        // loadOrCreateDefault's getProperty(key, null) default already treats a missing key the
        // same as "not configured").
        if (stalzoneInstallDir != null) {
            p.setProperty("stalzoneInstallDir", stalzoneInstallDir);
        }
        p.setProperty("startZone", startZone);
        p.setProperty("mapCacheVersion", mapCacheVersion);
        p.setProperty("weaponsJsonPath", weaponsJsonPath);
        if (lootTablesJsonPath != null) {
            p.setProperty("lootTablesJsonPath", lootTablesJsonPath);
        }
        if (mobHealthTableJsonPath != null) {
            p.setProperty("mobHealthTableJsonPath", mobHealthTableJsonPath);
        }
        p.setProperty("tickIntervalMillis", String.valueOf(tickIntervalMillis));
        p.setProperty("enableConsoleDriver", String.valueOf(enableConsoleDriver));
        if (playerSaveFilePath != null) {
            p.setProperty("playerSaveFilePath", playerSaveFilePath);
        }
        if (modsDir != null) {
            p.setProperty("modsDir", modsDir);
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            p.store(out, "STALZONE server-emulator config. All gameplay multipliers below are "
                    + "emulator-defined placeholders, not confirmed real-game defaults -- see "
                    + "docs/RESEARCH_LOG.md. Not wired to real gameplay logic yet (no protocol "
                    + "implementation exists until the wire encryption is cracked).");
        }
    }
}
