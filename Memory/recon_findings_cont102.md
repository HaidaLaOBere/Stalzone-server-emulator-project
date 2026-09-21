# Part 146 -- `EncagedCatalog`

## Real shape

The real STANDALONE top-level `encaged.*` namespace (29 lines), the twenty-sixth concrete
namespace built from part 123's follow-up census. Reconciled exactly: 4 (`class.<id>.name`) + 25
(`score_action.<actionId>`, one shared id) = 29.

## Genuinely distinct from GuiLangCatalog's gui.encaged.*

That group is a tutorial-card system. This standalone prefix is a real 4-class roster
(MEDIC/MILITARY/ENGINEER/SCOUT) plus a real post-match scoreboard action-label system for both
asymmetric roles (hunter_*/survivor_*) -- confirmed via direct content comparison.

## A real client typo, disclosed as-is

`hunter_infect_folder_picking`="Infected iItem picked up" -- literal doubled lowercase "i".

## Testing

3 new `EncagedCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1002 -> 1005.

## Context for future parts

Twenty-six namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `hint`(24), `gameobjects`(21, en.lang text distinct from the
file-based GameObjectsCatalog), `incident`(20), `snowstorm`(18), `industries`(18),
`challenge`(17, singular, distinct from plural `challenges`), `arena`(17), `inside`(16),
`blizzard`(16), `outpost`(14, check for overlap with existing `HomeOutpostCatalog`),
`ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11).
