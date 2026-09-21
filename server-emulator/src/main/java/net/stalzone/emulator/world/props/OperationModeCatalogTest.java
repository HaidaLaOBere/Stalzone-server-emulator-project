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
 * Regression tests for {@link OperationModeCatalog}, using a fixture built from a representative
 * real slice of the {@code operation.*} namespace (part 172, live-verified against the full {@code
 * en.lang} file), covering all five real sub-groups.
 */
public final class OperationModeCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "operation.classes.combat.name=Assault",
            "operation.classes.combat.desc=The Assault class specializes in battlefield control, provoking enemies, and managing damage.",
            "operation.classes.scientist.name=Support",
            "operation.classes.combined.name=Operative",
            "operation.introduction.description_big_cleanup=Phil from the ZIVCAS Scientific Research Institute corporation needs fighters for good pay.",
            "operation.introduction.description_sea_alienation=Something strange is happening in the North. Rise is in a frenzy over some mysterious documents, Frontier has lost a squad of military stalkers, and the Covenant has encountered an old enemy.",
            "operation.big_cleanup.name=The Big Clean Up",
            "operation.big_cleanup.desc=",
            "operation.sea_alienation.name=Sea of Exclusion",
            "operation.quest_sea_alienation.name=Sea of Exclusion",
            "operation.abilities.ops_ultimate_tank.name=Tremor",
            "operation.abilities.ops_ultimate_tank.desc=Accumulates damage and explodes.",
            "operation.abilities.passive_ops_scout_mix.name=Combat composition",
            "operation.task.absorber.name=Phase Generator",
            "operation.task.absorber.desc=Place the battery with dust in the generator",
            "operation.task.boss_ratter.name=Ratcatcher Boss"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("operation-mode-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OperationModeCatalog loadFixture() throws IOException {
        return OperationModeCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real class names/descriptions resolve, and classIds() surfaces all 3 real classes", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertEquals("Assault", catalog.className("combat"), "real combat class name");
            assertEquals("Support", catalog.className("scientist"), "real scientist class name");
            assertEquals("Operative", catalog.className("combined"), "real combined class name");
            assertTrue(catalog.classDescription("combat").contains("battlefield control"), "real combat class description");
            assertEquals(3, catalog.classIds().size(), "3 real classes in the fixture");
        }));

        out.add(test("real mission names resolve under the bare operation.<missionId> table, without absorbing the dotted sub-namespaces", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertEquals("The Big Clean Up", catalog.missionName("big_cleanup"), "real big_cleanup mission name");
            assertEquals("Sea of Exclusion", catalog.missionName("sea_alienation"), "real sea_alienation mission name");
            assertEquals("Sea of Exclusion", catalog.missionName("quest_sea_alienation"), "the real duplicate display name across two different real mission ids is preserved, not merged");
            assertTrue(!catalog.missionIds().contains("classes") && !catalog.missionIds().contains("abilities") && !catalog.missionIds().contains("task") && !catalog.missionIds().contains("introduction"), "the real dotted sub-namespaces are correctly excluded from the mission table");
        }));

        out.add(test("real mission intro descriptions resolve by mission id, and only missions that really have one are listed", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertTrue(catalog.missionIntroDescription("big_cleanup").contains("ZIVCAS Scientific Research Institute"), "real ZIVCAS corroboration in the big_cleanup intro");
            assertTrue(catalog.missionIntroDescription("sea_alienation").contains("Rise") && catalog.missionIntroDescription("sea_alienation").contains("Frontier") && catalog.missionIntroDescription("sea_alienation").contains("Covenant"), "real faction display names corroborated in the sea_alienation intro");
            assertNull(catalog.missionIntroDescription("quest_sea_alienation"), "this real mission id has no real intro description of its own");
            assertEquals(2, catalog.missionIdsWithIntroDescription().size(), "2 real missions with an intro description in the fixture");
        }));

        out.add(test("real ability names/descriptions resolve, including an ability with only a real name and no real desc in the fixture", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertEquals("Tremor", catalog.abilityName("ops_ultimate_tank"), "real Tremor ability name");
            assertTrue(catalog.abilityDescription("ops_ultimate_tank").contains("explodes"), "real Tremor ability description");
            assertEquals("Combat composition", catalog.abilityName("passive_ops_scout_mix"), "real passive_ops_scout_mix ability name");
            assertNull(catalog.abilityDescription("passive_ops_scout_mix"), "this real ability has no real desc line in the fixture");
        }));

        out.add(test("real task names/descriptions resolve", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertEquals("Phase Generator", catalog.taskName("absorber"), "real absorber task name");
            assertTrue(catalog.taskDescription("absorber").contains("generator"), "real absorber task description");
            assertEquals("Ratcatcher Boss", catalog.taskName("boss_ratter"), "real boss_ratter task name");
        }));

        out.add(test("unknown ids resolve to null rather than a guess, and size() reflects the total real line count across all five sub-groups", () -> {
            OperationModeCatalog catalog = loadFixture();
            assertNull(catalog.className("not_a_real_class"), "unknown real class id resolves to null");
            assertNull(catalog.abilityName("not_a_real_ability"), "unknown real ability id resolves to null");
            assertNull(catalog.taskName("not_a_real_task"), "unknown real task id resolves to null");
            assertNull(catalog.missionName("not_a_real_mission"), "unknown real mission id resolves to null");
            assertEquals(16, catalog.size(), "16 real lines in this fixture, across all five sub-groups");
        }));

        return out;
    }
}
