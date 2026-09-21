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
 * Regression tests for {@link CustomNpcsCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/customnpcs/} folder (part 68), live-verified on
 * device: the complete real 14-file {@code anim/} folder (incl. the literal Russian
 * "only for the technar boss" developer comment left in a real filename), the complete real 3-file
 * {@code models/} folder (incl. the real "slime" Minecraft-mob-name echo), a representative {@code
 * music/steven/sounds/} subset covering both {@code adept/} and a cross-section of the real 24-faction
 * {@code npc/} roster, and small representative subsets of {@code streamrandom/} and {@code
 * textures/}. Only real game-content names are used, per this project's privacy rule -- never a real
 * player username.
 */
public final class CustomNpcsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // anim/ -- the complete real 14-file folder
            "anim/boss_anim.mcal",
            "anim/character_thrasher.mcal",
            "anim/dialog_anim.mcal",
            "anim/menu_anim.mcal",
            "anim/npc_anim.mcal",
            "anim/npc_bar_anim.mcal",
            "anim/npc_gen2.mcal",
            "anim/npc_generic.mcal",
            "anim/npc_operations.mcal",
            "anim/npc_sanitar_anomaly_death_scene_0.mcal",
            "anim/npc_sanitar_base_anim.mcal",
            "anim/npc_zombie_crowd.mcal",
            "anim/revposes.mcal",
            "anim/technar_anims(только для босса технаря).mcal",
            // models/ -- the complete real 3-file folder
            "models/jahik.ol",
            "models/jahikarm.ol",
            "models/slime_model.mcsb",
            // music/steven/sounds/adept/ -- representative subset (42 real files total)
            "music/steven/sounds/adept/adept_attack1.ogg",
            "music/steven/sounds/adept/adept_growl1.ogg",
            "music/steven/sounds/adept/death_1.ogg",
            "music/steven/sounds/adept/hit_1.ogg",
            // music/steven/sounds/npc/ -- representative subset of the real 24-faction roster
            "music/steven/sounds/npc/dolg/after_fight_idle/after_fight_idle_1.ogg",
            "music/steven/sounds/npc/dolg/attack/attack_1.ogg",
            "music/steven/sounds/npc/freedom/attack/attack_1.ogg",
            "music/steven/sounds/npc/monolit/attack/attack_1.ogg",
            "music/steven/sounds/npc/bandits/attack/attack_1.ogg",
            "music/steven/sounds/npc/bandits_2(chihachev)/attack/attack_1.ogg",
            "music/steven/sounds/npc/military/attack/attack_1.ogg",
            "music/steven/sounds/npc/military_NATO/attack/attack_1.ogg",
            "music/steven/sounds/npc/mercenary/attack/attack_1.ogg",
            "music/steven/sounds/npc/diggers/attack/attack_1.ogg",
            "music/steven/sounds/npc/dungeon_sanitarov/attack/attack_1.ogg",
            "music/steven/sounds/npc/kondor_squad/attack/attack_1.ogg",
            "music/steven/sounds/npc/bar_npc/attack/attack_1.ogg",
            "music/steven/sounds/npc/blue_squad/attack/attack_1.ogg",
            "music/steven/sounds/npc/stalker/attack/attack_1.ogg",
            "music/steven/sounds/npc/tutorial/attack/attack_1.ogg",
            "music/steven/sounds/npc/zombie/attack/attack_1.ogg",
            "music/steven/sounds/npc/insideout/attack/attack_1.ogg",
            "music/steven/sounds/npc/pioneer/attack/attack_1.ogg",
            "music/steven/sounds/npc/toxichnaya_zemlyanka/attack/attack_1.ogg",
            "music/steven/sounds/npc/voland/voland_battle_phrase_one.ogg",
            "music/steven/sounds/npc/voland/voland_battle_phrase_two.ogg",
            "music/steven/sounds/npc/voland/voland_pain_scream_1.ogg",
            "music/steven/sounds/npc/zavet/attack/attack_1.ogg",
            "music/steven/sounds/npc/sujet3_npc/afon_4dunge/fight_idle_fire/fight_idle_fire_1.ogg",
            "music/steven/sounds/npc/14 new npc/attack/attack_1.ogg",
            // sound/ -- the complete real 1-file folder
            "sound/misc/swosh.ogg",
            // streamrandom/ -- representative subset (18 real files total, all under dmz/zombie/attack/)
            "streamrandom/dmz/zombie/attack/attack_1.ogg",
            "streamrandom/dmz/zombie/attack/attack_2.ogg",
            // textures/ -- representative subset
            "textures/gui/some_gui_element.mic",
            "textures/items/item_icon_1.ol",
            "textures/entity/entity_texture_1.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("CustomNpcsCatalog.load finds exactly the 6 real top-level folders", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertEquals(6, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"anim", "models", "music", "sound", "streamrandom", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("customnpcs-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            Path loose = dir.resolve("sorting_map.txt");
            Files.writeString(loose, "");
            loose.toFile().deleteOnExit();
            CustomNpcsCatalog catalog = CustomNpcsCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "loose root file must not be counted");
        }));

        out.add(test("foldersContaining: the literal 'only for the technar boss' developer comment resolves under anim", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("technar").contains("anim"), "technar");
        }));

        out.add(test("foldersContaining: slime_model.mcsb (a second real Minecraft-mob-name echo) resolves under models", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("slime").contains("models"), "slime");
        }));

        out.add(test("foldersContaining: real S.T.A.L.K.E.R.-series faction names (dolg, freedom, monolit) resolve under music", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("dolg").contains("music"), "dolg");
            assertTrue(catalog.foldersContaining("freedom").contains("music"), "freedom");
            assertTrue(catalog.foldersContaining("monolit").contains("music"), "monolit");
        }));

        out.add(test("foldersContaining: the named bandit-leader variant chihachev resolves under music", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("chihachev").contains("music"), "chihachev");
        }));

        out.add(test("foldersContaining: voland's own battle-phrase/pain-scream set resolves under music", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("voland_battle_phrase").contains("music"), "voland_battle_phrase");
            assertTrue(catalog.foldersContaining("voland_pain_scream").contains("music"), "voland_pain_scream");
        }));

        out.add(test("foldersContaining: DMZ-mode zombie attack barks resolve under streamrandom", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("dmz").contains("streamrandom"), "dmz");
        }));

        out.add(test("filesIn: models is the complete real 3-file fixture", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("models");
            assertEquals(3, files.size(), "models file count");
            assertTrue(files.contains("jahikarm.ol"), "jahikarm.ol present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            CustomNpcsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("CustomNpcsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("customnpcs-fixture-empty");
                dir.toFile().deleteOnExit();
                CustomNpcsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static CustomNpcsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("customnpcs-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return CustomNpcsCatalog.load(dir);
    }
}
