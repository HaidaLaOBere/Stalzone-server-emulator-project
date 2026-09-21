## Part 115 — `ShopCatalog`: the in-client item-shop's full UI text, a real seasonal-event timeline, and a data-driven catch-all for a growing promo-page roster

Part 115 investigated `shop` (731 lines) in `stalker/lang/en.lang` — the in-client item-shop UI's
own text. This is the largest single namespace fully catalogued outside `we` (508 lines, part 111)
and the still-uninvestigated `item`/`go`/`stalker`/`general`/`quest_contract`/`challenges`
namespaces.

New Java class `ShopCatalog.java` (package `net.stalzone.emulator.world.props`), plus
`ShopCatalogTest.java` (10 tests). Real sub-groupings, modeled with a mix of generic (id, field)
tables and flat maps:

- `season.<id>.name` (10 lines) — a real COMPLETE named seasonal-event timeline running summer24
  through autumn26: summer24="ZIVCAS CORPORATION", autumn24="BLACK MARKET: INTERVENTION",
  winter25="OPERATION CRYOSTASIS", spring25="BACK TO THE INSIDE OUT", summer25="MURMUR OF THE
  ZONE", autumn25="HORRORS OF THE ZONE", winter26="SUB-ZERO ZONE", spring26="TEMPERED STEEL",
  summer26="ANTICYCLONE", autumn26="TULPA!". The last two (spring26/autumn26) are real season
  names never seen in this project before.
- `categories.mk.title=SEA OF EXCLUSION` — a FOURTH independent real corroboration of part 113's
  "Sea of Exclusion Operation" name (`OperationsCatalog`), tying the shop's "mk" category code to
  that Operation.
- `categories.mt.title=ASSAULT ON THE REICHSTAG` — ties the shop's "mt" category code to the
  confirmed Reichstag battlefield. Disclosed per the part-109 standing note as just another entry
  in the long corroboration chain — NO new ordinal number asserted.
- `dlc.*` (69 lines) — a real Battle Pass/season-pack promotional timeline going back to
  `autumun_2023_bp` ("Dead Man's Marathon"), which PREDATES every other seasonal-timeline source
  found so far (`season.*` above only starts at summer24). `refund.mainTitle`="Compensation"
  documents a real automatic-refund-to-stalcoins mailer for an unused pack whose associated event
  ended.
- `customTags.*` (73 lines) — real shop badge/tag text (new="NEW", tradeable="TRADABLE GEAR",
  sequence="PACK CHAIN"). Two real ids, `bundle_dmz26_discount` and `ny_tree_gifts_discount`,
  carry `desc_global`/`desc_ru` variants with genuinely DIFFERENT real discount percentages for
  the same purchase tier (e.g. bundle_dmz26_discount: 36% global vs 40% ru for the 6th-10th-
  purchase tier) — a real, disclosed regional-pricing variance within the English locale file
  itself, NOT merged into one value.
- `tabs.*` (117 lines) and `offers.*` (247 lines) — each modeled generically but split the
  OPPOSITE way from each other: tabs groups by the OUTERMOST real tab id (a hierarchy: tab ->
  subpage -> field), while offers splits at the TERMINAL field (titleLabel/typeLabel/name/desc/
  warningMessage), keeping compound offer ids like `items.dayx_pack` intact as one opaque id.
  Deliberate, documented design choice (not an inconsistency) — the two namespaces have genuinely
  different real hierarchies. One real flat exception: `shop.offers.amount={amount} UNITS` (a
  template string, no id/field structure at all) is exposed via a separate dedicated accessor
  rather than force-fit into the table.
- `category.<id>.name` (49 lines) — a real shop-filter taxonomy with compound ids expressing a
  two-level category/sub-category hierarchy (weapon="Weapons", weapon.sub.melee="Melee", etc.,
  for weapon/armor/equip/supplies/rank/month). One real flat exception: `category.bundles=Season
  Pack` (no `.name` suffix at all) folds into the same lookup rather than being treated as an
  error.
- `topup.method.<id>.name` (7 lines) — a real real-money payment-method roster: sbp="FPS",
  card="Card", xsolla="Xsolla", vkplay="VK Play", 4game="4game", steam="Steam", stove="Stove".
- Smaller flat groups also modeled: `categories.<id>.title` (15 lines), `displayedCurrencies.*`
  (20 lines, generic), `deepPage.filters.title.<key>` (9 lines), `gui.offer.preview.<key>`
  (4 lines), `year.<id>.name` (2 lines: 25="COLLECTION 2025", 26="2026 COLLECTION").
- A real, GROWING set of roughly twenty standalone one-off top-level promo/bundle-page ids
  (bundle_newyear24, bundle_spring24_1/_2, bundle_halloween23 and variants, bundle_ravent23 — the
  season-pack page for the confirmed "Arena of Slavs" PvP mode from part 93 — dayx_pack, starter
  packs, daily/weekly delivery pages, etc.) are deliberately NOT individually enumerated by name
  in the code. Since they all share one real shape (a promo detail page whose field suffix can
  nest arbitrarily deep) and this list visibly grows every season, `ShopCatalog.load()` instead
  treats ANY top-level `shop.<id>.<field>` line whose `id` doesn't match one of the explicitly-
  named groupings as belonging to a generic catch-all table — a deliberately DATA-DRIVEN design
  so a future season's new promo page is captured automatically rather than silently dropped or
  requiring a code change. First time this project used an implicit "everything else" catch-all
  keyed by absence-from-a-known-list, rather than an explicit enumerated skip/include list.

**Doc-only edits made this part**: `LangCatalog.java` marks `shop` as now fully parsed.
`OperationsCatalog.java` (part 113) got an "UPDATED part 115" Javadoc note cross-referencing the
fourth Sea-of-Exclusion corroboration and the disclosed-not-counted Reichstag tie.

**Testing/build**: 10 new `ShopCatalogTest` tests, all passing. Full suite went from 859 to 869
passing tests. Compiled clean with `--release 11` in the cloud, synced to the device, and
live-verified passing (869/869) on the device's actual Java 11 JRE. No build hiccups this part.

`README.md` and `docs/RESEARCH_LOG.md` were both updated ON THE DEVICE already (README's test
count 859->869; RESEARCH_LOG got a full new "## Part 115" section) — only the project-memory
system (recon_findings/MEMORY.md) update is outstanding.

**Context for future parts**: this closes the `shop` entry from part 90's original top-15
namespace list. Remaining fully-uninvestigated top-15 entries: `item` (12,611 lines, by far the
largest), `go` (2,624), `stalker` (2,020), `general` (1,143), `quest_contract` (1,064), `challenges`
(1,011). `gui` (395) remains partially closed (only its `operation.how_to_play` sub-namespace, 191
of 395 lines, was done at part 113).
