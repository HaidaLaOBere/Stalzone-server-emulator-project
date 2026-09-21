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
 * Regression tests for {@link StalkerSoundsCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/stalkersounds/} folder (part 92),
 * live-verified on device: the complete real 145-file {@code music/} folder, the complete real
 * 117-file {@code stream/} root, the complete real 27-file {@code stream/guitar_na/} folder, and
 * representative subsets of the two large real radio-voice-line folders ({@code
 * stream/radio/rock1_radio/en/}, 111 real files, sparsely numbered 1-210; {@code
 * stream/radio/rock1_radio/ru/}, 405 real files across two series). Only real game-content names are
 * used, per this project's privacy rule -- never a real player username.
 */
public final class StalkerSoundsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // music/ -- the complete real 145-file folder, flat, no subfolders
            "music/audio_diary_dmz_jiguli.ogg",
            "music/audio_diary_dmz_lada.ogg",
            "music/audio_diary_dmz_pegas.ogg",
            "music/audio_diary_operation_lesta_intervention.ogg",
            "music/audio_diary_operation_lesta_main_caliber.ogg",
            "music/audio_diary_operation_lesta_mine.ogg",
            "music/audio_diary_operation_lesta_rainbow.ogg",
            "music/audio_diary_operation_lesta_water_area.ogg",
            "music/breadcrumbs_audiocassette_fundament_7.ogg",
            "music/breadcrumbs_audiocassette_lekciya_1.ogg",
            "music/breadcrumbs_audiocassette_nepredskazuemost_8.ogg",
            "music/breadcrumbs_audiocassette_paradoxteseya_3.ogg",
            "music/breadcrumbs_audiocassette_poligon_4.ogg",
            "music/breadcrumbs_audiocassette_prestizhno_5.ogg",
            "music/breadcrumbs_audiocassette_rashozhdeniye_2.ogg",
            "music/breadcrumbs_audiocassette_razvoplosheniye_9.ogg",
            "music/breadcrumbs_audiocassette_reabilitaciya_6.ogg",
            "music/copy_nina1.ogg",
            "music/copy_nina2.ogg",
            "music/copy_nina3.ogg",
            "music/copy_poet.ogg",
            "music/copy_prist.ogg",
            "music/copy_prrofessor.ogg",
            "music/covenant_typoon.ogg",
            "music/duty_typoon.ogg",
            "music/echo_nina1.ogg",
            "music/echo_nina2.ogg",
            "music/echo_nina3.ogg",
            "music/echo_poet.ogg",
            "music/echo_prist.ogg",
            "music/echo_prrofessor.ogg",
            "music/freedom_typoon.ogg",
            "music/goblin1.ogg",
            "music/goblin2.ogg",
            "music/goblin3.ogg",
            "music/goblin4.ogg",
            "music/goblin5.ogg",
            "music/goblin6.ogg",
            "music/goblin7.ogg",
            "music/goblin8.ogg",
            "music/goblin9.ogg",
            "music/goblin10.ogg",
            "music/goblin11.ogg",
            "music/goblin12.ogg",
            "music/goblin13.ogg",
            "music/goblin14.ogg",
            "music/kasseta_bunker.ogg",
            "music/kasseta_stalkera1.ogg",
            "music/kasseta_stalkera2.ogg",
            "music/kasseta_stalkera3.ogg",
            "music/kasseta_stalkera4.ogg",
            "music/kasseta_voenstala.ogg",
            "music/kasseta_zorki1.ogg",
            "music/kasseta_zorki2.ogg",
            "music/merc_typoon.ogg",
            "music/murmur_typoon.ogg",
            "music/na_amazing_grace.ogg",
            "music/na_american_country.ogg",
            "music/na_this_land_is_your_land.ogg",
            "music/na_true_country_american_blues.ogg",
            "music/na_us_national_anthem.ogg",
            "music/naemnikiaudiodnevnik1.ogg",
            "music/naemnikiaudiodnevnik2.ogg",
            "music/naemnikiaudiodnevnik3.ogg",
            "music/naemnikiaudiodnevnik4.ogg",
            "music/naemnikiaudiodnevnik5.ogg",
            "music/naemnikiaudiodnevnik6.ogg",
            "music/naemnikiaudiodnevnik7.ogg",
            "music/naemnikiaudiodnevnik8.ogg",
            "music/naemnikiaudiodnevnik9.ogg",
            "music/naemnikiaudiodnevnik10.ogg",
            "music/operatsii_audiodnevnik1.ogg",
            "music/operatsii_audiodnevnik2.ogg",
            "music/operatsii_audiodnevnik3.ogg",
            "music/operatsii_audiodnevnik4.ogg",
            "music/operatsii_audiodnevnik5.ogg",
            "music/operatsii_audiodnevnik6.ogg",
            "music/operatsii_audiodnevnik7.ogg",
            "music/poligons_typoon.ogg",
            "music/shaiba_typoon.ogg",
            "music/shlam_cassete1.ogg",
            "music/shlam_cassete2.ogg",
            "music/smotrikov1.ogg",
            "music/smotrikov2.ogg",
            "music/smotrikov3.ogg",
            "music/smotrikov4.ogg",
            "music/smotrikov5.ogg",
            "music/smotrikov6.ogg",
            "music/smotrikov7.ogg",
            "music/smotrikov8.ogg",
            "music/smotrikov9.ogg",
            "music/smotrikov10.ogg",
            "music/smotrikov11.ogg",
            "music/smotrikov12.ogg",
            "music/smotrikov13.ogg",
            "music/smotrikov14.ogg",
            "music/smotrikov15.ogg",
            "music/smotrikov16.ogg",
            "music/smotrikov17.ogg",
            "music/smotrikov18.ogg",
            "music/smotrikov19.ogg",
            "music/smotrikov20.ogg",
            "music/smotrikov21.ogg",
            "music/smotrikov22.ogg",
            "music/smotrikov23.ogg",
            "music/smotrikov24.ogg",
            "music/smotrikov25.ogg",
            "music/smotrikov26.ogg",
            "music/snegurochka2025_1.ogg",
            "music/snegurochka2025_2.ogg",
            "music/snegurochka2025_3.ogg",
            "music/snegurochka2025_4.ogg",
            "music/snegurochka2025_5.ogg",
            "music/snegurochka2025_6.ogg",
            "music/snegurochka2025_7.ogg",
            "music/snegurochka2025_8.ogg",
            "music/snegurochka2025_9.ogg",
            "music/snegurochka2025_10.ogg",
            "music/snegurochka2025_11.ogg",
            "music/snegurochka2025_12.ogg",
            "music/snegurochka2025_13.ogg",
            "music/snegurochka2025_14.ogg",
            "music/snegurochka_audiodnevnik_inside.ogg",
            "music/unknown_typoon.ogg",
            "music/zaryaaudiodnevnik1lou.ogg",
            "music/zaryaaudiodnevnik2lou.ogg",
            "music/zaryaaudiodnevnik3mid.ogg",
            "music/zaryaaudiodnevnik4mid.ogg",
            "music/zaryaaudiodnevnik5mid.ogg",
            "music/zaryaaudiodnevnik6haj.ogg",
            "music/zaryaaudiodnevnik7haj.ogg",
            "music/zaryaaudiodnevnik8haj.ogg",
            "music/zaryaaudiodnevnik9haj.ogg",
            "music/zaryaaudiodnevnik10haj.ogg",
            "music/zavetaudiodnevnik1.ogg",
            "music/zavetaudiodnevnik2.ogg",
            "music/zavetaudiodnevnik3.ogg",
            "music/zavetaudiodnevnik4.ogg",
            "music/zavetaudiodnevnik5.ogg",
            "music/zavetaudiodnevnik6.ogg",
            "music/zavetaudiodnevnik7.ogg",
            "music/zavetaudiodnevnik8.ogg",
            "music/zavetaudiodnevnik9.ogg",
            "music/zavetaudiodnevnik10.ogg",
            "music/zivcas_typoon.ogg",
            // stream/ -- the complete real 117-file root
            "stream/bar_npc_one.ogg",
            "stream/loudspeaker_limansk_1.ogg",
            "stream/loudspeaker_limansk_2.ogg",
            "stream/loudspeaker_limansk_3.ogg",
            "stream/loudspeaker_limansk_4.ogg",
            "stream/loudspeaker_limansk_5.ogg",
            "stream/loudspeaker_limansk_6.ogg",
            "stream/radar_tower_1.ogg",
            "stream/radar_tower_2.ogg",
            "stream/radar_tower_3.ogg",
            "stream/radar_tower_4.ogg",
            "stream/radar_tower_5.ogg",
            "stream/radar_tower_6.ogg",
            "stream/radar_tower_7.ogg",
            "stream/radar_tower_8.ogg",
            "stream/radar_tower_9.ogg",
            "stream/radar_tower_10.ogg",
            "stream/radar_tower_11.ogg",
            "stream/radioburan_1.ogg",
            "stream/radioburan_2.ogg",
            "stream/radioburan_3.ogg",
            "stream/radioburan_4.ogg",
            "stream/radioburan_5.ogg",
            "stream/radioburan_6.ogg",
            "stream/radioburan_7.ogg",
            "stream/radioburan_8.ogg",
            "stream/radioburan_9.ogg",
            "stream/radioburan_10.ogg",
            "stream/radioburan_11.ogg",
            "stream/radioburan_12.ogg",
            "stream/radioburan_13.ogg",
            "stream/radioburan_14.ogg",
            "stream/radioburan_15.ogg",
            "stream/radioburan_16.ogg",
            "stream/radioburan_17.ogg",
            // radiosssr_2..55 -- real, note the disclosed real gap: no radiosssr_1.ogg exists
            "stream/radiosssr_2.ogg",
            "stream/radiosssr_3.ogg",
            "stream/radiosssr_4.ogg",
            "stream/radiosssr_5.ogg",
            "stream/radiosssr_6.ogg",
            "stream/radiosssr_7.ogg",
            "stream/radiosssr_8.ogg",
            "stream/radiosssr_9.ogg",
            "stream/radiosssr_10.ogg",
            "stream/radiosssr_11.ogg",
            "stream/radiosssr_12.ogg",
            "stream/radiosssr_13.ogg",
            "stream/radiosssr_14.ogg",
            "stream/radiosssr_15.ogg",
            "stream/radiosssr_16.ogg",
            "stream/radiosssr_17.ogg",
            "stream/radiosssr_18.ogg",
            "stream/radiosssr_19.ogg",
            "stream/radiosssr_20.ogg",
            "stream/radiosssr_21.ogg",
            "stream/radiosssr_22.ogg",
            "stream/radiosssr_23.ogg",
            "stream/radiosssr_24.ogg",
            "stream/radiosssr_25.ogg",
            "stream/radiosssr_26.ogg",
            "stream/radiosssr_27.ogg",
            "stream/radiosssr_28.ogg",
            "stream/radiosssr_29.ogg",
            "stream/radiosssr_30.ogg",
            "stream/radiosssr_31.ogg",
            "stream/radiosssr_32.ogg",
            "stream/radiosssr_33.ogg",
            "stream/radiosssr_34.ogg",
            "stream/radiosssr_35.ogg",
            "stream/radiosssr_36.ogg",
            "stream/radiosssr_37.ogg",
            "stream/radiosssr_38.ogg",
            "stream/radiosssr_39.ogg",
            "stream/radiosssr_40.ogg",
            "stream/radiosssr_41.ogg",
            "stream/radiosssr_42.ogg",
            "stream/radiosssr_43.ogg",
            "stream/radiosssr_44.ogg",
            "stream/radiosssr_45.ogg",
            "stream/radiosssr_46.ogg",
            "stream/radiosssr_47.ogg",
            "stream/radiosssr_48.ogg",
            "stream/radiosssr_49.ogg",
            "stream/radiosssr_50.ogg",
            "stream/radiosssr_51.ogg",
            "stream/radiosssr_52.ogg",
            "stream/radiosssr_53.ogg",
            "stream/radiosssr_54.ogg",
            "stream/radiosssr_55.ogg",
            "stream/soundvoid.ogg",
            "stream/guitar_1.ogg",
            "stream/guitar_2.ogg",
            "stream/guitar_3.ogg",
            "stream/guitar_4.ogg",
            "stream/guitar_5.ogg",
            "stream/guitar_6.ogg",
            "stream/guitar_7.ogg",
            "stream/guitar_8.ogg",
            "stream/guitar_9.ogg",
            "stream/guitar_10.ogg",
            "stream/guitar_11.ogg",
            "stream/guitar_12.ogg",
            "stream/guitar_13.ogg",
            "stream/guitar_14.ogg",
            "stream/guitar_15.ogg",
            "stream/guitar_16.ogg",
            "stream/guitar_17.ogg",
            "stream/guitar_18.ogg",
            "stream/guitar_19.ogg",
            "stream/guitar_20.ogg",
            "stream/guitar_21.ogg",
            "stream/guitar_22.ogg",
            "stream/guitar_23.ogg",
            "stream/guitar_24.ogg",
            "stream/guitar_25.ogg",
            "stream/guitar_26.ogg",
            "stream/guitar_27.ogg",
            // stream/guitar_na/ -- the complete real 27-file folder (same real numbering as stream/'s own guitar_*)
            "stream/guitar_na/guitar_1.ogg",
            "stream/guitar_na/guitar_2.ogg",
            "stream/guitar_na/guitar_3.ogg",
            "stream/guitar_na/guitar_4.ogg",
            "stream/guitar_na/guitar_5.ogg",
            "stream/guitar_na/guitar_6.ogg",
            "stream/guitar_na/guitar_7.ogg",
            "stream/guitar_na/guitar_8.ogg",
            "stream/guitar_na/guitar_9.ogg",
            "stream/guitar_na/guitar_10.ogg",
            "stream/guitar_na/guitar_11.ogg",
            "stream/guitar_na/guitar_12.ogg",
            "stream/guitar_na/guitar_13.ogg",
            "stream/guitar_na/guitar_14.ogg",
            "stream/guitar_na/guitar_15.ogg",
            "stream/guitar_na/guitar_16.ogg",
            "stream/guitar_na/guitar_17.ogg",
            "stream/guitar_na/guitar_18.ogg",
            "stream/guitar_na/guitar_19.ogg",
            "stream/guitar_na/guitar_20.ogg",
            "stream/guitar_na/guitar_21.ogg",
            "stream/guitar_na/guitar_22.ogg",
            "stream/guitar_na/guitar_23.ogg",
            "stream/guitar_na/guitar_24.ogg",
            "stream/guitar_na/guitar_25.ogg",
            "stream/guitar_na/guitar_26.ogg",
            "stream/guitar_na/guitar_27.ogg",
            // stream/radio/rock1_radio/en/ -- representative subset of the real 111-file, sparsely-numbered (1-210) folder
            "stream/radio/rock1_radio/en/en_radio_host_1.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_2.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_3.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_99.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_100.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_200.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_209.ogg",
            "stream/radio/rock1_radio/en/en_radio_host_210.ogg",
            // stream/radio/rock1_radio/ru/ -- representative subset of the real 405-file folder (two real series)
            "stream/radio/rock1_radio/ru/ru_radio_host_1.ogg",
            "stream/radio/rock1_radio/ru/ru_radio_host_2.ogg",
            "stream/radio/rock1_radio/ru/ru_radio_host_100.ogg",
            "stream/radio/rock1_radio/ru/ru_radio_host_251.ogg",
            "stream/radio/rock1_radio/ru/ru_radio_host_300.ogg",
            "stream/radio/rock1_radio/ru/ru_radio_host_324.ogg",
            "stream/radio/rock1_radio/ru/ru_radiorock_song_1.ogg",
            "stream/radio/rock1_radio/ru/ru_radiorock_song_100.ogg",
            "stream/radio/rock1_radio/ru/ru_radiorock_song_231.ogg",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerSoundsCatalog.load finds exactly the 2 real top-level folders", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertEquals(2, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"music", "stream"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: music is the complete real 145-file flat fixture", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("music");
            assertEquals(145, files.size(), "music file count");
            for (String name : new String[]{
                    "smotrikov1.ogg", "goblin1.ogg", "freedom_typoon.ogg", "zivcas_typoon.ogg",
            }) {
                assertTrue(files.contains(name), name + " present");
            }
        }));

        out.add(test("filesIn: stream aggregates its own root files with guitar_na/ and radio/ subfolders", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertEquals(FIXTURE_FILES.length - 145, files.size(), "stream file count (all non-music fixture files)");
            assertTrue(files.contains("soundvoid.ogg"), "root-level soundvoid.ogg present");
            assertTrue(files.contains("guitar_na/guitar_1.ogg"), "nested guitar_na/guitar_1.ogg present");
            assertTrue(files.contains("radio/rock1_radio/en/en_radio_host_1.ogg"), "nested en radio file present");
            assertTrue(files.contains("radio/rock1_radio/ru/ru_radiorock_song_1.ogg"), "nested ru radiorock_song file present");
        }));

        out.add(test("stream/: real radiosssr numbering gap -- no radiosssr_1.ogg, starts at radiosssr_2.ogg", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertTrue(files.contains("radiosssr_2.ogg"), "radiosssr_2.ogg present");
            assertTrue(!files.contains("radiosssr_1.ogg"), "radiosssr_1.ogg correctly absent");
        }));

        out.add(test("foldersContaining: _typoon cassette family (all 9 real, confirmed via en.lang) resolves under music", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            for (String needle : new String[]{
                    "freedom_typoon", "covenant_typoon", "duty_typoon", "merc_typoon", "zivcas_typoon",
                    "murmur_typoon", "shaiba_typoon", "poligons_typoon", "unknown_typoon",
            }) {
                assertTrue(catalog.foldersContaining(needle).contains("music"), needle);
            }
        }));

        out.add(test("foldersContaining: breadcrumbs_audiocassette psychoscope-cassette family resolves under music", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("breadcrumbs_audiocassette_poligon_4").contains("music"), "poligon_4 (Area-16 Cassette)");
            assertTrue(catalog.foldersContaining("breadcrumbs_audiocassette_paradoxteseya_3").contains("music"), "paradoxteseya_3 (Ship of Theseus Cassette)");
        }));

        out.add(test("foldersContaining: audio_diary_dmz Boral-cassette trio resolves under music", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            for (String needle : new String[]{"audio_diary_dmz_jiguli", "audio_diary_dmz_lada", "audio_diary_dmz_pegas"}) {
                assertTrue(catalog.foldersContaining(needle).contains("music"), needle);
            }
        }));

        out.add(test("foldersContaining: real disclosed unconfirmed leads (no en.lang match) still resolve under music", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            for (String needle : new String[]{"zavetaudiodnevnik", "kasseta_stalkera", "shlam_cassete"}) {
                assertTrue(catalog.foldersContaining(needle).contains("music"), needle);
            }
        }));

        out.add(test("foldersContaining: guitar_1 resolves under stream (both root and guitar_na/ carry it)", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("guitar_1.ogg").contains("stream"), "guitar_1.ogg");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerSoundsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerSoundsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkersounds-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerSoundsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerSoundsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkersounds-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerSoundsCatalog.load(dir);
    }
}
