# Part 155 -- `BlizzardCatalog`

## Real shape

The real `blizzard.*` namespace (16 lines), the thirty-fifth concrete namespace built from part
123's follow-up census. Reconciled exactly: 11 (`event.<field>`) + 3
(`event.props.class.<id>.name`) + 2 (`base.<id>`) = 16.

## Real finds

A real two-sided PvPvE base-defense world event. "Human" side (Stalker/Bandit Base) defends real
"heating installations" against mutants; a real "Frost Fighters" faction defends real "Crystals"
against the Blizzard's own hunter/shaman/thug forces. 3 real hunter-side classes: Hunter, Shaman,
Thug ("tank" id maps to real name "Thug").

## Real corroborations, disclosed not merged

The real "Frost Fighters" faction name is the SAME name already confirmed by part 111's
WorldEventCatalog seasonal rescue event `we.ny25`="Help the Frost Fighters" -- an independent
real narrative link, no shared id/key structure. A real reference to "the Sovkhoz... swarming
with their spawn" reconfirms part 116's LocationNameRegistry `location.sovhoz.name=Sovkhoz of
Shevchenko`. Not merged with part 150's SnowstormCatalog -- a genuinely distinct winter-themed
system with no base-defense content, a thematic pairing only.

## Testing

3 new `BlizzardCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1029 -> 1032.

## Context for future parts

Thirty-five namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `outpost`(14, MUST check for overlap with existing
`HomeOutpostCatalog` before writing), `ny`(34)/`ny_toys`(13), `season`(12, singular, distinct from
`shop.season.*` and `seasons`), `personal_storage`(11, worth a quick disclosed-not-merged check
against part 143's PremiumCatalog `perk_block.personal_storage` field). After that, the smaller
grab-bag: `profile_icon`, `banner`, `territories`, `provision`, `mail`, `entity`, `item_tier`,
`anomaly`, `barter`, `menulinks`, `death`, `review`, `main_menu_popup`, `tutorial`, `bonuscode`,
plus ~15 single-line "prefixes" not worth individual classes.
