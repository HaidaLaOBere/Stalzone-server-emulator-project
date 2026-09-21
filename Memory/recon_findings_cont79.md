# Part 123 -- en.lang full namespace census (survey/documentation part, no new code)

Motivated directly by part 122's own closing note: part 90's original "top 15 by count" list
(fully closed out as of part 122's `GuiLangCatalog`) was always a snapshot of the file, not an
exhaustive inventory. This part runs a full top-level-prefix census of the real 28,300-line
`en.lang` file to find what else is in there, cross-references it against every namespace this
project has already modeled (both `en.lang`-based and file-based), and produces a prioritized
list of genuinely uncovered real namespaces for future parts.

## Method

`sed -E 's/^([a-zA-Z0-9_]+)\..*/\1/' en.lang | grep -v "=" | sort | uniq -c | sort -rn` on the
live file at `.../Stalzone july 29.2026 game/depot_1818452/modassets/assets/stalker/lang/en.lang`
(confirmed the only `en.lang` in this extracted-client snapshot; no `patchassets` or
`depot_1818454` alternate copy exists). This gives every real top-level dot-prefix and its line
count. Cross-referenced against `LangCatalog.java`'s "UPDATED part N" Javadoc history (parts
106-117, covering the original top-15 list) plus this project's own parts 118-122 (general,
stalker, go, item, gui) and every file-based catalog whose name could plausibly collide.

## Disclosed but NOT corrected: a live-file count drift on several already-closed namespaces

Direct re-counts today do not match the line counts recorded when those parts were originally
written:

| namespace | recorded (part) | live count today |
|---|---|---|
| `shop` | 731 (part 115) | 698 |
| `challenges` | 1,011 (part 116) | 987 |
| `we` | 508 (part 111) | 506 |
| `currency` | 305 (part 109) | 295 |
| `pda` | 301 (part 114) | 299 |
| `location` | 345 (part 105, per `LocationNameRegistry`'s own doc) | 336 |

Every one of these DECREASED. Verified this is not a grep artifact (reconciled twice) and not an
alternate-file issue (only one `en.lang` exists in this snapshot). The most likely explanation:
the mounted game install is the user's actual live client, which Steam keeps patched -- so the
exact `en.lang` on disk today is not guaranteed to be byte-identical to whatever was on disk on
the day each of those earlier parts ran, even though the extracted-folder path name hasn't
changed. This is content trimming/consolidation from ordinary live-service patching, not a
finding about the client's structure -- per standing rule 5 ("ignore live game rebalancing; only
track new content and bug fixes; extracted stat data is a one-time snapshot, never re-extracted
just because a patch changed a number"), this is disclosed here as an observed fact and the
historical Part 115/116/111/109/114/105 RESEARCH_LOG entries and Javadocs are deliberately left
as-is, not rewritten. `challenges`(987 today) is unaffected by the substring-vs-prefix distinction
(no lines contain `challenges.` outside the `^challenges\.` prefix); `shop` has 17 real lines
elsewhere that merely contain the substring `shop.` (e.g. `item.shop.stalcoins_100.name`,
`loadscreens.location.arsenal_workshop.name`) which is a real, separate, already-noted
distinction, not the explanation for shop's drift either (698+17=715, still short of 731).

## Confirmed near-misses (explicit content check performed, not assumed)

- **`general.region.*` (4 lines) vs. standalone top-level `region.*` (4 lines)** -- genuinely
  different real namespaces despite identical size and id name. `general.region.*` (part 118) is
  a flat platform/publishing-region label map (`eu`="Europe", `na`="North America", etc).
  Standalone `region.<id>.name` gives faction-controlled-area display names, e.g.
  `region.stalkers_bolota.name`="Ataman's HQ", `region.bandits_bolota.name`="Boathouse" -- and
  those exact values also appear as `location.atamans_hq.name`/`location.boat_station.name`, a
  real cross-namespace corroboration (region id encodes which faction currently controls a named
  `location`).
- **`location.*` is only PARTIALLY covered.** `LocationNameRegistry` (part 105) already parses
  the `location.<id>.name` subset (298 of the prefix's total lines) but nothing yet parses the
  remaining ~38 lines under the same prefix -- real `.hint`/`.description` text for a handful of
  locations (e.g. `location.arsenal_workshop.hint`, `location.arsenal_workshop.description`).
  Candidate for a small follow-up class or an addition to `LocationNameRegistry` itself.
- **`settlement.*` (202 lines, real shape `settlement.id.general.perk.<id>`) is a completely
  different real namespace from the file-based `SettlementRegistry` (part 28)** -- that class
  models a bare 15-name client-log roster with no further detail; this en.lang prefix is a real
  settlement-perk description system (trader stock, storage capacity, etc.) with no relation
  beyond the shared English word "settlement". Genuinely uncovered, and the largest fully-uncovered
  real namespace after the five per-faction protected-area systems below.
- **`stalkerguide.*` (26 lines) / `stalkerguide2019.*` (28 lines) are unrelated to the file-based
  `StalkerGuideCatalog` (part 59)**, which catalogs cutscene video/sound/texture assets under
  `modassets/assets/stalkerguide/`. These two en.lang prefixes are real tutorial button-prompt
  text (two versions of the same onboarding flow -- an older 2019 variant and a current one, both
  real, both present in the client simultaneously). Genuinely uncovered.
- **Five per-faction "protected area" trespass-warning systems -- `merc`/`freedom`/`duty`/
  `covenant`/`bandit` (5 lines each, 25 total), all real top-level prefixes shaped
  `<faction>.protected_areas.alert.notification.<n>` -- are a DIFFERENT, richer real system from
  `stalker.protected_areas.*`** (18 lines, already fully covered part 119 as a single generic
  overlay/notification/owner system). The standalone ones give each faction its own
  in-character voice for the same "you're trespassing" beat: bandit ("Oi, dumbass! You got the
  wrong base or something?"), covenant ("Unbeliever! You are on Covenant land!\nTurn aside, or
  feel the divine wrath!"), duty, freedom, merc all distinct. Confirmed via direct content
  comparison, not assumed from the shared `protected_areas` substring. This is a strong
  candidate for the next concrete part: small (25 lines), self-contained, high narrative value
  (five factions' worth of voice-acted-style warning lines in one place).
- **`frontlineline` (12 lines) vs. `frontline` (192 lines)** are two genuinely different real
  content pockets that happen to share a look-alike prefix pair in the client's own key naming
  (not a data error on this project's part to "fix"): `frontlineline.intro.*` is real
  Frontline-mode campaign narrative text (multi-paragraph in-universe history), while
  `frontline.calldown.*` is a real airstrike/artillery item-name list (SC-50, SC-250, SD-4-HL,
  etc, most with empty `.desc` values). Disclosed as an odd but real client naming coincidence.

## The uncovered list (prioritized by line count, real content sampled for every entry)

Large, clearly worth a dedicated class:
- `settlement` (202) -- settlement-perk descriptions, see above.
- `checkpoints` (202) -- real per-dungeon checkpoint/objective labels (e.g.
  `checkpoints.dungeon_spusk.entry`="Entrance").
- `core` (197) -- a real weapon-handbook category label system (`core.handbook.category.*`,
  e.g. "Assault Rifles", "Shotguns and Rifles").
- `map` (193) -- real map-page display names (`map.page.id.<id>.name`).
- `frontline` (192) -- real airstrike/calldown item names, see above.
- `ejection` (188) -- real "Emission"-style radioactive-fallout event announcer lines.

Medium, real standalone systems:
- `world` (138) -- real named "world modifier" buff/debuff descriptions tied to the Arena
  (`world.modifier.ravent_level_*`).
- `phonograph` (135) -- real per-location vinyl-record/collectible titles (an audio-log-adjacent
  collectible system, e.g. `phonograph.vinyl_location_bolota.title`="The Swamps").
- `tile` (114) -- real numbered flooring/material tile display names (a level-design asset
  labeling system, e.g. `tile.3500.name`="Stones (Floor)").
- `bpseason` (98) -- real Battle Pass season event copy (confirms "Arena of Slavs" as a real
  spring-2024/25-era Battle Pass season name, corroborating part 121's shop-season-name find).
- `party_missions` (95) -- real DMZ-mode co-op mission task labels.
- `ravent` (87) -- real Arena-mode wave/round names ("Baptism of fire", "Lizard Invasion").
- `prestige` (79) -- real bar-reputation-gated feature-lock messages; possibly ties to the
  file-based `PrestigeAbility` (part 45?) -- not yet checked in detail.
- `dmz` / `dmz2026` (78 / 17) -- real DMZ-mode ability name/description text (near-identical
  "Hook" grappling ability text appears under both `dmz.ability.active.*` and
  `dmz2026.ability.active.*` -- likely successive season revisions of the same mode, worth an
  explicit compare when built).
- `delivery` (65) -- real delivery-quest destination labels reusing existing location names.
- `orders` (64) -- real bounty/order task labels (Murder, Gathering supplies).
- `modules` (59) -- real weapon-attachment "Module" stat-tradeoff descriptions; likely ties to
  part 113's `OperationsCatalog` module-crafting system, worth an explicit compare when built.
- `item_origins` (55) -- real item-source category labels ("home furniture", "military objects").
- `seasonpass` (49) -- real Battle Pass UI copy (trader name, tier labels).

Small, real, worth grouping into a shared "misc small namespaces" class rather than one class
each: `savepoints`(41, respawn-point display names, reusing location names again),
`premium`(38, premium-subscription perk copy), `arcade`(36, Arcade-mode rank-tag text),
`seasons`(29, a month-by-month event-name index back to Nov 2023), `encaged`(29, top-level
Encaged-mode class names, distinct from already-covered `gui.encaged.*`), `hint`(24, standalone
signal-related combat hints), `gameobjects`(21, quest-object interaction-verb tooltips, en.lang
text, distinct from the file-based `GameObjectsCatalog`), `incident`(20, care-package loot-list
descriptions), `snowstorm`(18, a Blizzard/snowstorm-event announcer, thematically paired with
`blizzard` below), `industries`(18, a settlement "logistics" upgrade-tier benefit list),
`challenge`(17, singular -- a distinct real id space from plural `challenges`, e.g.
`challenge.wild_north/1.name`), `arena`(17, Arena-mode map display names), `inside`(16,
"Inside Out" event ability text), `blizzard`(16, the winter-storm event announcer lines),
`outpost`(14, capture-point attacker/defender status text), `ny`(34)/`ny_toys`(13, New Year
leaderboard and ornament-set copy), `season`(12, singular -- per-season tab/title strings,
distinct from `shop.season.*` and `seasons`), `personal_storage`(11, reputation-gate messages
reusing settlement/location names), `profile_icon`(10, social-platform icon labels: Telegram,
YouTube), `banner`(9, a barter/modification-rework announcement banner), `territories`(8, real
mutant-territory names e.g. "Skitter Territory", "Boar Territory"), `provision`(7, faction-supply
depot names), `mail`(6, an in-game mail-message template for clan season-reward payouts),
`entity`(5, two named mutant/boss display names), `item_tier`(4, item-tier display names:
Newbie/Stalker/...), `anomaly`(4, a noo-converter/chronodevice placement notification popup),
`barter`(2, two barter-category labels), `menulinks`(3, main-menu external link labels),
`death`(3, real kill-feed death-cause text), `review`(2, an in-game review-prompt popup),
`main_menu_popup`(2, a real limited-time item-removal warning banner), `tutorial`(1, a single
medkit-use hint line).

The five per-faction protected-area systems (`merc`/`freedom`/`duty`/`covenant`/`bandit`, 5 lines
each) are called out separately above as the most narratively interesting small find and a good
next-part candidate.

A further ~15 single-line "prefixes" exist below this (`signals`, `signal_wnorth`,
`signal_sequence`, `signal_sequence_dungeon_key`, `signal_sequence_beacon_key`, `signal_dungeon`,
`signal_chrono`, `polygon7_hint`, `player_effect`, `filler`, `effect`, `toxic_dogout_hint1`,
`subtitles`, `stalkerclans`, `bonuscode` (5 lines total, real donor-appreciation "bonus code"
redemption message copy, includes a real "since 2016 devs" self-reported anniversary claim
worth noting but not verifying) -- not worth individual classes; candidates for a shared small
grab-bag class if ever built, low priority given how little real gameplay-relevant content each
carries.

## Confirmed already covered (no action needed)

`item`(121), `go`(120), `stalker`(119), `general`(118), `quest_contract`(117), `challenges`(116),
`shop`(115), `we`(111), `gui`(122+113), `quote`(112), `cnps`(107), `pda`(114, partially --
295/301 duplicate of `WorldEventCatalog`), `currency`(109), `loadscreens`(110), `hideout`(106),
`clans`(108), `craft_recipe`(106), `battlefield`(pre-106), `location`(105, partially -- `.name`
only), `operation`(67 lines -- NOT covered, see below), `ops`(10 lines -- NOT covered, see below).

Correction to two items initially assumed covered by name similarity, then explicitly checked and
found NOT covered: standalone top-level `operation.*` (67 lines, real Operations-mode player CLASS
descriptions -- Assault/Support/Operative playstyles) is distinct from `gui.operation.how_to_play.*`
(already covered part 113's `OperationsCatalog`, which is tutorial/lore text, not class
descriptions). Standalone top-level `ops.*` (10 lines, real Operations league/rank names --
"Absolute Unity", "Ascendant", "Revenant", etc, plus per-rank reward flavor text) is distinct from
`go.ops.<id>` (already covered part 120, which are Operations-mode battery/objective prop ids, not
league names). Both `operation` and `ops` are added to the uncovered list above (grouped with
`core`/`map`/`item_tier` bucket by size).

## Context for future parts

This part deliberately writes no Java code -- it mirrors part 90's own original role (a survey,
not a namespace model) per part 122's own closing note. The next concrete part should pick from
the uncovered list above. Best next candidates, in rough priority order: (1) the five per-faction
protected-area warning systems (`merc`/`freedom`/`duty`/`covenant`/`bandit`, 25 lines total,
small and self-contained, strong narrative payoff, natural to model as one class given the shared
shape); (2) `settlement.id.*` (202 lines, single generic `(id, field)` table, same shape as prior
`*LangCatalog` classes); (3) `checkpoints`/`core`/`map`/`frontline` (192-202 lines each) as a
follow-up batch of large single-shape namespaces.

## Memory-store note

Continuing the `Memory/` folder as the active store (project memory via the MCP tool remains
unavailable; not re-checked this part, per the user's standing instruction to just use this
folder going forward). This is `cont79`, following directly from `cont78` (part 122).
