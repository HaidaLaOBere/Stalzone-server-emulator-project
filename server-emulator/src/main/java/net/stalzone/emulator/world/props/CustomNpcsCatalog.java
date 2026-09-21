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
 * Catalogs the real assets under {@code modassets/assets/customnpcs/} (part 68) -- 7,661 files across
 * 6 real top-level folders ({@code anim} 14, {@code models} 3, {@code music} 7,310, {@code sound} 1,
 * {@code streamrandom} 18, {@code textures} 314) plus 1 real loose root file ({@code
 * sorting_map.txt}). Squarely game content (NPC dialogue/faction voice-bark data, boss animation
 * sets, character models), not an EXENS anti-cheat/launcher asset, so stays in scope under rule 2.
 * This part turned up this project's strongest DIRECT confirmation yet of a cut/prototype boss
 * (a developer's own filename comment, not inference from evidence), and a second real
 * Minecraft-mob-name echo alongside part 63's "steve".
 *
 * <p><b>{@code anim/}</b> (14 files, all loose) is mostly generic NPC animation-layer definitions
 * ({@code npc_anim.mcal}, {@code npc_generic.mcal}, {@code npc_bar_anim.mcal}, {@code
 * dialog_anim.mcal}, {@code menu_anim.mcal}, {@code revposes.mcal}), but two real files are
 * significant, fresh finds: {@code npc_sanitar_anomaly_death_scene_0.mcal} -- a real, specific scripted
 * death-scene animation for a "sanitar" ("orderly/medic", Russian, also a well-known real S.T.A.L.K.E.R.-
 * series faction name) NPC in an anomaly context -- and {@code
 * technar_anims(только для босса
 * технаря).mcal}, whose parenthetical is a real, literal Russian
 * developer comment left directly in the filename: "только для
 * босса технаря" translates to "only for the
 * technar boss". This is this project's STRONGEST direct confirmation yet that "technar" (first seen in
 * part 63's {@code colliders/steve/collider_technar.mcvd}, given a full animation set in part 65) is a
 * real boss -- not inferred from collider/animation circumstantial evidence as in parts 63/65, but
 * stated outright by the developers themselves in their own file naming. {@code
 * character_thrasher.mcal} names a fresh, unconfirmed character/boss lead, "Thrasher".
 *
 * <p><b>{@code models/}</b> (3 files, all loose) is small but has a second real Minecraft-mob-name
 * echo alongside part 63's "steve": {@code slime_model.mcsb} -- "Slime" is a real, iconic Minecraft
 * mob name, directly corroborating the user's own firsthand account (part 63's addendum) that STALZONE
 * was originally built on or with Minecraft-derived tooling, from a second independent angle beyond
 * part 67's real "Connected Textures Mod" config-format evidence. {@code jahik.ol}/{@code jahikarm.ol}
 * are a real, unexplained fresh name lead ("jahik", with a separate "arm" texture variant, suggesting
 * a distinct character with its own limb/prop texture).
 *
 * <p><b>{@code music/steven/sounds/}</b> (7,310 of this folder's files, the overwhelming majority of
 * this entire tree) is a large, well-organized NPC combat/social voice-bark library, split into two
 * real subfolders. {@code adept/} (42 files) is a real, self-contained named-creature voice set
 * (attack/growl/death/hit) for a "Adept" enemy type, not previously catalogued. {@code npc/} (7,268
 * files) is organized into 24 real named faction/group subfolders, most sharing a consistent real
 * 27-category voice-bark template ({@code after_fight_idle}, {@code after_fight_relax}, {@code
 * attack}, {@code death}, 8 real {@code fight_idle_*} tactical-callout categories -- {@code
 * allydeath}/{@code allyhit}/{@code cover}/{@code fire}/{@code flank}/{@code grenade}/{@code hit}/
 * {@code spoted}/{@code throw} -- {@code hit}, {@code idle}, {@code joke_reaction_bad/good}, {@code
 * kill}, {@code med}/{@code med_request}, {@code meet_hello}/{@code meet_hide_weapon}, {@code
 * music_reaction_bad/good} -- a real, fresh mechanic lead: NPCs apparently react differently to
 * in-game background/radio music -- {@code music_start}, {@code search_enemy}, {@code sleep_idle},
 * {@code thks}). The real faction/group roster itself is this project's richest NPC-faction find to
 * date, including THREE names directly matching well-known real S.T.A.L.K.E.R.-series faction names --
 * {@code dolg} ("Долг" = "Duty"), {@code freedom}, {@code monolit} ("Monolith") --
 * alongside {@code bandits}, a real named bandit-leader variant {@code bandits_2(chihachev)}
 * ("Chihachev", a real Russian surname left in a parenthetical dev note exactly like the technar-boss
 * case above), {@code military}/{@code military_NATO} (two distinct real military factions), {@code
 * mercenary}, {@code diggers}, {@code dungeon_sanitarov} (cross-referencing {@code anim/}'s sanitar
 * finding above), {@code kondor_squad} (cross-referencing part 63/65's real {@code verbovshik_kondor}
 * NPC), {@code bar_npc}, {@code blue_squad}, {@code stalker} (a generic stalker voice set), {@code
 * tutorial}, {@code zombie}, and real fresh unexplained leads {@code insideout} (RESOLVED part 106
 * -- see the class doc below), {@code pioneer},
 * {@code toxichnaya_zemlyanka} ("toxic dugout", Russian), {@code voland} (a real named character with
 * its own {@code voland_battle_phrase_one-four.ogg}/{@code voland_pain_scream_1-2.ogg} set -- plausibly,
 * though not confirmed, a reference to Woland, the real literary devil-figure from Bulgakov's "The
 * Master and Margarita"), {@code zavet} ("Завет" = "Covenant/Testament", RESOLVED part 107 --
 * {@code cnps.faction.zavet.name=Covenant} directly confirms this was the real faction-name lead it
 * looked like, see {@link net.stalzone.emulator.world.props.CnpsCatalog}), and {@code sujet3_npc}
 * ("sujet" ≈ Russian "сюжет",
 * "plot/story" -- literally "story-chapter-3 NPCs"), whose own {@code afon_4dunge/} subfolder names a
 * further fresh character/location lead, "Afon", tied to a dungeon. {@code 14 new npc/} (1,258 files,
 * the single largest real subfolder here) is itself a real, literal developer milestone label ("14 new
 * NPCs") left as a folder name rather than a proper category name.
 *
 * <p><b>{@code textures/}</b> (314 files: 3 real subfolders, {@code entity} 308, {@code items} 5,
 * {@code gui} 1) and <b>{@code streamrandom/}</b> (18 files, all under a real {@code
 * dmz/zombie/attack/} path) were confirmed to exist by direct listing but not individually inventoried
 * this part -- {@code streamrandom}'s DMZ-mode zombie attack barks corroborate the zombie mob's
 * presence in the DMZ game mode, already known from other trees.
 *
 * <p><b>{@code sorting_map.txt}</b> (the one real loose root file, 41 real lines) has the same real
 * 41-group line count as the {@code sorting_map.txt} this project's own {@code ItemGroups} (part 23)
 * already loads from a different path -- disclosed as consistent with, but NOT confirmed
 * byte-identical to, that file; no diff was attempted this part.
 *
 * <p><b>UPDATED part 106 (2026-09-11):</b> the {@code insideout} voice-set folder name above is
 * resolved by {@code stalker/lang/en.lang}'s real {@code spring26/challenges/season/insideout/*}
 * seasonal-challenge text (a namespace shape distinct from the plain-prefix namespaces {@link
 * net.stalzone.emulator.localization.LangCatalog} otherwise counts, since it embeds literal {@code
 * /} path separators): "Inside Out" is a real named seasonal live-event/game-mode tied to the
 * already-confirmed {@code Forge-11} location ({@code
 * world/props/StalkerCloseEnvironmentCatalog}'s part-92 finding), featuring real "Inside Out
 * mutants," "phantom camps," and "phantom bases" cleared for a centrifuge-based reward at Forge-11.
 * This resolves what the {@code insideout} NPC voice-set folder itself names -- a seasonal event
 * with its own mutant roster -- without altering part 92/68's underlying Forge-11/ZIVCAS findings.
 * A second real seasonal challenge namespace, {@code summer26/challenges/electrostorm/*}, further
 * corroborates the same Forge-11/Lightning Rod/Electric-Storm artifact-farming mechanic ({@code
 * world/props/TextureParticlesCatalog}'s prior finding) from an independent angle.
 */
public final class CustomNpcsCatalog {

    private final Map<String, List<String>> filesByFolder;

    private CustomNpcsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param customNpcsDir the real {@code .../modassets/assets/customnpcs} folder. */
    public static CustomNpcsCatalog load(Path customNpcsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(customNpcsDir)) {
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
            throw new IOException(customNpcsDir + ": no customnpcs asset folders found");
        }

        return new CustomNpcsCatalog(filesByFolder);
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

    /** Real top-level customnpcs-asset folder names, alphabetical. */
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
