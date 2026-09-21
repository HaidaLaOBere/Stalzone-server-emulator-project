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
 * Regression tests for {@link OddsAndEndsCatalog}, using a fixture built from real key=value
 * lines drawn directly from {@code stalker/lang/en.lang} (part 164, live-verified on device) --
 * the last ~15 single-line "prefixes" from part 123's follow-up census, closing it out in full.
 */
public final class OddsAndEndsCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "signals.exit.name=Exit",
            "signal_chrono.name=Chronorift",
            "signal_dungeon.name=Dungeon",
            "signal_wnorth.name=ES-30",
            "signal_sequence.name=Tangled",
            "signal_sequence_beacon_key.name=A",
            "signal_sequence_dungeon_key.name=B",
            "polygon7_hint.description=First, Burn must leave the Mutation Department.",
            "toxic_dogout_hint1.description=The door is locked. Perhaps a key can be found among Art's belongings.",
            "subtitles.quest.dungeon_toxic_dogout.text=We need to get out of here. Maybe it's worth searching for Art's Hideout.",
            "player_effect.hunger.title=Hunger",
            "effect.category.inside.phantoms=Phantoms",
            "stalkerclans.battlefields.no_access.message=The mode becomes available at Bar reputation level 3",
            "filler.wild_mining_5.additional_stats_tip=A large energy cell with an empty charge capacity.",
            "item.filler.wild_mining_5.name=Broken Phase Battery"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("odds-and-ends-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static OddsAndEndsCatalog loadFixture() throws IOException {
        return OddsAndEndsCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real signal marker names resolve, including the chrono theme link to WorldFlavorCatalog's anomaly_chrono", () -> {
            OddsAndEndsCatalog catalog = loadFixture();
            assertEquals("Exit", catalog.signalsExit(), "signals.exit name");
            assertEquals("Chronorift", catalog.signalChrono(), "signal_chrono name shares the real 'chrono' theme with WorldFlavorCatalog's anomaly_chrono");
            assertEquals("ES-30", catalog.signalWnorth(), "signal_wnorth name");
            assertEquals("A", catalog.signalSequenceBeaconKey(), "signal_sequence_beacon_key name");
            assertEquals("B", catalog.signalSequenceDungeonKey(), "signal_sequence_dungeon_key name");
        }));

        out.add(test("the real coherent Burn/Art questline snippet resolves across all three separate real lines, confirming part 26's MilestoneMap dungeon_toxic_dogout zone", () -> {
            OddsAndEndsCatalog catalog = loadFixture();
            assertTrue(catalog.polygon7Hint().contains("Burn"), "polygon7_hint references real NPC Burn");
            assertTrue(catalog.toxicDogoutHint1().contains("Art's belongings"), "toxic_dogout_hint1 references real NPC Art");
            assertTrue(catalog.dungeonToxicDogoutSubtitle().contains("Art's Hideout"), "subtitle confirms the real dungeon_toxic_dogout zone from MilestoneMap");
        }));

        out.add(test("the remaining odds and ends resolve, including the real orphaned filler.wild_mining_5 key sitting apart from item.filler.wild_mining_5", () -> {
            OddsAndEndsCatalog catalog = loadFixture();
            assertEquals("Hunger", catalog.playerEffectHungerTitle(), "player_effect hunger title");
            assertEquals("Phantoms", catalog.effectCategoryInsidePhantoms(), "effect category inside phantoms");
            assertTrue(catalog.stalkerclansNoAccessMessage().contains("Bar reputation level 3"), "stalkerclans no_access message");
            assertTrue(catalog.orphanedFillerWildMining5Tip().contains("energy cell"), "the real orphaned bare filler.* key resolves separately from item.filler.wild_mining_5.name");
        }));

        return out;
    }
}
