package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real base-building/crafting-hub data from {@code stalker/lang/en.lang}'s {@code hideout}
 * namespace (250 lines; part 90's {@link LangCatalog} counted it but explicitly flagged it "an
 * unconfirmed base-building-system lead, not investigated" -- this class is that investigation,
 * part 106).
 *
 * <p>Confirms a real player-hideout base-building system distinct from {@code
 * world/props/CraftRecipeCatalog}'s recipes (that class covers what's craftable; this class covers
 * where/how). Real crafting-station categories ({@code hideout.benches.categories.<path>.name}, 42
 * lines, some nested two levels deep): {@code ammunition}="Ammo" (with real per-caliber
 * subcategories {@code 9mm}, {@code 10mm}, {@code 545mm}="5.45 mm", {@code 556mm}="5.56 mm", {@code
 * 762mm}="7.62 mm", {@code 939mm}="9x39 mm", {@code 127mm}="12.7 mm", {@code 12mm}="12 caliber",
 * {@code 23mm}="23x75 mm"), {@code furniture}="Functional objects" (workshop/kitchen/laboratory/
 * generator/storage subcategories), {@code materials}="Materials" (real subcategories: blanks,
 * components, electronics, solutions, {@code gematology}="Hematology", mixtures="Compounds",
 * reagents, {@code ferments}="Enzymes", metals, ingredients, raw), {@code pyrotechnics}=
 * "Explosives" (hand grenades, underbarrel grenades), {@code decorative}="Decorative furniture",
 * {@code engineering}, {@code armor}="Armor Plates" (steel/ceramic/composite), {@code medicine}
 * (infection protection), {@code cooking}, {@code brewing}="Alcohol", {@code other}. A separate,
 * smaller {@code benches.subcategory.<id>.name} family (2 lines) adds {@code keyholders}="Key
 * Holders" and {@code stashes}="Stashes".
 *
 * <p>8 real crafting skill perks ({@code hideout.perks.<id>.name}/{@code .desc}/{@code
 * .descShort}): {@code ammunition}, {@code pyrotechnics}, {@code armorer}="Protective Gear", {@code
 * engineering}, {@code cooking}, {@code brewing}="Home-Brewing" (real desc: "Craft raw materials
 * and alcohol at the kitchen table... Skill Experience: Moonshining"), {@code medicine}, {@code
 * materials}="Raw Materials" -- each crafted at a specific real station ("the workbench", "the
 * kitchen table", "the laboratory table").
 *
 * <p>~38 real workbench-upgrade "features" ({@code hideout.feature.<id>.name}/{@code .desc}, most
 * sharing the literal real desc "Access to new recipes"): a real progression from basic tools
 * ({@code screwdrivers}, {@code wrenches_kit}, {@code electronics_kit}) through precision/power
 * tooling ({@code precise_tools}, {@code precise_powertools}, {@code lathe}, {@code cnc}, {@code
 * milling_machine}, {@code robotic_turning_center}) to real scientific/lab equipment ({@code
 * centrifuge}, {@code chemical_reactor}, {@code matter_synthesizer}="Matter Synthesizer" -- a real
 * named piece of Zone-adjacent pseudo-science hardware, {@code chromatographic_equipment}, {@code
 * laminar_box}="Laminar Flow Box"), plus 5 real vendor-access unlocks ({@code access/auction},
 * {@code access/barter}, {@code access/buyer}="Access to Fence", {@code access/mail}, {@code
 * access/supplier}) and 3 real generator energy-source options ({@code
 * generator_energy_source_gas}="GASES", {@code _battery}="BATTERIES", {@code _anomal}=
 * "ANOMALOUS" -- a real anomalous-energy hideout-power mechanic).
 *
 * <p>5 real generator/storage upgrade metrics ({@code hideout.metric.<id>.name}): {@code
 * extra_space}="Storage Capacity", {@code generator_slots}="Recycling Cells", {@code
 * generator_storage_modifier}="Energy Capacity", {@code generator_energy_recycle_modifier}=
 * "Processing Effectiveness", {@code generator_disintegration_rate_modifier}="Fission speed of
 * fuel sources", {@code generator_researcher_disintegration_rate_modifier}="Fission speed at the
 * Researcher" -- confirming a real fuel-disintegration/energy-recycling hideout-generator
 * subsystem. <b>Part 174 fix:</b> each of these 6 real metrics also carries its own real {@code
 * hideout.metric.<id>.value} line -- a real numeric-display format template, not previously
 * modeled by this class (only {@code .name} was captured; {@code .value} silently fell through
 * the branch chain with no accessor and no disclosure, unlike the deliberately-excluded {@code
 * misc.*}/{@code hookah.*} families below). Found via an exhaustive real-vs-captured line-count
 * audit (a Python script mirroring this class's exact branch logic against the full real file),
 * the same methodology that found part 173's {@code pda.waypoint.we} gap. Real values, verbatim:
 * {@code extra_space}="+{value}", {@code generator_slots}="+{value}", {@code
 * generator_storage_modifier}="+{value}%", {@code generator_energy_recycle_modifier}="+{value}%",
 * {@code generator_disintegration_rate_modifier}="+{value}%/SEC", {@code
 * generator_researcher_disintegration_rate_modifier}="+{value}%/SEC" -- each a real UI format
 * string with a single {@code {value}} placeholder, confirming these 6 metrics are percentage- or
 * flat-count-formatted (not further specified beyond what the literal template shows, per rule 1).
 *
 * <p>A real combinatorial hideout-naming generator ({@code hideout.titlePool.leftSide.<id>}, 26
 * real base nouns e.g. {@code barracks}="Barracks", {@code lair}, {@code catacombs}, {@code
 * fortress}, {@code refuge}="Hideout"; {@code hideout.titlePool.rightSide.<id>}, 28 real "Of
 * &lt;X&gt;" suffixes) that independently corroborates this project's existing real faction/entity
 * roster from a brand-new angle: {@code covenant}="Of Covenant", {@code military}="Of Military",
 * {@code bandits}="Of Bandits", {@code mercenaries}="Of Mercenaries", {@code zory}="Of Rise"
 * (confirming the {@code zory}/"Rise" faction crosswalk independently of prior parts), {@code
 * rubezh}="Of Frontier" (confirming {@code rubezh}="Frontier" independently), and {@code
 * whisperers}="Of Murmur" -- directly tying the real "Murmur" antagonist entity (already confirmed
 * via {@code battlefield.id.pripyat.description} and part 92's SSPP thread) to a Russian-derived
 * internal id, "whisperers" (Murmur -&gt; one who whispers), not previously recorded anywhere in
 * this project.
 *
 * <p>7 real hideout NPC vendor roles ({@code hideout.quickVendors.<id>.title}): {@code auction},
 * {@code mail}, {@code trader}="Gear Merchant", {@code supplier}="Supplies Merchant", {@code
 * buyer}="Fence", {@code researcher}="Researcher", {@code repairman}="Mechanic". 3 real placement
 * categories ({@code hideout.category.<id>}: {@code hideout}="Hideout", {@code furnishing}=
 * "Furniture", {@code special}="Special") with 16 real subcategories total ({@code
 * hideout.subcategory.<categoryId>.<id>}, e.g. {@code special.trophies}="Trophies", {@code
 * special.events}="Thematic", {@code furnishing.cabinets}="Cabinets and Nightstands").
 *
 * <p>One real standalone string, {@code hideout.firstWeatherStyleIntroMail}, discloses a real main
 * -storyline plot beat previously uncatalogued by this project: "...complete the southern
 * storyline: decide the fate of the Blue Sphere and join one of the northern factions." -- a real
 * named story macguffin ("the Blue Sphere") and confirmation that joining a real "northern faction"
 * is a real story-gated unlock condition for Hideout access, tying to this project's existing
 * {@code north}/{@code wnorth} zone-code findings. {@code hideout.conveniences.shared.hint}
 * confirms hideouts are visitable by friends-list players. Not pursued further: {@code
 * hideout.misc.*} (3 short UI-flag strings), {@code hideout.hookah.*} (2 lines, a decorative
 * hookah item's flavor text).
 *
 * <p>Live-verified directly against the real client's {@code en.lang} on 2026-09-11; the part 174
 * {@code metric.*.value} fix live-verified again on 2026-09-20, confirming a full real-vs-captured
 * reconciliation with zero remaining undisclosed dropped lines (the only real lines this class now
 * excludes are the already-disclosed {@code hideout.misc.*} and {@code hideout.hookah.*} families).
 */
public final class HideoutCatalog {

    private static final String PREFIX = "hideout.";

    private final Map<String, String> perkNameById;
    private final Map<String, String> perkDescriptionById;
    private final Map<String, String> perkDescriptionShortById;
    private final Map<String, String> benchCategoryNameByPath;
    private final Map<String, String> benchSubcategoryNameById;
    private final Map<String, String> featureNameById;
    private final Map<String, String> featureDescriptionById;
    private final Map<String, String> metricNameById;
    private final Map<String, String> metricValueById;
    private final Map<String, String> quickVendorTitleById;
    private final Map<String, String> categoryNameById;
    private final Map<String, String> subcategoryNameByPath;
    private final Map<String, String> titlePoolLeftById;
    private final Map<String, String> titlePoolRightById;
    private final String firstWeatherStyleIntroMail;
    private final String sharedHint;

    private HideoutCatalog(Map<String, String> perkNameById,
                            Map<String, String> perkDescriptionById,
                            Map<String, String> perkDescriptionShortById,
                            Map<String, String> benchCategoryNameByPath,
                            Map<String, String> benchSubcategoryNameById,
                            Map<String, String> featureNameById,
                            Map<String, String> featureDescriptionById,
                            Map<String, String> metricNameById,
                            Map<String, String> metricValueById,
                            Map<String, String> quickVendorTitleById,
                            Map<String, String> categoryNameById,
                            Map<String, String> subcategoryNameByPath,
                            Map<String, String> titlePoolLeftById,
                            Map<String, String> titlePoolRightById,
                            String firstWeatherStyleIntroMail,
                            String sharedHint) {
        this.perkNameById = perkNameById;
        this.perkDescriptionById = perkDescriptionById;
        this.perkDescriptionShortById = perkDescriptionShortById;
        this.benchCategoryNameByPath = benchCategoryNameByPath;
        this.benchSubcategoryNameById = benchSubcategoryNameById;
        this.featureNameById = featureNameById;
        this.featureDescriptionById = featureDescriptionById;
        this.metricNameById = metricNameById;
        this.metricValueById = metricValueById;
        this.quickVendorTitleById = quickVendorTitleById;
        this.categoryNameById = categoryNameById;
        this.subcategoryNameByPath = subcategoryNameByPath;
        this.titlePoolLeftById = titlePoolLeftById;
        this.titlePoolRightById = titlePoolRightById;
        this.firstWeatherStyleIntroMail = firstWeatherStyleIntroMail;
        this.sharedHint = sharedHint;
    }

    public static HideoutCatalog load(LangCatalog catalog) {
        Map<String, String> perkNameById = new LinkedHashMap<>();
        Map<String, String> perkDescriptionById = new LinkedHashMap<>();
        Map<String, String> perkDescriptionShortById = new LinkedHashMap<>();
        Map<String, String> benchCategoryNameByPath = new LinkedHashMap<>();
        Map<String, String> benchSubcategoryNameById = new LinkedHashMap<>();
        Map<String, String> featureNameById = new LinkedHashMap<>();
        Map<String, String> featureDescriptionById = new LinkedHashMap<>();
        Map<String, String> metricNameById = new LinkedHashMap<>();
        Map<String, String> metricValueById = new LinkedHashMap<>();
        Map<String, String> quickVendorTitleById = new LinkedHashMap<>();
        Map<String, String> categoryNameById = new LinkedHashMap<>();
        Map<String, String> subcategoryNameByPath = new LinkedHashMap<>();
        Map<String, String> titlePoolLeftById = new LinkedHashMap<>();
        Map<String, String> titlePoolRightById = new LinkedHashMap<>();
        String firstWeatherStyleIntroMail = null;
        String sharedHint = null;

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());

            if (rest.startsWith("perks.")) {
                String tail = rest.substring("perks.".length());
                int lastDot = tail.lastIndexOf('.');
                if (lastDot > 0) {
                    String id = tail.substring(0, lastDot);
                    String field = tail.substring(lastDot + 1);
                    if (field.equals("name")) {
                        perkNameById.put(id, catalog.get(key));
                    } else if (field.equals("desc")) {
                        perkDescriptionById.put(id, catalog.get(key));
                    } else if (field.equals("descShort")) {
                        perkDescriptionShortById.put(id, catalog.get(key));
                    }
                }
            } else if (rest.startsWith("benches.categories.") && rest.endsWith(".name")) {
                String path = rest.substring("benches.categories.".length(), rest.length() - ".name".length());
                benchCategoryNameByPath.put(path, catalog.get(key));
            } else if (rest.startsWith("benches.subcategory.") && rest.endsWith(".name")) {
                String id = rest.substring("benches.subcategory.".length(), rest.length() - ".name".length());
                benchSubcategoryNameById.put(id, catalog.get(key));
            } else if (rest.startsWith("feature.")) {
                String tail = rest.substring("feature.".length());
                if (tail.endsWith(".name")) {
                    featureNameById.put(tail.substring(0, tail.length() - ".name".length()), catalog.get(key));
                } else if (tail.endsWith(".desc")) {
                    featureDescriptionById.put(tail.substring(0, tail.length() - ".desc".length()), catalog.get(key));
                } else if (tail.endsWith(".value")) {
                    // generator_energy_source_* uses .name/.value instead of .name/.desc; folded
                    // into featureDescriptionById under the same id for uniform lookup.
                    featureDescriptionById.put(tail.substring(0, tail.length() - ".value".length()), catalog.get(key));
                }
            } else if (rest.startsWith("metric.") && rest.endsWith(".name")) {
                String id = rest.substring("metric.".length(), rest.length() - ".name".length());
                metricNameById.put(id, catalog.get(key));
            } else if (rest.startsWith("metric.") && rest.endsWith(".value")) {
                // Part 174 fix: real .value format-template lines alongside .name, previously
                // silently dropped (this branch chain checked only .name for the metric. prefix).
                String id = rest.substring("metric.".length(), rest.length() - ".value".length());
                metricValueById.put(id, catalog.get(key));
            } else if (rest.startsWith("quickVendors.") && rest.endsWith(".title")) {
                String id = rest.substring("quickVendors.".length(), rest.length() - ".title".length());
                quickVendorTitleById.put(id, catalog.get(key));
            } else if (rest.startsWith("titlePool.leftSide.")) {
                String id = rest.substring("titlePool.leftSide.".length());
                titlePoolLeftById.put(id, catalog.get(key));
            } else if (rest.startsWith("titlePool.rightSide.")) {
                String id = rest.substring("titlePool.rightSide.".length());
                titlePoolRightById.put(id, catalog.get(key));
            } else if (rest.startsWith("subcategory.")) {
                String path = rest.substring("subcategory.".length());
                subcategoryNameByPath.put(path, catalog.get(key));
            } else if (rest.startsWith("category.")) {
                String id = rest.substring("category.".length());
                categoryNameById.put(id, catalog.get(key));
            } else if (rest.equals("firstWeatherStyleIntroMail")) {
                firstWeatherStyleIntroMail = catalog.get(key);
            } else if (rest.equals("conveniences.shared.hint")) {
                sharedHint = catalog.get(key);
            }
        }

        return new HideoutCatalog(perkNameById, perkDescriptionById, perkDescriptionShortById,
                benchCategoryNameByPath, benchSubcategoryNameById, featureNameById, featureDescriptionById,
                metricNameById, metricValueById, quickVendorTitleById, categoryNameById, subcategoryNameByPath,
                titlePoolLeftById, titlePoolRightById, firstWeatherStyleIntroMail, sharedHint);
    }

    public String perkName(String id) {
        return perkNameById.get(id);
    }

    public String perkDescription(String id) {
        return perkDescriptionById.get(id);
    }

    public String perkDescriptionShort(String id) {
        return perkDescriptionShortById.get(id);
    }

    public String benchCategoryName(String path) {
        return benchCategoryNameByPath.get(path);
    }

    public String benchSubcategoryName(String id) {
        return benchSubcategoryNameById.get(id);
    }

    public String featureName(String id) {
        return featureNameById.get(id);
    }

    public String featureDescription(String id) {
        return featureDescriptionById.get(id);
    }

    public String metricName(String id) {
        return metricNameById.get(id);
    }

    /** Part 174: the real {@code .value} format-template line for this metric id (e.g. {@code "+{value}%"}), or null. */
    public String metricValue(String id) {
        return metricValueById.get(id);
    }

    public String quickVendorTitle(String id) {
        return quickVendorTitleById.get(id);
    }

    public String categoryName(String id) {
        return categoryNameById.get(id);
    }

    public String subcategoryName(String categoryId, String subcategoryId) {
        return subcategoryNameByPath.get(categoryId + "." + subcategoryId);
    }

    public String titlePoolLeft(String id) {
        return titlePoolLeftById.get(id);
    }

    public String titlePoolRight(String id) {
        return titlePoolRightById.get(id);
    }

    public String firstWeatherStyleIntroMail() {
        return firstWeatherStyleIntroMail;
    }

    public String sharedHint() {
        return sharedHint;
    }

    public List<String> perkIds() {
        List<String> ids = new ArrayList<>(perkNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public List<String> titlePoolLeftIds() {
        List<String> ids = new ArrayList<>(titlePoolLeftById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public List<String> titlePoolRightIds() {
        List<String> ids = new ArrayList<>(titlePoolRightById.keySet());
        Collections.sort(ids);
        return ids;
    }
}
