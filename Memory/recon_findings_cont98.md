# Part 142 -- `SavepointsCatalog`

## Real shape

The real `savepoints.id.<savepointId>.name` namespace (41 lines), the twenty-second concrete
namespace built from part 123's follow-up census -- real respawn-point display labels, every one
of the 41 real lines ending in the literal `.name` tail, modeled with the established flat
PREFIX/SUFFIX strip.

## A real confirmed corroboration

`fallback_stalkers.name`="Ataman's HQ" and `fallback_bandits.name`="Boathouse" reuse the EXACT
same real display names as `location.atamans_hq.name`/`location.boat_station.name` (part 105's
`LocationNameRegistry`, cross-confirmed against `settlement.id.csky.title` in part 123's census)
-- a real, evidenced "which faction currently holds this named location" fallback-respawn
convention.

## Other real finds

5 real `kontrolnaya_tochka_<n>` ("Checkpoint_1".."Checkpoint_5") plus 4 more real
`poligon_kontrolnaya_tochka[_<n>]` ids all literally named plain "Checkpoint" -- disclosed not
corrected. Several `isolator_room_<n>`/`isolator_lobby` ids share "Elevator". `school_1`/
`school_2` both ="School beginning"; `school_end_1`/`school_end_2` both ="School ending".

## Testing

3 new `SavepointsCatalogTest` tests against a fixture built from real key=value lines. Run clean
on both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 990 -> 993.

## Context for future parts

Twenty-two namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `premium`(38), `arcade`(36), `seasons`(29), `encaged`(29),
`hint`(24), `gameobjects`(21, en.lang text distinct from the file-based GameObjectsCatalog),
`incident`(20), `snowstorm`(18), `industries`(18), `challenge`(17, singular), `arena`(17),
`inside`(16), `blizzard`(16), `outpost`(14, check for overlap with existing `HomeOutpostCatalog`
before writing), `ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11),
`profile_icon`(10), `banner`(9), `territories`(8), `provision`(7), `mail`(6), `entity`(5),
`item_tier`(4), `anomaly`(4), `barter`(2), `menulinks`(3), `death`(3), `review`(2),
`main_menu_popup`(2), `tutorial`(1), `bonuscode`(5).
