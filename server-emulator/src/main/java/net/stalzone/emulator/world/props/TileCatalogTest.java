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
 * Regression tests for {@link TileCatalog}, using a fixture built from real {@code tile.*} lines
 * drawn directly from {@code stalker/lang/en.lang} (part 130, live-verified on device) -- real
 * level-design material/flooring tile display names, including the disclosed real inconsistent
 * capitalization between entries.
 */
public final class TileCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "tile.3500.name=Stones (Floor)",
            "tile.3501.name=dull yellow tile",
            "tile.3503.name=wall"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("tile-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static TileCatalog loadFixture() throws IOException {
        return TileCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real tile name resolves, including inconsistent real capitalization", () -> {
            TileCatalog catalog = loadFixture();
            assertEquals("Stones (Floor)", catalog.name("3500"), "3500 name");
            assertEquals("dull yellow tile", catalog.name("3501"), "3501 name, real lowercase");
        }));

        out.add(test("a second real tile name resolves independently", () -> {
            TileCatalog catalog = loadFixture();
            assertEquals("wall", catalog.name("3503"), "3503 name");
        }));

        out.add(test("ids() is sorted and unknown ids resolve to null rather than throwing", () -> {
            TileCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(3, ids.size(), "3 real tile ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(1)) < 0, "sorted ascending");
            assertTrue(ids.get(1).compareTo(ids.get(2)) < 0, "sorted ascending");
            assertNull(catalog.name("9999999"), "unknown tile id");
        }));

        return out;
    }
}
