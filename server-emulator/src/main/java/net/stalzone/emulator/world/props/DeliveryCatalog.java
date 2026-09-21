package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 65-line {@code delivery.*} namespace in {@code stalker/lang/en.lang} (part
 * 136, the sixteenth concrete namespace built from part 123's follow-up census) -- a real
 * courier/parcel "delivery" contract system, previously undocumented as a distinct feature in
 * this project. Confirmed by direct count to be a single real flat shape with zero exceptions:
 * all 65 lines fit {@code delivery.<contractId>.name}, one field only, so this class keeps a
 * simple flat map rather than the generic (id, field) table used elsewhere.
 *
 * <p>Real contract ids combine an {@code abstract_} prefix, an existing real zone code (already
 * confirmed by {@link net.stalzone.emulator.world.LocationNameRegistry} and this project's other
 * location-naming classes -- {@code les}, {@code pd}, {@code armsklad}, {@code zaton}, {@code
 * yama}, {@code mg}, {@code agroprom}, {@code svalka}, {@code kordon}, {@code bolota}, {@code td},
 * {@code nbolota}, plus a real {@code north_*}/{@code wnorth_*} sub-zone family: {@code
 * north_mines}="Mines", {@code north_inflow}="Inflow", {@code north_institute}="Institute",
 * {@code north_forge}="Forge-11", {@code north_hrop}="Temple Swamp", {@code
 * north_barrier}="Barrier", {@code north_serpentinte}="Serpentine", {@code wnorth}="Wild Northern
 * Locations", {@code wnorth_yanov}="Uranus Mill", {@code wnorth_rijik}="The Red Forest", {@code
 * wnorth_zaton}="Stillwaters"), and usually a faction tag ({@code bandit}/{@code scientist}/
 * {@code stalker}), with an optional {@code _premium} suffix variant. Every real {@code
 * _premium} id checked shares a BYTE-IDENTICAL display name with its non-premium counterpart
 * (e.g. {@code abstract_agroprom_bandit}/{@code abstract_agroprom_bandit_premium} both ="Kolos
 * Agrocomplex") -- real evidence "premium" is a reward/difficulty tier of the same delivery
 * contract, not a separately-named one. This namespace introduces no new real zone codes beyond
 * what this project has already confirmed; its real value is the delivery-contract SYSTEM
 * structure itself (location + faction + tier), not new place names.
 */
public final class DeliveryCatalog {

    private final Map<String, String> nameById;

    private DeliveryCatalog(Map<String, String> nameById) {
        this.nameById = nameById;
    }

    private static final String PREFIX = "delivery.";
    private static final String SUFFIX = ".name";

    public static DeliveryCatalog load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.endsWith(SUFFIX)) {
                String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
                nameById.put(id, catalog.get(key));
            }
        }
        return new DeliveryCatalog(nameById);
    }

    public String name(String id) { return nameById.get(id); }

    /** Real contract ids with a disclosed name, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(nameById.keySet());
        Collections.sort(out);
        return out;
    }
}
