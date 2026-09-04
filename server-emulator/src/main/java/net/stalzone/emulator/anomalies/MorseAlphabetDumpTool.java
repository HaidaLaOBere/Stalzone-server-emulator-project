package net.stalzone.emulator.anomalies;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for MorseAlphabet, same spirit as the other *DumpTool classes. Loads the real
 * modassets/assets/anomalies/morse_alphabet.txt, prints its size and every ambiguous code
 * (shared by more than one character), and (if text is given on the command line) encodes it.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.anomalies.MorseAlphabetDumpTool <morse_alphabet.txt> [text-to-encode]
 */
public final class MorseAlphabetDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: MorseAlphabetDumpTool <morse_alphabet.txt> [text-to-encode]");
            System.exit(2);
        }

        Path morseTxt = Path.of(args[0]);
        MorseAlphabet alphabet = MorseAlphabet.load(morseTxt);
        System.out.println("Loaded " + alphabet.size() + " characters from " + morseTxt);

        List<String> ambiguous = alphabet.ambiguousCodes();
        System.out.println();
        System.out.println("Codes shared by more than one character (" + ambiguous.size() + "):");
        for (String code : ambiguous) {
            System.out.println("  " + code + " -> " + alphabet.charsFor(code));
        }

        if (args.length > 1) {
            String text = args[1].toLowerCase();
            System.out.println();
            System.out.println("Encoding \"" + args[1] + "\": " + alphabet.encode(text));
        }
    }
}
