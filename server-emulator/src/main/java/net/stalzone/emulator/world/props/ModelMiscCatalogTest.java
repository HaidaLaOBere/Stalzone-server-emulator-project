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
 * Regression tests for {@link ModelMiscCatalog}, using a fixture directory tree with the complete
 * real filenames drawn directly from the real {@code modassets/assets/stalker/models/} folder's 9
 * small leaf trees swept together in part 95 (204 files total): {@code food/} (162), {@code
 * shamanfight/} (10), {@code dropped_tooltip/} (8), {@code hunter/} (6), {@code halloween/} (6),
 * {@code undead/} (4), {@code blizzard/} (4), {@code gift/} (3), {@code cloth/} (1), live-verified
 * on device via a single {@code find}-per-folder sweep. Only real game-content/asset names are
 * used, per this project's privacy rule -- never a real player username. Two real on-disk filename
 * quirks are preserved verbatim in the fixture (not normalized away): {@code
 * food/meat_dog_сooked.ol} uses a Cyrillic {@code с} in place of the Latin {@code c}, and {@code
 * hunter/hook/hook_diff — копия.ol}/{@code hook_spek — копия.ol} carry a literal Cyrillic
 * "— копия" ("— copy") suffix -- both genuine real filenames, not transcription errors.
 */
public final class ModelMiscCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "blizzard/frozen.mcsb",
            "blizzard/frozen_diff.ol",
            "blizzard/frozen_nrm.ol",
            "blizzard/frozen_spek.ol",
            "cloth/cloth_hp.mcvd",
            "dropped_tooltip/stick.mcsb",
            "dropped_tooltip/stick.ol",
            "dropped_tooltip/stick_LEGEND.ol",
            "dropped_tooltip/stick_MASTER.ol",
            "dropped_tooltip/stick_NEWBIE.ol",
            "dropped_tooltip/stick_PICKLOCK.ol",
            "dropped_tooltip/stick_STALKER.ol",
            "dropped_tooltip/stick_VETERAN.ol",
            "food/aceton.ol",
            "food/banka_diff.ol",
            "food/banka_nrm.ol",
            "food/banka_spek.ol",
            "food/blizzard/grog_diff.ol",
            "food/blizzard/grog_nrm.ol",
            "food/blizzard/grog_spek.ol",
            "food/blizzard/vodka_brus_diff.ol",
            "food/blizzard/vodka_brus_nrm.ol",
            "food/blizzard/vodka_brus_spek.ol",
            "food/blizzard/vodka_new.mcsb",
            "food/box_croup.mcsb",
            "food/buter_ikra.mcsb",
            "food/buterb.mcsb",
            "food/buterw.mcsb",
            "food/champ_breakfast.mcsb",
            "food/champ_breakfast_diff.ol",
            "food/champ_breakfast_nrm.ol",
            "food/champ_breakfast_spek.ol",
            "food/chesnok.mcsb",
            "food/chesnok.ol",
            "food/chesnok_nrm.ol",
            "food/cucuruza.ol",
            "food/customfood_diff.ol",
            "food/difuz.ol",
            "food/erp.mcsb",
            "food/erp2_diff.ol",
            "food/erp_diff.ol",
            "food/erp_nrm.ol",
            "food/erp_spek.ol",
            "food/fasol.mcsb",
            "food/fasol_lod1.mcsb",
            "food/fasoll.ol",
            "food/food_diff.ol",
            "food/food_makflot_Diffuse.ol",
            "food/food_makflot_Normal.ol",
            "food/food_makflot_Specular.ol",
            "food/food_nrm.ol",
            "food/goroh.ol",
            "food/grecha.ol",
            "food/hleb_afro.mcsb",
            "food/hleb_bely.mcsb",
            "food/hleb_diff.ol",
            "food/hleb_nrm.ol",
            "food/horog_diff.ol",
            "food/ikra.ol",
            "food/kakao.mcsb",
            "food/kastr_salat_oliv.mcsb",
            "food/kastr_sup_ches.mcsb",
            "food/kastr_sup_go.mcsb",
            "food/kastr_sup_kop.mcsb",
            "food/kastr_sup_som.mcsb",
            "food/kolbasa.mcsb",
            "food/kolbasa.ol",
            "food/kolbasa2.ol",
            "food/kolbasa_nrm.ol",
            "food/kolbasa_spek.ol",
            "food/konserva2_diff.ol",
            "food/konserva3_diff.ol",
            "food/konserva_diff.ol",
            "food/konserva_ikra.mcsb",
            "food/konserva_nrm.ol",
            "food/konserva_spek.ol",
            "food/kotel_krish_kasha_fa.mcsb",
            "food/kotel_krish_kasha_go.mcsb",
            "food/kotel_krish_kasha_ku.mcsb",
            "food/kotel_krish_kasha_ps.mcsb",
            "food/kotel_krish_krabi.mcsb",
            "food/kotel_krish_makaroni_flot.mcsb",
            "food/kotel_krish_olivie.mcsb",
            "food/kotel_krish_zalivnoe.mcsb",
            "food/kotel_poh_fatom.mcsb",
            "food/kotel_poh_go.mcsb",
            "food/kotel_poh_oliv.mcsb",
            "food/kotel_poh_ragu.mcsb",
            "food/lemon.mcsb",
            "food/lemon.ol",
            "food/lemon_nrm.ol",
            "food/macaroni.ol",
            "food/mandarin.mcsb",
            "food/mandarin.ol",
            "food/mandarin_nrm.ol",
            "food/meat_dog.ol",
            "food/meat_dog_nrm.ol",
            "food/meat_dog_сooked.ol",
            "food/meat_kaban.ol",
            "food/meat_kaban_cooked.ol",
            "food/meat_kaban_nrm.ol",
            "food/meat_leg.mcsb",
            "food/meat_plot.ol",
            "food/meat_plot_cooked.ol",
            "food/meat_plot_nrm.ol",
            "food/meat_raw.mcsb",
            "food/meat_raw_dog.mcsb",
            "food/newyear2022/bottle/1/bottle2_diff.ol",
            "food/newyear2022/bottle/1/bottle2_spek.ol",
            "food/newyear2022/bottle/1/bottle_nrm.ol",
            "food/newyear2022/bottle/2/bottle_2_diff.ol",
            "food/newyear2022/bottle/2/bottle_2_spek.ol",
            "food/newyear2022/bottle/3/bottle3_diff.ol",
            "food/newyear2022/bottle/3/bottle3_spek.ol",
            "food/newyear2022/bottle/4/bottle4_diff.ol",
            "food/newyear2022/bottle/4/bottle4_nrm.ol",
            "food/newyear2022/bottle/4/bottle4_spek.ol",
            "food/newyear2022/bottle/alcobull/bottle4_diff.ol",
            "food/newyear2022/bottle/alcobull/bottle4_emi.ol",
            "food/newyear2022/bottle/bottle_1.mcsb",
            "food/newyear2022/bottle/bottle_2.mcsb",
            "food/newyear2022/bottle/bottle_hideout.mcsb",
            "food/newyear2022/bottle/geyser/bottle4_diff.ol",
            "food/newyear2022/bottle/geyser/bottle4_emi.ol",
            "food/newyear2022/icecream1/icecream1.mcsb",
            "food/newyear2022/icecream1/icecream1_diff.ol",
            "food/newyear2022/icecream1/icecream1_nrm.ol",
            "food/newyear2022/icecream1/icecream1_spek.ol",
            "food/newyear2022/icecream2/icecream2.mcsb",
            "food/newyear2022/icecream2/icecream2_diff.ol",
            "food/newyear2022/icecream2/icecream2_nrm.ol",
            "food/newyear2022/icecream2/icecream2_spek.ol",
            "food/newyear2022/icecream3/icecream3.mcsb",
            "food/newyear2022/icecream3/icecream3_diff.ol",
            "food/newyear2022/icecream3/icecream3_nrm.ol",
            "food/newyear2022/icecream3/icecream3_spek.ol",
            "food/newyear2022/icecream4/icecream4.mcsb",
            "food/newyear2022/icecream4/icecream4_diff.ol",
            "food/newyear2022/icecream4/icecream4_nrm.mic",
            "food/newyear2022/icecream4/icecream4_nrm.ol",
            "food/newyear2022/icecream4/icecream4_spek.ol",
            "food/newyear2022/konserva/kons_bobi.mcsb",
            "food/newyear2022/konserva/kons_fasol.mcsb",
            "food/newyear2022/konserva/kons_goroh.mcsb",
            "food/newyear2022/konserva/kons_kukuruza.mcsb",
            "food/newyear2022/konserva/ny_konserva_diff.ol",
            "food/newyear2022/konserva/ny_konserva_nrm.ol",
            "food/newyear2022/konserva/ny_konserva_spek.ol",
            "food/newyear2022/mandarine/mandarine_diff.ol",
            "food/newyear2022/mandarine/mandarine_full.mcsb",
            "food/newyear2022/mandarine/mandarine_gnil.mcsb",
            "food/newyear2022/mandarine/mandarine_gnil_diff.ol",
            "food/newyear2022/mandarine/mandarine_gnil_nrm.ol",
            "food/newyear2022/mandarine/mandarine_gnil_spek.ol",
            "food/newyear2022/mandarine/mandarine_mandarine_peel.mcsb",
            "food/newyear2022/mandarine/mandarine_mandarine_peel_half.mcsb",
            "food/newyear2022/mandarine/mandarine_nrm.ol",
            "food/newyear2022/mandarine/mandarine_spek.ol",
            "food/noname1.mcsb",
            "food/noname2.mcsb",
            "food/noname2_lod1.mcsb",
            "food/posuda_diff.ol",
            "food/posuda_nrm.ol",
            "food/posuda_spek.ol",
            "food/psheno.ol",
            "food/radost.mcsb",
            "food/radost_lod1.mcsb",
            "food/sguha.mcsb",
            "food/soldat_diff.ol",
            "food/tunec.mcsb",
            "food/tunec_lod1.mcsb",
            "food/voda.mcsb",
            "food/voda.ol",
            "food/voda_n.mcsb",
            "food/wine_glass.mcsb",
            "gift/gift_box.mcsb",
            "gift/gift_diffuse.ol",
            "gift/gift_diffuse2.ol",
            "halloween/big.mcsb",
            "halloween/goner_diff.ol",
            "halloween/goner_emi.ol",
            "halloween/goner_nrm.ol",
            "halloween/medium.mcsb",
            "halloween/small.mcsb",
            "hunter/hook/hook.mcsb",
            "hunter/hook/hook_diff — копия.ol",
            "hunter/hook/hook_diff.ol",
            "hunter/hook/hook_nrm.ol",
            "hunter/hook/hook_spek — копия.ol",
            "hunter/hook/hook_spek.ol",
            "shamanfight/column/column.mcsb",
            "shamanfight/flames/flames.mcsb",
            "shamanfight/flames/flames.ol",
            "shamanfight/phantom_proj/phantomproj.mcsb",
            "shamanfight/phantom_proj/phantomproj.ol",
            "shamanfight/phantom_zone/phantom_zone.mcsb",
            "shamanfight/shield/perlin.mic",
            "shamanfight/shield/shield.mcsb",
            "shamanfight/shield/shield.mcvd",
            "shamanfight/shield/shield.ol",
            "undead/hook/hook.mcsb",
            "undead/hook/hook_diff.ol",
            "undead/hook/hook_nrm.ol",
            "undead/hook/hook_spek.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ModelMiscCatalog.load finds exactly the 9 known small-tree folders", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertEquals(9, catalog.totalFolderCount(), "folder count");
            for (String known : ModelMiscCatalog.KNOWN_FOLDERS) {
                assertTrue(catalog.folderNames().contains(known), known + " present");
            }
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once (204 total)", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
            assertEquals(204, catalog.totalFileCount(), "matches the real 204-file sweep");
        }));

        out.add(test("filesIn: food is the complete real 162-file folder", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("food");
            assertEquals(162, files.size(), "food file count");
            assertTrue(files.contains("meat_dog_сooked.ol"), "meat_dog_сooked.ol present (Cyrillic с)");
        }));

        out.add(test("filesIn: shamanfight is the complete real 10-file folder", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("shamanfight");
            assertEquals(10, files.size(), "shamanfight file count");
            assertTrue(files.contains("column/column.mcsb"), "column.mcsb present");
            assertTrue(files.contains("shield/shield.mcsb"), "shield.mcsb present");
        }));

        out.add(test("filesIn: dropped_tooltip is the complete real 8-file folder", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("dropped_tooltip");
            assertEquals(8, files.size(), "dropped_tooltip file count");
            assertTrue(files.contains("stick_LEGEND.ol"), "stick_LEGEND.ol present");
            assertTrue(files.contains("stick_PICKLOCK.ol"), "stick_PICKLOCK.ol present");
        }));

        out.add(test("filesIn: hunter is the complete real 6-file folder, incl. Cyrillic copy duplicates", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("hunter");
            assertEquals(6, files.size(), "hunter file count");
            assertTrue(files.contains("hook/hook_diff — копия.ol"), "hook_diff — копия.ol present");
        }));

        out.add(test("filesIn: undead is the complete real 4-file folder (mirrors hunter's hook set)", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> files = catalog.filesIn("undead");
            assertEquals(4, files.size(), "undead file count");
            assertTrue(files.contains("hook/hook.mcsb"), "hook.mcsb present");
        }));

        out.add(test("foldersContaining: hook resolves under both hunter and undead", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> hookFolders = catalog.foldersContaining("hook");
            assertTrue(hookFolders.contains("hunter"), "hunter has hook");
            assertTrue(hookFolders.contains("undead"), "undead has hook");
            assertEquals(2, hookFolders.size(), "exactly 2 folders carry a hook model");
        }));

        out.add(test("foldersContaining: goner resolves under halloween only", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> goner = catalog.foldersContaining("goner");
            assertEquals(1, goner.size(), "goner folder count");
            assertTrue(goner.contains("halloween"), "halloween has goner");
        }));

        out.add(test("foldersContaining: phantom_proj and phantom_zone resolve under shamanfight", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("phantom_proj").contains("shamanfight"), "phantom_proj");
            assertTrue(catalog.foldersContaining("phantom_zone").contains("shamanfight"), "phantom_zone");
        }));

        out.add(test("foldersContaining: newyear2022 (New Year 2022 seasonal food props) resolves under food", () -> {
            ModelMiscCatalog catalog = loadFixture();
            List<String> ny = catalog.foldersContaining("newyear2022");
            assertEquals(1, ny.size(), "newyear2022 folder count");
            assertTrue(ny.contains("food"), "food has newyear2022");
        }));

        out.add(test("foldersContaining: blizzard-tied props (frozen, vodka_brus) resolve correctly", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertTrue(catalog.foldersContaining("frozen").contains("blizzard"), "frozen under blizzard");
            assertTrue(catalog.foldersContaining("vodka_brus").contains("food"), "vodka_brus under food");
        }));

        out.add(test("load ignores a real models/ folder that is not on the known-small-tree whitelist", () -> {
            ModelMiscCatalog catalog = loadFixtureWithUnrelatedFolder();
            assertEquals(9, catalog.totalFolderCount(), "folder count unaffected by unrelated folder");
            assertTrue(!catalog.folderNames().contains("armor"), "armor not indexed (covered by ArmorModelCatalog)");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            ModelMiscCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("ModelMiscCatalog.load rejects a root with none of the known folders", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("model-misc-fixture-empty");
                dir.toFile().deleteOnExit();
                ModelMiscCatalog.load(dir);
            }, "root with no known folders");
        }));

        return out;
    }

    private static ModelMiscCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("model-misc-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return ModelMiscCatalog.load(dir);
    }

    private static ModelMiscCatalog loadFixtureWithUnrelatedFolder() throws Exception {
        Path dir = Files.createTempDirectory("model-misc-fixture-unrelated");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        Path unrelated = dir.resolve("armor/some_base/some_base.mcsb");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "");
        unrelated.toFile().deleteOnExit();
        return ModelMiscCatalog.load(dir);
    }
}
