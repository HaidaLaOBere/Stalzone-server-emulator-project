# Part 132 -- `RaventCatalog`: real Slavic-folklore Arena-mode player abilities and enemy wave names

`ravent` was flagged in part 131's own closing note as a natural next candidate given its direct
tie to part 130's `WorldModifierCatalog` `ravent_level_*` finds. This part models the real
87-line `ravent.*` namespace -- the real player passive-ability roster and enemy wave/round-name
roster for the same real Slavic-folklore Arena mode already surfaced twice before from
independent angles: part 130's `WorldModifierCatalog` `ravent_level_*` spawn-rate modifiers, and
part 131's `BpSeasonCatalog` `24/spring` "Arena of Slavs" seasonal event (Max Crow, the idol of
Perun, bogatyrs). This is the third and richest angle on that same real mode -- its actual named
enemy waves and player perks.

## Real shape

Confirmed by direct count to be a single real group with zero flat exceptions: all 87 lines fit
`ravent.<id>.<field>` via the same last-dot-split generic scheme already used throughout this
project.

`ability.passive.<abilityId>.name`/`.desc` (22 lines, 11 real named player passive perks):
`gladiator_health`="Life-Giving Cross", `gladiator_bullet_dmg`="Sturdy", `gladiator_speed`=
"Shining Heels", `gladiator_damage`="Bogatyr Strength", `gladiator_regen`="New Skin",
`gladiator_reload`="Wondrous Quiver", `gladiator_coins`="Poor Man's Bargain",
`gladiator_sickness`="Viper's Blood", `gladiator_bleeding`="Enchanted Cloth",
`gladiator_attack`="For Homeland-Mother!". Each `.desc` carries real literal tiered-scaling
placeholder syntax verbatim (e.g. "increases by %1%10§r/%2%20§r/%3%30§r units"),
not normalized or parsed further by this class.

`waveTypes.<waveId>.name` (65 lines, flat) is a real named enemy-encounter roster: a tiered
"Nth_base_n"/"Nth_boss_n" ladder across 5 real difficulty tiers (first/second/third/fourth/
fifth), plus 16 real named special "boss_<name>" encounters drawn from genuine Slavic folklore --
`boss_perun`, `boss_hors`, `boss_stribog`, `boss_leshiy`, `boss_kikimora`, `boss_tugarin`,
`boss_gorynych`, `boss_kojemyaka`, `boss_werewolf`, `boss_wolf`, `boss_adepts`, `boss_altushka`,
`boss_boar_flesh`, `boss_durak`, `boss_fedot`, `boss_spirit` -- alongside comedic tier-ladder
wave titles ("Slavic Chainsaw Massacre", "Battle of Dogs", "Green serpent", "Pigstorm", "Lads
from the woods", "Fairytale brawl", "Baikal shootout").

## Testing

3 new `RaventCatalogTest` tests against a fixture built from real key=value lines drawn directly
from the live client's `en.lang`: a real passive ability's name/desc resolving together, real
named Slavic-folklore boss and tier-ladder waves resolving independently, and sorted/null-safe
id and field lookups. Compiled with `--release 11`, run clean in the cloud (963/963) and, synced
across via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE
(963/963).

## Context for future parts

Two more down from part 123's long tail (this part plus part 131), and the Arena-of-Slavs/
`ravent` thread is now covered from all three of its independent angles (modifiers, seasonal-
event announcement, actual abilities/waves). What remains: `party_missions` (95), `prestige`
(79), `dmz`/`dmz2026` (78/17, ability text -- still flagged for an explicit
near-identical-"Hook"-ability compare when built), `delivery` (65), `orders` (64), `modules`
(59), `item_origins` (55), `seasonpass` (49), and the long tail of smaller namespaces documented
in part 123's own writeup (`Memory/recon_findings_cont79.md`).
