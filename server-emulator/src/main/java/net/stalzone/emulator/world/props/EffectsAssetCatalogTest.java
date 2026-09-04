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
 * Regression tests for {@link EffectsAssetCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/effects/} folder (part 62),
 * live-verified on device: the complete real 11-file {@code models/} folder, the single real {@code
 * shaders/} file, the complete real 48-file {@code textures/} folder, and a small representative
 * {@code particles/} subset -- including a direct regression test for this part's real
 * self-correction: {@code gravitation_distort.ol} genuinely exists TWICE, under both {@code models/}
 * and {@code particles/Texture/}, so {@code foldersContaining("gravitation_distort")} must resolve
 * to BOTH folders, not the single folder the first Javadoc draft assumed. Only real game-content
 * names are used, per this project's privacy rule -- never a real player username.
 */
public final class EffectsAssetCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // models/ -- the complete real 11-file folder
            "models/capsule_flat.mcsb",
            "models/capsule_symmetric.mcsb",
            "models/fxcloudroundtile.ol",
            "models/fxfogheavy.ol",
            "models/fxgroundwhisps.ol",
            "models/fxpuffstile.ol",
            "models/gravitation_distort.ol",
            "models/model_list.pack",
            "models/portal_dist1.ol",
            "models/teleport_bubble.mcsb",
            "models/unit_sphere.mcsb",
            // particles/ -- small representative subset, incl. the real duplicate regression case
            "particles/Texture/gravitation_distort.ol",
            "particles/prestige/scout_stun.efkmat",
            "particles/shamanfight_fx/arena_boss_bg.efkmat",
            "particles/anomalies/electra_n.efkmat",
            // shaders/ -- the complete real 1-file folder
            "shaders/light/ft_culling.comp",
            // textures/ -- the complete real 48-file folder
            "textures/bleeding.mic",
            "textures/brdf.ol",
            "textures/dummy_nmap.ol",
            "textures/envmap_diffuse.ol",
            "textures/envmap_gui.ol",
            "textures/envmap_gui_diffuse.ol",
            "textures/fx_noise.ol",
            "textures/horizon_mask.ol",
            "textures/kubemas.ol",
            "textures/lens_color.ol",
            "textures/lens_dirt_1.ol",
            "textures/missing_mipmaps.ol",
            "textures/moon_phases.mic",
            "textures/noise_stencil.ol",
            "textures/nvd_lamp.ol",
            "textures/pnv_offset.ol",
            "textures/pnv_offset0.ol",
            "textures/pnv_offset1.ol",
            "textures/pnv_offset2.ol",
            "textures/pnv_offset3.ol",
            "textures/pnv_offset4.ol",
            "textures/rain/PsiEjection_particles.mic",
            "textures/rain/PsiEjection_particles_2.mic",
            "textures/rain/ash_1.mic",
            "textures/rain/ash_2.mic",
            "textures/rain/rain.mic",
            "textures/rain/snow.mic",
            "textures/rain_drops.mic",
            "textures/snow/ice_texture_0.ol",
            "textures/snow/ice_texture_1.ol",
            "textures/snow/ice_texture_2.ol",
            "textures/snow/noise.ol",
            "textures/snow/snow.ol",
            "textures/snow/snow_cover_0.ol",
            "textures/snow/snow_cover_1.ol",
            "textures/snow/snow_cover_2.ol",
            "textures/snow/snow_cover_3.ol",
            "textures/sun.mic",
            "textures/sun.ol",
            "textures/sun_envmap.mic",
            "textures/sun_glare.mic",
            "textures/sun_gradient.mic",
            "textures/sun_occlusion.mic",
            "textures/sunglare.ol",
            "textures/water/large_waves.ol",
            "textures/water/noise.ol",
            "textures/water/normal_waves.ol",
            "textures/water/subtle_waves.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("EffectsAssetCatalog.load finds exactly the 4 real top-level folders", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            assertEquals(4, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("models"), "models present");
            assertTrue(catalog.folderNames().contains("particles"), "particles present");
            assertTrue(catalog.folderNames().contains("shaders"), "shaders present");
            assertTrue(catalog.folderNames().contains("textures"), "textures present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("REGRESSION (part 62 self-correction): gravitation_distort.ol resolves to BOTH real folders, not one", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> gravitationDistort = catalog.foldersContaining("gravitation_distort");
            assertEquals(2, gravitationDistort.size(), "gravitation_distort folder count");
            assertTrue(gravitationDistort.contains("models"), "gravitation_distort present under models");
            assertTrue(gravitationDistort.contains("particles"), "gravitation_distort present under particles");
        }));

        out.add(test("foldersContaining: portal_dist1 and teleport_bubble both live under models only", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> portal = catalog.foldersContaining("portal_dist1");
            assertEquals(1, portal.size(), "portal_dist1 folder count");
            assertTrue(portal.contains("models"), "portal_dist1 lives under models");
            List<String> teleport = catalog.foldersContaining("teleport");
            assertEquals(1, teleport.size(), "teleport folder count");
            assertTrue(teleport.contains("models"), "teleport_bubble lives under models");
        }));

        out.add(test("foldersContaining: ft_culling is the sole real shaders file", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> culling = catalog.foldersContaining("ft_culling");
            assertEquals(1, culling.size(), "ft_culling folder count");
            assertTrue(culling.contains("shaders"), "ft_culling lives under shaders");
            List<String> files = catalog.filesIn("shaders");
            assertEquals(1, files.size(), "shaders file count");
            assertTrue(files.contains("light/ft_culling.comp"), "ft_culling.comp present");
        }));

        out.add(test("foldersContaining: PsiEjection weather-particle textures live under textures", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> psi = catalog.foldersContaining("psiejection");
            assertEquals(1, psi.size(), "psiejection folder count (case-insensitive)");
            assertTrue(psi.contains("textures"), "PsiEjection textures live under textures");
        }));

        out.add(test("foldersContaining: moon_phases is real evidence of a lunar-cycle texture", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> moon = catalog.foldersContaining("moon_phases");
            assertEquals(1, moon.size(), "moon_phases folder count");
            assertTrue(moon.contains("textures"), "moon_phases lives under textures");
        }));

        out.add(test("foldersContaining: pnv_offset night-vision variants all resolve to textures", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> pnv = catalog.foldersContaining("pnv_offset");
            assertEquals(1, pnv.size(), "pnv_offset folder count");
            List<String> files = catalog.filesIn("textures");
            assertTrue(files.contains("pnv_offset0.ol"), "pnv_offset0 present");
            assertTrue(files.contains("pnv_offset4.ol"), "pnv_offset4 present");
        }));

        out.add(test("foldersContaining: bleeding status-effect texture lives under textures", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            List<String> bleeding = catalog.foldersContaining("bleeding");
            assertEquals(1, bleeding.size(), "bleeding folder count");
            assertTrue(bleeding.contains("textures"), "bleeding lives under textures");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            EffectsAssetCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("EffectsAssetCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("effects-asset-fixture-empty");
                dir.toFile().deleteOnExit();
                EffectsAssetCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static EffectsAssetCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("effects-asset-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return EffectsAssetCatalog.load(dir);
    }
}
