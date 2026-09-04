package net.stalzone.emulator;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.network.PacketLogger;
import net.stalzone.emulator.network.UdpListener;
import net.stalzone.emulator.world.MilestoneMap;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point. Loads config, starts the UDP listener. That's genuinely all this does
 * right now -- there is no login/auth/session/gameplay logic, because the real wire
 * protocol is still encrypted and unread (see docs/RESEARCH_LOG.md part 7). This is
 * Phase 1 scaffolding (docs/PROJECT_PLAN.md), not a working server yet.
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        Path configPath = Path.of(args.length > 0 ? args[0] : "config/server.properties");
        ServerConfig config = ServerConfig.loadOrCreateDefault(configPath);

        System.out.println("=================================================");
        System.out.println(" STALZONE server-emulator -- Phase 1 scaffolding");
        System.out.println(" Config: " + configPath.toAbsolutePath());
        System.out.println(" Bind:   " + config.bindAddress + ":" + config.port);
        System.out.println(" Milestone-1 target map: " + MilestoneMap.MILESTONE_1_MAP);
        System.out.println(" NOTE: no protocol implementation exists yet. This only");
        System.out.println(" listens and logs raw UDP datagrams -- see README.md.");
        System.out.println("=================================================");

        PacketLogger logger = new PacketLogger(config.packetLoggingEnabled ? config.packetLogHexBytes : 0);
        UdpListener listener = new UdpListener(config.bindAddress, config.port, logger);

        Runtime.getRuntime().addShutdownHook(new Thread(listener::close));
        listener.run(); // blocks on this thread; Ctrl+C to stop
    }
}
