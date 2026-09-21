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
 * Regression tests for {@link PersonalStorageCatalog}, using a fixture built from real {@code
 * personal_storage.*} lines drawn directly from {@code stalker/lang/en.lang} (part 159,
 * live-verified on device) -- the real per-settlement unlock text and its rich CnpsCatalog
 * reputation-rank corroboration chain.
 */
public final class PersonalStorageCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "personal_storage.availability.settlement.csky=Required reputation:\\nAtaman's HQ—Vodyanoy (5)",
            "personal_storage.availability.settlement.sever=Required reputation:\\nDrunken Geiger Bar—Legend of the Zone (7)",
            "personal_storage.availability.settlement.alliance_stalkers=Required reputation:\\nFrontier Base—General (7)\\nRise Base—Zone Guardian (7)",
            "personal_storage.availability.settlement.wnorth_10=Required reputation:\\nDrunken Geiger Bar—Legend of the Zone (10)",
            "personal_storage.availability.premium=Purchase Premium\\nto get access to this shelf"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("personal-storage-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PersonalStorageCatalog loadFixture() throws IOException {
        return PersonalStorageCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real settlement unlock text resolves, matching CnpsCatalog's exact top-of-ladder rank titles", () -> {
            PersonalStorageCatalog catalog = loadFixture();
            assertTrue(catalog.field("availability.settlement", "csky").contains("Vodyanoy"), "csky references real Vodyanoy rank");
            assertTrue(catalog.field("availability.settlement", "sever").contains("Legend of the Zone"), "sever references real Legend of the Zone rank");
        }));

        out.add(test("a real multi-line alliance requirement resolves, and the real sever(7)/wnorth_10(10) same-rank-different-number quirk is preserved exactly as found", () -> {
            PersonalStorageCatalog catalog = loadFixture();
            assertTrue(catalog.field("availability.settlement", "alliance_stalkers").contains("General"), "alliance_stalkers references real General rank");
            assertTrue(catalog.field("availability.settlement", "alliance_stalkers").contains("Zone Guardian"), "alliance_stalkers references real Zone Guardian rank");
            assertTrue(catalog.field("availability.settlement", "sever").contains("(7)"), "sever requires tier 7");
            assertTrue(catalog.field("availability.settlement", "wnorth_10").contains("(10)"), "wnorth_10 requires tier 10, same rank name as sever's tier 7");
        }));

        out.add(test("the flat availability.premium line lands as a separate id from availability.settlement, ids() is sorted, and unknown lookups resolve to null", () -> {
            PersonalStorageCatalog catalog = loadFixture();
            assertEquals("Purchase Premium\\nto get access to this shelf", catalog.field("availability", "premium"), "availability.premium is a distinct real id from availability.settlement");
            List<String> ids = catalog.ids();
            assertEquals(2, ids.size(), "2 real ids in fixture (availability, availability.settlement)");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "csky"), "unknown id");
        }));

        return out;
    }
}
