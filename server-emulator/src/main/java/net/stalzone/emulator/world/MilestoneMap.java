package net.stalzone.emulator.world;

/**
 * The confirmed playable-map roster and the pick for milestone 1 ("a single static zone,
 * walk-around only" -- docs/PROJECT_PLAN.md).
 *
 * UPDATED 2026-08-31 (docs/RESEARCH_LOG.md part 15): this used to be a documentation
 * placeholder with no real geometry behind it. That's no longer true. The world/voxel
 * format has been identified (.mdat region-cache files, NOT the .pda minimap tiles this
 * comment used to point at -- those are flat 2D images) via the public, actively
 * maintained onejeuu/sc-file project (https://github.com/onejeuu/sc-file), and real
 * cached data for MILESTONE_1_MAP has been decoded and verified: 4 region files
 * (reg.-10.-10 / reg.-10.-11 / reg.-9.-10 / reg.-9.-11), 790 chunks total, converted to
 * standard Minecraft Anvil .mca and independently spot-checked (block-ID histograms
 * look like real terrain: mostly stone/air with a grass surface layer and scattered
 * sand/gravel/clay -- not noise). There is also no such thing as ".mcmtl" -- that was
 * an unconfirmed guess in an earlier version of this file; real .mcsb mesh files simply
 * embed their material as a name string, there's no separate material file on disk
 * (confirmed by listing a real weapon model's folder: only .mcsb + several _diff/_nrm/
 * _spek .ol texture files, no .mcmtl anywhere).
 *
 * This file's list below is also corrected/expanded: the previous ALL_KNOWN_MAPS list
 * was assembled from modassets/assets/pda/ (minimap tile folder names) and missed most
 * of the actual world. The list now below comes from map_cache/5.0/'s subfolder names --
 * each one has real cached .mdat region data on this account's install, i.e. each one is
 * a real, visited, loadable zone (not just an asset reference).
 *
 * UPDATED 2026-09-01 (docs/RESEARCH_LOG.md part 26): re-checked map_cache/5.0/ against
 * three newly-produced client.log sessions (client-11/12/13.log, none analyzed as of
 * part 25) and found the real roster had grown from 30 to 46 zones -- 16 new ones, every
 * one independently confirmed to carry real .mdat region data (2-9 files each, not empty
 * placeholder folders): armsklad, dungeon_DMZ1_barons_safehouse, dungeon_DMZ1_base_merc,
 * dungeon_DMZ1_jivchik_fight, dungeon_chimera, dungeon_koshmar, dungeon_perestrelka,
 * dungeon_polygon7_0, dungeon_school, dungeon_spusk, dungeon_tonnelradar, mg, nbolota,
 * outpost_merc, pd, td. The three "dungeon_DMZ1_*" zones are the first concrete, named
 * evidence for the "dmz" extraction-style mode this project only had an unconfirmed HUD
 * atlas flag for since part 11 (config/options.json's "dmz" key) -- "DMZ1" reads as a
 * distinct game-mode namespace (a specific extraction map set), not a location name.
 * "nbolota" is a second real, separately-cached Bolota-zone variant (parallel to how
 * dungeon_bolota_tutorial_2 already existed alongside dungeon_bolota_tutorial) -- NOT
 * the same thing as the "map-new_bar" PDA-texture 404s described below, which never got
 * a resolution before and now has one: the client's own "pda:map-new_bar/r.X.Z.ol"
 * texture-load attempts (repeatedly 404ing across all three new sessions) are a stale
 * internal PDA-minimap codename for the exact same zone map_cache calls "bar" -- this
 * file's own MILESTONE_1_FALLBACK comment already recorded that history below. No
 * map_cache folder named "new_bar" exists; it is a real, disclosed client-side naming
 * mismatch (minimap-texture code vs. world-cache code disagree on this one zone's
 * internal name), not a hidden/cut zone. Short two/three-letter names "mg"/"pd"/"td"
 * could not be tied to a human-readable name from the three new session logs (their
 * zone folder names never appear verbatim in client-log text -- unlike "armsklad",
 * which does, once, as a game-object collider name "armsklad_node") -- left as
 * confirmed-real-but-unidentified rather than guessed at.
 *
 * UPDATED 2026-09-03 (docs/RESEARCH_LOG.md part 53): re-checked map_cache/5.0/ against
 * ten newly-produced client-log sessions (client-14 through client-23, spanning
 * 2026-09-02/03, none analyzed as of part 52) and found the real roster had grown again,
 * 46 -> 67 zones -- 21 new ones, every one independently confirmed to carry real .mdat
 * region data (2-9 files each, not empty placeholder folders): bar_sever,
 * battlefield_kolizei_open, battlefield_sovetpunk_gungame, dungeon_base_sanitar_angar,
 * dungeon_base_sanitar_fight, dungeon_base_sanitar_outside, dungeon_base_sanitar_staff,
 * dungeon_marin, dungeon_polygon_7_pve, dungeon_radar, dungeon_radar_city, dungeon_stels,
 * overpass, signals_dungeon_chimera, signals_dungeon_dosug, signals_dungeon_final_boss,
 * signals_dungeon_merc_02, signals_dungeon_polygon_anomaly, signals_dungeon_polygon_motyga,
 * signals_dungeon_sklad_frimena, signals_dungeon_upir. "bar_sever" directly resolves part
 * 28's previously-unplaced settlement-roster name of the same spelling to a real,
 * independently-cached zone. The 4 "dungeon_base_sanitar_*" zones plus a widespread real
 * "sanitar" armor-skin variant found across at least 8 unrelated armor bases (bover, kz3b,
 * mule, saturn, skat10, smog_old, voshod_old, and the newly-found "zarya" set below) are
 * disclosed as plausibly, but not confirmed, the same in-game faction EXBO's own public
 * wiki calls "Orderlies" (part 36) -- "sanitar" is Russian for "orderly", a real semantic
 * match, but no STALZONE file or log line directly equates the two names, so this stays a
 * hedged reading, not a fact. The 8 "signals_dungeon_*" zones (mirroring existing
 * chimera/sklad_frimena/upir dungeon names under a new "signals_" prefix, plus a new
 * "final_boss"-named one) line up with a real "signal" barter-item category found the
 * same part -- a plausible, not confirmed, shared "signal hunt" activity variant of
 * existing dungeons. mg/pd/td remain unidentified by
 * full name, but gained real independent corroboration this part: a real, on-disk
 * per-zone barter-item icon set (customitems/textures/items/barter/<zone>.<category>.ol)
 * places mg/pd/td in the exact same reference class as already-confirmed real zones
 * (armsklad, bar, bolota, kordon, les, svalka), plus three zone-shaped names new to this
 * project entirely from that same icon set, "limansk", "sever", and "blizzard" (none of the
 * three is a map_cache folder name, so their own playable-zone status, if any, is
 * unconfirmed -- see
 * docs/RESEARCH_LOG.md part 53 for the full barter-icon evidence and the real "zarya"
 * armor-model discovery from the same sweep). Zero zones were removed since part 26.
 *
 * UPDATED 2026-09-11 (docs/RESEARCH_LOG.md part 105): mg/pd/td are FINALLY resolved by full
 * real name, via a completely different real source than this file's own part-26/53 sweeps --
 * {@code stalker/lang/en.lang}'s {@code location.<id>.name} key family (see the new {@link
 * LocationNameRegistry}, this part): {@code mg}="The Dead City", {@code pd}="The Path of Fools"
 * (independently reinforcing part 96's own separate resolution), {@code td}="Black Willows".
 * This also resolves {@code dungeon_spusk} (already on the 67-zone roster above since part 26)
 * to "Vagant's Den", reinforcing part 104's {@code vagant/} boss finding with a real named lair.
 */
public final class MilestoneMap {

    private MilestoneMap() {}

    /**
     * Real zone folder names confirmed via map_cache/5.0/ (has actual cached .mdat
     * region files, not just referenced in assets). Names match classic S.T.A.L.K.E.R.
     * zone naming (agroprom, bar, kordon, svalka=garbage, yantar=amber, bolota=swamp,
     * les=forest) -- STALZONE clearly reuses/reimagines the original game's map roster
     * rather than being fully original. 67 entries as of part 53 (was 46 as of part 26,
     * 30 as of part 15); order matches the real filesystem's own (case-sensitive) sort
     * order, not manually re-sorted.
     */
    public static final String[] ALL_KNOWN_MAPS = {
            "agroprom",
            "armsklad",
            "bar",
            "bar_save",
            "bar_save_residential_hangar",
            "bar_sever",
            "base_merc",
            "battlefield_kolizei_open",
            "battlefield_sovetpunk_gungame",
            "boat_station",
            "bolota",
            "dungeon_DMZ1_barons_safehouse",
            "dungeon_DMZ1_base_merc",
            "dungeon_DMZ1_jivchik_fight",
            "dungeon_base_sanitar_angar",
            "dungeon_base_sanitar_fight",
            "dungeon_base_sanitar_outside",
            "dungeon_base_sanitar_staff",
            "dungeon_bolota_tutorial",
            "dungeon_bolota_tutorial_2",
            "dungeon_chimera",
            "dungeon_domiki",
            "dungeon_koshmar",
            "dungeon_lab",
            "dungeon_marin",
            "dungeon_mexdvor_bandits",
            "dungeon_perestrelka",
            "dungeon_polygon7_0",
            "dungeon_polygon_7_pve",
            "dungeon_psisobaka",
            "dungeon_radar",
            "dungeon_radar_city",
            "dungeon_sanitar_kordon",
            "dungeon_school",
            "dungeon_sklad_frimena",
            "dungeon_spusk",
            "dungeon_stels",
            "dungeon_svetliy_bandits",
            "dungeon_tonnelradar",
            "dungeon_toxic_dogout",
            "dungeon_travnik",
            "dungeon_upir",
            "dungeon_zemlcap1",
            "foton2",
            "hideout",
            "kordon",
            "les",
            "mg",
            "nbolota",
            "north_mines",
            "outpost_merc",
            "overpass",
            "pd",
            "poligon2",
            "savezone_agroprom",
            "signals_dungeon_chimera",
            "signals_dungeon_dosug",
            "signals_dungeon_final_boss",
            "signals_dungeon_merc_02",
            "signals_dungeon_polygon_anomaly",
            "signals_dungeon_polygon_motyga",
            "signals_dungeon_sklad_frimena",
            "signals_dungeon_upir",
            "svalka",
            "td",
            "yantar",
            "zemlyanka_shaman",
    };

    /**
     * Milestone-1 pick: dungeon_bolota_tutorial.
     *
     * Rationale (unchanged from the original pick, now validated): it's the client's own
     * new-player tutorial zone -- small, linear/contained, light on systems. Now backed
     * by real decoded data: 4 region files, 790 chunks, real terrain confirmed by direct
     * block-ID inspection (see docs/RESEARCH_LOG.md part 15 for the full histogram).
     * "dungeon_bolota_tutorial_2" also exists in the real roster and may be a sequel/
     * second-stage tutorial zone -- not investigated yet. "nbolota" (part 26) is a
     * separate, third Bolota-family zone, also not investigated yet.
     *
     * Fallback: "bar", a classic small STALKER hub zone (previously called "new_bar" in
     * this file before the roster was corrected against real map_cache folder names --
     * and, as of part 26, now confirmed to be the SAME stale name the client's own PDA
     * minimap-texture loader still uses internally for this zone, causing real, harmless,
     * repeating FileNotFoundException warnings in every session's log).
     */
    public static final String MILESTONE_1_MAP = "dungeon_bolota_tutorial";
    public static final String MILESTONE_1_FALLBACK = "bar";
}
