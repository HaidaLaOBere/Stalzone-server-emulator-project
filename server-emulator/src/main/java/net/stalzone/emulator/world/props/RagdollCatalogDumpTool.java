package net.stalzone.emulator.world.props;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI: dumps {@link RagdollCatalog} folder/file totals, and optionally searches for a substring across
 * every real filename.
 *
 * <p>Usage: {@code RagdollCatalogDumpTool <ragdollsDir> [substring]}
 */
public final class RagdollCatalogDumpTool {

    private RagdollCatalogDumpTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: RagdollCatalogDumpTool <ragdollsDir> [substring]");
            System.exit(2);
            return;
        }

        RagdollCatalog catalog = RagdollCatalog.load(Path.of(args[0]));
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
