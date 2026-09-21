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
 * Regression tests for {@link PdaWaypointEventCatalog}, using a fixture built from a representative
 * real slice of {@code pda.waypoint.we.*} (part 173, live-verified against the full {@code en.lang}
 * file), including the real compound-id cases, the real flat exceptions, and real examples of all
 * three real cross-reference-mismatch categories against {@link WorldEventCatalog}.
 */
public final class PdaWaypointEventCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "pda.waypoint.we.abstract_camp_tushkan.title=Rat Nest",
            "pda.waypoint.we.discharger.title=Discharger",
            "pda.waypoint.we.discharger.desc=A suspicious electric anomaly is flying around here",
            "pda.waypoint.we.insideout_centrifuge.status.title=TRANSMUTER ACTIVATION",
            "pda.waypoint.we.insideout_centrifuge.status.description=You can restart centrifuges to earn rewards",
            "pda.waypoint.we.insideout_centrifuge.map_marker.description.active=Active centrifuges: {active}/{total}",
            "pda.waypoint.we.insideout_centrifuge.map_marker.description.inactive=Centrifuges are inactive",
            "pda.waypoint.we.outpost.merc=Mercenary Outpost",
            "pda.waypoint.we.outpost.freedom=Rise Outpost",
            "pda.waypoint.we.outpost.duty=Frontier Outpost",
            "pda.waypoint.we.shopot_exit.title=Murmur Post",
            "pda.waypoint.we.shopot_exit=Murmur Post",
            "pda.waypoint.we.shopot_control_center.title=Murmur Control Point",
            "pda.waypoint.we.shopot_control_center=Murmur Control Point",
            "pda.waypoint.we.supply_base_duty.title=Supply Base",
            "pda.waypoint.we.science_camp_duty.title=Frontier scientists",
            "pda.waypoint.we.science_camp_mercenary.title=Mercenary scientists",
            "pda.waypoint.we.inside_extractor.title=Onyx Area"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("pda-waypoint-event-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static PdaWaypointEventCatalog loadFixture() throws IOException {
        return PdaWaypointEventCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real title/desc fields resolve for a simple real id", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertEquals("Rat Nest", catalog.title("abstract_camp_tushkan"), "real simple title");
            assertEquals("Discharger", catalog.title("discharger"), "real discharger title");
            assertEquals("A suspicious electric anomaly is flying around here", catalog.desc("discharger"), "real discharger desc");
        }));

        out.add(test("real compound ids are treated as their own distinct id by the generic last-dot split, without special-casing", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertEquals("TRANSMUTER ACTIVATION", catalog.field("insideout_centrifuge.status", "title"), "real compound id insideout_centrifuge.status, field title");
            assertEquals("You can restart centrifuges to earn rewards", catalog.field("insideout_centrifuge.status", "description"), "same compound id, field description");
            assertEquals("Active centrifuges: {active}/{total}", catalog.field("insideout_centrifuge.map_marker.description", "active"), "a real deeper compound id, field active");
            assertEquals("Centrifuges are inactive", catalog.field("insideout_centrifuge.map_marker.description", "inactive"), "same deeper compound id, field inactive");
        }));

        out.add(test("the real outpost id's per-faction 'fields' resolve, since the generic split naturally handles them without special-casing", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertEquals("Mercenary Outpost", catalog.field("outpost", "merc"), "real outpost/merc");
            assertEquals("Rise Outpost", catalog.field("outpost", "freedom"), "real outpost/freedom");
            assertEquals("Frontier Outpost", catalog.field("outpost", "duty"), "real outpost/duty");
        }));

        out.add(test("real flat exceptions resolve via flatValue(), and independently also carry their own real, byte-identical .title line", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertEquals("Murmur Post", catalog.flatValue("shopot_exit"), "real flat shopot_exit value");
            assertEquals("Murmur Post", catalog.title("shopot_exit"), "the same real id also has its own separate .title line with the identical value");
            assertEquals("Murmur Control Point", catalog.flatValue("shopot_control_center"), "real flat shopot_control_center value");
            assertEquals(2, catalog.flatIds().size(), "2 real flat-exception ids in the fixture");
        }));

        out.add(test("real cross-reference-mismatch examples against WorldEventCatalog are captured as this namespace's own real data, not corrected", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertEquals("Supply Base", catalog.title("supply_base_duty"), "capitalization-only real mismatch case: this namespace has the capital-B form");
            assertEquals("Frontier scientists", catalog.title("science_camp_duty"), "the swapped-content bug case: this namespace has duty correctly reading Frontier scientists");
            assertEquals("Mercenary scientists", catalog.title("science_camp_mercenary"), "the swapped-content bug case: this namespace has mercenary correctly reading Mercenary scientists");
            assertEquals("Onyx Area", catalog.title("inside_extractor"), "different-real-UI-role case: this namespace's title is a place name, not an objective status");
        }));

        out.add(test("unknown id/field resolve to null rather than a guess, and size() reflects the total real line count", () -> {
            PdaWaypointEventCatalog catalog = loadFixture();
            assertNull(catalog.title("not_a_real_id"), "unknown real id resolves to null");
            assertNull(catalog.field("discharger", "not_a_real_field"), "unknown real field resolves to null");
            assertNull(catalog.flatValue("not_a_real_flat_id"), "unknown real flat id resolves to null");
            assertEquals(18, catalog.size(), "18 real lines in this fixture");
        }));

        return out;
    }
}
