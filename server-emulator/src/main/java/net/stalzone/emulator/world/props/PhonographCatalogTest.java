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
 * Regression tests for {@link PhonographCatalog}, using a fixture built from real {@code
 * phonograph.*} lines drawn directly from {@code stalker/lang/en.lang} (part 130, live-verified
 * on device) -- a real named vocal track, a real ambient track tied to a known location, a
 * location-category vinyl, an event-category vinyl (including the Reichstag tie), and the one
 * real standalone vinyl id outside either category.
 */
public final class PhonographCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "phonograph.gui.track.Grin_Danilov_moi_dozhd=My Rain",
            "phonograph.gui.track.ambient_bolota=Sounds of the Swamps",
            "phonograph.vinyl_location_bolota.title=The Swamps",
            "phonograph.vinyl_event_reichstag_breakthrough.title=Reichstag Breakthrough",
            "phonograph.vinyl_gribnaya_bylina.title=Mushroom Bylina"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("phonograph-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PhonographCatalog loadFixture() throws IOException {
        return PhonographCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real vocal and ambient track titles resolve", () -> {
            PhonographCatalog catalog = loadFixture();
            assertEquals("My Rain", catalog.trackTitle("Grin_Danilov_moi_dozhd"), "vocal track");
            assertEquals("Sounds of the Swamps", catalog.trackTitle("ambient_bolota"), "ambient track tied to bolota");
        }));

        out.add(test("real vinyl titles resolve verbatim by their full prefixed id, across all 3 shapes", () -> {
            PhonographCatalog catalog = loadFixture();
            assertEquals("The Swamps", catalog.vinylTitle("vinyl_location_bolota"), "location-category vinyl");
            assertEquals("Reichstag Breakthrough", catalog.vinylTitle("vinyl_event_reichstag_breakthrough"), "event-category vinyl");
            assertEquals("Mushroom Bylina", catalog.vinylTitle("vinyl_gribnaya_bylina"), "standalone vinyl outside either category");
        }));

        out.add(test("trackIds()/vinylIds() are sorted and unknown ids resolve to null rather than throwing", () -> {
            PhonographCatalog catalog = loadFixture();
            List<String> trackIds = catalog.trackIds();
            assertEquals(2, trackIds.size(), "2 real track ids in fixture");
            assertTrue(trackIds.get(0).compareTo(trackIds.get(1)) < 0, "trackIds sorted ascending");
            List<String> vinylIds = catalog.vinylIds();
            assertEquals(3, vinylIds.size(), "3 real vinyl ids in fixture");
            assertNull(catalog.trackTitle("not_a_real_track"), "unknown track id");
            assertNull(catalog.vinylTitle("vinyl_not_a_real_id"), "unknown vinyl id");
        }));

        return out;
    }
}
