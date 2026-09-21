# Part 134 -- `PrestigeLangCatalog`: the real "Coliseum"/"Cyclical Trials" PvP arena mode

Continuing top-down from part 133's closing note, this part models the real 79-line
`prestige.*` namespace -- the real "Coliseum" / "Cyclical Trials" PvP arena mode, already
independently confirmed real by several earlier parts from completely different angles:
`TextureGuiCatalog`'s `bundle.notf.prestige.info.start`="Soothsayer has stabilized the Cyclical
Trials arena once again. The Coliseum awaits its..." (naming the same "Soothsayer" entity part
109's `CurrencyDisplayCatalog` first surfaced from `prestige_dollars`="Patterns"),
`StalkerClansCatalog`'s `kolizei` ("coliseum") map id (part 72), `StalkerSoundEventsCatalog`'s
`arena/` sound-file set, and `ClientPrefs`'s real handbook-article path naming the same "Coliseum"
session mode.

## Naming and an explicit overlap disclosure

Named `PrestigeLangCatalog`, not `PrestigeCatalog`, to avoid confusion with the existing
asset-derived `progression.PrestigeAbility` enum (part 66/90). `PrestigeAbility.realDisplayName`/
`realDescription` already read this same namespace's `prestige.ability.<key>.name`/`.description`
entries directly by constructed key, but only for 8 of this namespace's 25 real ability ids -- the
8 `PrestigeAbility` could confirm a literal-word crosswalk for from its own separate asset-derived
roster -- and never enumerated the namespace, nor covered its other 5 real groups at all. This
class is the first full, enumerable catalog of the entire real namespace; it does not replace
`PrestigeAbility`'s own asset-based evidence chain.

## Real shape

Confirmed by direct count to be a single real group with zero flat exceptions: all 79 lines fit
`prestige.<id>.<field>` via the established last-dot-split scheme.

`ability.<abilityId>.name`/`.description` (49 lines, 25 real abilities -- one real id,
`tank_high_gravity_aura`, has a `.description` but no `.name`, an asymmetric field gap already
seen elsewhere in this project, e.g. part 118's `GeneralCatalog` `legend` id).

`classes.<classId>.name`/`.desc` (8 lines, 4 real classes: `noclass`="NO CLASS", `combat`=
"Assault", `scientist`="Support", `combined`="Operative" -- two of these display names
("Assault"/"Support") share exact real English words with 2 of the asset-derived
`progression.PlayerClass` roster's 6 names, while the `ability.*` group's own internal ids use a
DIFFERENT real class-tag-prefix vocabulary (`tank_`/`scout_`/`support_`) sharing literal words
with 3 different `PlayerClass` names -- two genuinely separate real internal-id schemes for what
may be the same underlying class roster, disclosed as-is rather than merged into an unverified
crosswalk, per rule 1).

`howtoplay.tip.<n>.title`/`.description` (8 lines, 4 real tips describing a rifts/batteries/
generators multi-team objective-capture mechanic never documented anywhere else in this project).

`rating.no_access.message` (1 line, "Level 2 Drunken Geiger Bar reputation" -- a real reputation
gate tied to `SettlementRegistry`'s `sever` settlement, part 105).

`specialization.<classId>.<specId>.name`/`.desc` (12 lines, 6 real per-class specializations,
e.g. `combat.push`="Onslaught", `combat.bastion`="Bastion").

`usual.no_access.message` (1 line, "Level 3 Bar reputation" -- a second, distinct real
reputation gate for the same mode's other queue, tied to the standalone "Bar" location already
referenced by part 118's `GeneralCatalog` achievement ids).

## Testing

3 new `PrestigeLangCatalogTest` tests against a fixture built from real key=value lines drawn
directly from the live client's `en.lang`: a real ability's name/description resolving together
plus the real name-less ability resolving its description only, real class/howtoplay/
reputation-gate groups resolving independently, and sorted/null-safe id and field lookups.
Compiled with `--release 11`, run clean in the cloud (969/969) and, synced across via the usual
tarball pipeline, live-verified on the device's actual Java 11 JRE (969/969).

## Context for future parts

Four more down from part 123's long tail (parts 131-134). What remains: `dmz`/`dmz2026` (78/17,
ability text -- still flagged for an explicit near-identical-"Hook"-ability compare when built),
`delivery` (65), `orders` (64), `modules` (59), `item_origins` (55), `seasonpass` (49), and the
long tail of smaller namespaces documented in part 123's own writeup
(`Memory/recon_findings_cont79.md`).
