package net.stalzone.emulator.examplemod;

import net.stalzone.emulator.modding.ModContext;
import net.stalzone.emulator.modding.ServerMod;
import net.stalzone.emulator.modding.events.MobKilledEvent;
import net.stalzone.emulator.modding.events.MobSpawnedEvent;

/**
 * A complete, buildable example {@link ServerMod} (part 89) -- demonstrates the three things a
 * real mod will normally do: log something on load via {@link ModContext#log}, subscribe to real
 * simulation events via {@link ModContext#events()}, and reach one of this project's own part-88
 * live-tuning knobs via {@link ModContext#gameLoop()}. See this folder's README.md for how to
 * build this into a real {@code *.jar} and drop it into a running server's {@code mods/}
 * directory.
 *
 * <p>What this mod actually does, end to end: on load, it doubles the live damage multiplier
 * (a real {@code GameLoop.setDamageMultiplier} call, the exact same knob the {@code tune} console
 * command uses) so every attack against a mob in this session hits twice as hard. It then counts
 * every real mob spawn and kill it's notified of via the event bus, logging a running tally each
 * time. None of this is reverse-engineered STALZONE behavior -- it is this emulator's own example
 * of what "full control... later modding support" (the project's own original stated goal) looks
 * like in practice.
 */
public final class ExampleMod implements ServerMod {

    private int spawnCount = 0;
    private int killCount = 0;

    @Override
    public String name() {
        return "ExampleMod";
    }

    @Override
    public void onLoad(ModContext context) {
        context.log("loaded -- doubling the live damage multiplier and tallying spawns/kills");

        // Reach the exact same live-tuning knob the part-88 'tune' console command uses -- a mod
        // is, deliberately, just another kind of operator (see GameLoop's own class doc).
        double current = context.gameLoop().damageMultiplier();
        context.gameLoop().setDamageMultiplier(current * 2.0);
        context.log("damageMultiplier: " + current + " -> " + context.gameLoop().damageMultiplier());

        context.events().on(MobSpawnedEvent.class, event -> {
            spawnCount++;
            context.log("spawn #" + spawnCount + ": " + event.mob().config().mobName());
        });

        context.events().on(MobKilledEvent.class, event -> {
            killCount++;
            context.log("kill #" + killCount + ": " + event.mob().config().mobName()
                    + " (" + event.loot().size() + " loot drop(s))");
        });
    }

    @Override
    public void onUnload() {
        // ModLoader#unloadAll calls this before closing this mod's own classloader -- a real mod
        // that opened a file/socket/thread of its own would release it here. This example holds
        // nothing that needs releasing, so a log line is all this needs to do.
    }
}
