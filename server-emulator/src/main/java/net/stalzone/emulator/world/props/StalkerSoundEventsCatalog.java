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
 * Catalogs the complete real {@code modassets/assets/stalker/sound/events/} folder (part 104) -- the
 * larger of the two real subfolders part 102's {@code StalkerSoundMiscCatalog} deliberately excluded
 * from its own sweep, and, with part 103's {@link StalkerCloseEnvironmentCatalog} having already closed
 * out {@code closeenvironment/}, the LAST remaining unswept real subfolder anywhere under {@code
 * stalker/sound/} -- 4,352 files across 61 real top-level folders plus 2 real loose files at the
 * folder's own root ({@code spawn.ogg}/{@code spawned.ogg}, generic mob/event spawn cues not tied to
 * any one location, so this class reuses the same synthetic {@code ROOT_KEY} folder-key deviation parts
 * 91/98/99/100 established, even though here it covers only 2 of 4,352 files -- kept for completeness
 * rather than silently dropping two real files that don't fit the per-location folder shape). With
 * {@code events/} closed, every real folder under {@code modassets/assets/stalker/} that this project
 * has ever enumerated has now been catalogued end to end.
 *
 * <p>Most of the 61 real top-level folders are per-location ambience sets whose names directly echo
 * already-confirmed real STALZONE map locations from part 72's {@link StalkerClansCatalog} roster and
 * elsewhere -- {@code bar}/{@code kordon}/{@code zaton}/{@code agroprom}/{@code yanov}/{@code svalka}/
 * {@code bolota}/{@code radar}/{@code mogilnik}/{@code sanitars}/{@code murmur}/{@code stealthfields}/
 * {@code pd}/{@code yama}/{@code yama_close}/{@code reichstag}/{@code arena}/{@code battleroyale}/
 * {@code new_year}/{@code dolg}/{@code freedom}/{@code merc} -- so this part's real value is in the two
 * names that were NOT already known, cross-referenced against {@code stalker/lang/en.lang} (part 90's
 * {@link net.stalzone.emulator.localization.LangCatalog}) under this project's literal-word evidentiary
 * bar.
 *
 * <p><b>Confirmed:</b> {@code limansk/} (37 files, {@code limansk_surround*.ogg}) is CONFIRMED the real
 * "Lyubech-3" location via {@code ui.recap.stats.metric.limansk_extract=Exits from Lyubech-3}, with
 * further real narrative context in {@code stalker.gui.frontline.presentation.stage1.par1}: "On SSPP's
 * side were data from Boral, Lyubech-3, extensive study of materials received from military stalkers and
 * intelligence, as well as the works of scientists from the Mainland." -- directly corroborating part
 * 101's confirmed "Attack on Perimeter"/frontline operation with a named real location it defends.
 * {@code vagant/} (4 files, all {@code vagant_sings*.ogg}) is CONFIRMED a real named BOSS via {@code
 * ui.recap.metric.boss_vagant.title=TIME'S UP, VAGANT} and {@code
 * ui.recap.metric.boss_vagant.desc=You defeated Vagant in {value} min. So much for the talk!} -- the
 * real "sings" filenames and the "talk"/taunt flavor text line up: Vagant is a talking/singing boss
 * encounter, a previously-uncatalogued named boss distinct from parts 92/102's Skalolaz/Palach/Voland
 * roster.
 *
 * <p><b>Disclosed, plausible but NOT literal-word confirmed:</b> {@code foton2/} (100 files, {@code
 * foton2_surround*.ogg}; "foton" is the Russian transliteration of "photon") plausibly ties to a real
 * operation named via {@code operation.hud.absorber.header.title=ACTIVATE THE PHOTON GENERATOR} and
 * {@code operation.hud.absorber.header.desc=Extract batteries from attractors to power the generator} --
 * no {@code en.lang} entry uses the literal id {@code foton}, only the translated English word "photon"
 * under the unrelated id {@code absorber}, so per this project's evidentiary bar (the same caution
 * applied to part 79's {@code probuilder} coincidence and part 98's {@code kislota}/Aspic translation
 * lead) this stays a plausible, not confirmed, tie.
 *
 * <p><b>RESOLVED part 113 (partially):</b> {@link OperationsCatalog}'s {@code
 * gui.operation.how_to_play.breach.absorber.name=Photon Generator Power Supply} is a literal real
 * confirmation that the id {@code absorber} does mean "Photon Generator" -- its {@code desc} matches
 * this class's own {@code operation.hud.absorber.header.desc} almost verbatim. This closes the
 * "not literal-word confirmed" gap for {@code absorber}/"Photon Generator" itself, but the separate
 * {@code foton2/} sound-folder-name coincidence noted above remains only plausible, not confirmed.
 *
 * <p><b>Further corroborations (no new resolution, reinforcing already-confirmed findings from a new
 * angle):</b> {@code murmur/} (50 files) and {@code sanitars/} (63 files) reinforce part 92's confirmed
 * Murmur faction and part 103's confirmed "Orderly Base" location; {@code reichstag/} (64 files) is
 * another in this project's long independent Reichstag-event corroboration chain running since part 61;
 * {@code stealthfields/} (49 files) and {@code pd/} (26 files) reinforce part 103's "Helicopter Crash"
 * and part 96's "Path of Fools" phonograph-track ties respectively; {@code yama/} (41 files) and {@code
 * yama_close/} (15 files) reinforce part 103's confirmed "The Pit"; {@code mogilnik/} (56 files)
 * reinforces the confirmed "Graveyard"/"Forgotten City" location, now corroborated from a THIRD
 * independent folder (part 103's own top-level {@code mogilnik/} plus its {@code easter_eggs/24mogilnik}
 * makes this the third); {@code dolg/} (107 files), {@code freedom/} (101 files), and {@code merc/} (100
 * files) reinforce part 91's confirmed 6-id playable-alliance roster; {@code battleroyale/} (20 files)
 * and {@code arena/} (41 files) reinforce the confirmed Battle Royale mechanic and Crow Arena/Coliseum
 * events respectively; {@code radar/} (48 files), {@code rls/} (29 files), {@code rls_close/} (33
 * files), and {@code radartunnel/} (35 files) all reinforce the confirmed Radar Station location ({@code
 * rls} is the Russian abbreviation for "radiolocation station").
 *
 * <p>The remaining folders ({@code arm_sklad}/{@code atp}/{@code bar_safezone}/{@code bar_sever}/{@code
 * base_common}/{@code boatstation}/{@code bolota_close}/{@code buran}/{@code dog}/{@code
 * drone}/{@code dungeon_rework}/{@code horror_2025}/{@code lab}/{@code les}/{@code
 * metalscrapefar}/{@code mg}/{@code nn}/{@code rijik}/{@code schoolmg}/{@code scare}/{@code
 * spusk}/{@code stavka_atamana}/{@code td}/{@code tochka_nevozvrata}/{@code tryasina}/{@code
 * tryasina_close}/{@code tutorial}/{@code underworld}/{@code ushelie}/{@code warp}/{@code weirds}/{@code
 * wind}/{@code zavet}) are real generic or evocatively-named location/event ambience sets with no
 * literal {@code en.lang} cross-reference found -- disclosed as real, several plausible by name alone
 * (e.g. {@code horror_2025} is self-evidently a dated seasonal Halloween-style event, {@code
 * tochka_nevozvrata} is Russian for "point of no return", {@code stavka_atamana} is Russian for "the
 * ataman's headquarters"), but NOT confirmed, per rule 1.
 */
public final class StalkerSoundEventsCatalog {

    /** Synthetic folder key for the 2 real loose files ({@code spawn.ogg}/{@code spawned.ogg}) at {@code events/}'s own root. */
    public static final String ROOT_KEY = "(root)";

    private final Map<String, List<String>> filesByFolder;

    private StalkerSoundEventsCatalog(Map<String, List<String>> filesByFolder) {
        this.filesByFolder = filesByFolder;
    }

    /** @param eventsDir the real {@code .../modassets/assets/stalker/sound/events} folder. */
    public static StalkerSoundEventsCatalog load(Path eventsDir) throws IOException {
        Map<String, List<String>> filesByFolder = new TreeMap<>();
        List<String> rootFiles = new ArrayList<>();

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(eventsDir)) {
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
            throw new IOException(eventsDir + ": no events asset folders/files found");
        }

        return new StalkerSoundEventsCatalog(filesByFolder);
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

    /** Real top-level {@code events} folder names, alphabetical, incl. {@link #ROOT_KEY} (62 total). */
    public List<String> folderNames() {
        return Collections.unmodifiableList(new ArrayList<>(filesByFolder.keySet()));
    }

    /** Real file paths (relative to the folder itself) inside one real folder, or empty if unknown. */
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
