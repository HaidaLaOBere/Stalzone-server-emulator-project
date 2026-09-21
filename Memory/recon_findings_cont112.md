# Part 156 -- `OutpostModeCatalog`

## Real shape

The real `outpost.*` namespace (14 lines), the thirty-sixth concrete namespace built from part
123's follow-up census. Reconciled exactly: 11 (generic id/field table) + 3 (flat reward-tier
carve-out) = 14. Named distinct from part 114's `HomeOutpostCatalog` (confirmed via direct
comparison: no shared keys/vocabulary).

## Real finds

A real capture-the-point PvP mode. Attacker/defender role objectives and HUD status text, both
sides sharing an identical real mail-reward `.desc` line (client copy-paste, disclosed not
corrected), a real "Headquarters" capturable spawn point, win/loss outcome banners, and 3 real
zero-dot reward-tier fields (low/medium/high, all "for 1 outpost capture") requiring a flat
carve-out alongside the generic table -- same shape as part 143's PremiumCatalog.

## Real corroboration

Part 26's MilestoneMap already confirmed a real, cached, visited zone named `outpost_merc`. This
namespace independently supplies the actual capture-mode UI text for that exact real
Mercenary-affiliated map -- a further corroboration, not a new independent finding.

## Testing

3 new `OutpostModeCatalogTest` tests against a fixture built from real key=value lines. Run clean
on both a plain and a `--release 11` build in the cloud and, synced across, on the device's
actual Java 11 JRE: 1032 -> 1035.

## Context for future parts

Thirty-six namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `ny`(34)/`ny_toys`(13), `season`(12, singular, distinct from
`shop.season.*` and `seasons`), `personal_storage`(11, worth a quick disclosed-not-merged check
against part 143's PremiumCatalog `perk_block.personal_storage` field). After that, the smaller
grab-bag: `profile_icon`, `banner`, `territories`, `provision`, `mail`, `entity`, `item_tier`,
`anomaly`, `barter`, `menulinks`, `death`, `review`, `main_menu_popup`, `tutorial`, `bonuscode`,
plus ~15 single-line "prefixes" not worth individual classes.
