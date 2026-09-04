package net.stalzone.emulator.world.region;

import net.stalzone.emulator.nbt.CompressionUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reader for STALZONE's .mdat world-region-cache files (map_cache/5.0/&lt;zone&gt;/reg.X.Z.mdat).
 *
 * This is a direct port of onejeuu/sc-file's (https://github.com/onejeuu/sc-file) MDAT decoder
 * (scfile/formats/mdat/decoder.py) and payload splitter (scfile/formats/mdat/payload.py), verified
 * line-by-line against that source rather than paraphrased from memory. See
 * docs/RESEARCH_LOG.md part 15 for the full validation writeup (790 real chunks decoded/
 * cross-checked for dungeon_bolota_tutorial) and ChunkHeader/ChunkSection/RegionChunk's own doc
 * comments for the per-field provenance of the pieces this class ties together.
 *
 * File layout (all integers big-endian):
 *   1. A fixed 1024-entry (32x32 chunk slots) header table, 24 bytes/entry:
 *        u32 sectorOffset (0 = chunk absent; else the record starts at sectorOffset*4096)
 *        u32 count        (present in every real file; not used by the decoder itself, kept
 *                           only for diagnostics -- sc-file's own decoder also ignores it
 *                           beyond storing it on the parsed object)
 *        16 bytes uuid    (not used by the decoder either; stored for diagnostics only)
 *      = 24,576 bytes total, always at the start of the file.
 *   2. For every non-zero slot, a chunk record at byte offset (sectorOffset*4096):
 *        20-byte header: 5x u32 BE = (recordSize, sectionMask, addMask, terrainSize, compressedSize)
 *        followed immediately by compressedSize bytes of a single zstd frame.
 *      Decompressing that frame yields the raw payload that ChunkSection/biome data is sliced
 *      out of -- see splitChunk() below, a direct port of payload.chunk().
 */
public final class RegionFile {

    public static final int CHUNK_SLOT_COUNT = 32 * 32; // 1024, matches scfile's CHUNK_COUNT
    public static final int SECTOR_SIZE = 4096;
    public static final int HEADER_TABLE_BYTES = CHUNK_SLOT_COUNT * 24; // 24,576

    private static final int SECTION_SIZE = ChunkSection.BLOCK_COUNT; // 16*16*16 = 4096
    private static final int NIBBLE_SIZE = 16 * 16 * 8;               // 2048
    private static final int BIOME_SIZE = 16 * 16;                    // 256

    /** Region-grid X coordinate, parsed from the filename ("reg.X.Z.mdat"); 0 if unparseable. */
    public final int regionX;

    /** Region-grid Z coordinate, parsed from the filename; 0 if unparseable. */
    public final int regionZ;

    private final List<RegionChunk> chunks;

    private RegionFile(int regionX, int regionZ, List<RegionChunk> chunks) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.chunks = chunks;
    }

    /** All present chunks in this region (only slots with a non-zero offset -- typically far fewer than 1024). */
    public List<RegionChunk> chunks() {
        return Collections.unmodifiableList(chunks);
    }

    /** Finds the chunk at the given local (0..31, 0..31) position within this region, or null if absent. */
    public RegionChunk chunkAt(int localX, int localZ) {
        for (RegionChunk c : chunks) {
            if (c.localChunkX() == localX && c.localChunkZ() == localZ) {
                return c;
            }
        }
        return null;
    }

    /**
     * Parses "reg.X.Z.mdat" (X/Z may be negative, e.g. "reg.-10.-11.mdat") into a region
     * grid coordinate. Splitting on '.' is safe here because a negative number's '-' sign
     * doesn't itself contain a '.', so "reg.-10.-11.mdat".split("\\.") cleanly yields
     * ["reg", "-10", "-11", "mdat"]. Returns null if the name doesn't match that shape.
     */
    static int[] parseRegionCoords(String fileName) {
        String[] parts = fileName.split("\\.");
        if (parts.length != 4 || !parts[0].equals("reg") || !parts[3].equalsIgnoreCase("mdat")) {
            return null;
        }
        try {
            return new int[] { Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static RegionFile read(Path path) throws IOException {
        int[] coords = parseRegionCoords(path.getFileName().toString());
        int rx = coords != null ? coords[0] : 0;
        int rz = coords != null ? coords[1] : 0;

        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
                FileChannel channel = raf.getChannel()) {

            ByteBuffer headerBuf = ByteBuffer.allocate(HEADER_TABLE_BYTES).order(ByteOrder.BIG_ENDIAN);
            readFully(channel, headerBuf, 0);
            headerBuf.flip();

            int[] offsets = new int[CHUNK_SLOT_COUNT];
            for (int i = 0; i < CHUNK_SLOT_COUNT; i++) {
                offsets[i] = headerBuf.getInt();
                headerBuf.getInt();      // count -- unused, see class doc comment
                headerBuf.position(headerBuf.position() + 16); // uuid -- unused
            }

            List<RegionChunk> chunks = new ArrayList<>();
            for (int slot = 0; slot < CHUNK_SLOT_COUNT; slot++) {
                int sector = offsets[slot];
                if (sector == 0) {
                    continue;
                }
                chunks.add(readChunk(channel, path, slot, sector));
            }

            return new RegionFile(rx, rz, chunks);
        }
    }

    private static RegionChunk readChunk(FileChannel channel, Path path, int slotIndex, int sector)
            throws IOException {
        long pos = (long) sector * SECTOR_SIZE;

        ByteBuffer headerBuf = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN);
        readFully(channel, headerBuf, pos);
        headerBuf.flip();

        long recordSize = headerBuf.getInt() & 0xFFFFFFFFL;
        long sectionMask = headerBuf.getInt() & 0xFFFFFFFFL;
        long addMask = headerBuf.getInt() & 0xFFFFFFFFL;
        long terrainSize = headerBuf.getInt() & 0xFFFFFFFFL;
        long compressedSize = headerBuf.getInt() & 0xFFFFFFFFL;

        ChunkHeader header = new ChunkHeader(recordSize, sectionMask, addMask, terrainSize, compressedSize);

        if (compressedSize < 0 || compressedSize > Integer.MAX_VALUE) {
            throw new IOException(path + ": slot " + slotIndex + " has an implausible compressedSize="
                    + compressedSize);
        }

        ByteBuffer compressedBuf = ByteBuffer.allocate((int) compressedSize);
        readFully(channel, compressedBuf, pos + 20);
        byte[] compressed = compressedBuf.array();

        byte[] data;
        try {
            data = CompressionUtil.decompressIfNeeded(compressed);
        } catch (IOException e) {
            throw new IOException(path + ": failed to zstd-decompress chunk at slot " + slotIndex
                    + " (file offset " + (pos + 20) + ", compressedSize=" + compressedSize + ")", e);
        }

        return splitChunk(slotIndex, header, data);
    }

    /**
     * Direct port of scfile/formats/mdat/payload.py's chunk(). The byte-range math (blocksEnd/
     * metadataEnd/lightingEnd/addEnd/biomesEnd) is computed unconditionally exactly as the Python
     * does, regardless of whether metadata/additions end up used, because those ranges occupy real
     * space in the decompressed buffer either way -- getting biomes right requires walking past all
     * of it. Unlike the Python reference (whose default `extended=False` throws metadata/additions/
     * lighting/records away), this port always keeps blocks + metadata + additions (the fields our
     * ChunkSection models) since block metadata (e.g. rotation/variant nibbles) is plausibly useful
     * for the emulator later; lighting and the trailing "records" region are still discarded since
     * nothing in this project needs them yet.
     */
    private static RegionChunk splitChunk(int slotIndex, ChunkHeader header, byte[] data) {
        int sectionCount = header.sectionCount();
        int addCount = header.addSectionCount();

        int blockSize = sectionCount * SECTION_SIZE;
        int metadataSize = sectionCount * NIBBLE_SIZE;
        int lightingSize = metadataSize * 3;
        int addSize = addCount * NIBBLE_SIZE;

        int blocksEnd = blockSize;
        int metadataEnd = blocksEnd + metadataSize;
        int lightingEnd = metadataEnd + lightingSize;
        int addEnd = lightingEnd + addSize;
        int biomesEnd = addEnd + BIOME_SIZE;

        if (data.length < biomesEnd) {
            throw new IllegalStateException("Decompressed chunk payload too short: have " + data.length
                    + " bytes, need at least " + biomesEnd + " (slot=" + slotIndex + ", header=" + header + ")");
        }

        List<ChunkSection> sections = new ArrayList<>();
        int sectionIdx = 0;
        for (int y = 0; y < 16; y++) {
            if (((header.sectionMask >> y) & 1L) == 0) {
                continue;
            }

            byte[] blocks = slice(data, sectionIdx * SECTION_SIZE, SECTION_SIZE);
            byte[] metadata = slice(data, blocksEnd + sectionIdx * NIBBLE_SIZE, NIBBLE_SIZE);

            byte[] additions;
            if (((header.addMask >> y) & 1L) != 0) {
                int addSectionIndex = Long.bitCount(header.addMask & ((1L << y) - 1));
                additions = slice(data, lightingEnd + addSectionIndex * NIBBLE_SIZE, NIBBLE_SIZE);
            } else {
                additions = new byte[0];
            }

            sections.add(new ChunkSection(y, blocks, metadata, additions));
            sectionIdx++;
        }

        byte[] biomes = slice(data, addEnd, BIOME_SIZE);

        return new RegionChunk(slotIndex, header, sections, biomes);
    }

    private static byte[] slice(byte[] data, int offset, int length) {
        byte[] out = new byte[length];
        System.arraycopy(data, offset, out, 0, length);
        return out;
    }

    private static void readFully(FileChannel channel, ByteBuffer buf, long position) throws IOException {
        long pos = position;
        while (buf.hasRemaining()) {
            int n = channel.read(buf, pos);
            if (n < 0) {
                throw new IOException("Unexpected EOF at position " + pos + " while reading "
                        + buf.remaining() + " more bytes");
            }
            pos += n;
        }
    }
}
