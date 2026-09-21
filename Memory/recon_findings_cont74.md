## Part 118 — `GeneralCatalog`: a real 558-id achievement title/description system, plus platform-region and seasonal-stat label groups

**Part 118 -- `GeneralCatalog`: a real 558-id achievement title/description system, plus
platform-region and seasonal-stat label groups.**

- Investigated `general` (1,121 lines), the next entry down part 90's original "top 15 by count"
  `en.lang` namespace list after part 117's `quest_contract`.
- Direct count confirmed the namespace splits cleanly into exactly three real groups with no
  leftover lines: `achievement` (1,116 lines), `region` (4 lines), `stats` (1 line); 1,116 + 4 + 1 =
  1,121.
- `general.achievement.id.<id>.title`/`.desc` (1,115 of the 1,116 achievement lines): a real
  achievement system, 558 distinct real string achievement ids. Direct count confirmed `title`
  (558 lines) and `desc` (557 lines) are the ONLY two real field suffixes used in this group.
- Many achievement ids are real zone-exploration achievements whose ids/titles tie directly to zone
  codes already modeled by `LocationNameRegistry` (part 105): `bolota_explored`="Beloved Marsh",
  `kordon_explored`="Road of Life", `svalka_explored`="Radioactive Arena",
  `agroprom_explored`="Mugging Institute", `td_explored`="Mysterious Outskirts",
  `dt_explored`="Evil Pervomaisky Factory", `bar_explored`="Concrete Oasis",
  `bar100_explored`="Private Club", `yantar_explored`="Mind-Blowing Hole", `les_explored`="Calm Fog".
  Disclosed as a real cross-reference opportunity for a future part, NOT re-modeled or further
  pursued this part.
- One real id genuinely has a `title` but no matching `desc`: `legend`="Legend of the Zone" --
  confirmed by DIRECT GREP against the live client `en.lang` (not a `comm`/diff-tool artifact) that
  no `general.achievement.id.legend.desc` line exists anywhere in the file. Disclosed as a real,
  minor, flat/incomplete achievement entry. No special-casing was needed in code -- the generic
  `(id, field)` accessor already returns null for any missing field.
- A real compound id explicitly checked because it could have been mistaken for a second such
  discrepancy: `dmz_2026_quest.tankiWatch` is a genuine CHILD id with its own complete title/desc
  pair (`title`="40 Tons", `desc`="Find all tanks on Boral"), fully distinct from its
  similarly-named PARENT id `dmz_2026_quest` (`title`="Bottom", `desc` starting "Congratulations!
  You found a unique murmurer residing in a marine environment! How did it get there...?"). Both
  pairs are complete. The last-dot split (first established part 117) handles this correctly with
  no special-casing.
- `general.achievement.category.*` (the remaining 1 of the 1,116 lines): a real, separate flat
  label map with only one real entry today, `quest`="Quest".
- `general.region.*` (4 lines, complete, unrelated to achievements): a real flat map of
  platform/publishing region labels -- `eu`="Europe", `na`="North America", `sea`="Southeast Asia
  (Singapore, Australia)", `nea`="Northeast Asia (Korea, Japan)".
- `general.stats.*` (1 line, unrelated to achievements): a real flat map, keyed after stripping the
  `stat.` sub-group segment -- `raffle-tickets-ny22`="Participation Tickets found", a real seasonal
  (New Year 2022) event-currency display label.
- Doc-only: `LangCatalog`'s class doc marks `general` as now fully parsed.
- Code: new `GeneralCatalog.java` + `GeneralCatalogTest.java` (6 new tests), wired into
  `AllTests.java`/`sources.txt`. Compiled with `javac --release 11 -d out @sources.txt` in the
  cloud (zero warnings/errors), full suite run clean in the cloud (888/888), synced to the device
  and live-verified clean on the device's actual Java 11 JRE (888/888): test count 882 -> 888.
- Also fixed a real documentation gap found while doing this part's README update: `README.md`'s
  per-part bullet list had silently stopped after Part 112 (parts 113-117 only ever got their
  RESEARCH_LOG.md sections written, never their own README bullets, though the running
  "Runs all N tests" count line WAS kept current each time). Retroactively added the missing
  README bullets for parts 113-117 (condensed from their existing RESEARCH_LOG.md sections, no new
  claims), then added Part 118's own bullet, then bumped the running test-count line from 882 to
  888. Worth a note for whichever future part next touches `README.md`, in case this gap recurs.
- Remaining fully-uninvestigated top-15 `en.lang` namespaces after part 118: `stalker` (2,020),
  `go` (2,624), `item` (12,611, by far the largest, likely needing a dedicated multi-part effort).
  `gui` remains partially closed (only 191 of 395 lines done, at part 113).
