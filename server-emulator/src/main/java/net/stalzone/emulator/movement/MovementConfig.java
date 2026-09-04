package net.stalzone.emulator.movement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code modassets/assets/stalcraft/moving.txt} -- a real, plain-text player-MOVEMENT
 * tuning config: walk/run/sprint/sneak/crawl speed factors, jump height/control factors per
 * jump type, climbing (free-climb, ladders, vines, ceilings) speed and angle tolerances, fall
 * damage thresholds, swim/dive speed, and a full stamina ("exhaustion") and hunger gain/loss
 * model with per-action rates. Confirmed real format by direct inspection (part 23): plain
 * UTF-8, LF line endings, no BOM. 238 {@code key:value} settings plus 3 trailing
 * {@code key=value} lines (each preceded by a genuine Russian-language developer comment,
 * {@code # ...}) -- a small, deliberate syntax switch late in the file, not a parsing error;
 * both separators are handled identically here.
 *
 * <p><b>Real "variant override" quirk, confirmed not guessed at</b>: 23 of the 238 keys have a
 * value shaped like {@code base;LETTER:override;LETTER:override...}, e.g.
 * {@code move.speed.user:false;c:true} (the base/survival value is {@code false}, but under
 * variant {@code c} it's {@code true}). The 4 real letters found -- {@code c}, {@code e},
 * {@code h}, {@code m} -- are exactly the ones the file's own
 * {@code move.config.survival.keys}/{@code creative.keys}/{@code adventure.keys} lines declare,
 * and {@code move.config.key.name:e:Easy;h:Hard;m:Medium} spells out their display names
 * directly: {@code c} = Creative game mode, {@code e}/{@code h} = Easy/Hard survival difficulty,
 * {@code m} = Medium difficulty (used as the Adventure-mode variant letter too). One key,
 * {@code move.config.key.name}, has ONLY variant pairs and no base value at all -- handled
 * honestly via {@link MovementSetting#baseValue()} returning null rather than inventing one.
 *
 * <p>Nothing in the emulator applies any of these values yet -- there's no player movement
 * simulation, no server-side position validation beyond what {@code netcode.log} already showed
 * happens client-side (part 14). This is a read/query layer only, the same status as this
 * project's other four data-layer classes ({@code WeaponRegistry}, {@code MobRegistry},
 * {@code ItemWeightTable}, {@code DetectorConfig}).
 */
public final class MovementConfig {

    private final Map<String, MovementSetting> settings;

    private MovementConfig(Map<String, MovementSetting> settings) {
        this.settings = settings;
    }

    public static MovementConfig load(Path movingTxtPath) throws IOException {
        List<String> lines = Files.readAllLines(movingTxtPath, StandardCharsets.UTF_8);
        Map<String, MovementSetting> settings = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int colon = line.indexOf(':');
            int equals = line.indexOf('=');
            int sep;
            if (colon < 0 && equals < 0) {
                throw new IOException(movingTxtPath + ": no ':' or '=' separator in line: \"" + line + "\"");
            } else if (colon < 0) {
                sep = equals;
            } else if (equals < 0) {
                sep = colon;
            } else {
                sep = Math.min(colon, equals);
            }

            String key = line.substring(0, sep);
            String valuePart = line.substring(sep + 1);
            if (settings.containsKey(key)) {
                throw new IOException(movingTxtPath + ": duplicate key \"" + key + "\"");
            }

            String[] tokens = valuePart.split(";", -1);
            String base = null;
            Map<Character, String> variants = new LinkedHashMap<>();
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                int tokenColon = token.indexOf(':');
                if (tokenColon == 1) {
                    // "<letter>:<value>" -- a per-variant override.
                    variants.put(token.charAt(0), token.substring(2));
                } else if (tokenColon >= 0) {
                    throw new IOException(movingTxtPath + ": unrecognized variant token \"" + token
                            + "\" in line: \"" + line + "\"");
                } else if (i == 0 && variants.isEmpty()) {
                    base = token; // may be the empty string -- that's a real, intentional value
                } else {
                    throw new IOException(movingTxtPath + ": unexpected plain token \"" + token
                            + "\" in line: \"" + line + "\"");
                }
            }

            settings.put(key, new MovementSetting(key, base, variants));
        }

        return new MovementConfig(settings);
    }

    public MovementSetting get(String key) {
        return settings.get(key);
    }

    public List<String> keys() {
        return new ArrayList<>(settings.keySet());
    }

    public int size() {
        return settings.size();
    }

    /** Convenience: this key's base value parsed as a double, or null if the key is missing/non-numeric/has no base. */
    public Double asDouble(String key) {
        MovementSetting s = settings.get(key);
        return s != null ? s.asDouble() : null;
    }

    public Double asDouble(String key, char variant) {
        MovementSetting s = settings.get(key);
        return s != null ? s.asDouble(variant) : null;
    }

    /** Convenience: this key's base value parsed as a boolean ("true"/"false"), or null if missing/not a boolean. */
    public Boolean asBoolean(String key) {
        MovementSetting s = settings.get(key);
        return s != null ? s.asBoolean() : null;
    }

    public Boolean asBoolean(String key, char variant) {
        MovementSetting s = settings.get(key);
        return s != null ? s.asBoolean(variant) : null;
    }

    /** Every distinct variant letter used anywhere in the file (real data: c, e, h, m -- see class doc comment). */
    public List<Character> variantLetters() {
        List<Character> letters = new ArrayList<>();
        for (MovementSetting s : settings.values()) {
            for (Character c : s.variants().keySet()) {
                if (!letters.contains(c)) {
                    letters.add(c);
                }
            }
        }
        Collections.sort(letters);
        return letters;
    }
}
