package net.stalzone.emulator.anomalies;

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
 * Loads {@code modassets/assets/anomalies/morse_alphabet.txt} -- a real Morse code table (82
 * real entries: 33 Cyrillic letters incl. `ё`, 26 Latin letters, 10 digits, 13 punctuation marks)
 * that almost certainly backs an in-game radio/signal-decoding activity in the `anomalies`
 * namespace -- a genuine "activity" per the user's stated interest in game content, not
 * background/launcher internals, even though it's small.
 *
 * <p>Confirmed real format by direct inspection (part 24): UTF-8 with CRLF line endings, each
 * line exactly {@code <char><4 spaces><morse code>} (confirmed via hex dump: 4 literal space
 * bytes, not a tab), no header, no comments.
 *
 * <p><b>Real, disclosed quirk -- not a parsing bug</b>: character-&gt;code is a clean 1:1 map (no
 * character repeats), but the reverse direction is genuinely ambiguous -- 27 Morse codes are
 * shared by more than one character, because Cyrillic and Latin letters that trace back to the
 * same telegraphic origin share a code (e.g. Cyrillic `а` and Latin `a` are both {@code .-};
 * Cyrillic `ц` and Latin `c` are both {@code -.-.}). One code, {@code .} (a single dot), is
 * shared by THREE characters: Cyrillic `е`, Cyrillic `ё`, and Latin `e`. Punctuation has one too:
 * `(` and `)` share {@code -.--.-} (the real ITU convention -- Morse has no separate closing-
 * parenthesis prosign). {@link #charsFor(String)} returns every match rather than picking one,
 * consistent with this project's "disclose an ambiguity, don't silently resolve it" approach
 * used for {@code ItemWeightTable}'s conflicting ids and {@code ItemGroups}' multi-group ids.
 */
public final class MorseAlphabet {

    private final Map<Character, String> codeByChar;
    private final Map<String, List<Character>> charsByCode;

    private MorseAlphabet(Map<Character, String> codeByChar, Map<String, List<Character>> charsByCode) {
        this.codeByChar = codeByChar;
        this.charsByCode = charsByCode;
    }

    public static MorseAlphabet load(Path morseAlphabetTxtPath) throws IOException {
        List<String> lines = Files.readAllLines(morseAlphabetTxtPath, StandardCharsets.UTF_8);

        Map<Character, String> codeByChar = new LinkedHashMap<>();
        Map<String, List<Character>> charsByCode = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = stripTrailingCr(rawLine);
            if (line.isEmpty()) {
                continue;
            }
            int sep = line.indexOf("    ");
            if (sep != 1) {
                // Every real line is exactly one character followed by 4 spaces -- if that
                // shape doesn't hold, this isn't a line we understand well enough to guess at.
                throw new IOException(morseAlphabetTxtPath + ": unrecognized line shape: \"" + line + "\"");
            }
            char ch = line.charAt(0);
            String code = line.substring(sep + 4);
            if (code.isEmpty()) {
                throw new IOException(morseAlphabetTxtPath + ": empty code for character '" + ch + "'");
            }
            if (codeByChar.containsKey(ch)) {
                throw new IOException(morseAlphabetTxtPath + ": duplicate character '" + ch + "'");
            }

            codeByChar.put(ch, code);
            charsByCode.computeIfAbsent(code, k -> new ArrayList<>()).add(ch);
        }

        return new MorseAlphabet(codeByChar, charsByCode);
    }

    private static String stripTrailingCr(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    public int size() {
        return codeByChar.size();
    }

    /** This character's Morse code, or null if it isn't in the table. */
    public String codeOf(char ch) {
        return codeByChar.get(ch);
    }

    /**
     * Every character that encodes to this Morse code, in file order. Usually size 1; see the
     * class doc comment for the 27 real codes shared by more than one character. Empty if the
     * code isn't in the table at all.
     */
    public List<Character> charsFor(String code) {
        List<Character> chars = charsByCode.get(code);
        return chars != null ? Collections.unmodifiableList(chars) : Collections.emptyList();
    }

    /** Every Morse code shared by more than one character (27 on the real file), in file order of first appearance. */
    public List<String> ambiguousCodes() {
        List<String> ambiguous = new ArrayList<>();
        for (Map.Entry<String, List<Character>> e : charsByCode.entrySet()) {
            if (e.getValue().size() > 1) {
                ambiguous.add(e.getKey());
            }
        }
        return ambiguous;
    }

    /** Every character in the table, in file order. */
    public List<Character> characters() {
        return new ArrayList<>(codeByChar.keySet());
    }

    /** Encodes a string char-by-char (space-separated codes; a character not in the table throws). Purely a convenience -- the real client's own encode/decode UX for this activity is unknown. */
    public String encode(String text) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String code = codeByChar.get(ch);
            if (code == null) {
                throw new IllegalArgumentException("no Morse code for character '" + ch + "' (index " + i + ")");
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(code);
        }
        return out.toString();
    }
}
