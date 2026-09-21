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
 * Regression tests for {@link HighpolyCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/highpoly/} folder (part 65), live-verified on
 * device: a representative {@code animations/} subset, the complete real 4-file {@code
 * animationsShnir/} folder plus representative subsets of {@code animationstechnar/}/{@code
 * animationsnpc/}/{@code animationszombie/}/{@code casts/} under {@code character/}, the complete
 * real 5-file {@code firstperson/base/} folder, and a representative {@code lipsync/} subset
 * including the cross-reference names shared with part 63's {@code gloomycore/banks/}. Only real
 * game-content names are used, per this project's privacy rule -- never a real player username.
 */
public final class HighpolyCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // animations/ -- representative subset (395 real files total)
            "animations/character.mcal",
            "animations/fp_walkcycles.mcvd",
            "animations/wpn_fp_6p62.mcvd",
            "animations/wpn_fp_92fs.mcvd",
            "animations/wpn_fp_a545.mcvd",
            "animations/wpn_fp_a545_newyear2023.mcvd",
            "animations/wpn_fp_G36.mcvd",
            "animations/wpn_ppk20_winter25.mcvd",
            "animations/wpn_sa20_rhythm.mcvd",
            "animations/wpn_sa20_takt.mcvd",
            // character/ -- 7 real loose files
            "character/hitbox.mcvd",
            "character/origin.mcsb",
            "character/origin.mcvd",
            "character/originShnir.mcsb",
            "character/originpov.mcsb",
            "character/origintechnar.mcsb",
            "character/ragdoll.mcsb",
            // character/animationsShnir/ -- the complete real 4-file folder
            "character/animationsShnir/shnir_airtime.mcal",
            "character/animationsShnir/shnir_idle.mcal",
            "character/animationsShnir/shnir_jump_cut.mcal",
            "character/animationsShnir/shnir_run.mcal",
            // character/animationstechnar/ -- representative subset (55 real files total)
            "character/animationstechnar/hadouken_cycle.mcal",
            "character/animationstechnar/hadouken_start.mcal",
            "character/animationstechnar/hadouken_end.mcal",
            "character/animationstechnar/stolb_cycle.mcal",
            "character/animationstechnar/vent_cycle.mcal",
            "character/animationstechnar/button_cycle.mcal",
            "character/animationstechnar/character_stand_unarmed_idle.mcal",
            // character/animationsnpc/ and animationszombie/ -- representative subsets
            "character/animationsnpc/character_stand_unarmed_idle.mcal",
            "character/animationszombie/character_jump_armed_idle_rightleg_cycle.mcal",
            "character/animationszombie/character_jump_rifle_run_forward_rightleg_enter.mcal",
            // character/casts/ -- representative subset (46 real files total)
            "character/casts/CharacterCharacterGunLayerLib.sal",
            "character/casts/CharacterCharacterGunLayerPistol.sai",
            "character/casts/CharacterCharacterGunLayerRifle.sai",
            "character/casts/CharacterCharacterMeleeLayerAxe.sai",
            "character/casts/CharacterCharacterMeleeLayerDubina.sai",
            "character/casts/CharacterCharacterMeleeLayerFists.sai",
            "character/casts/CharacterCharacterMeleeLayerKarambit.sai",
            "character/casts/CharacterCharacterMeleeLayerKatana.sai",
            "character/casts/CharacterCharacterMeleeLayerLib.sal",
            "character/casts/CharacterCharacterMeleeLayerLopataDigger.sai",
            // firstperson/base/ -- the complete real 5-file folder
            "firstperson/base/FpBaseWalkType.sal",
            "firstperson/base/FpBaseWalkTypeDefault.sai",
            "firstperson/base/FpBaseWalkTypeDefaultAiming.sai",
            "firstperson/base/FpBaseWalkTypeProne.sai",
            "firstperson/base/FpBaseWalkTypeProneAiming.sai",
            // lipsync/ -- representative subset (322 real files total), incl. the banks/ cross-reference
            "lipsync/2_ahav.mcvd",
            "lipsync/2_ahav_en.mcvd",
            "lipsync/vasilich.mcvd",
            "lipsync/vasilich_en.mcvd",
            "lipsync/verbovshik_grey.mcvd",
            "lipsync/verbovshik_kondor.mcvd",
            "lipsync/verbovshik_perst.mcvd",
            "lipsync/verbovshik_rakita.mcvd",
            "lipsync/verbovshik_sozonka.mcvd",
            "lipsync/vodovorot_sobitiy.mcvd",
            "lipsync/voennie_v_peshere.mcvd",
            "lipsync/tutorial_3.mcvd",
            "lipsync/tutorial_3_en.mcvd",
            "lipsync/terentiy.mcvd",
            "lipsync/terentiy_en.mcvd",
            "lipsync/svyatoy_en.mcvd",
            "lipsync/tochka_nevozvrata.mcvd",
            "lipsync/tochka_nevozvrata2.mcvd",
            "lipsync/tetris.mcvd",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("HighpolyCatalog.load finds exactly the 4 real top-level folders", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"animations", "character", "firstperson", "lipsync"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("highpoly-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            String[] looseRootFiles = {"character.mcsb", "character_hands.mcsb", "wpn_fp_mp7.mcvd"};
            for (String loose : looseRootFiles) {
                Path file = dir.resolve(loose);
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            HighpolyCatalog catalog = HighpolyCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(),
                    "loose root files must not be counted");
        }));

        out.add(test("foldersContaining: shnir now resolves to a real, distinct animated character (part 63 lead resolved)", () -> {
            HighpolyCatalog catalog = loadFixture();
            List<String> shnir = catalog.foldersContaining("shnir");
            assertEquals(1, shnir.size(), "shnir folder count");
            assertTrue(shnir.contains("character"), "shnir lives under character/");
        }));

        out.add(test("foldersContaining: technar's hadouken special move is a real, striking cut-content lead", () -> {
            HighpolyCatalog catalog = loadFixture();
            List<String> hadouken = catalog.foldersContaining("hadouken");
            assertEquals(1, hadouken.size(), "hadouken folder count");
            assertTrue(hadouken.contains("character"), "hadouken lives under character/");
            List<String> technar = catalog.foldersContaining("technar");
            assertTrue(technar.contains("character"), "technar (originstechnar/animationstechnar) lives under character/");
        }));

        out.add(test("foldersContaining: lipsync cross-references the real banks/ NPC roster (vasilich, verbovshik set)", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("vasilich").contains("lipsync"), "vasilich");
            assertTrue(catalog.foldersContaining("verbovshik_kondor").contains("lipsync"), "verbovshik_kondor");
            assertTrue(catalog.foldersContaining("vodovorot_sobitiy").contains("lipsync"), "vodovorot_sobitiy");
        }));

        out.add(test("foldersContaining: real melee weapon-layer types (Karambit, Katana, LopataDigger) live under character/casts", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("Karambit").contains("character"), "Karambit");
            assertTrue(catalog.foldersContaining("Katana").contains("character"), "Katana");
            assertTrue(catalog.foldersContaining("LopataDigger").contains("character"), "LopataDigger");
        }));

        out.add(test("filesIn: firstperson/base is the complete real 5-file fixture", () -> {
            HighpolyCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("firstperson");
            assertEquals(5, files.size(), "firstperson file count");
            assertTrue(files.contains("base/FpBaseWalkTypeProneAiming.sai"), "FpBaseWalkTypeProneAiming.sai present");
        }));

        out.add(test("foldersContaining: seasonal weapon-skin variants (newyear2023, winter25) live under animations", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("newyear2023").contains("animations"), "newyear2023");
            assertTrue(catalog.foldersContaining("winter25").contains("animations"), "winter25");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            HighpolyCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("HighpolyCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("highpoly-fixture-empty");
                dir.toFile().deleteOnExit();
                HighpolyCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static HighpolyCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("highpoly-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return HighpolyCatalog.load(dir);
    }
}
