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
import java.util.TreeSet;

/**
 * Catalogs the real assets under {@code modassets/assets/stalkerplayer/} (part 91) -- 278 files, one of
 * two real top-level folders under {@code modassets/assets/} that part 90 found had NEVER been
 * catalogued anywhere in this project (the other, {@code stalkersounds/}, remains a disclosed open lead
 * for a future part). Squarely game content (real player-character customization data, not an EXENS
 * anti-cheat/launcher asset), so stays in scope under rule 2.
 *
 * <p>Unlike every other catalog in this package, {@link #load} also walks the 13 real files sitting
 * loose at {@code stalkerplayer/}'s own root -- a deliberate, disclosed deviation from this package's
 * standard folder-only walk (the same kind of justified deviation part 79's {@code MiscAssetsCatalog}
 * made), because those root files are this folder's single most significant finding: {@code steve.mcsb}/
 * {@code steve.obj}/{@code steve.ol}/{@code steve.mic} plus {@code steve_vanilla_proportions.mcsb} are
 * the real BASE PLAYER RIG this game's whole player-head/body customization system is built around --
 * not a stray collider (part 63) or a death-ragdoll (part 76), but the literal foundation model every
 * head/body skin in this folder attaches to. This is this project's strongest evidence yet for the
 * user's firsthand Minecraft-origin account (part 63's addendum): the default player character is
 * still, at the engine level, named after Minecraft's own default skin. Root files are exposed under the
 * synthetic folder key {@link #ROOT_KEY}. Also at the root: {@code reich_head_test.mcsb}, a real
 * dedicated test head asset for the Reichstag/WWII event -- a NINTH independent tree corroborating that
 * event, after parts 61, 64, 66, 70, 72, 73, 80, and 90's {@code battlefield.id.reich} lang entry; and
 * {@code murmur_zombie.mcsb}, cross-referencing {@code heads/zombie/}'s own roster below.
 *
 * <p><b>{@code arkit/}</b> (3 files) is a real Apple ARKit-based facial-capture rig ({@code arkit.mcsb}/
 * {@code arkit.ol}, plus a real backup copy, {@code arkit_beckap.ol} -- "beckap" is a real, disclosed
 * misspelling of "backup" left in the shipped filename, not a rule-1 guess). <b>{@code bodies/}</b> (3
 * files) and <b>{@code head_animations/}</b> (5 files, incl. a real {@code zombie.mcvd} facial-animation
 * set) are small real seasonal-body and facial-animation trees.
 *
 * <p><b>{@code heads/}</b> (254 files, 5 real subfolders) is the bulk of this catalog:
 * <ul>
 * <li>{@code blessed/} (8 files, 4 real named skins {@code blessed_01}-{@code 04}) -- "blessed" is also
 * a real in-lore term, cross-referenced in part 70's Knyazev-camouflage item text ("the blessed and
 * orderlies who survived the skirmish at the Radar"), plausibly this game's real Covenant-faction laity
 * rank, not confirmed further this part.</li>
 * <li>{@code players/} (123 files) is a rich real seasonal/event player-head-skin roster: New Year
 * ({@code newyear1}-{@code 10}, cross-referencing {@code stalkersounds/music/snegurochka2025_*.ogg}'s
 * disclosed New Year 2025 voice set), Halloween ({@code halloween1}-{@code 5}), a real 2022-2026
 * multi-year seasonal timeline ({@code spring24}/{@code summer24}/{@code autumn24}/{@code autumn25}/
 * {@code spring25}/{@code summer25}/{@code autumn26}/{@code spring26}/{@code summer26}/{@code
 * winter25}/{@code winter26}, plus {@code birthday2022}), real Twitch-drop skins ({@code twitch1}-{@code
 * 4}), real regional/diversity presets ({@code na_afram_1}, {@code na_latam_1}, {@code na_texas_1},
 * {@code nea_foreman_1}), and -- the headline finding -- 4 real faction-branded skins whose names are
 * exactly the 4 of this game's 6 real playable alliance ids this part confirmed via {@code
 * stalker/lang/en.lang} (see below): {@code duty1}, {@code freedom1}, {@code merc1}, {@code covenant1}.
 * Several more names are real, disclosed, UNCONFIRMED leads not resolved this part: {@code blitz_1},
 * {@code incident1}/{@code 2}, {@code inside1}/{@code 2} (plus a real {@code inside2_emi.ol} and two
 * {@code spring25_*_emi.ol} siblings -- "emi" here reads as a real render/shader term (an emissive-map
 * variant), NOT this project's unrelated {@code emission/EmissionCycle} blowout mechanic; flagged
 * explicitly so the coincidence isn't mistaken for a cross-reference), {@code mist2}/{@code 5}, {@code
 * mk_1}, {@code mt_1}, {@code ravent1}.</li>
 * <li>{@code unique/} (72 files, 33 real named characters) is this project's richest single
 * named-NPC-identity roster. Cross-referenced directly against {@code stalker/lang/en.lang} (part 90's
 * {@link net.stalzone.emulator.localization.LangCatalog}) using the same literal-word evidentiary bar as
 * part 90: confirmed only where a head's own real filename-derived name appears literally (allowing the
 * same kind of real English-transliteration spelling variance already accepted for part 90's exact
 * display-name matches) in a lang entry's key or its real English text. This resolves 4 of this game's 6
 * real playable-alliance leaders by name: {@code davydov} (real quote, "Davydov, Frontier Commander" --
 * exact spelling match; also reveals the real in-lore nickname for the classic-S.T.A.L.K.E.R.-styled
 * {@code duty} alliance is "the Frontier"), {@code muller} (real quote, "Muller, Mercenary Commander" --
 * exact match), {@code chatsky} (real quote, "Chatskiy, Rise Commander" for the {@code freedom} alliance
 * -- one added letter, same transliteration-variance standard as part 90's {@code TRANQUILIZER}; also
 * reveals "Rise" as freedom's real in-lore nickname, distinct from a separate, unresolved "Dawn" faction
 * name a quest document also attributes to Chatsky -- a real, disclosed naming inconsistency, not
 * resolved further this part), and {@code mitropolit} (real title "Metropolite, the head of Covenant" --
 * one letter differs plus a trailing e, same variance standard as part 90's {@code SCOUT_MINE} vs. exact
 * matches; corroborated further by a real {@code stalker.navigation} location literally named
 * "Metropolite's Room"). 5 more resolve to a real, different English display name via an achievement or
 * item entry: {@code lipa}-&gt;"Linden" (a real achievement title, {@code general.achievement.id.
 * suget_bandits_kill_Lipa.title=Lumberjack}/{@code .desc=Execute Linden.}), {@code morg}-&gt;"Morgue"
 * (real achievement title {@code Time's up, Morgue} for {@code boss_morg}), {@code orakul}-&gt;"Oracle"
 * (real achievement desc "Convince Oracle to go on a solo trip." for {@code orakul_leave}), {@code
 * raguil}-&gt;"Raguel" (real item name {@code Jumpsuit Raguel}, one letter differs), {@code
 * sglaz}-&gt;"Hex" (real item {@code murmur_sglaz.name=Hex's Armored Suit} -- confirmed via the KEY
 * containing "sglaz" literally, same rule part 90 used when a display value itself didn't match). 3 more
 * are confirmed as real named characters without a separate resolved display name: {@code knyazev} (real
 * location names "Trailblazers' Laboratory"/"Trailblazers Case" corroborate his real customitems-part-70
 * "Knyazev's expedition" as the in-lore "Trailblazers" group), {@code terentii} (a real dedicated {@code
 * stalker.quickcommands.terentii.*} voice-line/chat-macro set with its own distinct meme-flavored
 * personality), {@code voroshilov} (real quest-item lore, "improved by Voroshilov and Herschel" -- a
 * plausible, NOT confirmed, tie to this same roster's {@code gershel}, since "Herschel" and "gershel"
 * differ by more than this project's accepted transliteration variance). {@code shaman} additionally
 * cross-references the existing real {@code progression.PlayerClass.SHAMAN} (part 66/83) via a real
 * {@code blizzard.event.props.class.shaman.name=Shaman} lang entry and a real "Shaman's Den"/"Shaman's
 * side" storyline location/achievement pair. The remaining 20 names ({@code bering}, {@code doctor},
 * {@code godun} -- real but only loosely tied to the "Rise"/freedom orbit via a quest-document mention,
 * not a title, so deliberately left unconfirmed -- {@code hamzi}, {@code hippie}, {@code jeltuha}, {@code
 * kondor}, {@code korenastiy}, {@code mahorkin}, {@code manok}, {@code marxont}, {@code mongol}, {@code
 * plastinka} -- named in a real {@code quote.ziv.344} line but with no separate display-name resolution
 * -- {@code polkovnik}, {@code romeo}, {@code solynikov}, {@code trup_quest} -- Russian for "corpse
 * quest," a real, self-disclosed dev/test-artifact name rather than a proper character, matching this
 * project's established z_old_models/test-artifact pattern (parts 70, 79) -- {@code zigfrid}, {@code
 * ziv}) have NO lang-file match meeting this project's evidentiary bar and are left as real, disclosed,
 * unconfirmed leads -- not guessed.</li>
 * <li>{@code universal/} (39 files, real generic {@code universal_01}-{@code 20} presets) is a
 * plain/no-theme customization tier.</li>
 * <li>{@code zombie/} (12 files, real {@code zombie_01}-{@code 06} variants) corroborates a real
 * zombie-enemy/infection angle, cross-referencing this folder's own root {@code murmur_zombie.mcsb} and
 * {@code head_animations/zombie.mcvd}; not investigated further this part.</li>
 * </ul>
 *
 * <p>This part's {@code stalker/lang/en.lang} cross-check also directly confirmed this game's real
 * canonical 6-id playable-alliance roster for the first time: {@code stalkers} (real leader "Ataman"),
 * {@code bandits} (real leader "Saint"), {@code duty}, {@code freedom}, {@code merc}, {@code covenant} --
 * each with its own real {@code stalker.alliance.gui.description.short/full.<id>} lore text and named
 * leader quote. {@code ataman} and {@code saint} do not appear among this folder's 33 real unique-head
 * names, a real, disclosed gap (those two leaders may simply have no dedicated head skin, or one not
 * named after their title) -- not investigated further this part.
 */
public final class StalkerPlayerCatalog {

    /** Synthetic folder key for the 13 real loose files at {@code stalkerplayer/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private StalkerPlayerCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param stalkerPlayerDir the real {@code .../modassets/assets/stalkerplayer} folder. */
    public static StalkerPlayerCatalog load(Path stalkerPlayerDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(stalkerPlayerDir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    String folderName = entry.getFileName().toString();
                    List<String> files = new ArrayList<>();
                    collectFiles(entry, entry, files);
                    Collections.sort(files);
                    filesByFolder.put(folderName, files);
                } else {
                    rootFiles.add(entry.getFileName().toString());
                }
            }
        }

        if (!rootFiles.isEmpty()) {
            Collections.sort(rootFiles);
            filesByFolder.put(ROOT_KEY, rootFiles);
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(stalkerPlayerDir + ": no stalkerplayer asset folders/files found");
        }

        return new StalkerPlayerCatalog(filesByFolder);
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

    /** Real top-level stalkerplayer-asset folder names, alphabetical, incl. {@link #ROOT_KEY}. */
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

    /**
     * Real unique-character base names under {@code heads/unique/} (part 91), derived from the real
     * shipped filenames by stripping the {@code unique_} prefix, the file extension, and -- only for the
     * 6 real names that ship a second head-tone/variant file this way ({@code gershel}, {@code hippie},
     * {@code marxont}, {@code mitropolit}, {@code plastinka}, {@code sglaz}) -- a real observed {@code
     * _2}/{@code _green}/{@code _yellow} variant suffix, so each of the 33 real characters appears
     * exactly once. See the class doc for which of these 33 have a confirmed real {@code en.lang}
     * crosswalk.
     */
    public List<String> uniqueHeadNames() {
        java.util.Set<String> names = new TreeSet<>();
        for (String relPath : filesIn("heads")) {
            if (!relPath.startsWith("unique/")) {
                continue;
            }
            String fileName = relPath.substring("unique/".length());
            int dot = fileName.lastIndexOf('.');
            String base = dot >= 0 ? fileName.substring(0, dot) : fileName;
            if (base.startsWith("unique_")) {
                base = base.substring("unique_".length());
            }
            for (String suffix : new String[]{"_2", "_green", "_yellow"}) {
                if (base.endsWith(suffix)) {
                    base = base.substring(0, base.length() - suffix.length());
                    break;
                }
            }
            names.add(base);
        }
        return new ArrayList<>(names);
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
