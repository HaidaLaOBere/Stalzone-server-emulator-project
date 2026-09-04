#!/usr/bin/env python3
"""
Builds an internal numeric item-id -> display name lookup from
modassets/assets/stalker/itemnames.dat (gzip+NBT, see nbt_dump.py).

The file's "key" field for most catalog items looks like
"internal.<categoryId>.<itemId>.<variant>" (e.g. "internal.16012.10251.0").
<itemId> (the middle number) matches the numeric "id" field seen in
save-data records like config/vendor_archive.dat (see docs/RESEARCH_LOG.md).
Not all records are "internal.*" keyed (some use plain string keys like
"crash.pda_scientist") - those are skipped for this numeric lookup.

Usage:
  python3 item_lookup.py <itemnames.dat> [id ...]
    no ids given -> prints how many internal.* records were indexed
    ids given    -> prints name for each, or "NOT FOUND"
"""
import re
import sys

sys.path.insert(0, __file__.rsplit("/", 1)[0])
import nbt_dump as nbt

STRIP_CODES = re.compile(r"§.")


def clean(s):
    return STRIP_CODES.sub("", s) if isinstance(s, str) else s


def build_index(path):
    data = nbt.maybe_zstd_decompress(path)
    _, value, _ = nbt.read_root(data)
    records = nbt.to_jsonable(value).get("Data", [])
    by_id = {}
    for rec in records:
        key = rec.get("key")
        if not isinstance(key, str) or not key.startswith("internal."):
            continue
        parts = key.split(".")
        if len(parts) < 3:
            continue
        try:
            item_id = int(parts[2])
        except ValueError:
            continue
        name = clean(rec.get("display_name", ""))
        variant = parts[3] if len(parts) > 3 else "0"
        by_id.setdefault(item_id, []).append((variant, name, key))
    return by_id


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    path = sys.argv[1]
    ids = [int(x) for x in sys.argv[2:]]
    idx = build_index(path)
    if not ids:
        print(f"Indexed {len(idx)} distinct internal.* item ids "
              f"({sum(len(v) for v in idx.values())} records total).")
        return
    for i in ids:
        hits = idx.get(i)
        if not hits:
            print(f"{i}: NOT FOUND")
            continue
        for variant, name, key in hits:
            print(f"{i} (variant {variant}, key={key}): {name!r}")


if __name__ == "__main__":
    main()
