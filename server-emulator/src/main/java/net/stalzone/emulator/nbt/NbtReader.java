package net.stalzone.emulator.nbt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Port of tools/nbt_dump.py's Reader + read_payload/read_root, validated byte-exact
 * (0 leftover bytes) against 9+ real STALZONE save/content files. Standard big-endian
 * NBT, same tag-type layout Minecraft uses. See docs/RESEARCH_LOG.md "Live session
 * findings, part 3" for how this was reverse engineered and confirmed.
 *
 * NOTE: this reads the *inner* NBT only. Callers should run the bytes through
 * CompressionUtil.decompressIfNeeded() first if the file may be zstd/gzip-wrapped
 * (local save data and static content files both are; wire packets are NOT — see
 * RESEARCH_LOG.md part 7, wire payloads are encrypted/high-entropy, not this format).
 */
public final class NbtReader {

    public static final int TAG_END = 0;
    public static final int TAG_BYTE = 1;
    public static final int TAG_SHORT = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTE_ARRAY = 7;
    public static final int TAG_STRING = 8;
    public static final int TAG_LIST = 9;
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT_ARRAY = 11;
    public static final int TAG_LONG_ARRAY = 12;

    /** name + parsed value of the root compound tag. */
    public static final class Root {
        public final String name;
        public final Object value;
        public Root(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private final byte[] data;
    private int pos;

    private NbtReader(byte[] data) {
        this.data = data;
        this.pos = 0;
    }

    /** Reads a root tag (name + compound payload). Throws if trailing bytes remain. */
    public static Root readRoot(byte[] data) throws IOException {
        NbtReader r = new NbtReader(data);
        int tagType = r.u8();
        String name = r.nbtString();
        Object value = r.readPayload(tagType);
        if (r.pos != r.data.length) {
            throw new IOException("Trailing bytes after root tag: " + (r.data.length - r.pos)
                    + " of " + r.data.length + " unread");
        }
        return new Root(name, value);
    }

    /** Same as readRoot but tolerates trailing bytes (returns however many were left via bytesLeft[0]). */
    public static Root readRootLenient(byte[] data, int[] bytesLeftOut) throws IOException {
        NbtReader r = new NbtReader(data);
        int tagType = r.u8();
        String name = r.nbtString();
        Object value = r.readPayload(tagType);
        if (bytesLeftOut != null && bytesLeftOut.length > 0) {
            bytesLeftOut[0] = r.data.length - r.pos;
        }
        return new Root(name, value);
    }

    private Object readPayload(int tagType) throws IOException {
        switch (tagType) {
            case TAG_END:
                return null;
            case TAG_BYTE:
                return i8();
            case TAG_SHORT:
                return i16();
            case TAG_INT:
                return i32();
            case TAG_LONG:
                return i64();
            case TAG_FLOAT:
                return f32();
            case TAG_DOUBLE:
                return f64();
            case TAG_BYTE_ARRAY: {
                int len = i32();
                byte[] out = new byte[len];
                readFully(out);
                return out;
            }
            case TAG_STRING:
                return nbtString();
            case TAG_LIST: {
                int elemType = u8();
                int len = i32();
                List<Object> list = new ArrayList<>(Math.max(0, len));
                for (int i = 0; i < len; i++) {
                    list.add(readPayload(elemType));
                }
                return list;
            }
            case TAG_COMPOUND: {
                Map<String, Object> map = new LinkedHashMap<>();
                while (true) {
                    int childType = u8();
                    if (childType == TAG_END) break;
                    String childName = nbtString();
                    map.put(childName, readPayload(childType));
                }
                return map;
            }
            case TAG_INT_ARRAY: {
                int len = i32();
                int[] out = new int[len];
                for (int i = 0; i < len; i++) out[i] = i32();
                return out;
            }
            case TAG_LONG_ARRAY: {
                int len = i32();
                long[] out = new long[len];
                for (int i = 0; i < len; i++) out[i] = i64();
                return out;
            }
            default:
                throw new IOException("Unknown NBT tag type " + tagType + " at byte offset " + pos);
        }
    }

    // --- primitive readers (big-endian) ---

    private int u8() throws IOException {
        requireBytes(1);
        return data[pos++] & 0xFF;
    }

    private byte i8() throws IOException {
        requireBytes(1);
        return data[pos++];
    }

    private short i16() throws IOException {
        requireBytes(2);
        int v = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
        pos += 2;
        return (short) v;
    }

    private int i32() throws IOException {
        requireBytes(4);
        int v = ((data[pos] & 0xFF) << 24) | ((data[pos + 1] & 0xFF) << 16)
                | ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
        pos += 4;
        return v;
    }

    private long i64() throws IOException {
        requireBytes(8);
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v = (v << 8) | (data[pos + i] & 0xFFL);
        }
        pos += 8;
        return v;
    }

    private float f32() throws IOException {
        return Float.intBitsToFloat(i32());
    }

    private double f64() throws IOException {
        return Double.longBitsToDouble(i64());
    }

    private String nbtString() throws IOException {
        int len = i16() & 0xFFFF; // unsigned 16-bit length, matches Python's u16
        requireBytes(len);
        String s = new String(data, pos, len, StandardCharsets.UTF_8);
        pos += len;
        return s;
    }

    private void readFully(byte[] out) throws IOException {
        requireBytes(out.length);
        System.arraycopy(data, pos, out, 0, out.length);
        pos += out.length;
    }

    private void requireBytes(int n) throws IOException {
        if (pos + n > data.length) {
            throw new IOException("Unexpected end of NBT data at offset " + pos
                    + " (need " + n + " more bytes, have " + (data.length - pos) + ")");
        }
    }
}
