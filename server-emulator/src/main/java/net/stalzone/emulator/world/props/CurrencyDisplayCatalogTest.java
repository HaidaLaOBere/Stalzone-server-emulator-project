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
 * Regression tests for {@link CurrencyDisplayCatalog}, using a fixture built from real {@code
 * currency.type.id.*} lines drawn directly from {@code stalker/lang/en.lang} (part 109,
 * live-verified on device) -- covering the real Reichstag crate/XP economy (another entry in this
 * project's long Reichstag corroboration chain, deliberately left uncounted -- see the class doc),
 * several year-tagged seasonal-event currencies, and the real per-id
 * name/postfix/tooltip/hint/counter-template crosswalk.
 */
public final class CurrencyDisplayCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "currency.type.id.parts.name=Repair Parts",
            "currency.type.id.parts.postfix= units",
            "currency.type.id.parts.tooltip=Repair Parts",
            "currency.type.id.parts.hint=Parts are an in-game currency that you can earn by completing daily tasks in the game. This currency is used for equipment repair.",
            "currency.type.id.parts.counter.template=Repair Parts received: {valueFormatted}",
            "currency.type.id.reichstag_xp_infantry.name=ASSAULTER CLASS XP",
            "currency.type.id.reichstag_xp_infantry.hint=XP can be earned by participating in the Assault on the Reichstag event using Assaulter class equipment.",
            "currency.type.id.reichstag_xp_scout.name=SCOUT CLASS XP",
            "currency.type.id.reichstag_xp_machinegunner.name=MACHINE GUNNER CLASS XP",
            "currency.type.id.reichstag_xp_engineer.name=ENGINEER CLASS XP",
            "currency.type.id.reichstag_xp_free.name=Free XP",
            "currency.type.id.reichstag_silver.name=Silver",
            "currency.type.id.reichstag_silver.hint=Silver can be used to purchase crate items for a fixed amount, as well as to purchase the crates themselves.",
            "currency.type.id.sc_birthday_19.name=Coins",
            "currency.type.id.pumkins_19.name=Pumpkins",
            "currency.type.id.twitch_shards.name=Twitch Fragments",
            "currency.type.id.twitch_shards.hint=Twitch Fragments are an in-game currency that can be obtained by selling some items from Twitch Drops to a special NPC.",
            "currency.type.id.prestige_dollars.name=Patterns",
            "currency.type.id.prestige_dollars.hint=Patterns are units of informational states from which the Soothsayer assembles Gifts of Unity."
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("currency-display-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static CurrencyDisplayCatalog loadFixture() throws IOException {
        return CurrencyDisplayCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("name/postfix/tooltip/hint/counterTemplate all resolve for a real currency", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            assertEquals("Repair Parts", catalog.name("parts"), "name");
            assertEquals(" units", catalog.postfix("parts"), "postfix");
            assertEquals("Repair Parts", catalog.tooltip("parts"), "tooltip");
            assertTrue(catalog.hint("parts").contains("daily tasks"), "hint");
            assertEquals("Repair Parts received: {valueFormatted}", catalog.counterTemplate("parts"), "counter.template");
        }));

        out.add(test("the real 6-currency Reichstag crate/XP economy resolves", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            assertEquals("ASSAULTER CLASS XP", catalog.name("reichstag_xp_infantry"), "infantry");
            assertEquals("SCOUT CLASS XP", catalog.name("reichstag_xp_scout"), "scout");
            assertEquals("MACHINE GUNNER CLASS XP", catalog.name("reichstag_xp_machinegunner"), "machinegunner");
            assertEquals("ENGINEER CLASS XP", catalog.name("reichstag_xp_engineer"), "engineer");
            assertEquals("Free XP", catalog.name("reichstag_xp_free"), "free");
            assertEquals("Silver", catalog.name("reichstag_silver"), "silver");
            assertTrue(catalog.hint("reichstag_silver").contains("purchase crate items"), "silver hint");
        }));

        out.add(test("real year-tagged seasonal currencies resolve without a hint line", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            assertEquals("Coins", catalog.name("sc_birthday_19"), "sc_birthday_19");
            assertEquals("Pumpkins", catalog.name("pumkins_19"), "pumkins_19");
            assertNull(catalog.hint("sc_birthday_19"), "sc_birthday_19 has no hint line");
        }));

        out.add(test("twitch_shards and prestige_dollars resolve their real flavor hints", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            assertEquals("Twitch Fragments", catalog.name("twitch_shards"), "twitch name");
            assertTrue(catalog.hint("twitch_shards").contains("Twitch Drops"), "twitch hint");
            assertEquals("Patterns", catalog.name("prestige_dollars"), "prestige name");
            assertTrue(catalog.hint("prestige_dollars").contains("Soothsayer"), "prestige hint");
        }));

        out.add(test("currencyIds is sorted and currencyCount matches the real fixture id count", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            List<String> ids = catalog.currencyIds();
            assertEquals(11, catalog.currencyCount(), "currency count");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted");
            }
            assertTrue(ids.contains("reichstag_silver"), "contains reichstag_silver");
        }));

        out.add(test("unknown ids resolve to null across all lookups", () -> {
            CurrencyDisplayCatalog catalog = loadFixture();
            assertNull(catalog.name("not_a_real_currency"), "unknown name");
            assertNull(catalog.postfix("not_a_real_currency"), "unknown postfix");
            assertNull(catalog.counterTemplate("not_a_real_currency"), "unknown counter.template");
        }));

        return out;
    }
}
