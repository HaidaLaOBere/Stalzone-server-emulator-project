# Recon findings -- cont126 (Part 170)

## `world.props.OpsLeagueCatalog`

Continuing the fresh full-file `en.lang` re-scan, this part builds the second of part 168's
remaining two open leads: a real, previously-uncatalogued 10-line namespace,
`ops.leagues.<id>.<field>` -- a real 5-tier ranked-ladder system. Confirmed via `grep -c
"^ops\."` against the full real file: exactly 10 lines, no other real `ops.*` content exists
anywhere in the client.

**Real content.** Tier names by real numeric id: `1`="Absolute Unity", `2`="Ascendant",
`3`="Revenant", `4`="Acolyte", `5`="Adept". Each real id also has a real `reward_text` flavor
passage repeating the tier name, written entirely in a cultic/hive-mind register ("Unity", "the
truth", "you will become one of all", "the understanding of this blessing will follow quickly").

**4th recurrence of the "leagues" naming convention.** This project has now found the real
"leagues" ranked-ladder convention in `DmzLangCatalog` (part 135), `InsideCatalog` (part 154),
`NewYearEventCatalog` (part 157, which explicitly called itself the third recurrence), and now
`ops.leagues.*` (this part) -- confirmed by the shared literal `leagues` path segment across all
four, not asserted from naming similarity alone. This particular recurrence's tone (cultic/
hive-mind) is notably distinct from the other three.

**Tier ordering deliberately NOT asserted -- a rule-1-compliant disclosure, not a gap filled with
a guess.** The real numeric ids alone do not disclose which direction is "up." Reading the five
real `reward_text` passages in numeric order 5, 4, 3, 2, 1 traces a plausible initiate-to-ultimate
narrative arc:
- id 5 ("Adept"): "you do not yet understand it... Awareness will come soon" -- reads as a bare
  joining/initiate step.
- id 4 ("Acolyte"): "We welcome you... into our ranks... You will gain wisdom" -- reads as gaining
  standing.
- id 3 ("Revenant"): "You died... and returned... changed. Rise as the strongest" -- reads as a
  transformative rebirth.
- id 2 ("Ascendant"): "becoming a part... of a great algorithm... closer to the truth" -- reads as
  approaching the whole.
- id 1 ("Absolute Unity"): "You|We|All are absolutely one!" -- reads as final union.

This would make numeric id 5 the LOWEST/initiate tier and id 1 the HIGHEST/final tier -- the
reverse of a naive "1 is first/lowest" reading. Nothing in the real client text confirms whether a
player's numeric id increases or decreases with rank, so this reading is disclosed in the class
Javadoc as plausible but unproven, and the class itself exposes only the literal real `(id,
field)` data -- deliberately no `rank()`/`tierOrder()` method that would assert either direction
as fact. This is a direct application of the standing rule 1 (never turn an assumption into a
protocol/data specification).

**Modeling.** Standard generic last-dot-split `(id, field)` table, stripping the literal
`ops.leagues.` prefix. Ids `"1"`-`"5"`; fields `name`/`reward_text` (the only two real fields
observed). `ids()` returns them in real file order (which happens to be ascending numeric order,
since that's how the real file lists them -- not a sort this class imposes).

**Testing.** 5 new `OpsLeagueCatalogTest` tests, fixture built from all 10 real key=value lines
verbatim (no privacy redaction needed -- no player-submitted or account-identifying text in this
namespace): real id enumeration, real tier-name resolution by id, real reward_text content checks
(the real id-5 initiate-toned line and the real id-1 union-toned line), real field-name
enumeration (confirms `name`/`reward_text` are the only two real fields), and unknown id/field
resolving to null rather than a guess.

**Live-verification, per this project's standing discipline.** Re-confirmed the real `ops.*`
content directly against the full, real `en.lang` file both before writing any code (as part of
this part's own investigation) and implicitly reconfirmed again through the shipped test fixture,
which was built directly from that same verified capture -- all 10 real lines byte-exact, zero
drift from the count first captured in part 168's closing-note investigation.

**Build.** Compiled clean with `--release 11` in the cloud (1080 total tests, 0 failures), synced
to the device via the standard tarball/`SendUserFile`/`device_commit_files`/extract pipeline, and
re-run live on the device's actual Java 11 JRE with an identical clean result: 1075 -> 1080.

**Doc-only resolutions.** None this part.

**Closing note / open leads.** One lead from part 168's original three-item list remains unbuilt:
`triumph.2026.*` -- 4 real lines of reward-mail text (`reward_type.reward_level_10`=`STATUE`,
`reward_type.reward_level_30`=`CUSTOMIZATION`, and two `mail.message.reward.*` title/text fields),
tying to part 121's `ItemLangCatalog` `item.customisation.collection.triumph26`="Triumphs. Faction
Race 2026" (a confirmed but not-yet-written-up crosswalk). Real content already fully captured and
confirmed via `grep -c "^triumph\."` = 4 (exact match). Not yet decided whether to fold this into
another class or give it its own tiny dedicated class, consistent with this project's precedent of
dedicating classes even to very small namespaces. Ready to build in the next part following the
identical per-part pipeline used for parts 166-170.

Beyond that specific lead, future work could also continue wiring already-built catalogs into
engine behavior wherever a confirmed crosswalk exists (several such wiring attempts were declined
in earlier parts for lack of a confirmed id-space crosswalk -- see parts 165-167's `EjectionCatalog`,
`ItemLoreCatalog`, and `QuestContractCatalog` wiring-decline notes), or continuing the fresh
full-file re-scan methodology itself, which has now productively surfaced four genuinely new
namespaces across parts 167-170 (`spring26/challenges/*`, `stalkerguide.*`/`stalkerguide2019.*`,
`frontlineline.*`, `ops.leagues.*`).
