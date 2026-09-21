package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real assets under {@code modassets/assets/shop/} (part 73) -- 494 files total, of which
 * 486 fall under 3 real top-level folders this catalog indexes ({@code models} 303, {@code sound} 147,
 * {@code textures} 36) and 8 are loose root-level files (a generic {@code box1.mcsb}/{@code box2.mcsb}
 * crate prop plus its {@code box}/{@code box_jiden} diffuse/normal/specular texture set) that the
 * folder-only {@link #load} walk deliberately does not see, same as every other catalog in this
 * package. Squarely game content (the in-game item-shop's loot-box/gacha system), not an EXENS
 * anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code models/}</b> (303 files) names a real loot-box ("case") system via 13 real case-type
 * subfolders: {@code case_bp2024} (36, a real 2024 Battle Pass case), {@code case_bp2025} (7, its 2025
 * successor), {@code case_collector} (6), {@code case_functional} (4, plus a reused {@code autumn25/}
 * skin variant), {@code case_ny25} (27, a real New Year 2025 case), {@code
 * case_ops} (4), {@code case_plastik} (55), {@code case_styles} (36), {@code case_twitch} (4, real
 * evidence of a Twitch Drops promotion, cross-referenced by {@code sound/cases/}'s own {@code
 * case_twitch_drop_opening.ogg}), {@code case_wooden} (67), {@code caseny24} (28, the 2024 New Year
 * case), and {@code podarki} ("gifts", Russian; 5 real {@code podarok1.mcsb}-{@code podarok5.mcsb} real
 * gift-box models). <b>{@code case_long/}</b> (11 files) is denser than its size suggests: a single
 * {@code case.mcsb} model reused across real {@code frontline_1}-{@code frontline_4} and {@code
 * divisions} texture-only skin-tier subfolders (the same "frontline"/"divisions" names also seen in
 * {@code textures/gui/background/} below, now confirmed as real case-skin tiers, not just background
 * art), an unconfirmed {@code ssop_N} filename-prefix lead, and a real leftover {@code test/
 * Cont_2_BaseColor.ol} dev-test artifact directly hitting this project's original cut-content interest.
 * Loose files directly in {@code models/} include a real faction-skinned case-texture
 * set -- {@code faction_duty.ol}/{@code faction_freedom.ol}/{@code faction_merc.ol} are real
 * S.T.A.L.K.E.R.-series faction names already confirmed elsewhere in this project (part 68's Dolg/
 * Freedom NPC voice-bark roster), while {@code faction_covenant.ol} is a fresh, unconfirmed custom
 * faction-name lead -- plus {@code halloween.ol}/{@code hw23.ol} (a real Halloween 2023 case skin) and
 * {@code blizzard.ol}/{@code ravent.ol} (cross-referencing {@code sound/cases/blizzard23}/{@code
 * ravent23} below).
 *
 * <p><b>{@code sound/}</b> (147 files) further corroborates the dated event-case system: {@code cases/}
 * (129 files: 65 loose generic case-open/UI sound effects plus 11 real dated event-case subfolders --
 * {@code autumn26}, {@code blizzard23}, {@code dmz}, {@code electrostorm}, {@code ny24}, {@code
 * operationscase25}, {@code ravent23}, {@code reichstag26}, {@code spring25}, {@code spring26}, {@code
 * summer26}). {@code reichstag26/}'s own literal {@code reichstag_2026_case_open_level_1_.ogg}-{@code
 * _6_.ogg} filenames are this project's SIXTH independent tree corroborating the real Reichstag/WWII
 * event (after parts 61, 64, 66, 70, 72), and the first direct evidence the event was reused/repeated
 * across years rather than a one-off (distinct from part 70's {@code reichstag_3shots} weapon arsenal).
 * The full set of dated case names spans a real ~2023-2026 event-release timeline. {@code itempacks/}
 * (1 file). {@code treasury/} (17 files) is a real, distinct card-based gacha/gambling minigame separate
 * from the case system ({@code cards_reroll.ogg}, {@code cards_eliminate.ogg}, {@code
 * cards_select_shops_open_gold.ogg}/{@code _silver.ogg} -- a real gold/silver tier distinction).
 *
 * <p><b>{@code textures/}</b> (36 files) has {@code ads/} (1 file, {@code wow.mic} -- a real, informally
 * named shop advertisement texture) and {@code gui/} (35 files: a top-level {@code total_atlas.mic} plus
 * {@code background/}, 34 files across a real seasonal shop-background rotation -- {@code autumn24/25/
 * 26}, {@code spring24/25/26}, {@code summer24/25/26}, {@code winter25/26}, plus {@code newyear}, {@code
 * dmz}, {@code reichstag}, {@code divisions} and {@code frontline} -- the latter two now directly
 * confirmed, via {@code models/case_long/} above, as real case-skin tiers rather than unexplained
 * background-only names).
 */
public final class StalkerShopCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerShopCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param shopDir the real {@code .../modassets/assets/shop} folder. */
    public static StalkerShopCatalog load(Path shopDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(shopDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    String folderName = entry.getFileName().toString();
                    List<String> files = new ArrayList<>();
                    collectFiles(entry, entry, files);
                    Collections.sort(files);
                    filesByFolder.put(folderName, files);
                }
            }
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(shopDir + ": no shop asset folders found");
        }

        return new StalkerShopCatalog(filesByFolder);
    }

    private static void collectFiles(Path folderRoot, Path dir, List<String> out) throws IOException {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    collectFiles(folderRoot, entry, out);
                } else {
                    out.add(folderRoot.relativize(entry).toString().replace('\\', '/'));
                }
            }
        }
    }

    /** Real top-level shop-asset folder names, alphabetical. */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /** Real file paths (relative to the folder itself) inside one real folder, or empty if unknown. */
    public List<String> filesIn(String folderName) {
        return Collections.unmodifiableList(filesByFolder.getOrDefault(folderName, List.of()));
    }

    /**
     * Real folder names that have at least one nested file whose name contains {@code substring}
     * (case-insensitive). Generic on purpose -- see the class doc for why this project verifies
     * narrative claims this way instead of hardcoding a fixed list.
     */
    public List<String> foldersContaining(String substring) {
        String needle = substring.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesByFolder.entrySet()) {
            for (String file : entry.getValue()) {
                if (file.toLowerCase(Locale.ROOT).contains(needle)) {
                    matches.add(entry.getKey());
                    break;
                }
            }
        }
        Collections.sort(matches);
        return matches;
    }

    public int totalFolderCount() {
        return filesByFolder.size();
    }

    public int totalFileCount() {
        int total = 0;
        for (List<String> files : filesByFolder.values()) {
            total += files.size();
        }
        return total;
    }
}
