package net.stalzone.emulator.mobs;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads every {@code *.smm} mob/mutant config from a real
 * {@code modassets/assets/stalkermobs/configs/} folder (35 files confirmed on this install, see
 * docs/RESEARCH_LOG.md part 10 and part 20) into {@link MobConfig}s indexed by {@code mobName}.
 *
 * Two of the 35 real files ({@code base_puppet.smm}, {@code base_puppet_clone.smm}) contain a
 * stray {@code //} line comment left in by a developer (a Russian-language note reading roughly
 * "remove walk if we don't want it to walk", attached to the "walk" animation entry) -- standard
 * JSON has no comment syntax, so a strict parser (this project's {@link JsonReader} included)
 * rejects them as-is. This is confirmed by direct inspection: those are the ONLY two `//`
 * occurrences across all 35 files, both are genuine trailing comments (not `//` inside a string
 * value, e.g. a URL, which would need to be left alone), so a string-aware pass that strips only
 * `//...end-of-line` sequences OUTSIDE quoted strings is a safe, minimal, and honest fix -- not a
 * guess at intent, just tolerating a real formatting quirk this file set actually has.
 */
public final class MobRegistry {

    private final List<MobConfig> all;
    private final Map<String, MobConfig> byConfigId;
    private final Map<String, List<MobConfig>> byMobName;

    private MobRegistry(List<MobConfig> all, Map<String, MobConfig> byConfigId, Map<String, List<MobConfig>> byMobName) {
        this.all = all;
        this.byConfigId = byConfigId;
        this.byMobName = byMobName;
    }

    @SuppressWarnings("unchecked")
    public static MobRegistry load(Path configsDir) throws IOException {
        List<MobConfig> all = new ArrayList<>();
        Map<String, MobConfig> byConfigId = new LinkedHashMap<>();
        Map<String, List<MobConfig>> byMobName = new LinkedHashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(configsDir, "*.smm")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String configId = fileName.endsWith(".smm") ? fileName.substring(0, fileName.length() - 4) : fileName;

                String rawText = Files.readString(path, StandardCharsets.UTF_8);
                String cleanText = stripLineComments(rawText);
                Object parsed = JsonReader.parse(cleanText);
                if (!(parsed instanceof Map)) {
                    throw new IOException(path + ": expected a top-level JSON object, got "
                            + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
                }
                MobConfig config = new MobConfig(configId, (Map<String, Object>) parsed);
                all.add(config);
                byConfigId.put(configId, config);
                if (config.mobName() != null) {
                    byMobName.computeIfAbsent(config.mobName(), k -> new ArrayList<>()).add(config);
                }
            }
        }

        return new MobRegistry(all, byConfigId, byMobName);
    }

    /**
     * Strips {@code //...} line comments that appear outside of JSON string literals, leaving
     * everything inside strings (including a literal {@code //} inside one, e.g. a URL) intact.
     * Tracks quote state and backslash-escapes character by character rather than using a regex,
     * since a regex can't correctly tell "inside a string" from "outside" for arbitrary escaped
     * content.
     */
    static String stripLineComments(String text) {
        StringBuilder out = new StringBuilder(text.length());
        boolean inString = false;
        boolean escaped = false;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }
                continue; // leave the newline itself to be appended on the next loop iteration
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    public int size() {
        return all.size();
    }

    public List<MobConfig> all() {
        return Collections.unmodifiableList(all);
    }

    /** Lookup by source filename stem, e.g. "base_boar" or "base_tsarevich" -- always unique, one entry per real file. */
    public MobConfig byConfigId(String configId) {
        return byConfigId.get(configId);
    }

    /**
     * Lookup by the config's own {@code mobName} field, e.g. "boar" or "tushkan". NOT unique --
     * see {@link MobConfig#configId()} for why -- so this returns every config sharing that name
     * (for "tushkan" that's 3 configs: base_tick, base_tushkan, base_tushkan_clone). Empty list
     * if no config declares that mobName.
     */
    public List<MobConfig> byMobName(String mobName) {
        List<MobConfig> found = byMobName.get(mobName);
        return found != null ? Collections.unmodifiableList(found) : Collections.emptyList();
    }
}
