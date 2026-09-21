# Part 165 -- GameLoop/SingleplayerServer engine wiring: real mob display names in spawn events

Not a new en.lang catalog class. The first actual `GameLoop`/`SingleplayerServer` engine wiring
since part 90, after 74 content-only catalog parts (91-164).

## What was wired

- `WorldFlavorCatalog` (part 162) gained `entityNameForMobConfigId(String mobConfigId)`: a small,
  hardcoded 3-entry crosswalk from real `MobConfig` configIds to real `entity.*` display names --
  `base_tsarevich` -> `entityName("carevich")` = "Boar Tsarevich", `base_tsarevna` ->
  `entityName("carevna")` = "Piggy Princess", `base_tick` -> `entityName("tick")` = "Tick" --
  returning `null` for any other configId (e.g. `base_boar`), deliberately not a generic
  automatic id transform.
- `GameLoop` gained `setEntityCatalog(WorldFlavorCatalog)`/`entityCatalog()`, mirroring the
  existing `setLangCatalog`/`langCatalog()` shape exactly. `tickSpawns()` now appends a real
  display name to its event line using the identical `realName`/`namePart` idiom already
  established by part 90's `tickAbilityUse`.
- `SingleplayerServer`'s real startup sequence -- which already builds a `LangCatalog` from the
  real `en.lang` file and calls `loop.setLangCatalog(langCatalog)` -- now also builds
  `WorldFlavorCatalog.load(langCatalog)` from that SAME already-loaded catalog (no second file
  read) and calls `loop.setEntityCatalog(entityCatalog)`, with a startup log line through the
  existing event sink.

## Why this integration point, not another (rule-1 discipline)

`EjectionCatalog`/`EmissionCycle.Phase` was explicitly considered and declined as a wiring target
because that class's own Javadoc had already disclosed the phase correspondence as merely
plausible, not confirmed -- wiring it in would have turned an assumption into a protocol/behavior
specification. The mob-display-name crosswalk was chosen instead because BOTH halves of the
correspondence (the real `en.lang` entity text, and the real `MobConfig` configId) are
independently evidenced, and the crosswalk needed no invented logic beyond a 3-entry lookup with a
`null` default for anything unconfirmed.

## A real refinement upgraded to CONFIRMED

Part 162 had already found real `entity.carevna`="Piggy Princess" text that REFINED (not yet
confirmed) part 69's `StalkerMobsCatalog` hedged "presumably the same royal-boar theme" guess for
`tsarevna`, suggesting a piggy/flesh theme instead of boar. Part 165 upgrades this to fully
CONFIRMED: real `MobConfig` evidence (a `mobName="flesh_tsarevna"`-shaped field) directly settles
the flesh/piggy theme over the original boar guess.

## Testing

A 4th test was added to the EXISTING `WorldFlavorCatalogTest` (no new test class) verifying:
- `entityNameForMobConfigId("base_tsarevich")` == "Boar Tsarevich"
- `entityNameForMobConfigId("base_tsarevna")` == "Piggy Princess"
- `entityNameForMobConfigId("base_tick")` == "Tick"
- `entityNameForMobConfigId("base_boar")` == null (unconfirmed id, no guess)

Run clean on both a plain and a `--release 11` build in the cloud and, synced across, on the
device's actual Java 11 JRE: 1059 -> 1060.

Neither `GameLoop` nor `SingleplayerServer` has ever had its own dedicated automated test class
(confirmed via codebase search) -- consistent with every one of `GameLoop`'s six prior opt-in
setter features (`setLootTables`, `setMobHealthTable`, `setEmissionCycle`, `setVendorArchive`,
`setEventBus`, `setLangCatalog`) also never getting one. Both classes are exercised via manual
demos and `ConsoleDriver`, not `AllTests`.

## Live smoke test against the real install (extra step, beyond the standard pipeline)

`Main` was run against the real STALZONE install directory on the user's device with
`stalzoneInstallDir` configured and `enableConsoleDriver=false`, to visually confirm the wiring
against real game data end-to-end (beyond the unit test). It failed fast with a clear
`IOException` naming the exact missing path:

```
missing zone data (stalzoneInstallDir/map_cache/5.0/dungeon_bolota_tutorial): ...
	at net.stalzone.emulator.session.SingleplayerServer.requireExists(SingleplayerServer.java:268)
```

Investigation confirmed this is a REAL ENVIRONMENTAL LIMITATION of this particular install
snapshot, NOT a part-165 defect: `depot_1818452` contains only the raw `assets`/`bin_global`/
`bin_ru`/`modassets` depot contents -- there is no `map_cache/` directory at all anywhere under
it, meaning the actual game client has never been launched on this machine to generate a runtime
map cache. `SingleplayerServer.requireExists()` behaved correctly, reporting the exact missing
directory with a clear message rather than failing silently or partially. No further action was
taken to generate a `map_cache` (outside this part's scope); `entityNameForMobConfigId`'s own
logic remains fully covered by the unit test above regardless of this environmental gap.

## Closing note

This is the first `GameLoop`/`SingleplayerServer` engine-behavior wiring since part 90's
ability-name annotation, following 74 content-only catalog parts (91-164). Future work could
continue wiring already-built catalogs into engine behavior wherever a confirmed crosswalk exists
(the same discipline used here), pursue a fresh `en.lang` full-file re-scan for anything part
123's now-complete census missed, or shift to a different confirmed-but-uninvestigated asset area.
