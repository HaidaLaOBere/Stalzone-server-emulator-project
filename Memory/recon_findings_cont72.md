## Part 116 — `ChallengesCatalog`: 1,011 real challenge/achievement lines modeled with a single generic table, zero flat exceptions

Part 116 investigated `challenges` (1,011 lines) in `stalker/lang/en.lang` — real in-game
challenge/achievement task text (what to do, flavor text, a UI hint, and a separate achievement
title) spanning nearly every season and live-service event this project has confirmed.

New Java class `ChallengesCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`ChallengesCatalogTest.java` (8 tests).

A direct count established that every one of the 1,011 real lines fits ONE single shape,
`challenges.<id>.<field>`, with the field always the terminal simple token and the id everything
before it — including embedded `/` path segments (e.g. `season.summer26/pg_challenges/g4/8`) and,
for several real ids, embedded dots of their own (e.g. a distinct real `.na` variant id alongside
the base id — disclosed as two separate real ids rather than merged, since nothing confirms what
`.na` stands for beyond the naming pattern). This let the WHOLE namespace be modeled with a single
generic `(id, field)` table via one `field()` accessor — confirmed to have ZERO flat exceptions
(every real line has at least one dot after the `challenges.` prefix), unlike part 111's
`WorldEventCatalog` or part 113's `OperationsCatalog`, each of which needed a separate flat-line
carve-out. This is architecturally notable: the simplest design yet for a namespace this large,
justified by actually checking (not assuming) the "zero flat exceptions" property first.

Five real one-off field names round out the small tail beyond `name`/`description`/`hint`/`label`:
three real week-label lines filed directly on the `newyear24` group id itself (not under any
numbered sub-challenge) — `first_week`="First week", `second_week`="Second week",
`third_week`="Third week"; a real template string, `achievements.complete=Complete the {key}
achievement`; and the one real id in the whole namespace using the short field name `desc` instead
of `description` — `upg_motif.fmg9_bureau.desc=Get money from the trader for selling items`. All
five handled by the same generic split, no special-casing required.

`reichstag/1`-`reichstag/8` (8 lines, real Assault-on-the-Reichstag session-mode challenge tasks —
"Kill {amount} players while playing the Assaulter/Scout/Engineer/Machine Gunner class", "Score
{amount} capture/defense points", "Complete 1 game") is disclosed per the part-109 standing note as
simply another entry in the long Reichstag corroboration chain — NO new ordinal asserted.

The real `label` field (60 lines) surfaces this project's densest single block of
pop-culture-reference achievement titles found in one namespace: `summer26/pg_challenges/g1/7.label
=Roadside Picnic` (the real 1972 Strugatsky brothers novel the original S.T.A.L.K.E.R. game series
itself draws from), `g4/1.label=Hacksaw Ridge` (a real film title), `g3/3.na.label=The Cave of
Wonders` (Aladdin), `g3/7.na.label=Ohio Final Boss` (a real internet-meme reference), `g1/3.label
=Money Changer`/`.na.label=No ATMs in the Zone` — disclosed as real client text, not investigated
further per rule 1.

**Doc-only edits made this part**: `LangCatalog.java` marks `challenges` as now fully parsed.

**Testing/build**: 8 new `ChallengesCatalogTest` tests, all passing. Full suite went from 869 to
877 passing tests. Compiled clean with `--release 11` in the cloud, synced to the device, and
live-verified passing (877/877) on the device's actual Java 11 JRE. No build hiccups this part.

`README.md` and `docs/RESEARCH_LOG.md` were both updated ON THE DEVICE already (README's test count
869->877; RESEARCH_LOG got a full new "## Part 116" section) — only the project-memory system
(recon_findings/MEMORY.md) update is outstanding, same as part 115.

**Context for future parts**: this closes the `challenges` entry from part 90's original top-15
namespace list. Remaining fully-uninvestigated top-15 entries: `item` (12,611 lines, by far the
largest), `go` (2,624), `stalker` (2,020), `general` (1,143), `quest_contract` (1,064). `gui` (395)
remains partially closed (only its `operation.how_to_play` sub-namespace, 191 of 395 lines, was
done at part 113).
