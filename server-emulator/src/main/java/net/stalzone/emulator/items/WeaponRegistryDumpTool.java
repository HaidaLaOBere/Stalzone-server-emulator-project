package net.stalzone.emulator.items;

import net.stalzone.emulator.config.ServerConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * CLI demo, same spirit as the other *DumpTool classes: loads the real weapons.json dataset,
 * applies a chosen damageMultiplier, and prints before/after numbers for one or more named
 * weapons -- so "damage values are configurable" is something you can see happen, not just a
 * claim. This is the simplest possible end-to-end proof of the config -> data pipeline this
 * project is for.
 *
 * <p>As of part 30, also prints every real field {@link WeaponEntry} gained that part -- real
 * headshot/limb multipliers (with the effective, multiplier-applied number alongside, same as
 * base damage), the raw {@code damageFalloff} curve (printed as-is, not interpolated -- see that
 * method's doc for why), and the melee-only extras (reach, piercing, bloodlust chance, crit and
 * vs.-mutant multipliers). This makes the tool a real "look up everything this project knows
 * about one weapon" reference, not just a damage-multiplier demo.
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.items.WeaponRegistryDumpTool <weapons.json> <damageMultiplier> [weaponName...]
 *
 * Example:
 *   java -cp out net.stalzone.emulator.items.WeaponRegistryDumpTool weapons.json 2.0 "AK-12" "PM" "Hunter's Knife"
 */
public final class WeaponRegistryDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 2) {
            System.err.println("usage: WeaponRegistryDumpTool <weapons.json> <damageMultiplier> [weaponName...]");
            System.exit(2);
        }

        Path weaponsJson = Path.of(args[0]);
        double multiplier = Double.parseDouble(args[1]);

        ServerConfig config = new ServerConfig();
        config.damageMultiplier = multiplier;

        WeaponRegistry registry = WeaponRegistry.load(weaponsJson);
        System.out.println("Loaded " + registry.size() + " weapon records from " + weaponsJson);
        System.out.println("damageMultiplier = " + multiplier);
        System.out.println();

        String[] names = args.length > 2
                ? java.util.Arrays.copyOfRange(args, 2, args.length)
                : new String[] { "AK-12", "PM", "Hunter's Knife" };

        for (String name : names) {
            WeaponEntry entry = registry.byName(name);
            if (entry == null) {
                System.out.println(name + ": not found in dataset");
                continue;
            }
            printEntry(entry, registry, config);
        }
    }

    private static void printEntry(WeaponEntry entry, WeaponRegistry registry, ServerConfig config) {
        System.out.println(entry.name() + "  (id=" + entry.id() + ", category=" + entry.category()
                + ", rank=" + entry.rank() + ", source=" + orDefault(entry.source(), "stalcraftdb.net") + ")");

        if (entry.isFirearm()) {
            double base = entry.damage();
            double effective = registry.effectiveDamage(entry, config);
            System.out.printf(Locale.ROOT, "  damage: %.2f -> %.2f (base x %.2f)%n", base, effective, config.damageMultiplier);
            if (entry.rateOfFireRpm() != null) {
                System.out.println("  rate of fire: " + fmt(entry.rateOfFireRpm()) + " rpm, magazine: "
                        + fmt(entry.magazineCapacity()));
            }
            if (entry.headshotMultiplier() != null) {
                System.out.printf(Locale.ROOT, "  headshot: x%.2f -> effective %.2f%n",
                        entry.headshotMultiplier(), registry.effectiveHeadshotDamage(entry, config));
            }
            if (entry.limbMultiplier() != null) {
                System.out.printf(Locale.ROOT, "  limb hit: x%.2f -> effective %.2f%n",
                        entry.limbMultiplier(), registry.effectiveLimbDamage(entry, config));
            }
            if (entry.overheatAfterShots() != null) {
                System.out.println("  overheats after: " + fmt(entry.overheatAfterShots()) + " shots");
            }
            double[] falloff = entry.damageFalloff();
            if (falloff != null) {
                System.out.printf(Locale.ROOT,
                        "  damage falloff (boundaries officially confirmed by EXBO's public API, part 39): "
                                + "%.1f dmg out to %.0fm, decreasing to %.1f dmg by %.0fm, max range %.0fm%n",
                        falloff[0], falloff[1], falloff[2], falloff[3], falloff[4]);
                double mid = (falloff[1] + falloff[3]) / 2.0;
                System.out.printf(Locale.ROOT,
                        "    at %.0fm, %.0fm, %.0fm (midpoint of the ramp, linear-assumption, part 39): "
                                + "%.1f, %.1f, %.1f%n",
                        falloff[1], mid, falloff[3],
                        registry.effectiveDamageAtDistance(entry, falloff[1], config),
                        registry.effectiveDamageAtDistance(entry, mid, config),
                        registry.effectiveDamageAtDistance(entry, falloff[3], config));
            }
        } else if (entry.isMelee()) {
            double[] common = registry.effectiveMeleeCommonDamage(entry, config);
            double[] strong = registry.effectiveMeleeStrongDamage(entry, config);
            System.out.printf(Locale.ROOT, "  light attack: %.1f-%.1f -> %.1f-%.1f%n",
                    entry.meleeDamageMinCommon(), entry.meleeDamageMaxCommon(), common[0], common[1]);
            if (strong != null) {
                System.out.printf(Locale.ROOT, "  heavy attack: %.1f-%.1f -> %.1f-%.1f%n",
                        entry.meleeDamageMinStrong(), entry.meleeDamageMaxStrong(), strong[0], strong[1]);
            }
            if (entry.meleeReachCommon() != null) {
                System.out.println("  reach: " + fmt(entry.meleeReachCommon())
                        + (entry.meleeReachStrong() != null ? " (light) / " + fmt(entry.meleeReachStrong()) + " (heavy)" : ""));
            }
            if (entry.meleePiercing() != null) {
                System.out.println("  piercing: " + fmt(entry.meleePiercing()));
            }
            if (entry.meleeBloodlustChancePct() != null) {
                System.out.println("  bloodlust chance: " + fmt(entry.meleeBloodlustChancePct()) + "%");
            }
            if (entry.meleeCriticalHitMultiplier() != null) {
                System.out.printf(Locale.ROOT, "  critical hit multiplier: x%.2f (no crit-chance field in this "
                        + "dataset -- not wired into GameLoop.tickCombat, see that method's doc)%n",
                        entry.meleeCriticalHitMultiplier());
            }
            if (entry.meleeMutantDamageMultiplier() != null) {
                System.out.printf(Locale.ROOT, "  vs. mutant multiplier: x%.2f (this IS wired into "
                        + "GameLoop.tickCombat -- every combat target there is a mutant)%n",
                        entry.meleeMutantDamageMultiplier());
            }
        } else {
            System.out.println("  no damage field in this record (likely a device/detector)");
        }
        System.out.println();
    }

    private static String fmt(Double d) {
        if (d == null) return "?";
        return (d == Math.floor(d)) ? String.valueOf(d.longValue()) : String.valueOf(d);
    }

    private static String orDefault(String s, String fallback) {
        return s != null ? s : fallback;
    }
}
