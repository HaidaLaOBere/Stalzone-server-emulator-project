package net.stalzone.emulator.world.props;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * CLI demo for VehiclePropCatalog, same spirit as the other *DumpTool classes. Loads the real
 * {@code vehicles/} folder and prints every group's real model folders and loose files.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.world.props.VehiclePropCatalogDumpTool <vehicles dir>
 */
public final class VehiclePropCatalogDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: VehiclePropCatalogDumpTool <vehicles dir>");
            System.exit(2);
        }

        Path vehiclesDir = Path.of(args[0]);
        VehiclePropCatalog catalog = VehiclePropCatalog.load(vehiclesDir);

        System.out.println("Groups: " + catalog.groups().size()
                + ", total real named model folders: " + catalog.totalModelFolderCount());
        System.out.println();

        for (VehiclePropGroup group : catalog.groups()) {
            System.out.println(group.label() + " -- " + group.modelFolders().size() + " models, "
                    + group.looseFileNames().size() + " loose files");
            System.out.println("  models: " + group.modelFolders());
            if (!group.looseFileNames().isEmpty()) {
                System.out.println("  loose:  " + group.looseFileNames());
            }
        }
    }
}
