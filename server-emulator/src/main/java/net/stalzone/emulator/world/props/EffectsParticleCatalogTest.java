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
 * Regression tests for {@link EffectsParticleCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/effects/particles/} folder (part
 * 61), live-verified on device: the "prestige" ability-name cross-reference (a real, class/loadout-role
 * reading), the {@code shamanfight_fx}/{@code electrostorm_mutants} boss/mutant VFX, the
 * {@code anomalies} cross-reference to part 55's classic artefact-family names, a dated-seasonal
 * name, and -- most importantly -- a direct regression test for this part's real self-correction:
 * {@code laserpointer.efkmat} genuinely exists TWICE, under both {@code prestige_prototype_effects/}
 * and {@code weapons/stratg_exp/}, so {@code foldersContaining("laserpointer")} must resolve to
 * BOTH folders, not the single "isolated lead" the first Javadoc draft incorrectly claimed. Only
 * real game-content names are used, per this project's privacy rule -- never a real player username.
 */
public final class EffectsParticleCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "prestige/charger_enabled.efkmat",
            "prestige/charger_enabled_distortion.efkmat",
            "prestige/scanner_installation_zone.efkmat",
            "prestige/scanner_zone.efkmat",
            "prestige/scout_stun.efkmat",
            "prestige/scout_stun_post.efkmat",
            "prestige/shield_depth_pulse.efkmat",
            "prestige/support_guard.efkmat",
            "prestige/tank_explosion_sphere.efkmat",
            "prestige/tank_explosion_zone.efkmat",
            "prestige_prototype_effects/laserpointer.efkmat",
            "weapons/stratg_exp/laserpointer.efkmat",
            "shamanfight_fx/arena_boss_bg.efkmat",
            "shamanfight_fx/shamanfight_field_zone_blue.efkmat",
            "shamanfight_fx/shamanfight_field_zone_green.efkmat",
            "shamanfight_fx/shamanfight_field_zone_orange.efkmat",
            "shamanfight_fx/shamanfight_field_zone_pink.efkmat",
            "shamanfight_fx/shamanfight_field_zone_red.efkmat",
            "shamanfight_fx/shamanfight_field_zone_yellow.efkmat",
            "shamanfight_fx/clouds.efkmat",
            "shamanfight_fx/clouds2.efkmat",
            "shamanfight_fx/dust_particles.efkmat",
            "shamanfight_fx/glow1.efkmat",
            "shamanfight_fx/floor_t.efkmat",
            "morgfight_fx/morg_celling.efkmodel",
            "morgfight_fx/morg_floor.efkmodel",
            "electrostorm_mutants/boar.efkmat",
            "electrostorm_mutants/burelom.efkmat",
            "electrostorm_mutants/chimera.efkmat",
            "electrostorm_mutants/flesh.efkmat",
            "electrostorm_mutants/krovosos.efkmat",
            "electrostorm_npc/shield.efkmat",
            "operations/boss_electra.efkmat",
            "operations/electra_pillar.efkmat",
            "operations/cargo.efkmat",
            "anomalies/electra_n.efkmat",
            "anomalies/kisel.efkmat",
            "anomalies/voronka_n.efkmat",
            "anomalies/carousel_n.efkmat",
            "anomalies/tesla.efkmat",
            "artefacts/buran26.efkmat",
            "artefacts/jele.efkmat",
            "artefacts/linza.efkmat",
            "artefacts/petlya.efkmat",
            "hw23/pumpkin.efkmat",
            "caseopen/case_larec_dust.efkmat",
            "caseopen/case_light_dust.efkmat",
    };

    /**
     * Real files that sit loose directly in the {@code particles/} root, outside any subfolder --
     * the same disclosed root-vs-walked gap parts 55/57 found in other trees. {@link
     * EffectsParticleCatalog#load} only walks top-level DIRECTORIES, so these are deliberately kept
     * out of {@link #FIXTURE_FILES} (which totalFileCount is asserted against) and are instead used
     * by a dedicated test confirming the catalog genuinely does not see them.
     */
    private static final String[] LOOSE_ROOT_FILES = {
            "1.efkmat",
            "ground_line_default.efkmat",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("EffectsParticleCatalog.load finds every fixture top-level folder", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertTrue(catalog.folderNames().contains("prestige"), "prestige present");
            assertTrue(catalog.folderNames().contains("prestige_prototype_effects"), "prestige_prototype_effects present");
            assertTrue(catalog.folderNames().contains("weapons"), "weapons present");
            assertTrue(catalog.folderNames().contains("shamanfight_fx"), "shamanfight_fx present");
            assertTrue(catalog.folderNames().contains("morgfight_fx"), "morgfight_fx present");
            assertTrue(catalog.folderNames().contains("electrostorm_mutants"), "electrostorm_mutants present");
            assertTrue(catalog.folderNames().contains("electrostorm_npc"), "electrostorm_npc present");
            assertTrue(catalog.folderNames().contains("operations"), "operations present");
            assertTrue(catalog.folderNames().contains("anomalies"), "anomalies present");
            assertTrue(catalog.folderNames().contains("artefacts"), "artefacts present");
            assertTrue(catalog.folderNames().contains("hw23"), "hw23 present");
            assertTrue(catalog.folderNames().contains("caseopen"), "caseopen present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("REGRESSION (part 61 self-correction): laserpointer.efkmat resolves to BOTH real folders, not one", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            List<String> laserpointer = catalog.foldersContaining("laserpointer");
            assertEquals(2, laserpointer.size(), "laserpointer folder count");
            assertTrue(laserpointer.contains("prestige_prototype_effects"), "laserpointer present under prestige_prototype_effects");
            assertTrue(laserpointer.contains("weapons"), "laserpointer present under weapons");
        }));

        out.add(test("foldersContaining: prestige ability names (scout/tank/support) all live under prestige/", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(1, catalog.foldersContaining("scout_stun").size(), "scout_stun folder count");
            assertEquals(1, catalog.foldersContaining("tank_explosion").size(), "tank_explosion folder count");
            assertEquals(1, catalog.foldersContaining("support_guard").size(), "support_guard folder count");
            List<String> files = catalog.filesIn("prestige");
            assertTrue(files.contains("scout_stun.efkmat"), "scout_stun.efkmat present");
            assertTrue(files.contains("tank_explosion_zone.efkmat"), "tank_explosion_zone.efkmat present");
        }));

        out.add(test("foldersContaining: shamanfight_field_zone colored variants all resolve to shamanfight_fx", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            List<String> zones = catalog.foldersContaining("shamanfight_field_zone");
            assertEquals(1, zones.size(), "shamanfight_field_zone folder count");
            assertTrue(zones.contains("shamanfight_fx"), "zones live under shamanfight_fx");
            List<String> files = catalog.filesIn("shamanfight_fx");
            assertTrue(files.contains("shamanfight_field_zone_blue.efkmat"), "blue variant present");
            assertTrue(files.contains("shamanfight_field_zone_red.efkmat"), "red variant present");
            assertTrue(files.contains("shamanfight_field_zone_yellow.efkmat"), "yellow variant present");
        }));

        out.add(test("foldersContaining: morg_celling preserves the real on-disk ceiling misspelling", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            List<String> morgCelling = catalog.foldersContaining("morg_celling");
            assertEquals(1, morgCelling.size(), "morg_celling folder count");
            assertTrue(morgCelling.contains("morgfight_fx"), "morg_celling lives under morgfight_fx");
        }));

        out.add(test("foldersContaining: electrostorm named mutant roster resolves to electrostorm_mutants", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(1, catalog.foldersContaining("krovosos").size(), "krovosos folder count");
            assertEquals(1, catalog.foldersContaining("burelom").size(), "burelom folder count");
            List<String> files = catalog.filesIn("electrostorm_mutants");
            assertTrue(files.contains("boar.efkmat"), "boar present");
            assertTrue(files.contains("chimera.efkmat"), "chimera present");
            assertTrue(files.contains("flesh.efkmat"), "flesh present");
        }));

        out.add(test("foldersContaining: boss_electra corroborates operations as the Electra boss encounter", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            List<String> electra = catalog.foldersContaining("boss_electra");
            assertEquals(1, electra.size(), "boss_electra folder count");
            assertTrue(electra.contains("operations"), "boss_electra lives under operations");
        }));

        out.add(test("foldersContaining: anomalies cross-references the four classic artefact-family names", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(1, catalog.foldersContaining("electra_n").size(), "electra_n folder count");
            assertEquals(1, catalog.foldersContaining("kisel").size(), "kisel folder count");
            assertEquals(1, catalog.foldersContaining("voronka_n").size(), "voronka_n folder count");
            assertEquals(1, catalog.foldersContaining("carousel_n").size(), "carousel_n folder count");
            List<String> files = catalog.filesIn("anomalies");
            assertTrue(files.contains("electra_n.efkmat"), "electra_n.efkmat present");
            assertTrue(files.contains("carousel_n.efkmat"), "carousel_n.efkmat present");
        }));

        out.add(test("foldersContaining: buran26 is a real dated artefact name under artefacts", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            List<String> buran26 = catalog.foldersContaining("buran26");
            assertEquals(1, buran26.size(), "buran26 folder count");
            assertTrue(buran26.contains("artefacts"), "buran26 lives under artefacts");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            EffectsParticleCatalog catalog = loadFixture();
            assertTrue(!catalog.folderNames().contains("1.efkmat"), "loose root file is not a folder");
            assertEquals(0, catalog.foldersContaining("ground_line_default").size(), "loose root file unmatched by any folder walk");
        }));

        out.add(test("EffectsParticleCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("effects-particle-fixture-empty");
                dir.toFile().deleteOnExit();
                EffectsParticleCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static EffectsParticleCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("effects-particle-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        for (String relativePath : LOOSE_ROOT_FILES) {
            Path file = dir.resolve(relativePath);
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return EffectsParticleCatalog.load(dir);
    }
}
