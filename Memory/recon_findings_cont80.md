# Part 124 -- `FactionBaseWarningCatalog`: five per-faction trespass-warning voice lines

First concrete namespace class built from part 123's census. Models five real, separate
top-level `en.lang` prefixes: `merc.protected_areas.alert.notification.<n>`, `duty.*`,
`freedom.*`, `covenant.*`, `bandit.*` -- 5 real lines each, 25 total, each faction's own
in-character guard warning for trespassing near its base, indexed 0-4.

## Confirmed distinct from the already-covered generic system

`StalkerLangCatalog`'s `stalker.protected_areas.*` (part 119, 18 lines) is a single GENERIC
overlay/notification/owner system shared across 12 real protected areas regardless of controlling
faction. These five standalone prefixes are a different, richer real system: each faction gets
its own voice for the same "you're trespassing" beat. Confirmed via direct content comparison
(not just id-name similarity) before concluding they were distinct, per this project's standing
near-miss-checking discipline.

## Real content -- faction-name corroboration

Four of the five ids independently corroborate `SettlementRegistry`'s (part 105) real
display-name crosswalk from a second angle:
- `merc` = "Mercenary Camp" -- guard line: "You're approaching the Mercenary base, stranger."
- `duty` = "Frontier Base" -- guard line: "Frontier base perimeter breached."
- `freedom` = "Rise Base" -- guard line: "You're not respecting Rise's boundaries."
- `covenant` = "Covenant Abode" -- guard lines lean hard into an ecclesiastical register:
  "Unbeliever! You are on Covenant land!", "The Lord sees all!", "Repent and leave Covenant
  territory" -- consistent with `CnpsCatalog`'s (part 107) real Covenant reputation ladder
  (Acolyte through Apostle).

The fifth, `bandit`, has NO entry at all in `SettlementRegistry.ALL_KNOWN_SETTLEMENTS` --
bandits get their own real guard dialogue ("Oi, dumbass! You got the wrong base or something?")
despite apparently having no formal registered "settlement" in this client build. Disclosed as a
real asymmetry, not assumed to be an oversight on this project's part.

## Design

One flat `Map<String, List<String>>` keyed by faction id (`notificationsByFactionId`), since all
25 real lines share the exact same shape (`<factionId>.protected_areas.alert.notification.<n>`).
`KNOWN_FACTION_IDS` is the explicit 5-id list this project has confirmed carry this family --
no other top-level prefix in part 123's census matched this shape.

## Testing

4 new `FactionBaseWarningCatalogTest` tests against a fixture built from real lines drawn
directly from the live client's `en.lang`: merc's two lines resolve in index order, the other
four factions' distinct voices resolve correctly, `factionIds()` is sorted and reflects only
factions with real data, and an unknown/data-less faction id resolves to an empty list rather
than throwing. Compiled with `--release 11`, run clean in the cloud (920/920) and, synced across
via the usual tarball pipeline, live-verified on the device's actual Java 11 JRE (920/920).

## Context for future parts

Confirms part 123's survey was directly actionable -- this took one focused pass from "found in
the census" to "modeled and tested." Next candidates from that same survey, in priority order:
`settlement.id.*` (202 lines, single generic `(id, field)` table, same shape as the existing
`*LangCatalog` classes), then a follow-up batch covering `checkpoints`/`core`/`map`/`frontline`
(192-202 lines each).

## Memory-store note

`cont80`, following directly from `cont79` (part 123's survey). `Memory/` remains the active
store; MCP project memory not re-checked this part.
