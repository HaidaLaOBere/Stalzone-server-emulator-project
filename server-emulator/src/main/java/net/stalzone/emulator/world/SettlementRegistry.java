package net.stalzone.emulator.world;

/**
 * The real "settlement" roster -- a fixed list of 15 named strings the client logs reading
 * at {@code Game Startup}, one line each: {@code Reading settlement <name>} (source:
 * {@code client-13.log}, a session that started 2026-09-01, first log this project has seen
 * that shows this; not present in the client-log sweep used for part 26, i.e. genuinely new
 * evidence, not a re-read of something already catalogued).
 *
 * <p>ADDED 2026-09-01 (docs/RESEARCH_LOG.md part 28). This resolves part 27's open question
 * about {@code lodochnaya} and {@code rostok} (two trader-id location fragments from
 * {@code config/vendor_archive.dat} that did not match any {@link MilestoneMap#ALL_KNOWN_MAPS}
 * entry): both are real settlement names, confirmed here verbatim. So is {@code foton2}
 * (already a known map_cache zone) and {@code bar_save_residential_hangar} (also already a
 * known map_cache zone) -- those two overlap with {@link MilestoneMap#ALL_KNOWN_MAPS}
 * exactly. The other 13 settlement names do NOT match any of the 46 {@code ALL_KNOWN_MAPS}
 * entries verbatim, confirming "settlement" is a real, distinct namespace from map_cache's
 * zone-folder roster, not just an alias list for the same 46 zones.
 *
 * <p><b>What "settlement" means here is not confirmed</b> -- this project has only ever seen
 * the bare 15-name list logged once per client startup, with no further per-settlement
 * detail (no owning faction, no territory bounds, no coordinates) found anywhere in the
 * mounted install's {@code config/} tree or client-log text. Two names strongly read as
 * faction/territory concepts rather than places: {@code duty} and {@code freedom} are
 * canonical playable factions in the original S.T.A.L.K.E.R. trilogy, and {@code csky}
 * plausibly abbreviates the third canonical faction, "Clear Sky" -- this is an informed
 * reading based on the original games' well-known faction names, not something confirmed
 * from STALZONE's own client code or text. {@code covenant}, {@code north_factions_block},
 * and {@code battlefields} do not match any original-trilogy faction or location name this
 * project knows of and may be STALZONE-original; also not confirmed. {@code savezone_td}
 * and {@code savezone_agro} follow the same {@code savezone_*} naming pattern as
 * {@code map_cache}'s own {@code savezone_agroprom} entry (confirming {@code agro} here is
 * shorthand for {@code agroprom}), which is suggestive but not conclusive evidence that the
 * still-unidentified map_cache zone code {@code td} (see {@link MilestoneMap}'s part-26
 * doc) is a real, actively-used zone with its own save-zone -- {@code mg} and {@code pd}
 * have no equivalent {@code savezone_*} settlement entry here and remain completely
 * unidentified.
 *
 * <p>This is a startup log list only -- treat it as a confirmed-real name roster, not a
 * territory-control or faction-ownership model. No behavior is implied or implemented here.
 *
 * <p><b>UPDATED part 105:</b> the real {@code stalker/lang/en.lang} {@code settlement.id.<id>.
 * title} key family (202 total real {@code settlement.*} keys, part 90's {@link
 * net.stalzone.emulator.localization.LangCatalog} already counted the namespace but its own
 * class doc mischaracterized it as "real settlement PERK descriptions... not per-settlement
 * identity/territory data" -- that characterization missed these {@code .title} lines
 * specifically and is corrected here, not by this project's evidence changing) resolves EVERY
 * ONE of the 15 real names above to a real display title, closing this class's own disclosed
 * gap outright. Most resolve via a direct id match; two resolve through a one-step alias this
 * part also confirms directly against {@code en.lang} ({@code savezone_agro} via {@code
 * settlement.id.agro.title}, {@code savezone_td} via {@code settlement.id.td.title} -- both
 * aliases are themselves independently corroborated by the {@code location.agro}/{@code
 * location.td} zone-code entries {@link LocationNameRegistry} resolves the same way). See
 * {@link #realTitle} for the lookup and {@link LocationNameRegistry} for the much larger,
 * separate real {@code location.<id>.name} namespace this same part also found.
 *
 * <p>The headline correction: {@code csky} was previously only an "informed reading" that it
 * "plausibly abbreviates... Clear Sky," the third canonical S.T.A.L.K.E.R.-trilogy faction --
 * explicitly flagged as NOT confirmed. {@code settlement.id.csky.title=Ataman's HQ} instead
 * confirms {@code csky} as "Ataman's HQ," home to "the swamp stalkers and their leader, the
 * Ataman" ({@code settlement.id.csky.level.4.desc}), directly corroborated by two independent
 * {@code en.lang} lines outside the {@code settlement} namespace ({@code
 * region.stalkers_bolota.name=Ataman's HQ}, {@code savepoints.id.fallback_stalkers.name=
 * Ataman's HQ}). The Clear-Sky guess is superseded, not merely supplemented, by this real text.
 *
 * <p>The other 14 real titles, verbatim: {@code lodochnaya}="Boathouse", {@code garage}="Fire
 * Station" (also a real named point inside the {@code battlefield.id.pripyat} "Pripyat Streets"
 * operation, {@code battlefield.id.pripyat.point.garage.name=Garage} -- part 72's {@code
 * pripyat} battlefield-map name gains its first real narrative detail here: {@code
 * battlefield.id.pripyat.description=SSPP is conducting an operation against Murmur}, tying
 * directly into this project's existing confirmed SSPP/Murmur thread), {@code foton2}="Village
 * School" (a concrete real place name for the zone code -- distinct from, and not resolving,
 * part 104's still-unconfirmed {@code foton2}/"Photon Generator" operation lead), {@code
 * rostok}="The Bar" (the real hangout itself; the separate zone code {@code bar} names the
 * surrounding "Pervomaisky Factory" location -- see {@link LocationNameRegistry}), {@code
 * bar_save_residential_hangar}="Dorms", {@code bar_sever}="Drunken Geiger Bar" (independently
 * reconfirming part 93's own finding via an entirely separate {@code en.lang} key; {@code
 * en.lang} also carries a redundant unprefixed alias, {@code settlement.id.sever.title}, same
 * value, not one of this class's own 15 known ids), {@code merc}="Mercenary Camp", {@code
 * duty}="Frontier Base", {@code freedom}="Rise Base", {@code covenant}="Covenant Abode", {@code
 * north_factions_block}="Factions of the North", {@code battlefields}="Session battles" (real
 * flavor text: "For battle-hardened veterans of the Standoff," tying to part 72's clan-war PvP
 * content).
 *
 * <p><b>UPDATED part 107:</b> a real per-settlement REPUTATION-RANK ladder was found in a
 * completely separate {@code en.lang} namespace, {@code cnps.faction.settlement_<id>.relation.
 * <level>} -- see {@link net.stalzone.emulator.world.props.CnpsCatalog}. This gives several of
 * this class's confirmed settlements a real numbered rank-title progression (e.g. {@code csky}:
 * Unknown/Picklock/Swamp Stalker/Ensign/Vodyanoy; {@code covenant}: a real ecclesiastical ladder,
 * Acolyte through Apostle), and reveals that two settlements ({@code rostok}, {@code wnorth})
 * express reputation through real quoted bartender dialogue lines instead of a formal rank noun.
 */
public final class SettlementRegistry {

    private SettlementRegistry() {}

    /**
     * This settlement's real display title from {@code catalog} (part 105), or {@code null} if
     * {@code settlementId} isn't one this class knows a crosswalk for. Looks up {@code
     * settlement.id.<settlementId>.title} directly, except for the two confirmed {@code
     * savezone_*} aliases ({@code savezone_agro} -&gt; real key {@code settlement.id.agro.title},
     * {@code savezone_td} -&gt; real key {@code settlement.id.td.title}) documented in this
     * class's own doc above. Does not fall back to guessing for any id outside {@link
     * #ALL_KNOWN_SETTLEMENTS} or a key {@code catalog} doesn't actually contain.
     */
    public static String realTitle(String settlementId, net.stalzone.emulator.localization.LangCatalog catalog) {
        if (settlementId == null) {
            return null;
        }
        String key = "savezone_agro".equals(settlementId) ? "settlement.id.agro.title"
                : "savezone_td".equals(settlementId) ? "settlement.id.td.title"
                : "settlement.id." + settlementId + ".title";
        return catalog.get(key);
    }

    /**
     * Real settlement names in the exact order the client logs them at startup (source
     * order, not re-sorted). 15 entries, all confirmed verbatim from {@code client-13.log}'s
     * {@code Reading settlement <name>} lines (2026-09-01 session).
     */
    public static final String[] ALL_KNOWN_SETTLEMENTS = {
            "csky",
            "lodochnaya",
            "garage",
            "foton2",
            "savezone_td",
            "savezone_agro",
            "rostok",
            "bar_save_residential_hangar",
            "bar_sever",
            "merc",
            "duty",
            "freedom",
            "covenant",
            "north_factions_block",
            "battlefields",
    };
}
