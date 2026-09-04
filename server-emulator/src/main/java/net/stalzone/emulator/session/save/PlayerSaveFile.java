package net.stalzone.emulator.session.save;

import net.stalzone.emulator.nbt.CompressionUtil;
import net.stalzone.emulator.nbt.NbtReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loads one real per-character save-data file. On a real STALZONE install these live at
 * {@code config/<CharacterName>/{common,sd1,sd2,sd3,sd4}} (each with an {@code _old} backup
 * copy alongside it) -- discovered this part by browsing the real config directory, not
 * documented anywhere this project had found before. Every one of the five files is
 * Zstandard-compressed NBT, the SAME format already validated for
 * {@code net.stalzone.emulator.economy.VendorArchive} (part 24) -- confirmed by magic-byte
 * inspection ({@code 28 B5 2F FD}) and a clean parse (0 leftover bytes) on real samples of all
 * five slots from two different real characters. No new binary-format work was needed; this
 * class is a thin, content-shape-aware wrapper over the existing {@link NbtReader}/
 * {@link CompressionUtil} pipeline, same as {@code VendorArchive} and {@code WorldModel} before it.
 *
 * <p>What the five slot names actually hold, confirmed from real files (not guessed from the
 * names -- {@code sd1}-{@code sd4} give no hint on their own):
 * <ul>
 *   <li>{@code common}: client UI/preference state -- no server-authoritative game data found
 *       here. Real top-level shape confirmed across two real characters (part 47): ~20 real
 *       one-shot {@code TAG_BYTE} "seen/opened" flags (see {@link #getFlag}), several real empty
 *       {@code TAG_LIST}s (thin evidence, element type unconfirmed), and two well-evidenced
 *       nested structures now with typed wrappers -- {@code caseLastOpenCount.caseLastOpenCount}
 *       (real per-case open-count tracking, see {@link CaseOpenCount}/{@link #caseOpenCounts()})
 *       and {@code lastOpsDifficultyByMap} (real per-map Operations difficulty, see {@link
 *       OpsMapDifficulty}/{@link #opsDifficultyByMap()}). <b>Real finds, new to this project,
 *       from real flag/key names alone</b>: {@code hasSeenDMZHowToPlay} and {@code
 *       hasSeenReichstagHowToPlay} name two real game modes/locations ("DMZ", "Reichstag") not
 *       previously documented anywhere in this project; {@code snowmanFeed}/{@code nyVisitedTabs}
 *       real evidence of a New Year seasonal event; {@code phonographListenList} (real nested
 *       {@code records}/{@code tracks} lists, both empty in real samples -- thin evidence) real
 *       evidence of an in-game phonograph/jukebox feature; {@code personalShopOpenedCards} (real
 *       empty compound) real evidence of a player-to-player personal shop feature; {@code
 *       complaintsData} real evidence of an in-client player-reporting/moderation UI (report
 *       categories only -- {@code reportedMailIds}/{@code reportedProfiles}/{@code
 *       reportedHacks}/{@code reportedCharnames} -- no moderation/anti-cheat internals touched or
 *       implied, consistent with rule 2). {@code arcadesMenuOpened} is a THIRD independent real
 *       source (after the part 42 keybindings and part 45 {@code prefs} fields) corroborating a
 *       real "arcades" mode.</li>
 *   <li>{@code sd1}: top-level {@code interacts} list -- see {@link #interactions()}.</li>
 *   <li>{@code sd2}: top-level {@code notifications} list -- see {@link #notifications()}.</li>
 *   <li>{@code sd3}: top-level {@code observedOffers} list -- real Cyrillic shop/cosmetic-case
 *       offer names this character has already seen in a shop UI (part 46: now cross-validated
 *       across THREE real characters/accounts, 5-108 real entries per sample, with the same
 *       offer names recurring across characters -- e.g. {@code "Набор «Суеверия»"} appears in
 *       every sample -- confirming this is a shared real shop catalog, not per-character random
 *       data). Not gameplay-authoritative. See {@link #observedOffers()}.</li>
 *   <li>{@code sd4}: 4 real {@code TAG_BYTE} "last seen cosmetic-tab version" counters --
 *       {@code lastSeenTagsVersion}, {@code lastSeenBackgroundsVersion}, {@code
 *       lastSeenPatternsVersion}, {@code lastSeenStickersVersion} -- plus one real {@code
 *       observedElements} byte array. <b>Real, byte-level-confirmed decoding, part 46, not
 *       previously documented</b>: {@code observedElements} is a flat sequence of little-endian
 *       {@code uint16} element ids, not opaque bytes -- confirmed by decoding two real,
 *       independent characters' arrays (8 bytes / 4 ids, and 18 bytes / 9 ids) and finding THREE
 *       exact ids in common ({@code 4200}, {@code 4500}, {@code 4800}) between the two
 *       independently-observed sets, which would not happen from an arbitrary/wrong byte
 *       grouping. All decoded ids fall in a tight real range ({@code 4200}-{@code 4923}),
 *       consistent with a small, shared cosmetic-element id space. See {@link
 *       #observedElementIds()}.</li>
 * </ul>
 * All five slots now have well-evidenced, typed wrappers where the evidence supports it ({@link
 * PlayerInteraction}, {@link PlayerNotification}, {@link #observedOffers()}, {@link
 * #observedElementIds()}, the {@code lastSeen*Version} getters, {@link #getFlag}, {@link
 * #caseOpenCounts()}, {@link #opsDifficultyByMap()}); fields with only thin/empty real samples so
 * far (e.g. {@code trashedItems.ids}'s element type, {@code phonographListenList}'s two lists)
 * stay reachable only via {@link #rawFields()}, following this project's rule 1 -- don't build a
 * typed model past what's actually been observed.
 *
 * <p><b>Privacy note:</b> {@code sd1}'s real {@code interacts} entries name OTHER real players by
 * their actual account username (see {@link PlayerInteraction}'s doc). This class does not filter
 * or redact that field -- a real server implementation legitimately needs it (e.g. to render a kill
 * feed) -- but nothing in this project's own docs, comments, or tests reproduces an actual
 * username seen while validating this class; only the field schema and the enum-like {@code type}
 * values observed are documented.
 *
 * <p><b>Real cross-validation, part 40</b>: the top-level {@code config/quests.cfg} file (JSON,
 * {@code {hiddenQuests:[...], readQuests:[...]}}, bare numeric quest ids) was checked against a
 * real {@code sd2} sample from the same character -- every quest id appearing in {@code sd2} with
 * a {@code QuestId} field (e.g. 27091, 27621, 33388, 33389) also appears in {@code quests.cfg}'s
 * {@code readQuests} list, and the one id in {@code hiddenQuests} (27091) is exactly the quest
 * that cycles new-&gt;failed-&gt;new-&gt;failed-&gt;...-&gt;finished in {@code sd2} (a
 * {@code quest_type=2} entry -- see {@link PlayerNotification}'s doc). This is real, observed
 * cross-file consistency, not an assumption about how the two files relate.
 */
public final class PlayerSaveFile {

    private final Map<String, Object> fields;

    @SuppressWarnings("unchecked")
    private PlayerSaveFile(Object rootValue) {
        this.fields = (rootValue instanceof Map) ? (Map<String, Object>) rootValue : Map.of();
    }

    public static PlayerSaveFile load(Path file) throws IOException {
        byte[] raw = Files.readAllBytes(file);
        byte[] decompressed = CompressionUtil.decompressIfNeeded(raw);
        NbtReader.Root root = NbtReader.readRoot(decompressed);
        return new PlayerSaveFile(root.value);
    }

    /** Direct access to every top-level field this file has, exactly as parsed from NBT. */
    public Map<String, Object> rawFields() {
        return fields;
    }

    public boolean hasInteractions() {
        return fields.get("interacts") instanceof List;
    }

    public boolean hasNotifications() {
        return fields.get("notifications") instanceof List;
    }

    /** The real {@code interacts} list (sd1-shaped files), or an empty list if this file has none. */
    @SuppressWarnings("unchecked")
    public List<PlayerInteraction> interactions() {
        Object raw = fields.get("interacts");
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> rawList = (List<Object>) raw;
        List<PlayerInteraction> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            if (o instanceof Map) {
                out.add(new PlayerInteraction((Map<String, Object>) o));
            }
        }
        return out;
    }

    /** The real {@code notifications} list (sd2-shaped files), or an empty list if this file has none. */
    @SuppressWarnings("unchecked")
    public List<PlayerNotification> notifications() {
        Object raw = fields.get("notifications");
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> rawList = (List<Object>) raw;
        List<PlayerNotification> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            if (o instanceof Map) {
                out.add(new PlayerNotification((Map<String, Object>) o));
            }
        }
        return out;
    }

    public boolean hasObservedOffers() {
        return fields.get("observedOffers") instanceof List;
    }

    /**
     * The real {@code observedOffers} list (sd3-shaped files) -- real Cyrillic shop/cosmetic-case
     * offer names this character has already seen in a shop UI -- or an empty list if this file
     * has none. See class doc for the real cross-character validation of this field.
     */
    @SuppressWarnings("unchecked")
    public List<String> observedOffers() {
        Object raw = fields.get("observedOffers");
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> rawList = (List<Object>) raw;
        List<String> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            if (o instanceof String) {
                out.add((String) o);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private int lastSeenVersion(String key) {
        Object v = fields.get(key);
        return (v instanceof Byte) ? (Byte) v : 0;
    }

    /** Real {@code TAG_BYTE} "last seen cosmetic-tab version" counter (sd4-shaped files), 0 if absent. */
    public int lastSeenTagsVersion() {
        return lastSeenVersion("lastSeenTagsVersion");
    }

    /** Real {@code TAG_BYTE} "last seen cosmetic-tab version" counter (sd4-shaped files), 0 if absent. */
    public int lastSeenBackgroundsVersion() {
        return lastSeenVersion("lastSeenBackgroundsVersion");
    }

    /** Real {@code TAG_BYTE} "last seen cosmetic-tab version" counter (sd4-shaped files), 0 if absent. */
    public int lastSeenPatternsVersion() {
        return lastSeenVersion("lastSeenPatternsVersion");
    }

    /** Real {@code TAG_BYTE} "last seen cosmetic-tab version" counter (sd4-shaped files), 0 if absent. */
    public int lastSeenStickersVersion() {
        return lastSeenVersion("lastSeenStickersVersion");
    }

    /**
     * The real {@code observedElements} byte array (sd4-shaped files), decoded as a flat sequence
     * of little-endian {@code uint16} cosmetic-element ids -- confirmed decoding, see class doc.
     * A trailing odd byte (never observed on a real file) is ignored rather than guessed at.
     * Empty if the field is absent or not a byte array.
     */
    public List<Integer> observedElementIds() {
        Object raw = fields.get("observedElements");
        if (!(raw instanceof byte[])) {
            return Collections.emptyList();
        }
        byte[] bytes = (byte[]) raw;
        List<Integer> out = new ArrayList<>(bytes.length / 2);
        for (int i = 0; i + 1 < bytes.length; i += 2) {
            int lo = bytes[i] & 0xFF;
            int hi = bytes[i + 1] & 0xFF;
            out.add(lo | (hi << 8));
        }
        return Collections.unmodifiableList(out);
    }

    /** A real top-level {@code TAG_BYTE} 0/1 flag (common-shaped files) as a boolean; false if absent/not a byte. */
    public boolean getFlag(String key) {
        Object v = fields.get(key);
        return (v instanceof Byte) && (Byte) v != 0;
    }

    /** A real top-level string-list field (common-shaped files), or empty if absent/not a list. */
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object v = fields.get(key);
        if (!(v instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> raw = (List<Object>) v;
        List<String> out = new ArrayList<>(raw.size());
        for (Object o : raw) {
            if (o instanceof String) {
                out.add((String) o);
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * The real nested {@code caseLastOpenCount.caseLastOpenCount} list (common-shaped files) --
     * real per-case open-count tracking, see {@link CaseOpenCount}. Empty if absent/not shaped as
     * expected.
     */
    @SuppressWarnings("unchecked")
    public List<CaseOpenCount> caseOpenCounts() {
        Object outer = fields.get("caseLastOpenCount");
        if (!(outer instanceof Map)) {
            return Collections.emptyList();
        }
        Object inner = ((Map<String, Object>) outer).get("caseLastOpenCount");
        if (!(inner instanceof List)) {
            return Collections.emptyList();
        }
        List<CaseOpenCount> out = new ArrayList<>();
        for (Object o : (List<Object>) inner) {
            if (o instanceof Map) {
                out.add(new CaseOpenCount((Map<String, Object>) o));
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * The real top-level {@code lastOpsDifficultyByMap} list (common-shaped files) -- real
     * per-map Operations difficulty, see {@link OpsMapDifficulty}. Empty if absent/not a list.
     */
    @SuppressWarnings("unchecked")
    public List<OpsMapDifficulty> opsDifficultyByMap() {
        Object raw = fields.get("lastOpsDifficultyByMap");
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<OpsMapDifficulty> out = new ArrayList<>();
        for (Object o : (List<Object>) raw) {
            if (o instanceof Map) {
                out.add(new OpsMapDifficulty((Map<String, Object>) o));
            }
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public String toString() {
        return "PlayerSaveFile{topLevelKeys=" + fields.keySet() + "}";
    }
}
