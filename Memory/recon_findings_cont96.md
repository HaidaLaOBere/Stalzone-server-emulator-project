# Part 140 -- `SeasonPassCatalog`

## Real shape

The real `seasonpass.*` namespace (49 lines), the twentieth concrete namespace built from part
123's follow-up census, modeled with the established generic last-dot-split (id, field) table.
Reconciled exactly: 12 real `task.category.<id>.desc` + 9 `.completionTextInfo` + 6
`.conditionTextInfo` + 4 `title` + 4 `description` + 14 single-occurrence fields (the 12 real
`task.category.<id>` leaf LABELS themselves, plus `season_trader.name` and
`daily_bonus.description.summer25.1`) = 49.

## The real task.category double-use quirk

Same shape already handled cleanly (no special-casing needed) in part 137's `OrdersCatalog`
`map_marker.title`: `seasonpass.task.category.base`="BASIC OPEN WORLD ACTIVITIES" is
simultaneously a leaf VALUE (id=`task.category`, field=`base`) and a PREFIX for
`seasonpass.task.category.base.desc` (id=`task.category.base`, field=`desc`). All 12 real task
categories: `base`="BASIC OPEN WORLD ACTIVITIES", `worldevent`="CLEARING EVENTS",
`hard`="COMPLEX ACTIVITIES", `battlefields`="SESSION BATTLES", `clans`="SUBDIVISION BATTLES",
`event`="FROST TROOPERS", `wildnorth`="WILD LOCATIONS", `operations`="OPERATIONS",
`ny26`="X-MAS TREE", `insideout`="INSIDE OUT PERCOLATION", `reichstag`="ASSAULT ON THE REICHSTAG",
`ssop_help`="SSPP CONTRACTS".

## Cross-namespace corroborations (disclosed, not merged)

- `event`="FROST TROOPERS" directly corroborates part 135's `DmzLangCatalog` `dmz2026.*` and part
  138's `ChallengesCatalog` "DMZ26/'Frost Troopers'" find.
- `reichstag`/`reichstag.desc` are further uncounted entries in the long part-109 Reichstag
  corroboration chain (also reconfirmed by `StalkerLangCatalog`'s
  `stalker.gui.seasonpass.event.reichstag.industry.*` reward-UI find).
- `ny26`="X-MAS TREE" ties to part 139's `ItemOriginsCatalog` `ny_tree_reward` find.
- `operations.conditionTextInfo`="GET PHIL'S QUEST AT THE BAR" names a real NPC "Phil" -- checked
  via grep across every existing `world.props` catalog, found nowhere else. Genuinely new, not yet
  corroborated.
- `insideout.desc` and `clans.desc` are byte-identical real text -- a real client copy-paste
  artifact, disclosed as-is, not corrected.

## A real narrative finding

`event.title`="ATTACK ON PERIMETER" and its `event.description` read as a climactic beat for the
long-running SSPP/Murmur conflict thread (parts 105/107/109/123/126/129/131/135): "The defense of
the Perimeter is over. After prolonged combat, the Murmur vanished as swiftly as appeared... a
huge crater remained where the military town once stood... No one knows what will happen next. Is
this the end of the story or a lull before something bigger?" Flagged for future parts as a
possible conclusion/pivot point, not asserted as definitive since the client text itself leaves it
open.

## Other real finds

The real `bp.option.<id>.title`/`.description` group (3 ids: `base`, `bonus50`, `bonus20`) is the
Season-Pass-purchase-tier UI text. `season_trader.name`="Exchanger" is a real NPC/vendor name tied
to the Season Pass currency exchange flow. `daily_bonus.description.summer25.1` is a real seasonal
(summer25) daily-bonus tooltip, its trailing literal digit `1` landing as an ordinary field name
under the generic scheme.

## Testing

3 new `SeasonPassCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`. Run clean on both a plain and a `--release 11` build in
the cloud and, synced across, on the device's actual Java 11 JRE: 984 -> 987.

## Context for future parts

Twenty namespaces down from part 123's follow-up census. What remains is the long tail of smaller
namespaces documented in part 123's own writeup (`Memory/recon_findings_cont79.md`). The "Phil" NPC
find and the "ATTACK ON PERIMETER" narrative beat are both worth a corroboration check if any
future namespace surfaces related text.
