package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Part 168. Catalogs two real, closely related {@code en.lang} namespaces found by continuing part
 * 167's fresh full-file re-scan: {@code stalkerguide.*} (26 real lines) and {@code
 * stalkerguide2019.*} (28 real lines) -- both real new-player onboarding/tutorial prompt text
 * (move, stand up, mark a target, draw/reload a weapon, loot a corpse, open inventory, use an
 * elevator/ammo crate). Named {@code TutorialGuideCatalog}, deliberately NOT {@code
 * StalkerGuideCatalog}, to avoid confusion with this project's already-existing, completely
 * unrelated {@link StalkerGuideCatalog} (part 59) -- that class catalogs real FILE-based
 * story/cutscene assets under {@code modassets/assets/stalkerguide/} (video/audio/texture files),
 * a totally different real thing that merely happens to share the English word "guide"; confirmed
 * disjoint by direct content comparison, not assumed from the name alone.
 *
 * <p>Real keys in both namespaces vary in dot-depth in a way this project's usual generic
 * last-dot-split {@code (id, field)} table does not fit cleanly (e.g. {@code
 * quest.weaponuse.2.text.reload} vs. {@code quest.getup.header} vs. {@code
 * controlpoints.control_point.ammo_crate}), so this class deliberately does NOT attempt that
 * split. Instead each namespace is a flat {@code Map<String,String>} keyed by the exact real
 * string after the version prefix -- honest to the real irregular shape rather than forcing an
 * artificial id/field boundary onto it.
 *
 * <p><b>Real content, reconciled exactly: 24 quest + 2 controlpoints = 26 (current); 27 quest + 1
 * controlpoints = 28 ({@code 2019}).</b> Direct key-set comparison between the two real namespaces
 * finds BOTH real overlap and real, disclosed asymmetry -- not a simple superset/subset or a clean
 * versioned duplicate:
 * <ul>
 * <li>Two real keys exist ONLY in the current namespace, confirmed by direct set comparison, not
 * sampling: {@code controlpoints.control_point.ammo_crate} (real text: "Hover over the ammo crate
 * and hold [#key.interact] to replenish your ammo") -- {@code stalkerguide2019} has no ammo-crate
 * step at all -- and {@code quest.walkaround.header}="Moving", a header {@code stalkerguide2019}'s
 * own {@code quest.walkaround.text} step never got.
 * <li>Four real keys exist ONLY in {@code stalkerguide2019}, confirmed the same way: {@code
 * quest.medkit_pre.*}/{@code quest.medkit.*} (a first-aid-kit pickup/use tutorial step) -- the
 * current namespace skips this step entirely.
 * <li>For several real step ids present in BOTH namespaces, the actual real text differs, not just
 * the surrounding key: {@code quest.getup.header} is "Ascent" (current) vs. "Stand Up" ({@code
 * 2019}); {@code quest.weaponuse.3.desc.killed} is "Destroy {total number} enemies" (current) vs.
 * "Targets killed ({killed number}/{total number})" ({@code 2019}, a different real template-
 * variable style entirely). Other shared steps ({@code quest.getup.text}, {@code
 * quest.walkaround.text}, several {@code quest.inventory.*} lines) are real byte-identical text
 * between the two namespaces.
 * </ul>
 *
 * <p>What the {@code 2019} suffix real means is NOT confirmed anywhere in this project's evidence.
 * A plausible reading -- consistent with, but not proven by, part 163's real finding that this
 * game is presented in-fiction as STALZONE's own predecessor/rebrand of an earlier project,
 * "STALCRAFT" -- is that {@code stalkerguide2019} is a real, retained LEGACY tutorial-text version
 * still shipped in this client build alongside the current one, rather than an active duplicate.
 * This class asserts only the literal observed real facts above; it does not assert which
 * namespace is actually shown to a real player, or when/why the wording diverged.
 */
public final class TutorialGuideCatalog {

    private static final String CURRENT_PREFIX = "stalkerguide.";
    private static final String LEGACY_2019_PREFIX = "stalkerguide2019.";

    private final Map<String, String> current;
    private final Map<String, String> legacy2019;

    private TutorialGuideCatalog(Map<String, String> current, Map<String, String> legacy2019) {
        this.current = current;
        this.legacy2019 = legacy2019;
    }

    public static TutorialGuideCatalog load(LangCatalog catalog) {
        Map<String, String> current = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(CURRENT_PREFIX)) {
            // Real data quirk, not a bug: every real "stalkerguide2019.*" key ALSO starts with the
            // literal prefix "stalkerguide" but never "stalkerguide." (the very next real character
            // is always the digit '2', never '.') -- so this exact-dot prefix match cannot
            // accidentally pull a real 2019-namespace key into the current table. Confirmed by
            // direct count, not assumed from the two literals merely looking similar.
            current.put(key.substring(CURRENT_PREFIX.length()), catalog.get(key));
        }
        Map<String, String> legacy2019 = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(LEGACY_2019_PREFIX)) {
            legacy2019.put(key.substring(LEGACY_2019_PREFIX.length()), catalog.get(key));
        }
        return new TutorialGuideCatalog(current, legacy2019);
    }

    /** Real current-namespace text for this exact real key suffix (e.g. {@code "quest.getup.header"}), or null if unknown. */
    public String current(String keySuffix) {
        return current.get(keySuffix);
    }

    /** Real {@code stalkerguide2019} text for this exact real key suffix, or null if unknown. */
    public String legacy2019(String keySuffix) {
        return legacy2019.get(keySuffix);
    }

    public List<String> currentKeys() {
        List<String> out = new ArrayList<>(current.keySet());
        Collections.sort(out);
        return out;
    }

    public List<String> legacy2019Keys() {
        List<String> out = new ArrayList<>(legacy2019.keySet());
        Collections.sort(out);
        return out;
    }

    /** Real key suffixes present in the current namespace but NOT in {@code stalkerguide2019} -- see class doc for the confirmed real example ({@code controlpoints.control_point.ammo_crate}). */
    public List<String> onlyInCurrent() {
        List<String> out = new ArrayList<>();
        for (String key : current.keySet()) {
            if (!legacy2019.containsKey(key)) {
                out.add(key);
            }
        }
        Collections.sort(out);
        return out;
    }

    /** Real key suffixes present in {@code stalkerguide2019} but NOT in the current namespace -- see class doc for the confirmed real example ({@code quest.medkit_pre.*}/{@code quest.medkit.*}). */
    public List<String> onlyInLegacy2019() {
        List<String> out = new ArrayList<>();
        for (String key : legacy2019.keySet()) {
            if (!current.containsKey(key)) {
                out.add(key);
            }
        }
        Collections.sort(out);
        return out;
    }

    /** Real key suffixes present in BOTH namespaces -- use {@link #current}/{@link #legacy2019} on each to compare the actual real text, which for some of these real keys differs (see class doc). */
    public List<String> inBothVersions() {
        java.util.Set<String> both = new TreeSet<>(current.keySet());
        both.retainAll(legacy2019.keySet());
        return new ArrayList<>(both);
    }
}
