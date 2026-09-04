package net.stalzone.emulator.world.props;

import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link StalkerGuideCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/stalkerguide/} folder (part 59),
 * live-verified on device: the real faction/story-arc start/end video pairs with no matching
 * {@code stream/video/} audio track (the disclosed video-without-audio gap), the real
 * {@code frontline_intro}/{@code inside} audio-without-video gap, the Shaman/Morg cross-reference,
 * and the {@code glitch_monitor/} numbered-variant subfolder. Only real game-content names are
 * used, per this project's privacy rule -- never a real player username.
 */
public final class StalkerGuideCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "sound/elevator_bell_synth.ogg",
            "sound/elevator_close.ogg",
            "sound/elevator_open.ogg",
            "sound/hint.ogg",
            "sound/glitch_monitor/glitch_monitor_1.ogg",
            "sound/glitch_monitor/glitch_monitor_2.ogg",
            "sound/glitch_monitor/glitch_monitor_3.ogg",
            "stream/elevator_ride.ogg",
            "stream/getup_anim.ogg",
            "stream/wind_ext.ogg",
            "stream/wind_int.ogg",
            "stream/video/frontline_intro.ogg",
            "stream/video/inside.ogg",
            "stream/video/vlasov_death.ogg",
            "stream/video/vlasov_escape.ogg",
            "stream/video/morg_shaman_fight.ogg",
            "stream/video/morg_shaman_meet.ogg",
            "stream/video/tutorial_intro.ogg",
            "stream/video/tutorial_outro.ogg",
            "stream/video/sstory1.ogg",
            "textures/gui/advbanner.mic",
            "textures/gui/banner.mic",
            "textures/gui/palka.mic",
            "video/duty_start_720.mp4",
            "video/duty_start_1080.mp4",
            "video/duty_end_720.mp4",
            "video/duty_end_1080.mp4",
            "video/zarya_start_720.mp4",
            "video/zarya_end_720.mp4",
            "video/zavet_start_720.mp4",
            "video/zavet_end_720.mp4",
            "video/merc_start_720.mp4",
            "video/merc_end_720.mp4",
            "video/reichstag_atmosphere.mp4",
            "video/honey_badger.mp4",
            "video/vlasov_death_720.mp4",
            "video/vlasov_escape_720.mp4",
            "video/morg_shaman_fight_720.mp4",
            "video/morg_shaman_meet_720.mp4",
            "video/tutorial_intro_720.mp4",
            "video/tutorial_outro_720.mp4",
            "video/sstory1_720.mp4",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerGuideCatalog.load finds exactly the 4 real top-level folders", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("sound"), "sound present");
            assertTrue(catalog.folderNames().contains("stream"), "stream present");
            assertTrue(catalog.folderNames().contains("textures"), "textures present");
            assertTrue(catalog.folderNames().contains("video"), "video present");
        }));

        out.add(test("filesIn: sound/glitch_monitor nested subfolder path is relative to the parent folder itself", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            List<String> sound = catalog.filesIn("sound");
            assertTrue(sound.contains("glitch_monitor/glitch_monitor_1.ogg"), "nested glitch_monitor path");
            assertTrue(sound.contains("hint.ogg"), "root-level hint.ogg");
        }));

        out.add(test("foldersContaining: a faction start/end video pair has no matching stream audio track", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            List<String> duty = catalog.foldersContaining("duty");
            assertEquals(1, duty.size(), "duty folder count");
            assertTrue(duty.contains("video"), "duty_start/duty_end only in video, no stream audio");
        }));

        out.add(test("foldersContaining: reichstag corroborates the cross-project WWII-event reference", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            List<String> reichstag = catalog.foldersContaining("REICHSTAG");
            assertEquals(1, reichstag.size(), "reichstag folder count");
            assertTrue(reichstag.contains("video"), "reichstag_atmosphere lives under video");
        }));

        out.add(test("foldersContaining: the Shaman/Morg cutscene pairing has both a video and a stream audio track", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            List<String> shaman = catalog.foldersContaining("morg_shaman");
            assertEquals(2, shaman.size(), "morg_shaman folder count");
            assertTrue(shaman.contains("stream"), "shaman audio track present in stream");
            assertTrue(shaman.contains("video"), "shaman video present in video");
        }));

        out.add(test("foldersContaining: frontline_intro is a real audio-only track with no matching video", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            List<String> frontline = catalog.foldersContaining("frontline_intro");
            assertEquals(1, frontline.size(), "frontline_intro folder count");
            assertTrue(frontline.contains("stream"), "frontline_intro audio-only, lives in stream");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerGuideCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("StalkerGuideCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkerguide-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerGuideCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerGuideCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkerguide-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerGuideCatalog.load(dir);
    }
}
