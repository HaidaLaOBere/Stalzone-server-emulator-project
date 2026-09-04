package net.stalzone.emulator.audio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Catalogs the real FMOD audio banks under {@code modassets/assets/gloomycore/banks/} (472 real
 * {@code .bank} files on this install, flat -- confirmed no subfolders, part 35) and, separately,
 * which of them are CONFIRMED to have actually loaded in a real, logged session -- by scanning
 * the client's own real {@code logs/client-*.log} files for {@code Loading bank <path>} lines
 * (part 35's own evidence method). On this install: 75 of the 472 real bank files are confirmed
 * live this way (the other 397 are real files that may or may not ever load -- absence from the
 * logs is not evidence they never do, only that this project hasn't observed it yet).
 *
 * <p><b>Deliberately dynamic, not a hardcoded snapshot</b>: an earlier research pass (part 35)
 * recorded "75 confirmed live" as a point-in-time finding from the client logs available then.
 * Rather than freezing that list into this class as a hardcoded 75-name table -- which would
 * silently go stale the moment a new session log appears with a bank this project hasn't seen
 * load before -- {@link #loadConfirmedLiveBankIds} re-derives the confirmed-live set from
 * whatever real log files are actually present at call time. On this install, right now, that
 * happens to also be exactly 75 -- confirmed by re-running the same log scan while building this
 * class, not assumed to still match part 35's number.
 *
 * <p>The FMOD {@code .bank} binary format itself is NOT parsed by this class (or anywhere else
 * in this project, beyond a shallow {@code strings} probe of {@code Master.strings.bank} in part
 * 35) -- this is a filename-level catalog + real load-evidence cross-reference, not a bank-content
 * reader.
 */
public final class BankCatalog {

    private final List<String> allBankIds;

    private BankCatalog(List<String> allBankIds) {
        this.allBankIds = allBankIds;
    }

    /** @param banksDir the real {@code .../gloomycore/banks} folder. */
    public static BankCatalog load(Path banksDir) throws IOException {
        List<String> ids = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(banksDir, "*.bank")) {
            for (Path entry : entries) {
                ids.add(entry.getFileName().toString());
            }
        }
        Collections.sort(ids);
        return new BankCatalog(ids);
    }

    /**
     * Scans every real {@code client-*.log} file in {@code logsDir} for lines containing
     * {@code Loading bank <path>} (confirmed real format, part 35: {@code [timestamp] [thread/
     * INFO] Loading bank D:\...\gloomycore\banks\<name>.bank}, CRLF line endings tolerated) and
     * returns the distinct set of {@code <name>.bank} filenames actually observed loading. Not
     * every log line's path necessarily uses {@code \} as separator on every possible install
     * (this one does, confirmed) -- this method takes the substring after the LAST {@code \} (or
     * {@code /}, defensively) rather than assuming one separator exclusively.
     */
    public static Set<String> loadConfirmedLiveBankIds(Path logsDir) throws IOException {
        Set<String> confirmedLive = new LinkedHashSet<>();
        try (DirectoryStream<Path> logFiles = Files.newDirectoryStream(logsDir, "client-*.log")) {
            for (Path logFile : logFiles) {
                for (String rawLine : Files.readAllLines(logFile, StandardCharsets.UTF_8)) {
                    String line = rawLine.strip();
                    int markerIdx = line.indexOf("Loading bank ");
                    if (markerIdx < 0) {
                        continue;
                    }
                    String fullPath = line.substring(markerIdx + "Loading bank ".length()).strip();
                    if (!fullPath.endsWith(".bank")) {
                        continue;
                    }
                    int lastSep = Math.max(fullPath.lastIndexOf('\\'), fullPath.lastIndexOf('/'));
                    String bankId = lastSep >= 0 ? fullPath.substring(lastSep + 1) : fullPath;
                    confirmedLive.add(bankId);
                }
            }
        }
        return confirmedLive;
    }

    public List<String> allBankIds() {
        return Collections.unmodifiableList(allBankIds);
    }

    public int size() {
        return allBankIds.size();
    }

    /** Real bank ids confirmed live (from {@code confirmedLive}) that also exist as a real file here. */
    public List<String> confirmedLiveBankIds(Set<String> confirmedLive) {
        List<String> result = new ArrayList<>();
        for (String id : allBankIds) {
            if (confirmedLive.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /** Real bank files with no log evidence of ever loading -- NOT evidence they never do. */
    public List<String> unconfirmedBankIds(Set<String> confirmedLive) {
        List<String> result = new ArrayList<>();
        for (String id : allBankIds) {
            if (!confirmedLive.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * Log-confirmed bank ids that are NOT present as a real file in this {@code banksDir} --
     * should always be empty on a healthy install (checked, not assumed, while building this
     * class: confirmed empty on this install). A non-empty result would mean either a stale/
     * relocated bank file or a log line this parser is misreading, worth investigating rather
     * than ignoring.
     */
    public List<String> confirmedLiveButMissingFromDisk(Set<String> confirmedLive) {
        Set<String> onDisk = new TreeSet<>(allBankIds);
        List<String> missing = new ArrayList<>();
        for (String id : confirmedLive) {
            if (!onDisk.contains(id)) {
                missing.add(id);
            }
        }
        Collections.sort(missing);
        return missing;
    }
}
