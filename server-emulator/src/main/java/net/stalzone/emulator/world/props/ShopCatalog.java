package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 731-line {@code shop.*} namespace in {@code stalker/lang/en.lang} (part 115) --
 * the in-client item-shop UI's own text: tab labels, promo/bundle detail pages, a live-service
 * seasonal-event timeline, currency/payment info, and the shop's item/offer display labels. This is
 * the largest single namespace this project has fully catalogued outside {@code we} (508) and
 * {@code item}/{@code go}/{@code stalker}/{@code general}/{@code quest_contract}/{@code
 * challenges} (all still uninvestigated, per {@link LangCatalog}'s top-15 list). Real sub-groupings
 * genuinely differ in shape, so -- following the precedent established at parts 111/113 -- most are
 * modeled as generic {@code (id, field)} tables rather than one fixed schema, with a handful of
 * simple flat {@code id -> value} maps where the real data only ever carries one field.
 *
 * <p><b>{@code season.<id>.name}</b> (10 lines) is the most valuable single find here: a real,
 * complete named seasonal-event timeline running summer24 through autumn26 -- {@code
 * summer24}="ZIVCAS CORPORATION", {@code autumn24}="BLACK MARKET: INTERVENTION", {@code
 * winter25}="OPERATION CRYOSTASIS", {@code spring25}="BACK TO THE INSIDE OUT", {@code
 * summer25}="MURMUR OF THE ZONE", {@code autumn25}="HORRORS OF THE ZONE", {@code
 * winter26}="SUB-ZERO ZONE", {@code spring26}="TEMPERED STEEL", {@code
 * summer26}="ANTICYCLONE", {@code autumn26}="TULPA!" -- the last two ({@code spring26}/{@code
 * autumn26}) are real season names this project had not seen anywhere before this part. {@code
 * categories.mt.title=ASSAULT ON THE REICHSTAG} and {@code categories.mk.title=SEA OF EXCLUSION}
 * tie the shop's own "mt"/"mk" category codes directly to the confirmed Reichstag battlefield and
 * {@link OperationsCatalog}'s "Sea of Exclusion Operation" respectively -- the Reichstag tie is
 * disclosed per the project's part-109 standing note as simply another entry in the long
 * corroboration chain, with no new ordinal number asserted.
 *
 * <p><b>{@code tabs.*}</b> (117 lines) is modeled by {@link #tabField} as {@code (tabId, field)}
 * with {@code field} grouped under the OUTERMOST real tab id (e.g. {@code
 * special.main_page.primaryAction.text} resolves under tab id {@code special}, field {@code
 * main_page.primaryAction.text}) -- real shop tab labels for the full cosmetic/gear/season-pass
 * navigation tree, including a real "REFRESHES DAILY" personal-offer rotation banner
 * ({@code special.personal_offers}) and a linked "GO TO EVENT" call-to-action.
 *
 * <p><b>{@code offers.*}</b> (247 lines) is modeled by {@link #offerField} as {@code (offerId,
 * field)} but split the OPPOSITE way from {@code tabs} -- {@code field} is always the terminal
 * simple token ({@code titleLabel}/{@code typeLabel}/{@code name}/{@code desc}/{@code
 * warningMessage}), so {@code offerId} is everything before it, including any embedded dots (e.g.
 * {@code items.dayx_pack.titleLabel} resolves under offer id {@code items.dayx_pack}, field {@code
 * titleLabel}). One real flat exception, {@code shop.offers.amount={amount} UNITS} (a template
 * string with no id/field structure at all), is exposed separately via {@link
 * #offersAmountTemplate()} rather than force-fit into the table. A real, previously-uncatalogued
 * NPC/system name surfaces here too: {@code offers.bundles.customization_spring25.titleLabel=BACK
 * TO THE INSIDE OUT} and its siblings name each season's profile-customization bundle after that
 * season's own real name (cross-checked against {@code season.*} above, consistent).
 *
 * <p><b>{@code customTags.*}</b> (73 lines) is modeled by {@link #customTagField} as {@code (tagId,
 * field)} -- real shop badge/tag text ({@code new}="NEW", {@code tradeable}="TRADABLE GEAR", {@code
 * sequence}="PACK CHAIN"). Two real ids, {@code bundle_dmz26_discount} and {@code
 * ny_tree_gifts_discount}, carry {@code desc_global}/{@code desc_ru} variants with genuinely
 * DIFFERENT real discount percentages for the same tier (e.g. {@code
 * bundle_dmz26_discount.desc_global}: "36% discount on 6th-10th purchases" vs. {@code
 * ..._ru}: "40% discount on 6th-10th purchases") -- a real, disclosed regional-pricing variance
 * within the English locale file itself, not merged into one value.
 *
 * <p><b>{@code dlc.*}</b> (69 lines) is modeled by {@link #dlcField} as {@code (dlcId, field)} --
 * a real Battle Pass/season-pack promotional timeline going back to {@code autumun_2023_bp}
 * ("Dead Man's Marathon"), predating every other seasonal-timeline source this project has found
 * ({@code season.*} above only starts at summer24). {@code refund.mainTitle}="Compensation" and
 * {@code refund.mailText} document a real automatic-refund-to-stalcoins mailer for an unused pack
 * whose associated event has ended.
 *
 * <p><b>{@code category.<id>.name}</b> (49 lines, one real flat exception: {@code
 * category.bundles=Season Pack} has no {@code .name} suffix at all -- disclosed and folded into the
 * same {@link #categoryName} accessor rather than treated as an error) is a real shop-filter
 * taxonomy with compound real ids expressing a two-level category/sub-category hierarchy ({@code
 * weapon}="Weapons", {@code weapon.sub.melee}="Melee" etc. for {@code weapon}/{@code armor}/{@code
 * equip}/{@code supplies}/{@code rank}/{@code month}).
 *
 * <p><b>{@code categories.<id>.title}</b> (15 lines, one field only, flat), {@code
 * displayedCurrencies.*}</b> (20 lines, modeled generically by {@link #displayedCurrencyField} --
 * real currency-info panel text: {@code stalbucks} earned only "by recycling and selling Equipment
 * from the store", {@code stalcoins} obtained only "by replenishing the account"), {@code
 * deepPage.filters.title.<key>}</b> (9 lines, flat, real armor/weapon/melee/container style-motif-
 * part filter tab labels), {@code topup.method.<id>.name}</b> (7 lines, flat -- a real real-money
 * payment-method roster: {@code sbp}="FPS", {@code card}="Card", {@code xsolla}="Xsolla", {@code
 * vkplay}="VK Play", {@code 4game}="4game", {@code steam}="Steam", {@code stove}="Stove"), {@code
 * gui.offer.preview.<key>}</b> (4 lines, flat, real offer-preview disclaimer text), and {@code
 * year.<id>.name}</b> (2 lines, flat -- {@code 25}="COLLECTION 2025", {@code 26}="2026 COLLECTION")
 * round out the remaining named sub-groupings.
 *
 * <p><b>Everything else</b> -- roughly twenty real standalone one-off top-level ids not covered by
 * any named grouping above ({@code bundle_newyear24}/{@code bundle_newyear24_1}/{@code
 * bundle_newyear24_2}, {@code bundle_spring24}/{@code _1}/{@code _2}, {@code
 * bundle_halloween23}/{@code _1}/{@code _2}, {@code bundle_summer24}, {@code bundle_autumn24},
 * {@code dayx_pack}, {@code summer24_starter_pack}, {@code autumn24_starter_pack}, {@code
 * free_daily_case}, {@code abc_mini_daily_bundle}, {@code abc_mini_weekly_bundle}, {@code
 * sivorotka_ssop_rotation}, and {@code bundle_ravent23} -- the season-pack page for {@code
 * category.ravent}="Arena of Slavs", this project's part-93-confirmed PvP mode) -- all share one
 * real shape: a promo/bundle detail page with a top-level id and a {@code (field)} suffix that can
 * nest arbitrarily deep (e.g. {@code bundle_newyear24.banner_benefits.gachapons.title}). Rather
 * than hardcode this growing, seasonally-refreshed list of ids by name, {@link #load} treats
 * ANY top-level {@code shop.<id>.<field>} line whose {@code id} does not match one of the named
 * groupings above as belonging to this generic {@link #promoPageField} table -- a deliberately
 * data-driven, forward-compatible design so a future season's new promo page is captured
 * automatically rather than silently dropped.
 */
public final class ShopCatalog {

    private final Map<String, Map<String, String>> tabFieldsById;
    private final Map<String, Map<String, String>> offerFieldsById;
    private final String offersAmountTemplate;
    private final Map<String, Map<String, String>> customTagFieldsById;
    private final Map<String, Map<String, String>> dlcFieldsById;
    private final Map<String, String> categoryNameById;
    private final Map<String, String> categoriesLabelById;
    private final Map<String, String> seasonNameById;
    private final Map<String, Map<String, String>> displayedCurrencyFieldsById;
    private final Map<String, String> deepPageFilterTitleByKey;
    private final Map<String, String> topupMethodNameById;
    private final Map<String, String> guiOfferPreviewTextByKey;
    private final Map<String, String> yearNameById;
    private final Map<String, Map<String, String>> promoPageFieldsById;

    private ShopCatalog(Map<String, Map<String, String>> tabFieldsById,
                         Map<String, Map<String, String>> offerFieldsById,
                         String offersAmountTemplate,
                         Map<String, Map<String, String>> customTagFieldsById,
                         Map<String, Map<String, String>> dlcFieldsById,
                         Map<String, String> categoryNameById,
                         Map<String, String> categoriesLabelById,
                         Map<String, String> seasonNameById,
                         Map<String, Map<String, String>> displayedCurrencyFieldsById,
                         Map<String, String> deepPageFilterTitleByKey,
                         Map<String, String> topupMethodNameById,
                         Map<String, String> guiOfferPreviewTextByKey,
                         Map<String, String> yearNameById,
                         Map<String, Map<String, String>> promoPageFieldsById) {
        this.tabFieldsById = tabFieldsById;
        this.offerFieldsById = offerFieldsById;
        this.offersAmountTemplate = offersAmountTemplate;
        this.customTagFieldsById = customTagFieldsById;
        this.dlcFieldsById = dlcFieldsById;
        this.categoryNameById = categoryNameById;
        this.categoriesLabelById = categoriesLabelById;
        this.seasonNameById = seasonNameById;
        this.displayedCurrencyFieldsById = displayedCurrencyFieldsById;
        this.deepPageFilterTitleByKey = deepPageFilterTitleByKey;
        this.topupMethodNameById = topupMethodNameById;
        this.guiOfferPreviewTextByKey = guiOfferPreviewTextByKey;
        this.yearNameById = yearNameById;
        this.promoPageFieldsById = promoPageFieldsById;
    }

    private static final String PREFIX = "shop.";

    public static ShopCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> tabFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> offerFieldsById = new LinkedHashMap<>();
        String[] offersAmountTemplateHolder = new String[1];
        Map<String, Map<String, String>> customTagFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> dlcFieldsById = new LinkedHashMap<>();
        Map<String, String> categoryNameById = new LinkedHashMap<>();
        Map<String, String> categoriesLabelById = new LinkedHashMap<>();
        Map<String, String> seasonNameById = new LinkedHashMap<>();
        Map<String, Map<String, String>> displayedCurrencyFieldsById = new LinkedHashMap<>();
        Map<String, String> deepPageFilterTitleByKey = new LinkedHashMap<>();
        Map<String, String> topupMethodNameById = new LinkedHashMap<>();
        Map<String, String> guiOfferPreviewTextByKey = new LinkedHashMap<>();
        Map<String, String> yearNameById = new LinkedHashMap<>();
        Map<String, Map<String, String>> promoPageFieldsById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int groupDot = rest.indexOf('.');
            if (groupDot < 0) {
                continue;
            }
            String group = rest.substring(0, groupDot);
            String afterGroup = rest.substring(groupDot + 1);
            String value = catalog.get(key);

            switch (group) {
                case "tabs": {
                    int dot = afterGroup.indexOf('.');
                    if (dot < 0) {
                        continue;
                    }
                    String id = afterGroup.substring(0, dot);
                    String field = afterGroup.substring(dot + 1);
                    tabFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                    break;
                }
                case "offers": {
                    if (afterGroup.equals("amount")) {
                        offersAmountTemplateHolder[0] = value;
                        continue;
                    }
                    int lastDot = afterGroup.lastIndexOf('.');
                    if (lastDot < 0) {
                        continue;
                    }
                    String id = afterGroup.substring(0, lastDot);
                    String field = afterGroup.substring(lastDot + 1);
                    offerFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                    break;
                }
                case "customTags": {
                    int dot = afterGroup.indexOf('.');
                    if (dot < 0) {
                        continue;
                    }
                    String id = afterGroup.substring(0, dot);
                    String field = afterGroup.substring(dot + 1);
                    customTagFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                    break;
                }
                case "dlc": {
                    int dot = afterGroup.indexOf('.');
                    if (dot < 0) {
                        continue;
                    }
                    String id = afterGroup.substring(0, dot);
                    String field = afterGroup.substring(dot + 1);
                    dlcFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                    break;
                }
                case "category": {
                    // Real field is always "name" except one flat exception (category.bundles, no
                    // suffix at all) -- both fold into the same id -> name lookup.
                    String id = afterGroup.endsWith(".name")
                            ? afterGroup.substring(0, afterGroup.length() - ".name".length())
                            : afterGroup;
                    categoryNameById.put(id, value);
                    break;
                }
                case "categories": {
                    if (afterGroup.endsWith(".title")) {
                        String id = afterGroup.substring(0, afterGroup.length() - ".title".length());
                        categoriesLabelById.put(id, value);
                    }
                    break;
                }
                case "season": {
                    if (afterGroup.endsWith(".name")) {
                        String id = afterGroup.substring(0, afterGroup.length() - ".name".length());
                        seasonNameById.put(id, value);
                    }
                    break;
                }
                case "displayedCurrencies": {
                    int dot = afterGroup.indexOf('.');
                    if (dot < 0) {
                        continue;
                    }
                    String id = afterGroup.substring(0, dot);
                    String field = afterGroup.substring(dot + 1);
                    displayedCurrencyFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
                    break;
                }
                case "deepPage": {
                    String marker = "filters.title.";
                    if (afterGroup.startsWith(marker)) {
                        String keyName = afterGroup.substring(marker.length());
                        deepPageFilterTitleByKey.put(keyName, value);
                    }
                    break;
                }
                case "topup": {
                    String marker = "method.";
                    if (afterGroup.startsWith(marker) && afterGroup.endsWith(".name")) {
                        String id = afterGroup.substring(marker.length(), afterGroup.length() - ".name".length());
                        topupMethodNameById.put(id, value);
                    }
                    break;
                }
                case "gui": {
                    String marker = "offer.preview.";
                    if (afterGroup.startsWith(marker)) {
                        String keyName = afterGroup.substring(marker.length());
                        guiOfferPreviewTextByKey.put(keyName, value);
                    }
                    break;
                }
                case "year": {
                    if (afterGroup.endsWith(".name")) {
                        String id = afterGroup.substring(0, afterGroup.length() - ".name".length());
                        yearNameById.put(id, value);
                    }
                    break;
                }
                default: {
                    // A real, growing set of one-off top-level promo/bundle pages, not enumerated by
                    // name -- captured generically so future seasonal ids are not silently dropped.
                    promoPageFieldsById.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(afterGroup, value);
                    break;
                }
            }
        }

        return new ShopCatalog(tabFieldsById, offerFieldsById, offersAmountTemplateHolder[0], customTagFieldsById,
                dlcFieldsById, categoryNameById, categoriesLabelById, seasonNameById, displayedCurrencyFieldsById,
                deepPageFilterTitleByKey, topupMethodNameById, guiOfferPreviewTextByKey, yearNameById,
                promoPageFieldsById);
    }

    private static String field(Map<String, Map<String, String>> fieldsById, String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    private static List<String> ids(Map<String, Map<String, String>> fieldsById) {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    private static List<String> fieldNames(Map<String, Map<String, String>> fieldsById, String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }

    public String tabField(String tabId, String field) { return field(tabFieldsById, tabId, field); }
    public String tabTitle(String tabId) { return tabField(tabId, "title"); }
    public List<String> tabIds() { return ids(tabFieldsById); }
    public List<String> tabFieldNames(String tabId) { return fieldNames(tabFieldsById, tabId); }

    public String offerField(String offerId, String field) { return field(offerFieldsById, offerId, field); }
    public List<String> offerIds() { return ids(offerFieldsById); }
    public List<String> offerFieldNames(String offerId) { return fieldNames(offerFieldsById, offerId); }
    public String offersAmountTemplate() { return offersAmountTemplate; }

    public String customTagField(String tagId, String field) { return field(customTagFieldsById, tagId, field); }
    public String customTagText(String tagId) { return customTagField(tagId, "text"); }
    public List<String> customTagIds() { return ids(customTagFieldsById); }

    public String dlcField(String dlcId, String field) { return field(dlcFieldsById, dlcId, field); }
    public String dlcTitle(String dlcId) { return dlcField(dlcId, "title"); }
    public List<String> dlcIds() { return ids(dlcFieldsById); }

    public String categoryName(String id) { return categoryNameById.get(id); }
    public List<String> categoryIds() {
        List<String> out = new ArrayList<>(categoryNameById.keySet());
        Collections.sort(out);
        return out;
    }

    public String categoriesLabel(String id) { return categoriesLabelById.get(id); }

    public String seasonName(String id) { return seasonNameById.get(id); }
    public List<String> seasonIds() {
        List<String> out = new ArrayList<>(seasonNameById.keySet());
        Collections.sort(out);
        return out;
    }

    public String displayedCurrencyField(String id, String field) { return field(displayedCurrencyFieldsById, id, field); }

    public String deepPageFilterTitle(String key) { return deepPageFilterTitleByKey.get(key); }

    public String topupMethodName(String id) { return topupMethodNameById.get(id); }

    public String guiOfferPreviewText(String key) { return guiOfferPreviewTextByKey.get(key); }

    public String yearName(String id) { return yearNameById.get(id); }

    /** Real raw field value for a standalone promo/bundle page id not covered by any named grouping
     * above (e.g. {@code "bundle_newyear24"}), or null. */
    public String promoPageField(String promoId, String field) { return field(promoPageFieldsById, promoId, field); }
    public List<String> promoPageIds() { return ids(promoPageFieldsById); }
    public List<String> promoPageFieldNames(String promoId) { return fieldNames(promoPageFieldsById, promoId); }
}
