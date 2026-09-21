# Part 158 -- `SeasonCatalog`

## Real shape

The real SINGULAR `season.<year>/<seasonName>.tabTitle`/`.title` namespace (12 lines), the
thirty-ninth concrete namespace built from part 123's follow-up census. 6 ids x 2 fields = 12.
Named distinct from PLURAL `SeasonsCatalog` (part 145) and `shop.season.*`, confirmed disjoint by
content.

## Real finds

6 real quarterly thematic season titles: 25/spring="Back to the Inside Out", 25/summer="MURMUR OF
THE ZONE", 25/autumn="HORRORS OF THE ZONE", 26/winter="SUB-ZERO ZONE", 26/spring="TEMPERED
STEEL", 26/summer="ANTICYCLONE". Real ids embed a literal `/`. Every id's tabTitle/title are
byte-identical, disclosed as observed duplication.

## Real corroborations

25/spring="Back to the Inside Out" dates part 139/154's ranked event to Spring 2025. 26/winter=
"SUB-ZERO ZONE" exactly matches part 121's ItemLangCatalog winter26 collection crosswalk.

## Testing

3 new `SeasonCatalogTest` tests against a fixture built from real key=value lines. Run clean on
both a plain and a `--release 11` build in the cloud and, synced across, on the device's actual
Java 11 JRE: 1038 -> 1041.
