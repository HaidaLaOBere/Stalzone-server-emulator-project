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
 * Regression tests for {@link TextureParticlesCatalog}, using a fixture directory tree with the
 * complete real filenames from {@code modassets/assets/stalker/textures/particles/} (part 98) -- 95
 * files, live-verified on device via direct {@code find}: the complete real 49-file loose root, and
 * all 8 real subfolders ({@code campfire}, {@code debris}, {@code distortion}, {@code explosion},
 * {@code explosion_new}, {@code sj_boss}, {@code smoke}, {@code sparks}) in full.
 */
public final class TextureParticlesCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // root -- the complete real 49-file loose-file set
            "bubble.mic",
            "bubble.ol",
            "bubble2.mic",
            "candle_light.mic",
            "circle.ol",
            "crystal.mic",
            "crystaldif.mic",
            "dimfog.ol",
            "firetexture.mic",
            "fxfog01tile.ol",
            "fxfogheavy.ol",
            "fxgroundwhisps.ol",
            "fxpuffstile.ol",
            "fxsmokesoftsub.ol",
            "fxsmokesoftsub01.mic",
            "fxsmokesoftsub01.ol",
            "fxsmokesoftsubpng.mic",
            "fxsnowflakesblur01.mic",
            "fxsnowflakesblur01.ol",
            "fxsnowsoftsub.ol",
            "fxsnowsub.ol",
            "fxsnowtile.ol",
            "gradfroststream02.ol",
            "gradicestorm.ol",
            "gradsmokediss.ol",
            "gradsmokedisssoft.ol",
            "gradsmokelinesdissipate.ol",
            "gradsteamthin_ice.ol",
            "ice_spikes.mic",
            "kislota.mic",
            "kislota.ol",
            "lensflare_nod.mic",
            "lensflare_nod2.mic",
            "lensflare_nod2.ol",
            "raibow.mic",
            "redshar_eff.mic",
            "skyrimcloudlut.ol",
            "skyrimclouds01.ol",
            "skyrimcloudsupper03.ol",
            "skyrimcloudsupper04.ol",
            "smoke_nod.mic",
            "smoke_nod2.mic",
            "smoke_nod3.mic",
            "smokeparticles01.ol",
            "smokespiralstatic01.ol",
            "sphax.mic",
            "vaportile01.ol",
            "vaportile02.ol",
            "zast_1.mic",
            // campfire/ -- complete real 4-file folder
            "campfire/fire1.mic",
            "campfire/fire2.mic",
            "campfire/fire3.mic",
            "campfire/fire4.mic",
            // debris/ -- complete real 4-file folder
            "debris/stone_debris_1.ol",
            "debris/stone_debris_2.ol",
            "debris/stone_debris_3.ol",
            "debris/stone_debris_4.ol",
            // distortion/ -- complete real 13-file folder
            "distortion/bubble_distortion.ol",
            "distortion/glow.ol",
            "distortion/glow2.mic",
            "distortion/glow3.mic",
            "distortion/glow3.ol",
            "distortion/gravitation_distort.mic",
            "distortion/gravitation_distort.ol",
            "distortion/heat_distortion.mic",
            "distortion/heat_distortion.ol",
            "distortion/pfx_dist2_big.mic",
            "distortion/portal_dist1.mic",
            "distortion/portal_el.mic",
            "distortion/sphere.mic",
            // explosion/ -- complete real 1-file folder
            "explosion/dirt-particles.mic",
            // explosion_new/ -- complete real 4-file folder
            "explosion_new/explosion2_new.mic",
            "explosion_new/explosion_new.mic",
            "explosion_new/smoke_new.mic",
            "explosion_new/spark.ol",
            // sj_boss/ -- complete real 1-file folder
            "sj_boss/pfx_stonedebris1_boss.mic",
            // smoke/ -- complete real 4-file folder
            "smoke/1.mic",
            "smoke/smoke5.mic",
            "smoke/smoke_animated.mic",
            "smoke/smoketest.mic",
            // sparks/ -- complete real 15-file folder
            "sparks/karusel_eff.mic",
            "sparks/karusel_eff.ol",
            "sparks/karusel_eff2.mic",
            "sparks/molny.mic",
            "sparks/molny.ol",
            "sparks/molny2.mic",
            "sparks/molny2.ol",
            "sparks/molny2z.mic",
            "sparks/molny3.mic",
            "sparks/molny6.mic",
            "sparks/molny7.mic",
            "sparks/pfx_concretepuffs_01.mic",
            "sparks/pfx_vehglassdebris_01.ol",
            "sparks/spark_fire.ol",
            "sparks/sparks.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("TextureParticlesCatalog.load finds all 8 real subfolders plus the root", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(9, catalog.totalFolderCount(), "folder count (8 subfolders + root)");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (95 total)", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(95, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("filesIn: root is the complete real 49-file loose-file set", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(49, catalog.filesIn(TextureParticlesCatalog.ROOT_KEY).size(), "root file count");
        }));

        out.add(test("filesIn: distortion is the complete real 13-file folder", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(13, catalog.filesIn("distortion").size(), "distortion file count");
        }));

        out.add(test("filesIn: sparks is the complete real 15-file folder", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("sparks");
            assertEquals(15, files.size(), "sparks file count");
            assertTrue(files.contains("molny.ol"), "molny.ol present");
        }));

        out.add(test("foldersContaining: zast resolves at the root, the real Stasis anomaly effect", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("zast").contains(TextureParticlesCatalog.ROOT_KEY), "zast");
        }));

        out.add(test("foldersContaining: candle_light resolves at the root, the real Lighter/Inferno artifact", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("candle_light").contains(TextureParticlesCatalog.ROOT_KEY),
                    "candle_light");
        }));

        out.add(test("foldersContaining: crystal resolves at the root, the real Blizzard-event Crystal", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("crystal").contains(TextureParticlesCatalog.ROOT_KEY), "crystal");
        }));

        out.add(test("foldersContaining: bubble resolves at the root, the real Burrow anomaly", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("bubble").contains(TextureParticlesCatalog.ROOT_KEY), "bubble");
        }));

        out.add(test("foldersContaining: gravitation_distort resolves under distortion (cross-ref to part 62)", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("gravitation_distort").contains("distortion"),
                    "gravitation_distort");
        }));

        out.add(test("foldersContaining: molny resolves under sparks, the real Electrostorm lightning effect", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("molny").contains("sparks"), "molny");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            TextureParticlesCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("TextureParticlesCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("textureparticles-fixture-empty");
                dir.toFile().deleteOnExit();
                TextureParticlesCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static TextureParticlesCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("textureparticles-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return TextureParticlesCatalog.load(dir);
    }
}
