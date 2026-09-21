# recon_findings_cont134.md — Part 178: engine wiring, 4th mob-name crosswalk entry

**Date:** 2026-09-20

## Context

The user chose to pivot from content cataloguing to engine wiring after the faction-templated bug
hunt closed out (part 177). Extended that same check one more step first: ran the internal
keyword-consistency check against `FactionBaseWarningCatalog`'s 25 guard-dialogue lines and
`CnpsCatalog`'s 126-line faction/settlement reputation ladders. Both came back completely clean —
no cross-contamination anywhere — confirming the faction-template bug hunt is exhaustively closed
at exactly 2 real bugs (parts 173, 177).

Then moved to engine wiring proper. `GameLoop`'s own class doc names its real headline gap
directly: `WorldFlavorCatalog.entityNameForMobConfigId` (part 165) is a hardcoded 3-entry crosswalk
from a real `MobConfig#configId()` to a real `en.lang entity.*` display name, out of the full real
35-config mob roster — meaning 32 configs still have no real display-name crosswalk. Only 5 real
`entity.*` lines exist in `en.lang` at all; 3 were already wired (`tick`, `carevich`, `carevna`),
leaving 2 open leads (`tushkanchik_vor`="Rat-Robber", `kukla`="Doll") that earlier parts had found
but never matched to a specific mob config.

## Methodology: read the real files, don't guess from summaries

Rather than reasoning from already-written Javadoc alone, staged and directly read every real
`stalkermobs/configs/*.smm` file on the device (35 files) and extracted each one's own `mobName`
field via a direct grep, specifically looking for anything that could plausibly correspond to
`tushkanchik_vor` or `kukla`.

Full real mobName roster pulled this part (35 configs):
```
base_boar -> boar, base_boar_clone -> boar, base_chimera -> chimera, base_chimera_clone -> chimera,
base_cybergiant -> cybergiant, base_dog -> dog, base_dog_clone -> dog, base_doge -> doge,
base_drone -> spirit, base_flesh -> flesh, base_flesh_clone -> flesh, base_fpv -> fpv,
base_krovosos -> krovosos, base_krovosos_clone -> krovosos, base_lurker -> lurker,
base_pseudodog -> pseudodog, base_pseudodog_clone -> pseudodog, base_pseudogigant -> pseudogigant,
base_pseudogigant_clone -> pseudogigant, base_psidog -> psidog, base_psidog_clone -> psidog_clone,
base_puppet -> puppet, base_puppet_clone -> puppet, base_rudolf -> rudolf, base_snork -> snork,
base_snork_clone -> snork, base_spirit -> spirit, base_thief_rat -> tushkan_thief,
base_tick -> tushkan, base_tsarevich -> boar_tsarevich, base_tsarevna -> flesh_tsarevna,
base_tumbler -> tumbler, base_tushkan -> tushkan, base_tushkan_clone -> tushkan, base_wisp -> wisp
```

## The find: `base_thief_rat` -> `tushkanchik_vor`

`base_thief_rat.smm`'s real `mobName` is `"tushkan_thief"` — a distinct, non-generic value not
shared with any other of the 35 real configs. "Thief" + "tushkan" (jerboa/rat) is exactly the same
real compound concept as `tushkanchik_vor` ("tushkanchik"=little-tushkan, "vor"=Russian for
"thief") and its own real display text "Rat-Robber". This is the same kind of real
transliteration/spelling variance already established and accepted for `tsarevich`/`carevich`
(same real underlying concept, two different real id spellings from two independent parts of the
client) — not an exact string match, disclosed with that same caveat.

## Checked and found inconclusive: `base_puppet` vs. `kukla`

`base_puppet.smm`'s real `mobName` is plainly `"puppet"` — no doll-related field anywhere. Its
`_sounds` block reuses the `tushkan` (jerboa/rat) sound family wholesale (identical sound IDs to
`base_thief_rat` and other tushkan-family configs), which weakens rather than strengthens a
doll-specific reading, since sharing that sound family is common and not distinguishing. Per rule
1, this is disclosed as a checked, inconclusive lead — `kukla` is NOT wired into the crosswalk on
the strength of name similarity alone.

## Incidental re-confirmation

`base_drone.smm`'s real `mobName` is `"spirit"` (shared with `base_spirit.smm`) — already disclosed
in `MobConfig`'s own class doc from an earlier part, not new, but directly re-confirmed against the
real file during this pass.

## Code changes

- `WorldFlavorCatalog.entityNameForMobConfigId`: added `if ("base_thief_rat".equals(mobConfigId)) return entityName("tushkanchik_vor");`.
- `WorldFlavorCatalog`'s class Javadoc and the method's own Javadoc: disclosed the new confirmed
  entry and the checked-but-inconclusive puppet/kukla lead, with full reasoning.
- `GameLoop`'s class doc and `setEntityCatalog`'s doc: updated "3 real mob configs" to "4", added a
  new Javadoc paragraph documenting this part's investigation and its "after 12 more content-only
  parts" framing, mirroring part 165's own "after 74 parts" framing.

## Testing

1 new `WorldFlavorCatalogTest` test, extending the fixture with the real `entity.tushkanchik_vor.name`/
`entity.kukla.name` lines, asserting `base_thief_rat` resolves to "Rat-Robber" and `base_puppet`
still resolves to null. Live-verified with a dedicated scratch probe (`EntityCheck.java`, deleted
after use) directly against the real, full `en.lang` file: all 4 wired configIds and both raw
`entity.*` lookups confirmed exactly. Full suite run clean on both a plain and a `--release 11`
build in the cloud, then synced across and re-run on the device's actual Java 11 JRE: 1109 -> 1110.

## Standing rules followed

- Rule 1: the thief_rat/tushkanchik_vor match is disclosed with its transliteration-variance
  caveat, not asserted as an exact identity; the puppet/kukla lead is explicitly NOT wired despite
  superficial name similarity, since the actual config evidence doesn't support it.
- Rule 3: no wire-protocol work touched.
- Rule 6: nothing pushed to GitHub this part.

## Next step

This is the second-ever `GameLoop`/`WorldFlavorCatalog` engine-wiring pass (after part 165), and
like that one, purely cosmetic — no new numeric simulation. Further progress along this exact
avenue would mean checking the remaining 31 unmatched mob configs' `mobName`/sound/texture fields
against any OTHER already-catalogued `en.lang` name list (not just the 5-line `entity.*`) for a
similar transliteration-variant match — not attempted this part. Otherwise the standing menu
remains open: push to GitHub, or apply the branch-mirroring completeness technique to the handful
of large classes not yet checked that way.
