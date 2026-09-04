package net.stalzone.emulator.dialogue;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * CLI demo for SubtitleRegistry, same spirit as the other *DumpTool classes. Loads the real
 * {@code _localized} tree, prints the locale/npcId roster and cue-count totals, and -- if an
 * npcId is given -- that NPC's cue timing table for one locale (text included: this is the
 * user's own installed game asset, being read and printed locally, not reproduced from any
 * external source). A first argument of literally {@code groups} instead prints the real
 * {@link NpcIdGrouping} breakdown.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.dialogue.SubtitleRegistryDumpTool <_localized dir> [npcId] [locale]
 *   java -cp out net.stalzone.emulator.dialogue.SubtitleRegistryDumpTool groups <_localized dir>
 */
public final class SubtitleRegistryDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: SubtitleRegistryDumpTool <_localized dir> [npcId] [locale]");
            System.err.println("   or: SubtitleRegistryDumpTool groups <_localized dir>");
            System.exit(2);
        }

        if (args[0].equals("groups")) {
            if (args.length < 2) {
                System.err.println("usage: SubtitleRegistryDumpTool groups <_localized dir>");
                System.exit(2);
            }
            SubtitleRegistry forGroups = SubtitleRegistry.load(Path.of(args[1]));
            Map<String, List<String>> groups = NpcIdGrouping.groupByRawPrefixToken(forGroups.npcIds());
            System.out.println("Raw-prefix-token groups (" + groups.size() + " tokens, "
                    + forGroups.npcIdCount() + " npcIds total):");
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                System.out.println("  " + entry.getKey() + " (" + entry.getValue().size() + "): " + entry.getValue());
            }
            return;
        }

        Path localizedRoot = Path.of(args[0]);
        SubtitleRegistry registry = SubtitleRegistry.load(localizedRoot);

        List<String> locales = registry.locales();
        System.out.println("Locales (" + locales.size() + "): " + locales);
        System.out.println("Distinct npcIds (" + registry.npcIdCount() + ")");
        System.out.println("Total cues across all locales: " + registry.totalCueCount());

        for (String locale : locales) {
            System.out.println("  " + locale + ": " + registry.npcIdsFor(locale).size() + " files");
        }

        List<String> mismatched = registry.npcIdsWithCrossLocaleTimingMismatch();
        System.out.println("npcIds with a cross-locale timing mismatch (" + mismatched.size() + "): " + mismatched);
        for (String detail : registry.crossLocaleTimingMismatchDetails()) {
            System.out.println("  " + detail);
        }

        if (args.length < 2) {
            return;
        }
        String npcId = args[1];
        String locale = args.length > 2 ? args[2] : (locales.contains("en") ? "en" : locales.get(0));

        SubtitleTrack track = registry.track(npcId, locale);
        if (track == null) {
            System.out.println();
            System.out.println("No track found for npcId=\"" + npcId + "\" locale=\"" + locale + "\"");
            return;
        }

        System.out.println();
        System.out.println("Track " + npcId + " (" + locale + "): " + track.cueCount()
                + " cues, " + track.totalDurationMs() + "ms total");
        for (SubtitleCue cue : track.cues()) {
            System.out.println("  " + cue);
        }
    }
}
