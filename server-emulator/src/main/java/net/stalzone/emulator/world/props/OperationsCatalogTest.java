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
 * Regression tests for {@link OperationsCatalog}, using a fixture built from real {@code
 * gui.operation.how_to_play.*} lines drawn directly from {@code stalker/lang/en.lang} (part 113,
 * live-verified on device) -- covering the flat {@code category} crosswalk, the regular {@code
 * mode} table, and the generic {@code (id, field)} model shared by {@code enemies}/{@code
 * rewards}/{@code breach}/{@code focus}/{@code sea_alienation}, including the real {@code skills}
 * pseudo-id irregularity in {@code enemies} and {@code sea_alienation}.
 */
public final class OperationsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "gui.operation.how_to_play.category.operation=Operations Mode",
            "gui.operation.how_to_play.category.enemies=Enemies",
            "gui.operation.how_to_play.category.rewards=Unique Rewards: Modules",
            "gui.operation.how_to_play.category.breach=The Big Clean Up Operation",
            "gui.operation.how_to_play.category.focus=Focus Operation",
            "gui.operation.how_to_play.category.sea_alienation=Sea of Exclusion Operation",
            "gui.operation.how_to_play.mode.mode_operations.name=Operations",
            "gui.operation.how_to_play.mode.mode_operations.desc=A PvE game mode designed for 3 players, where they complete a chosen linear dungeon with unique mechanics and bosses.",
            "gui.operation.how_to_play.mode.mode_cast.name=Enemy Ability Timer",
            "gui.operation.how_to_play.mode.mode_cast.desc=When an enemy ability is used, a circle with a timer appears above the enemy's health indicator.",
            "gui.operation.how_to_play.enemies.assault.name=Murmur Zombie",
            "gui.operation.how_to_play.enemies.assault.desc=Zombie whose neural circuits have undergone irreversible changes due to the harmful effects of psy-emission.",
            "gui.operation.how_to_play.enemies.skills.battle_cry.title=Battle Cry",
            "gui.operation.how_to_play.enemies.skills.battle_cry.text=- Restores an armor plate\\n- Loads explosive rounds\\n- Activates Suppressive Fire and Resistance",
            "gui.operation.how_to_play.rewards.module_base.desc=Modules are enhancements that can be installed on any weapon.",
            "gui.operation.how_to_play.breach.absorber.name=Photon Generator Power Supply",
            "gui.operation.how_to_play.breach.ratter.name=Ratcatcher",
            "gui.operation.how_to_play.breach.ratter.skills.whispers.title=Murmur Mantras",
            "gui.operation.how_to_play.breach.ratter.skills.whispers.text=Transforms into a rat and temporarily escapes the battlefield, sending other rats to attack opponents",
            "gui.operation.how_to_play.focus.electra.name=Storm",
            "gui.operation.how_to_play.focus.electra.skills.armor.title=Armor",
            "gui.operation.how_to_play.sea_alienation.technician.name=Tech",
            "gui.operation.how_to_play.sea_alienation.quest_boss.name=Captain Rozhin",
            "gui.operation.how_to_play.sea_alienation.skills.ship_artillery.title=Naval Salvo",
            "gui.operation.how_to_play.sea_alienation.skills.epmc71.title=Prototype Shot"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("operations-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OperationsCatalog loadFixture() throws IOException {
        return OperationsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("categoryLabel resolves the real flat tab-label crosswalk, including the three real Operation names", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Operations Mode", catalog.categoryLabel("operation"), "operation label");
            assertEquals("The Big Clean Up Operation", catalog.categoryLabel("breach"), "breach label");
            assertEquals("Focus Operation", catalog.categoryLabel("focus"), "focus label");
            assertEquals("Sea of Exclusion Operation", catalog.categoryLabel("sea_alienation"), "sea_alienation label");
            assertNull(catalog.categoryLabel("not_a_real_category"), "unknown category");
        }));

        out.add(test("modeName/modeDescription resolve real mode ids", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Operations", catalog.modeName("mode_operations"), "mode_operations name");
            assertEquals("Enemy Ability Timer", catalog.modeName("mode_cast"), "mode_cast name");
            assertTrue(catalog.modeDescription("mode_operations").contains("3 players"), "mode_operations desc");
        }));

        out.add(test("enemyName/enemyDescription resolve a real fighter, and enemyField resolves the real skills pseudo-id", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Murmur Zombie", catalog.enemyName("assault"), "assault name");
            assertTrue(catalog.enemyDescription("assault").contains("neural circuits"), "assault desc");
            assertEquals("Battle Cry", catalog.enemyField("skills", "battle_cry.title"), "skills pseudo-id title");
            assertTrue(catalog.enemyField("skills", "battle_cry.text").contains("armor plate"), "skills pseudo-id text");
        }));

        out.add(test("rewardDescription resolves the real Module-crafting lore", () -> {
            OperationsCatalog catalog = loadFixture();
            assertTrue(catalog.rewardDescription("module_base").contains("Modules are enhancements"), "module_base desc");
        }));

        out.add(test("breachField resolves the real Photon Generator lead and the ratter boss's nested skills", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Photon Generator Power Supply", catalog.breachName("absorber"), "absorber name (part 104 lead resolved)");
            assertEquals("Ratcatcher", catalog.breachName("ratter"), "ratter name");
            assertEquals("Murmur Mantras", catalog.breachField("ratter", "skills.whispers.title"), "ratter nested skill title");
        }));

        out.add(test("focusField resolves the real Storm boss and its nested skill", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Storm", catalog.focusName("electra"), "electra name");
            assertEquals("Armor", catalog.focusField("electra", "skills.armor.title"), "electra nested skill title");
        }));

        out.add(test("seaAlienationField resolves Tech/Captain Rozhin and the real top-level skills pseudo-id", () -> {
            OperationsCatalog catalog = loadFixture();
            assertEquals("Tech", catalog.seaAlienationName("technician"), "technician name");
            assertEquals("Captain Rozhin", catalog.seaAlienationName("quest_boss"), "quest_boss name-only lead");
            assertNull(catalog.seaAlienationDescription("quest_boss"), "quest_boss has no desc in real data");
            assertEquals("Naval Salvo", catalog.seaAlienationField("skills", "ship_artillery.title"), "skills pseudo-id ship_artillery");
            assertEquals("Prototype Shot", catalog.seaAlienationField("skills", "epmc71.title"), "skills pseudo-id epmc71");
        }));

        out.add(test("id-listing accessors are sorted and reflect real fixture ids", () -> {
            OperationsCatalog catalog = loadFixture();
            List<String> enemyIds = catalog.enemyIds();
            assertEquals(2, enemyIds.size(), "assault + skills pseudo-id");
            for (int i = 1; i < enemyIds.size(); i++) {
                assertTrue(enemyIds.get(i - 1).compareTo(enemyIds.get(i)) < 0, "sorted ascending");
            }
            assertTrue(catalog.breachIds().contains("ratter"), "breachIds has ratter");
            assertTrue(catalog.seaAlienationIds().contains("quest_boss"), "seaAlienationIds has quest_boss");
        }));

        out.add(test("unknown ids/fields/groups resolve to null/empty rather than throwing", () -> {
            OperationsCatalog catalog = loadFixture();
            assertNull(catalog.enemyName("not_a_real_enemy"), "unknown enemy name");
            assertNull(catalog.breachField("ratter", "not_a_real_field"), "unknown field on known id");
            assertEquals(0, catalog.enemyFieldNames("not_a_real_enemy").size(), "unknown enemy field names");
        }));

        return out;
    }
}
