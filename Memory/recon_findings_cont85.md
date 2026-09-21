# Part 129 -- `FrontlineCatalog`: the real Frontline SSPP-vs-Murmur war campaign

Sixth concrete namespace built from part 123's census, and the last of the four large candidates
from that survey. Models the real 192-line `frontline.*` namespace -- the real "Frontline"
large-scale SSPP-vs-Murmur war-campaign game mode. This is the most narratively significant
namespace this project has catalogued from a single `en.lang` prefix: 60 of its 192 lines are
full multi-paragraph campaign BRIEFING TEXT, not just short display labels.

## Real shape

Confirmed by direct count to split into 4 real groups with no leftover lines (60+129+2+1=192,
itself confirmed zero flat exceptions -- every line fits `frontline.<id>.<field>`):
`briefing_<series>_<n>.text` (60 lines, 4 real series -- `s` and `s2` [10 and 20 entries], `n` and
`n2` [10 and 20 entries], read as two campaign directions, South and North, each with two
escalating phases), `calldown.<itemId>.<field>` (129 lines, 40 real airstrike/ordnance items),
`calldown_quest_dmz26.name`/`.desc` (2 lines, one real DMZ-2026-tie-in quest, "Thousand Leagues
Cargo 200" -- distinct from the `calldown.*` item roster, its own real id), `bonus_event.
completion` (1 line, a real squad-credited bonus-payout message template).

## Real narrative payoff

The 60 real briefing lines are a substantial, previously-uncaptured piece of this project's
running SSPP/Murmur conflict thread (parts 105/107/109/123/126): they name, corroborate, or
extend several already-confirmed real locations and threads from an independent source -- the
Swamps, Roadside, Fire Station, School, Radar Station bunker, `Lyubech-3`, `Boral` (both from
part 126's `MapLangCatalog`), and `Chernobyl-4`/`Ch-4` (the same real former SSPP staging ground
part 126 disclosed) -- and introduce two real named elements not seen anywhere else in this
project before: a named military operation, "Operation Pole," and two real named archianomalies,
"Sauna" and "Evil Well." Consistent with this project's Reichstag-corroboration discipline, these
are disclosed as further real entries in an existing thread, not treated as newly "confirming"
anything that needed confirmation.

## Calldown roster

`calldown.<itemId>.<field>` (40 real items, e.g. `fab100`, `rbk250`, `zab100`, `pfm1`, `helic5`,
`flamethrower`, `m249`) gives real airstrike flavor text (`desc`) and short real property tags
(`prop1`-`prop4`, e.g. "HIGH DAMAGE", "PRECISION TARGETING", "LARGE COVERAGE AREA") -- not every
item has every field (39 of 40 have a `name`; only 2 real items, `flamethrower` and `m249`, carry
a fourth property).

## Testing

6 new `FrontlineCatalogTest` tests against a fixture built from real lines drawn directly from
the live client's `en.lang` (briefing paragraphs truncated to their real opening sentence for
fixture brevity): two briefing series resolving in index order and staying genuinely separate, a
real calldown item's full field set, a real item missing its own name field resolving null rather
than a fallback, the DMZ26 tie-in quest, the bonus-event completion template, and sorted/null-safe
id lookups. Compiled with `--release 11`, run clean in the cloud (948/948) and, synced across via
the usual tarball pipeline, live-verified on the device's actual Java 11 JRE (948/948).

## Context for future parts

Six for six from part 123's census, and all four of its large "worth a dedicated class" candidates
now modeled. What remains from that survey is its long tail of medium/small namespaces: `world`
(138), `phonograph` (135), `tile` (114), `bpseason` (98), `party_missions` (95), `ravent` (87),
`prestige` (79), `dmz`/`dmz2026` (78/17, ability text -- flagged for an explicit near-identical-
"Hook"-ability compare when built), `delivery` (65), `orders` (64), `modules` (59), `item_origins`
(55), `seasonpass` (49), and a further tail of smaller ones documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`). A natural next step is either to keep working this list
top-down (`world` next by size) or to batch several small related ones together in a single part,
as this project has occasionally done before (e.g. parts 118/119 covering `general`/`stalker`
together).

## Memory-store note

`cont85`, following directly from `cont84` (part 128). `Memory/` remains the active store; MCP
project memory not re-checked this part.
