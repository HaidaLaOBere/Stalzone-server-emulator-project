package net.stalzone.emulator.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Groups real {@link SubtitleRegistry} npcIds by the raw filename token before their first
 * {@code _} -- e.g. {@code duty_2_6yushka} and {@code duty_3_8davidov} both group under
 * {@code duty}. Computed purely mechanically from the real 108-name roster (part 34), NOT a
 * verified in-game "quest arc" system -- this project has found no evidence anywhere of an
 * actual arc/chapter data structure in the client's real files, so this class does not claim
 * one exists. It is a naming-convention OBSERVATION: a real, useful way to browse the roster
 * (10 tokens cover 88 of the 108 real names with 5+ entries each: {@code bandits}(7),
 * {@code bigeagle}(5), {@code duty}(15), {@code merc}(9), {@code nina}(5), {@code palach}(12),
 * {@code stalkers}(7), {@code twins}(9), {@code zarya}(13), {@code zavet}(10)), while the
 * remaining 20 names split into 10 small/single-entry tokens that don't fit that shape as
 * cleanly ({@code eremey}, {@code operativnik}, {@code p} -- from {@code p_fil_2}/{@code p_fil_3},
 * a genuinely awkward one-letter token, {@code poteryannoe} -- from {@code poteryannoe_vosp_2},
 * {@code romeo}, {@code skalolaz} -- no underscore at all, {@code terentii}, {@code terentiy} --
 * a real spelling-variant split (almost certainly the same character, transliterated two ways
 * across 3 files -- see {@code terentii_na_okraine}/{@code terentii_verse} vs.
 * {@code terentiy_skuchno}, not merged here since that would be interpreting, not observing),
 * {@code tim}, {@code vospominanie}). Reporting these small groups as-is, rather than folding
 * them into a guessed "real" arc, is the point -- see this project's rule 1.
 */
public final class NpcIdGrouping {

    private NpcIdGrouping() {
    }

    /** The substring before the first {@code _}, or the whole id if it has none (e.g. {@code skalolaz_monologue} -> {@code skalolaz}). */
    public static String rawPrefixToken(String npcId) {
        int idx = npcId.indexOf('_');
        return idx < 0 ? npcId : npcId.substring(0, idx);
    }

    /** Real npcIds grouped by {@link #rawPrefixToken}, tokens sorted alphabetically, each group's members sorted. */
    public static Map<String, List<String>> groupByRawPrefixToken(Collection<String> npcIds) {
        Map<String, List<String>> groups = new TreeMap<>();
        for (String npcId : npcIds) {
            groups.computeIfAbsent(rawPrefixToken(npcId), k -> new ArrayList<>()).add(npcId);
        }
        for (List<String> members : groups.values()) {
            Collections.sort(members);
        }
        return groups;
    }
}
