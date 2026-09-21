# Part 164 -- `OddsAndEndsCatalog`

## Real shape

The real ~15 remaining single-line "prefixes" from part 123's follow-up census (14 real lines
found and modeled), closing that census out in full.

## Codebase verification before writing

Directly checked the codebase (not the stale part-123 writeup) for the large/medium namespaces
it had flagged "uncovered": settlement, checkpoints, core, map, frontline, world, phonograph,
tile, bpseason, ravent, prestige, modules, and the five per-faction protected-area systems --
ALL already covered (SettlementPerkCatalog, CheckpointsCatalog, GloomycoreCatalog, MapLangCatalog/
PdaMapCatalog, FrontlineCatalog, WorldModifierCatalog, PhonographCatalog, TileCatalog,
BpSeasonCatalog, RaventCatalog, PrestigeLangCatalog, ModulesCatalog, FactionBaseWarningCatalog).
Confirms the census was fully worked through across parts 124-163; only true single-line
leftovers remained.

## Real finds

A 6-line "signal" PDA marker-name family; signal_chrono="Chronorift" ties to part 162's
WorldFlavorCatalog anomaly_chrono text. A coherent 3-line questline snippet naming new NPCs
"Burn" and "Art", confirming part 26's MilestoneMap dungeon_toxic_dogout zone. player_effect.
hunger="Hunger" (new). effect.category.inside.phantoms shares the "phantom" theme with part
154's InsideCatalog. stalkerclans.battlefields.no_access is the first real en.lang TEXT for the
clan-war mode (assets already covered by part 72's StalkerClansCatalog, a separate namespace). A
real orphaned bare filler.wild_mining_5.additional_stats_tip key sits apart from part 121's
item.filler.wild_mining_5.name for the same item id -- disclosed not corrected.

## Testing

3 new `OddsAndEndsCatalogTest` tests. Run clean on both a plain and a `--release 11` build in
the cloud and, synced across, on the device's actual Java 11 JRE: 1056 -> 1059.

## Context for future parts

Part 123's follow-up census is now COMPLETE -- 56 concrete namespaces built across parts
124-164, every real namespace it identified has a dedicated class or a documented merge. Future
en.lang work needs either a fresh full-file re-scan for anything the original census missed, or
a shift to a different confirmed-but-uninvestigated asset area (file-based assets, other
lang files, or protocol work gated behind explicit user go-ahead), per this project's broader
standing goal of full server-emulator coverage.
