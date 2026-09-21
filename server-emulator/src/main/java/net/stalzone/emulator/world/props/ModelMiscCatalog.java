package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the real assets under {@code modassets/assets/stalker/models/} for 9 small, unrelated
 * leaf trees swept together in part 95 (204 files total): {@code food/} (162), {@code shamanfight/}
 * (10), {@code dropped_tooltip/} (8), {@code hunter/} (6), {@code halloween/} (6), {@code undead/}
 * (4), {@code blizzard/} (4), {@code gift/} (3), {@code cloth/} (1). These are the last real
 * top-level folders under {@code models/} not already covered by a dedicated catalog -- {@code
 * armor/} (2,240 files, part 54, {@link ArmorModelCatalog}), {@code artefacts/} (553, part 55,
 * {@link ArtefactModelCatalog}), {@code backpacks/} (385, part 56, {@link BackpackModelCatalog}),
 * and {@code items/} (133, part 57, {@link ItemModelCatalog}) -- so this class, following part 93's
 * {@link StalkerMiscCatalog} whitelist-of-named-folders precedent (itself a shared-root variant of
 * part 79's {@link MiscAssetsCatalog}), closes out {@code models/} entirely: every real file under
 * {@code modassets/assets/stalker/models/} is now indexed by exactly one of these 5 catalog classes.
 * {@link #load} is given the shared {@code models/} root and walks only a fixed whitelist of 9
 * known folder names ({@link #KNOWN_FOLDERS}), each recursed fully into one flat "folder" bucket --
 * it does NOT see or re-index {@code armor/}/{@code artefacts/}/{@code backpacks/}/{@code items/}.
 * Squarely game content, not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 *
 * <p><b>{@code food/}</b> (162 files) -- by far the largest of the 9, real consumable-item props
 * named in Russian (matching this project's long-established pattern of the client's internal asset
 * names being Russian while player-facing {@code en.lang} strings are translated): {@code
 * buterb}/{@code buterw} (sandwiches), {@code chesnok} (garlic), {@code fasol}/{@code goroh}/{@code
 * grecha}/{@code psheno} (beans/peas/buckwheat/millet), {@code hleb_afro}/{@code hleb_bely} (bread
 * loaves), {@code ikra} (caviar), {@code kolbasa} (sausage), {@code konserva} (canned goods), a
 * {@code kotel_krish_*}/{@code kotel_poh_*} pair of real cook-pot prop lines (8 distinct stew/kasha
 * variants each), {@code meat_kaban} (boar meat), {@code meat_dog}, {@code meat_plot}, {@code
 * posuda} (tableware), and {@code voda} (water), among others. One real on-disk filename
 * inconsistency is disclosed rather than silently normalized: {@code meat_dog_сooked.ol} uses a
 * Cyrillic {@code с} (U+0441) in place of the Latin {@code c} in "cooked" -- a genuine mixed-script
 * typo in the game's own shipped asset name, not a transcription error introduced by this project.
 * Two real seasonal sub-trees live inside {@code food/}: a {@code blizzard/} subfolder ({@code
 * grog}, {@code vodka_brus}, {@code vodka_new}) directly ties the Blizzard winter event -- already
 * confirmed via {@code stream/}'s audio in part 94 and via {@code en.lang}'s "Frost Bones"/"Frost
 * Fighter" currency and skin lines -- to real in-world alcohol props; and a rich, previously-
 * uncatalogued {@code newyear2022/} sub-tree (bottles in 4 numbered variants plus {@code alcobull}/
 * {@code hideout}/{@code geyser} skins, 4 numbered ice-cream props, a {@code konserva} 4-can set,
 * and a {@code mandarine} prop with {@code full}/{@code gnil} ("rotten")/{@code peel}/{@code
 * peel_half} variants) -- a full real New Year 2022 seasonal food/decor prop pack this project had
 * not previously indexed by name.
 *
 * <p><b>{@code shamanfight/}</b> (10 files) -- real 3D props for the Shaman half of the linked
 * Morg/Shaman two-phase boss encounter this project's audio sweep confirmed in part 94 ({@link
 * StalkerStreamCatalog}), and already flagged (but not catalogued) as an open lead in parts 54/58's
 * Javadoc: {@code column/column.mcsb} directly corroborates the confirmed {@code
 * destroyed_1st-4th_pillar} audio cue with an actual destructible-pillar mesh; {@code shield/}
 * ({@code shield.mcsb}/{@code .mcvd}, plus a {@code perlin.mic} noise texture) corroborates the
 * confirmed {@code shield_broke_a}-{@code e} audio cues with a real shield prop; {@code flames/}
 * corroborates the confirmed {@code burning_threat_a/b/c} audio cues. {@code phantom_proj/} and
 * {@code phantom_zone/} are two real props this project had not previously disclosed for this
 * fight -- a projectile and an area-effect zone, both literally scoped inside {@code shamanfight/}
 * (the same direct-folder-co-location evidentiary standard this project used to confirm the
 * Morg/Shaman link itself). {@code en.lang} separately has a real, unrelated "Phantom" mob/location
 * namespace at Limansk ({@code pda.waypoint.we.limansk_chimera_boss.title=Phantom Fiend Den}), but
 * no filename or log line directly equates that namespace to these two shamanfight props, so any
 * tie between them stays an explicitly disclosed, unconfirmed possibility, not a claim.
 *
 * <p><b>{@code dropped_tooltip/}</b> (8 files) -- a single {@code stick} marker prop with 6 real
 * named tier-skin variants: {@code stick_LEGEND}, {@code stick_MASTER}, {@code stick_NEWBIE}, {@code
 * stick_PICKLOCK}, {@code stick_STALKER}, {@code stick_VETERAN}. CONFIRMED via {@code en.lang}
 * against two separate real tier systems that between them name all 6: the item/equipment
 * {@code item_tier.*} namespace ({@code item_tier.novice.name=Newbie}, {@code .master.name=Master},
 * {@code .stalker.name=Stalker}, {@code .veteran.name=Veteran}) supplies 4, and the faction
 * reputation-tier namespace supplies the other 2 ({@code cnps.faction.settlement_stalkers_csky.
 * relation.1=Picklock}, {@code cnps.faction.settlement_wnorth.relation.10="Legend of the Zone"}) --
 * this stick prop is plausibly a shared 6-tier rank-badge marker shown above a dropped item or
 * player, reusing both namespaces' tier names as its skin set.
 *
 * <p><b>{@code hunter/}</b> (6 files) and <b>{@code undead/}</b> (4 files) -- both hold a {@code
 * hook/} subfolder with the same real {@code hook.mcsb}/{@code hook_diff.ol}/{@code hook_nrm.ol}/
 * {@code hook_spek.ol} four-file set (identical filenames, so plausibly a literal shared/duplicated
 * asset rather than two coincidentally-identical models). CONFIRMED via {@code en.lang} as a real
 * "Grappling Hook" traversal tool ({@code gui.winter26_event.industry.hook.title=Grappling Hook},
 * {@code gui.winter26_event.industry.hook=Helps you quickly move across terrain by using the
 * environment}), tied to a real "Frost Troopers event" achievement pair ({@code general.
 * achievement.id.dmz_2026_batman.desc=Upgrade the grappling hook to the maximum level within the
 * Frost Troopers event}, {@code .dmz_2026_matrix.desc=Avoid a tactical support in mid-air through a
 * grappling hook and kill a player within the Frost Troopers event}) -- i.e. this is a real
 * DMZ-mode/winter-2026-event mechanic, not evidence that a folder literally named "hunter" is itself
 * a mode called "Hunter" (a distinct real "Hunter" name also exists in {@code en.lang}, as a
 * Blizzard-event prop class and as a shotgun-focused player class, but no file or log ties either of
 * those to this {@code hook} model, so that reading is explicitly NOT made here). The {@code hunter/}
 * copy alone carries two real on-disk duplicate files with a literal Cyrillic {@code — копия}
 * ("— copy") suffix ({@code hook_diff — копия.ol}, {@code hook_spek — копия.ol}) -- disclosed as a
 * real leftover dev-workflow artifact, not investigated further. The plain {@code hook} model being
 * reused under {@code undead/} plausibly corroborates part 94's confirmed "Undead" mode with a real
 * grappling-hook traversal mechanic, though (same standard) no direct string ties the two beyond
 * this shared filename set.
 *
 * <p><b>{@code halloween/}</b> (6 files) -- a single {@code goner} character prop in 3 real size
 * variants ({@code big}/{@code medium}/{@code small} {@code .mcsb} meshes) plus a shared {@code
 * diff}/{@code emi}/{@code nrm} texture set. {@code en.lang} has many casual uses of the English
 * idiom "goner" ("I'm a goner, dudes!") but no proper-noun {@code goner.name}/{@code .title} entry,
 * so this stays a real, disclosed, unconfirmed lead: a Halloween-event prop plausibly named for the
 * idiom, not a confirmed enemy/character name.
 *
 * <p><b>{@code blizzard/}</b> (4 files) -- a single {@code frozen} prop ({@code frozen.mcsb} plus
 * {@code diff}/{@code nrm}/{@code spek} textures). CONFIRMED via {@code en.lang} as tied to the real
 * Blizzard winter seasonal event: {@code currency.type.id.blizzard_23.name=Frozen Bones}, {@code
 * item.crt.frozenbones.name=Frozen Bones} ("Processed frost mutant bones have become a popular
 * currency among Frost Fighters"), alongside a real "Frost Fighter Skin" cosmetic line across the
 * {@code blizzard_2021}/{@code blizzard_2022}/{@code blizzard_2023} lootbox rosters.
 *
 * <p><b>{@code gift/}</b> (3 files, a generic {@code gift_box} prop with 2 diffuse variants) and
 * <b>{@code cloth/}</b> (1 file, {@code cloth_hp.mcvd}, a single cloth-physics/vertex-deformation
 * asset) -- both real but generic; neither has an {@code en.lang} match specific enough to name, and
 * neither is investigated further.
 *
 * <p>Pure cataloguing, like parts 54-57 and 93 -- no new {@code GameLoop}/{@code SingleplayerServer}
 * wiring, so no live {@code Main}-entry-point run was needed. This closes out {@code models/}
 * entirely; {@code stalker/}'s 2 other remaining large top-level trees ({@code sound/} 10,500 files,
 * {@code textures/} 4,066) remain the largest real open leads for a future part.
 */
public final class ModelMiscCatalog {

    /** The fixed whitelist of small leaf-tree folder names this class exclusively loads. */
    public static final List<String> KNOWN_FOLDERS = List.of(
            "food", "shamanfight", "dropped_tooltip", "hunter", "halloween", "undead", "blizzard",
            "gift", "cloth");

    private final Map<String, List<String>> filesByFolder;

    private ModelMiscCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param modelsRoot the real {@code .../modassets/assets/stalker/models} folder (the shared parent, not a dedicated one). */
    public static ModelMiscCatalog load(Path modelsRoot) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        for (String folderName : KNOWN_FOLDERS) {
            Path folderRoot = modelsRoot.resolve(folderName);
            if (!Files.isDirectory(folderRoot)) {
                continue;
            }
            List<String> files = new ArrayList<>();
            collectFiles(folderRoot, folderRoot, files);
            Collections.sort(files);
            if (!files.isEmpty()) {
                filesByFolder.put(folderName, files);
            }
        }

        if (filesByFolder.isEmpty()) {
            throw new IOException(modelsRoot + ": none of the known small model folders were found");
        }

        return new ModelMiscCatalog(filesByFolder);
    }

    private static void collectFiles(Path folderRoot, Path dir, List<String> out) throws IOException {
        try (var entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    collectFiles(folderRoot, entry, out);
                } else {
                    out.add(folderRoot.relativize(entry).toString().replace('\\', '/'));
                }
            }
        }
    }

    /** Real known-small-tree folder names actually found under the given root, alphabetical. */
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
