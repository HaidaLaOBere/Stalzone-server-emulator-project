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
 * Regression tests for {@link StalkerClansCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/stalkerclans/} folder (part 72), live-verified
 * on device: the complete real 15-file {@code battlefields/} folder, the complete real 17-file {@code
 * models/} folder, the complete real 9-file {@code sound/} folder, the complete real 1-file {@code
 * stream/} folder, and a representative {@code textures/} subset. Only real game-content names are
 * used, per this project's privacy rule -- never a real player username.
 */
public final class StalkerClansCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // battlefields/ -- the complete real 15-file folder
            "battlefields/bridges.mic",
            "battlefields/hospital.mic",
            "battlefields/idiotway.mic",
            "battlefields/jail.mic",
            "battlefields/kishka.mic",
            "battlefields/kolizei.mic",
            "battlefields/kordon.mic",
            "battlefields/krot.mic",
            "battlefields/moryatnik.mic",
            "battlefields/peklo.mic",
            "battlefields/pripyat.mic",
            "battlefields/reich.mic",
            "battlefields/svalka.mic",
            "battlefields/td.mic",
            "battlefields/yama.mic",
            // models/ -- the complete real 17-file folder
            "models/br_root/br_root.mcsb",
            "models/br_spawn/br_spawn.mcsb",
            "models/clanchest.mcsb",
            "models/explosive.mcsb",
            "models/explosive.ol",
            "models/flag.mcsb",
            "models/flag_down.ol",
            "models/flag_up.ol",
            "models/orange_diff.ol",
            "models/orange_nrm.ol",
            "models/orange_spek.ol",
            "models/turrel1.mcsb",
            "models/turrel1.ol",
            "models/turrel2.mcsb",
            "models/turrel2.ol",
            "models/turrel3.mcsb",
            "models/turrel3.ol",
            // sound/ -- the complete real 9-file folder
            "sound/br_cinematic_missle_strike.ogg",
            "sound/br_wall.ogg",
            "sound/fight_preparation_start.ogg",
            "sound/gg_player_katana.ogg",
            "sound/gg_player_katana_other.ogg",
            "sound/gg_player_level_down.ogg",
            "sound/gg_player_level_up.ogg",
            "sound/transmit_end.ogg",
            "sound/transmit_start.ogg",
            // stream/ -- the complete real 1-file folder
            "stream/EvacuationBackground.ogg",
            // textures/ -- representative subset
            "textures/gui/atlas_battlefields_sections.mic",
            "textures/gui/atlas_killlog.mic",
            "textures/gui/battle_drop.mic",
            "textures/items/door.mic",
            "textures/items/explosive.mic",
            "textures/logo/alpha.ol",
            "textures/logo/anarchy.ol",
            "textures/logo/antarctica.ol",
            "textures/logo-dds/alpha.ol",
            "textures/logo-dds/anarchy.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerClansCatalog.load finds exactly the 5 real top-level folders", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertEquals(5, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"battlefields", "models", "sound", "stream", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: battlefields is the complete real 15-file fixture, incl. real S.T.A.L.K.E.R. locations", () -> {
            StalkerClansCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("battlefields");
            assertEquals(15, files.size(), "battlefields file count");
            for (String name : new String[]{"kordon.mic", "pripyat.mic", "svalka.mic"}) {
                assertTrue(files.contains(name), name + " present");
            }
        }));

        out.add(test("foldersContaining: reich is a fifth independent Reichstag-event corroboration", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("reich").contains("battlefields"), "reich");
        }));

        out.add(test("foldersContaining: real capture-the-flag props (flag_up, flag_down, clanchest) resolve under models", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("flag_up").contains("models"), "flag_up");
            assertTrue(catalog.foldersContaining("flag_down").contains("models"), "flag_down");
            assertTrue(catalog.foldersContaining("clanchest").contains("models"), "clanchest");
        }));

        out.add(test("foldersContaining: br_ prefix corroborates a real battle-royale mechanic across models and sound", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("br_root").contains("models"), "br_root under models");
            assertTrue(catalog.foldersContaining("br_wall").contains("sound"), "br_wall under sound");
        }));

        out.add(test("foldersContaining: gg_player_katana cross-references the existing real Katana melee weapon type", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("gg_player_katana").contains("sound"), "gg_player_katana");
        }));

        out.add(test("filesIn: sound is the complete real 9-file fixture", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertEquals(9, catalog.filesIn("sound").size(), "sound file count");
        }));

        out.add(test("filesIn: stream is the complete real 1-file fixture", () -> {
            StalkerClansCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("stream");
            assertEquals(1, files.size(), "stream file count");
            assertTrue(files.contains("EvacuationBackground.ogg"), "EvacuationBackground.ogg present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerClansCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerClansCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkerclans-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerClansCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerClansCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkerclans-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerClansCatalog.load(dir);
    }
}
