package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for {@link CurrencyCatalog}. Loads the real {@code currency} folder and prints the
 * total folder/file counts plus every folder matching a given substring query (the generic
 * mechanism this class uses to verify narrative claims against real data instead of a
 * hardcoded/hand-counted list -- see the class doc).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.world.props.CurrencyCatalogDumpTool <currency dir> [substring]
 */
public final class CurrencyCatalogDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: CurrencyCatalogDumpTool <currency dir> [substring]");
            System.exit(2);
        }

        Path currencyDir = Path.of(args[0]);
        CurrencyCatalog catalog = CurrencyCatalog.load(currencyDir);

        System.out.println("Real currency asset folders: " + catalog.totalFolderCount()
                + ", total real files: " + catalog.totalFileCount());

        if (args.length >= 2) {
            String substring = args[1];
            List<String> matches = catalog.foldersContaining(substring);
            System.out.println();
            System.out.println(matches.size() + " folder(s) with a file containing \"" + substring + "\": " + matches);
        } else {
            System.out.println();
            System.out.println("Folders: " + catalog.folderNames());
        }
    }
}
