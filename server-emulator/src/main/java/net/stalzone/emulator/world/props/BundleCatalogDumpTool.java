package net.stalzone.emulator.world.props;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI: dumps {@link BundleCatalog} folder/file totals, and optionally searches for a substring across
 * every real filename.
 *
 * <p>Usage: {@code BundleCatalogDumpTool <bundleDir> [substring]}
 */
public final class BundleCatalogDumpTool {

    private BundleCatalogDumpTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: BundleCatalogDumpTool <bundleDir> [substring]");
            System.exit(2);
            return;
        }

        BundleCatalog catalog = BundleCatalog.load(Path.of(args[0]));
        System.out.println("Folders: " + catalog.totalFolderCount());
        System.out.println("Files:   " + catalog.totalFileCount());

        if (args.length >= 2) {
            String substring = args[1];
            List<String> matches = catalog.foldersContaining(substring);
            System.out.println("Folders containing \"" + substring + "\": " + matches);
        } else {
            System.out.println("Folder names: " + catalog.folderNames());
        }
    }
}
