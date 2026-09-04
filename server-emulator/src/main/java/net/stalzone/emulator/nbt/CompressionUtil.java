package net.stalzone.emulator.nbt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

/**
 * Detects and undoes the outer compression wrapper STALZONE uses for its NBT-based
 * save/content files, mirroring tools/nbt_dump.py's maybe_zstd_decompress().
 *
 * Confirmed magic bytes (from real files inspected during research):
 *   zstd: 28 B5 2F FD   (e.g. config/vendor_archive.dat, save-data files)
 *   gzip: 1F 8B          (e.g. modassets/assets/stalker/itemnames.dat)
 * Anything else is treated as already-raw NBT.
 */
public final class CompressionUtil {

    private CompressionUtil() {}

    public static byte[] decompressIfNeeded(byte[] data) throws IOException {
        if (data.length >= 4
                && (data[0] & 0xFF) == 0x28
                && (data[1] & 0xFF) == 0xB5
                && (data[2] & 0xFF) == 0x2F
                && (data[3] & 0xFF) == 0xFD) {
            return decompressZstd(data);
        }
        if (data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B) {
            return decompressGzip(data);
        }
        return data;
    }

    private static byte[] decompressGzip(byte[] data) throws IOException {
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
            byte[] buf = new byte[8192];
            int n;
            while ((n = gz.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    /**
     * The JDK has no built-in zstd support, and this sandbox can't fetch a zstd
     * Maven dependency (Maven Central isn't on the egress allowlist here). Shell
     * out to the system 'zstd'/'unzstd' CLI instead, same as the earlier Python
     * tooling did. TODO once real dependency access exists: vendor a pure-Java
     * zstd decoder (e.g. com.github.luben:zstd-jni) so this doesn't depend on an
     * external binary being on PATH at deploy time.
     */
    private static byte[] decompressZstd(byte[] data) throws IOException {
        String bin = which("unzstd") != null ? "unzstd" : which("zstd") != null ? "zstd" : null;
        if (bin == null) {
            throw new IOException(
                    "zstd-compressed data found but no 'zstd'/'unzstd' binary is on PATH. "
                            + "The JDK has no built-in zstd support and this environment can't fetch a "
                            + "zstd Java library dependency; install the zstd CLI, or vendor a pure-Java "
                            + "zstd decoder once dependency access is available.");
        }
        ProcessBuilder pb = new ProcessBuilder(bin, "-d", "-c");
        pb.redirectErrorStream(false);
        Process proc = pb.start();
        Thread writer = new Thread(() -> {
            try (var os = proc.getOutputStream()) {
                os.write(data);
            } catch (IOException ignored) {
            }
        });
        writer.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
        try (var is = proc.getInputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        try {
            writer.join();
            int code = proc.waitFor();
            if (code != 0) {
                throw new IOException(bin + " exited with code " + code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for " + bin, e);
        }
        return out.toByteArray();
    }

    private static String which(String bin) {
        String path = System.getenv("PATH");
        if (path == null) return null;
        for (String dir : path.split(java.io.File.pathSeparator)) {
            java.io.File f = new java.io.File(dir, bin);
            if (f.isFile() && f.canExecute()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }
}
