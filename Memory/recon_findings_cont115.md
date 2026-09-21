# Part 159 -- `PersonalStorageCatalog`

## Real shape

The real `personal_storage.*` namespace (11 lines), the fortieth concrete namespace built from
part 123's follow-up census. Generic (id,field) table, no special-casing: availability.settlement
(10 fields) + availability.premium (1 field, separate id) = 11.

## Real finds -- richest reputation-rank corroboration chain yet

All 10 real availability.settlement.* unlock texts exactly match part 107's CnpsCatalog
per-settlement rank ladder top titles: csky=Vodyanoy, lodochnaya=Fisherman, garage=Border Guard,
foton2=Brigadier, rostok=On the house!, sever/wnorth_10=Legend of the Zone (same rank, two
settlements/numbers), alliance_stalkers=General/Zone Guardian, alliance_bandits=Professional/
Apostle. Also re-confirms CnpsCatalog's own dolg=Frontier/svoboda=Rise crosswalk via the real
"Frontier Base"/"Rise Base" names.

## Real inconsistency, disclosed not resolved

The "required reputation (N)" number equals CnpsCatalog's (top level index + 1) for the 0-4 and
0-6 ladders, but NOT for the 0-10 wnorth ladder (wnorth_10 requires literal "(10)", not "(11)").
No universal formula asserted.

## Testing

3 new `PersonalStorageCatalogTest` tests against a fixture built from real key=value lines. Run
clean on both a plain and a `--release 11` build in the cloud and, synced across, on the device's
actual Java 11 JRE: 1041 -> 1044.

## Context for future parts

Forty namespaces down. What remains is the smaller grab-bag from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `profile_icon`, `banner`, `territories`, `provision`,
`mail`, `entity`, `item_tier`, `anomaly`, `barter`, `menulinks`, `death`, `review`,
`main_menu_popup`, `tutorial`, `bonuscode`, plus ~15 single-line "prefixes" not worth individual
classes. Each of these is small enough that several may be worth combining into one class/part
going forward (two-tables-in-one-class or generic-table-plus-flat-carve-out as content dictates),
similar to how this part handled `ny.*`/`ny_toys.*` together.
