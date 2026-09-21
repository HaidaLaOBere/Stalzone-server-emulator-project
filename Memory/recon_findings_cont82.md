# Part 126 -- `MapLangCatalog`: 13 real named world regions plus fast-travel routes

Third concrete namespace built from part 123's census. Models the real 193-line `map.*`
namespace, confirmed by direct count to split cleanly into 5 groups with no leftover lines
(81+78+26+6+2=193): `location`, `teleport`, `global.region`, `safezone`, `page`. Named
`MapLangCatalog`, not `MapCatalog`, to avoid confusion with the file-based `PdaMapCatalog` (part
64, PDA map asset files) and `MilestoneMap` -- both checked before naming.

## Real content -- the standout find

`global.region.<id>.name`/`.desc` (26 lines) turned out to hold 13 real world regions, not the
smaller number a first sample suggested -- each with a full multi-sentence lore description, not
just a name. This is a THIRD distinct real "region" concept the project has now found (confirmed
by direct content comparison, not assumed): `GeneralCatalog`'s `general.region.*` (part 118,
platform/publishing labels), the standalone top-level `region.*` (part 123's census,
faction-controlled sub-area labels), and this one, which names whole in-fiction geographic
regions of the Zone. All 13: `south`="South Zone", `sever`="North Zone" (a real id collision --
`SettlementRegistry`/`CnpsCatalog` already use `sever` for the "Drunken Geiger Bar" settlement, a
genuinely separate id space sharing one short string, disclosed not merged), `limansk`=
"Lyubech-3", `zaton`="Stillwaters", `tournament_nizina`="Lowland", `tournament_roza_vetrov`="The
Wind Rose", `tournament_hvoiny`="Coniferous", `pripyat`="Pripyat", `cnpp`="ChNPP" ("the heart of
the Zone"), `wnorth`="Wild North" (ties to `SettlementRegistry`'s own `wnorth` id), `sovhoz`=
"Sovkhoz", `chernobyl4`="Chernobyl-4" (real lore: a former SSPP staging ground, now a crater,
tying into the existing SSPP/Murmur conflict thread), `dmz`="Boral" (a real named DMZ-mode
island, finally naming what part 123's bare `dmz`/`dmz2026` prefixes never themselves named).

## Other groups

`location.id.<id>.name` (81 lines) substantially overlaps the already-covered top-level
`location.<id>.name` family (`LocationNameRegistry`, part 105) -- real corroboration from an
independent prefix, not a duplicate to skip. `teleport.id.<fromId>-<toId>.name` (78 lines) gives
real named fast-travel route labels never catalogued from any other angle before (e.g.
`bolota-kordon.name`="The Swamps—The Roadside"). `safezone.id.<id>.name` (6 lines) corroborates
`SettlementRegistry` yet again, several under different zone-code ids: `bar`="Pervomaisky
Factory" (direct match), `lodka`="Boathouse" (alias for `lodochnaya`), `atp`="Fire Station"
(alias for `garage`), `bvs`="Village School" (alias for `foton2`), `podzemka`="Intersection"
(new). The sixth, `cs`="Clear Sky Base", is a genuinely new real id -- distinct from
`SettlementRegistry`'s own `csky` (already resolved to "Ataman's HQ," which supersedes an earlier
speculative "csky abbreviates Clear Sky" guess). This confirms Clear Sky IS real in-fiction
content, just under a different id than the one originally guessed, without reopening the
already-settled `csky` resolution. `page.id.<id>.name` (2 lines): `east.name`="West" (a real
odd inverted/legacy label, disclosed as-is) and `limansk.name`="Lyubech-3" (matches the global
region name exactly).

## Testing

6 new `MapLangCatalogTest` tests against a fixture built from real lines drawn directly from the
live client's `en.lang`, one from each group plus the Clear Sky Base and inverted-"West" finds.
Compiled with `--release 11`, run clean in the cloud (932/932) and, synced across via the usual
tarball pipeline, live-verified on the device's actual Java 11 JRE (932/932).

## Context for future parts

Three for three from part 123's census now. Remaining large candidates from that same list:
`checkpoints` (202 lines, mostly a generic dungeon-checkpoint-name table with a handful of flat
`_finish`-suffixed exceptions, not yet fully characterized), `core` (197 lines, dominated by a
`handbook.<category>.title`/`.main` pair, 94 of 197 lines), `frontline` (192 lines, a mixed
namespace: `calldown.*` airstrike items plus separate campaign-briefing text, likely the most
structurally complex of the four and worth its own closer look before modeling).

## Memory-store note

`cont82`, following directly from `cont81` (part 125). `Memory/` remains the active store; MCP
project memory not re-checked this part.
