package net.stalzone.emulator.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Bare non-blocking UDP listener, plain java.nio (no Netty -- this sandbox can't
 * reach Maven Central to fetch it, see server-emulator/README.md). Currently does
 * nothing but log what it receives via PacketLogger; there is no protocol
 * implementation yet because the wire format is still unknown (encrypted payloads,
 * see docs/RESEARCH_LOG.md part 7). This is groundwork: once we have any way to get
 * bytes out of a real client (decompiled crypto code, or just observing what a
 * redirected client sends first), this is where handling it would start.
 */
public final class UdpListener implements Runnable, AutoCloseable {

    private final String bindAddress;
    private final int port;
    private final PacketLogger logger;
    private volatile boolean running = true;
    private DatagramChannel channel;
    private Selector selector;

    public UdpListener(String bindAddress, int port, PacketLogger logger) {
        this.bindAddress = bindAddress;
        this.port = port;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(bindAddress, port));
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            System.out.println("UDP listener bound on " + bindAddress + ":" + port);

            ByteBuffer buf = ByteBuffer.allocate(65535);
            while (running) {
                int ready = selector.select(500);
                if (ready == 0) continue;
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (!key.isValid()) continue;
                    if (key.isReadable()) {
                        buf.clear();
                        SocketAddress from = channel.receive(buf);
                        if (from != null) {
                            buf.flip();
                            int length = buf.remaining();
                            logger.logReceived(from, buf, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("UDP listener error: " + e);
            }
        } finally {
            closeQuietly();
        }
    }

    @Override
    public void close() {
        running = false;
        if (selector != null) selector.wakeup();
        closeQuietly();
    }

    private void closeQuietly() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
        } catch (IOException ignored) {
        }
        try {
            if (selector != null && selector.isOpen()) selector.close();
        } catch (IOException ignored) {
        }
    }
}
