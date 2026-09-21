# Part 130 -- `WorldModifierCatalog`/`PhonographCatalog`/`TileCatalog`: three small/medium namespaces batched from part 123's long tail

Part 129's own closing note explicitly flagged batching several small/medium namespaces from
part 123's census together as a viable next-part strategy, rather than continuing strictly
one-class-per-part. This part follows through on that option: three namespaces investigated
together, `world.modifier.*` (138 lines), `phonograph.*` (135 lines), `tile.*` (114 lines) --
all three confirmed zero flat exceptions at their top level before any code was written.

## `WorldModifierCatalog`

`world.modifier.<id>.name`/`.description` (138 lines, 69 real modifiers x 2 fields, zero flat
exceptions) is a real Arena-mode buff/debuff description system. Named `WorldModifierCatalog`,
not `WorldCatalog`, to avoid any confusion with the unrelated already-covered `we.*` namespace
(`WorldEventCatalog`, part 111, "World Events") -- checked and confirmed a genuinely separate
real prefix and real feature, not a naming variant of the same system. Several real ids share the
`ravent_level_*` prefix (e.g. `ravent_level_electra_activity`="Proto-Discharge Activity",
`ravent_level_lighter_activity`="Proto-Lighter Activity", `ravent_level_funnel_activity`=
"Proto-Whirlpool Activity") -- real anomaly-spawn-rate modifiers tying directly to the
still-uncovered top-level `ravent.*` namespace from part 123's own census (Arena-mode wave/round
names), not modeled by this class.

## `PhonographCatalog`

`phonograph.*` (135 lines) resolves a real in-game "phonograph"/jukebox
collectible-vinyl-and-soundtrack feature, confirmed by direct count to split into two real groups
with no leftover lines (100+35=135): `gui.track.<trackId>` (100 lines, flat, real soundtrack
TRACK NAMES playable through the in-game phonograph -- a mix of real named vocal tracks, e.g.
`Grin_Danilov_moi_dozhd`="My Rain", `Grin_Danilov_na_reke`="On The River", and real ambient
("Sounds of...") tracks tied to already-confirmed real locations, e.g. `ambient_bolota`="Sounds
of the Swamps", `ambient_limansk`="Sounds of the Lyubech-3"), `vinyl_<category>.title` (35 lines,
flat, real COLLECTIBLE VINYL RECORD titles): most ids fall into two real category prefixes,
`vinyl_location_<zoneId>` (per-location collectible records reusing already-confirmed real
location display names, e.g. `vinyl_location_bolota`="The Swamps") and `vinyl_event_<eventId>`
(event-tied collectible records: `black_raid`, `blizzard`, `dmz26`, `frontline`, `newyear26`,
`reichstag_breakthrough` -- another entry in this project's long-running Reichstag corroboration
chain, no new ordinal asserted -- `rus_arena`), plus one real standalone id outside either
category, `vinyl_gribnaya_bylina`="Mushroom Bylina". This class stores the `vinyl_*` id verbatim
(including its `vinyl_` prefix and category infix) rather than splitting it into a
category/sub-id pair, since the one real standalone id does not fit either category cleanly and
this project does not invent a taxonomy the client data itself does not enforce.

## `TileCatalog`

`tile.<numericId>.name` (114 lines, flat, zero flat exceptions -- the entire namespace fits this
one shape) is a real level-design material/flooring-tile display-name table, e.g.
`3500`="Stones (Floor)", `3501`="dull yellow tile", `3503`="wall". Real capitalization/formatting
is inconsistent across entries (compare `3500`="Stones (Floor)" with `3501`="dull yellow tile")
-- disclosed as a genuine real client inconsistency, not normalized by this class.

## Testing

9 new tests (3 `WorldModifierCatalogTest` + 3 `PhonographCatalogTest` + 3 `TileCatalogTest`)
against fixtures built from real key=value lines drawn directly from the live client's
`en.lang`: for each class, a real entry's fields resolving together, a second real entry
resolving independently, and sorted/null-safe id lookups. Compiled with `--release 11`, run clean
in the cloud (957/957) and, synced across via the usual tarball pipeline, live-verified on the
device's actual Java 11 JRE (957/957).

## Context for future parts

Three more down from part 123's long tail. What remains: `bpseason` (98), `party_missions` (95),
`ravent` (87, itself referenced by this part's `world.modifier.ravent_level_*` finds), `prestige`
(79), `dmz`/`dmz2026` (78/17, ability text -- still flagged for an explicit
near-identical-"Hook"-ability compare when built), `delivery` (65), `orders` (64), `modules`
(59), `item_origins` (55), `seasonpass` (49), and the long tail of smaller namespaces documented
in part 123's own writeup (`Memory/recon_findings_cont79.md`). Continuing to batch several
related small/medium namespaces per part (as done here) remains a viable option alongside working
the list top-down one at a time.
