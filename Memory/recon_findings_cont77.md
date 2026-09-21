## Part 121 — `ItemLangCatalog`: the single largest namespace on part 90's original top-15 list (12,427 lines) — real display text for this game's entire item catalog, modeled with one generic table, zero flat exceptions

Part 121 investigated `item` (12,427 lines, direct count — part 90's original estimate of 12,611
had been approximate), the single largest namespace on part 90's original top-15 list. Named the
class `ItemLangCatalog` (not `ItemCatalog`/`ItemsCatalog`) to avoid confusion with several
existing, unrelated item-related classes: `ItemModelCatalog` (file-based 3D models, part 57),
`BarterItemCatalog`, `ItemGroups`, `ItemTiers`, `ItemWeightTable`.

New Java class `ItemLangCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`ItemLangCatalogTest.java` (6 tests).

- Direct count confirmed `item.*` is the THIRD namespace found (after part 116's
  ChallengesCatalog and part 120's GoLangCatalog) with ZERO real flat exceptions — every line
  fits `item.<id>.<field>`: 7,834 distinct real item ids, field distribution dominated by name
  (7,545) and description (2,345), long tail: additional_stats_tip (1,411), lore (392),
  short_name (223), item_origin_tip (179), case_description (115), dozens more one-offs.
- Real-world firearm names under wpn.<id>: kedr="PP-91 KEDR", mp5="HK MP5", uzi="IMI Uzi",
  aks74="AKS-74", ak74m="AK-74M".
- Real cosmetic/gear sub-families sampled but NOT exhaustively enumerated (single generic table
  needs no enumeration to be useful): camo (967, weapon skins), custom (805, cosmetics), arm (748,
  armor), charm (633), misc (619), att (489, attachments), style/motif (356/280, cosmetic
  categories), ny_toys (273, New Year seasonal line), lootbox (265), brt (256, barter items), plc
  (250), art (248, artifacts), bag (192, backpacks), med (183, medicine), wpn_mod (182), filler
  (171), exp (163, explosives — EXPLICITLY CHECKED and confirmed a real separate id space from
  part 120's go.exp.<id>, not assumed to be the same).
- Real deeply-nested custom.internal.<n1>.<n2>.<n3>.lore family — quest-reward-item flavor text
  (e.g. custom.internal.16022.12177.0.lore="Six! That thing has six eyes!...").
- Real item.customisation.collection.<seasonId> crosswalk (8 lines: summer24="ZIVCAS Corporation",
  winter26="Sub-Zero Zone", triumph26="Triumphs. Faction Race 2026", etc.) — corroborates the
  existing seasonal-event timeline (parts 109/115) from yet another independent angle.
- This class deliberately does NOT re-model the already-catalogued file-based item rosters
  (WeaponRegistry etc.) — it exposes real display text those classes don't carry, not a duplicate
  of their own id/stat data.
- Doc-only: `LangCatalog.java` marks `item` as now fully parsed.
- Code compiled with `javac --release 11 -d out @sources.txt` in the cloud (zero warnings/errors),
  full suite run clean in the cloud (909/909), synced to the device and live-verified clean on the
  device's actual Java 11 JRE (909/909): test count 903 -> 909.
- README.md/RESEARCH_LOG.md both updated on the device (README bullet + running count;
  RESEARCH_LOG got a full "## Part 121" section).

**Context for future parts**: this closes EVERY entry on part 90's original top-15 `en.lang`
namespace list except the still-partial top-level `gui.*` (204 of 395 lines remaining — distinct
from `stalker.gui.*`, already fully covered by part 119). A natural next step for a future part:
either finish `gui.*`, or move past part 90's original top-15 list entirely to survey what other
`en.lang` namespaces remain uncatalogued (part 90's list was itself a "top 15 by count" snapshot,
not an exhaustive list of every namespace in the file).

**Memory-store note**: continues the file-based Memory folder numbering (cont71-76 so far). See
`Memory/NOTE_ABOUT_THIS_FOLDER.md` for the full situation re: the MCP project-memory outage.
