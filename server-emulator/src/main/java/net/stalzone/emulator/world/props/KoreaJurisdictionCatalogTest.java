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
 * Regression tests for {@link KoreaJurisdictionCatalog}, using a fixture directory tree with real
 * filenames drawn directly from the real {@code modassets/assets/_jurisdiction/korea/} folder
 * (part 60), live-verified on device: the 5 real top-level folders, the {@code stalkerguide/}
 * partial cutscene-override subset (the disclosed 11-pair/7-single {@code _720}/{@code _1080}
 * asymmetry), the anomalous unmatched {@code stream/video/prestige.ogg} file, the
 * {@code quest_cutted_ear} trophy-texture pair, the dated {@code stalboy} variants, and the
 * cross-subsystem duplicate {@code cowgirl.ol} sticker. Only real game-content names are used, per
 * this project's privacy rule -- never a real player username. (Note: {@code foldersContaining}
 * only searches file NAMES, not file contents, so the real {@code ko.lang}/{@code en.lang} text
 * content -- the "Holepunch" item description -- is not exercised here; that was verified directly
 * via {@code cat} against the live device file, not via this class's API.)
 */
public final class KoreaJurisdictionCatalogTest {

    private static final String[] FIXTURE_FILES = {
            "customitems/models/blocks/decals/newDecals/propaganda_posters/regular/horizontal/friendship.ol",
            "customitems/models/blocks/decals/plakat/dec_mirnas.ol",
            "customitems/models/items/quest/otkritka_diff.ol",
            "customitems/models/items/quest/stalboy/stalboy_diff.ol",
            "customitems/models/items/quest/stalboy/stalboy_hw23_diff.ol",
            "customitems/models/items/quest/stalboy/stalboy_summer24_diff.ol",
            "customitems/textures/items/quest_cutted_ear.ol",
            "customitems/textures/items/quest_cutted_ear_3.ol",
            "stalcraft/textures/items/customisation/stickers/na/cowgirl.ol",
            "stalker/lang/en.lang",
            "stalker/lang/ko.lang",
            "stalker/textures/gui/customisation/stickers/na/cowgirl.ol",
            "stalkerguide/sound/stream/cinemat.ogg",
            "stalkerguide/sound/stream/grenade_throw.ogg",
            "stalkerguide/sound/stream/heli_crash.ogg",
            "stalkerguide/sound/stream/morg_death.ogg",
            "stalkerguide/sound/stream/morg_fires_roof.ogg",
            "stalkerguide/sound/stream/morg_shaman_fight.ogg",
            "stalkerguide/sound/stream/morg_shaman_meet.ogg",
            "stalkerguide/sound/stream/shaman_death.ogg",
            "stalkerguide/sound/stream/shaman_flies.ogg",
            "stalkerguide/sound/stream/vlasov_death.ogg",
            "stalkerguide/sound/stream/vlasov_escape.ogg",
            "stalkerguide/stream/video/prestige.ogg",
            "stalkerguide/video/cinemat_1080.mp4",
            "stalkerguide/video/cinemat_720.mp4",
            "stalkerguide/video/grenade_throw_1080.mp4",
            "stalkerguide/video/heli_crash_1080.mp4",
            "stalkerguide/video/heli_crash_720.mp4",
            "stalkerguide/video/lipa_1_1080.mp4",
            "stalkerguide/video/lipa_1_720.mp4",
            "stalkerguide/video/lipa_2_1080.mp4",
            "stalkerguide/video/lipa_2_720.mp4",
            "stalkerguide/video/lipa_3_1080.mp4",
            "stalkerguide/video/lipa_3_720.mp4",
            "stalkerguide/video/morg_death_1080.mp4",
            "stalkerguide/video/morg_fires_roof_1080.mp4",
            "stalkerguide/video/morg_shaman_fight_1080.mp4",
            "stalkerguide/video/morg_shaman_meet_1080.mp4",
            "stalkerguide/video/phits_1080.mp4",
            "stalkerguide/video/phits_720.mp4",
            "stalkerguide/video/ppayalnik_1080.mp4",
            "stalkerguide/video/ppayalnik_720.mp4",
            "stalkerguide/video/pshoker_1080.mp4",
            "stalkerguide/video/pshoker_720.mp4",
            "stalkerguide/video/pshot_1080.mp4",
            "stalkerguide/video/pshot_720.mp4",
            "stalkerguide/video/shaman_death_1080.mp4",
            "stalkerguide/video/shaman_flies_1080.mp4",
            "stalkerguide/video/vlasov_death_1080.mp4",
            "stalkerguide/video/vlasov_death_720.mp4",
            "stalkerguide/video/vlasov_escape_1080.mp4",
            "stalkerguide/video/vlasov_escape_720.mp4",
            "weapons/models/bolt/lighter/art/art_diff.ol",
            "weapons/models/bolt/lighter/art/art_spek.ol",
            "weapons/textures/items/att/accessories/stalcraft_store/dakimakura.ol",
            "weapons/textures/items/att/accessories/stalcraft_store/dakimakura_naked.ol",
            "weapons/textures/items/bolts/lighter.ol",
            "weapons/textures/items/weapons/mr153hell.ol",
            "weapons/textures/items/weapons/styles/m1014b_halloween23.ol",
    };

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("KoreaJurisdictionCatalog.load finds exactly the 5 real top-level folders", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            assertEquals(5, catalog.totalFolderCount(), "folder count");
            assertTrue(catalog.folderNames().contains("customitems"), "customitems present");
            assertTrue(catalog.folderNames().contains("stalcraft"), "stalcraft present");
            assertTrue(catalog.folderNames().contains("stalker"), "stalker present");
            assertTrue(catalog.folderNames().contains("stalkerguide"), "stalkerguide present");
            assertTrue(catalog.folderNames().contains("weapons"), "weapons present");
        }));

        out.add(test("totalFileCount: accounts for every fixture file exactly once", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            assertEquals(FIXTURE_FILES.length, catalog.totalFileCount(), "total file count");
        }));

        out.add(test("foldersContaining: a 720/1080 paired cutscene resolves to stalkerguide only", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> lipa = catalog.foldersContaining("lipa_1");
            assertEquals(1, lipa.size(), "lipa_1 folder count");
            assertTrue(lipa.contains("stalkerguide"), "lipa_1 pair lives under stalkerguide");
        }));

        out.add(test("foldersContaining: a 1080-only single (dropped 720 variant) still resolves correctly", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> morgFiresRoof = catalog.foldersContaining("morg_fires_roof");
            assertEquals(1, morgFiresRoof.size(), "morg_fires_roof folder count");
            List<String> files = catalog.filesIn("stalkerguide");
            assertTrue(files.contains("video/morg_fires_roof_1080.mp4"), "1080 single present");
            assertTrue(!files.contains("video/morg_fires_roof_720.mp4"), "720 variant genuinely absent");
        }));

        out.add(test("foldersContaining: prestige is the one real anomaly, unmatched by any of the 18 cutscene names", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> prestige = catalog.foldersContaining("prestige");
            assertEquals(1, prestige.size(), "prestige folder count");
            assertTrue(prestige.contains("stalkerguide"), "prestige.ogg lives under stalkerguide");
        }));

        out.add(test("foldersContaining: quest_cutted_ear trophy texture pair lives under customitems", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> cuttedEar = catalog.foldersContaining("cutted_ear");
            assertEquals(1, cuttedEar.size(), "cutted_ear folder count");
            assertTrue(cuttedEar.contains("customitems"), "quest_cutted_ear lives under customitems");
        }));

        out.add(test("foldersContaining: stalboy dated variants both resolve under customitems", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> stalboy = catalog.foldersContaining("stalboy");
            assertEquals(1, stalboy.size(), "stalboy folder count");
            List<String> files = catalog.filesIn("customitems");
            assertTrue(files.contains("models/items/quest/stalboy/stalboy_hw23_diff.ol"), "hw23 dated variant present");
            assertTrue(files.contains("models/items/quest/stalboy/stalboy_summer24_diff.ol"), "summer24 dated variant present");
        }));

        out.add(test("foldersContaining: cowgirl sticker is genuinely duplicated across two different subsystems", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> cowgirl = catalog.foldersContaining("cowgirl");
            assertEquals(2, cowgirl.size(), "cowgirl folder count");
            assertTrue(cowgirl.contains("stalcraft"), "cowgirl present under stalcraft");
            assertTrue(cowgirl.contains("stalker"), "cowgirl present under stalker");
        }));

        out.add(test("foldersContaining: dakimakura cosmetic accessory pair lives under weapons", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            List<String> dakimakura = catalog.foldersContaining("dakimakura");
            assertEquals(1, dakimakura.size(), "dakimakura folder count");
            List<String> files = catalog.filesIn("weapons");
            assertTrue(files.contains("textures/items/att/accessories/stalcraft_store/dakimakura.ol"), "base variant present");
            assertTrue(files.contains("textures/items/att/accessories/stalcraft_store/dakimakura_naked.ol"), "naked variant present");
        }));

        out.add(test("foldersContaining: returns empty (not null) for a substring nothing matches", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            assertEquals(0, catalog.foldersContaining("nonexistent_substring_xyz").size(), "no-match count");
        }));

        out.add(test("filesIn: returns empty (not null) for an unknown folder", () -> {
            KoreaJurisdictionCatalog catalog = loadFixture();
            assertEquals(0, catalog.filesIn("nonexistent_folder").size(), "unknown folder file count");
        }));

        out.add(test("KoreaJurisdictionCatalog.load rejects an empty directory", () -> {
            assertThrows(IOException.class, () -> {
                Path dir = Files.createTempDirectory("korea-jurisdiction-fixture-empty");
                dir.toFile().deleteOnExit();
                KoreaJurisdictionCatalog.load(dir);
            }, "empty directory");
        }));

        return out;
    }

    private static KoreaJurisdictionCatalog loadFixture() throws Exception {
        Path dir = Files.createTempDirectory("korea-jurisdiction-fixture");
        dir.toFile().deleteOnExit();
        for (String relativePath : FIXTURE_FILES) {
            Path file = dir.resolve(relativePath);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "");
            file.toFile().deleteOnExit();
        }
        return KoreaJurisdictionCatalog.load(dir);
    }
}
