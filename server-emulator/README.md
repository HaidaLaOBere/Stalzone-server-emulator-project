# server-emulator

Phase 1 scaffolding for the STALZONE server emulator (see `../docs/PROJECT_PLAN.md`).
**This is not a working game server.** It starts, binds a UDP socket, and logs raw
bytes it receives. There is no login, no session, no gameplay -- the real client's
wire protocol is still encrypted and unread (see `../docs/RESEARCH_LOG.md`, part 7),
so there's nothing to implement against yet beyond this groundwork.

## What's actually here

- `Main.java` -- entry point: loads config, starts the UDP listener.
- `config/ServerConfig.java` -- loads/writes `config/server.properties`. Holds the
  "spawn rate / damage / loot rate" multiplier fields the project exists for, but
  **they aren't wired to anything yet** -- there's no gameplay logic to apply them to.
- `network/UdpListener.java`, `network/PacketLogger.java` -- a bare `java.nio`
  non-blocking UDP listener (see "Why no Netty" below) that hex-dumps whatever it
  receives. Tested: it correctly logs a real UDP datagram sent to it (see commit
  message / RESEARCH_LOG for the smoke-test evidence).
- `nbt/` -- a Java port of `tools/nbt_dump.py`'s NBT reader, PLUS a new writer (the
  Python tool was read-only). Validated byte-exact and round-trip-safe against a real
  file (`itemnames.dat`, staged and parsed during this session -- 577,074 decompressed
  bytes, 0 leftover after parsing, and write-then-reread reproduced the exact same
  577,074 bytes). `nbt/NbtDumpTool.java` is a CLI you can point at any save/content
  file the same way `tools/nbt_dump.py` is used.
- `world/MilestoneMap.java` -- documents the confirmed real 46-zone map roster (from
  `map_cache/5.0/`, not the old placeholder list; grew from 30 to 46 as of part 26 once
  three newly-produced client-log sessions surfaced 16 more real cached zones, incl. the
  first named `dmz`-mode zones and a client-side stale-name quirk resolution for
  `map-new_bar`) and the milestone-1 pick (`dungeon_bolota_tutorial`, fallback `bar`)
  and why.
- `world/region/` -- **a working, validated `.mdat` world-region-file reader.**
  `RegionFile.java` opens a real region file (`map_cache/5.0/<zone>/reg.X.Z.mdat`),
  parses its chunk-offset table, zstd-decompresses each present chunk, and splits it
  into `ChunkSection`s (raw block-id/metadata/"add" nibble arrays) + biome data. This
  is a direct port of the public `onejeuu/sc-file` project's Python decoder, and was
  validated byte-for-byte against that decoder's own output on real data from this
  install (not just "it compiled and looked plausible") -- see
  `../docs/RESEARCH_LOG.md` part 16 for the full validation writeup. `RegionDumpTool.java`
  is a CLI to point at a `.mdat` file or a whole zone folder and see a summary
  (chunk/section counts, block-id histogram) without writing any code.
- `world/WorldModel.java` -- turns one or more loaded `RegionFile`s into a queryable
  whole map, addressable by ordinary world block coordinates instead of per-region/
  per-chunk indices: `WorldModel.load(zoneFolder, mapName)` loads every `.mdat` in a
  zone folder and indexes its chunks; `blockIdAt(x, y, z)` / `fullBlockIdAt(x, y, z)`
  answer "what block is here"; `highestSolidY(x, z)` answers "where would something
  standing here land" (the obvious building block for spawn placement, not wired to
  any spawn logic yet). Coordinate math (region -> global chunk -> world block, and
  back) was checked against the exact chunk/section this project already
  byte-verified against `onejeuu/sc-file`'s own decoder (part 16) -- not just "it
  compiled and returned plausible numbers." `world/WorldDumpTool.java` is the CLI for
  it, same pattern as the other dump tools.
  **What this is NOT yet**: nothing in the emulator's networking/game-loop code calls
  into `WorldModel` -- it's a queryable model, not a game system with players in it.

- `json/JsonReader.java` -- a minimal dependency-free JSON parser (no library was
  fetchable here, see "Why no Netty" below for the same sandbox constraint applied to
  a JSON dependency). Reads plain JSON documents into nested `Map`/`List`/`String`/
  `Double`/`Boolean`/`null` -- enough to load `data/weapons/weapons.json` and similar
  data files this project produces. Not a general-purpose/streaming parser.
- `items/WeaponRegistry.java`, `items/WeaponEntry.java` -- loads the real, 337-record
  `data/weapons/weapons.json` dataset and applies `ServerConfig.damageMultiplier` to
  it via `effectiveDamage()` (firearms) and `effectiveMeleeCommonDamage()` /
  `effectiveMeleeStrongDamage()` (melee light/heavy attacks). **This is the first
  place in the emulator where the project's headline promise -- "full control over
  ... damage values" -- is something you can actually run and see change a number**,
  rather than a config field sitting unused. Validated against the real dataset (not
  just "it compiled"): loaded all 337 records with zero null/duplicate ids, confirmed
  the firearm/melee/device-with-no-damage-field split (272/54/11) independently in
  Python against the same file, and spot-checked the multiplier math for one weapon
  of each shape (firearm, melee, device) -- see `docs/RESEARCH_LOG.md` part 19.
  `items/WeaponRegistryDumpTool.java` is the CLI: prints base-vs-effective damage for
  named weapons at a chosen multiplier. **Update, part 30**: also prints every real field
  found that part -- headshot/limb multipliers (with the effective number), the raw
  `damageFalloff` curve (not interpolated, see that method's doc), and the melee-only extras
  (reach, piercing, bloodlust chance, crit and vs.-mutant multipliers) -- a full "everything
  this project knows about one weapon" reference, not just a damage-multiplier demo.
  **Update, part 30**: this IS now called from a live loop -- `session/GameLoop.tickCombat`
  fires real `effectiveDamage`/`effectiveMelee*Damage`/`effectiveHeadshotDamage`/
  `effectiveLimbDamage` numbers at a spawned mob's health pool (see the new
  `mobs/MobInstance.java` bullet below for why that pool itself is a disclosed
  placeholder -- real mob configs have no health field). Also gained real per-weapon
  headshot/limb multipliers, melee crit/mutant multipliers, and the raw damage-falloff
  curve as typed accessors on `WeaponEntry` (part 30) -- see that class's doc for a
  real JSON string-vs-number encoding quirk found while adding them.

- `mobs/MobRegistry.java`, `mobs/MobConfig.java` -- loads all 35 real mutant AI configs
  from `modassets/assets/stalkermobs/configs/*.smm` (confirmed plain JSON, see
  `../docs/RESEARCH_LOG.md` part 10 and part 20). These files carry NO combat numbers
  (no health, no attack damage -- every attack's `effector` block is empty in every
  file checked), so unlike `WeaponRegistry` this is NOT a damage-scaling layer -- it's
  a moddable-behavior layer: per-mob flags (`canWalk`, `canRunAttack`, `bodyEater`, ...)
  and faction relations (`mutantRelations()`, e.g. making a mutant hostile or allied to
  another type). **Important gotcha found and handled**: the config's own `mobName`
  field is NOT unique across files -- `base_boar.smm`/`base_boar_clone.smm` both report
  `mobName="boar"`, and `base_tick.smm`/`base_tushkan.smm`/`base_tushkan_clone.smm` all
  report `mobName="tushkan"` -- so `MobRegistry` indexes by the source filename stem
  (`configId`, always unique, one per real file) and exposes `mobName` lookup
  separately as a list. Also handles a real formatting quirk: 2 of the 35 files
  (`base_puppet.smm`, `base_puppet_clone.smm`) contain a stray `//` developer comment
  that breaks strict JSON parsing -- `MobRegistry` strips `//` line comments outside
  quoted strings before parsing (verified this is the only such occurrence across all
  35 files, and that it's never inside a string value like a URL). `mobs/
  MobRegistryDumpTool.java` is the CLI.

- `items/ItemWeightTable.java` -- loads `modassets/assets/stalker/weight.txt`, a real plain-
  text per-item inventory weight table (415 unique item ids, keyed by the client's INTERNAL
  numeric id space -- not the opaque ids `weapons.json` uses). Confirmed CRLF line endings and
  `//`-commented section headers by direct inspection (part 21). **Real, disclosed data quirk**:
  3 of the 418 data lines repeat an item id with a genuinely different weight value (consistent
  with item ids only being unique within a category, not globally -- see `itemnames.dat`'s
  `internal.<categoryId>.<itemId>.<variant>` key format) -- `weightOf()` returns the last value
  in file order, and `conflictingIds()`/`allValuesOf()` expose the ambiguity rather than hiding
  it. `items/ItemWeightTableDumpTool.java` is the CLI.

- `hazards/DetectorConfig.java`, `hazards/DetectorTier.java` -- parses
  `modassets/assets/stalker/detectors.txt`, a real custom-format config (NOT JSON: bare
  `<type>{ key: value; ... }` blocks, UTF-8 with a BOM, CRLF line endings -- confirmed by
  direct inspection, part 22) that drives the client's Geiger-counter-style proximity
  detector for 3 hazard types (`rad`, `thr`=chemical, `bio`). 16 real tiers on this
  install. `DetectorConfig.tierFor(type, distance)` resolves which tier applies at a
  given distance -- confirmed against the source data itself that closer really does
  mean faster beeping (rad's `period_base` strictly decreases from 12s to 1s as
  `start_distance` goes from 5 to 0), not just assumed from the name. Small
  hand-written scanner, not `JsonReader` -- the format isn't JSON.
  `hazards/DetectorConfigDumpTool.java` is the CLI.

- `movement/MovementConfig.java`, `movement/MovementSetting.java` -- parses
  `modassets/assets/stalcraft/moving.txt`, a real player-movement tuning config: walk/run/
  sprint/sneak/crawl speed factors, jump control factors per jump type, climbing/swimming/
  diving speed, fall-damage thresholds, and a full stamina ("exhaustion") + hunger gain/loss
  model. Confirmed real format by direct inspection (part 23): plain UTF-8, LF endings, no
  BOM, 238 `key:value` settings plus 3 trailing `key=value` lines (each preceded by a genuine
  Russian developer comment). **Real "variant override" quirk**: 23 keys have values shaped
  like `base;LETTER:override;...` -- the 4 real letters (`c`,`e`,`h`,`m`) match the file's own
  declared creative/easy/hard/medium(adventure) mode-and-difficulty keys, confirmed via
  `move.config.key.name:e:Easy;h:Hard;m:Medium`. One key (`move.config.key.name` itself) has
  only variant pairs and no base value -- `MovementSetting.baseValue()` returns null for that
  case rather than inventing one. `movement/MovementConfigDumpTool.java` is the CLI.

- `items/ItemGroups.java` -- parses `modassets/assets/bundle/item_groups.txt`, a real
  category map for the internal numeric item id space (41 real groups on this install:
  `weapons`, `melee_weapons`, `armor`, `ammo`, `artifacts`, `medicine`, `lootboxes`,
  `clan_stuff`, `barter`, etc.), each a comma-separated list of inclusive id ranges/singles.
  **Real, disclosed quirk**: 11 item ids belong to more than one group (`barter` is a
  curated cross-cutting list layered on top of the primary categories, not disjoint from
  them) -- `groupsOf(itemId)` returns every match rather than picking one. Cross-validated
  against `customnpcs/sorting_map.txt` (part 21): that file's id groupings land inside this
  file's category boundaries (e.g. sorting_map.txt's first line all falls inside `crates`,
  16600-16611) -- two independently-found real files agreeing on the same boundary.
  `items/ItemGroupsDumpTool.java` is the CLI.

- `anomalies/MorseAlphabet.java` -- parses `modassets/assets/anomalies/morse_alphabet.txt`, a
  real Morse code table (82 entries: 33 Cyrillic letters incl. `ё`, 26 Latin letters, 10 digits,
  13 punctuation marks) that almost certainly backs an in-game radio/signal-decoding activity --
  genuine "game content" rather than background/launcher internals, even though small. Confirmed
  format by direct inspection (part 24): UTF-8, CRLF, each line `<char><4 spaces><code>`.
  **Real, disclosed quirk**: character->code is 1:1, but 27 codes are shared by more than one
  character (Cyrillic/Latin pairs that trace back to the same telegraphic code, e.g. `а`/`a`
  both `.-`; one code, a single dot, is shared by THREE characters -- `е`, `ё`, `e`) --
  `charsFor(code)` returns every match rather than picking one. `anomalies/MorseAlphabetDumpTool.java`
  is the CLI.

- `audio/SoundDurations.java` -- parses the client's `bundle/sound_duration.csv` (1,481 unique
  keys) and `bundle/speech_duration.csv` (7,447 unique keys), plain `<key>,<durationMs>` CSVs
  with no header. Confirmed real format by direct inspection (part 25): UTF-8, no BOM, LF
  endings, disjoint key spaces between the two files (0 overlap). Genuinely useful for anything
  that needs to know how long a scripted audio cue takes -- and a source of real, previously-
  uncatalogued content: this file revealed a named mission this project hadn't found before,
  `operation_lesta` (5 audio-diary parts), and confirms a battle-royale match structure via
  English announcer keys (`en_landing01`..`en_landing15`, `en_airdrop01`..`en_airdrop05`,
  `en_evacuation`). **Real, disclosed quirk**: 15 keys (`guitar_1`..`guitar_15`, the in-game
  guitar minigame) each appear TWICE in `sound_duration.csv` -- confirmed harmless, both
  occurrences always carry the identical duration; `load()` tolerates an exact-duplicate
  re-declaration but throws on a genuinely conflicting one (none exist in the real file).
  `audio/SoundDurationsDumpTool.java` is the CLI.
- `economy/VendorArchive.java` -- parses `config/vendor_archive.dat`, a real per-account
  zstd+NBT save file recording every item this account has ever bought from a trader (which
  trader, what item, how much it cost, in what currency). The first class to actually load this
  file end-to-end -- `nbt/CompressionUtil.java`'s own doc comment already named it as a real
  zstd-magic-bytes example before this class existed. Surfaced new real content on its own:
  3 characters on this account, 12 distinct trader ids (incl. two new named locations,
  `lodochnaya` and `rostok`, not found anywhere else in this project as of part 27), and 3 real
  currencies (`money`, `sc_birthday_21`, `season_pass_ticket_3`). **Real, disclosed quirk**: an
  item stack's optional `tag` compound has a richer schema than its first-seen keys suggested --
  besides `upgrade_bonus`/`u1`/`u2`, real tag blocks also carry `repairs_amount`,
  `currentVisualDefault`, `currentMotifDefault`, and `stash_durability` (this one with real
  varying values). `VendorPurchase.rawTag()` exposes the whole parsed compound rather than
  assuming a fixed schema. `economy/VendorArchiveDumpTool.java` is the CLI.

- `world/SettlementRegistry.java` -- a real, 15-name "settlement" roster (`csky`/`lodochnaya`/`garage`/`foton2`/`savezone_td`/`savezone_agro`/`rostok`/`bar_save_residential_hangar`/`bar_sever`/`merc`/`duty`/`freedom`/`covenant`/`north_factions_block`/`battlefields`), confirmed verbatim from a freshly-logged client startup (`Reading settlement <name>`, part 28). Distinct namespace from `MilestoneMap`'s 46 map_cache zones -- only 2 of the 15 names overlap. Resolves part 27's `lodochnaya`/`rostok` mystery; gives circumstantial (not conclusive) evidence for part 26's unidentified `td` map_cache code via `savezone_td`. No per-settlement detail (faction owner, bounds) found anywhere -- name roster only, no behavior implied.

- `session/PlayerState.java` + `session/Inventory.java` + `session/GameLoop.java` -- part 29: the first thing in this project to actually CONSTRUCT and CALL INTO the nine-plus previously-unused data layers together (`WorldModel`, `MovementConfig` via a new `movement/MovementSimulator.java`, `DetectorConfig`, `ItemWeightTable`, `MobRegistry` via a new `mobs/SpawnScheduler.java`). This is a LOCAL simulation loop, not networked -- it does not speak the real (still-encrypted) wire protocol. `MovementSimulator` is explicit about what's real (the relative speed factors from `moving.txt`) vs. emulator-chosen (the absolute base blocks/second, unevidenced anywhere). `SpawnScheduler` finally wires `ServerConfig.spawnRateMultiplier` into real behavior -- disclosed as this emulator's own generic spawn-cadence design, since no real per-zone spawn table has been found anywhere in this project. See `session/GameLoopDemo.java` below.

- `mobs/MobInstance.java` + combat additions to `items/WeaponEntry.java`/`items/WeaponRegistry.java`/`session/GameLoop.java` -- part 30: wires "damage values" (the other half of the project's original ask, alongside spawn rates) into the local `GameLoop`. Real .smm mob configs have NO health field anywhere (confirmed part 10/20/29) so `MobInstance` tracks a disclosed, arbitrary placeholder health pool (`DEFAULT_MAX_HEALTH = 100.0`, flat across every mob type) -- not evidenced data, an emulator design choice. `GameLoop.tickCombat` applies real, evidenced `WeaponRegistry` damage numbers to it. **Real data-format quirk found while building this**: `headshotMultiplier`, `limbMultiplier`, `overheatAfterShots`, `meleeCriticalHitMultiplier`, `meleeMutantDamageMultiplier` are consistently JSON STRINGS in `weapons.json` (e.g. `"1.4"`), not JSON numbers, unlike every other numeric field including `damageFalloff`'s sub-fields -- confirmed across all occurrences, not a one-off. `WeaponEntry` now exposes typed accessors for all of these plus the real melee extras (`meleeReachCommon/Strong`, `meleePiercing`, `meleeBloodlustChancePct`) and the raw `damageFalloff` curve (exposed, NOT interpolated -- no evidence for the curve's shape, so no formula is guessed). `GameLoop.tickCombat` gained a `HitLocation` (`BODY`/`HEAD`/`LIMB`) parameter that applies the real headshot/limb multiplier when the weapon record has one (275 of 337 records do); hit-location selection itself is entirely the caller's choice, no aim/hit-detection model exists. Melee attacks also apply the real `meleeMutantDamageMultiplier` on top of the light-attack midpoint -- unlike `meleeCriticalHitMultiplier` (still unwired, no crit-chance field exists to trigger it), this one is honestly applicable: every combat target in this emulator is a `MobInstance` wrapping a real `stalkermobs` config, i.e. a mutant by this dataset's own definition, so "is this a mutant" isn't a guessed context here, it's the only one that exists. See `session/GameLoopDemo.java` below (now also loads real `weapons.json` and fires a scripted headshot-then-body-shots sequence at each spawn). **Update, part 39**: `damageFalloff`'s
  five-field shape is now OFFICIALLY confirmed (not just inferred from field names) by EXBO's own
  public API docs (`eapi.stalzone.net/items.html`, the "Damage Ramp Block" type) -- see
  `WeaponEntry.damageFalloff()`'s updated doc. `WeaponEntry.damageAtDistanceLinearAssumption()` /
  `WeaponRegistry.effectiveDamageAtDistance()` now compute a real damage-at-distance number using
  those officially-confirmed flat/ramp/flat/zero boundaries, with linear interpolation for the one
  still-undocumented middle segment -- disclosed by name and doc as an assumption, not confirmed
  fact. Real, tested edge case: 1 of 268 falloff records (`knmdv`, Gauss Rifle) has a zero-width
  ramp (`damageDecreaseStart == damageDecreaseEnd == 200`) and flat `startDamage == endDamage`, a
  thematically-fitting no-falloff railgun; the boundary conditions handle it correctly without a
  division-by-zero special case, confirmed by testing that exact record.

- **`session/GameLoop.java` -- part 41: range/damage-dropoff is now actually wired into combat.**
  A new `GameLoop.tickCombat(player, weapon, target, distanceMeters, location, sink)` overload
  applies `WeaponRegistry.effectiveDamageAtDistance` (the part-39 curve) instead of the flat
  `effectiveDamage` before any headshot/limb multiplier, so a shot at long range now really does
  less damage. The existing flat `tickCombat` overload is UNCHANGED (still always 0m) -- both stay
  available, refactored to share one internal `resolveFlatDamage` helper so the range-independent
  logic isn't duplicated. Restating the part-39 disclosure since it now drives an actual gameplay
  number rather than a dump-tool printout: the flat/ramp/flat/zero boundary SHAPE is officially
  confirmed by EXBO's own docs, but the interpolation strictly between `damageDecreaseStart` and
  `damageDecreaseEnd` is still this project's own disclosed linear-interpolation assumption, not
  verified fact -- wiring it in doesn't change that, it's the same kind of disclosed simplification
  already accepted for the melee light-attack midpoint pick. `distanceMeters` is entirely
  CALLER-supplied, same pattern as `tickHazardCheck`'s hazard coordinates -- `MobInstance` has no
  tracked world position (nothing in this project's evidence gives mobs one), so there's no real
  per-shot distance this class could compute itself. Weapons with no falloff curve (69/337) and all
  melee weapons are unaffected -- they fall back to the identical flat-overload behavior, not a
  guessed range effect. `session/GameLoopDemo.java` now exercises this: each engagement opens from
  5m (headshot) out to 5m+20m-per-shot on the same target, a disclosed demo distance sequence (not
  real telemetry) chosen specifically so a short demo run crosses a real weapon's
  `damageDecreaseStart`/`damageDecreaseEnd` boundaries and the applied damage number visibly
  changes tick to tick -- confirmed by an actual run against real AK-12 data (37.5 base): 52.5 at
  5m (inside `decreaseStart`, headshot x1.4), 36.5 at 25m (inside the ramp, body shot), matching
  the disclosed linear formula by hand-calculation, not just "it ran without crashing."

- `dialogue/SubtitleCue.java` + `dialogue/SubtitleTrack.java` + `dialogue/SubtitleRegistry.java` --
  part 38 (session continuation after part 37's research pass): loads every real quest/NPC dialogue
  subtitle file this project has found (part 34) -- 108 real `.srt` files under each of 5 real locale
  folders (`en`/`es`/`fr`/`ko`/`ru`), 540 files total, all loaded and validated end-to-end against
  the real install (not just "it compiled"). This is a genuine "game content, not EXENS" system by
  the project's own rule 2. It is intentionally an inert content catalog only -- no evidence exists
  anywhere in this project for what actually triggers a given line in a live session, so it is NOT
  wired into `session.GameLoop`; it answers "what dialogue exists and how is it timed", not "when
  does it play". **Two real, isolated data quirks found and handled while building this (both
  confirmed by scanning all 540 files, not assumed from one example)**: 3 of the 540 files (all real
  `ru`-locale `terentii*`/`terentiy*` files) use CRLF line endings instead of the otherwise-universal
  LF; and `terentiy_skuchno.srt`'s `ru` track is missing cue #1's leading index line entirely (the
  only such occurrence anywhere), tolerated only on a file's first block, not silently anywhere else.
  **Third real quirk, disclosed rather than hidden**: for 107 of the 108 real npcIds, cue timestamps
  are byte-identical across all 5 locales (a shared audio timeline with localized text tracks) --
  the one exception, `bigeagle_newbie`, has an untranslated `ru` file (byte-identical English text to
  the `en` file, confirmed by direct diff) whose cue #10 onward also carries different timings and one
  doubled-space text typo not present in `en`. Rather than failing the whole registry load over this
  one npcId, `SubtitleRegistry.npcIdsWithCrossLocaleTimingMismatch()` surfaces it explicitly, the same
  "disclose an ambiguity, don't hide it" standard as `ItemWeightTable`/`ItemGroups`. Per the project's
  copyright discipline, this class reads the user's own installed game asset files at runtime and does
  not embed or reproduce any dialogue text in its own source. `dialogue/SubtitleRegistryDumpTool.java`
  is the CLI. **Update, same part**: `dialogue/NpcIdGrouping.java` mechanically groups the real
  108 npcIds by the raw filename token before their first `_` (e.g. `duty_2_6yushka` groups under
  `duty`) -- explicitly NOT claimed to be a verified in-game "quest arc" system (no evidence of an
  actual arc/chapter data structure exists anywhere in the client's real files), just a real,
  useful naming-convention observation. 10 tokens cover 88 of the 108 names with 5+ entries each
  (`bandits`/`bigeagle`/`duty`/`merc`/`nina`/`palach`/`stalkers`/`twins`/`zarya`/`zavet`); the other
  20 names split into 10 small/single-entry tokens reported as-is rather than force-fit into a
  guessed grouping -- including a real spelling-variant split (`terentii` vs `terentiy`, almost
  certainly the same character transliterated two ways across 3 files) and one genuinely awkward
  one-letter token (`p`, from `p_fil_2`/`p_fil_3`). `SubtitleRegistryDumpTool groups <_localized dir>`
  prints the full breakdown.

- `world/props/VehiclePropGroup.java` + `world/props/VehiclePropCatalog.java` -- part 38: catalogs
  the real vehicle-model assets under `modassets/assets/customitems/models/blocks/vehicles/` -- 3
  real top-level categories (`cars`/`boats`/`heli`), `cars` further split into 5 real subcategories
  (`cargo_cars`/`light_cars`/`military`/`other`/`toy`). Real, named vehicles found this way include
  authentic Soviet-era vehicles: `zil131`/`zil131_firetruck`/`maz515` (cargo trucks), `moskvich_2136`/
  `moskvich_2140`/`niva`/`uaz_452`/`uaz_469`/`vaz_2106`/`zaz_968` (cars), `batm`/`brdm`/`btr70`/`t72`/
  `mir_tankov` (military armor), `dragline`/`dt75`/`laz`/`paz`/`t40` (industrial/civilian), boats
  (`aist`/`boat_wood`/`kazanka`/`saturn_rusty`/`saturn_not_rusty`), and a helicopter (`mi6`, plus real
  `mi8_dek_*` mesh files that exist only as loose files with no `mi8` subfolder of their own). Per
  part 33's research, these are, with high confidence, decorative Level Art / wreck props, NOT
  drivable vehicles -- this catalog exists so the real model names/paths are on record for any
  future drivable-vehicle work, without itself adding any vehicle behavior. **Real structural quirk,
  disclosed rather than merged by a guessed rule**: the same vehicle sometimes has BOTH a same-named
  subdirectory (texture variants, e.g. `boats/aist/default`, `boats/aist/rust`) AND separate loose
  mesh files one level up sharing a name prefix (e.g. `boats/aist.mcsb`) -- `VehiclePropGroup`
  exposes `modelFolders()` and `looseFileNames()` as two separate, honest facts rather than guessing
  which loose files "belong" to which model folder (e.g. `kazanka_a.mcsb` vs `kazanka_b.mcsb` could be
  two variants of one boat or two different boats -- nothing in this project's evidence says which,
  so no naming-convention merge is invented). A small, explicitly-named set of confirmed non-vehicle
  auxiliary folders (`addons` under `cars/cargo_cars`; `crashed_parts`/`warmup` under `heli`) is
  excluded from the model list, not silently miscounted as vehicles. `world/props/
  VehiclePropCatalogDumpTool.java` is the CLI.

- `audio/BankCatalog.java` -- part 38: catalogs the real FMOD audio banks under
  `modassets/assets/gloomycore/banks/` (472 real `.bank` files, flat, part 35) and cross-references
  them against real evidence of which ones actually loaded in a session, by scanning the client's
  own `logs/client-*.log` files for `Loading bank <path>` lines (the same evidence method part 35
  used by hand). **Deliberately dynamic, not a hardcoded snapshot**: part 35 recorded "75 confirmed
  live" as a point-in-time research finding; rather than freezing that specific list into this class
  (which would silently go stale the moment a new session log shows a previously-unseen bank
  loading), `BankCatalog.loadConfirmedLiveBankIds` re-derives the confirmed-live set from whatever
  real `client-*.log` files exist at call time. On this install, right now, that re-derivation lands
  on exactly 75 again -- confirmed by re-running the scan while building this class, not assumed to
  still match part 35's number just because it happens to. Also cross-checks the log-confirmed ids
  actually exist as real files on disk (`confirmedLiveButMissingFromDisk`, confirmed empty on this
  install, not assumed). The FMOD `.bank` binary format itself is still NOT parsed -- this is a
  filename-level catalog + real load-evidence cross-reference only. `audio/BankCatalogDumpTool.java`
  is the CLI.

- `session/save/PlayerInteraction.java` + `session/save/PlayerNotification.java` +
  `session/save/PlayerSaveFile.java` -- part 40: reads real per-character save data, discovered
  this part at `config/<CharacterName>/{common,sd1,sd2,sd3,sd4}` on a real STALZONE install (plus
  `_old` backup copies) -- not documented anywhere this project had found before. Every one of the
  five files turned out to be Zstandard-compressed NBT, the SAME format already validated for
  `economy/VendorArchive` (part 24), so no new binary-format work was needed -- `PlayerSaveFile`
  is a thin content-shape-aware wrapper over the existing `nbt/NbtReader` + `nbt/CompressionUtil`
  pipeline. Of the five real slots, two got well-evidenced typed wrappers at the time (part 40;
  `sd3`/`sd4` followed in part 46, below), per this
  project's rule 1: `sd1`'s real top-level `interacts` list (`PlayerInteraction`: `type`/
  `username`/`allianceId`, confirmed from 30 real entries across two real characters -- 6 distinct
  `type` values seen: `DIED_FROM_PLAYER`/`PLAYER_KILLED`/`REVIVED_BY_PLAYER`/`REVIVED_PLAYER`/
  `BOUGHT_LOT_FROM`/`SOLD_LOT_TO`, kept as a raw string rather than a closed enum since 2 real
  characters is a small sample) and `sd2`'s real top-level `notifications` list
  (`PlayerNotification`: typed accessors for the 3 well-evidenced channels -- `quests`
  (`questId`/`questTitle`/`questNotificationType`/`questTypeCode`), `outpost` (`outpostX`/
  `outpostZ`, real world coordinates), `handbook` (`handbookLink`) -- with `generic`/`savepoint`/
  `mail` left as raw `payload()` only, one real sample each being too thin an evidence base for a
  typed accessor). **Real cross-file validation**: `config/quests.cfg`'s `readQuests` list (bare
  numeric ids) was checked against a real `sd2` sample from the same character -- every quest id
  `sd2` names with full metadata also appears in `quests.cfg`, and `quests.cfg`'s one
  `hiddenQuests` entry is exactly the quest `sd2` shows cycling new-\>failed-\>new-\>failed-\>
  ...-\>finished (a `quest_type=2` entry). **Privacy note**: `sd1`'s real entries name other real
  players by their actual account username -- this project's docs/comments/tests never reproduce
  an actual username seen while validating this class, only the field schema and the type-value
  set above; the CLI tool itself has no reason to withhold a real server operator's own data,
  since it's a diagnostic tool over data the caller already has. `session/save/
  PlayerSaveFileDumpTool.java` is the CLI.

- **`session/save/PlayerSaveFile.java`, extended -- part 46**: real typed accessors for the
  remaining two save slots, now that real samples exist across THREE real characters/accounts.
  `sd3`'s real `observedOffers` list (`observedOffers()`) -- real Cyrillic shop/cosmetic-case
  offer names, cross-validated: the same offer names (e.g. `"Набор «Суеверия»"`) recur across all
  three real characters, confirming a shared real shop catalog rather than per-character noise;
  the real samples also surfaced real World of Tanks-branded crossover cosmetics (`customization/
  Набор Мир Танков Маневры`, `customization/Набор Мир Танков Поля`), catalogued as real observed
  offer names only. `sd4`'s 4 real `TAG_BYTE` `lastSeen*Version` counters (`lastSeenTagsVersion`/
  `lastSeenBackgroundsVersion`/`lastSeenPatternsVersion`/`lastSeenStickersVersion`) plus real
  **byte-level-confirmed decoding** of `observedElements` (`observedElementIds()`): a flat
  sequence of little-endian `uint16` cosmetic-element ids, confirmed not by assumption but by
  decoding two independent real characters' arrays and finding 3 EXACT ids in common (`4200`,
  `4500`, `4800`) between them -- a coincidence this unlikely from a wrong byte grouping is real
  evidence the decoding is correct. Compiled clean and smoke-tested end-to-end against all real
  samples; `PlayerSaveFileDumpTool.java` prints both new shapes.

- **`session/save/PlayerSaveFile.java`, extended again -- part 47**: `common`'s real shape
  finally gets typed accessors too, cross-validated across two real characters. ~20 real
  one-shot `TAG_BYTE` flags via the new generic `getFlag(String)`; two new small record
  classes for the best-evidenced nested data -- `session/save/CaseOpenCount.java` (real
  per-case open-count tracking, real `caseId` 15081 seen on both characters with different
  real `openAmount` values, confirming a genuine per-character counter) and
  `session/save/OpsMapDifficulty.java` (real per-map Operations difficulty, real map ids
  `big_cleanup`/`sea_alienation`). **New real finds, from real flag/key names alone**: `
  hasSeenDMZHowToPlay` and `hasSeenReichstagHowToPlay` name two real game modes/locations
  ("DMZ", "Reichstag") not documented anywhere in this project before; `snowmanFeed`/`
  nyVisitedTabs` real evidence of a New Year seasonal event; `phonographListenList` real
  evidence of an in-game phonograph/jukebox feature; `personalShopOpenedCards` real evidence
  of a player-to-player personal shop; `complaintsData` real evidence of an in-client
  player-reporting UI (report categories only, no moderation/anti-cheat internals touched,
  consistent with rule 2). `arcadesMenuOpened` is a THIRD independent real source (after part
  42's keybindings and part 45's `prefs`) corroborating a real "arcades" mode. Compiled
  clean and smoke-tested end-to-end against both real characters' `common` files.

- **`loot/` -- part 48: the third and last of the project's headline tuning knobs,
  `ServerConfig.lootRateMultiplier`, finally wired up.** Unlike `spawnRateMultiplier`
  (part 29) and `damageMultiplier` (part 19), there is no real STALZONE loot/drop-rate table
  to scale -- this project looked (the "163 additional loot configs" log-line investigation,
  part 43) and concluded one is very likely absent from client files entirely, the same as the
  already-repeatedly-confirmed absence of a real mob spawn table. **This package is therefore
  emulator-original design, not reverse-engineered data, and its own class docs say so up
  front and repeatedly** -- it exists because a private/singleplayer server needs SOME loot
  behavior to be playable, and the project's own stated goal ("full control over what can be
  done... later modding support") extends naturally from spawn rates and damage to this.
  `LootEntry`/`LootTable`/`LootTables` (plain JSON, operator/modder-editable, loaded the same
  way `weapons.json` is) + `LootRoller` (rolls a drop-chance check scaled by
  `lootRateMultiplier`, clamped to `[0,1]`, then a weighted pick among a table's entries) +
  `LootTableDumpTool.java` (CLI: `dump` prints a file's tables, `simulate` rolls a mob id N
  times and tallies outcomes, for tuning without a full demo run). Wired into `GameLoop` via
  `setLootTables(...)` -- a fresh kill now rolls loot (or explicitly reports "dropped
  nothing") if an operator has opted in; nothing changes for a `GameLoop` that never calls
  it. `data/loot/example_loot_tables.json` is a small example file (real mob ids `base_boar`/
  `base_tushkan`, real item ids reused from `GameLoopDemo`'s own demo inventory, but entirely
  invented chance/weight/count values -- see that directory's own `README.md`). Compiled
  clean (94 total sources) and verified two ways: 20 new unit tests (property-style --
  `dropChance=0`/`lootRateMultiplier=0` never drop, `dropChance=1`/clamped-multiplier always
  drop, counts stay in range, every weighted entry is reachable, across 20-200 seeds each)
  plus an end-to-end `GameLoopDemo` run showing both the "unconfigured mob -> dropped
  nothing" and (via `LootTableDumpTool simulate`) the configured weighted-roll path.

- **`mobs/MobHealthTable.java` -- part 49: opt-in per-mob `maxHealth` overrides on top of
  `MobInstance.DEFAULT_MAX_HEALTH`.** Same status as `loot/`: real `.smm` mob config files have
  NO health field anywhere in their schema (confirmed all 35 real files, part 10, re-confirmed
  part 30 while building `MobInstance` itself), so **this is emulator-original tuning, not
  reverse-engineered data**, and the class doc says so up front. A flat JSON object of
  `configId -> maxHealth` (deliberately simpler than `LootTable`'s shape -- nothing else to
  configure per mob yet); `maxHealthFor(id)` returns the configured value or falls back to
  `MobInstance.DEFAULT_MAX_HEALTH` unconditionally, so a caller never needs a null check.
  `MobHealthTableDumpTool.java` (CLI: `dump` prints every override and, given a real mob
  configs dir, flags real mob ids with no override; `get` prints one mob's resolved value).
  Wired into `GameLoop.tickSpawns()` via `setMobHealthTable(...)` -- a freshly spawned mob now
  uses its configured override if one exists, or the same flat default as before this method
  existed if an operator never calls it. `data/mobs/example_mob_health.json` is a small example
  file (6 real mob ids -- `base_boar`, `base_tushkan`, `base_flesh`, `base_dog`,
  `base_pseudogigant`, `base_chimera` -- entirely invented `maxHealth` values -- see that
  directory's own `README.md`). Compiled clean (97 total sources) and verified two ways: 7 new
  unit tests (load/override/fallback/`unconfigured()`/all three malformed-input rejection
  paths) plus `MobHealthTableDumpTool dump` run against the example file AND all 35 real
  `.smm` mob configs, confirming exactly 6 configured / 29 real ids correctly falling back to
  `DEFAULT_MAX_HEALTH=100.0`.

- **`survival/MinFoodConfig.java` -- part 50: reads the last unread real plain-text config
  file in `modassets/assets/stalker/`, confirming this project has now read every one in that
  directory** (alongside `weight.txt`/`ItemWeightTable`, `detectors.txt`/`DetectorConfig`,
  `moving.txt`/`MovementConfig`). `min_food.txt` is tiny (2 bytes on disk, `"10"`, no
  trailing newline) but genuinely new evidence: it confirms a real STALZONE hunger/food
  survival mechanic with SOME minimum-food threshold this project had never found before.
  **What is and isn't evidenced is stated carefully**: the file, its exact content, and its
  location are real; what the number `10` actually MEANS (unit, scale, what happens below it,
  any decay rate) is NOT evidenced anywhere -- a broad search (real `config/keybindings`,
  `config/options.json`, and every `food`/`hunger`/`thirst`/`water`-named real path under
  `modassets/assets/`) found no corroborating field name or context; the only other
  food/hunger-adjacent real path, `stalker/hungerBuffs/`, turned out to hold a single UI icon
  sprite sheet, not data. So `MinFoodConfig` deliberately exposes exactly one accessor,
  `minFood()`, and does not invent a decay rate, max value, or buff mechanic to wrap around
  it -- unlike `loot/`(part 48) and `mobs/MobHealthTable`(part 49), there was no stated
  project goal needing an invented simulation here, so none was added. Compiled clean (100
  total sources) and verified: 5 new unit tests (real-content fixture, whitespace tolerance,
  two malformed-input rejection paths, `toString()`) plus `MinFoodConfigDumpTool` run against
  the real, md5-verified `min_food.txt` itself, confirming `minFood() = 10` end to end.

- **`items/ItemTiers.java` + `tips/AdviceList.java` + `economy/TraderItemGroups.java` -- part 51:
  three more real, previously-unread plain-text files, found by sweeping every
  previously-unexamined `modassets/assets/` subdirectory for text configs.** `gloomycore/
  item_tiers.txt` (4 real names, `novice`/`stalker`/`veteran`/`master`, in that file order --
  the ordering reads naturally as ascending, but this project has no other real file tagging
  any specific item id with one of these names, so `ItemTiers` states that plainly rather than
  asserting it). `gloomycore/advices_en.txt`/`advices_ru.txt` (real player-facing loading-tip
  text -- 6 English tips, including a real `"STALCRAFT"` mention corroborating this project's
  existing name-history evidence; 84 Russian tips, a much larger and mostly DIFFERENT set,
  almost entirely real account-security notices naming the real `support.exbo.ru`/
  `forum.exbo.ru` domains this project has already independently found evidence for).
  `bundle/logged_trader_item_groups.txt` (10 real item-category names) -- cross-checked
  programmatically (not assumed) against the real 41-category `item_groups.txt` (part 23):
  all 10 are present there, consistent with this file being a curated subset rather than an
  unrelated list. Also swept and found two REAL but thin/out-of-scope items, noted rather
  than silently dropped but not built into classes: `party/weapon_levels.cfg` (12 bytes, one
  key-value pair) and `config/cluster` (2 bytes, a server-region code -- launcher-adjacent,
  rule-2 out of scope); and `bundle/levels/br_location.txt` (12 bytes, `"battleroyale"` --
  real evidence of a Battle Royale mode, also noted but not built into its own class this
  part) and `customnpcs/sorting_map.txt` (already cross-validated in `ItemGroups`'s own class
  doc since part 23, still no dedicated typed reader). Compiled clean (109 total sources) and
  verified: 12 new unit tests plus all three `*DumpTool`s run directly against md5-verified
  real files (including a live cross-check of `TraderItemGroups` against the real
  `item_groups.txt`).

- `clientdata/QuestProgress.java` + `clientdata/Waypoint.java` + `clientdata/WaypointList.java` +
  `clientdata/KeyBindings.java` -- part 42: reads three more real local client-state files found
  alongside the per-character save data, this time plain JSON rather than NBT: `config/quests.cfg`
  (`{readQuests:[...], hiddenQuests:[...]}`, bare numeric quest ids -- real cross-validated against
  `sd2` in part 40, restated here since this is what actually reads the file), `config/waypoints.cfg`
  (a JSON array of death-marker waypoints -- only the real `"death"` type confirmed, kept as a raw
  string rather than assumed closed), and `config/keybindings` (314 real action-name -> keycode
  bindings). **Both `quests.cfg` and `waypoints.cfg` were confirmed, by re-reading the same real
  install twice in one session, to be actively written by the running client** -- `readQuests` grew
  by one real id between reads, and `waypoints.cfg` itself showed the same current/`_old`-backup
  rotation pattern already confirmed for the per-character save files, applied here to a top-level
  `config/` file too. **Real find in `keybindings`, new to this project**: of 314 real action names,
  205 are NOT ordinary `key_key.*` gameplay binds -- they're a real, first-hand-evidenced internal
  developer/QA tool suite shipped inside the client (English and Russian names: Mutant Editor, Item
  Editor, Weather Editor, Warp Editor, Scene/Behavior editors, FPS/memory/FMOD/navmesh debug
  overlays, chunk/item/language reload actions, a "No clip" toggle, and debug result screens for
  Prestige/Operations/Arcade systems this project has independently found evidence of elsewhere).
  This is real "possible cut/hidden content" evidence per the project's original scope (rule 2
  explicitly excludes EXENS/anti-cheat/launcher internals, not game-engine dev tooling) --
  `KeyBindings` only CATALOGS these real names and their real bound keys; nothing in this project
  implements, invokes, or unlocks any of them. `clientdata/ClientDataDumpTool.java` is the combined
  CLI for all three (`quests`/`waypoints`/`keybindings` modes). **Part 52 update**: a later real `waypoints.cfg`/`_old` pair, diffed with this
  same `WaypointList` reader, showed the file going from 1 real death-marker entry down to `[]`
  -- confirmed real evidence that this file isn't append-only, it's genuinely clearable client
  state (what specifically clears it is not evidenced, not guessed at).

- `testing/` -- part 43: a small dependency-free unit test framework (`TestCase`, `Assertions`, `TestRunner`, `AllTests`, `TestMain`), built because this sandbox cannot reach Maven Central and so a real JUnit dependency can't be fetched (see "Why no Netty (yet)" below for the same limitation elsewhere in this project). `AllTests` explicitly registers every `*Test.java` class's tests (no reflection scanning, matching this project's existing explicit-registration style); `TestMain` is the CLI entry point and exits non-zero on any failure so it's usable as a CI gate. 44 real regression tests currently live alongside the classes they cover -- `items/WeaponRegistryTest.java` (real AK-12/Gauss Rifle damage-falloff fixture data), `dialogue/NpcIdGroupingTest.java` (real npcId spelling-variant grouping), `mobs/MobInstanceTest.java` (health/damage clamping), `session/save/PlayerSaveFileTest.java` (a real NBT round-trip through `NbtWriter` -> `PlayerSaveFile.load`, since the class's constructor is private and `load(Path)` is its only real entry point), `clientdata/QuestProgressTest.java` (real quest ids), `clientdata/WaypointListTest.java` (a real death-marker JSON fixture), and `clientdata/KeyBindingsTest.java` (real keybinding action-name/keycode fixtures, including the real two-code primary+secondary binding case). All 44 pass. Every fixture uses real evidenced values where the field is real data (ids, names, numbers); the one synthetic username used in the save-file fixture is an explicitly-fake placeholder, per this project's standing policy of never committing a real player's username to code.

- `clientdata/ClientOptions.java` -- part 44: reads the real local client-state file `config/options.json`, a flat JSON object of 159 real client graphics/audio/UI/input settings keys (same current/`_old` rotation pattern already confirmed for the other `config/` files). **Real, byte-level-confirmed finding, new to this project**: 4 real keys hold values far larger than any other numeric field -- `admin_hotbar_scale`, `admin_inv_icon_scale`, and `world_outliner_width` each decode, byte-for-byte, to the exact IEEE-754 binary32 bit pattern of a small round float (2.5, 1.0, and 2.0 respectively -- `0x3f800000` for `1.0f` is a universally-known constant, not a coincidental match), while `world_outliner_color` decodes instead to a plausible packed ARGB color (alpha byte `0x66` over black RGB). `getFloatBits`/`getArgbComponents` are generic bit-reinterpret utilities confirmed correct for exactly these 4 real keys, not asserted as a general rule for every large numeric option. Smoke-tested end-to-end against the real, live `options.json` in the cloud build -- all 159 keys parse and the 4 special keys decode exactly as predicted. `clientdata/ClientDataDumpTool.java`'s `options` mode is the CLI for it.

- `clientdata/ClientPrefs.java` -- part 45: reads the real local client-state file `config/prefs`, confirmed by magic-byte inspection (`28 B5 2F FD`, real Zstandard magic) and a clean parse (0 leftover bytes) to be Zstandard-compressed NBT -- the SAME format already validated for `PlayerSaveFile`/`VendorArchive`, just applied to a NEW real file, using the project's existing `nbt/NbtDumpTool.java` to get an authoritative first structural dump before writing the typed class. Real fields: 8 real one-shot "seen/done" boolean flags, a real `arcadesMapFilterMask` int bitmask plus `arcadesSelectedMaps`/`arcadesFavoriteMaps` string lists (real cross-validation: corroborates the "Arcade Team Battles" debug-screen keybinding names from part 42 -- two independent real sources agree an arcade mode with selectable maps exists), `favoriteCharacters`, `seenArticleLinks` (real Steam news URLs for the real AppID 1818450, mixed with at least one real `handbook://` article link -- not Steam-only, corrected from an earlier draft assumption), `seenExperiences` (real season/episode codes -- e.g. `experience_season_summer26`, `experience_season_autumn26` -- real evidence of a seasonal/episodic content structure), and `unseenHandbookEntries` (real `handbook://` paths, same URI scheme cross-validated in part 40). Also confirmed actively written: the real `prefs`/`prefs_old` pair differed, `seenArticleLinks` growing from 9 to 10 real entries between writes. **Part 52 update**: a later `prefs`/`_old` pair instead
showed `unseenHandbookEntries` shrinking from 3 real entries to 0 -- confirming that field behaves
as its name implies, a to-read list that's cleared as entries are presumably viewed, not just a
growing log like `seenArticleLinks`. `clientdata/ClientDataDumpTool.java`'s `prefs` mode is the CLI for it.

- **`world/MilestoneMap.java`, extended + `economy/BarterItemCatalog.java` -- part 53: re-checked `map_cache/5.0/` against ten newly-produced client-log sessions (client-14 through client-23) and a direct on-disk sweep, and found real new content on two fronts.** First, the real playable-zone roster grew again, 46 -> 67 zones (21 new, 0 removed, every one independently confirmed to carry real non-empty `.mdat` region data) -- see `MilestoneMap.java`'s own part-53 doc comment for the full list, including `bar_sever` (directly resolving part 28's previously-unplaced settlement name), four new `dungeon_base_sanitar_*` zones, eight new `signals_dungeon_*` zones, two new named PvP arenas, and two new "Radar" callback zones. Second, a real, previously-unread asset folder -- `modassets/assets/customitems/textures/items/barter/` (101 real `.ol` files) -- gave this project its first real corroboration of the long-open "mg"/"pd"/"td" zone-code mystery (parts 26/35/36/37): those three tokens appear here as barter-item zone prefixes in the exact same reference class as already-confirmed real zones (armsklad, bar, bolota, kordon, les, svalka), via a real, consistent `<zone>.<category>[_add].ol` naming pattern. The same sweep also found three zone-shaped names new to this project entirely -- `limansk`, `sever`, `blizzard` -- disclosed as real and on-disk but NOT confirmed as actual playable `map_cache` zones (none of the three is a real `map_cache/5.0/` folder name); a real armor model set literally named `zarya` (`stalker:models/armor/zarya/...`), the first evidence tying the part-34 "zarya" quest-faction name to actual in-game equipment; and a widespread real "sanitar" armor-skin variant across 8+ unrelated armor bases plus dedicated NPC animation/game-object assets, disclosed as plausibly-but-NOT-confirmed the same faction EXBO's own public wiki calls "Orderlies" (part 36) -- a real semantic match ("sanitar" is Russian for "orderly"), but no file or log line directly equates the two names, so it stays a hedged reading. `economy/BarterItemCatalog.java` is the new typed reader for the barter-icon folder (zone-scoped vs. standalone vs. bare-numeric-id classification); `economy/BarterItemCatalogDumpTool.java` is its CLI. Compiled clean (112 total sources) and verified: 8 new unit tests (a 9-file fixture drawn from real filenames, including the mg/pd/td names this class exists to corroborate) plus the DumpTool run against a reconstructed, md5-verified 101-file real listing. See `docs/RESEARCH_LOG.md` part 53 for the full writeup.

- **`world/props/ArmorModelCatalog.java` -- part 54: catalogs the real armor/character model assets under `modassets/assets/stalker/models/armor/`, the first sweep of this folder in this project.** 192 real top-level folders, 2,240 real files total (mostly `.mcsb` meshes and `.ol` diffuse/normal/emissive textures, the same format already identified in part 15), both counts confirmed by a direct `find` listing on the real install, not estimated. The class deliberately does not hardcode any narrative reading -- `foldersContaining(String)` is a generic, case-insensitive substring search over every real nested filename, so specific claims below are verified programmatically rather than hand-counted (the exact class of error this project caught and disclosed in part 53). Real cross-references found: `armor/twins/` (real `left_twin`/`right_twin` body meshes, a body texture pair for each twin, and a shared `twins_heads` texture pair) is the first real character-model evidence for the "Twins" (bliznetsi) boss encounter, previously only known via 5 FMOD bank names (part 35); `armor/nina/` (a real mesh, body texture pair, and separate `face_diff.ol`) corroborates part 34's `nina_2`-`nina_6` subtitle files with an actual model; `armor/reichstag/` holds two real, symmetric 9-mesh WWII-style unit rosters -- `reich_*` and `rkka_*` (the real historical Soviet Red Army acronym) -- each with capitan/corporal/engineer/gunner/hands_1/hands_2/hands_sniper/sniper/stormtrooper roles, corroborating the `reichstag2026` FMOD bank (part 35) and the real `hasSeenReichstagHowToPlay` save flag (part 47); 8 real `murmur_*` folders plus a plain `murmur` base give the first real in-game armor evidence for EXBO's public "Monolit -> Murmur" faction rename (part 36); `zivcas`/`zivcas_commander`/`zivcas_fighter`/`zivcas_heavy` plus a separate `dr_zivcas` extend part 34's "Zivcas special forces" name with a real 4-role roster; and `zombie_bm` is considerably larger than an initial pass suggested -- 8 distinct real zombie character lines (`ao4`, `ao5`, `ao6`, `centurion`, `ghost`, `goplit`, `legion`, `mule`), not just the 3 gas-mask variants first assumed, plausibly but NOT confirmed tied to the `bm2024` seasonal FMOD bank (part 35) via the shared "bm" token. The real "sanitar" filename-suffix variant (part 53's hand-counted "8+ armor bases") is now an exact, programmatically-verified 9: `bover`, `kz3b`, `mule`, `saturn`, `seva` (newly named here), `skat10`, `smog_old`, `voshod_old`, `zarya` -- structurally inconsistent and disclosed as such: most bases add only a same-level texture-suffix pair, `kz3b` adds its own dedicated mesh, `skat10`'s variant lives in its own `motif_sanitar/` subfolder, and `zarya` is the richest of the nine with BOTH a same-level texture pair AND its own `zarya/sanitar/` subfolder holding four dedicated meshes -- still only corroborating, not confirming, part 36's hedged "sanitar" = EXBO's public "Orderlies" faction reading. Other real, disclosed-as-unconfirmed finds: `zarya/lipa_mask.*` (a real mask model tied to the "Lipa" tutorial character, parts 32/34/35) plus a separate `zarya/lipa_diff.ol`/`lipa_nrm.ol` pair without the "_mask" suffix; a real `zarya/outlaw_diff.ol`/`outlaw_nrm.ol` named skin variant, unconfirmed meaning; and a real `dr_zivcas`/`dr_tank` pair sharing a `dr_` prefix (plausibly "doctor"/medic, unconfirmed) and a matching `_hw22_diff.ol`/`_hw22_emi.ol`/`_hw22_nrm.ol` texture variant -- `dr_zivcas` additionally carries three real `.mic`-extension files whose names read "zivkas" (with a k) rather than "zivcas" (with a c) everywhere else in the folder, disclosed as a real on-disk spelling inconsistency in the game's own asset files (not a transcription error introduced here), and the first `.mic` extension this project has catalogued under `models/armor` (previously only `.mcsb`/`.ol`, part 15/54). `world/props/ArmorModelCatalogDumpTool.java` is the CLI. Compiled clean (115 total sources) and verified: 8 new unit tests (a 12-file fixture drawn from real filenames) plus the DumpTool's underlying folder-walk logic cross-checked against a fresh, live `find` listing of the real device directory -- every predicted count (192 folders, 2,240 files, all 9 sanitar bases, 8 murmur_* + plain, 4 zivcas_* + dr_zivcas) matched exactly, and the live check is what caught and corrected the reichstag role-count and zombie_bm scope inaccuracies described above before they were committed to this writeup. See `docs/RESEARCH_LOG.md` part 54 for the full writeup.

- **`world/props/ArtefactModelCatalog.java` -- part 55: catalogs the real artefact model assets under `modassets/assets/stalker/models/artefacts/`, the first sweep of this folder in this project.** 17 real top-level folders confirmed by direct `find` listing, holding 536 real files between them (139 `.mcsb` meshes, 397 `.ol` textures) that this class's folder-only walk (same generic, no-hardcoding design as `ArmorModelCatalog`, part 54) actually reaches -- plus 17 more real files sitting loose directly in the artefacts root, outside any subfolder, which the class does not walk and which the live device check is what caught: the first Javadoc draft claimed 553 as this class's own total before that check caught the mismatch and it was corrected to the real, walked 536. The 17 real loose root files, disclosed rather than hidden: a `krisstal.mcsb` mesh with a real on-disk spelling mismatch against its own `kristal.ol`/`kristal_emi.ol` textures (double-s mesh name, single-s textures -- the same class of naming inconsistency as `dr_zivkas`/`dr_zivcas` in part 54), a `1623.ol`/`1623_emi.ol` texture pair with no matching mesh, and two apparently shared mesh-less texture groups (`dist.ol`-`dist5.ol`/`dist2k.ol`, read as a distortion/heat-haze effect set, and `sopli.ol`-`sopli6.ol`, "sopli" being Russian slang for "snot/mucus", read as a shared organic/slime effect set). Two real structural eras found by direct inspection: 6 of the 17 top-level folders (`electra`, `jarka`, `karusel`, `kisel`, `puh`, `voronka`) are flat "classic" family folders with shared, reused textures; a newer `n_art/` ("new artefacts") folder reorganizes those same 6 names into 68 real per-artefact leaf folders across 12 real family subfolders, each with its own dedicated texture set -- a real, disclosed asset-pipeline evolution, not an invented reading. The other 6 real `n_art/` families (`autumn25`, `biohim_summer25`, `collab_art`, `inside`, `ny26`, `trampoline`) have no flat-era counterpart at all. Real dated seasonal folders are a new kind of evidence for this project: `n_art/autumn25`, `n_art/biohim_summer25`, and `n_art/ny26` are real, literally year-suffixed names (Autumn '25, Summer '25, New Year '26) -- the first directly dated naming convention found in a file/folder name itself, as opposed to the dated FMOD bank names found earlier (`reichstag2026`, `bm2024`, part 35) from a different data source. A real, cross-referenced Halloween-adjacent cluster: this same part's sweep also found a standalone `models/halloween/` folder (a shared `goner_diff`/`goner_emi`/`goner_nrm` texture set across real `big`/`medium`/`small` size-variant meshes, a likely creature/prop) plus `models/artefacts/helewin/` (`pup.ol`, `pupdif.ol`, `pupdif_emi.ol`, `ty_kva.mcsb` -- "helewin" reads as a transliteration of "Halloween" and "ty_kva" plausibly as "tykva", Russian for "pumpkin", both hedged) plus a third, independent `n_art/trampoline/art_pumpkin.mcsb` with the literal English word "pumpkin" -- three differently-named folders in two real trees converging on the same seasonal theme, real corroborating evidence of a bundle, NOT evidence they ship together or share a name in any player-facing string. Other real, disclosed-as-unconfirmed finds: `n_art/collab_art/` (real leaf folders `hedgehog`, `jele`, `spheretank`, `tracks` -- the literal substring "collab" in a real folder name, partner unconfirmed); a real `"proto"`-prefixed naming habit recurring independently in four unrelated folders (`electra/protoart.mcsb`, `jarka/proto.ol`, `n_art/trampoline/art_protocibulya.mcsb`, `trump/art_protomedusa.mcsb`); real dev-leftover files sitting directly in the shipped tree (`jarka/test.ol`, `karusel/test.mcsb`); a real naming duplication in `karusel/` (`art_ostov.mcsb` with no textures of its own, alongside a separately-named `ostov.mcsb` WITH its own diffuse/normal/specular set); a real gap in `barter/` (`art_tesla.mcsb` and `art_toksikoz.mcsb`, unlike their 6 siblings, have no matching texture files anywhere in that folder); and a real `clan`/`clan2` skin-tier pair in `clan/` (`clan2` notably missing its own normal map). This same part's sweep also directly inspected 8 small standalone folders under `models/` not covered by any catalog class (too small/miscellaneous for one, but real and disclosed): `blizzard/` (a real `frozen` prop mesh+texture set, distinct from the still-unconfirmed `blizzard` map-zone-name lead, part 15); `cloth/` (a single real `cloth_hp.mcvd` file, an apparent orphan); `gift/` (a real `gift_box` mesh, likely a holiday reward-crate prop); `hunter/hook/` (a real "hook" model whose folder also holds real Cyrillic-suffixed duplicate files, `hook_diff — копия.ol` and `hook_spek — копия.ol`, "копия" being Russian for "copy" -- a genuine on-disk dev leftover, not a transcription artifact); `undead/hook/` (a parallel real "hook" model under a different faction folder, no Cyrillic duplicates); `shamanfight/` (a real boss-fight asset set -- `column`, `flames`, `phantom_proj`, `phantom_zone`, and a `shield` with its own `perlin.mic` noise texture -- the first real evidence of a "Shaman" encounter with a phantom-projectile/shield mechanic); and `dropped_tooltip/` (a real `stick` model with 6 real rank-suffixed texture variants -- `NEWBIE`, `STALKER`, `VETERAN`, `MASTER`, `LEGEND`, `PICKLOCK` -- the first real, file-name evidence of a player rank/tier ladder, with `PICKLOCK` an unexplained outlier among otherwise general-sounding tier names). `world/props/ArtefactModelCatalogDumpTool.java` is the CLI. Compiled clean (118 total sources) and verified: 9 new unit tests (an 18-file fixture drawn from real filenames spanning both structural eras) plus the DumpTool's underlying folder-walk logic cross-checked against a fresh, live `find` listing of the real device directory -- every predicted count matched exactly once corrected (17 folders, 536 walked files, 68 n_art leaf folders across 12 families, the `proto` cross-reference), and the live check is what caught the 553-vs-536 total-count error described above before it was committed to this writeup. See `docs/RESEARCH_LOG.md` part 55 for the full writeup.
- **`world/props/BackpackModelCatalog.java` -- part 56: catalogs the real backpack/container model assets under `modassets/assets/stalker/models/backpacks/`, the first sweep of this folder in this project.** 65 real top-level folders confirmed by direct `find` listing, holding 385 real files between them (154 `.mcsb` meshes, 231 `.ol` diffuse/normal/specular/emissive textures) -- unlike part 55's `ArtefactModelCatalog` find, ZERO loose files sit outside the 65 named subfolders, so this class's `totalFileCount()` needs no disclosed root-vs-walked gap. Same generic, no-hardcoding design as `ArmorModelCatalog`/`ArtefactModelCatalog`: `foldersContaining(String)` is a reusable substring search, so narrative claims are checked programmatically, not hand-counted -- which is exactly what caught this part's own error: the two real naming families split the 65 folders as 30 `bag_`-prefixed and 33 `cont_`-prefixed (plus 2 outside either family, `clan/` and `corpse_bags/`), but the first Javadoc draft hand-counted these as 26 and 37 and omitted two real folders, `cont_cocoon` and `cont_kolotun`, entirely -- caught and corrected by the live device DumpTool run before being committed here, the same class of error and the same catch-before-ship discipline as part 55's 553-vs-536 total. A real structural convention not seen in the armor or artefact sweeps: most meshes ship in TWO forms, a worn/carried mesh and a separate `_ground` mesh (the model as it appears dropped in the world) -- confirmed real and widespread (53 of 65 folders) via `foldersContaining("_ground")`, not assumed from a handful of examples; textures are not duplicated per state, only the mesh gets a ground variant. A real, reusable `style_<season>` subfolder convention, distinct from part 55's per-artefact `n_art` folders: `cont_bear/style_autumn25/`, `cont_hive/style_autumn25/`, and `cont_pathfinder/style_autumn26/` each hold a dedicated seasonal texture/mesh variant of their parent container, confirmed non-redundant with the parent folder's own base files (`cont_hive`'s base `hive.mcsb` set and its `style_autumn25/hive_autumn25.mcsb` set are two distinct file groups, not a duplicate) -- `style_autumn26` is real, on-disk, literally year-suffixed evidence of Autumn '26 content, the first "26"-dated SEASON name this project has found anywhere (part 55's dated folders topped out at `ny26`); `bag_dmz26/` is a second, independent real 2026-dated find in this same sweep, a DMZ-mode bag with `bake`/`frost` finish variants plus a crossover `bag_trizip_dmz26_diff.ol`/`_nrm.ol` texture pair tying it to the separately-cataloged `bag_trizip/` folder. Real "kept the old version" content in THREE distinct forms found in one tree: sibling `_old` folders (`bag_npa_old/` -- identical file set to `bag_npa/` plus one extra `npa_spek.ol`; `bag_mbss_old/` -- byte-for-byte identical filename set to `bag_mbss/`); and, new to this project, nested `old/` SUBFOLDERS inside an otherwise-current folder (`cont_kzs1/old/` and `cont_bidon/old/`, each holding a prior texture revision -- `cont_bidon/old/kzs3_diff.ol` sitting alongside the real, separately-cataloged `cont_kzs3/` folder's own current textures). Real cross-references extending established findings into equipment: `bag_flamethrower/` carries `flamethrower_reich_diff.ol` and `flamethrower_rkka_diff.ol`, tying part 54's Reichstag/RKKA WWII-event armor roster to a matching backpack skin (structurally an outlier itself -- no plain diffuse, no `_ground` mesh); `cont_zivcas/` and `cont_murmur/` extend the established Zivcas (parts 34/53/54) and Murmur (parts 36/54) faction cross-references from armor into containers. Other real, disclosed-as-unconfirmed finds: a same-tree bee-themed cluster (`cont_apiary/`, `cont_hive/`, `cont_honeycomb/`), unconfirmed as a deliberate single set; `cont_hellboy/hellboydonate_diff.ol`, the first evidence in this project of a monetization/donation-tied cosmetic skin variant; `bag_tv/`, holding only `tv.mcsb` -- the sole folder in this tree with a mesh and no texture file of any kind; `bag_sports/` and `bag_sports_quest/`, two differently-named folders with identical internal file names, the same duplication pattern as `bag_mbss_old` read here as quest-specific asset reuse; `bag_trader/` (one shared mesh, 10 numbered diffuse skins) alongside 11 separate, fully-independent `bag_trader1/` through `bag_trader11/` folders, a naming duplication across two different "trader" bag systems, unknown if related in-game (the same class of oddity as part 55's `karusel/art_ostov` vs `ostov`); `clan/`, holding `clan_supply.mcsb` plus `clan_tent_medic`/`clan_tent_supply` sets, corroborating the clans system already inferred from keybindings/prefs data (parts 42/45); `corpse_bags/`, holding real `big`/`medium`/`small` size-variant meshes with no textures, plausibly a death/loot-drop bag prop, unconfirmed by any log; and a recurring `u`/`d` suffix letter across several container names, read as a plausible camo/finish marker, not confirmed by any file or log this project has found. `world/props/BackpackModelCatalogDumpTool.java` is the CLI. Compiled clean (121 total sources) and verified: 11 new unit tests (a 31-file fixture drawn from real filenames, covering the `_old` sibling gap, the bare-mesh `bag_tv` gap, the nested `style_<season>` subfolder, and the nested `old/` subfolder) plus the DumpTool's underlying folder-walk logic cross-checked against a fresh, live `find` listing of the real device directory -- every predicted count matched exactly once corrected (65 folders, 385 files, 30/33/2 family split, 53-folder `_ground` spread, the two `style_autumn25` folders and the one `style_autumn26` folder), and the live check is what caught and corrected the 26-vs-30 / 37-vs-33 family-count error and the two omitted folder names described above before they were committed to this writeup. See `docs/RESEARCH_LOG.md` part 56 for the full writeup.
- **`world/props/ItemModelCatalog.java` -- part 57: catalogs the real consumable/attachment item model assets under `modassets/assets/stalker/models/items/`, the first sweep of this folder in this project.** 3 real top-level folders confirmed by direct `find` listing (`armorplate`, `food`, `meds`), holding 130 real files that this class's folder-only walk (same generic, no-hardcoding design as `ArmorModelCatalog`/`ArtefactModelCatalog`/`BackpackModelCatalog`) actually reaches -- plus 3 more real files sitting loose directly in the `items/` root, outside any subfolder (`habar.mcsb` plus its diff/nrm textures, "habar" being Russian slang for "loot"/"swag"), the same class of disclosed root-vs-walked gap part 55's `ArtefactModelCatalog` found, just much smaller here (3 loose files, not 17). `armorplate/` (15 files) has 6 real mesh variants (`common`, `durable`, `meh`, `radiator`, `solid`, `stell`), but only `meh` and `radiator` have their own dedicated texture sets -- the other four have no textures of their own, while three generic folder-root-level texture files (`armorplate_diff.ol`, `armorplate2_diff.ol`, `armorplate_nrm.ol`) sit alongside them, plausibly shared/default textures for the texture-less meshes, unconfirmed. `food/` (4 files) is the thinnest folder in this tree, a single item (`galet`, a hardtack/cracker) with a full texture set. `meds/` (111 files) is by far the richest: its own flat root holds 6 real named meds with texture files but no matching mesh anywhere in this tree (`astrikcin`, `desperol`, `psiblock`, `sas`, `stark`, `stomping`) -- the inverse of part 55's texture-less-mesh gap. A real shared-mesh-many-skins convention not seen in the armor, artefact, or backpack sweeps: `meds/inside_lech/` (30 files) has one mesh, `inside_lech.mcsb`, reused across roughly a dozen differently-named real med items via texture-only variants (`amulet`, `cyan`, `grace`, `insight`, `mazz`, `pilul`, `red`, `salt`, `sedative`, `stimulator`, `treat`, `yellow`, plus `nootrop` which uniquely also has its own dedicated mesh) -- "lech" is Russian for "cure"/"treatment"; a smaller second instance of the same pattern, `meds/inside_grass_extract/` (9 files), has one full-set mesh sharing its folder with two more texture-only meds (`ozverin`, `schizoyorsh`). `meds/nmeds/` (32 files) carries a real, disclosed naming-convention shift -- capitalized `_Diffuse`/`_Normal`/`_Specular` texture suffixes instead of the lowercase `_diff`/`_nrm`/`_spek` used everywhere else in this tree and every other model tree this project has catalogued to date -- with real numbered-tier variants (`antirad_191`/`_292`/`_393`, `pciblock_neiron_11`/`_22`/`_33`) and a real `aptechka_color1`-`color5` set plus an `aptechka_color_prestige` variant, tentative evidence tying a cosmetic tier to the STALCRAFT prestige/rank system already known from this project's wire-traffic and API work (part 11), unconfirmed as the same mechanic. `nmeds/psiblock.mcsb` has no texture of its own in `nmeds/`; its only real candidates are the differently-cased `meds/psiblock_diff.ol`/`psiblock_spek.ol` one directory up, a disclosed cross-folder pairing not confirmed as what the mesh actually uses in-game. `meds/reichstag26/` (4 files, `aptechkareih` + `reichstag_medbag.mcsb`) is a third real Reichstag/RKKA WWII-event cross-reference from this project (armor, part 54; backpacks, part 56; now consumables). `meds/memoria/` (1 file, `memoria.mcsb`) is the sole bare-mesh-no-texture gap in this tree, the same class of gap as part 56's `bag_tv/`. `world/props/ItemModelCatalogDumpTool.java` is the CLI. Compiled clean (124 total sources) and verified: 10 new unit tests (a 22-file fixture drawn from real filenames, covering the texture-sharing armorplate gap, the mesh-less psiblock texture, and the nmeds capitalized-suffix convention) plus the DumpTool's underlying folder-walk logic cross-checked against a fresh, live `find` listing of the real device directory -- every predicted count matched exactly (3 folders, 130 walked files, the armorplate/food/meds split, and the `prestige`/`reichstag`/`memoria` substring checks). See `docs/RESEARCH_LOG.md` part 57 for the full writeup.

- **`world/props/CurrencyCatalog.java` -- part 58: catalogs the real currency/token model and icon assets under `modassets/assets/currency/`, extending part 27's `VendorArchive` multi-currency finding into a much larger real currency/token roster.** 2 real top-level folders confirmed by direct listing (`models`, `textures`), holding all 98 real files with no loose root-vs-walked gap this time -- the smallest folder tree this project has catalogued, but the richest in named-currency terms. `models/` (8 files) has 6 real Russian ruble banknote-denomination props (`10rub`-`5000rub`), a generic `money.ol` texture, and a dedicated `token_electroshtorm2026.mcsb` prop with its own full texture set in `textures/` root. `textures/items/` (86 files) is mostly icon-only UI textures, but a live check found 7 of the 86 DO pair with a real model above (the 6 `money_10`-`money_5000` ruble icons and `token_electroshtorm2026` itself) -- an earlier Javadoc draft claimed none of the 86 had a dedicated model, which this live check disproved and corrected in place, per this project's "verify before committing a narrative number" discipline (the same discipline that caught real errors in parts 53/55/56). The folder holds a real, systematic dated seasonal-currency calendar spanning 2024-2026 (`autumn`/`spring`/`summer`/`winter` x `_24`/`_25`/`_26`, with a disclosed `winter_24` gap), a parallel dated series for the season pass (`season_battlepass_points` plus 9 dated variants) plus 5 named one-off event battlepass currencies, real DMZ-mode-specific currencies (`dmz_coin`, `dmz26_token`, `popularnostDMZ`, `respectDMZ`), a second and third real Reichstag/RKKA WWII-event cross-reference (`reichstag_silver`, `reichstag_xp_free` -- a fifth cross-reference for this event across five content areas), a `shards` currency family with dated variants plus the project's first evidence of a Twitch-integration currency (`twitch_shards`), a standalone `token_prestige` icon corroborating part 57's hedged `aptechka_color_prestige` finding, and several other real named tokens tying to the "Triumph" endpoint from part 11 (`token_triumph26`). `world/props/CurrencyCatalogDumpTool.java` is the CLI. Compiled clean (127 total sources) and verified: 10 new unit tests (a 31-file fixture drawn from real filenames) plus the DumpTool's folder-walk logic cross-checked against a fresh, live listing of the real device directory -- every predicted count matched exactly (2 folders, 98 files) and every targeted substring check (`reichstag`, `prestige`, `dmz`, `battlepass`, `shards`, `twitch`, `token`, plus a direct `winter_24`-absence check) confirmed, except the one icon/model-pairing claim corrected above. See `docs/RESEARCH_LOG.md` part 58 for the full writeup.

- **`world/props/StalkerGuideCatalog.java` -- part 59: catalogs the real narrative/tutorial cutscene assets under `modassets/assets/stalkerguide/` -- real story/cutscene content despite the folder's "guide" name, not an anti-cheat asset.** 4 real top-level folders confirmed by direct listing (`sound`, `stream`, `textures`, `video`), holding all 157 real files with no loose root-vs-walked gap. `video/` (99 files) is real `.mp4` cutscenes, almost all shipped as `_720`/`_1080` pairs except 4 real singles (`honey_badger`, `memagent`, `reichstag_atmosphere` -- a fifth real Reichstag/RKKA WWII-event cross-reference after armor/backpacks/meds/currency -- and `vozduhan`). Real named faction/story-arc start/end video pairs, live-confirmed: `duty_start`/`duty_end` (the first real evidence of the "Duty" faction anywhere in this project), `zarya_start`/`zarya_end` (corroborating part 53's `zarya` armor), `zavet_start`/`zavet_end` ("zavet" = Russian "covenant"), `merc_start`/`merc_end` (corroborating parts 26/27/53's merc trader/zone content) -- none of these 8 files, nor the 4 singles above, have a matching audio track in `stream/video/`. `stream/` (44 files) holds 4 ambient tracks at its own root plus `stream/video/` (40 `.ogg` tracks); a live cross-check found the video/audio pairing is NOT total in either direction -- 2 real audio-only orphans (`frontline_intro`, `inside`) and, complementarily, 14 real video-only entries (the 8 pairs and 4 singles above, plus `cinemat` and `floating_away_2`). Real named characters/locations recurring across multiple files: "Vlasov" (`vlasov_death`/`vlasov_escape`, branching outcomes), "Shaman" (`shaman_death`/`shaman_flies`/`morg_shaman_fight`/`morg_shaman_meet` -- the `morg_shaman_*` pair directly corroborates part 55's `shamanfight` artefact folder, confirming Shaman is a real in-fiction boss encounter), "Morg" (5 cutscenes, plausibly "morgue"), and "Lipa" (4 cutscenes plus a fifth tying into the tutorial sequence, "linden tree" in Russian). A real 4-part tutorial sequence (`tutorial_intro`/`_lipa_tonnel`/`_ejection`/`_outro`) and 3 numbered generic-story cutscenes (`sstory1`-`3`). `sound/` (11 files) has 3 elevator effects plus a `glitch_monitor/` subfolder of 7 numbered variants. `textures/gui/` (3 files) are `.mic` banner textures, the same shader/effect-role extension seen in parts 55 and 58. `world/props/StalkerGuideCatalogDumpTool.java` is the CLI. Compiled clean (130 total sources) and verified: 10 new unit tests (a 41-file fixture drawn from real filenames) plus the DumpTool's folder-walk logic cross-checked against a fresh, live listing of the real device directory -- every predicted count matched exactly (4 folders, 157 files) and every targeted substring check (`reichstag`, `duty`, `zarya`, `zavet`, `merc`, `vlasov`, `shaman`, `morg`, `lipa`, `tutorial`, `sstory`, `glitch_monitor`, `palka`) confirmed -- no correction needed this part. See `docs/RESEARCH_LOG.md` part 59 for the full writeup.

- **`world/props/KoreaJurisdictionCatalog.java` -- part 60: catalogs the real regional content-override assets under `modassets/assets/_jurisdiction/korea/` -- a genuine regional content-override system (alternate cutscenes, localized strings, cosmetic item variants), not an anti-cheat/launcher asset, and still in-scope under this project's game-content focus.** 5 real top-level folders confirmed by direct listing (`customitems`=8, `stalcraft`=1, `stalker`=3, `stalkerguide`=41, `weapons`=7), holding all 60 real files with no loose root-vs-walked gap; `_jurisdiction/` itself holds only this one region. `stalkerguide/` (41 files) is a real PARTIAL override of part 59's base `stalkerguide/` cutscene catalog -- all 18 unique cutscene base names it ships are real names already found in the base 99-file `video/` roster, confirming a substitution set for exactly these story beats. A real, disclosed asymmetry: only 11 of the 18 names ship as a `_720`/`_1080` pair here; the other 7 ship as a `_1080`-only single, dropping the `_720` variant the base game has for every one of them. The audio side uses a real, DIFFERENT internal path shape than the base game (`sound/stream/*.ogg`, 11 files, versus base stalkerguide's separate `sound/`+`stream/video/` split). One real anomaly: `stream/video/prestige.ogg` is the only file under this jurisdiction's `stream/video/`, and "prestige" matches none of the 18 real video names here or any name in part 59's base roster -- a third independent real "prestige" cross-reference in this project, after part 57's `aptechka_color_prestige` and part 58's `token_prestige`, still unconfirmed as the same system. `stalker/lang/` (2 files, `ko.lang`+`en.lang`, read directly) is a real, narrow localization override defining exactly 2 real numeric item ids -- `16120` ("Bag of Holepunch's Hair", the first real evidence in this project of a named character "Holepunch") and `16429` ("A Bag of Hair") -- both real quest/trophy items. `customitems/` (8 files) holds real decal/poster overrides, a real recurring "stalboy" postcard/mascot item with 2 real dated texture variants (`stalboy_hw23_diff.ol`, `stalboy_summer24_diff.ol`, joining this project's established dated-content-series pattern from parts 55/56/58), and the real `quest_cutted_ear.ol`/`quest_cutted_ear_3.ol` pair -- a grim trophy-item texture, a second real body-part/trophy quest-item name in this jurisdiction override alongside the hair-bag items. `weapons/` (7 files) holds a real "lighter" bolt-throwable item, a real `mr153hell.ol` weapon skin, a real Halloween-2023 dated weapon skin (`m1014b_halloween23.ol`), and a real cosmetic weapon-charm accessory pair (`dakimakura.ol`/`dakimakura_naked.ol`). A real cosmetic sticker `cowgirl.ol` appears duplicated under BOTH `stalcraft/textures/items/` and `stalker/textures/gui/`, unconfirmed whether the two files are byte-identical. `world/props/KoreaJurisdictionCatalogDumpTool.java` is the CLI. Compiled clean (133 total sources) and verified: 12 new unit tests (a 60-file fixture drawn from real filenames) plus the DumpTool's folder-walk logic cross-checked against a fresh, live listing of the real device directory -- every predicted count matched exactly (5 folders, 60 files, the 8/1/3/41/7 per-folder split, the 11-pair/7-single video breakdown) and every targeted substring check (`prestige`, `cutted_ear`, `stalboy`, `dakimakura`, `cowgirl`, `lighter`, `holepunch`, `mr153hell`, `m1014b_halloween23`, `otkritka`, `plakat`, `friendship`) confirmed -- no correction needed this part. See `docs/RESEARCH_LOG.md` part 60 for the full writeup.

- **`world/props/EffectsParticleCatalog.java` -- part 61: catalogs the real VFX/particle-effect assets under `modassets/assets/effects/particles/` -- squarely game content (anomalies, artefacts, weapons, bosses, seasonal events, gameplay mechanics), not an anti-cheat/launcher asset.** 48 real top-level folders confirmed by direct listing, holding 829 real files reached by this class's folder-only walk, plus 2 more real files sitting loose directly in the `particles/` root outside any subfolder (`1.efkmat`, `ground_line_default.efkmat`) -- the same disclosed root-vs-walked gap parts 55/57 found in other trees. `prestige/` (10 files) is the richest evidence yet for this project's recurring, still-unconfirmed "prestige" cross-reference (a fourth independent hit, after parts 57/58/60) -- and, unlike the first three, these are real ABILITY/MECHANIC names, not just a label on an unrelated asset: `charger_enabled`, `scanner_zone`, `scout_stun`, `shield_depth_pulse`, `support_guard`, `tank_explosion_zone` -- the real "scout"/"tank"/"support" tokens read as class or loadout-role names, plausibly (not confirmed) a role-based extraction-mode ability system gated behind this "prestige" mechanic. A real, separate `prestige_prototype_effects/` (1 file, `laserpointer.efkmat`) initially looked like an isolated cut/unreleased-content lead; a live `foldersContaining("laserpointer")` check disproved this and found a SECOND, identically-named real file, `weapons/stratg_exp/laserpointer.efkmat` -- disclosed instead as a real duplicate (a normal weapon-attachment laser-sight effect also filed under the "prestige prototype" folder), a correction made transparently per this project's rule-1 discipline and now covered by a dedicated regression test. `shamanfight_fx/` (12 files) and `morgfight_fx/` (2 files) are real VFX for the Shaman/Morg boss encounter already corroborated across parts 55/59 -- now with actual arena effects: a real `arena_boss_bg`, six real colored `shamanfight_field_zone_<color>` effects, plus atmosphere, and `morgfight_fx/`'s `morg_celling.efkmodel` preserving the same real on-disk "ceiling" misspelling this project has disclosed before. `electrostorm_mutants/` (7 files) and `electrostorm_npc/` (2 files) give this project its first real evidence of SPECIFIC mutant names tied to the "electrostorm" event already known only as a currency/token (part 58's `token_electroshtorm2026`): real spawner effects for `boar`, `burelom`, `chimera`, `flesh`, and `krovosos` ("bloodsucker", Russian -- the classic S.T.A.L.K.E.R.-series mutant). `operations/` (41 files) gives this project its first real evidence that "Electra" -- previously known only as part 55's classic `electra/` artefact-model family -- is an actual BOSS encounter: real `boss_electra` and `electra_pillar` effects, alongside real raid/operation-mode content (`cargo`, `ground_borders`, `operation_ship`). `anomalies/` (70 files) directly corroborates four of part 55's classic artefact family names as real ANOMALY types: `electra_n`, `kisel`, `voronka_n`, and `carousel_n` -- the last a real English spelling next to part 55's transliterated `karusel`, a genuine cross-tree spelling variant. `artefacts/` (36 files) holds `buran26` -- the first real "26"-dated artefact name this project has found -- plus a wide 2023-2026 dated-seasonal pattern recurring across many independent subtrees in this one sweep (`hw23/`, `leto23/`, `buran23/`, `voron2024/`, dated Battle-Pass-'24 melee/weapon skins, `winter25/`, `caseopen/`). `iznanka_iv/` (125 files, the single largest folder in this tree) and `ves_iv/` (22 files) both carry a real `_iv` suffix; a real `HW_portal.efkmat` ties `iznanka_iv/` to a Halloween event. `caseopen/` (15 files) is real loot-case-opening monetization VFX corroborating part 58's currency findings. Other real disclosed finds: `sj_boss/` and `cybergiant/` (single-file boss/mutant names), `monolit_antena/` (3 files, the first VFX-level evidence for the Monolit faction from parts 36/54), `scaries/` (real `lenin_eye` Soviet-era imagery), `tutorial_eff/` corroborating part 59's tutorial cutscene sequence, `events/reihstag_tanks` (a sixth real Reichstag/RKKA WWII-event cross-reference, spelled "reihstag" here), and real special grenade types under `grenades/`. A real, disclosed naming-case inconsistency: `Texture/` and `NewVFX/` are capitalized, unlike all 46 other real top-level folder names in this tree, which are lowercase -- the same class of on-disk inconsistency already disclosed in parts 54/55. `world/props/EffectsParticleCatalogDumpTool.java` is the CLI. Compiled clean (136 total sources) and verified: 14 new unit tests (a real-filename fixture, including a dedicated regression test for the `laserpointer.efkmat` duplicate correction) plus the DumpTool's folder-walk logic cross-checked against a fresh, live listing of the real device directory -- the 48-folder/829-file total and every targeted substring check matched, except the laserpointer discrepancy, which was caught, disclosed, and corrected per this project's self-correction discipline. See `docs/RESEARCH_LOG.md` part 61 for the full writeup.



## Why no Netty (yet)

The original plan (`PROJECT_PLAN.md`) was Java/Kotlin + Netty, matching the client's
own stack. This scaffold uses plain Java stdlib only, because the sandboxed
environment this was built in cannot reach Maven Central or any other Java package
registry (confirmed: `repo.maven.apache.org`, `repo1.maven.org`, and
`maven.google.com` all return HTTP 403 from the egress proxy here) -- so `gradle
build`/`mvn` with a Netty dependency would simply fail to resolve it. `java.nio`
gets us a working, dependency-free UDP listener in the meantime. If/when this is
built somewhere with real internet access (your own machine, outside this sandboxed
session), migrating `UdpListener` to Netty's `NioDatagramChannel` is a small, isolated
change -- nothing else in this scaffold depends on it.

The same limitation is why zstd decompression (`nbt/CompressionUtil.java`) shells out
to a `zstd`/`unzstd` binary on PATH instead of using a pure-Java library -- there's no
pure-Java zstd in the JDK, and a real dependency (e.g. `com.github.luben:zstd-jni`)
isn't fetchable here either. Gzip works natively via `java.util.zip`.

## Building and running

No build tool required -- plain `javac`/`java`:

```
javac -d out $(find src -name '*.java')
java -cp out net.stalzone.emulator.Main [path/to/server.properties]
```

First run creates `config/server.properties` with commented defaults if it doesn't
exist yet. The NBT CLI:

```
java -cp out net.stalzone.emulator.nbt.NbtDumpTool <path-to-save-or-content-file> [--roundtrip]
```

The world-region CLI (point it at a single `.mdat` file, or a whole zone folder like
`map_cache/5.0/dungeon_bolota_tutorial/` to summarize every region in it):

```
java -cp out net.stalzone.emulator.world.region.RegionDumpTool <path-to-.mdat-or-zone-folder>
```

The whole-map CLI (loads every region file in a zone folder into one `WorldModel` and
probes it with real world-coordinate queries):

```
java -cp out net.stalzone.emulator.world.WorldDumpTool <path-to-zone-folder> [mapName]
```

The weapon-data CLI (loads `data/weapons/weapons.json`, applies a chosen
`damageMultiplier`, prints before/after damage for named weapons -- or three
defaults if none are given -- plus, as of part 30, real headshot/limb multipliers, the raw
damage-falloff curve, and melee reach/piercing/bloodlust/crit/vs.-mutant multipliers when
the record has them):

```
java -cp out net.stalzone.emulator.items.WeaponRegistryDumpTool <weapons.json> <damageMultiplier> [weaponName...]
java -cp out net.stalzone.emulator.items.WeaponRegistryDumpTool data/weapons/weapons.json 2.0 "AK-12" "PM" "Hunters Knife"
```

The mob-config CLI (loads every `*.smm` in a folder, prints behavior flags +
faction relations for named configs -- by filename stem, e.g. `base_boar`):

```
java -cp out net.stalzone.emulator.mobs.MobRegistryDumpTool <configs-dir> [configId...]
java -cp out net.stalzone.emulator.mobs.MobRegistryDumpTool modassets/assets/stalkermobs/configs base_boar base_tsarevich
```

The item-weight CLI (loads `weight.txt`, prints the carry-weight cap, every
conflicting id, and any specific item ids given):

```
java -cp out net.stalzone.emulator.items.ItemWeightTableDumpTool <weight.txt> [itemId...]
java -cp out net.stalzone.emulator.items.ItemWeightTableDumpTool modassets/assets/stalker/weight.txt 16340 16347
```

The detector-config CLI (loads `detectors.txt`, prints every tier grouped by
hazard type, optionally resolves the active tier at a given type+distance):

```
java -cp out net.stalzone.emulator.hazards.DetectorConfigDumpTool <detectors.txt> [type distance]
java -cp out net.stalzone.emulator.hazards.DetectorConfigDumpTool modassets/assets/stalker/detectors.txt rad 3.5
```

The movement-config CLI (loads `moving.txt`, prints every variant letter found
and given keys' base value + overrides -- or 7 defaults if none are given):

```
java -cp out net.stalzone.emulator.movement.MovementConfigDumpTool <moving.txt> [key...]
java -cp out net.stalzone.emulator.movement.MovementConfigDumpTool modassets/assets/stalcraft/moving.txt move.sprint.factor move.speed.user
```

The item-groups CLI (loads `item_groups.txt`, prints every group + its entry
count, and which group(s) any given item ids belong to):

```
java -cp out net.stalzone.emulator.items.ItemGroupsDumpTool <item_groups.txt> [itemId...]
java -cp out net.stalzone.emulator.items.ItemGroupsDumpTool modassets/assets/bundle/item_groups.txt 16613 20180
```

The Morse-alphabet CLI (loads `morse_alphabet.txt`, prints every ambiguous code,
optionally encodes a given string):

```
java -cp out net.stalzone.emulator.anomalies.MorseAlphabetDumpTool <morse_alphabet.txt> [text-to-encode]
java -cp out net.stalzone.emulator.anomalies.MorseAlphabetDumpTool modassets/assets/anomalies/morse_alphabet.txt "SOS"
```

The sound-duration CLI (loads a duration CSV, prints every key containing a given
substring -- or "operation_lesta" by default):

```
java -cp out net.stalzone.emulator.audio.SoundDurationsDumpTool <duration.csv> [substring]
java -cp out net.stalzone.emulator.audio.SoundDurationsDumpTool modassets/assets/bundle/speech_duration.csv zarya
```

The vendor-archive CLI (loads a real vendor_archive.dat, prints size/currencies/traders and
per-character purchase counts, and -- if given an item id -- every real price paid for it):

```
java -cp out net.stalzone.emulator.economy.VendorArchiveDumpTool <vendor_archive.dat> [itemId]
java -cp out net.stalzone.emulator.economy.VendorArchiveDumpTool config/vendor_archive.dat 11352
```

The subtitle CLI (loads the real `_localized` tree, prints the locale/npcId roster, total cue
count, and the cross-locale timing-mismatch disclosure; optionally prints one npcId's real cue
timing table for one locale):

```
java -cp out net.stalzone.emulator.dialogue.SubtitleRegistryDumpTool <_localized dir> [npcId] [locale]
java -cp out net.stalzone.emulator.dialogue.SubtitleRegistryDumpTool modassets/assets/_localized bigeagle_bar en
java -cp out net.stalzone.emulator.dialogue.SubtitleRegistryDumpTool groups modassets/assets/_localized
```

The vehicle-prop CLI (loads the real `vehicles/` folder, prints every category/subcategory's
real model folders and loose files):

```
java -cp out net.stalzone.emulator.world.props.VehiclePropCatalogDumpTool <vehicles dir>
java -cp out net.stalzone.emulator.world.props.VehiclePropCatalogDumpTool modassets/assets/customitems/models/blocks/vehicles
```

The audio-bank CLI (loads the real `banks/` folder; if given a logs folder too, cross-references
real `client-*.log` files to split the catalog into confirmed-live vs. unconfirmed):

```
java -cp out net.stalzone.emulator.audio.BankCatalogDumpTool <banks dir> [logs dir]
java -cp out net.stalzone.emulator.audio.BankCatalogDumpTool modassets/assets/gloomycore/banks logs
```

```
java -cp out net.stalzone.emulator.session.save.PlayerSaveFileDumpTool <save file path>
java -cp out net.stalzone.emulator.session.save.PlayerSaveFileDumpTool "config/<CharacterName>/sd1"
java -cp out net.stalzone.emulator.session.save.PlayerSaveFileDumpTool "config/<CharacterName>/sd2"
```

```
java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool quests "config/quests.cfg"
java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool waypoints "config/waypoints.cfg"
java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool keybindings "config/keybindings"
java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool options "config/options.json"
java -cp out net.stalzone.emulator.clientdata.ClientDataDumpTool prefs "config/prefs"
```

### Running the test suite

```
java -cp out net.stalzone.emulator.testing.TestMain
```

Runs all 202 tests (this count grows as parts are added -- see `docs/RESEARCH_LOG.md` for the
running total per part), prints one PASS/FAIL line per test plus a summary, and exits 0 only if
every test passed (non-zero exit on any failure).

### session/GameLoopDemo.java

```
java -cp out net.stalzone.emulator.session.GameLoopDemo \
  <dungeon_bolota_tutorial dir> <moving.txt> <detectors.txt> <weight.txt> <mob configs dir> \
  <weapons.json> [ticks] [spawnRateMultiplier] [damageMultiplier] [weaponName] \
  [lootTables.json] [lootRateMultiplier] [mobHealthTable.json]
```

Loads real Milestone-1 zone data, real movement/detector/weight configs, all 35 real mob
configs, and all 337 real `weapons.json` records; runs a scripted player walk while a demo "rad"
hazard and `ServerConfig`-driven mob spawns tick alongside it. As of part 30, once a mob spawns
the demo player also fires the chosen real weapon (default `AK-12`) at it every following tick --
a headshot first (exercising the real `headshotMultiplier`), then body shots -- until it dies,
using `MobInstance`'s disclosed placeholder health pool (as of part 49, a per-mob override from
`data/mobs/example_mob_health.json` if a mob-health-table path is given, via `mobs/MobHealthTable`).
As of part 48, if a loot-table JSON path is given (e.g. `data/loot/example_loot_tables.json`) a
kill also rolls loot via `loot/LootRoller`.
Prints one line per system per tick (position, real weight-vs-cap check, real detector beep
timing, spawn events, combat hits/deaths, loot drops) -- see `docs/RESEARCH_LOG.md` parts 29, 30,
and 48 for full sample runs and what in that output is real evidenced data vs. this emulator's own
placeholder/original-design choices (base walk speed, spawn cadence, mob health pool, the demo
hazard's coordinates, the demo's headshot-then-body-shots policy, and the entire loot system).

## What's genuinely NOT done

Everything that requires understanding the wire protocol: login/auth, session
handling, entity/world sync, literally any actual gameplay. That's blocked on
reading the encrypted UDP payloads, which per `RESEARCH_LOG.md` needs either the
class-dump-agent approach (real anti-cheat ban risk, needs your explicit go-ahead
each time) or some other source of the client's crypto/serialization code. Until
then, "Phase 1" stays at this scaffolding level by necessity, not by choice.
