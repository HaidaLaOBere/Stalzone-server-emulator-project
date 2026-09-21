# Part 153 -- `ArenaCatalog`

## Real shape

The real `arena.*` namespace (17 lines), the thirty-third concrete namespace built from part
123's follow-up census. Reconciled exactly: 6 (`id.<mapId>.name`) + 11
(`equip_preset.id.<presetId>.name`) = 17.

## Real finds

6 real Arena maps (Hangar/Depot/Mine/Warehouse/Hospital/Inferno), corroborating part 106's
StalkerSoundEventsCatalog `arena/` folder. 11 real loadout presets including `custom`="Your gear".

## Real corroborations and one real inconsistency vs IncidentCatalog

`sniper`="Sniper" and `marksman`="Shooter" are EXACT id+name matches with part 149's
IncidentCatalog sample roster. `assault`/`stormtrooper` and `shotgun`/`druid` are
same-name-different-id pairs. Most notably: the SAME id `scout` maps to "Raider" here but
"Scout" in IncidentCatalog -- a genuine real client data inconsistency, disclosed exactly as
found, not corrected.

## Testing

3 new `ArenaCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1023 -> 1026.

## Context for future parts

Thirty-three namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `inside`(16), `blizzard`(16, likely thematically paired with
part 150's `snowstorm`), `outpost`(14, check for overlap with existing `HomeOutpostCatalog`),
`ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11).
