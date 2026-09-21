# Part 167 -- world.props.Spring26ChallengeCatalog: a fresh full-file re-scan finds genuinely new content

## What was found

Taking part 165/166's "fresh `en.lang` full-file re-scan" closing option literally: a full
top-level-prefix census of the CURRENT install's `en.lang` (28,300 lines, up from part 90's
original count -- newer client content, not a rebalance) turned up 41 real lines starting bare
with `spring26/challenges/`, with NO leading dotted namespace prefix at all. Confirmed genuinely
new: a direct source-tree grep for the literal string `spring26` turned up nothing before this
part.

## A real third, structurally distinct bucket

`ChallengesCatalog` (plural, part 116, 1,011 lines) and `ChallengeCatalog` (singular, part 152, 17
lines, real slash-embedded ids) are both real DOTTED namespaces (`challenges.*`/`challenge.*`).
This new namespace has no leading dot at all -- `spring26/challenges/` is itself the start of the
key. A third, separate real bucket sharing only the English word, named `Spring26ChallengeCatalog`
to avoid the exact same confusion `ChallengeCatalog`'s own doc already flagged for the first two.

## Real content, reconciled exactly: 3 + 3 + 22 + 4 + 9 = 41

(An initial pass miscounted two of these groups as 24 and 7 -- the two errors happened to cancel
out in the 41 total, which is exactly why this project always live-verifies counts against the
real file directly. Caught before the class's own doc/test shipped uncorrected.)

- `ships_artefacts_episode/1-3` and `tanks_artefacts_episode/1-3` (3+3 lines): real single-artifact
  hunt objectives. CONFIRMED, byte-exact crosswalk: all 4 real artifact names referenced
  ("Jelly"/"Loop"/"Link"/"Flicker") match part 121's `ItemLangCatalog` real `item.art.<id>.name`
  entries exactly (`lesta_jele`/`lesta_petlya`/`lesta_tracks`/`lesta_spheretank`).
- `reichstag/<n>` (22 lines, real non-contiguous ids 1,3,5,7,9,12,14-17,19-30): real PvP challenge
  objectives for "Assault on the Reichstag". Per this project's own STANDING NOTE (part 109): just
  ANOTHER real corroboration of the long-open Reichstag battlefield/event, no new ordinal asserted.
- `fools_day/1-4` (4 lines): a real April Fools'-Day PvP event, intentionally comedic copy
  preserved verbatim: "Why was I given a butterfly knife?", "M-m-monster Kill!".
- `season/insideout/<n>` (9 lines, real ids 1,3-10 -- id 2 is a real disclosed gap): real 2026
  objectives for a "centrifuge" mechanic at a real named location, "Forge-11". This project's
  first real evidence of a 2026 continuation of the "Inside Out" event line already covered in
  depth (parts 139/154/158) -- disclosed as a narrative continuation only, not asserted to share
  id/key structure with the earlier real 2025 catalogs.

## Standing-rule note

Per this project's rule to track new content, not live rebalancing: this namespace's real
existence -- absent from the much earlier original `en.lang` census, present now -- is itself the
finding: a real seasonal content addition between client versions, not a stat change.

## Testing

3 new `Spring26ChallengeCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`, covering all 5 real id groups.

Also live-verified directly against the real, full `en.lang` file (not just the fixture): total
ids=41, reichstag=22, season/insideout=9, fools_day=4, ships_artefacts_episode=3,
tanks_artefacts_episode=3 -- this direct re-count is exactly what caught and corrected the 24/7
arithmetic error above before it reached the class's own doc uncorrected.

Run clean on both a plain and a `--release 11` build in the cloud and, synced across, on the
device's actual Java 11 JRE: 1063 -> 1066.

## Closing note

A fresh full-file re-scan, once tried, paid off with genuinely new content this project's original
census never saw (added in a later client patch than part 90's original scan). Future work could
repeat this same fresh-re-scan approach for any other top-level prefix that may have grown new
content since the original census, continue wiring already-built catalogs into engine behavior
wherever a confirmed crosswalk exists, or revisit `ItemLoreCatalog` once `Inventory` gains a
categoryId to disambiguate with.
