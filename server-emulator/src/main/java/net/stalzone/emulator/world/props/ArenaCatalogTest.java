package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ArenaCatalog}, using a fixture built from real {@code arena.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 153, live-verified on device) --
 * real map names, and the real "scout" id/name inconsistency against {@link IncidentCatalog}.
 */
public final class ArenaCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "arena.id.arena_angar.name=Hangar",
            "arena.id.arena_peklo.name=Inferno",
            "arena.equip_preset.id.sniper.name=Sniper",
            "arena.equip_preset.id.marksman.name=Shooter",
            "arena.equip_preset.id.assault.name=Assault Trooper",
            "arena.equip_preset.id.scout.name=Raider"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("arena-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ArenaCatalog loadFixture() throws IOException {
        return ArenaCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real Arena-mode map names resolve", () -> {
            ArenaCatalog catalog = loadFixture();
            assertEquals("Hangar", catalog.field("id.arena_angar", "name"), "arena_angar name");
            assertEquals("Inferno", catalog.field("id.arena_peklo", "name"), "arena_peklo name");
        }));

        out.add(test("real exact id+name matches against IncidentCatalog's sample roster resolve", () -> {
            ArenaCatalog catalog = loadFixture();
            assertEquals("Sniper", catalog.field("equip_preset.id.sniper", "name"), "sniper preset name");
            assertEquals("Shooter", catalog.field("equip_preset.id.marksman", "name"), "marksman preset name");
        }));

        out.add(test("the real 'scout' id resolves to 'Raider' here, a genuine display-name inconsistency versus IncidentCatalog's scout='Scout', and ids() is sorted with null-safe lookups", () -> {
            ArenaCatalog catalog = loadFixture();
            assertEquals("Raider", catalog.field("equip_preset.id.scout", "name"), "scout preset name is really 'Raider', not 'Scout', in this namespace");
            List<String> ids = catalog.ids();
            assertEquals(6, ids.size(), "6 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
        }));

        return out;
    }
}
