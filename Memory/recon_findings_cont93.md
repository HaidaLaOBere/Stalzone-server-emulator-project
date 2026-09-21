# Part 137 -- `OrdersCatalog`: a real per-faction/DMZ squad-"order" objective system

Continuing top-down from part 136's closing note, this part models the real 64-line `orders.*`
namespace -- a real per-faction/DMZ squad-"order" objective system, confirmed distinct from part
133's `PartyMissionsCatalog` (a different real co-op mission-tracker namespace,
`party_missions.*`, with no shared ids checked between the two).

## Real shape

Confirmed by direct count to be a single real group with zero flat exceptions: all 64 lines fit
`orders.<id>.<field>` via the established last-dot-split scheme (40 lines with one dot in the
remainder, 19 with two, 5 with three). Real ids split into 4 rough families: per-faction
repeating objectives (e.g. `destroy_ammunition_covenant_1`/`_2`/`_3`,
`visit_positions_covenant`/`_duty`/`_freedom`/`_merc`), a real DMZ-tied objective set
(`dmz_collect_objects_<n>`, `dmz_destroy_objects_<n>`, `dmz_visit_positions_<n>`,
`dmz_kill_player`), a real internal test/QA objective set (`*_party_test`/`*_single_test` ids,
left uninterpreted rather than guessed at), and 4 real shared UI-label groups (`map_marker`,
`objective`, `reward`, `waypoint`).

## A real structural quirk

Not normalized: `orders.map_marker.title` is itself both a real leaf VALUE ("A Mission from the
Command") and a real prefix for a further-nested key, `orders.map_marker.title.description`
("Follow the command's instructions") -- the same quirk recurs at
`orders.dmz_kill_player.source_map_marker.title`/`.title.description`. Handled correctly without
special-casing by this class's generic last-dot split, since `map_marker.title` and `map_marker`
are simply two different real ids in the same table.

## Testing

3 new `OrdersCatalogTest` tests against a fixture built from real key=value lines drawn directly
from the live client's `en.lang`: a real per-faction objective and a real DMZ-tied objective
resolving, the real `map_marker.title` double-nesting quirk resolving both ids independently, and
sorted/null-safe id and field lookups. Compiled with `--release 11`, run clean in the cloud
(978/978) and, synced across via the usual tarball pipeline, live-verified on the device's actual
Java 11 JRE (978/978).

## Context for future parts

Seven more down from part 123's long tail (parts 131-137). What remains: `modules` (59),
`item_origins` (55), `seasonpass` (49), and the long tail of smaller namespaces documented in
part 123's own writeup (`Memory/recon_findings_cont79.md`).
