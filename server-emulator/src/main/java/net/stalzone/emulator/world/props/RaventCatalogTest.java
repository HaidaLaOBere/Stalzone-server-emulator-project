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
 * Regression tests for {@link RaventCatalog}, using a fixture built from real {@code ravent.*}
 * lines drawn directly from {@code stalker/lang/en.lang} (part 132, live-verified on device) --
 * a real passive ability with its tiered-scaling desc text, and real named Slavic-folklore boss
 * waves tying to part 130's `world.modifier.ravent_level_*` finds and part 131's bpseason
 * "Arena of Slavs" event.
 */
public final class RaventCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "ravent.ability.passive.gladiator_health.name=Life-Giving Cross",
            "ravent.ability.passive.gladiator_health.desc=Player health increases by %1%10 units",
            "ravent.ability.passive.gladiator_damage.name=Bogatyr Strength",
            "ravent.waveTypes.boss_perun.name=Perun",
            "ravent.waveTypes.first_base_3.name=Slavic Chainsaw Massacre"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("ravent-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static RaventCatalog loadFixture() throws IOException {
        return RaventCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real passive ability's name/desc resolve together", () -> {
            RaventCatalog catalog = loadFixture();
            assertEquals("Life-Giving Cross", catalog.name("ability.passive.gladiator_health"), "gladiator_health name");
            assertEquals("Player health increases by %1%10 units", catalog.desc("ability.passive.gladiator_health"), "gladiator_health desc");
        }));

        out.add(test("real named Slavic-folklore boss and tier-ladder waves resolve independently", () -> {
            RaventCatalog catalog = loadFixture();
            assertEquals("Bogatyr Strength", catalog.name("ability.passive.gladiator_damage"), "gladiator_damage name");
            assertEquals("Perun", catalog.name("waveTypes.boss_perun"), "boss_perun name");
            assertEquals("Slavic Chainsaw Massacre", catalog.name("waveTypes.first_base_3"), "first_base_3 name");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            RaventCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.name("waveTypes.not_a_real_wave"), "unknown wave id");
            assertNull(catalog.desc("ability.passive.gladiator_damage"), "gladiator_damage has no real desc in fixture");
        }));

        return out;
    }
}
