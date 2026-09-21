package net.stalzone.emulator.session;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A player's balances across STALZONE's real multi-currency economy. {@link #CURRENCY_MONEY},
 * {@link #CURRENCY_BIRTHDAY_EVENT}, and {@link #CURRENCY_SEASON_PASS} are real, evidenced
 * currency-id strings read directly out of {@code config/vendor_archive.dat} (see
 * {@link net.stalzone.emulator.economy.VendorArchive}'s class doc, part 27) -- the first and
 * only direct evidence in this project that STALZONE tracks more than one currency per account.
 * Three is simply how many distinct ids that one account's real archive happens to contain, not
 * a claim that the real game's currency list is closed to exactly those three.
 *
 * <p>Everything else about this class -- that balances live in a simple per-currency ledger, that
 * a debit either fully succeeds or changes nothing, that a fresh wallet starts at zero for every
 * currency -- is this emulator's own design for holding that state server-side, same standing as
 * {@link Inventory} for carried items: STALZONE's real wire representation for granting/spending
 * currency is still unknown (payloads are encrypted, see docs/RESEARCH_LOG.md part 7), and no
 * starting-balance number for any currency has been found in any file this project has read.
 */
public final class Wallet {

    // Real currency ids (part 27, VendorArchive.currencies()).
    public static final String CURRENCY_MONEY = "money";
    public static final String CURRENCY_BIRTHDAY_EVENT = "sc_birthday_21";
    public static final String CURRENCY_SEASON_PASS = "season_pass_ticket_3";

    private final Map<String, Long> balances = new LinkedHashMap<>();

    /** Zero for any currency never credited yet -- not an error, same as {@link Inventory#countOf} for an uncarried item id. */
    public long balanceOf(String currency) {
        return balances.getOrDefault(currency, 0L);
    }

    public void credit(String currency, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        balances.merge(currency, amount, Long::sum);
    }

    /**
     * Debits {@code amount} of {@code currency} only if the full amount is available; returns
     * whether it happened. Never partially debits -- unlike {@link Inventory#remove}, which
     * removes "up to" a count, a currency amount that can't be paid in full isn't a real
     * transaction at all, so this leaves the balance untouched on failure.
     */
    public boolean debit(String currency, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        long have = balanceOf(currency);
        if (have < amount) {
            return false;
        }
        balances.put(currency, have - amount);
        return true;
    }

    /** Every currency this wallet has ever been credited in, in first-credited order (a currency debited back to exactly 0 is still listed). */
    public Set<String> currenciesHeld() {
        return new LinkedHashSet<>(balances.keySet());
    }
}
