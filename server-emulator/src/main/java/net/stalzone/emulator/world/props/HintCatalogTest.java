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
 * Regression tests for {@link HintCatalog}, using a fixture built from real {@code hint.*} lines
 * drawn directly from {@code stalker/lang/en.lang} (part 147, live-verified on device) -- a real
 * keybind-template hint, the real Zheltuha signal hint, and the real duplicate "Tip" header
 * group.
 */
public final class HintCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "hint.pda.header=PDA",
            "hint.pda.text=Press [key:key.pda] to open your PDA and go to your Tasks tab.",
            "hint.signals_Zheltuha.header=Fight without firearms!",
            "hint.signals_Zheltuha.text=Primary and secondary weapons are blocked.",
            "hint.destroy_wood_board.header=Tip",
            "hint.search_stash.header=Tip"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("hint-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static HintCatalog loadFixture() throws IOException {
        return HintCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real keybind-template hint resolves", () -> {
            HintCatalog catalog = loadFixture();
            assertEquals("PDA", catalog.field("pda", "header"), "pda header");
            assertTrue(catalog.field("pda", "text").contains("[key:key.pda]"), "pda text contains real keybind template token");
        }));

        out.add(test("the real Zheltuha signal-location hint resolves", () -> {
            HintCatalog catalog = loadFixture();
            assertEquals("Fight without firearms!", catalog.field("signals_Zheltuha", "header"), "signals_Zheltuha header");
            assertEquals("Primary and secondary weapons are blocked.", catalog.field("signals_Zheltuha", "text"), "signals_Zheltuha text");
        }));

        out.add(test("the real duplicate 'Tip' header group resolves as distinct ids sharing literal text, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            HintCatalog catalog = loadFixture();
            assertEquals("Tip", catalog.field("destroy_wood_board", "header"), "destroy_wood_board header");
            assertEquals("Tip", catalog.field("search_stash", "header"), "search_stash header");
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "header"), "unknown id");
            assertNull(catalog.field("pda", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}
