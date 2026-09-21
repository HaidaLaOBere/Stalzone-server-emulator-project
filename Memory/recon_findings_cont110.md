# Part 154 -- `InsideCatalog`

## Real shape

The real `inside.*` namespace (16 lines), the thirty-fourth concrete namespace built from part
123's follow-up census. Reconciled exactly: 2 (`ability.active.inside_recovery.*`) + 2
(`ability.passive.phantom_cure_buffs.*`) + 6 (`leagues.<n>.name`) + 6 (`leagues.<n>.reward_text`)
= 16.

## Real finds

`inside_recovery`="Burnt Onyx", a real active ability usable as a med kit.
`phantom_cure_buffs`="Altar of Cleansing", a real passive gravity-reduction ability. The real
numbered "Back to the Inside Out" ranked-event ladder (`leagues.1`-`leagues.6`) with full
reward-letter flavor text for each rank.

## A CONFIRMED crosswalk, resolving part 139's suggestive one

Part 139's ItemOriginsCatalog found a real `inside_league_<codename>` naming series and inferred
which numbered league each of 6 codenames belonged to purely from parallel text content, without
ever seeing the actual numbered ladder. This standalone `inside.leagues.<n>` namespace IS that
real numbered ladder, and its 6 real names match ItemOriginsCatalog's inferred mapping exactly:
`leagues.1`="Legend of the Inside Out" (`legend`), `leagues.2`="Risen" (`master`),
`leagues.3`="Phantom" (`veteran`), `leagues.4`="Distorted" (`stalker`), `leagues.5`="Devastated"
(`newbie`), `leagues.6`="Shadow" (`picklock`) -- upgrading part 139's inference to a directly
confirmed real crosswalk. The crown-jewel corroboration of this part.

## Testing

3 new `InsideCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1026 -> 1029.

## Context for future parts

Thirty-four namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `blizzard`(16, likely thematically paired with part 150's
`snowstorm` -- worth an explicit compare when built), `outpost`(14, MUST check for overlap with
existing `HomeOutpostCatalog` before writing), `ny`(34)/`ny_toys`(13), `season`(12, singular,
distinct from `shop.season.*` and `seasons`), `personal_storage`(11, worth a quick
disclosed-not-merged check against part 143's PremiumCatalog `perk_block.personal_storage`
field). After that, the smaller grab-bag: `profile_icon`, `banner`, `territories`, `provision`,
`mail`, `entity`, `item_tier`, `anomaly`, `barter`, `menulinks`, `death`, `review`,
`main_menu_popup`, `tutorial`, `bonuscode`, plus ~15 single-line "prefixes" not worth individual
classes.
