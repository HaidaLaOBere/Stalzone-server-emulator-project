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
 * design, not reverse-engineered data. Nothing here should be read as a claim
 * about the real game's actual default values — these are our own emulator's tunable
 * defaults, chosen arbitrarily until we have evidence (a capture, decompiled code, or
 * doc) for what STALZONE itself uses.
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

    public boolean packetLoggingEnabled = true;
    public int packetLogHexBytes = 64;

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
            cfg.packetLoggingEnabled = Boolean.parseBoolean(
                    p.getProperty("packetLoggingEnabled", String.valueOf(cfg.packetLoggingEnabled)));
            cfg.packetLogHexBytes = Integer.parseInt(
                    p.getProperty("packetLogHexBytes", String.valueOf(cfg.packetLogHexBytes)));
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
        p.setProperty("packetLoggingEnabled", String.valueOf(packetLoggingEnabled));
        p.setProperty("packetLogHexBytes", String.valueOf(packetLogHexBytes));
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
