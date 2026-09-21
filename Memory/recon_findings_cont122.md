# Part 166 -- items.ItemLoreCatalog: the real itemnames.dat NBT file, finally a first-class Java catalog

## What was built

`modassets/assets/stalker/itemnames.dat` -- a real gzip-wrapped NBT file this project's own early
(pre-"Part N") research had already located, decompressed, and byte-exact-validated via Python
tooling (`tools/nbt_dump.py`/`tools/item_lookup.py`) and this project's own `nbt/NbtReader`/
`NbtWriter` -- had never had a first-class Java catalog class built on top of it; it sat as ad hoc
CLI tooling only. Direct re-verification against the live client (this project's standard
discipline for old findings) confirmed the file is still present at the same path, still
gzip-magic, and still parses to exactly 577,074 bytes with 0 leftover bytes -- identical to the
original finding, now ~150 parts later.

`items/ItemLoreCatalog.java`: a root compound with one real `Data` list of 1483 real compound
records, each with a real `key`, optional real `display_name`, optional real `Lore` (list of
lines), optional real `creator`.

## The two real key styles (confirmed by direct count over all 1483 records)

- 1288 records: real `internal.<categoryId>.<itemId>.<variant>` keys. `itemId` is the SAME
  internal numeric item-id space `items.ItemGroups`/`items.ItemWeightTable`/
  `customnpcs/sorting_map.txt` all use.
- 195 records: real free-form slug keys (e.g. `crash.pda_scientist`,
  `hallowin_goluboe_steklo_s_chernohvosta`). A direct count found these 195 are EXACTLY the
  records with a real `creator` field (100% correlation) -- genuine player-submitted lore/
  flavor-text entries (community-writing-contest-shaped), not developer-authored content.
- A smaller real minority (19 of 214 total creator-tagged entries) layers community-submitted lore
  onto an EXISTING numeric catalog item id instead of a new standalone one.
- Real disclosed gaps: 23 of 1288 internal-keyed records have no real `display_name`; 145 of the
  full 1483 have no real `Lore`.
- Real distinct creator usernames are embedded verbatim in the live file. This class's own unit
  test fixture substitutes a placeholder for the 2 it would otherwise carry, per this project's
  standing privacy rule against committing real third-party usernames to test fixtures; `load()`
  itself returns whatever the real file actually contains.

## The real itemId/categoryId ambiguity, reconfirmed with a concrete example

This project's early research already established (see `ItemWeightTable`'s own class doc) that the
internal numeric item id is only unique WITHIN a category, from 3 real `weight.txt` collisions.
This file gives the same real fact fresh, independent evidence: of 1148 distinct real `itemId`
values among the internal-keyed records (an EXACT match for that early research's own "~1148-item
lore-text subset" count -- a nice cross-validation of a years-old finding against a fresh direct
re-parse), 19 (about 1.6%) appear under more than one real `categoryId`.

Followed all the way to real content: real itemId `11485` appears under BOTH `categoryId 28200`
(variant 1, display_name "Конверт с красной пометкой" -- a red-marked envelope) and `categoryId
28202` (variant 0, display_name "Конверт с синей пометкой" -- a blue-marked envelope) -- two
genuinely DIFFERENT real items, not a duplicate record, sharing one bare itemId.

`categoriesForItemId(int)`/`ambiguousItemIds()` surface this real ambiguity directly, the same
disclose-don't-hide standard as `ItemWeightTable.conflictingIds()`. Deliberately, NO
`displayNameForItemId(int)` convenience method exists on this class, since it could only resolve
these 19 real ambiguous ids by guessing which category the caller meant.

## Why this was NOT wired into GameLoop/Inventory this part

`session.Inventory` (this emulator's own server-side design, not evidenced real wire data -- see
that class's own doc) is keyed by a bare `int itemId` with no `categoryId` field at all. Wiring a
real display name into `GameLoop.tickPurchase`'s event line would mean inventing a resolution rule
for the 19 real ambiguous ids -- this project has no evidence for which category an
emulator-tracked `Inventory` stack "really" belongs to, so any such rule would be invented, not
evidenced. Same standard as part 165's explicit decision not to wire `EjectionCatalog` into
`EmissionCycle.Phase` over an unconfirmed correspondence: the gap is disclosed, not papered over.
If `Inventory` ever gains a real or emulator-design category field, `byInternalId` is already the
exact lookup that wiring would need.

## `variant`'s meaning stays unconfirmed

Observed real values across all `internal.*` keys: 0, 1, 2, 3, 4, 5, 7 (6 never appears). NOT
simply "page N of one shared lore text" -- direct inspection shows different variants of the very
same (categoryId, itemId) pair can carry entirely different real `display_name`s (e.g. real
`internal.20050.10015.1`="Абакан Зрячего" vs. a same-id-family real
`internal.20476.12529.5`="Глок Льда" -- both read plausibly as named/unique weapon variants of a
base weapon id, though the class asserts nothing beyond the literal observed field values). The
class exposes `variant` as a plain integer, not a guessed enum.

## Testing

3 new `ItemLoreCatalogTest` tests against a fixture built from real records (real
key/display_name/Lore text verbatim; the 2 real creator usernames the fixture would otherwise
carry are replaced with the placeholder `"REDACTED_CREATOR"`, per this project's privacy rule),
gzip+NBT-encoded via this project's own `NbtWriter` so the fixture exercises the exact same
decompress-then-parse path `ItemLoreCatalog.load` uses against the real file.

Also live-verified directly against the real, full `itemnames.dat` file (not just the fixture):
`size()`=1483, `freeformKeys().size()`=195, `ambiguousItemIds().size()`=19, and the real
itemId-11485 red/blue envelope pair resolved exactly as documented above.

Run clean on both a plain and a `--release 11` build in the cloud and, synced across, on the
device's actual Java 11 JRE: 1060 -> 1063.

## Closing note

This part is a "shift to a different confirmed-but-uninvestigated asset area" (one of part 165's
three closing options) that turned up a genuine, concrete new disclosed gap (the itemId/
categoryId ambiguity) rather than a clean new engine-wiring opportunity -- consistent with this
project's standard of reporting a real blocker honestly instead of forcing one part's scope to
include an invented resolution. Future work could still wire `ItemLoreCatalog` in once `Inventory`
(or a real evidenced wire format) has a categoryId to disambiguate with, continue wiring other
already-built catalogs into engine behavior wherever a confirmed crosswalk exists, or pursue a
fresh `en.lang` full-file re-scan for anything part 123's census missed.
