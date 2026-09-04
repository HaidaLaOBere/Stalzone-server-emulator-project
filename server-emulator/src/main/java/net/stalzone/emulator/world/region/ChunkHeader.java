package net.stalzone.emulator.world.region;

/**
 * The 20-byte big-endian header that precedes every compressed chunk record inside a
 * .mdat region file. Field names/order come straight from onejeuu/sc-file's
 * scfile/content/regions.py `ChunkHeader` dataclass (see docs/RESEARCH_LOG.md part 15
 * for the full citation) -- this is a port, not a guess.
 *
 * All five fields are big-endian uint32. Java has no unsigned int, so these are stored
 * as `long` to avoid sign-overflow surprises; in practice every real value seen so far
 * fits comfortably in a positive int, but storing as long costs nothing and avoids a
 * whole class of "why is this negative" bugs later.
 */
public final class ChunkHeader {

    /** Total on-disk record size in bytes (header + compressed payload), as reported by the file itself. */
    public final long recordSize;

    /** Bitmask, bits 0-15 = which of the 16 vertical 16x16x16 sections (Y=0..15) are present in this chunk. */
    public final long sectionMask;

    /** Bitmask, bits 0-15 = which sections additionally have an "add" (high block-id bits) nibble-plane. */
    public final long addMask;

    /** Reported size of the uncompressed terrain payload. Not used for parsing (the actual decompressed
     *  byte array's length is what parsing uses); kept for diagnostics/cross-checking. */
    public final long terrainSize;

    /** Exact number of compressed bytes to read for this chunk's zstd-compressed payload. */
    public final long compressedSize;

    public ChunkHeader(long recordSize, long sectionMask, long addMask, long terrainSize, long compressedSize) {
        this.recordSize = recordSize;
        this.sectionMask = sectionMask;
        this.addMask = addMask;
        this.terrainSize = terrainSize;
        this.compressedSize = compressedSize;
    }

    public int sectionCount() {
        return Long.bitCount(sectionMask & 0xFFFFL);
    }

    public int addSectionCount() {
        return Long.bitCount(addMask & 0xFFFFL);
    }

    @Override
    public String toString() {
        return "ChunkHeader{recordSize=" + recordSize
                + ", sectionMask=" + Long.toBinaryString(sectionMask & 0xFFFFL)
                + ", addMask=" + Long.toBinaryString(addMask & 0xFFFFL)
                + ", terrainSize=" + terrainSize
                + ", compressedSize=" + compressedSize + "}";
    }
}
