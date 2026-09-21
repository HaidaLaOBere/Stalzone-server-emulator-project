# Part 160 -- `AccountBadgeCatalog`

## Real shape

Three real tiny namespaces (15 lines total), the forty-first through forty-third concrete
namespaces from part 123's follow-up census: profile_icon.* (10, flat), menulinks.help.* (3,
flat), review.notf.* (2, generic table).

## Real finds

10 profile badges incl. a real oddity: chzzk/soop (distinct Korean streaming platforms) share
"TikTok Blogger" text with tiktok itself. admin/helper both = "Developer". 3 footer help links.
A 2-field review prompt.

## Testing

3 new `AccountBadgeCatalogTest` tests. Run clean on both a plain and a `--release 11` build in
the cloud and, synced across, on the device's actual Java 11 JRE: 1044 -> 1047.
