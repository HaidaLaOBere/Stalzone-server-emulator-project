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
 * Regression tests for {@link WorldFlavorCatalog}, using a fixture built from real key=value
 * lines drawn directly from {@code stalker/lang/en.lang} (part 162, live-verified on device) --
 * seven small namespaces including the rich DeliveryCatalog/StalkerMobsCatalog/ItemOriginsCatalog
 * corroborations.
 */
public final class WorldFlavorCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "territories.goal=clear out the territory",
            "territories.boar.name=Boar Territory",
            "territories.spring25_crystal.name=Small Growth of the Inside Out",
            "territories.spring25_crystal.goal=collect Raw Glass",
            "entity.tick.name=Tick",
            "entity.carevich.name=Boar Tsarevich",
            "entity.carevna.name=Piggy Princess",
            "entity.tushkanchik_vor.name=Rat-Robber",
            "entity.kukla.name=Doll",
            "anomaly.notf.anomaly.title=Attention!",
            "anomaly.notf.anomaly.info=A noo-converter was installed at the location",
            "anomaly.notf.anomaly_chrono.info=A chronodevice has been set up in this location",
            "anomaly.notf.anomaly_chro.info=A noo-converter was installed at the location",
            "death.attack.behaviour_node.subject=Killed by Zone Owners",
            "death.attack.laser_rave.subject=Killed by the Masters of the Zone",
            "tutorial.hint.medical=Don't forget to use the first-aid kit in battle [{key}]",
            "item_tier.novice.name=Newbie",
            "item_tier.veteran.name=Veteran",
            "provision.provision_zaton.name=Stillwaters",
            "provision.provision_institute.name=Institute"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("world-flavor-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static WorldFlavorCatalog loadFixture() throws IOException {
        return WorldFlavorCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real territories fields resolve via the generic table and the flat goal carve-out", () -> {
            WorldFlavorCatalog catalog = loadFixture();
            assertEquals("Boar Territory", catalog.territoryField("boar", "name"), "boar territory name");
            assertEquals("Small Growth of the Inside Out", catalog.territoryField("spring25_crystal", "name"), "spring25_crystal name ties to part 154/158's Inside Out event");
            assertEquals("collect Raw Glass", catalog.territoryField("spring25_crystal", "goal"), "spring25_crystal goal");
            assertEquals("clear out the territory", catalog.territoryFlatField("goal"), "flat territories.goal");
        }));

        out.add(test("real entity names resolve, including the carevich/carevna royal-boar naming pair and the tick confirmation", () -> {
            WorldFlavorCatalog catalog = loadFixture();
            assertEquals("Tick", catalog.entityName("tick"), "tick entity name confirms part 69's StalkerMobsCatalog tick mutant");
            assertEquals("Boar Tsarevich", catalog.entityName("carevich"), "carevich confirms part 69's crowned-boar tsarevich mutant");
            assertEquals("Piggy Princess", catalog.entityName("carevna"), "carevna's real 'Piggy' wording refines part 69's hedged royal-boar guess");
        }));

        out.add(test("the real anomaly_chro/anomaly text mismatch is preserved exactly as found, and death/tutorial/item_tier/provision fields resolve", () -> {
            WorldFlavorCatalog catalog = loadFixture();
            assertEquals(catalog.anomalyField("notf.anomaly", "info"), catalog.anomalyField("notf.anomaly_chro", "info"), "anomaly_chro's real text duplicates plain anomaly's, not anomaly_chrono's");
            assertTrue(!catalog.anomalyField("notf.anomaly_chrono", "info").equals(catalog.anomalyField("notf.anomaly_chro", "info")), "anomaly_chrono and anomaly_chro really carry different text despite the closer id spelling");
            assertEquals("Killed by Zone Owners", catalog.deathSubject("behaviour_node"), "behaviour_node death subject");
            assertTrue(catalog.tutorialMedicalHint().contains("first-aid kit"), "tutorial medical hint");
            assertEquals("Newbie", catalog.itemTierName("novice"), "novice item tier reuses the real codename vocabulary");
            assertEquals("Stillwaters", catalog.provisionName("zaton"), "zaton provision name matches part 136's DeliveryCatalog wnorth_zaton exactly");
            assertNull(catalog.entityName("not_a_real_id"), "unknown entity id");
        }));

        out.add(test("the part-165 MobConfig crosswalk resolves for the 3 original confirmed real configIds, including the transliteration-mismatched tsarevich/tsarevna pair, and null for anything else", () -> {
            WorldFlavorCatalog catalog = loadFixture();
            assertEquals("Boar Tsarevich", catalog.entityNameForMobConfigId("base_tsarevich"), "base_tsarevich maps to the real carevich entity despite the ts/c transliteration mismatch");
            assertEquals("Piggy Princess", catalog.entityNameForMobConfigId("base_tsarevna"), "base_tsarevna maps to the real carevna entity, confirming the flesh-theme (not boar) for tsarevna");
            assertEquals("Tick", catalog.entityNameForMobConfigId("base_tick"), "base_tick maps via configId, not MobConfig's own shared mobName='tushkan' field");
            assertNull(catalog.entityNameForMobConfigId("base_boar"), "a real mob config with no confirmed crosswalk resolves to null, not a guess");
        }));

        out.add(test("Part 178: base_thief_rat maps to the real tushkanchik_vor entity by transliteration-variant match, and base_puppet (checked, inconclusive) still resolves to null", () -> {
            WorldFlavorCatalog catalog = loadFixture();
            assertEquals("Rat-Robber", catalog.entityNameForMobConfigId("base_thief_rat"), "base_thief_rat's real mobName='tushkan_thief' matches tushkanchik_vor's thief+rat theme");
            assertEquals("Doll", catalog.entityName("kukla"), "kukla itself still resolves as real entity.* data");
            assertNull(catalog.entityNameForMobConfigId("base_puppet"), "base_puppet was checked against kukla and found inconclusive -- not wired despite both existing as real data");
        }));

        return out;
    }
}
