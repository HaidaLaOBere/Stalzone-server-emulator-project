# recon_findings_cont130.md — Part 174: `world.props.HideoutCatalog` (metric.value fix)

**Date:** 2026-09-20

## Context

Continuing the second block of second-level-completeness audit checks (started after part 173),
covering the namespaces listed in part 173's closing note: `cnps.`, `currency.`, `loadscreens.`,
`hideout.`, `settlement.`, `checkpoints.`, `map.`, `ejection.`, `world.`, `phonograph.`, `tile.`.

All namespaces except `hideout.` were confirmed fully covered by their existing classes with zero
code changes needed, verified either by direct grep-based line-count reconciliation or by mirroring
the covering class's exact branch/prefix logic in a standalone Python script run against the real,
full `en.lang` file:

- `cnps.` (314 lines) — fully covered (`CnpsCatalog`).
- `loadscreens.` (272 lines) — fully covered (`LoadscreenCatalog`); its one irregular line
  (`loadscreens.battlefield_breakthrough_reich`) was already disclosed in the class's own Javadoc.
- `checkpoints.` (202 lines) — fully covered (`CheckpointsCatalog`), no discard branch exists.
- `ejection.` (188 lines) — fully covered (`EjectionCatalog`); the code has a `continue` for
  no-dot keys but zero real lines actually lack a dot.
- `tile.` (114 lines) — fully covered (`TileCatalog`); all 114 real lines end in `.name`.
- `world.` (138 lines) — fully covered (`WorldModifierCatalog`); 100% under `modifier`.
- `settlement.` (202 lines) — fully covered (`SettlementPerkCatalog`); 100% under `id`, with an
  unconditional catch-all in the loader (no discard condition can fire for real content).
- `currency.` (295 lines) — fully covered (`CurrencyDisplayCatalog`); 100% under `type.id`, field
  set matches exactly.
- `map.` (193 lines) — fully covered (`MapLangCatalog`); all 5 real second-level groups
  (`location`=81, `teleport`=78, `global`=26, `safezone`=6, `page`=2) match the class's 5 sub-prefix
  constants exactly, and zero lines under any of the 5 lack a further dot past the id.
- `phonograph.` (135 lines) — fully covered (`PhonographCatalog`); all 100 `gui.*` lines are
  `gui.track.*`, and all 35 flat `vinyl_*` segments end in `.title` and are captured by the bare
  `phonograph.` prefix filtered to ids starting with `vinyl`.

## The one genuine finding: `HideoutCatalog`'s undisclosed `metric.*.value` gap

`HideoutCatalog` (part 106, `hideout.*`, 250 lines) is a long if/else-if branch chain rather than a
single generic table. Mirroring its exact branch logic in a Python script against the real, full
file found 11 real lines falling through uncaptured — more than the other ten namespaces in this
block combined.

Of those 11:
- 3 (`hideout.misc.can_be_closed`/`can_change_color`/`can_change_pose`) and 2
  (`hideout.hookah.daily_reward.1`/`weapon_visualisation.desc.1`) were ALREADY explicitly disclosed
  in the class's own Javadoc as deliberately-excluded ("Not pursued further: hideout.misc.* ...,
  hideout.hookah.* ..."). Not a gap — already honestly disclosed.
- The remaining 6 (`hideout.metric.<id>.value` for all 6 real generator/storage metrics) were
  **not disclosed anywhere**. The branch chain's `metric.` handling only ever checked for a
  `.name` suffix; `.value` silently fell through with no accessor and no mention in the Javadoc,
  unlike the deliberately-excluded families above.

Real values, verbatim:
- `extra_space.value` = `+{value}`
- `generator_slots.value` = `+{value}`
- `generator_storage_modifier.value` = `+{value}%`
- `generator_energy_recycle_modifier.value` = `+{value}%`
- `generator_disintegration_rate_modifier.value` = `+{value}%/SEC`
- `generator_researcher_disintegration_rate_modifier.value` = `+{value}%/SEC`

These are real UI number-format templates for each metric (flat count vs. percentage vs.
percentage-per-second), useful for correctly rendering a metric's numeric bonus in-game.

## Fix shipped

- Added `metricValueById` map, a new `metric.` + `.value` branch in `load()`, and a
  `metricValue(String id)` accessor to `HideoutCatalog.java`.
- Updated the Javadoc in place to disclose all 6 real values and explicitly state that the only
  real `hideout.*` lines this class still excludes are the already-disclosed `misc.*`/`hookah.*`
  families — zero remaining undisclosed drops.
- No new class/part-numbered file; this is an in-place completeness fix, tracked as its own part
  (174) per this project's convention of numbering every code change, not just every new class.

## Testing

- Extended `HideoutCatalogTest`'s fixture with 3 real `metric.*.value` lines (one flat-count
  template, two percentage/rate templates) and added 1 new test asserting `metricValue()` resolves
  all three correctly, that `metricName()` still resolves independently for the same ids, and that
  an unknown metric id resolves to null.
- Live-verified directly against the real, full `en.lang` file with a dedicated scratch probe
  (`HideoutCheck.java`, deleted after use) before writing any documentation: all 6 real metric ids
  resolve both their real `.name` and real `.value` text exactly, matching the Javadoc's verbatim
  claims byte-for-byte.
- Full suite run clean on both a plain and a `--release 11` build in the cloud, then synced across
  and re-run on the device's actual Java 11 JRE: 1096 -> 1097.

## Standing rules followed

- Rule 1 (no assumption as spec): the `.value` templates are exposed literally, with no inferred
  unit/rounding behavior beyond what the template string itself shows.
- Rule 5 (no re-extraction for rebalancing): this is a structural coverage fix (a previously
  unmodeled but always-present field), not a response to a stat/number changing — no snapshot data
  was re-extracted.
- Rule 6 (no push without request): nothing pushed to GitHub this part; all work remains local
  until explicitly requested.

## Next step

This closes out the second audit block. Per the user's own standing instruction to check in once a
block finishes rather than continuing indefinitely, the next message presents a short menu of
directions instead of unilaterally picking one.
