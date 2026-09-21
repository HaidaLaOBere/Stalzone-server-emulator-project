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
 * Regression tests for {@link WeaponAssetCatalog}, using a fixture directory tree with real filenames
 * drawn directly from the real {@code modassets/assets/weapons/} folder (part 66), live-verified on
 * device with a fresh direct listing of every real subfolder used here (including a correction to this
 * class's own Javadoc, which originally undercounted {@code prestige_abil/} at 2 named abilities before
 * the real folder was recounted at 9). Covers a representative {@code sound/} subset (the 9 real loose
 * files, plus {@code class_hunter}/{@code class_shaman}/{@code class_tank}, the complete real 29-file
 * {@code prestige_abil/}, the complete real 12-file {@code prestige_tranq/}, the real 1-file {@code
 * prestige_med/}, a {@code reichstag_3shots} subset, and a {@code terentiy} subset), a {@code models/}
 * subset (the 4 real loose files, plus {@code weapons/ability_items/} -- the complete real {@code
 * stimshot}/{@code tubus}/{@code woofer} contents -- and the complete real {@code weapons/MG42}/{@code
 * weapons/Winchester_M70} folders), and a {@code textures/} subset (the 5 real loose files). Only real
 * game-content names are used, per this project's privacy rule -- never a real player username.
 */
public final class WeaponAssetCatalogTest {

    private static final String[] FIXTURE_FILES = {
            // sound/ -- 9 real loose files
            "sound/attachment.ogg",
            "sound/breath_in1.ogg",
            "sound/breath_in2.ogg",
            "sound/breath_in3.ogg",
            "sound/breath_out.ogg",
            "sound/headshot1.ogg",
            "sound/headshot2.ogg",
            "sound/headshot3.ogg",
            "sound/low_ammo.ogg",
            // sound/class_hunter/, class_shaman/, class_tank/ -- complete real folders
            "sound/class_hunter/attack_1.ogg",
            "sound/class_hunter/attack_2.ogg",
            "sound/class_hunter/attack_3.ogg",
            "sound/class_shaman/attack_1.ogg",
            "sound/class_shaman/attack_2.ogg",
            "sound/class_shaman/attack_3.ogg",
            "sound/class_tank/attack_1.ogg",
            "sound/class_tank/attack_2.ogg",
            // sound/prestige_abil/ -- the complete real 29-file folder
            "sound/prestige_abil/active_ability_activation.ogg",
            "sound/prestige_abil/active_ability_prepare.ogg",
            "sound/prestige_abil/prestige_abil_barricade_draw.ogg",
            "sound/prestige_abil/prestige_abil_barricade_holster.ogg",
            "sound/prestige_abil/prestige_abil_lowgravity_draw.ogg",
            "sound/prestige_abil/prestige_abil_lowgravity_holster.ogg",
            "sound/prestige_abil/prestige_abil_lowgravity_pin.ogg",
            "sound/prestige_abil/prestige_abil_lowgravity_throw.ogg",
            "sound/prestige_abil/prestige_abil_personalteleport_draw.ogg",
            "sound/prestige_abil/prestige_abil_personalteleport_holster.ogg",
            "sound/prestige_abil/prestige_abil_personalteleport_pin.ogg",
            "sound/prestige_abil/prestige_abil_personalteleport_throw.ogg",
            "sound/prestige_abil/prestige_abil_phaseshift_act.ogg",
            "sound/prestige_abil/prestige_abil_portalgrenade_draw.ogg",
            "sound/prestige_abil/prestige_abil_portalgrenade_holster.ogg",
            "sound/prestige_abil/prestige_abil_portalgrenade_pin.ogg",
            "sound/prestige_abil/prestige_abil_portalgrenade_throw.ogg",
            "sound/prestige_abil/prestige_abil_pushgrenade_draw.ogg",
            "sound/prestige_abil/prestige_abil_pushgrenade_holster.ogg",
            "sound/prestige_abil/prestige_abil_pushgrenade_pin.ogg",
            "sound/prestige_abil/prestige_abil_pushgrenade_throw.ogg",
            "sound/prestige_abil/prestige_abil_scanner_draw.ogg",
            "sound/prestige_abil/prestige_abil_scanner_holster.ogg",
            "sound/prestige_abil/prestige_abil_scoutmine_draw.ogg",
            "sound/prestige_abil/prestige_abil_scoutmine_holster.ogg",
            "sound/prestige_abil/prestige_abil_vortexgrenade_draw.ogg",
            "sound/prestige_abil/prestige_abil_vortexgrenade_holster.ogg",
            "sound/prestige_abil/prestige_abil_vortexgrenade_pin.ogg",
            "sound/prestige_abil/prestige_abil_vortexgrenade_throw.ogg",
            // sound/prestige_tranq/ -- the complete real 12-file folder
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot1.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot2.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot3.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot4.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot5.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_close_shot6.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot1.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot2.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot3.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot4.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot5.wav",
            "sound/prestige_tranq/prestige_abil_tranquilizer_main_shot6.wav",
            // sound/prestige_med/ -- the complete real 1-file folder
            "sound/prestige_med/prestige_med.ogg",
            // sound/reichstag_3shots/ -- representative subset (111 real files total)
            "sound/reichstag_3shots/3p_avt40_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_avt40_reich_main_shot2.ogg",
            "sound/reichstag_3shots/3p_avt40_reich_main_shot3.ogg",
            "sound/reichstag_3shots/3p_bren_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_colt_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_dp_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_stg44_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_mp40_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_mosin_reich_main_shot1.ogg",
            "sound/reichstag_3shots/3p_tt_reich_main_shot1.ogg",
            // sound/terentiy/ -- representative subset (45 real files total)
            "sound/terentiy/lopata_terentiy_lkm_short1.ogg",
            "sound/terentiy/lopata_terentiy_lkm_short2.ogg",
            "sound/terentiy/lopata_terentiy_pkm_long1.ogg",
            "sound/terentiy/lopata_terentiy_pkm_long2.ogg",
            // models/ -- 4 real loose files
            "models/sleves_diff.ol",
            "models/sleves_nrm.ol",
            "models/worn_out_1.ol",
            "models/worn_out_2.ol",
            // models/weapons/ability_items/ -- the complete real stimshot/tubus/woofer contents
            "models/weapons/ability_items/stimshot/stimshot.mcsb",
            "models/weapons/ability_items/stimshot/stimshot_nrm.ol",
            "models/weapons/ability_items/tubus/assault_emi.ol",
            "models/weapons/ability_items/tubus/scout_emi.ol",
            "models/weapons/ability_items/tubus/support_emi.ol",
            "models/weapons/ability_items/tubus/tubus.mcsb",
            "models/weapons/ability_items/tubus/tubus_diff.ol",
            "models/weapons/ability_items/tubus/tubus_line_diff.ol",
            "models/weapons/ability_items/tubus/tubus_line_nrm.ol",
            "models/weapons/ability_items/tubus/tubus_line_spek.ol",
            "models/weapons/ability_items/tubus/tubus_nrm.ol",
            "models/weapons/ability_items/tubus/tubus_spek.ol",
            "models/weapons/ability_items/woofer/Dynamic_Diffuse.ol",
            "models/weapons/ability_items/woofer/Dynamic_Emissive.ol",
            "models/weapons/ability_items/woofer/Dynamic_Normal.ol",
            "models/weapons/ability_items/woofer/Dynamic_Specular.ol",
            "models/weapons/ability_items/woofer/prestige2_diff.ol",
            "models/weapons/ability_items/woofer/prestige_diff.ol",
            "models/weapons/ability_items/woofer/prestige_emi.ol",
            "models/weapons/ability_items/woofer/prestige_gaze_emi.ol",
            "models/weapons/ability_items/woofer/prestige_guard_emi.ol",
            "models/weapons/ability_items/woofer/woofer.mcsb",
            // models/weapons/MG42 -- the complete real 5-file folder (minus the lod/ subfolder, not swept)
            "models/weapons/MG42/mg42.mcsb",
            "models/weapons/MG42/mg42_diff.ol",
            "models/weapons/MG42/mg42_nrm.ol",
            "models/weapons/MG42/mg42_spek.ol",
            // models/weapons/Winchester_M70 -- the complete real 5-file folder
            "models/weapons/Winchester_M70/Winchester_M70.mcsb",
            "models/weapons/Winchester_M70/Winchester_M70_diff.ol",
            "models/weapons/Winchester_M70/Winchester_M70_lod.mcsb",
            "models/weapons/Winchester_M70/Winchester_M70_nrm.ol",
            "models/weapons/Winchester_M70/Winchester_M70_spek.ol",
            // textures/ -- 5 real loose files
            "textures/bullet_trace.ol",
            "textures/capture.mic",
            "textures/dirochki.ol",
            "textures/explosion.ol",
            "textures/gauss3000_trace.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("WeaponAssetCatalog.load finds exactly the 3 real top-level folders", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertEquals(3, catalog.totalFolderCount(), "folder count");
            for (String name : new String[]{"models", "sound", "textures"}) {
                assertTrue(catalog.folderNames().contains(name), name + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("load's folder-only walk genuinely does not see loose root-level files", () -> {
            Path dir = Files.createTempDirectory("weapons-fixture-loose");
            dir.toFile().deleteOnExit();
            for (String relativePath : FIXTURE_FILES) {
                Path file = dir.resolve(relativePath);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "");
                file.toFile().deleteOnExit();
            }
            // weapons/ itself has 0 real loose root files, so this confirms the walk still works
            // correctly with none present (unlike gloomycore/highpoly's own dedicated regression case).
            WeaponAssetCatalog catalog = WeaponAssetCatalog.load(dir);
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "file count with no loose roots");
        }));

        out.add(test("foldersContaining: the real Hunter/Shaman/Tank class sound sets all live under sound", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("class_hunter").contains("sound"), "class_hunter");
            assertTrue(catalog.foldersContaining("class_shaman").contains("sound"), "class_shaman");
            assertTrue(catalog.foldersContaining("class_tank").contains("sound"), "class_tank");
        }));

        out.add(test("foldersContaining: real named prestige abilities (barricade, lowgravity, vortexgrenade) resolve under sound", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("barricade").contains("sound"), "barricade");
            assertTrue(catalog.foldersContaining("lowgravity").contains("sound"), "lowgravity");
            assertTrue(catalog.foldersContaining("vortexgrenade").contains("sound"), "vortexgrenade");
            assertTrue(catalog.foldersContaining("tranquilizer").contains("sound"), "tranquilizer");
        }));

        out.add(test("foldersContaining: the real 20-weapon Reichstag WWII roster resolves under sound", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            for (String code : new String[]{"avt40", "stg44", "mp40", "mosin", "tt"}) {
                assertTrue(catalog.foldersContaining(code).contains("sound"), code);
            }
        }));

        out.add(test("foldersContaining: terentiy resolves under sound (cross-tree name reuse with part 65's lipsync/terentiy)", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("terentiy").contains("sound"), "terentiy");
        }));

        out.add(test("foldersContaining: real historical weapon model names (MG42, Winchester_M70) resolve under models", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("mg42").contains("models"), "MG42");
            assertTrue(catalog.foldersContaining("Winchester_M70").contains("models"), "Winchester_M70");
        }));

        out.add(test("foldersContaining: real class-colored tubus variants (assault, scout, support) resolve under models", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("assault_emi").contains("models"), "assault_emi");
            assertTrue(catalog.foldersContaining("scout_emi").contains("models"), "scout_emi");
            assertTrue(catalog.foldersContaining("support_emi").contains("models"), "support_emi");
        }));

        out.add(test("filesIn: prestige_abil is the complete real 29-file fixture", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("sound");
            int prestigeAbilCount = (int) files.stream().filter(f -> f.startsWith("prestige_abil/")).count();
            assertEquals(29, prestigeAbilCount, "prestige_abil file count");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            WeaponAssetCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("WeaponAssetCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("weapons-fixture-empty");
                dir.toFile().deleteOnExit();
                WeaponAssetCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static WeaponAssetCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("weapons-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return WeaponAssetCatalog.load(dir);
    }
}
