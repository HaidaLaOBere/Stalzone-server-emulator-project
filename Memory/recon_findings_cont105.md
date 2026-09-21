# Part 149 -- `IncidentCatalog`

## Real shape

The real `incident.*` namespace (20 lines), the twenty-ninth concrete namespace built from part
123's follow-up census. Reconciled exactly: 1 (`battlepack.description`) + 1
(`weekly_drop.description`) + 18 (`game_class.sample.<id>.name`/`.desc`, 9 ids) = 20.

## Real finds

`battlepack.description`/`weekly_drop.description` are real multi-line bulleted care-package loot
lists. `game_class.sample.<id>` (9 ids: support, raider, stormtrooper, druid, sniper, marksman,
healer, grenadier, scout) gives a full display name + flavor description per class.

## A real, cautiously disclosed name overlap

`stormtrooper.name`="Assault Trooper" is textually identical to `OperationsCatalog`'s already
-covered Operations-mode class text -- suggestive that `stormtrooper` is the real internal id for
that class, but the other 8 ids in this roster don't match Operations' 3-class roster, so this is
disclosed as a suggestive match, not merged. `scout`/`support` also bare-word-match 2 of
`PlayerClass`'s 6-member enum -- a third independent recurrence of this class-id vocabulary,
disclosed not merged.

## Testing

3 new `IncidentCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1011 -> 1014.

## Context for future parts

Twenty-nine namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `snowstorm`(18), `industries`(18), `challenge`(17, singular,
distinct from plural `challenges`), `arena`(17), `inside`(16), `blizzard`(16), `outpost`(14,
check for overlap with existing `HomeOutpostCatalog`), `ny`(34)/`ny_toys`(13), `season`(12,
singular), `personal_storage`(11).
