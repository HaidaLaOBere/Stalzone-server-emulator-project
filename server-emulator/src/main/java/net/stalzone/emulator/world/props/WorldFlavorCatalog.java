package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs seven real tiny namespaces from {@code stalker/lang/en.lang} together (part 162, the
 * forty-seventh through fifty-third concrete namespaces built from part 123's follow-up census),
 * kept as separate tables in one class: open-world territory-control objectives, named unique
 * mutants, anomaly-device notifications, death-cause text, one tutorial hint, real item-quality
 * tiers, and real open-world supply-point names.
 *
 * <p><b>{@code territories.*}</b> (8 lines, reconciled 7 + 1) fits the generic last-dot-split (id,
 * field) table for 7 real lines ({@code snork}/{@code boar}/{@code krovosos}/{@code
 * flesh}.name = real mutant-den "Territory" objective names; {@code inside26}.name="Inside Out";
 * {@code spring25_crystal}.name="Small Growth of the Inside Out"/.goal="collect Raw Glass"), plus
 * one real flat zero-dot line ({@code territories.goal}="clear out the territory") via a flat
 * carve-out, the same shape already used by parts 156/157. {@code spring25_crystal} independently
 * ties a real open-world territory-control objective directly to part 158's {@link SeasonCatalog}
 * {@code 25/spring}="Back to the Inside Out" season and part 154's {@link InsideCatalog} event.
 *
 * <p><b>{@code entity.&lt;id&gt;.name}</b> (5 lines, flat PREFIX/SUFFIX strip) names 5 real unique
 * entities: {@code tushkanchik_vor}="Rat-Robber" and {@code kukla}="Doll" (both genuinely new real
 * finds, no prior corroboration in this project when first catalogued), {@code tick}="Tick"
 * (confirming part 69's {@link StalkerMobsCatalog} already-known real {@code tick} mutant id's
 * display name), and a real "royal boar" naming pair, {@code carevich}="Boar Tsarevich" and
 * {@code carevna}="Piggy Princess" -- the former confirms part 69's crowned-boar {@code
 * tsarevich} mutant exactly, but the latter's real "Piggy" (not "Boar") wording REFINES that same
 * part-69 doc's own hedged guess that {@code tsarevna} was "presumably the same royal-boar theme"
 * as {@code tsarevich}: this real display text instead suggests a "flesh"/piggy-mutant theme for
 * {@code tsarevna}, disclosed as new evidence against an earlier presumption, not asserted as
 * fully confirmed.
 *
 * <p><b>Part 178 update:</b> {@code tushkanchik_vor}="Rat-Robber" now has a real, disclosed
 * (not fully string-exact) crosswalk too. Staging and reading the real {@code
 * stalkermobs/configs/base_thief_rat.smm} file directly found its {@code mobName} field reports
 * {@code "tushkan_thief"} -- a distinct, non-generic mobName (not shared with any other of the 35
 * real configs), and "thief" + "tushkan" (jerboa/rat) is exactly the same real compound concept as
 * {@code tushkanchik_vor} ("little-tushkan" + "vor", Russian for "thief") and its own real display
 * text "Rat-Robber" -- the same kind of real transliteration/spelling variance already established
 * for {@code tsarevich}/{@code carevich}, not an exact id match, so disclosed with that same
 * caveat rather than silently assumed. {@code kukla}="Doll" was ALSO investigated this part against
 * {@code base_puppet.smm} (the one remaining unmatched creature name that could plausibly fit) --
 * that config's {@code mobName} is plainly {@code "puppet"} with no "doll"-related field anywhere,
 * and its {@code _sounds} block reuses the {@code tushkan} (jerboa/rat) sound family wholesale, not
 * anything doll-themed. This is disclosed as a checked, INCONCLUSIVE lead, not a confirmed
 * crosswalk -- {@code kukla} is NOT wired into {@link #entityNameForMobConfigId} on the strength of
 * name similarity alone, per rule 1.
 *
 * <p><b>{@code anomaly.notf.*}</b> (4 lines) fits the generic table with 3 real distinct ids:
 * {@code anomaly} (2 fields: {@code title}="Attention!", {@code info}="A noo-converter was
 * installed at the location"), {@code anomaly_chrono} (1 field: {@code info}="A chronodevice has
 * been set up in this location"), {@code anomaly_chro} (1 field: {@code info}, real text
 * IDENTICAL to plain {@code anomaly}'s "noo-converter" line, NOT to {@code anomaly_chrono}'s
 * "chronodevice" line despite the closer id spelling) -- a real client id/text mismatch disclosed
 * exactly as found, not corrected.
 *
 * <p><b>{@code death.attack.&lt;id&gt;.subject}</b> (3 lines, flat PREFIX/SUFFIX strip) names 3
 * real death-cause captions: {@code kissel_2}="{entityName} dissolved into Jellify" (a real,
 * previously-uncatalogued substance name), {@code behaviour_node}="Killed by Zone Owners", {@code
 * laser_rave}="Killed by the Masters of the Zone" (two real, previously-uncatalogued superboss/
 * antagonist-tier faction phrases, not tied to any existing confirmed faction in this project).
 *
 * <p><b>{@code tutorial.hint.medical}</b> (1 real line, a lone flat field) is a real first-aid
 * combat tip: "Don't forget to use the first-aid kit in battle [{key}]".
 *
 * <p><b>{@code item_tier.&lt;id&gt;.name}</b> (4 lines, flat PREFIX/SUFFIX strip) names 4 real
 * item-quality tiers: {@code novice}="Newbie", {@code stalker}="Stalker", {@code
 * veteran}="Veteran", {@code master}="Master". These real display names REUSE, as actual item
 * tier names, the exact same codename vocabulary this project has already found recurring as
 * ranked-ladder codenames in part 139's {@link ItemOriginsCatalog} ({@code dmz_league_&lt;
 * codename&gt;}/{@code inside_league_&lt;codename&gt;}: {@code newbie}/{@code picklock}/{@code
 * stalker}/{@code veteran}/{@code master}/{@code legend}) -- a further, independent real
 * confirmation that this vocabulary functions as a genuine tier-naming convention across
 * multiple, separate real systems, disclosed not merged (this namespace has only 4 of the 6
 * codenames, and no {@code picklock}/{@code legend} entries).
 *
 * <p><b>{@code provision.provision_&lt;id&gt;.name}</b> (7 lines, flat PREFIX/SUFFIX strip) names
 * 7 real supply-point locations. Every one of the 7 real names is an EXACT match for a real
 * location display name already confirmed by part 136's {@link DeliveryCatalog} real {@code
 * north_*}/{@code wnorth_*} sub-zone family: {@code institute}="Institute", {@code
 * serpentine}="Serpentine" (this namespace's own id spells it correctly, unlike {@code
 * DeliveryCatalog}'s real {@code north_serpentinte} id, which carries a real client typo --
 * both, independently, display the correct "Serpentine" text), {@code hrop}="Temple Swamp",
 * {@code barrier}="Barrier", {@code inflow}="Inflow", {@code mines}="Mines", {@code
 * zaton}="Stillwaters". A direct, clean, independently-confirmed real crosswalk between two
 * separate real id namespaces naming the same 7 real locations.
 *
 * <p><b>As of part 165, a further CONFIRMED crosswalk against part 20/69's real {@code MobConfig}
 * roster, now wired live into {@link net.stalzone.emulator.session.GameLoop#tickSpawns} via
 * {@link #entityNameForMobConfigId}.</b> {@code MobConfig}'s own doc already disclosed that the
 * real {@code base_tsarevich.smm}/{@code base_tsarevna.smm} boss-event configs report {@code
 * mobName="boar_tsarevich"}/{@code "flesh_tsarevna"} respectively -- real, in-file evidence that
 * {@code tsarevna} is a FLESH-themed variant, not a second boar-themed one as part 69's own
 * {@link StalkerMobsCatalog} doc had only presumed ("presumably the same royal-boar theme"). This
 * directly CONFIRMS (not merely refines, as this class's own {@code entity.carevna} paragraph
 * above put it before this crosswalk was drawn) that presumption wrong: {@code
 * carevna}="Piggy Princess" here really does mean a "piggy"/flesh-mutant theme, matching {@code
 * mobName="flesh_tsarevna"} exactly. A real transliteration mismatch is disclosed, not
 * papered over: the real {@code .smm} config ids use {@code tsarevich}/{@code tsarevna} (a fuller
 * Cyrillic-to-Latin transliteration of &#1094;&#1072;&#1088;&#1077;&#1074;&#1080;&#1095;/
 * &#1094;&#1072;&#1088;&#1077;&#1074;&#1085;&#1072;), while this namespace's own real ids drop the
 * leading "t" ({@code carevich}/{@code carevna}) -- the SAME real underlying word, transliterated
 * two different real ways by two independent parts of the client. {@code
 * entityNameForMobConfigId} hardcodes this small, fully-evidenced crosswalk ({@code
 * base_tsarevich}, {@code base_tsarevna}, and {@code base_tick} -- the last via {@code
 * MobConfig#configId()} rather than {@code mobName()}, since that config's own {@code mobName}
 * field reports the shared value {@code "tushkan"}, not {@code "tick"}, per that class's doc; part
 * 178 adds a 4th, {@code base_thief_rat}, see that update's own paragraph above) rather than
 * attempting any generic automatic id transformation, precisely because the real transliteration
 * mismatch above means no generic rule would be safe to invent.
 */
public final class WorldFlavorCatalog {

    private final Map<String, Map<String, String>> territoryFieldsById;
    private final Map<String, String> territoryFlatFields;
    private final Map<String, String> entityNameById;
    private final Map<String, Map<String, String>> anomalyFieldsById;
    private final Map<String, String> deathSubjectById;
    private final String tutorialMedicalHint;
    private final Map<String, String> itemTierNameById;
    private final Map<String, String> provisionNameById;

    private WorldFlavorCatalog(Map<String, Map<String, String>> territoryFieldsById, Map<String, String> territoryFlatFields,
            Map<String, String> entityNameById, Map<String, Map<String, String>> anomalyFieldsById,
            Map<String, String> deathSubjectById, String tutorialMedicalHint, Map<String, String> itemTierNameById,
            Map<String, String> provisionNameById) {
        this.territoryFieldsById = territoryFieldsById;
        this.territoryFlatFields = territoryFlatFields;
        this.entityNameById = entityNameById;
        this.anomalyFieldsById = anomalyFieldsById;
        this.deathSubjectById = deathSubjectById;
        this.tutorialMedicalHint = tutorialMedicalHint;
        this.itemTierNameById = itemTierNameById;
        this.provisionNameById = provisionNameById;
    }

    private static final String TERRITORIES_PREFIX = "territories.";
    private static final String ENTITY_PREFIX = "entity.";
    private static final String ENTITY_SUFFIX = ".name";
    private static final String ANOMALY_PREFIX = "anomaly.";
    private static final String DEATH_PREFIX = "death.attack.";
    private static final String DEATH_SUFFIX = ".subject";
    private static final String TUTORIAL_MEDICAL_KEY = "tutorial.hint.medical";
    private static final String ITEM_TIER_PREFIX = "item_tier.";
    private static final String ITEM_TIER_SUFFIX = ".name";
    private static final String PROVISION_PREFIX = "provision.provision_";
    private static final String PROVISION_SUFFIX = ".name";

    public static WorldFlavorCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> territoryFieldsById = new LinkedHashMap<>();
        Map<String, String> territoryFlatFields = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(TERRITORIES_PREFIX)) {
            String rest = key.substring(TERRITORIES_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                territoryFlatFields.put(rest, catalog.get(key));
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            territoryFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, String> entityNameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(ENTITY_PREFIX)) {
            if (!key.endsWith(ENTITY_SUFFIX)) {
                continue;
            }
            String id = key.substring(ENTITY_PREFIX.length(), key.length() - ENTITY_SUFFIX.length());
            entityNameById.put(id, catalog.get(key));
        }

        Map<String, Map<String, String>> anomalyFieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(ANOMALY_PREFIX)) {
            String rest = key.substring(ANOMALY_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            anomalyFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, String> deathSubjectById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(DEATH_PREFIX)) {
            if (!key.endsWith(DEATH_SUFFIX)) {
                continue;
            }
            String id = key.substring(DEATH_PREFIX.length(), key.length() - DEATH_SUFFIX.length());
            deathSubjectById.put(id, catalog.get(key));
        }

        String tutorialMedicalHint = catalog.get(TUTORIAL_MEDICAL_KEY);

        Map<String, String> itemTierNameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(ITEM_TIER_PREFIX)) {
            if (!key.endsWith(ITEM_TIER_SUFFIX)) {
                continue;
            }
            String id = key.substring(ITEM_TIER_PREFIX.length(), key.length() - ITEM_TIER_SUFFIX.length());
            itemTierNameById.put(id, catalog.get(key));
        }

        Map<String, String> provisionNameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PROVISION_PREFIX)) {
            if (!key.endsWith(PROVISION_SUFFIX)) {
                continue;
            }
            String id = key.substring(PROVISION_PREFIX.length(), key.length() - PROVISION_SUFFIX.length());
            provisionNameById.put(id, catalog.get(key));
        }

        return new WorldFlavorCatalog(territoryFieldsById, territoryFlatFields, entityNameById, anomalyFieldsById,
                deathSubjectById, tutorialMedicalHint, itemTierNameById, provisionNameById);
    }

    public String territoryField(String id, String field) {
        Map<String, String> fields = territoryFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** The real flat zero-dot {@code territories.goal} field. */
    public String territoryFlatField(String field) {
        return territoryFlatFields.get(field);
    }

    /** A real unique entity's display name, e.g. {@code carevich} -&gt; "Boar Tsarevich". */
    public String entityName(String id) {
        return entityNameById.get(id);
    }

    public String anomalyField(String id, String field) {
        Map<String, String> fields = anomalyFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** A real death-cause caption, e.g. {@code behaviour_node} -&gt; "Killed by Zone Owners". */
    public String deathSubject(String id) {
        return deathSubjectById.get(id);
    }

    /** The one real {@code tutorial.hint.medical} line. */
    public String tutorialMedicalHint() {
        return tutorialMedicalHint;
    }

    /** A real item-quality tier's display name, e.g. {@code veteran} -&gt; "Veteran". */
    public String itemTierName(String id) {
        return itemTierNameById.get(id);
    }

    /** A real supply-point location's display name, e.g. {@code zaton} -&gt; "Stillwaters". */
    public String provisionName(String id) {
        return provisionNameById.get(id);
    }

    /**
     * A confirmed real, hardcoded crosswalk (part 165, extended part 178) from a real {@code
     * net.stalzone.emulator.mobs.MobConfig#configId()} to this catalog's own {@code entity.*}
     * display name, for the exact 4 real mob configs this project has independently confirmed a
     * match for -- see this class's own top-level doc for the full evidence (including the real
     * {@code tsarevich}/{@code carevich} and {@code tsarevna}/{@code carevna} transliteration
     * mismatch, and part 178's {@code base_thief_rat}/{@code tushkanchik_vor} addition, both
     * hardcoded around rather than derived generically since no generic rule would be safe to
     * invent). Returns null for every other real {@code configId} in the 35-file mob roster,
     * including {@code base_puppet} -- {@code kukla}="Doll" was checked against it and found
     * inconclusive, not wired here. This is NOT a general mob display-name lookup, only the 4
     * entries this project has actual evidence for.
     */
    public String entityNameForMobConfigId(String mobConfigId) {
        if ("base_tsarevich".equals(mobConfigId)) {
            return entityName("carevich");
        }
        if ("base_tsarevna".equals(mobConfigId)) {
            return entityName("carevna");
        }
        if ("base_tick".equals(mobConfigId)) {
            return entityName("tick");
        }
        if ("base_thief_rat".equals(mobConfigId)) {
            return entityName("tushkanchik_vor");
        }
        return null;
    }
}
