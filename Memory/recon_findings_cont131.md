# recon_findings_cont131.md — Part 175: `world.props.AnnouncerCatalog`

**Date:** 2026-09-20

## Context

Following part 174's fix, the user chose "keep auditing rarer namespaces" as the direction. Ran a
fresh full-file top-level-prefix census (the same technique as part 123's original one) against
the currently staged `en.lang` file, then cross-checked every entry against every existing class's
actual `PREFIX` constant in source (not just Javadoc claims).

The census's large "blank" (1016) bucket was investigated first and confirmed to be literal blank
lines in the file, not a namespace: 1016 blank + 27,285 `=`-bearing lines accounts for the file's
28,300 total lines (off by one from a trailing-newline/rounding artifact, not a concern).

Every other top-level prefix in the census matched an existing class's actual prefix scope, except
one: `bundle.` (67 lines), genuinely uncatalogued.

## The find: `bundle.notf.*`

Real content: a live-commentary/"Commentator" announcer-line system for the DMZ raid modes, plus
a real "Admin" announcer variant for an April Fools'-Day event (`fools_day2026`).

Naming hazard caught before building: `BundleCatalog` (part 78) already exists and covers a
completely unrelated real file-asset directory (`modassets/assets/bundle/`). Confirmed the two are
genuinely distinct (file-asset directory contents vs. `en.lang` text under a same-named top-level
prefix — a coincidental collision at that one layer only) before naming the new class
`AnnouncerCatalog`.

Real shape, reconciled exactly (62 + 5 = 67, zero leftover):
- `bundle.notf.<modeId>.annotator.<eventId>` (62 lines) — `dmz` (59), `dmz2026` (1, just `title`),
  `fools_day2026` (2).
- `bundle.notf.dmz2026.<field>` (5 lines) — a separate, non-annotator "FROST TROOPERS"
  operation-queue info block (`title`, `info.start`/`.end`/`.join`/`.leave`).

A real, disclosed naming quirk: of the 59 lines under `bundle.notf.dmz.annotator.*`, 30 use
genuinely legacy-DMZ-flavored event ids (`world_dmz_*`/`party_dmz_*`/`player_dmz_*`) and 29 use
`dmz2026`-flavored event ids (`world_dmz2026_*`/`party_dmz2026_*`/`player_dmz2026_*`) despite both
families living under the `dmz` mode prefix rather than the separate `dmz2026` mode prefix that
does exist (used only for that mode's own `title` and the non-annotator info block). Modeled
exactly as keyed — both id families resolve under `modeId="dmz"` — not re-bucketed into a guessed
grouping the client itself doesn't use.

7 real named boss-kill lines, previously uncatalogued under any name in this project (checked — no
existing class names these): `boss_death_dochide`="Dr. Hyde killed!", `_grenade`="Bomber killed!",
`_infected`="Toxic is killed!", `_objora`="Guzzler killed!", `_wizard`="Warlock is killed!",
`_rhino`="Rhino killed!", `_aid`="Hades is killed!".

Several real event ids repeat byte-identical text across the legacy/2026 id families (e.g. both
`world_dmz_evacuation_started` and `world_dmz2026_auto_evacuation_started` = "The last helicopter
leaves in a minute!"), while others carry genuinely different real wording for the same event
(`player_dmz_left_game`="A member quit the game" vs. `player_dmz2026_left_game`="Participant left
the game") — both kinds preserved and disclosed separately, not merged or "corrected."

The non-annotator info block's `title`="FROST TROOPERS" is a THIRD independent real corroboration
of that operation name, alongside part 135's `DmzLangCatalog` and part 140's `SeasonPassCatalog`
(`seasonpass.task.category.event`="FROST TROOPERS").

## Class built

`AnnouncerCatalog` (`bundle.notf.*`, PREFIX `"bundle.notf."`, splitting on the `.annotator.` infix
when present, else treating the remainder as a `(modeId, field)` pair for the non-annotator info
block). Accessors: `announcerLine(modeId, eventId)`, `announcerModeIds()`,
`announcerEventIds(modeId)`, `infoField(modeId, field)`, `size()`.

## Testing

7 new `AnnouncerCatalogTest` tests against a representative real fixture: mode titles including the
fools_day2026 Admin variant, real boss-kill lines, the real dmz/dmz2026 naming quirk (a 2026-flavored
id resolves under the dmz mode, not a guessed dmz2026 mode), real duplicate-vs-different text pairs,
the separate FROST TROOPERS info block not bleeding into the annotator table, sorted id listings,
and unknown lookups resolving to null.

Live-verified with a dedicated scratch probe (`AnnouncerCheck.java`, deleted after use) directly
against the real, full `en.lang` file before writing documentation: `size()`=67, mode counts
(59/1/2) confirmed, all 5 spot-checked real boss-kill lines confirmed byte-exact, the FROST
TROOPERS info block confirmed. Full suite run clean on both a plain and a `--release 11` build in
the cloud, then synced across and re-run on the device's actual Java 11 JRE: 1097 -> 1104.

## Standing rules followed

- Rule 1: the dmz/dmz2026 naming quirk and the duplicate-vs-different text pairs are disclosed
  exactly as found, with no assumed "correct" grouping imposed.
- Rule 2: pure game content (raid announcer flavor text), no EXENS/launcher material involved.
- Rule 6: nothing pushed to GitHub this part.

## Next step

Continuing the same rarer-namespace sweep into the remaining smaller top-level prefixes (part 176).
