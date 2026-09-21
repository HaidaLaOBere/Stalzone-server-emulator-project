package net.stalzone.emulator.localization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads one of the client's real UI-localization files, {@code modassets/assets/stalker/lang/
 * <locale>.lang} (part 90) -- plain {@code <dotted.key>=<value>} lines, no header, one real key
 * space shared across 5 real locale files ({@code en}/{@code es}/{@code fr}/{@code ko}/{@code
 * ru}, checked directly: en/es/fr/ko all carry 28,700/28,700/28,698/28,697 lines, ru trails at
 * 27,892 -- a real, disclosed ~2.8% localization-coverage gap for Russian specifically, not
 * investigated further this part). Only {@code en.lang} is parsed anywhere in this codebase so
 * far; the other 4 are confirmed to exist and share the same key convention (spot-checked, e.g.
 * {@code prestige.ability.tank_barricade.name} resolves to a real Cyrillic value in {@code
 * ru.lang}) but are not wired into anything yet.
 *
 * <p>This is a major, previously-uncatalogued real content source -- the localization-key
 * resolver this project's own {@code recon_findings_cont11.md} "Not yet done" notes flagged as
 * missing since part 11 ("Standard weapons/ammo/meds names likely via {@code
 * item.<category>.<name>.name} localization key client-side -- still not located, strings.dat is
 * encrypted"). {@code en.lang} is plain UTF-8 text (not encrypted), 28,700 real keys, found at
 * {@code stalker/lang/} -- a subfolder of {@code modassets/assets/stalker/} that had otherwise
 * gone completely unswept until now (only 2 of its subfolders, {@code models/backpacks} and
 * {@code models/items}, had ever been catalogued, parts 56-57; {@code stalker/} as a whole holds
 * 18,861 files, and two entirely separate top-level asset folders, {@code stalkerplayer/} [278
 * files, real player customization: heads/bodies/arkit] and {@code stalkersounds/} [805 files,
 * real music/audio], were never found at all -- all of this contradicts part 80's closing claim
 * that "every known tree under {@code modassets/assets/} has now been swept"; that claim was
 * wrong, and is corrected here. The rest of {@code stalker/}, and {@code stalkerplayer/}/{@code
 * stalkersounds/} in full, remain open leads for a future part -- this part scopes to just {@code
 * stalker/lang/} and one concrete wiring win (see {@link net.stalzone.emulator.progression.PrestigeAbility}).
 *
 * <p>Real key namespaces found by prefix (top 15 by count, {@code en.lang}): {@code item} (12,611
 * -- real item id/category-string display names and tooltips, a DIFFERENT id space from {@code
 * itemnames.dat}'s numeric-id keys, e.g. {@code item.plate.steel.description}), {@code go}
 * (2,624), {@code stalker} (2,020), {@code general} (1,143), {@code quest_contract} (1,064 -- a
 * real quest-naming namespace distinct from {@code session/save/PlayerNotification}'s
 * notification-toast titles, not cross-referenced this part), {@code challenges} (1,011), {@code
 * shop} (731), {@code we} (508), {@code gui} (395), {@code quote} (366), {@code location} (345),
 * {@code cnps} (314), {@code currency} (305), {@code pda} (301), {@code loadscreens} (272). Also
 * present: {@code battlefield} (68 -- real display names for {@code
 * world/props/StalkerClansCatalog}'s part-72 map roster, e.g. {@code battlefield.id.reich.name=
 * Assault on the Reichstag}, an EIGHTH independent Reichstag corroboration and its first real
 * human-readable title), {@code clans} (98), {@code prestige} (87 -- see {@link
 * net.stalzone.emulator.progression.PrestigeAbility}), {@code settlement} (202 -- real settlement
 * PERK descriptions, e.g. {@code settlement.id.general.perk.storage}, not per-settlement
 * identity/territory data, so does not resolve {@code world/SettlementRegistry}'s disclosed gap),
 * {@code craft_recipe} (49 -- an unconfirmed crafting-system lead, not investigated),
 * {@code hideout} (250 -- an unconfirmed base-building-system lead, not investigated). None of
 * these further namespaces are parsed into a dedicated class this part; {@link #get}/{@link
 * #keysWithPrefix} expose the raw table for a future part to build on.
 *
 * <p><b>CORRECTED part 105:</b> the {@code settlement} (202) line above previously claimed real
 * settlement PERK descriptions do "not resolve {@code world/SettlementRegistry}'s disclosed
 * gap" -- that was an incomplete characterization, not new evidence: the same 202-entry {@code
 * settlement} namespace this part-90 doc already counted also contains 16 real {@code
 * settlement.id.<id>.title} identity lines (part 105 checked specifically for the {@code
 * .title} suffix pattern, which part 90 did not), which DO resolve that gap in full. See {@link
 * net.stalzone.emulator.world.SettlementRegistry#realTitle} and, for the much larger separate
 * {@code location.<id>.name} namespace (298 of the {@code location} prefix's 345 keys) that
 * resolves several further long-open zone-code leads, {@link
 * net.stalzone.emulator.world.LocationNameRegistry}.
 *
 * <p><b>UPDATED part 106:</b> the two namespaces the part-90 doc above left as "not investigated"
 * are now fully parsed: {@code craft_recipe} (49 -- see {@link
 * net.stalzone.emulator.world.props.CraftRecipeCatalog}, a real crafting-recipe system) and {@code
 * hideout} (250 -- see {@link net.stalzone.emulator.world.props.HideoutCatalog}, a real
 * base-building/crafting-hub system). {@code battlefield} (68) is now also fully parsed ({@link
 * net.stalzone.emulator.world.props.BattlefieldCatalog}), resolving the map-name doc note above
 * into a full 23-map roster plus a NINTH Reichstag corroboration via {@code
 * battlefield.id.reich_breakthrough}'s real named historical Berlin landmark points. {@code
 * quest_contract} (1,064) was investigated this part and found to be entirely templated,
 * procedural bounty-board contract quests (e.g. {@code quest_contract.quest_murder_kordon_1.name=
 * Contract killing}) parameterized only by already-known real zone codes ({@code kordon}, {@code
 * bar}, {@code sever}, {@code north}/{@code wnorth}) and a difficulty tier -- no new named story
 * content, so deliberately NOT built into a dedicated class (consistent with rule 5's "ignore live
 * rebalancing" spirit: this is templated board filler, not authored content). Also newly found
 * this part: a small, differently-shaped real namespace family embedding literal {@code /} path
 * separators in its keys rather than following the plain-prefix convention counted above --
 * {@code <season>/challenges/<event>/<n>}, e.g. {@code spring26/challenges/reichstag/1} -- real
 * seasonal challenge/event text, disclosed and partly cross-referenced in {@link
 * net.stalzone.emulator.world.props.CustomNpcsCatalog}'s and {@link
 * net.stalzone.emulator.tips.AdviceList}'s own part-106 doc updates rather than given a dedicated
 * parser here, since {@link #keysWithPrefix} already exposes them fine by exact prefix match. A
 * separate 5-line {@code bonuscode} namespace (real anniversary-gift mail bodies) is disclosed only
 * in {@code AdviceList}'s doc, not parsed into a class.
 *
 * <p><b>UPDATED part 107:</b> {@code cnps} (314 lines -- from the part-90 "top 15 by count" list
 * above, never previously investigated) is now fully parsed: see {@link
 * net.stalzone.emulator.world.props.CnpsCatalog}, a real NPC-faction/reputation-rank/dialogue-UI
 * system that directly resolves part 68's disclosed {@code zavet} faction-name lead and adds a real
 * per-settlement reputation-rank ladder cross-referencing {@link
 * net.stalzone.emulator.world.SettlementRegistry}.
 *
 * <p><b>UPDATED part 108:</b> {@code clans} (98 lines, listed among the "top 15 by count" above)
 * is now fully parsed: see {@link net.stalzone.emulator.world.props.ClanWarCatalog}, a real
 * clan-war PvP FFA/tournament map roster and open-world base-capture reward system, confirmed
 * distinct from both part 72's file-based {@link net.stalzone.emulator.world.props.
 * StalkerClansCatalog} and part 106's {@link net.stalzone.emulator.world.props.BattlefieldCatalog}
 * (neither of which share this namespace's real map ids).
 *
 * <p><b>UPDATED part 109:</b> {@code currency} (305 lines, listed among the "top 15 by count"
 * above) is now fully parsed: see {@link net.stalzone.emulator.world.props.CurrencyDisplayCatalog},
 * real UI display metadata for 69 real named currencies/tokens, distinct from part 58's file-based
 * {@link net.stalzone.emulator.world.props.CurrencyCatalog}. Includes another entry in this
 * project's long Reichstag corroboration chain (a real 6-currency crate/XP economy, deliberately
 * left uncounted -- see that class's doc for why) and a real multi-year seasonal-event currency
 * timeline (2019-2026).
 *
 * <p><b>UPDATED part 110:</b> {@code loadscreens} (272 lines, listed among the "top 15 by count"
 * above) is now fully parsed: see {@link net.stalzone.emulator.world.props.LoadscreenCatalog}, a
 * real 5-tier location danger/PvP-safety classification plus real per-location feature call-outs,
 * cross-referenced against (and disclosing real naming variances with) part 105's {@link
 * net.stalzone.emulator.world.LocationNameRegistry} and part 106's {@link
 * net.stalzone.emulator.world.props.BattlefieldCatalog}. Also surfaces a genuinely new real 24th
 * battlefield map, {@code battlefield_promzona_open}="Industrial Zone".
 *
 * <p><b>UPDATED part 111:</b> {@code we} (508 lines, listed among the "top 15 by count" above,
 * "World Events") is now fully parsed: see {@link net.stalzone.emulator.world.props.
 * WorldEventCatalog}, a real open-world dynamic-encounter system (roaming faction/mutant camps,
 * rescue distress calls, named boss lairs, a full New Year 2023 seasonal boss roster) modeled as a
 * generic {@code (eventId, field)} table rather than one fixed per-id schema, since real field
 * combinations genuinely differ across event archetypes.
 *
 * <p><b>UPDATED part 112:</b> {@code quote} (366 lines, listed among the "top 15 by count" above)
 * is now fully parsed: see {@link net.stalzone.emulator.tips.DailyExcuseCatalog}, a real 365-day
 * comedic "reward delayed" excuse generator for a previously-uncatalogued named NPC, {@code ziv}.
 *
 * <p><b>UPDATED part 113:</b> {@code gui.operation.how_to_play} (191 lines, listed among the "top
 * 15 by count" above) is now fully parsed: see {@link
 * net.stalzone.emulator.world.props.OperationsCatalog}, the real "Operations" PvE game mode's full
 * tutorial/lore tree, which also resolves the part-104 {@code absorber}/"Photon Generator" lead.
 *
 * <p><b>UPDATED part 114:</b> {@code pda} (301 lines, listed among the "top 15 by count" above) is
 * now fully investigated, though NOT wholly modeled: 295 of its 301 lines ({@code
 * pda.waypoint.we.<eventId>.title}) are a confirmed real duplicate of {@link
 * net.stalzone.emulator.world.props.WorldEventCatalog}'s own {@code we.<eventId>.status_title}
 * values (spot-checked, exact matches), so they are deliberately not re-modeled in a dedicated
 * class. Only the remaining 6 lines, {@code pda.waypoint.home_outpost.<factionId>}, are real
 * previously-uncatalogued content: see {@link
 * net.stalzone.emulator.world.props.HomeOutpostCatalog}, a real per-faction "Headquarters" waypoint
 * roster that is a third independent naming source for the {@code duty}/{@code freedom}/{@code
 * merc}/{@code covenant} hub concept {@link net.stalzone.emulator.world.props.ClanWarCatalog} and
 * {@link net.stalzone.emulator.world.SettlementRegistry} already track.
 *
 * <p><b>UPDATED part 115:</b> {@code shop} (731 lines, listed among the "top 15 by count" above) is
 * now fully parsed: see {@link net.stalzone.emulator.world.props.ShopCatalog}, the in-client
 * item-shop UI's own text -- tab labels, promo/bundle pages, a real named seasonal-event timeline
 * (summer24 through autumn26), currency/payment info, and item-offer display labels.
 *
 * <p><b>UPDATED part 116:</b> {@code challenges} (1,011 lines, listed among the "top 15 by count"
 * above) is now fully parsed: see {@link net.stalzone.emulator.world.props.ChallengesCatalog}, real
 * in-game challenge/achievement task text spanning nearly every confirmed season and live-service
 * event, modeled as a single generic {@code (id, field)} table since every real line fits one shape
 * with zero flat exceptions.
 *
 * <p><b>UPDATED part 117:</b> {@code quest_contract} (1,064 lines, listed among the "top 15 by
 * count" above) is now fully parsed: see {@link
 * net.stalzone.emulator.world.props.QuestContractCatalog}, a real string-id quest-naming namespace
 * (717 ids, up to a {@code name}/{@code title} pair each) plus a real roster of named
 * contract-giver labels.
 *
 * <p><b>Values are stored exactly as they appear on disk, not further interpreted.</b> Many
 * values contain a literal two-character {@code \n} escape sequence (e.g. a tooltip's paragraph
 * break) or {@code {placeholder number}} template tokens (e.g. {@code {cd_time number}}) -- this
 * class does not unescape the former or substitute the latter; a caller that wants either
 * behavior applies it itself, since guessing the client's own exact template-substitution
 * behavior would be exactly the kind of unverified-assumption-as-spec this project's rule 1
 * forbids. Confirmed directly against the real file: keys never repeat with a different value (0
 * conflicting duplicates across 28,700 real {@code en.lang} lines), and every non-blank line
 * contains at least one {@code =} (0 unparseable lines) -- {@link #load} still throws if either
 * ever turns out false against a different locale file, rather than silently accepting it.
 */
public final class LangCatalog {

    private final Map<String, String> valueByKey;

    private LangCatalog(Map<String, String> valueByKey) {
        this.valueByKey = valueByKey;
    }

    /** @param langFile the real {@code .../modassets/assets/stalker/lang/<locale>.lang} file. */
    public static LangCatalog load(Path langFile) throws IOException {
        List<String> lines = Files.readAllLines(langFile, StandardCharsets.UTF_8);
        Map<String, String> valueByKey = new LinkedHashMap<>();

        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0) {
                throw new IOException(langFile + ": unparseable line, no '=' found: \"" + line + "\"");
            }
            String key = line.substring(0, eq);
            String value = line.substring(eq + 1);

            String existing = valueByKey.get(key);
            if (existing != null && !existing.equals(value)) {
                throw new IOException(langFile + ": key \"" + key + "\" re-declared with a different "
                        + "value -- a genuine conflict this file has never shown before");
            }
            valueByKey.put(key, value);
        }

        if (valueByKey.isEmpty()) {
            throw new IOException(langFile + ": no key=value lines found");
        }

        return new LangCatalog(valueByKey);
    }

    public int size() {
        return valueByKey.size();
    }

    /** The raw value for this exact key, or null if it isn't in the table. */
    public String get(String key) {
        return valueByKey.get(key);
    }

    /** Every real key that starts with {@code prefix}, in file order -- e.g. {@code keysWithPrefix("prestige.ability.")}. */
    public List<String> keysWithPrefix(String prefix) {
        List<String> matches = new ArrayList<>();
        for (String key : valueByKey.keySet()) {
            if (key.startsWith(prefix)) {
                matches.add(key);
            }
        }
        return matches;
    }

    /** Every real key whose name contains this substring (case-sensitive), in file order -- a convenience for exploring the table. */
    public List<String> keysContaining(String substring) {
        List<String> matches = new ArrayList<>();
        for (String key : valueByKey.keySet()) {
            if (key.contains(substring)) {
                matches.add(key);
            }
        }
        return matches;
    }
}
