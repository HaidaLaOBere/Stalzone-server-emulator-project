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
 * Regression tests for {@link OrdersCatalog}, using a fixture built from real {@code orders.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 137, live-verified on device) --
 * a real per-faction objective, a real DMZ-tied objective, and the real {@code map_marker.title}
 * double-nesting quirk.
 */
public final class OrdersCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "orders.destroy_ammunition_covenant_1.name=Destroy Enemy Supplies",
            "orders.dmz_kill_player.name=Kill",
            "orders.dmz_kill_player.target_waypoint.name=Eliminate contestant",
            "orders.map_marker.title=A Mission from the Command",
            "orders.map_marker.title.description=Follow the command's instructions",
            "orders.objective.visit_positions.status_text_main=Check positions: {completed}/{required}"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("orders-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OrdersCatalog loadFixture() throws IOException {
        return OrdersCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real per-faction objective and a real DMZ-tied objective resolve", () -> {
            OrdersCatalog catalog = loadFixture();
            assertEquals("Destroy Enemy Supplies", catalog.field("destroy_ammunition_covenant_1", "name"), "covenant destroy_ammunition name");
            assertEquals("Kill", catalog.field("dmz_kill_player", "name"), "dmz_kill_player name");
            assertEquals("Eliminate contestant", catalog.field("dmz_kill_player.target_waypoint", "name"), "dmz_kill_player target_waypoint name");
        }));

        out.add(test("the real map_marker.title double-nesting quirk resolves both ids independently", () -> {
            OrdersCatalog catalog = loadFixture();
            assertEquals("A Mission from the Command", catalog.field("map_marker", "title"), "map_marker title leaf value");
            assertEquals("Follow the command's instructions", catalog.field("map_marker.title", "description"), "map_marker.title description, a separate id");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            OrdersCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(6, ids.size(), "6 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_order", "name"), "unknown order id");
            assertNull(catalog.field("map_marker", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
