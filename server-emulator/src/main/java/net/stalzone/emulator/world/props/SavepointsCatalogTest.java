package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link SavepointsCatalog}, using a fixture built from real {@code
 * savepoints.id.*.name} lines drawn directly from {@code stalker/lang/en.lang} (part 142,
 * live-verified on device) -- the real fallback/location-name corroboration pair, and two real
 * duplicate-name savepoint groups.
 */
public final class SavepointsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "savepoints.id.fallback_stalkers.name=Ataman's HQ",
            "savepoints.id.fallback_bandits.name=Boathouse",
            "savepoints.id.kontrolnaya_tochka_1.name=Checkpoint_1",
            "savepoints.id.poligon_kontrolnaya_tochka.name=Checkpoint",
            "savepoints.id.school_1.name=School beginning",
            "savepoints.id.school_2.name=School beginning"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("savepoints-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SavepointsCatalog loadFixture() throws IOException {
        return SavepointsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("the real fallback savepoints resolve the same real display names as the location-registry corroboration", () -> {
            SavepointsCatalog catalog = loadFixture();
            assertEquals("Ataman's HQ", catalog.name("fallback_stalkers"), "fallback_stalkers name");
            assertEquals("Boathouse", catalog.name("fallback_bandits"), "fallback_bandits name");
        }));

        out.add(test("two real duplicate-name savepoint groups resolve as distinct ids sharing literal text", () -> {
            SavepointsCatalog catalog = loadFixture();
            assertEquals("Checkpoint_1", catalog.name("kontrolnaya_tochka_1"), "kontrolnaya_tochka_1 name");
            assertEquals("Checkpoint", catalog.name("poligon_kontrolnaya_tochka"), "poligon_kontrolnaya_tochka name");
            assertEquals("School beginning", catalog.name("school_1"), "school_1 name");
            assertEquals("School beginning", catalog.name("school_2"), "school_2 name");
        }));

        out.add(test("ids() is sorted and unknown ids resolve to null rather than throwing", () -> {
            SavepointsCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(6, ids.size(), "6 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.name("not_a_real_savepoint"), "unknown savepoint id");
        }));

        return out;
    }
}
