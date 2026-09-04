package net.stalzone.emulator.clientdata;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * CLI demo/inspection tool covering all five real local client-state files this package reads:
 * {@code config/quests.cfg} ({@link QuestProgress}), {@code config/waypoints.cfg} ({@link
 * WaypointList}), {@code config/keybindings} ({@link KeyBindings}), {@code config/options.json}
 * ({@link ClientOptions}, part 44), and {@code config/prefs} ({@link ClientPrefs}, part 45). One
 * combined tool rather than five, since each individual file is small and a server operator
 * inspecting one of these on a real install is likely poking at the whole real {@code config/}
 * directory at once.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool quests &lt;quests.cfg&gt;
 *   java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool waypoints &lt;waypoints.cfg&gt;
 *   java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool keybindings &lt;keybindings&gt;
 *   java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool options &lt;options.json&gt;
 *   java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool prefs &lt;prefs&gt;
 */
public final class ClientDataDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 2) {
            System.err.println("usage: ClientDataDumpTool <quests|waypoints|keybindings|options|prefs> <file path>");
            System.exit(2);
        }

        String mode = args[0];
        Path path = Path.of(args[1]);

        switch (mode) {
            case "quests": {
                QuestProgress progress = QuestProgress.load(path);
                System.out.println("Loaded " + path + ": " + progress);
                System.out.println("readQuests (" + progress.readQuestIds().size() + "): " + progress.readQuestIds());
                System.out.println("hiddenQuests (" + progress.hiddenQuestIds().size() + "): " + progress.hiddenQuestIds());
                break;
            }
            case "waypoints": {
                List<Waypoint> waypoints = WaypointList.load(path);
                System.out.println("Loaded " + path + ": " + waypoints.size() + " waypoint(s)");
                for (Waypoint w : waypoints) {
                    System.out.println("  " + w + " name=\"" + w.name() + "\" icon=" + w.iconIndex()
                            + " color=" + w.color() + " canPositionFloat=" + w.canPositionFloat());
                }
                break;
            }
            case "keybindings": {
                KeyBindings bindings = KeyBindings.load(path);
                System.out.println("Loaded " + path + ": " + bindings);
                System.out.println("unbound actions: " + bindings.unboundActionCount() + " / " + bindings.size());
                System.out.println();
                System.out.println("-- gameplay actions (key_key.*) --");
                for (String name : bindings.gameplayActionNames()) {
                    if (bindings.isBound(name)) {
                        System.out.println("  " + name + " -> " + bindings.codesFor(name));
                    }
                }
                System.out.println();
                System.out.println("-- non-gameplay (developer/debug-tool) actions, bound ones only --");
                System.out.println("   (cataloged as real data only -- this project implements/invokes none of them, see class doc)");
                for (String name : bindings.nonGameplayActionNames()) {
                    if (bindings.isBound(name)) {
                        System.out.println("  " + name + " -> " + bindings.codesFor(name));
                    }
                }
                break;
            }
            case "options": {
                ClientOptions options = ClientOptions.load(path);
                System.out.println("Loaded " + path + ": " + options);
                System.out.println();
                for (String key : options.keys()) {
                    System.out.println("  " + key + " = " + options.getRaw(key));
                }
                System.out.println();
                System.out.println("-- real float-bits / packed-color keys (see ClientOptions class doc) --");
                if (options.has("admin_hotbar_scale")) {
                    System.out.println("  admin_hotbar_scale as float bits = " + options.getFloatBits("admin_hotbar_scale"));
                }
                if (options.has("admin_inv_icon_scale")) {
                    System.out.println("  admin_inv_icon_scale as float bits = " + options.getFloatBits("admin_inv_icon_scale"));
                }
                if (options.has("world_outliner_width")) {
                    System.out.println("  world_outliner_width as float bits = " + options.getFloatBits("world_outliner_width"));
                }
                if (options.has("world_outliner_color")) {
                    int[] argb = options.getArgbComponents("world_outliner_color");
                    System.out.println("  world_outliner_color as ARGB = alpha=" + argb[0] + " r=" + argb[1] + " g=" + argb[2] + " b=" + argb[3]);
                }
                break;
            }
            case "prefs": {
                ClientPrefs prefs = ClientPrefs.load(path);
                System.out.println("Loaded " + path + ": " + prefs);
                System.out.println();
                for (Map.Entry<String, Object> e : prefs.rawFields().entrySet()) {
                    System.out.println("  " + e.getKey() + " = " + e.getValue());
                }
                break;
            }
            default:
                System.err.println("unknown mode \"" + mode + "\" -- expected quests, waypoints, keybindings, options, or prefs");
                System.exit(2);
        }
    }
}
