# STALZONE Server Emulator

A from-scratch, evidence-based reverse-engineering and server-emulator project for **STALZONE**
(Steam AppID 1818450), aiming at full singleplayer/private-server control over spawn rates, damage
values, and eventually mod support (in the spirit of what the SPT team has done for Escape from
Tarkov).

**This repository holds only the project's own source, documentation, and derived research data --
never the game's own copyrighted client files, raw network captures, or local save data.** Those stay
on the researcher's own machine and are deliberately excluded (see `.gitignore`).

## Ground rules this project holds itself to

1. **Never turn an assumption into a protocol specification.** Every reverse-engineered behavior
   must have evidence from client code, controlled captures, reproducible tests, or documented
   public sources -- confirmed facts and hypotheses are always labeled separately.
2. **Focus is game content and possible cut content.** Not the EXBO anti-cheat/launcher internals.
3. **Wire-protocol decryption work is gated behind explicit go-ahead each time**, given the real
   anti-cheat ban risk of experimenting with the live protocol.
4. **Privacy:** no real third-party or the researcher's own players' usernames are ever committed to
   test fixtures -- only field schema/enum-like values or placeholders. Real non-personal
   game-content strings (item names, zone names, faction names, event codes, quest/item text) are
   used verbatim, since they aren't personal data.

## Layout

- `server-emulator/` -- the actual Java server-emulator source: a local (and, longer-term, networked)
  game-logic implementation built directly on validated, evidence-backed data readers for the real
  client's file formats (weapon stats, world geometry, mob AI configs, item weights, anomaly-detector
  behavior, movement tuning, item categorization, Morse code, sound durations, map/settlement
  rosters, vendor trading history, currency, and a growing set of `world/props/*Catalog` classes
  cataloging real game-content asset trees). See `server-emulator/README.md` for the full, current
  state and file-by-file breakdown.
- `docs/RESEARCH_LOG.md` -- the canonical, numbered part-by-part research log: what was found, how
  it was verified, and every disclosed self-correction along the way.
- `docs/PROJECT_PLAN.md` -- the original project plan and stack decision.
- `docs/CAPTURE_GUIDE.md` -- notes on capturing/analyzing traffic (data, not the raw captures
  themselves).
- `data/` -- curated, structured research data derived from the client (weapon stats, example loot
  tables, example mob health data), with each subfolder's own README documenting provenance and
  completeness.
- `tools/` -- standalone Python analysis tools (`nbt_dump.py`, `item_lookup.py`) used during
  research, independent of the Java emulator.

## Status

As of this writing the project has completed 61 numbered research/build parts, compiles clean, and
passes its full Java unit-test suite end to end. See `server-emulator/README.md` for the exact
current source/test counts and `docs/RESEARCH_LOG.md` for the full history.

## Methodology

Every part follows the same discipline: gather live evidence first (client files, logs, captures),
build a typed Java reader/model against it, verify with a live re-check against real data (never
trust a hand-written narrative number), write unit tests from real (non-personal) fixture data, and
disclose corrections transparently rather than silently fixing them -- several parts recorded a
caught-and-corrected error for exactly this reason.
