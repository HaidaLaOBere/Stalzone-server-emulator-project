# Example mods (part 89)

This folder holds complete, buildable example mods for the emulator's modding support
(`net.stalzone.emulator.modding`, see that package's `ModLoader`/`ServerMod`/`ModContext`/
`ModEventBus` class docs for the full design). Each subfolder is one standalone mod you can build
into a `*.jar` and drop into a running server's `mods/` directory (the path named by
`ServerConfig.modsDir`, `mods` by default).

## example-mod

`example-mod/src/net/stalzone/emulator/examplemod/ExampleMod.java` implements `ServerMod` and
demonstrates the three things a real mod normally does:

- logs a message on load via `ModContext.log`
- doubles the live `damageMultiplier` on load via `ModContext.gameLoop()` -- the exact same
  part-88 live-tuning knob the console's `tune` command uses; a mod is, deliberately, just another
  kind of operator
- subscribes to real simulation events (`MobSpawnedEvent`/`MobKilledEvent`) via
  `ModContext.events()` and logs a running spawn/kill tally

`example-mod/resources/META-INF/services/net.stalzone.emulator.modding.ServerMod` is the
`ServiceLoader` declaration file every mod jar needs -- one line naming the fully-qualified
`ServerMod` implementation class(es) in that jar, one per line if a jar declares more than one mod.

### Building it

Run these from the repository root (`server-emulator/`, next to `sources.txt`). They assume the
emulator itself has already been compiled to `out/` (`find src -name "*.java" > sources.txt &&
javac -encoding UTF-8 -d out @sources.txt`), since a mod compiles against those classes.

```
# 1. Compile the mod against the already-built emulator classes. --release 11 matters here, not
#    just style: a mod jar built for a newer Java version than the server's own runtime crashes
#    the WHOLE server process on startup with an uncaught UnsupportedClassVersionError, confirmed
#    directly during this feature's own live testing (see ModLoader's class doc) -- always target
#    the oldest Java version the server itself supports.
mkdir -p /tmp/example-mod-build
javac --release 11 -encoding UTF-8 -cp out -d /tmp/example-mod-build \
    examples/mods/example-mod/src/net/stalzone/emulator/examplemod/ExampleMod.java

# 2. Copy the ServiceLoader declaration file next to the compiled classes.
mkdir -p /tmp/example-mod-build/META-INF/services
cp examples/mods/example-mod/resources/META-INF/services/net.stalzone.emulator.modding.ServerMod \
    /tmp/example-mod-build/META-INF/services/

# 3. Jar it up -- the jar needs ONLY the mod's own compiled classes and its services file, never
#    the emulator's own classes (ModLoader gives every mod's classloader a parent that can already
#    see those).
jar --create --file example-mod.jar -C /tmp/example-mod-build .

# 4. Install it: drop the jar into the server's mods/ directory (ServerConfig.modsDir, "mods" by
#    default) before starting the server. There is no hot-reload -- mods are discovered once, at
#    SingleplayerServer startup.
mkdir -p mods
cp example-mod.jar mods/
```

Then start the server (`java -cp out net.stalzone.emulator.Main`, once `ServerConfig` is
configured with a real `stalzoneInstallDir` -- see the main README's "Building and running"
section). The startup log should include a `mods: loaded 'ExampleMod' from example-mod.jar` line,
followed by a `damageMultiplier: ... -> ...` line from the mod itself, and then a running
`spawn #N`/`kill #N` tally as the simulation ticks. The console's `mods` command lists every mod
that loaded this way at any time while the server is running.

### Writing your own mod

Copy this folder's shape: one class implementing `ServerMod` (only `onLoad`/`onUnload` are worth
overriding for most mods -- `name()` defaults to the class's simple name), and a
`META-INF/services/net.stalzone.emulator.modding.ServerMod` file naming it. `ModContext` (passed
to `onLoad`) is a mod's entire surface for reaching the running server: `gameLoop()` for every
real tick method and the four part-88 live-tuning setters, `config()` for the live `ServerConfig`,
`events()` to subscribe to any type under `net.stalzone.emulator.modding.events`, and `log(...)`
for output tagged with your mod's name. Compile your mod with `--release 11` (or whatever Java
version the server you're targeting actually runs) -- a mod jar built for a newer Java version is
safely skipped rather than crashing the server, but it still won't load.

A broken mod (a bad jar, a missing services entry, a `ServerMod` that can't be instantiated, an
`onLoad`/`onUnload` that throws, or a class file built for too new a Java version) is logged and
skipped -- it never stops any other mod, or the server itself, from starting. See `ModLoader`'s own
class doc for the full fault-isolation design.
