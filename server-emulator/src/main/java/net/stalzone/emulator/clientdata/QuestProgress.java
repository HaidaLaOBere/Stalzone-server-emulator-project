package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads the real local client-state file {@code config/quests.cfg} -- plain JSON (unlike the
 * NBT-based per-character save files in {@link net.stalzone.emulator.session.save}), found in
 * the same real {@code config/} directory the save-file discovery (part 40) came from. Real
 * shape, confirmed from a real, live install: {@code {"readQuests": [<int>, ...], "hiddenQuests":
 * [<int>, ...]}} -- bare numeric quest ids, no other fields.
 *
 * <p><b>Real cross-file validation (part 40, restated here since this class is what actually
 * reads that file)</b>: every quest id a real {@code sd2} save file names with full metadata
 * (title, type) also appears in this file's {@code readQuests}, and this file's one {@code
 * hiddenQuests} entry is exactly the quest {@code sd2} shows repeat-cycling
 * new-&gt;failed-&gt;new-&gt;failed-&gt;...-&gt;finished (a {@code quest_type=2} entry, see
 * {@code PlayerNotification}'s doc). What "hidden" and "read" mean beyond that correlation is NOT
 * confirmed -- {@code readQuests} plausibly means "quest-log entries the player has opened/seen"
 * and {@code hiddenQuests} plausibly means "repeatable/bounty quests suppressed from some default
 * quest-log view", but neither is stated anywhere in evidence this project has found; these
 * accessors expose the raw ids only, no interpretation.
 *
 * <p><b>Confirmed live/actively-written, not a static config</b>: re-reading this exact file on
 * the same real install twice in one work session showed {@code readQuests} grow from 73 to 74
 * entries (a new real id, 11863, appeared) with no other change -- this file is written by the
 * running game client as the account plays, same as the per-character save files.
 */
public final class QuestProgress {

    private final List<Integer> readQuests;
    private final List<Integer> hiddenQuests;

    private QuestProgress(List<Integer> readQuests, List<Integer> hiddenQuests) {
        this.readQuests = readQuests;
        this.hiddenQuests = hiddenQuests;
    }

    @SuppressWarnings("unchecked")
    public static QuestProgress load(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof Map)) {
            throw new IOException(path + ": expected a top-level JSON object, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }
        Map<String, Object> m = (Map<String, Object>) parsed;
        return new QuestProgress(toIntList(m.get("readQuests")), toIntList(m.get("hiddenQuests")));
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> toIntList(Object raw) {
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<Object> rawList = (List<Object>) raw;
        List<Integer> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            if (o instanceof Double) {
                out.add(((Double) o).intValue());
            }
        }
        return out;
    }

    /** Real quest ids in {@code readQuests}, in file order (duplicates, if any, preserved as-is). */
    public List<Integer> readQuestIds() {
        return Collections.unmodifiableList(readQuests);
    }

    /** Real quest ids in {@code hiddenQuests}, in file order. */
    public List<Integer> hiddenQuestIds() {
        return Collections.unmodifiableList(hiddenQuests);
    }

    public boolean isRead(int questId) {
        return readQuests.contains(questId);
    }

    public boolean isHidden(int questId) {
        return hiddenQuests.contains(questId);
    }

    /** Every distinct real quest id mentioned by either list, sorted ascending. */
    public Set<Integer> allQuestIds() {
        Set<Integer> all = new LinkedHashSet<>(readQuests);
        all.addAll(hiddenQuests);
        List<Integer> sorted = new ArrayList<>(all);
        Collections.sort(sorted);
        return new LinkedHashSet<>(sorted);
    }

    @Override
    public String toString() {
        return "QuestProgress{" + readQuests.size() + " read, " + hiddenQuests.size() + " hidden}";
    }
}
