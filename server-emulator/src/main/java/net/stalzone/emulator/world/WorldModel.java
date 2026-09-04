package net.stalzone.emulator.world;

import net.stalzone.emulator.world.region.ChunkSection;
import net.stalzone.emulator.world.region.RegionChunk;
import net.stalzone.emulator.world.region.RegionFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A whole loaded map (every region file under a single zone folder, e.g.
 * map_cache/5.0/dungeon_bolota_tutorial/), addressable by ordinary world block coordinates
 * instead of per-region/per-chunk indices. This is the piece {@code region.RegionFile} was
 * missing: RegionFile reads one region file in isolation; WorldModel is what an actual
 * collision/spawn/movement system in the emulator would call.
 *
 * Coordinate system: standard Minecraft Anvil-style chunk/region addressing, which is what
 * this project already established applies here (see region.RegionChunk's own doc comment,
 * and docs/RESEARCH_LOG.md parts 15-16 for the citation chain back to onejeuu/sc-file's
 * verbatim-copy re-encoder). A region file "reg.X.Z.mdat" covers a 32x32 chunk area whose
 * chunk coordinates run from (X*32, Z*32) to (X*32+31, Z*32+31) inclusive; each chunk is a
 * 16x16-block column; each of the (up to 16) vertical sections in a chunk covers 16 world-Y
 * levels, so world Y is assumed to be in [0, 255] with no negative range -- consistent with
 * ChunkHeader.sectionMask being a 16-bit mask (Y bands 0-15) and with every real chunk
 * inspected during part-15/16 validation.
 */
public final class WorldModel {

    public final String mapName;

    /** Global chunk coordinate (packed) -> the chunk at that position. Built once at load time
     *  so per-block queries don't need to linearly scan a RegionFile's chunk list. */
    private final Map<Long, RegionChunk> chunksByGlobalCoord;

    private final int regionFileCount;

    private WorldModel(String mapName, Map<Long, RegionChunk> chunksByGlobalCoord, int regionFileCount) {
        this.mapName = mapName;
        this.chunksByGlobalCoord = chunksByGlobalCoord;
        this.regionFileCount = regionFileCount;
    }

    public int regionFileCount() {
        return regionFileCount;
    }

    public int loadedChunkCount() {
        return chunksByGlobalCoord.size();
    }

    /** Loads every "reg.*.mdat" file directly inside {@code mapDir} into one queryable world. */
    public static WorldModel load(Path mapDir, String mapName) throws IOException {
        Map<Long, RegionChunk> index = new HashMap<>();
        int regionCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(mapDir, "*.mdat")) {
            for (Path path : stream) {
                RegionFile region = RegionFile.read(path);
                regionCount++;
                for (RegionChunk chunk : region.chunks()) {
                    long globalChunkX = (long) region.regionX * 32 + chunk.localChunkX();
                    long globalChunkZ = (long) region.regionZ * 32 + chunk.localChunkZ();
                    index.put(packChunkCoord(globalChunkX, globalChunkZ), chunk);
                }
            }
        }

        return new WorldModel(mapName, index, regionCount);
    }

    /** Packs a (globalChunkX, globalChunkZ) pair into one long key. Each half gets 32 bits,
     *  offset by a bias so negative chunk coordinates (very real here -- the milestone-1 zone's
     *  own region files are named reg.-10.-10 etc.) don't collide once packed. */
    private static long packChunkCoord(long chunkX, long chunkZ) {
        long bias = 1L << 31;
        return ((chunkX + bias) << 32) | ((chunkZ + bias) & 0xFFFFFFFFL);
    }

    private RegionChunk chunkAtBlock(int worldX, int worldZ) {
        long chunkX = Math.floorDiv(worldX, 16);
        long chunkZ = Math.floorDiv(worldZ, 16);
        return chunksByGlobalCoord.get(packChunkCoord(chunkX, chunkZ));
    }

    /**
     * Raw block id at a world coordinate. Returns 0 (air, per the block-id histograms in part
     * 15/16) for any position in a chunk that hasn't been generated/cached, or in an unloaded
     * chunk -- there's no way to distinguish "really air" from "not loaded" from this data
     * alone, which is an honest limitation, not a bug: this is a cache snapshot of visited
     * terrain, not the authoritative world generator.
     */
    public int blockIdAt(int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY > 255) {
            return 0;
        }
        RegionChunk chunk = chunkAtBlock(worldX, worldZ);
        if (chunk == null) {
            return 0;
        }
        ChunkSection section = chunk.sectionForBand(worldY >> 4);
        if (section == null) {
            return 0;
        }
        int localX = Math.floorMod(worldX, 16);
        int localZ = Math.floorMod(worldZ, 16);
        int localY = worldY & 15;
        return section.blockIdAt(localX, localY, localZ);
    }

    /** Full block id including "add" high bits where present (see ChunkSection.fullBlockIdAt). */
    public int fullBlockIdAt(int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY > 255) {
            return 0;
        }
        RegionChunk chunk = chunkAtBlock(worldX, worldZ);
        if (chunk == null) {
            return 0;
        }
        ChunkSection section = chunk.sectionForBand(worldY >> 4);
        if (section == null) {
            return 0;
        }
        int localX = Math.floorMod(worldX, 16);
        int localZ = Math.floorMod(worldZ, 16);
        int localY = worldY & 15;
        return section.fullBlockIdAt(localX, localY, localZ);
    }

    /**
     * Highest Y with a non-air (id != 0) block at this (worldX, worldZ) column, scanning down
     * from Y=255, or -1 if the column has no loaded chunk or is all air. This is the obvious
     * building block for "where does a player/mob stand" spawn placement -- not wired to any
     * spawn logic yet, just the query it would need.
     */
    public int highestSolidY(int worldX, int worldZ) {
        RegionChunk chunk = chunkAtBlock(worldX, worldZ);
        if (chunk == null) {
            return -1;
        }
        int localX = Math.floorMod(worldX, 16);
        int localZ = Math.floorMod(worldZ, 16);
        for (int y = 255; y >= 0; y--) {
            ChunkSection section = chunk.sectionForBand(y >> 4);
            if (section == null) {
                y -= 15; // skip the rest of this empty band in one step (y-- in the loop covers the last one)
                continue;
            }
            if (section.blockIdAt(localX, y & 15, localZ) != 0) {
                return y;
            }
        }
        return -1;
    }

    /** Every loaded global chunk coordinate, as [chunkX, chunkZ] pairs. Mainly for CLI/debug tools. */
    public List<long[]> loadedChunkCoords() {
        List<long[]> out = new ArrayList<>(chunksByGlobalCoord.size());
        long bias = 1L << 31;
        for (long packed : chunksByGlobalCoord.keySet()) {
            long chunkX = (packed >>> 32) - bias;
            long chunkZ = (packed & 0xFFFFFFFFL) - bias;
            out.add(new long[] { chunkX, chunkZ });
        }
        return out;
    }
}
