package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 12,427-line {@code item.*} namespace in {@code stalker/lang/en.lang} (part
 * 121) -- the single largest namespace on part 90's original "top 15 by count" list, and the LAST
 * one remaining from that list besides the still-partial top-level {@code gui.*} (204 of 395 lines
 * left, unrelated to {@link StalkerLangCatalog}'s already-fully-covered {@code stalker.gui.*}).
 * Named {@code ItemLangCatalog} (not {@code ItemCatalog}/{@code ItemsCatalog}) to avoid confusion
 * with this project's many existing, unrelated item-related classes ({@link ItemModelCatalog}
 * [file-based 3D models], {@link net.stalzone.emulator.economy.BarterItemCatalog}, {@link
 * net.stalzone.emulator.items.ItemGroups}, {@link net.stalzone.emulator.items.ItemTiers}, {@link
 * net.stalzone.emulator.items.ItemWeightTable}).
 *
 * <p>A direct count established this is the THIRD namespace this project has found (after part
 * 116's {@link ChallengesCatalog} and part 120's {@link GoLangCatalog}) with zero real flat
 * exceptions: every one of the 12,427 lines fits the shape {@code item.<id>.<field>}, letting the
 * whole namespace be modeled with the same single generic {@code (id, field)} table -- 7,834
 * distinct real item ids, carrying one of many real field names dominated by {@code name} (7,545
 * lines) and {@code description} (2,345), with a long real tail including {@code
 * additional_stats_tip} (1,411), {@code lore} (392), {@code short_name} (223), {@code
 * item_origin_tip} (179), {@code case_description} (115), and dozens of rarer one-off field names.
 *
 * <p>The namespace covers the real display text for this game's ENTIRE item catalog: real-world
 * firearm names under {@code wpn.<id>} (e.g. {@code kedr}="PP-91 KEDR", {@code mp5}="HK MP5",
 * {@code uzi}="IMI Uzi", {@code aks74}="AKS-74", {@code ak74m}="AK-74M"), plus real cosmetic/gear
 * sub-families sampled but NOT exhaustively enumerated here (the point of a single generic table
 * is that no enumeration is needed to use the data): {@code camo.<id>} (967 lines, weapon skins),
 * {@code custom.<id>} (805, cosmetic customization items -- including a real deeply-nested {@code
 * custom.internal.<n1>.<n2>.<n3>.lore} quest-reward-item flavor-text family), {@code arm.<id>}
 * (748, armor), {@code charm.<id>} (633, charms), {@code misc.<id>} (619), {@code att.<id>} (489,
 * weapon attachments), {@code style.<id>}/{@code motif.<id>} (356/280, cosmetic categories), {@code
 * ny_toys.<id>} (273, a real New Year seasonal cosmetic line), {@code lootbox.<id>} (265), {@code
 * brt.<id>} (256, barter items), {@code plc.<id>} (250), {@code art.<id>} (248, artifacts), {@code
 * bag.<id>} (192, backpacks), {@code med.<id>} (183, medicine), {@code wpn_mod.<id>} (182, weapon
 * modifications), {@code filler.<id>} (171), {@code exp.<id>} (163, explosives -- a real separate
 * id space from part 120's {@code go.exp.<id>}, confirmed distinct by differing id vocabulary, not
 * assumed). A real {@code item.customisation.collection.<seasonId>} crosswalk (8 lines, e.g. {@code
 * summer24}="ZIVCAS Corporation", {@code winter26}="Sub-Zero Zone", {@code
 * triumph26}="Triumphs. Faction Race 2026") gives real display names for several seasonal
 * cosmetic-collection ids, corroborating this project's existing seasonal-event timeline (parts
 * 109/115) from yet another independent angle.
 *
 * <p>This class deliberately does NOT re-model the already-catalogued file-based item rosters
 * ({@link net.stalzone.emulator.items.WeaponRegistry} etc.) -- it exposes real DISPLAY TEXT for
 * item ids, which is new content those file-based catalogs don't carry, not a duplicate of their
 * own id/stat data.
 */
public final class ItemLangCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ItemLangCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "item.";

    public static ItemLangCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new ItemLangCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }
    public String description(String id) { return field(id, "description"); }
    public String lore(String id) { return field(id, "lore"); }
    public String shortName(String id) { return field(id, "short_name"); }
    public String additionalStatsTip(String id) { return field(id, "additional_stats_tip"); }

    /** Real item ids that have at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    public List<String> fieldNames(String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }
}
