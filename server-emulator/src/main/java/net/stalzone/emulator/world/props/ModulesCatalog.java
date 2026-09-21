package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 59-line {@code modules.*} namespace in {@code stalker/lang/en.lang} (part
 * 138, the eighteenth concrete namespace built from part 123's follow-up census) -- a real weapon
 * "module"/attachment perk description system, confirmed by direct count to split into 2 real
 * shapes with no leftover lines (50 + 9 = 59): {@code modules.<id>.description} (50 lines, real
 * stat-affecting perks, e.g. {@code gyroscope}="significantly reduces recoil buildup", {@code
 * marksman}="increases damage to the head and limbs in exchange for reduced damage to plate
 * Durability") and {@code modules.<id>.behaviour.description} (9 lines, a real distinct
 * sub-category of special on-hit bullet-effect modules -- {@code gas_bullets}, {@code
 * foam_bullets}, {@code tranquilizer}, {@code seaker}, {@code heal_bullets}, {@code blastrain},
 * {@code gravity_on_hit}, {@code pyromaniac}, {@code lizard_strike} -- kept as their own real ids
 * (e.g. {@code gas_bullets.behaviour}) by the same generic last-dot-split scheme used throughout
 * this project, with no special-casing required.
 *
 * <p>A real recurring {@code _pre}/{@code _aff}/{@code _suf} affix family (e.g. {@code
 * recoil_pre}/{@code recoil_aff}="reduces recoil", {@code recoil_suf}="increases recoil") reads
 * as a real positive/negative modifier-text pair for a procedural mod-naming system, disclosed as
 * observed rather than interpreted further. One real value, {@code ballista.description}="ballista
 * 1", is disclosed as-is -- a literal placeholder-looking client string, not corrected. The real
 * ids {@code inside}/{@code inside2} (damage-distribution modules) are unrelated to this
 * project's already-confirmed "Inside Out" seasonal event/mode ({@link CustomNpcsCatalog} et al.)
 * -- checked, a coincidental short-word match only, not a content overlap.
 */
public final class ModulesCatalog {

    private final Map<String, String> descriptionById;

    private ModulesCatalog(Map<String, String> descriptionById) {
        this.descriptionById = descriptionById;
    }

    private static final String PREFIX = "modules.";
    private static final String SUFFIX = ".description";

    public static ModulesCatalog load(LangCatalog catalog) {
        Map<String, String> descriptionById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.endsWith(SUFFIX)) {
                String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
                descriptionById.put(id, catalog.get(key));
            }
        }
        return new ModulesCatalog(descriptionById);
    }

    public String description(String id) { return descriptionById.get(id); }

    /** Real module ids with a disclosed description, sorted (behaviour-group ids end in {@code .behaviour}). */
    public List<String> ids() {
        List<String> out = new ArrayList<>(descriptionById.keySet());
        Collections.sort(out);
        return out;
    }
}
