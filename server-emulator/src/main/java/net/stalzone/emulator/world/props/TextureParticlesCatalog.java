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
 * Catalogs the real assets under {@code modassets/assets/stalker/textures/particles/} (part 98) -- 95
 * files, the next-smallest of the real top-level folders under {@code stalker/textures/} part 96 left
 * uncatalogued after part 97 closed out {@code loadscreen/}. Squarely game content (VFX/particle
 * texture data, not an EXENS anti-cheat/launcher asset), so stays in scope under rule 2.
 *
 * <p>Unlike part 97's {@link TextureLoadscreenCatalog} (a complete folder with no loose root files),
 * this folder has 49 real files sitting loose at its own root alongside 8 real subfolders (46 files) --
 * the same {@code ROOT_KEY} deviation part 91's {@link StalkerPlayerCatalog} established, since most of
 * this part's real findings live in those loose root files, not the subfolders.
 *
 * <p><b>Confirmed anomaly-effect textures, cross-referenced against {@code stalker/lang/en.lang}
 * (part 90's {@link net.stalzone.emulator.localization.LangCatalog}):</b>
 * <ul>
 * <li>{@code zast_1.mic} -- CONFIRMED the visual effect for part 71's disclosed {@code zastoy} anomaly
 * type via {@code go.zastoy.name=Stasis}, giving that roster entry its first real texture asset.</li>
 * <li>{@code candle_light.mic} -- CONFIRMED via the shared {@code Candlelight} artifact real to BOTH
 * {@code core.handbook.id.jarka.title=Lighter} and {@code core.handbook.id.circus.title=Inferno}. This
 * resolves part 71's {@code circus} cross-reference (previously named only via part 63's {@code
 * gloomycore/handbook/circus.mic}, no title) into a full CONFIRMED anomaly name, and additionally
 * surfaces {@code jarka}/"Lighter" as an entirely new anomaly name not previously catalogued anywhere
 * in this project -- both are real thermal/plasma anomalies with a near-identical artifact roster (Red
 * Crystal, Candlelight, Leech, Wolf Tears/Wolf's Tears, Viburnum Branch, Comb/Scallop, Firebird, Sun,
 * Radiator).</li>
 * <li>{@code crystal.mic}/{@code crystaldif.mic} -- CONFIRMED real Blizzard-event "Crystal" textures via
 * {@code blizzard.event.crystal_destroyed_notification=Frost Fighters have destroyed a Crystal.} and
 * {@code blizzard.event.one_crystal_left_notification}, corroborating parts 91/95/96's confirmed Frost
 * Fighters/Blizzard-event findings with a third, direct particle-texture tie.</li>
 * <li>{@code bubble.ol}/{@code bubble.mic}/{@code bubble2.mic} -- CONFIRMED as the real "Burrow"
 * gravitational anomaly (also called Wormhole/"teleport" internally) via {@code
 * go.teleport_bubble.name=Burrow} and the {@code core.handbook.id.teleport.main} lore entry.</li>
 * <li>{@code distortion/gravitation_distort.mic}/{@code .ol} -- the SAME real on-disk filenames part 62's
 * {@link EffectsAssetCatalog} already catalogued (and self-corrected) under {@code
 * modassets/assets/effects/}; this is a second, distinct real copy of the same asset shipped under this
 * folder too, cross-referenced here rather than treated as a new find. {@code
 * core.handbook.category.gravity=Gravitational} confirms the shared real anomaly class (Trampoline,
 * Burrow/teleport, Whirlpool/voronka) this distortion effect renders for.</li>
 * <li>{@code sparks/molny.mic}/{@code molny.ol}/{@code molny2.mic}/{@code molny2.ol}/{@code molny2z.mic}/
 * {@code molny3.mic}/{@code molny6.mic}/{@code molny7.mic} ("molniya" = lightning, Russian) -- CONFIRMED
 * lightning-strike particle effects for the real "Electrostorm" weather event, previously known in this
 * project only as a currency/token (part 58's {@code token_electroshtorm2026}) and a mutant-spawner
 * effect set (part 61's {@code electrostorm_mutants/}/{@code electrostorm_npc/}). This part surfaces
 * Electrostorm's full real weather-event narrative for the first time: a real 15-line prepare/start/end
 * warning-broadcast set at a location literally called "the Forge" ({@code electricstorm.notf.prepare_1}
 * through {@code .end_5}, e.g. {@code electricstorm.notf.start_5=...unless you want to play the role of
 * a lightning rod!}), a real artifact-farming mechanic ({@code pda.waypoint.we.lightning_rod.title=
 * Lightning Rod}, {@code we.electric_storm.lightning_rod.statusDescription=You must be within the
 * Lightning Rod's effective range to obtain artifacts.}), and real shop content ({@code
 * shop.categories.electricstorm.title=ELECTROSTORM PACKS}, {@code
 * shop.offers.bundles.electricstorm_scout.titleLabel=Typhoon Scout Supplies} -- a real cross-reference
 * to part 92's confirmed Typhoon faction/Forge-area lore).</li>
 * </ul>
 *
 * <p><b>Disclosed, plausible but NOT literal-word confirmed (this project's evidentiary bar was not
 * met, so these stay leads rather than CONFIRMED findings):</b> {@code kislota.ol}/{@code kislota.mic}
 * ("acid", Russian) plausibly renders the real "Aspic" anomaly ({@code core.handbook.id.kisel.title=
 * Aspic}, a corrosive acid-colony anomaly whose artifact roster includes "Acid Crystal") -- no literal
 * {@code kislota} match exists in {@code en.lang} itself, so this is a translation-based plausible tie
 * only, the same caution this project applied to part 79's {@code probuilder} naming coincidence.
 * {@code distortion/heat_distortion.mic}/{@code .ol} plausibly renders the real "Kettle" thermal anomaly
 * ({@code core.handbook.id.par.title=Kettle}, "a localized area of space with an extremely high
 * temperature, where any liquid instantly evaporates"), not confirmed further. {@code
 * distortion/portal_dist1.mic}, {@code distortion/portal_el.mic}, and {@code distortion/sphere.mic} are
 * real portal/distortion-shader textures with two plausible, mutually-uncertain real ties -- the Burrow
 * gravitational anomaly above, or the "Inside Out"/Typhoon Den portal system ({@code
 * location.inside_out_zone.name=Typhoon Den}, corroborating part 94's {@code insideout}/Typhoon Den tie
 * a further time; {@code go.portal_invasion_boss_point.name=Spawn Point for Portal Boss} and {@code
 * go.portal_invasion_boss_snork_kisliy_point.name=Spawn Point for Acid Skitter} additionally resolve
 * part 68/70's disclosed {@code insideout} NPC-faction lead into a real boss-invasion mode with a named
 * "Acid Skitter" boss mutant, itself CONFIRMED via {@code general.achievement.id.boss_snork_toxic.desc=
 * Defeat an acid skitter}) -- deliberately left unresolved which (or both) this specific texture set
 * belongs to. {@code sphax.mic} is a striking but unconfirmed Minecraft-origin echo (a real, well-known
 * third-party Minecraft texture-pack name, "Sphax PureBDcraft"), disclosed alongside part 63's addendum
 * rather than treated as further proof by itself. {@code skyrimcloudlut.ol}, {@code skyrimclouds01.ol},
 * {@code skyrimcloudsupper03.ol}/{@code 04.ol} are real filenames literally referencing a different game
 * (Skyrim), a disclosed borrowed/reference-asset lead with no further confirmation. {@code raibow.mic}
 * has only a weak, likely-coincidental tie to the real {@code item.quest.audio_diary_operation_lesta_
 * rainbow.name=RAINBOW} audio-diary quest item -- the spelling differs ("raibow" vs "rainbow") and the
 * content type differs (a visual FX texture vs. an audio-diary collectible), so this is disclosed but
 * NOT claimed as a match. {@code sparks/pfx_vehglassdebris_01.ol} is a real vehicle-glass-debris effect;
 * the only real drivable-vehicle content confirmed anywhere in this project is a boat family ({@code
 * go.vehicle_boat_1.name=Boat}, {@code go.tutorial_vehicle_boat_kazanka.name=Kazanka Boat}, {@code
 * go.tutorial_vehicle_boat_wood.name=Wooden Boat}), which has no obvious "glass" component -- disclosed
 * as a real but unconnected vehicle-adjacent asset, not claimed as a car/automobile system this project
 * has otherwise found no other evidence for.</li>
 *
 * <p><b>Generic, unconfirmed environmental/combat FX (no further {@code en.lang} tie found):</b> {@code
 * campfire/} (4 files, a real campfire prop corroborating {@code go.campfire_1}/{@code 1a.name=
 * Campfire}), {@code debris/} (4 real {@code stone_debris_*.ol} files) and {@code explosion/}/{@code
 * explosion_new/} (5 files total, generic explosion/dirt-particle FX), {@code sj_boss/}'s single {@code
 * pfx_stonedebris1_boss.mic} (a real boss-specific debris variant, no further named tie), {@code smoke/}
 * (4 files) plus the numerous root {@code fx*}/{@code grad*}/{@code smoke_nod*}/{@code vaportile*} fog,
 * smoke, snow, and vapor tile textures, and {@code sparks/}'s remaining generic spark/glow files ({@code
 * karusel_eff}/{@code eff2} -- "karusel" is Russian for carousel, plausibly but not confirmed tied to
 * part 71's disclosed {@code carousel} anomaly name, since no literal {@code karusel} match exists in
 * {@code en.lang}; {@code pfx_concretepuffs_01.mic}, {@code spark_fire.ol}, {@code sparks.ol}), {@code
 * circle.ol}, {@code dimfog.ol}, {@code firetexture.mic}, {@code ice_spikes.mic}, {@code
 * lensflare_nod}/{@code nod2} (2 files), {@code redshar_eff.mic}, {@code smoke_nod}/{@code 2}/{@code 3}
 * (3 files), {@code smokeparticles01.ol}, {@code smokespiralstatic01.ol}. These are disclosed as real
 * but generic -- not claimed as evidence for any specific named mechanic.
 */
public final class TextureParticlesCatalog {

    /** Synthetic folder key for the 49 real loose files at {@code particles/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private TextureParticlesCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param particlesDir the real {@code .../modassets/assets/stalker/textures/particles} folder. */
    public static TextureParticlesCatalog load(Path particlesDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(particlesDir)) {
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
            throw new IOException(particlesDir + ": no particles asset folders/files found");
        }

        return new TextureParticlesCatalog(filesByFolder);
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

    /** Real top-level particles-asset folder names, alphabetical, incl. {@link #ROOT_KEY}. */
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
