package net.stalzone.emulator.session.save;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for PlayerInteraction/PlayerNotification (part 40), using synthetic field maps
 * built to the EXACT real shapes confirmed by direct NBT-type inspection against real save files
 * (see PlayerNotification's own doc for the field-by-field type confirmation) -- not files, since
 * these two classes' package-private constructors take an already-parsed Map directly and this
 * project's privacy discipline means no real other-player username belongs in this repo's own
 * files (see PlayerInteraction's doc); the username value below is an invented placeholder, not
 * real data, used only to test that the accessor returns whatever string it's given.
 */
public final class PlayerSaveFileTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("PlayerInteraction exposes type/username/allianceId from the real confirmed schema", () -> {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("type", "DIED_FROM_PLAYER");
            fields.put("username", "not_a_real_player");
            fields.put("allianceId", "freedom");
            PlayerInteraction pi = new PlayerInteraction(fields);
            assertEquals("DIED_FROM_PLAYER", pi.type(), "type");
            assertEquals("not_a_real_player", pi.username(), "username");
            assertEquals("freedom", pi.allianceId(), "allianceId");
        }));

        out.add(test("PlayerNotification: quests channel with a QuestId (real 'new'/'quest_finished' shape)", () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("QuestId", 27091);
            payload.put("type", "new");
            payload.put("QuestTitle", "For the Good of the Mercenaries");
            payload.put("quest_type", 2);

            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("channel_id", "quests");
            notification.put("time", 1788271328345L);
            notification.put("expire_time", 1788876128345L);
            notification.put("payload", payload);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("notification", notification);
            entry.put("isRead", (byte) 0);
            entry.put("receivedMoment", 1788271328160L);

            PlayerNotification pn = new PlayerNotification(entry);
            assertTrue(pn.isQuestChannel(), "should be quests channel");
            assertFalse(pn.isRead(), "isRead byte 0 should be false");
            assertEquals(27091, pn.questId(), "questId");
            assertEquals("new", pn.questNotificationType(), "questNotificationType");
            assertEquals("For the Good of the Mercenaries", pn.questTitle(), "questTitle");
            assertEquals(2, pn.questTypeCode(), "questTypeCode");
            assertEquals(1788271328345L, pn.time(), "time");
            assertEquals(1788271328160L, pn.receivedMoment(), "receivedMoment");
        }));

        out.add(test("PlayerNotification: quests channel with NO QuestId (real 'quest_failed'/'mail_reward' shape) -- questId() is null, not a guess", () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "quest_failed");
            payload.put("QuestTitle", "For the Good of the Mercenaries");
            payload.put("quest_type", 2);

            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("channel_id", "quests");
            notification.put("payload", payload);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("notification", notification);
            entry.put("isRead", (byte) 1);

            PlayerNotification pn = new PlayerNotification(entry);
            assertTrue(pn.isRead(), "isRead byte 1 should be true");
            assertNull(pn.questId(), "questId should be null when QuestId field is absent");
        }));

        out.add(test("PlayerNotification: outpost channel real x/z Float payload", () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("x", -10345.242f);
            payload.put("z", 2260.937f);

            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("channel_id", "outpost");
            notification.put("payload", payload);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("notification", notification);

            PlayerNotification pn = new PlayerNotification(entry);
            assertTrue(pn.isOutpostChannel(), "should be outpost channel");
            assertEquals(-10345.242f, pn.outpostX(), "outpostX");
            assertEquals(2260.937f, pn.outpostZ(), "outpostZ");
        }));

        out.add(test("PlayerNotification: handbook channel real link payload", () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("link", "handbook://articles/pvp/smert.md/#header_3");

            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("channel_id", "handbook");
            notification.put("payload", payload);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("notification", notification);

            PlayerNotification pn = new PlayerNotification(entry);
            assertTrue(pn.isHandbookChannel(), "should be handbook channel");
            assertEquals("handbook://articles/pvp/smert.md/#header_3", pn.handbookLink(), "handbookLink");
        }));

        out.add(test("PlayerSaveFile.load: interactions()/notifications() are empty (not null) when the top-level key is absent", () -> {
            // Round-trips through the real NbtWriter/NbtReader pipeline (a raw, uncompressed
            // compound with neither "interacts" nor "notifications" -- the real shape of a
            // common/sd3/sd4-type file, see PlayerSaveFile's own doc) rather than reaching past
            // PlayerSaveFile's private constructor, since load(Path) is the class's only real
            // entry point.
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("someOtherField", "irrelevant");
            byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("save-fixture", ".dat");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.write(tmp, bytes);

            PlayerSaveFile save = PlayerSaveFile.load(tmp);
            assertTrue(save.interactions().isEmpty(), "interactions should be empty list, not throw, when absent");
            assertTrue(save.notifications().isEmpty(), "notifications should be empty list, not throw, when absent");
            assertFalse(save.hasInteractions(), "hasInteractions should be false");
            assertFalse(save.hasNotifications(), "hasNotifications should be false");
        }));

        out.add(test("PlayerSaveFile.load: a real interacts list round-trips through NBT write+read correctly", () -> {
            Map<String, Object> interaction = new LinkedHashMap<>();
            interaction.put("type", "DIED_FROM_PLAYER");
            interaction.put("username", "not_a_real_player");
            interaction.put("allianceId", "freedom");
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("interacts", java.util.List.of(interaction));
            byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("save-fixture", ".dat");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.write(tmp, bytes);

            PlayerSaveFile save = PlayerSaveFile.load(tmp);
            assertTrue(save.hasInteractions(), "hasInteractions should be true");
            assertEquals(1, save.interactions().size(), "interactions size");
            assertEquals("DIED_FROM_PLAYER", save.interactions().get(0).type(), "round-tripped type");
        }));

        out.add(test("PlayerSaveFile.load: a real sd3-shaped observedOffers list round-trips (real Cyrillic offer names)", () -> {
            // Real offer names, exactly as observed on a real install (part 46) -- these are
            // in-game shop/cosmetic item names, not player-identifying data, so kept verbatim.
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("observedOffers", java.util.List.of("Набор «Суеверия»", "season/Продвинутые запчасти"));
            byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("save-fixture", ".dat");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.write(tmp, bytes);

            PlayerSaveFile save = PlayerSaveFile.load(tmp);
            assertTrue(save.hasObservedOffers(), "hasObservedOffers should be true");
            assertEquals(2, save.observedOffers().size(), "observedOffers size");
            assertTrue(save.observedOffers().get(1).startsWith("season/"), "second offer should keep its real season/ namespace prefix");
        }));

        out.add(test("PlayerSaveFile.load: real sd4-shaped fields round-trip -- version bytes and the confirmed little-endian uint16 observedElements decoding", () -> {
            // The real 8-byte observedElements array from a real AveghiliaBoy sd4 sample (part 46),
            // confirmed by cross-character byte-level analysis to decode as little-endian uint16
            // pairs -- see PlayerSaveFile's class doc for the two-real-character validation.
            byte[] observedElements = new byte[] {
                (byte) 0x94, (byte) 0x11, (byte) 0x3b, (byte) 0x13,
                (byte) 0x68, (byte) 0x10, (byte) 0xc0, (byte) 0x12
            };
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("lastSeenTagsVersion", (byte) 0);
            root.put("lastSeenBackgroundsVersion", (byte) 1);
            root.put("lastSeenPatternsVersion", (byte) 1);
            root.put("lastSeenStickersVersion", (byte) 1);
            root.put("observedElements", observedElements);
            byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("save-fixture", ".dat");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.write(tmp, bytes);

            PlayerSaveFile save = PlayerSaveFile.load(tmp);
            assertEquals(0, save.lastSeenTagsVersion(), "lastSeenTagsVersion");
            assertEquals(1, save.lastSeenBackgroundsVersion(), "lastSeenBackgroundsVersion");
            assertEquals(1, save.lastSeenPatternsVersion(), "lastSeenPatternsVersion");
            assertEquals(1, save.lastSeenStickersVersion(), "lastSeenStickersVersion");
            java.util.List<Integer> ids = save.observedElementIds();
            assertEquals(4, ids.size(), "observedElementIds count");
            assertEquals(4500, ids.get(0), "id[0], real value confirmed against a second real character's overlapping id");
            assertEquals(4923, ids.get(1), "id[1]");
            assertEquals(4200, ids.get(2), "id[2], real value confirmed against a second real character's overlapping id");
            assertEquals(4800, ids.get(3), "id[3], real value confirmed against a second real character's overlapping id");
        }));

        out.add(test("PlayerSaveFile.load: a real common-shaped getFlag/caseOpenCounts/opsDifficultyByMap round-trip (part 47)", () -> {
            // Real field names and real shapes from a real common file (two real characters
            // cross-validated the schema); real caseId 15081 and real map ids big_cleanup/
            // sea_alienation kept exactly as observed -- none of this is player-identifying data.
            Map<String, Object> caseEntry = new LinkedHashMap<>();
            caseEntry.put("caseId", 15081);
            caseEntry.put("openAmount", 20);
            Map<String, Object> caseOuter = new LinkedHashMap<>();
            caseOuter.put("caseLastOpenCount", java.util.List.of(caseEntry));

            Map<String, Object> opsEntry1 = new LinkedHashMap<>();
            opsEntry1.put("id", "big_cleanup");
            opsEntry1.put("difficulty", 1);
            Map<String, Object> opsEntry2 = new LinkedHashMap<>();
            opsEntry2.put("id", "sea_alienation");
            opsEntry2.put("difficulty", 1);

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("arcadesMenuOpened", (byte) 1);
            root.put("hasSeenDMZHowToPlay", (byte) 0);
            root.put("caseLastOpenCount", caseOuter);
            root.put("lastOpsDifficultyByMap", java.util.List.of(opsEntry1, opsEntry2));

            byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("save-fixture", ".dat");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.write(tmp, bytes);

            PlayerSaveFile save = PlayerSaveFile.load(tmp);
            assertTrue(save.getFlag("arcadesMenuOpened"), "arcadesMenuOpened");
            assertFalse(save.getFlag("hasSeenDMZHowToPlay"), "hasSeenDMZHowToPlay");
            assertFalse(save.getFlag("does_not_exist"), "missing flag defaults false");

            List<CaseOpenCount> counts = save.caseOpenCounts();
            assertEquals(1, counts.size(), "caseOpenCounts size");
            assertEquals(15081, counts.get(0).caseId(), "real caseId");
            assertEquals(20, counts.get(0).openAmount(), "real openAmount");

            List<OpsMapDifficulty> ops = save.opsDifficultyByMap();
            assertEquals(2, ops.size(), "opsDifficultyByMap size");
            assertEquals("big_cleanup", ops.get(0).mapId(), "real map id 0");
            assertEquals("sea_alienation", ops.get(1).mapId(), "real map id 1");
            assertEquals(1, ops.get(0).difficulty(), "real difficulty 0");
        }));

        return out;
    }
}
