# Part 139 -- `ItemOriginsCatalog`: real loot-source tooltips, a confirmed DMZ-league crosswalk, and a new "Inside Out" 2025 ranked event

Continuing top-down from part 138's closing note, this part models the real 55-line
`item_origins.*` namespace -- real loot-source/"where did this item come from" tooltip text (53
`name` + 2 `item_origin_tip` = 55 exactly), modeled with the established generic last-dot-split
(id, field) table; several real ids nest two segments deep (e.g. `bag.murmur`,
`wpn.gauss_pistol`), handled without special-casing.

## A real confirmed crosswalk into part 135's DMZ leagues

The 6 real `dmz_league_<codename>` ids literally NAME which of `dmz.leagues.<n>` each codename
is: `legend`="...entering the Baron's Guard league..." (leagues.1), `master`="...Market Elite
league..." (leagues.2), `veteran`="...Market Agents league..." (leagues.3), `stalker`="...Black
Raiders league..." (leagues.4), `newbie`="...Market Operatives league..." (leagues.5=
"Operatives" -- a real minor textual variance, an extra "Market " prefix, disclosed not merged),
`picklock`="...Market Recruits league..." (leagues.6="Recruits", same real "Market " variance).
All 6 real lines date this ladder to the "Black Raid event ranking (2024)".

## A real NEW "Back to the Inside Out" 2025 ranked event

The 6 real `inside_league_<codename>` ids (the same 6 codenames) name an entirely separate real
ranked ladder for a "Back to the Inside Out event ranking (2025)": `picklock`="Shadow",
`newbie`="Devastated", `stalker`="Distorted", `veteran`="Phantom", `master`="Risen", `legend`=
"Legend of the Inside Out" -- a genuinely new real tier-name roster for this project's
long-running "Inside Out" thread (parts 106/122, `CustomNpcsCatalog`, `TextureGuiCatalog`,
`TextureItemsCatalog`, `TextureParticlesCatalog`). The tier CODENAME vocabulary itself
(`newbie`/`picklock`/`stalker`/`veteran`/`master`/`legend`) recurs a third time in this project
as the real word "Picklock", already a named reputation rank on part 107's `CnpsCatalog` `csky`
settlement ladder -- disclosed as a recurring real tier-naming convention across independent
systems, not claimed to be the same underlying rank structure.

## Other real finds

`arsenal.name`="Arsenal Workshop" directly corroborates part 109's `CurrencyDisplayCatalog`
`arsenal_reputation` find; `arcade_<n>.name` (8 real level thresholds: 2/5/15/35/75/150/220/300)
is a real "session modes" level-reward ladder; `bag.murmur`/`arm.zivcas_commander_player`/`wpn.
gauss_pistol`/`wpn.gauss` tie to the already-confirmed Murmur faction, the "ZIVCAS CORPORATION"
seasonal event (part 115), and the Gauss Rifle ammo economy (part 106).

## Testing

3 new `ItemOriginsCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`: the same real "picklock" tier codename resolving two
genuinely different real league texts, a real nested 2-segment id plus the real arsenal
corroboration resolving, and sorted/null-safe id and field lookups. Compiled with `--release 11`,
run clean in the cloud (984/984) and, synced across via the usual tarball pipeline, live-verified
on the device's actual Java 11 JRE (984/984).

## Context for future parts

Nine more down from part 123's long tail (parts 131-139). What remains: `seasonpass` (49), and
the long tail of smaller namespaces documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`).
