package net.stalzone.emulator.tips;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogs a real 366-line {@code quote.<npcId>.*} namespace in {@code stalker/lang/en.lang} (part
 * 112) -- a real comedic "reward delayed" excuse generator for a named NPC, {@code ziv}, who is
 * evidently a daily-reward/mailbox vendor that always has some absurd new reason your reward isn't
 * ready yet. This project has not previously catalogued {@code ziv} by name anywhere else.
 *
 * <p>The real numbering is not arbitrary: {@code quote.ziv.<n>} runs {@code n}=1 through 365 with
 * NO gaps (directly counted, not assumed) -- one distinct real excuse per calendar day of a
 * non-leap year, plus one further real line, {@code quote.ziv.answer=Okay, I'll drop by tomorrow.},
 * the player's own real stock reply. This is this project's largest single block of real comedic
 * writing found in one namespace, spanning a huge range of tones and references: mundane
 * (paperwork, sick couriers, a broken toilet), Zone-flavored (anomalies, archianomalies, a real
 * cross-reference to the confirmed "Inside Out" seasonal event in excuse #332 -- "not even to the
 * Inside Out, but to some hellish dimension"), meta/fourth-wall-breaking (excuse #33: "rewards had
 * to be rebalanced because their properties were too strong. We're waiting for the balancer to
 * finish up"; excuse #362: "the whole office testing new mechanics that our game designers came up
 * with"), and one real fresh named-character lead this project had not seen before: excuse #344
 * names "Plastinka" ("Записка"/record-related word, Russian) as someone who "borrowed five million
 * rubles from the warehouse and did not return the money" -- disclosed as a real name, not
 * confirmed as an NPC or a joke-only reference.
 *
 * <p>Modeled generically as {@code (npcId, dayNumber)} -&gt; excuse text plus a separate {@code
 * (npcId)} -&gt; answer text, rather than hardcoding {@code ziv} into the API, since nothing rules
 * out a second real NPC using the same {@code quote.<npcId>.*} shape in a future content update.
 */
public final class DailyExcuseCatalog {

    private final Map<String, String> answerByNpcId;
    private final Map<String, Map<Integer, String>> excusesByNpcId;

    private DailyExcuseCatalog(Map<String, String> answerByNpcId,
                                Map<String, Map<Integer, String>> excusesByNpcId) {
        this.answerByNpcId = answerByNpcId;
        this.excusesByNpcId = excusesByNpcId;
    }

    private static final String PREFIX = "quote.";

    public static DailyExcuseCatalog load(LangCatalog catalog) {
        Map<String, String> answerByNpcId = new LinkedHashMap<>();
        Map<String, Map<Integer, String>> excusesByNpcId = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int dot = rest.indexOf('.');
            if (dot < 0) {
                continue;
            }
            String npcId = rest.substring(0, dot);
            String suffix = rest.substring(dot + 1);

            if (suffix.equals("answer")) {
                answerByNpcId.put(npcId, catalog.get(key));
                continue;
            }

            try {
                int dayNumber = Integer.parseInt(suffix);
                excusesByNpcId.computeIfAbsent(npcId, k -> new TreeMap<>()).put(dayNumber, catalog.get(key));
            } catch (NumberFormatException ignored) {
                // not a numbered excuse line; skip rather than guess
            }
        }

        return new DailyExcuseCatalog(answerByNpcId, excusesByNpcId);
    }

    public String answer(String npcId) {
        return answerByNpcId.get(npcId);
    }

    public String excuse(String npcId, int dayNumber) {
        Map<Integer, String> excuses = excusesByNpcId.get(npcId);
        return excuses == null ? null : excuses.get(dayNumber);
    }

    public int excuseCount(String npcId) {
        Map<Integer, String> excuses = excusesByNpcId.get(npcId);
        return excuses == null ? 0 : excuses.size();
    }

    /** Real numbered day slots this NPC has an excuse for, sorted ascending. */
    public List<Integer> excuseDayNumbers(String npcId) {
        Map<Integer, String> excuses = excusesByNpcId.get(npcId);
        return excuses == null ? List.of() : new ArrayList<>(excuses.keySet());
    }

    public List<String> npcIds() {
        List<String> ids = new ArrayList<>(excusesByNpcId.keySet());
        Collections.sort(ids);
        return ids;
    }
}
