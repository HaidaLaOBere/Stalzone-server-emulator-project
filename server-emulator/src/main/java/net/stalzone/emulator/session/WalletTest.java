package net.stalzone.emulator.session;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link Wallet} (part 82) -- pure Java logic, no real files needed, same
 * standing as this project's other in-memory session-state classes (e.g. {@code InventoryTest}
 * would be, if one existed -- {@link Inventory} itself has never had a dedicated unit test file;
 * its behavior is instead exercised end-to-end via {@code GameLoopDemo}, the same place {@link
 * net.stalzone.emulator.session.GameLoop#tickPurchase} -- which uses this class -- is verified).
 */
public final class WalletTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a fresh wallet has a zero balance for any currency, including ones never touched", () -> {
            Wallet wallet = new Wallet();
            assertEquals(0L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "fresh money balance");
            assertEquals(0L, wallet.balanceOf("some_currency_never_credited"), "fresh unknown-currency balance");
            assertTrue(wallet.currenciesHeld().isEmpty(), "nothing held yet");
        }));

        out.add(test("credit increases the named currency's balance and adds it to currenciesHeld", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 500);
            assertEquals(500L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "balance after one credit");
            assertTrue(wallet.currenciesHeld().contains(Wallet.CURRENCY_MONEY), "now held");
        }));

        out.add(test("repeated credits to the same currency accumulate", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 500);
            wallet.credit(Wallet.CURRENCY_MONEY, 250);
            assertEquals(750L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "balance after two credits");
        }));

        out.add(test("credits to different currencies don't affect each other", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 1000);
            wallet.credit(Wallet.CURRENCY_SEASON_PASS, 3);
            assertEquals(1000L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "money untouched by season-pass credit");
            assertEquals(3L, wallet.balanceOf(Wallet.CURRENCY_SEASON_PASS), "season-pass balance");
            assertEquals(0L, wallet.balanceOf(Wallet.CURRENCY_BIRTHDAY_EVENT), "birthday-event currency never credited");
        }));

        out.add(test("credit rejects a negative amount", () -> {
            Wallet wallet = new Wallet();
            assertThrows(IllegalArgumentException.class, () -> wallet.credit(Wallet.CURRENCY_MONEY, -1),
                    "negative credit amount");
        }));

        out.add(test("debit succeeds and reduces the balance when funds are sufficient", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 1000);
            boolean ok = wallet.debit(Wallet.CURRENCY_MONEY, 400);
            assertTrue(ok, "debit within balance succeeds");
            assertEquals(600L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "balance after debit");
        }));

        out.add(test("debit of exactly the full balance succeeds and leaves zero", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 250);
            boolean ok = wallet.debit(Wallet.CURRENCY_MONEY, 250);
            assertTrue(ok, "debit of the exact balance succeeds");
            assertEquals(0L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "balance now zero");
        }));

        out.add(test("debit fails and leaves the balance unchanged when funds are insufficient", () -> {
            Wallet wallet = new Wallet();
            wallet.credit(Wallet.CURRENCY_MONEY, 100);
            boolean ok = wallet.debit(Wallet.CURRENCY_MONEY, 101);
            assertFalse(ok, "debit beyond balance fails");
            assertEquals(100L, wallet.balanceOf(Wallet.CURRENCY_MONEY), "balance untouched by a failed debit");
        }));

        out.add(test("debit of a currency never credited fails cleanly (0 available)", () -> {
            Wallet wallet = new Wallet();
            boolean ok = wallet.debit(Wallet.CURRENCY_SEASON_PASS, 1);
            assertFalse(ok, "can't debit a currency with nothing in it");
        }));

        out.add(test("debit rejects a negative amount", () -> {
            Wallet wallet = new Wallet();
            assertThrows(IllegalArgumentException.class, () -> wallet.debit(Wallet.CURRENCY_MONEY, -1),
                    "negative debit amount");
        }));

        return out;
    }
}
