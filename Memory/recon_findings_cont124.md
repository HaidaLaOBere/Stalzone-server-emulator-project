# Part 168 -- world.props.TutorialGuideCatalog: two real, textually-divergent onboarding-tutorial namespaces

## What was found

Continuing part 167's fresh full-file re-scan approach across the rest of the smaller top-level
prefixes turned up several more genuinely uncovered real namespaces. The richest: `stalkerguide.*`
(26 real lines) and `stalkerguide2019.*` (28 real lines) -- both real new-player onboarding/
tutorial prompt text (move, stand up, mark a target, draw/reload a weapon, loot a corpse, open
inventory, use an elevator/ammo crate).

A direct source-tree check confirmed neither was actually covered: the existing, similarly-named
`StalkerGuideCatalog` (part 59) catalogs a completely unrelated real thing -- real FILE-based
story/cutscene assets under `modassets/assets/stalkerguide/` (video/audio/texture files). A
coincidental name collision with this `en.lang` text namespace, not an overlap. Named the new class
`TutorialGuideCatalog` specifically to avoid repeating that confusion.

## A real shape that doesn't fit the usual generic table

Real keys in both namespaces vary in dot-depth in a way this project's established last-dot-split
`(id, field)` table doesn't cleanly fit (`quest.weaponuse.2.text.reload` vs. `quest.getup.header`
vs. `controlpoints.control_point.ammo_crate`). Modeled instead as a flat `Map<String,String>` per
namespace, keyed by the full real suffix string -- honest to the real irregular shape rather than
forcing an artificial id/field boundary.

## Real content, reconciled exactly: 24 quest + 2 controlpoints = 26 (current); 27 quest + 1 controlpoints = 28 (2019)

Direct key-set comparison (not sampling) found real overlap AND real, disclosed asymmetry:

- Two real keys exist ONLY in the current namespace: `controlpoints.control_point.ammo_crate` and
  `quest.walkaround.header`="Moving" (a header the `2019` version's own walkaround step never got).
- Four real keys exist ONLY in `stalkerguide2019`: a first-aid-kit pickup/use tutorial step
  (`quest.medkit_pre.*`/`quest.medkit.*`) the current namespace skips entirely.
- For several real step ids present in BOTH namespaces, the actual real text differs, not just the
  key: `quest.getup.header` is "Ascent" (current) vs. "Stand Up" (`2019`);
  `quest.weaponuse.3.desc.killed` is "Destroy {total number} enemies" (current) vs. "Targets
  killed ({killed number}/{total number})" (`2019`, an entirely different real template-variable
  style). Other shared steps are real byte-identical text between the two namespaces.

What the real `2019` suffix actually means is NOT confirmed anywhere in this project's evidence. A
plausible but unproven reading, consistent with part 163's finding that this game is presented
in-fiction as a rebrand of an earlier project ("STALCRAFT"), is that `stalkerguide2019` is a
retained legacy tutorial-text version still shipped alongside the current one. The class asserts
only the literal observed facts, not which namespace a real player actually sees.

## A live-verification catch, disclosed rather than silently fixed

An initial pass through the real file identified `controlpoints.control_point.ammo_crate` as the
only current-only key. A follow-up live re-check against the real, full `en.lang` (this project's
standing discipline before shipping any count) found a SECOND real current-only key,
`quest.walkaround.header`, that the first pass missed. Caught and corrected before this class's
doc/test shipped with an incomplete disclosure -- exactly the kind of miss live re-verification
exists to catch, directly following part 167's own arithmetic-error catch two parts ago.

## Testing

3 new `TutorialGuideCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`, covering the real overlap, the real differing-text-for-
shared-keys case, and both real disclosed asymmetries.

Also live-verified directly against the real, full `en.lang` file: currentKeys=26,
legacy2019Keys=28, onlyInCurrent=[`controlpoints.control_point.ammo_crate`,
`quest.walkaround.header`], onlyInLegacy2019=4 real medkit-related keys, inBothVersions=24.

Run clean on both a plain and a `--release 11` build in the cloud and, synced across, on the
device's actual Java 11 JRE: 1066 -> 1069.

## Closing note

The fresh-re-scan approach continues to pay off -- this is the second genuinely new namespace
found this way in two parts, after part 167's `spring26/challenges/*`. Other small prefixes
surfaced by the same re-scan but not yet built into a class remain open leads for a future part:
`ops.leagues.*` (a real 5-tier ranked ladder with names like "Absolute Unity"/"Revenant"/"Adept"),
real `frontlineline.*` narrative briefing text (distinct spelling/content from the already-covered
`frontline.*`), and `triumph.2026.*` reward-mail text (ties to part 121's `ItemLangCatalog`
`triumph26` collection id). Future work could pick any of these up, continue wiring already-built
catalogs into engine behavior wherever a confirmed crosswalk exists, or revisit `ItemLoreCatalog`
once `Inventory` gains a categoryId to disambiguate with.
