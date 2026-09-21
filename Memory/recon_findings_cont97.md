# Part 141 -- `EjectionCatalog`

## Real shape

The real `ejection.*` namespace (188 lines), the twenty-first concrete namespace built from part
123's follow-up census, and the largest remaining fully-uncovered `en.lang` namespace at the time
this class was written. Modeled with the established generic last-dot-split (id, field) table.
Reconciled exactly: 180 (`stage.notf.<phase>_<n>`) + 4 (`winter.*`) + 2 (`fallout.*`) + 2
(`shelter.msg.*`) = 188.

## The real stage.notf pool

Five phase categories -- `preparation`, `starting`, `storm`, `ending`, `deadline` -- each with
exactly 36 numbered flavor-text variants, all sharing one id `stage.notf` with fields like
`storm_1`..`storm_36`. Randomized announcer lines for the main eruption event (e.g.
`storm_1`="Attention! The eruption is starting. Hang in there, fellas!").

## Three separate real announcer systems

- `winter.notification_prepare.<n>`/`.notification_start.<n>` (4 lines, 2 variants each) -- a
  separate seasonal "winter storm" announcer pool.
- `fallout.notification_prepare`/`.notification_start` (2 lines) -- a separate "radioactive
  fallout" announcer pair, no numbered pool.
- `shelter.msg.enter`/`.exit` (2 lines) -- the in-shelter status toggle texts.

## Corroboration: "Ejection" confirmed as real en.lang text for the already-modeled emission mechanic

"Ejection" was already flagged (part 79/80, disclosed as unconfirmed) as this project's English
rendering of the real S.T.A.L.K.E.R.-series "emission"/"blowout" mechanic. This namespace gives
this project's first `en.lang`-sourced TEXT evidence for that mechanic, on top of
`emission/EmissionCycle`'s existing part-80 handbook-article evidence.

## Disclosed-not-asserted: Phase enum correspondence

`EmissionCycle.Phase` has 4 values (`WAITING`/`WARNING`/`ACTIVE`/`ARTEFACT_WINDOW`); the real
`stage.notf` flavor pool has 5 categories. No direct field-to-enum mapping is claimed or wired --
disclosed as a plausible narration of the same 4-stage cycle, per standing rule 1.

## Testing

3 new `EjectionCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 987 -> 990.

## Context for future parts

Twenty-one namespaces down from part 123's follow-up census. What remains is the long tail of
smaller namespaces documented in part 123's own writeup (`Memory/recon_findings_cont79.md`) --
mostly small (1-41 line) real systems, plus the still-uncovered "large" bucket check should be
re-verified (settlement/checkpoints/core/map/frontline/ejection all now confirmed covered as of
this part). Next reasonable picks by size: `savepoints`(41), `premium`(38), `arcade`(36),
`seasons`(29), `encaged`(29), `hint`(24), `gameobjects`(21, en.lang text distinct from the
file-based GameObjectsCatalog), `incident`(20).
