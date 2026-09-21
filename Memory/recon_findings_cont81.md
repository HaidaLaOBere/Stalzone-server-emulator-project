# Part 125 -- `SettlementPerkCatalog`: real per-settlement reputation-level progression

Second concrete namespace built from part 123's census. Models the real 202-line
`settlement.id.*` namespace -- confirmed a COMPLETELY DIFFERENT real system from the file-based
`SettlementRegistry` (part 28, a bare 15-name client-log roster) despite the shared word
"settlement", as flagged in part 123's survey.

## Real shape

`settlement.id.general.perk.<perkId>` (47 lines) is a flat, settlement-agnostic lookup of perk
DESCRIPTION TEXT BY VALUE (`research_cost_25`="25% discount on research",
`art_price_50`="+50% to the cost of selling artifacts and mutant parts", etc.) -- a generic value
catalog, not tied to any one settlement's actual unlock levels.

The remaining 155 lines are organized per real settlement/base id: `merc`, `freedom`, `duty`,
`covenant`, `sever`, `rostok`, `lodochnaya`, `wnorth_bar`, `garage`, `foton2`, `csky`,
`battlefields`, `north_factions_block`, `bar_sever`, `bar_save_residential_hangar`, `td`, `agro`
-- the same 15 real ids `SettlementRegistry.ALL_KNOWN_SETTLEMENTS` already names (plus the
`sever` unprefixed alias for `bar_sever` that class's own part-105 doc had already flagged),
confirming that crosswalk once more from a third independent `en.lang` source. Each carries:
`.title` (16), `.default_desc` (14, level-1 flavor text), `.level.<n>.desc` (62, level-2-and-up
flavor text), `.level.<n>.perk.<perkId>` (60, the SPECIFIC perk that settlement grants at that
level -- with its own concrete value, not just a reference into `general.perk.*`: e.g.
`csky.level.3.perk.art_price`="+10%..." while `lodochnaya.level.3.perk.art_price`="+20%...",
genuinely different values per settlement, verified by direct comparison). One real one-off:
`battlefields.unavailableLine`="Not enough reputation" (a locked-content UI message).

## Real find: a cross-settlement unlock

`sever`'s own level ladder runs to level 7 (deeper than the 5-level ladders everywhere else), and
`rostok.level.7.perk.sever`="Access to the Drunken Geiger" -- reaching level-7 reputation at
Rostok grants entry to the Bar's "Drunken Geiger" area. This directly extends
`SettlementRegistry`'s own part-107 update, which found (via `CnpsCatalog`'s
`cnps.faction.settlement_<id>.relation.<level>`) that `rostok`/`wnorth` express reputation
through quoted dialogue rather than a formal rank noun -- this namespace supplies the matching
real flavor-text and perk-unlock side of that same progression, a third independent corroborating
source.

## Design

Not a single generic `(id, field)` table this time -- the real 3-level nesting
(`settlement.id.<settlementId>.level.<n>.perk.<perkId>`) doesn't fit that shape cleanly, so this
class uses purpose-built fields (title/defaultDesc/levelDesc/levelPerk/generalPerkDescription/
extraField) parsed via two small regexes, the same bespoke-fields-over-forced-genericism approach
used for `GeneralCatalog` (part 118).

## Testing

6 new `SettlementPerkCatalogTest` tests against a fixture built from real lines drawn directly
from the live client's `en.lang`: the generic `general.perk.*` lookup, csky's full title/
default_desc/level-desc/level-perk chain, the csky-vs-lodochnaya differing per-settlement perk
values for the same perk id, the real rostok-to-sever cross-settlement unlock plus sever's deeper
7-level ladder, battlefields' one-off `unavailableLine`, and sorted/null-safe id and level lookups.
Compiled with `--release 11`, run clean in the cloud (926/926) and, synced across via the usual
tarball pipeline, live-verified on the device's actual Java 11 JRE (926/926).

## Context for future parts

Confirms part 123's census keeps paying off directly. Next candidates from that same survey:
a follow-up batch covering `checkpoints`/`core`/`map`/`frontline` (192-202 lines each, all large
single-shape namespaces sampled but not yet modeled).

## Memory-store note

`cont81`, following directly from `cont80` (part 124). `Memory/` remains the active store; MCP
project memory not re-checked this part.
