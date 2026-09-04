package net.stalzone.emulator.tips;

import java.nio.file.Path;

/**
 * CLI demo for {@link AdviceList}. Loads one real {@code advices_<locale>.txt} file and prints
 * every real tip string with its index.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.tips.AdviceListDumpTool <advices_en.txt|advices_ru.txt>
 */
public final class AdviceListDumpTool {

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: AdviceListDumpTool <advices_en.txt|advices_ru.txt>");
            System.exit(2);
        }

        Path path = Path.of(args[0]);
        AdviceList advices = AdviceList.load(path);
        System.out.println("Loaded " + advices.size() + " tip(s) from " + path);
        for (int i = 0; i < advices.tips().size(); i++) {
            System.out.println("  [" + i + "] " + advices.tips().get(i));
        }
    }
}
