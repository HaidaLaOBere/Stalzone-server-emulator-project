package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for ClientPrefs (part 45), built from a synthetic field map to the EXACT real
 * shape confirmed by direct NBT parse of a real {@code config/prefs} (zstd+NBT, real field names
 * and real NBT types -- {@code TAG_BYTE} flags, a {@code TAG_INT} bitmask, {@code TAG_LIST} of
 * {@code TAG_STRING}) -- not a real file, since the round trip goes through the real {@code
 * NbtWriter}/{@code NbtReader} pipeline the same way PlayerSaveFileTest's NBT test does, and the
 * only real per-account value that would appear here (the account's own character name) is
 * replaced with the same explicitly-fake placeholder this project always uses for save-shaped
 * fixtures, per its standing no-real-usernames policy. Real, non-personal values (the Steam
 * AppID-1818450 URL shape, real season/episode code strings, real handbook:// paths) are kept
 * exactly as observed, since those aren't player-identifying data.
 */
public final class ClientPrefsTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("ClientPrefs.load parses real-shaped flags, int, and string lists via the real zstd/NBT pipeline", () -> {
            ClientPrefs p = loadFixture();
            assertEquals(9, p.rawFields().size(), "top-level field count");
        }));

        out.add(test("getFlag: real TAG_BYTE 1/0 values decode to true/false", () -> {
            ClientPrefs p = loadFixture();
            assertTrue(p.getFlag("hasDoneInitialOptionsSetup"), "hasDoneInitialOptionsSetup");
            assertFalse(p.getFlag("hasSeenShopAd"), "hasSeenShopAd");
        }));

        out.add(test("getFlag: a missing key returns false, not a crash", () -> {
            ClientPrefs p = loadFixture();
            assertFalse(p.getFlag("does_not_exist"), "missing flag");
        }));

        out.add(test("getInt: real arcadesMapFilterMask TAG_INT value round-trips", () -> {
            ClientPrefs p = loadFixture();
            assertEquals(0, p.getInt("arcadesMapFilterMask", -1), "arcadesMapFilterMask");
        }));

        out.add(test("getStringList: real seenExperiences season/episode codes preserved in order", () -> {
            ClientPrefs p = loadFixture();
            List<String> experiences = p.getStringList("seenExperiences");
            assertEquals(2, experiences.size(), "seenExperiences count");
            assertEquals("triumph_episode_eu", experiences.get(0), "first experience code");
            assertEquals("experience_season_autumn26", experiences.get(1), "second experience code");
        }));

        out.add(test("getStringList: real unseenHandbookEntries handbook:// paths preserved", () -> {
            ClientPrefs p = loadFixture();
            List<String> entries = p.getStringList("unseenHandbookEntries");
            assertEquals(1, entries.size(), "unseenHandbookEntries count");
            assertEquals("handbook://articles/pvp/smert.md", entries.get(0), "handbook path");
        }));

        out.add(test("getStringList: an empty real list (arcadesSelectedMaps) comes back empty, not null", () -> {
            ClientPrefs p = loadFixture();
            assertNotNull(p.getStringList("arcadesSelectedMaps"), "arcadesSelectedMaps");
            assertEquals(0, p.getStringList("arcadesSelectedMaps").size(), "arcadesSelectedMaps count");
        }));

        out.add(test("getStringList: a key that doesn't exist returns empty, not null", () -> {
            ClientPrefs p = loadFixture();
            assertEquals(0, p.getStringList("does_not_exist").size(), "missing list");
        }));

        return out;
    }

    private static ClientPrefs loadFixture() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("hasDoneInitialOptionsSetup", (byte) 1);
        root.put("hasSeenShopAd", (byte) 0);
        root.put("handbookNotificationReceptionCalibrated", (byte) 1);
        root.put("arcadesMapFilterMask", 0);
        root.put("arcadesSelectedMaps", new ArrayList<String>());
        root.put("favoriteCharacters", Arrays.asList("not_a_real_player"));
        root.put("seenArticleLinks", Arrays.asList(
                "https://store.steampowered.com/news/app/1818450/view/686386819418292416"));
        root.put("seenExperiences", Arrays.asList("triumph_episode_eu", "experience_season_autumn26"));
        root.put("unseenHandbookEntries", Arrays.asList("handbook://articles/pvp/smert.md"));

        byte[] bytes = net.stalzone.emulator.nbt.NbtWriter.writeRoot("", root);
        Path tmp = Files.createTempFile("prefs-fixture", ".dat");
        tmp.toFile().deleteOnExit();
        Files.write(tmp, bytes);
        return ClientPrefs.load(tmp);
    }
}
