package net.stalzone.emulator.world.region;

/**
 * One vertical 16x16x16-voxel slice of a chunk (Y band 0..15, each band covering 16
 * world-Y levels). Mirrors scfile/content/regions.py `ChunkSection`.
 *
 * Index convention: for x in [0,16), y in [0,16), z in [0,16), the flat index into
 * `blocks`/`metadata` is `y*256 + z*16 + x` -- the standard Minecraft Beta/Anvil
 * per-section block-array order. Provenance for that claim, since it isn't spelled out
 * anywhere in the STALZONE-specific format itself: onejeuu/sc-file's own re-encoder
 * (scfile/formats/mca/payload.py) writes each section's raw `blocks` bytes straight
 * into a real Minecraft "Blocks" NBT array with NO reordering/reshaping (only a
 * byte-for-byte block-id translation table is applied) -- so whatever order the raw
 * .mdat bytes are already in IS a real Minecraft Blocks array's order by construction,
 * and that order is externally, independently documented Minecraft-format knowledge
 * (y*256+z*16+x), not something reverse-engineered for this project specifically.
 */
public final class ChunkSection {

    public static final int SIZE = 16;
    public static final int BLOCK_COUNT = SIZE * SIZE * SIZE; // 4096

    /** Which of the 16 possible Y-bands this section is (0..15). */
    public final int y;

    /** Raw block-ID bytes, one per voxel, BLOCK_COUNT (4096) bytes. Never null. */
    public final byte[] blocks;

    /** Metadata nibbles (4 bits/voxel, 2 voxels packed per byte), 2048 bytes. Empty array if not requested/present. */
    public final byte[] metadata;

    /** High-bits "add" nibbles (extends block id beyond 8 bits), 2048 bytes. Empty array if this section has none. */
    public final byte[] additions;

    public ChunkSection(int y, byte[] blocks, byte[] metadata, byte[] additions) {
        this.y = y;
        this.blocks = blocks;
        this.metadata = metadata;
        this.additions = additions;
    }

    /** Raw block id byte at local (x,y,z) within this section (y here is 0..15 local, not world Y). */
    public int blockIdAt(int x, int localY, int z) {
        int idx = localY * 256 + z * 16 + x;
        return blocks[idx] & 0xFF;
    }

    /** 4-bit metadata nibble at local (x,y,z), or 0 if this section's metadata wasn't parsed. */
    public int metadataAt(int x, int localY, int z) {
        if (metadata.length == 0) return 0;
        int idx = localY * 256 + z * 16 + x;
        int b = metadata[idx / 2] & 0xFF;
        return (idx % 2 == 0) ? (b & 0x0F) : ((b >> 4) & 0x0F);
    }

    /** Full block id including the "add" high bits, if present (id | add<<8); falls back to the plain 8-bit id. */
    public int fullBlockIdAt(int x, int localY, int z) {
        int base = blockIdAt(x, localY, z);
        if (additions.length == 0) return base;
        int idx = localY * 256 + z * 16 + x;
        int b = additions[idx / 2] & 0xFF;
        int add = (idx % 2 == 0) ? (b & 0x0F) : ((b >> 4) & 0x0F);
        return base | (add << 8);
    }
}
