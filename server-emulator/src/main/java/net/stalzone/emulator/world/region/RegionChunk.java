package net.stalzone.emulator.world.region;

import java.util.List;

/**
 * One decoded chunk (a 16x16-block, 0-255-Y-world column, made of up to 16 vertical
 * ChunkSections) from a .mdat region file. Mirrors scfile/content/regions.py `RegionChunk`.
 */
public final class RegionChunk {

    /** Index into the region's 32x32 chunk-slot table (0..1023) this chunk came from. */
    public final int slotIndex;

    public final ChunkHeader header;

    /** Present vertical sections, in ascending Y-band order (not necessarily 16 of them -- only bands with data). */
    public final List<ChunkSection> sections;

    /** 256 bytes (16x16), one biome id per column. Always present. */
    public final byte[] biomes;

    public RegionChunk(int slotIndex, ChunkHeader header, List<ChunkSection> sections, byte[] biomes) {
        this.slotIndex = slotIndex;
        this.header = header;
        this.sections = sections;
        this.biomes = biomes;
    }

    /**
     * This chunk's X position within its region (0..31). Formula confirmed against
     * onejeuu/sc-file's scfile/formats/mca/encoder.py: `local_x, local_z =
     * chunk.index % 32, chunk.index // 32`.
     */
    public int localChunkX() {
        return slotIndex % 32;
    }

    /** This chunk's Z position within its region (0..31). See localChunkX() for the source citation. */
    public int localChunkZ() {
        return slotIndex / 32;
    }

    /** Finds the section for a given Y-band (0..15), or null if that band has no data (e.g. above the terrain). */
    public ChunkSection sectionForBand(int y) {
        for (ChunkSection s : sections) {
            if (s.y == y) return s;
        }
        return null;
    }
}
