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
 * Catalogs the real assets under {@code modassets/assets/gloomycore/} (part 63) -- the 9 real
 * top-level folders confirmed by direct listing: {@code banks} (474 files), {@code colliders} (56
 * files), {@code fonts} (60 files), {@code handbook} (43 files), {@code lang} (5 files), {@code
 * models} (8 files), {@code sky} (101 files), {@code sound} (104 files), and {@code textures} (5
 * files) -- 856 files total across these 9 folders. Same folder-only walk as {@link
 * EffectsAssetCatalog} and {@link EffectsParticleCatalog}: this does NOT see the 7 real loose files
 * sitting directly in {@code gloomycore/} itself ({@code RU_ru.txt}, {@code egs_dlc_ids.txt}, {@code
 * steam_dlc_ids.txt}, {@code mcmtlList.txt}, plus {@code advices_en.txt}/{@code advices_ru.txt}/
 * {@code item_tiers.txt}, the last three already covered by {@link
 * net.stalzone.emulator.tips.AdviceList} and {@link net.stalzone.emulator.items.ItemTiers} from
 * their real source location here) -- see the class doc's regression test for that behavior. {@code
 * gloomycore/} is squarely game-engine-and-content data (mob physics colliders, HUD fonts, PDA
 * handbook images, sky/weather presets, UI sound effects) plus a small amount of pure engine
 * machinery (Maya tool-cache swatches, FMOD bank binaries) disclosed as such below -- not an EXENS
 * anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code banks/}</b> (474 files, mostly {@code .bank}) are FMOD Studio sound banks -- opaque
 * compiled binary containers this project cannot parse without the FMOD SDK, so only real filenames
 * are catalogued as asset-name evidence, not their audio contents. Two real master banks ({@code
 * Master.bank}, {@code Master.strings.bank}) plus single-letter banks ({@code A.bank}..{@code
 * K.bank}, likely a per-zone/per-area ambience split) and {@code abilities.bank}/{@code
 * worldEvents.bank} are generic engine structure. The bulk are real named-NPC voice-line banks in
 * matched pairs -- a Russian-audio file and an {@code _en} English-dub sibling (112 real {@code
 * _en.bank} files confirmed) -- e.g. {@code 2_ahav}/{@code 2_ahav_en}, {@code 2_bashlakov}, {@code
 * 2_chatskiy}, {@code 2_davydov}, {@code 2_glaz}, {@code 2_gudron}, {@code 2_jeltuha}, {@code
 * 2_kashin}, {@code vasilich}, {@code visokii}, {@code torf} -- real named-character evidence, most
 * not yet cross-referenced against any other catalogued source in this project, disclosed as fresh
 * leads. A real {@code agitatsii_*} ("agitation"/propaganda-speech) subset names several more real
 * characters ({@code afon}, {@code arhont}, {@code baldezhnik}, {@code burger}, {@code chatskii},
 * {@code davidov}, {@code eremey}) and a real {@code verbovshik_*} ("recruiter") subset names four
 * more ({@code sozonka}, {@code rakita}, {@code perst}, {@code kondor}, {@code grey}) -- both
 * plausibly faction-recruitment dialogue, unconfirmed beyond the filenames themselves. Real
 * event/quest-flavored names {@code vodovorot_sobitiy} ("whirlwind of events") and {@code
 * voennie_v_peshere} ("military in the cave") are disclosed as fresh, unconfirmed quest/event leads.
 * {@code tutorial_3}/{@code tutorial_3_en} corroborate a tutorial sequence.
 *
 * <p><b>{@code colliders/}</b> (56 real {@code .mcvd} files across 38 real named subfolders) is real
 * mob hit-collision geometry, directly corroborating this project's existing {@link
 * net.stalzone.emulator.mobs.MobRegistry} roster (35 real {@code *.smm} configs, docs/RESEARCH_LOG.md
 * parts 10/20): {@code boar}, {@code cat}, {@code chimera}, {@code cybergiant}, {@code dog}, {@code
 * doge}, {@code drone}, {@code flesh}, {@code fpv}, {@code krovosos}, {@code lurker}, {@code
 * pseudodog}, {@code pseudogigant}, {@code psidog}, {@code puppet}, {@code rudolf} (confirmed also
 * present as a real {@code base_rudolf.smm} config), {@code snork}, {@code spirit}, {@code
 * thief_rat}, {@code tick}, {@code tsarevich}, {@code tsarevna}, {@code tumbler}, {@code tushkan},
 * {@code wisp}, several with a real {@code _clone} sibling folder. 37 of these 38 folders hold
 * exactly one real, generically-named {@code collider.mcvd} file each (directly confirmed by listing
 * every folder's contents -- NOT a per-mob-named file, an earlier assumption this Javadoc corrected
 * before finalizing). Two real folder names do NOT match any of the 35 real {@code .smm} configs
 * directly checked against the live {@code stalkermobs/configs/} folder: {@code shnir} (a single
 * generic {@code collider.mcvd}, same shape as the 35 confirmed mobs -- an unconfirmed lead, plausibly
 * a human NPC's own collision tuning rather than a "mutant" config, since {@link
 * net.stalzone.emulator.mobs.MobRegistry} only covers that one config family) and {@code steve} --
 * which, directly checked, is NOT a placeholder: it alone holds 19 of the 56 real files, with names
 * far richer than any other folder here: {@code collider_forge_boss_proto.mcvd} (real evidence of a
 * boss prototype named "Forge", not otherwise catalogued anywhere in this project so far), {@code
 * collider_technar.mcvd} (a real, unconfirmed character/unit name), {@code
 * collider_test_apostle_percentdamagespot.mcvd} and {@code collider_test_percentdamagespot.mcvd} (a
 * real "percent damage spot" mechanic under active test, tied to a real unconfirmed "apostle" name),
 * plus real {@code heavy}/{@code medium}/{@code light} weight-class variants and a real {@code
 * murmur}-tagged set carrying real 5-digit numeric IDs ({@code 25120}-{@code 25129}, each with a
 * {@code _1}/{@code _2} pair). This reads as a real developer prototyping/test rig reused across
 * several in-development features rather than a single placeholder -- disclosed plainly as a fresh,
 * unconfirmed lead into possible cut/prototype boss content (this project's rule 2 scope), not turned
 * into a spec. <b>Naming context (user firsthand account, 2026-09-04, labeled separately from
 * file-based evidence per rule 1 -- not independently verified against client files):</b> the user
 * played this game before its first season and reports it was originally built on/derived from
 * Minecraft, still using literal Minecraft running and parkour-mod animations at that time (changed
 * since). "Steve" is Minecraft's own iconic default player-character name, so this real folder name
 * is plausibly a direct holdover default/test-rig name from that heritage, not a coincidence or an
 * in-house placeholder invented for this project. This also plausibly explains the {@code mc}-
 * prefixed custom file extensions already catalogued in this project -- {@code .mcvd} (this folder),
 * {@code .mcsb} (real model files, see {@code models/} below and other catalogs), {@code .mcmtl}
 * (the real material-file extension named in {@code mcmtlList.txt}, see the class doc's open-leads
 * note) -- as plausibly standing for "MC" (Minecraft), consistent with an engine that started as a
 * Minecraft derivative. Disclosed as a plausible, user-sourced explanation, not confirmed by any
 * client file or documented public source this project has directly checked.
 *
 * <p><b>{@code fonts/}</b> (60 files) is a real UI font asset set: {@code Golos_Text_Regular}/{@code
 * Golos_Text_Italic} (Cyrillic-capable), {@code FuturaPT-Medium}, {@code Montserrat-Bold}, {@code
 * Tahoma}/{@code Tahoma_Bold}, {@code Capture_it}, {@code Europe-Book-Edited}, {@code murmur.otf}, a
 * real {@code SourceHanSansHW-VF}/{@code SourceHanSansHW-VFImGui} pair (Han/CJK glyph coverage,
 * corroborating this project's existing Korean-localization findings, {@link
 * net.stalzone.emulator.world.props.KoreaJurisdictionCatalog}), a real {@code braille.ttf}
 * (accessibility font), and real Font Awesome icon fonts ({@code fa-regular-400}, {@code
 * fa-solid-900}). {@code dict.txt}/{@code dict-ranges.txt} are real glyph-range definition files for
 * dynamic font-atlas generation -- engine tooling, not narrative content. The other 44 real files sit
 * in 4 real precomputed-rendering-cache subfolders mirroring the same font set in different GPU text
 * rendering formats: {@code msdf/} (11 files, multi-channel signed-distance-field atlases), {@code
 * sdf/} (2 files, plain signed-distance-field), {@code slug/} (13 files, the third-party "Slug"
 * vector font-rendering library's own format), and {@code ttf/} (18 files, pre-baked bitmap font
 * atlases at fixed sizes/scales, e.g. {@code capture_it_16_x2.0.bfnt}) -- all engine rendering
 * machinery, not narrative content.
 *
 * <p><b>{@code handbook/}</b> (43 files, all under {@code img/}) are real PDA in-game handbook/
 * knowledge-base images, split across three real subfolders: {@code anomalies}, {@code guide}, and
 * {@code locations} -- corroborating a documented in-game encyclopedia/codex system covering those
 * three topics.
 *
 * <p><b>{@code lang/}</b> (5 real {@code .lang} files: {@code en}, {@code es}, {@code fr}, {@code
 * ko}, {@code ru}) is this engine-core package's own localization set, matching the same 5-locale
 * scope seen elsewhere in this project (and again corroborating the Korean-localization finding
 * above).
 *
 * <p><b>{@code models/}</b> (8 files) is real level-design marker geometry, not visible game content:
 * {@code wepoint/} (real AI waypoint markers: {@code wepoint.mcsb}, {@code aipoint.ol}, {@code
 * wb.ol}), {@code spawn_spot/} (real spawn-marker geometry: {@code spawn_spot.mcsb}, {@code
 * material_spot.mcsb}), and {@code jump_spot/} (a real AI jump-navigation marker: {@code
 * jump_spot.mcsb}, {@code jump_spot.ol}). {@code .mayaSwatches/steve.dds_hcm.swatch} is a real Maya
 * material-editor swatch-cache artifact (tool byproduct, not game content) -- notably reusing the
 * same "steve" name as the real 19-file prototype/test collider set flagged under {@code colliders/}
 * above, consistent with "steve" being a real, reused internal rig/asset name rather than a
 * one-off coincidence.
 *
 * <p><b>{@code sky/}</b> (101 files) is real sky/weather-preset data across 7 real subfolders: {@code
 * skybox}, {@code hdri}, {@code black}, {@code world0}, a real {@code fake_ejection} preset
 * (corroborating this project's existing anomalous-emission/"psi ejection" findings, part 62's {@link
 * EffectsAssetCatalog}), a real seasonal-event preset {@code halloween2022} (direct evidence of a
 * real past in-game Halloween event), and a real {@code mogilnik} preset -- "mogilnik" is a real
 * Russian word for "graveyard/burial ground", a plausible STALKER-verse location name, disclosed as
 * an unconfirmed fresh lead. Two real loose files ({@code terrain_depth.ol}, {@code terrain_mask.ol})
 * sit directly under {@code sky/} alongside the 7 subfolders.
 *
 * <p><b>{@code sound/}</b> (104 files) is real UI/status sound effects: 18 real loose files including
 * {@code click.ogg}/{@code click_insert.ogg} (generic UI clicks) and a real {@code deaf1.ogg}..{@code
 * deaf15.ogg} set (15 files -- a real tinnitus/flashbang-deafness ringing effect pool, corroborating
 * a documented sound-based disorientation status effect), plus a real {@code damage/} subfolder (86
 * files, a real {@code death1.ogg}..{@code death25.ogg}-style death-sound-effect pool, non-contiguous
 * numbering as directly observed -- e.g. {@code death4}/{@code death5}/{@code death9}/{@code death10}
 * are genuinely absent from the real file set, not a cataloguing gap).
 *
 * <p><b>{@code textures/}</b> (5 files, all under {@code gui/}) are real core HUD/UI texture atlases:
 * {@code buttons.mic}, {@code daily_reward.mic}, {@code hud_cooldown.mic}, {@code inventory.mic},
 * {@code widgets.mic}.
 *
 * <p>Two real loose root files under {@code gloomycore/} are substantial enough to need their own
 * dedicated future part rather than a cursory mention here, and are deliberately left unparsed by
 * this class: {@code mcmtlList.txt} (10,345 real lines -- an engine-wide manifest of every {@code
 * .mcmtl} material file path across the whole {@code modassets/assets/} tree, reaching well beyond
 * {@code gloomycore/} itself into e.g. {@code anomalies/}, {@code currency/}, {@code customitems/})
 * and {@code RU_ru.txt} (340 real lines of Russian localization for the level EDITOR's own UI --
 * real internal field/label names like {@code editor.entity.projectile.damage}, {@code
 * editor.ai.skill.animationName}, {@code editor.entity.attackProjectile.collisionSize} -- genuine
 * evidence of the underlying entity/component field names this project's own server-side model
 * should eventually mirror, not player-facing content). Both are flagged here as open leads rather
 * than silently skipped.
 */
public final class GloomycoreCatalog {

    private final Map<String, List<String>> filesByFolder;

    private GloomycoreCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param gloomycoreDir the real {@code .../modassets/assets/gloomycore} folder. */
    public static GloomycoreCatalog load(Path gloomycoreDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(gloomycoreDir)) {
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
            throw new IOException(gloomycoreDir + ": no gloomycore asset folders found");
        }

        return new GloomycoreCatalog(filesByFolder);
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

    /** Real top-level gloomycore-asset folder names, alphabetical. */
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
