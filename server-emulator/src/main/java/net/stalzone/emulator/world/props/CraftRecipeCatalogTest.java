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
 * Regression tests for {@link CraftRecipeCatalog}, using a fixture built from real {@code
 * craft_recipe.*} lines drawn directly from {@code stalker/lang/en.lang} (part 106, live-verified
 * on device).
 */
public final class CraftRecipeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "craft_recipe.group.food=Food",
            "craft_recipe.group.containers=Containers",
            "craft_recipe.group.other=Other",
            "craft_recipe.group.ammo=Rounds",
            "craft_recipe.group.med=Medicine",
            "craft_recipe.id.sandwich_white.name=Salami Sandwich",
            "craft_recipe.id.sandwich_white.desc=Salami on white bread.",
            "craft_recipe.id.sandwich_black.name=Salami Sandwich",
            "craft_recipe.id.sandwich_black.desc=Salami on rye bread.",
            "craft_recipe.id.5_kzs_1.name=KZS-5",
            "craft_recipe.id.5_kzs_1.desc=A spacious container based on KZS-1 technology.",
            "craft_recipe.id.accum.name=Bell Battery",
            "craft_recipe.id.accum.desc=A battery made from parts and charged using a Spectral Crystal.",
            "craft_recipe.id.gaus_ammo.name=Fragment Projectiles for Gauss Rifle",
            "craft_recipe.id.gaus_ammo.desc=Projectiles and Battery for Gauss Rifle",
            "craft_recipe.id.homut_ris.name=Clamp with RIS Rail",
            "craft_recipe.id.homut_ris_double.name=Double Clamp with RIS Rail",
            "craft_recipe.id.mozgovik.name=Brainer",
            "craft_recipe.id.mozgovik.desc=A medicine made from rat brains and cerebral hyacinth. Used internally to reduce sensitivity to psyonic.",
            "craft_recipe.id.ointment_snork.name=Membrane Oil",
            "craft_recipe.id.ointment_snork.desc=A heated oil made from skitter heels and acid webcaps. It has a beneficial effect on the joints, improves flexibility and speeds up healing after exertion."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("craft-recipe-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static CraftRecipeCatalog loadFixture() throws IOException {
        return CraftRecipeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load counts real groups and recipes correctly", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals(5, catalog.groupCount(), "group count");
            assertEquals(9, catalog.recipeCount(), "recipe count");
        }));

        out.add(test("groupName resolves all five real craft groups", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals("Food", catalog.groupName("food"), "food");
            assertEquals("Containers", catalog.groupName("containers"), "containers");
            assertEquals("Rounds", catalog.groupName("ammo"), "ammo");
            assertEquals("Medicine", catalog.groupName("med"), "med");
        }));

        out.add(test("two distinct real recipe ids can share one display name", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals("Salami Sandwich", catalog.recipeName("sandwich_white"), "sandwich_white");
            assertEquals("Salami Sandwich", catalog.recipeName("sandwich_black"), "sandwich_black");
            assertEquals("Salami on white bread.", catalog.recipeDescription("sandwich_white"), "sandwich_white desc");
            assertEquals("Salami on rye bread.", catalog.recipeDescription("sandwich_black"), "sandwich_black desc");
        }));

        out.add(test("recipeName resolves the real Gauss Rifle ammo recipe", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals("Fragment Projectiles for Gauss Rifle", catalog.recipeName("gaus_ammo"), "gaus_ammo");
        }));

        out.add(test("recipeDescription returns null for a real id with no desc line", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals("Clamp with RIS Rail", catalog.recipeName("homut_ris"), "homut_ris name");
            assertEquals("Double Clamp with RIS Rail", catalog.recipeName("homut_ris_double"), "homut_ris_double name");
            assertNull(catalog.recipeDescription("homut_ris_double"), "homut_ris_double has no real desc line");
        }));

        out.add(test("real mutant-part alchemy recipes resolve with their real ingredient flavor text", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertEquals("Brainer", catalog.recipeName("mozgovik"), "mozgovik");
            assertTrue(catalog.recipeDescription("mozgovik").contains("cerebral hyacinth"), "mozgovik ingredients");
            assertEquals("Membrane Oil", catalog.recipeName("ointment_snork"), "ointment_snork");
            assertTrue(catalog.recipeDescription("ointment_snork").contains("skitter heels"), "ointment_snork ingredients");
        }));

        out.add(test("recipeName/groupName return null for unknown ids", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            assertNull(catalog.recipeName("not_a_real_recipe"), "unknown recipe");
            assertNull(catalog.groupName("not_a_real_group"), "unknown group");
        }));

        out.add(test("recipeIds/groupIds are sorted and contain the real fixture ids", () -> {
            CraftRecipeCatalog catalog = loadFixture();
            List<String> recipeIds = catalog.recipeIds();
            List<String> groupIds = catalog.groupIds();
            for (int i = 1; i < recipeIds.size(); i++) {
                assertTrue(recipeIds.get(i - 1).compareTo(recipeIds.get(i)) < 0, "recipeIds alphabetical at " + i);
            }
            for (int i = 1; i < groupIds.size(); i++) {
                assertTrue(groupIds.get(i - 1).compareTo(groupIds.get(i)) < 0, "groupIds alphabetical at " + i);
            }
            assertTrue(recipeIds.contains("accum"), "contains accum");
            assertTrue(groupIds.contains("med"), "contains med");
        }));

        return out;
    }
}
