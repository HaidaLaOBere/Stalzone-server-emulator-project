package net.stalzone.emulator.nbt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Command-line parity check against tools/nbt_dump.py: reads a save/content file,
 * decompresses (zstd/gzip auto-detect), parses NBT, and pretty-prints it, plus a
 * round trip through NbtWriter to sanity-check the writer. Usage:
 *   java -cp out net.stalzone.emulator.nbt.NbtDumpTool <path> [--roundtrip]
 */
public final class NbtDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: NbtDumpTool <path> [--roundtrip]");
            System.exit(2);
        }
        byte[] raw = Files.readAllBytes(Path.of(args[0]));
        byte[] inner = CompressionUtil.decompressIfNeeded(raw);

        int[] bytesLeft = new int[1];
        NbtReader.Root root = NbtReader.readRootLenient(inner, bytesLeft);

        System.out.println("root name: " + (root.name.isEmpty() ? "(empty)" : root.name));
        System.out.println("decompressed size: " + inner.length + " bytes, leftover after parse: " + bytesLeft[0]);
        System.out.println(pretty(root.value, 0));

        boolean roundtrip = args.length > 1 && args[1].equals("--roundtrip");
        if (roundtrip) {
            byte[] rewritten = NbtWriter.writeRoot(root.name, root.value);
            NbtReader.Root reparsed = NbtReader.readRoot(rewritten);
            boolean same = pretty(root.value, 0).equals(pretty(reparsed.value, 0));
            System.out.println("\nroundtrip write+reread produced " + rewritten.length
                    + " bytes (original inner was " + inner.length + "); structurally identical: " + same);
        }
    }

    private static String pretty(Object v, int indent) {
        String pad = "  ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        if (v instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) v;
            sb.append("{\n");
            for (Map.Entry<String, Object> e : m.entrySet()) {
                sb.append(pad).append("  ").append(e.getKey()).append(": ")
                        .append(pretty(e.getValue(), indent + 1)).append("\n");
            }
            sb.append(pad).append("}");
        } else if (v instanceof List) {
            List<?> list = (List<?>) v;
            sb.append("[").append(list.size()).append(" items]");
            int shown = 0;
            for (Object item : list) {
                if (shown++ >= 3) {
                    sb.append(pad).append("  ...");
                    break;
                }
                sb.append("\n").append(pad).append("  - ").append(pretty(item, indent + 1));
            }
        } else if (v instanceof byte[]) {
            sb.append("byte[").append(((byte[]) v).length).append("]");
        } else if (v instanceof int[]) {
            sb.append("int[").append(((int[]) v).length).append("]");
        } else if (v instanceof long[]) {
            sb.append("long[").append(((long[]) v).length).append("]");
        } else {
            sb.append(v);
        }
        return sb.toString();
    }
}
