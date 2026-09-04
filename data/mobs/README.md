# Mob health table (emulator-original, part 49)

**This directory does NOT contain reverse-engineered STALZONE data.** Real
`modassets/assets/stalkermobs/configs/*.smm` mob config files carry NO health/HP field
anywhere in their schema -- confirmed across all 35 real files (`docs/RESEARCH_LOG.md`
part 10, re-checked part 20, re-confirmed again while building
`net.stalzone.emulator.mobs.MobInstance` in part 30). There is nothing to
reverse-engineer here.

`example_mob_health.json` is a small, structurally-valid EXAMPLE file for
`net.stalzone.emulator.mobs.MobHealthTable` -- loaded by `GameLoopDemo` when a
mob-health-table path is passed on the command line. Its keys are real mob
`configId`s (real `.smm` config-file stems, e.g. `base_boar`, `base_pseudogigant` --
see `mobs/MobConfig#configId()`), but the `maxHealth` VALUES are entirely invented for
this example -- not measured, not leaked, not a claim about what STALZONE itself uses
(there is no such real value to leak). A mob id with no entry in this file simply falls
back to `net.stalzone.emulator.mobs.MobInstance#DEFAULT_MAX_HEALTH` (flat 100.0, same
placeholder used before this table existed). An operator or modder is expected to
replace this file with their own, per the project's original goal of "full control over
what can be done, like ... damage values ... later modding support."

See `net.stalzone.emulator.mobs.MobHealthTable`'s Javadoc for the full explanation.
