package net.stalzone.emulator.session;

import net.stalzone.emulator.progression.PlayerClass;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.progression.QuestStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Part 87: this emulator's own session-persistence file for a {@link PlayerState}. Before this
 * part, every {@link SingleplayerServer} run started a brand-new {@code PlayerState} at a fresh
 * spawn position with an empty inventory/wallet/quest-log every single time it started -- nothing
 * survived a restart, even after real {@link ConsoleDriver} commands (move/attack/buy/unlock/
 * quest) had changed that state during a prior run. This class closes that gap.
 *
 * <p><b>This is NOT a reverse-engineered copy of any real STALZONE save format.</b> The real
 * per-character save file this project already reads is {@link
 * net.stalzone.emulator.session.save.PlayerSaveFile} (a real NBT/zstd file). This class is a
 * plain {@link Properties} file (the same format {@link net.stalzone.emulator.config.ServerConfig}
 * already uses for its own persistence) -- an entirely emulator-original design for holding this
 * emulator's own local simulation state between runs, same standing as {@link Inventory}/{@link
 * Wallet}'s own "the ledger design is ours, only the ids/structure inside it are real evidence"
 * disclosure. Records: position/zone, carried items ({@link Inventory}), wallet balances ({@link
 * Wallet}), chosen {@link PlayerClass}, unlocked {@link PrestigeAbility} names, and {@link
 * QuestLog} state (known/hidden ids, real titles/status where recorded) -- i.e. everything a
 * console operator can currently change via {@link ConsoleDriver}.
 *
 * <p>A saved position from a different real zone than the one currently configured ({@code
 * ServerConfig.startZone}) is still restored numerically -- this class does not try to detect or
 * warn about a zone mismatch -- but that zone's terrain won't be loaded by this run's {@code
 * WorldModel}, so {@link GameLoop#tickMovement}'s existing "unknown terrain" handling (leaving Y
 * as-is when the column isn't loaded, already disclosed on that method's own doc) applies exactly
 * as it would for any position outside the currently loaded chunks -- not a new limitation this
 * class introduces.
 */
public final class PlayerStateSave {

    private static final Pattern QUEST_KEY = Pattern.compile("^quest\\.(\\d+)\\.(known|hidden|title|status)$");

    private PlayerStateSave() {
    }

    /** Writes every field {@link ConsoleDriver} can change into a fresh {@link Properties} file at {@code path}, creating parent directories if needed. Overwrites any existing file at that path. */
    public static void save(PlayerState player, Path path) throws IOException {
        Properties p = new Properties();
        p.setProperty("zoneName", player.zoneName);
        p.setProperty("x", String.valueOf(player.x));
        p.setProperty("y", String.valueOf(player.y));
        p.setProperty("z", String.valueOf(player.z));
        if (player.playerClass != null) {
            p.setProperty("playerClass", player.playerClass.name());
        }
        if (!player.unlockedAbilities.isEmpty()) {
            StringBuilder abilities = new StringBuilder();
            for (PrestigeAbility ability : player.unlockedAbilities) {
                if (abilities.length() > 0) {
                    abilities.append(',');
                }
                abilities.append(ability.name());
            }
            p.setProperty("unlockedAbilities", abilities.toString());
        }
        for (int itemId : player.inventory.itemIds()) {
            p.setProperty("item." + itemId, String.valueOf(player.inventory.countOf(itemId)));
        }
        for (String currency : player.wallet.currenciesHeld()) {
            p.setProperty("currency." + currency, String.valueOf(player.wallet.balanceOf(currency)));
        }
        Set<Integer> allQuestIds = new HashSet<>(player.questLog.knownQuestIds());
        allQuestIds.addAll(player.questLog.hiddenQuestIds());
        for (int questId : allQuestIds) {
            if (player.questLog.isKnown(questId)) {
                p.setProperty("quest." + questId + ".known", "true");
            }
            if (player.questLog.isHidden(questId)) {
                p.setProperty("quest." + questId + ".hidden", "true");
            }
            String title = player.questLog.titleOf(questId);
            if (title != null) {
                p.setProperty("quest." + questId + ".title", title);
            }
            QuestStatus status = player.questLog.statusOf(questId);
            if (status != null) {
                p.setProperty("quest." + questId + ".status", status.name());
            }
        }

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            p.store(out, "STALZONE server-emulator singleplayer save (part 87) -- this emulator's own "
                    + "session-persistence format, NOT a reverse-engineered copy of any real STALZONE "
                    + "save file. See PlayerStateSave's class doc.");
        }
    }

    /**
     * Restores every field {@link #save} writes onto the already-constructed {@code player},
     * mutating it in place -- position/zone, inventory, wallet, class, unlocked abilities, and
     * quest-log state. Does nothing (and does not throw) if {@code path} doesn't exist yet, since
     * "no save file yet" is the normal, expected first-run state, same as {@code
     * ServerConfig.loadOrCreateDefault} treating a missing config file as defaults rather than an
     * error.
     */
    public static void load(PlayerState player, Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        }

        String zoneName = p.getProperty("zoneName");
        if (zoneName != null) {
            player.zoneName = zoneName;
        }
        if (p.getProperty("x") != null) {
            player.x = Double.parseDouble(p.getProperty("x"));
        }
        if (p.getProperty("y") != null) {
            player.y = Double.parseDouble(p.getProperty("y"));
        }
        if (p.getProperty("z") != null) {
            player.z = Double.parseDouble(p.getProperty("z"));
        }
        String playerClass = p.getProperty("playerClass");
        if (playerClass != null) {
            player.playerClass = PlayerClass.valueOf(playerClass);
        }
        String abilities = p.getProperty("unlockedAbilities");
        if (abilities != null && !abilities.isBlank()) {
            for (String name : abilities.split(",")) {
                player.unlockedAbilities.add(PrestigeAbility.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            }
        }

        for (String key : p.stringPropertyNames()) {
            if (key.startsWith("item.")) {
                int itemId = Integer.parseInt(key.substring("item.".length()));
                int count = Integer.parseInt(p.getProperty(key));
                if (count > 0) {
                    player.inventory.add(itemId, count);
                }
            } else if (key.startsWith("currency.")) {
                String currency = key.substring("currency.".length());
                long amount = Long.parseLong(p.getProperty(key));
                if (amount > 0) {
                    player.wallet.credit(currency, amount);
                }
            }
        }

        // Quest state is spread across up to 4 keys per id (known/hidden/title/status) -- collect
        // every id mentioned by any of them first, then apply that id's own set of keys together.
        Set<Integer> questIds = new HashSet<>();
        for (String key : p.stringPropertyNames()) {
            Matcher m = QUEST_KEY.matcher(key);
            if (m.matches()) {
                questIds.add(Integer.parseInt(m.group(1)));
            }
        }
        for (int questId : questIds) {
            if ("true".equals(p.getProperty("quest." + questId + ".known"))) {
                player.questLog.markKnown(questId);
            }
            if ("true".equals(p.getProperty("quest." + questId + ".hidden"))) {
                player.questLog.markHidden(questId);
            }
            String title = p.getProperty("quest." + questId + ".title");
            QuestStatus status = p.getProperty("quest." + questId + ".status") != null
                    ? QuestStatus.valueOf(p.getProperty("quest." + questId + ".status"))
                    : null;
            if (title != null || status != null) {
                // recordEvent also marks the id known -- harmless double-mark if .known was
                // already applied above, and status may legitimately be null here (a quest that
                // has a real title from one event but whose most recent status was recorded
                // separately -- recordEvent overwrites status every call, same as it does live).
                player.questLog.recordEvent(questId, title, status);
            }
        }
    }
}
