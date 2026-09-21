package net.stalzone.emulator.session;

import net.stalzone.emulator.progression.PlayerClass;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.testing.TestRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link PlayerStateSave} (part 87) -- pure round-trip logic against real
 * {@link PlayerState}/{@link Inventory}/{@link Wallet}/{@link QuestLog} objects and a real
 * temp-file {@link Path}, no real client files needed (this is this emulator's own persistence
 * format, not a reverse-engineered one -- see that class's own doc).
 */
public final class PlayerStateSaveTest {

    private static Path tempSavePath() throws Exception {
        Path path = Files.createTempFile("playerstatesave-test", ".properties");
        Files.delete(path); // exercise the real "file doesn't exist yet" -> save -> load round trip, not an overwrite
        return path;
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load is a no-op when the save file doesn't exist yet", () -> {
            Path path = tempSavePath();
            PlayerState player = new PlayerState("player", "dungeon_bolota_tutorial", 1.0, 2.0, 3.0);
            PlayerStateSave.load(player, path); // must not throw
            assertEquals(1.0, player.x, 1e-9, "position untouched when no save file exists");
            assertTrue(!Files.exists(path), "load does not create the file either");
        }));

        out.add(test("a full save/load round trip restores position, zone, class, and abilities", () -> {
            Path path = tempSavePath();
            PlayerState original = new PlayerState("player", "dungeon_bolota_tutorial", 111.5, 64.0, -222.25);
            original.playerClass = PlayerClass.HUNTER;
            original.unlockedAbilities.add(PrestigeAbility.BARRICADE);
            original.unlockedAbilities.add(PrestigeAbility.SCANNER);

            PlayerStateSave.save(original, path);
            assertTrue(Files.exists(path), "save creates the file");

            PlayerState restored = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            PlayerStateSave.load(restored, path);

            assertEquals("dungeon_bolota_tutorial", restored.zoneName, "zone restored");
            assertEquals(111.5, restored.x, 1e-9, "x restored");
            assertEquals(64.0, restored.y, 1e-9, "y restored");
            assertEquals(-222.25, restored.z, 1e-9, "z restored");
            assertEquals(PlayerClass.HUNTER, restored.playerClass, "class restored");
            assertTrue(restored.unlockedAbilities.contains(PrestigeAbility.BARRICADE), "BARRICADE restored");
            assertTrue(restored.unlockedAbilities.contains(PrestigeAbility.SCANNER), "SCANNER restored");
            assertEquals(2, restored.unlockedAbilities.size(), "exactly the 2 saved abilities, no extras");
        }));

        out.add(test("a save/load round trip restores inventory stacks and wallet balances", () -> {
            Path path = tempSavePath();
            PlayerState original = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            original.inventory.add(11352, 3);
            original.inventory.add(99001, 1);
            original.wallet.credit(Wallet.CURRENCY_MONEY, 5000);
            original.wallet.credit(Wallet.CURRENCY_SEASON_PASS, 7);

            PlayerStateSave.save(original, path);
            PlayerState restored = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            PlayerStateSave.load(restored, path);

            assertEquals(3, restored.inventory.countOf(11352), "item 11352 count restored");
            assertEquals(1, restored.inventory.countOf(99001), "item 99001 count restored");
            assertEquals(2, restored.inventory.distinctStackCount(), "exactly the 2 saved stacks, no extras");
            assertEquals(5000L, restored.wallet.balanceOf(Wallet.CURRENCY_MONEY), "money balance restored");
            assertEquals(7L, restored.wallet.balanceOf(Wallet.CURRENCY_SEASON_PASS), "season pass balance restored");
        }));

        out.add(test("a save/load round trip restores quest-log known/hidden/title/status together", () -> {
            Path path = tempSavePath();
            PlayerState original = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            original.questLog.markKnown(27621); // known-only, no title/status (the real quests.cfg-alone case)
            original.questLog.recordEvent(27091, "For the Good of the Mercenaries", QuestStatus.NEW);
            original.questLog.markHidden(27091); // 27091 is both known (via recordEvent) and hidden, real part-40 case

            PlayerStateSave.save(original, path);
            PlayerState restored = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            PlayerStateSave.load(restored, path);

            assertTrue(restored.questLog.isKnown(27621), "27621 known restored");
            assertTrue(!restored.questLog.isHidden(27621), "27621 not hidden");
            assertNull(restored.questLog.titleOf(27621), "27621 has no title");
            assertTrue(restored.questLog.isKnown(27091), "27091 known restored");
            assertTrue(restored.questLog.isHidden(27091), "27091 hidden restored");
            assertEquals("For the Good of the Mercenaries", restored.questLog.titleOf(27091), "27091 title restored");
            assertEquals(QuestStatus.NEW, restored.questLog.statusOf(27091), "27091 status restored");
        }));

        out.add(test("save then save-again then load reflects only the latest state (overwrite, not merge)", () -> {
            Path path = tempSavePath();
            PlayerState player = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            player.inventory.add(11352, 1);
            PlayerStateSave.save(player, path);

            player.inventory.remove(11352, 1);
            player.inventory.add(99001, 9);
            PlayerStateSave.save(player, path);

            PlayerState restored = new PlayerState("player", "dungeon_bolota_tutorial", 0, 0, 0);
            PlayerStateSave.load(restored, path);
            assertEquals(0, restored.inventory.countOf(11352), "11352 no longer present after the second save");
            assertEquals(9, restored.inventory.countOf(99001), "99001 present with the latest count");
        }));

        return out;
    }
}
