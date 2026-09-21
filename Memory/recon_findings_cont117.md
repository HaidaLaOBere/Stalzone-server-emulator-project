# Part 161 -- `EconomyRolloutCatalog`

## Real shape

Three real tiny namespaces (13 lines total), the forty-fourth through forty-sixth concrete
namespaces from part 123's follow-up census: banner.information.barter_upgrade_compensation.*
(9, generic table, 4 ids via nesting), barter.category.* (2, flat), main_menu_popup.
act1_ticket_warning.* (2, generic table).

## Real finds

A real "BARTER AND MODIFICATIONS REWORK" banner resolving cleanly to 4 ids via generic split
with no special-casing even at 4+ nesting levels. barter.category names the 2 real affected UI
categories (Suits/Attachments), thematically paired with the banner, disclosed not merged. A
real separate "ACT TICKETS REMOVAL" announcement referencing real ZIVCAS cosmetic items,
confirming part 115's ZIVCAS branding.

## Testing

3 new `EconomyRolloutCatalogTest` tests. Run clean on both a plain and a `--release 11` build in
the cloud and, synced across, on the device's actual Java 11 JRE: 1047 -> 1050.
