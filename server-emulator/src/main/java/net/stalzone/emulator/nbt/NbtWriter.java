package net.stalzone.emulator.nbt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Symmetric counterpart to NbtReader: writes the same generic Object tree
 * (LinkedHashMap = compound, List = list, byte[]/int[]/long[] = array tags,
 * boxed primitives = scalar tags) back out as standard big-endian NBT bytes.
 *
 * Not yet used anywhere in the server — added now so save-data / world-content
 * files can eventually be edited and re-written, not just read. Untested against
 * real files beyond the reader-then-writer round-trip in NbtRoundTripTest.
 */
public final class NbtWriter {

    private NbtWriter() {}

    public static byte[] writeRoot(String name, Object value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int tagType = tagTypeOf(value);
        out.write(tagType);
        writeString(out, name);
        writePayload(out, tagType, value);
        return out.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static int tagTypeOf(Object value) {
        if (value instanceof Byte) return NbtReader.TAG_BYTE;
        if (value instanceof Short) return NbtReader.TAG_SHORT;
        if (value instanceof Integer) return NbtReader.TAG_INT;
        if (value instanceof Long) return NbtReader.TAG_LONG;
        if (value instanceof Float) return NbtReader.TAG_FLOAT;
        if (value instanceof Double) return NbtReader.TAG_DOUBLE;
        if (value instanceof byte[]) return NbtReader.TAG_BYTE_ARRAY;
        if (value instanceof String) return NbtReader.TAG_STRING;
        if (value instanceof List) return NbtReader.TAG_LIST;
        if (value instanceof Map) return NbtReader.TAG_COMPOUND;
        if (value instanceof int[]) return NbtReader.TAG_INT_ARRAY;
        if (value instanceof long[]) return NbtReader.TAG_LONG_ARRAY;
        throw new IllegalArgumentException("No NBT tag type for Java type " + (value == null ? "null" : value.getClass()));
    }

    @SuppressWarnings("unchecked")
    private static void writePayload(ByteArrayOutputStream out, int tagType, Object value) throws IOException {
        switch (tagType) {
            case NbtReader.TAG_BYTE:
                out.write((Byte) value);
                return;
            case NbtReader.TAG_SHORT:
                writeI16(out, (Short) value);
                return;
            case NbtReader.TAG_INT:
                writeI32(out, (Integer) value);
                return;
            case NbtReader.TAG_LONG:
                writeI64(out, (Long) value);
                return;
            case NbtReader.TAG_FLOAT:
                writeI32(out, Float.floatToIntBits((Float) value));
                return;
            case NbtReader.TAG_DOUBLE:
                writeI64(out, Double.doubleToLongBits((Double) value));
                return;
            case NbtReader.TAG_BYTE_ARRAY: {
                byte[] arr = (byte[]) value;
                writeI32(out, arr.length);
                out.write(arr);
                return;
            }
            case NbtReader.TAG_STRING:
                writeString(out, (String) value);
                return;
            case NbtReader.TAG_LIST: {
                List<Object> list = (List<Object>) value;
                int elemType = list.isEmpty() ? NbtReader.TAG_END : tagTypeOf(list.get(0));
                out.write(elemType);
                writeI32(out, list.size());
                for (Object elem : list) {
                    writePayload(out, elemType, elem);
                }
                return;
            }
            case NbtReader.TAG_COMPOUND: {
                Map<String, Object> map = (Map<String, Object>) value;
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    int childType = tagTypeOf(e.getValue());
                    out.write(childType);
                    writeString(out, e.getKey());
                    writePayload(out, childType, e.getValue());
                }
                out.write(NbtReader.TAG_END);
                return;
            }
            case NbtReader.TAG_INT_ARRAY: {
                int[] arr = (int[]) value;
                writeI32(out, arr.length);
                for (int v : arr) writeI32(out, v);
                return;
            }
            case NbtReader.TAG_LONG_ARRAY: {
                long[] arr = (long[]) value;
                writeI32(out, arr.length);
                for (long v : arr) writeI64(out, v);
                return;
            }
            default:
                throw new IOException("Unknown NBT tag type " + tagType);
        }
    }

    private static void writeI16(ByteArrayOutputStream out, int v) {
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    private static void writeI32(ByteArrayOutputStream out, int v) {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    private static void writeI64(ByteArrayOutputStream out, long v) {
        for (int shift = 56; shift >= 0; shift -= 8) {
            out.write((int) ((v >>> shift) & 0xFF));
        }
    }

    private static void writeString(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeI16(out, bytes.length);
        out.write(bytes, 0, bytes.length);
    }
}
