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
 */
public final class SettlementRegistry {

    private SettlementRegistry() {}

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
