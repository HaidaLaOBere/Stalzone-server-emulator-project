package net.stalzone.emulator.world;

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
 * Regression tests for {@link LocationNameRegistry}, using a fixture file built from real {@code
 * location.<id>.name} lines drawn directly from {@code stalker/lang/en.lang} (part 105, live-
 * verified on device) -- a representative slice covering the part-26/72 {@code mg}/{@code pd}/
 * {@code td} resolution, the part-104 {@code spusk} resolution, an "Unknown"-valued real entry,
 * the self-referential {@code dungeon_x18} entry, and one unrelated deeper {@code location.*} key
 * ({@code location.td.feature.0}) that must NOT be misread as an identity line.
 */
public final class LocationNameRegistryTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "location.mg.name=The Dead City",
            "location.pd.name=The Path of Fools",
            "location.td.name=Black Willows",
            "location.td.desc=Due to the constant presence of military stalkers, stalkers prefer not to set up major camps in the Black Willows.",
            "location.td.feature.0=To the southeast of the location there is a cunningly hidden archianomaly, which often spawns artifacts.",
            "location.dungeon_spusk.name=Vagant's Den",
            "location.dungeon_school.name=Vlasov's School",
            "location.angar_s.name=Unknown",
            "location.dungeon_x18.name=dungeon_x18",
            "location.inside_pd.name=The Path of Fools",
            "location.dungeon_barsani.name=The Path of Fools"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("location-name-registry-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static LocationNameRegistry loadFixture() throws IOException {
        return LocationNameRegistry.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load only counts real location.<id>.name identity lines, not deeper location.* keys", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals(9, registry.size(), "identity line count (feature.0/desc excluded)");
        }));

        out.add(test("realName resolves the part-26/72 mg/pd/td short codes", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("The Dead City", registry.realName("mg"), "mg");
            assertEquals("The Path of Fools", registry.realName("pd"), "pd");
            assertEquals("Black Willows", registry.realName("td"), "td");
        }));

        out.add(test("realName resolves the part-104 spusk lead via dungeon_spusk", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("Vagant's Den", registry.realName("dungeon_spusk"), "dungeon_spusk");
        }));

        out.add(test("realName resolves dungeon_school to the real Vlasov's School tie", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("Vlasov's School", registry.realName("dungeon_school"), "dungeon_school");
        }));

        out.add(test("realName returns the literal Unknown placeholder, not null, for an unnamed real entry", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("Unknown", registry.realName("angar_s"), "angar_s (real but unnamed by EXBO)");
        }));

        out.add(test("realName returns null for a locationId this registry has never heard of", () -> {
            LocationNameRegistry registry = loadFixture();
            assertNull(registry.realName("not_a_real_zone_code"), "unknown locationId");
        }));

        out.add(test("the same real display name can legitimately resolve from several distinct ids", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("The Path of Fools", registry.realName("pd"), "pd");
            assertEquals("The Path of Fools", registry.realName("inside_pd"), "inside_pd");
            assertEquals("The Path of Fools", registry.realName("dungeon_barsani"), "dungeon_barsani");
        }));

        out.add(test("a self-referential real entry (dungeon_x18) is still surfaced as-is", () -> {
            LocationNameRegistry registry = loadFixture();
            assertEquals("dungeon_x18", registry.realName("dungeon_x18"), "dungeon_x18 (unlocalized real value)");
        }));

        out.add(test("locationIds returns every real id, alphabetical", () -> {
            LocationNameRegistry registry = loadFixture();
            List<String> ids = registry.locationIds();
            assertEquals(9, ids.size(), "id count");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "alphabetical order at index " + i);
            }
            assertTrue(ids.contains("td"), "contains td");
            assertTrue(ids.contains("dungeon_spusk"), "contains dungeon_spusk");
        }));

        return out;
    }
}
