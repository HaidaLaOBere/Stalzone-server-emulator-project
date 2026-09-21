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
 * Regression tests for {@link GuiLangCatalog}, using a fixture built from real {@code gui.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 122, live-verified on device) --
 * covering a real season banner, the real empty {@code spring25.banner.desc} value, the
 * {@code encaged} tutorial-card text, and confirming {@code gui.operation.how_to_play.*} is
 * excluded since part 113's {@link OperationsCatalog} already owns it.
 */
public final class GuiLangCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "gui.summer25.banner.endTimeLabel=Until the end of the season:",
            "gui.summer25.banner.headerLabel=NEW SEASON",
            "gui.summer25.banner.title=MURMUR OF THE ZONE",
            "gui.spring25.banner.title=BACK TO THE INSIDE OUT",
            "gui.spring25.banner.desc=",
            "gui.encaged.survivors.cards.select_class.title=CHOOSE A CLASS",
            "gui.encaged.survivors.cards.select_class.desc=Select the class that suits you best — each has its advantages",
            "gui.encaged.hunter_desc.wh=Can see survivors and equipment through walls",
            "gui.case.treasury.bonus.legend.title=Unique item",
            "gui.bureau.reward_type.weapon=WEAPON",
            "gui.operation.how_to_play.category.breach=The Big Clean Up Operation"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("gui-lang-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static GuiLangCatalog loadFixture() throws IOException {
        return GuiLangCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("title/headerLabel/endTimeLabel resolve for a real season banner", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertEquals("MURMUR OF THE ZONE", catalog.title("summer25.banner"), "summer25 title");
            assertEquals("NEW SEASON", catalog.field("summer25.banner", "headerLabel"), "summer25 header");
            assertEquals("Until the end of the season:", catalog.field("summer25.banner", "endTimeLabel"), "summer25 endTimeLabel");
        }));

        out.add(test("a real empty banner desc resolves as an empty string, not null", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertEquals("BACK TO THE INSIDE OUT", catalog.title("spring25.banner"), "spring25 title");
            assertEquals("", catalog.desc("spring25.banner"), "spring25 desc genuinely empty");
        }));

        out.add(test("encaged tutorial-card and enemy-ability-tag text resolves", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertEquals("CHOOSE A CLASS", catalog.title("encaged.survivors.cards.select_class"), "encaged card title");
            assertTrue(catalog.desc("encaged.survivors.cards.select_class").startsWith("Select the class"), "encaged card desc");
            assertEquals("Can see survivors and equipment through walls",
                    catalog.field("encaged.hunter_desc", "wh"), "encaged hunter wallhack tag");
        }));

        out.add(test("case treasury and bureau reward-type labels resolve", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertEquals("Unique item", catalog.title("case.treasury.bonus.legend"), "case treasury legend title");
            assertEquals("WEAPON", catalog.field("bureau.reward_type", "weapon"), "bureau weapon reward type");
        }));

        out.add(test("gui.operation.how_to_play.* is excluded, already owned by OperationsCatalog", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertEquals(0, catalog.fieldNames("operation.how_to_play.category").size(),
                    "operation.how_to_play excluded from this catalog");
        }));

        out.add(test("ids/fieldNames are sorted and reflect the real fixture data", () -> {
            GuiLangCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(6, ids.size(), "6 real ids in fixture, excluding the operation.how_to_play line");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            GuiLangCatalog catalog = loadFixture();
            assertNull(catalog.title("not_a_real_id"), "unknown id title");
            assertNull(catalog.field("summer25.banner", "not_a_real_field"), "unknown field");
            assertEquals(0, catalog.fieldNames("not_a_real_id").size(), "unknown id field names");
        }));

        return out;
    }
}
