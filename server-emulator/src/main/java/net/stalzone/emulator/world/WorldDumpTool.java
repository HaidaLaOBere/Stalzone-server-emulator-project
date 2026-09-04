package net.stalzone.emulator.world;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI sanity check for WorldModel, same spirit as region.RegionDumpTool and nbt.NbtDumpTool.
 * Loads a whole zone folder (e.g. map_cache/5.0/dungeon_bolota_tutorial/) and reports region/
 * chunk counts plus the world-block bounds actually covered, then probes highestSolidY at a
 * handful of sample columns across that range so the query layer can be eyeballed, not just
 * trusted because it compiled. Usage:
 *   java -cp out net.stalzone.emulator.world.WorldDumpTool <path-to-zone-folder> [mapName]
 */
public final class WorldDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: WorldDumpTool <path-to-zone-folder> [mapName]");
            System.exit(2);
        }

        Path mapDir = Path.of(args[0]);
        String mapName = args.length > 1 ? args[1] : mapDir.getFileName().toString();

        WorldModel world = WorldModel.load(mapDir, mapName);
        System.out.println("map: " + world.mapName);
        System.out.println("region files loaded: " + world.regionFileCount());
        System.out.println("chunks loaded: " + world.loadedChunkCount());

        List<long[]> coords = world.loadedChunkCoords();
        long minCx = Long.MAX_VALUE, maxCx = Long.MIN_VALUE, minCz = Long.MAX_VALUE, maxCz = Long.MIN_VALUE;
        for (long[] c : coords) {
            minCx = Math.min(minCx, c[0]);
            maxCx = Math.max(maxCx, c[0]);
            minCz = Math.min(minCz, c[1]);
            maxCz = Math.max(maxCz, c[1]);
        }
        System.out.println("chunk coord bounds: X[" + minCx + ".." + maxCx + "] Z[" + minCz + ".." + maxCz + "]");
        System.out.println("block coord bounds: X[" + (minCx * 16) + ".." + (maxCx * 16 + 15)
                + "] Z[" + (minCz * 16) + ".." + (maxCz * 16 + 15) + "]");

        System.out.println();
        System.out.println("Sample highestSolidY probes (5 evenly spaced points across the loaded area):");
        for (int i = 0; i <= 4; i++) {
            long wx = minCx * 16 + (maxCx - minCx) * 16 * i / 4;
            long wz = minCz * 16 + (maxCz - minCz) * 16 * i / 4;
            int y = world.highestSolidY((int) wx, (int) wz);
            int blockAtY = y >= 0 ? world.blockIdAt((int) wx, y, (int) wz) : -1;
            System.out.println("  (" + wx + ", " + wz + ") -> highest solid Y=" + y
                    + (y >= 0 ? " (blockId=" + blockAtY + ")" : " (no loaded chunk / all air)"));
        }
    }
}
