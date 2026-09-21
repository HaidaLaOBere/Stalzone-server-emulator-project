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
 * Catalogs the complete real {@code modassets/assets/stalker/sound/closeenvironment/} folder (part
 * 103), the smaller of the two subfolders part 102's {@code StalkerSoundMiscCatalog} deliberately
 * excluded from its own sweep -- 3,122 files across exactly 37 real top-level folders, no loose root
 * files, so (like part 92's {@link StalkerSoundsCatalog}) this class follows {@link
 * StalkerClansCatalog}'s plain per-top-level-folder walk rather than the synthetic {@code ROOT_KEY}
 * deviation. No single subfolder dominates the tree the way {@code events/} and this folder's own
 * sibling {@code closeenvironment/} dominated {@code sound/} itself (the largest here, {@code
 * easter_eggs/}, is 21.6% of the total, not 34%+), so no further exclusion was warranted -- the whole
 * folder is catalogued in one part.
 *
 * <p><b>Headline finding: a real 26-location "easter egg" roster.</b> {@code easter_eggs/} (675 files,
 * the largest subfolder) is internally organized as 26 numbered subfolders ({@code 01serpantine}
 * through {@code 26labirint}), each a real named STALZONE location's own dedicated set of hidden
 * ambient environmental-storytelling sounds (crying, laughter, radio chatter, animal calls, and so
 * on) -- distinct in kind from the other 36 folders' generic terrain/prop ambience. Several resolve
 * against {@code en.lang} under this project's literal-word evidentiary bar: {@code 11polesskoye} is
 * CONFIRMED the real "Polesskoye" quest-delivery location; {@code 14yama} is CONFIRMED the real "The
 * Pit"; {@code 05kuznya11} is CONFIRMED the real "Forge-11" ({@code
 * phonograph.gui.track.track20_kuznya11=Forge-11}); and {@code 24mogilnik} ties to the real
 * "Graveyard"/"Forgotten City" location this class's own separate top-level {@code mogilnik/} folder
 * (63 files, {@code church}/{@code house}/{@code school} subfolders) also confirms -- two independent
 * folders corroborating the same real location from different angles (generic ambience vs. a specific
 * hidden easter-egg prop set). The headline single file is {@code 26labirint/exbo1.ogg} alongside
 * {@code 26labirint/ziv1.ogg}: a real hidden easter-egg scene in a location literally named "Labyrinth"
 * naming EXBO (the real-world publisher/developer, in scope under rule 2) and a real named quote-giving
 * NPC, "Ziv" ({@code quote.ziv.*} dialogue lines, e.g. {@code quote.ziv.answer=Okay, I'll drop by
 * tomorrow.}) -- disclosed as a DIFFERENT real entity from part 92's confirmed "ZIVCAS" institute, not
 * assumed to be the same one without further evidence. The other 22 easter-egg locations (e.g. {@code
 * 02cherni_obezd}, {@code 09hramovaya_top}, {@code 12mertvi_gorod}, {@code 15les}, {@code
 * 21rizhi_les}) have real, evocative Russian names (several matching or echoing well-known
 * S.T.A.L.K.E.R.-series locations) but no literal {@code en.lang} match found -- disclosed as real,
 * plausible by name/context alone, but NOT confirmed, per rule 1.
 *
 * <p><b>Further confirmed cross-references:</b> {@code sanitarsbase/} is CONFIRMED the real "Orderly
 * Base" location ({@code phonograph.gui.track.escape_sanitars=Orderly Base}), tied to a real "Jaundice"
 * NPC quest ({@code item.quest.sanitars_keys}, found in "Jaundice's safe"); {@code npc/npc_auctioneer*}
 * (18 files) corroborates part 77's confirmed in-game auction house via a real "OPEN AUCTION" dialog
 * tooltip; {@code electric_storm/psi_izluchatel*.ogg} ("izluchatel" = "emitter", Russian) is CONFIRMED
 * the real "Psy-Installation" mechanic from part 101's confirmed DMZ "Black Raid" operation ({@code
 * go.dmz2026_psy_tower.name=Psy-Installation}, a real deactivation objective), directly corroborating
 * part 92's Murmur psionic-faction theme, while the same folder's {@code generator_forge.ogg} ties to
 * the real "Forge" location from part 98's confirmed Electrostorm event narrative; {@code
 * tut/tut_blackhole*.ogg} is real tutorial ambience for part 71's confirmed "Blackhole" anomaly, and
 * the same folder's {@code tut/radiation/geiger.ogg} is real radiation-hazard tutorial ambience;
 * {@code stealthfields/} corroborates a real phonograph track ({@code
 * phonograph.gui.track.stealthfields_1=Helicopter Crash}); and {@code propaganda/} (154 files) ties to
 * a real DMZ 2026 achievement ({@code general.achievement.id.dmz_2026_no_propaganda.title=No
 * Propaganda}, tied to the same psy-tower mechanic) and a real Covenant propaganda quest item naming
 * part 91's confirmed Covenant leader, Metropolite.
 *
 * <p>The remaining folders ({@code anything}/{@code background}/{@code bar_map}/{@code
 * burnt_house}/{@code cistern}/{@code factory}/{@code grass}/{@code hangarclosed}/{@code
 * horror_backgrounds}/{@code immersion}/{@code junkyard}/{@code lake}/{@code lava}/{@code
 * metalcreak}/{@code metallowcreak}/{@code mg}/{@code mountain}/{@code nolife}/{@code
 * powerline}/{@code schoolmg}/{@code ship}/{@code sky}/{@code sosnodub}/{@code swamp}/{@code
 * tree}/{@code water}/{@code wind}/{@code wood_creak}/{@code zavod}) are real generic terrain/prop
 * ambience libraries (grass rustle, metal creaks, wind, water, tree sounds, and similarly generic
 * environmental foley) with no {@code en.lang} cross-reference found -- disclosed but not further
 * pursued, since a bare foley-file name carries no narrative content to confirm against.
 */
public final class StalkerCloseEnvironmentCatalog {

    private final Map<String, List<String>> filesByFolder;

    private StalkerCloseEnvironmentCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param closeEnvironmentDir the real {@code .../modassets/assets/stalker/sound/closeenvironment} folder. */
    public static StalkerCloseEnvironmentCatalog load(Path closeEnvironmentDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(closeEnvironmentDir)) {
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
            throw new IOException(closeEnvironmentDir + ": no closeenvironment asset folders found");
        }

        return new StalkerCloseEnvironmentCatalog(filesByFolder);
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

    /** Real top-level {@code closeenvironment} folder names, alphabetical (37 total). */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /**
     * Real file paths (relative to the folder itself) inside one real folder, or empty if unknown.
     * For {@code easter_eggs}, this includes each numbered location subfolder's own nested files
     * (e.g. {@code 26labirint/exbo1.ogg}); other folders nest more shallowly (e.g. {@code mogilnik}'s
     * {@code church}/{@code house}/{@code school}).
     */
    public List<String> filesIn(String folderName) {
        return Collections.unmodifiableList(filesByFolder.getOrDefault(folderName, List.of()));
    }

    /**
     * Real folder names that have at least one nested file whose name contains {@code substring}
     * (case-insensitive). Same generic evidentiary-verification helper as every prior catalog class
     * in this package.
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
