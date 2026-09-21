package net.stalzone.emulator.tips;

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
 * Regression tests for {@link DailyExcuseCatalog}, using a fixture built from real {@code
 * quote.ziv.*} lines drawn directly from {@code stalker/lang/en.lang} (part 112, live-verified on
 * device) -- covering the real numbered-excuse lookup, the player's stock answer line, the real
 * "Inside Out"/"Plastinka" cross-references, and the day-number/npc-id listing accessors.
 */
public final class DailyExcuseCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "quote.ziv.answer=Okay, I'll drop by tomorrow.",
            "quote.ziv.1=Something wild happened today: there was a cargo-hauling competition at the warehouse.",
            "quote.ziv.33=All the rewards had to be rebalanced because their properties were too strong. We're waiting for the balancer to finish up.",
            "quote.ziv.332=A portal opened up in the warehouse, and not even to the Inside Out, but to some hellish dimension that started spilling out all sorts of demons that ruined the rewards. We'll buy new ones tomorrow.",
            "quote.ziv.344=One Plastinka borrowed five million rubles from the warehouse and did not return the money, so we can't pay salaries. We'll look for him and try to get our money back.",
            "quote.ziv.362=We had the whole office testing new mechanics that our game designers came up with, so we didn't have much time for gifts.",
            "quote.ziv.365=A pack of piggies broke into our gift warehouse. Can you believe it? Now we're looking for hunters who can chase the piggies out of there. I think things should be fine tomorrow!"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("daily-excuse-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static DailyExcuseCatalog loadFixture() throws IOException {
        return DailyExcuseCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("answer resolves the real player stock reply", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            assertEquals("Okay, I'll drop by tomorrow.", catalog.answer("ziv"), "ziv answer");
        }));

        out.add(test("excuse resolves real numbered day slots, including a meta/fourth-wall-breaking one", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            assertTrue(catalog.excuse("ziv", 1).contains("cargo-hauling competition"), "day 1");
            assertTrue(catalog.excuse("ziv", 33).contains("rebalanced"), "day 33 (meta)");
            assertTrue(catalog.excuse("ziv", 362).contains("game designers"), "day 362 (meta)");
        }));

        out.add(test("excuse resolves the real Inside Out cross-reference and the Plastinka name lead", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            assertTrue(catalog.excuse("ziv", 332).contains("Inside Out"), "day 332 Inside Out tie");
            assertTrue(catalog.excuse("ziv", 344).contains("Plastinka"), "day 344 Plastinka lead");
        }));

        out.add(test("excuseCount/excuseDayNumbers reflect the real fixture data, sorted ascending", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            assertEquals(6, catalog.excuseCount("ziv"), "ziv excuse count");
            List<Integer> days = catalog.excuseDayNumbers("ziv");
            for (int i = 1; i < days.size(); i++) {
                assertTrue(days.get(i - 1) < days.get(i), "sorted ascending");
            }
            assertEquals(Integer.valueOf(365), days.get(days.size() - 1), "last day is 365");
        }));

        out.add(test("npcIds is sorted and contains the real ziv id", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            List<String> ids = catalog.npcIds();
            assertEquals(1, ids.size(), "one real npc in fixture");
            assertEquals("ziv", ids.get(0), "ziv");
        }));

        out.add(test("unknown npc/day combinations resolve to null/zero/empty", () -> {
            DailyExcuseCatalog catalog = loadFixture();
            assertNull(catalog.answer("not_a_real_npc"), "unknown npc answer");
            assertNull(catalog.excuse("ziv", 999), "unknown day for known npc");
            assertNull(catalog.excuse("not_a_real_npc", 1), "unknown npc excuse");
            assertEquals(0, catalog.excuseCount("not_a_real_npc"), "unknown npc count");
            assertEquals(0, catalog.excuseDayNumbers("not_a_real_npc").size(), "unknown npc days");
        }));

        return out;
    }
}
