package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for {@link PdaMapCatalog}. Loads the real {@code pda} folder and prints the total
 * folder/file counts plus every folder matching a given substring query (the generic mechanism this
 * class uses to verify narrative claims against real data instead of a hardcoded/hand-counted list --
 * see the class doc).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.world.props.PdaMapCatalogDumpTool <pda dir> [substring]
 */
public final class PdaMapCatalogDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: PdaMapCatalogDumpTool <pda dir> [substring]");
            System.exit(2);
        }

        Path pdaDir = Path.of(args[0]);
        PdaMapCatalog catalog = PdaMapCatalog.load(pdaDir);

        System.out.println("Real pda-asset folders: " + catalog.totalFolderCount()
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
