# Part 150 -- `SnowstormCatalog`

## Real shape

The real `snowstorm.notf.<phase>_<n>` namespace (18 lines, all sharing one id `notf`), the
thirtieth concrete namespace built from part 123's follow-up census -- real winter-storm announcer
flavor text. Modeled with the established generic last-dot-split (id, field) table.

## A richer winter-storm pool alongside EjectionCatalog

Part 141's `EjectionCatalog` `ejection.winter.*` (4 short lines) already confirmed a real winter
storm event. This standalone `snowstorm.*` namespace is a much larger, richer real text pool for
the same general event: 10 `start_<n>` variants + 8 `end_<n>` variants, each in a distinct
radio-operator narrator voice. Disclosed as a further, richer pool, not merged (no shared id/key
structure ties the two together).

## Real profanity, disclosed not censored

One real line (`end_4`) contains real unfiltered profanity as authored by the client -- disclosed
as a fact, kept out of the test fixture for cleanliness, but readable verbatim in the live
`en.lang` source.

## Testing

3 new `SnowstormCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1014 -> 1017.

## Context for future parts

Thirty namespaces down -- a milestone part. Next reasonable picks by size from part 123's own
writeup (`Memory/recon_findings_cont79.md`): `industries`(18), `challenge`(17, singular, distinct
from plural `challenges`), `arena`(17), `inside`(16), `blizzard`(16, likely thematically paired
with this part's `snowstorm` per part 123's own note), `outpost`(14, check for overlap with
existing `HomeOutpostCatalog`), `ny`(34)/`ny_toys`(13), `season`(12, singular),
`personal_storage`(11).
