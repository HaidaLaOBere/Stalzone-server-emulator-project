package net.stalzone.emulator.modding;

/**
 * The interface a third-party mod JAR implements to be discovered and loaded by {@link
 * ModLoader} (part 89) -- this project's own answer to the original ask's "later modding support
 * (similar to what SPT team has done)". A mod JAR declares its implementation via the standard
 * JDK {@link java.util.ServiceLoader} convention: a plain-text file at {@code
 * META-INF/services/net.stalzone.emulator.modding.ServerMod} inside the JAR, naming the fully
 * qualified implementation class (one per line, so a single JAR can bundle more than one mod).
 * No new build tooling or external dependency is required -- {@code ServiceLoader} and {@link
 * java.net.URLClassLoader} are both standard JDK, the same "nothing but the JDK itself"
 * constraint documented in {@code testing/Assertions}'s doc and the README's "Why no Netty"
 * section. See {@code examples/mods/} for a complete, independently-buildable example mod and
 * step-by-step build/install instructions.
 *
 * <p>{@link #onLoad} is called exactly once per discovered mod, after {@code
 * SingleplayerServer}'s constructor has already wired up every core opt-in system (loot tables,
 * mob-health overrides, the emission cycle, the vendor archive, the quest seed, the player-state
 * restore) but before the background simulation starts ticking -- a mod can safely retune a live
 * knob at startup ({@link ModContext#gameLoop()}'s {@code setXxxMultiplier} methods, part 88) or
 * subscribe to events ({@link ModContext#events()}) from inside it. If {@code onLoad} throws,
 * {@link ModLoader} catches it, logs which mod failed and why, and continues loading the
 * remaining mods -- one broken mod must not stop the others, or crash server startup, the same
 * "one typo does not kill the console thread" discipline {@code ConsoleDriver} already applies to
 * bad operator input.
 *
 * <p>{@link #onUnload} is called once, best-effort (also caught and logged, never thrown), when
 * the server shuts down -- a mod that opened its own resources (a file, a background thread) can
 * release them here. Both methods have empty default bodies: a mod that only wants to log a
 * message and register one event listener need not override {@code onUnload} at all.
 */
public interface ServerMod {

    /** A short, human-readable name for this mod, used in every log line this project's own {@code ModLoader}/{@code ModContext} write about it. Defaults to the implementing class's simple name. */
    default String name() {
        return getClass().getSimpleName();
    }

    /** Called once at server startup, after every core system is wired and before the background simulation starts ticking. See this interface's class doc for the exact ordering guarantee and for what happens if this throws. */
    default void onLoad(ModContext context) {
    }

    /** Called once, best-effort, at server shutdown -- exceptions are caught and logged, never propagated. */
    default void onUnload() {
    }
}
