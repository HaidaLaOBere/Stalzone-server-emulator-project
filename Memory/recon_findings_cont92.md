# Part 136 -- `DeliveryCatalog`: a real courier/parcel "delivery" contract system

Continuing top-down from part 135's closing note, this part models the real 65-line
`delivery.*` namespace -- a real courier/parcel "delivery" contract system, previously
undocumented as a distinct feature in this project.

## Real shape

Confirmed by direct count to be a single real flat shape with zero exceptions: all 65 lines fit
`delivery.<contractId>.name`, one field only, so this class keeps a simple flat map rather than
the generic (id, field) table used elsewhere. Real contract ids combine an `abstract_` prefix,
an existing real zone code (already confirmed by `LocationNameRegistry` and this project's other
location-naming classes -- `les`, `pd`, `armsklad`, `zaton`, `yama`, `mg`, `agroprom`, `svalka`,
`kordon`, `bolota`, `td`, `nbolota`, plus a real `north_*`/`wnorth_*` sub-zone family:
`north_mines`="Mines", `north_inflow`="Inflow", `north_institute`="Institute",
`north_forge`="Forge-11", `north_hrop`="Temple Swamp", `north_barrier`="Barrier",
`north_serpentinte`="Serpentine", `wnorth`="Wild Northern Locations", `wnorth_yanov`="Uranus
Mill", `wnorth_rijik`="The Red Forest", `wnorth_zaton`="Stillwaters"), and usually a faction tag
(`bandit`/`scientist`/`stalker`), with an optional `_premium` suffix variant.

## A real disclosed find

Every real `_premium` id checked shares a BYTE-IDENTICAL display name with its non-premium
counterpart (e.g. `abstract_agroprom_bandit`/`abstract_agroprom_bandit_premium` both ="Kolos
Agrocomplex") -- real evidence "premium" is a reward/difficulty tier of the same delivery
contract, not a separately-named one. This namespace introduces no new real zone codes beyond
what this project has already confirmed; its real value is the delivery-contract SYSTEM
structure itself (location + faction + tier), not new place names.

## Testing

3 new `DeliveryCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`: a real premium/non-premium contract pair sharing a
byte-identical name, real north sub-zone contract names resolving independently, and
sorted/null-safe id lookups. Compiled with `--release 11`, run clean in the cloud (975/975) and,
synced across via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE
(975/975).

## Context for future parts

Six more down from part 123's long tail (parts 131-136). What remains: `orders` (64), `modules`
(59), `item_origins` (55), `seasonpass` (49), and the long tail of smaller namespaces documented
in part 123's own writeup (`Memory/recon_findings_cont79.md`).
