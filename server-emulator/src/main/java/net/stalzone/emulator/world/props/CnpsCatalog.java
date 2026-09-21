package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Real NPC-faction/reputation/dialogue data from {@code stalker/lang/en.lang}'s {@code cnps}
 * namespace (314 lines; part 90's {@link LangCatalog} counted it under its "top 15 by count" list
 * but never investigated it -- this class is that investigation, part 107). "cnps" is not expanded
 * anywhere in this project's evidence; based on its content (faction/relation/dialogue data) it is
 * plausibly a Russian abbreviation for an NPC-relationship system, not confirmed.
 *
 * <p><b>{@code cnps.faction.<id>.name}</b> (real, ~60 ids) is this project's richest real
 * faction/NPC-group display-name crosswalk to date, and directly RESOLVES several long-open leads
 * from a brand-new angle: {@code zavet}="Covenant" (part 68's {@code CustomNpcsCatalog} disclosed
 * "zavet" ("Завет"="Covenant/Testament") as a "fresh faction-name lead" -- CONFIRMED here directly);
 * {@code dolg}="Frontier" (directly confirms part 91's inferred "the Frontier" nickname for the
 * {@code duty} alliance, this time via a literal faction-id crosswalk rather than a leader quote);
 * {@code svoboda}="Rise" (a further independent corroboration of the {@code freedom}/"Rise"
 * crosswalk, "svoboda" being Russian for "freedom"); {@code monolit}="Murmur" (a THIRD independent
 * corroboration of part 99's confirmed monolit/Murmur identity, after `cnps.faction.monolit.name`
 * itself was part 99's own original evidence -- here re-confirmed as part of the complete sweep);
 * {@code ssop}="SSPP" (ties directly to the confirmed Pripyat-battlefield SSPP thread). A genuinely
 * new real faction not seen anywhere else in this project: {@code odinochki}="Loner" (a real,
 * classic S.T.A.L.K.E.R.-series faction name). Also real: {@code namniki}="Mercenary", {@code
 * voennye}/{@code voennye_ukr}/{@code voennye_rus}/{@code voennye_nato}="Military Member"/
 * "Ukrainian/Russian/NATO Military Member", {@code uchenye}="Scientist", {@code sanitary}=
 * "Orderly", {@code neitraly}/{@code frendli}="Neutral", {@code renegaty}="Renegade", {@code
 * zombirovannye}="Zombie", {@code moby}="Mutant", {@code morozoborcy}="Frost Fighter" (winter-event
 * tie), {@code black_market}="Black Market".
 *
 * <p><b>{@code cnps.faction.<settlementId>.relation.<level>}</b> is a real, previously-uncatalogued
 * per-settlement REPUTATION-RANK ladder, directly extending {@code world/SettlementRegistry}'s
 * confirmed 15-settlement roster with a real rank-title system: numbered ladders for {@code
 * settlement_stalkers_csky} (0-4: Unknown/Picklock/Swamp Stalker/Ensign/Vodyanoy), {@code
 * settlement_bandits_lodochnaya} (0-4: Vagrant/Mildew/Leech/Catfish/Fisherman), {@code
 * settlement_stalkers_garage} (0-4: Newbie/Venturer/The Rookie Hunter/One Of Us/Border Guard),
 * {@code settlement_bandits_foton2} (0-4: Drifter/Lackey/Hard Worker/Master/Brigadier), {@code
 * settlement_sever} (0-6: Doser/Daredevil/Nuclear Adventurer/Radiant/Survivor/Liquidator/Legend of
 * the Zone), {@code settlement_merc} (0-6: Greenhorn/Craftsman/Specialist/Killer/Cleaner/
 * Executioner/Professional), {@code settlement_duty} (0-6: Recruit/Soldier/Master Sergeant/Junior
 * Lieutenant/Captain/Major/General), {@code settlement_freedom} (0-6: Liberated/Young Rebel/
 * Venturer/Anarchist/Zone Junkie/Revolutionary/Zone Guardian), {@code settlement_covenant} (0-6:
 * Acolyte/Word Bearer/Bell-Ringer/Hieromonk/Baptist/Bishop/Apostle -- a real ecclesiastical rank
 * ladder matching Covenant's established religious theming). Two settlements use real quoted
 * BARTENDER DIALOGUE LINES instead of rank nouns: {@code settlement_rostok} (0-6, e.g. level 0
 * ="\"We don't serve your kind\"", level 6="\"On the house!\"") and {@code settlement_wnorth}
 * (0-10, e.g. level 0="\"We don't serve your kind\"" through level 10="\"Legend of the Zone\"") --
 * this project's first evidence that some settlements express reputation through a barkeep's
 * changing attitude rather than a formal rank title.
 *
 * <p><b>{@code cnps.dialog.option.icon.tooltip.<id>.title}/{@code .desc}</b> (44 lines) confirms
 * real dialogue-UI icon tooltips: faction-flavored response options (BANDIT/STALKER/FACTION MEMBER,
 * each with a "_ex" FACTION DESERTER/FORMER variant explicitly tied to the {@code duty}/{@code
 * freedom}/{@code merc}/{@code covenant} alliance roster), a real "PLOT TWIST" branching-dialogue
 * marker ("Can affect the plot and the reward"), and real vendor-menu-opener icons corroborating
 * this project's confirmed NPC vendor roles (auctioneer, postman, trader, techtrader,
 * trader_signal, researcher, provisiontrader, contract_supplier, rumours) plus a real "SPECIAL
 * PERSONALITY" roleplay-dialogue marker.
 *
 * <p><b>{@code cnps.entity.<id>.name}</b> (8 lines) names real individual entities distinct from
 * the faction/group rosters above: {@code vagant}="Vagant" (a further, independent corroboration of
 * part 104's confirmed named boss), and 6 real entities not previously catalogued anywhere in this
 * project -- {@code psidog}="Beast", {@code beelzebub}="Beelzebub" (a real named boss/entity), {@code
 * gerrsvin}="Herr Swine" (a real German-themed boar-boss name, "Herr" being German for "Mister"),
 * {@code 415}="415" (a real numeric entity codename, unexplained), {@code kashtanka}="Dumbo" (real,
 * "Kashtanka" itself is a well-known real dog name from Chekhov's short story of the same name;
 * disclosed as a plausible literary echo, not confirmed), {@code apocalypse_krovosos}="Drover" (a
 * real named display variant of the confirmed "krovosos"/"Ghoul" mutant), {@code saltzombie}=
 * "Marionette" (a real named zombie variant).
 *
 * <p><b>{@code cnps.reward.mail.<id>}</b> (2 lines) confirms two real reward-mail templates tying
 * directly into the relation-level system above: {@code settlement}="Faction Reward... Your
 * relationship with the faction has improved to the {level} level..." and {@code arcade}="Session
 * Modes Reward... For reaching level {level} in session modes...".
 *
 * <p><b>{@code cnps.availability.desc.*}</b> (134 lines) is a real per-trader, per-item barter
 * unlock-condition namespace (e.g. {@code cnps.availability.desc.barter.trader_csky.
 * bolota_safezone_4.att.forend_sks=Complete Trade Affairs}, gating a real weapon attachment behind
 * both a reputation-rank threshold -- cross-referencing the {@code settlement_*.relation.*} ladders
 * above, e.g. "Ataman's HQ—Picklock (2)" -- and a real named quest). This class deliberately does
 * NOT model the full per-trader/per-item path structure (adjacent to progression/economy tuning,
 * rule 5's territory); instead {@link #realQuestNamesReferenced()} extracts just the real,
 * previously-uncatalogued quest NAMES this namespace discloses as plain text: "Bandit Affairs",
 * "Being a Hunter", "Boar Affairs", "Chop", "Military Needs", "Souls of the Dead", "The Favor",
 * "The Rat Pit", "Trade Affairs", "Working for Scientists" -- 10 real quest titles never seen
 * anywhere else in this project.
 *
 * <p>Live-verified directly against the real client's {@code en.lang} on 2026-09-11.
 */
public final class CnpsCatalog {

    private static final Pattern QUEST_NAME_PATTERN = Pattern.compile("Complete ([A-Za-z' ]+)");

    private final Map<String, String> factionNameById;
    private final Map<String, String> defaultRelationByKey;
    private final Map<String, String> factionRelationByKey;
    private final Map<String, String> dialogTitleById;
    private final Map<String, String> dialogDescriptionById;
    private final Map<String, String> entityNameById;
    private final Map<String, String> rewardMailById;
    private final List<String> availabilityDescriptions;

    private CnpsCatalog(Map<String, String> factionNameById,
                         Map<String, String> defaultRelationByKey,
                         Map<String, String> factionRelationByKey,
                         Map<String, String> dialogTitleById,
                         Map<String, String> dialogDescriptionById,
                         Map<String, String> entityNameById,
                         Map<String, String> rewardMailById,
                         List<String> availabilityDescriptions) {
        this.factionNameById = factionNameById;
        this.defaultRelationByKey = defaultRelationByKey;
        this.factionRelationByKey = factionRelationByKey;
        this.dialogTitleById = dialogTitleById;
        this.dialogDescriptionById = dialogDescriptionById;
        this.entityNameById = entityNameById;
        this.rewardMailById = rewardMailById;
        this.availabilityDescriptions = availabilityDescriptions;
    }

    public static CnpsCatalog load(LangCatalog catalog) {
        Map<String, String> factionNameById = new LinkedHashMap<>();
        Map<String, String> defaultRelationByKey = new LinkedHashMap<>();
        Map<String, String> factionRelationByKey = new LinkedHashMap<>();
        Map<String, String> dialogTitleById = new LinkedHashMap<>();
        Map<String, String> dialogDescriptionById = new LinkedHashMap<>();
        Map<String, String> entityNameById = new LinkedHashMap<>();
        Map<String, String> rewardMailById = new LinkedHashMap<>();
        List<String> availabilityDescriptions = new ArrayList<>();

        for (String key : catalog.keysWithPrefix("cnps.faction.")) {
            String rest = key.substring("cnps.faction.".length());
            String[] parts = rest.split("\\.");
            if (parts.length == 3 && parts[0].equals("default") && parts[1].equals("relation")) {
                defaultRelationByKey.put(parts[2], catalog.get(key));
            } else if (parts.length == 2 && parts[1].equals("name")) {
                factionNameById.put(parts[0], catalog.get(key));
            } else if (parts.length == 3 && parts[1].equals("relation")) {
                factionRelationByKey.put(parts[0] + "/" + parts[2], catalog.get(key));
            }
        }

        String dialogPrefix = "cnps.dialog.option.icon.tooltip.";
        for (String key : catalog.keysWithPrefix(dialogPrefix)) {
            String rest = key.substring(dialogPrefix.length());
            if (rest.endsWith(".title")) {
                dialogTitleById.put(rest.substring(0, rest.length() - ".title".length()), catalog.get(key));
            } else if (rest.endsWith(".desc")) {
                dialogDescriptionById.put(rest.substring(0, rest.length() - ".desc".length()), catalog.get(key));
            }
        }

        String entityPrefix = "cnps.entity.";
        for (String key : catalog.keysWithPrefix(entityPrefix)) {
            String rest = key.substring(entityPrefix.length());
            if (rest.endsWith(".name")) {
                entityNameById.put(rest.substring(0, rest.length() - ".name".length()), catalog.get(key));
            }
        }

        String rewardPrefix = "cnps.reward.mail.";
        for (String key : catalog.keysWithPrefix(rewardPrefix)) {
            rewardMailById.put(key.substring(rewardPrefix.length()), catalog.get(key));
        }

        for (String key : catalog.keysWithPrefix("cnps.availability.")) {
            availabilityDescriptions.add(catalog.get(key));
        }

        return new CnpsCatalog(factionNameById, defaultRelationByKey, factionRelationByKey,
                dialogTitleById, dialogDescriptionById, entityNameById, rewardMailById,
                availabilityDescriptions);
    }

    public String factionName(String factionId) {
        return factionNameById.get(factionId);
    }

    public String defaultRelation(String key) {
        return defaultRelationByKey.get(key);
    }

    /** {@code settlementFactionId} e.g. {@code "settlement_covenant"}; {@code level} e.g. {@code "3"}. */
    public String factionRelationTitle(String settlementFactionId, String level) {
        return factionRelationByKey.get(settlementFactionId + "/" + level);
    }

    public String dialogTooltipTitle(String id) {
        return dialogTitleById.get(id);
    }

    public String dialogTooltipDescription(String id) {
        return dialogDescriptionById.get(id);
    }

    public String entityName(String id) {
        return entityNameById.get(id);
    }

    public String rewardMail(String id) {
        return rewardMailById.get(id);
    }

    public int availabilityDescriptionCount() {
        return availabilityDescriptions.size();
    }

    /**
     * Real quest names disclosed as plain "Complete &lt;name&gt;" text inside {@code
     * cnps.availability.desc.*} lines, deduplicated and alphabetically sorted. Does not attempt to
     * resolve which trader/item each quest gates -- see this class's own doc.
     */
    public List<String> realQuestNamesReferenced() {
        Set<String> names = new LinkedHashSet<>();
        for (String desc : availabilityDescriptions) {
            Matcher m = QUEST_NAME_PATTERN.matcher(desc);
            while (m.find()) {
                names.add(m.group(1).trim());
            }
        }
        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);
        return sorted;
    }

    public List<String> factionIds() {
        List<String> ids = new ArrayList<>(factionNameById.keySet());
        Collections.sort(ids);
        return ids;
    }
}
