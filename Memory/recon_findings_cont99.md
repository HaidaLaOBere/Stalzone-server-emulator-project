# Part 143 -- `PremiumCatalog`

## Real shape

The real `premium.*` namespace (38 lines), the twenty-third concrete namespace built from part
123's follow-up census -- real "STALZONE PREMIUM" subscription-perk marketing copy. Reconciled
exactly: 1 (`desc`) + 3 (`inline_perk.*`) + 12 (`perk_block.*`, 6 ids x title/description) + 2
(`purchaseOptions.*`) + 20 (`perkBlocks.*`, 6 ids x title/desc/details.N) = 38.

## Generic-table-plus-flat-carve-out

`premium.desc` has zero dots after the prefix, so it does not fit the generic last-dot-split
table -- modeled as a separate flat `desc()` field, same pattern as part 133's
`PartyMissionsCatalog` `name_format` exception.

## Two UI generations of the same real feature set

The older `perk_block.<id>.title`/`.description` layout (numbered "1."-"6." in titles) and the
newer `perkBlocks.<id>.title`/`.desc`/`.details.<n>` layout (all-caps) both describe the SAME 6
real perks under different id spellings/casings (e.g. `perk_block.fillers` vs `perkBlocks.loot`
both describe "earn more resources for sale") -- disclosed as two UI generations shipped side by
side, not merged.

## Real markup preserved verbatim

The older layout's descriptions use real `§#FFC34F...§r` inline color-tag markup,
preserved as-is (a real client rich-text convention, not an encoding error).

## Testing

3 new `PremiumCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 993 -> 996.

## Context for future parts

Twenty-three namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `arcade`(36), `seasons`(29), `encaged`(29), `hint`(24),
`gameobjects`(21, en.lang text distinct from the file-based GameObjectsCatalog), `incident`(20),
`snowstorm`(18), `industries`(18), `challenge`(17, singular), `arena`(17), `inside`(16),
`blizzard`(16), `outpost`(14, check for overlap with existing `HomeOutpostCatalog` before
writing), `ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11) -- note this last
one shares its name with a real id INSIDE this part's own `perk_block.personal_storage`/
`perkBlocks.all` fields; worth a quick disclosed-not-merged check when built.
