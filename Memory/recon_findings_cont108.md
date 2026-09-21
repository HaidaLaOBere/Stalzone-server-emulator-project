# Part 152 -- `ChallengeCatalog`

## Real shape

The real SINGULAR `challenge.*` namespace (17 lines), the thirty-second concrete namespace built
from part 123's follow-up census. Named distinct from the already-covered PLURAL `challenges.*`
(part 116). Reconciled exactly: 11 (`wild_north/<n>.name`) + 6 (`episode.wild_north/<n>.name`) =
17.

## Real finds

`wild_north/<n>` (n=1-11): 7 anti-Murmur objectives + 4 mutant-hunting objectives
(Skitters/Ghoul/Piggies/Boars Hunting) -- Boars corroborates StalkerMobsCatalog's boar mutant.
`episode.wild_north/<n>` (n=1,3,4,5,6,7): real story episodes with a genuine gap at episode 2,
disclosed as-is. Ids embed a literal `/`, handled without special-casing (same pattern as
bpseason/party_missions).

## Testing

3 new `ChallengeCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1020 -> 1023.

## Context for future parts

Thirty-two namespaces down. Next reasonable picks by size from part 123's own writeup
(`Memory/recon_findings_cont79.md`): `arena`(17), `inside`(16), `blizzard`(16, likely
thematically paired with part 150's `snowstorm`), `outpost`(14, check for overlap with existing
`HomeOutpostCatalog`), `ny`(34)/`ny_toys`(13), `season`(12, singular), `personal_storage`(11).
