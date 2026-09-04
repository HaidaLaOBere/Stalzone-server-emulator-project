package net.stalzone.emulator.economy;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link BarterItemCatalog}, using a fixture directory with real filenames
 * drawn directly from the real {@code textures/items/barter/} folder (part 53): a subset of
 * real zone-scoped entries (including the mg/pd/td names this class exists to corroborate),
 * a real standalone item, and a real bare numeric-id item.
 */
public final class BarterItemCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "mg.node.ol",
            "mg.signal.ol",
            "pd.mining.ol",
            "pd.mining_add.ol",
            "bar.npc.ol",
            "td.mob.ol",
            "limansk.boss.ol",
            "ffa_token.ol",
            "28703.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("BarterItemCatalog.load finds every real zone prefix in the fixture", () -> {
            BarterItemCatalog catalog = loadFixture();
            assertEquals(5, catalog.zoneCount(), "zone count");
            assertTrue(catalog.zoneNames().contains("mg"), "mg present");
            assertTrue(catalog.zoneNames().contains("pd"), "pd present");
            assertTrue(catalog.zoneNames().contains("td"), "td present");
            assertTrue(catalog.zoneNames().contains("bar"), "bar present");
            assertTrue(catalog.zoneNames().contains("limansk"), "limansk present");
        }));

        out.add(test("categoriesFor: returns every real category for a zone, including _add variants", () -> {
            BarterItemCatalog catalog = loadFixture();
            List<String> pdCategories = catalog.categoriesFor("pd");
            assertEquals(2, pdCategories.size(), "pd category count");
            assertTrue(pdCategories.contains("mining"), "pd has mining");
            assertTrue(pdCategories.contains("mining_add"), "pd has mining_add as a distinct category");
        }));

        out.add(test("categoriesFor: returns exactly the fixture's 2 mg categories", () -> {
            BarterItemCatalog catalog = loadFixture();
            List<String> mgCategories = catalog.categoriesFor("mg");
            assertEquals(2, mgCategories.size(), "mg category count");
            assertTrue(mgCategories.contains("node"), "mg has node");
            assertTrue(mgCategories.contains("signal"), "mg has signal");
        }));

        out.add(test("categoriesFor: returns empty (not null) for an unknown zone", () -> {
            BarterItemCatalog catalog = loadFixture();
            assertEquals(0, catalog.categoriesFor("nonexistent_zone").size(), "unknown zone category count");
        }));

        out.add(test("standaloneItemNames: real non-zone-scoped item is separated out", () -> {
            BarterItemCatalog catalog = loadFixture();
            assertEquals(1, catalog.standaloneItemNames().size(), "standalone count");
            assertTrue(catalog.standaloneItemNames().contains("ffa_token"), "ffa_token present");
        }));

        out.add(test("numericIdItemNames: a bare numeric-id file is separated from named items", () -> {
            BarterItemCatalog catalog = loadFixture();
            assertEquals(1, catalog.numericIdItemNames().size(), "numeric id count");
            assertTrue(catalog.numericIdItemNames().contains("28703"), "28703 present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            BarterItemCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("BarterItemCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("barter-fixture-empty");
                dir.toFile().deleteOnExit();
                BarterItemCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static BarterItemCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("barter-fixture");
        dir.toFile().deleteOnExit();
        for (String name : FIXTURE_FILES) {
            Path file = dir.resolve(name);
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return BarterItemCatalog.load(dir);
    }
}
