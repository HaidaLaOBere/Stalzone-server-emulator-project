# Part 127 -- `CheckpointsCatalog`: real per-dungeon waypoint/checkpoint names

Fourth concrete namespace built from part 123's census. Models the real 202-line `checkpoints.*`
namespace -- real per-dungeon waypoint/checkpoint display names (entry points, numbered control
points, boss-warp points), used to label a dungeon's progress markers on the in-game map/PDA.

## Real shape

Confirmed by direct count to split into two real shapes, no leftover lines (196 + 6 = 202): 196
lines fit `checkpoints.<dungeonId>.<pointId>`, modeled with a generic `(id, field)` table (the
checkpoint slot -- `entry`, `control`, `control_point`, `control_point_<n>` (n observed 1-14,
NON-CONTIGUOUS in several real dungeons -- e.g. `dungeon_stels` jumps from `control_point_8`
straight to `control_point_12`, a real gap not a data error), `control_warp`, `start` -- as
field). 6 real lines are flat exceptions with no second dot at all: `dungeon_enlightened`=
"Unknown", `dungeon_zemlcap1_finish`="Dugout", `dungeon_sklad_frimena_finish`="Warehouse",
`dungeon_svetliy_stalkers_finish`="Light's Hideout", `dungeon_svetliy_bandits_finish`="Light's
Hideout" (two real dungeon variants sharing one finish label), `dungeon_psisobaka_lobby_finish`=
"Unknown". Same generic-table-plus-flat-carve-out shape already established by `WorldEventCatalog`
(part 111) and `OperationsCatalog` (part 113), rather than the "zero flat exceptions" shape of
several other `*LangCatalog` classes.

## Real content note

The literal string "Unknown" is itself a real, disclosed-as-is client value for several
checkpoints, not a placeholder invented by this project -- several real dungeons ship with one or
more control points still unnamed in this client build (e.g. `blizzard_boss`'s entire 6-point
ladder is "Unknown" except for its own id).

## Testing

5 new `CheckpointsCatalogTest` tests against a fixture built from real lines drawn directly from
the live client's `en.lang`: a real dungeon's entry/numbered control points including the
non-contiguous numbering gap, the literal real "Unknown" value, sorted point/dungeon id listings,
the flat `_finish`-suffixed exceptions (including the two-variant-one-label case), and null-safe
unknown-id lookups. Compiled with `--release 11`, run clean in the cloud (937/937) and, synced
across via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE (937/937).

## Context for future parts

Four for four from part 123's census. Two large candidates remain from that same list: `core`
(197 lines, dominated by a `handbook.<category>.title`/`.main` pair, 94 of 197 lines, with a
dozen-plus small one-off fields not yet characterized in detail) and `frontline` (192 lines, a
mixed namespace blending `calldown.*` airstrike items with separate campaign-briefing text,
likely the most structurally complex of the four and worth a closer sampling pass before
modeling).

## Memory-store note

`cont83`, following directly from `cont82` (part 126). `Memory/` remains the active store; MCP
project memory not re-checked this part.
