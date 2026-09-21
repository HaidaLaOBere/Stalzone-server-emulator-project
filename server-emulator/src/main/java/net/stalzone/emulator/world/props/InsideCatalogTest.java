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
 * Regression tests for {@link InsideCatalog}, using a fixture built from real {@code inside.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 154, live-verified on device) --
 * the real active/passive abilities and the real numbered leagues ladder that confirms part
 * 139's {@link ItemOriginsCatalog} inference.
 */
public final class InsideCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "inside.ability.active.inside_recovery.name=Burnt Onyx",
            "inside.ability.active.inside_recovery.description=You can use §f{value} pcs.§r of Burnt Onyx as a med kit",
            "inside.ability.passive.phantom_cure_buffs.name=Altar of Cleansing",
            "inside.leagues.1.name=Legend of the Inside Out",
            "inside.leagues.6.name=Shadow",
            "inside.leagues.1.reward_text=Legend of the Inside Out\\n\\nHello, Legend!"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("inside-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static InsideCatalog loadFixture() throws IOException {
        return InsideCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real active and passive abilities resolve", () -> {
            InsideCatalog catalog = loadFixture();
            assertEquals("Burnt Onyx", catalog.field("ability.active.inside_recovery", "name"), "inside_recovery name");
            assertTrue(catalog.field("ability.active.inside_recovery", "description").contains("med kit"), "inside_recovery description contains real phrase");
            assertEquals("Altar of Cleansing", catalog.field("ability.passive.phantom_cure_buffs", "name"), "phantom_cure_buffs name");
        }));

        out.add(test("the real numbered leagues ladder resolves, confirming part 139's ItemOriginsCatalog codename inference (leagues.1=legend, leagues.6=picklock)", () -> {
            InsideCatalog catalog = loadFixture();
            assertEquals("Legend of the Inside Out", catalog.field("leagues.1", "name"), "leagues.1 name matches the real 'legend' codename text from part 139");
            assertEquals("Shadow", catalog.field("leagues.6", "name"), "leagues.6 name matches the real 'picklock' codename text from part 139");
        }));

        out.add(test("a real reward_text resolves distinct from name, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            InsideCatalog catalog = loadFixture();
            assertTrue(catalog.field("leagues.1", "reward_text").contains("Hello, Legend!"), "leagues.1 reward_text contains real greeting");
            List<String> ids = catalog.ids();
            assertTrue(ids.size() > 0, "at least one real id in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) <= 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "name"), "unknown id");
        }));

        return out;
    }
}
