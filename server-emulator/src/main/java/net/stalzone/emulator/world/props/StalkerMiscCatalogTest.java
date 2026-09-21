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
 * Regression tests for {@link StalkerMiscCatalog}, using a fixture directory tree with the complete
 * real filenames drawn directly from the real {@code modassets/assets/stalker/} folder's 5 small
 * leaf trees swept together in part 93 (85 files total): {@code hungerBuffs/} (1), {@code anims/}
 * (5), {@code figma/} (10), {@code slices/} (13), {@code figma_gui/} (56 across 13 subfolders),
 * live-verified on device. Only real game-content/asset names are used, per this project's privacy
 * rule -- never a real player username.
 */
public final class StalkerMiscCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // hungerBuffs/ -- the complete real 1-file folder
            "hungerBuffs/kit.mic",
            // anims/ -- the complete real 5-file folder (all under menu/)
            "anims/menu/idle.mcal",
            "anims/menu/idle_new.mcal",
            "anims/menu/menu_idle.mcal",
            "anims/menu/menu_walk.mcal",
            "anims/menu/steve_anim.mcal",
            // figma/ -- the complete real 10-file folder (figma.bin + 9 hashed images)
            "figma/figma.bin",
            "figma/images/025dac4dee7ba7d3e5e1269905d081532f170d44.mic",
            "figma/images/02c890ebfa9d0a0a3170b62fc3f7e63f854b812c.mic",
            "figma/images/474f5e964c7876e7227e78d91db766ec9fdc5ac5.mic",
            "figma/images/4e6e70a6063eda030ac481205861ae41b0062be9.mic",
            "figma/images/8f14469462772900726f581054e25b0481e09c3d.mic",
            "figma/images/92c6299061dc06bbd3b5599c83e07e05e456bc02.mic",
            "figma/images/b8c50be02c369a2b3a841a855816743f39267ae1.mic",
            "figma/images/c4b50aa76aac82998a3c5b60d78c409701f16e99.mic",
            "figma/images/c5e9d05d1356ad2535bfd4ad69d2f98fa3384308.mic",
            // slices/ -- the complete real 13-file folder
            "slices/bar_save_residential_hangar.mcws",
            "slices/battlefields.mcws",
            "slices/derevnya_novichkov.mcws",
            "slices/north_factions_block.mcws",
            "slices/sever.mcws",
            "slices/stalkers_sovhoz.mcws",
            "slices/testers_base.mcws",
            "slices/testers_covenant.mcws",
            "slices/testers_duty.mcws",
            "slices/testers_freedom.mcws",
            "slices/testers_merc.mcws",
            "slices/testers_rostok.mcws",
            "slices/testers_sever.mcws",
            // figma_gui/ -- the complete real 56-file folder across 13 subfolders
            "figma_gui/bf_finish_stat/figma.bin",
            "figma_gui/bf_finish_stat/images/025dac4dee7ba7d3e5e1269905d081532f170d44.mic",
            "figma_gui/bf_finish_stat/images/02c890ebfa9d0a0a3170b62fc3f7e63f854b812c.mic",
            "figma_gui/bf_finish_stat/images/3b41171c3496d82a5276041c47b192df9dd28289.mic",
            "figma_gui/bf_finish_stat/images/3da97c0444669466ac263213e2019e88aae68374.mic",
            "figma_gui/bf_finish_stat/images/44827001efdc0230504cfc720271b97a4f14dde8.mic",
            "figma_gui/bf_finish_stat/images/474f5e964c7876e7227e78d91db766ec9fdc5ac5.mic",
            "figma_gui/bf_finish_stat/images/4e6e70a6063eda030ac481205861ae41b0062be9.mic",
            "figma_gui/bf_finish_stat/images/666ad67692447f962dd02128f8d759d114db9600.mic",
            "figma_gui/bf_finish_stat/images/8f14469462772900726f581054e25b0481e09c3d.mic",
            "figma_gui/bf_finish_stat/images/92c6299061dc06bbd3b5599c83e07e05e456bc02.mic",
            "figma_gui/bf_finish_stat/images/ae8c92007c2db5bcc9219d03f8ca30ec81e87078.mic",
            "figma_gui/bf_finish_stat/images/b538396ba85640fc1ba9fdd2912d625380f9e5c1.mic",
            "figma_gui/bf_finish_stat/images/b8c50be02c369a2b3a841a855816743f39267ae1.mic",
            "figma_gui/bf_finish_stat/images/c4b50aa76aac82998a3c5b60d78c409701f16e99.mic",
            "figma_gui/bf_finish_stat/images/c5e9d05d1356ad2535bfd4ad69d2f98fa3384308.mic",
            "figma_gui/bf_finish_stat/images/e9cafcd9c5257e870539f3676af409bf104f3922.mic",
            "figma_gui/bf_spawn_select/figma.bin",
            "figma_gui/bf_spawn_select/images/2e8d3227a4f65e23cb24ee1f89fcbe38e46faeac.mic",
            "figma_gui/bf_spawn_select/images/d1198369a540471d7f559230c6fad7538912012c.mic",
            "figma_gui/bf_spawn_select/images/fe188a326f25d08ef34f83bfd9ae32f5b25be26f.mic",
            "figma_gui/bf_stats/figma.bin",
            "figma_gui/buttons/figma.bin",
            "figma_gui/clans_hud/icons/home.ol",
            "figma_gui/death_screen/figma.bin",
            "figma_gui/death_screen/images/772a2cc5ede3db5efc643b9a0d4077e71b85c413.mic",
            "figma_gui/death_screen/images/b097c89e9a0d99908e5062230f64915d4f9a5df7.mic",
            "figma_gui/death_screen/images/d1f3e0993a95297d428eb2a0e2ea8ee5a50d4358.mic",
            "figma_gui/death_screen/images/da7de998e2ce3b298964b7bce8433a7254dfb10f.mic",
            "figma_gui/death_screen/images/e16e325541de20ca332e40d70c3aa12bb1634190.mic",
            "figma_gui/ffa_finish_stat/figma.bin",
            "figma_gui/ffa_finish_stat/images/025dac4dee7ba7d3e5e1269905d081532f170d44.mic",
            "figma_gui/ffa_finish_stat/images/02c890ebfa9d0a0a3170b62fc3f7e63f854b812c.mic",
            "figma_gui/ffa_finish_stat/images/3b41171c3496d82a5276041c47b192df9dd28289.mic",
            "figma_gui/ffa_finish_stat/images/3da97c0444669466ac263213e2019e88aae68374.mic",
            "figma_gui/ffa_finish_stat/images/474f5e964c7876e7227e78d91db766ec9fdc5ac5.mic",
            "figma_gui/ffa_finish_stat/images/4e6e70a6063eda030ac481205861ae41b0062be9.mic",
            "figma_gui/ffa_finish_stat/images/8f14469462772900726f581054e25b0481e09c3d.mic",
            "figma_gui/ffa_finish_stat/images/92c6299061dc06bbd3b5599c83e07e05e456bc02.mic",
            "figma_gui/ffa_finish_stat/images/97d6922f7251d259c5f1fdb5b77ad847fdeabd2f.mic",
            "figma_gui/ffa_finish_stat/images/ae8c92007c2db5bcc9219d03f8ca30ec81e87078.mic",
            "figma_gui/ffa_finish_stat/images/b8c50be02c369a2b3a841a855816743f39267ae1.mic",
            "figma_gui/ffa_finish_stat/images/c4b50aa76aac82998a3c5b60d78c409701f16e99.mic",
            "figma_gui/ffa_finish_stat/images/c5e9d05d1356ad2535bfd4ad69d2f98fa3384308.mic",
            "figma_gui/ffa_finish_stat/images/e9cafcd9c5257e870539f3676af409bf104f3922.mic",
            "figma_gui/ffa_grid/figma.bin",
            "figma_gui/ffa_spawn_select/figma.bin",
            "figma_gui/ffa_spawn_select/images/2e8d3227a4f65e23cb24ee1f89fcbe38e46faeac.mic",
            "figma_gui/ffa_spawn_select/images/d1198369a540471d7f559230c6fad7538912012c.mic",
            "figma_gui/ffa_spawn_select/images/fe188a326f25d08ef34f83bfd9ae32f5b25be26f.mic",
            "figma_gui/ffa_stats/figma.bin",
            "figma_gui/ravent_finish/figma.bin",
            "figma_gui/ravent_round/figma.bin",
            "figma_gui/ravent_round/images/0996ee7099770c95e511ca6e5222047e30a6d5f8.mic",
            "figma_gui/ravent_round/images/4638291ff51c0febc2603ac9da0a53c47408276a.mic",
            "figma_gui/test/figma.bin",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerMiscCatalog.load finds exactly the 5 known small-tree folders", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertEquals(5, catalog.totalFolderCount(), "folder count");
            for (String known : StalkerMiscCatalog.KNOWN_FOLDERS) {
                assertTrue(catalog.folderNames().contains(known), known + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (85 total)", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(85, catalog.totalFileCount(), "matches the real 85-file sweep");
        }));

        out.add(test("filesIn: hungerBuffs is the complete real 1-file folder", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("hungerBuffs");
            assertEquals(1, files.size(), "hungerBuffs file count");
            assertTrue(files.contains("kit.mic"), "kit.mic present");
        }));

        out.add(test("filesIn: anims is the complete real 5-file menu-animation folder", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("anims");
            assertEquals(5, files.size(), "anims file count");
            assertTrue(files.contains("menu/steve_anim.mcal"), "steve_anim.mcal present");
        }));

        out.add(test("filesIn: slices is the complete real 13-file world-slice folder", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("slices");
            assertEquals(13, files.size(), "slices file count");
            assertTrue(files.contains("stalkers_sovhoz.mcws"), "stalkers_sovhoz.mcws present");
            assertTrue(files.contains("sever.mcws"), "sever.mcws present");
        }));

        out.add(test("filesIn: figma_gui is the complete real 56-file folder across 13 subfolders", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("figma_gui");
            assertEquals(56, files.size(), "figma_gui file count");
        }));

        out.add(test("foldersContaining: steve_anim (third independent \"steve\" reference) resolves under anims", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("steve_anim").contains("anims"), "steve_anim");
        }));

        out.add(test("foldersContaining: testers_covenant/testers_duty/testers_freedom/testers_merc resolve under slices", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("testers_covenant").contains("slices"), "testers_covenant");
            assertTrue(catalog.foldersContaining("testers_duty").contains("slices"), "testers_duty");
            assertTrue(catalog.foldersContaining("testers_freedom").contains("slices"), "testers_freedom");
            assertTrue(catalog.foldersContaining("testers_merc").contains("slices"), "testers_merc");
        }));

        out.add(test("foldersContaining: sovhoz and sever (real Bar/winter-zone locations) resolve under slices", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("sovhoz").contains("slices"), "sovhoz");
            assertTrue(catalog.foldersContaining("sever").contains("slices"), "sever");
        }));

        out.add(test("foldersContaining: bf_ (Battlefield clan-war mode) resolves under figma_gui", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("bf_finish_stat").contains("figma_gui"), "bf_finish_stat");
            assertTrue(catalog.foldersContaining("bf_spawn_select").contains("figma_gui"), "bf_spawn_select");
        }));

        out.add(test("foldersContaining: ffa_ (second PvP mode, free-for-all) resolves under figma_gui", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("ffa_grid").contains("figma_gui"), "ffa_grid");
            assertTrue(catalog.foldersContaining("ffa_spawn_select").contains("figma_gui"), "ffa_spawn_select");
        }));

        out.add(test("foldersContaining: ravent_ (Crow Arena / Arena of Slavs seasonal event) resolves under figma_gui", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("ravent_finish").contains("figma_gui"), "ravent_finish");
            assertTrue(catalog.foldersContaining("ravent_round").contains("figma_gui"), "ravent_round");
        }));

        out.add(test("load ignores a real folder that is not on the known-small-tree whitelist", () -> {
            StalkerMiscCatalog catalog = loadFixtureWithUnrelatedFolder();
            assertEquals(5, catalog.totalFolderCount(), "folder count unaffected by unrelated folder");
            assertTrue(!catalog.folderNames().contains("sound"), "sound not indexed (not in whitelist)");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerMiscCatalog.load rejects a root with none of the known folders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalker-misc-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerMiscCatalog.load(dir);
            }, "root with no known folders");
        }));

        return out;
    }

    private static StalkerMiscCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalker-misc-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerMiscCatalog.load(dir);
    }

    private static StalkerMiscCatalog loadFixtureWithUnrelatedFolder() throws Exception {
        Path dir = Files.createTempDirectory("stalker-misc-fixture-unrelated");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        Path unrelated = dir.resolve("sound/effects/not_from_this_catalog.ogg");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "");
        unrelated.toFile().deleteOnExit();
        return StalkerMiscCatalog.load(dir);
    }
}
