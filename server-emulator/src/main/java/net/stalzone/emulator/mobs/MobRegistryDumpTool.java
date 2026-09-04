package net.stalzone.emulator.mobs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * CLI demo for MobRegistry, same spirit as the other *DumpTool classes: loads every real
 * {@code *.smm} mob config from a folder and prints behavior flags + faction relations for named
 * mobs -- the AI-tuning analogue of WeaponRegistryDumpTool's damage numbers. No damage/health
 * numbers are printed because the source files don't have any (see MobConfig's doc comment).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.mobs.MobRegistryDumpTool <configs-dir> [configId...]
 *
 * Example:
 *   java -cp out net.stalzone.emulator.mobs.MobRegistryDumpTool modassets/assets/stalkermobs/configs base_boar base_tsarevich
 */
public final class MobRegistryDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: MobRegistryDumpTool <configs-dir> [configId...]");
            System.exit(2);
        }

        Path configsDir = Path.of(args[0]);
        MobRegistry registry = MobRegistry.load(configsDir);
        System.out.println("Loaded " + registry.size() + " mob configs from " + configsDir);
        System.out.println();

        String[] ids = args.length > 1
                ? java.util.Arrays.copyOfRange(args, 1, args.length)
                : new String[] { "base_boar", "base_tsarevich", "base_cybergiant" };

        for (String id : ids) {
            MobConfig config = registry.byConfigId(id);
            if (config == null) {
                System.out.println(id + ": not found (try a filename stem, e.g. base_boar)");
                continue;
            }
            printConfig(config);
        }
    }

    private static void printConfig(MobConfig c) {
        System.out.println(c.configId() + "  (mobName=" + c.mobName() + ")");
        System.out.println("  canWalk=" + c.canWalk() + " canRest=" + c.canRest()
                + " canRunAttack=" + c.canRunAttack() + " bodyEater=" + c.bodyEater());
        System.out.println("  attacks defined: " + c.attacks().size()
                + ", sounds defined: " + c.sounds().size());

        Map<String, String> relations = c.mutantRelations();
        if (relations.isEmpty()) {
            System.out.println("  relations: (none declared)");
        } else {
            StringBuilder enemies = new StringBuilder();
            StringBuilder allies = new StringBuilder();
            for (Map.Entry<String, String> e : relations.entrySet()) {
                if ("ENEMY".equals(e.getValue())) {
                    enemies.append(enemies.length() > 0 ? ", " : "").append(e.getKey());
                } else if ("ALLY".equals(e.getValue())) {
                    allies.append(allies.length() > 0 ? ", " : "").append(e.getKey());
                }
            }
            System.out.println("  enemies: " + (enemies.length() > 0 ? enemies : "(none)"));
            System.out.println("  allies: " + (allies.length() > 0 ? allies : "(none)"));
        }

        System.out.println();
    }
}
