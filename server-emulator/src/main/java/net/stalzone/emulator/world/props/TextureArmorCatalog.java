package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs the complete real {@code modassets/assets/stalker/textures/armor/} folder (part 99) --
 * 731 files: 323 real camo/skin/emissive textures loose at the folder's own root, plus 36 real named
 * subfolders (mostly seasonal or streaming-platform-tied skin drops). Like {@link TextureParticlesCatalog}
 * (part 98) and {@link StalkerPlayerCatalog} (part 91), this root has more loose files than it has
 * subfolders holding data, so {@link #load} uses the same synthetic {@link #ROOT_KEY} deviation rather
 * than ignoring those files or inventing a fake subfolder name for them.
 *
 * <p>The bulk of the root-level {@code skin_*.ol}/{@code emi_*.ol} files are a real-world military
 * camouflage-pattern library (DPM, ERDL, Flecktarn, Kryptek, hexcam, several "digital" pixel camos) --
 * disclosed as real but generic, not claimed as evidence for any specific named mechanic beyond being a
 * camo texture. Several root names ARE confirmed real named content via {@code en.lang}
 * (part 90's {@link net.stalzone.emulator.localization.LangCatalog}): {@code skin_monolit.ol} resolves
 * part 68's disclosed {@code monolit} faction-name lead into a CONFIRMED identity with part 92's already
 * -confirmed "Murmur" faction (the same internal id, {@code cnps.faction.monolit.name=Murmur} -- these
 * are NOT two different factions, they are the same one under two names); {@code skin_ornament_perun.ol}
 * and {@code skin_ornament_stribog.ol} are CONFIRMED real Slavic-mythology camos ("Breathing of Stribog
 * Camouflage", tied to a real "Champion of Stribog" boss wave and an "Idol of Perun" battlefield point,
 * plus a real "Grace of Perun"/"Folktale Spring" seasonal event) -- a THIRD independent real
 * Slavic-folklore-themed event thread, distinct from part 93's "Arena of Slavs" and part 94's "Mushroom
 * Bylina"; {@code skin_kryptek_typhoon.ol} is a further corroboration of part 92's confirmed Typhoon
 * faction; and {@code skin_zivcas.ol}/{@code skin_dpm_zivcas.ol}/{@code skin_erdl_zivcas.ol} further
 * corroborate part 92's confirmed ZIVCAS faction. {@code skin_duty.ol}/{@code skin_freedom.ol}/
 * {@code skin_covenant.ol}/{@code skin_mercenary.ol} (plus their {@code _quest} variants) directly
 * corroborate part 91's confirmed 6-id playable-alliance roster.
 *
 * <p>Several of the 36 real subfolders resolve real, previously-undisclosed content via {@code en.lang}:
 * {@code mk/} is CONFIRMED tied to the already-named "Sea of Exclusion" operation
 * ({@code shop.categories.mk.title=SEA OF EXCLUSION}); {@code mt/} is an ELEVENTH independent Reichstag
 * corroboration ({@code shop.categories.mt.title=ASSAULT ON THE REICHSTAG}); {@code nea/} is CONFIRMED
 * tied to a real named region, "Northeast Asia (Korea, Japan)" ({@code general.region.nea.desc}),
 * extending part 60's Korea-jurisdiction findings; {@code chzzk/} and {@code soop/} are CONFIRMED real
 * third-party streaming-platform tie-ins (CHZZK and SOOP are real Korean livestreaming platforms) via
 * real charm/sticker/camo items bearing those platform names; {@code dayx/} is CONFIRMED a real named
 * "Day X" event/pack with its own developer-credit achievement; {@code scx/} is CONFIRMED a real
 * "SC:X" sub-brand tied to the game's own internal "Stalcraft" codename (part 94); {@code stalcraft_store/}
 * further corroborates that same codename via real official-merchandise camo names; {@code bureau/} is
 * CONFIRMED a real in-game "Market" lootbox line; {@code red_expo/} and {@code stream_battle/} are
 * CONFIRMED real promotional/streamer-event content, disclosed but not tied to any other previously
 * -found system; {@code media/}'s single {@code media_partner.ol} is CONFIRMED a real media-partner-program
 * branding asset. {@code bf/}, {@code na/}, and {@code blitz/} hold real, `en.lang`-confirmed skin
 * content (including a real "Jotun Skin" in {@code blitz/}) but their short folder names themselves are
 * NOT resolved to a specific named event -- disclosed as real but unexplained abbreviations, not guessed
 * at. {@code quest/}'s {@code runa2.ol} ties to a real quest-reward camo ("Vision of the Future Paint")
 * from a small real quest chain involving a "Nocturne MIS-X Blueprint" and a substitution-cipher note --
 * disclosed as a real but tangential quest-content lead, not pursued further here.
 */
public final class TextureArmorCatalog {

    /** Synthetic folder key for the 323 real loose files at {@code armor/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private TextureArmorCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param armorDir the real {@code .../modassets/assets/stalker/textures/armor} folder. */
    public static TextureArmorCatalog load(Path armorDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(armorDir)) {
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
            throw new IOException(armorDir + ": no armor asset folders/files found");
        }

        return new TextureArmorCatalog(filesByFolder);
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

    public List<String> folderNames() {
        return new ArrayList<>(filesByFolder.keySet());
    }

    public List<String> filesIn(String folderName) {
        List<String> files = filesByFolder.get(folderName);
        return files == null ? Collections.emptyList() : Collections.unmodifiableList(files);
    }

    public List<String> foldersContaining(String substring) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesByFolder.entrySet()) {
            for (String file : entry.getValue()) {
                if (file.contains(substring)) {
                    out.add(entry.getKey());
                    break;
                }
            }
        }
        return out;
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
