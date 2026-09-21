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
 * Catalogs the real currency/token model and icon assets under {@code modassets/assets/currency/}
 * (part 58) -- 2 real top-level FOLDERS ({@code models}, {@code textures}), confirmed by direct
 * directory listing, holding all 98 real files on disk with NO loose root-vs-walked gap (unlike
 * parts 55/57's finds): every file sits inside one of the two folders. This is a much smaller
 * folder tree than the armor/artefact/backpack/item sweeps, but by far the richest single folder
 * this project has catalogued in named-currency terms -- {@link #foldersContaining(String)} is
 * still a reusable substring search over every real nested filename, so narrative claims below are
 * checked programmatically, not hand-counted, even though the top-level split is only two folders.
 *
 * <p><b>{@code models/}</b> (8 files): 6 real Russian ruble banknote-denomination props --
 * {@code 10rub.mcsb}, {@code 50rub.mcsb}, {@code 100rub.mcsb}, {@code 500rub.mcsb}, {@code
 * 1000rub.mcsb}, {@code 5000rub.mcsb} -- literal real-world currency values modeled as physical
 * pickup props, plus a generic {@code money.ol} texture and a single dedicated physical token prop,
 * {@code token_electroshtorm2026.mcsb} ("Electroshtorm" = "Electric Storm"), with its own full
 * diff/nrm/spek set in {@code textures/} root -- a real "26"-dated find, joining part 56's {@code
 * style_autumn26}/{@code bag_dmz26} and this same folder's own dated icon series below.
 *
 * <p><b>{@code textures/items/}</b> (86 files): mostly icon-only UI textures with no dedicated 3D
 * model, but NOT uniformly so -- a live check found 7 of the 86 icons DO pair with a real model
 * above: {@code money_10}, {@code money_50}, {@code money_100}, {@code money_500}, {@code
 * money_1000}, {@code money_5000} name-match the 6 ruble-denomination {@code .mcsb} models, and
 * {@code token_electroshtorm2026} (the icon) name-matches its own dedicated {@code
 * token_electroshtorm2026.mcsb} model; the other 79 icons remain the only real asset for their
 * currency. An earlier draft of this Javadoc claimed all 86 icons lacked a dedicated model -- this
 * live check disproved that, and it is corrected here rather than silently fixed, per this
 * project's "verify before committing a narrative number" discipline (the same discipline that
 * caught real errors in parts 53/55/56). Even with the correction this is still a real
 * currency/material roster far larger than the 3 currencies part 27's {@code VendorArchive} found
 * in actual trading-history save data ({@code money}, {@code sc_birthday_21}, {@code
 * season_pass_ticket_3}).
 *
 * <p><b>A real, systematic, dated seasonal-currency calendar spanning 2024-2026</b> -- the most
 * complete dated-content series this project has found, extending part 55/56's isolated dated
 * folders into an actual recurring structure: {@code autumn_24}, {@code autumn_25} (plus {@code
 * autumn_25_episode}/{@code _1}/{@code _2}/{@code _3} sub-variants), {@code autumn_26}, {@code
 * spring_24}, {@code spring_25}, {@code spring_26}, {@code summer_24}, {@code summer_25}, {@code
 * summer_26}, {@code winter_25} (plus {@code winter_26_episode}), {@code winter_26} -- {@code
 * winter_24} is real and notably ABSENT, a disclosed gap in an otherwise complete quarterly series.
 * {@code spring_26}/{@code summer_26}/{@code autumn_26}/{@code winter_26} are real, on-disk icon
 * names for 2026 content whose ship status relative to any specific date is NOT evidenced by the
 * file alone.
 *
 * <p><b>A second, parallel dated series specifically for the season pass</b>: {@code
 * season_battlepass_points} (base/undated) plus 9 real dated variants -- {@code _autumn24}, {@code
 * _autumn25}, {@code _spring24}, {@code _spring25}, {@code _spring26}, {@code _summer24}, {@code
 * _summer25}, {@code _winter25}, {@code _winter26} -- real evidence that {@code
 * season_pass_ticket_3} (part 27's only directly-observed battle-pass currency) is one of a much
 * larger real seasonal family, not a standalone currency. Structurally separate from these: 5 real
 * named ONE-OFF event battle-pass currencies -- {@code battlepass_points_hw22}, {@code
 * _halloween23}, {@code _ravent23}, {@code _blizzard23}, {@code _newyear24} -- five distinct real
 * past events, each apparently minting its own currency rather than sharing the seasonal token.
 *
 * <p><b>Real DMZ-mode-specific currencies</b>, extending parts 26/53's DMZ zone-name/asset-name
 * evidence into an actual progression economy: {@code dmz_coin}, {@code dmz26_token}, {@code
 * popularnostDMZ} ("popularnost" is Russian for "popularity"), {@code respectDMZ} -- real, distinct
 * from the base game's own {@code respect}/{@code loyal}/{@code levels} currencies found in this
 * same folder.
 *
 * <p><b>A SECOND and THIRD real Reichstag/RKKA cross-reference in this one folder</b>, on top of
 * the token model above: {@code reichstag_silver}, {@code reichstag_xp_free} -- extending the WWII
 * -event content this project has now found in armor (part 54), backpacks (part 56), and meds
 * (part 57) into a dedicated currency, a fifth real cross-reference for this event across five
 * different content areas.
 *
 * <p><b>A "shards" currency family with dated variants, plus a real platform-integration
 * currency</b>: {@code shards} (base), {@code shards21}, {@code shards22} (year-dated), and {@code
 * twitch_shards} -- the first real evidence in this project of a Twitch drops/streaming-integration
 * currency. {@code coins21} is a separate, independently year-dated icon -- the earliest real dated
 * content this project has found anywhere, predating every other dated file or folder by at least
 * 3 years.
 *
 * <p><b>{@code token_prestige}</b>: a standalone real prestige-currency icon that independently
 * corroborates part 57's hedged {@code aptechka_color_prestige} finding -- two now-independent real
 * assets point toward a cosmetic/rank "prestige" tier, still NOT confirmed as literally the same
 * system by any file or log.
 *
 * <p><b>Other real named tokens</b>: {@code token_hunter}, {@code token_fighter}, {@code
 * token_science}, {@code token_innovative}, {@code token_functional}, {@code
 * token_summer25_functional}, {@code token_snowflake}, {@code token_wrapper}, and {@code
 * token_triumph26} -- real evidence tying to the "Triumph" named live connection endpoint this
 * project found in its earliest wire-traffic work (part 11). {@code token_event} additionally has
 * its own {@code token_event.mic} file at {@code textures/} root -- the same real use of a {@code
 * .mic} extension for a shader/effect role (not diffuse/normal/emissive) this project first saw in
 * part 55's {@code shamanfight/shield/perlin.mic}.
 *
 * <p><b>Other real, disclosed-as-unconfirmed named currencies/materials, no further evidence
 * found</b>: {@code coupon}, {@code ticket}, {@code subsidies}, {@code cone}, {@code charge}, {@code
 * redenergy}, {@code module_ingibitor}, {@code onyx}/{@code onyx_burnt}, {@code frozenbones}, {@code
 * fireballs}/{@code fireballs2}, {@code kotleta} (Russian for "cutlet"), {@code sleeves}, {@code
 * ssop_help}, {@code stalbucks} (literally "Stalker Bucks", plausibly a premium currency name),
 * {@code respect}, {@code loyal}, {@code levels}, {@code craft_details}/{@code craft_details_hd},
 * {@code details}, {@code new_parts}, {@code encaged_currency}, and {@code pg_currency}/{@code
 * pg_na_currency} (abbreviation meaning unconfirmed).
 *
 * <p><b>UPDATED part 109:</b> a real, separate {@code en.lang} namespace, {@code
 * currency.type.id.<id>.*} (see {@link CurrencyDisplayCatalog}), gives real display
 * names/tooltips/hints to several ids this class found only as bare icon filenames: {@code
 * reichstag_silver}, {@code reichstag_xp_free}, {@code twitch_shards}="Twitch Fragments", {@code
 * ssop_help}/{@code ssop_help_currency}="Receipts issued by the SSPP...", {@code
 * encaged_currency} (a real dark atmospheric hint text), and {@code sc_birthday_21} (part 27's
 * VendorArchive currency) ="Trade Coins". Also real and newly found there: {@code
 * arsenal_reputation} and {@code prestige_dollars}="Patterns" (tying to a real, previously-unseen
 * "Soothsayer" entity/NPC name), plus four more real Reichstag-event currency ids ({@code
 * reichstag_xp_infantry}/{@code _scout}/{@code _machinegunner}/{@code _engineer}) this class never
 * found as icons at all.
 */
public final class CurrencyCatalog {

    private final Map<String, List<String>> filesByFolder;

    private CurrencyCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param currencyDir the real {@code .../currency} folder. */
    public static CurrencyCatalog load(Path currencyDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(currencyDir)) {
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
            throw new IOException(currencyDir + ": no currency asset folders found");
        }

        return new CurrencyCatalog(filesByFolder);
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

    /** Real top-level currency asset folder names, alphabetical. */
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
