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
 * Regression tests for {@link StalkerMobsCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/stalkermobs/} folder (part 69), live-verified on
 * device: the complete real 35-file {@code configs/} folder, representative {@code models/} subsets
 * covering the {@code cybergiant}/{@code tsarevich}/{@code rudolf}/{@code doge} findings, a
 * representative {@code sound/} subset (incl. the model-less {@code jellyfish} lead and an {@code
 * inside_*} pair), and the complete real 2-file {@code textures/} folder. Only real game-content names
 * are used, per this project's privacy rule -- never a real player username.
 */
public final class StalkerMobsCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // configs/ -- the complete real 35-file folder
            "configs/base_boar.smm",
            "configs/base_boar_clone.smm",
            "configs/base_chimera.smm",
            "configs/base_chimera_clone.smm",
            "configs/base_cybergiant.smm",
            "configs/base_dog.smm",
            "configs/base_dog_clone.smm",
            "configs/base_doge.smm",
            "configs/base_drone.smm",
            "configs/base_flesh.smm",
            "configs/base_flesh_clone.smm",
            "configs/base_fpv.smm",
            "configs/base_krovosos.smm",
            "configs/base_krovosos_clone.smm",
            "configs/base_lurker.smm",
            "configs/base_pseudodog.smm",
            "configs/base_pseudodog_clone.smm",
            "configs/base_pseudogigant.smm",
            "configs/base_pseudogigant_clone.smm",
            "configs/base_psidog.smm",
            "configs/base_psidog_clone.smm",
            "configs/base_puppet.smm",
            "configs/base_puppet_clone.smm",
            "configs/base_rudolf.smm",
            "configs/base_snork.smm",
            "configs/base_snork_clone.smm",
            "configs/base_spirit.smm",
            "configs/base_thief_rat.smm",
            "configs/base_tick.smm",
            "configs/base_tsarevich.smm",
            "configs/base_tsarevna.smm",
            "configs/base_tumbler.smm",
            "configs/base_tushkan.smm",
            "configs/base_tushkan_clone.smm",
            "configs/base_wisp.smm",
            // models/cybergiant/ -- the complete real 9-file folder (a real pseudogigant-boss reskin)
            "models/cybergiant/att_emiss.ol",
            "models/cybergiant/cybergiant.mcsb",
            "models/cybergiant/psevdogigant_boss_att_diff.ol",
            "models/cybergiant/psevdogigant_boss_att_nrm.ol",
            "models/cybergiant/psevdogigant_boss_att_spek.ol",
            "models/cybergiant/psevdogigant_boss_body_diff.ol",
            "models/cybergiant/psevdogigant_boss_body_nrm.ol",
            "models/cybergiant/psevdogigant_boss_body_spek.ol",
            "models/cybergiant/setka_diff.ol",
            // models/tsarevich/ -- representative subset (16 real files total)
            "models/tsarevich/body_Diffuse.ol",
            "models/tsarevich/crown_Diffuse.ol",
            "models/tsarevich/crown_Normal.ol",
            "models/tsarevich/crown_Specular.ol",
            "models/tsarevich/frost_boar_diff.ol",
            // models/rudolf/ -- the complete real 13-file folder
            "models/rudolf/Rudolf_Angry_Diffuse.ol",
            "models/rudolf/Rudolf_Angry_Emissive.ol",
            "models/rudolf/Rudolf_Angry_Normal.ol",
            "models/rudolf/Rudolf_Angry_Specular.ol",
            "models/rudolf/Rudolf_Base_Diffuse.ol",
            "models/rudolf/Rudolf_Base_Emissive.ol",
            "models/rudolf/Rudolf_Base_Normal.ol",
            "models/rudolf/Rudolf_Base_Specular.ol",
            "models/rudolf/Rudolf_Medium_Diffuse.ol",
            "models/rudolf/Rudolf_Medium_Emissive.ol",
            "models/rudolf/Rudolf_Medium_Normal.ol",
            "models/rudolf/Rudolf_Medium_Specular.ol",
            "models/rudolf/rudolf.mcsb",
            // models/doge/ -- the complete real 7-file folder
            "models/doge/act_skeleton.ol",
            "models/doge/act_skeleton_fresh.ol",
            "models/doge/act_skeleton_tox.ol",
            "models/doge/act_skeleton_tox_emiss.ol",
            "models/doge/act_skeleton_tox_emiss2.ol",
            "models/doge/doge.mcsb",
            "models/doge/doge_mist.ol",
            // sound/ -- representative subset
            "sound/boar/boar_attack1.ogg",
            "sound/lurker/lurker_attack1.ogg",
            "sound/inside_snork/inside_snork_1.ogg",
            "sound/inside_krovosos/inside_krovosos_1.ogg",
            "sound/jellyfish/jellyfish_1.ogg",
            "sound/jellyfish/jellyfish_2.ogg",
            "sound/jellyfish/jellyfish_3.ogg",
            // textures/ -- the complete real 2-file folder
            "textures/particles/distortion.mic",
            "textures/particles/funnel_eye.mic",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("StalkerMobsCatalog.load finds exactly the 4 real top-level folders", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"configs", "models", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: configs is the complete real 35-file fixture", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("configs");
            assertEquals(35, files.size(), "configs file count");
            assertTrue(files.contains("base_wisp.smm"), "base_wisp.smm present");
        }));

        out.add(test("foldersContaining: cybergiant's real filenames confirm it is a pseudogigant-boss reskin", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            List<String> matches = catalog.foldersContaining("psevdogigant_boss");
            assertEquals(1, matches.size(), "psevdogigant_boss folder count");
            assertTrue(matches.contains("models"), "psevdogigant_boss lives under models/");
        }));

        out.add(test("foldersContaining: tsarevich's real crown + frost_boar textures confirm a crowned winter boar mutant", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("crown_Diffuse").contains("models"), "crown_Diffuse");
            assertTrue(catalog.foldersContaining("frost_boar").contains("models"), "frost_boar");
        }));

        out.add(test("foldersContaining: rudolf's real Angry/Base/Medium texture tiers resolve under models", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("Rudolf_Angry").contains("models"), "Rudolf_Angry");
            assertTrue(catalog.foldersContaining("Rudolf_Medium").contains("models"), "Rudolf_Medium");
        }));

        out.add(test("foldersContaining: doge's real skeleton/toxin texture variants resolve under models", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("act_skeleton_tox").contains("models"), "act_skeleton_tox");
        }));

        out.add(test("foldersContaining: jellyfish has real sound data but no models/ entry in this fixture", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("jellyfish").contains("sound"), "jellyfish under sound");
            assertTrue(catalog.filesIn("models").stream().noneMatch(f -> f.toLowerCase().contains("jellyfish")),
                    "jellyfish absent from models fixture");
        }));

        out.add(test("filesIn: textures is the complete real 2-file fixture", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("textures");
            assertEquals(2, files.size(), "textures file count");
            assertTrue(files.contains("particles/funnel_eye.mic"), "funnel_eye.mic present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            StalkerMobsCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("StalkerMobsCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("stalkermobs-fixture-empty");
                dir.toFile().deleteOnExit();
                StalkerMobsCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static StalkerMobsCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("stalkermobs-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return StalkerMobsCatalog.load(dir);
    }
}
