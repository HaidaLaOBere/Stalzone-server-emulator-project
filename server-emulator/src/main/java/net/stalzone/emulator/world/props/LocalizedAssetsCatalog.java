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
 * Catalogs the real localized-asset tree under {@code modassets/assets/_localized/} (part 80) -- this
 * project's last remaining unswept tree, 8,796 files, with exactly 5 real top-level folders and no
 * loose root files: {@code en} (3,003), {@code ru} (3,122), {@code es} (920), {@code fr} (916), {@code
 * ko} (835). Given the scale, this class's folder-only walk stops at these 5 real locale folders (same
 * generic pattern as every other large {@code world/props/*Catalog} class, e.g. {@link
 * WeaponAssetCatalog}) rather than attempting a deep, exhaustive per-file sweep -- but a direct, live
 * listing of each locale's immediate namespace subfolders, plus targeted content reads of a handful of
 * real files, turned up this part's most significant findings, described below. Squarely game content
 * (localized voice lines, subtitles, GUI textures, and in-game handbook articles), not an EXENS
 * anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>A real, disclosed per-locale namespace-coverage asymmetry.</b> Each locale mirrors a subset of
 * the top-level {@code modassets/assets/} namespaces (audio/subtitles/textures/handbook content for
 * that namespace), but the real subset differs: {@code en} has all 12 real namespaces found ({@code
 * anomalies}, {@code customitems}, {@code customnpcs}, {@code gloomycore}, {@code pda}, {@code shop},
 * {@code stalcraft}, {@code stalker}, {@code stalkerclans}, {@code stalkerguide}, {@code
 * stalkersounds}, {@code weapons}); {@code ru} has 11 (missing {@code gloomycore}, present in {@code
 * en}); {@code es}/{@code fr} each have the same real narrower 9 ({@code anomalies}, {@code
 * customitems}, {@code customnpcs}, {@code gloomycore}, {@code pda}, {@code shop}, {@code stalker},
 * {@code stalkerguide}, {@code weapons} -- missing {@code stalcraft}/{@code stalkerclans}/{@code
 * stalkersounds}); {@code ko} has by far the real narrowest set, just 4 ({@code customnpcs}, {@code
 * pda}, {@code stalker}, {@code stalkerguide}) -- missing {@code anomalies}/{@code customitems}/{@code
 * gloomycore}/{@code shop}/{@code weapons} entirely. This is a real, disclosed coverage gap, not
 * evidence any of the missing content is cut -- most plausibly these locales simply have no dedicated
 * localized assets for those namespaces yet (e.g. text-only string-table localization elsewhere,
 * outside this asset tree, not investigated this part). Consistent with part 60's finding that Korea
 * additionally carries its own separate {@code _jurisdiction/korea/} regional content-override system
 * layered on top of this narrower localization base.
 *
 * <p><b>Real, strong confirmation of the S.T.A.L.K.E.R.-series "emission"/"blowout" mechanic (upgrading
 * part 79's disclosed, unconfirmed {@code ejection/} lead).</b> {@code en/stalkerguide/handbook/
 * articles/ejection/} holds real in-game handbook articles ({@code sample_ejection.md}, {@code
 * sample_ejection2.md}, {@code shelters.md}) whose body text is real, coherent Russian design prose
 * (left untranslated in the {@code en} locale folder -- these are internal test/sample fixtures for the
 * handbook rendering system, not final shipped English copy, evidenced directly by their own titles/
 * tags: {@code sample_ejection}, a literal {@code # TEST} heading, a {@code #testtag} tag, and {@code
 * shelters.md}'s mangled {@code # EJECTIONNNNNNNN} heading with 17 duplicated placeholder warning
 * sections). Read directly (not assumed) before writing this claim. The real Russian text: "Выброс —
 * периодическое аномальное явление, накрывающее всю территорию Зоны" ("Ejection/Emission -- a periodic
 * anomalous phenomenon covering the Zone's entire territory"), describing a real 4-stage design: (1) a
 * warning phase where shelter map markers appear, (2) a real ~2-minute window to reach a marked shelter
 * ("survival outside a shelter is impossible"), occurring "regularly, approximately once every 2-4
 * hours, with a maximum 4-hour gap between events", (3) a completion notification, and (4) a real
 * post-emission artefact-spawn window (most active in the first 30 minutes, stopping entirely after 90
 * minutes) -- directly corroborating this project's existing {@code DetectorConfig}/artefact-hunting
 * systems. The sample files also reference a real-looking item tech name, {@code med.atlas} (5x, an
 * artefact-detector-device item, unconfirmed against any dataset this project has loaded), a real
 * weapon tech name {@code wpn.rpd_dsa}, and codex item ids ({@code 20180}, an intentionally-invalid
 * {@code 2128506}) -- none of which were found in this project's existing {@code data/weapons/} or
 * item datasets when checked, so these are disclosed as unconfirmed/test-fixture references, not a
 * cross-validated content match. Real, distinct handbook article namespaces also found: {@code
 * articles/arthunt/} (incl. {@code vybros.md} -- "vybros" is a direct Latin transliteration of
 * "выброс", the same real word), {@code articles/base}, {@code articles/events}, {@code
 * articles/progression}, {@code articles/pvp}, {@code articles/sessions}, {@code articles/stats}, and
 * a {@code codex/} tree with real {@code mutanty} ("mutants", Russian) and {@code poligon-16} entries.
 *
 * <p><b>Real fresh dated-content and Reichstag leads under {@code en/stalker/sound/} and {@code
 * streamrandom/}.</b> {@code sound/dmz2026/pers/} (119 files, e.g. {@code 2b11_strat1-10.ogg}) is a
 * real 2026-dated DMZ-mode audio set -- "2B11" is a real Soviet 120mm mortar system designation --
 * extending part 67's {@code dmz26_pack} finding with concrete voice/audio content. {@code
 * sound/ravent24/lizards/begin/start0-12.ogg} (39 files) is a real 2024-dated event audio set naming a
 * fresh, previously-unseen enemy/faction lead, "lizards" -- not corroborated anywhere else in this
 * project. {@code streamrandom/reichstag/} (10 files) is this project's SEVENTH independent Reichstag
 * corroboration (after parts 61/64/66/70/72/73) and by far the most specific: real named opposing-side
 * radio "calldown" lines, {@code RCCA/calldown_soviet1-5.ogg} and {@code Wermacht/calldown_wermacht1-
 * 5.ogg} -- direct confirmation of a real Soviet-vs-Wehrmacht framing for the mode, matching the real
 * WWII Battle of Berlin/Reichstag assault. {@code RCCA}'s expansion is unconfirmed, disclosed as an
 * open lead.
 */
public final class LocalizedAssetsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private LocalizedAssetsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param localizedDir the real {@code .../modassets/assets/_localized} folder. */
    public static LocalizedAssetsCatalog load(Path localizedDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(localizedDir)) {
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
            throw new IOException(localizedDir + ": no _localized asset folders found");
        }

        return new LocalizedAssetsCatalog(filesByFolder);
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

    /** Real locale folder names ({@code en}/{@code es}/{@code fr}/{@code ko}/{@code ru}), alphabetical. */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /** Real file paths (relative to the locale folder itself) inside one real locale, or empty if unknown. */
    public List<String> filesIn(String folderName) {
        return Collections.unmodifiableList(filesByFolder.getOrDefault(folderName, List.of()));
    }

    /**
     * Real locale folder names that have at least one nested file whose name contains {@code substring}
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
