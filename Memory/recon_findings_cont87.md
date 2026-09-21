# Part 131 -- `BpSeasonCatalog`: real Battle Pass seasonal "how to play" mechanics text plus promotional ad-card copy

Continuing top-down from part 130's closing note, this part models the real 98-line
`bpseason.*` namespace -- real Battle Pass seasonal-event "how to play" mechanics text plus
in-client promotional ad-card copy. Confirmed by direct count to be a single real group with
zero flat exceptions: every line fits `bpseason.<id>.<field>` via the same last-dot-split
generic scheme already used by `GuiLangCatalog`/`WorldEventCatalog`.

## Naming and a real structural quirk

Named `BpSeasonCatalog` after the real literal prefix, explicitly distinct from `ShopCatalog`'s
already-covered Battle Pass BANNER/marketing timeline (part 115) -- checked and confirmed a
different real system. Several real ids embed a literal `/` character (e.g. `24/spring`,
`26/winter`) -- a genuine real structural quirk, not normalized, already seen once before with
`StalkerLangCatalog`'s (part 119) `approv/mark/<type>` ids.

## Real shape

Splits into 3 real groups: `<year>/<season>.event.*` (58 lines across 7 real season ids --
`24/spring`, `24/autumn`, `25/winter`, `25/spring`, `25/summer`, `26/spring`, `26/winter` --
each a real 4-block "how to play" mechanics breakdown for that season's headline event mode;
only `24/spring` additionally carries a root `event.title`/`event.description`, the other 6
have only their 4 blocks), `ads.*` (37 lines, real in-client Battle Pass promotional ad-card
copy: weapon/style level-reward previews, a "SEASON PASS" upsell card, recurring-reward and
item-pack callouts -- one real line is disclosed as genuinely empty rather than missing,
`ads.ad_item_pack_1.description=`), `rewards.*` (3 lines, flat reward-category labels:
`motif`="Motif", `style`/`style_and_motif`="Style").

## Real cross-namespace finds

Two are worth disclosing rather than silently modeling away. First: `24/spring.event.title`/
`.description` ("Arena of Slavs" / the "Max Crow returns to the Zone..." blurb) are
BYTE-IDENTICAL to `GuiLangCatalog`'s already-covered `spring24.banner.title`/`.desc` (part 122)
-- a real verbatim client duplication of the same marketing copy across two differently-shaped
namespaces (`24/spring` vs `spring24`), confirmed genuinely distinct where it matters: each
namespace's own block-level breakdown is unique (gui's `banner.block1-3` are marketing feature
callouts -- "Case with styles", "Season Styles", "Season Pass" -- while this class's
`event.howtoplay.block1-4` are the actual gameplay-mechanics explanation -- "Protect the Idol of
Perun", "Buy equipment", "Test your Strength", "Get Rewards"). Second: `25/summer`'s 4 real
blocks name two NPCs never seen before ("Kondratenko", "Zaharov") and describe choosing between
"southern and northern directions" with SSPP artillery/aircraft support -- a real independent
corroboration of part 129's `FrontlineCatalog` South/North campaign-direction structure from a
completely separate namespace. `26/spring`'s 4 real blocks are explicitly Reichstag-mode "how to
play" text -- disclosed per the standing note as simply another entry in the long Reichstag
corroboration chain, no new ordinal asserted.

## Testing

3 new `BpSeasonCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`: a real season id embedding a literal `/` resolving
both its root and nested block fields, the real Reichstag-tied `26/spring` block plus the
`ads`/`rewards` groups resolving independently, and sorted/null-safe id and field lookups.
Compiled with `--release 11`, run clean in the cloud (960/960) and, synced across via the usual
tarball pipeline, live-verified on the device's actual Java 11 JRE (960/960).

## Context for future parts

One more down from part 123's long tail. What remains: `party_missions` (95), `ravent` (87, tied
to part 130's `world.modifier.ravent_level_*` finds), `prestige` (79), `dmz`/`dmz2026` (78/17,
ability text -- still flagged for an explicit near-identical-"Hook"-ability compare when built),
`delivery` (65), `orders` (64), `modules` (59), `item_origins` (55), `seasonpass` (49), and the
long tail of smaller namespaces documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`). `ravent` is a natural next candidate given its direct tie to
this project's own part-130 `WorldModifierCatalog` finds.
