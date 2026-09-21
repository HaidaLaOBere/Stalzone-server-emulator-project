package net.stalzone.emulator.modding;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.session.GameLoop;

import java.util.function.Consumer;

/**
 * What a {@link ServerMod} is handed at {@link ServerMod#onLoad} (part 89): the live {@link
 * GameLoop} (so a mod can call the SAME real tick methods and the part-88 live-tuning setters a
 * {@code ConsoleDriver} operator can -- a mod is, deliberately, just another kind of operator),
 * the live {@link ServerConfig} (a mod may read it directly, but {@link GameLoop}'s own
 * {@code setXxxMultiplier} methods are the supported way to CHANGE a multiplier live, since those
 * also keep this object in sync, see part 88's doc), the shared {@link ModEventBus} every mod and
 * the core simulation both fire/subscribe to, and {@link #log} for attributable console output.
 *
 * <p>Built fresh by {@link ModLoader} for each mod (not shared between mods) purely so {@link
 * #log} can prefix every line with the correct mod's own {@link ServerMod#name()} -- the
 * underlying {@link GameLoop}/{@link ServerConfig}/{@link ModEventBus} instances are the exact
 * same live objects for every mod, not per-mod copies.
 */
public final class ModContext {

    private final GameLoop gameLoop;
    private final ServerConfig config;
    private final ModEventBus events;
    private final Consumer<String> sink;
    private final String modName;

    ModContext(GameLoop gameLoop, ServerConfig config, ModEventBus events, Consumer<String> sink, String modName) {
        this.gameLoop = gameLoop;
        this.config = config;
        this.events = events;
        this.sink = sink;
        this.modName = modName;
    }

    /** The live, fully-wired {@link GameLoop} this mod is running against. */
    public GameLoop gameLoop() {
        return gameLoop;
    }

    /** The live {@link ServerConfig}. Prefer {@link GameLoop}'s {@code setXxxMultiplier} methods (part 88) to change a tuning knob, since those also keep this object in sync with the live component. */
    public ServerConfig config() {
        return config;
    }

    /** The shared event bus every mod and the core simulation both fire/subscribe to -- see {@link ModEventBus}'s class doc and {@code net.stalzone.emulator.modding.events} for the real event types fired. */
    public ModEventBus events() {
        return events;
    }

    /** Writes {@code message} to the same sink {@code SingleplayerServer}'s own event lines go to, prefixed with {@code [mod:<this mod's name>]} so operator console output stays attributable when multiple mods are loaded. */
    public void log(String message) {
        sink.accept("[mod:" + modName + "] " + message);
    }
}
