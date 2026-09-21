# Recon findings -- cont127 (Part 171)

## `world.props.TriumphCatalog`

The last of the three open leads part 168's own closing note originally flagged: a real,
previously-uncatalogued 4-line namespace, `triumph.2026.*` -- real reward-mail text for a
"Triumphs" seasonal event. Confirmed via `grep -c "^triumph\."` against the full real file: exactly
4 lines, no other real `triumph.*` content exists anywhere in the client.

**Real content.**
- `reward_type.reward_level_10`="STATUE"
- `reward_type.reward_level_30`="CUSTOMIZATION"
- `mail.message.reward.title`="Reward for completing the Triumphs:"
- `mail.message.reward.text`="Your reward for completing {count} Triumphs"

The two reward-type lines key a real reward name by a real numeric completion-level threshold
embedded directly in the key (10, 30) rather than by a separate id field. The two mail lines are a
real templated in-game mail body; `{count}` is a real client-side substitution placeholder, kept
verbatim rather than interpreted.

**Modeling decision.** Real key depth is irregular across the two real sub-groups
(`reward_type.reward_level_<n>` is 2 real segments after the `triumph.2026.` prefix;
`mail.message.reward.<field>` is 3), so -- following this project's established precedent for
exactly this situation (`TutorialGuideCatalog`, part 168) -- this class does not force the generic
last-dot-split `(id, field)` table onto it. Instead: a flat `Map<String,String>` keyed by the real
full suffix, plus two small typed accessors honest to each real sub-group's own shape:
`rewardTypeForLevel(int)` (parses the real embedded level number, so callers don't reconstruct the
real key string by hand) and `mailRewardTitle()`/`mailRewardText()` (fixed real single-instance
fields -- there is only ever one real mail-reward title/text pair in this namespace, not a table).

**Confirmed real crosswalk, written up for the first time.** This namespace's `2026` year tag ties
to `ItemLangCatalog`'s already-confirmed real `item.customisation.collection.triumph26`="Triumphs.
Faction Race 2026" cosmetic-collection display name (part 121, previously only mentioned in that
class's own Javadoc as part of an 8-line seasonal-collection crosswalk, never cross-referenced
against this namespace before now). Both real strings independently name the same real "Triumphs" /
"Faction Race 2026" seasonal event from two entirely separate real namespaces -- corroborating each
other, not duplicating the same source line.

**Testing.** 4 new `TriumphCatalogTest` tests, fixture built from all 4 real key=value lines
verbatim (no privacy redaction needed -- no player-submitted or account-identifying text in this
namespace): exact size/key-count confirmation, real embedded-level reward-type resolution
(including a check that a real absent level-20 correctly resolves to null, not a guess), the real
single-instance mail title/text fields (including the real `{count}` placeholder preserved
verbatim, not stripped or interpreted), and `field()` resolving both a known real key (cross-checked
against the typed accessor for the same key) and an unknown key (null).

**Live-verification, per this project's standing discipline.** Re-confirmed the real `triumph.*`
content directly against the full, real `en.lang` file before writing any code -- all 4 real lines
byte-exact, zero drift from the count first captured in part 168's closing-note investigation.

**Build.** Compiled clean with `--release 11` in the cloud (1084 total tests, 0 failures), synced to
the device via the standard tarball/`SendUserFile`/`device_commit_files`/extract pipeline, and
re-run live on the device's actual Java 11 JRE with an identical clean result: 1080 -> 1084.

**Doc-only resolutions.** None this part.

**Closing note.** This closes out all three open leads part 168's closing note originally flagged
(`ops.leagues.*`, `frontlineline.*`, `triumph.2026.*` -- now built as parts 169, 170, and 171
respectively). The fresh full-file re-scan methodology (parts 167-171) has productively surfaced
five genuinely new namespaces in five consecutive parts (`spring26/challenges/*`,
`stalkerguide.*`/`stalkerguide2019.*`, `frontlineline.*`, `ops.leagues.*`, `triumph.2026.*`).

No further open leads remain queued from this particular investigation thread as of this part.
Future work could either continue the fresh-re-scan methodology from scratch against the current
full `en.lang` file (a new pass may surface further small namespaces missed by the specific
prefix-census approach used in parts 167-171, per that approach's own documented false-negative
caveat for dynamically-built prefixes), or shift toward wiring already-built catalogs into engine
behavior wherever a confirmed crosswalk exists -- several such attempts were disclosed as genuinely
blocked (not forced with an invented resolution rule) in parts 165-167, for `EjectionCatalog`,
`ItemLoreCatalog`, and `QuestContractCatalog`, each for lack of a confirmed id-space crosswalk
between the en.lang-side id space and the relevant `GameLoop`/engine-side id space.
