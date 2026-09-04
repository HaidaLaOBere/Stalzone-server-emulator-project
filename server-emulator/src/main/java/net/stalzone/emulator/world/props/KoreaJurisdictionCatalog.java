package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;

/**
 * Catalogs the real regional content-override assets under {@code modassets/assets/_jurisdiction/
 * korea/} (part 60) -- 5 real top-level FOLDERS ({@code customitems}, {@code stalcraft}, {@code
 * stalker}, {@code stalkerguide}, {@code weapons}), confirmed by direct directory listing, holding
 * all 60 real files with NO loose root-vs-walked gap. This is a genuine regional content-override
 * SYSTEM, not an anti-cheat/launcher asset: {@code _jurisdiction/} itself contains only ONE region
 * folder, {@code korea}, confirmed by direct listing -- no evidence of any other region in this
 * build. The override content is a mix of alternate cutscenes, localized strings, and cosmetic
 * item variants; whether any specific file here is a genuine ratings-board content substitution
 * versus an unrelated Korea-market addition is NOT evidenced by the file listing alone and is
 * disclosed as unconfirmed throughout, in line with this project's evidence rules. This folder is
 * squarely game CONTENT (regional asset variance), not EXBO anti-cheat/launcher internals, so it
 * stays within this project's standing focus (rule 2).
 *
 * <p><b>{@code stalkerguide/}</b> (41 files) is by far the largest sub-area, and is a real PARTIAL
 * override of part 59's base {@code stalkerguide/} cutscene catalog -- every one of its 18 unique
 * cutscene base names ({@code cinemat}, {@code grenade_throw}, {@code heli_crash}, {@code lipa_1}/
 * {@code _2}/{@code _3}, {@code morg_death}, {@code morg_fires_roof}, {@code morg_shaman_fight},
 * {@code morg_shaman_meet}, {@code phits}, {@code ppayalnik}, {@code pshoker}, {@code pshot},
 * {@code shaman_death}, {@code shaman_flies}, {@code vlasov_death}, {@code vlasov_escape}) is a
 * real name already found in the base {@code stalkerguide/video/} roster -- confirming this is a
 * substitution set for exactly these 18 story beats, not new Korea-exclusive narrative content.
 * Real, disclosed asymmetry: only 11 of the 18 names ship as a {@code _720}/{@code _1080} pair here
 * ({@code cinemat}, {@code heli_crash}, all 3 {@code lipa_*}, {@code phits}, {@code ppayalnik},
 * {@code pshoker}, {@code pshot}, both {@code vlasov_*}); the other 7 ({@code grenade_throw},
 * {@code morg_death}, {@code morg_fires_roof}, {@code morg_shaman_fight}, {@code
 * morg_shaman_meet}, {@code shaman_death}, {@code shaman_flies}) ship as a real {@code _1080}-only
 * single file, dropping the {@code _720} variant the base game has for every one of these names.
 * The audio side uses a real, DIFFERENT internal path shape than the base game: {@code
 * sound/stream/*.ogg} (11 files, matching 11 of the 18 video names) here, versus base
 * stalkerguide's separate {@code sound/} (with {@code glitch_monitor/}) and {@code stream/} (with
 * {@code stream/video/}) split -- disclosed as a real structural difference, not just a content
 * difference. One real anomaly: {@code stream/video/prestige.ogg} is the only file under this
 * jurisdiction's {@code stream/video/}, and "prestige" does NOT match any of the 18 real video
 * names here or any name in part 59's base 99-file {@code video/} roster -- a THIRD independent
 * real "prestige" cross-reference in this project, after part 57's {@code aptechka_color_prestige}
 * and part 58's {@code token_prestige}, still unconfirmed as the same system.
 *
 * <p><b>{@code stalker/lang/}</b> (2 files, {@code ko.lang} + {@code en.lang}, both real plain-text
 * files, 3-4 lines each) is a real, NARROW localization override defining exactly 2 real
 * numeric item ids -- {@code 16120} ("Bag of Holepunch's Hair" / en: {@code A bag filled with
 * Holepunch's "patchy" hair. Stored in a sack for easier carrying.}) and {@code 16429} ("A Bag of
 * Hair") -- both real quest/trophy items. {@code 16120}'s description is the first real evidence
 * in this project of a named character "Holepunch" (an NPC nickname in the STALKER-series naming
 * tradition), tied to a grim hair-collection trophy item; disclosed alongside part 59's {@code
 * customitems/textures/items/quest_cutted_ear.ol} finding below as a second real body-part/trophy
 * quest-item name, though no direct evidence links the two into a single quest. Whether this 2-item
 * {@code .lang} override is a ratings-related text edit versus an unrelated Korea-specific
 * localization addition is NOT evidenced by the file content alone (this project's own broader
 * {@code _localized/} folder, flagged but not yet swept as of part 58, was not consulted for a
 * base-text comparison this part).
 *
 * <p><b>{@code customitems/}</b> (8 files): real decal/poster overrides ({@code
 * propaganda_posters/regular/horizontal/friendship.ol}, {@code plakat/dec_mirnas.ol} -- "plakat" is
 * Russian for "poster"), a real recurring "stalboy" postcard/mascot quest item with 2 real dated
 * texture variants ({@code stalboy_hw23_diff.ol}, {@code stalboy_summer24_diff.ol}, joining this
 * project's established dated-content-series pattern from parts 55/56/58), a real {@code
 * otkritka_diff.ol} ("otkritka" is Russian for "postcard"), and the real {@code
 * quest_cutted_ear.ol}/{@code quest_cutted_ear_3.ol} pair -- a grim trophy-item texture (a severed
 * ear), a second real body-part/trophy quest-item name in this same jurisdiction override alongside
 * the hair-bag items above, plausibly (not confirmed) a ratings-sensitive asset in this region.
 *
 * <p><b>{@code weapons/}</b> (7 files): a real "lighter" bolt-throwable item ({@code
 * models/bolt/lighter/art/art_diff.ol}/{@code art_spek.ol} plus {@code
 * textures/items/bolts/lighter.ol}), a real {@code mr153hell.ol} weapon skin, a real Halloween-2023
 * dated weapon skin ({@code styles/m1014b_halloween23.ol}), and a real cosmetic weapon-charm
 * accessory pair -- {@code accessories/stalcraft_store/dakimakura.ol} and {@code
 * dakimakura_naked.ol} -- disclosed factually as a real two-variant asset pair with no further
 * claim about their in-game content, since the file names alone do not evidence what visually
 * distinguishes the two textures.
 *
 * <p><b>{@code stalcraft/}</b> and the rest of {@code stalker/}</b> (1 and 1 remaining files):
 * a real cosmetic sticker {@code customisation/stickers/na/cowgirl.ol} appears under BOTH {@code
 * stalcraft/textures/items/} and {@code stalker/textures/gui/} -- the same real sticker name
 * duplicated across two different subsystems in this one jurisdiction override, unconfirmed
 * whether the two files are byte-identical or distinct per-subsystem variants.
 */
public final class KoreaJurisdictionCatalog {

    private final Map<String, List<String>> filesByFolder;

    private KoreaJurisdictionCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param koreaDir the real {@code .../_jurisdiction/korea} folder. */
    public static KoreaJurisdictionCatalog load(Path koreaDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(koreaDir)) {
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
            throw new IOException(koreaDir + ": no jurisdiction override folders found");
        }

        return new KoreaJurisdictionCatalog(filesByFolder);
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

    /** Real top-level jurisdiction-override folder names, alphabetical. */
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
