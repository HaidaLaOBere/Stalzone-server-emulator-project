# recon_findings_cont132.md — Part 176: `world.props.RegionLabelCatalog`

**Date:** 2026-09-20

## Context

Continuing the rarer-namespace sweep from part 175, cross-referenced the remaining smaller
top-level prefixes against every covering class's actual source (not just Javadoc claims):
`core`, `clans`, `battlefield`, `arcade`, `ny`/`ny_toys`, `seasons`, `encaged`, `hint`,
`gameobjects`, `incident`, `snowstorm`, `industries`, `arena`, `blizzard`, `outpost`, `mail`,
`merc`/`freedom`/`duty`/`covenant`/`bandit`, `region`, and the small `toxic_dogout_hint1`/
`subtitles`/`signals`/`signal_*`/`polygon7_hint` singles.

All of these were confirmed already genuinely covered:
- `industries.logistics.*` (18 lines) — `IndustriesCatalog`, 100% under `logistics.`.
- `mail.message.season_reward.*` (6 lines) — `PlayerMessagingCatalog`; confirmed NOT the same real
  key family as `TriumphCatalog`'s separate `triumph.2026.mail.message.reward.*` (2 lines, nested
  under a different top-level prefix entirely).
- `merc./freedom./duty./covenant./bandit.protected_areas.alert.notification.*` (25 lines total,
  5 per faction) — `FactionBaseWarningCatalog`.
- `core.handbook.*` — `HandbookCatalog`. `clans.*` — `ClanWarCatalog`. `battlefield.id.*` —
  `BattlefieldCatalog`. `arcade.*` — `ArcadeCatalog`. `ny.*`/`ny_toys.*` — `NewYearEventCatalog`.
  `seasons.*` — `SeasonsCatalog`. `encaged.*` — `EncagedCatalog`. `hint.*` — `HintCatalog`.
  `gameobjects.*` — `GameObjectsLangCatalog`. `incident.*` — `IncidentCatalog`. `snowstorm.*` —
  `SnowstormCatalog`. `arena.*` — `ArenaCatalog`. `blizzard.*` — `BlizzardCatalog`. `outpost.*` —
  `OutpostModeCatalog`. `toxic_dogout_hint1.*`/`subtitles.*`/`signals.*`/`signal_*.*`/
  `polygon7_hint.*` — all raw-key lookups already in `OddsAndEndsCatalog`.

Exactly one exception: the bare top-level `region.<factionId>_<zoneId>.name` (4 lines).

## The find: `region.*`

This 4-line namespace had been independently quoted VERBATIM as crosswalk evidence in three
different classes' own Javadoc across multiple earlier parts — `SettlementRegistry` (part 105),
`MapLangCatalog` (part 126), and `TextureLoadscreenCatalog` — but none of the three had actually
modeled it as its own parseable class. Confirmed by a direct source-tree search for a literal bare
`"region."` prefix load: the only hit was `GeneralCatalog`'s own `REGION_PREFIX` constant, which on
inspection is actually scoped to `general.region.` (a real, different, unrelated key family under
the `general.` prefix), not this bare top-level one. A small, previously-unmodeled — though
partially pre-quoted — gap, not a wholly blind spot.

Real content, all 4 lines: `stalkers_bolota.name`="Ataman's HQ", `bandits_bolota.name`="Boathouse",
`stalkers_kordon.name`="Fire Station", `bandits_kordon.name`="Village School".

Both halves of the real `<factionId>_<zoneId>` id shape are already-confirmed real vocabulary
elsewhere: `stalkers`/`bandits` match part 173's already-confirmed `pda.waypoint.we.outpost.
stalkers`="Stalker Outpost"/`.bandits`="Bandit Outpost"; `bolota`/`kordon` match
`LocationNameRegistry`'s already-confirmed real zone codes ("The Swamps"/"The Roadside"). This
justified adding `factionId(id)`/`zoneId(id)` accessors that split on the first underscore, backed
by real evidence rather than an unconfirmed guess.

All 4 real entries resolve to already-known settlements from a FOURTH independent angle: `csky`=
"Ataman's HQ" was already directly quoted by `SettlementRegistry`'s own doc as one of its two
corroborating strings; `lodochnaya`="Boathouse", `garage`="Fire Station", `foton2`="Village School"
each gain a NEW corroborating real id here, not previously spelled out with these specific ids.
Read together, the real data shows the `bolota` and `kordon` zones each have one
"stalkers"-labeled and one "bandits"-labeled named sub-area with DIFFERENT display names — not the
same settlement renamed per faction.

## Class built

`RegionLabelCatalog` (`region.*`, PREFIX `"region."`, SUFFIX `".name"`, flat id-to-name map).
Accessors: `name(id)`, `factionId(id)`, `zoneId(id)`, `ids()`, `size()`.

## Testing

4 new `RegionLabelCatalogTest` tests against the real, complete 4-line fixture: name resolution for
all 4 real ids with their crosswalk annotations, the factionId/zoneId split, confirming both zones
carry genuinely different real names for their two labeled entries, and unknown-id/no-underscore
edge cases resolving to null.

Live-verified with a dedicated scratch probe (`RegionCheck.java`, deleted after use) directly
against the real, full `en.lang` file: `size()`=4, all 4 real ids/names/faction-zone splits
confirmed exactly. Full suite run clean on both a plain and a `--release 11` build in the cloud,
then synced across and re-run on the device's actual Java 11 JRE: 1104 -> 1108.

## Milestone: full top-level-prefix census now complete

Cross-checking parts 174, 175, and 176 together against the entire real top-level-prefix list from
the fresh census this block, EVERY real top-level `en.lang` prefix now resolves to a covering
class, verified against that class's actual source. This closes out the "rarer namespaces" audit
direction in full.

## Standing rules followed

- Rule 1: the factionId/zoneId split is backed by real, already-confirmed vocabulary elsewhere in
  this project, not an unevidenced guess.
- Rule 5: no snapshot data re-extracted; this is new structural coverage, not a rebalance response.
- Rule 6: nothing pushed to GitHub this part.

## Next step

The full top-level-prefix census is complete. Per the user's own standing instruction to check in
once a block finishes, the next message reports this milestone and offers fresh directions (the
same menu offered after part 174 — chase the swapped-content bug pattern further, push to GitHub,
revisit engine-wiring blockers — plus a note that continuing this exact technique further would now
mean re-verifying SECOND-level completeness for the handful of large classes not yet checked that
way, since every top-level namespace already has a home).
