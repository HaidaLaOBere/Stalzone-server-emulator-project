package net.stalzone.emulator.network;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hex-dumps received UDP datagrams to stdout. Deliberately dumb right now — we don't
 * know the wire protocol (payloads look encrypted/high-entropy from byte 0, see
 * docs/RESEARCH_LOG.md part 7), so there's no framing to parse yet. This exists so
 * that once we DO get a real client to send us something (e.g. via a hosts-file
 * redirect experiment), we have visibility into exactly what arrives, when.
 */
public final class PacketLogger {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final int maxHexBytes;

    public PacketLogger(int maxHexBytes) {
        this.maxHexBytes = maxHexBytes;
    }

    public void logReceived(SocketAddress from, ByteBuffer buf, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(LocalDateTime.now().format(TS)).append("] RECV ")
                .append(length).append(" bytes from ").append(from);
        int shown = Math.min(length, maxHexBytes);
        if (shown > 0) {
            sb.append(" | hex: ");
            for (int i = 0; i < shown; i++) {
                sb.append(String.format("%02x", buf.get(buf.position() + i)));
            }
            if (shown < length) sb.append("...");
        }
        System.out.println(sb);
    }
}
