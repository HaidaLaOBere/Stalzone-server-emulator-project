# Part 157 -- `NewYearEventCatalog`

## Real shape

The real `ny.*` (34 lines) and `ny_toys.*` (13 lines) namespaces, the
thirty-seventh/thirty-eighth concrete namespaces built from part 123's follow-up census, kept as
two separate tables in one class (same shape as part 135's DmzLangCatalog). `ny.*` reconciled
31 (generic table: leagues 18 + tutorial 10 + rewardmap 3) + 3 (flat carve-out) = 34. `ny_toys.*`
is 13 flat lines.

## Real finds

A real "X-Mas Tree 2025" seasonal event. 6-tier leaderboard ladder (Ice/Golden/Silver/Bronze/
Wooden/Snow) with a real client copy-paste bug: snow's reward_text is byte-identical to wood's,
disclosed not corrected. 5 tutorial cards (tree, decoration merging, Battle Pass, challenges,
Pine Cones currency). 13 real decoration set names (setA="Red Decorations" through setM, real
non-alphabetical ordering with setQ inserted out of sequence, skipping setI).

## Real corroborations, disclosed not merged

Third recurrence of the "leagues" ranked-ladder naming convention (after DmzLangCatalog part 135
and InsideCatalog part 154) -- independently-named each time, not merged. `ny_toys.*` set names
sit directly above part 121's ItemLangCatalog already-covered 273 individual
`item.name.ny_toys.<id>` item names -- a completion, not a new item roster.

## Testing

3 new `NewYearEventCatalogTest` tests against a fixture built from real key=value lines. Run
clean on both a plain and a `--release 11` build in the cloud and, synced across, on the device's
actual Java 11 JRE: 1035 -> 1038.

## Context for future parts

Thirty-eight namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `season`(12, singular, distinct from `shop.season.*` and
`seasons`), `personal_storage`(11, worth a quick disclosed-not-merged check against part 143's
PremiumCatalog `perk_block.personal_storage` field). After that, the smaller grab-bag:
`profile_icon`, `banner`, `territories`, `provision`, `mail`, `entity`, `item_tier`, `anomaly`,
`barter`, `menulinks`, `death`, `review`, `main_menu_popup`, `tutorial`, `bonuscode`, plus ~15
single-line "prefixes" not worth individual classes.
