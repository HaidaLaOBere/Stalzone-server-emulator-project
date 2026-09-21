package net.stalzone.emulator;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.network.PacketLogger;
import net.stalzone.emulator.network.UdpListener;
import net.stalzone.emulator.session.ConsoleDriver;
import net.stalzone.emulator.session.SingleplayerServer;
import net.stalzone.emulator.world.MilestoneMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point. Loads config, starts the UDP listener, and -- as of part 84, only when {@link
 * ServerConfig#stalzoneInstallDir} is configured -- also starts {@link SingleplayerServer}, a
 * genuine continuously-ticking local world simulation (real spawn cadence, real emission cycle,
 * real weight-cap checks, all through the real, already-verified {@code session/GameLoop}). There
 * is still no NETWORKED gameplay logic here: the real wire protocol is still encrypted and unread
 * (see docs/RESEARCH_LOG.md part 7), so the UDP listener remains exactly Phase-1 scaffolding
 * (docs/PROJECT_PLAN.md) -- it only logs raw datagrams, it does not feed them to the simulation.
 * With {@code stalzoneInstallDir} left unset (the default), this class's behavior is byte-for-byte
 * identical to every version before part 84: UDP packet logging only. See {@link
 * SingleplayerServer}'s class doc for exactly what the simulation does and deliberately does not
 * do without a real network input source driving it.
 *
 * <p>As of part 86, whenever {@link SingleplayerServer} starts, this class also starts {@link
 * ConsoleDriver} on stdin (unless {@link ServerConfig#enableConsoleDriver} is set to {@code
 * false}) -- an operator command console that can actually call {@code GameLoop}'s movement/
 * hazard/combat/purchase/ability/quest tick methods against this real running server. See {@link
 * ConsoleDriver}'s own class doc for the full command list.
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        Path configPath = Path.of(args.length > 0 ? args[0] : "config/server.properties");
        ServerConfig config = ServerConfig.loadOrCreateDefault(configPath);

        System.out.println("=================================================");
        System.out.println(" STALZONE server-emulator");
        System.out.println(" Config: " + configPath.toAbsolutePath());
        System.out.println(" Bind:   " + config.bindAddress + ":" + config.port);
        System.out.println(" Milestone-1 target map: " + MilestoneMap.MILESTONE_1_MAP);
        System.out.println(" NOTE: no protocol implementation exists yet. The UDP listener");
        System.out.println(" below only logs raw datagrams -- see README.md.");
        System.out.println("=================================================");

        PacketLogger logger = new PacketLogger(config.packetLoggingEnabled ? config.packetLogHexBytes : 0);
        UdpListener listener = new UdpListener(config.bindAddress, config.port, logger);
        Runtime.getRuntime().addShutdownHook(new Thread(listener::close));

        if (SingleplayerServer.isConfigured(config)) {
            System.out.println(" stalzoneInstallDir is set -- starting the local singleplayer");
            System.out.println(" world simulation (session/SingleplayerServer, part 84) alongside");
            System.out.println(" the UDP listener above. See that class's doc for what it does.");
            SingleplayerServer server = new SingleplayerServer(config, System.out::println);
            Runtime.getRuntime().addShutdownHook(new Thread(server::close));
            Thread simThread = new Thread(server, "singleplayer-sim");
            simThread.setDaemon(true);
            simThread.start();

            if (config.enableConsoleDriver) {
                System.out.println(" enableConsoleDriver is true -- starting the operator console");
                System.out.println(" (session/ConsoleDriver, part 86). Type 'help' + Enter for");
                System.out.println(" real commands (move/attack/buy/quest/...) against this server.");
                ConsoleDriver console = new ConsoleDriver(server,
                        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                        System.out::println);
                Thread consoleThread = new Thread(console, "singleplayer-console");
                consoleThread.setDaemon(true);
                consoleThread.start();
            }
        } else {
            System.out.println(" stalzoneInstallDir is not set in " + configPath.toAbsolutePath());
            System.out.println(" -- no local simulation started (UDP listener only). Set it to a");
            System.out.println(" real STALZONE install dir to also run session/SingleplayerServer.");
        }

        listener.run(); // blocks on this thread; Ctrl+C to stop
    }
}
