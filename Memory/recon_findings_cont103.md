# Part 147 -- `HintCatalog`

## Real shape

The real `hint.<id>.header`/`.text` namespace (24 lines, 12 ids), the twenty-seventh concrete
namespace built from part 123's follow-up census -- real in-world contextual hint-card copy.
Modeled with the established generic last-dot-split (id, field) table.

## Real finds

Real key-binding tutorial prompts (`pda`/`bolt`/`wpn_mod`/`personal_storage`), each referencing a
real `[key:...]` client keybind-template token. `signals_Zheltuha.header`="Fight without
firearms!" ties to the real "Zheltuha" location, already confirmed by `StalkerSoundMiscCatalog`'s
real `zheltuha/` sound-asset folder -- a further corroboration. Three real ids
(`destroy_wood_board`, `search_stash`, `destroy_lock`) share the literal header "Tip", disclosed
as-is. Several body texts use a literal em dash with no following space, preserved verbatim.

## Testing

3 new `HintCatalogTest` tests against a fixture built from real key=value lines. Run clean on both
a plain and a `--release 11` build in the cloud and, synced across, on the device's actual Java 11
JRE: 1005 -> 1008.

## Context for future parts

Twenty-seven namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `gameobjects`(21, en.lang text distinct from the file-based
GameObjectsCatalog), `incident`(20), `snowstorm`(18), `industries`(18), `challenge`(17, singular,
distinct from plural `challenges`), `arena`(17), `inside`(16), `blizzard`(16), `outpost`(14,
check for overlap with existing `HomeOutpostCatalog`), `ny`(34)/`ny_toys`(13), `season`(12,
singular), `personal_storage`(11).
