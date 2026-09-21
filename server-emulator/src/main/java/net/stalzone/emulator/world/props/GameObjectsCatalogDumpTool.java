package net.stalzone.emulator.world.props;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI: dumps {@link GameObjectsCatalog} folder/file totals, and optionally searches for a substring
 * across every real filename.
 *
 * <p>Usage: {@code GameObjectsCatalogDumpTool <gameObjectsDir> [substring]}
 */
public final class GameObjectsCatalogDumpTool {

    private GameObjectsCatalogDumpTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: GameObjectsCatalogDumpTool <gameObjectsDir> [substring]");
            System.exit(2);
            return;
        }

        GameObjectsCatalog catalog = GameObjectsCatalog.load(Path.of(args[0]));
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
