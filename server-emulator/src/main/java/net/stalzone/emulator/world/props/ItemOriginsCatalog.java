package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 55-line {@code item_origins.*} namespace in {@code stalker/lang/en.lang}
 * (part 139, the nineteenth concrete namespace built from part 123's follow-up census) -- real
 * loot-source/"where did this item come from" tooltip text (53 {@code name} + 2 {@code
 * item_origin_tip} = 55 exactly), modeled with the established generic last-dot-split (id, field)
 * table; several real ids nest two segments deep (e.g. {@code bag.murmur}, {@code
 * wpn.gauss_pistol}), handled without special-casing.
 *
 * <p><b>A real confirmed crosswalk into part 135's {@link DmzLangCatalog} leagues.</b> The 6 real
 * {@code dmz_league_<codename>} ids literally NAME which of {@code dmz.leagues.<n>} each
 * codename is: {@code legend}="...entering the Baron's Guard league..." (leagues.1), {@code
 * master}="...Market Elite league..." (leagues.2), {@code veteran}="...Market Agents league..."
 * (leagues.3), {@code stalker}="...Black Raiders league..." (leagues.4), {@code newbie}=
 * "...Market Operatives league..." (leagues.5="Operatives" -- a real minor textual variance, an
 * extra "Market " prefix, disclosed not merged), {@code picklock}="...Market Recruits league..."
 * (leagues.6="Recruits", same real "Market " variance). All 6 real lines date this ladder to the
 * "Black Raid event ranking (2024)".
 *
 * <p><b>A real NEW "Back to the Inside Out" 2025 ranked event.</b> The 6 real {@code
 * inside_league_<codename>} ids (same 6 codenames: {@code newbie}/{@code picklock}/{@code
 * stalker}/{@code veteran}/{@code master}/{@code legend}) name an entirely separate real ranked
 * ladder for a "Back to the Inside Out event ranking (2025)": {@code picklock}="Shadow", {@code
 * newbie}="Devastated", {@code stalker}="Distorted", {@code veteran}="Phantom", {@code
 * master}="Risen", {@code legend}="Legend of the Inside Out" -- a genuinely new real tier-name
 * roster for this project's long-running "Inside Out" thread (parts 106/122/{@link
 * CustomNpcsCatalog}/{@link TextureGuiCatalog}/{@link TextureItemsCatalog}/{@link
 * TextureParticlesCatalog}). The tier CODENAME vocabulary itself ({@code newbie}/{@code
 * picklock}/{@code stalker}/{@code veteran}/{@code master}/{@code legend}) recurs a third time in
 * this project as the real word "Picklock", already a named reputation rank on part 107's {@link
 * CnpsCatalog} `csky` settlement ladder -- disclosed as a recurring real tier-naming convention
 * across independent systems, not claimed to be the same underlying rank structure.
 *
 * <p>Other real finds: {@code arsenal.name}="Arsenal Workshop" directly corroborates part 109's
 * {@link CurrencyDisplayCatalog} `arsenal_reputation` find; {@code arcade_<n>.name} (8 real level
 * thresholds: 2/5/15/35/75/150/220/300) is a real "session modes" level-reward ladder; {@code
 * bag.murmur}/{@code arm.zivcas_commander_player}/{@code wpn.gauss_pistol}/{@code wpn.gauss} tie
 * to the already-confirmed Murmur faction, the "ZIVCAS CORPORATION" seasonal event (part 115),
 * and the Gauss Rifle ammo economy (part 106).
 */
public final class ItemOriginsCatalog {

    private final Map<String, Map<String, String>> fieldsById;

    private ItemOriginsCatalog(Map<String, Map<String, String>> fieldsById) {
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "item_origins.";

    public static ItemOriginsCatalog load(LangCatalog catalog) {
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
        return new ItemOriginsCatalog(fieldsById);
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    public String name(String id) { return field(id, "name"); }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}
