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
 * Regression tests for {@link HandbookCatalog}, using a fixture built from real {@code core.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 128, live-verified on device) --
 * a real bestiary wiki entry, a nested category path, a real report-feedback option, and the one
 * real stray {@code core.tooltop.*} (client-typo'd "tooltip") line.
 */
public final class HandbookCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "core.handbook.id.snork.title=Skitter",
            "core.handbook.id.snork.main=A hunched, hairless mutant that moves on all fours.",
            "core.handbook.id.voronka.title=Whirlpool",
            "core.handbook.category.assault_rifle=Assault Rifles",
            "core.handbook.category.articles.article.arthunt=Zone Phenomena",
            "core.handbook.subcategory.articles.article.arthunt.subcategory1=SUBCATEGORY 1",
            "core.handbook.tag.pvp=PvP",
            "core.handbook.tag.testtag=TEST TAG",
            "core.handbook.report.option_5=Errors, inaccuracies, or contradictions in the content",
            "core.handbook.notification.custom_1=Custom notification description",
            "core.tooltop.locitem.loc_type.frontline=Conflict zone"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("handbook-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static HandbookCatalog loadFixture() throws IOException {
        return HandbookCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real bestiary wiki entry's title/main resolve, corroborating the territories.snork find", () -> {
            HandbookCatalog catalog = loadFixture();
            assertEquals("Skitter", catalog.articleTitle("snork"), "snork title matches territories.snork.name Skitter Territory");
            assertTrue(catalog.articleMain("snork").startsWith("A hunched, hairless mutant"), "snork main body");
            assertEquals("Whirlpool", catalog.articleTitle("voronka"), "voronka title");
            assertNull(catalog.articleMain("voronka"), "voronka has no main in this fixture -- resolves null, not throws");
        }));

        out.add(test("real category/subcategory labels resolve, including a multi-dot nested path", () -> {
            HandbookCatalog catalog = loadFixture();
            assertEquals("Assault Rifles", catalog.categoryLabel("assault_rifle"), "flat category path");
            assertEquals("Zone Phenomena", catalog.categoryLabel("articles.article.arthunt"), "nested category path");
            assertEquals("SUBCATEGORY 1", catalog.subcategoryLabel("articles.article.arthunt.subcategory1"), "nested subcategory path");
        }));

        out.add(test("real tag/report/notification flat entries resolve", () -> {
            HandbookCatalog catalog = loadFixture();
            assertEquals("PvP", catalog.tagLabel("pvp"), "pvp tag");
            assertEquals("TEST TAG", catalog.tagLabel("testtag"), "real leftover testtag");
            assertEquals("Errors, inaccuracies, or contradictions in the content", catalog.reportOption("option_5"), "report option 5");
            assertEquals("Custom notification description", catalog.notification("custom_1"), "notification");
        }));

        out.add(test("the one real core.tooltop.* line (client typo for tooltip) resolves", () -> {
            HandbookCatalog catalog = loadFixture();
            assertEquals("Conflict zone", catalog.frontlineLocTypeTooltip(), "frontline loc_type tooltip");
        }));

        out.add(test("articleIds() is sorted and unknown ids/fields/paths resolve to null/empty rather than throwing", () -> {
            HandbookCatalog catalog = loadFixture();
            List<String> ids = catalog.articleIds();
            assertEquals(2, ids.size(), "2 real article ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertNull(catalog.articleTitle("not_a_real_entry"), "unknown article id");
            assertNull(catalog.categoryLabel("not_a_real_path"), "unknown category path");
            assertNull(catalog.tagLabel("not_a_real_tag"), "unknown tag id");
        }));

        return out;
    }
}
