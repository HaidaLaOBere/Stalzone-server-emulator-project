## Part 120 — `GoLangCatalog`: 2,613 real in-world "Game Object" lines modeled with a single generic table, zero flat exceptions, plus a large fresh Reichstag WWII prop roster

Part 120 investigated `go` (2,613 lines, direct count — part 90's original estimate had been
approximate) in `stalker/lang/en.lang`. Named the class `GoLangCatalog` (not
`GameObjectCatalog`/`GameObjectsCatalog`) to avoid confusion with the existing, unrelated,
file-based `GameObjectsCatalog` (part 74, models the real `modassets/assets/gameobjects/` asset
folder). `go` most plausibly abbreviates "Game Object" given this namespace's content — disclosed
as a plausible reading only, not client-confirmed, per rule 1.

New Java class `GoLangCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`GoLangCatalogTest.java` (6 tests).

- Direct count confirmed `go.*` is the SECOND namespace found (after part 116's ChallengesCatalog)
  with ZERO real flat exceptions — every line fits `go.<id>.<field>`, so the whole namespace is a
  single generic (id, field) table: 2,413 distinct real object ids, 10 real field names (name:
  2,402; additional_stats_tip: 88; hint: 71; tip_action/description: 17 each; lore: 12;
  notify/activation_text: 2 each; text/desc: 1 each).
- `misc.<id>` (326 ids) is a real mixed bag: anomaly-research/artifact-extraction installations
  (misc.extractor="Anomaly Research Installation", misc.artefact_extractor="Noo-converter") plus a
  LARGE real misc.reichstag_* tail naming actual WWII vehicles/props/soldier roles — reichstag_t34
  ="Т-34-85" (sic, Cyrillic Т in English client text), reichstag_panther="Panther",
  reichstag_tiger="Tiger", reichstag_junkers87="Junkers Ju 87", reichstag_rkka_sniper="Red Army
  Sniper. Winter 1941", reichstag_wehrmacht_officer="Wehrmacht Officer. Eastern Front". Disclosed
  per the STANDING part-109 note as further uncounted Reichstag corroborations — NO new ordinal.
- Other real id families found by sampling (not exhaustively enumerated — the single generic
  table needs no enumeration to be useful): exp.<id> (explosive/hazard props: claymore="M18
  Claymore", claymore_reich="Trip Wire Mine", stretch="RGD-5 Trip Wire", psymine="Psy-mine");
  ops.<id> (real "Phase Battery" props Red/Green/Blue, tying to part 113's OperationsCatalog
  breach/focus/sea_alienation Operations); clan.<id> (clan-base building props — gabions,
  barricades — plus clan.extractor="Chronodevice" distinct from the misc extractors); reward_beacon
  .<id>.<subId> (stockpile-reward prop names); a very large tutor_*/tutor_hint_<n>/tutor_descent_<n>
  tail (in-world tutorial-popup trigger objects, hint+name pairs, e.g. tutor_hint_0: hint="Press
  [#key.jump] to jump.", name="Hint (Jumping)").
- Doc-only: `LangCatalog.java` marks `go` as now fully parsed.
- Code compiled with `javac --release 11 -d out @sources.txt` in the cloud (zero warnings/errors),
  full suite run clean in the cloud (903/903), synced to the device and live-verified clean on the
  device's actual Java 11 JRE (903/903): test count 897 -> 903.
- README.md/RESEARCH_LOG.md both updated on the device (README bullet + running count; RESEARCH_LOG
  got a full "## Part 120" section).

**Context for future parts**: only `item` (12,611 lines, by far the largest remaining namespace,
likely needing a dedicated multi-part effort) and the still-partial `gui` (top-level `gui.*`
prefix, 204 of 395 lines remaining — NOT `stalker.gui.*`, already fully covered by part 119) remain
from part 90's original top-15 `en.lang` namespace list.

**Memory-store note**: this file continues the file-based Memory folder numbering established at
part 119 (cont71-75). See `Memory/NOTE_ABOUT_THIS_FOLDER.md` for the full situation re: the MCP
project-memory outage.
