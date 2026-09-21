# Part 162 -- `WorldFlavorCatalog`

## Real shape

Seven real tiny namespaces (32 lines total), the forty-seventh through fifty-third concrete
namespaces from part 123's follow-up census: territories.* (8, generic+flat carve-out),
entity.* (5, flat), anomaly.notf.* (4, generic), death.attack.* (3, flat), tutorial.hint.medical
(1), item_tier.* (4, flat), provision.* (7, flat).

## Real finds -- richest single-part corroboration count of the grab-bag sweep

territories.spring25_crystal ties an open-world objective to part 158's SeasonCatalog 25/spring
"Back to the Inside Out". entity.tick/carevich confirm part 69's StalkerMobsCatalog tick/
tsarevich mutants exactly; entity.carevna="Piggy Princess" REFINES that doc's hedged "presumably
the same royal-boar theme" guess with new evidence pointing to a piggy/flesh theme instead.
anomaly.notf.anomaly_chro's real text matches plain anomaly, NOT anomaly_chrono despite closer
id spelling -- disclosed as-is. item_tier.* reuses the newbie/stalker/veteran/master codename
vocabulary from part 139's ItemOriginsCatalog as real item-tier names. provision.* is an EXACT
crosswalk against part 136's DeliveryCatalog north_*/wnorth_* location names (Institute,
Serpentine, Temple Swamp, Barrier, Inflow, Mines, Stillwaters), including a real client typo in
DeliveryCatalog's own north_serpentinte id not shared here.

## Testing

3 new `WorldFlavorCatalogTest` tests. Run clean on both a plain and a `--release 11` build in
the cloud and, synced across, on the device's actual Java 11 JRE: 1050 -> 1053.
