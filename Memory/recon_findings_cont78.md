## Part 122 — `GuiLangCatalog`: closes out the top-level `gui.*` namespace, completing part 90's original top-15 `en.lang` namespace list in full

Part 122 investigated the remainder of the top-level `gui.*` namespace (402 lines total, direct
count — part 90's original estimate of 395 had been approximate): 211 lines not already covered by
part 113's OperationsCatalog, which modeled `gui.operation.how_to_play` alone (191 lines). This
class deliberately excludes that already-covered prefix rather than re-parsing it. Together the two
classes now close the top-level `gui.*` prefix in full. (Not to be confused with StalkerLangCatalog's
already-fully-covered `stalker.gui.*` — a distinct real namespace under a different top-level prefix.)

New Java class `GuiLangCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`GuiLangCatalogTest.java` (7 tests).

- Direct count confirmed this remainder is a FOURTH namespace found (after parts 116/120/121) with
  ZERO real flat exceptions — every line fits `gui.<id>.<field>`: 74 distinct real ids across 17
  real event/season/mode groups, dominated by real marketing-banner text (title: 58, desc: 50,
  headerLabel/actionText: 9 each, endTimeLabel: 7, plus numbered title1-4/desc1-4 sub-blocks).
- 13 of 17 groups (nytree, encaged, summer25_event, summer25, streamer_battle, battlefields,
  autumn24_event, winter25, summer24, spring25, autumn24, spring24, newyear24) are real in-client
  season/event marketing banners — corroborate the existing seasonal timeline (parts 109/115/121)
  with RICHER narrative copy than those sources carried alone (full "MURMUR OF THE ZONE" summer25
  announcement text; spring24="Arena of Slavs" with its own bogatyr-themed flavor text). One real
  line disclosed as genuinely EMPTY rather than missing: spring25.banner.desc= has no text at all
  in this client build.
- Remaining 4 groups are distinct real UI pockets: encaged.* (a real "Encaged" mode tutorial-card
  system — class-select/mission/enemy-warning cards, incl. hunter_desc.wh="Can see survivors and
  equipment through walls", a wallhack-style enemy ability tag); pause.* (pause-menu grab-bag: clan
  -season-reward rules + 4-tier arcade rank ladder Rookie/Soldier/Experienced/Specialist);
  case.treasury.bonus.<tier>.* (real 3-tier loot-case pity-system explanation, legend/silver/bronze,
  drop-chance-increases-per-opening resetting on drop); bureau.* (reward-type labels distinguishing
  unique numbered/effect weapon rewards from plain ones).
- Doc-only: `LangCatalog.java` marks the top-level `gui.*` prefix as now fully parsed (combined
  with part 113's OperationsCatalog).
- Code compiled with `javac --release 11 -d out @sources.txt` in the cloud (zero warnings/errors),
  full suite run clean in the cloud (916/916), synced to the device and live-verified clean on the
  device's actual Java 11 JRE (916/916): test count 909 -> 916.
- README.md/RESEARCH_LOG.md both updated on the device (README bullet + running count;
  RESEARCH_LOG got a full "## Part 122" section).

**MILESTONE**: this completes part 90's original "top 15 by count" `en.lang` namespace list IN
FULL — every one of those 15 namespaces now has real, verified, tested coverage (a dedicated
class, or an explicit documented decision that its content duplicates/belongs to an
already-modeled system). That list was always a snapshot, not an exhaustive inventory of the whole
file — a natural next step for a future part is to survey what OTHER `en.lang` namespaces exist
outside that original top-15, rather than treating this as the end of `en.lang` work.

**Memory-store note**: continues the file-based Memory folder numbering (cont71-77 so far). See
`Memory/NOTE_ABOUT_THIS_FOLDER.md` for the full situation re: the MCP project-memory outage.
