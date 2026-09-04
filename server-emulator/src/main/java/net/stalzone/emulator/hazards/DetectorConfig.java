package net.stalzone.emulator.hazards;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code modassets/assets/stalker/detectors.txt} -- a small custom brace-delimited config
 * format (NOT JSON, NOT one of this project's other known formats), confirmed by direct
 * inspection (part 22): UTF-8 with a BOM, CRLF line endings, a flat sequence of
 * {@code <type>{ key: value; ... }} blocks with no nesting. 16 real blocks on this install: 6
 * {@code rad}, 5 {@code thr}, 5 {@code bio}, each one a {@link DetectorTier}.
 *
 * <p>This is a from-scratch hand-written recursive-descent-style scanner (not {@code JsonReader}
 * -- the format isn't JSON: bare unquoted type/field names, {@code key: value;} instead of
 * {@code "key": value,}, and blocks aren't wrapped in an array). Kept intentionally small and
 * specific to this one file's shape rather than generalized, the same way {@code MobRegistry}'s
 * comment stripper was kept specific to the one real quirk it needed to handle.
 */
public final class DetectorConfig {

    private final List<DetectorTier> allTiers;
    private final Map<String, List<DetectorTier>> tiersByType;

    private DetectorConfig(List<DetectorTier> allTiers, Map<String, List<DetectorTier>> tiersByType) {
        this.allTiers = allTiers;
        this.tiersByType = tiersByType;
    }

    public static DetectorConfig load(Path detectorsTxtPath) throws IOException {
        byte[] raw = Files.readAllBytes(detectorsTxtPath);
        String text = new String(raw, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') { // strip UTF-8 BOM
            text = text.substring(1);
        }
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        List<DetectorTier> allTiers = new ArrayList<>();
        Parser parser = new Parser(text, detectorsTxtPath);
        while (parser.hasMoreBlocks()) {
            allTiers.add(parser.parseBlock());
        }

        Map<String, List<DetectorTier>> tiersByType = new LinkedHashMap<>();
        for (DetectorTier tier : allTiers) {
            tiersByType.computeIfAbsent(tier.type, k -> new ArrayList<>()).add(tier);
        }
        for (List<DetectorTier> tiers : tiersByType.values()) {
            tiers.sort(Comparator.comparingInt((DetectorTier t) -> t.startDistance).reversed());
        }

        return new DetectorConfig(allTiers, tiersByType);
    }

    public int size() {
        return allTiers.size();
    }

    public List<DetectorTier> all() {
        return Collections.unmodifiableList(allTiers);
    }

    /** Hazard type names actually present in the file, e.g. ["rad", "thr", "bio"]. */
    public List<String> types() {
        return new ArrayList<>(tiersByType.keySet());
    }

    /** Every tier of this type, sorted by startDistance descending (farthest tier first). Empty list if the type isn't in the file. */
    public List<DetectorTier> tiersOf(String type) {
        List<DetectorTier> tiers = tiersByType.get(type);
        return tiers != null ? Collections.unmodifiableList(tiers) : Collections.emptyList();
    }

    /**
     * The tier that applies at this distance from a hazard of the given type: the tier with the
     * largest {@code startDistance} that is still &lt;= {@code distance} (i.e. the closest-matching
     * threshold the player has crossed). Returns null if the type is unknown or the player is
     * farther away than every tier's startDistance (no detector reaction yet).
     */
    public DetectorTier tierFor(String type, double distance) {
        DetectorTier best = null;
        for (DetectorTier tier : tiersOf(type)) {
            if (tier.startDistance <= distance && (best == null || tier.startDistance > best.startDistance)) {
                best = tier;
            }
        }
        return best;
    }

    // ---- minimal hand-written scanner for this one file's brace/key-value shape ----

    private static final class Parser {
        private final String text;
        private final Path sourcePath;
        private int pos;

        Parser(String text, Path sourcePath) {
            this.text = text;
            this.sourcePath = sourcePath;
        }

        boolean hasMoreBlocks() {
            skipWhitespace();
            return pos < text.length();
        }

        DetectorTier parseBlock() throws IOException {
            skipWhitespace();
            String type = readIdentifier();
            skipWhitespace();
            expect('{');

            Integer startDistance = null;
            List<String> soundNames = null;
            Double periodBase = null;
            Double periodRandom = null;
            Double pitchMin = null;
            Double pitchMax = null;

            while (true) {
                skipWhitespace();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = readIdentifier();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String rawValue = readValueUntilSemicolon();
                expect(';');

                switch (key) {
                    case "start_distance": startDistance = (int) Double.parseDouble(rawValue.trim()); break;
                    case "sound_names": soundNames = splitSoundNames(unquote(rawValue.trim())); break;
                    case "period_base": periodBase = Double.parseDouble(rawValue.trim()); break;
                    case "period_random": periodRandom = Double.parseDouble(rawValue.trim()); break;
                    case "pitch_min": pitchMin = Double.parseDouble(rawValue.trim()); break;
                    case "pitch_max": pitchMax = Double.parseDouble(rawValue.trim()); break;
                    default:
                        throw new IOException(sourcePath + ": unrecognized field \"" + key + "\" at offset " + pos);
                }
            }

            if (startDistance == null || soundNames == null || periodBase == null || periodRandom == null) {
                throw new IOException(sourcePath + ": block of type \"" + type + "\" is missing a required field");
            }
            return new DetectorTier(type, startDistance, soundNames, periodBase, periodRandom, pitchMin, pitchMax);
        }

        private static List<String> splitSoundNames(String value) {
            List<String> names = new ArrayList<>();
            for (String part : value.split("@")) {
                if (!part.isEmpty()) {
                    names.add(part);
                }
            }
            return names;
        }

        private static String unquote(String value) {
            if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

        private String readIdentifier() throws IOException {
            int start = pos;
            while (pos < text.length() && (Character.isLetterOrDigit(text.charAt(pos)) || text.charAt(pos) == '_')) {
                pos++;
            }
            if (pos == start) {
                throw new IOException(sourcePath + ": expected identifier at offset " + pos);
            }
            return text.substring(start, pos);
        }

        /** Reads up to (not including) the next top-level ';', respecting a quoted string so a ';' inside quotes wouldn't end the value (none occur in the real file, but this is the safe way to scan a delimited value). */
        private String readValueUntilSemicolon() throws IOException {
            int start = pos;
            boolean inQuotes = false;
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ';' && !inQuotes) {
                    break;
                }
                pos++;
            }
            if (pos >= text.length()) {
                throw new IOException(sourcePath + ": unterminated value starting at offset " + start);
            }
            return text.substring(start, pos);
        }

        private void expect(char c) throws IOException {
            if (pos >= text.length() || text.charAt(pos) != c) {
                throw new IOException(sourcePath + ": expected '" + c + "' at offset " + pos);
            }
            pos++;
        }

        private char peek() throws IOException {
            if (pos >= text.length()) {
                throw new IOException(sourcePath + ": unexpected end of input");
            }
            return text.charAt(pos);
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }
    }
}
