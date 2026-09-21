package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link ShopCatalog}, using a fixture built from real {@code shop.*} lines
 * drawn directly from {@code stalker/lang/en.lang} (part 115, live-verified on device) -- covering
 * each named sub-grouping's own split rule (tabs: outermost id; offers: terminal field), the real
 * flat exceptions ({@code offers.amount}, {@code category.bundles}), the real regional discount-text
 * variance, and the generic catch-all promo-page table.
 */
public final class ShopCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "shop.tabs.personal.title=PERSONAL OFFER",
            "shop.tabs.special.title=SPECIAL OFFER",
            "shop.tabs.special.main_page.title=HOME PAGE",
            "shop.tabs.special.main_page.primaryAction.text=BUY SEASON PASS",
            "shop.offers.items.dayx_pack.typeLabel=SPECIAL",
            "shop.offers.items.dayx_pack.titleLabel=X-DAY SET",
            "shop.offers.bundles.customization_spring25.titleLabel=BACK TO THE INSIDE OUT",
            "shop.offers.personality.name=PERSONALITY",
            "shop.offers.empty_type.name=",
            "shop.offers.amount={amount} UNITS",
            "shop.customTags.new.text=NEW",
            "shop.customTags.new.desc=New stock—for true Trailblazers!",
            "shop.customTags.bundle_dmz26_discount.desc_global=75% discount on 1st and 2nd purchases\\n55% discount on 3rd-5th purchases\\n36% discount on 6th-10th purchases\\nNo discount thereafter",
            "shop.customTags.bundle_dmz26_discount.desc_ru=75% discount on 1st and 2nd purchases\\n55% discount on 3rd-5th purchases\\n40% discount on 6th-10th purchases\\nNo discount thereafter",
            "shop.dlc.autumun_2023_bp.title=Battle Pass",
            "shop.dlc.autumun_2023_bp.subtitle=Dead Man's Marathon",
            "shop.dlc.refund.mainTitle=Compensation",
            "shop.category.weapon.name=Weapons",
            "shop.category.weapon.sub.melee.name=Melee",
            "shop.category.bundles=Season Pack",
            "shop.categories.mt.title=ASSAULT ON THE REICHSTAG",
            "shop.categories.mk.title=SEA OF EXCLUSION",
            "shop.season.summer24.name=ZIVCAS CORPORATION",
            "shop.season.autumn26.name=TULPA!",
            "shop.displayedCurrencies.stalbucks.desc=Stalbucks can be obtained by recycling and selling Equipment from the store.",
            "shop.displayedCurrencies.stalcoins.desc=Stalbucks can only be obtained by replenishing the account",
            "shop.deepPage.filters.title.armor_style=ARMOR STYLES",
            "shop.topup.method.sbp.name=FPS",
            "shop.topup.method.steam.name=Steam",
            "shop.gui.offer.preview.equipment_offer=The equipment is not included in the offer.",
            "shop.year.25.name=COLLECTION 2025",
            "shop.year.26.name=2026 COLLECTION",
            "shop.bundle_ravent23.title=Arena of Slavs",
            "shop.bundle_newyear24.banner_benefits.gachapons.title=10 Cases",
            "shop.dayx_pack.short_title=Special pack"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("shop-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ShopCatalog loadFixture() throws IOException {
        return ShopCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("tabField groups by the outermost real tab id, including nested sub-page fields", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("PERSONAL OFFER", catalog.tabTitle("personal"), "personal tab title");
            assertEquals("SPECIAL OFFER", catalog.tabTitle("special"), "special tab title");
            assertEquals("HOME PAGE", catalog.tabField("special", "main_page.title"), "special nested main_page title");
            assertEquals("BUY SEASON PASS", catalog.tabField("special", "main_page.primaryAction.text"), "special nested primaryAction text");
        }));

        out.add(test("offerField splits at the terminal field, keeping compound ids intact, plus the real flat amount template", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("X-DAY SET", catalog.offerField("items.dayx_pack", "titleLabel"), "items.dayx_pack titleLabel");
            assertEquals("SPECIAL", catalog.offerField("items.dayx_pack", "typeLabel"), "items.dayx_pack typeLabel");
            assertEquals("BACK TO THE INSIDE OUT", catalog.offerField("bundles.customization_spring25", "titleLabel"), "bundles.customization_spring25 titleLabel");
            assertEquals("PERSONALITY", catalog.offerField("personality", "name"), "personality name");
            assertEquals("", catalog.offerField("empty_type", "name"), "empty_type name is a real blank value, not null");
            assertEquals("{amount} UNITS", catalog.offersAmountTemplate(), "flat amount template");
        }));

        out.add(test("customTagField resolves real tag text and the real desc_global/desc_ru regional discount variance", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("NEW", catalog.customTagText("new"), "new tag text");
            String global = catalog.customTagField("bundle_dmz26_discount", "desc_global");
            String ru = catalog.customTagField("bundle_dmz26_discount", "desc_ru");
            assertTrue(global.contains("36% discount"), "global variant real percentage");
            assertTrue(ru.contains("40% discount"), "ru variant real different percentage");
            assertTrue(!global.equals(ru), "regional variants genuinely differ");
        }));

        out.add(test("dlcField resolves the real pre-2024 Battle Pass timeline and the refund mailer", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("Battle Pass", catalog.dlcTitle("autumun_2023_bp"), "autumun_2023_bp title");
            assertEquals("Dead Man's Marathon", catalog.dlcField("autumun_2023_bp", "subtitle"), "autumun_2023_bp subtitle");
            assertEquals("Compensation", catalog.dlcField("refund", "mainTitle"), "refund mainTitle");
        }));

        out.add(test("categoryName resolves compound sub-category ids and the real flat category.bundles exception", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("Weapons", catalog.categoryName("weapon"), "weapon category");
            assertEquals("Melee", catalog.categoryName("weapon.sub.melee"), "weapon.sub.melee category");
            assertEquals("Season Pack", catalog.categoryName("bundles"), "real flat category.bundles exception");
        }));

        out.add(test("categoriesLabel resolves the real Reichstag/Sea-of-Exclusion cross-references", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("ASSAULT ON THE REICHSTAG", catalog.categoriesLabel("mt"), "mt category (Reichstag tie)");
            assertEquals("SEA OF EXCLUSION", catalog.categoriesLabel("mk"), "mk category (Sea of Exclusion tie)");
        }));

        out.add(test("seasonName resolves the real seasonal timeline including the newly-found spring26/autumn26 names", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("ZIVCAS CORPORATION", catalog.seasonName("summer24"), "summer24 season name");
            assertEquals("TULPA!", catalog.seasonName("autumn26"), "autumn26 season name");
        }));

        out.add(test("remaining flat/simple accessors resolve their real single-field namespaces", () -> {
            ShopCatalog catalog = loadFixture();
            assertTrue(catalog.displayedCurrencyField("stalbucks", "desc").contains("recycling"), "stalbucks desc");
            assertTrue(catalog.displayedCurrencyField("stalcoins", "desc").contains("replenishing"), "stalcoins desc");
            assertEquals("ARMOR STYLES", catalog.deepPageFilterTitle("armor_style"), "deepPage filter title");
            assertEquals("FPS", catalog.topupMethodName("sbp"), "topup sbp");
            assertEquals("Steam", catalog.topupMethodName("steam"), "topup steam");
            assertEquals("The equipment is not included in the offer.", catalog.guiOfferPreviewText("equipment_offer"), "gui offer preview text");
            assertEquals("COLLECTION 2025", catalog.yearName("25"), "year 25");
        }));

        out.add(test("promoPageField is a data-driven catch-all for standalone promo/bundle ids not named above", () -> {
            ShopCatalog catalog = loadFixture();
            assertEquals("Arena of Slavs", catalog.promoPageField("bundle_ravent23", "title"), "bundle_ravent23 title");
            assertEquals("10 Cases", catalog.promoPageField("bundle_newyear24", "banner_benefits.gachapons.title"), "bundle_newyear24 nested field");
            assertEquals("Special pack", catalog.promoPageField("dayx_pack", "short_title"), "dayx_pack short_title");
            assertTrue(catalog.promoPageIds().contains("bundle_ravent23"), "promoPageIds contains bundle_ravent23");
        }));

        out.add(test("unknown ids/fields resolve to null rather than throwing", () -> {
            ShopCatalog catalog = loadFixture();
            assertNull(catalog.tabTitle("not_a_real_tab"), "unknown tab");
            assertNull(catalog.offerField("items.dayx_pack", "not_a_real_field"), "unknown offer field");
            assertNull(catalog.promoPageField("not_a_real_promo", "title"), "unknown promo id");
        }));

        return out;
    }
}
