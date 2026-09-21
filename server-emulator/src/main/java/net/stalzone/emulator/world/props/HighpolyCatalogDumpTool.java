package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI demo for {@link HighpolyCatalog}. Loads the real {@code highpoly} folder and prints the total
 * folder/file counts plus every folder matching a given substring query (the generic mechanism this
 * class uses to verify narrative claims against real data instead of a hardcoded/hand-counted list --
 * see the class doc).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.world.props.HighpolyCatalogDumpTool <highpoly dir> [substring]
 */
public final class HighpolyCatalogDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: HighpolyCatalogDumpTool <highpoly dir> [substring]");
            System.exit(2);
        }

        Path highpolyDir = Path.of(args[0]);
        HighpolyCatalog catalog = HighpolyCatalog.load(highpolyDir);

        System.out.println("Real highpoly-asset folders: " + catalog.totalFolderCount()
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
