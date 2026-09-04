# Weapon stats dataset (stalcraftdb.net + stalzone.wiki)

## Status: 337 of 338 weapon-category items documented (2026-08-31)

`weapons.json` is now effectively complete. 178 records came from stalcraftdb.net
(the original run, part 12/13); the other 159 came from a second pass against
**stalzone.wiki** (`https://stalzone.wiki/en/items`) after stalcraftdb.net imposed a
site-wide rate-limit ban that was still active when the second pass started. Every
record has a `"source"` field (`"stalcraftdb.net"` records don't have one set --
absence means stalcraftdb.net, matching the original schema before this field
existed -- `"stalzone.wiki"` records are explicitly tagged).

**Only one item remains undocumented**: `5lndo` / "Modified Colt Walker"
(`weapon/pistol`). It's in `weapon_pending_ids.json` (now just this one entry). Its
stalcraftdb.net id returns a soft-404 on stalzone.wiki too (`GET
/en/items/weapon/5lndo` -> HTTP 200 but a client-rendered "not found" page, confirmed
by fetching and inspecting the raw response) -- this looks like a genuine gap in this
particular item's public listing on both sites, not a scraping bug. Left as an open,
documented gap rather than guessed at, per this project's evidence rule.

## Both sources use the SAME underlying id space and schema

`stalzone.wiki`'s item pages expose an `exbo_id` field that is **the exact same
opaque id** stalcraftdb.net uses (e.g. `1r7dr` = PM on both sites) -- both sites are
built on EXBO's official per-update data export, just presented through different
front-ends. This meant the second pass could match by id directly (no fuzzy
name-matching needed), and meant the stat schema translation-keys are identical too
(e.g. `weapon.tooltip.weapon.info.clip_size` for magazine capacity appears verbatim
in both sites' underlying data). Confirmed by direct inspection before writing any
scraping code, not assumed.

## How the stalzone.wiki data was fetched

stalzone.wiki is a Next.js (App Router) site. Item pages don't call a public REST API
for their stats -- the stats are embedded in the page's React Server Component
("flight protocol") payload, inside `<script>` tags that normally hydrate the page
client-side. The scraper: `fetch()`s each item page's raw HTML (same-origin, run
in-page via the browser's javascript tool, not a full page navigation -- lighter on
the server than loading a full page with all its JS/CSS/images), parses out those
script tags, evaluates each one in a sandboxed `self.__next_f.push` collector (the
exact mechanism the real page uses to hydrate, just redirected into a plain array
instead of React), concatenates the resulting text chunks, and JSON-parses each
`id:payload` line of the flight stream to find the one containing the item's
`info_blocks` array. Paced at ~500ms between requests (a full browser-tab fetch, not
a raw API hit) -- given the stalcraftdb.net lesson (part 13), this was deliberately
conservative. 159 of 160 pending ids succeeded with zero errors on the first
attempt at that pace.

## What's in `weapons.json` now

Same field names as before for drop-in compatibility (`id`, `category`, `name`,
`rank`, `classDisplay`, `weightKg`, `durability`, `maxDurability`,
`movementSpeedModifierPct`, `ammoType`, `damage`, `magazineCapacity`, `maxDistanceM`,
`rateOfFireRpm`, `reloadSec`, `tacticalReloadSec`, `ergonomics`, `spreadDeg`,
`hipSpreadDeg`, `verticalRecoilDeg`, `horizontalRecoilDeg`, `drawTimeSec`,
`aimTimeSec`, `headshotMultiplier`, `limbMultiplier`, `overheatAfterShots`,
`damageFalloff`), plus new fields the wiki pass introduced:

- `meleeDamageMinCommon` / `meleeDamageMaxCommon` / `meleeDamageMinStrong` /
  `meleeDamageMaxStrong` -- light-attack and heavy/"strong"-attack damage ranges.
- `meleeReachCommon` / `meleeReachStrong` -- attack reach in meters, per attack type.
- `meleePiercing` -- armor-piercing stat.
- `meleeBloodlustChancePct` -- proc chance for the "bloodlust" effect.
- `meleeCriticalHitMultiplier` / `meleeMutantDamageMultiplier` -- damage multipliers.
- `raw` -- catch-all for every field this transform doesn't have a friendly name for
  yet (present on `stalzone.wiki`-sourced records; the original stalcraftdb.net pass
  silently dropped its unmapped fields instead -- a difference in the original
  methodology, not something corrected retroactively, since re-deriving `raw` for the
  178 stalcraftdb.net records would need re-fetching them and risking another ban for
  no functional gain).

## RESOLVED: the melee "zero stats" question from part 12/13

Earlier writeups flagged that all 54 `weapon/melee` items came back from
stalcraftdb.net with no numeric stat fields, and left open whether that meant melee
weapons genuinely have no server-exposed combat stats, or whether it was an artifact.
**Now resolved: it was a translation-key mismatch, not a real absence.** Melee weapons
use an entirely different key family (`weapon.tooltip.melee_weapon.info.damage.min/
max.common/strong`, `.info.reach.*`, `.stat_name.piercing`, `.stat_name.
bloodlust_chance`, `.critical_hit_mod`, `.mutant_damage_mod`) instead of the firearm
`core.tooltip.stat_name.damage_type.direct` key the original extraction only looked
for. All 54/54 melee items have real, populated damage data once the correct keys are
read -- see e.g. `96w9q` ("Hunter's Knife": 31-38 light-attack damage, 67-82
heavy-attack, 2.1m reach both, 13.75 piercing, 50% bloodlust chance, x1.7 crit, x2.35
vs. mutants).

## Known taxonomy quirk: "device" vs "weapon/device"

The original stalcraftdb.net-derived pending list tagged the 8 short-range-detector
items (Beluga/Candle/Buryat/Elbrus detectors, SN-1/1u/2/2u "Pancake"/"Leg" devices) as
category `weapon/device`. stalzone.wiki's own data returns their category as plain
`device` -- these items are filed under the site's separate top-level "Devices"
category (18 items total there), not under "Weapon" at all. Recorded exactly as each
source reported it (not silently harmonized) -- if something downstream keys off
`category`, it needs to handle both spellings for these 8 items.

## Raw source snapshot kept for re-processing

`wiki_scrape_raw.json` is the untransformed output of the stalzone.wiki scrape (same
159 records, before merging into `weapons.json`'s combined-source shape) -- kept so
the extraction/normalization logic can be revisited later (e.g. to promote more `raw`
fields to named ones) without re-scraping the site.

## Category counts (from stalcraftdb.net's `/api/listing/eu/grouped`, part 13)

weapon/assault_rifle 90, weapon/submachine_gun 33, weapon/machine_gun 18,
weapon/sniper_rifle 51, weapon/shotgun_rifle 36, weapon/pistol 41 (40 documented + 1
gap, see above), weapon/heavy 7, weapon/melee 54, weapon/device 8. 338 total.
