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
 * Catalogs the real character/weapon animation assets under {@code modassets/assets/highpoly/}
 * (part 65) -- the 4 real top-level folders confirmed by direct listing, 1,194 files via this
 * class's folder-only walk, plus 3 more real files sitting loose directly in {@code highpoly/}
 * itself ({@code character.mcsb}, {@code character_hands.mcsb}, {@code wpn_fp_mp7.mcvd} -- the same
 * disclosed root-vs-walked gap this project has found in several other trees, e.g. {@code
 * particles/}'s own root, part 61): {@code animations} (395), {@code character} (472), {@code
 * firstperson} (5), {@code lipsync} (322) -- 1,197 files total. Squarely game content (character and
 * weapon animation data), not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 * This part directly resolves two of part 63's "unconfirmed lead" names into confirmed distinct
 * character types with their own real animation sets -- see {@code character/} below.
 *
 * <p><b>{@code lipsync/}</b> (322 real {@code .mcvd} files) is a clean, near-1:1 real cross-reference
 * against part 63's {@code gloomycore/banks/} named-NPC voice-line roster: the same real matched
 * Russian/{@code _en} pairs reappear here as facial lipsync data -- {@code vasilich}/{@code
 * vasilich_en}, the full real {@code verbovshik_*} ("recruiter") set ({@code grey}/{@code
 * kondor}/{@code perst}/{@code rakita}/{@code sozonka}), {@code vodovorot_sobitiy}/{@code
 * voennie_v_peshere}, {@code tutorial_3}/{@code tutorial_3_en} -- confirming these are real spoken-
 * dialogue characters with matching facial animation, not just voice-bank filenames in isolation.
 * Real names NOT seen in the part-63 banks sweep also surface here as fresh leads: {@code terentiy}/
 * {@code terentiy_en} (a real Russian first name, a fresh unconfirmed character), {@code svyatoy}/
 * {@code svyatoy_en} ("holy"/"saint", a real character nickname), {@code tochka_nevozvrata}/{@code
 * tochka_nevozvrata2} ("point of no return", a real fresh quest/event-name lead), and {@code tetris}
 * (a real, unexplained character-name lead -- disclosed as-is, not guessed at).
 *
 * <p><b>{@code character/}</b> (472 files: 7 real loose files -- {@code hitbox.mcvd}, {@code
 * origin.mcvd}/{@code origin.mcsb} (base rig), {@code originpov.mcsb} (first-person-view rig), {@code
 * ragdoll.mcsb} -- plus 5 real subfolders) is where the two part-63 "unconfirmed lead" names get
 * directly resolved. {@code animationsShnir/} (4 real files: {@code shnir_idle}/{@code
 * shnir_run}/{@code shnir_airtime}/{@code shnir_jump_cut.mcal}) and a real dedicated {@code
 * originShnir.mcsb} rig CONFIRM "shnir" (part 63's {@code colliders/shnir/} unconfirmed lead) is a
 * real, distinct character/creature type with its own locomotion set -- the real {@code
 * airtime}/{@code jump_cut} animation names plausibly point to unusual, non-standard-human movement,
 * not evidence of a specific type though. {@code animationstechnar/} (55 real files) and a real
 * dedicated {@code origintechnar.mcsb} rig similarly CONFIRM "technar" (first seen in part 63's
 * {@code colliders/steve/collider_technar.mcvd}) is a real, distinct character with a large real
 * unarmed-combat animation set, including three named special-move triplets ({@code
 * cycle}/{@code start}/{@code end} each): a real {@code hadouken_*} set (literally named after the
 * classic fighting-game fireball special move -- a striking, unconfirmed lead into a possible
 * cut/prototype special-ability character), plus {@code stolb_*} ("pillar/pole", Russian) and {@code
 * vent_*}, both real, unconfirmed special-move names. This directly corroborates part 63's own
 * self-corrected finding that {@code steve}/{@code colliders/steve/} held real cut/prototype boss
 * content (including {@code collider_forge_boss_proto.mcvd} and {@code collider_technar.mcvd}) rather
 * than a placeholder -- {@code technar} now has a confirmed, named, substantial animation set to match
 * that collider evidence. {@code animationsnpc/} (126 real files) and {@code animationszombie/} (234
 * real files, the largest subfolder here) are real generic-NPC and real "zombie" locomotion sets --
 * {@code zombie} directly corroborates this project's existing {@link ArmorModelCatalog} finding (a
 * real {@code zombie_bm} armor folder with 8 distinct numbered zombie mesh variants and gas-mask
 * variants), now with a first, complete real movement-animation set confirming it as a fully
 * AI-animated mob type, not just a cosmetic model. {@code casts/} (46 real files) names real weapon-
 * category animation layers, corroborating real melee weapon TYPES beyond guns: {@code Axe}, {@code
 * Dubina} ("club", Russian), {@code Fists}, {@code Karambit} (a real curved combat knife), {@code
 * Katana}, {@code LopataDigger} ("shovel"), alongside real {@code Pistol}/{@code Rifle} gun-layer
 * definitions.
 *
 * <p><b>{@code firstperson/}</b> (5 files, all under a real {@code base/} subfolder) is generic
 * first-person movement-state layer definitions ({@code FpBaseWalkTypeDefault}, {@code
 * FpBaseWalkTypeDefaultAiming}, {@code FpBaseWalkTypeProne}, {@code FpBaseWalkTypeProneAiming}) --
 * standard FPS locomotion states, not narrative content.
 *
 * <p><b>{@code animations/}</b> (395 files) is real first-person weapon-animation data, almost
 * entirely real {@code wpn_fp_<weapon-code>.mcvd} files -- one real weapon per file, corroborating a
 * large slice of this project's existing weapon roster from an independent asset tree (not
 * cross-checked file-by-file against {@code WeaponRegistry} this part). Two real seasonal-skin
 * variants surface directly in the filenames themselves: {@code wpn_fp_a545_newyear2023.mcvd} and
 * {@code wpn_ppk20_winter25.mcvd} -- both real, fresh corroborating data points alongside this
 * project's existing seasonal-event findings (Halloween, New Year, winter). Two real,
 * unexplained-name weapon variants, {@code wpn_sa20_rhythm.mcvd} and {@code wpn_sa20_takt.mcvd}
 * ("takt" = "beat/tempo", Russian), are disclosed as open leads rather than guessed at. {@code
 * character.mcal} and {@code fp_walkcycles.mcvd} are the 2 non-weapon real files in this folder.
 */
public final class HighpolyCatalog {

    private final Map<String, List<String>> filesByFolder;

    private HighpolyCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param highpolyDir the real {@code .../modassets/assets/highpoly} folder. */
    public static HighpolyCatalog load(Path highpolyDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(highpolyDir)) {
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
            throw new IOException(highpolyDir + ": no highpoly asset folders found");
        }

        return new HighpolyCatalog(filesByFolder);
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

    /** Real top-level highpoly-asset folder names, alphabetical. */
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
