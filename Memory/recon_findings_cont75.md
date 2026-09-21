## Part 119 — `StalkerLangCatalog`: a real 14-group `stalker.*` namespace (2,020 lines) — per-voice-pack quick-chat phrases, a fast-travel connector graph, faction-allegiance lore, and further uncounted Reichstag ties

Part 119 investigated `stalker` (2,020 lines) in `stalker/lang/en.lang` — a real namespace sharing
its name with the game's own `stalker/` asset folder (which many of this project's other
`Stalker*Catalog` classes model, file/folder-based) but confirmed to be a wholly separate thing:
the literal `stalker.` key prefix inside the localization file. A direct count established the
namespace splits into exactly 14 real top-level groups with no leftover lines: `quickcommands`
(914), `navigation` (331), `ui_guide` (264), `hud` (233), `guide` (116), `gui` (48), `clans` (24),
`alliance` (21), `protected_areas` (18), `battlefield` (17), `dmz2026` (14), `conditional_guide`
(14), `artefact_properties` (4), `player_params` (2); sum = 2,020.

New Java class `StalkerLangCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`StalkerLangCatalogTest.java` (9 tests). Real sub-groupings:

- `quickcommands.*` (914 lines) — a real per-character-voice-pack "quick chat" phrase-bank: 7 real
  100-line packs (stalkers/bandits/duty/freedom/merc/covenant/terentii), a 100-line WWII-army pack
  split wehrmacht/rkka (50 each, tied to the Reichstag mode), and three smaller irregular packs
  (lizard/rusi/hunter), each with up to 10 real phrase categories (hello/approv/disapprov/thanks/
  sorry/attack/cover/med/ammo/surrender) as ordered numbered-variant lists. 5 real flat exceptions
  (happynewyear + four approv/mark/<type> ping labels with literal `/` in the id) are detected
  generically — a 3-segment id whose last segment is all-digits is structured, anything else falls
  through to the flat table — no voice-pack id list hardcoded.
- `navigation.transition.<from>-<to>.name` (331 lines) — a real, previously-uncatalogued
  inter-location fast-travel/route connector graph (e.g. agroprom-dungeon_mozglyak="Weakling's
  Den").
- `alliance.*` (21 lines) — real faction-allegiance UI text, confirmed DISTINCT from part 107's
  CnpsCatalog (short names/reputation ranks, not this content). `gui.description.short/full.
  <factionId>` (12 lines) give real taglines and quoted lore blurbs attributed to named faction
  leaders: duty→"Davydov, Frontier Commander", bandits→"Saint, leader of the bandits",
  stalkers→"Ataman, leader of the stalkers" — corroborating part 105's csky="Ataman's HQ" title
  with a named leader for the first time. Plus a flat quest_join (1 line) and real per-faction
  <factionId>.quest_join/.quest_leave pairs (8 lines, duty/freedom/merc/covenant only —
  stalkers/bandits have none, consistent with being baseline not joinable factions).
- Two EXPLICITLY-CHECKED non-duplicate near-misses: `stalker.clans.tournament.*` (event-TYPE
  descriptions + reward-mail templates) vs standalone `clans.tournament.*` (part 108
  ClanWarCatalog, per-MAP names, same 3 short ids reused as sub-ids of a different id) — confirmed
  distinct by direct comparison, not assumed. `stalker.battlefield.team_name.*` (real in-match
  SQUAD names incl. redarmy="Red Army"/vermacht="Wehrmacht") vs standalone `battlefield.*` (part
  106 BattlefieldCatalog, MAP names) — also confirmed distinct.
- The squad names above, plus `stalker.hud.breakthrough.team.attackers`="RED ARMY"/
  `.defenders`="WEHRMACHT" and a real `stalker.gui.seasonpass.event.reichstag.industry.*`/
  `arcades.results.breakthrough.*` reward-UI block (reconfirming part 109's 4-class Reichstag XP
  economy — infantry/scout/machinegunner/engineer — by name), are ALL disclosed per the STANDING
  part-109 note as further uncounted entries in the long Reichstag corroboration chain — NO new
  ordinal asserted for any of them.
- `ui_guide.*` (264), `guide.*` (116, covering both a default.* and a conditional.* sub-group),
  `gui.*` (48), `clans.*` (24), `conditional_guide.*` (14) — each modeled with the established
  generic last-dot-split (id, field) table (same pattern since part 111's WorldEventCatalog).
  `conditional_guide.*` was explicitly checked and confirmed a real namespace DISTINCT from
  `guide.conditional.*` despite the similar name — two separate real things, not merged.
- `protected_areas.*` (18 lines) — a real faction-base trespass-warning system: flat 12-entry
  owner map (which faction/settlement controls each of 12 real protected areas) + ordered guard
  dialogue lists (1-line overlay prompt, 5-line escalating warnings using the real in-universe
  slur "scofflaw" for trespassers).
- `dmz2026.stat.<id>.name` (14 lines), `artefact_properties.factor.<id>`/`.desc` (4 lines, a real
  flat label+desc pair with a dedicated accessor since the label itself has no field suffix), and
  `player_params.effect.category.<id>` (2 lines) round out the namespace as small real flat label
  groups.

**Doc-only edits made this part**: `LangCatalog.java` marks `stalker` as now fully parsed.

**Testing/build**: 9 new `StalkerLangCatalogTest` tests, all passing. Full suite went from 888 to
897 passing tests. Compiled clean with `--release 11` in the cloud, synced to the device, and
live-verified passing (897/897) on the device's actual Java 11 JRE. No build hiccups this part.

`README.md` and `docs/RESEARCH_LOG.md` were both updated ON THE DEVICE (README's test count
888->897 plus a new per-part bullet; RESEARCH_LOG got a full new "## Part 119" section).

**Context for future parts**: this closes the `stalker` entry from part 90's original top-15
namespace list. Remaining fully-uninvestigated top-15 entries: `item` (12,611 lines, by far the
largest, likely needing a dedicated multi-part effort), `go` (2,624). `gui` (395, the top-level
`gui.*` prefix, NOT `stalker.gui.*` which this part already covered) remains partially closed —
only its `operation.how_to_play` sub-namespace (191 of 395 lines) was done at part 113.

**Also this part (before the code work)**: the user supplied `gl_licence.pdf`/`gl_privacy.pdf`
(EXBO's License Agreement + Privacy Policy). Reviewed in full; factual notes saved to
`Memory/exbo_legal_terms_notes.md` (see that file's own index entry in `MEMORY.md`) rather than
duplicated here. Headline: License Agreement clause 4.1.6 explicitly prohibits "unauthorized
servers that emulate or attempt to emulate the operation of the Game" — directly names this
project's own core premise — while 4.1.1 carves out an official modding exception (a support
article + a real `mods/` folder shipped with the game) and 3.2 references a real "STALZONE API"
or	an official derivative-works path. Not re-litigated here; see the dedicated notes file.

**Also this part (memory-store change)**: the user created a `Memory/` folder directly in the
project directory and asked that project memory be kept there going forward, since the MCP
`project_memory_read`/`project_memory_write` tool has been reporting unavailable since part 115
across multiple sessions (including a full server reconnect). Parts 115-118's stopgap files were
migrated into `Memory/recon_findings_cont71.md`-`cont74.md`; this file is `cont75`, continuing the
same numbering, written directly (no more stopgap layer). See `Memory/NOTE_ABOUT_THIS_FOLDER.md`
for the full situation.
