package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs a real 305-line {@code currency.type.id.<id>.*} namespace in {@code stalker/lang/
 * en.lang} (part 109) -- real UI display metadata (name/postfix/tooltip/hint/counter-message) for
 * 69 real named in-game currencies/tokens, entirely distinct from part 58's file-based {@link
 * CurrencyCatalog} (which catalogs the {@code modassets/assets/currency/} 3D-model/icon assets for
 * only the physical ruble-banknote/token props, a small subset of the 69 real ids named here).
 *
 * <p>This namespace is this project's richest single real historical timeline of the game's
 * live-service seasonal events, spanning real year-tagged ids from {@code sc_birthday_19}/{@code
 * pumkins_19}/{@code newyear_19} (2019) through {@code spring_26}/{@code summer_26}/{@code
 * autumn_26} (2026) -- real, dated evidence of at least 8 years of continuous seasonal live
 * content, corroborating part 106's disclosed "twelve years" self-reported anniversary claim from
 * a completely independent angle (a real currency-id trail, not developer marketing copy). Several
 * of these year-tagged currencies are, by definition, now-retired real event content (e.g. {@code
 * halloween_23}="Respect", {@code blizzard_21}/{@code blizzard_22}="Ice Shards", {@code
 * newyear_19}="Blizzard Shards", {@code pumkins_19}="Pumpkins" through {@code pumkins_22}=
 * "Reflection Fragments") -- real disclosed candidates for "possible cut content" under rule 2,
 * though none are asserted to be actually removed from the live game, only that their originating
 * event has passed.
 *
 * <p>Yet another entry in this project's long-running Reichstag/WWII-event corroboration chain (the
 * exact sequential count is disclosed here as NOT reliably trackable any further -- a check across
 * this project's own files this part found the ordinal already inconsistently claimed in different
 * places, e.g. "NINTH" in {@code BattlefieldCatalog}/{@code LangCatalog} for {@code
 * reich_breakthrough} vs. "TENTH" in the separately-written {@code TextureLoadscreenCatalog} for
 * what reads like the same real id, plus an "ELEVENTH" and a "FOURTEENTH" claimed elsewhere for
 * still other real finds -- so this class deliberately does NOT add its own new ordinal number to
 * an already-diverged count, per rule 1's evidence bar; it is presented as a real, additional,
 * independently-confirmed corroboration, uncounted): {@code reichstag_xp_infantry}, {@code
 * reichstag_xp_scout}, {@code reichstag_xp_machinegunner}, and {@code reichstag_xp_engineer} name a
 * real 6-currency Reichstag crate/XP economy -- "ASSAULTER CLASS XP", "SCOUT CLASS XP", "MACHINE
 * GUNNER CLASS XP", "ENGINEER CLASS XP" respectively (each real hint text: "XP can be earned by
 * participating in the Assault on the Reichstag event using &lt;class&gt; equipment... Used to
 * upgrade this class or can be converted into Free XP to level up any class"), plus {@code
 * reichstag_xp_free}="Free XP" and {@code reichstag_silver}="Silver" ("used to purchase crate
 * items... as well as... the crates themselves"). The genuinely new fact here, on top of what part
 * 58's {@link CurrencyCatalog} already found as bare icon-filename ids ({@code reichstag_silver},
 * {@code reichstag_xp_free} only, with no display text): the Reichstag event has its own real
 * 4-class (Assaulter/Scout/Machine Gunner/Engineer) progression-and-crate economy, not merely a
 * themed PvP map. Four of the six currency ids here ({@code reichstag_xp_infantry}, {@code
 * reichstag_xp_scout}, {@code reichstag_xp_machinegunner}, {@code reichstag_xp_engineer}) were
 * never seen by any prior part at all -- only {@code reichstag_silver}/{@code reichstag_xp_free}
 * were already known, as bare ids with no display text.
 *
 * <p>Other real named currencies of note: {@code dmz_coin} (a generic real DMZ-mode event
 * currency), {@code twitch_shards}="Twitch Fragments" (real evidence of a Twitch-integration
 * Drops-to-currency exchange mechanic), {@code encaged_currency} (a real dark, atmospheric hint --
 * "A dark crimson metal coin jingles in the pocket as a quiet reminder of the ill-fated descent
 * into the forgotten underground complex" -- for a real named "Encaged" event this project had not
 * previously catalogued), {@code arsenal_reputation} ("Supply resources to Arsenal Workshop to earn
 * its reputation" -- a real, previously uncatalogued workshop/reputation sink), {@code
 * prestige_dollars}="Patterns" (real: "units of informational states from which the Soothsayer
 * assembles Gifts of Unity... obtained for completing ranked matches in the Coliseum", tying to
 * part 72's existing Coliseum/Crow-Arena PvP finding and introducing a real, unconfirmed-elsewhere
 * "Soothsayer" NPC/entity name), {@code ssop_help_currency} ("Receipts issued by the SSPP as proof
 * that you have completed a contract" -- a real SSPP-faction quest-currency, further corroborating
 * the existing confirmed SSPP thread).
 *
 * <p>All 305 real lines are accounted for by the parser (5 real suffixes per id: {@code name}
 * [69], {@code postfix} [60], {@code tooltip} [56], {@code hint} [64], {@code counter.template}
 * [56] -- not every id has all five, e.g. several older year-tagged ids have a name but no hint).
 */
public final class CurrencyDisplayCatalog {

    private final Map<String, String> nameById;
    private final Map<String, String> postfixById;
    private final Map<String, String> tooltipById;
    private final Map<String, String> hintById;
    private final Map<String, String> counterTemplateById;

    private CurrencyDisplayCatalog(Map<String, String> nameById,
                                    Map<String, String> postfixById,
                                    Map<String, String> tooltipById,
                                    Map<String, String> hintById,
                                    Map<String, String> counterTemplateById) {
        this.nameById = nameById;
        this.postfixById = postfixById;
        this.tooltipById = tooltipById;
        this.hintById = hintById;
        this.counterTemplateById = counterTemplateById;
    }

    private static final String PREFIX = "currency.type.id.";

    public static CurrencyDisplayCatalog load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        Map<String, String> postfixById = new LinkedHashMap<>();
        Map<String, String> tooltipById = new LinkedHashMap<>();
        Map<String, String> hintById = new LinkedHashMap<>();
        Map<String, String> counterTemplateById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id;
            String suffix;
            if (rest.endsWith(".counter.template")) {
                id = rest.substring(0, rest.length() - ".counter.template".length());
                suffix = "counter.template";
            } else {
                id = rest.substring(0, lastDot);
                suffix = rest.substring(lastDot + 1);
            }

            switch (suffix) {
                case "name":
                    nameById.put(id, catalog.get(key));
                    break;
                case "postfix":
                    postfixById.put(id, catalog.get(key));
                    break;
                case "tooltip":
                    tooltipById.put(id, catalog.get(key));
                    break;
                case "hint":
                    hintById.put(id, catalog.get(key));
                    break;
                case "counter.template":
                    counterTemplateById.put(id, catalog.get(key));
                    break;
                default:
                    break;
            }
        }

        return new CurrencyDisplayCatalog(nameById, postfixById, tooltipById, hintById, counterTemplateById);
    }

    public String name(String currencyId) { return nameById.get(currencyId); }
    public String postfix(String currencyId) { return postfixById.get(currencyId); }
    public String tooltip(String currencyId) { return tooltipById.get(currencyId); }
    public String hint(String currencyId) { return hintById.get(currencyId); }
    public String counterTemplate(String currencyId) { return counterTemplateById.get(currencyId); }

    /** Real currency ids that have at least a {@code .name} line, sorted. */
    public List<String> currencyIds() {
        List<String> ids = new ArrayList<>(nameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int currencyCount() {
        return nameById.size();
    }
}
