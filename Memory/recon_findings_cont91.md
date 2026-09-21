# Part 135 -- `DmzLangCatalog`: `dmz`/`dmz2026` catalogued together, the flagged "Hook"-ability compare performed

Continuing top-down from part 134's closing note, this part models the real `dmz.*` (78 lines)
and `dmz2026.*` (17 lines) namespaces TOGETHER in one class, finally performing the direct
"Hook"-ability text compare part 119's `StalkerLangCatalog` explicitly flagged for once both
were built.

## Real shape

Both confirmed by direct count to be single real groups with zero flat exceptions, modeled with
two SEPARATE generic last-dot-split tables (not merged into one, since they are two real distinct
top-level prefixes).

`dmz.<id>.<field>` (28 `name` + 22 `description` + 14 `hint` + 14 `header` = 78 exactly) splits
into `ability.*` (44 lines: one real human "soldier" ability, `active.soldier_hook`="Hook", plus
a full 21-ability real "zombie" playable-class kit -- 12 active, 9 passive -- tying to part 133's
`PartyMissionsCatalog` `kill_entity_living.zombies`/`dmz_zombie_scenario` ids), `leagues.<n>.name`
(6 lines, a real previously-uncatalogued DMZ ranked ladder: "Baron's Guard", "Market Elite",
"Market Agents", "Black Raiders", "Operatives", "Recruits"), `tutorial.<id>.header`/`.hint` (28
lines, 14 real tutorial tips with embedded `[map:waypoint_*]` tags).

`dmz2026.<id>.<field>` (7 `description` + 2 `title` + 2 `name` + 2 `desc` + 4 single-digit level
labels = 17 exactly) splits into `ability.active.hook.*` (the real 2026-generation player Hook
ability), `calldown.respawn.name` (1 line, "Reinforcement"), `industry.equipment.level.<n>` (4
flat "LV n" labels), `trader_terminal_requirement.level_<n>.description` (4 lines),
`chest.requirement.<tier>.description` (2 lines), `game_mode.<id>.title`/`.desc` (4 lines, 2 real
modes: `solo`="SCOUT", `party`="TROOPER GROUP").

## The flagged Hook compare

Three real "Hook" grappling-ability texts exist across the two namespaces.
`dmz.ability.active.soldier_hook` and `dmz2026.ability.active.hook` are BYTE-IDENTICAL ("You
throw a grappling hook in the direction of view. When it latches onto a surface, the hook pulls
you in.") -- real evidence the 2026 player-facing Hook ability is a direct carryover of the
original `dmz` soldier ability, not a redesign. `dmz.ability.active.zombie_hook` is a real
NEAR-identical but NOT byte-identical variant ("in the direction of your view" -- an extra "your"
-- vs. "in the direction of view" for the other two) -- a genuine minor real client text
inconsistency between the human and zombie versions of the same ability, disclosed as-is per rule
1, not corrected or merged.

## Testing

3 new `DmzLangCatalogTest` tests against a fixture built from real key=value lines drawn directly
from the live client's `en.lang`: the real soldier_hook/dmz2026-hook descriptions resolving
byte-identical, the real zombie_hook description resolving as a near-identical but distinct
variant, and a real league name plus a real dmz2026 game mode resolving with sorted/null-safe id
and field lookups. Compiled with `--release 11`, run clean in the cloud (972/972) and, synced
across via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE
(972/972).

## Context for future parts

Five more down from part 123's long tail (parts 131-135), and the part-119-flagged Hook compare
is now resolved. What remains: `delivery` (65), `orders` (64), `modules` (59), `item_origins`
(55), `seasonpass` (49), and the long tail of smaller namespaces documented in part 123's own
writeup (`Memory/recon_findings_cont79.md`).
