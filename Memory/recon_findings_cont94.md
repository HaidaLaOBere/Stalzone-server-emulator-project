# Part 138 -- `ModulesCatalog`: a real weapon "module"/attachment perk description system

Continuing top-down from part 137's closing note, this part models the real 59-line `modules.*`
namespace -- a real weapon "module"/attachment perk description system.

## Real shape

Confirmed by direct count to split into 2 real shapes with no leftover lines (50 + 9 = 59):
`modules.<id>.description` (50 lines, real stat-affecting perks, e.g. `gyroscope`="significantly
reduces recoil buildup", `marksman`="increases damage to the head and limbs in exchange for
reduced damage to plate Durability", `acceleration`="increases damage of the first shots",
`finish`="increases damage of the last shots") and `modules.<id>.behaviour.description` (9 lines,
a real distinct sub-category of special on-hit bullet-effect modules -- `gas_bullets`,
`foam_bullets`, `tranquilizer`, `seaker`, `heal_bullets`, `blastrain`, `gravity_on_hit`,
`pyromaniac`, `lizard_strike` -- kept as their own real ids (e.g. `gas_bullets.behaviour`) by the
same generic last-dot-split scheme used throughout this project, with no special-casing
required).

## Real disclosed finds

A real recurring `_pre`/`_aff`/`_suf` affix family (e.g. `recoil_pre`/`recoil_aff`="reduces
recoil", `recoil_suf`="increases recoil"; `horizontal_recoil_pre`/`_aff`="reduces horizontal
recoil", `_suf`="increases horizontal recoil") reads as a real positive/negative modifier-text
pair for a procedural mod-naming system, disclosed as observed rather than interpreted further.
One real value, `ballista.description`="ballista 1", is disclosed as-is -- a literal
placeholder-looking client string, not corrected. The real ids `inside`/`inside2`
(damage-distribution modules) were checked against this project's already-confirmed "Inside Out"
seasonal event/mode and found to be a coincidental short-word match only, not a content overlap.

## Testing

3 new `ModulesCatalogTest` tests against a fixture built from real key=value lines drawn directly
from the live client's `en.lang`: a real plain-description module resolving, the real
`_pre`/`_suf` affix pair resolving as opposite real modifier texts, and a real
`.behaviour.description` on-hit module resolving as its own distinct id with sorted/null-safe id
lookups. Compiled with `--release 11`, run clean in the cloud (981/981) and, synced across via
the usual tarball pipeline, live-verified on the device's actual Java 11 JRE (981/981).

## Context for future parts

Eight more down from part 123's long tail (parts 131-138). What remains: `item_origins` (55),
`seasonpass` (49), and the long tail of smaller namespaces documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`).
