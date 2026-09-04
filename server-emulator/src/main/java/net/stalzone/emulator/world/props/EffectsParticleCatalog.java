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
 * Catalogs the real VFX/particle-effect assets under {@code modassets/assets/effects/particles/}
 * (part 61) -- 48 real top-level FOLDERS, confirmed by direct directory listing, holding 829 real
 * files that this class's folder-only walk (same generic, no-hardcoding design as {@code
 * ArmorModelCatalog}/{@code ArtefactModelCatalog}/{@code BackpackModelCatalog}/{@code
 * ItemModelCatalog}) actually reaches -- plus 2 more real files sitting loose directly in the {@code
 * particles/} root, outside any subfolder ({@code 1.efkmat}, {@code ground_line_default.efkmat}), the
 * same class of disclosed root-vs-walked gap parts 55/57 found in other trees. This is squarely game
 * CONTENT -- visual effects tied to anomalies, artefacts, weapons, bosses, seasonal events, and
 * gameplay mechanics -- not EXBO anti-cheat/launcher internals, so it stays within this project's
 * standing focus (rule 2). The sibling {@code effects/models}/{@code shaders}/{@code textures}
 * folders (11/1/48 files) are NOT covered by this sweep and remain open for a future part.
 *
 * <p><b>{@code prestige/}</b> (10 files) is the richest evidence yet for this project's recurring,
 * still-unconfirmed "prestige" cross-reference (a fourth independent hit, after part 57's {@code
 * aptechka_color_prestige}, part 58's {@code token_prestige}, and part 60's {@code
 * stream/video/prestige.ogg}) -- and, unlike the first three, these are real ABILITY/MECHANIC names,
 * not just a label on an unrelated asset: {@code charger_enabled}/{@code
 * charger_enabled_distortion}, {@code scanner_installation_zone}/{@code scanner_zone}, {@code
 * scout_stun}/{@code scout_stun_post}, {@code shield_depth_pulse}, {@code support_guard}, {@code
 * tank_explosion_sphere}/{@code tank_explosion_zone} -- the real "scout"/"tank"/"support" tokens read
 * as class or loadout-role names, plausibly (not confirmed) a role-based extraction-mode ability
 * system gated behind this "prestige" mechanic. A real, separate {@code
 * prestige_prototype_effects/} (1 file, {@code laserpointer.efkmat}) uses the literal word
 * "prototype" in its folder name -- an initial reading of this as an isolated cut/unreleased-content
 * lead was checked live and disproved: a live {@code foldersContaining("laserpointer")} check found a
 * SECOND, identically-named real file, {@code weapons/stratg_exp/laserpointer.efkmat}, so this is
 * disclosed instead as a real duplicate -- a normal weapon-attachment laser-sight effect that also has
 * a same-named copy filed under the "prestige prototype" folder, not necessarily unreleased content of
 * its own. Whether the two files are identical, and why the ordinary weapons-tree effect has a second
 * copy here, is NOT evidenced by the file listing alone.
 *
 * <p><b>{@code shamanfight_fx/}</b> (12 files) and {@code morgfight_fx/}</b> (2 files) are real VFX
 * for the Shaman/Morg boss encounter already corroborated across parts 55 (the {@code shamanfight}
 * artefact folder) and 59 ({@code morg_shaman_fight}/{@code morg_shaman_meet} cutscenes) -- now with
 * actual arena effects: a real {@code arena_boss_bg}, SIX real colored {@code
 * shamanfight_field_zone_<color>} effects ({@code blue}/{@code green}/{@code orange}/{@code
 * pink}/{@code red}/{@code yellow}), plus atmosphere ({@code clouds}, {@code clouds2}, {@code
 * dust_particles}, {@code glow1}, {@code floor_t}). {@code morgfight_fx/} holds {@code
 * morg_celling.efkmodel} (a real on-disk spelling of "ceiling", the same class of in-game spelling
 * quirk this project has disclosed before) and {@code morg_floor.efkmodel}.
 *
 * <p><b>{@code electrostorm_mutants/}</b> (7 files) and {@code electrostorm_npc/}</b> (2 files) give
 * this project its first real evidence of SPECIFIC mutant names tied to the "electrostorm" event
 * already known only as a currency/token (part 58's {@code token_electroshtorm2026}): real spawner
 * effects for {@code boar}, {@code burelom} ("windfall"/fallen-timber, Russian), {@code chimera},
 * {@code flesh}, and {@code krovosos} ("bloodsucker", Russian -- the classic S.T.A.L.K.E.R.-series
 * mutant). {@code electrostorm_npc/} holds a real NPC {@code shield} effect for the same event.
 *
 * <p><b>{@code operations/}</b> (41 files) gives this project its first real evidence that "Electra"
 * -- previously known only as part 55's classic {@code electra/} artefact-model family -- is an
 * actual BOSS encounter: real {@code boss_electra} and {@code electra_pillar} effects. The same
 * folder holds real raid/operation-mode content: {@code cargo}, {@code gas_ui_mat}, {@code
 * ground_borders}, {@code heal_cross}, {@code operation_ship}, {@code operations_dome_shield}.
 *
 * <p><b>{@code anomalies/}</b> (70 files) directly corroborates FOUR of part 55's classic artefact
 * family names as real ANOMALY types, not just artefact-folder labels: {@code electra_n} (also see
 * {@code operations/} above), {@code kisel}, {@code voronka_n}, and {@code carousel_n} -- the last a
 * real English spelling next to part 55's transliterated {@code karusel}, a genuine cross-tree
 * spelling variant, the same class of on-disk inconsistency this project has disclosed before (part
 * 54's {@code dr_zivkas}/{@code dr_zivcas}, part 55's {@code krisstal}/{@code kristal}). Other real
 * anomaly names: {@code batut_n} ("trampoline", Russian), {@code circus}, {@code gubilnik}, {@code
 * lighter_n} (plausibly related to part 60's Korea-jurisdiction "lighter" bolt item, unconfirmed),
 * {@code oboch_anomaly}, {@code portal}/{@code portal_inside}, {@code proto} (this project's
 * recurring "proto"-prefix naming habit, parts 55/58), {@code psiop}, {@code slizen} ("slug", Russian
 * -- plausibly a mutant, unconfirmed), and {@code tesla}.
 *
 * <p><b>{@code artefacts/}</b> (36 files) holds {@code buran26} -- the first real "26"-dated ARTEFACT
 * name this project has found (extending the dated-content pattern from parts 55/56/58/60, which
 * topped out at "26"-suffixed seasons and currencies, into artefacts themselves), {@code jele}
 * (corroborating part 55's {@code n_art/collab_art/jele} leaf folder), {@code linza} ("lens",
 * Russian), {@code petlya} ("loop"/"noose", Russian), and a separate {@code inside} leaf (see the
 * root-level {@code inside/} folder below).
 *
 * <p>A real, WIDE-spanning dated-seasonal-content pattern recurs across many independent subtrees in
 * this one sweep: {@code hw23/} (Halloween '23, 3 files), {@code leto23/} ("summer" '23, 5 numbered
 * {@code wall_*} effects), {@code buran23/} ("blizzard" '23, 2 files -- a real, differently-scoped
 * "buran" than {@code artefacts/buran26} above), {@code voron2024/} ("raven" '24, 9 files), real
 * Battle-Pass-'24-prefixed melee/weapon skins ({@code melee/bp24_antiterror}, {@code
 * melee/bp24_baton}, {@code weapons/bp24_deagle}), {@code winter25/} (1 file), and {@code
 * caseopen/}'s real {@code case_autumn}, {@code case_ny25_inside}, and {@code
 * efk_autumn24_tickets_big} -- together the widest real date-range (2023-2026) and the largest
 * number of independent dated subtrees this project has found in a single sweep.
 *
 * <p><b>{@code iznanka_iv/}</b> (125 files, the single largest folder in this tree) -- "iznanka" is
 * Russian for "the other side"/"inside-out" -- holds a real {@code HW_portal.efkmat} (tying it to a
 * Halloween event), plus {@code barrier}, {@code bulat}, and {@code cubes_mat} materials; the real
 * {@code _iv} suffix (Roman numeral four) plausibly marks a fourth iteration of a recurring event,
 * unconfirmed. A second, smaller real folder shares the same {@code _iv} suffix -- {@code ves_iv/}
 * (22 files): a real ice-themed arena ({@code ice_arena}, 5 numbered {@code
 * ice_block_Collection_<n>} effects) -- disclosed as a real, possibly-related recurring
 * numbered-event naming pattern, NOT confirmed as the same series as {@code iznanka_iv}.
 *
 * <p><b>{@code caseopen/}</b> (15 files) is real monetization VFX -- loot-case-opening effects
 * ({@code case_larec_dust} -- "larec" is Russian for "casket"/"small box" -- {@code
 * case_light_dust}/{@code _inside}, {@code case_light_open}/{@code _v2}) corroborating part 58's
 * currency/monetization findings with real UI/VFX evidence for the case-opening mechanic itself.
 *
 * <p>Other real, disclosed finds: {@code sj_boss/} (1 file, {@code pfx_stonedebris1_boss.ol}) and
 * {@code cybergiant/} (1 file, {@code testmat.efkmat}) are two more real boss/mutant names with only
 * a single, minimal VFX file each so far -- plausibly early-development or lightly-used content,
 * unconfirmed. {@code monolit_antena/} (3 files: {@code monolit.efkmodel}, {@code
 * monolit_antena.efkmat}, {@code Noise120.mic}) is real VFX for the "Monolit" faction (parts 36/54's
 * disclosed, still-hedged "Monolit -> Murmur" public-wiki rename reading), the first VFX-level
 * evidence for that faction in this project. {@code scaries/} (4 files) holds a real {@code crow},
 * real {@code lenin_eye}/{@code lenin_eye_floor} effects (evocative Soviet-era imagery, unconfirmed
 * in-game context), and {@code scaries_grdbasefire4_fwd}. {@code sermistor/} (11 files: a real {@code
 * arena}, an {@code el_radius} effect, three numbered {@code tubes_<n>}, and a spark effect) is an
 * unresolved name, plausibly a boss/hazard arena, not confirmed by any other evidence in this
 * project. {@code placeholder/} (3 files, {@code zone1}-{@code zone3}) is a real, disclosed dev
 * placeholder. {@code tutorial_eff/} (3 files: {@code ground_spawner}, {@code smoke_room_spawn},
 * {@code wall_Spawn1}) corroborates part 59's tutorial cutscene sequence with real gameplay VFX.
 * {@code events/reihstag_tanks} is a sixth real Reichstag/RKKA WWII-event cross-reference in this
 * project (after armor/backpacks/meds/currency/stalkerguide), spelled "reihstag" here -- a real,
 * disclosed cross-tree spelling variant of "reichstag" used everywhere else. {@code grenades/} (15
 * files) holds real special grenade types: {@code black_hole}, {@code efk_raidal_grenade}, {@code
 * low_gravity}, {@code n_aoe}. {@code melee/} (15 files) also holds a real named knife, {@code
 * knife_crowbeak}, alongside its dated Halloween-'23 katana effects noted above.
 *
 * <p>A real, disclosed naming-case inconsistency: {@code Texture/} (141 files, the second-largest
 * folder) and {@code NewVFX/} (4 files) are capitalized, unlike all 46 other real top-level folder
 * names in this tree, which are lowercase -- the same class of on-disk inconsistency already
 * disclosed in parts 54/55.
 */
public final class EffectsParticleCatalog {

    private final Map<String, List<String>> filesByFolder;

    private EffectsParticleCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param particlesDir the real {@code .../effects/particles} folder. */
    public static EffectsParticleCatalog load(Path particlesDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(particlesDir)) {
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
            throw new IOException(particlesDir + ": no particle-effect folders found");
        }

        return new EffectsParticleCatalog(filesByFolder);
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

    /** Real top-level particle-effect folder names, alphabetical. */
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
