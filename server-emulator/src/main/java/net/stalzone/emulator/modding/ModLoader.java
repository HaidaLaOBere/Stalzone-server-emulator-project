package net.stalzone.emulator.modding;

import net.stalzone.emulator.config.ServerConfig;
import net.stalzone.emulator.session.GameLoop;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Discovers and loads every {@link ServerMod} bundled in a {@code *.jar} under {@link
 * ServerConfig#modsDir} (part 89) -- this project's own answer to the original ask's "later
 * modding support (similar to what SPT team has done)", built on nothing but the standard JDK
 * ({@link URLClassLoader} + {@link ServiceLoader}, same "no external dependency access"
 * constraint as everywhere else in this project, see {@code testing/Assertions}'s doc and the
 * README's "Why no Netty" section).
 *
 * <p><b>Discovery.</b> {@code modsDir} not existing, or existing but containing no {@code *.jar}
 * files, is a normal "no mods installed" state, not an error -- {@link #loadAll} returns an empty
 * list either way, same "absent config = feature off" pattern {@link
 * ServerConfig#stalzoneInstallDir}/{@link ServerConfig#lootTablesJsonPath} already use. Each real
 * {@code *.jar} found gets its OWN {@link URLClassLoader}, parented to this class's own loader (so
 * a mod's code can still see this project's classes, e.g. {@link GameLoop}/{@link ServerConfig}) --
 * isolating mods from each other so two unrelated mods bundling different versions of the same
 * third-party library don't collide on one shared classpath, the same reason a real Java plugin
 * host (Bukkit/Forge/Sponge-style -- a fitting analogy, given this project's own Minecraft-origin
 * finding) gives each plugin its own loader rather than one flat classpath for all of them. {@link
 * ServiceLoader#load(Class, ClassLoader)} then finds every class that JAR's own {@code
 * META-INF/services/net.stalzone.emulator.modding.ServerMod} file names.
 *
 * <p><b>Fault isolation, at every layer.</b> A JAR that fails to open is logged and skipped. A JAR
 * that opens but declares no {@link ServerMod} via {@code ServiceLoader} (no services file, or an
 * empty one) is logged as such and skipped -- most likely a plain, non-mod JAR sitting in {@code
 * modsDir} by mistake, or a mod built incorrectly. One provider entry inside a JAR's services file
 * that cannot actually be resolved ({@link ServiceConfigurationError}, e.g. the named class
 * doesn't exist, or exists but doesn't implement {@link ServerMod}, or has no public no-args
 * constructor) is logged and skipped WITHOUT preventing any other provider entry in the SAME jar
 * from still loading.
 *
 * <p>Getting that last guarantee right took a real, empirically-verified correction during this
 * part's own testing: the natural-looking {@code ServiceLoader.load(...).stream().collect(...)}
 * does NOT give per-entry isolation, despite each element notionally being lazy -- collecting the
 * whole stream forces the WHOLE services file to resolve as one eager unit, so a single bad entry
 * anywhere in the file throws out of the collect() call and silently discards every entry in that
 * jar, including good ones listed after it (confirmed directly: a real jar with a bad class name
 * on line 1 and a real, loadable {@link ServerMod} on line 2 lost the good entry too, both when
 * collected via {@link java.util.stream.Collectors#toList()} and when consumed via a plain {@code
 * for}-each over that same stream). The fix actually used here instead walks {@code
 * ServiceLoader.load(ServerMod.class, classLoader).stream().iterator()} one entry at a time by
 * hand, calling {@code hasNext()}/{@code next()} directly rather than a terminal stream operation
 * -- and, critically, a {@link ServiceConfigurationError} thrown BY {@code hasNext()} itself (not
 * {@code next()} -- resolving whether another entry exists is what actually does the class
 * lookup) is caught and {@code hasNext()} is called AGAIN, which was directly confirmed to advance
 * the underlying services-file reader past the bad line and correctly reach every entry after it,
 * one at a time, however many bad entries appear in a row. This is the actual mechanism that
 * delivers the "one bad entry only costs that one entry" guarantee this class doc always
 * intended -- {@code stream()} alone does not.
 *
 * <p>A second real, empirically-confirmed correction from this same live testing: {@code
 * hasNext()}/{@code next()} do not only throw {@link ServiceConfigurationError} -- a mod jar
 * compiled for a newer Java version than this server's own runtime actually crashed the whole
 * server process with an uncaught {@link LinkageError} ({@code UnsupportedClassVersionError}
 * specifically, thrown from deep inside {@code ServiceLoader}'s own class-resolution code, well
 * before {@link ServerConfig}'s own Java-11-compatibility discipline -- see part 83 -- could do
 * anything about a THIRD-PARTY jar someone else built). Every {@code catch
 * (ServiceConfigurationError e)} below is therefore a {@code catch (ServiceConfigurationError |
 * LinkageError e)} instead, at every point a class from an untrusted mod jar first gets resolved
 * or instantiated ({@code hasNext()}, {@code next()}, {@link ServiceLoader.Provider#get()}) --
 * the same "one bad entry/jar must not take anything else down with it" principle, just covering
 * a failure category the original design had not yet been tested against.
 *
 * <p>Finally, a mod whose {@link ServerMod#onLoad} itself throws a {@link RuntimeException} (or,
 * for the same class-resolution reason above, a {@link LinkageError}) is logged and skipped --
 * {@link #loadAll} still returns every OTHER mod (from this jar or any other) that loaded
 * successfully. One broken or malformed mod must never stop the rest of the mods, or the server
 * itself, from starting.
 */
public final class ModLoader {

    private ModLoader() {
    }

    /**
     * Discovers and loads every mod under {@code modsDir}, wiring each one's {@link ModContext} to
     * {@code loop}/{@code config}/{@code events} and calling its {@link ServerMod#onLoad}. Returns
     * every mod that loaded successfully, in the order its JAR/provider entry was encountered
     * (directory-listing order is filesystem-dependent, not alphabetical -- a mod relying on load
     * order against another mod should not assume one).
     */
    public static List<LoadedMod> loadAll(Path modsDir, GameLoop loop, ServerConfig config,
                                           ModEventBus events, Consumer<String> sink) {
        List<LoadedMod> loaded = new ArrayList<>();
        if (modsDir == null || !Files.isDirectory(modsDir)) {
            return loaded; // no mods/ dir -- normal "no mods installed" state, not an error
        }
        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path jar : stream) {
                jars.add(jar);
            }
        } catch (IOException e) {
            sink.accept("mods: failed to list " + modsDir + " -- " + e.getMessage());
            return loaded;
        }
        for (Path jar : jars) {
            loadOneJar(jar, loop, config, events, sink, loaded);
        }
        return loaded;
    }

    private static void loadOneJar(Path jar, GameLoop loop, ServerConfig config, ModEventBus events,
                                    Consumer<String> sink, List<LoadedMod> loaded) {
        URLClassLoader classLoader;
        try {
            URL url = jar.toUri().toURL();
            classLoader = new URLClassLoader(new URL[] { url }, ModLoader.class.getClassLoader());
        } catch (IOException e) {
            sink.accept("mods: failed to open " + jar + " -- " + e.getMessage());
            return;
        }

        // Walked by hand via hasNext()/next() rather than a terminal stream operation (e.g.
        // Collectors.toList(), or a plain for-each) -- see this class's own doc for the real,
        // empirically-confirmed reason: collecting/iterating the stream as one eager unit loses
        // per-entry isolation entirely, silently discarding every entry (including good ones)
        // once any single entry fails to resolve.
        Iterator<ServiceLoader.Provider<ServerMod>> providers = ServiceLoader.load(ServerMod.class, classLoader).stream().iterator();
        boolean sawAnyEntry = false;
        int consecutiveResolutionFailures = 0;
        while (true) {
            boolean hasNext;
            try {
                hasNext = providers.hasNext();
                consecutiveResolutionFailures = 0;
            } catch (ServiceConfigurationError | LinkageError e) {
                sawAnyEntry = true;
                // Confirmed directly: calling hasNext() again advances the underlying
                // services-file reader past the bad line and reaches whatever comes after it --
                // this is what actually isolates a bad entry to just that one entry. LinkageError
                // is caught alongside ServiceConfigurationError because hasNext() itself resolves
                // the next entry's class -- a mod jar built for a newer Java version than this
                // server's own runtime throws UnsupportedClassVersionError (a LinkageError) right
                // here, confirmed live against a real too-new mod jar during this part's testing.
                sink.accept("mods: " + jar.getFileName() + " -- a service declaration entry could not be resolved, skipped -- " + e);
                if (++consecutiveResolutionFailures > 1000) {
                    // Defensive only -- never observed in testing, but a corrupt services file
                    // must not be able to hang this loop forever.
                    sink.accept("mods: " + jar.getFileName() + " -- too many unresolvable service entries in a row, giving up on this jar");
                    return;
                }
                continue;
            }
            if (!hasNext) {
                break;
            }
            sawAnyEntry = true;
            ServiceLoader.Provider<ServerMod> provider;
            try {
                provider = providers.next();
            } catch (ServiceConfigurationError | LinkageError e) {
                sink.accept("mods: " + jar.getFileName() + " -- a service declaration entry could not be resolved, skipped -- " + e);
                continue;
            }
            ServerMod mod;
            try {
                mod = provider.get(); // still its own try/catch -- a resolved provider can still fail to instantiate (e.g. a constructor that throws, or a static initializer that fails)
            } catch (ServiceConfigurationError | LinkageError e) {
                sink.accept("mods: " + jar.getFileName() + " declares a ServerMod that could not be instantiated, skipped -- " + e);
                continue;
            }
            String modName = mod.name();
            try {
                ModContext context = new ModContext(loop, config, events, sink, modName);
                mod.onLoad(context);
            } catch (RuntimeException | LinkageError e) {
                sink.accept("mods: '" + modName + "' from " + jar.getFileName() + " threw during onLoad, skipped -- " + e);
                continue;
            }
            loaded.add(new LoadedMod(mod, classLoader, jar));
            sink.accept("mods: loaded '" + modName + "' from " + jar.getFileName());
        }

        if (!sawAnyEntry) {
            sink.accept("mods: " + jar.getFileName() + " has no META-INF/services/"
                    + ServerMod.class.getName() + " entry -- not a mod JAR, or built incorrectly (see examples/mods/)");
        }
    }

    /** Calls {@link ServerMod#onUnload} (best-effort, caught+logged, never thrown) and closes each mod's isolated {@link URLClassLoader}, in {@code loaded}'s order. */
    public static void unloadAll(List<LoadedMod> loaded, Consumer<String> sink) {
        for (LoadedMod entry : loaded) {
            try {
                entry.mod.onUnload();
            } catch (RuntimeException | LinkageError e) {
                sink.accept("mods: '" + entry.mod.name() + "' threw during onUnload -- " + e);
            }
            try {
                entry.classLoader.close();
            } catch (IOException e) {
                sink.accept("mods: failed to close classloader for " + entry.jarPath.getFileName() + " -- " + e.getMessage());
            }
        }
    }
}
