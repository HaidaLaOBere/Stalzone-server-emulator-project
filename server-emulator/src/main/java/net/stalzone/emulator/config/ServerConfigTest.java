package net.stalzone.emulator.config;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ServerConfig}'s load/write round-trip -- pure Java logic against a
 * temp file, no real client files needed. {@link ServerConfig} itself had no dedicated test file
 * before part 84 (its four gameplay multipliers were exercised only indirectly, via whichever
 * {@code GameLoop}-adjacent part wired each one up). Covers the part-84 fields specifically: the
 * new String fields are the first nullable-by-default ones this class has ever had, so the
 * "missing key stays null" and "null field is left out of the written file" behaviors are worth
 * pinning explicitly (see {@link ServerConfig#writeTo}'s comment on why).
 */
public final class ServerConfigTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("loadOrCreateDefault on a missing path creates one with all defaults, including part-84 fields", () -> {
            Path path = Files.createTempDirectory("serverconfig-test").resolve("server.properties");
            ServerConfig cfg = ServerConfig.loadOrCreateDefault(path);
            assertTrue(Files.exists(path), "config file created");
            assertNull(cfg.stalzoneInstallDir, "stalzoneInstallDir defaults to unset");
            assertEquals("dungeon_bolota_tutorial", cfg.startZone, "startZone default");
            assertEquals("5.0", cfg.mapCacheVersion, "mapCacheVersion default");
            assertEquals("data/weapons/weapons.json", cfg.weaponsJsonPath, "weaponsJsonPath default");
            assertNull(cfg.lootTablesJsonPath, "lootTablesJsonPath defaults to unset");
            assertNull(cfg.mobHealthTableJsonPath, "mobHealthTableJsonPath defaults to unset");
            assertEquals(1000L, cfg.tickIntervalMillis, "tickIntervalMillis default");
        }));

        out.add(test("writeTo then loadOrCreateDefault round-trips a configured stalzoneInstallDir", () -> {
            Path path = Files.createTempDirectory("serverconfig-test").resolve("server.properties");
            ServerConfig original = new ServerConfig();
            original.stalzoneInstallDir = "/fake/install/dir";
            original.startZone = "new_bar";
            original.mapCacheVersion = "5.1";
            original.lootTablesJsonPath = "data/loot/example_loot_tables.json";
            original.mobHealthTableJsonPath = "data/mobs/example_mob_health.json";
            original.tickIntervalMillis = 250;
            original.writeTo(path);

            ServerConfig reloaded = ServerConfig.loadOrCreateDefault(path);
            assertEquals("/fake/install/dir", reloaded.stalzoneInstallDir, "stalzoneInstallDir round-trip");
            assertEquals("new_bar", reloaded.startZone, "startZone round-trip");
            assertEquals("5.1", reloaded.mapCacheVersion, "mapCacheVersion round-trip");
            assertEquals("data/loot/example_loot_tables.json", reloaded.lootTablesJsonPath, "lootTablesJsonPath round-trip");
            assertEquals("data/mobs/example_mob_health.json", reloaded.mobHealthTableJsonPath, "mobHealthTableJsonPath round-trip");
            assertEquals(250L, reloaded.tickIntervalMillis, "tickIntervalMillis round-trip");
        }));

        out.add(test("isConfigured (SingleplayerServer) is false for a fresh default config", () -> {
            ServerConfig cfg = new ServerConfig();
            assertTrue(!net.stalzone.emulator.session.SingleplayerServer.isConfigured(cfg),
                    "unset stalzoneInstallDir means SingleplayerServer never starts");
            cfg.stalzoneInstallDir = "/fake/install/dir";
            assertTrue(net.stalzone.emulator.session.SingleplayerServer.isConfigured(cfg),
                    "set stalzoneInstallDir means SingleplayerServer is configured to start");
        }));

        return out;
    }
}
