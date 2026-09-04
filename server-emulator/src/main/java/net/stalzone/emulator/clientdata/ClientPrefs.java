package net.stalzone.emulator.clientdata;

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
 * Loads the real local client-state file {@code config/prefs} -- confirmed by real magic-byte
 * inspection ({@code 28 B5 2F FD}, the standard Zstandard frame magic) and a clean parse (0
 * leftover bytes) to be Zstandard-compressed NBT, the SAME format already validated for {@code
 * session/save/PlayerSaveFile} and {@code economy/VendorArchive}. This is an account-wide/
 * top-level {@code config/} file (same directory as {@code quests.cfg}/{@code waypoints.cfg}/
 * {@code keybindings}/{@code options.json}), distinct from the per-character {@code common} slot
 * inside each {@code config/<CharacterName>/} folder (which {@link
 * net.stalzone.emulator.session.save.PlayerSaveFile}'s class doc already describes as holding
 * similar but per-character UI-preference state) -- this project has not found evidence either
 * file is a copy of the other; they're two separate real preference stores.
 *
 * <p>Confirmed real top-level fields, from a real, live install (also confirmed to be ACTIVELY
 * written -- the real {@code prefs}/{@code prefs_old} pair differed, with {@code seenArticleLinks}
 * containing 10 real entries in the newer copy vs 9 in the older, one new real Steam news URL
 * appended between writes -- same current/{@code _old} rotation pattern already confirmed
 * elsewhere in this project, applied here to a NEW real file):
 * <ul>
 *   <li>8 real one-shot "have I seen/done this" boolean flags (NBT {@code TAG_BYTE}, 0 or 1):
 *       {@code hasDoneInitialOptionsSetup}, {@code hasSeenShopAd}, {@code hasSeenEncagedHowToPlay},
 *       {@code hasSeenPrestigeHowToPlay}, {@code handbookNotificationReceptionCalibrated}, {@code
 *       skipComparisonCategoryDeleteConfirmation}, {@code profileMusicMuted}, {@code
 *       notifyAboutEncagedRewards} -- see {@link #getFlag}.</li>
 *   <li>{@code arcadesMapFilterMask}: a real {@code TAG_INT} bitmask (0 on the real install this
 *       was validated against -- no real nonzero sample seen, so which bit means which arcade map
 *       is NOT documented, per rule 1). {@code arcadesSelectedMaps}/{@code arcadesFavoriteMaps}:
 *       real string lists, both empty on the real install validated. <b>Real cross-validation,
 *       new to this project</b>: these three fields' existence corroborates the real "Arcade Team
 *       Battles" debug/result-screen keybinding names already documented in part 42 -- two
 *       independently-found real data sources agreeing an "arcades" game mode with selectable/
 *       favoritable maps genuinely exists client-side.</li>
 *   <li>{@code favoriteCharacters}: real string list of the account's own character names (the
 *       account this was validated against has favorited its own real character, already named
 *       elsewhere in this project's docs since it's the account's own data, not a third party's).</li>
 *   <li>{@code seenArticleLinks}: real string list, mostly {@code https://store.steampowered.com/
 *       news/app/1818450/view/&lt;id&gt;} URLs (real AppID 1818450, already documented in
 *       project_overview.md) -- real Steam news posts this account has dismissed/seen -- but NOT
 *       exclusively: the real install validated against had one real {@code handbook://
 *       articles/sessions/coliseum-informaciya-o-rezhime.md} entry mixed into the same list (a
 *       real handbook article about what its own path names a "Coliseum" session mode -- itself
 *       new evidence of a real game mode this project hadn't seen named elsewhere). So this field
 *       is a general "seen article/link" list spanning both external Steam posts and internal
 *       handbook articles, not two disjoint categories -- documented as observed, not assumed to
 *       be Steam-only.</li>
 *   <li>{@code seenExperiences}: real string list of season/event/episode codes -- e.g. {@code
 *       triumph_episode_eu}, {@code 260722_summer26_prestige_episode}, {@code
 *       experience_season_summer26}, {@code bp_weekend_episode}, {@code airdrop_forge_episode},
 *       {@code experience_season_autumn26}. Real, first-hand evidence of a seasonal/episodic
 *       content structure (battlepass-style "experience seasons" named after real seasons,
 *       consistent with the multi-year battlepass evidence already documented in part 40) --
 *       catalogued as real identifiers only, no attempt made to reconstruct what each episode's
 *       actual content is.</li>
 *   <li>{@code unseenHandbookEntries}: real string list of {@code handbook://articles/&lt;path&gt;.md}
 *       URIs -- the same real internal URI scheme already cross-validated in part 40 via {@code
 *       PlayerNotification}'s handbook channel. Real paths seen include categories {@code
 *       progression/}, {@code stats/}, {@code pvp/}, {@code base/}, and {@code arthunt/}.</li>
 * </ul>
 * All fields are exposed as their raw NBT-parsed values via {@link #rawFields()}; the well-
 * evidenced ones above additionally get a typed accessor. This class only reads and catalogs
 * these real values.
 */
public final class ClientPrefs {

    private final Map<String, Object> fields;

    @SuppressWarnings("unchecked")
    private ClientPrefs(Object rootValue) {
        this.fields = (rootValue instanceof Map) ? (Map<String, Object>) rootValue : Map.of();
    }

    public static ClientPrefs load(Path file) throws IOException {
        byte[] raw = Files.readAllBytes(file);
        byte[] decompressed = CompressionUtil.decompressIfNeeded(raw);
        NbtReader.Root root = NbtReader.readRoot(decompressed);
        return new ClientPrefs(root.value);
    }

    /** Direct access to every top-level field this file has, exactly as parsed from NBT. */
    public Map<String, Object> rawFields() {
        return fields;
    }

    /** A real {@code TAG_BYTE} 0/1 flag as a boolean; false if the key is absent or not a byte. */
    public boolean getFlag(String key) {
        Object v = fields.get(key);
        return (v instanceof Byte) && (Byte) v != 0;
    }

    /** A real {@code TAG_INT} value, or {@code defaultValue} if the key is absent or not an int. */
    public int getInt(String key, int defaultValue) {
        Object v = fields.get(key);
        return (v instanceof Integer) ? (Integer) v : defaultValue;
    }

    /** A real string-list field (e.g. {@code seenArticleLinks}), or empty if absent/not a list. */
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

    @Override
    public String toString() {
        return "ClientPrefs{topLevelKeys=" + fields.keySet() + "}";
    }
}
