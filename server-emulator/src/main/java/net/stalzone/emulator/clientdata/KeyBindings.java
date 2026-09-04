package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Loads the real local client-state file {@code config/keybindings} -- plain JSON, a flat object
 * mapping an action name to a list of real keycode ints (0-2 entries seen: unbound, one binding,
 * or a primary+secondary pair). 314 real action names confirmed on a real, live install.
 *
 * <p><b>Two real, distinct namespaces of action names in this one file, NOT previously documented
 * anywhere in this project</b>:
 * <ul>
 *   <li>109 real actions prefixed {@code key_key.} -- ordinary gameplay binds:
 *       {@code key_key.attack}, {@code key_key.inventory}, {@code key_key.use}, etc. These are
 *       what {@link #gameplayActionNames()} returns.</li>
 *   <li><b>205 real actions with NO {@code key_key.} prefix, in a mix of English and Russian,
 *       naming what is unmistakably an internal developer/QA tool suite shipped inside the real
 *       client</b>: editors ({@code Редактор мутантов} "Mutant Editor", {@code Редактор предметов}
 *       "Item Editor", {@code Редактор погоды} "Weather Editor", {@code Редактор варпов} "Warp
 *       Editor", Scene Editor, Behavior Editor), debug overlays/HUDs (FPS, memory, FMOD, player
 *       position, navmesh, collision boxes), config reload actions ({@code Перезагрузить чанки}
 *       "Reload Chunks", {@code Перезагрузить предметы} "Reload Items"), a "No clip" toggle, and
 *       result/debug screens for named real systems this project has already found evidence of
 *       elsewhere (Prestige divisions, Operations, Arcade Team Battles, case/loot drop-rate
 *       tables). This is real, first-hand evidence of "possible cut/hidden content" in the sense
 *       the user asked about at project start -- developer tooling, not gameplay content, and NOT
 *       anti-cheat/launcher internals (rule 2's exclusion), so it's in scope to document. {@link
 *       #nonGameplayActionNames()} returns these, VERBATIM as they appear in the file (including
 *       the Russian text) -- this project has no evidence of a canonical/English name for the
 *       Russian-named ones, so none is invented.</li>
 * </ul>
 *
 * <p><b>This class only CATALOGS these real names and their real bound keycodes -- it does not
 * implement, invoke, expose a way to trigger, or otherwise unlock any of them.</b> There is no
 * code anywhere in this project that opens a mutant editor, disables collision, or reloads
 * server-side config; a real keybinding existing in a real config file is a documented fact about
 * the client, not a feature this emulator provides.
 *
 * <p><b>Real, unconfirmed detail</b>: 4 distinct negative keycodes appear ({@code -1} through
 * {@code -4}, e.g. on {@code key_key.use}/{@code key_key.pick_item}/{@code
 * key_key.editor.backward}/{@code key_key.editor.forward}) alongside ordinary positive keycodes
 * elsewhere. No source found anywhere in this project decodes what a negative keycode means
 * (mouse-wheel direction and "unassigned" sentinel are both plausible, neither confirmed) -- kept
 * as the raw signed int, not translated to a guessed meaning.
 */
public final class KeyBindings {

    private final Map<String, List<Integer>> bindings;

    private KeyBindings(Map<String, List<Integer>> bindings) {
        this.bindings = bindings;
    }

    @SuppressWarnings("unchecked")
    public static KeyBindings load(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof Map)) {
            throw new IOException(path + ": expected a top-level JSON object, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }
        Map<String, Object> raw = (Map<String, Object>) parsed;
        Map<String, List<Integer>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            List<Integer> codes = new ArrayList<>();
            if (e.getValue() instanceof List) {
                for (Object v : (List<Object>) e.getValue()) {
                    if (v instanceof Double) {
                        codes.add(((Double) v).intValue());
                    }
                }
            }
            out.put(e.getKey(), Collections.unmodifiableList(codes));
        }
        return new KeyBindings(out);
    }

    public int size() {
        return bindings.size();
    }

    /** Every real action name in this file, exactly as it appears (raw JSON key, including the {@code key_} prefix). */
    public Set<String> actionNames() {
        return Collections.unmodifiableSet(bindings.keySet());
    }

    /** Real bound keycodes for one action, or an empty list if unbound / the action doesn't exist. */
    public List<Integer> codesFor(String actionName) {
        return bindings.getOrDefault(actionName, Collections.emptyList());
    }

    public boolean isBound(String actionName) {
        return !codesFor(actionName).isEmpty();
    }

    /** True for the 109 real {@code key_key.*}-prefixed ordinary gameplay actions -- see class doc. */
    public boolean isGameplayAction(String actionName) {
        return actionName.startsWith("key_key.");
    }

    /** The 109 real ordinary gameplay action names, sorted. */
    public Set<String> gameplayActionNames() {
        Set<String> out = new TreeSet<>();
        for (String name : bindings.keySet()) {
            if (isGameplayAction(name)) {
                out.add(name);
            }
        }
        return out;
    }

    /** The 205 real non-gameplay (developer/debug-tool) action names, sorted -- see class doc for what this data means and doesn't mean. */
    public Set<String> nonGameplayActionNames() {
        Set<String> out = new TreeSet<>();
        for (String name : bindings.keySet()) {
            if (!isGameplayAction(name)) {
                out.add(name);
            }
        }
        return out;
    }

    /** Count of real actions with zero bound keycodes (178 of 314 on the real install this was validated against). */
    public int unboundActionCount() {
        int count = 0;
        for (List<Integer> codes : bindings.values()) {
            if (codes.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "KeyBindings{" + bindings.size() + " actions, " + gameplayActionNames().size()
                + " gameplay, " + nonGameplayActionNames().size() + " non-gameplay}";
    }
}
