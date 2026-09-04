# tools/

## nbt_dump.py
Reads STALZONE's local save-data format: a zstd-framed container wrapping a
big-endian, Minecraft-style NBT (Named Binary Tag) structure. Confirmed
2026-08-30 against `config/vendor_archive.dat` and `config/<account>/sd1-sd3`
— all four parse byte-exact (zero leftover bytes) with this reader.

Usage:
    python3 nbt_dump.py path/to/file            # pretty-print
    python3 nbt_dump.py path/to/file --raw-out out.bin   # also dump decompressed raw bytes

Requires the `zstd`/`unzstd` CLI on PATH (already present on this machine) and Python 3, no extra packages.

Does NOT yet handle: `config/prefs` or `modassets/assets/containers.l` (neither
starts with the zstd magic bytes and both look like a different/encrypted
format — not cracked yet).

Next planned use: point this at captured network packet payloads once a
capture is available (see ../docs/CAPTURE_GUIDE.md) — the wire format is
hypothesized to be the same zstd+NBT scheme, unconfirmed until tested.
