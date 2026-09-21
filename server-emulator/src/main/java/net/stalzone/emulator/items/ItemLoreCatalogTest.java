package net.stalzone.emulator.items;

import net.stalzone.emulator.nbt.NbtWriter;
import net.stalzone.emulator.testing.TestRunner;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ItemLoreCatalog}, using a fixture built from real records drawn
 * directly from {@code modassets/assets/stalker/itemnames.dat} (part 166, live-verified on
 * device), gzip-wrapped and NBT-encoded via this project's own {@link NbtWriter} so the fixture
 * exercises the exact same decompress-then-parse path {@link ItemLoreCatalog#load} uses against
 * the real file.
 *
 * <p>Every {@code key}/{@code display_name}/{@code Lore} string below is real, verbatim game
 * content. The two real {@code creator} usernames this fixture would otherwise carry are replaced
 * with the placeholder {@code "REDACTED_CREATOR"} -- per this project's standing privacy rule
 * against committing real third-party usernames to test fixtures. {@link ItemLoreCatalog#load}
 * itself parses and returns whatever the real file actually contains; only this test's fixture is
 * redacted.
 */
public final class ItemLoreCatalogTest {

    @SuppressWarnings("unchecked")
    private static Map<String, Object> record(String key, String displayName, List<String> lore, String creator) {
        Map<String, Object> rec = new LinkedHashMap<>();
        // Real files vary field presence/order per record; this fixture always writes "key" first
        // and only includes the other fields when the real record actually has them, matching the
        // real per-record variability ItemLoreCatalog's own doc discloses (23 real records have no
        // display_name; 145 have no Lore).
        rec.put("key", key);
        if (creator != null) {
            rec.put("creator", creator);
        }
        if (lore != null) {
            rec.put("Lore", new ArrayList<Object>(lore));
        }
        if (displayName != null) {
            rec.put("display_name", displayName);
        }
        return rec;
    }

    private static byte[] buildFixtureBytes() throws Exception {
        List<Object> data = new ArrayList<>();

        // Real record: a simple internal-keyed item with both display_name and Lore.
        data.add(record("internal.26021.10887.0", "§rВодка",
                List.of("§rПоставь на стол"), null));

        // Real record: a free-form, creator-submitted slug key (real creator name redacted, see class doc).
        data.add(record("hallowin_goluboe_steklo_s_chernohvosta",
                "Осколок голубого стекла c Чернохвоста",
                List.of("§rЗагадочный осколок стекла. Внутри него пульсирует энергия..."),
                "REDACTED_CREATOR"));

        // Real ambiguity example, part of ItemLoreCatalog's own class doc: itemId 11485 is real and
        // shared by two genuinely different real items under two different real categoryIds.
        data.add(record("internal.28200.11485.1", "§rКонверт с красной пометкой",
                List.of("§rВнутри лежат сканворды для продвинутых."), null));
        data.add(record("internal.28202.11485.0", "§rКонверт с синей пометкой",
                List.of("§rВнутри порножурналы."), null));

        // Real record with no display_name at all (23 of 1483 real records) -- Lore only.
        data.add(record("internal.12353.9325.1", null,
                List.of("§rСнаряд из схрона."), null));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("Data", data);

        byte[] inner = NbtWriter.writeRoot("", root);
        ByteArrayOutputStream gz = new ByteArrayOutputStream();
        try (GZIPOutputStream gzOut = new GZIPOutputStream(gz)) {
            gzOut.write(inner);
        }
        return gz.toByteArray();
    }

    private static ItemLoreCatalog loadFixture() throws Exception {
        return ItemLoreCatalog.loadFromBytes(buildFixtureBytes());
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("loads the real gzip+NBT fixture and resolves internal-keyed and free-form-keyed records", () -> {
            ItemLoreCatalog catalog = loadFixture();
            assertEquals(5, catalog.size(), "5 real records in the fixture");

            assertEquals("§rВодка", catalog.displayName("internal.26021.10887.0"), "real display_name, verbatim including the §r formatting code");
            assertEquals(1, catalog.lore("internal.26021.10887.0").size(), "one real Lore line");
            assertNull(catalog.creator("internal.26021.10887.0"), "no creator on a developer-authored entry");

            assertEquals("REDACTED_CREATOR", catalog.creator("hallowin_goluboe_steklo_s_chernohvosta"), "free-form-keyed real entries are creator-submitted (fixture placeholder, see class doc)");
            assertTrue(catalog.freeformKeys().contains("hallowin_goluboe_steklo_s_chernohvosta"), "free-form key list");
            assertTrue(!catalog.freeformKeys().contains("internal.26021.10887.0"), "an internal-keyed record is never in the free-form list");
        }));

        out.add(test("byInternalId resolves the real (categoryId, itemId, variant) triple, and missing display_name/Lore are disclosed as null/empty, not guessed", () -> {
            ItemLoreCatalog catalog = loadFixture();
            ItemLoreCatalog.Record r = catalog.byInternalId(26021, 10887, 0);
            assertTrue(r != null, "real internal id resolves");
            assertEquals("internal.26021.10887.0", r.key(), "record key matches the lookup triple");

            assertNull(catalog.displayName("internal.12353.9325.1"), "23 of 1483 real records really have no display_name -- null, not a fallback guess");
            assertEquals(1, catalog.lore("internal.12353.9325.1").size(), "but Lore is still present for this real record");
            assertTrue(catalog.byInternalId(1, 2, 3) == null, "an id triple with no real record resolves to null");
        }));

        out.add(test("the real itemId-11485 ambiguity (two different real items, two real categoryIds) is surfaced, not silently merged", () -> {
            ItemLoreCatalog catalog = loadFixture();
            List<Integer> categories = catalog.categoriesForItemId(11485);
            assertEquals(2, categories.size(), "itemId 11485 really has 2 distinct real categoryIds in this fixture");
            assertTrue(categories.contains(28200) && categories.contains(28202), "both real categoryIds present");
            assertTrue(catalog.ambiguousItemIds().contains(11485), "11485 is correctly flagged ambiguous");

            String redEnvelope = catalog.displayName("internal.28200.11485.1");
            String blueEnvelope = catalog.displayName("internal.28202.11485.0");
            assertTrue(!redEnvelope.equals(blueEnvelope), "the two real items sharing itemId 11485 really do have different display names -- confirms why a bare-itemId lookup would be unsound");

            assertEquals(0, catalog.categoriesForItemId(999999).size(), "an itemId with no real internal-keyed record resolves to an empty category list, not null");
        }));

        return out;
    }
}
