# recon_findings_cont133.md — Part 177: `world.props.WorldEventCatalog` (second real client bug)

**Date:** 2026-09-20

## Context

The user chose "chase the swapped-content bug pattern further across other faction-templated
groups" after the full top-level-prefix census closed out in part 176. Part 173 had found one real
client bug (`science_camp_duty`/`science_camp_mercenary` content swapped) by CROSS-REFERENCING
`we.*` against `pda.waypoint.we.*`. This part took a different angle: exhaustively check every real
faction-templated field WITHIN `we.*` itself for internal keyword consistency, without needing a
second namespace to compare against.

## Methodology

1. Enumerated every real base-id group under `we.*` that repeats the 4-faction template
   (`_duty`/`_mercenary`/`_merc`/`_freedom`/`_covenant` suffixes) across its `status_title` field —
   found ~30 real base names (`abstract_camp`, `abstract_rescue`, `base`, `field_research`,
   `force_group`, `north_house`, `officer`, `outpost`, `rescue`, `sapper`, `savepoint`,
   `science_camp`, `science_rescue`, `settlement`, `sniper`, `supply_base`, and more).
2. First pass (status_title only, keyword-presence check): flagged several false positives where
   ALL 4 factions share IDENTICAL generic text by design — `officer_*`="Officers' camp",
   `rescue_*`/`science_rescue_*`="ZIVCAS Raid", `supply_base_*`="Supply base" (this last one
   already disclosed in part 173 as a capitalization-only mismatch against a different namespace).
   These are not bugs, just non-differentiated content, and were excluded.
3. Second pass: isolated only the groups with GENUINELY DIFFERENTIATED (non-identical) per-faction
   text — 7 groups (`abstract_camp`, `field_research`, `force_group`, `north_house`, `sapper`,
   `science_camp`, `sniper`) — and checked each faction's text for its own expected keyword. Only
   `science_camp` (already known) failed this check.
4. Third pass: extended the same check across every OTHER real field under `we.*`
   (`goal`/`task_text`/`failure_text`/`success_text`/`notification_text`/`awaiting_text`/
   `description`, etc.), for every faction-templated base group, not just `status_title`. This
   surfaced one new, genuine finding.

## The new find: `we.abstract_rescue_duty`

Real lines, confirmed via direct grep against the full file:
```
we.abstract_rescue_freedom.goal=Don't let the Rise soldiers die
we.abstract_rescue_freedom.task_text=Help the Rise soldiers
we.abstract_rescue_freedom.failure_text=The Rise soldiers died!
we.abstract_rescue_freedom.success_text=The Rise soldiers survived and offer a reward
we.abstract_rescue_duty.goal=Don't let the Frontier soldiers die
we.abstract_rescue_duty.task_text=Help the Frontier soldiers
we.abstract_rescue_duty.failure_text=The Rise soldiers died!
we.abstract_rescue_duty.success_text=The Rise soldiers survived and offer a reward
```

`abstract_rescue_duty`'s own `goal`/`task_text` correctly name "Frontier" (its own faction, per the
already-established `duty`=Frontier mapping). But its `failure_text`/`success_text` are
BYTE-IDENTICAL to `abstract_rescue_freedom`'s own `failure_text`/`success_text` — both literally say
"The Rise soldiers...". This is a real, genuine client copy-paste error: whoever wrote
`abstract_rescue_duty`'s outcome text copied it from the freedom variant and forgot to change "Rise"
to "Frontier", affecting only half of this one id's real field set.

Checked exhaustively (not sampled) across every other faction-templated group and field
combination in `we.*`: this is the ONLY other occurrence of this bug pattern in the entire
namespace, beyond the already-known `science_camp` swap. Also checked whether `pda.waypoint.we.*`
has a parallel `abstract_rescue_duty`/`_freedom` entry to cross-reference against (it doesn't — no
`abstract_rescue_duty`/`_freedom`/etc. ids exist in that namespace at all, only generic
`abstract_rescue`/`abstract_rescue_scientist`/`_zombie`/`_extractor` variants), so this finding is
self-contained within `we.*`'s own data.

## Fix

No code change needed to `WorldEventCatalog` — its existing generic `field(eventId, field)` table
already returns whatever real text is in the file, bug or not; `failureText()`/`successText()`
accessors already existed. This part:
- Added a Javadoc paragraph disclosing the finding: the methodology, the exact real values, and
  confirmation that no other occurrence exists.
- Added 1 new regression test locking in the real values (both the correctly Frontier-flavored
  `goal`/`task_text` and the real Rise-flavored `failure_text`/`success_text`, plus a direct
  byte-identity assertion against the `freedom` sibling) so a future change cannot silently
  "correct" real client data without that being a deliberate, disclosed decision.

## Testing

1 new `WorldEventCatalogTest` test, using an extended fixture with both `abstract_rescue_duty` and
`abstract_rescue_freedom`'s real lines. Live-verified with a dedicated scratch probe
(`WeCheck.java`, deleted after use) directly against the real, full `en.lang` file before shipping
documentation: all values confirmed exactly, including a repeat confirmation of the already-known
`science_camp` swap for good measure. Full suite run clean on both a plain and a `--release 11`
build in the cloud, then synced across and re-run on the device's actual Java 11 JRE: 1108 -> 1109.

## Standing rules followed

- Rule 1: the finding is disclosed exactly as found, with the confirmation step (byte-identity
  check against the freedom sibling) done directly rather than assumed from pattern-matching alone.
- Rule 5: no snapshot re-extraction; this is a documentation/test addition, not a rebalance
  response.
- Rule 6: nothing pushed to GitHub this part.

## Next step

The exhaustive faction-template sweep across `we.*` itself is now complete — 2 real bugs total
found in this namespace (parts 173, 177), confirmed to be the only 2 of this specific pattern.
Continuing this exact bug-chase direction further would mean checking OTHER already-modeled
faction-templated namespaces (e.g. `FactionBaseWarningCatalog`'s `protected_areas.alert.
notification.*`, `CnpsCatalog`'s per-faction dialog/reward groups) for the same kind of internal
keyword mismatch — not yet attempted. Otherwise, the standing menu from part 176 remains open:
push to GitHub, revisit engine-wiring blockers, or re-verify second-level completeness for the
handful of large classes not yet checked via the branch-mirroring technique.
