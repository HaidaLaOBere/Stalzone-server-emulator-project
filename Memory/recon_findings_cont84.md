# Part 128 -- `HandbookCatalog`: the real in-game Handbook wiki feature

Fifth concrete namespace built from part 123's census. Models the real 197-line `core.*`
namespace -- almost entirely a real in-game "Handbook" help/wiki feature (`core.handbook.*`, 196
lines), plus one real stray tooltip line under a differently-misspelled top-level id
(`core.tooltop.*` -- a real client typo for "tooltip", disclosed as-is).

## Real shape

`core.handbook.*` splits into 6 real groups with no leftover lines (94+71+18+6+6+1=196):
`id.<entryId>.title`/`.main` (94 lines, 47 real wiki-style entries), `category.<path>` (71, flat
category display labels, several real paths nested several dots deep, e.g.
`category.articles.article.arthunt`="Zone Phenomena"), `tag.<tagId>` (18, flat filter-tag labels:
GENERAL, ZONE, SURVIVAL, PvP, RESOURCES, BARTERING, TRADING, GROUP ACTIVITIES, RESEARCH, ARTHUNT,
CRAFTING, THREATS, OPEN WORLD, TASKS, FACTIONS, SEASON, CHARACTER PROGRESSION, plus a real
leftover `testtag`="TEST TAG"), `subcategory.<path>` (6, flat, only under the `articles.article.*`
category tree), `report.option_<n>` (6, flat -- real "report this article" feedback reasons: too
complicated, missing info, outdated, formatting issues, factual errors, overloaded with text),
`notification.custom_1`="Custom notification description" (1, a real but generic-sounding
placeholder-style string, disclosed as-is).

## Real content -- a mutant bestiary hiding inside a help menu

The 47 real `id.<entryId>` wiki entries are a mixed bestiary-plus-mechanics glossary, each with a
`title` and a full `main` body (often several paragraphs). Confirmed real titles for the
mutant-bestiary-looking ids: `boar`="Boar", `chimera`="Fiend", `krovosos`="Ghoul" (this client's
own English title for what the id's Russian root literally means "bloodsucker" -- a real
localization choice, not this project's translation), `snork`="Skitter" (directly corroborating
part 123's census find `territories.snork.name`="Skitter Territory"), `tushkan`="Rat", `psidog`=
"Psy-Hound", `pseudogigant`="Behemoth", `pseudodog`="Wild Hound", `dog`="Mutt", `kisel`="Aspic".
One non-mutant entry sampled in full: `voronka`="Whirlpool", a real gravitational-anomaly lore
entry with a full multi-paragraph description and a listed real artifact roster (Sponge, Prima,
Shrimp, Golden Prima, Amberite, Cryogen, Dark Crystal).

`core.tooltop.locitem.loc_type.frontline`="Conflict zone" is the one real line outside
`handbook.*` -- a location-type tooltip label for the Frontline game mode, filed under a real
client typo (`tooltop`, not `tooltip`).

## Testing

5 new `HandbookCatalogTest` tests against a fixture built from real lines drawn directly from the
live client's `en.lang`: the snork/voronka bestiary and lore entries, nested category/subcategory
paths, tag/report/notification flat entries, the `core.tooltop.*` typo line, and sorted/null-safe
id and path lookups. Compiled with `--release 11`, run clean in the cloud (942/942) and, synced
across via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE (942/942).

## Context for future parts

Five for five from part 123's census. One large candidate remains from that same list: `frontline`
(192 lines, a mixed namespace blending `calldown.*` airstrike items with separate
campaign-briefing text -- the most structurally complex of the four large candidates and worth a
closer sampling pass before modeling). Beyond that, the census's long tail of medium/small
namespaces (`world`, `phonograph`, `tile`, `bpseason`, `party_missions`, `ravent`, `prestige`,
`dmz`/`dmz2026`, `delivery`, `orders`, `modules`, `item_origins`, `seasonpass`, and more) remains
untouched.

## Memory-store note

`cont84`, following directly from `cont83` (part 127). `Memory/` remains the active store; MCP
project memory not re-checked this part.
