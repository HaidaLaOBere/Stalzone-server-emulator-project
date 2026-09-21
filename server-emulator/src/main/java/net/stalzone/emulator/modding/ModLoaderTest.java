package net.stalzone.emulator.modding;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for the directory-discovery portion of {@link ModLoader#loadAll} (part 89) --
 * the "no mods installed" states only, which are pure filesystem logic and need no real mod JAR
 * or {@code GameLoop}/{@code ServerConfig} instance. The real {@code ServiceLoader}/{@link
 * java.net.URLClassLoader} discovery-and-load path (a real mod JAR being found, instantiated, and
 * its {@link ServerMod#onLoad} called) is exercised only by live end-to-end verification (see
 * docs/RESEARCH_LOG.md part 89 and examples/mods/), same "no dedicated unit test for a
 * real-file-heavy path" precedent {@link net.stalzone.emulator.mobs.SpawnSchedulerTest} and
 * {@link net.stalzone.emulator.session.GameLoop}'s own constructor-heavy paths already set for
 * this project -- {@code loadAll}'s own null/errorSink-free signature for these two cases is what
 * makes them testable without any of that setup.
 */
public final class ModLoaderTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("loadAll on a null modsDir returns an empty list without touching the sink", () -> {
            List<LoadedMod> loaded = ModLoader.loadAll(null, null, null, null,
                    msg -> { throw new AssertionError("sink must not be called for a null modsDir: " + msg); });
            assertTrue(loaded.isEmpty(), "no mods loaded for a null modsDir");
        }));

        out.add(test("loadAll on a nonexistent modsDir path returns an empty list without touching the sink", () -> {
            Path missing = Files.createTempDirectory("modloader-test").resolve("does-not-exist");
            List<LoadedMod> loaded = ModLoader.loadAll(missing, null, null, null,
                    msg -> { throw new AssertionError("sink must not be called for a missing modsDir: " + msg); });
            assertTrue(loaded.isEmpty(), "no mods loaded for a nonexistent modsDir");
        }));

        out.add(test("loadAll on an existing but empty modsDir returns an empty list without touching the sink", () -> {
            Path emptyDir = Files.createTempDirectory("modloader-test");
            List<LoadedMod> loaded = ModLoader.loadAll(emptyDir, null, null, null,
                    msg -> { throw new AssertionError("sink must not be called when no *.jar files are present: " + msg); });
            assertTrue(loaded.isEmpty(), "no mods loaded when modsDir has no jar files");
        }));

        out.add(test("loadAll on a modsDir containing only non-jar files returns an empty list without touching the sink", () -> {
            Path dir = Files.createTempDirectory("modloader-test");
            Files.writeString(dir.resolve("readme.txt"), "not a mod");
            List<LoadedMod> loaded = ModLoader.loadAll(dir, null, null, null,
                    msg -> { throw new AssertionError("sink must not be called for non-jar files: " + msg); });
            assertTrue(loaded.isEmpty(), "non-jar files in modsDir are ignored entirely");
        }));

        out.add(test("unloadAll on an empty list is a silent no-op", () -> {
            ModLoader.unloadAll(new ArrayList<>(),
                    msg -> { throw new AssertionError("sink must not be called for an empty loaded list: " + msg); });
        }));

        return out;
    }
}
