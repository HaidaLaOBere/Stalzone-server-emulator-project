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
 * Regression tests for {@link SettlementRegistry}, using a fixture file built from real {@code
 * settlement.id.<id>.title} lines drawn directly from {@code stalker/lang/en.lang} (part 105,
 * live-verified on device) -- covers the headline {@code csky}="Ataman's HQ" correction, a
 * direct-id title, both confirmed {@code savezone_agro}/{@code savezone_td} aliases, and the
 * real unprefixed {@code sever} alias of {@code bar_sever} (present in the real file, not one of
 * {@link SettlementRegistry#ALL_KNOWN_SETTLEMENTS}).
 */
public final class SettlementRegistryTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "settlement.id.csky.title=Ataman's HQ",
            "settlement.id.rostok.title=The Bar",
            "settlement.id.agro.title=Digger Hut",
            "settlement.id.td.title=Digger Plot",
            "settlement.id.bar_sever.title=Drunken Geiger Bar",
            "settlement.id.sever.title=Drunken Geiger Bar"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("settlement-registry-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static LangCatalog loadFixtureCatalog() throws IOException {
        return LangCatalog.load(writeFixture());
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ALL_KNOWN_SETTLEMENTS still has all 15 real confirmed names", () -> {
            assertEquals(15, SettlementRegistry.ALL_KNOWN_SETTLEMENTS.length, "settlement count");
        }));

        out.add(test("realTitle resolves the headline csky correction to the real confirmed title", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertEquals("Ataman's HQ", SettlementRegistry.realTitle("csky", catalog),
                    "csky (supersedes the earlier plausibly-Clear-Sky reading)");
        }));

        out.add(test("realTitle resolves a direct-id settlement title", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertEquals("The Bar", SettlementRegistry.realTitle("rostok", catalog), "rostok");
        }));

        out.add(test("realTitle resolves the savezone_agro/savezone_td aliases", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertEquals("Digger Hut", SettlementRegistry.realTitle("savezone_agro", catalog), "savezone_agro -> agro");
            assertEquals("Digger Plot", SettlementRegistry.realTitle("savezone_td", catalog), "savezone_td -> td");
        }));

        out.add(test("realTitle resolves bar_sever directly (not via the redundant sever alias)", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertEquals("Drunken Geiger Bar", SettlementRegistry.realTitle("bar_sever", catalog), "bar_sever");
        }));

        out.add(test("realTitle returns null for a settlementId with no crosswalk in catalog", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertNull(SettlementRegistry.realTitle("garage", catalog), "garage (not in this fixture)");
        }));

        out.add(test("realTitle returns null for a null settlementId", () -> {
            LangCatalog catalog = loadFixtureCatalog();
            assertNull(SettlementRegistry.realTitle(null, catalog), "null settlementId");
        }));

        return out;
    }
}
