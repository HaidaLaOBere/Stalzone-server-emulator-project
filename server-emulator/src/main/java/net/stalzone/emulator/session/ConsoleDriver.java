package net.stalzone.emulator.session;

import net.stalzone.emulator.items.WeaponEntry;
import net.stalzone.emulator.mobs.MobInstance;
import net.stalzone.emulator.modding.LoadedMod;
import net.stalzone.emulator.movement.MovementSimulator;
import net.stalzone.emulator.progression.PlayerClass;
import net.stalzone.emulator.progression.PrestigeAbility;
import net.stalzone.emulator.progression.QuestStatus;
import net.stalzone.emulator.session.GameLoop.HitLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Part 86: an operator/console input source for a running {@link SingleplayerServer}, closing the
 * exact gap that class's own doc has flagged since part 84 -- {@link GameLoop#tickMovement},
 * {@link GameLoop#tickHazardCheck}, both {@code tickCombat} overloads, {@link
 * GameLoop#tickPurchase}, {@link GameLoop#tickAbilityUse}, and {@link GameLoop#tickQuestEvent} are
 * all real, evidenced tick methods (parts 29/30/39/82/83/85), but until this part nothing in this
 * project had ever called any of them against a REAL running {@code SingleplayerServer} -- only
 * {@code GameLoopDemo}'s own disclosed, scripted demo run does. Until the real wire protocol is
 * read (still gated behind explicit per-instance go-ahead, docs/RESEARCH_LOG.md part 7), there is
 * no real network input to drive these methods with; this class is the "in the meantime a
 * console/manual driver" caller {@code SingleplayerServer}'s own doc already anticipated -- a
 * human operator, typing commands into the running server's own process, standing in for that
 * missing real input source.
 *
 * <p><b>This invents no new game content.</b> Every command below is a thin, explicitly-labeled
 * wrapper around one already-evidenced {@code GameLoop} tick method, passed exactly the
 * caller-supplied parameters those methods' own docs already disclose as NOT reverse-engineered
 * data (target coordinates, hazard type/position, trader id/price, which real ability/quest id).
 * The one command that is NOT a tick-method wrapper, {@code unlock}, is a direct operator grant
 * (adds a real {@link PrestigeAbility} straight to {@link PlayerState#unlockedAbilities}) rather
 * than a simulated unlock condition -- nothing in this project's evidence says HOW a real STALZONE
 * account unlocks an ability, so this is deliberately labeled as operator/admin control (matching
 * this project's own "full control over what can be done" framing), not a claim about real
 * progression mechanics. As of part 87, {@code save} is also not a tick-method wrapper -- it calls
 * {@link SingleplayerServer#saveNow()} directly, writing every state change any command above has
 * made to disk via {@link PlayerStateSave} (this emulator's own persistence format), so a session
 * survives a restart instead of resetting every time (which is also what {@link
 * SingleplayerServer#close()} already does automatically on shutdown). As of part 88, {@code tune}
 * is a third command that is not a tick-method wrapper -- it calls {@link
 * GameLoop#setSpawnRateMultiplier}/{@link GameLoop#setLootRateMultiplier}/{@link
 * GameLoop#setDamageMultiplier}/{@link GameLoop#setEmissionRateMultiplier}, finally letting this
 * project's own headline "full control over ... spawn rates or damage values" knobs actually be
 * changed on a LIVE running server -- see those methods' own docs for why three of the four
 * silently had no live effect at all before this part. As of part 89, {@code mods} is a fourth
 * command that is not a tick-method wrapper -- it lists every {@link LoadedMod} {@link
 * SingleplayerServer#loadedMods()} already loaded at startup (name + source JAR), a read-only
 * status view; a mod's own behavior is driven by the events it subscribed to on {@link
 * SingleplayerServer#events()}, not by anything typed at this console.
 *
 * <p>Reads one command per line from a supplied {@link BufferedReader} (typically wrapping {@code
 * System.in}) until end-of-stream or {@code quit}/{@code exit}, writing every response line to a
 * supplied sink (typically {@code System.out::println}) -- the same {@code Consumer<String>}
 * shape {@link SingleplayerServer}'s own event sink already uses. A blank line is ignored; an
 * unknown command, a missing argument, or a malformed argument (bad number, unknown enum name) is
 * caught and reported as a console line, never thrown -- one typo does not kill the console thread
 * or the background simulation. {@code help} lists every real command. {@code quit}/{@code exit}
 * stops only this console loop -- the {@link SingleplayerServer} it was constructed with keeps
 * ticking in the background exactly as before, same as closing a terminal does not stop a
 * detached background process.
 */
public final class ConsoleDriver implements Runnable, AutoCloseable {

    private final SingleplayerServer server;
    private final BufferedReader input;
    private final Consumer<String> output;
    private volatile boolean running = true;

    public ConsoleDriver(SingleplayerServer server, BufferedReader input, Consumer<String> output) {
        this.server = server;
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        output.accept("Console driver ready (part 86) -- type 'help' for commands, 'quit' to stop "
                + "this console (the background simulation keeps ticking).");
        try {
            String line;
            while (running && (line = input.readLine()) != null) {
                handle(line);
            }
        } catch (IOException e) {
            output.accept("console: input stream closed (" + e.getMessage() + ") -- console driver stopping");
        }
    }

    @Override
    public void close() {
        running = false;
    }

    void handle(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String[] parts = trimmed.split("\\s+");
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        try {
            switch (cmd) {
                case "help":
                    printHelp();
                    break;
                case "status":
                    printStatus();
                    break;
                case "mobs":
                    printMobs();
                    break;
                case "inventory":
                    printInventory();
                    break;
                case "move":
                    doMove(parts);
                    break;
                case "hazard":
                    doHazard(parts);
                    break;
                case "attack":
                    doAttack(parts);
                    break;
                case "buy":
                    doBuy(parts);
                    break;
                case "unlock":
                    doUnlock(parts);
                    break;
                case "useability":
                    doUseAbility(parts);
                    break;
                case "class":
                    doClass(parts);
                    break;
                case "quest":
                    doQuest(parts);
                    break;
                case "save":
                    doSave();
                    break;
                case "tune":
                    doTune(parts);
                    break;
                case "mods":
                    printMods();
                    break;
                case "quit":
                case "exit":
                    running = false;
                    output.accept("console: stopping this console (the background simulation keeps running)");
                    break;
                default:
                    output.accept("console: unknown command '" + cmd + "' -- type 'help'");
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            output.accept("console: bad command '" + trimmed + "' -- " + e + " (type 'help')");
        }
    }

    private void printHelp() {
        output.accept("Commands (part 86 -- every one below is a thin wrapper around an already-evidenced GameLoop tick method; see ConsoleDriver's class doc):");
        output.accept("  status                                              -- player position/zone, wallet, class, unlocked abilities, quest-log size, alive mob count");
        output.accept("  mobs                                                -- list alive mobs by index (use the index with 'attack')");
        output.accept("  inventory                                           -- list carried item ids/counts");
        output.accept("  move <x> <z> <WALK|RUN|SPRINT|SNEAK|CRAWL> [dtSecs] -- tickMovement (real per-mode speed, part 23)");
        output.accept("  hazard <x> <z> <type>                               -- tickHazardCheck (type/position are caller-supplied, e.g. rad/thr/bio -- no real placement data exists, part 22/71)");
        output.accept("  attack <mobIndex> <weaponName> [body|head|limb] [distanceMeters] -- tickCombat (real weapon damage/multipliers/falloff, part 19/30/39)");
        output.accept("  buy <traderId> <itemId> <count> <currency> <price>  -- tickPurchase (real currency ids money/sc_birthday_21/season_pass_ticket_3, part 27/82)");
        output.accept("  unlock <ABILITY_NAME>                               -- operator grant: adds a real PrestigeAbility (part 66) to this player's unlocked set (not a simulated unlock condition)");
        output.accept("  useability <ABILITY_NAME>                           -- tickAbilityUse (no numeric effect simulated -- see PrestigeAbility's class doc)");
        output.accept("  class <CLASS_NAME>                                  -- sets this player's real PlayerClass (part 66)");
        output.accept("  quest <questId> <NEW|FINISHED|FAILED> [title words...] -- tickQuestEvent (part 40/85, no reward simulated)");
        output.accept("  save                                                -- write this session's state to disk now (part 87 -- also happens automatically on shutdown)");
        output.accept("  tune [spawnRate|lootRate|damage|emissionRate <value>] -- live-retune a multiplier with no restart, or show all four with no arguments (part 88)");
        output.accept("  mods                                                -- list every mod loaded at startup, by name and source JAR (part 89, see examples/mods/)");
        output.accept("  quit / exit                                         -- stop this console only (the background simulation keeps ticking)");
    }

    private void printStatus() {
        PlayerState player = server.player();
        output.accept("status: " + player + " known-zone=" + player.isInKnownZone()
                + " class=" + (player.playerClass == null ? "(none)" : player.playerClass)
                + " abilities=" + player.unlockedAbilities);
        if (player.wallet.currenciesHeld().isEmpty()) {
            output.accept("  wallet: empty");
        } else {
            for (String currency : player.wallet.currenciesHeld()) {
                output.accept("  wallet: " + currency + " = " + player.wallet.balanceOf(currency));
            }
        }
        output.accept("  quest log: " + player.questLog.knownQuestIds().size() + " known, "
                + player.questLog.hiddenQuestIds().size() + " hidden");
        output.accept("  alive mobs: " + server.gameLoop().aliveMobs().size());
    }

    private void printMobs() {
        List<MobInstance> mobs = server.gameLoop().aliveMobs();
        if (mobs.isEmpty()) {
            output.accept("mobs: none alive");
            return;
        }
        for (int i = 0; i < mobs.size(); i++) {
            MobInstance m = mobs.get(i);
            output.accept(String.format("  [%d] %s (%s) -- %.1f/%.1f hp",
                    i, m.config().mobName(), m.config().configId(), m.health(), m.maxHealth()));
        }
    }

    private void printInventory() {
        PlayerState player = server.player();
        List<Integer> ids = player.inventory.itemIds();
        if (ids.isEmpty()) {
            output.accept("inventory: empty");
            return;
        }
        for (int id : ids) {
            output.accept("  item " + id + " x" + player.inventory.countOf(id));
        }
    }

    private void doMove(String[] parts) {
        double x = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        MovementSimulator.MoveMode mode = MovementSimulator.MoveMode.valueOf(parts[3].toUpperCase(Locale.ROOT));
        double deltaSeconds = parts.length > 4 ? Double.parseDouble(parts[4]) : 1.0;
        server.gameLoop().tickMovement(server.player(), x, z, mode, deltaSeconds, output::accept);
    }

    private void doHazard(String[] parts) {
        double x = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        String hazardType = parts[3];
        server.gameLoop().tickHazardCheck(server.player(), x, z, hazardType, output::accept);
    }

    private void doAttack(String[] parts) {
        int index = Integer.parseInt(parts[1]);
        List<MobInstance> mobs = server.gameLoop().aliveMobs();
        if (index < 0 || index >= mobs.size()) {
            output.accept("attack: no mob at index " + index + " (see 'mobs')");
            return;
        }
        WeaponEntry weapon = server.weapons().byName(parts[2]);
        if (weapon == null) {
            output.accept("attack: no weapon named '" + parts[2] + "' in weapons.json (see data/weapons/weapons.json for real names)");
            return;
        }
        HitLocation location = parts.length > 3 ? HitLocation.valueOf(parts[3].toUpperCase(Locale.ROOT)) : HitLocation.BODY;
        MobInstance target = mobs.get(index);
        if (parts.length > 4) {
            double distanceMeters = Double.parseDouble(parts[4]);
            server.gameLoop().tickCombat(server.player(), weapon, target, distanceMeters, location, output::accept);
        } else {
            server.gameLoop().tickCombat(server.player(), weapon, target, location, output::accept);
        }
    }

    private void doBuy(String[] parts) {
        String traderId = parts[1];
        int itemId = Integer.parseInt(parts[2]);
        int count = Integer.parseInt(parts[3]);
        String currency = parts[4];
        long price = Long.parseLong(parts[5]);
        server.gameLoop().tickPurchase(server.player(), traderId, itemId, count, currency, price, output::accept);
    }

    private void doUnlock(String[] parts) {
        PrestigeAbility ability = PrestigeAbility.valueOf(parts[1].toUpperCase(Locale.ROOT));
        boolean added = server.player().unlockedAbilities.add(ability);
        output.accept("unlock: " + ability + (added
                ? " granted (operator action, not a simulated in-game unlock condition -- see ConsoleDriver's class doc)"
                : " was already unlocked -- no change"));
    }

    private void doUseAbility(String[] parts) {
        PrestigeAbility ability = PrestigeAbility.valueOf(parts[1].toUpperCase(Locale.ROOT));
        server.gameLoop().tickAbilityUse(server.player(), ability, output::accept);
    }

    private void doClass(String[] parts) {
        PlayerClass playerClass = PlayerClass.valueOf(parts[1].toUpperCase(Locale.ROOT));
        server.player().playerClass = playerClass;
        output.accept("class: set to " + playerClass);
    }

    private void doSave() {
        try {
            server.saveNow();
            output.accept("save: session state written to disk (part 87 -- see ServerConfig.playerSaveFilePath)");
        } catch (IOException e) {
            output.accept("save: failed -- " + e.getMessage());
        }
    }

    private void doQuest(String[] parts) {
        int questId = Integer.parseInt(parts[1]);
        QuestStatus status = QuestStatus.valueOf(parts[2].toUpperCase(Locale.ROOT));
        String title = parts.length > 3 ? String.join(" ", Arrays.copyOfRange(parts, 3, parts.length)) : null;
        server.gameLoop().tickQuestEvent(server.player(), questId, title, status, output::accept);
    }

    private void doTune(String[] parts) {
        if (parts.length == 1) {
            printTune();
            return;
        }
        String knob = parts[1].toLowerCase(Locale.ROOT);
        double value = Double.parseDouble(parts[2]);
        // Intentionally does not fetch server.gameLoop() until a recognized knob is confirmed
        // below, so an unrecognized knob name is reported without ever touching the server --
        // same "validate/parse before dispatch" discipline as every other command in this class.
        switch (knob) {
            case "spawnrate":
                server.gameLoop().setSpawnRateMultiplier(value);
                output.accept("tune: spawnRateMultiplier set to " + value
                        + " (takes effect starting with the next spawn interval this scheduler computes, part 88)");
                break;
            case "lootrate":
                server.gameLoop().setLootRateMultiplier(value);
                output.accept("tune: lootRateMultiplier set to " + value
                        + " (takes effect on the very next loot roll, part 88)");
                break;
            case "damage":
                server.gameLoop().setDamageMultiplier(value);
                output.accept("tune: damageMultiplier set to " + value + " (applies to the very next attack, part 88)");
                break;
            case "emissionrate":
                boolean applied = server.gameLoop().setEmissionRateMultiplier(value);
                output.accept(applied
                        ? "tune: emissionRateMultiplier set to " + value
                                + " (takes effect starting with the next emission interval this cycle computes, part 88)"
                        : "tune: emissionRateMultiplier stored (" + value + "), but has no live effect right now -- "
                                + "no EmissionCycle is configured on this loop");
                break;
            default:
                output.accept("tune: unknown knob '" + knob + "' -- spawnRate/lootRate/damage/emissionRate (or 'tune' alone to show current values)");
        }
    }

    private void printTune() {
        GameLoop loop = server.gameLoop();
        Double emissionRate = loop.emissionRateMultiplier();
        output.accept("tune: current live multipliers (part 88 -- 'tune <knob> <value>' to change one without a restart):");
        output.accept("  spawnRate    = " + loop.spawnRateMultiplier());
        output.accept("  lootRate     = " + loop.lootRateMultiplier());
        output.accept("  damage       = " + loop.damageMultiplier());
        output.accept("  emissionRate = " + (emissionRate == null ? "(no EmissionCycle configured)" : emissionRate));
    }

    private void printMods() {
        List<LoadedMod> mods = server.loadedMods();
        if (mods.isEmpty()) {
            output.accept("mods: none loaded (part 89 -- see ServerConfig.modsDir, and examples/mods/ for how to build one)");
            return;
        }
        output.accept("mods: " + mods.size() + " loaded (part 89):");
        for (LoadedMod loaded : mods) {
            output.accept("  " + loaded.mod.name() + "  (from " + loaded.jarPath.getFileName() + ")");
        }
    }
}
