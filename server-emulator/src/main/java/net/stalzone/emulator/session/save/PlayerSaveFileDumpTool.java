package net.stalzone.emulator.session.save;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo/inspection tool, same spirit as the other *DumpTool classes: loads one real
 * per-character save-data file (see {@link PlayerSaveFile}'s doc for the real
 * {@code config/<CharacterName>/{common,sd1,sd2,sd3,sd4}} layout this reads) and prints its
 * content in a readable, content-shape-aware form -- typed output for the two well-evidenced
 * shapes ({@code interacts}, {@code notifications}), raw field dump otherwise.
 *
 * <p>This tool is meant to be pointed at a real save file the person running the server actually
 * owns access to (their own install, or a save file they're operating a private server for) --
 * unlike this project's own docs/tests, it has no reason to withhold real field values it's asked
 * to print, since it's a diagnostic tool operating on data the caller already has. This project's
 * privacy discipline (see {@link PlayerSaveFile}'s doc) applies to what THIS PROJECT'S OWN
 * documentation and source comments say, not to what a server operator's own tooling shows them
 * about their own data.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.session.save.PlayerSaveFileDumpTool <save file path>
 */
public final class PlayerSaveFileDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: PlayerSaveFileDumpTool <save file path>");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        PlayerSaveFile save = PlayerSaveFile.load(path);
        System.out.println("Loaded " + path + " -- top-level keys: " + save.rawFields().keySet());
        System.out.println();

        if (save.hasInteractions()) {
            List<PlayerInteraction> interactions = save.interactions();
            System.out.println("interacts: " + interactions.size() + " entries");
            for (PlayerInteraction it : interactions) {
                System.out.println("  " + it.type() + "  allianceId=" + it.allianceId()
                        + "  username=" + it.username());
            }
            System.out.println();
        }

        if (save.hasNotifications()) {
            List<PlayerNotification> notifications = save.notifications();
            System.out.println("notifications: " + notifications.size() + " entries");
            int i = 0;
            for (PlayerNotification n : notifications) {
                StringBuilder sb = new StringBuilder();
                sb.append("  [").append(i++).append("] channel=").append(n.channelId());
                if (n.isRead()) sb.append(" (read)");
                if (n.isQuestChannel()) {
                    sb.append("  questId=").append(n.questId())
                            .append(" type=").append(n.questNotificationType())
                            .append(" questType=").append(n.questTypeCode())
                            .append(" title=\"").append(n.questTitle()).append('"');
                } else if (n.isOutpostChannel()) {
                    sb.append("  x=").append(n.outpostX()).append(" z=").append(n.outpostZ());
                } else if (n.isHandbookChannel()) {
                    sb.append("  link=").append(n.handbookLink());
                } else {
                    sb.append("  payload=").append(n.payload());
                }
                System.out.println(sb);
            }
            System.out.println();
        }

        if (save.hasObservedOffers()) {
            List<String> offers = save.observedOffers();
            System.out.println("observedOffers: " + offers.size() + " entries");
            for (String offer : offers) {
                System.out.println("  " + offer);
            }
            System.out.println();
        }

        if (save.rawFields().containsKey("observedElements")
                || save.rawFields().containsKey("lastSeenTagsVersion")) {
            System.out.println("cosmetic-tab state (part 46):");
            System.out.println("  lastSeenTagsVersion=" + save.lastSeenTagsVersion()
                    + " lastSeenBackgroundsVersion=" + save.lastSeenBackgroundsVersion()
                    + " lastSeenPatternsVersion=" + save.lastSeenPatternsVersion()
                    + " lastSeenStickersVersion=" + save.lastSeenStickersVersion());
            System.out.println("  observedElementIds (decoded little-endian uint16 pairs): "
                    + save.observedElementIds());
            System.out.println();
        }

        boolean isCommonShaped = save.rawFields().containsKey("caseLastOpenCount")
                || save.rawFields().containsKey("arcadesMenuOpened");
        if (isCommonShaped) {
            System.out.println("common-shaped file (part 47):");
            System.out.println("  arcadesMenuOpened=" + save.getFlag("arcadesMenuOpened")
                    + " hasSeenDMZHowToPlay=" + save.getFlag("hasSeenDMZHowToPlay")
                    + " hasSeenReichstagHowToPlay=" + save.getFlag("hasSeenReichstagHowToPlay"));
            List<CaseOpenCount> caseCounts = save.caseOpenCounts();
            System.out.println("  caseOpenCounts: " + caseCounts.size() + " entries");
            for (CaseOpenCount c : caseCounts) {
                System.out.println("    " + c);
            }
            List<OpsMapDifficulty> opsDifficulty = save.opsDifficultyByMap();
            System.out.println("  opsDifficultyByMap: " + opsDifficulty.size() + " entries");
            for (OpsMapDifficulty d : opsDifficulty) {
                System.out.println("    " + d);
            }
            System.out.println();
        }

        if (!save.hasInteractions() && !save.hasNotifications() && !save.hasObservedOffers()
                && !save.rawFields().containsKey("observedElements") && !isCommonShaped) {
            System.out.println("raw fields (no typed accessor for this file's content shape):");
            for (var e : save.rawFields().entrySet()) {
                String preview = String.valueOf(e.getValue());
                if (preview.length() > 200) preview = preview.substring(0, 200) + "...";
                System.out.println("  " + e.getKey() + " = " + preview);
            }
        }
    }
}
