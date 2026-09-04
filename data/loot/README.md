# Loot tables (emulator-original, part 48)

**This directory does NOT contain reverse-engineered STALZONE data.** This project
looked for a real client-side loot/drop-rate table (the "163 additional loot configs"
log-line investigation, `docs/RESEARCH_LOG.md` part 43) and concluded one is very
likely absent from client files by design, consistent with the already-repeatedly-
confirmed absence of a real per-zone mob spawn table (parts 29/30/etc.). There is
nothing to reverse-engineer here.

`example_loot_tables.json` is a small, structurally-valid EXAMPLE file for
`net.stalzone.emulator.loot.LootTables` -- loaded by `GameLoopDemo` when a loot-table
path is passed on the command line. Its `mobId` values are real (`base_boar`,
`base_tushkan`, real `.smm` config-file stems -- see `mobs/MobConfig#configId()`) and
its `itemId` values (`16340`, `16347`) are real item ids already used elsewhere in this
project's own demo data (`GameLoopDemo`'s demo inventory, from real `weight.txt` data),
but the `dropChance`/`weight`/count-range VALUES are entirely invented for this example
-- not measured, not leaked, not a claim about what STALZONE itself drops. An operator
or modder is expected to replace this file with their own, per the project's original
goal of "full control over what can be done... later modding support."

See `net.stalzone.emulator.loot.LootTable`'s Javadoc for the full explanation, and
`net.stalzone.emulator.loot.LootRoller`'s Javadoc for the exact roll semantics
(`ServerConfig.lootRateMultiplier` scales drop CHANCE, not dropped count).
