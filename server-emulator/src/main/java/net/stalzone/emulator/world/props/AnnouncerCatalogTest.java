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
 * Regression tests for {@link AnnouncerCatalog}, using a fixture built from a representative real
 * slice of {@code bundle.notf.*} (part 175, live-verified against the full {@code en.lang} file),
 * covering the real dmz/dmz2026/fools_day2026 announcer lines, the disclosed dmz/dmz2026
 * naming quirk, real boss-kill lines, real duplicate-text pairs, and the non-annotator FROST
 * TROOPERS info block.
 */
public final class AnnouncerCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "bundle.notf.dmz.annotator.title=Commentator",
            "bundle.notf.dmz2026.annotator.title=Commentator",
            "bundle.notf.fools_day2026.annotator.title=Admin",
            "bundle.notf.fools_day2026.annotator.session_intro=Attention! The server rules are being read",
            "bundle.notf.dmz.annotator.world_dmz_airdrop=Valuable cargo has been dropped!",
            "bundle.notf.dmz.annotator.party_dmz_boss_death_dochide=Dr. Hyde killed!",
            "bundle.notf.dmz.annotator.party_dmz_boss_death_objora=Guzzler killed!",
            "bundle.notf.dmz.annotator.world_dmz_evacuation_started=The last helicopter leaves in a minute!",
            "bundle.notf.dmz.annotator.world_dmz2026_auto_evacuation_started=The last helicopter leaves in a minute!",
            "bundle.notf.dmz.annotator.player_dmz_left_game=A member quit the game",
            "bundle.notf.dmz.annotator.player_dmz2026_left_game=Participant left the game",
            "bundle.notf.dmz2026.title=FROST TROOPERS",
            "bundle.notf.dmz2026.info.start=Access to the Frost Troopers operation is granted",
            "bundle.notf.dmz2026.info.leave=You have left the queue for the Frost Troopers operation: {reason}"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("announcer-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static AnnouncerCatalog loadFixture() throws IOException {
        return AnnouncerCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real mode titles resolve, including the fools_day2026 Admin variant distinct from the raid Commentator", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals("Commentator", catalog.announcerLine("dmz", "title"), "dmz title");
            assertEquals("Commentator", catalog.announcerLine("dmz2026", "title"), "dmz2026 title");
            assertEquals("Admin", catalog.announcerLine("fools_day2026", "title"), "fools_day2026 title");
            assertEquals("Attention! The server rules are being read", catalog.announcerLine("fools_day2026", "session_intro"), "fools_day2026 session_intro");
        }));

        out.add(test("real boss-kill lines resolve under the dmz mode", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals("Dr. Hyde killed!", catalog.announcerLine("dmz", "party_dmz_boss_death_dochide"), "dochide boss kill");
            assertEquals("Guzzler killed!", catalog.announcerLine("dmz", "party_dmz_boss_death_objora"), "objora boss kill");
        }));

        out.add(test("the real dmz/dmz2026 naming quirk: 2026-flavored event ids resolve under the dmz mode, not a separate dmz2026 mode, exactly as the client keys them", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals("The last helicopter leaves in a minute!", catalog.announcerLine("dmz", "world_dmz2026_auto_evacuation_started"), "2026-flavored id lives under the dmz mode");
            assertNull(catalog.announcerLine("dmz2026", "world_dmz2026_auto_evacuation_started"), "not duplicated under a guessed dmz2026 mode");
        }));

        out.add(test("real duplicate-text and real different-text pairs across the legacy/2026 id families are both preserved as-is", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals(catalog.announcerLine("dmz", "world_dmz_evacuation_started"), catalog.announcerLine("dmz", "world_dmz2026_auto_evacuation_started"), "real byte-identical duplicate text");
            assertEquals("A member quit the game", catalog.announcerLine("dmz", "player_dmz_left_game"), "legacy wording");
            assertEquals("Participant left the game", catalog.announcerLine("dmz", "player_dmz2026_left_game"), "genuinely different real 2026 wording for the same event");
        }));

        out.add(test("the non-annotator dmz2026 info block resolves separately from the annotator lines, including the FROST TROOPERS crosswalk", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals("FROST TROOPERS", catalog.infoField("dmz2026", "title"), "real FROST TROOPERS operation name, third independent corroboration");
            assertEquals("Access to the Frost Troopers operation is granted", catalog.infoField("dmz2026", "info.start"), "info.start");
            assertTrue(catalog.infoField("dmz2026", "info.leave").contains("{reason}"), "info.leave real placeholder");
            assertNull(catalog.announcerLine("dmz2026", "info.start"), "info block fields are not mixed into the annotator table");
        }));

        out.add(test("announcerModeIds/announcerEventIds surface the real fixture ids sorted, and unknown lookups resolve to null", () -> {
            AnnouncerCatalog catalog = loadFixture();
            List<String> modes = catalog.announcerModeIds();
            for (int i = 1; i < modes.size(); i++) {
                assertTrue(modes.get(i - 1).compareTo(modes.get(i)) < 0, "modes sorted at " + i);
            }
            assertTrue(modes.contains("dmz") && modes.contains("dmz2026") && modes.contains("fools_day2026"), "all 3 real modes present");
            assertTrue(catalog.announcerEventIds("dmz").contains("world_dmz_airdrop"), "real event id present");
            assertNull(catalog.announcerLine("not_a_real_mode", "not_a_real_event"), "unknown mode/event resolves to null");
        }));

        out.add(test("size() reflects the total real line count across both shapes in the fixture", () -> {
            AnnouncerCatalog catalog = loadFixture();
            assertEquals(14, catalog.size(), "14 real lines in this fixture");
        }));

        return out;
    }
}
