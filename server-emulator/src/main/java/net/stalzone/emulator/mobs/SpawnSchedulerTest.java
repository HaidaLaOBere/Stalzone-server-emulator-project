package net.stalzone.emulator.mobs;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link SpawnScheduler}'s part-88 live-retune API only ({@link
 * SpawnScheduler#spawnRateMultiplier()}/{@link SpawnScheduler#setSpawnRateMultiplier}) -- {@code
 * null} is passed for the {@link MobRegistry} constructor argument here, since neither of those
 * two methods (nor the constructor itself) ever dereferences it, same pattern already used
 * elsewhere in this project (e.g. {@code ConsoleDriverTest}) for a real dependency a given code
 * path never touches. {@link SpawnScheduler#tick} itself DOES need a real, populated {@link
 * MobRegistry} (real mob data, not a small bundleable fixture) and has never had a dedicated unit
 * test for that reason -- it is verified end-to-end via a live run instead, same established
 * precedent as {@code SingleplayerServer}/{@code GameLoopDemo}. This is the first dedicated test
 * file for {@link SpawnScheduler}; it exists now specifically because part 88 added real,
 * deterministically-testable logic (a live setter) that didn't exist before.
 */
public final class SpawnSchedulerTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("spawnRateMultiplier() starts at the ServerConfig value passed to the constructor", () -> {
            ServerConfig config = new ServerConfig();
            config.spawnRateMultiplier = 3.5;
            SpawnScheduler scheduler = new SpawnScheduler(null, config, new Random(1));
            assertEquals(3.5, scheduler.spawnRateMultiplier(), 1e-9, "constructor-captured value");
        }));

        out.add(test("setSpawnRateMultiplier (part 88) live-updates the value the getter reports", () -> {
            SpawnScheduler scheduler = new SpawnScheduler(null, new ServerConfig(), new Random(1));
            assertEquals(1.0, scheduler.spawnRateMultiplier(), 1e-9, "ServerConfig default before any retune");
            scheduler.setSpawnRateMultiplier(250.0);
            assertEquals(250.0, scheduler.spawnRateMultiplier(), 1e-9, "getter reflects the live override immediately, before the next tick() ever runs");
        }));

        return out;
    }
}
