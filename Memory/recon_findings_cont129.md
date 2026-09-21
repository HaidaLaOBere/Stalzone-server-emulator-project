# Recon findings -- cont129 (Part 173)

## The requested item./general./quest_contract. audit -- result: all confirmed complete

Per the user's explicit direction, ran a full second-level audit of the three largest
already-partially-covered `en.lang` namespaces, checking each against the actual covering class's
source (not just a literal grep) and a fresh line-count against the real, full file:

- `item.*` (12,427 lines) -- fully covered by `ItemLangCatalog`. Single generic `(id, field)`
  table, zero real flat exceptions, confirmed by direct count.
- `general.*` (1,121 lines) -- fully covered by `GeneralCatalog`. Reconciled exactly:
  `achievement` (1,116: `id`=1,115 + `category`=1) + `region` (4) + `stats` (1) = 1,121.
- `quest_contract.*` (1,064 lines) -- fully covered by `QuestContractCatalog`. Reconciled exactly:
  `name` (668) + `title` (390) + 6 real flat contract-giver exceptions = 1,064.
- `challenges.*` (987 lines, checked as an adjacent sanity check) -- fully covered by
  `ChallengesCatalog`, same single-generic-table pattern, zero lines lack a field suffix.
- `shop.*` (698), `go.*` (2,613), `stalker.*` (2,020) -- also spot-checked (the latter two already
  fully generic/exhaustively-enumerated switches) and confirmed complete.
- `we.*` and `quote.*` turned out to already be covered OUTSIDE the `world.props` package
  (`WorldEventCatalog` and `tips.DailyExcuseCatalog` respectively) -- a real methodological
  reminder that a package-scoped grep can produce the same kind of false negative a
  literal-prefix-only grep can (already documented for the latter in part 172's own methodology
  note).

No gap was found in any of these. This confirms the earlier top-level census result (part 172):
this project's prior work was unusually thorough, and the "find silently-missing content" search
has diminishing returns in the namespaces already given the generic-single-table treatment.

## `world.props.PdaWaypointEventCatalog` -- the one real gap this audit did find

Extending the same technique to the smaller `pda.*` top-level prefix (299 lines total) turned up a
genuine, substantial gap: `HomeOutpostCatalog` covers only 6 of those 299 lines (`pda.waypoint.
home_outpost.*`). The remaining 293, `pda.waypoint.we.*`, were not a silent miss -- `WorldEventCatalog`'s
own Javadoc had already disclosed the namespace's existence, describing it as "confirmed consistent
where checked, not separately catalogued here." That was an honest disclosure, but the checking
behind it was a spot check, not exhaustive. This part did the exhaustive work.

**Real shape.** `pda.waypoint.we.<eventId>.<field>`, the same `eventId` vocabulary
`WorldEventCatalog` uses for `we.<eventId>.*`. Modeled with the generic last-dot-split `(id, field)`
table: 280 distinct real ids, 291 lines (`title`: 273, `desc`: 7, plus a handful of one-off fields
from compound ids), plus 2 real completely flat exceptions (`shopot_exit`, `shopot_control_center`)
that -- a real, minor, disclosed client quirk -- each ALSO carry their own separate, real,
byte-identical `.title` line alongside the flat one (e.g. both `pda.waypoint.we.shopot_exit`=
"Murmur Post" and `pda.waypoint.we.shopot_exit.title`="Murmur Post" exist side by side). 291 + 2 =
293, confirmed by direct count with zero leftover lines. A handful of real compound ids
(`insideout_centrifuge.status`, `insideout_centrifuge.map_marker.description`) and a real `outpost`
id whose "fields" are actually per-faction outpost labels (`merc`, `freedom`, `duty`, `covenant`,
`neutral`, `stalkers`, `bandits`, `in_progress`) are all handled by the generic split with no
special-casing needed.

**The genuine finding: exhaustive cross-reference against `WorldEventCatalog` surfaced a real
client text bug, not just more content.** Of this namespace's 280 real ids, 191 also carry a real
`we.<id>.status_title` entry; 183 match byte-for-byte, 8 do not, in three genuinely different
categories:

1. **Cosmetic capitalization only** (4 ids: `supply_base_duty`/`_merc`/`_freedom`/`_covenant`):
   "Supply base" (we.*) vs. "Supply Base" (this namespace) -- trivial, preserved as found on both
   sides.
2. **A real, genuine client bug -- two ids' content is swapped** (`science_camp_duty` /
   `science_camp_mercenary`): `we.science_camp_duty.status_title`="Mercenary scientists" while
   `we.science_camp_mercenary.status_title`="Frontier scientists" -- backwards, since `duty` is
   this project's already-confirmed "Frontier" faction id. This namespace's own `title` field has
   the correct pairing for BOTH ids (`science_camp_duty.title`="Frontier scientists`,
   `science_camp_mercenary.title`="Mercenary scientists"), so `PdaWaypointEventCatalog`'s data can
   be used to identify which of `WorldEventCatalog`'s two strings is the swapped one. Per this
   project's standing practice, `WorldEventCatalog`'s own already-shipped data was NOT silently
   "corrected" -- both classes expose exactly what their own real file lines say.
3. **Different real UI role, not a factual conflict** (`shopot_exit`, `inside_extractor`):
   `we.shopot_exit.status_title`="Clear the area and leave the location" (a real in-progress
   objective instruction) vs. this namespace's `title`="Murmur Post" (a real place/waypoint name);
   similarly `we.inside_extractor.status_title`="Defending Installation" vs. this namespace's
   `title`="Onyx Area". Disclosed as a plausible reading (`WorldEventCatalog.statusTitle` = live
   mission-status text; this namespace's `title` = static map label for the same event) rather than
   a confirmed fact, per rule 1.

Separately, id-set comparison found 57 real ids that exist ONLY in `pda.waypoint.we.*` (e.g.
`frontline_camp_psy_officer`, `dmz26_camp_docs`, `outpost`) and 60 real ids that exist ONLY in
`we.*` (e.g. `abstract_clash_st_ba`, `inside_arena`, `ny24_worldboss`) -- each namespace genuinely
covers some real events the other does not; neither is a strict subset/superset of the other.

**Testing.** 6 new `PdaWaypointEventCatalogTest` tests: simple title/desc resolution; the real
compound-id cases handled without special-casing; the real `outpost` per-faction fields; the real
flat-exception-plus-duplicate-`.title` quirk; real examples of all three cross-reference-mismatch
categories (captured as this namespace's own data, not "corrected"); and unknown id/field
resolving to null rather than a guess.

**Live-verification.** Wrote a dedicated probe (`PdaCheck.java`, scratchpad-only) and ran it
directly against the real, full `en.lang` file before writing the Javadoc's specific claims and
again before shipping: `size()`=293, `ids().size()`=280, both flat ids
(`shopot_control_center`/`shopot_exit`) confirmed, both swapped-bug titles confirmed byte-exact
(`science_camp_duty`="Frontier scientists", `science_camp_mercenary`="Mercenary scientists"),
`outpost` fields and the `insideout_centrifuge.status` compound id both confirmed exactly as
documented.

**Build.** Compiled clean with `--release 11` in the cloud (1096 total tests, 0 failures), synced
to the device via the standard tarball/`SendUserFile`/`device_commit_files`/extract pipeline, and
re-run live on the device's actual Java 11 JRE with an identical clean result: 1090 -> 1096.

**A secondary, doc-only finding along the way (no code change needed).** While auditing `location.*`
(336 real lines today), found that `LocationNameRegistry`'s part-105 Javadoc cites stale historical
counts (298 of 345 real `location.*` lines) that no longer match the currently-staged file (292 of
336). Investigated whether this was a functional gap: it is not. `LocationNameRegistry`'s loader
and `size()` are fully dynamic and correctly reflect whatever file is actually loaded at runtime;
only the descriptive PROSE in its Javadoc is stale. Cross-checking against `ItemLangCatalog` (part
121, states "12,427" -- matches today exactly) and the original part-90 `LangCatalog` top-level
census (which had claimed `item`=12,611, `location`=345, etc.) confirmed the real content changed
between part 90's original snapshot and part 105/part 121's later ones, not since -- i.e., this is
old, already-absorbed content drift the project's own later classes had already correctly adapted
to, not a new discovery. Per the standing rule against re-extracting snapshot data just because a
count changed, the stale prose was left as-is rather than "fixed" reflexively; this is disclosed
here for the record rather than silently ignored.

**Closing note.** The user's requested `item.`/`general.`/`quest_contract.` audit is complete, with
one genuine, substantial gap found and fully resolved this part. Namespaces not yet individually
re-verified by this same technique: `cnps.`, `currency.`, `loadscreens.`, `hideout.`, `settlement.`,
`checkpoints.`, `map.`, `ejection.`, `world.`, `phonograph.`, `tile.`, and the smaller top-level
prefixes below `banner` in the original census. Future work could continue the same audit technique
there, or shift toward wiring already-built catalogs into engine behavior wherever a confirmed
crosswalk exists (this part's `science_camp_duty`/`mercenary` swap finding is itself a candidate:
if `GameLoop`/`WorldEventCatalog` ever surfaces this text to a player, `PdaWaypointEventCatalog`'s
correctly-paired data could resolve which string is right -- not attempted this part).
