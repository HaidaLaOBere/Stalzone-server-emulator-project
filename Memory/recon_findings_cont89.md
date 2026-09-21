# Part 133 -- `PartyMissionsCatalog`: a real co-op squad "party mission" objective-tracker system

Continuing top-down from part 132's closing note, this part models the real 95-line
`party_missions.*` namespace -- a real co-op squad "party mission" objective-tracker system:
mission NAME labels plus STATUS-TEXT progress templates.

## Real shape

Confirmed by direct count to split into a generic table plus one real flat exception (51 `name`
+ 43 `status_text` + 1 `name_format` = 95 exactly): almost every line fits
`party_missions.<id>.<field>` via the established last-dot-split scheme, except
`party_missions.name_format={name} {status}` itself, which has no second dot at all and is kept
as its own flat value -- the same generic-table-plus-flat-carve-out shape already established by
`WorldEventCatalog`/`OperationsCatalog`/`CheckpointsCatalog`. Real status_text templates carry
real literal `{marker}`/`{completed}`/`{required}` placeholder tokens (e.g.
`kill_any_turned_player.status_text={marker} Kill people {completed}/{required}`), not
substituted by this class.

## Real cross-references

Real ids tie to three already-covered event systems by name: `complete_world_event.frontline_*`
ids reuse Frontline wave/objective vocabulary (parts 111/130), `complete_world_event.dmz_*` ids
tie to the DMZ/DMZ-2026 thread (part 129's `FrontlineCatalog` `calldown_quest_dmz26`), and
several ids reference `encaged` (part 122's `GuiLangCatalog` `encaged.*` tutorial-card group). A
separate real id family, `bolota/frontline_<eventId>`, embeds a literal `/` character -- the same
real structural quirk already seen with `StalkerLangCatalog`'s (part 119) `approv/mark/<type>`
ids and `BpSeasonCatalog`'s (part 131) `24/spring`-style ids -- and reuses the SAME tail
vocabulary as the parallel `complete_world_event.frontline_<eventId>` ids: two real distinct id
shapes over overlapping event-name vocabulary, kept as separate real ids rather than merged,
since the client itself never merges them.

## Testing

3 new `PartyMissionsCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`: the real flat `name_format` exception resolving
distinct from the generic table, real status_text templates and an id embedding a literal `/`
resolving, and sorted/null-safe id and field lookups. Compiled with `--release 11`, run clean in
the cloud (966/966) and, synced across via the usual tarball pipeline, live-verified on the
device's actual Java 11 JRE (966/966).

## Context for future parts

Three more down from part 123's long tail (parts 131-133). What remains: `prestige` (79),
`dmz`/`dmz2026` (78/17, ability text -- still flagged for an explicit near-identical-"Hook"-
ability compare when built), `delivery` (65), `orders` (64), `modules` (59), `item_origins` (55),
`seasonpass` (49), and the long tail of smaller namespaces documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`).
