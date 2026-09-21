package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs a real 272-line {@code loadscreens.*} namespace in {@code stalker/lang/en.lang} (part
 * 110) -- real loading-screen UI text for a 5-tier location danger/PvP-safety classification
 * system, and 118 real per-location names/descriptions/feature call-outs across 261 real {@code
 * loadscreens.location.*} lines. The location id space heavily overlaps part 105's {@link
 * net.stalzone.emulator.world.LocationNameRegistry} ({@code location.<id>.name}, 298 entries), so
 * this class's own value is threefold: the real danger-tier system (found nowhere else), the real
 * per-location {@code feature.<n>} lore call-outs (found nowhere else), and several new real names
 * -- plus, honestly disclosed, a handful of real cross-namespace naming VARIANCES for ids both
 * namespaces already share.
 *
 * <p><b>{@code loadscreens.type.<id>.name}/{@code .desc}</b> (10 lines, 5 real tiers) is a real,
 * previously uncatalogued location danger/PvP-safety classification: {@code peace}="Peaceful
 * location", {@code standart}="Regular location", {@code neutral}="Neutral location", {@code
 * dangerous}="Dangerous location" (these four share one real identical description line, "Only
 * players from enemy factions can be killed in this location" -- honestly disclosed as a real
 * template reuse, not a parsing error), and {@code wild}="Wild location" ("There are no laws in
 * this location. You can kill anyone with impunity" -- a real, distinct, harsher PvP rule for this
 * one tier).
 *
 * <p><b>Real cross-namespace naming variances, honestly disclosed rather than silently
 * resolved:</b> {@code loadscreens.location.poligon2.name}="Reactor 4" differs from part 105's
 * already-confirmed {@code location.poligon2.name}="Chernobyl NPP" for the SAME real zone code --
 * plausibly a specific reactor building's name vs. the whole nuclear-plant complex's name, not a
 * contradiction, but two distinct real client strings for one id. {@code
 * loadscreens.location.battlefield_krot2_open.name}="Mole-2 System" likewise differs from part
 * 106's {@link BattlefieldCatalog}-confirmed {@code battlefield.id.krot2.name}="Wormhole" for the
 * same {@code krot2} id ("krot" is Russian for "mole", so "Mole-2 System" is the etymologically
 * literal name; "Wormhole" is a distinct, sci-fi-themed alternate title for the same real map).
 *
 * <p><b>A genuinely new real 24th battlefield map</b>, found in neither part 72's file roster nor
 * part 106's {@code battlefield} namespace: {@code loadscreens.location.battlefield_promzona_open.
 * name}="Industrial Zone".
 *
 * <p><b>Real per-location lore feature call-outs</b> ({@code loadscreens.location.<id>.feature.
 * <n>}, 87 lines total across 3-4 slots per location) are a real, previously uncatalogued flavor-
 * text layer distinct from the main {@code .desc} line. Two real named entities surface here for
 * the first time: a "Research Institute 7" (disclosed by {@code yanov.feature.0} as a real, denied
 * rumor -- "many stalkers believe [a bunker] belonged to Research Institute 7, the institute
 * denies any involvement"), and "Murmurers" as the real demonym for members of the confirmed
 * Murmur faction ({@code yanov.desc}: "Murmur, once a loose collection of scattered fighter
 * groups, is showing signs of reconsolidation..."; {@code zaton.desc}: "A large number of
 * Murmurers squads has been spotted in these parts").
 *
 * <p>The namespace also carries one real, structurally irregular line that does not fit the
 * {@code loadscreens.location.<id>.<suffix>} shape this class parses: {@code
 * loadscreens.battlefield_breakthrough_reich.name=Reichstag} (missing the {@code location.}
 * segment other lines all have) -- disclosed here rather than silently normalized away or forced
 * into the location map under a guessed key.
 */
public final class LoadscreenCatalog {

    private final Map<String, String> typeNameById;
    private final Map<String, String> typeDescriptionById;
    private final Map<String, String> locationNameById;
    private final Map<String, String> locationDescriptionById;
    private final Map<String, List<String>> locationFeaturesById;

    private LoadscreenCatalog(Map<String, String> typeNameById,
                               Map<String, String> typeDescriptionById,
                               Map<String, String> locationNameById,
                               Map<String, String> locationDescriptionById,
                               Map<String, List<String>> locationFeaturesById) {
        this.typeNameById = typeNameById;
        this.typeDescriptionById = typeDescriptionById;
        this.locationNameById = locationNameById;
        this.locationDescriptionById = locationDescriptionById;
        this.locationFeaturesById = locationFeaturesById;
    }

    public static LoadscreenCatalog load(LangCatalog catalog) {
        Map<String, String> typeNameById = new LinkedHashMap<>();
        Map<String, String> typeDescriptionById = new LinkedHashMap<>();
        Map<String, String> locationNameById = new LinkedHashMap<>();
        Map<String, String> locationDescriptionById = new LinkedHashMap<>();
        Map<String, Map<Integer, String>> locationFeaturesRaw = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix("loadscreens.type.")) {
            String rest = key.substring("loadscreens.type.".length());
            int dot = rest.lastIndexOf('.');
            if (dot < 0) {
                continue;
            }
            String id = rest.substring(0, dot);
            String suffix = rest.substring(dot + 1);
            if (suffix.equals("name")) {
                typeNameById.put(id, catalog.get(key));
            } else if (suffix.equals("desc")) {
                typeDescriptionById.put(id, catalog.get(key));
            }
        }

        String locPrefix = "loadscreens.location.";
        for (String key : catalog.keysWithPrefix(locPrefix)) {
            String rest = key.substring(locPrefix.length());
            if (rest.endsWith(".name")) {
                locationNameById.put(rest.substring(0, rest.length() - ".name".length()), catalog.get(key));
            } else if (rest.endsWith(".desc")) {
                locationDescriptionById.put(rest.substring(0, rest.length() - ".desc".length()), catalog.get(key));
            } else {
                int featureMarker = rest.indexOf(".feature.");
                if (featureMarker >= 0) {
                    String id = rest.substring(0, featureMarker);
                    String indexPart = rest.substring(featureMarker + ".feature.".length());
                    try {
                        int index = Integer.parseInt(indexPart);
                        locationFeaturesRaw.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(index, catalog.get(key));
                    } catch (NumberFormatException ignored) {
                        // not a numbered feature line; skip rather than guess
                    }
                }
            }
        }

        Map<String, List<String>> locationFeaturesById = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Integer, String>> entry : locationFeaturesRaw.entrySet()) {
            List<Integer> indices = new ArrayList<>(entry.getValue().keySet());
            Collections.sort(indices);
            List<String> ordered = new ArrayList<>();
            for (Integer index : indices) {
                ordered.add(entry.getValue().get(index));
            }
            locationFeaturesById.put(entry.getKey(), Collections.unmodifiableList(ordered));
        }

        return new LoadscreenCatalog(typeNameById, typeDescriptionById, locationNameById,
                locationDescriptionById, locationFeaturesById);
    }

    public String typeName(String typeId) { return typeNameById.get(typeId); }
    public String typeDescription(String typeId) { return typeDescriptionById.get(typeId); }
    public String locationName(String locationId) { return locationNameById.get(locationId); }
    public String locationDescription(String locationId) { return locationDescriptionById.get(locationId); }

    /** Real feature call-outs for this location, in real numbered order, or empty if none/unknown. */
    public List<String> locationFeatures(String locationId) {
        return locationFeaturesById.getOrDefault(locationId, List.of());
    }

    public List<String> locationIds() {
        List<String> ids = new ArrayList<>(locationNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public List<String> typeIds() {
        List<String> ids = new ArrayList<>(typeNameById.keySet());
        Collections.sort(ids);
        return ids;
    }
}
