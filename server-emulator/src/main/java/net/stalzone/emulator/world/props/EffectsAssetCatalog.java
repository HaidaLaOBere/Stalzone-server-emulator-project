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
 * Catalogs the real assets under {@code modassets/assets/effects/} as a whole (part 62) -- the 4
 * real top-level folders confirmed by direct listing (no loose root-level files at this level,
 * unlike {@code particles/}'s own root): {@code models} (11 files), {@code particles} (831 files,
 * already deep-catalogued by {@link EffectsParticleCatalog}, part 61 -- re-confirmed unchanged here),
 * {@code shaders} (1 file), and {@code textures} (48 files). This class's own new contribution is
 * {@code models}/{@code shaders}/{@code textures} (60 files) -- the three sibling folders part 61's
 * class doc explicitly flagged as "NOT covered by this sweep, remain open for a future part."
 * Squarely game content (weather, day/night rendering, HUD status effects, anomaly-tied models),
 * plus one pure rendering-engine file disclosed as such -- not an EXENS anti-cheat/launcher asset,
 * so stays in scope under rule 2.
 *
 * <p><b>{@code models/}</b> (11 files) holds real anomaly/weather-tied effect models: {@code
 * gravitation_distort.ol} and {@code portal_dist1.ol} directly corroborate part 61's {@code
 * anomalies/} findings (gravity/funnel-type anomalies, and the real {@code portal}/{@code
 * portal_inside} anomaly names) with actual distortion-effect geometry. A live {@code
 * foldersContaining("gravitation_distort")} check (this project's standard verify-before-commit
 * pass) caught a real duplicate before this Javadoc was finalized: an identically-named {@code
 * gravitation_distort.ol} also exists at {@code particles/Texture/gravitation_distort.ol} (the same
 * capitalized {@code Texture/} folder part 61 already flagged for its real naming-case
 * inconsistency) -- disclosed here as a real cross-folder duplicate, same class of finding as part
 * 61's {@code laserpointer.efkmat} correction, not evidence the two folders share unrelated content.
 * {@code teleport_bubble.mcsb}
 * is a real teleport-effect model -- no other "teleport" evidence has surfaced elsewhere in this
 * project so far, so this is disclosed as a fresh, unconfirmed lead rather than a corroboration.
 * {@code fxcloudroundtile.ol}/{@code fxfogheavy.ol}/{@code fxgroundwhisps.ol}/{@code fxpuffstile.ol}
 * are real atmospheric/weather-tile effect models. {@code capsule_flat.mcsb}/{@code
 * capsule_symmetric.mcsb}/{@code unit_sphere.mcsb} are real generic collision/bounding primitives,
 * plausibly reusable scaling bases for other effects rather than named content of their own. {@code
 * model_list.pack} is a real packed manifest/index file, not model geometry itself.
 *
 * <p><b>{@code shaders/}</b> (1 file) holds exactly one real file, {@code light/ft_culling.comp} -- a
 * compute shader, plausibly for forward-tiled lighting culling. This is disclosed plainly as pure
 * rendering-engine machinery, not narrative game content -- the one file in this sweep that isn't.
 *
 * <p><b>{@code textures/}</b> (48 files) is dominated by real dynamic-weather and day/night rendering
 * sets. {@code rain/} (6 files) holds real {@code rain}/{@code snow}/{@code ash_1}/{@code ash_2}
 * weather textures plus two real {@code PsiEjection_particles} files -- a real weather-particle name
 * combining this project's recurring "psi" theme (part 61's {@code psiop} anomaly) with "ejection",
 * plausibly (not confirmed by any other evidence in this project) the classic S.T.A.L.K.E.R.-series
 * anomalous-emission event under a literal English name, rather than the transliterated "vybros" used
 * elsewhere in the genre. {@code snow/} (8 files) is a real ice/snow texture set ({@code
 * ice_texture_0-2}, {@code snow_cover_0-3}, plus {@code noise}/{@code snow}). {@code water/} (4
 * files) is real water-surface textures ({@code large_waves}, {@code normal_waves}, {@code
 * subtle_waves}, {@code noise}). Real day/night rendering: {@code sun.mic}/{@code sun.ol}/{@code
 * sun_envmap}/{@code sun_glare}/{@code sun_gradient}/{@code sun_occlusion}/{@code sunglare.ol} (a
 * full sun-rendering set) alongside {@code moon_phases.mic} -- a real lunar-phase texture, the first
 * evidence in this project's swept trees of a day/night or lunar-cycle system. Real HUD/status VFX:
 * {@code bleeding.mic}, a screen-effect texture for a real bleeding/low-health status. Real
 * night-vision-device evidence: {@code pnv_offset}/{@code pnv_offset0-4.ol} (6 files) plus {@code
 * nvd_lamp.ol} -- a real PNV (passive night-vision) glow/static texture set, not otherwise catalogued
 * elsewhere in this project's swept trees so far. The remaining files ({@code brdf.ol}, {@code
 * dummy_nmap.ol}, {@code envmap_diffuse/gui/gui_diffuse.ol}, {@code fx_noise.ol}, {@code
 * horizon_mask.ol}, {@code kubemas.ol}, {@code lens_color.ol}, {@code lens_dirt_1.ol}, {@code
 * missing_mipmaps.ol}, {@code noise_stencil.ol}) are real generic rendering-utility textures (BRDF
 * lookup, environment maps, lens post-processing, placeholders), not narrative content.
 */
public final class EffectsAssetCatalog {

    private final Map<String, List<String>> filesByFolder;

    private EffectsAssetCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param effectsDir the real {@code .../modassets/assets/effects} folder. */
    public static EffectsAssetCatalog load(Path effectsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(effectsDir)) {
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
            throw new IOException(effectsDir + ": no effects asset folders found");
        }

        return new EffectsAssetCatalog(filesByFolder);
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

    /** Real top-level effects-asset folder names, alphabetical. */
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
