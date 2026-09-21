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
 * Regression tests for {@link CheckpointsCatalog}, using a fixture built from real {@code
 * checkpoints.*} lines drawn directly from {@code stalker/lang/en.lang} (part 127, live-verified
 * on device) -- a real dungeon's non-contiguous control-point numbering, the literal real
 * "Unknown" value, and the 6 real flat {@code _finish}-suffixed exceptions.
 */
public final class CheckpointsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "checkpoints.dungeon_stels.entry=Fallen Helicopter",
            "checkpoints.dungeon_stels.control_point_1=Descent to Cave",
            "checkpoints.dungeon_stels.control_point_8=Radiolocation Tower",
            "checkpoints.dungeon_stels.control_point_12=Radiolocation Tower",
            "checkpoints.dungeon_stels.control_warp=Unknown",
            "checkpoints.blizzard_boss.control_point_1=Unknown",
            "checkpoints.dungeon_enlightened=Unknown",
            "checkpoints.dungeon_zemlcap1_finish=Dugout",
            "checkpoints.dungeon_svetliy_stalkers_finish=Light's Hideout",
            "checkpoints.dungeon_svetliy_bandits_finish=Light's Hideout"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("checkpoints-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static CheckpointsCatalog loadFixture() throws IOException {
        return CheckpointsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real dungeon's entry and numbered control points resolve, including non-contiguous numbering", () -> {
            CheckpointsCatalog catalog = loadFixture();
            assertEquals("Fallen Helicopter", catalog.pointName("dungeon_stels", "entry"), "stels entry");
            assertEquals("Descent to Cave", catalog.pointName("dungeon_stels", "control_point_1"), "stels cp1");
            assertEquals("Radiolocation Tower", catalog.pointName("dungeon_stels", "control_point_8"), "stels cp8");
            assertEquals("Radiolocation Tower", catalog.pointName("dungeon_stels", "control_point_12"), "stels cp12, real gap 9-11");
        }));

        out.add(test("the literal real value \"Unknown\" resolves as real disclosed-as-is content", () -> {
            CheckpointsCatalog catalog = loadFixture();
            assertEquals("Unknown", catalog.pointName("dungeon_stels", "control_warp"), "stels control_warp is genuinely Unknown");
            assertEquals("Unknown", catalog.pointName("blizzard_boss", "control_point_1"), "blizzard_boss cp1 is genuinely Unknown");
        }));

        out.add(test("pointIds()/dungeonIds() are sorted and reflect real fixture data", () -> {
            CheckpointsCatalog catalog = loadFixture();
            List<String> pointIds = catalog.pointIds("dungeon_stels");
            assertEquals(5, pointIds.size(), "5 real point ids for dungeon_stels in fixture");
            for (int i = 1; i < pointIds.size(); i++) {
                assertTrue(pointIds.get(i - 1).compareTo(pointIds.get(i)) < 0, "pointIds sorted ascending");
            }
            List<String> dungeonIds = catalog.dungeonIds();
            assertEquals(2, dungeonIds.size(), "2 real dungeon ids with slots in fixture");
        }));

        out.add(test("the 3 real flat _finish-suffixed exceptions resolve via flatName, not pointName", () -> {
            CheckpointsCatalog catalog = loadFixture();
            assertEquals("Unknown", catalog.flatName("dungeon_enlightened"), "dungeon_enlightened flat");
            assertEquals("Dugout", catalog.flatName("dungeon_zemlcap1_finish"), "zemlcap1 finish");
            assertEquals("Light's Hideout", catalog.flatName("dungeon_svetliy_stalkers_finish"), "svetliy stalkers finish");
            assertEquals("Light's Hideout", catalog.flatName("dungeon_svetliy_bandits_finish"),
                    "svetliy bandits finish -- two real variants share one label");
            List<String> flatIds = catalog.flatIds();
            assertEquals(4, flatIds.size(), "4 real flat ids in fixture");
        }));

        out.add(test("unknown dungeon/point/flat ids resolve to null/empty rather than throwing", () -> {
            CheckpointsCatalog catalog = loadFixture();
            assertNull(catalog.pointName("not_a_real_dungeon", "entry"), "unknown dungeon");
            assertNull(catalog.pointName("dungeon_stels", "not_a_real_point"), "unknown point in a real dungeon");
            assertNull(catalog.flatName("not_a_real_flat_id"), "unknown flat id");
            assertEquals(0, catalog.pointIds("not_a_real_dungeon").size(), "unknown dungeon point ids");
        }));

        return out;
    }
}
