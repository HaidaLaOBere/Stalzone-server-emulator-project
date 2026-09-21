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
 * Regression tests for {@link StalkerPlayerCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/stalkerplayer/} folder (part 91), live-verified
 * on device via direct {@code ls}: the complete real 13-file root, the complete real 3-file {@code
 * arkit/}, the complete real 3-file {@code bodies/}, the complete real 5-file {@code head_animations/},
 * the complete real 8-file {@code heads/blessed/}, the complete real 123-file {@code heads/players/},
 * the complete real 72-file {@code heads/unique/}, the complete real 12-file {@code heads/zombie/}, and
 * a representative subset of the real 39-file {@code heads/universal/}. Only real game-content names are
 * used, per this project's privacy rule -- never a real player username.
 */
public final class StalkerPlayerCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // root -- the complete real 13-file set (part 91's headline finding: the real base player rig)
            "empty.ol",
            "head.mcsb",
            "murmur_zombie.mcsb",
            "old_hands.mcsb",
            "origin_nothing.mcsb",
            "origin_torso_hands.mcsb",
            "reich_head_test.mcsb",
            "right_arm.mcsb",
            "steve.mcsb",
            "steve.mic",
            "steve.obj",
            "steve.ol",
            "steve_vanilla_proportions.mcsb",
            // arkit/ -- complete real 3-file folder
            "arkit/arkit.mcsb",
            "arkit/arkit.ol",
            "arkit/arkit_beckap.ol",
            // bodies/ -- complete real 3-file folder
            "bodies/spring24_1.ol",
            "bodies/spring26_2.ol",
            "bodies/summer24_1.ol",
            // head_animations/ -- complete real 5-file folder
            "head_animations/blink1.mcvd",
            "head_animations/blink2.mcvd",
            "head_animations/test_animation.mcvd",
            "head_animations/test_animation1.mcvd",
            "head_animations/zombie.mcvd",
            // heads/blessed/ -- complete real 8-file folder
            "heads/blessed/blessed_01.mcsb",
            "heads/blessed/blessed_01.ol",
            "heads/blessed/blessed_02.mcsb",
            "heads/blessed/blessed_02.ol",
            "heads/blessed/blessed_03.mcsb",
            "heads/blessed/blessed_03.ol",
            "heads/blessed/blessed_04.mcsb",
            "heads/blessed/blessed_04.ol",
            // heads/players/ -- complete real 123-file folder
            "heads/players/players_autumn24_1.ol",
            "heads/players/players_autumn25_1.mcsb",
            "heads/players/players_autumn25_1.ol",
            "heads/players/players_autumn25_2.mcsb",
            "heads/players/players_autumn25_2.ol",
            "heads/players/players_autumn25_3.mcsb",
            "heads/players/players_autumn25_3.ol",
            "heads/players/players_autumn26.mcsb",
            "heads/players/players_autumn26.ol",
            "heads/players/players_birthday2022.mcsb",
            "heads/players/players_birthday2022.ol",
            "heads/players/players_blitz_1.mcsb",
            "heads/players/players_blitz_1.ol",
            "heads/players/players_blizzard1.mcsb",
            "heads/players/players_blizzard1.ol",
            "heads/players/players_blizzard3.mcsb",
            "heads/players/players_blizzard3.ol",
            "heads/players/players_blizzard4.mcsb",
            "heads/players/players_blizzard4.ol",
            "heads/players/players_blizzard5.mcsb",
            "heads/players/players_blizzard5.ol",
            "heads/players/players_blizzard6.mcsb",
            "heads/players/players_blizzard6.ol",
            "heads/players/players_blizzard7.mcsb",
            "heads/players/players_blizzard7.ol",
            "heads/players/players_covenant1.mcsb",
            "heads/players/players_covenant1.ol",
            "heads/players/players_duty1.mcsb",
            "heads/players/players_duty1.ol",
            "heads/players/players_freedom1.mcsb",
            "heads/players/players_freedom1.ol",
            "heads/players/players_halloween1.mcsb",
            "heads/players/players_halloween1.ol",
            "heads/players/players_halloween2.mcsb",
            "heads/players/players_halloween2.ol",
            "heads/players/players_halloween3.mcsb",
            "heads/players/players_halloween3.ol",
            "heads/players/players_halloween4.mcsb",
            "heads/players/players_halloween4.ol",
            "heads/players/players_halloween5.mcsb",
            "heads/players/players_halloween5.ol",
            "heads/players/players_incident1.mcsb",
            "heads/players/players_incident1.ol",
            "heads/players/players_incident2.mcsb",
            "heads/players/players_incident2.ol",
            "heads/players/players_inside1.mcsb",
            "heads/players/players_inside1.ol",
            "heads/players/players_inside2.mcsb",
            "heads/players/players_inside2.ol",
            "heads/players/players_inside2_emi.ol",
            "heads/players/players_merc1.mcsb",
            "heads/players/players_merc1.ol",
            "heads/players/players_mist2.mcsb",
            "heads/players/players_mist2.ol",
            "heads/players/players_mist5.mcsb",
            "heads/players/players_mist5.ol",
            "heads/players/players_mk_1.mcsb",
            "heads/players/players_mk_1.ol",
            "heads/players/players_mt_1.mcsb",
            "heads/players/players_mt_1.ol",
            "heads/players/players_na_afram_1.mcsb",
            "heads/players/players_na_afram_1.ol",
            "heads/players/players_na_latam_1.mcsb",
            "heads/players/players_na_latam_1.ol",
            "heads/players/players_na_texas_1.mcsb",
            "heads/players/players_na_texas_1.ol",
            "heads/players/players_nea_foreman_1.mcsb",
            "heads/players/players_nea_foreman_1.ol",
            "heads/players/players_newyear1.mcsb",
            "heads/players/players_newyear1.ol",
            "heads/players/players_newyear10.mcsb",
            "heads/players/players_newyear10.ol",
            "heads/players/players_newyear2.mcsb",
            "heads/players/players_newyear2.ol",
            "heads/players/players_newyear3.mcsb",
            "heads/players/players_newyear3.ol",
            "heads/players/players_newyear4.mcsb",
            "heads/players/players_newyear4.ol",
            "heads/players/players_newyear5.mcsb",
            "heads/players/players_newyear5.ol",
            "heads/players/players_newyear6.mcsb",
            "heads/players/players_newyear6.ol",
            "heads/players/players_newyear7.mcsb",
            "heads/players/players_newyear7.ol",
            "heads/players/players_newyear8.mcsb",
            "heads/players/players_newyear8.ol",
            "heads/players/players_newyear9.mcsb",
            "heads/players/players_newyear9.ol",
            "heads/players/players_ravent1.mcsb",
            "heads/players/players_ravent1.ol",
            "heads/players/players_spring24_1.mcsb",
            "heads/players/players_spring24_1.ol",
            "heads/players/players_spring24_2.mcsb",
            "heads/players/players_spring24_2.ol",
            "heads/players/players_spring25_1.mcsb",
            "heads/players/players_spring25_1.ol",
            "heads/players/players_spring25_1_emi.ol",
            "heads/players/players_spring25_2.mcsb",
            "heads/players/players_spring25_2.ol",
            "heads/players/players_spring25_2_emi.ol",
            "heads/players/players_spring26_1.mcsb",
            "heads/players/players_spring26_1.ol",
            "heads/players/players_summer24_1.mcsb",
            "heads/players/players_summer24_1.ol",
            "heads/players/players_summer25_1.mcsb",
            "heads/players/players_summer25_1.ol",
            "heads/players/players_summer25_2.mcsb",
            "heads/players/players_summer25_2.ol",
            "heads/players/players_summer26_1.ol",
            "heads/players/players_twitch1.mcsb",
            "heads/players/players_twitch1.ol",
            "heads/players/players_twitch2.mcsb",
            "heads/players/players_twitch2.ol",
            "heads/players/players_twitch3.mcsb",
            "heads/players/players_twitch3.ol",
            "heads/players/players_twitch4.mcsb",
            "heads/players/players_twitch4.ol",
            "heads/players/players_winter25_1.mcsb",
            "heads/players/players_winter25_1.ol",
            "heads/players/players_winter26_1.mcsb",
            "heads/players/players_winter26_1.ol",
            "heads/players/players_winter26_2.mcsb",
            "heads/players/players_winter26_2.ol",
            // heads/unique/ -- complete real 72-file folder, 33 real named characters
            "heads/unique/unique_bering.mcsb",
            "heads/unique/unique_bering.ol",
            "heads/unique/unique_chatsky.mcsb",
            "heads/unique/unique_chatsky.ol",
            "heads/unique/unique_davydov.mcsb",
            "heads/unique/unique_davydov.ol",
            "heads/unique/unique_doctor.mcsb",
            "heads/unique/unique_doctor.ol",
            "heads/unique/unique_gershel.mcsb",
            "heads/unique/unique_gershel.ol",
            "heads/unique/unique_gershel_2.ol",
            "heads/unique/unique_godun.mcsb",
            "heads/unique/unique_godun.ol",
            "heads/unique/unique_hamzi.mcsb",
            "heads/unique/unique_hamzi.ol",
            "heads/unique/unique_hippie.mcsb",
            "heads/unique/unique_hippie.ol",
            "heads/unique/unique_hippie_2.ol",
            "heads/unique/unique_jeltuha.mcsb",
            "heads/unique/unique_jeltuha.ol",
            "heads/unique/unique_knyazev.mcsb",
            "heads/unique/unique_knyazev.ol",
            "heads/unique/unique_kondor.mcsb",
            "heads/unique/unique_kondor.ol",
            "heads/unique/unique_korenastiy.mcsb",
            "heads/unique/unique_korenastiy.ol",
            "heads/unique/unique_lipa.mcsb",
            "heads/unique/unique_lipa.ol",
            "heads/unique/unique_mahorkin.mcsb",
            "heads/unique/unique_mahorkin.ol",
            "heads/unique/unique_manok.mcsb",
            "heads/unique/unique_manok.ol",
            "heads/unique/unique_marxont.mcsb",
            "heads/unique/unique_marxont.ol",
            "heads/unique/unique_marxont_2.ol",
            "heads/unique/unique_mitropolit.mcsb",
            "heads/unique/unique_mitropolit.ol",
            "heads/unique/unique_mitropolit_2.ol",
            "heads/unique/unique_mongol.mcsb",
            "heads/unique/unique_mongol.ol",
            "heads/unique/unique_morg.mcsb",
            "heads/unique/unique_morg.ol",
            "heads/unique/unique_muller.mcsb",
            "heads/unique/unique_muller.ol",
            "heads/unique/unique_orakul.mcsb",
            "heads/unique/unique_orakul.ol",
            "heads/unique/unique_plastinka.mcsb",
            "heads/unique/unique_plastinka.ol",
            "heads/unique/unique_plastinka_2.ol",
            "heads/unique/unique_polkovnik.mcsb",
            "heads/unique/unique_polkovnik.ol",
            "heads/unique/unique_raguil.mcsb",
            "heads/unique/unique_raguil.ol",
            "heads/unique/unique_romeo.mcsb",
            "heads/unique/unique_romeo.ol",
            "heads/unique/unique_sglaz.mcsb",
            "heads/unique/unique_sglaz_green.ol",
            "heads/unique/unique_sglaz_yellow.ol",
            "heads/unique/unique_shaman.mcsb",
            "heads/unique/unique_shaman.ol",
            "heads/unique/unique_solynikov.mcsb",
            "heads/unique/unique_solynikov.ol",
            "heads/unique/unique_terentii.mcsb",
            "heads/unique/unique_terentii.ol",
            "heads/unique/unique_trup_quest.mcsb",
            "heads/unique/unique_trup_quest.ol",
            "heads/unique/unique_voroshilov.mcsb",
            "heads/unique/unique_voroshilov.ol",
            "heads/unique/unique_zigfrid.mcsb",
            "heads/unique/unique_zigfrid.ol",
            "heads/unique/unique_ziv.mcsb",
            "heads/unique/unique_ziv.ol",
            // heads/zombie/ -- complete real 12-file folder
            "heads/zombie/zombie_01.mcsb",
            "heads/zombie/zombie_01.ol",
            "heads/zombie/zombie_02.mcsb",
            "heads/zombie/zombie_02.ol",
            "heads/zombie/zombie_03.mcsb",
            "heads/zombie/zombie_03.ol",
            "heads/zombie/zombie_04.mcsb",
            "heads/zombie/zombie_04.ol",
            "heads/zombie/zombie_05.mcsb",
            "heads/zombie/zombie_05.ol",
            "heads/zombie/zombie_06.mcsb",
            "heads/zombie/zombie_06.ol",
            // heads/universal/ -- representative subset of the real 39-file folder
            "heads/universal/universal_01.mcsb",
            "heads/universal/universal_01.ol",
            "heads/universal/universal_13.ol",
            "heads/universal/universal_14.mcsb",
            "heads/universal/universal_14.ol",
            "heads/universal/universal_20.mcsb",
            "heads/universal/universal_20.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerPlayerCatalog.load finds exactly 4 real top-level folders plus the synthetic root key", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertEquals(5, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"arkit", "bodies", "head_animations", "heads", StalkerPlayerCatalog.ROOT_KEY}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn(ROOT_KEY): the complete real 13-file root, incl. the real base player rig", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn(StalkerPlayerCatalog.ROOT_KEY);
            assertEquals(13, files.size(), "root file count");
            for (String name : new String[]{"steve.mcsb", "steve.obj", "steve.ol", "steve.mic",
                    "steve_vanilla_proportions.mcsb", "reich_head_test.mcsb"}) {
                assertTrue(files.contains(name), name + " present");
            }
        }));

        out.add(test("filesIn(heads): every fixtured heads/ file is captured under one folder key", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            // 8 blessed + 123 players + 72 unique + 12 zombie + 7 universal (representative subset)
            assertEquals(222, catalog.filesIn("heads").size(), "heads file count");
        }));

        out.add(test("uniqueHeadNames: exactly the 33 real characters, each appearing once despite variant files", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            List<String> names = catalog.uniqueHeadNames();
            assertEquals(33, names.size(), "unique head name count");
            for (String name : new String[]{"davydov", "muller", "chatsky", "mitropolit", "lipa", "morg",
                    "orakul", "raguil", "sglaz", "terentii", "voroshilov", "knyazev", "shaman", "gershel"}) {
                assertTrue(names.contains(name), name + " present");
            }
        }));

        out.add(test("uniqueHeadNames: sglaz's two color-variant files still collapse to one name", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            long sglazCount = catalog.uniqueHeadNames().stream().filter(n -> n.equals("sglaz")).count();
            assertEquals(1, (int) sglazCount, "sglaz name occurrences");
        }));

        out.add(test("foldersContaining: reich_head_test is a ninth independent Reichstag corroboration, at the root", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("reich").contains(StalkerPlayerCatalog.ROOT_KEY), "reich");
        }));

        out.add(test("foldersContaining: steve resolves at the root, the real base player rig", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("steve").contains(StalkerPlayerCatalog.ROOT_KEY), "steve");
        }));

        out.add(test("foldersContaining: the 4 real faction-branded player skins all resolve under heads", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            for (String faction : new String[]{"duty1", "freedom1", "merc1", "covenant1"}) {
                assertTrue(catalog.foldersContaining(faction).contains("heads"), faction);
            }
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerPlayerCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("uniqueHeadNames: returns empty (not null) when heads/unique/ isn't present at all", () -> {
            Path dir = Files.createTempDirectory("stalkerplayer-fixture-nounique");
            dir.toFile().deleteOnExit();
            Path file = dir.resolve("arkit/arkit.mcsb");
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
            StalkerPlayerCatalog catalog = StalkerPlayerCatalog.load(dir);
            assertEquals(0, catalog.uniqueHeadNames().size(), "unique head name count with no heads/ folder");
        }));

        out.add(test("StalkerPlayerCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkerplayer-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerPlayerCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerPlayerCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkerplayer-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerPlayerCatalog.load(dir);
    }
}
