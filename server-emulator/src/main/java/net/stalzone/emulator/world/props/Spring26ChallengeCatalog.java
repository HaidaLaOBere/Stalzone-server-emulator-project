package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 167. Catalogs a real, previously-uncatalogued 41-line namespace found by a fresh full-file
 * re-scan of {@code stalker/lang/en.lang} against this project's current install snapshot (per
 * part 165/166's closing-note option to do exactly this once part 123's original census closed
 * out) -- 41 real top-level keys starting bare with {@code spring26/challenges/}, with NO leading
 * dotted namespace prefix at all. Confirmed genuinely new territory, not a re-find: a direct grep
 * of this project's entire source tree for the literal string {@code spring26} before this part
 * turned up nothing.
 *
 * <p>This is a REAL THIRD, structurally distinct bucket that merely shares the English word
 * "challenge(s)" with two already-covered, unrelated real namespaces -- named accordingly to avoid
 * confusion, the same discipline {@link ChallengeCatalog}'s own doc already used for the same
 * reason: {@link ChallengesCatalog} (plural, 1,011 real lines, part 116) and {@link
 * ChallengeCatalog} (singular, 17 real lines, part 152, real ids embedding a literal {@code /}) are
 * both real DOTTED namespaces ({@code challenges.*}/{@code challenge.*}); THIS namespace has no
 * leading dotted prefix whatsoever -- the literal string {@code spring26/challenges/} is itself
 * the start of the key, confirmed by direct count (0 of the 41 real lines have a dot before that
 * point).
 *
 * <p>Real shape: {@code spring26/challenges/<id>.name} for all 41 lines -- {@code name} is the
 * ONLY real field suffix found (confirmed by direct count: 41 of 41). Real ids embed one or more
 * {@code /} characters, same as {@link ChallengeCatalog}'s already-established real ids -- handled
 * the identical way, without decomposing them further, by the same generic last-dot-split (id,
 * field) table this project uses throughout.
 *
 * <p><b>Real content, reconciled exactly: 3 + 3 + 22 + 4 + 9 = 41.</b> (An earlier draft of this
 * doc miscounted the reichstag/season groups as 24/7 -- the two errors happened to cancel out in
 * the 41 total, which is exactly why this project always live-verifies counts against the real
 * file directly rather than trusting arithmetic alone; see this class's own test for the corrected,
 * live-verified figures.)
 * <ul>
 * <li>{@code ships_artefacts_episode/1-3} (3 lines) and {@code tanks_artefacts_episode/1-3} (3
 * lines) name real single-artifact hunt objectives -- e.g. {@code ships_artefacts_episode/1.name}
 * ="Find the Jelly artifact". All 4 real artifact names referenced across these 6 lines
 * ("Jelly"/"Loop"/"Link"/"Flicker") are a CONFIRMED, byte-exact crosswalk to part 121's {@link
 * ItemLangCatalog} real {@code item.art.<id>.name} entries: {@code lesta_jele}="Jelly", {@code
 * lesta_petlya}="Loop", {@code lesta_tracks}="Link", {@code lesta_spheretank}="Flicker" -- not
 * assumed, checked line-for-line.
 * <li>{@code reichstag/<n>} (22 lines, real ids 1,3,5,7,9,12,14-17,19-30 -- a genuinely
 * non-contiguous real numbering, disclosed as-is, not assumed complete) names real per-class-level
 * and per-match PvP challenge objectives for the "Assault on the Reichstag" mode. Per this
 * project's own STANDING NOTE (part 109, {@code Memory/MEMORY.md}): this is simply ANOTHER real,
 * independent corroboration of the long-open Reichstag battlefield/event, with NO new sequential
 * ordinal asserted for how many corroborations now exist.
 * <li>{@code fools_day/1-4} (4 lines) names a real April Fools'-Day PvP event with real
 * intentionally comedic copy, preserved verbatim, not cleaned up: {@code fools_day/2.name}="Play 3
 * games in April Fools' mode (please)", {@code fools_day/3.name}="Why was I given a butterfly
 * knife?", {@code fools_day/4.name}="M-m-monster Kill!".
 * <li>{@code season/insideout/<n>} (9 lines, real ids 1,3-10 -- id 2 is a real, disclosed gap, not
 * assumed) names real 2026 objectives for a "centrifuge" mechanic at a real named location,
 * "Forge-11", tied to the real "Inside Out" event line this project already covers in depth (part
 * 139's {@code ItemOriginsCatalog}, part 154's {@link InsideCatalog}, part 158's {@link
 * SeasonCatalog}'s real {@code 25/spring}="Back to the Inside Out" season) -- this namespace is
 * this project's first real evidence of a 2026 continuation/sequel to that same real event line,
 * disclosed as a real narrative continuation, not asserted to share any id/key structure with the
 * earlier real 2025 catalogs.
 * </ul>
 *
 * <p>Per this project's standing rule to track new content rather than re-extract for live
 * rebalancing: this namespace's real existence (evidenced fresh against the current
 * "Stalzone july 29.2026" install, absent from this project's much earlier original {@code
 * en.lang} census) is itself the finding -- a real seasonal content addition, not a stat change.
 */
public final class Spring26ChallengeCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private Spring26ChallengeCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "spring26/challenges/";

    public static Spring26ChallengeCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue; // no real line in this namespace has ever lacked a field suffix
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new Spring26ChallengeCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }

    /** Real ids that have at least one real field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real ids whose id string starts with {@code group + "/"} (e.g. {@code "reichstag/"}), sorted -- a convenience for this namespace's real sub-families. */
    public List<String> idsInGroup(String group) {
        String groupPrefix = group + "/";
        List<String> out = new ArrayList<>();
        for (String id : fieldsById.keySet()) {
            if (id.startsWith(groupPrefix)) {
                out.add(id);
            }
        }
        Collections.sort(out);
        return out;
    }

    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }
}
