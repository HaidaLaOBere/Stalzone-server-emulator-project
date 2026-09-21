# Recon findings -- cont125 (Part 169)

## `world.props.FrontlineBriefingCatalog`

Continuing the fresh full-file `en.lang` re-scan (parts 167/168), this part builds the second of
three open leads flagged in part 168's closing note: a real, previously-uncatalogued 12-line
namespace, `frontlineline.intro.<group>.<stage>.<paragraph>` -- real multi-paragraph war-campaign
narrative briefing text.

**Confirmed genuinely distinct from the already-covered `frontline.*`** (192 lines, part 129,
`FrontlineCatalog`), not a substring collision or duplicate:
- Prefix safety: the real character immediately after `frontline` in every one of these 12 real
  keys is `l` (from the doubled "line"), never `.` -- a literal `"frontline."` prefix match cannot
  accidentally pull one in. Confirmed by direct count.
- Content: three real named characters (Colonel Kondratenko, Major Makarov, Zaharov) appear in
  this namespace's text but do NOT appear anywhere in `FrontlineCatalog`'s own already-covered
  briefing text. Confirmed by direct search, not assumed from the names merely looking similar.

Both namespaces do share the same real SSPP-vs-Murmur war-campaign setting -- this is new content
in an established fictional conflict, not an unrelated one.

**Modeling.** Standard generic last-dot-split `(id, field)` table, stripping the literal
`frontlineline.intro.` prefix. Real ids: `second.stage1`, `second.stage2`, `final.stage1`,
`final.stage2`. Real fields: `par0`, `par1`, `par2` (no id has any other field).

**Real content, confirmed by direct count against the full file: 2 groups x 2 stages x 3
paragraphs = 12, zero leftover lines.** Narrative summary:
- `second.stage1`: allied SSPP forces push Murmur back past the "Railway Embankment"; Colonel
  Kondratenko's units, aided by Zaharov's strike groups, capture and hold the "Radar Station
  bunker"; Murmur switches to a deeply echeloned defense.
- `second.stage2`: a "psy-suppressor" is lost; engineers work with the "ZIVCAS Scientific Research
  Institute" to restore the defensive system while new operations are planned. (`ZIVCAS` further
  corroborates part 92's already-confirmed real "ZIVCAS Scientific Research Institute" -- just
  another entry in that long chain, per the standing rule against asserting a numbered ordinal for
  this particular recurring corroboration.)
- `final.stage1`: Major Makarov reports Murmur's main forces destroyed, but the military town,
  forward command post, and gravitational beacon are lost; Colonel Kondratenko and Zaharov's fate
  is unknown.
- `final.stage2`: the affected space "warped, folded into a sphere, and simply... disappeared";
  the SSPP formally thanks the player and withdraws, closing this campaign arc.

**Part-numbering note.** This class was drafted mid-session under a working "Part 170" Javadoc
label, on the assumption that an `OpsLeagueCatalog` would be built first as "Part 169." Since no
such class was ever actually created, and this is in fact the very next sequential class after
part 168, the label was corrected to "Part 169" before any test file, wiring, compilation, sync, or
documentation work proceeded -- keeping the part number consistent across the class Javadoc, the
README bullet, and the RESEARCH_LOG heading, per this project's unbroken numbering discipline.

**Testing.** 6 new `FrontlineBriefingCatalogTest` tests, fixture built from all 12 real key=value
lines verbatim (no privacy redaction needed -- no player-submitted or account-identifying text in
this namespace): real id enumeration (sorted), real paragraph ordering via `paragraphs(id)`, the
real ZIVCAS cross-namespace corroboration via direct field access, the three real named characters
in the final-arc text, and unknown id/field resolving to null/empty rather than a guess.

**Live-verification, per this project's standing discipline (the Part 167/168 lesson: never trust
a first pass or arithmetic alone).** Before writing any code, re-confirmed the exact real content
of all three of part 168's flagged leads directly against the live, full `en.lang` file:
`ops.leagues.*` (10 lines), `triumph.2026.*` (4 lines), and `frontlineline.*` (12 lines) -- all
counts matched prior captures exactly, with zero drift. After the class and fixture were written,
re-verified the real `frontlineline.*` content again directly against the full file before shipping
any documentation; all 12 real lines confirmed byte-exact.

**Build.** Compiled clean with `--release 11` in the cloud (1075 total tests, 0 failures), synced
to the device via the standard tarball/`SendUserFile`/`device_commit_files`/extract pipeline, and
re-run live on the device's actual Java 11 JRE with an identical clean result: 1069 -> 1075.

**Doc-only resolutions.** The part-numbering correction described above; no other Javadoc changes
were needed after the live-verification pass (unlike parts 167 and 168, no factual error was found
this time).

**Closing note / open leads.** Two of part 168's three flagged leads remain unbuilt going into the
next part:
- `ops.leagues.*` -- a real 5-tier ranked ladder (`Adept` -> `Acolyte` -> `Revenant` -> `Ascendant`
  -> `Absolute Unity`), a 4th recurrence of the "leagues" naming convention (after
  `DmzLangCatalog`/`InsideCatalog`/`NewYearEventCatalog`), with a distinctly cultic/hive-mind tonal
  register unlike the other three. A plausible-but-unconfirmed reading (to be explicitly hedged,
  not asserted) is that the numeric order is INVERTED from the other ladders -- id 5 ("Adept")
  reading as the lowest/initiate tier and id 1 ("Absolute Unity") as the highest/final tier, based
  on each `reward_text`'s narrative content. Real content already fully captured (10 lines,
  confirmed exact match via `grep -c`), ready to model. No class name chosen definitively;
  `OpsLeagueCatalog` was under consideration and confirmed via grep not to collide with any
  existing class.
- `triumph.2026.*` -- real reward-mail text (4 lines: `reward_type.reward_level_10`=`STATUE`,
  `reward_type.reward_level_30`=`CUSTOMIZATION`, and two `mail.message.reward.*` title/text
  fields), tying to part 121's `ItemLangCatalog` `item.customisation.collection.triumph26`="Triumphs.
  Faction Race 2026" (a confirmed but not-yet-written-up crosswalk). Not yet decided whether to
  fold this into another class or give it its own tiny dedicated class, consistent with this
  project's precedent of dedicating classes even to very small namespaces.

Both are captured and ready to build in the next part, following the identical per-part pipeline
used for parts 166-169.
