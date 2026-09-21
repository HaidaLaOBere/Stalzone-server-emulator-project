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
 * Regression tests for {@link HideoutCatalog}, using a fixture built from real {@code hideout.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 106, live-verified on device) --
 * covering the real workbench categories/perks/features/metrics, the titlePool naming generator's
 * faction corroborations (including the new {@code whisperers}/"Of Murmur" tie), the vendor roles,
 * and the real "Blue Sphere" storyline mail text.
 */
public final class HideoutCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "hideout.misc.can_be_closed=Can be closed",
            "hideout.benches.categories.ammunition.name=Ammo",
            "hideout.benches.categories.ammunition.9mm.name=9 mm",
            "hideout.benches.categories.materials.name=Materials",
            "hideout.benches.categories.materials.gematology.name=Hematology",
            "hideout.benches.subcategory.keyholders.name=Key Holders",
            "hideout.benches.subcategory.stashes.name=Stashes",
            "hideout.perks.brewing.name=Home-Brewing",
            "hideout.perks.brewing.desc=Craft raw materials and alcohol at the kitchen table to improve your skill",
            "hideout.perks.brewing.descShort=Skill Experience: Moonshining",
            "hideout.feature.matter_synthesizer.name=Matter Synthesizer",
            "hideout.feature.matter_synthesizer.desc=Access to new recipes",
            "hideout.feature.access/buyer.name=Access to Fence",
            "hideout.feature.generator_energy_source_anomal.name=Energy Sources:",
            "hideout.feature.generator_energy_source_anomal.value=ANOMALOUS",
            "hideout.metric.extra_space.name=Storage Capacity",
            "hideout.metric.extra_space.value=+{value}",
            "hideout.metric.generator_storage_modifier.name=Energy Capacity",
            "hideout.metric.generator_storage_modifier.value=+{value}%",
            "hideout.metric.generator_disintegration_rate_modifier.name=Fission speed of fuel sources",
            "hideout.metric.generator_disintegration_rate_modifier.value=+{value}%/SEC",
            "hideout.quickVendors.buyer.title=Fence",
            "hideout.quickVendors.repairman.title=Mechanic",
            "hideout.category.special=Special",
            "hideout.subcategory.special.trophies=Trophies",
            "hideout.subcategory.special.events=Thematic",
            "hideout.titlePool.leftSide.refuge=Hideout",
            "hideout.titlePool.leftSide.fortress=Fortress",
            "hideout.titlePool.rightSide.whisperers=Of Murmur",
            "hideout.titlePool.rightSide.zory=Of Rise",
            "hideout.titlePool.rightSide.rubezh=Of Frontier",
            "hideout.firstWeatherStyleIntroMail=You've received a Hideout Style!\\nYou can apply it via the Laptop in the Hideout.\\n\\nIf access to the Hideout is not yet unlocked, complete the southern storyline: decide the fate of the Blue Sphere and join one of the northern factions.",
            "hideout.conveniences.shared.hint=You can use the facilities of another player's Hideout, provided that he is on your friends list."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("hideout-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static HideoutCatalog loadFixture() throws IOException {
        return HideoutCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("benchCategoryName resolves both one- and two-level real category paths", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Ammo", catalog.benchCategoryName("ammunition"), "ammunition");
            assertEquals("9 mm", catalog.benchCategoryName("ammunition.9mm"), "ammunition.9mm");
            assertEquals("Hematology", catalog.benchCategoryName("materials.gematology"), "materials.gematology");
        }));

        out.add(test("benchSubcategoryName resolves the separate benches.subcategory family", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Key Holders", catalog.benchSubcategoryName("keyholders"), "keyholders");
            assertEquals("Stashes", catalog.benchSubcategoryName("stashes"), "stashes");
        }));

        out.add(test("perk name/desc/descShort all resolve for a real crafting skill", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Home-Brewing", catalog.perkName("brewing"), "brewing name");
            assertTrue(catalog.perkDescription("brewing").contains("kitchen table"), "brewing desc");
            assertEquals("Skill Experience: Moonshining", catalog.perkDescriptionShort("brewing"), "brewing descShort");
        }));

        out.add(test("featureName/featureDescription resolve a real lab feature and its access-slash id", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Matter Synthesizer", catalog.featureName("matter_synthesizer"), "matter_synthesizer name");
            assertEquals("Access to new recipes", catalog.featureDescription("matter_synthesizer"), "matter_synthesizer desc");
            assertEquals("Access to Fence", catalog.featureName("access/buyer"), "access/buyer name");
        }));

        out.add(test("a generator energy-source feature's .value line resolves via featureDescription", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Energy Sources:", catalog.featureName("generator_energy_source_anomal"), "name");
            assertEquals("ANOMALOUS", catalog.featureDescription("generator_energy_source_anomal"), "value folded into description");
        }));

        out.add(test("metricName/quickVendorTitle resolve real generator metrics and NPC roles", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Storage Capacity", catalog.metricName("extra_space"), "extra_space");
            assertEquals("Fence", catalog.quickVendorTitle("buyer"), "buyer");
            assertEquals("Mechanic", catalog.quickVendorTitle("repairman"), "repairman");
        }));

        out.add(test("Part 174: metricValue resolves the real .value format-template lines alongside metricName, for both flat-count and percentage/rate metrics", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("+{value}", catalog.metricValue("extra_space"), "extra_space value template");
            assertEquals("Energy Capacity", catalog.metricName("generator_storage_modifier"), "generator_storage_modifier name still resolves");
            assertEquals("+{value}%", catalog.metricValue("generator_storage_modifier"), "generator_storage_modifier value template");
            assertEquals("+{value}%/SEC", catalog.metricValue("generator_disintegration_rate_modifier"), "generator_disintegration_rate_modifier value template");
            assertNull(catalog.metricValue("not_a_real_metric"), "unknown metric id resolves to null");
        }));

        out.add(test("categoryName/subcategoryName resolve the real placement taxonomy", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Special", catalog.categoryName("special"), "special category");
            assertEquals("Trophies", catalog.subcategoryName("special", "trophies"), "special.trophies");
            assertEquals("Thematic", catalog.subcategoryName("special", "events"), "special.events");
        }));

        out.add(test("titlePool resolves real faction-corroborating right-side suffixes, including the new Murmur tie", () -> {
            HideoutCatalog catalog = loadFixture();
            assertEquals("Hideout", catalog.titlePoolLeft("refuge"), "refuge");
            assertEquals("Fortress", catalog.titlePoolLeft("fortress"), "fortress");
            assertEquals("Of Murmur", catalog.titlePoolRight("whisperers"), "whisperers -> Of Murmur");
            assertEquals("Of Rise", catalog.titlePoolRight("zory"), "zory -> Of Rise");
            assertEquals("Of Frontier", catalog.titlePoolRight("rubezh"), "rubezh -> Of Frontier");
        }));

        out.add(test("firstWeatherStyleIntroMail discloses the real Blue Sphere storyline beat", () -> {
            HideoutCatalog catalog = loadFixture();
            assertTrue(catalog.firstWeatherStyleIntroMail().contains("Blue Sphere"), "mentions Blue Sphere");
            assertTrue(catalog.firstWeatherStyleIntroMail().contains("northern factions"), "mentions northern factions");
        }));

        out.add(test("sharedHint confirms hideouts are visitable by friends", () -> {
            HideoutCatalog catalog = loadFixture();
            assertTrue(catalog.sharedHint().contains("friends list"), "friends list");
        }));

        out.add(test("titlePoolLeftIds/titlePoolRightIds are sorted and contain the real fixture ids", () -> {
            HideoutCatalog catalog = loadFixture();
            List<String> left = catalog.titlePoolLeftIds();
            List<String> right = catalog.titlePoolRightIds();
            for (int i = 1; i < left.size(); i++) {
                assertTrue(left.get(i - 1).compareTo(left.get(i)) < 0, "left alphabetical at " + i);
            }
            for (int i = 1; i < right.size(); i++) {
                assertTrue(right.get(i - 1).compareTo(right.get(i)) < 0, "right alphabetical at " + i);
            }
            assertTrue(left.contains("refuge"), "contains refuge");
            assertTrue(right.contains("whisperers"), "contains whisperers");
        }));

        out.add(test("unknown ids resolve to null across all lookups", () -> {
            HideoutCatalog catalog = loadFixture();
            assertNull(catalog.perkName("not_a_real_perk"), "unknown perk");
            assertNull(catalog.featureName("not_a_real_feature"), "unknown feature");
            assertNull(catalog.categoryName("not_a_real_category"), "unknown category");
        }));

        return out;
    }
}
