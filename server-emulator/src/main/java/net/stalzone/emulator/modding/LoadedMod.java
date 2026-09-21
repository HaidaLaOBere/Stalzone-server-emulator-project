package net.stalzone.emulator.modding;

import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * One successfully-loaded {@link ServerMod} (part 89), paired with the isolated {@link
 * URLClassLoader} {@link ModLoader} built for its JAR (so {@link ModLoader#unloadAll} can close
 * it and release the JAR's file handle) and the JAR path it came from (for log/status lines, e.g.
 * {@code ConsoleDriver}'s {@code mods} command).
 */
public final class LoadedMod {

    public final ServerMod mod;
    public final URLClassLoader classLoader;
    public final Path jarPath;

    LoadedMod(ServerMod mod, URLClassLoader classLoader, Path jarPath) {
        this.mod = mod;
        this.classLoader = classLoader;
        this.jarPath = jarPath;
    }
}
