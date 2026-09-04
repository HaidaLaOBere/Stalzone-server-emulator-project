#!/usr/bin/env python3
"""
Generic reader for STALZONE's local save-data format, discovered 2026-08-30:
  zstd-framed container, wrapping a big-endian, Minecraft-style NBT
  (Named Binary Tag) structure.

Usage:
  python3 nbt_dump.py <file>            # auto zstd-decompress (if needed) + pretty-print as JSON-ish text
  python3 nbt_dump.py <file> --raw-out out.bin   # also write the decompressed raw bytes

Confirmed against STALZONE's config/vendor_archive.dat and config/<account>/sdN files.
Standard NBT tag IDs: 0 End,1 Byte,2 Short,3 Int,4 Long,5 Float,6 Double,
7 ByteArray,8 String,9 List,10 Compound,11 IntArray,12 LongArray.
"""
import sys
import struct
import subprocess
import tempfile
import os

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

TAG_NAMES = {
    0: "End", 1: "Byte", 2: "Short", 3: "Int", 4: "Long", 5: "Float",
    6: "Double", 7: "ByteArray", 8: "String", 9: "List", 10: "Compound",
    11: "IntArray", 12: "LongArray",
}


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.pos = 0

    def remaining(self):
        return len(self.data) - self.pos

    def take(self, n):
        if self.pos + n > len(self.data):
            raise EOFError(
                f"tried to read {n} bytes at offset {self.pos}, "
                f"only {len(self.data) - self.pos} remain"
            )
        b = self.data[self.pos:self.pos + n]
        self.pos += n
        return b

    def u8(self):
        return self.take(1)[0]

    def i8(self):
        v = self.u8()
        return v - 256 if v >= 128 else v

    def u16(self):
        return struct.unpack(">H", self.take(2))[0]

    def i16(self):
        return struct.unpack(">h", self.take(2))[0]

    def i32(self):
        return struct.unpack(">i", self.take(4))[0]

    def u32(self):
        return struct.unpack(">I", self.take(4))[0]

    def i64(self):
        return struct.unpack(">q", self.take(8))[0]

    def f32(self):
        return struct.unpack(">f", self.take(4))[0]

    def f64(self):
        return struct.unpack(">d", self.take(8))[0]

    def nbt_string(self):
        n = self.u16()
        raw = self.take(n)
        try:
            return raw.decode("utf-8")
        except UnicodeDecodeError:
            return raw.decode("utf-8", errors="replace")


def read_payload(r: Reader, tag_type: int):
    if tag_type == TAG_BYTE:
        return r.i8()
    if tag_type == TAG_SHORT:
        return r.i16()
    if tag_type == TAG_INT:
        return r.i32()
    if tag_type == TAG_LONG:
        return r.i64()
    if tag_type == TAG_FLOAT:
        return r.f32()
    if tag_type == TAG_DOUBLE:
        return r.f64()
    if tag_type == TAG_BYTE_ARRAY:
        n = r.i32()
        return [r.i8() for _ in range(n)]
    if tag_type == TAG_STRING:
        return r.nbt_string()
    if tag_type == TAG_LIST:
        elem_type = r.u8()
        n = r.i32()
        return {
            "__list_elem_type__": TAG_NAMES.get(elem_type, elem_type),
            "items": [read_payload(r, elem_type) for _ in range(n)],
        }
    if tag_type == TAG_COMPOUND:
        out = {}
        while True:
            t = r.u8()
            if t == TAG_END:
                break
            name = r.nbt_string()
            out[name] = read_payload(r, t)
        return out
    if tag_type == TAG_INT_ARRAY:
        n = r.i32()
        return [r.i32() for _ in range(n)]
    if tag_type == TAG_LONG_ARRAY:
        n = r.i32()
        return [r.i64() for _ in range(n)]
    raise ValueError(f"unknown tag type {tag_type} at offset {r.pos - 1}")


def read_root(data: bytes):
    r = Reader(data)
    t = r.u8()
    name = r.nbt_string()
    value = read_payload(r, t)
    return name, value, r


def maybe_zstd_decompress(path: str) -> bytes:
    with open(path, "rb") as f:
        head = f.read(4)
    if head == b"\x28\xb5\x2f\xfd":
        out = subprocess.run(
            ["unzstd", "-f", "-c", path], capture_output=True, check=True
        )
        return out.stdout
    if head[:2] == b"\x1f\x8b":
        import gzip
        with open(path, "rb") as f:
            return gzip.decompress(f.read())
    with open(path, "rb") as f:
        return f.read()


def to_jsonable(v):
    if isinstance(v, dict):
        if "__list_elem_type__" in v:
            return [to_jsonable(x) for x in v["items"]]
        return {k: to_jsonable(x) for k, x in v.items()}
    if isinstance(v, list):
        return v
    if isinstance(v, bytes):
        return v.hex()
    return v


def pretty(v, indent=0):
    pad = "  " * indent
    if isinstance(v, dict):
        if not v:
            print(pad + "{}")
            return
        for k, val in v.items():
            if isinstance(val, (dict, list)) and val:
                print(f"{pad}{k}:")
                pretty(val, indent + 1)
            else:
                print(f"{pad}{k}: {val!r}")
    elif isinstance(v, list):
        if not v:
            print(pad + "[]")
            return
        for i, item in enumerate(v):
            if isinstance(item, (dict, list)) and item:
                print(f"{pad}[{i}]:")
                pretty(item, indent + 1)
            else:
                print(f"{pad}[{i}]: {item!r}")
    else:
        print(f"{pad}{v!r}")


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    path = sys.argv[1]
    data = maybe_zstd_decompress(path)
    if "--raw-out" in sys.argv:
        outp = sys.argv[sys.argv.index("--raw-out") + 1]
        with open(outp, "wb") as f:
            f.write(data)
    try:
        name, value, r = read_root(data)
    except Exception as e:
        print(f"PARSE ERROR: {e}")
        sys.exit(2)
    print(f"# root name: {name!r}")
    print(f"# decompressed size: {len(data)} bytes; parsed through offset {r.pos}; "
          f"{r.remaining()} bytes left over (0 = clean parse)")
    pretty(to_jsonable(value))


if __name__ == "__main__":
    main()
