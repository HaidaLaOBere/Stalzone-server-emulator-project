# Part 148 -- `GameObjectsLangCatalog`

## Real shape

The real `gameobjects.*` namespace (21 lines), the twenty-eighth concrete namespace built from
part 123's follow-up census -- real quest-object interaction-verb tooltip text. Named
`GameObjectsLangCatalog` to avoid confusion with the unrelated file-based `GameObjectsCatalog`
(part 74, asset files). Reconciled exactly: 20 (`action_tip.go_quest.<id>`, one shared id) + 1
(`action_tip.safe.requires_key`) = 21.

## Real finds

20 quest-object interaction prompts (`ex_chrism`="Hang the chrism", `dig_hole`="Dig a hole",
`cook_soup`="Cook soup", etc). Three real ids share the literal text "Place bait" (a real
DMZ-mode bait mechanic). One real id (`33627`) is a literal decimal quest/object identifier used
directly as the field name. `action_tip.safe.requires_key`="Requires" is a separate real
safe/lockbox prompt fragment.

## Testing

3 new `GameObjectsLangCatalogTest` tests against a fixture built from real key=value lines. Run
clean on both a plain and a `--release 11` build in the cloud and, synced across, on the device's
actual Java 11 JRE: 1008 -> 1011.

## Context for future parts

Twenty-eight namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `incident`(20), `snowstorm`(18), `industries`(18),
`challenge`(17, singular, distinct from plural `challenges`), `arena`(17), `inside`(16),
`blizzard`(16), `outpost`(14, check for overlap with existing `HomeOutpostCatalog`),
`ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11).
