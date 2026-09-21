# Recon findings -- cont128 (Part 172)

## `world.props.OperationModeCatalog`

With part 168's three flagged leads now closed out (parts 169-171), this part extended the
fresh-re-scan methodology with a full top-level-prefix census against the current, complete
`en.lang` file: `cut -d= -f1 | sed 's#/#.#g' | awk -F. '{print $1}' | sort | uniq -c | sort -rn`,
covering all ~28,300 lines. Every top-level prefix from that census was cross-referenced against
existing catalogs by reading the actual covering class (not just a literal grep for the prefix
string), per this project's own documented false-negative caveat about dynamically-built prefixes.

**One genuinely new bare namespace surfaced: `operation.*`, 67 real lines** -- a real "Operations"
game mode: three real player classes, their real abilities, a handful of real named missions, and a
small set of real in-mission tasks.

**Confirmed genuinely distinct from the existing, confusingly-similarly-named `OperationsCatalog`
(part 111).** Direct inspection of that class's source showed its `PREFIX` constant is `"gui.
operation.how_to_play."` -- an entirely different, GUI-tutorial-tab real namespace -- plus a
`categoryLabel()` lookup that happens to resolve the literal string `"operation"` to "Operations
Mode" as one entry among several UI category labels. Neither parses or exposes any of this new
class's 67 real lines. Also confirmed: the real bare `operations.` (plural) prefix does not exist
anywhere in the client at all (zero real lines), so there's no risk of a plural-variant collision
either.

**Real content, by real sub-group:**
- `operation.classes.<classId>.<name|desc>` -- 3 real classes x 2 fields = 6 lines. Real ids/names:
  `combat`="Assault", `scientist`="Support", `combined`="Operative".
- `operation.abilities.<abilityId>.<name|desc>` -- 21 real abilities x 2 fields = 42 lines, the
  bulk of the namespace. Ability ids loosely group by class via naming convention (`ops_tank_*`/
  `passive_ops_tank_*` for Assault, `ops_ultimate_support_*`/`passive_ops_support_*` for Support,
  `ops_scout_*`/`passive_ops_scout_*` for Operative) -- disclosed as an observed pattern, not
  asserted as a strict mapping beyond what the ids themselves literally spell out.
- `operation.task.<taskId>.<name|desc>` -- 3 real tasks x 2 fields = 6 lines: `absorber`="Phase
  Generator", `capture`="Communication Hubs Capturing", `boss_ratter`="Ratcatcher Boss".
- `operation.introduction.description_<missionId>` -- 3 real lines, one flavor-text paragraph per
  mission (only 3 of the 5 real missions have one: `big_cleanup`, `shock_therapy`,
  `sea_alienation`). The mission id is joined by a literal underscore, not a further dot -- an
  irregular shape relative to the other four sub-groups, modeled as its own small flat lookup.
- `operation.<missionId>.<name|desc>` (the bare-prefix table itself) -- 5 real missions x 2 fields
  = 10 lines: `big_cleanup`="The Big Clean Up", `shock_therapy`="Focus",
  `quest_polygon0`="Area-0", `sea_alienation`="Sea of Exclusion", `quest_sea_alienation`="Sea of
  Exclusion". The real duplicate "Sea of Exclusion" display name across two different real mission
  ids is preserved exactly as found, not merged or treated as an error.

**Real content, reconciled exactly: 6 + 42 + 6 + 3 + 10 = 67, confirmed by direct count against the
full real file with zero leftover lines.**

**Modeling decision.** Because the bare `operation.<missionId>.*` table sits directly alongside the
four dotted sub-namespaces under the same `operation.` prefix, the loader explicitly excludes real
keys starting with `classes.`, `abilities.`, `task.`, or `introduction.` before falling through to
the bare-mission table -- otherwise those sub-namespaces would be silently absorbed into it as
spurious "mission" ids.

**Real content corroborates two already-established real facts from independent angles, not new
lore.** Both real `operation.introduction.description_big_cleanup` and `description_shock_therapy`
name "Phil from the ZIVCAS Scientific Research Institute corporation" -- `ZIVCAS` is just another
entry in this project's long-standing chain of independent corroborations (parts 92, 169, and now
this one; no numbered ordinal asserted, per the standing disclosure convention for this particular
recurring corroboration). Separately, real `description_sea_alienation` names "Rise" and "Frontier"
alongside "the Covenant" as factions -- "Rise" and "Frontier" are already-confirmed real display
names for the `freedom` and `duty` faction ids respectively (`ClanWarCatalog`, `CnpsCatalog`,
`FactionBaseWarningCatalog`), and "Covenant" is the already-confirmed `covenant` faction's own
display name -- so this passage is a further independent real corroboration of all three, not a new
faction reveal.

**Testing.** 6 new `OperationModeCatalogTest` tests against a fixture built from a representative
real slice covering all five real sub-groups: real class name/description resolution and
`classIds()` enumeration; real mission names resolving under the bare table while explicitly
confirming the four dotted sub-prefixes are excluded from it, and that the real duplicate "Sea of
Exclusion" display name across two mission ids is preserved rather than merged; real mission intro
descriptions (including the real ZIVCAS and Rise/Frontier/Covenant corroborations, and confirming a
mission with no intro resolves to null, not a guess); real ability name/description resolution
(including a real ability with only a name and no desc line in the fixture); real task name/
description resolution; and unknown ids resolving to null across all four id spaces, with `size()`
reflecting the total real line count across all five sub-groups.

**Live-verification, per this project's standing discipline.** Wrote a dedicated probe
(`OperationModeCheck.java`, scratchpad-only, not shipped) and ran it directly against the real, full
`en.lang` file both before finalizing the class's Javadoc claims and to confirm every specific
figure before writing any documentation: `size()`=67; `classIds`=[combat, combined, scientist];
`abilityIds.size`=21 (all 21 real ids enumerated and cross-checked against a fresh `grep`-based
unique-id count, which agreed exactly); `taskIds`=[absorber, boss_ratter, capture]; `missionIds`=
[big_cleanup, quest_polygon0, quest_sea_alienation, sea_alienation, shock_therapy]; real class/task/
mission display names all resolved and matched the Javadoc verbatim. Zero drift from the initial
capture.

**Build.** Compiled clean with `--release 11` in the cloud (1090 total tests, 0 failures), synced to
the device via the standard tarball/`SendUserFile`/`device_commit_files`/extract pipeline, and
re-run live on the device's actual Java 11 JRE with an identical clean result: 1084 -> 1090.

**Doc-only resolutions.** None this part.

**Closing note / methodology note.** The top-level-prefix census found no other new bare namespace
this pass -- every other top-level prefix in the full census (`dmz2026`, `profile_icon`, `region`,
`menulinks`, `main_menu_popup`, `barter`, `stalkerclans`, `tutorial`, `subtitles`, the small
`signal_*` variants, `player_effect`, `filler`, `effect`, `item_tier`, `provision`, `death`,
`review`, and several 1-3 line singletons) was individually checked and confirmed, by reading the
actual covering class's source rather than a literal grep alone, to already be covered by an
existing catalog.

This suggests the bare top-level-prefix census has now reached diminishing returns as a discovery
method -- five genuinely new namespaces were found across parts 167-172, but this pass found only
one, and it was a near-miss collision with an existing class's name (`operation.` vs. `gui.
operation.how_to_play.` under `OperationsCatalog`) rather than a namespace invisible to any prior
census. Genuinely new gaps at this point are more likely to be nested inside the project's largest
already-partially-covered namespaces (`item.` at 12,427 lines, `go.` at 2,613, `stalker.` at 2,020,
`quest_contract.` at 1,064, `general.` at 1,121, `challenges.` at 987, `shop.` at 698, `we.` at 506,
`quote.` at 366, `core.` at 197, etc.) than at the bare top level -- finding those would need a
second-level sub-prefix census run separately within each such namespace (comparing its real
sub-prefixes against exactly which sub-prefixes the covering class(es) actually parse), rather than
a repeat of the top-level pass used in parts 167-172.

No specific lead is queued going into the next part. Future work could either pursue that
second-level census methodology (starting with a mid-sized, already-partially-covered namespace
like `we.` (506 lines) or `quote.` (366 lines), both of unclear current coverage), or shift toward
wiring already-built catalogs into engine behavior wherever a confirmed crosswalk exists (several
such attempts were disclosed as genuinely blocked, not forced, in parts 165-167 for
`EjectionCatalog`, `ItemLoreCatalog`, and `QuestContractCatalog`, each for lack of a confirmed
id-space crosswalk).
