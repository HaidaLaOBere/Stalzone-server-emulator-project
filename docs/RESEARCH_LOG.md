# STALZONE Server Emulator — Research Log

Ground rule: every entry below is labeled CONFIRMED (with source) or HYPOTHESIS (needs verification). Never treat a HYPOTHESIS as implemented fact.

## Target build
- Steam AppID 1818450, main depot 1818452 (manifest 1873341655299177973, 2026-07-29), support depot 1818454 (manifest 7528983772013232428, 2026-07-29).
- Local copy: "STALZONE FULL CLIENT 30.8.2026 (STEAM)" folder (installed client) + "app_1818450/.../depot_1818452" and "depot_1818454" (raw depot download).

## Client tech stack — CONFIRMED (from bin_global/classes.jar and bin_global/client_class_index.txt, 2026-08-30)
- JVM-based client. Entry point class name: `exbo.stalcraft.engine.clientcore.Main`.
- Bundles Netty (`io.netty.*`, 1401 classes) — near-certain the game network layer is Netty-based (TCP, possibly with a custom framing/codec).
- Bundles Auth0 JWT library (`com.auth0.jwt.*`, EdDSA algorithm support) — sessions are plausibly JWT-based.
- Bundles Protobuf (`com.google.protobuf.*`) — packet payloads are plausibly Protobuf-encoded. NOT yet confirmed against actual wire bytes.
- Also bundled: Jackson (JSON), Google Guice (DI), Ebean (ORM enhancer artifacts only, not necessarily used at runtime), JGraphX (`com.mxgraph.*`, likely a dev/debug tool, not gameplay), Discord RPC (`com.jagrosh.discordipc`).
- EXENS anti-cheat integration: `exbo/stalcraft/dev/exens/obf` and `exbo/stalcraft/dev/exens/service` packages exist in classes.jar. Installer at `%PROGRAMFILES%\Exens\ExensInstaller.exe`; driver/service manifest served from `sc-exens.cdn77.exbo.net` and a Selectel object-storage URL with a crypto signature.
- Domains seen in `sclauncher.log`: `sc-exens.cdn77.exbo.net`, `stalcraft.net`, `stalzone.com`.

## classes.jar packing format — CONFIRMED (2026-08-30)
- classes.jar has 59,840 zip entries but only ~12 conventional `exbo/...` `.class` files (all `package-info.class` stubs) and 13 other loose `.class` files total.
- 59,781 entries are named as bare 32-hex-char strings (e.g. `c9e44e35e9c6606a7281fefc85451ae0`) with **no `.class` extension and no Java class-file magic bytes (`CAFEBABE`)** — verified by scanning every such entry's first 4 bytes in Python. These are NOT raw class files.
- The jar bundles the zstd compression library's C source (`zstd/compress/*.c`, `zstd/decompress/*.c`, `zstd/legacy/*.c`) plus a 1.5MB `strings.dat` and per-module `tinylog.properties` (`backend/`, `frontend/`, `client/`).
- HYPOTHESIS: the hash-named entries are zstd-compressed, possibly encrypted, blobs — each likely bundling one or more real class files — produced by EXBO's own build/obfuscation pipeline (see the `exens/obf` package name), decoded at runtime by a custom classloader. This is presumably done both to shrink patch deltas between builds (content-addressed storage) and to raise the bar against static decompilation.
- Consequence: **pointing a decompiler (CFR/vineflower/etc.) straight at classes.jar will not work.** We have not yet found the name→hash mapping or the unpacking routine.
- `bin_global/client_class_index.txt` (96,582 lines) is a broader index that mixes fully-qualified class names (`exbo.stalcraft.engine.clientcore.Main`, third-party library classes) with what look like bare obfuscated numeric identifiers (e.g. `exbo.stalcraft.engine.clientcore.1971414666`, and standalone lines like `1000098238`). Exact semantics of this file are NOT yet understood — needs more analysis (is it a class-name table, an asset ID table, or both interleaved?).

## Planned unpacking approaches — HYPOTHESIS / NOT YET ATTEMPTED
1. **Live class-dump via Java agent (recommended primary approach).** Attach a `-javaagent` (or dynamic Attach API) jar implementing `ClassFileTransformer` to the real client process. By the time `transform()` is called, the JVM holds the plain, decrypted bytecode regardless of how it was packed on disk — this sidesteps reverse engineering the custom packer entirely. Standard, well-established technique.
   - **Risk: EXENS anti-cheat may detect Java instrumentation/agents and could flag or ban the account if used while connected to the official live servers.** Should only be attempted (a) fully offline/no network, if the client will even launch that way, or (b) accepting the risk knowingly if done online. Needs a decision before building/using this.
2. Reverse the custom unpacker itself (find where/how the hash blobs are decoded) — harder, but zero anti-cheat exposure since it's pure static analysis.
3. Ask community RE resources (Tier 1 links) whether this packing format is already documented/solved for STALCRAFT/STALZONE — worth checking before reinventing this.

## Known/likely API endpoints — HYPOTHESIS (source: community doc `Art3mLapa/unofficial-stalzone-api`, written for STALCRAFT; NOT yet re-verified against this July 29 2026 STALZONE build)
- `http://launcher.stalcraft.net/auth` — login (password hash, 2FA, HWID auto-login)
- `http://launcher.stalcraft.net/distributor` — Steam auth integration
- `http://launcher.stalcraft.net/listServers` — server list/status
- `http://launcher.stalcraft.net/torrentData` — file distribution manifest
- `https://backend.stalcraftx.ru/address_list` — game server tunnel IPs by region
- `https://backend.stalcraftx.ru/characters/list`, `/characters/create` — character data
- `https://backend.stalcraftx.ru/session/game` — game session token
- CDN: `exbo.b-cdn.net`, `m-cdn.exbo.net`; trackers: `tracker1/2.stalcraft.net`

## Not yet examined
- `config/*/vendor_archive.dat`, `quests.cfg`, `waypoints.cfg`, `options.json` — local save-state files, format unknown.
- `modassets/assets/` — unclear what this is used for.
- The multi-GB `logs/client-N.log` files — only skimmed for URLs so far.
- Raw `depot_1818452`/`depot_1818454` download — not yet diffed against the installed client folder.

## Live session findings — 2026-08-30, read directly from the user's live client logs while playing
(Source: `D:\SteamLibrary\steamapps\common\STALZONE\logs\client.log`, read passively, no capture tooling needed for this batch. Access to this live install folder was granted this session.)

### CONFIRMED — transport & connection routing
- The client does NOT connect straight to a fixed game server IP. At connect time (`RegionPingThread`) it RTT-pings a whole set of named "tunnel pools" per world region (seen: FRA, KYI, MSK2X, DAL, NYC, LAX, MIA, CHI, ASH, ATL, SEA, TOR, SAN, LIM, SGP, SYD, TYO, KOR — this looks like a global relay/tunnel mesh, not literal "regions"), each containing one or more named entrypoints (e.g. `GAME-EU-1`, `GAME-EU-4`, `GAME-EU-7`, `SYD-ROXY1`, `KYI-ROXY1/2`, `MSK2-ROXY11`, `KOR-2/3/4`), measures `clientToTunnelRtt` and `tunnelToBackendRtt` for each, and picks the best weighted one. Log evidence: `Selected tunnel entrypoint: RoxyTunnelEndpoint(name=GAME-EU-1, address=/89.222.108.193:29450)`.
- Actual game-socket connection observed this session: **`89.222.108.193:29450`** (tunnel entrypoint `GAME-EU-1`, pool FRA). This is a real, currently-live server address, captured directly from the log, not a guess.
- ~~Transport is Netty NIO TCP~~ **CORRECTED in part 7 below — real packet capture proves this is UDP, not TCP.** The `NioEventLoop` thread name in the log does NOT imply TCP; Netty's `NioEventLoop` is protocol-agnostic and drives both `NioSocketChannel` (TCP) and `NioDatagramChannel` (UDP). The log line `Connecting with transport NetworkTransport{/89.222.108.193:29450}` only proves a connection was made, not which L4 protocol — that required an actual capture to settle. See "Live session findings, part 7" for the evidence and correction.
- Session state machine (exact strings logged): `null → BACKEND_LOGIN → GAME_SERVER_LOGIN → GAME_SERVER_CONNECTED`, and on a "backend reconnect notification" it cycles `GAME_SERVER_CONNECTED → BACKEND_LOGIN → GAME_SERVER_LOGIN → GAME_SERVER_CONNECTED` again.
- **Implication for the emulator**: to get the real client to connect to *our* server, we likely need to intercept/replace whatever API response feeds this tunnel-entrypoint list (probably the backend's server-list/address-list call), not just redirect a single hardcoded IP — unless we find a simpler override (e.g. a hosts-file-level redirect of the backend/tunnel hostnames once we know them, or a client config override). Not yet investigated.

### CONFIRMED — packet dispatch identifies packet types by (obfuscated) class, not a small opcode
- Real error seen live: `Can not process packet from server (packet_id: class 1775361307): java.lang.RuntimeException: Exception while handling inventory packet for windowId=0, local view windowId=0`.
- This "packet_id: class <number>" format matches the obfuscated numeric class-name pattern already seen in `client_class_index.txt` (e.g. `exbo.stalcraft.engine.clientcore.1971414666`) — strong confirmation that those numeric identifiers really are packet/message class names, and that this specific one (`1775361307`) is an inventory-window packet. This gives us a concrete, named target to look for once classes.jar can be unpacked/dumped (see the packing-format section above).
- This error is a **live bug in the official client itself** right now (windowId=0 inventory packet failing to parse) — not something we caused; just useful because it leaks internal naming.

### Noise identified and ruled out (don't mistake these for the core game protocol)
- `Failed to parse RPC packet` (repeating every ~15s on `Thread-19`) is very likely the bundled Discord Rich Presence IPC (`com.jagrosh.discordipc`, confirmed present in classes.jar) failing, not the game protocol.
- The verbose `[VIVOX/info]` XML blocks are the Vivox third-party voice chat SDK (SIP-based, its own `AccessToken` JWT-like tokens) — a separate subsystem from core gameplay, low priority for the emulator's early milestones.
- `Exception spawning game object (chunkX: ..., chunkZ: ...)` NullPointerException spam is a live client-side bug triggered around a reconnect event, not evidence of protocol structure — though it does confirm the world uses a `chunkX`/`chunkZ` chunked-coordinate system.

### HYPOTHESIS (raised, not confirmed) — engine may be Minecraft-derived
- World coordinates use `chunkX`/`chunkZ` naming; asset/resource log lines reference files like `customitems:models/blocks/tubes/tube_long.mcsb` / `.mcmtl` and a `customitems:` resource namespace. The `mc`-prefixed extensions, chunk terminology, and namespaced resource IDs closely resemble a modified Minecraft-derived engine. This matches community lore that STALCRAFT is built on a heavily modified Minecraft base. Not proven via decompiled code yet — treat as a working hypothesis that should shape where we look (e.g. Minecraft protocol/server literature may transfer conceptually — framing, chunk packets, entity metadata — even though the actual wire format here is almost certainly custom/proprietary, not vanilla Minecraft protocol).

## Live session findings, part 2 — 2026-08-30, local save-data format cracked
(Source: the user's live install, `D:\SteamLibrary\steamapps\common\STALZONE\config\`. All read-only, no game/process interaction.)

### CONFIRMED — local persisted data format: zstd-framed, then a compact NBT-like binary tag format
- `config/vendor_archive.dat`, and per-account `config/<name>/sd1`, `sd2`, `sd3` all start with bytes `28 B5 2F FD` — the **zstd frame magic number**. `/usr/bin/zstd`/`unzstd` (present on the user's machine) decompresses them cleanly (verified on `vendor_archive.dat`: 14,616 → 106,616 bytes; `sd2`: 17,902 → readable content) — **not encrypted**, just zstd-compressed.
- Decompressed content is a compact binary key/value tag format (short-string keys prefixed by a length byte, followed by a type byte, e.g. `0a 00 05 "stack"`-style patterns) — not JSON/Protobuf. Field names read directly as plaintext strings via `strings` on the decompressed bytes.
- **`vendor_archive.dat` schema (the player's marketplace/vendor listings)**: `archives[] { username, traders[] { traderId, buyer_lodochnaya, items[] { stack, id, Count, Damage, tag { upgrade_bonus }, currency { money, price } } } }`.
  - `id` / `Count` / `Damage` / `tag` as an item's core fields is **exactly Minecraft's classic ItemStack NBT schema** — this is now strong, near-conclusive evidence (not just circumstantial naming) that the engine's data model descends from Minecraft's, on top of the earlier chunkX/chunkZ + `.mcsb`/`.mcmtl` asset-naming clues.
- **`config/<account>/sd2` schema (local notification/inbox cache)**: `notifications[] { notification { channel_id, time, expire_time, payload { msg {type,key}, title {type,key}, link, headerColor, icon_id, sound }, isRead, receivedMoment } }`. Localization keys follow a dotted convention, e.g. `stalker.gui.seasonpass.notification.task.daily.completed_repeat`, `challenges.season.summer26/pg_challenges/g1/6.name` — confirms a season-pass/challenges system exists (out of scope for the early milestones, but good to know it's there).
- `config/cluster` is plaintext: `EU` — the account's selected region cluster (consistent with the FRA/EU tunnel pool seen connecting live).
- `config/quests.cfg` and `waypoints.cfg` are plain JSON (`{"readQuests":[...ids...],"hiddenQuests":[...]}`, `[]`) — trivial to model later.
- `config/options.json` is plain Jackson-style JSON — the full local client settings (FOV, sensitivity, keybind-adjacent toggles, etc.) — not protocol-relevant but useful reference for a future settings/GUI layer.
- `config/prefs` does **not** start with the zstd magic and its bytes look like genuine high entropy from byte 0 — likely a different (possibly actually encrypted) format; not investigated further.
- `modassets/assets/containers.l` (336 bytes) also has no zstd magic and is high-entropy from byte 0 — not decoded, not a priority.

### Working hypothesis raised to "likely" — network packets probably use this same binary tag format
Since this custom zstd+binary-tag scheme is used for *every* piece of local persisted game-object data we've checked (vendor listings, notifications), it is the game's general-purpose serialization scheme, not a one-off file format. It is now the leading candidate for how packet payloads are encoded on the wire too — more likely than raw Protobuf (protobuf is still bundled and could be used for something, e.g. analytics, but hasn't shown up in any of the object schemas found so far). **Still unconfirmed for network traffic specifically** — only a real packet capture (per CAPTURE_GUIDE.md) or a class dump can confirm this; keep labeling it a hypothesis until then.

### Asset/content taxonomy (from `modassets/assets/` top level)
Confirms a namespaced content system: `_jurisdiction`, `_localized`, `anomalies`, `auction`, `bundle`, `chat`, `currency`, `customitems`, `customnpcs`, `effects`, `ejection`, `gameobjects`, `gloomycore`, `handcuffs`, `highpoly`, `misc`, `party`, `pda`, `probuilder`, `ragdolls`, `shop`, `skinarmor`, `stalcraft`, `stalker`, `stalkerclans`, `stalkerguide`, `stalkermobs`, `stalkerplayer`, `stalkersounds`, `trade`, `vegetation`, `weapons`. This is a useful map of game subsystems to eventually implement server-side (economy/trade/auction, factions/clans, party system, anomalies, PDA/map, containers, capture mechanics like handcuffs).

## Live session findings, part 3 — 2026-08-30, working save-data parser built and validated
A generic reader (`tools/nbt_dump.py`) was written and confirmed to **byte-exact parse** (0 leftover bytes) all four local save files checked: `vendor_archive.dat`, and `config/<account>/sd1`, `sd2`, `sd3`. This means the format is confidently confirmed as: zstd container → standard big-endian NBT structure (tag types 0-12, exactly per classic Minecraft NBT: End/Byte/Short/Int/Long/Float/Double/ByteArray/String/List/Compound/IntArray/LongArray). This is now a reusable tool, not just a one-off decode.

### Schema correction — `vendor_archive.dat`
The earlier flat guess was wrong; the real (parser-confirmed) structure is:
```
archives: [ { username, traders: [ { traderId, items: [ { stack: { id, Count_i, Damage, tag: { upgrade_bonus, u1, u2 } }, currency, price } ] } ] } ]
```
- `stack` is a **nested compound** (the actual Minecraft-style ItemStack: `id`, `Count_i` — note the literal field name really is `Count_i`, not `Count`, confirmed byte-exact, possibly an internal/obfuscated rename — `Damage`, `tag`).
- `tag.u1` / `tag.u2` are two 64-bit signed longs with high-entropy values — near-certainly an item instance UUID split into most/least-significant halves (a very common Minecraft-modding pattern, usually named `UUIDMost`/`UUIDLeast`, here shortened to `u1`/`u2`).
- `currency` is a **plain string** (e.g. `"money"`), not a nested object as first guessed; `price` is a sibling Int, not nested under currency.
- `id` values seen (numeric item IDs, not yet cross-referenced to names): 11103, 11153, 11157, 10251, 10263, 11155, 31001, ... — cross-referencing these against `EXBO-Studio/stalzone-database` (Tier 1 resource) is a good next step whenever item data matters.

### New schemas discovered
- **`config/<account>/sd1` — PvP interaction/kill log**: `interacts: [ { type: 'DIED_FROM_PLAYER' | 'PLAYER_KILLED', username, allianceId } ]`. Confirms a faction/alliance system (`allianceId` seen: `"stalkers"`).
- **`config/<account>/sd3` — observed store offers**: `observedOffers: [ "<bundle/cosmetic name>", ... ]` — flat list of shop bundle/cosmetic names (localized, seen in Russian in this account's client language, e.g. `"Сезонный набор №1"`), some prefixed `customization/`. Confirms UTF-8 (including Cyrillic) is handled correctly by the format/parser.
- **`config/<account>/sd2` — notification/battle-pass inbox** (previously found): schema as documented in part 2, now byte-confirmed via the parser instead of `strings`-guessed.

### Still not cracked
`config/prefs` and `modassets/assets/containers.l` — neither starts with the zstd magic (`28 B5 2F FD`); likely a different or genuinely encrypted format. Not attempted further.

### Why this matters for the network protocol
Getting 4/4 real files to parse byte-exact with zero slack is strong validation that "zstd + this exact NBT tag-ID scheme" is STALZONE's one general-purpose object serialization format, not a coincidence. It raises confidence (still not proof) that captured network packet payloads (see CAPTURE_GUIDE.md) will use the same inner NBT structure, likely with a different/no outer zstd framing (or a lighter per-message framing instead of a full zstd frame) given packets need to be small and low-latency. `tools/nbt_dump.py` is now ready to be pointed at real packet payload bytes once a capture exists, and would only need adjusting for the outer framing, not the tag format itself.

## Live session findings, part 4 — 2026-08-30, more accounts + asset tree survey
### More save files parsed with tools/nbt_dump.py (all clean, 0 leftover bytes)
- Confirms **two different playable factions** across the three local accounts: `AveghiliaBoy` → `allianceId: "stalkers"`; `WolfZero` → `allianceId: "freedom"` (classic STALKER-lore faction names).
- **Quest notification schema** (from `WolfZero/sd2`, `Buji/sd2`): `payload: { QuestId, type: 'new'|'quest_failed', QuestTitle, quest_type }` (`quest_type` is a small int enum, values 0 and 2 seen).
- **`common` file (per account) — misc UI/meta state**, new schema: `snowmanFeed{...}` (seasonal event UI state), `caseLastOpenCount.caseLastOpenCount[]{caseId, openAmount}` (confirms a loot-case/crate-opening system with per-case open counts), `arcadesMenuOpened`, `complaintsData{lastChatReport, reportedMailIds[], reportedProfiles[], reportedHacks[], reportedCharnames[]}` (an in-client player-reporting/moderation system), plus flags referencing unreleased-to-us features: `frontline...`, `triumph...`, `prestige...` (separate game modes/systems, out of scope for now).
- **`sd4` (per account) — cosmetics-seen tracker**: `lastSeenTagsVersion/BackgroundsVersion/PatternsVersion/StickersVersion` (ints) + `observedElements[]` (flat list of small signed ints, likely paired id/category values — not decoded further).
- Item tier system found as plain text (`modassets/assets/gloomycore/item_tiers.txt`): 4 tiers, in order — `novice, stalker, veteran, master`.

### Map/zone inventory (from `modassets/assets/pda/`)
The PDA (map) asset folder lists these actual playable zones/maps by name — a concrete candidate list for picking milestone 1's "single static zone":
`map` (base/open world?), `map-arena_raven`, `map-dungeon_bolota_tutorial`, `map-kolizei`, `map-nbolota`, `map-new_bar`, `map-prestige`, `map-reichstag`, `map-sovetpunk`, `map-sovhoz_overlay`, plus a `map_overlay` folder. Names reference classic STALKER locations (Bar, Sovkhoz/"sovhoz", Bolota=swamp) alongside what look like PvP arenas (`kolizei`=colosseum, `arena_raven`) and a tutorial dungeon.
Each map's PDA image data is stored as region-tile files named `r.<X>.<Y>.ol` (e.g. `map-new_bar/r.-3.-10.ol`) — confirms a region/chunk-grid coordinate system for the world map, consistent with the earlier `chunkX`/`chunkZ` log evidence. `onejeuu/sc-mapmerge` (Tier 1 resource) is specifically built to merge these region tiles into one image — worth using once we need to visualize a target map.

### `gloomycore/` explained
This folder name had looked like it might be a game mechanic; it's actually just cross-cutting engine/meta assets: localization text (`RU_ru.txt`), loading-screen tips (`advices_en.txt`/`advices_ru.txt`), a full material-file index (`mcmtlList.txt`, 682KB), a `handbook/` (item/lore reference, only an `img/` subfolder inspected so far), and two tiny reference files: `item_tiers.txt` (above) and `egs_dlc_ids.txt` (a single Epic Games Store DLC GUID — irrelevant to Steam builds).

### Backend tunnel-list endpoint — still not found by name
Grepped the full live `client.log` for `address_list`, `listServers`, `backend.stalcraft`, `torrentData`, `distributor` (the community-documented endpoint names) — **zero literal matches**. Either this July 29 2026 build doesn't log the URL it calls for the tunnel-entrypoint list, uses a different endpoint name than the community docs describe, or resolves it some other way (e.g. baked into a config/manifest rather than a live API call). Still open — would need an HTTPS capture (CAPTURE_GUIDE.md, Part A) to settle.

## Live session findings, part 5 — 2026-08-30, item-name database found and cross-referenced
- `modassets/assets/stalker/itemnames.dat` is **gzip**-compressed (magic `1F 8B`, not zstd) but wraps the exact same NBT tag structure as everything else — parses byte-exact (0 leftover) with `tools/nbt_dump.py` after extending it to also auto-detect gzip. This is now a third confirmed use of the same universal NBT scheme (local save data, and now static game content data), and further confirms the Minecraft lineage: item Lore/display strings use `§`-prefixed Minecraft-style chat formatting codes (e.g. `§r`) verbatim.
- Structure: `Data: [ { key, display_name, Lore: [...], creator } , ... ]` — 1288 records, ~1148 with a distinct numeric id.
- Most (not all) records have `key` in the form `internal.<categoryId>.<itemId>.<variant>`, e.g. `internal.16012.10251.0`. **The middle number matches the numeric `id` field seen in save-data item records** (`vendor_archive.dat` etc.) — confirmed directly: looking up our previously-decoded vendor-archive item ids against this file resolved real names:
  - `10251` → *"Зелёный контейнер"* ("Green Container") — matches the trader `buyer_lodochnaya` dealing in containers.
  - `11153` → *"После сигнала3"*, `11155` → *"После сигнала1"*, `11157` → *"После сигнала3"* (repeat) — quest/lore item chain ("After the Signal").
  - `11103`, `10263`, `31001` were **not found** in this file — itemnames.dat evidently only covers items with special/unique lore text (quest items, notes, PDAs, unique containers), not the full catalog. Standard catalog items (weapons, ammo, meds — likely including these three) are probably named via the structured `item.<category>.<name>.name` localization-key convention documented in the public API (`https://eapi.stalzone.net/items.html`), which lives elsewhere (not yet located locally).
- New tool: **`tools/item_lookup.py`** — builds the id→name index from `itemnames.dat` and looks up specific numeric ids from the command line. Reuses `nbt_dump.py` as a library.
- Correction/clarification: the **public** `EXBO-Studio/stalzone-database` / `eapi.stalzone.net` item IDs are short opaque alphanumeric strings (e.g. `"y1q9"`), **not** the plain numeric ids used internally in save data / `itemnames.dat`. These are two different ID systems — don't conflate them. The public API's schema (per its docs) is: `{id, category, name:{type,key,args,lines:{ru,en}}, color, status, infoBlocks}` — useful as a *reference model* for how item metadata is organized (translation keys, category paths like `weapon/submachine_gun`, color/rank tags like `RANK_STALKER`), even though its ID space differs from what we're decoding locally.
- Also confirmed launch classpath (from the community docs): `stalcraft.exe -classpath "classes.jar;libs.jar;modassets"` — note `modassets` itself is on the Java classpath, not just a data folder.

## Live session findings, part 6 — 2026-08-30, stalcraftdb.net checked (user-suggested resource)
Checked https://stalcraftdb.net/eu (a community item database site, "by kaas93"). It's a JS SPA; used the browser tool to load it and inspect its network calls directly rather than guessing.

- **Backing API found**: `GET https://stalcraftdb.net/api/listing/eu/grouped` returns a big JSON grouped by category (`other`, and presumably one key per left-nav category: Armor/*, Artefact/*, Attachment/*, Backpacks, Bullet, Containers, Device, Grenade, Medicine, Miscellaneous, Weapon/*, Weapon_modules/*, etc. — see the full category list in the page text). Each entry: `{id, category, dataUrl, iconUrl, name}`.
- **Per-item detail**: `dataUrl` points at `https://raw.githubusercontent.com/EXBO-Studio/stalcraft-database/main/global/items/<category>/<id>.json` (this IS the `EXBO-Studio/stalzone-database`/`stalcraft-database` repo from the Tier 1 resource list, just accessed via this site's convenience API/UI). Confirmed full schema on a real item (`other/qyvk.json`): `{id, category, name, color, status:{state}, infoBlocks:[...]}`. `infoBlocks` looks like where stats (damage/weight/etc.) would live — not fully inspected yet.
- **Confirms (again) there is no numeric ID anywhere in this public dataset** — only the opaque 4-char id (e.g. `qyvk`, `1jog`). Tried a targeted search for our already-identified item "Зелёный контейнер" (internal id `10251`) by name across the `other` category — no match (it's presumably filed under the `Containers` category specifically, not checked yet, and there is no reliable ID-based crosswalk regardless — name-matching would be a heuristic at best, not authoritative).
- **Conclusion**: this is a genuinely useful resource for Phase 3 (real item stats, multi-language names, icons, category taxonomy) once we're building actual item content, sourced from `EXBO-Studio/stalcraft-database` on GitHub (same data, nicer to browse via stalcraftdb.net). It does **not** solve the internal-numeric-id-to-name gap for items missing from `itemnames.dat` — that still needs either finding the structured localization source client-side, or accepting a manual/fuzzy name-matching pass later when it actually matters.

## Live session findings, part 7 — 2026-08-30, first real packet capture analyzed (correction: transport is UDP)
(Source: `Caputre 1.pcapng`, a Wireshark capture the user made with the game running, saved to the project root. 67,646,032 bytes, 317,412 total packets, captured **12:24:30.918498–12:47:43.976716 UTC** (≈23 min 13 s). It is a **whole-system, unfiltered** capture — everything on the NIC during that window, not just the game — so it also contains Steam, Discord, ChatGPT, Windows telemetry, NordVPN and WPS Office traffic. Filtered to the confirmed live game-server IP (`89.222.108.193`, entrypoint `GAME-EU-1`, see part above) with `tcpdump -r "Caputre 1.pcapng" -n host 89.222.108.193`: **242,693 packets**, 100% to/from that host on UDP port 29450.

### CORRECTION — transport is UDP, not TCP
The earlier "CONFIRMED — Netty NIO TCP" claim above was wrong and is struck through. Evidence for the correction: `tcpdump -r "Caputre 1.pcapng" -n host 89.222.108.193 | grep -v UDP` returns **zero lines** — every one of the 242,693 packets to/from the game server is UDP. There is no TCP handshake (no SYN/SYN-ACK/ACK) to this host anywhere in the capture. Root cause of the earlier mistake: I inferred TCP purely from the log thread name `NioEventLoop-0`, but Netty reuses `NioEventLoop` for both `NioSocketChannel` (TCP) and `NioDatagramChannel` (UDP) — the thread name alone never distinguished them. This is exactly the kind of "assumption presented as fact" the project's ground rules exist to catch; flagging it here rather than quietly editing it away.

### CONFIRMED — payload appears encrypted / high entropy from byte 0
Hex-dumped a representative sample of UDP payloads (`tcpdump -X`) at steady state. No discernible plaintext header, no repeating magic bytes, no ASCII, and no structure that lines up with the zstd (`28 B5 2F FD`) or gzip (`1F 8B`) magics used by local save data — bytes look uniformly high-entropy starting at offset 0. Working conclusion: wire payloads are encrypted (or at minimum compressed with an unrecognized/custom scheme) and are **not** simply "NBT inside zstd like local saves" — that hypothesis from earlier findings does not carry over to the wire protocol without more evidence. Making sense of payload contents will most likely require recovering the client's actual crypto/serialization code (i.e. the deferred classes.jar class-dump-agent approach), not passive capture analysis alone.

### CONFIRMED — steady-state traffic shape
Baseline (whole 23-minute filtered capture): ~174 packets/sec average, packet lengths cluster heavily at a handful of small sizes — `45` bytes (89,507 occurrences), `54` (28,554), `57` (17,820), `56` (9,953) dominate, consistent with a high-frequency tick of small state-delta/heartbeat/ack packets, plus a long tail of larger variable-size packets (up to 1200 bytes) for bulkier updates.

### CONFIRMED — "backend reconnect notification" has a distinctive, reproducible traffic signature
`client.log` recorded 8 "Received backend reconnect notification" events inside the capture window (converting the log's local timestamps, UTC+3, to the capture's UTC clock): `12:25:33, 12:26:53, 12:27:30, 12:32:48, 12:33:23, 12:41:55, 12:42:12, 12:46:45`, plus two unrelated Vivox "Network interface changed" events at `12:34:01`/`12:34:23`. Built a per-second packet-count/byte-count histogram of the filtered capture and inspected a ±5s window around every timestamp. All 8 backend-reconnect events show the **same three-phase shape**, distinct from the ~150–190 pkt/s / ~15KB/s steady state:
1. **Taper** (~1s): traffic count drops (e.g. 161→57 pkts).
2. **Stall** (~1–2s): traffic drops to near-zero (0–10 pkts/s) — the socket goes effectively silent.
3. **Burst** (~1s): a spike to 400–500 packets and 300,000–420,000 bytes in that single second (roughly 20–25× the normal per-second byte rate), then an immediate return to steady state.
   - Example (12:25:35 burst, immediately after the 12:25:33 event): 427 packets, direction split 390 server→client / 37 client→server (~91% inbound) — i.e. the burst is overwhelmingly the *server* pushing data. Size histogram of that second: 310 of the 427 packets are exactly **1200 bytes** (near max UDP payload before fragmentation) — a strong contrast with the steady-state distribution (dominated by 45–57 byte packets). A second example (12:42:14 burst) matches: 432 packets, 354 inbound / 78 outbound.
   - The two **Vivox** "Network interface changed" events (12:34:01, 12:34:23) show **no corresponding deviation** in the game-server traffic at all — packet counts stay flat at the steady-state baseline through those seconds. This is useful negative evidence: it confirms those two log lines are a separate subsystem (Vivox voice, its own socket) reacting to something else (a NIC/network-interface change reported by the OS), unrelated to the main game UDP channel, exactly as expected from their log text.
- **Interpretation (labeled as inference, not confirmed)**: the stall-then-burst-of-1200-byte-packets-from-server pattern is consistent with a full state resynchronization after reconnect — the server appears to push a large batch of near-MTU-sized chunks after the client re-establishes `GAME_SERVER_CONNECTED`, rather than a lightweight token/keepalive refresh. This is a reasonable read of the traffic shape, not a decoded fact — we have not decrypted payloads to confirm what's actually inside those 1200-byte packets, and we have not yet correlated to the log with sub-second precision (client.log reconnect timestamps are second-granularity, so the "before/after" boundary is approximate, e.g. ±1s).

### Capture hygiene note (for future captures)
This capture was whole-system and unfiltered — 317,412 total packets but only 242,693 (76%) were the game. For a cleaner capture next time: either set a Wireshark **capture filter** (`udp and host 89.222.108.193`, once the entrypoint IP is known from `client.log`) before starting, or after the fact use *File → Export Specified Packets → Displayed* with a display filter applied, rather than saving the raw whole-system capture. This also incidentally picked up Vivox SIP signaling containing base64-encoded usernames of other nearby players in voice chat — a reminder that whole-system captures can catch more than intended and should be handled/stored accordingly.

### Tooling used for this analysis
No Wireshark GUI/tshark available in this environment — analysis done with `tcpdump -r <file> -n host <ip>` (reading the pcapng file directly) plus small Python scripts (stdlib only: `re`, `collections.Counter`/`defaultdict`) to bucket packets by second and compare against baseline. No new checked-in tool yet; if pcap analysis becomes routine, worth adding a `tools/pcap_stats.py` later.

### Small passive-monitoring note — 2026-08-30, ~13:10 UTC
Spotted in `client.log` (benign WARN, not an error): `Failed to find subtitles file: customnpcs:music/steven/sounds/npc/bandits/meet_hide_weapon/meet_hide_weapon_2.srt`. Two small new data points, not yet followed up: (1) a `customnpcs` asset namespace exists (distinct from `modassets/assets/stalker`, `gloomycore` etc. already surveyed) holding NPC voice-line audio + subtitle files; (2) it references a `bandits` NPC faction (separate from the two playable factions `stalkers`/`freedom` already confirmed) with named interaction events, e.g. `meet_hide_weapon` — a player-hides-weapon NPC greeting/reaction line. Noted here as a lead for later NPC/AI-behavior modeling, not investigated further this session.

## Live session findings, part 8 — 2026-08-30, second capture confirms reconnect-burst signature is reproducible
(Source: `Caputre 2.pcapng`, 56,548,928 bytes, 266,212 total packets, window **13:03:34–13:26:07 UTC** (~22.5 min) — a separate, independent capture from part 7's, taken after troubleshooting the Wireshark filter in-chat. Still whole-system: 227,158/266,212 (85%) of packets are to/from the game server — better than capture 1's 76%, but the capture filter still isn't fully isolating game traffic; worth double-checking it's set on the *capture* start screen, not just the display filter, next time.)

### CONFIRMED (now reproduced on independent data) — the reconnect-burst signature is real, not a one-off
Cross-referenced this capture's 22.5-minute window against `client.log` for the same event types: found 6 more "Received backend reconnect notification" events (13:10:51, 13:12:53, 13:16:27, 13:20:45, 13:23:57, 13:24:24 UTC) and 2 more Vivox "Network interface changed" events (13:03:33, 13:03:52 UTC) — all converted from the log's local (UTC+3) timestamps. Ran the same per-second packet/byte histogram used in part 7. Result: **all 6 reconnect events reproduce the same taper → near-total stall (~1-2s) → large burst → resume-steady-state shape** first seen in capture 1, on a totally independent recording. Combined across both captures, this pattern has now held for **14 out of 14** backend-reconnect events and shown up in **0 out of 4** unrelated Vivox network-change events — a clean, reproducible, evidence-backed correlation per the project's evidence bar, even though what's inside the burst packets is still unread (encrypted).
- New observation: burst *magnitude* varies noticeably more in this capture than in capture 1's fairly uniform ~300-420KB bursts — here they range from ~350KB up to **1.1MB in a single second** (13:12:57, 1131 packets) and 648KB (13:16:30, 714 packets). Labeled as inference, not confirmed: this is consistent with burst size scaling with how much state actually needs resyncing (e.g. more nearby entities/players at that moment), but we have no direct evidence for that explanation yet — it's equally possible different reconnects trigger different resync scopes for other reasons.

## Phase 1 started, 2026-08-30 — server-emulator scaffolding built and tested
Set up `server-emulator/` (plain Java, no build tool needed — see `server-emulator/README.md` for why Gradle/Netty aren't used yet: this sandbox's egress proxy blocks Maven Central, `repo1.maven.org`, and `maven.google.com`, all confirmed HTTP 403, so a dependency-based build can't resolve anything here). Everything below was actually compiled and run, not just written:
- **NBT reader + new writer** ported from `tools/nbt_dump.py` to Java (`server-emulator/src/.../nbt/`). Tested against a live-staged real file (`itemnames.dat`): decompressed to 577,074 bytes, parsed with **0 leftover bytes**, and a write-then-reread round trip reproduced the exact same 577,074 bytes structurally identical to the original parse. This is the first NBT writer we have (the Python tool was read-only).
- **Config system** (`config/ServerConfig.java`) generates/loads `config/server.properties` with the spawn-rate/damage/loot-rate multiplier fields the project exists for. Explicitly documented as NOT wired to any gameplay logic yet, since there is no gameplay logic — just the config plumbing, ready for later phases.
- **Bare UDP listener** (`network/UdpListener.java` + `PacketLogger.java`), plain `java.nio` (no Netty, see above). Smoke-tested live: started it, sent it a real UDP datagram from a separate process, confirmed it logged the correct source address, length, and hex bytes.
- **Milestone-1 map picked**: `map-dungeon_bolota_tutorial` (the client's own new-player tutorial zone — reasoned to be the smallest/simplest real zone, since tutorials are built to teach movement with minimal systems), fallback `map-new_bar`. Documented in `world/MilestoneMap.java`; no actual map-loading exists yet (region-tile/mesh formats still unparsed).
- Delivered as a zip via chat and unpacked directly into `server-emulator/` in this folder (the cloud sandbox that built it has no direct write access to this connected folder's Java toolchain, so it was built+tested in the cloud, then the validated source was copied over — this connected-folder copy is now the live one to keep editing).

**Still fully blocked, unchanged from part 7**: no login/session/gameplay implementation is possible until the encrypted UDP wire payloads can actually be read, which needs either the class-dump-agent approach (real ban risk, requires explicit go-ahead) or some other source of the client's crypto/serialization code.

## Live session findings, part 9 — 2026-08-30, classes.jar packing investigated further (static analysis only, zero game-process risk)
Went back to the still-open "how does classes.jar's packing actually work" question, purely via static file analysis on disk — no game process was run or touched, no anti-cheat exposure at all.

### CONFIRMED — the real launch chain, from sclauncher.log
`sclauncher.log` (the EXENS launcher's own log, which turns out to be a Rust binary using the `winit` windowing crate, going by its Debug-formatted structs) records the exact command line used every time the game has actually launched successfully:
```
D:\...\STALZONE\bin_global\win64\java\bin\ExensLauncher.exe bin_global/win64/java/bin/stalzonew.exe -Dcountry.code=RO
  -classpath bin_global/classes.jar;bin_global/libs.jar;modassets;bin_global/patchassets
  -XX:-PrintCommandLineFlags -Xmx3G -Djava.library.path=bin_global/win64/java/bin -Dfile.encoding=UTF-8
  -Dread_derived=true -DusePrestitchedAtlas=true -XX:+UseG1GC -XX:MaxGCPauseMillis=3
  -Dgame_distributor=STEAM -Dpatch_assets_dir=bin_global/patchassets
  exbo.stalcraft.engine.clientcore.Main --gameDir . --assetsDir ./assets
```
So: `ExensLauncher.exe` (anti-cheat wrapper) launches `stalzonew.exe` (NOT a stock `java.exe` — a custom-named binary living exactly where `java.exe` would, taking the exact same JVM flags: `-classpath`, `-Xmx`, `-XX:...`, `-D...`, then the Main-Class name, then app args). Almost certainly a relinked/renamed JVM launcher rather than a from-scratch native app, but not confirmed either way — haven't inspected the binary itself.

### CONFIRMED — the obfuscation is total, not partial, and not a simple name hash
Re-examined `classes.jar` (59,840 entries) and `libs.jar` (20,945 entries) with Python's `zipfile`:
- `classes.jar`: **0** normally-named `.class` files. `exbo/stalcraft/engine/clientcore/Main.class` is **not present as a literal entry name** — confirmed by direct lookup. 59,781 entries are bare 32-hex-character names (MD5-length hash strings), the rest (59) are non-class resources: `strings.dat` (1.5MB, likely localization strings — not yet inspected), a few `tinylog.properties` config files, `assets/stalkermobs/relations` (see below), and embedded zstd-jni C source files (license/reference noise, not game code).
- `libs.jar`: also mostly hash blobs (19,507 of them), plus 793 *normally*-named `.class` files — but **zero** of those normal ones are under the `exbo.*` namespace. They're third-party dependency classes (Google protobuf's `.proto` schema files are bundled too, ~600 of them, confirming heavy protobuf use — plus unexpectedly, NVIDIA ACE (`nvidia_ace.*.proto` — an AI NPC voice/animation system) proto schemas, a new finding not previously noted). So even libs.jar's actual STALZONE-authored code is just as hash-obfuscated as classes.jar; only genuine open-source dependencies keep readable names.
- **Tested and disproved** the natural hypothesis that a blob's filename is a simple hash of the class's name: computed MD5, SHA-1 (32-char truncated), and SHA-256 (32-char truncated) of `exbo/stalcraft/engine/clientcore/Main` and `exbo.stalcraft.engine.clientcore.Main` (both slash- and dot-form, both with and without a trailing `.class`, both UTF-8 and ASCII encoded) — **none of the 6 resulting hashes appear anywhere in classes.jar's entry list.** Whatever produces the blob filenames, it is not a plain hash of the class's textual name in any of these obvious forms.
- Blob *content* (not just filenames) also doesn't look like plain class bytes: sampled first bytes of several blobs — no CAFEBABE magic (expected at offset 0 of a real .class file), no shared header across a 3,000-blob sample of first-2-bytes (all distinct, i.e. uniformly high entropy from byte 0), and sizes vary naturally (64–66,960 bytes in a 5,000-blob sample, median 752 — consistent with per-class-sized content, not some fixed block format). Reads as encrypted per-entry, not just renamed.
- `client_class_index.txt` (96,582 lines) does contain the literal line `exbo.stalcraft.engine.clientcore.Main` (found at line 68335, alongside three sibling numeric-suffixed names in the same package: `.1238051822`, `.1682645177`, `.1971414666` — these numeric ones match the "packet_id: class <number>" pattern already confirmed as packet/message classes). But it's a **flat list of names/IDs, one per line** — not a name↔hash mapping we can read directly. If it IS the index used to resolve names to blob hashes, the mapping isn't stored as plaintext pairs in this file; more likely this file is just a name dictionary and the actual name→hash resolution happens computationally (a keyed hash, a lookup embedded in code, or something else) at runtime inside the client itself.

### New unrelated finding — mob faction relations, plain text, immediately usable
`classes.jar:assets/stalkermobs/relations` is **uncompressed plain text**, no obfuscation at all: a full ALLY/ENEMY/NEUTRAL relation matrix between mob types. Confirms mob roster: `boar, krovosos (bloodsucker), flesh, dog, pseudodog, psidog, psidog_clone, cat, chimera, pseudogigant, zombie, burer, controller, poltergeist, snork, fracture, bird, rat, tushkan` — classic STALKER mutant bestiary. Directly usable later for the spawn-rate/AI-relation config system without needing anything else cracked first.

### Where this leaves classes.jar
Still unread. The packing/obfuscation scheme is confirmed to be more than cosmetic renaming (content itself looks encrypted, not just relocated), and the simplest name-hashing theories are now ruled out with evidence rather than just assumed to be wrong. This was pure static analysis — zero live game process involved, zero anti-cheat exposure — so more of this kind of investigation (inspecting `stalzonew.exe`/`ExensLauncher.exe` statically, `strings.dat`, deeper blob-content analysis) remains available as a ban-risk-free avenue before falling back to the class-dump-agent option.

## Live session findings, part 10 — 2026-08-30, pivoted to game content per user request (maps/gunplay/NPCs/activities/cut content, NOT EXENS/launcher internals)
User explicitly redirected research priorities away from the EXENS launcher/anti-cheat investigation (part 9) toward actual game content. Re-checked `strings.dat` first (the one lead left over from part 9): it's also high-entropy/encrypted from byte 0, same as everything else in classes.jar — no plaintext localization strings to mine there, dead end confirmed with evidence (not just assumed).

Pivoted to `modassets/assets/` instead, which is a **plain, unencrypted filesystem tree** (not inside the obfuscated jars) — the real, sustainable source for this kind of content, not classes.jar.

### CONFIRMED — mob/NPC AI configs are plain readable JSON
`modassets/assets/stalkermobs/configs/*.smm` (35 files) are uncompressed, unencrypted **plain JSON** — read `base_boar.smm` in full as a sample. Schema per mob: `_sounds` (named sound-event keys, e.g. `pain`, `death`, `attack`, `threaten`, `eat`), `_animations` (name/duration-in-frames/animationSpeed per state), `_attacks` (named attack variants with `timeOfAttack`, `rotationLimit`, `movementType`, impulse vector, `effector`), `mutantRelations` (this mob's ALLY/ENEMY/NEUTRAL view of every other mob type — same relation system as the flat `assets/stalkermobs/relations` file found in part 9, but per-mob and structured), plus behavior flags (`canWalk`, `canRest`, `canSuddenTurn`, `canRunAttack`, `bodyEater`, idle-sound timing, etc.) and `mobName`.
**Full confirmed mob roster (35, from filenames)**: boar(+clone), chimera(+clone), cybergiant, dog(+clone), doge, drone, flesh(+clone), fpv, krovosos(+clone), lurker, pseudodog(+clone), pseudogigant(+clone), psidog(+clone), puppet(+clone), rudolf, snork(+clone), spirit, thief_rat, tick, tsarevich, tsarevna, tumbler, tushkan(+clone), wisp. Several of these are new to our research (not in the earlier flat relations file, which only had the "common" subset) and read as clearly special/seasonal/event content, not standard zone mobs: **`rudolf`** (a reindeer — Christmas/New Year event mob), **`tsarevich`/`tsarevna`** (Russian for "prince"/"princess" — a fairy-tale-themed boss encounter), **`cybergiant`** (a sci-fi-themed giant, likely an endgame/event boss), **`fpv`** (an FPV drone as a mob/hazard type, not just player equipment), **`drone`**, **`puppet`**, **`spirit`**, **`wisp`**, **`lurker`**, **`thief_rat`** (an item-stealing rat variant), **`tumbler`**, **`tick`** — none of these were previously documented. This is real, evidence-based "cut/seasonal content" per the user's ask, not speculation: the files exist on disk with full AI/animation/relation configs.

### CONFIRMED — full weapon and melee-weapon roster (folder names = real item catalog)
`modassets/assets/weapons/models/weapons/` contains **256 top-level entries** (mesh+texture folders, no readable stat files alongside them — ballistics/damage numbers are NOT here, likely still locked in the encrypted item database). Overwhelming majority are real-world firearm names (huge realistic roster: ak12, akm, aug_a3, m4, hk416, svd, mosinka, deagle, glock, mp5, saiga12, rpg7, dshk, gauss + gauss_pistol, flamethrower + flammenwerfer, etc. — full list is long, see the folder directly). Notable non-generic entries worth flagging: `prestige/` (not a gun — a subfolder of prestige-tier utility items: flasks, mines, a scanner, a battery, a barricade, matching the "prestige" game-mode flag already found in save data and the `map-prestige` map name — a whole prestige-tier progression system exists), `test/` (checked contents — this is NOT a leftover dev/cut weapon, it's dictaphone/tape-recorder/scanner props, likely quest/detector-related, correcting an initial guess before verifying), `npc` (npc-only weapon variants folder), `ability_items`, `walkietalkie`, `musical_instruments`, `xpainter`. `modassets/assets/weapons/models/meleeweapons/` has its own separate roster (~90 entries) with clear seasonal/special items: `nye2021` (New Year's Eve 2021 event weapon), `zseva`/`zseva2018`/`zseva2020` (a recurring named weapon across multiple years — likely an anniversary item), `twitch` (a Twitch-drop weapon), `endgame`, `iceaxe_prestige`, `misery` (possibly a nod to the well-known "Misery" S.T.A.L.K.E.R. mod), `cryomancer`. Also noticed in passing: `weapons/models/weapons/ak12/rail_winter26_diff.ol` — a Winter 2026 seasonal texture variant, confirming at least one live seasonal cosmetic pass this year.

### Still open for maps/gunplay specifically
Actual weapon stats (damage, fire rate, recoil, ammo) are not in `modassets` in any readable file found so far — likely still inside the encrypted classes.jar item/weapon database, same blocker as before. Map geometry (`pda/` region tiles, `.mcsb`/`.mcmtl` meshes) still unparsed — format not yet reverse engineered, `onejeuu/sc-mapmerge` (Tier 1 community resource) is meant for exactly this and hasn't been tried yet.

## Scope note, 2026-08-30 (from the user)
User clarified: not interested in EXENS (anti-cheat/launcher) itself as an investigation target — the goal is the game and its protocol/content, plus possible cut/unreleased content. EXENS facts already confirmed (part 9's launch chain) stay recorded since they explained how classes.jar/libs.jar are actually loaded, but no further EXENS-specific digging (its internals, detection mechanisms, etc.) unless it's directly in service of understanding the game itself. Noted candidates for a future cut-content pass: the `frontline`/`triumph`/`prestige` flags seen in save-data `common` files (part 4) and `map-prestige` in the map list — none investigated yet.

## Live session findings, part 11 — 2026-08-30, more cut-content hunting: mostly found LIVE content instead (honest result)
Continued the cut-content pass started in part 10 (which found the mob roster and weapon folder catalog) by following up on the `frontline`/`triumph`/`prestige` flags noted in part 4/9, plus a couple of options.json settings, to see if any of it was cut/unreleased content. Short answer: **no clear evidence of anything cut was found** — every one of these turned out to be real, currently-loading game systems, just ones this particular account hasn't opened the how-to-play/intro screen for. Reporting this plainly since claiming "cut content" without evidence would violate the project's own ground rule — the honest result here is "these are bigger live systems than we'd catalogued, not missing ones."

- **Prestige**: real and substantial. `client.log` shows dozens of `customitems:models/blocks/prestige/*` and `hideout/other/prestige/*` assets actively loaded at every startup — walls, ceiling, shield, mine, scanner, teleporter, charger/discharger, a "psychoscope", a throne, a cup — reads as a hideout-decoration/base-building item set, plus prestige-themed grenade models. `modassets/assets/pda/map-prestige/` also has 4 real region-tile (`.ol`) files on disk (smaller than a full open map, ~1.1MB vs map-new_bar's 7.8MB, but real). `prestigeHowToPlayOpened: 0` just means this account hasn't opened that one tutorial popup.
- **Triumph**: a seasonal rewards/battle-pass-style UI system. `stalker:textures/gui/triumph/2026/reward_background.mic`, `reward_preview_1.mic`, and (separately) `triumph/2025/preview_mask.mic` are loaded live during actual play — confirms it's a **recurring annual system** (a "Triumph 2025" and "Triumph 2026"), not a one-off, and not cut.
- **"Operation"** (from `seenOperationHtpPages` in the common save file): a real PvE co-op mission/raid mode. Assets: `customitems:models/blocks/operations_w/{flesh,boar,dog,snork,bloodsacker,pseudogiant,chimera}_N.mcvd_trace` (mutant-themed, with a `snow/` variant subfolder — a winter version of the same operation?), plus a specifically named mission `operations/kutuzov/session/Kutuzov_sesion.mcvd_trace` and `hideout/other/operations/quest_kutuzov/`, and mechanical set pieces (`operations/lift/lift.mcvd_trace`, `hideout/other/operations/small_lift/`). All loaded at every startup, not gated.
- **`frontline_sample_1/2/3`**: the one genuinely ambiguous lead — these are registered as "default collider" placeholder game objects once at boot (`Game Startup` phase), and nothing else in the log ties them to real Frontline content. Meanwhile actual `frontline`-named assets that DO load during real play are weapon/grenade skins and hideout decorations under a `.../summer25/frontline...` path — i.e. "Frontline" reads like a **cosmetic/seasonal item theme** (Summer '25 event), not a distinct game mode, and the "_sample" colliders are probably just generic placeholder physics shapes with an internal naming coincidence, not evidence of a removed Frontline mode. Inconclusive either way — noted as unresolved rather than asserted.
- **New, incidentally confirmed via `logs/client.log`'s startup dump of `config/options.json`** (this is the account's live settings, already known to exist, just hadn't been read in full): confirms several previously uncatalogued real game systems by their setting keys — `enable_broyale_warning` (a Battle Royale mode, "broyale"), `fight_equipment`/`fight_map`/`fight_bet` (a wagered 1v1 duel/"fight" system), a `dmz` HUD atlas (`stalker:textures/gui/dmz/atlas_event_hud.mic` — likely an extraction-style event mode, DMZ-named same as the genre convention), and a `stalkerclans` asset namespace (a clans system). Also directly useful for later protocol/server work: confirmed real client-side networking settings `protocol` (currently `0`), `send_rate_hint` (`1`), `tunnel_selection_mode` (`0`), `chunk_net_caching` (`true`) — these are genuine client config keys, not guesses, though their exact effect on the wire protocol is still unknown.
- **Full asset namespace list** (`modassets/assets/*`, top level) now fully catalogued: `_jurisdiction, _localized, anomalies, auction, bundle, chat, currency, customitems, customnpcs, effects, ejection, gameobjects, gloomycore, handcuffs, highpoly, misc, party, pda, probuilder, ragdolls, shop, skinarmor, stalcraft, stalker, stalkerclans, stalkerguide, stalkerplayer, stalkersounds, trade, vegetation, weapons`. Two notably new: `stalcraft` (separate from `stalker` — likely shared/base assets from the parent game STALCRAFT) and `probuilder` (Unity ProBuilder — suggests in-game building geometry may be authored/loaded via ProBuilder-style meshes, relevant to the hideout-building system).

**Conclusion**: the systems that looked like they might be "cut" (per the user's ask) are, on actual evidence, live and shipping — just larger and more numerous than what we'd catalogued from a single account's limited play history. If genuinely cut/scrapped content exists, it hasn't turned up yet from log/save-data reading; the next place to look would be unreferenced assets sitting in the jars/asset trees that never get logged as "loaded" during normal play (a different kind of search than what was done here), or version-diffing against an older client build if one's ever available.

## Live session findings, part 12 — 2026-08-30, real weapon stats found on stalcraftdb.net (fills a known gap)
Part 10/11 flagged that actual weapon stats (damage, RPM, recoil, etc.) aren't in `modassets` — only mesh/texture folders. Went back to `stalcraftdb.net` (the user-suggested community DB, already used in part 6) with the browser tool to check properly this time, since the first WebFetch-based check of a random item earlier (part 6-era) happened to land on a cosmetic knife *skin*, which has no gameplay stats — a bad example, not evidence stats are absent.

### CONFIRMED — stalcraftdb.net has full, structured weapon stats via a documented API
Navigated the site's UI to `[Weapon] Assault Rifle → AK-12` and captured the real backing network request: **`GET https://stalcraftdb.net/api/items/<opaque-4-char-id>?region=eu`** (e.g. AK-12's id is `g4mdp`) returns a full JSON item record — same `infoBlocks` schema noted in part 6, but for a *functional* weapon it's populated with real numeric stats, not just rank/flavor text. Confirmed real category naming convention from this response: `"category":"weapon/assault_rifle"` (lowercase, slash-separated — corrects an earlier uncertain guess at the taxonomy).

Concrete example (AK-12, id `g4mdp`, EU region, this build's live data): weight 3.35kg, durability 100%, movement speed -2%, ammo type 5.45mm, **damage 37.5**, magazine 30, max distance 160m, **rate of fire 700 shots/min** (burst-fire mode: 850 shots/min), reload 3s / tactical reload 2.5s, weapon ergonomics 100%, spread 0.4°, hip-fire spread 3°, vertical recoil 0.51°, horizontal recoil 0.12°, draw time 1s, aiming time 0.2s, headshot damage modifier x1.4, limb damage modifier x0.8, overheats after 51 consecutive shots. Most usefully, there's a dedicated structured block for damage falloff by range:
```
{"type":"damage","startDamage":37.5,"damageDecreaseStart":18,"endDamage":29.9,"damageDecreaseEnd":72,"maxDistance":160}
```
i.e. full damage (37.5) out to 18m, linearly dropping to 29.9 by 72m, holding to a 160m max effective range. This is a clean, directly-usable ballistics model — exactly the shape of data the config system's `damageMultiplier` (server-emulator/config/ServerConfig.java) will eventually need to apply against.

### Why this counts as evidence, not assumption
Per the project's ground rule: this is a **documented public source** (the community DB, sourced from EXBO's own official per-update data exports per the site's own disclaimer: "EXBO exports item data from the game with every new update which StalcraftDB relies on... only whitelisted items are included"), not a guess or a STALCRAFT-vs-STALZONE crossover assumption — it's specifically an EU-region STALZONE export. Caveat: "only whitelisted items are included," so coverage isn't guaranteed complete, and this is the live/current game's balance data, not necessarily unchanged from patch to patch — treat as a snapshot, re-check before relying on exact numbers long after this session.

### Practical path forward for weapon content
The `[Weapon] *` category tree (Assault Rifle, Device, Heavy, Machine Gun, Melee, Pistol, Shotgun Rifle, Sniper Rifle, Submachine Gun) plus `[Weapon_modules] *` are all browsable the same way. A future scraping pass (respecting the site) could walk the already-known `GET /api/listing/eu/grouped` category listing, then hit `GET /api/items/<id>?region=eu` per item id, to build a real starter weapon-stats table for the server-emulator — the actual numeric `infoBlocks` entries (`type:"numeric"`, has a raw `value` plus localized `formatted` strings) and the special `type:"damage"` block are both straightforward to parse into a config-ready format. Not built yet — this session just confirmed the schema and one real example end-to-end.

## Live session findings, part 13 — 2026-08-30, bulk weapon-stats scrape (partial — rate-limited)

Continuation of part 12 ("Practical path forward for weapon content" above — this is that pass, attempted). Built a bulk scraper against stalcraftdb.net's own API, run as same-origin `fetch()` calls in-page via the browser tool (not WebFetch — avoids the earlier summarization-truncation problem noted in part 12).

### Method
Fetched `/api/listing/eu/grouped` to get the id/category/name for all 338 items across the 9 real weapon categories (`weapon/assault_rifle` 90, `weapon/submachine_gun` 33, `weapon/machine_gun` 18, `weapon/sniper_rifle` 51, `weapon/shotgun_rifle` 36, `weapon/pistol` 41, `weapon/heavy` 7, `weapon/melee` 54, `weapon/device` 8 — confirmed complete, matches part-12 category survey exactly). Then fetched `/api/items/<id>?region=eu` per id in batches, extracting every `infoBlocks` entry into a flat record keyed by the item's own translation key (e.g. `weapon.tooltip.weapon.info.clip_size` → magazine capacity), so nothing is hand-guessed — every field name traces to a literal key the client's own localization system uses.

### CONFIRMED — 178 of 338 items got real, structured data before hitting a rate limit
Delivered to the user and committed to `data/weapons/`: `weapons.json` (178 clean records with full stats — weight, damage, magazine, rate of fire, reload times, ergonomics, spread, recoil, draw/aim time, headshot/limb multipliers, overheat, damage-falloff block, plus a `raw` catch-all for any per-weapon-specific field this pass didn't have a friendly name for yet), `weapon_pending_ids.json` (the 160 not yet fetched), `README.md` (full methodology and the gap below, written so this can't be mistaken for a complete dataset).

### Known gap — 160 items NOT fetched (rate-limited, not "no stats")
Partway through the bulk fetch (around item ~180/338, roughly 1 request per 450ms average), the API started returning `{"error": "You're now on the naughty list."}` instead of real item data. That's valid JSON with no `infoBlocks`, so the extractor silently produced empty records for everything after that point — a data-quality bug in the scrape, not a real "these items have no stats" finding. Caught it by checking field counts per record (all 160 affected records had zero extracted fields, in a contiguous block from index ~178 onward). Waited ~30s and retried once — still blocked — so stopped rather than continuing to hit the site. The 160 pending ids are preserved for a later, properly-paced re-fetch (800ms-1s between requests, with explicit detection of the `{"error": ...}` shape and backoff-and-retry instead of silently recording it as data).

### Side finding, unresolved — melee weapons show zero stats in every clean fetch
All 54 `weapon/melee` items that WERE cleanly fetched (not caught by the rate limit) came back with zero stat fields — consistent with the part-12 finding that at least one melee item (a knife skin) is cosmetic-only, but not yet confirmed true for every melee weapon on the site. Could mean melee weapons genuinely have no stats in this API (real combat stats defined only client-side), or could be an artifact worth re-checking once a clean re-fetch is possible. Flagged as an open question, not a conclusion — per the project's evidence rule.

### Follow-up needed
Re-fetch the 160 pending ids once the rate limit clears, with real pacing and explicit error-response handling; merge into `weapons.json`; specifically re-verify a few melee ids to settle the open question above; delete `weapon_pending_ids.json` once complete.

## Live session findings, part 14 — 2026-08-31, full 14-hour client.log analysis (real playthrough, not a capture)

The user played a genuinely long session (2026-08-30 14:08:24 → 2026-08-31 02:07:55 UTC, confirmed by the log's own first/last lines and the literal "Game stopped!" shutdown message — just under 14 hours, 3.66M log lines, 668MB). Analyzed via targeted `grep` on the live client.log/netcode.log/watchdog.log (never fully loaded — far too large), not a packet capture. This is the single richest evidence source so far because it's the client's own text logging, not encrypted wire bytes.

### CONFIRMED — full multi-region tunnel-selection topology, with the real proxy technology name
This fills in and hard-confirms the "RTT-based tunnel selection" behavior noted in earlier findings (previously inferred/partial). At startup, a `RegionPingThread` pings **4 regions** (EU, NA, SEA, NEA), each with multiple named "pools" (physical PoPs/relay clusters), each pool with one or more named "entrypoints" that get RTT-measured:
- **EU**: pool FRA (Frankfurt) → GAME-EU-1/2/3/4/7; pool MSK2X (Moscow) → MSK2-ROXY11; pool KYI (Kyiv) → KYI-ROXY1/2
- **NA**: pool DAL (Dallas) → GAME-NA-3/4/5; plus single-entrypoint pools NYC, LAX, MIA, CHI, ASH, ATL, SEA(Seattle), TOR, SAN, LIM — each `<CODE>-ROXY1`
- **SEA region** (Southeast Asia — distinct from the NA "SEA" pool which is Seattle): pool SGP (Singapore) → SGP-1; pool SYD (Sydney) → SYD-ROXY1
- **NEA region**: pool TYO (Tokyo) → TYO-ROXY1; pool KOR → KOR-1 through KOR-6

Each measurement line has this exact shape: `MeasuredEntrypoint(name=X, clientToTunnelRtt=N, tunnelToBackendRtt=N, isConnectionsLimitReached=bool)`. The actual selection is logged explicitly: `[Game thread/INFO] Selected tunnel entrypoint: RoxyTunnelEndpoint(name=GAME-EU-1, address=/89.222.108.193:29450)` — this is the literal, real Java class name for the tunnel-proxy client (`RoxyTunnelEndpoint`), confirming the proxy layer is internally called "Roxy" (matches the `*-ROXY*` entrypoint naming). It picked GAME-EU-1 because it had the lowest `clientToTunnelRtt` (22-23ms) among the FRA pool's reachable entrypoints — consistent with simple min-RTT selection. **This directly explains and confirms `89.222.108.193:29450`**, the address tracked since the very first packet captures, as one specific entrypoint (GAME-EU-1) of one specific pool (FRA) of one specific region (EU) in this much larger global system — not a fixed/singular game server.

### CONFIRMED — real backend HTTPS domain: `backend-nea.stalzone.com`
Seen in a `RegionPingThread` HTTP client log line (`https://backend-nea.stalzone.com:443`) — a genuinely new, real, literal endpoint string (previously only inferred as `backend.stalcraftx.ru` from partial evidence). Only appeared once, right at shutdown, as a connection-reset retry (client tearing down mid-request) — not evidence of a persistent problem, just caught mid-flight.

### New — `85.236.100.155:28352` is Vivox (voice chat), not a second game server
Ruled out as a lead: full context shows it's `[VIVOX/error] [::__sendtov] Failed to send to 85.236.100.155:28352`, a voice-server send failure, unrelated to the game protocol. Recorded so this IP doesn't get re-investigated as a mystery server later.

### New — netcode.log gives real, literal server-validation reason codes
`netcode-9.log` (small, separate from client.log) logs a "Shot processed correctly" line per shot (high-frequency, expected), plus rare deviations with **literal reason strings straight from the client's own netcode layer**:
- `Movement desync (reason: position_mismatch)` — 11 times over 14h (server correcting client-predicted position; confirms client-side prediction + server reconciliation)
- `Shot rejected (reason: can_not_shoot)`, `(reason: no_weapon)` (×3), `(reason: dead)` — concrete validation-rejection reasons
These are genuinely useful as ground truth for what validation reasons the emulator's own future movement/shooting logic should be able to produce, once that's built.

### New — recurring "Failed to parse RPC packet" warning (5,730 times over 14h, cause unconfirmed)
`[Thread-19/WARN] Failed to parse RPC packet` — no stack trace, no packet content, just the bare message. Starts 6 seconds after `GAME_SERVER_CONNECTED` (not before — ruled out as being about Discord's presence IPC, which uses a differently-named thread and connects 22s earlier), and recurs in pairs every ~15 seconds for the entire session, confirmed by spot-checking a window 6 hours later (same cadence). Consistent cadence + total silence about *why* it fails strongly suggests either (a) an expected/optional packet type this build doesn't fully decode, logged defensively, or (b) something the client swallows on purpose. Left as an open question — flagged here rather than guessed at, per project rule.

### New — one real, stable numeric "packet_id" tied to a named packet category
54 occurrences of `[Game thread/ERROR] Can not process packet from server (packet_id: class 1775361307): java.lang.RuntimeException: Exception while handling inventory packet for windowId=0, local view windowId=0`. The packet_id (`1775361307`) is the **same exact number every single time** across the 14h session — not a per-run-random hashCode — meaning it's a stable identifier (most likely a deterministic hash, e.g. Java's own `String.hashCode()` of a packet class/type name, computed once and reused). The exception message text itself ("inventory packet") is NOT obfuscated — it's a literal string constant — so we know this specific packet_id maps to inventory-related packets. Worth a future low-effort experiment: brute-force short candidate strings (plausible packet class/type names) through Java's public, deterministic `String.hashCode()` algorithm to see if any produce `1775361307` — pure math on public info, zero classes.jar/anti-cheat risk.

### Side note — obfuscation reaches into method names using Java reserved keywords
Java forbids using reserved words as identifiers in source, but the bytecode-level restriction is looser — this obfuscator renames methods to keywords like `this`, `continue`, `finally`, `const`, `default`, `switch`, `package`, `volatile`, `throw`, `else`, `public`, `goto`, `enum`, `extends`, `super`, `throws`, `private`, `static`. Seen only incidentally in exception stack traces surfaced by normal gameplay logging (not from opening classes.jar) — consistent with, not an escalation of, the part-9 "obfuscation is total" finding. Not pursued further per the project's EXENS/launcher-internals scope note.

### Engine health, for context (not protocol-relevant)
`watchdog.log` recorded 8 "freezing for above 500ms" hitches over 14h — mostly Steam DLC-check calls (`SteamApps.isDlcInstalled`) and GC pauses blocking the game thread briefly. Nothing alarming, just real engine behavior worth knowing if performance ever gets compared against the real client.

### Update, 2026-08-31 — stalcraftdb.net ban confirmed site-wide, not just the API
Checked the scrape-resume status ~16 hours after the block first hit. Still fully blocked — and the site's own homepage now explicitly says so: **"Temporarily banned — You seem to have exceeded the rate limit too many times. Please try again later."** This is a whole-site ban (not just the `/api/items/...` endpoint), confirming the earlier bulk scrape (part 13) was too aggressive even before the ~450ms/request average pace that triggered it. Not retrying further right now — a site ban lasting 16+ hours suggests this needs real wait time (unknown duration, possibly 24h+), and continuing to poll it would be counterproductive. **Lesson for next attempt:** when resuming the 160 pending ids, use a much more conservative pace (2-3+ seconds between requests, not under 1s) to avoid re-triggering this.

### Update, 2026-08-31 — packet_id 1775361307 hash brute-force: negative result (honest)
Tried to crack the stable "packet_id: class 1775361307" number from part 14 by implementing Java's `String.hashCode()` and testing it against ~8,000 curated packet-naming candidates (Inventory/Window/Item/Slot/Container combined with common prefixes like S2C/C2S/Packet/Clientbound, plus package-qualified variants) and an exhaustive brute force of every alphanumeric+underscore string up to length 4 (~16 million strings). No match found.

**Revised interpretation:** re-examining the surrounding stack traces (part 9, part 14), essentially every obfuscated class in this build is renamed to a bare large signed-32-bit-looking number (`913632898`, `1968893277`, `1042539417`, etc. — dozens of distinct examples seen across every exception in client.log/watchdog.log). `1775361307` fits that exact same pattern. The much more likely explanation is that this number simply IS the obfuscated packet-handler class's own (post-obfuscation) name/identity — not a hash of some pre-obfuscation English string like "InventoryPacket" — so there's no meaningful source string to brute-force in the first place. Downgrading this from "promising lead" to "explored and set aside" — noted here so it isn't re-attempted without new evidence changing the picture.

## Live session findings, part 15 — 2026-08-31, map geometry format IDENTIFIED and VALIDATED against real milestone-1 data

Big one. This directly unblocks Phase 1's actual goal (a walkable single zone), not just more catalogued content.

### CONFIRMED — the world/voxel geometry format is `.mdat`, not the `pda/` minimap tiles
Earlier notes (including this file's own MilestoneMap.java comments) assumed "map geometry" lived somewhere under `modassets/assets/pda/` and referred to unparsed "`.mcsb`/`.mcmtl`" formats. Both assumptions were wrong, now corrected with real evidence:
- `pda/*.ol` files are flat 2D minimap **texture tiles** (regular `.ol`-format images, same container as every other in-game texture) — not 3D geometry at all. They tile together into the in-game map/PDA screen image, nothing more.
- **`.mcmtl` does not exist.** Checked directly: a real weapon model folder (`modassets/assets/weapons/models/weapons/6p62/`) contains `6p62.mcsb` plus several `_diff`/`_nrm`/`_spek` `.ol` texture files — no `.mcmtl` anywhere. `.mcsb` mesh files simply embed their material as a plain name string inside the mesh data (a reference resolved elsewhere), not a separate material file. This was an unconfirmed guess baked into earlier docs; now corrected.
- The actual 3D world/voxel data — the thing needed for a walkable zone — lives in **`.mdat` files under `map_cache/5.0/<map-name>/`**, one file per region (`reg.X.Z.mdat`), completely separate from `modassets`/`pda`.

### CONFIRMED — real, public, documented, actively-maintained format specs exist: `onejeuu/sc-file`
Found via web research (github.com/onejeuu/sc-file, MIT-licensed, docs at sc-file.readthedocs.io, 26 releases). This is a proper reverse-engineered asset converter for STALCRAFT/STALZONE with actual source code (not just a README) and **010 Editor binary templates** as a byte-level spec. Confirmed formats relevant to us:
- **`.mcsb`/`.mcsa`** (mesh/model): signature `MCSA`, versioned binary format (v7-v15), little-endian, quantized int16 positions/UVs and int8 normals/tangents, optional skeleton+skinning+animation+blendshapes. Material is an embedded name string, not a file.
- **`.mdat`** (world region cache — **the actual voxel data**): a McRegion/early-Anvil-style container. 1024-entry header table (offset + sector count per chunk slot, plus a 16-byte GUID per entry — this game's own addition vs vanilla McRegion). Each chunk record: a 20-byte big-endian header (`record_size, section_mask, add_mask, terrain_size, compressed_size`) followed by **Zstandard-compressed** (not zlib) payload containing, per present Y-section (16³ voxels each, selected by bits in `section_mask`/`add_mask`): raw block-ID bytes, metadata nibbles, 3 lighting nibble-planes, optional "add" high-bits nibble-plane, then a 256-byte biome array. Structurally a modified Minecraft Beta/early-Anvil chunk format — confirms the world/hideout-building system really is voxel-block-based under the hood (explains the `customitems:models/blocks/...` asset-namespace naming noticed back in part 11).
- **`.ol`** (textures, incl. `pda/` tiles): signature `0A 95 23 FD`, big-endian header, XOR-obfuscated (`byte ^ 0x67`, `0x47`-byte padding removed) 16-byte FourCC identifying the pixel format (DXT1/3/5, ATI1/2, RGBA8, BGRA8, RGBA32F), per-mipmap LZ4-block-compressed data.
- Also documented but not yet used by us: `.mcal` (shared animation library), `.texarr` (texture array archive), `.mic` (PNG-in-a-4-byte-wrapper). Explicitly NOT decoded by this tool (AES-encrypted, no recovered key): `.xeon`, `.mcws`, `.ta`, `.bank`.

Full citation and every field-layout detail (with source code quoted) is preserved in this session's research notes; the repo itself is the primary source of truth going forward — `https://github.com/onejeuu/sc-file`, docs at `https://sc-file.readthedocs.io/en/latest/formats.html`.

### CONFIRMED, independently re-verified — real decode of our exact milestone-1 map, not just trusting the tool
Per the project's evidence rule, didn't stop at "a community tool claims to support this" — ran it against real files from this install and manually re-verified the output myself rather than trusting the tool's own success message:
1. Installed `sc-file` (needs Python 3.13+) from source, ran it on a real weapon mesh (`6p62.mcsb`, an actual weapon model file) → produced a valid 2.1MB `.obj` with 15,534 vertices, 18,111 faces, and sensible named sub-objects (`bullet_0`, `body_`, `default_magazine`, `shell_mesh`) — a real, structurally sound weapon model, not garbage.
2. Ran `sc-file`'s `mapcache` command against the real cached region data for **dungeon_bolota_tutorial** (our milestone-1 target) — all 4 of its cached region files (`reg.-10.-10`, `reg.-10.-11`, `reg.-9.-10`, `reg.-9.-11`, found under `map_cache/5.0/dungeon_bolota_tutorial/` on this install). It merged them into standard Minecraft Anvil `.mca` files, reporting 194+141+239+216 = **790 chunks total**.
3. Independently re-parsed the header of one output `.mca` by hand (not using the tool) — found exactly 194 non-empty chunk slots, matching the tool's own count exactly.
4. Manually decompressed and NBT-parsed a real chunk from that file (zlib, standard Anvil chunk NBT: `DataVersion`, `Level.xPos/zPos`, `Biomes`, `Sections` list) — valid, well-formed structure.
5. Pulled raw block-ID bytes out of 10 real chunks and histogrammed them: dominated by air/stone with a grass surface layer and small pockets of sand/gravel/clay — exactly what real terrain looks like, not noise or corrupted data. Chunk coordinates were contiguous and sensible (e.g., x=-302..-293, z=-320).

**Conclusion: this is real, validated, working access to STALZONE's actual 3D world geometry for our exact milestone-1 zone.** This is evidence-backed at every layer (public documented format spec → real file on this install → tool output → independent manual re-verification), not an assumption.

### CONFIRMED, corrected — the real map roster is much bigger than previously catalogued
`map_cache/5.0/` lists 29 zone folders with real cached region data (i.e., zones this account has actually visited/loaded, not just referenced in assets): `agroprom, bar, bar_save, bar_save_residential_hangar, base_merc, boat_station, bolota, dungeon_bolota_tutorial, dungeon_bolota_tutorial_2, dungeon_domiki, dungeon_lab, dungeon_mexdvor_bandits, dungeon_psisobaka, dungeon_sanitar_kordon, dungeon_sklad_frimena, dungeon_svetliy_bandits, dungeon_toxic_dogout, dungeon_travnik, dungeon_upir, dungeon_zemlcap1, foton2, hideout, kordon, les, north_mines, poligon2, savezone_agroprom, svalka, yantar, zemlyanka_shaman`. Several are direct S.T.A.L.K.E.R.-series callbacks (agroprom, bar, kordon=cordon, svalka=garbage, yantar=amber, bolota=swamp, les=forest) — STALZONE clearly reuses/reimagines the original franchise's zone roster. This supersedes the old, much shorter `ALL_KNOWN_MAPS` list (which was built from `pda/` minimap folder names and missed most of the real world) — `server-emulator/.../world/MilestoneMap.java` has been corrected to match. Milestone-1 pick (`dungeon_bolota_tutorial`) is unchanged and now validated; fallback changed from the incorrect `map-new_bar` to the real folder name `bar`.

### Practical path forward
Next real engineering step (not yet done): port an `.mdat` reader to Java (Zstandard decompression is available as a real library — check if it's on Maven or vendor a pure-Java implementation given this sandbox's network restriction; the section/nibble-plane parsing itself is straightforward once decompressed) so the server-emulator can load real chunk data natively, rather than round-tripping through the Python tool every time. Alternative/faster path: pre-convert milestone-1's region files to `.mca` with the Python tool once (already proven working) and ship that as static data, with a much smaller Java-side Anvil-format reader (region header table + zlib, both doable with `java.util.zip` alone, no new dependencies) reusing the NBT reader already built in part "Phase 1 started". Not decided yet which path to take.

## Part 16: .mdat world-region reader ported to Java, compiled, and byte-for-byte validated (2026-08-31)

Following part 15's format identification, the actual Java reader has now been built,
compiled, and tested -- this is no longer just a documented format, it's working code in
the server-emulator.

### What was built

New package `server-emulator/src/main/java/net/stalzone/emulator/world/region/`:

- `ChunkHeader.java` -- the 20-byte per-chunk record header (recordSize, sectionMask,
  addMask, terrainSize, compressedSize), ported field-for-field from onejeuu/sc-file's
  `scfile/content/regions.py` `ChunkHeader` dataclass.
- `ChunkSection.java` -- one 16x16x16 vertical Y-band of a chunk (blocks/metadata/additions
  byte arrays), with `blockIdAt`/`metadataAt`/`fullBlockIdAt` accessors using the standard
  Minecraft Beta/Anvil `y*256 + z*16 + x` per-section index order.
- `RegionChunk.java` -- one chunk (up to 16 sections + biomes), with `localChunkX()` =
  `slotIndex % 32` / `localChunkZ()` = `slotIndex / 32` (matches
  `scfile/formats/mca/encoder.py` line 35 exactly).
- `RegionFile.java` -- **the actual reader.** Opens a `.mdat` file, parses the fixed
  1024-entry/24,576-byte header table, seeks to each non-zero chunk's `offset*4096`, reads
  the 20-byte header, reads `compressedSize` bytes, decompresses via the existing
  `CompressionUtil.decompressIfNeeded()` (zstd-via-CLI-shellout, already built and reused
  as-is -- no new decompression code needed), and splits the decompressed payload into
  `ChunkSection`s + biomes using the exact byte-range math from
  `scfile/formats/mdat/payload.py`'s `chunk()` function (blocksEnd / metadataEnd /
  lightingEnd / addEnd / biomesEnd, and the `popcount(addMask & ((1<<y)-1))` formula for
  finding a sparse Y-band's slot in the `additions` array).
- `RegionDumpTool.java` -- a CLI sanity-check tool (same pattern/spirit as the existing
  `nbt.NbtDumpTool`): point it at a `.mdat` file or a whole zone folder and it prints
  per-region chunk/section counts plus a block-id histogram, so the reader's output can be
  eyeballed without needing to trust it blindly. Usage:
  `java -cp out net.stalzone.emulator.world.region.RegionDumpTool <path-to-.mdat-or-folder>`

### Validation performed (not just "it compiled")

Per this project's standing evidence rule, the reader was validated two ways, both against
the real staged `dungeon_bolota_tutorial` region files (the same 4 files from part 15):

1. **Aggregate cross-check**: `RegionDumpTool` run against all 4 region files reports
   **790 total chunks** -- an exact match to the previously-recorded, independently
   Python-tool-validated chunk count from part 15. Block-id histogram again dominated by
   id=1/0/2 (stone/air/grass-or-dirt in classic Minecraft numeric IDs), consistent with
   real terrain, not noise.

2. **Byte-for-byte cross-check against onejeuu/sc-file's own decoder**: ran
   `MdatDecoder(path, Options(extended_chunk=True))` directly from the cloned Python
   reference implementation against `reg.-10.-10.mdat`, and compared its output field-by-
   field against the new Java reader's output for the same file:
   - Chunk count: 194 == 194.
   - First chunk (slotIndex/index=18): header fields (`recordSize=314, sectionMask=2287,
     addMask=2280, terrainSize=108800, compressedSize=297`) identical in both. Section
     count (8) identical. First two sections' raw `blocks`/`metadata` bytes identical hex.
   - **Sparse `additions` array indexing specifically stress-tested**: chunk 18's 8 present
     sections have `addMask` bits set only at y=3,5,6,7,11 (non-contiguous, not aligned with
     `sectionMask`'s own bit pattern) -- exactly the case that would expose an off-by-one or
     wrong-popcount bug in the `add_section_index = popcount(addMask & ((1<<y)-1))` formula.
     All 8 sections' `additions` length and hex bytes matched exactly between Python and
     Java, including a section (y=5) with genuinely non-zero additions data
     (`09 00 00 00 00 00 00 00 09 00 00 00 00 00 00 00 ...`), ruling out a
     "matched because it was all zeros" false pass.
   - A middle chunk (slotIndex=223, a different section-count/mask shape) was also spot-
     checked and matched.

This is the strongest validation done on any single piece of this project so far: not
"the counts look plausible" but literal byte-identical output against the community
reference implementation, on real data from the user's own install, specifically probing
the trickiest part of the algorithm (sparse add-section indexing) rather than just the
common case.

### What this unblocks

The server-emulator can now genuinely load STALZONE's real 3D voxel world geometry for
`MilestoneMap.MILESTONE_1_MAP` ("dungeon_bolota_tutorial") and any of the other 28 real
zones in `MilestoneMap.ALL_KNOWN_MAPS`. This is the actual technical blocker for the
project's milestone-1 goal ("a single static walkable zone") -- it's no longer blocked on
"we don't know the map format," it's now "load region files into the world model and wire
up movement/collision," which is ordinary emulator-building work rather than unresolved
reverse-engineering.

### Explicitly not done yet (so this isn't overclaimed)

- No collision/physics/spawn logic uses this data yet -- `RegionFile` reads and exposes the
  voxel data; nothing consumes it into an actual world model or serves it to a client.
- `.mcsb` (weapon/prop mesh) has no Java reader yet -- only the world/region format was
  ported this round. The `6p62.mcsb` test file is staged and validated as decodable (via
  the Python tool) but has no Java-side equivalent yet.
- `lighting` and the trailing "extended records" region of the decompressed payload are
  parsed-past (offsets computed correctly, byte-for-byte proven via the biomes match, which
  sits after both) but not exposed/stored -- nothing in the project needs them yet, and
  they can be added later without changing anything else if that changes.
- Region-to-world coordinate mapping (how region X/Z + local chunk X/Z + block X/Y/Z
  combine into one global world coordinate space) is not implemented -- `RegionFile.regionX`/
  `regionZ` are parsed from the filename and stored, but nothing yet does the actual
  region-grid math.

## Part 17: weapon-stats dataset completed via a second source (stalzone.wiki), 337/338 done (2026-08-31)

Direct continuation of part 13 (the stalcraftdb.net scrape that hit a site-wide ban
with 160/338 items still pending). The user pointed at an alternative community site,
`https://stalzone.wiki/en/items`, since stalcraftdb.net's ban was still active.

### Confirmed: both sites are the same underlying id space and schema

Before writing any scraping code, checked stalzone.wiki's own item-page data for an id
field. It exposes `exbo_id`, and it's **the exact same opaque id** stalcraftdb.net
uses (e.g. `1r7dr` = PM on both sites) -- both are front-ends over EXBO's official
per-update data export, not independent datasets. This meant the 160 pending ids could
be looked up directly by id (no fuzzy name matching), and that the same
translation-key-based field schema from part 12 applies unchanged.

### How the data was actually reachable

stalzone.wiki is a Next.js App Router site. Item pages don't expose a public REST API
for stats -- the numbers are embedded in the page's React Server Component ("flight
protocol") stream, inside `<script>` tags meant for client hydration. Checked several
plausible API paths first (`/api/exbo/item/<id>`, `/api/exbo/item/stats/<id>`, etc.)
-- all 404. The working approach: `fetch()` each item page's raw HTML (same-origin,
via the browser's javascript tool, not a full page navigation -- no CSS/JS/image
loads, much lighter per-request than a real page view), parse the HTML's `<script>`
tags containing `self.__next_f.push(...)` calls, evaluate each one in a sandboxed
`self.__next_f = { push: collector }` shim (literally the same mechanism the page
itself uses to hydrate, just intercepted instead of handed to React), concatenate the
resulting text chunks, and JSON-parse each `id:payload` line of the flight stream. The
line containing `"info_blocks"` and the requested exbo_id is the item's full data
object -- same `info_blocks` list-of-lists-of-elements shape as stalcraftdb.net's
`infoBlocks`, just a different top-level key name.

### Execution and pacing

Given the part-13 lesson (unthrottled fetching got the whole site banned), this pass
was deliberately conservative: ~500ms between requests, run as a same-origin
`fetch()` loop from the browser's javascript tool (paced in batches of ~40 to stay
within tool response limits), never as a raw unthrottled loop. Result: **159 of 160
pending ids succeeded on the first attempt, zero rate-limit errors.** One id
(`5lndo`, "Modified Colt Walker") returned a soft-404 on stalzone.wiki too (HTTP 200,
but a client-rendered "not found" page body) -- a genuine per-item gap on this site,
not a scraping failure; left undocumented and explicitly flagged rather than guessed
at.

### Merge and outcome

Merged the 159 new records into `data/weapons/weapons.json` (each tagged
`"source":"stalzone.wiki"` for provenance; the original 178 stalcraftdb.net records
are untagged, matching their original shape). **Result: 337 of 338 weapon-category
items now have real stats, 1 genuine gap remaining.** `weapon_pending_ids.json` now
holds just that one entry. Raw untransformed scrape output kept at
`data/weapons/wiki_scrape_raw.json` for future re-processing without re-scraping.

### Bonus finding: the part-12/13 "melee weapons have zero stats" question is RESOLVED

Part 13 flagged that all 54 `weapon/melee` items came back from stalcraftdb.net with
no stat fields and left open whether melee weapons genuinely lack server-exposed
combat stats. They don't lack them -- **the original extraction was looking for the
wrong translation keys.** Melee weapons use a completely different key family
(`weapon.tooltip.melee_weapon.info.damage.min/max.common/strong` for light/heavy-attack
damage ranges, `.info.reach.common/strong`, `.stat_name.piercing`,
`.stat_name.bloodlust_chance`, `.critical_hit_mod`, `.mutant_damage_mod`) instead of
the firearm damage key the original code only checked. Once the wiki-scrape extractor
was widened to also read these keys, all 54/54 melee items came back with real,
populated data (example: Hunter's Knife -- 31-38 light-attack damage, 67-82
heavy-attack, 2.1m reach, 13.75 piercing, 50% bloodlust proc chance, x1.7 crit
multiplier, x2.35 vs. mutants). This was caught by spot-checking the first scrape
batch rather than assuming the schema transferred cleanly -- worth noting as a small
methodology win: verify a sample of results structurally before trusting an entire
batch run, even when the batch itself reports zero errors.

### One taxonomy inconsistency noted, not resolved

The 8 short-range-detector items (Beluga/Candle/Buryat/Elbrus, SN-1/1u/2/2u) were
categorized `weapon/device` in the original stalcraftdb.net-derived pending list, but
stalzone.wiki's own data reports their category as plain `device` (a separate
top-level "Devices" section on that site, not under "Weapon"). Recorded exactly as
each source reported it rather than silently harmonized -- flagged in
`data/weapons/README.md` so anything consuming `category` downstream knows to expect
both spellings for these 8 items.

**Practical effect**: the weapon-stats dataset that Phase-1-and-beyond damage/loot
tuning will read from is now essentially complete (337/338, one honestly-documented
gap) -- this was previously the single largest acknowledged hole in the project's
content data next to the world-geometry format (now also solved, part 15/16).

## Part 18: WorldModel -- the `.mdat` reader is now queryable by world coordinates (2026-08-31)

Direct continuation of part 16 (the `RegionFile` reader). `RegionFile` reads one
`.mdat` file in isolation, addressed by region-local chunk slot index -- useful for
verifying the format, useless for anything that wants to ask "what's at this actual
in-game position." That's what `WorldModel` (new,
`server-emulator/src/main/java/net/stalzone/emulator/world/WorldModel.java`) adds.

### What it does

`WorldModel.load(zoneFolder, mapName)` loads every `.mdat` file directly inside a zone
folder (e.g. all 4 region files for `dungeon_bolota_tutorial`) and indexes every chunk
by its **global** chunk coordinate (`regionX*32 + localChunkX`, same for Z -- the
standard Anvil region/chunk relationship, consistent with everything already
established in parts 15/16). From there:

- `blockIdAt(worldX, worldY, worldZ)` / `fullBlockIdAt(...)` -- the actual "what block
  is here" query a collision or movement system would call. Converts world coordinates
  to (chunk, section, local x/y/z) via `Math.floorDiv`/`Math.floorMod` (not plain `/`
  and `%`, which are wrong for negative coordinates in Java -- and this map's real
  region files are all at negative coordinates, e.g. `reg.-10.-10`, so this wasn't a
  theoretical concern).
- `highestSolidY(worldX, worldZ)` -- highest non-air block in a column, scanning down
  from Y=255. The obvious building block for "where does a player standing here end up
  vertically" (spawn placement, teleport-to-ground, etc.) -- not wired to any spawn
  logic yet, just the query.
- Returns 0 (air) for any position with no loaded chunk, or Y outside [0,255] --
  documented honestly as ambiguous (a cached-terrain snapshot can't distinguish "really
  air" from "never visited/not cached"), not silently treated as solid ground.

`world/WorldDumpTool.java` is the CLI (same pattern as `NbtDumpTool`/`RegionDumpTool`):
loads a zone folder, reports region/chunk counts and the actual block-coordinate bounds
covered, then probes `highestSolidY` at a few sample points across that area.

### Validation

Ran `WorldDumpTool` against the real `dungeon_bolota_tutorial` zone folder: 4 region
files, **790 chunks loaded** (exact match to the count independently confirmed twice
already in parts 15 and 16), a chunk-coordinate bounding box that looks like real
sparse cached coverage (not a filled rectangle -- consistent with "only visited chunks
get cached"), and `highestSolidY` probes returning plausible ground Y values (64, 88,
76) with plausible surface block ids (2, 101 -- both were in the earlier block-id
histograms) at points that had loaded chunks, and a clean "no loaded chunk" result
(not a crash or a wrong answer) at points that didn't.

That alone would just be "plausible," so it was also checked against **exact ground
truth**: chunk slot 18 in `reg.-10.-10.mdat` is the same chunk this project
byte-for-byte verified against `onejeuu/sc-file`'s own Python decoder in part 16 --
its section y=0 and y=1 block arrays are known (from that earlier dump) to be
uniformly block id 1 for every voxel. Hand-computed what world coordinate that chunk's
local (0,0) origin maps to (region `-10,-10` + chunk-slot 18 -> global chunk
`(-302,-320)` -> world block origin `(-4832, -5120)`), then called
`blockIdAt(-4832, 0, -5120)` and two more points inside the same known-uniform
sections -- all three returned `1`, exactly as the byte-verified raw data says they
must. Also checked a coordinate far outside any loaded chunk (returns 0, no crash) and
an out-of-range Y (returns 0, not garbage). This ties the new coordinate-math layer
back to the same ground truth the reader itself was validated against, rather than
just trusting that region-to-world arithmetic is right because it compiled.

### What this unblocks / what's still not done

This is the piece that lets anything else in the emulator ask real questions about the
milestone-1 zone's geometry by ordinary coordinates. It is still **only a queryable
model** -- nothing in the emulator's networking or game-loop code calls into it yet,
there's no collision resolution (just single-point block lookups), no player/mob
placement logic, and no write path (read-only, matches the reader it's built on). The
next real step toward "a walkable zone" is wiring this into whatever stands in for
player state once there's a game loop to have one -- separate from, and still blocked
independent of, the wire-protocol decryption question.

## Part 19 -- WeaponRegistry: wiring real damage data into ServerConfig.damageMultiplier

`ServerConfig.java` has had a `damageMultiplier` field since early in this project,
documented in its own comment as an unwired placeholder -- there was no gameplay
logic anywhere to apply it to. With the weapon-stats dataset complete (part 17: 337
of 338 items, both stalcraftdb.net and stalzone.wiki sourced) and no game loop yet to
justify building full combat resolution, the smallest honest way to make "full
control over ... damage values" concretely true was: load the real dataset, and let a
server operator's configured multiplier scale its numbers. That's `WeaponRegistry`.

### What was built

- `json/JsonReader.java` -- a minimal recursive-descent JSON parser, written from
  scratch because this project's sandbox has no path to a real JSON library (same
  "no Maven Central" constraint documented for Netty/zstd earlier in this log) and
  the JDK ships none. Objects/arrays/strings (with standard `\"`, `\\`, `\/`, `\b`,
  `\f`, `\n`, `\r`, `\t`, `\uXXXX` escapes)/numbers (as `Double`)/booleans/null, read
  fully into memory as nested `Map`/`List`/`String`/`Double`/`Boolean`/`null`. Not
  general-purpose or streaming -- exactly what a data file this size needs.
- `items/WeaponEntry.java` -- wraps one raw parsed weapon record (a `Map`) with typed
  accessors for the fields tuning logic needs (`damage`, `magazineCapacity`,
  `rateOfFireRpm`, the four melee min/max-common/strong fields, `isFirearm()`,
  `isMelee()`). Deliberately not a fixed-field DTO, since the dataset's two source
  provenances have different field coverage per weapon class and a rigid DTO would
  either lose fields or need nullable getters everywhere anyway.
- `items/WeaponRegistry.java` -- `WeaponRegistry.load(weaponsJsonPath)` parses the
  whole file, indexes by id and by case-insensitive name. `effectiveDamage(entry,
  config)` returns a firearm's `damage * config.damageMultiplier` (or null for
  non-firearms). `effectiveMeleeCommonDamage`/`effectiveMeleeStrongDamage` do the same
  for melee light/heavy attack `[min, max]` ranges.
- `items/WeaponRegistryDumpTool.java` -- CLI: `WeaponRegistryDumpTool <weapons.json>
  <damageMultiplier> [weaponName...]`, prints base-vs-effective damage per named
  weapon, branching on firearm/melee/neither (devices/detectors have no damage field
  at all -- prints that explicitly rather than silently showing nothing).

### Validation (against the real 337-record dataset, not a fixture)

Compiled clean against all 20 existing sources in this project, then staged the real
`data/weapons/weapons.json` from this machine and ran the CLI against it directly --
not a synthetic test file. Checks performed:

- **Load count**: `WeaponRegistry.load()` reports 337 records loaded, matching the
  dataset's known size from part 17.
- **Id integrity**: iterated `registry.all()` and checked every entry has a non-null
  `id()` and that no id repeats -- 337 unique ids, zero duplicates or nulls.
- **Category-shape cross-check, done independently in Python against the same file**:
  counted firearms (`damage != null`), melee (`damage == null && meleeDamageMinCommon
  != null`), and neither (devices/detectors) two ways -- once via
  `WeaponEntry.isFirearm()`/`isMelee()` in Java, once with a standalone `python3 -c`
  one-liner reading the raw JSON directly. Both gave **272 firearms / 54 melee / 11
  neither**, summing to 337. Matching independently-computed numbers from two
  different code paths is stronger evidence than either one succeeding alone.
- **Multiplier math, spot-checked per weapon shape** at `damageMultiplier = 2.0`:
  - Firearm: AK-12 (id `g4mdp`) 37.5 -> 75.0; PM (id `1r7dr`) 34.5 -> 69.0.
  - Melee: Hunters Knife (id `96w9q`) light 31-38 -> 62-76, heavy 67-82 -> 134-164;
    Crowbar (id `g4vmn`) light 53-88 -> 106-176, heavy 60-99 -> 120-198. (Note: the
    real in-dataset name is "Hunters Knife", no apostrophe -- the CLI's own default
    weapon list originally used "Hunter's Knife" and correctly reported "not found,"
    which is how this was caught -- a useful reminder that a demo tool's own
    hardcoded defaults aren't ground truth either.)
  - Device/no-damage-field: Beluga Short Range Detector (id `z4v9`, category
    `device`) correctly reports "no damage field in this record" rather than a wrong
    number or a crash.
  All values matched hand-computed `base * 2.0` exactly.

### What this unblocks / what's still not done

This is the second data-and-tuning layer built ahead of having a game loop to call
it (after `WorldModel` in part 18) -- both are ready for whenever there's a live
session that can ask "what block is under this player" or "how much damage does this
shot do." Nothing in the emulator's networking code constructs a `WeaponRegistry` or
calls `effectiveDamage` yet; there is no session, no shot, no player. The
`spawnRateMultiplier` and `lootRateMultiplier` fields on `ServerConfig` remain
unwired for the same reason -- there's no spawn or loot system yet to apply them to.
Still blocked on the same thing every gameplay-facing piece of this project is
blocked on: reading the encrypted wire protocol, which needs your explicit go-ahead
per part 7's risk notes.

## Part 20 -- MobRegistry: real mutant AI configs loaded, an honest negative on combat stats, and a "keep going" recon detour that mostly came up empty

Continuing after part 19 with no specific new user direction ("keep going"). Re-examined
the plan to eventually give mutants real, tunable stats analogous to `WeaponRegistry`.

### First: does stalzone.wiki (or any evidence source) have mutant combat stats?

Checked before assuming. stalzone.wiki's own nav has no mutant/mob stat pages -- its
`/en/characters` route is a **player profile search**, not a mutant database; there is
no mutant-equivalent of the item TTK calculator. Re-inspected the client's own
`modassets/assets/stalkermobs/configs/*.smm` files directly (not from memory -- pulled
`base_boar.smm` fresh and read every top-level key): confirmed there is genuinely **no
health or attack-damage field anywhere in the schema**. Every `_attacks` entry has an
`effector` object, and it is empty (`{}`) in every attack in every file checked --
timing/rotation/animation/impulse data is real, but there is no damage number attached
to it anywhere in these client-shipped files. **Conclusion, stated honestly rather than
guessed around**: mutant combat stats (health, per-hit damage) are not present in
any evidence source found so far. Building a `MobRegistry.effectiveDamage()` to mirror
`WeaponRegistry` would have meant inventing numbers with zero backing -- exactly what
this project's founding rule forbids. Not done. If a community site or other source
for this ever turns up, it gets its own accessor then, the same way weapon damage did
(part 12).

### What WAS real and worth building: a mob-behavior config loader

The `.smm` files do have genuine, evidence-backed, moddable content: per-mob behavior
flags (`canWalk`, `canRest`, `canRunAttack`, `bodyEater`, sound-playback settings) and
a full faction-relation table (`mutantRelations`, e.g. `boar -> {krovosos: ENEMY, boar:
ALLY, ...}`). This is a legitimate "full control over what can be done" target even
without combat numbers -- a server operator retuning which mutants are hostile to each
other, or disabling body-eating, doesn't need a damage model. Built `mobs/MobConfig.java`
(typed wrapper, explicitly does NOT add a damage/health accessor -- see its doc comment)
and `mobs/MobRegistry.java` (`MobRegistry.load(configsDir)` loads every `*.smm` in a
folder) plus `mobs/MobRegistryDumpTool.java` (CLI).

### Two real gotchas found by checking real data before shipping, not assumed

- **`mobName` is not a unique key.** Loaded all 35 real files and printed every
  `mobName` before designing the index (this is the habit that caught the melee-key
  mismatch in part 17 and the CLI's own wrong default name in part 19 -- checking
  actual output instead of trusting a clean compile). Result: `base_boar.smm` and
  `base_boar_clone.smm` both report `mobName="boar"`; `base_tick.smm`,
  `base_tushkan.smm`, and `base_tushkan_clone.smm` **all three** report
  `mobName="tushkan"` (the tick is mechanically a tushkan variant, not a separate
  creature type -- a real fact this caught, not documented anywhere before); and
  `base_drone.smm` reports `mobName="spirit"`, the same name `base_spirit.smm` uses.
  Confirmed as deliberate reuse in the boss-event files too: `base_tsarevich.smm` ->
  `mobName="boar_tsarevich"`, `base_tsarevna.smm` -> `mobName="flesh_tsarevna"` -- the
  Christmas/fairy-tale boss mutants (part 11) are named variants built on the
  boar/flesh behavior base, confirmed mechanically now rather than just from asset
  naming. An initial version of `MobRegistry` indexed only by `mobName` and would have
  silently dropped 6 of the 35 configs to key collisions -- caught before compiling
  the final version, not after. Fixed by indexing by the source **filename stem**
  (`configId`, verified unique across all 35) as the primary key, with `mobName`
  lookup exposed separately as a `List<MobConfig>`.
- **2 of the 35 files aren't strict JSON.** `base_puppet.smm` and
  `base_puppet_clone.smm` each contain one stray `//`-style line comment (a
  Russian-language developer note, roughly "remove walk if we don't want it to walk",
  attached to the "walk" animation entry) -- standard JSON has no comment syntax, so
  both this project's own `JsonReader` and Python's `json` module reject them as-is.
  Grepped all 35 files for `//` before deciding how to handle it: confirmed those two
  lines are the *only* occurrences across the whole set, and both are genuine trailing
  comments, not `//` inside a string value (which a naive strip would corrupt). Added
  a string/escape-aware `stripLineComments()` pass in `MobRegistry` (walks the text
  tracking quote/escape state, only strips `//...` when outside a string) rather than
  a blind regex, specifically so it stays safe if a future file legitimately has `//`
  inside a string.

### Validation

Compiled clean against all 23 sources in the project (20 previous + the 3 new mob
files). Staged the real 35-file `stalkermobs/configs/` folder from this machine and
ran the CLI against it directly. Checks:
- Load count: 35 (matches the folder's real file count).
- `byMobName("tushkan")` returns exactly 3 configs (`base_tick`, `base_tushkan`,
  `base_tushkan_clone`); `byMobName("boar")` returns exactly 2 (`base_boar`,
  `base_boar_clone`) -- confirmed both via the Java registry and independently via a
  standalone Python scan of the same 35 files reading `mobName` directly, matching
  exactly.
- All 35 `configId`s are non-null and unique (a small verification program iterated
  every loaded config and checked for null/duplicate ids -- none found).
- `base_puppet.smm`'s comment-stripped parse cross-checked against an independent
  Python regex-strip-then-`json.loads()` of the same raw file: both agree on
  `mobName="puppet"`, 2 attacks, 11 sounds, and the exact relation table (16 NEUTRAL
  entries, one ALLY entry for `tushkan`, zero ENEMY entries) -- the CLI's own printed
  output for `base_puppet` correctly showed "enemies: (none)" / "allies: tushkan",
  matching that independently-parsed table field for field.

### Other recon done this session that came up thin (documented so it isn't re-tried blind)

- `party/weapon_levels.cfg` is a real file but nearly empty: 12 bytes, one entry,
  `25000=NEWBIE` -- looks like an XP-threshold-to-rank-name table with only one tier
  actually present in this build. Not enough to build anything from; noted in case a
  later build ships more tiers.
- `auction/`, `trade/` in modassets are UI textures/fonts only -- no structural data,
  as expected (auction/trade state is necessarily server-side).
- `shop/models/caseny24/` texture names reveal a real seasonal loot-case history
  (`case_autumn24`, `case_winter25`, `case_spring25`, `case_twitch`, `case_host`,
  `case_zivcas`) -- cosmetic/monetization content, not gameplay-tunable, noted but not
  pursued further per the user's stated focus on game content over monetization
  internals.
- `probuilder/` confirms the in-game hideout/base-building tool is Unity's ProBuilder
  asset (door/hammer/spraycan icons) -- consistent with the voxel-based
  hideout-building system already inferred from part 15's `.mdat` work, not new
  information on its own.
- Checked the local player's own `config/quests.cfg` (`{"readQuests": [16 ids],
  "hiddenQuests": [1 id]}`, confirming the plain-JSON schema already known from an
  earlier part) against `itemnames.dat` in case any quest ids resolved to lore text
  the way some item ids do (part 4) -- **15 of 16 not found, 1 coincidental hit** (item
  id 8252 = "Стальной ящик"/"Steel Box", unrelated to any quest -- item ids and quest
  ids are different, non-overlapping numeric spaces, as expected, not linked). Honest
  negative result: this local file's 16-quest sample doesn't reveal a wider quest
  roster or any leads toward cut quest content. Real quest names/text remain locked in
  the encrypted `strings.dat`/jars, same blocker as everywhere else lore-related.

### What this unblocks / what's still not done

Mutant AI behavior is now a loadable, moddable data layer alongside world geometry
(part 18) and weapon damage (part 19) -- all three built ahead of having a game loop
to call them. Still no combat-stat data for mutants; still no networking/game-loop
code that constructs any of these three registries. `spawnRateMultiplier` on
`ServerConfig` remains unwired -- these config files don't carry spawn-rate data
either (spawn placement/frequency is presumably a separate, likely server-authoritative
system not exposed in any client file found so far).

## Part 21 -- ItemWeightTable: real per-item carry-weight data found and loaded, plus two thin leads ruled out

Continuing after part 20 with no specific new user direction ("keep going"). Went looking for
whatever real, structured, moddable gameplay data was still sitting unexplored in `modassets`,
specifically anything that could round out the project's "full control over ... spawn rates or
damage values" goal the way `WeaponRegistry` (part 19) did for damage.

### Searched first, found `stalker/weight.txt`

Checked `gameobjects`, `customnpcs`, `stalker`, `stalkerclans` for any `.cfg`/`.json`/`.txt`/`.smm`
files (excluding the already-catalogued mob configs). Found three real files: `stalker/weight.txt`
(481 lines), `customnpcs/sorting_map.txt` (inventory UI sort-order groupings -- client-UI-only,
not server-relevant, not pursued further), and `stalker/detectors.txt` (a custom brace-syntax
config defining Geiger-counter-style anomaly-detector beep timing/pitch by distance -- real and
interesting, but lower gameplay impact than weight data; noted for a future pass, not built this
time). `weight.txt` was the clear pick: a real, dense (415 unique entries), directly server-
tunable per-item inventory weight table.

### Format, confirmed by direct inspection before writing a parser

`file` + a hex dump confirmed CRLF line endings (Windows-style) -- an initial `grep -E` sanity
check on the device's Linux shell falsely flagged well-formed lines as "non-matching" until this
was found; not a data problem, a line-ending problem, and the Java parser strips `\r` explicitly
because of it. Structure: one `//default max weight without bonuses` comment, one
`character_max-50` line (the default carry-weight cap), then `<itemId>-<weight>` entries grouped
under `//`-commented section headers (`//melee`, `//food`, `//armor`, `//other`, `//med`,
`//items`, `//ammo` -- an unlabeled first section before `//melee` is presumably weapons, matching
its ~212-entry size). `itemId` is the client's INTERNAL numeric id space (confirmed via
`item_lookup.py` against `itemnames.dat`: ids `16075`/`16347` both resolve to the same lore string
"Записка Куркуме"/"Kurkume's Note" -- real, valid ids in this space, just mostly uncovered by
`itemnames.dat`'s ~1148-item lore-text subset).

### Real data quirk found and disclosed, not silently resolved

3 of the 418 data lines repeat an item id with a genuinely DIFFERENT weight: `16001` (0.01, then
later 0.1 -- 10x difference), `16075` (10, then later 1 -- 10x difference), `16079` (1.2, then
later 1). Checked the section context of each: all three first appearances are in the unlabeled
weapons-presumed section, all three second appearances are in `//items`. Consistent with this
project's earlier-established fact that the internal numeric item id is only unique WITHIN a
category (`itemnames.dat`'s `internal.<categoryId>.<itemId>.<variant>` key format) -- `weight.txt`
has no category field, so these 3 ids are genuinely ambiguous in the source data itself, not a
parsing bug. `items/ItemWeightTable.java` does not silently pick a "correct" value:
`weightOf(id)` returns the LAST value in file order (the simplest defensible default for an
override-style flat config), but `conflictingIds()`/`allValuesOf(id)` surface every id with more
than one distinct value so a caller -- or a mod author retuning this table -- isn't misled into
trusting a number that the source file itself doesn't agree on.

### Validation

Compiled clean against all 25 sources in the project. Staged the real `weight.txt` from this
machine and ran the CLI directly against it: loaded 415 unique entries, `character_max=50.0`,
and found exactly the 3 conflicting ids above with exactly the values expected. Cross-checked
independently in Python (a from-scratch line scan, no shared code with the Java parser): 418 data
lines, 415 unique ids -- exactly matching the Java load count and confirming the 3-id conflict
count arithmetically (418 - 415 = 3). Spot-checked a handful of specific lookups (16340→0.4,
16346→0.01, 16347→25.0, an id with weight exactly 0 → 0.0 correctly distinguished from "not
found", and a made-up id → correctly reports not found rather than a wrong default).

### What this unblocks / what's still not done

A fourth real, validated, moddable data layer alongside world geometry (part 18), weapon damage
(part 19), and mob behavior (part 20) -- all built ahead of having a game loop to call them.
Nothing in the emulator wires this into `ServerConfig` yet (there's no `weightMultiplier` field,
and no inventory/carry-weight system to apply one to). `stalker/detectors.txt` (anomaly-detector
beep timing) and `customnpcs/sorting_map.txt` (inventory UI sort order) remain unbuilt leads from
this same pass, noted above so they aren't rediscovered from scratch later.

## Part 22 -- DetectorConfig: real anomaly-detector beep/pitch behavior parsed, confirmed against its own internal logic

Continuing after part 21 ("keep going", no new user direction). Picked up the one concrete
unbuilt lead noted at the end of part 21: `stalker/detectors.txt`, a real config controlling the
Geiger-counter-style proximity detector for radiation ("rad"), chemical ("thr"), and biological
("bio") hazard fields -- classic S.T.A.L.K.E.R.-series detector behavior (faster beeping / higher
pitch the closer you get to a hazard).

### Format, confirmed byte-for-byte before writing a parser

`file`/hex dump showed something no other file in this project has needed yet: **UTF-8 with a
BOM** (`EF BB BF`) on top of the already-familiar CRLF line endings. A first-pass block-counting
grep undercounted by exactly 1 (`rad{` appeared to occur only 5 times instead of 6) until the BOM
was found to be silently prefixing the very first line's `rad{`, breaking a `^[a-z]+\{` anchor
match for that one block only -- confirmed via `head -c 5 | xxd` showing `ef bb bf 72 61` (BOM +
"ra..."). Real block count once the BOM is accounted for: 16 total (`grep -c '^}$'` = 16),
6 `rad` + 5 `thr` + 5 `bio`. Structure: flat, non-nested `<type>{ key: value; ... }` blocks --
not JSON (bare unquoted keys, `: value;` instead of `"key": value,`, blocks not wrapped in an
array) -- so this needed its own small hand-written scanner rather than reusing `JsonReader`.
Fields, confirmed exhaustively via `grep -oE '^\s*[a-z_]+:'`: `start_distance`, `sound_names`
(a quoted string, `@`-separated, e.g. `"geiger_1_@geiger_2_@...@"`, or a single bare word like
`"chemical"`), `period_base`, `period_random`, and `pitch_min`/`pitch_max` (present only on `bio`
blocks -- confirmed no `rad`/`thr` block has them).

### Built and self-validated against the data's own internal logic

`hazards/DetectorTier.java` (one parsed block) and `hazards/DetectorConfig.java`
(`DetectorConfig.load(path)`, groups tiers by type sorted by `startDistance` descending,
`tierFor(type, distance)` resolves the applicable tier -- the largest `startDistance` still
&lt;= the given distance). `hazards/DetectorConfigDumpTool.java` is the CLI. Rather than just
checking the loader against itself, checked whether the parsed data actually behaves the way a
detector should: for `rad`, `period_base` goes 12, 8, 5, 3, 2, 1 seconds as `start_distance` goes
5, 4, 3, 2, 1, 0 -- strictly decreasing, i.e. **closer really does mean faster beeping in the
source file's own numbers**, not just something inferred from the block being named "rad" and the
sounds being named "geiger_N". This is a stronger validation than a structural parse check alone:
it confirms the parser extracted the right VALUES into the right FIELDS, not just valid-looking
JSON-adjacent syntax.

### Validation

Compiled clean against all 28 sources in the project. Staged the real `detectors.txt` from this
machine and ran the CLI directly: loaded 16 tiers, 3 types, printed every tier's fields correctly
(pitch fields present only under `bio`, sound-name lists correctly split on `@` with the trailing
empty segment dropped), and `tierFor("rad", 3.5)` correctly resolved to the `start_distance=3`
tier (not `4` or `2`) -- boundary-correct nearest-tier selection. Cross-checked independently in
Python (a separate regex-based block scan, `encoding='utf-8-sig'` to handle the BOM automatically
rather than the Java code's explicit `﻿` strip): 16 blocks, 6/5/5 split, and independently
re-derived the same rad `period_base` monotonic-decrease fact -- matching the Java output and the
claim in `DetectorTier`'s own doc comment exactly.

### What this unblocks / what's still not done

A fifth real, validated, data-driven layer (world geometry, weapon damage, mob behavior, item
weight, now detector-beep behavior), still built ahead of having a game loop or player-position
tracking to call `tierFor` from. `customnpcs/sorting_map.txt` (inventory UI sort-order groupings)
remains the one unbuilt, lower-priority lead noted in part 21 -- likely client-UI-only rather than
server-relevant, still not built.

## Part 23 -- MovementConfig (real player-movement tuning) and ItemGroups (real item categorization), plus two thin/closed leads

Continuing after part 22 ("keep going", no new user direction). Closed out the two remaining
leads noted at the end of part 21/22 before searching for new ones.

### `customnpcs/sorting_map.txt` and `stalkerclans/battlefields/` -- both checked, both thin, neither built

`sorting_map.txt` (41 lines, real data) is a comma-separated ordered list of internal numeric
item ids per line -- confirmed it's the client's inventory-grid sort/group order, cross-checking
a sample of ids against `itemnames.dat` (5/30 resolved to real quest/key items, consistent with
that file's known partial coverage). It carries no gameplay-affecting numeric value (no weight,
no rate, no stat) -- purely a UI-ordering hint -- so, per the user's stated focus on core game
content over UI/cosmetic mechanics, it's documented but not built into a registry.

`stalkerclans/battlefields/` (153 files: icons for 14 named siege locations -- bridges, hospital,
jail, kishka, kolizei, kordon, krot, moryatnik, peklo, pripyat, reich, svalka, td, yama --
`br_root`/`br_spawn`/`br_transport` battle-royale models+textures, a "fight" 1v1 wagered-duel
arena set matching part 11's `fight_equipment`/`fight_map`/`fight_bet` flags, clan
flags/turrets/chests) confirms multiple real PvP modes exist, but is 100% mesh/texture/sound/
icon assets -- zero structured/numeric config, same "UI/asset only" pattern already seen in
part 20's `auction`/`trade` check. Nothing to parse; documented so it isn't re-checked blind.

### Systematic sweep of the remaining un-checked namespaces turned up two real, substantial finds

Searched every top-level `modassets/assets/*` namespace not yet individually checked for
structured data (`_jurisdiction`, `anomalies`, `bundle`, `chat`, `currency`, `customitems`,
`effects`, `ejection`, `gameobjects`, `gloomycore`, `handcuffs`, `highpoly`, `misc`, `ragdolls`,
`skinarmor`, `stalcraft`, `stalkerguide`, `stalkerplayer`, `stalkersounds`, `vegetation`) for any
`.txt`/`.cfg`/`.json`/`.dat`/`.smm` files. Found several: `anomalies/morse_alphabet.txt` (81
lines, a real Cyrillic+Latin Morse code table -- likely backs an in-game radio/puzzle mechanic,
genuinely "an activity" per the user's stated interest, but small and not chased further this
pass), `gloomycore/item_tiers.txt` (4 lines: `novice`/`stalker`/`veteran`/`master` -- a real but
tiny rarity-tier name list, no numeric data attached), `gloomycore/mcmtlList.txt` (10,222-line
manifest of `.mcmtl` paths -- resolves part 15's ".mcmtl doesn't exist" finding: the extension is
used as an internal material-reference naming convention/manifest even though no standalone
`.mcmtl` files ship on disk, not a contradiction), `bundle/item_groups_barter.txt` (0 bytes,
real empty file), `bundle/logged_trader_item_groups.txt` (9 lines, just a list of trader
category names to log), and `bundle/levels/br_location.txt` (12 bytes, literally just the word
"battleroyale" -- confirms a `br_` level/mode entry exists, nothing more). All six noted, none
built -- too small or too thin to justify a parser class.

Two, however, were real and substantial enough to build:

### `stalcraft/moving.txt` -- built and validated: `movement/MovementConfig.java`, `MovementSetting.java`

A genuine player-movement tuning config: walk/run/sprint/sneak/crawl speed factors, jump
control factors per jump type (stand/sneak/walk/run/sprint/charge/climb/wall), climbing (free
climb, ladders, vines, ceilings) speed and angle tolerances, fall-damage thresholds, swim/dive
speed, and a full stamina ("exhaustion") + hunger gain/loss model with per-action rates. This is
squarely in-scope core-gameplay tunable data, the same category as `damageMultiplier` --
exactly the kind of knob the project exists to expose.

**Format confirmed by direct inspection first**: plain UTF-8, LF line endings, no BOM (`head -c 8
| xxd` -- starts directly with `move.spe`, no stray bytes). 241 total lines: 238 real
`key:value` settings, plus 3 comment lines (genuine Russian developer comments, `# ...`) each
immediately followed by one `key=value` line using `=` instead of `:` -- a small, deliberate
late-file syntax switch, not a parsing error (confirmed via `grep -n ':.*=\|=.*:'` finding zero
lines that mix both separators).

**Real "variant override" quirk, confirmed not guessed at**: 23 of the 238 keys have a value
shaped like `base;LETTER:override;LETTER:override...`, e.g. `move.speed.user:false;c:true`. The
4 real letters found -- `c`, `e`, `h`, `m` -- are exactly the ones the file's own
`move.config.survival.keys`/`creative.keys`/`adventure.keys` lines declare, and
`move.config.key.name:e:Easy;h:Hard;m:Medium` spells out their display names directly: `c` =
Creative mode, `e`/`h` = Easy/Hard survival difficulty, `m` = Medium difficulty (doubling as the
Adventure-mode letter). One key, `move.config.key.name` itself, has ONLY variant pairs and no
base value at all -- `MovementSetting.baseValue()` returns null for that one case rather than
inventing a base value that doesn't exist in the source.

**Validation methodology**: wrote an independent Python re-implementation of the exact parsing
rule (find first `:` or `=` as the key/value separator; split the value on `;`; a token
containing `:` at position 1 is a variant pair, the first plain token is the base value, an
empty base is a real value not a missing one) and ran it against the real file blind, before
looking at the Java output. Zero parse errors on all 238 keys. Then dumped every key/base/variant
triple from both the Java loader and the Python re-implementation to TSV and diffed field by
field: **exact match, 0 mismatches, 238/238 keys**. Re-ran the Java CLI directly against the
file freshly re-staged from the real device path (not a locally retyped copy) -- identical
238-setting, 4-letter-variant result, confirming the earlier hand-transcribed test copy used
during development was byte-identical to the real file (diff showed only a trailing-newline
difference, md5 content otherwise matching line-for-line).

### `bundle/item_groups.txt` -- built and validated: `items/ItemGroups.java`

A real category map for the internal numeric item id space (the same space as `itemnames.dat`,
`weight.txt`, and `sorting_map.txt`): 41 groups (`weapons`=20000-20999, `melee_weapons`, `armor`,
`ammo`, `artifacts`, `medicine`, `lootboxes`/`lootbox_keys`, `clan_stuff`, `barter`, `crates`,
`skins`, and more), each a comma-separated list of inclusive id ranges (`a-b`) and/or single ids.

**Real, disclosed data quirk, not a bug**: 11 item ids belong to more than one group (e.g. id
16613 is in both `lootboxes` and `lootbox_keys` -- a lootbox key is itself adjacent to the
lootbox category; several `weapons`/`upgrades`/`customitems_stuff`/`donate_protectors` ids also
appear a second time in `barter`). Consistent with `barter` being a curated cross-cutting list of
specifically tradeable items layered on the primary categories rather than a disjoint category of
its own. `groupsOf(itemId)` returns every matching group rather than silently picking one --
same "disclose, don't hide" approach as `ItemWeightTable.conflictingIds()` from part 21.

**Cross-validated against a second, independently-found real file**: `customnpcs/sorting_map.txt`
(part 21)'s first line groups ids 16600-16607 and 16610 together -- every one of those falls
inside `item_groups.txt`'s own `crates` group (16600-16611). Two files found in unrelated passes
of this project agree on the same category boundary without either referencing the other --
real corroborating evidence, not coincidence.

**Validation methodology**: same double-check pattern as `MovementConfig` -- an independent
Python parse (expand each range/single-id token into a Python set, collect group membership per
id) run blind against the real file, cross-checked field-by-field against the Java loader's TSV
dump: 41/41 groups, exact range-list match, 0 mismatches. The Python script also independently
found and printed the 11 multi-group ids, which the Java `groupsOf()` calls for those exact ids
(16613, 16505, 20180, and others) reproduced exactly. Re-ran directly against the real file
freshly staged from the device (not the local transcription used mid-development) -- identical
41-group result; diff against the transcription showed only the expected trailing-newline
difference.

### Not yet done

Nothing calls into either `MovementConfig` or `ItemGroups` yet -- no player-movement simulation,
no inventory/loot system to categorize items for. `anomalies/morse_alphabet.txt` remains a real,
confirmed, unbuilt lead (small, likely backs a radio/puzzle activity, lower priority than the two
built this part). The six other namespace-sweep finds noted above (`item_tiers.txt`,
`mcmtlList.txt`, both `bundle` trader-list/barter files, `br_location.txt`) are documented and
deliberately not built -- each too thin (a handful of names, or literally one word) to justify a
parser class. Six real, validated, data-driven layers now exist (world geometry, weapon damage,
mob behavior, item weight, detector-beep behavior, and now movement tuning + item categorization
as of this part), still built ahead of having a game loop or player-position tracking to wire any
of them into.

## Part 24 -- MorseAlphabet: the real in-game Morse code table, parsed and validated

Continuing after part 23 ("keep going", no new user direction). Picked up the one concrete
unbuilt lead noted at the end of part 23: `anomalies/morse_alphabet.txt`, flagged then as small
but genuinely "an activity" per the user's stated interest in game content over background
internals -- almost certainly backs an in-game radio/signal-decoding puzzle (the `anomalies`
namespace strongly suggests it's tied to the anomaly/hazard-field content this project has
already validated in part 22's `DetectorConfig`).

### Format confirmed by direct inspection first

`file`/hex dump: UTF-8, CRLF line endings, no BOM. Every one of 82 real lines has the exact same
shape -- one character, then exactly 4 literal space bytes (confirmed via hex dump, not a tab),
then a Morse code string. No header, no comments, no blank lines. 82 entries breaks down as 33
Cyrillic letters (the full alphabet plus `ё`), 26 Latin letters, 10 digits, and 13 punctuation
marks (`. , : ; ( ) ' " - / ? ! @`).

### Real, disclosed quirk -- not a parsing bug

Character-to-code is a clean 1:1 map (checked: zero duplicate character keys). The REVERSE
direction is genuinely ambiguous, though: 27 of the Morse codes are shared by more than one
character. Almost all of these are Cyrillic/Latin pairs that trace back to the same real-world
telegraphic code (Cyrillic `а` and Latin `a` are both `.-`; Cyrillic `ц` and Latin `c` are both
`-.-.`; and so on for 25 more pairs) -- which makes sense, since Russian Morse ("Кодовая азбука")
was historically built to reuse the Latin code assignments for cognate sounds. One code, a
single dot (`.`), is shared by THREE characters: Cyrillic `е`, Cyrillic `ё`, and Latin `e` --
genuinely ambiguous, not decodable to a single character without an alphabet-context hint the
file itself doesn't provide. Punctuation has one collision too: `(` and `)` share `-.--.-`, which
matches the real-world ITU Morse convention (there's no separate closing-parenthesis prosign).
`MorseAlphabet.charsFor(code)` returns every match rather than silently picking one -- the same
"disclose an ambiguity, don't hide it" approach as `ItemWeightTable.conflictingIds()` (part 21)
and `ItemGroups.groupsOf()` (part 23).

### Validation

An independent Python parse (split each line on 4 literal spaces, cross-check against the raw
line for any shape that doesn't match) found the same 82 entries, the same 0 duplicate
characters, and the same 27 ambiguous codes -- including the 3-way `.`/`е`/`ё`/`e` collision and
the `(`/`)` pair -- before the Java class was written, so the disclosed-quirk doc comment was
written from ground truth, not from Java output. Ran the Java CLI directly against the real
file re-staged fresh from the device: 82 characters loaded, 27 ambiguous codes exactly matching
the Python list, and `encode("SOS")` correctly produced `... --- ...` -- the universally-known
SOS pattern, a meaningful sanity check beyond "it didn't throw."

Built `anomalies/MorseAlphabet.java` (`MorseAlphabet.load(path)`, `codeOf(char)`,
`charsFor(code)`, `ambiguousCodes()`, `encode(text)`) and
`anomalies/MorseAlphabetDumpTool.java` (CLI). Compiles clean at 35 total sources.

### Not yet done

Nothing calls into `MorseAlphabet` yet -- no radio/signal minigame exists in the emulator, and
the real client-side UX for this activity (how a player actually encounters/uses Morse in-game)
is still unconfirmed; this is a read/query layer only, built ahead of any consuming feature, the
same status as every other data-layer class in this project. This closes out every concrete
unbuilt lead identified through part 23 -- the systematic namespace sweep in that part found no
other real, substantial, un-investigated structured-data file. Eight real, validated,
data-driven layers now exist (world geometry, weapon damage, mob behavior, item weight,
detector-beep behavior, movement tuning, item categorization, and now Morse code), still built
ahead of having a game loop or player state to wire any of them into.

## Part 25 -- SoundDurations, plus a broader extension/namespace sweep turning up min_food.txt and confirming several namespaces are cosmetic-only

Continuing after part 24 ("keep going until you need my input or hit 90% usage" -- explicit
standing authorization from the user to keep self-directing without waiting for a per-part
"keep going"). Part 23's sweep only checked `.txt`/`.cfg`/`.json`/`.dat`/`.smm`; broadened it to
`.xml`/`.yml`/`.yaml`/`.ini`/`.properties`/`.csv`/`.conf`/`.list`/`.tsv` across every namespace,
and did a direct top-level listing of a few namespaces (`stalker`, `anomalies`, `customitems`,
`gameobjects`) to check for files missed by extension alone.

### New real file found: `stalker/min_food.txt`

A companion to part 23's `moving.txt` (which has a full hunger/exhaustion gain-loss model but no
"minimum food" threshold field). Real, but tiny: the entire file is the bare scalar `10`, no key,
no newline. Documented as a fact (the client's minimum-food constant is 10) rather than built
into a class -- a single bare number doesn't warrant a loader; if a future hunger/exhaustion
system needs it, it belongs as a plain constant next to wherever `MovementConfig`'s hunger
fields get consumed.

### Everything else the broader extension sweep found is cosmetic, confirmed and set aside

All `.properties` files (~130, under `gloomycore/sky/*` and `stalcraft/ctmpatcher/ctm/*`) are sky
render-state configs and CTM (connected-texture-mod-style) block-face variant configs -- pure
rendering, no gameplay values, consistent with this project's now-repeated finding that
`gloomycore` and `stalcraft`'s non-`moving.txt` content is rendering/tooling, not tunable data.
`highpoly/lipsync/config/_default.yml` is a lipsync tuning file for facial animation -- cosmetic.
`stalker/stream/ravent/desktop.ini` is a Windows folder-view metadata file, not game data at all.
Direct listings of `anomalies/` (models/sound/textures/trace only -- no config files beyond the
already-built `morse_alphabet.txt`, though the `sound/morse`, `sound/random_radio`,
`sound/signal_noise`, `sound/scanner` subfolders confirm the Morse table really does back a
radio/signal activity, and `models/{kisel,zastoy,node,signal_point,extractor}` are real
S.T.A.L.K.E.R.-series anomaly-type names -- "kisel"/jelly, "zastoy"/stagnation -- with mesh/
texture/sound only, no stat data, consistent with part 20's finding that no anomaly-damage data
exists anywhere found so far), `customitems/` (just one real top-level txt, `empty.txt`, which
is exactly what its name says -- confirmed empty), and `gameobjects/` (models/textures/trace
only; the model folder names -- `br_ammo`, `br_armor1lvl`/`br_armor2lvl`, `br_med`, `br_build`,
`br_other`, `br_weapon1lvl`/`br_weapon2lvl`, `quest_placement`, `switches` -- confirm a real
battle-royale loot-tier system with at least 2 armor/weapon rarity tiers, corroborating part 23's
`stalkerclans/battlefields/br_*` finds, but again mesh/texture only, no numeric loot-table data)
all turned up nothing further to build.

### `bundle/sound_duration.csv` and `bundle/speech_duration.csv` -- built and validated: `audio/SoundDurations.java`

Two real, clean CSVs (`<resourceKey>,<durationMs>`, no header): `sound_duration.csv` (1,481
unique keys -- sound effects, ambience, the 15-clip in-game guitar minigame, faction radio
chatter, and English battle-royale announcer lines) and `speech_duration.csv` (7,447 unique
keys -- named NPC dialogue lines, e.g. `steven.sounds.npc.14 new npc.zarya.01_common.idle...`).
Confirmed by direct inspection: plain UTF-8, no BOM, LF endings, every line matches
`<key>,<digits>` (checked programmatically, 0 malformed lines across both files), and the two
files' key spaces are fully disjoint (0 overlap) -- genuinely independent registries.

**Real, disclosed quirk, not a bug**: 15 keys in `sound_duration.csv` (`guitar_1` through
`guitar_15`) each appear TWICE in the raw file. Checked whether this was a real conflict before
writing any handling for it: all 15 duplicate pairs carry the EXACT SAME duration value both
times -- a harmless redundancy, not an ambiguity. `SoundDurations.load()` tolerates an
exact-duplicate re-declaration silently but throws if a key is ever re-declared with a genuinely
different value (none exist in the real data, but the check exists so a future data update that
introduces a real conflict gets surfaced rather than silently overwritten -- same standard as
`ItemWeightTable`/`ItemGroups`/`MorseAlphabet`).

**New content surfaced, not previously catalogued**: scanning the real key names in
`sound_duration.csv` turned up `audio_diary_operation_lesta_intervention` /
`_main_caliber` / `_mine` / `_rainbow` / `_water_area` -- five audio-diary parts for a named
mission, "Lesta", that hasn't appeared anywhere else in this project's research (part 11 only
found "Kutuzov"). Also found `en_landing01` through at least `en_landing15` and
`en_airdrop01`-`en_airdrop05` plus `en_evacuation`/`en_boss`/`en_final` -- English-language
battle-royale match announcer lines, corroborating the `br_*` loot-tier and `stalkerclans/
battlefields/br_*` findings from parts 23 and this part with a THIRD independent signal (audio
asset names) that the battle-royale mode has real, substantial structure (numbered landing
zones, staged airdrops, an evacuation phase).

**Validation**: an independent Python parse (regex-match every line, count unique keys,
cross-check the two files for key overlap, and specifically diff the 15 duplicate `guitar_N`
pairs' values) run before writing the Java class found 1,481 unique sound keys, 7,447 unique
speech keys, 0 cross-file overlap, and all 15 duplicates non-conflicting. Ran the Java CLI
directly against both real files freshly staged from the device: identical counts (1,481 / 7,447),
correctly surfaced all 5 `operation_lesta` keys with their exact durations, and correctly
returned all 7 `guitar_1*` keys (`guitar_1`, `guitar_10`-`guitar_15`) via substring search with
no duplicate-key exception thrown, confirming the exact-duplicate tolerance path works against
real data, not just a synthetic test.

Built `audio/SoundDurations.java` (`SoundDurations.load(path)`, `durationMs(key)`,
`keysContaining(substring)`) and `audio/SoundDurationsDumpTool.java` (CLI). Compiles clean at 37
total sources.

### Not yet done

Nothing calls into `SoundDurations` yet -- it's asset-timing metadata, not itself a "control"
knob, built as a read layer the same as everything else in this project. `min_food.txt`'s value
(10) is documented but not wired anywhere. The battle-royale mode's real structure (numbered
landing zones, airdrops, loot tiers) is now corroborated by three independent asset-name
signals across three separate parts (23, this part) but still has zero numeric/behavioral config
data anywhere found -- if BR support is ever wanted, its actual rules would need a different
evidence source (there is none known yet). Nine real, validated, data-driven layers now exist
(world geometry, weapon damage, mob behavior, item weight, detector-beep behavior, movement
tuning, item categorization, Morse code, and now sound-duration metadata), still built ahead of
having a game loop or player state to wire any of them into.

## Part 26 (2026-09-01) -- three unswept sessions surface a real map roster jump (30 -> 46 zones), first named DMZ zones, and resolve the map-new_bar quirk

Per the user's standing authorization ("go on until you actually need my input/help or when 90%
of usage has been used"), continued self-directed work after part 25 closed out the previously-known
lead list. Checked the STALZONE install's `logs/` folder for anything new rather than re-treading
already-analyzed ground, and found three client-log sessions produced since part 14's 14h-log
analysis and never looked at: `client-11.log` (2026-08-31 10:51-13:10, ~2h20m), `client-12.log`
(2026-08-31 19:42-21:35+, ends mid-Vivox-reconnect, likely a crash or forced close -- not
investigated further), and `client-13.log` (2026-09-01 10:13-, today's live session, still actively
growing at time of writing). Combined ~366MB, none previously read.

**WARN/ERROR message-shape sweep** (regex-normalized, deduped, counted) across all three found
nothing pointing at a new decodable content lead in the obfuscated-internals sense -- the two most
frequent categories are client engine noise, not game content: `Exceeded ent occ query limit`
(28,894x, an entity-occlusion-culling rate limiter) and `Exception spawning game object ... NullPointerException`
(2,872x across two chunk-parity variants, full stack trace 100% obfuscated numeric-class/keyword-method
names -- the same de-facto-unreadable obfuscation pattern noted since part 9/14, correctly left
alone per the project's boundary against decompiling internals). A `Failed to parse config stalcraft:`
Gson `MalformedJsonException` fires exactly 3x at the start of every one of the three sessions,
identical message/line/column every time -- a real, reproducible client-side config-parse warning,
but with an empty path (`stalcraft:` with nothing after the colon) that names no actual file, so
there's nothing here to chase further; recorded as a confirmed harmless startup quirk.

**The one substantial new finding: map_cache/5.0/'s real zone roster grew from 30 to 46 folders**
since part 15's catalog, each of the 16 new ones independently confirmed to hold real `.mdat`
region data (not empty placeholders) via a direct `find -iname '*.mdat' | wc -l` check per folder
(2-9 files each): `armsklad`, `dungeon_DMZ1_barons_safehouse`, `dungeon_DMZ1_base_merc`,
`dungeon_DMZ1_jivchik_fight`, `dungeon_chimera`, `dungeon_koshmar`, `dungeon_perestrelka`,
`dungeon_polygon7_0`, `dungeon_school`, `dungeon_spusk`, `dungeon_tonnelradar`, `mg`, `nbolota`,
`outpost_merc`, `pd`, `td`.
- **First concrete, named evidence for the "dmz" mode.** Part 11 found only an unconfirmed
  `enable_broyale_warning`-style flag (`config/options.json`'s `dmz` HUD-atlas key) with zero
  named locations behind it. The three `dungeon_DMZ1_*` folders are the first real, cached,
  named zones for it -- `DMZ1` reads as a mode/map-set namespace (like `br_` for battle royale),
  not a location name, with `barons_safehouse`, `base_merc`, and `jivchik_fight` as the actual
  named locations inside it. Corroborates the DMZ/extraction-mode hypothesis with real data for
  the first time, though (same as every other newly-found mode) zero numeric/behavioral config
  was found alongside it.
- **`map-new_bar`'s repeated `FileNotFoundException` (41+39 occurrences across the new sessions,
  `pda:map-new_bar/r.X.Z.ol`) is now resolved, not just noted.** `MilestoneMap.java`'s own existing
  comment already recorded that this file used to call the Bar zone `"new_bar"` before its part-15
  roster correction against real `map_cache` folder names (which use plain `"bar"`). Checked the
  log context directly: the client's PDA-minimap-texture loader is still internally requesting
  tiles under the old `"new_bar"` codename, while the actual world-cache system (and every other
  place this project has found the zone referenced) uses `"bar"`. No `map_cache` folder named
  `new_bar` exists. This is a real, disclosed, harmless internal naming mismatch inside the client
  itself (minimap-texture code vs. world-cache code disagree on one zone's internal name) -- not a
  hidden or cut zone, and not something a private server needs to work around beyond knowing the
  `.ol` 404s are expected noise. A second, unrelated 404 pattern, `pda:global_map/r.X.Z.ol` (~90x),
  was also checked and is the client requesting global-overview minimap tiles that likewise never
  shipped -- same category of harmless client-side asset gap, no roster or content implication.
- **`nbolota` is real, separately-cached data** -- a third Bolota-family zone alongside
  `dungeon_bolota_tutorial`/`dungeon_bolota_tutorial_2`, not the same thing as the `map-new_bar`
  quirk above. Not investigated further (no human-readable name or purpose surfaced).
- **Three short codenames -- `mg`, `pd`, `td` -- are confirmed real (non-empty `.mdat` data) but
  could not be tied to a human-readable name or purpose.** Grepped each for a literal string match
  across all three new session logs: none of the three folder names appear verbatim in the log text
  (unlike `armsklad`, which appears once, as a game-object collider name `armsklad_node`, confirming
  that zone is real content and not just a leftover cache folder). Recorded honestly as
  confirmed-real-but-unidentified rather than guessed at -- a future pass could try correlating
  `map_cache` region-file mtimes against session timestamps more precisely, or check for these
  codes inside `config/quests.cfg` or other per-account save data, neither tried yet.
- **`MilestoneMap.java` updated** to the real 46-entry roster (was 30, part 15), in the same
  filesystem-sort order the real `map_cache/5.0/` directory listing itself produced (not manually
  re-sorted) so the list stays an honest transcription of real data rather than an edited one.
  Compiles clean at 37 total sources. `MILESTONE_1_MAP`/`MILESTONE_1_FALLBACK` unchanged --
  `dungeon_bolota_tutorial`/`bar` remain the right pick for the reasons already recorded in part 15.

**Not yet done**: `client-12.log`'s abrupt mid-Vivox-reconnect ending (possible crash) not
investigated -- would need the matching `watchdog-12.log` cross-referenced for a crash signature,
not attempted this pass. The three unidentified short-codename zones (`mg`/`pd`/`td`) remain open.
No new structured-data (config/table) file was found this pass -- this was a log-analysis sweep,
not a `modassets` namespace sweep (those were exhausted as of part 25). Full detail above; project
memory (`recon_findings.md`) updated to match.

## Part 27 (2026-09-01) -- VendorArchive: a real trading-history save file, surfacing two new named locations and the first evidence of multiple currencies

Continued self-directed per the standing authorization. Following up on part 26's `mg`/`pd`/`td` dead end, checked `config/` for anything not yet catalogued rather than guessing further at those three codes, and found `config/vendor_archive.dat` -- a file already named, but never loaded, in `nbt/CompressionUtil.java`'s own doc comment as a real zstd-magic-bytes example. Confirmed it's the same zstd+NBT format family as every other local save file this project has parsed, decoded it with the existing `tools/nbt_dump.py` first (read-only, no new code needed to see the shape), then built a proper Java loader.

**Real structure**: root compound -> `archives` (one per character on the account) -> `username` + `traders` (one per trader this character has bought from) -> `traderId` + `items` -> `stack` (`id`/`Count_i`/`Damage`/optional `tag`) + `currency` + `price`. Built `economy/VendorPurchase.java` (typed record wrapper) + `economy/VendorArchive.java` (`VendorArchive.load(path)`, `usernames()`, `archiveFor(username)`, `traderIds()`, `currencies()`, `pricesPaidFor(itemId)`) + `economy/VendorArchiveDumpTool.java` (CLI).

- **New real content surfaced by this file alone**: this account has 3 characters -- `AveghiliaBoy`, `WolfZero`, `Buji` (this resolved an early false alarm: their config-folder names first looked like they might belong to other Windows users on a shared PC, but `config/AveghiliaBoy/` etc. turned out to be per-character save-slot subfolders under this account's own `config/`, not other people's data). 12 distinct real trader ids across them: `buyer_foton2`/`supplier_foton2`, `buyer_lodochnaya`/`supplier_lodochnaya`, `buyer_merc`/`supplier_merc`, `buyer_rostok`/`supplier_rostok`, `arsenal_assistant_trader`, `compensation_trader`, `trader_seasonal`, `trader_starievshik`.
- **Two new named locations, not found anywhere else in this project**: `lodochnaya` (Russian for "boathouse"/"boat station") and `rostok` (a real named location from the original S.T.A.L.K.E.R. series). Neither matches any of the 46 real zones in `map_cache/5.0/` as of part 26 -- not investigated further this pass (could be trader-only location names not tied to an explorable cached zone, or zones this account simply hasn't visited/cached yet). `foton2` was already known (map_cache roster, part 15); `merc` plausibly ties to the existing `base_merc`/`outpost_merc` zones, not confirmed.
- **First direct evidence of more than one currency**: `money` (2,574/2,603 real purchases), `sc_birthday_21` (23, a birthday-event currency) and `season_pass_ticket_3` (6, a battle-pass currency). Every previous economy-adjacent finding in this project (weapon prices via stalcraftdb.net/stalzone.wiki, part 12/17) only ever showed plain money values.
- **Real, disclosed tag-schema quirk**: the first few purchase entries checked only showed `upgrade_bonus`/`u1`/`u2` inside a stack's optional `tag` compound, but a full sweep of all 877 real tag blocks in this account's archive also found `repairs_amount`, `currentVisualDefault`, `currentMotifDefault` (11/11/4 occurrences, always `0` in this data) and `stash_durability` (11 occurrences, real varying values: 180/420/900). `u1`/`u2` (paired, reads as a UUID split the way Minecraft NBT conventionally stores one -- not confirmed as the client's own name for it) are present in 877/877 tag blocks whenever a tag exists at all; `upgrade_bonus` is present in only 494/877, genuinely optional even within a tag, and every value found is exactly `0.0` -- disclosed honestly as inconclusive (could mean no purchased item in this dataset ever had a nonzero upgrade applied at purchase time, or that the value changes later during gameplay, not at purchase). Because the real schema is this varied, `VendorPurchase.rawTag()` exposes the whole parsed compound instead of the class inventing a fixed field set for a schema it hasn't fully seen.
- **Prices vary per purchase, not a fixed catalog value**: item id 11352 (spot-checked) shows 5 different real prices across 5 purchases (22,800 / 25,080 / 27,360 / 28,500 / 31,919) -- this file is genuine historical evidence of prices paid, not an authoritative price list; no canonical vendor price/stock config has been found anywhere in this project. 188 of 2,603 real purchases were price `0` -- not investigated further this pass, plausibly `compensation_trader`/event-unlock freebies.
- **Validated**: independent Python regex parse of the raw `nbt_dump.py` text output (before the Java class was written) found 2,603 total purchase records, 3 usernames, 3 currencies, 825 unique item ids, prices ranging 0-360,000, upgrade_bonus always exactly `0.0` when present. Java CLI re-run against the real staged file: identical 2,603 records, identical usernames/currencies/trader-id set, and the same 5-price history for item 11352 -- exact match. Compiles clean at 40 total sources.
- **Not yet done**: `lodochnaya`/`rostok` not cross-checked against map_cache or the client logs for a matching explorable zone. The 188 price-0 purchases not broken down by trader (which would likely explain them). Nothing in the emulator's (nonexistent) game loop constructs a `VendorArchive` or uses `pricesPaidFor` for anything. This does not resolve part 26's `mg`/`pd`/`td` zone-codename mystery -- none of the 12 trader ids found here match those three codes. Full detail above; project memory (`recon_findings.md`) updated to match.

## Part 28 (2026-09-01) -- SettlementRegistry: a new "settlement" namespace, resolving lodochnaya/rostok, and closing out two old open items

Picked up three of part 27's "not yet done" items in one pass, using the fact that the mounted STALZONE install had produced fresh logs since part 27 (client-13.log, a session starting 2026-09-01 10:13 -- not present during part 26's log sweep).

**1. Cross-checked `lodochnaya`/`rostok` (part 27's new named locations from `vendor_archive.dat`'s trader ids) against the fresh logs.** `grep -i` across `logs/*.log` found both in every client log, always inside the same block: `[Game Startup/INFO] Reading settlement <name>`. The full block (client-13.log, 15 lines, real source order): `csky`, `lodochnaya`, `garage`, `foton2`, `savezone_td`, `savezone_agro`, `rostok`, `bar_save_residential_hangar`, `bar_sever`, `merc`, `duty`, `freedom`, `covenant`, `north_factions_block`, `battlefields`. This is a real, distinct "settlement" namespace, not an alias for `map_cache`'s 46-zone roster -- only 2 of the 15 names (`foton2`, `bar_save_residential_hangar`) match a `MilestoneMap.ALL_KNOWN_MAPS` entry verbatim. Built `world/SettlementRegistry.java` (`ALL_KNOWN_SETTLEMENTS`, same style as `MilestoneMap`) to record this roster. No further per-settlement detail (owning faction, territory bounds, coordinates) was found anywhere in `config/` or client-log text -- this is a name list only, disclosed as such in the class doc rather than guessed at.
- Informed-but-unconfirmed reading, disclosed as inference in the class doc: `duty`/`freedom` are canonical S.T.A.L.K.E.R.-trilogy faction names; `csky` plausibly abbreviates the third canonical faction, "Clear Sky". `covenant`/`north_factions_block`/`battlefields` don't match any original-trilogy name known to this project and may be STALZONE-original.
- `savezone_td` and `savezone_agro` follow the same `savezone_*` pattern as map_cache's own `savezone_agroprom` (confirming `agro` = shorthand for `agroprom`) -- suggestive, not conclusive, evidence that part 26's unidentified map_cache code `td` is a real actively-used zone with its own save-zone. `mg`/`pd` have no equivalent settlement entry here and remain fully unidentified. The `mg`/`pd`/`td` mystery itself is NOT resolved by this part -- only `td` gained a little circumstantial support.

**2. Broke down `vendor_archive.dat`'s 188 price-0 purchases by trader** (part 27 left this unbroken-down, guessing they might cluster under `compensation_trader`/`trader_seasonal`). Wrote a small scratch program against the existing `VendorArchive` class: all 188 price-0 purchases come from exactly one trader, `supplier_merc` (188 of that trader's 304 total purchases, 61.8%) -- zero from any other of the 12 trader ids, including the originally-guessed `compensation_trader`/`trader_seasonal`. Independently cross-validated with a from-scratch Python NBT+zstd reader written for this check alone (not reusing `tools/nbt_dump.py` or any prior script) -- exact match on total purchase count (2,603), usernames, currencies, and the price-0/`supplier_merc` finding. Real quirk, not explained: why `supplier_merc` logs price 0 for the majority of its purchases is not known from evidence available (repair/quest-reward transactions logged through the same schema is a plausible guess, not confirmed) -- disclosed as open rather than assumed.

**3. Cross-checked `client-12.log`'s abrupt mid-session ending against `watchdog-12.log`** (part 25/26/27 all left this open). Not a crash: `watchdog-12.log`'s only freeze warnings (two ~500ms game-thread stalls, obfuscated stack traces) are timestamped 19:46:49/19:46:58 on 2026-08-31, hours before `client-12.log`'s actual last lines at 21:35:00, which show completely normal asset loading (mob textures, ragdoll models) followed by a VIVOX voice-chat reconnect message -- no exception, no error, no abnormal-termination marker anywhere near the real cutoff. The log simply rotates: `client-13.log` picks up the next session about 13 hours later (2026-09-01 10:13). Closed out as "normal log-rotation boundary, not a crash" rather than left open.

**Validation**: `SettlementRegistry.ALL_KNOWN_SETTLEMENTS` cross-checked byte-for-byte against a fresh `grep -i "Reading settlement"` of `client-13.log` (15/15 exact match, source order preserved). Price-0-by-trader breakdown independently re-derived via two separate code paths (existing Java `VendorArchive` class vs. a fresh from-scratch Python NBT reader) with identical results. Compiles clean at 41 total sources (was 40; `world/SettlementRegistry.java` is the only new file this part).

**Not yet done**: `mg`/`pd`/`td` still not identified by name (only `td` gained circumstantial support). `csky`="Clear Sky" and the other faction-name readings are informed guesses, not confirmed from any STALZONE source. No per-settlement data (ownership, bounds, spawn points) found -- if this matters for spawn/faction logic later, it isn't in any file this project has found yet. `supplier_merc`'s price-0 pattern is disclosed but not explained.

## Part 29 (2026-09-01) -- GameLoop: the first code that actually wires the data layers together (local simulation, not networked)

Every part since 18 has ended its "Not yet done" note the same way: a real, validated data layer, with nothing in the emulator constructing or calling into it. Nine-plus of them had piled up (`WorldModel`, `WeaponRegistry`, `MobRegistry`, `ItemWeightTable`, `DetectorConfig`, `MovementConfig`, `ItemGroups`, `MorseAlphabet`, `SoundDurations`) with zero game-loop code. This part closes that gap -- as far as it honestly can without the real wire protocol, which is still encrypted and stays behind the project's standing go-ahead gate.

**Scope, stated plainly**: this is a LOCAL simulation loop, not a networked one. It does not speak STALZONE's real protocol and doesn't claim to. What it does do: actually construct `WorldModel`, `MovementConfig`, `DetectorConfig`, `ItemWeightTable`, and `MobRegistry` together and drive one simulated player through them tick by tick. New code: `session/PlayerState.java` (position + zone + inventory, this emulator's own state-holding design, not a protocol claim), `session/Inventory.java` (real weight enforcement via `ItemWeightTable`), `movement/MovementSimulator.java` (turns `MovementConfig`'s real speed-factor keys into an actual blocks/second number), `mobs/SpawnScheduler.java` (finally wires `ServerConfig.spawnRateMultiplier` into behavior), `session/GameLoop.java` (the orchestrator) and `session/GameLoopDemo.java` (CLI demo, same pattern as every other `*DumpTool`).

- **`MovementSimulator` keeps real data and this emulator's own choices clearly separate.** Every factor it multiplies (`move.speed.factor`, `move.run.factor`, `move.sprint.factor`, `move.sneak.factor`, `move.crawl.factor`) is a real value from `moving.txt` (part 23) -- genuine relative speed ratios. But `moving.txt` never states an ABSOLUTE base speed in blocks/second anywhere -- only ratios. So the base walk speed (4.3 blocks/s) is an explicit, disclosed emulator placeholder, not a claim about STALZONE's real speed. Real, disclosed quirk found while building this: `move.run` (the boolean gating "run" mode) is `false` with no per-variant override for any of the 4 real mode/difficulty letters -- i.e. "run" is a real key with real dead data; only WALK/SPRINT/SNEAK/CRAWL are active in this config.
- **`SpawnScheduler` is explicit that only the mob roster is real, not a spawn table.** `MobRegistry` (part 20) is real and validated; a real per-zone spawn table (which mobs spawn where, how often) has never been found anywhere in this project across parts 11/20/23/25/26. So the scheduler picks uniformly at random from the full 35-mob roster on a fixed-mean interval scaled by `spawnRateMultiplier` -- disclosed as this emulator's own generic design, exactly the "full control over ... spawn rates" knob the project exists to provide, not a reverse-engineered claim.
- **Fixed a stale doc comment while here**: `ServerConfig.java`'s class doc said `damageMultiplier`/`spawnRateMultiplier` "aren't wired to any actual gameplay logic yet" -- true when written, false since part 19 (`damageMultiplier` was wired into `WeaponRegistry.effectiveDamage`) and now also false for `spawnRateMultiplier` as of this part. Corrected to name exactly what each field does and doesn't reach; `lootRateMultiplier` remains honestly unwired (no loot-table system exists).
- **Ran the CLI demo against real staged data, not a fixture**: Milestone-1's real `dungeon_bolota_tutorial` region files (4 files/790 chunks, same data parts 15/16/18 validated), the real `moving.txt`/`detectors.txt`/`weight.txt`, and all 35 real `.smm` mob configs. Player starts in a real loaded chunk (picked from `WorldModel.loadedChunkCoords()`, not an arbitrary/possibly-unloaded coordinate). Sample real output: walk speed resolved to 4.1 blocks/s (`4.3 * move.speed.factor(0.95)`); a demo inventory of 2x real item 16340 (weight 0.4) + 3x real item 16347 (weight 25) correctly totalled 75.8 and correctly flagged OVERWEIGHT against the real 50.0 cap; a demo "rad" hazard correctly resolved through 3 different real detector tiers as distance grew (0.6 blocks -> startDistance=0/period_base=1s; 4.6 blocks -> startDistance=4/period_base=8s; 8.7+ blocks -> startDistance=5/period_base=12s, the real 12s "rad" ceiling tier); a mob spawn (`flesh`, from the real roster) fired at tick 10 with `spawnRateMultiplier=3.0` (30s base mean / 3.0 = 10s, deterministic given fixed interval math).
- **Validated**: every one of the numbers above was independently re-derived in a from-scratch Python script (base speed arithmetic, weight sum, spawn-interval arithmetic, and the real rad-tier startDistance/period_base table from part 22's own findings) BEFORE trusting the Java CLI's output -- exact match on all of them. Compiles clean at 48 total sources (was 41; 7 new files, 1 modified).
- **Real, disclosed side effect of composing `WorldModel` with a moving player**: in a cave/dungeon zone, "highest solid block in this column" (part 18's own query) is sometimes a cave ceiling/overhang rather than a walkable floor, so the demo player's Y jumps around between ticks near real overhangs -- not a bug in this part's new code, an existing, already-disclosed `WorldModel` limitation (no floor-vs-ceiling concept) now visible for the first time because something finally calls it every tick.
- **Not yet done**: nothing here touches the real network layer -- `network/UdpListener` still just logs raw datagrams, completely separate from this simulation loop. No health/damage-taken model exists (mutant combat stats still have no evidence source anywhere, part 20) -- `WeaponRegistry.effectiveDamage` isn't called from `GameLoop` yet. No real anomaly/hazard PLACEMENT data exists anywhere in this project (only the detector's real response-to-distance behavior) -- the demo hazard's coordinates are necessarily a demo scenario. `ItemGroups`/`MorseAlphabet`/`SoundDurations`/`VendorArchive`/`SettlementRegistry` still aren't reached from `GameLoop` either. Full writeup above; project memory (`recon_findings.md`) updated to match.

## Part 30 (2026-09-01) -- Combat: real weapon damage finally reaches a spawned mob, plus a new real data-format quirk in weapons.json

Part 29's own "not yet done" note named this directly: "No health/damage-taken model exists ... `WeaponRegistry.effectiveDamage` isn't called from `GameLoop` yet." This part closes that gap, and along the way found real fields in `weapons.json` that no earlier part's `WeaponEntry` accessors exposed at all.

**The core problem or wiring "damage values" was never the weapon side -- `WeaponRegistry` has been real, validated, evidenced data since part 19.** It's the mob side: all 35 real `.smm` mob config files, re-confirmed here, have NO health/HP field anywhere in their schema (part 10, re-checked part 20, re-checked again while building this). So applying a real damage number to a mob needs *some* notion of a health pool to subtract it from, and nothing in this project's evidence supplies one. New class `mobs/MobInstance.java` makes this explicit rather than hiding it: it pairs a real `MobConfig` with a health pool using `DEFAULT_MAX_HEALTH = 100.0` -- an arbitrary, flat, disclosed emulator placeholder, not reverse-engineered data. `GameLoop` now tracks every spawned mob as a `MobInstance` in `aliveMobs()` (previously `tickSpawns` just reported a spawn event and forgot about it) and removes one once its health hits zero.

**`GameLoop.tickCombat(player, weapon, target, location, sink)`** resolves one attack: firearms use `WeaponRegistry.effectiveDamage` (or the new headshot/limb variants, see below); melee weapons use the real "common" (light-attack) range with the exact midpoint taken (disclosed simplification -- no evidence for whether/how the real game randomizes within the range, or for using "common" over "strong"). No range/falloff model is applied to firearm damage -- see the falloff-curve finding below for why.

**New real data found while wiring this, not previously exposed by `WeaponEntry`**: enumerating every key across all 337 `weapons.json` records (not just the ones `WeaponEntry` already reads) turned up five fields with zero existing accessor -- `headshotMultiplier` (275/337 records), `limbMultiplier` (275/337, always co-occurs with `headshotMultiplier` -- checked, zero mismatched presence), `damageFalloff` (268/337, a `{startDamage, damageDecreaseStart, endDamage, damageDecreaseEnd, maxDistance}` object), and four melee-only fields present on all 54 melee records (`meleeReachCommon/Strong`, `meleePiercing`, `meleeBloodlustChancePct`, `meleeCriticalHitMultiplier`, `meleeMutantDamageMultiplier`). All now have typed accessors on `WeaponEntry`.

**Real, disclosed data-format quirk**: `headshotMultiplier`, `limbMultiplier`, `overheatAfterShots`, `meleeCriticalHitMultiplier`, and `meleeMutantDamageMultiplier` are consistently encoded as JSON STRINGS across every one of the 337 records that has them (e.g. `"headshotMultiplier": "1.4"`), not JSON numbers -- confirmed by checking the Python type of every occurrence of each field name across the whole file, not a single sample. Every other numeric field in the dataset, including all five `damageFalloff` sub-fields, is a genuine JSON number. `WeaponEntry` handles this with a small `stringAsDoubleField()` helper alongside the existing `doubleField()` -- same "typed accessor, honest null on anything unparseable" pattern the class already used, just unwrapping one extra layer for these five fields.

**`damageFalloff` is exposed raw, not interpolated.** The five sub-fields describe a real damage-vs-distance curve (full damage out to `damageDecreaseStart`, degrading to `endDamage` by `damageDecreaseEnd`, `maxDistance` as the outer range), but nothing in this dataset states the shape of the degradation between those two distances -- linear is the obvious guess, but it is a guess, and per this project's rule 1 (never turn an assumption into a spec) `WeaponEntry.damageFalloff()` returns the five real numbers and stops there. `GameLoop.tickCombat` does not use it. This stays an honest "not yet done," not an invented formula.

**`HitLocation` (`BODY`/`HEAD`/`LIMB`) added to `tickCombat`.** `HEAD`/`LIMB` apply the real `headshotMultiplier`/`limbMultiplier` on top of `effectiveDamage` via two new `WeaponRegistry` methods (`effectiveHeadshotDamage`/`effectiveLimbDamage`) when the weapon record has the field (falls back to flat damage for the 62/337 records that don't); `location` has no effect on melee (nothing in this dataset ties melee damage to hit location, only to attack type). Which location a shot hits is entirely the caller's decision -- there is no aim/hit-detection model here, evidenced or otherwise.

**Ran the CLI demo against real staged data**: `dungeon_bolota_tutorial` (same real zone as part 29), all 35 real mob configs, all 337 real `weapons.json` records, demo weapon `AK-12` (real: `damage=37.5`, `headshotMultiplier="1.4"`, `limbMultiplier="0.8"`). Demo policy (disclosed, not real data): the first hit on a fresh spawn is a headshot, every hit after is a body shot. Real sample run: a `flesh` mob spawns at tick 10 with 100/100 hp (the placeholder pool); headshot for 52.5 (`37.5 * 1.4`) leaves it at 47.5/100; body shot for 37.5 leaves 10.0/100; final body shot for the remaining 10.0 (correctly clamped, not overshooting) kills it -- `Mobs killed: 1, still alive: 0`.

**Validated independently in Python before trusting the Java output**: re-derived the real AK-12 `damage=37.5` and `headshotMultiplier="1.4"`/`limbMultiplier="0.8"` values directly from `weapons.json`, confirmed `37.5 * 1.4 = 52.5` and `37.5 * 0.8 = 30.0` match the Java CLI's numbers exactly; independently re-counted the field-type histogram across all 337 records confirming the string-vs-number split (5 always-string fields, all others numeric); independently re-confirmed `275`/`337`/`62` and the zero-mismatch `headshotMultiplier`/`limbMultiplier` co-occurrence claim. Compiles clean at 48 total sources (1 new file `mobs/MobInstance.java`, 4 modified: `items/WeaponEntry.java`, `items/WeaponRegistry.java`, `session/GameLoop.java`, `session/GameLoopDemo.java`).

**Follow-up, same day**: `meleeMutantDamageMultiplier` turned out to be honestly wireable after all, on reflection -- every `tickCombat` target is a `MobInstance` wrapping a real `stalkermobs` config, i.e. a mutant by this dataset's own definition, so "this hit is against a mutant" isn't a guessed context, it's the only one that exists in this emulator. Melee attacks now multiply the light-attack midpoint by the real `meleeMutantDamageMultiplier` when present. Verified against real data: Hunters Knife (31-38 common range, midpoint 34.5) vs. a `flesh` mob with the real multiplier `x2.35` -> 81.075, correctly rounds to the CLI's printed 81.1 -- independently re-derived in Python before trusting the Java output. `meleeCriticalHitMultiplier` stays unwired -- unlike the mutant multiplier, applying it would require inventing WHEN a hit "crits", and this dataset has no crit-chance field anywhere, so that stays a genuine, disclosed gap rather than a forced fit.

**Not yet done**: mob health remains an emulator placeholder -- no real evidence for per-mob-type toughness exists anywhere. `damageFalloff`'s interpolation shape is unconfirmed and unimplemented (disclosed above, not guessed). `meleeCriticalHitMultiplier` is exposed as a typed accessor but not yet wired into `tickCombat` (no crit-chance field exists to trigger it -- see follow-up above). No real anomaly/hazard placement data exists anywhere (unchanged from part 29). `ItemGroups`/`MorseAlphabet`/`SoundDurations`/`VendorArchive`/`SettlementRegistry` still aren't reached from `GameLoop` -- on reflection, none of them has an equally clean, evidenced single-context fit the way `meleeMutantDamageMultiplier` did, so forcing them in risks inventing gameplay logic rather than exposing real data; left honestly unwired rather than force-fit. The real network layer remains completely untouched -- `network/UdpListener` still just logs raw datagrams, and the real wire protocol stays encrypted and behind the project's standing go-ahead gate.

**Second follow-up, same day**: `items/WeaponRegistryDumpTool.java` (part 19's CLI) is updated to print every real field part 30 found -- headshot/limb multipliers with the effective (multiplier-applied) number, the raw `damageFalloff` curve, and the melee-only extras (reach, piercing, bloodlust chance, crit and vs.-mutant multipliers) -- so all of part 30's new real data is inspectable standalone, not only inside a `GameLoop` demo run. Re-run and verified against real data: AK-12 (headshot 37.5x1.4=52.5, limb 37.5x0.8=30.0, falloff 37.5dmg/18m -> 29.9dmg/72m/160m max, matches part 19's own falloff numbers from the stalcraftdb.net API response found in part 12), PM (same fields, different real numbers), Hunters Knife (reach 2.1/2.1, piercing 13.75, bloodlust 50%, crit x1.7, vs.-mutant x2.35 -- all match part 30's numbers exactly).

## CONFIRMED (part 31, 2026-09-01) -- old rotated logs (client-0 through client-9) analyzed for the first time: settlement roster stable since July, one real dated quirk found, no confirmed cut content

Per the user's original stated interest in "possible cut content," and since `client-0.log` through `client-9.log` (the STALZONE install's `logs/` folder, device path) had never been analyzed by any prior part despite being noted as an open item since part 26, swept them now. These are all much smaller than `client-10.log`+ (1.5MB-34MB vs. 87MB-668MB) and span 2026-07-07 (`client-0.log`, the oldest available session, nearly 2 months before the most recently analyzed logs) through 2026-08-26 (`client-9.log`).

**1. The 15-name real "settlement" roster (part 28) is confirmed stable all the way back to the oldest available log.** `grep -c "Reading settlement"` returns exactly 15 in all 10 old logs (client-0 through client-9), and `client-0.log`'s own 15 names, in order, are byte-for-byte identical to part 28's `client-13.log`-derived roster. No addition, removal, or reordering in nearly 2 months of history -- a real negative result (this namespace has NOT changed), not itself new content, but it forecloses "maybe an older log has a 16th settlement that got removed" as a lead.

**2. A real, previously-uncatalogued "phonograph" audio category found, but it stays a thin, undevelopable lead.** `client-1.log` (2026-08-06) startup: `[Game Startup/WARN] [AUDIO] Routing for channel group PHONOGRAPH is not configured`, and every `options.json` dump in every old log carries a dedicated `"phonograph_volume_":50.0` field, sitting alongside the already-known `music_volume_`/`radio_volume_`/`detectors_volume_`/`annotator_volume_` sliders (real, distinct audio-source categories, part 22-25's territory). This is genuinely new -- "phonograph" had not surfaced anywhere in this project before. But it stays undevelopable: checked and confirmed ZERO matches for "phonograph" in `bundle/sound_duration.csv`/`speech_duration.csv` (part 25, 8,928 keys total), `data/weapons/weapons.json`, or any other file this project has already staged/catalogued -- no asset, no item, no duration key, nothing to build a class around. Documented as a fact only, same standard as `min_food.txt` (part 25) -- a single real settings field, not a structured dataset.

**3. A real, dated config-lookup quirk: `weapons:anims/hedge.anm.json` was looked up 37 times in `client-0.log` (2026-07-07) and is completely absent (0 occurrences) from every one of client-1 through client-9 (2026-08-06 through 2026-08-26) and from the already-analyzed client-10 through client-13.** Cross-checked against a stable control case from the same `client-0.log` WARN sweep -- `customitems:anims/quest/` (11 occurrences) -- which appears exactly 11 times in EVERY log checked across the full July-to-September range (client-0, client-1, client-9, client-13), confirming this isn't just "some config warnings vary session to session" noise; `hedge.anm`'s clean 37-to-0 transition between July 7 and August 6 is a real, dated change. Checked whether "hedge" corresponds to any known weapon: not in the 337-record `weapons.json` (no id or name containing "hedge"), not among the 256 real weapon-model folder names under `weapons/models/weapons/` (part 10's roster). The entire `weapons/anims/` directory (the path this warning was trying to read from) does not exist anywhere in the CURRENT install. **Disclosed as an open, inconclusive signal, not confirmed cut content**: this could mean a weapon or animation-config system named "hedge" existed and was removed between July and August (the strongest form of "cut content" this project could hope to find), or it could just as easily be a renamed/relocated animation-config path with no gameplay content ever actually attached to "hedge" -- there is no model, texture, item id, or stats record anywhere for it, so nothing beyond "this exact string stopped being referenced" is evidenced. Recorded honestly as open rather than guessed at either way.

**Follow-up, same day -- this lead is weaker than initially framed.** Re-examined the `hedge.anm.json` warning in context (`grep -B2 -A1 "hedge.anm" client-0.log`): it appears immediately adjacent to a `weapons:anims/grenade.anm` lookup, in the same log block, right after "Found 163 additional loot configs" / "Random loots parsed in 106 ms" -- i.e. inside what looks like a generic per-item/per-category animation-lookup loop, not a lookup singled out for one specific weapon. Checked whether `grenade.anm` shows the same July->August disappearance: `grep -c "grenade.anm" client-0.log` returns 1, and the same string returns 0 in `client-1.log`, `client-9.log`, and `client-13.log` -- the exact same boundary as `hedge.anm.json`. Since grenades are unambiguously real, still-existing content (not a plausible "cut" system), this pairing weakens the "a weapon/system named hedge was removed" reading in favor of "a shared animation-lookup/pipeline mechanism changed between these two versions" -- most likely unrelated to specific content removal, rather than evidence of it. This is a refinement, not a resolution: there is still no direct evidence for what changed in the loading code, or for what (if anything) "hedge" ever named, so the finding stays open rather than closed either way.

**Not yet done**: this sweep covered WARN/ERROR message-shape diversity in `client-0.log` only in real depth (the oldest, most likely to differ); client-1 through client-9 were checked only for the two specific strings above (settlement count, hedge.anm/quest-anim control case), not with the same full histogram pass. A full WARN/ERROR message-shape sweep of client-1 through client-9 (same methodology as part 26) remains a possible future angle if the "cut content" search continues, though repeated device-shell timeouts on broad recursive searches in this session suggest any further pass should stay narrowly scoped (single-file greps with explicit string targets, not open-ended `find`/`grep -r` across the full `modassets` tree). **`*_old` backup config files, opened this part**: `config/options.json_old`, `prefs_old`, `vendor_archive.dat_old`, `waypoints.cfg_old`, `cluster_old` are a mundane rolling one-generation save backup, NOT an older/historical snapshot -- `cmp` shows `options.json`/`cluster` byte-identical to their `_old` twin (no change since the last save), while `prefs`/`vendor_archive.dat`/`waypoints.cfg` differ by a few bytes near the start of the file from their `_old` twin (consistent with one incremental field, e.g. one more vendor purchase, changing between the previous save and the current one). This resolves the lead noted in earlier parts as "never opened" -- there is no deeper/older data hiding behind these files, just the game's own crash-safety backup-before-overwrite pattern.

## Part 32 (2026-09-01) -- Real vehicle content (cars/boats/helicopters/WWII aircraft) discovered under `customitems`, plus a full WARN/ERROR sweep of client-1 through client-9 turns up a distinct NPC combat-voice taxonomy and a named tutorial dialogue system

Continuing the still-open "Not yet done" item from part 31: ran the full WARN/ERROR message-shape sweep across `client-1.log` through `client-9.log` (the `[SE]` obfuscated-stack-trace frames -- part of the anti-cheat/launcher internals this project has no interest in per the user's standing "not the EXENS" instruction -- were excluded from the sweep up front). 1,076-6,339 non-`[SE]` WARN/ERROR lines per file, 198 unique message shapes total after normalizing numbers/hex/quoted substrings. Two genuinely new, evidenced leads came out of this sweep:

**1. A real vehicle-model system exists under `modassets/assets/customitems/models/blocks/vehicles/`, confirmed on disk (not just referenced) with three top-level categories: `cars/`, `boats/`, `heli/`.** This was found by chasing a single WARN line -- `Can not load effect data at effects:particles/warmup/uh60_warmup_fly.efkefc: java.io.FileNotFoundException` (client-9.log) -- which turned out to be one missing VFX asset sitting on top of a large, real, actively-used vehicle-model tree (confirmed the `effects/particles/warmup/` directory itself does not exist on disk, same "genuinely gone, not just unlucky" pattern as part 31's `hedge.anm`/`grenade.anm` finding -- but here the underlying content is NOT gone, only one particle effect is). `grep -c "uh60"` across all 14 client logs shows this is not a one-off: 24 hits in client-9.log, 148 in client-10.log, 14 in client-11.log, and the INFO-level lines in client-10.log show `customitems:models/blocks/vehicles/heli/warmup/uh-60/uh60_Normal.ol` loading successfully and repeatedly (`"was loaded (async=true) in NN ms"`) -- the model is real, working content; only its accompanying warmup-flight particle effect is missing.
  - **Cars** (`vehicles/cars/`), 5 subcategories, all real Soviet/Russian road and off-road vehicles: `cargo_cars/` (MAZ-515, ZIL-131, a ZIL-131 firetruck variant, plus a separate `quest_zil/` folder -- see below), `light_cars/` (Moskvich 2136, Moskvich 2140, Niva, UAZ-452, UAZ-469, VAZ-2106, ZAZ-968), `military/` (BTR-70, BRDM, T-72, and a `batm/` folder -- likely the real BAT-M engineering vehicle), `other/` (a dragline excavator, DT-75 and T-40 tractors, LAZ and PAZ buses), and `toy/` (a kid's pedal-car model in 4 paint variants, default/rust x 4 colors).
  - **Boats** (`vehicles/boats/`): Aist, a wooden boat (`boat_wood`/`boat_wooden_karkas`), Kazanka (a/b variants, one further tessellated), and Saturn (rusty and not-rusty variants, each with 2 LOD levels) -- all real Soviet-era boat models/brands, consistent with the project's already-documented STALKER-lore-callback naming pattern (part 26's `map_cache` zone survey).
  - **Helicopters** (`vehicles/heli/`): a large shared texture set (14-22MB `BODY_BaseColor`/`BODY_Normal`/interior atlases) backing multiple Mi-8 states -- `mi8_dek_broken`, `mi8_dek_crashed`, `mi8_dek_full`, `mi8_dek_open` (each with 3 LOD levels plus a `.mcvd` file, likely collision/vertex data) -- reading as environmental wreck/prop variants of one downed helicopter, consistent with the genre's "crashed chopper" landmark trope. A `crashed_parts/` subfolder and an `mi6/` subfolder (Mi-6: `mi_6`, `mi_6_closed`, `mi_6_closed_2`, `mi_6_scrapped`) extend the same wreck-prop pattern to a second real helicopter type.
  - **`mi8_attak_placeholder.mcsb`** -- this filename is a self-disclosed, dev-labeled placeholder (an "attack" Mi-8 variant that is explicitly not a finished asset), the strongest kind of "unfinished/planned content" evidence this project can have: the developers' own naming says so, nothing is inferred.
  - **The `heli/warmup/` subfolder is the real surprise**: alongside `mi_8_warmup`/`mi8_warmup_lod1-3` and `UH-60_warmup`/`UH-60_warmup_lod1-3` (the model the original log warning was trying to load an effect for), it also contains `mi24a/` (a full Mi-24 "Hind" gunship model + separate glass material, `mi24a_warmup.mcsb` + 3 LODs), `il2/` (a WWII Soviet Il-2 Sturmovik model, `il2_warmup.mcsb`), and `ju87/` (a WWII German Junkers Ju-87 "Stuka" dive bomber, `ju87_warmup.mcsb`, textures literally named `Junkers_87_AO.ol`/`Junkers_87_N.ol`). Grouping a modern Black Hawk, a modern Hind, and two WWII-era aircraft together under one `warmup` folder is disclosed as an observation, not a guessed conclusion: the most likely honest reading is a loading-screen/intro "warmup" flyover animation reel that cycles through several unrelated aircraft models for visual variety, but this project has no confirmed evidence of what `warmup` actually triggers in-game (no script, no UI hook, nothing beyond the file names and the one log line that references the UH-60 half of it) -- recorded as real, on-disk, working 3D content whose exact use is not yet evidenced.
  - **`cars/cargo_cars/quest_zil/`** contains `zil131_quest.mcsb` and a separate `zil131_engine_quest.mcsb` -- a distinct quest-specific ZIL-131 truck model with its own separate engine-part model, real evidence of a "get this truck running" style quest object (consistent with the genre's classic "fix the vehicle" quest trope), though no quest script, dialogue, or objective text has been located for it yet -- disclosed as a real prop only, not a described quest.
  - **What is NOT evidenced**: whether any of this is player-drivable, purely environmental set-dressing, or both. Nothing found this part confirms a drivable-vehicle system (no input bindings, no vehicle-seat/entry code, no vehicle physics config) -- only that a large, real, on-disk 3D-model vehicle roster exists and that at least the UH-60 half of the `warmup` set is actively loaded during real play sessions. Left open rather than assumed.

**2. The same sweep also confirms a real per-faction NPC combat-voice-line taxonomy and an entirely new tutorial dialogue system, extending part-anywhere's thin `bandits`/`meet_hide_weapon` lead into real depth.** `customnpcs:music/steven/sounds/npc/` (plus one outlier path, `customnpcs:music/steven/bandit/fight/attack/`) holds per-faction subtrees with named combat-AI voice states, confirmed across client-1 through client-9 (`grep -o` + dedup, 15 unique state paths): `bandits` (`idle`, `fight_idle_fire`, `meet_hide_weapon` -- the player-hides-weapon reaction line noted as a lead elsewhere in this file), a separately-named `bandits_2(chihachev)` with its own `idle` lines, `mercenary` (`idle`, `attack`, `fight_idle_cover`, `fight_idle_fire`, `fight_idle_flank`, `fight_idle_reloading`, `med_request`), and `monolit` (Monolith, the classic STALKER faction: `fight_idle_spoted`, `after_fight_relax`, `kill`). One path -- `customnpcs:music/steven/sounds/npc/14 new npc/mercenary/01 common/fight_idle_reloading/` -- carries an internal dev-folder name ("14 new npc") suggesting a numbered batch of NPC-voice additions during development; noted as a real naming artifact, not further investigated. This meaningfully deepens (not just re-confirms) the AI-behavior-state picture: distinct `fight_idle_*` sub-states (cover/fire/flank/reloading/spotted) plus faction-specific one-offs (`meet_hide_weapon`, `med_request`, `after_fight_relax`) are real, named, evidenced combat-AI states across at least 4 NPC factions/subtypes.

**A separate, entirely new lead: a named tutorial dialogue system, `tutorial_3`.** `client-9.log` (2026-08-26, a `dungeon_bolota_tutorial` session -- this project's own Milestone-1 zone) and `client-10.log` (245 occurrences, confirming this is a real recurring system and not a one-session fluke) both reference FMOD events under `event:/tutorial_3/all_persons/`, each paired with a "Failed to load lipsync payload" + "Failed to find fmod event" WARN pair (i.e. these are real dialogue lines the client tries and fails to play, not lines this project invented). 22 unique event ids resolve to 4 named characters: **belyash** (`belyash_1_1..1_4`, `belyash_obshhee_1_0` ["general"], `belyash_voprosy_1_1..1_3` ["questions"], `belyash_vozvrashhenie_iz_vopro` ["return from questions"]), **zob** (`zob_1_0..1_3`, `zob_1_2_obshhee`), **varvara** (`varvara_1_1..1_3`, `varvara_obshhee_1_0`), and a fourth referred to two different ways -- `dialog_s_lipoj_2_0/2_2/2_3` ("dialogue with Lipa") and `zaglushka_lipa_2_3_posle_obysk` ("Lipa stub/placeholder, after search/frisk"). The word "zaglushka" (placeholder/stub) is the developers' own label, same self-disclosure pattern as `mi8_attak_placeholder` above -- this is honestly a WIP/stand-in line, not a guess on this project's part. `posle_obysk` ("after being searched/frisked") is the first evidence anywhere in this project of a search/frisk beat in the tutorial's dialogue flow. Recorded as real, named tutorial content; no further story/quest-text content has been located for any of the four names.

**Not yet done**: the matching `netcode-0` through `netcode-9` and `watchdog-0` through `watchdog-9` logs (same session set as client-0 through client-9) remain completely unopened -- next natural step if this line of research continues. The vehicle system's actual gameplay hook (drivable vs. purely environmental) and the `tutorial_3` dialogue's full script/quest-text are both open threads with no further evidence gathered this part.

**Follow-up, same day -- netcode-0..9/watchdog-0..9 (plus the newer netcode-10..12/watchdog-10..13) opened and swept in full.** All are small (0-9,072 lines each). `netcode-*.log` holds a real, literal shot/movement server-validation reason-code vocabulary: `Shot processed correctly` (18,401x -- the success ack) and rejections `Shot rejected (dead)`, `Shot rejected (invalid_recoil)`, `Shot rejected (no_weapon)`, `Shot rejected (movement_desync)`, `Shot rejected (reloading)`, `Shot rejected (can_not_shoot)`, `Shot outcome skipped (dead_before_shot_outcome)`, `Movement desync (reason: position_mismatch)`, `Movement desync (reason: too_fast)`. This is gunplay/lag-compensation validation (the server checking a claimed shot against the shooter's own state: dead, unarmed, reloading, recoil-pattern-consistent, movement-consistent) -- in scope per the user's standing focus rule, distinct from the EXENS anti-cheat product. `watchdog-*.log` turned out to be entirely freeze/hang-detector thread dumps and obfuscated stack traces (same `[SE]`-style obfuscation as the already-excluded client-log anti-cheat frames) plus opaque `GMV: <hex>` session-identifier lines -- background/launcher-internal noise, correctly out of scope, left un-catalogued. This closes the item.

## Part 33 (2026-09-01) -- Vehicle drivability question resolved, strongly: real player testimony ("I never seen a vehicle in game") plus four independent evidence threads all point the same way -- environmental set-dressing/wreck props, not a drivable-vehicle system

Prompted directly by the user, who plays the real game and said they've never encountered a vehicle in it -- itself a real, first-hand data point worth recording alongside the file evidence. Chased this down and found `customitems/models/blocks/vehicles/` is one small corner of a MUCH larger sibling directory, `customitems/models/blocks/la/` ("Level Art" -- confirmed by content, not just guessed from the abbreviation): roughly 200 categories of pure environmental set-dressing -- furniture (`bookcase`, `dining_table`, `sofa_armchair`), hospital props (`hospital_bed`, `operating_table`, `medical_gurney`), corpses and body horror (`dead_poses`, `body_bags`, `cages_for_mutants`), fortifications (`barbed_wire`, `tank_trap`, `gabion`), and -- sitting in this same list as ordinary furniture -- `military_vehicles`, `military_hardware`, `sawn_vehicles`, `snowmobile_buran`, and a second, separate `il_2_m3`/`junkers_ju_87` pair (distinct folders from the ones already found under `heli/warmup/` in part 32). This directory also holds a real WWII-tank roster never noted before: `is_2` (IS-2), `isu_152` (ISU-152), `t34_85` (T-34/85), `tiger_e` (Tiger E), `su_76m` (SU-76M), `pzpz_kpfw_4` (Pz.Kpfw. IV) -- the same "period military hardware as level-art prop" pattern as the WWII aircraft, now extended to armor. `sawn_vehicles` holds named PIECE models (`veh_zil131_bottom`, `veh_zil131_cabin`, `veh_moskvich2140`, `veh_btr70_gun`, etc.) plus a separate `tire_single` loose-tire prop -- "sawn" (cut apart) reads as pre-broken wreck/scrap pieces for a car, consistent with debris set-dressing, not a whole functioning machine.

**Four independent evidence threads, checked separately, all agree these are non-drivable environmental content:**

1. **A one-time, catalog-wide startup pass explains the earlier "loaded constantly" numbers, and it is NOT evidence of placement.** `grep -c "AsyncStartupHelper.*mcvd_trace" client-10.log` = 3,623, ALL completing within a single ~14-second window at launch (14:08:37-14:08:51), and NOT limited to vehicles -- the exact same pattern fires for anomaly models, barricades, and random junk props (`anomalies:trace/zastoy.mcvd_trace`, `customitems:models/trace/barekad.mcvd_trace`, `customitems:models/blocks/things/perdelka.mcvd_trace`) in the identical window. This is a bulk collision/navmesh-trace precompute pass across the entire shipped asset catalog at boot, unrelated to whether any given model is actually placed anywhere in the world -- important to disclose because part 32's raw `grep -c` counts (zil131: 27,526 total hits, etc.) were NOT yet separated from this boot-noise, so those totals overstated how much of the "loading" is real in-world streaming.
2. **Separating that noise out, real runtime loading is still genuine and continuous, not a boot artifact.** Filtering out `mcvd_trace` lines, `zil131`'s remaining 11,652 hits in `client-10.log` (a ~14-hour real session, 2026-08-30 14:09 through 2026-08-31 02:07) are NOT clustered at startup -- they occur in every single hour of the session (477 to 1,594 hits/hour), consistent with the client streaming these models in and out as the player moves between zones/chunks throughout real play, exactly like any other static world prop.
3. **Direct confirmation of set-dressing (not vehicle) use**: `[Background loader/INFO] Built shader for material customitems:models/blocks/barricade/barrikada_ural0.mcmtl/zil131_body` -- the ZIL-131 truck model is used as geometry/texture source for a BARRICADE prop (`barrikada_ural0`, i.e. a wrecked-Ural-style roadblock), the classic S.T.A.L.K.E.R.-genre "abandoned vehicle as checkpoint obstacle" set piece -- not a functioning truck.
4. **No vehicle-control input scheme exists anywhere in the client.** The real `keybinds.txt` (STALZONE install root, 79 total real keybindings, read in full) covers on-foot movement/sprint/prone/crouch, all inventory and weapon actions, hideout building (`key.hideout.enter/fast_craft/edit/remove/overwatch`), PDA/map/waypoints, calldown, revive, surrender -- zero entries for entering, exiting, or controlling a vehicle (no `key.vehicle.*`, no seat/drive/horn/lights binding of any kind). `config/options.json`'s one "driver"-adjacent flag, `show_driver_notification`, sits among generic system/UI flags (`shown_disclaimer`, etc.) rather than gameplay flags (`fight_equipment`, `fight_bet`) -- almost certainly a GPU-driver-outdated warning toggle, not a vehicle-driver flag; checked and disclosed as ruled out rather than silently ignored.

**Conclusion, disclosed at the appropriate confidence level**: this is an absence-of-evidence argument, not a positive "we found the code that disables vehicles" -- but it is now four independently-checked threads (bulk-precompute artifact correctly separated out; genuine session-long streaming confirmed anyway; direct barricade-material reuse; zero input bindings) all pointing the same direction, plus the user's own real-world play experience agreeing with it. This project now treats "player-drivable vehicles" as very likely NOT a real STALZONE feature -- the real content is a large, genuinely-used roster of vehicle AND WWII-armor 3D models functioning as environmental set-dressing/wreck props (some doubling as barricade materials), the same category as furniture, corpses, and fortifications. Revises part 32's "left open" framing on this specific question. `mi8_attak_placeholder`'s dev-labeled "placeholder" status and the `tutorial_3` dialogue system's still-unfound full script remain the two genuinely open threads from part 32.

## Part 34 (2026-09-01) -- Real quest/story dialogue found: a 108-file, 5-language subtitle library with named characters, faction storylines, and a cross-referenced confirmation of the "Zivcas" faction name

Followed up on part 32's still-open `mi8_attak_placeholder` and `tutorial_3` threads. The placeholder resolved quickly and stayed thin: `grep`-ing all 14 client logs for `attak_placeholder`/`mi8_attak` returns zero hits anywhere -- this 2.5MB real model file is never loaded in any session this project has, consistent with its own "placeholder" self-label (real, on-disk, unused-so-far content, nothing more to add). Chasing `tutorial_3`'s text led somewhere much bigger.

**`modassets/assets/_localized/<lang>/customnpcs/subtitles/` holds 108 real `.srt` subtitle files, fully localized into all 5 shipped languages (en/es/fr/ko/ru, 108/108/108/108/108 -- a finished pipeline, not WIP).** This is a distinct system from part 32's ambient combat-voice-bark folders (`customnpcs:music/steven/sounds/npc/<faction>/<state>/`) -- these are flat-named, individually-authored quest/story dialogue lines, real proper SRT format (numbered cues, millisecond timestamps, real English sentences), read directly and confirmed genuine narrative content, not placeholder/lorem-ipsum text. Organizes into clear story arcs by filename convention (`<arc>_<chapter>_<character>.srt`):
- **`duty_1/2/3_*`** (Duty faction, already a known settlement/faction name): erzhan, godun, yushka, tarasov, semerskiy, davidov.
- **`merc_1/2/3_*`** (Mercenary faction, already known): romeo, tunda, muller, komun, leha.
- **`stalkers_1/2/3_*`** (Stalkers faction, already known): kefir, kuraga, erzhan (same name as a `duty` character -- not confirmed as the same individual, noted not assumed).
- **`zarya_1/2/3_*`** (NEW faction/location name, "dawn"): chatskiy, kuraga, makhorkin, garnitskiy, baldezhnik.
- **`zavet_1/2/3_*`** (NEW faction/location name, "covenant/testament"): tretiak, afon, maslinkin, evlampiy, mitropolit ("Metropolite" -- an Orthodox-church rank -- appears IN the dialogue text too, see below).
- **`palach_<arc>_1/2/3.srt`** (one antagonist character, "Palach" = "Executioner", addressing FOUR different factions/arcs by name: `merc`, `rubezh` (NEW name, "frontier/border"), `zarya`, `zavet`) -- a single recurring villain/rival character cutting across multiple storylines.
- **`bigeagle_{bar,legend,newbie,north,oldbie}.srt`** -- one character ("Big Eagle") with 5 status/location-conditioned dialogue variants, reads as a trader/quest-giver whose lines change with player rank or current zone.
- **`twins_*`** -- `twins_2/3`, `twins_oskolok_{2,3,4}` ("oskolok" = shard/fragment), `twins_peredacha_{2.5,3.5,4.5}` ("peredacha" = transmission/handover), `twins_vospominanie_3` ("vospominanie" = memory/reminiscence) -- a multi-beat arc for a "Twins" character/duo.
- **`nina_{2..6}.srt`**, **`eremey_1.srt`**, **`tim_{1,4,8}.srt`**, **`romeo_1.srt`**/`romeo_2_dop.srt`, **`operativnik_2.srt`** ("operative"), **`p_fil_{2,3}.srt`**, **`terentii_na_okraine.srt`**/`terentii_verse.srt`/`terentiy_skuchno.srt`, **`skalolaz_monologue.srt`** ("Skalolaz" = "Climber", the largest file at 6.8KB -- a substantial standalone monologue, read in full: an unhinged, grandiose boss/rival-type speech that name-drops the merc character `Muller` from `merc_2_1muller.srt`, tying it into the merc storyline), **`poteryannoe_vosp_2.srt`** ("lost memory"), **`vospominanie_{4,5}.srt`**, and **`bandits_{1,2,3}_Nburger.srt`** (an odd "burger"-suffixed bandit-line set, not further explained).
- **Real text read directly, confirming genuine narrative content** (paraphrased here per this project's copyright discipline, not reproduced verbatim beyond short fragments): `nina_2.srt` is a short, distressed "can you hear me... help me" plea (4 cues); `palach_zavet_1.srt` has Palach mockingly addressing a monastery faction and their "Metropolite," introducing himself as commander of the **"Zivcas" special forces**.
- **Cross-referenced, independent confirmation of "Zivcas" as a real faction/unit name**: part 33's `la/` level-art sweep already found a `zivcas_capsule` prop folder, unexplained at the time. Palach's own line ("commander of the Zivcas special forces") now explains it -- two unrelated evidence sources (a 3D-prop folder name, and quest dialogue text) independently agree on the same real, previously-uncatalogued faction name.
- **`zavet` = "covenant" cross-referenced against part 28's SettlementRegistry**: the 15-name real settlement roster (part 28) includes an unexplained `covenant` entry with no faction-name reading offered at the time; `zavet` is the literal Russian word for "covenant/testament," and this part's dialogue confirms `zavet` is a real, monastery-affiliated in-game faction (referencing a "Metropolite"). This resolves `covenant`'s meaning with real supporting text, upgrading it from an unexplained name to a documented religious/monastic faction.
- **Two new faction/location names surface for the first time**: `zarya` ("dawn") and `rubezh` ("frontier/border") -- neither appears in the part-28 settlement roster or the part-26 46-zone map roster, so their relationship to those namespaces is undetermined; recorded as real (backed by multiple named-character dialogue files each) but not yet placed on the map.
- **Not confirmed triggered in any analyzed session**: `grep`-ing all 14 client logs for six sample filenames (`duty_1_4erzhan`, `palach_zavet_1`, `skalolaz_monologue`, `nina_2`, `twins_2`, `bigeagle_bar`) returns zero hits across the board -- none of this dialogue has fired during any of the play sessions this project has log data for. Disclosed honestly as "real content, not yet observed triggering" rather than assumed cut or assumed live; the audio bank backing these subtitles was not located this part (`customnpcs/music/steven/sounds/` only contains `arena`/`npc` subfolders, neither matching these character names directly) -- an open thread if this line of research continues.

**Not yet done**: the audio-bank location/trigger path for these 108 quest lines is unresolved. Whether `zarya`/`rubezh` correspond to unidentified zone codes from part 26 (`mg`/`pd`, still unresolved) was not checked this part. The `stalkers`/`duty` shared "erzhan" name was noted but not investigated for whether it's the same character across storylines.

## Part 35 (2026-09-01) -- The audio bank found: 472 real FMOD banks (1.8GB), a Twins boss-fight encounter, a multi-year seasonal-event calendar, and `mg`/`pd` still unresolved

Directly answers part 34's "not yet done": the quest-dialogue audio bank IS `gloomycore/banks/`, just not reachable by matching character names against `customnpcs/music/steven/sounds/` (that path is only the ambient combat-voice-bark system, part 32). `gloomycore/banks/` holds **472 real `.bank` files, 1.8GB total** -- this is the actual FMOD audio-bank library backing nearly every named NPC this project has found across every part, plus a large amount of content never seen before. Confirmed as genuinely used (not just present) via real `[Game thread/INFO] Loading bank <path>` log lines: aggregating all 14 client logs, **75 of the 472 banks are confirmed actually loaded** during real play sessions (the other 397 are real, on-disk, but not observed loading in any session this project has log data for -- disclosed as unconfirmed-live, not assumed cut).

**Confirms/resolves several previously-open threads:**
- **`tutorial_3_en.bank` and `lipa_en.bank` are both in the confirmed-live 75** -- direct proof part 32/34's `tutorial_3` dialogue system and its "Lipa" character are real, shipped, and actually played, not orphaned data. A THIRD Lipa-specific bank, `lipa_tutorial.bank`, exists on disk (not confirmed live in these particular sessions) explicitly tying the character to the tutorial by name.
- **`starievshick_en.bank`** (confirmed live) matches the `trader_starievshik` trader id found in part 27's `VendorArchive` -- that trader has real character-voiced dialogue, not just a shop menu.
- **`dmz2024_quests.bank`** (confirmed live, loaded in `client-12.log`/`client-13.log`) is real, separate evidence of DMZ-mode quest content, corroborating part 26's DMZ-zone find with an independent source (quest audio, not just zone folders).
- **`bigEagle_en.bank`** and two of the five recruiter banks (`verbovshik_grey.bank`, `verbovshik_perst.bank`) are confirmed live, alongside `knyaz_tutorial_en.bank` -- a SECOND tutorial-tied character ("Knyaz" = "Prince/Duke") not seen in part 32/34's `tutorial_3` subtitle sweep, suggesting the tutorial has more voiced characters than the 4 named in the subtitle files alone.
- **`vimpel.bank`** (confirmed live) -- "Vympel" is the real name of an elite Soviet/Russian special-forces unit (historically linked to the KGB, alongside the better-known Alfa Group), matching this project's established pattern of the game naming things after real Russian military units.

**Real, substantial content never noted before, found purely from bank naming (on-disk, not yet confirmed live)**:
- **A "Twins" (`bliznetsi`, Russian for "twins" -- matches the `twins_*` subtitle files from part 34) dungeon boss fight, evidenced by FIVE dedicated banks**: `3_bliznetsi.bank` (the character/encounter itself), `3_dunge_before_twins.bank` + `3_dunge_twins.bank` (pre-fight and fight dungeon-specific audio), `3_twins_bossfight.bank`, `3_twins_resonance.bank` -- a real structured boss encounter, not just ambient dialogue. A second, unrelated `3_bossfight.bank` also exists, meaning at least one other (unidentified) boss fight is real content too.
- **Zavet (the monastery faction, part 34) has extensive dedicated content**: `3_dopzavet.bank` ("additional Zavet"), `3_chtec_zaveta.bank` ("reader of the covenant"), `3_games_zavet.bank`, `3_zvuki_zaventa.bank` ("sounds of the covenant") -- confirms Zavet is a major, developed storyline, not a minor faction.
- **A real "wounded NPC" dialogue variant exists per faction**: `2_raneniy_dolgovec`/`_naemnik`/`_officer`/`_svobodovec` ("wounded Duty-member/mercenary/officer/Freedom-member") -- direct new evidence that Duty, Mercenary, and Freedom (already-known factions) plus a faction-neutral "officer" role all have a distinct downed/wounded dialogue state.
- **A real multi-year seasonal-event calendar, evidenced by dated event-music banks**: `ny2023.bank`/`ny25.bank`/`christmas.bank`/`snegurochka*.bank` (New Year), `encaged2025_music.bank`, `bm2024_music.bank`, `autumn25.bank`, `reichstag2026_music.bank` (matches the `map-reichstag` zone, part 15), `dmz2026_music.bank` -- real evidence STALZONE has run recurring seasonal events since at least 2023.
- **Five named "recruiter" NPCs**: `verbovshik_grey/kondor/perst/rakita/sozonka` ("verbovshik" = recruiter) -- suggests a real faction-recruitment questline mechanic tied to named individual recruiters, not a generic menu.
- **A `sinchrophasotronloop.bank`** ("Synchrophasotron" -- a real Soviet-era particle-accelerator term, used in the original S.T.A.L.K.E.R. trilogy's "Brain Scorcher" superweapon lore) -- a strong, specific lore callback, not yet tied to any other evidence in this project.

**`mg`/`pd` (part 26's still-unidentified zone codes) checked against this part's finds and remain unresolved**: neither `zarya` nor `rubezh` (part 34) appear as a substring in any real `map_cache/5.0` zone folder name, and neither string appears in `quests.cfg`. `mg`/`pd` themselves are confirmed still on disk as real non-empty zone data (part 26) but this part found no bank, subtitle, or config file naming either one -- the mystery is unchanged, not resolved.

**Not yet done**: only 75/472 banks confirmed live; the other 397 (including every Twins/Zavet-specific bank, `sinchrophasotronloop`, most named-character banks) are real but unconfirmed as ever triggering in a logged session -- opening and listening to individual `.bank` files (an FMOD-specific binary format, unparsed by this project) was not attempted this part. The `stalkers`/`duty` shared "erzhan" name (part 34) remains uninvestigated.

## Part 36 (2026-09-01) -- STALZONE confirmed as (or extremely closely derived from) the real public game STALCRAFT: X, published by real company EXBO

**What changed this part**: for the first time this project used `WebSearch`/`WebFetch` against public sources (rule 1's third evidence category: "documented public sources"), rather than only client files/logs/tests. This is a new, separate research channel from the still-gated wire-protocol decryption work -- lower risk, and explicitly sanctioned by the project's own evidence rule. Findings below are public-source evidence, clearly labeled as such, and are cross-checked against real STALZONE files/logs wherever possible; where they are not yet cross-checked, that is stated plainly.

**STALZONE is real-world grounded.** Web search confirms a real, currently-live game called **STALCRAFT: X** on Steam (app id `1818450`), published/supported by a real company, **EXBO**, at the real official support site `support.exbo.net`. This directly matches the long-known internal Java package prefix found many parts ago in the client's own class names, `exbo.stalcraft.engine.clientcore.Main` -- "EXBO" was previously treated as an unexplained internal codename; it is now confirmed to be the actual real-world publisher/developer name. STALZONE is therefore either literally the same game as STALCRAFT: X, or an extremely tightly derived build of it (same engine lineage, same content, at minimum a shared codebase) -- this project cannot yet distinguish "is" from "derived from" with the evidence gathered so far, and does not claim to.

**Official EXBO patch-notes article found and fetched** (`support.exbo.net`, an article titled referencing "STALZONE from 10.07.2024" -- public source, fetched via `WebFetch`, not yet cross-checked against real STALZONE config/log files this part): documents a real faction-rename scheme -- **Duty -> Frontier**, **Freedom -> Rise**, **Monolit -> Murmur** -- plus several location renames (including **Rostok Factory -> Pervomaisky factory**), at least one new named NPC (**"Condor" / "Archon Zuza"**), and new mechanics described as **Hideout**, **Outposts**, **Territories**, and **Gathering**. None of these renamed/new names have yet been grepped against real STALZONE `config/`, `quests.cfg`, or log files this part -- they are recorded here as public-source claims about the real game family STALZONE belongs to, not yet as confirmed-present-in-STALZONE facts.

**Possible connection, explicitly hedged, NOT confirmed**: part 34 found an unidentified quest-arc name `rubezh` (Russian, roughly "frontier/border/boundary line") among the real subtitle files. The official patch-note's English rename "Duty -> Frontier" is a plausible semantic match for a Russian term meaning "frontier" -- but this is only a semantic/etymological hint, not verified evidence. `rubezh` has not been found in any STALZONE config, log, or asset filename beyond the subtitle file discovered in part 34, and no direct string match to "Frontier" or the renamed faction data exists in real STALZONE files checked so far. This connection is flagged as a hypothesis worth re-checking if more STALZONE faction data surfaces, not stated as fact.

**`stalcraft.wiki.gg/wiki/Factions` fetched** (public wiki, not an official EXBO source, cross-checking not yet done against STALZONE files): lists a faction hierarchy of **Stalker** (containing Frontier and Rise as sub-groups) versus **Bandit** (containing Mercenary and Covenant as sub-groups), alongside separate non-player factions **Murmur**, **Military**, **Scientists**, **Zombies**, and **Orderlies**. This is broadly consistent with the "Duty/Freedom/Monolit are player-aligned, Zombies/Military are NPC-only" pattern already independently observed in real STALZONE files across earlier parts, and gives "Covenant" as a public-source translation candidate for the `zavet` faction name (part 34 already independently translated `zavet` as "covenant" from the Russian word itself -- the wiki's "Covenant" faction name is an independent public-source corroboration of that translation, not the origin of it).

**"Master of the Zone" confirmed as a real, documented boss fight** (via `WebSearch` then `WebFetch` of `gameplay.tips/guides/stalcraft-x-how-to-kill-the-master-of-the-zone.html`, a third-party guide site -- public source, unverified against STALZONE files this part): fight takes place in an arena described as having "underground valve areas and a particle accelerator"; the boss speeds up in phases as its health drops in 25% increments; it has "aggressive electrical discharge attacks" and pursues players rapidly, especially in later phases; the guide states stun grenades thrown close to it are "the only way" to kill it; references to "launchers on his back" and electrical abilities suggest a cybernetic or mechanical creature, not a purely organic one.

**Likely (not confirmed) identity of part 35's second, unidentified boss bank**: part 35 found `3_bossfight.bank` as a real file distinct from the five dedicated Twins (`bliznetsi`) boss banks, with no further identification at the time. "Master of the Zone" is now a strong candidate for that unidentified encounter -- it is a documented major boss fight in the same game family, distinct from Twins. This is a plausible match based on process of elimination (a second real boss-fight bank existing, and a second real documented boss fight existing) rather than a direct string/name match inside the bank file itself, which was not attempted this part (the FMOD `.bank` binary format remains unparsed beyond the shallow `strings` probe from part 35).

**Notable lore connection, still speculative**: the "particle accelerator" arena detail for Master of the Zone lines up suggestively with the already-documented `sinchrophasotronloop.bank` (part 35's find; "Synchrophasotron" is a real Soviet-era particle accelerator, also referenced in the original S.T.A.L.K.E.R. trilogy's "Brain Scorcher" lore). If Master of the Zone's arena music/ambience is what `sinchrophasotronloop.bank` provides, that would tie two previously-separate findings together -- but this is an inference from thematic overlap (particle accelerator mentioned in both), not a confirmed file-to-encounter mapping, and is recorded here strictly as a hypothesis.

**Leads not yet successfully pursued**: a `WebFetch` of a Steam Community "HOW TO KILL TWINS" guide (`steamcommunity.com/sharedfiles/filedetails/?id=3326823848`) failed with a 503 server error this part and was not retried; it may contain useful Twins boss-fight detail to compare against the five Twins-related banks already found in part 35. `stalcraft.fandom.com/wiki/Locations` appeared in search results but was not yet fetched -- it is a natural next source to check against the still-unresolved `mg`/`pd` zone-code mystery (part 26, re-checked and still unresolved in part 35).

**Scope note**: per the project's rule 2 (game content, not EXENS/anti-cheat/launcher internals), this part's public-source research stayed entirely on game content (factions, locations, NPCs, boss mechanics, lore) and did not look into EXENS, anti-cheat, or launcher-related public information, even though `support.exbo.net` likely hosts such material too.

## Part 37 (2026-09-02) -- Follow-up public-source pass: fan-wiki corroboration for Zavet/Frontier locations, `mg`/`pd` and Twins-guide leads remain unresolved (documented as negative results, not gaps to hide)

**Purpose**: rule 1 requires evidence, but it does not require every lead to pan out -- a lead that was checked and came up empty is still worth recording, so a future pass does not repeat the same dead-end search. This part is mostly negative results plus one small corroborating find.

**New corroborating find**: a fan-run reference site, `stalzone.wiki` ("STALZONE WIKI"), explicitly brands itself as covering STALCRAFT: X (build calculators, TTK analysis, interactive map, etc.) under the name "STALZONE" -- independent, informal, public confirmation that players themselves use "STALZONE" and "STALCRAFT: X" interchangeably, consistent with part 36's identity finding. This is a fan-community naming convention, not an official EXBO statement, and is recorded as such.

**`stalcraft.wiki.gg/wiki/Locations` fetched** (public wiki, unverified against real STALZONE files): gives named sub-locations tying part 34/36's faction terms to actual map places -- **"Covenant Abode"** (Temple Swamp) and **"Covenant Headquarters"** (Inflow), both North Zone, for the Zavet/Covenant faction; **"Frontier Base"** (Barrier) and **"Frontier Headquarters"** (Inflow), also North Zone, for the Duty->Frontier faction. Both factions having a distinct "Headquarters" in the same zone (Inflow) is new detail not previously recorded. None of these location names (Temple Swamp, Inflow, Barrier) have yet been grepped against real STALZONE `map_cache/5.0` folder names or `quests.cfg` this part -- recorded as public-source claims only, not yet cross-checked.

**`mg`/`pd` zone-code mystery (parts 26/35/36): still unresolved.** `stalzone.wiki/en/maps` was fetched but only returned a partial "Wild North" region listing (Labyrinth, Lyubech-3, South Zone, North Zone, The Graveyard) with no short codes or abbreviations of any kind visible in the fetched content -- not proof the codes don't exist publicly, just that this particular page didn't surface them. A direct web search for "mg"/"pd" as STALCRAFT map codes returned no relevant results (only unrelated topic-code coincidences). This lead is now checked against two more public sources than before (the wiki.gg Locations page and the stalzone.wiki maps page) with no match either time; it remains an open question, most likely only resolvable from STALZONE's own real files/logs rather than public wikis.

**Twins boss-fight Steam guide (`steamcommunity.com/sharedfiles/filedetails/?id=3326823848`): still inaccessible.** Retried this part and again failed, this time with a 503 server error (same failure mode as the first attempt in part 36) -- two consecutive failures on the same URL, not a transient blip specific to one call. A follow-up search for alternative written Twins-fight guides (gameplay.tips, itemlevel.net, and similar sites) surfaced no dedicated Twins article -- only the already-known Master of the Zone guide (part 36) and an unrelated main-quest guide. The existing five Twins-related banks from part 35 remain the best evidence for that encounter; no new public-source detail on Twins' actual attack pattern was obtained this part.

**`gameplay.tips`'s STALCRAFT quests guide fetched** (public, unverified): covers only generic MSQ/side-quest UI conventions (yellow/purple markers), a "Kill the Beast" challenge, an "Arena Quest Boss" named Amaury, and NPCs Linden/Shaman/Ataman -- none of which match any name already found in real STALZONE files across this project (Nina, Palach, Skalolaz, Zivcas, the recruiters, etc.). Recorded as a negative result: this particular guide is not a useful cross-reference for STALZONE's own quest content, and "Amaury" is a new name with no connection to anything found in real STALZONE files so far -- not investigated further this part since it did not match any existing lead.

## Part 38 (2026-09-02) -- back to code: three new content systems built and validated against real data (dialogue, vehicle props, audio banks)

After part 37's diminishing-returns public-source pass, work shifted back to the actual server-emulator code (the project's primary deliverable) per the user's "keep making the server emulator" direction. Three new systems added to `server-emulator/`, each compiled clean and smoke-tested against real (or real-directory-structure) data before being treated as done -- not just "it compiled":

**`dialogue/SubtitleCue.java` + `SubtitleTrack.java` + `SubtitleRegistry.java`**: loads all 540 real `.srt` subtitle files (108 npcIds x 5 locales, part 34) end-to-end. Found and fixed two real parsing quirks while validating against the actual files (not assumed from the earlier research summary): 3 of 540 files (`ru`-locale `terentii`/`terentiy` files) use CRLF instead of the otherwise-universal LF; `terentiy_skuchno.srt`'s `ru` track is missing cue #1's index line entirely (tolerated only on a file's first block). Also found, while writing a cross-locale timing-consistency check, that the "shared timeline across locales" assumption is real but NOT universal: `bigeagle_newbie`'s `ru` file is an untranslated, byte-identical copy of the `en` text (confirmed by direct diff) with drifted timings from cue #10 on and one typo `en` doesn't have -- surfaced via `npcIdsWithCrossLocaleTimingMismatch()` rather than failing the whole load. `dialogue/NpcIdGrouping.java` mechanically groups the 108 npcIds by raw filename prefix token (10 tokens cover 88 names; 20 names split into 10 small/awkward tokens, explicitly NOT claimed to be a verified "quest arc" system).

**`world/props/VehiclePropCatalog.java`**: catalogs the real `vehicles/{cars/{cargo_cars,light_cars,military,other,toy},boats,heli}` model tree -- real Soviet-era vehicle names (`zil131`, `moskvich_2136`, `t72`, `mi6`, etc., see recon file for the full roster) -- explicitly disclosed as decorative Level Art per part 33's non-drivable finding, not a drivable-vehicle system. Found a real structural quirk: the same vehicle sometimes has both a same-named subfolder (texture variants) and separate loose mesh files sharing a name prefix one level up -- exposed as two separate facts rather than merged by a guessed naming rule (e.g. `kazanka_a.mcsb` vs `kazanka_b.mcsb` could be two variants or two different boats; nothing in this project's evidence says which).

**`audio/BankCatalog.java`**: catalogs the real 472 `.bank` files and cross-references them against real `client-*.log` "Loading bank" evidence for confirmed-live status -- deliberately DYNAMIC (re-scans real logs at call time) rather than a hardcoded snapshot of part 35's "75 confirmed live" figure, so it can't silently go stale. Re-running the scan while building this class landed on exactly 75 again, confirmed rather than assumed to still match.

## Part 39 (2026-09-02) -- found EXBO's own official public item-data API; it confirms (and lets this project finally implement) the weapons.json damage-falloff shape

Continuing the public-source research channel (part 36), but this time following a lead named in this project's OWN `docs/PROJECT_PLAN.md` "Tier 1" resource list from the very start of the project: `EXBO-Studio/stalzone-database`, a real, actively-maintained (654 commits, 80 stars) public GitHub repo storing STALCRAFT/STALZONE item data as JSON, kept in sync with game patches. Its README pointed to real, official EXBO developer documentation at **`https://eapi.stalzone.net/items.html`** -- EXBO's own domain, not a fan site, fetched and read this part.

**What the official docs say**: items are organized as `<realm>/items/<content-type>/<category>/<item-id>.json` (e.g. a real example item id `0n9q`, `item.wpn.mp5_mod`, English name "Viper"). One documented info-block type is the **"Damage Ramp Block"**, with five fields: `startDamage` ("weapon damage at distance of 0 meters"), `damageDecreaseStart` ("distance in meters at which damage starts to decrease"), `damageDecreaseEnd` ("distance in meters at which damage stops decreasing"), `endDamage` ("weapon damage at distance defined by damageDecreaseEnd"), `maxDistance` ("max distance in meters after which weapon deals no damage").

**This is a direct, word-for-word match to this project's own `data/weapons/weapons.json` dataset's `damageFalloff` sub-object field names** (confirmed by checking a real record, id `0r2g1`: `{startDamage: 33.9, damageDecreaseStart: 14, endDamage: 22.1, damageDecreaseEnd: 62, maxDistance: 135}`) -- the server-emulator's `WeaponEntry.damageFalloff()` had already inferred this exact shape from the field names alone (part 30) but explicitly declined to implement any interpolation, since nothing at the time confirmed the boundary semantics were correct rather than a guess. They now are: EXBO's own documentation confirms damage is flat at `startDamage` from 0 to `damageDecreaseStart`, flat at `endDamage` from `damageDecreaseEnd` to `maxDistance`, and exactly 0 beyond `maxDistance` -- every one of those boundary behaviors, not inferred.

**What remains genuinely undocumented**: the official docs describe the block only as a "damage ramp" with no formula for the segment strictly between `damageDecreaseStart` and `damageDecreaseEnd`. This project implemented `WeaponEntry.damageAtDistanceLinearAssumption()` using plain linear interpolation for that one segment -- named and documented explicitly as an unconfirmed assumption (the conventional default for a two-point ramp, not verified against real captured in-game damage numbers, which would need a controlled capture per rule 1). Tested against real data: AK-12 (`startDamage=37.5` to 18m, `endDamage=29.9` by 72m) interpolates smoothly (e.g. 33.7 at the 45m midpoint); and a real, isolated edge case was found and confirmed correct -- `knmdv` ("Gauss Rifle") has `damageDecreaseStart == damageDecreaseEnd == 200` AND `startDamage == endDamage == 247`, i.e. genuinely flat damage across its whole 0-250m range with no ramp at all (thematically fitting for a railgun-style weapon) -- the implementation's boundary conditions handle this zero-width-ramp case correctly with no division-by-zero special case needed, confirmed by testing that exact record rather than assumed safe.

**Scope note**: this stayed on item/weapon game-content data per rule 2 -- the `eapi.stalzone.net` docs likely cover other endpoints too (not explored), and nothing about EXENS/anti-cheat/launcher internals was looked at.

## Part 40 (2026-09-02) -- real per-character save-data files discovered and parsed: interaction log, quest/notification history, outpost coordinates, handbook links, multi-year battlepass evidence

Continuing code work per the user's "keep going" direction. While browsing the real STALZONE install's `config/` directory for other leads, found real per-character save data at **`config/<CharacterName>/{common,sd1,sd2,sd3,sd4}`** (each with an `_old` backup copy alongside it) -- not documented anywhere this project had found before, public sources included. Real samples from three different real characters were inspected (with real other-players' usernames encountered in the process -- see the privacy note below on how this project handled that).

**Format**: every one of the five files, magic-byte-confirmed (`28 B5 2F FD`), is Zstandard-compressed NBT -- the exact same format already validated for `config/vendor_archive.dat` (part 24). No new binary-format reverse-engineering was needed; the existing `nbt/NbtReader` + `nbt/CompressionUtil` pipeline parsed every real sample cleanly (0 leftover bytes every time).

**What the five slots actually hold** (confirmed from real file content, not guessed from the slot names -- `sd1`-`sd4` give no hint on their own):
- `common`: client UI/preference state only (menu-tab-seen flags, trashed-item ids, last op-map difficulty picked, phonograph listen list, etc.) -- no server-authoritative game data.
- `sd1`: a top-level `interacts` list. One real character's file had 1 entry; another had 29. Schema: `{type, username, allianceId}`. `username` names the OTHER real player involved and `allianceId` is their faction (using the pre-rename internal ids this project already confirmed, part 36: `freedom`/`duty`/`stalkers`/`bandits`/`merc`/`covenant`). Across the 30 real entries seen, exactly 6 distinct `type` values occurred: `DIED_FROM_PLAYER`, `PLAYER_KILLED`, `REVIVED_BY_PLAYER`, `REVIVED_PLAYER`, `BOUGHT_LOT_FROM`, `SOLD_LOT_TO` -- self-explanatory pairs (died to / killed; revived by / revived; bought a market lot from / sold one to), NOT treated as a closed/verified enum given the small (2-character) sample.
- `sd2`: a top-level `notifications` list -- the client's in-game toast/notification log. One real character's file had 60 entries across 6 distinct `channel_id` values: `quests` (36), `generic` (14), `handbook` (6), `outpost` (2), `savepoint` (1), `mail` (1). Outer shape: `{notification: {channel_id, time, expire_time, payload}, isRead, receivedMoment}` -- exact Java/NBT types confirmed field-by-field (`channel_id`/`String`, `time`/`expire_time`/`receivedMoment`/`Long`, `isRead`/`Byte`), not assumed from `NbtDumpTool`'s pretty-printer (which caps list previews at 3 items -- a display limit only, worked around with a small throwaway program reading the parsed structure directly to see all 60 real entries).
  - `quests` payload: `QuestId` (Integer, present on `new`/`quest_finished`, genuinely ABSENT on `quest_failed`/`mail_reward`), `type` (String: all 4 of `new`/`quest_finished`/`quest_failed`/`mail_reward` confirmed real), `QuestTitle` (String, real names e.g. "For the Good of the Mercenaries", "An Important Delivery", "Order: Pig Apples"), `quest_type` (Integer: only 0 and 2 seen in this sample -- meaning not confirmed, though the one `quest_type=2` quest id seen (27091, and separately 27131) is the only one that cycles new->failed->new->failed->...->finished, vs. `quest_type=0`'s single new->finished progressions in every case seen -- noted as a real pattern in this one sample, not stated as a confirmed mechanic).
  - `outpost` payload: `x`/`z` (real JSON/NBT Floats, both real occurrences landing near the same in-game location) -- structurally consistent with an outpost-contest notification (ties to part 36's official "Outposts" mechanic) but not confirmed against that mechanic's actual trigger conditions.
  - `handbook` payload: `link` (String, real values like `handbook://articles/pvp/smert.md/#header_3`). The real article-path categories seen -- `pvp`, `stats`, `progression`, `arthunt` -- line up with topic areas this project has separately confirmed exist in-game (arthunter enemies, part 33; outposts, part 36).
  - `generic` payload: heterogeneous -- battlepass/challenge-completion toasts with `msg`/`title` as localization-key maps and a `seasonpass://...` URI `link`, plus at least one plain world-event ping with only `msg`. Real localization keys seen include `challenges.triumph.triumph2025`, `challenges.triumph.triumph2026`, and `challenges.season.summer26` -- confirming, from an actual real save file rather than just patch-notes text, that STALZONE runs year-dated seasonal battlepass/challenge content spanning at least 2025-2026.
  - `savepoint` (payload includes a `BaseName` localization-key map, seen once, referencing a base named "savepoint_merc") and `mail` (payload had one field, `Author`, seen once with an empty value in this sample) were each seen only once -- too thin an evidence base for typed accessors.
- `sd3`: a top-level `observedOffers` list -- real shop/market offer names, in Cyrillic in the one real sample seen (e.g. "Сезонный уровень" / "Season Level", "Мотив «Бронебойный»" / "Armor-Piercing Motif") -- which offers this character has already seen in a shop UI. Not itself gameplay-authoritative.
- `sd4`: more UI "have I seen this cosmetic-tab version" version-counter flags, plus one raw `observedElements` byte array whose bit/byte layout was not decoded (no evidence yet of its structure).

**Real cross-file validation**: `config/quests.cfg` (a separate, plain-JSON file, `{hiddenQuests:[...], readQuests:[...]}` with bare numeric quest ids) was checked against the real `sd2` sample from the SAME character. Every quest id `sd2` names with full metadata (27091, 26098, 26105, 27621, 26004, 27756, 27653, 27131, 34764, 33132, 33389, 33388, 27976) appears in `quests.cfg`'s 73-entry `readQuests` list, and `quests.cfg`'s single `hiddenQuests` entry (27091) is exactly the quest id `sd2` shows cycling new->failed->new->failed->...->finished (the `quest_type=2` pattern noted above). This is real, observed cross-file consistency between two independently-discovered files, not an assumption about how they relate.

**Implementation**: `session/save/PlayerInteraction.java`, `session/save/PlayerNotification.java`, `session/save/PlayerSaveFile.java` (loader), and `session/save/PlayerSaveFileDumpTool.java` (CLI) -- following the same raw-fields-plus-typed-accessors pattern as `items/WeaponEntry.java`, and the same "only build typed accessors for well-evidenced shapes" discipline as `dialogue/SubtitleRegistry.java`'s cross-locale handling. Compiled clean (62 source files total now) and smoke-tested against real samples from all five slots across both real characters -- `PlayerInteraction`/`PlayerNotification` correctly parsed all 30 real interactions and all 60 real notifications, including every one of the edge cases above (missing `QuestId`, the `knmdv`-style zero-cycle repeatable quest, single-occurrence channels falling back to raw `payload()`).

**Privacy note (self-imposed, not user-instructed, consistent with this project's existing conventions -- e.g. the copyright discipline already applied to real `.srt` dialogue text)**: `sd1`'s real `interacts` entries name OTHER real players by their actual account usernames. Nothing in this project's own documentation, source comments, or this RESEARCH_LOG entry reproduces an actual username encountered while validating this feature -- only the field schema and the confirmed 6-value `type` set above are recorded. The `PlayerSaveFileDumpTool` CLI itself does print real field values when run (it's a diagnostic tool over a real server operator's own data, not project documentation), which is a deliberate, disclosed distinction, not an oversight.

**Scope note**: this stayed entirely on real client-local save-file content per rule 2 -- no wire-protocol/network work was touched, and nothing here relates to EXENS/anti-cheat/launcher internals.

## Part 41 (2026-09-02) -- wired the real range/damage-dropoff curve into actual combat, not just a dump-tool printout

Continuing code work. Part 39 found EXBO's own official confirmation of the `damageFalloff` boundary shape and implemented `WeaponEntry.damageAtDistanceLinearAssumption()`/`WeaponRegistry.effectiveDamageAtDistance()`, but nothing in `session/GameLoop.tickCombat` actually called them -- every combat hit still always used the flat, 0m-range `effectiveDamage` number, and `tickCombat`'s own doc explicitly said so ("no per-shot randomness, no range/falloff model is applied to firearm damage either... that stays an honest 'not yet done'"). This part closes that gap.

**What changed**: a new `GameLoop.tickCombat(player, weapon, target, distanceMeters, location, sink)` overload applies `effectiveDamageAtDistance` (instead of flat `effectiveDamage`) before any headshot/limb multiplier, for any weapon record that has a real falloff curve. The existing flat overload is unchanged and still available (both now share one internal `resolveFlatDamage` helper to avoid duplicating the headshot/limb/melee-midpoint/vs.-mutant logic). Weapons with no falloff curve (69 of 337 records) and all melee weapons (no range field exists for melee in this dataset at all) fall back to the identical flat behavior -- `distanceMeters` isn't silently treated as zero-effect for them, it's genuinely not applicable, and the code says so rather than guessing a range effect.

**Evidence discipline restated, since it now drives an actual gameplay number**: the flat-then-ramp-then-flat-then-zero boundary SHAPE is officially confirmed (EXBO's own docs, part 39) -- that part of what's now wired into combat is not a guess. The one thing that remains a disclosed, non-official assumption is the interpolation curve strictly between `damageDecreaseStart` and `damageDecreaseEnd` -- still plain linear interpolation, still named and documented as an assumption in the method that computes it, not verified against real captured in-game numbers. Wiring it into `tickCombat` doesn't upgrade that assumption to a fact; it's the same category of disclosed simplification this project already accepted for the melee light-attack midpoint pick (part 29/30) -- a necessary, honestly-labeled choice where the underlying data doesn't fully specify behavior, not a hidden guess dressed up as reverse-engineered data.

`distanceMeters` is entirely caller-supplied, same pattern already established for `tickHazardCheck`'s hazard coordinates: `MobInstance` has no tracked world position (nothing in this project's evidence gives mobs one -- see that class's own doc on why even its health pool is a disclosed placeholder), so there is no real per-shot distance this class could compute itself.

**Validated with an actual run**, not just a clean compile: `session/GameLoopDemo.java` now uses a disclosed demo distance sequence (5m on a fresh-spawn headshot, +20m per following shot on the same target -- explicitly documented as demo scripting, not real telemetry) specifically chosen so a short run crosses a real weapon's ramp boundaries. Ran against real `weapons.json` AK-12 data (`startDamage=37.5`, `damageDecreaseStart=18`, `endDamage=29.9`, `damageDecreaseEnd=72`): at 5m (inside `decreaseStart`) a headshot applied 52.5 damage (37.5 x 1.4 headshot multiplier, full flat startDamage, correct); at 25m (inside the ramp) a body shot applied 36.5 damage -- hand-calculated against the disclosed linear formula (`t = (25-18)/(72-18) = 0.1296`; `37.5 + 0.1296 * (29.9-37.5) = 36.5`) and confirmed to match exactly, not just "the program didn't crash."

**Scope note**: purely local combat-math wiring on top of already-evidenced data; no wire-protocol/network work touched, nothing relates to EXENS/anti-cheat/launcher internals.

## Part 42 (2026-09-02) -- two more real, live-written local client-state files parsed (quests.cfg, waypoints.cfg); real evidence of an internal developer/QA tool suite found in keybindings

Continuing to catalog the real `config/` directory the per-character save-file discovery (part 40) came from. Three more real files, all plain JSON this time (not NBT): `config/quests.cfg`, `config/waypoints.cfg`, `config/keybindings`. New package `clientdata/` with `QuestProgress.java`, `Waypoint.java` + `WaypointList.java`, `KeyBindings.java`, and a combined `ClientDataDumpTool.java` CLI.

**quests.cfg**: `{readQuests:[...], hiddenQuests:[...]}`, bare numeric quest ids -- already used for cross-validation in part 40, now actually has a loader. **Confirmed live-written, not static**: read twice on the same real install this session, `readQuests` grew from 73 to 74 real entries (id 11863 newly appeared) between reads, with no other change -- direct proof the running client is writing this file during normal play, not just at startup/shutdown.

**waypoints.cfg**: a JSON array of death-marker waypoints (`pos:{x,y,z}`, `name`, `icon_index`, `color`, `can_position_float`, `time`, `type`). Only the real `"death"` type has been observed (kept as a raw string, not a closed enum). **Confirmed to follow the same current/`_old`-backup rotation pattern already established for the per-character save files (part 40), this time on a top-level `config/` file**: the same real death-waypoint present in one session's `waypoints.cfg` had, in the next real read, moved to `waypoints.cfg_old` with fresh coordinates and a later timestamp (the account's character died again in the meantime -- this is a real, live install the user is actually playing on), while the live file was back to an empty `[]`.

**keybindings**: 314 real action-name -> keycode bindings. **New real finding, not previously documented anywhere in this project**: only 109 of the 314 are ordinary `key_key.*` gameplay binds; the other 205 -- in a mix of English and Russian -- name what is unmistakably an internal developer/QA tool suite shipped inside the real client. Real examples: a Mutant Editor, Item Editor, Weather Editor, Warp Editor, Scene Editor, Behavior Editor; debug HUDs/overlays for FPS, memory, FMOD audio, player position, navmesh, collision boxes; config-reload actions (chunks, items, languages, handbook, resources/shaders); a "No clip" toggle; and debug/result screens for Prestige divisions, Operations, and Arcade Team Battles -- systems this project has independently found evidence of elsewhere (e.g. Prestige in old-log sweeps, part 31-era work). This is real, first-hand "possible cut/hidden content" evidence in exactly the sense the user asked about at project start (rule 2 explicitly scopes OUT EXENS/anti-cheat/launcher internals, not game-engine developer tooling, so this is in scope). `KeyBindings.nonGameplayActionNames()` only CATALOGS these real names and their real bound keys -- nothing in this project implements, invokes, or provides any way to unlock any of them; this stays a data-cataloging fact, same standard as `BankCatalog` cataloging real bank filenames without playing them.

Compiled clean (67 source files total now) and smoke-tested against real files from the live install -- `QuestProgress`/`WaypointList`/`KeyBindings` all parsed correctly, Cyrillic action names round-tripped through UTF-8 output intact.

**Scope note**: real local client-state files only, plain JSON, no wire-protocol/network work touched; the developer-tool keybind names are documented as a cataloged fact, not implemented or made invocable by this project, consistent with rule 2's EXENS/anti-cheat/launcher exclusion.

## Part 43 (2026-09-02) -- dependency-free unit test framework, 44 real regression tests, all passing; loot-config search documented as a negative result

**Why a framework had to be built from scratch**: this project has no Maven Central access (already documented under "Why no Netty (yet)" in the README -- `repo.maven.apache.org`, `repo1.maven.org`, and `maven.google.com` all return HTTP 403 from the egress proxy in this sandbox), so a real JUnit dependency cannot be fetched. Everything reused elsewhere in this project (`NbtReader`, `NbtWriter`, `JsonReader`, `CompressionUtil`) is already hand-written stdlib-only for the same reason, so a hand-written test harness is consistent with the project's existing approach rather than a new kind of workaround.

**What was built**, package `net.stalzone.emulator.testing`: `TestCase` (a `@FunctionalInterface` for a single test body that may throw), `Assertions` (static `assertEquals`/`assertTrue`/`assertThrows`/etc. methods that throw `AssertionError` with a descriptive message on failure), `TestRunner` (pairs a name with a `TestCase` as `NamedTest`, and `runAll(List<NamedTest>)` runs every test, prints one PASS/FAIL line per test with the failure message on failure, and prints a final `X passed, Y failed, Z total` summary), `AllTests` (the explicit aggregator -- collects every `*Test.java` class's `NamedTest` list by direct method call, no reflection/classpath scanning, matching this project's existing explicit-registration style used elsewhere, e.g. `ClientDataDumpTool`'s mode dispatch), and `TestMain` (the CLI entry point -- calls `AllTests`, then `System.exit(0)` on all-pass or `System.exit(1)` on any failure, so it's usable as a CI/build gate, not just for human-readable output).

**The 44 tests and why these specific real values were chosen as fixtures** -- every fixture uses a real evidenced value where the field represents real data, so a test failure would mean an actual regression against known-real behavior, not just an internal-consistency check against made-up numbers:

- `items/WeaponRegistryTest.java` (11 tests) -- fixture JSON built from the real AK-12 (`g4mdp`) and Gauss Rifle (`knmdv`) `weapons.json` records used throughout this project's part 41 combat-falloff work (`damage:37.5`, `headshotMultiplier:1.4`, `limbMultiplier:0.8`, and the AK-12's real disclosed falloff curve `startDamage:37.5 -> endDamage:29.9` between 18m-72m; the Gauss Rifle's real flat-then-drop shape `startDamage:247` held constant to 200m then dropping), plus one synthetic `zzz01` "Fake Knife" record (explicitly fake id/name, used only to test melee min/max damage fields in isolation without needing a second real weapon's exact numbers). Tests cover parsing, per-field access, and the falloff-vs-flat-damage branch this project's `GameLoop.tickCombat` (part 41) depends on.
- `dialogue/NpcIdGroupingTest.java` (5 tests) -- real `npcId` strings including a real spelling-variant case already documented earlier in this project (two npcIds for what is evidently the same real NPC, differing only by a spelling variant), verifying the grouping logic treats them as intended rather than silently merging or splitting incorrectly.
- `mobs/MobInstanceTest.java` (6 tests) -- health/damage clamping (health cannot go negative, cannot exceed max, death triggers at exactly zero), using the disclosed placeholder health pool this project has always been explicit about (not a real evidenced value, since no real per-mob health pool has ever been found in client data -- see parts 29/30).
- `session/save/PlayerSaveFileTest.java` (7 tests) -- includes a genuine NBT round-trip: builds a synthetic in-memory map, serializes it with `NbtWriter.writeRoot` (previously unused/untested anywhere in this codebase despite existing since early sessions), writes the bytes to a temp file, and loads it back through `PlayerSaveFile.load(Path)` -- the class's only real entry point, since its constructor is private. This is the first test in the project to exercise `NbtWriter` at all, closing a real gap (a writer that had shipped untested). The one synthetic username needed for this fixture is the explicitly-fake placeholder `"not_a_real_player"`, consistent with this project's standing policy against committing any real player's username seen in real save data.
- `clientdata/QuestProgressTest.java` (4 tests) -- real quest ids from the same `quests.cfg`/`sd2` cross-validation documented in part 40/42.
- `clientdata/WaypointListTest.java` (3 tests) -- the exact real death-marker JSON structure confirmed live-written mid-session in part 42 (same `"death"` type string, same current/`_old` rotation shape).
- `clientdata/KeyBindingsTest.java` (8 tests) -- real action-name/keycode fixtures drawn from the 314 real bindings documented in part 42, including the real two-code case (a binding with both a primary and a secondary key, preserved in order) and a Cyrillic action name round-tripping intact.

**Result**: 79 total `.java` source files now compile clean (0 errors); `TestMain` reports **44 passed, 0 failed, 44 total**, exit code 0. Every test passing on first full run is itself weak positive evidence that the logic under test (damage falloff math, quest/waypoint/keybinding parsing, mob health clamping, NBT save-file round-tripping) matches its documented real-data basis -- a regression in any of that logic going forward will now fail loudly instead of silently.

**Negative result, formally recorded**: the "163 additional loot configs" / "Random loots parsed in 106 ms" real log lines (`logs/client-0.log`, `[Game Startup/INFO]`, immediately preceding the already-documented `hedge.anm.json`/`grenade.anm` warnings) were investigated across multiple passes this project -- searching for `*loot*`-named files/directories under `modassets/`, and checking the `modassets/assets/shop/` case-model folders whose names correspond to the real keybinding-suite entries "Caes/Case Drop Rates: Shufflebag/Treasury/Loot table" -- and found no JSON loot-table or drop-rate config file anywhere in the client install; only 3D models and sound assets exist under those paths. Combined with the already 4x-confirmed absence of any real per-zone mob spawn table in client files, the working (not yet certain) conclusion is that loot/drop-rate tables, like spawn tables, are server-authoritative only and never shipped to the client. This search was not exhaustive -- other asset roots (e.g. `gloomycore`) have not been explicitly swept for loot-related JSON -- so this remains an open item, not a closed one, and should not be upgraded to a stated fact without further checking per rule 1.

**Scope note**: no wire-protocol/network work touched; this is a pure testing/quality-assurance addition over already-evidenced logic, plus one documented negative search result.

## Part 44 (2026-09-02) -- config/options.json parsed (159 real client settings keys); real byte-level evidence of float32-bits-as-int32 and packed-ARGB-color serialization in two of them

**What was read**: `config/options.json`, a real, plain-JSON, flat object of 159 top-level client settings keys, found alongside `quests.cfg`/`waypoints.cfg`/`keybindings` in the same live `config/` directory (already documented in part 42), now given its own class (`clientdata/ClientOptions.java`) rather than staying an undocumented aside. Confirmed the same current/`_old` rotation shape as the other `config/` files applies here too (both copies were byte-identical at read time, which is the expected steady state between writes -- not new evidence either way, just consistent with the already-established pattern).

**The 159 real keys span**: graphics/rendering quality (`render_scale`, `antialiasing_mode`, `shadows_dynamic`, `shadows_pcf`, `shadows_map_size`, `soft_lighting_mode`, `grass_flag`, `grass_exp`, `tree_models`, `particle_render_distance`, `bloom`, `godrays_enabled`, `ssao`, `water`, `filtering_mode`, `texture_quality_blocks`, `texture_quality_objects`, `objects_render_distance`, `object_fade_mode`, `details_amount`, `bullet_holes`, `equipped_rendering`, `blur_hud_elements`, `fps_limit`, `vsyncEnabled`), audio channel volumes with a real trailing-underscore naming convention (`master_volume_`, `sound_volume_`, `dialogs_volume_`, `radio_volume_`, `detectors_volume_`, `annotator_volume_`, `music_volume_`, `phonograph_volume_`), voice chat (`vx_enable`, `vx_enable_proximity`, `vc_activation_method`, `vc_speakers_level`, `vc_mic_level`, `vx_vad_hangover`, `vx_vad_sensitivity`, `vx_capture_device`), real network-tuning-sounding client knobs (`protocol`, `send_rate_hint`, `chunk_net_caching`, `tunnel_selection_mode`, `dynamic_fps_limit`, `multithread_loading`, `memory_mapping`, `collect_metrics` -- catalogued as real plaintext option values only, no attempt made to interpret what any of them actually does server-side, consistent with the standing wire-protocol gate), gameplay/UI toggles (HUD elements, waypoint rendering, hit markers, death-point markers, handbook notifications), zoom-level sensitivity curves (14 real `sensitivity_zoom_<multiplier>` keys), and `language`/`language_voiceover` (both `"en"` on this real install). `vc_speakers_level`'s real value (`60.408165`) is a non-round, evidently user-tuned number -- further, minor evidence this file reflects real live settings rather than defaults.

**Real, byte-level-confirmed finding, not previously documented anywhere in this project**: 4 of the 159 real keys hold integer values many orders of magnitude larger than every other numeric field -- `admin_hotbar_scale=1075838976`, `admin_inv_icon_scale=1065353216`, `world_outliner_width=1073741824`, `world_outliner_color=1711276032`. Reinterpreting each as raw big-endian bytes:

- `admin_hotbar_scale` = `0x40200000` = the exact IEEE-754 binary32 bit pattern of `2.5f`
- `admin_inv_icon_scale` = `0x3f800000` = the exact IEEE-754 binary32 bit pattern of `1.0f` -- a universally-known constant; this is not a coincidental integer match, it requires exact alignment of the sign/exponent/mantissa bits
- `world_outliner_width` = `0x40000000` = the exact IEEE-754 binary32 bit pattern of `2.0f`

All three decode to small, round, plausible UI-scale multipliers (1x / 2x / 2.5x for an icon scale, hotbar scale, and outliner panel width respectively), which is strong evidence these specific fields are serialized as a raw float32-bits-as-int32 reinterpret rather than a decimal number -- most likely a generic engine property-serialization codepath that, for a `float`-typed property, writes `BitConverter` bits (or an equivalent reinterpret-cast) instead of the decimal value. `world_outliner_color`, by contrast, does NOT decode to a clean float (`0x66000000` -> ~1.5e23) but its bytes read as a plausible packed ARGB color instead: alpha byte `0x66` (102/255, a semi-transparent overlay) over RGB `000000` (black) -- entirely consistent with what a "world outliner" UI highlight/overlay tint color would sensibly be.

**Scope discipline applied (rule 1)**: this is confirmed only for these 4 specific real keys on this real file -- it is NOT asserted as a general rule that every large-magnitude integer option value in this or any other file uses either encoding. `ClientOptions.getFloatBits(String)` and `ClientOptions.getArgbComponents(String)` are generic bit-reinterpret utilities a caller can apply to any key; they don't themselves claim any other key uses either encoding, and the class doc says so explicitly. A search of all 159 real keys found no other candidates (every other numeric value is small and unremarkable).

**Verification**: 81 total `.java` source files now compile clean (0 errors); the full test suite is now 54 tests, 0 failures (10 new: `clientdata/ClientOptionsTest.java`, using real key names and the real fixture values above, including exact-equality checks on the 3 float-bit decodes and the 4-component ARGB decode). Additionally smoke-tested end-to-end against the real, live `options.json` staged from the real install -- all 159 real keys parsed correctly and the 4 special keys decoded to the exact same values computed independently by hand (2.5, 1.0, 2.0, and alpha=102/black). md5-verified byte-exact sync between the on-device source files and the cloud-compiled/tested copies for all 4 changed/new files (`ClientOptions.java`, `ClientOptionsTest.java`, `ClientDataDumpTool.java`, `testing/AllTests.java`).

**Scope note**: real local client-state file only, plain JSON, no wire-protocol/network work touched -- the network-tuning-sounding keys (`protocol`, `tunnel_selection_mode`, etc.) are catalogued as real plaintext values only, nothing about their real runtime effect is inferred or claimed.

## Part 45 (2026-09-02) -- config/prefs decoded: a 5th real client-state file, confirmed Zstandard+NBT (same format as save data), real evidence of an "arcades" mode and a seasonal/episodic content structure

**What was found**: `config/prefs` (top-level, account-wide -- same `config/` directory as `quests.cfg`/`waypoints.cfg`/`keybindings`/`options.json`, NOT the per-character `common` save slot, which is a separate real file already documented in `PlayerSaveFile`'s class doc as holding similar-but-per-character UI state) had never been parsed by this project; earlier session notes only had it flagged "binary/opaque, no evidence of format." Real magic-byte inspection of the real file's first 4 bytes gave `28 B5 2F FD` -- the standard Zstandard frame magic, the exact same signature already confirmed for `PlayerSaveFile`/`VendorArchive`. Ran the project's own existing `nbt/NbtDumpTool.java` (decompress via `CompressionUtil` + parse via `NbtReader`, already-validated tools, no new binary-format code needed) against the real file: clean parse, **0 leftover bytes** after decompressing 787 real compressed bytes to 1821 real NBT bytes. Format confirmed by direct evidence, not assumption.

**Real top-level fields, all confirmed from the real parsed structure** (now wrapped in `clientdata/ClientPrefs.java`):

- 8 real one-shot "have I seen/done this" boolean flags (NBT `TAG_BYTE`, 0/1): `hasDoneInitialOptionsSetup`, `hasSeenShopAd`, `hasSeenEncagedHowToPlay`, `hasSeenPrestigeHowToPlay`, `handbookNotificationReceptionCalibrated`, `skipComparisonCategoryDeleteConfirmation`, `profileMusicMuted`, `notifyAboutEncagedRewards`.
- `arcadesMapFilterMask` (real `TAG_INT`, 0 on the real install), `arcadesSelectedMaps`/`arcadesFavoriteMaps` (real empty string lists on the real install). **Real cross-validation, new to this project**: these three fields corroborate the real "Arcade Team Battles" debug/result-screen keybinding names already documented in part 42 -- two independently-discovered real data sources now agree a real "arcades" mode with selectable/favoritable maps exists client-side.
- `favoriteCharacters`: real string list containing the account's own real character name (already documented elsewhere in this project as it's the account's own data).
- `seenArticleLinks`: real string list, 10 entries on the newer real copy. **Real finding, corrected during validation**: NOT purely Steam news URLs as first assumed from the first several entries -- one real entry was `handbook://articles/sessions/coliseum-informaciya-o-rezhime.md`, a real handbook article about what its own path names a "Coliseum" session mode, itself new evidence of a real game mode this project hadn't seen named anywhere else. The other 9 entries are real `https://store.steampowered.com/news/app/1818450/view/<id>` URLs (real AppID 1818450, already documented in `project_overview.md`).
- `seenExperiences`: real string list of 9 season/event/episode codes -- `pg_eu_challenges`, `triumph_episode_eu`, `260722_summer26_prestige_episode`, `ssop_help_experience`, `summer26_media_episode`, `experience_season_summer26`, `bp_weekend_episode`, `airdrop_forge_episode`, `experience_season_autumn26`. Real, first-hand evidence of a seasonal/episodic content structure -- battlepass-style "experience seasons" literally named after real seasons (summer26, autumn26), consistent with (and now more concrete than) the multi-year battlepass evidence already documented in part 40. Catalogued as real identifiers only; no attempt made to reconstruct what content each episode actually contains.
- `unseenHandbookEntries`: real string list of 10 `handbook://articles/<path>.md` URIs, the same real internal URI scheme already cross-validated in part 40 via `PlayerNotification`'s handbook channel. Real categories seen: `progression/`, `stats/`, `pvp/`, `base/`, `arthunt/`.

**Real live-write confirmation**: the real `prefs`/`prefs_old` pair were NOT byte-identical (unlike `options.json` at the time it was checked) -- `seenArticleLinks` held 10 real entries in the newer copy vs. 9 in the older, one new real Steam news URL appended between writes. Same current/`_old` rotation pattern already established for every other real per-file pair in this project, now confirmed for a 5th real file.

**Verification**: 83 total `.java` source files compile clean (0 errors); test suite grew from 54 to 62 tests (8 new: `clientdata/ClientPrefsTest.java`, built from a synthetic field map matching the exact real NBT shapes observed, round-tripped through the real `NbtWriter`/`NbtReader` pipeline -- not a real file, since privacy discipline keeps even the account's own real character name out of this project's fixtures, replaced with the standard `not_a_real_player` placeholder; the real, non-personal season-code and handbook-path strings are kept exactly as observed). Additionally smoke-tested end-to-end against the real, live `prefs` staged from the real install -- output matched the initial `NbtDumpTool` dump field-for-field. md5-verified byte-exact sync between the on-device source files and the cloud-compiled/tested copies for all 4 changed/new files (`ClientPrefs.java`, `ClientPrefsTest.java`, `ClientDataDumpTool.java`, `testing/AllTests.java`).

**Scope note**: real local client-state file only, no wire-protocol/network work touched; the "arcades"/season-code findings are catalogued as real identifiers only, consistent with rule 2 (game content and cut/hidden-content evidence is in scope, EXENS/anti-cheat/launcher internals are not -- none of this touches the latter).

## Part 46 (2026-09-02) -- PlayerSaveFile's sd3/sd4 slots finally get typed accessors: real cross-character shop-catalog validation, a real World of Tanks crossover finding, and a byte-level-confirmed little-endian uint16 decoding of observedElements

**Why now**: part 40's `PlayerSaveFile` doc explicitly left `sd3`/`sd4` as raw-fields-only, citing thin evidence (one real sample each). Since then the user has kept playing for real, and three real characters (`AveghiliaBoy`, `Buji`, `WolfZero`) now have real `sd3`/`sd4` data -- enough to cross-validate a real schema instead of guessing from one sample. Used the project's own existing `nbt/NbtDumpTool.java` (already-validated tool, no new binary-format code) to get authoritative structural dumps of all real samples before writing any typed code.

**sd3 (`observedOffers`), confirmed real cross-character**: a single top-level list of real Cyrillic shop/cosmetic-case offer names. Real sample sizes: `AveghiliaBoy` 108 entries, `WolfZero` 102 entries, `Buji` 14 entries (grew from 5 in `Buji`'s `_old` copy -- more real live-write evidence, same rotation pattern already established elsewhere). The SAME real offer names recur across all three independent real characters (e.g. `"Набор «Суеверия»"`, `"Мотив «Бронебойный»"`, `"season/Продвинутые запчасти"`) -- confirms this is one shared real shop catalog being observed by each character, not per-character random/synthetic data. Real offer-id namespacing observed: bare names, plus `season/`-, `customization/`-, and `sequence/`-prefixed variants (category namespaces, not explained further -- no evidence found for what distinguishes them functionally beyond the prefix itself).

**New, unexpected real finding**: several real `customization/`-prefixed offers name a real World of Tanks crossover -- `customization/Набор Мир Танков Маневры` ("World of Tanks Maneuvers Set") and `customization/Набор Мир Танков Поля` ("World of Tanks Fields Set") -- alongside real "Golden ships"/"Golden tanks" (`Золотые корабли`/`Золотые танки`) and numbered "Ship 2"/"Ship 3" cosmetic offers. Catalogued as real observed shop-offer names only, same treatment as every other real item/cosmetic name already in this project (weapons.json entries, etc.) -- this is real, first-hand game-content evidence, squarely in scope per rule 2, not an assumption about what the crossover actually contains.

**sd4, confirmed real cross-character**: 4 real `TAG_BYTE` "last seen cosmetic-tab version" counters (`lastSeenTagsVersion`, `lastSeenBackgroundsVersion`, `lastSeenPatternsVersion`, `lastSeenStickersVersion`) plus one real `observedElements` byte array. **Real, byte-level-confirmed finding, not previously documented**: decoded `observedElements` as a flat sequence of little-endian `uint16` values from two independent real characters' real byte arrays --

- `AveghiliaBoy` (8 real bytes): `94 11 3b 13 68 10 c0 12` -> `[4500, 4923, 4200, 4800]`
- `WolfZero` (18 real bytes): `a8 11 94 11 96 11 68 10 c2 12 d4 12 c0 12 6a 10 7c 10` -> `[4520, 4500, 4502, 4200, 4802, 4820, 4800, 4202, 4220]`

Three EXACT values appear in BOTH independently-observed real sets: `4200`, `4500`, `4800`. This would not happen from an arbitrary or wrong byte grouping/endianness choice -- it's real, verifiable evidence the little-endian-uint16-pairs decoding is correct, the same standard of evidence already used for the `options.json` float32-bits finding in part 44. All 13 real decoded values fall in a tight range (4200-4923), consistent with a small, shared cosmetic-element id space (decal/pattern/sticker ids). Big-endian was also checked and rejected -- it produces no such cross-character overlap.

**Verification**: 83 total `.java` source files compile clean (0 errors, same file count as part 45 -- these were extensions to existing classes, not new files); test suite grew from 62 to 64 tests (2 new in `session/save/PlayerSaveFileTest.java`, one round-tripping a real-shaped `observedOffers` fixture through the real NBT pipeline, one round-tripping the REAL 8-byte `AveghiliaBoy` `observedElements` array and asserting the exact real decoded values `[4500, 4923, 4200, 4800]`). Additionally smoke-tested end-to-end against all the real staged `sd3`/`sd4` files from all three real characters -- output matched the hand-computed decode exactly. md5-verified byte-exact sync between the on-device source files and the cloud-compiled/tested copies for all 3 changed files (`PlayerSaveFile.java`, `PlayerSaveFileTest.java`, `PlayerSaveFileDumpTool.java`).

**Privacy note**: `observedOffers`/`seenExperiences`-style shop and cosmetic item names are real game content, not player-identifying data, and are used verbatim in this project's own docs/tests (unlike real other-player usernames, which are never reproduced, per this project's standing policy -- see `PlayerInteraction`'s doc). The `observedElements` fixture bytes are real device data but are cosmetic-element ids, not personal data.

**Scope note**: real local per-character save-data files only (already-documented `session/save/` package), no wire-protocol/network work touched; the World of Tanks crossover and cosmetic-element-id findings are catalogued as real observed data only, consistent with rule 2.

## Part 47 (2026-09-02) -- PlayerSaveFile's last remaining slot, common, gets typed accessors: real case-open-count tracking, real Operations map difficulty, and real first-hand evidence of DMZ and Reichstag game modes

**Why now**: with real `common` samples now available from two real characters (`AveghiliaBoy`, `WolfZero`), the schema that part 40 left as raw-fields-only ("client UI/preference state... no server-authoritative game data found here") could finally be cross-validated instead of guessed at from one file. Used the project's own `nbt/NbtDumpTool.java` again for the first authoritative structural dump, same disciplined approach as parts 45 and 46.

**Real top-level shape, confirmed across two real characters** (~20 real one-shot `TAG_BYTE` "seen/opened" UI flags, several real but currently-empty `TAG_LIST`s whose element type is therefore still unconfirmed, and two well-evidenced nested structures):

- `caseLastOpenCount.caseLastOpenCount`: a real nested list of `{caseId: TAG_INT, openAmount: TAG_INT}` entries. Both real characters have exactly one real entry, both with the SAME real `caseId` (15081) but DIFFERENT real `openAmount` (20 vs 1) -- confirms this is a genuine per-character running counter for a specific real case/gacha-box id, not a fixed constant. New typed wrapper: `session/save/CaseOpenCount.java`.
- `lastOpsDifficultyByMap`: a real top-level list of `{id: TAG_STRING, difficulty: TAG_INT}` entries. `WolfZero`'s real sample has 2 real entries -- `{id: "big_cleanup", difficulty: 1}`, `{id: "sea_alienation", difficulty: 1}` -- real internal Operations map identifiers not documented anywhere else in this project (`AveghiliaBoy`'s real sample has 0 entries, i.e. no Operations played yet on that character -- an empty real list, not evidence against the schema). New typed wrapper: `session/save/OpsMapDifficulty.java`.

**New real finds, directly from real flag/key names** (no interpretation needed -- the names ARE the finding):

- `hasSeenDMZHowToPlay` -- real, first-hand evidence of a "DMZ" game mode, not previously named anywhere in this project's research.
- `hasSeenReichstagHowToPlay` -- real, first-hand evidence of a "Reichstag" game mode or location (the real WWII Berlin building name), also new to this project.
- `snowmanFeed` (nested: `isFilterPanelOpen`/`isSetFilterOpen`/`showToysOnTree`/`disabledToySets`) and `nyVisitedTabs` -- real evidence of a New Year seasonal decoration/event feature.
- `phonographListenList` (nested `records`/`tracks`, both real but empty in both real samples) -- real evidence of an in-game phonograph/jukebox feature, distinguishing "records" from "tracks" as two separate real lists.
- `personalShopOpenedCards` (real, empty compound in both samples) -- real evidence of a player-to-player personal shop/marketplace feature.
- `complaintsData` (nested `lastChatReport`/`reportedMailIds`/`reportedProfiles`/`reportedHacks`/`reportedCharnames`, all empty in both real samples) -- real evidence of an in-client player-reporting/moderation UI. Scope discipline: this is UI-side report-category bookkeeping only, no moderation/anti-cheat engine internals touched or implied -- consistent with rule 2's EXENS/anti-cheat exclusion.
- `arcadesMenuOpened` (real `TAG_BYTE`, true on both real characters) -- a THIRD independent real source corroborating a real "arcades" mode, after the part 42 keybindings dev-tool names and the part 45 `prefs` `arcadesMapFilterMask`/`arcadesSelectedMaps`/`arcadesFavoriteMaps` fields. Three independently-discovered real data sources now agree.

**Verification**: 85 total `.java` source files compile clean (0 errors -- 2 new files, `CaseOpenCount.java` and `OpsMapDifficulty.java`); test suite grew from 64 to 65 tests (1 new, round-tripping real field names/shapes -- real `caseId` 15081 and real map ids `big_cleanup`/`sea_alienation` kept exactly as observed, since these are game data, not player-identifying data). Additionally smoke-tested end-to-end against both real characters' real `common` files -- output matched the initial `NbtDumpTool` dump and the hand-computed real values exactly (`AveghiliaBoy`: caseId=15081/openAmount=20, 0 ops entries; `WolfZero`: caseId=15081/openAmount=1, 2 ops entries matching the real map ids above). md5-verified byte-exact sync between the on-device source files and the cloud-compiled/tested copies for all 5 changed/new files (`PlayerSaveFile.java`, `PlayerSaveFileTest.java`, `PlayerSaveFileDumpTool.java`, `CaseOpenCount.java`, `OpsMapDifficulty.java`).

**Scope note**: real local per-character save-data file only (already-documented `session/save/` package), no wire-protocol/network work touched; the DMZ/Reichstag/arcades findings are catalogued as real observed flag/key names only -- nothing about what these modes actually contain or how they function is inferred or claimed, per rule 1.

## Part 48 (2026-09-02) -- lootRateMultiplier finally wired: an emulator-original, JSON-configurable loot system, plus a quick negative-result recheck of the still-unresolved mg/pd/td zone codes

**A quick negative result first, before the main work**: before starting new feature work, re-checked this session's substantial new real string data (real `keybindings` action names, real `prefs` fields, real `sd3` shop-offer lists from 3 characters, real `common` fields from 2 characters -- none of which existed as a checkable source when parts 26/35/36 last looked) for the still-unidentified real zone codenames `mg`/`pd`/`td` (part 26). A programmatic substring grep across all of it found zero matches -- extends the negative-result streak with genuinely new real data, still unresolved. Not worth its own part; recorded here for completeness per rule 1 (a checked-and-still-open lead should stay visibly checked, not silently dropped).

**Main work: `ServerConfig.lootRateMultiplier` was the last of the project's three headline tuning fields (`spawnRateMultiplier`, part 29; `damageMultiplier`, part 19) left completely unwired**, explicitly flagged as such in `ServerConfig`'s own class doc since it was written. Unlike the other two, wiring it up could not mean "scale a real number this project found" -- there is no real number. This project already spent real effort looking for a client-side loot/drop-rate table (part 43's "163 additional loot configs" log-line investigation) and concluded one is very likely absent from client files by design, the same conclusion already reached (4x independently) for a real per-zone mob spawn table. So this part is different in kind from almost everything else in this log: it is original emulator functionality, not reverse-engineering, built because the project's own stated goal ("full control over what can be done, like spawn rates or damage values" plus modding support) requires SOME loot behavior for a playable private/singleplayer server, and the real game gives no evidence to reverse-engineer here. Every new class's Javadoc says this explicitly and up front, not just this log entry.

**What was built**, package `net.stalzone.emulator.loot`: `LootEntry` (itemId + count range + weight, validated), `LootTable` (a drop chance + a weighted list of `LootEntry`), `LootTables` (loads a plain JSON array keyed by real mob `configId`, same loading style as `WeaponRegistry`), `LootDrop` (one rolled outcome), and `LootRoller` (the actual RNG logic -- captures `lootRateMultiplier` from `ServerConfig` at construction, same pattern `SpawnScheduler` already established for `spawnRateMultiplier`). Roll semantics (an emulator design choice, documented as such): `effectiveChance = clamp(dropChance * lootRateMultiplier, 0, 1)`; one roll decides whether anything drops; if so, exactly one entry is picked by relative weight and its count rolled uniformly in `[minCount, maxCount]`. `LootTableDumpTool.java` is the CLI (`dump` prints a file's tables; `simulate` rolls a mob id N times and tallies outcomes -- e.g. a 1000-roll simulation against the example `base_boar` table with `dropChance=0.5` produced a 52.5% no-drop rate, matching the configured chance; `lootRateMultiplier=2.0` against the same table correctly clamped to 0% no-drop).

**Wired into `GameLoop`**: a new `setLootTables(LootTables)` method (a `GameLoop` that never calls it behaves identically to before -- no default loot, opt-in only); both `tickCombat` overloads now call a shared `handleDeath` helper on a fresh kill, which rolls loot for the dead mob's real `configId` and reports the outcome (including an explicit "dropped nothing" line, both when the roll fails AND when the mob's id simply has no configured table) via the same `EventSink` every other event already uses. `GameLoopDemo` gained two new optional trailing CLI args (`lootTables.json`, `lootRateMultiplier`) so a demo run can opt in the same way `spawnRateMultiplier`/`damageMultiplier` already do.

**`data/loot/example_loot_tables.json`** (with its own `README.md` reiterating the "not real data" disclosure) is a small example file: real mob ids (`base_boar`, `base_tushkan`, real `.smm` config stems) and real item ids (`16340`/`16347`, reused from `GameLoopDemo`'s own existing demo inventory) but entirely invented `dropChance`/`weight`/count values -- an operator or modder is expected to replace it with their own.

**Verification**: 94 total `.java` source files compile clean (0 errors, 9 new files); test suite grew from 65 to 85 (20 new, property-style tests using seeded `Random` across many seeds rather than exact-value assertions where the outcome is genuinely random by design -- `dropChance=0`/`lootRateMultiplier=0` never drop across 20 seeds each, `dropChance=1`/clamped-multiplier always drop across 20 seeds each, rolled counts stay in `[min,max]` across 50 seeds, every weighted entry is reachable across 200 rolls, plus deterministic validation/parsing tests). Additionally smoke-tested end-to-end two ways: `LootTableDumpTool simulate` against the real example file (1000 rolls, both default and 2x multiplier, both matching expected statistics), and a full real `GameLoopDemo` run against real Milestone-1 zone/mob/weapon data with the loot table wired in -- two real kills during the run (`flesh`/`base_flesh_clone`, `krovosos`/`base_krovosos_clone`) correctly reported "dropped nothing" since neither mob id is in the example table, exercising the "unconfigured mob" path in real integrated code, not just in isolation. md5-verified byte-exact sync between the on-device source/data files and the cloud-compiled/tested copies for all 15 changed/new files (13 `.java` files, `example_loot_tables.json`, its `README.md`).

**Scope note**: no wire-protocol/network work touched; this entire part is explicitly-disclosed emulator-original functionality, not reverse-engineered STALZONE data -- see the repeated disclosures above and in every new class's own Javadoc for why, and to keep rule 1 discipline visible rather than assumed.

## Part 49 (2026-09-03) -- MobHealthTable: the last unwired "emulator placeholder" (MobInstance.DEFAULT_MAX_HEALTH) becomes an opt-in, JSON-configurable, per-mob override

Same shape and same reason as part 48's loot system, applied to the OTHER long-standing disclosed
placeholder in this codebase: `MobInstance.DEFAULT_MAX_HEALTH` (flat 100.0, introduced part 30) has
always been a single emulator-wide number with no way for an operator to vary it per mob type, even
though `MobConfig`'s own class doc has confirmed since part 10 (re-checked part 20, re-confirmed
again while building `MobInstance` in part 30) that real `.smm` mob config files carry NO health/HP
field anywhere in their schema -- so there was never any real data to distinguish a boar's toughness
from a pseudogigant's in the first place. New package member `mobs/MobHealthTable.java`: loads a
flat JSON object of `configId -> maxHealth` (deliberately simpler than `loot/LootTable`'s shape,
since there's nothing else to configure per mob yet), `maxHealthFor(id)` returns the configured
value or unconditionally falls back to `MobInstance.DEFAULT_MAX_HEALTH` (no null check needed by
callers), `unconfigured(ids)` reports which real mob ids have no entry (for the dump tool below).
**This is emulator-original tuning, not reverse-engineered STALZONE data, and the class doc says so
up front** -- same disclosure standard as `loot/LootTable`, `MobInstance.DEFAULT_MAX_HEALTH` itself,
and `SpawnScheduler`'s spawn cadence.

Wired into `session/GameLoop.java`: a new nullable `healthTable` field + `setMobHealthTable(...)`
setter, same opt-in pattern as part 48's `setLootTables(...)` -- existing behavior is completely
unchanged unless an operator calls it. `tickSpawns()` now resolves `maxHealth` via
`healthTable.maxHealthFor(spawned.configId())` when a table is set (else the same flat default as
before), then constructs `new MobInstance(spawned, maxHealth)` using `MobInstance`'s existing
two-arg constructor -- no change needed to `MobInstance` itself, it already supported an explicit
health value, `tickSpawns()` just never passed one before. `session/GameLoopDemo.java` gets one new
optional trailing CLI arg, `[mobHealthTable.json]`, loaded and wired the same way
`[lootTables.json]` was in part 48. New CLI tool `mobs/MobHealthTableDumpTool.java`: `dump` prints
every configured override and, given a real mob-configs directory, flags which real mob ids fall
back to the default; `get` prints one mob id's resolved value.

**Real-data verification**: `data/mobs/example_mob_health.json` configures 6 real mob ids
(`base_boar`, `base_tushkan`, `base_flesh`, `base_dog`, `base_pseudogigant`, `base_chimera` --
all real `.smm` config-file stems, invented `maxHealth` values, disclosed in that directory's own
`README.md`) -- running `MobHealthTableDumpTool dump` against this file AND the real, full 35-file
`stalkermobs/configs` directory correctly reports exactly 6 configured overrides and the other 29
real mob ids (`base_snork`, `base_krovosos`, `base_tsarevich`, etc.) falling back to
`DEFAULT_MAX_HEALTH=100.0` -- confirming both paths (`maxHealthFor` and `unconfigured`) against the
real, complete mob roster rather than just the fixture. `MobHealthTableDumpTool get` spot-checked
both an overridden id (`base_chimera` -> 350.0) and an unconfigured real id (`base_snork` -> 100.0)
end to end through the compiled CLI. Compiled clean (97 total sources, zero warnings) and the full
test suite passed 92/92 (85 prior + 7 new `MobHealthTableTest` cases: load count, configured-id
lookup, unconfigured-id fallback, `unconfigured()`'s real-id-list diffing, and all three malformed-
input rejection paths -- non-object top level, non-numeric value, zero/negative value). md5-verified
byte-exact sync between the on-device source/data files and the cloud-compiled/tested copies for
all 7 changed/new files (`mobs/MobHealthTable.java`, `mobs/MobHealthTableTest.java`,
`mobs/MobHealthTableDumpTool.java`, `session/GameLoop.java`, `session/GameLoopDemo.java`,
`testing/AllTests.java`, `data/mobs/example_mob_health.json`).

With this part, both of `MobInstance`'s and `GameLoop`'s disclosed emulator-original design
choices from parts 29/30 (spawn cadence and health pool) now have an operator-facing, JSON-
configurable path alongside `loot/` (part 48) -- all three of this project's headline "full
control over what can be done, like spawn rates or damage values" tuning surfaces (spawn rate,
damage, loot rate) plus this newer health-pool knob are now opt-in configurable rather than
hardcoded, without ever claiming any of the four is reverse-engineered STALZONE data.

**Scope note**: no wire-protocol/network work touched; this entire part is explicitly-disclosed
emulator-original functionality, not reverse-engineered STALZONE data -- same rule 1 discipline as
part 48.

## Part 50 (2026-09-03) -- min_food.txt: the last unread real plain-text config file in modassets/assets/stalker/, confirming a real (but almost entirely unevidenced) STALZONE hunger/food survival mechanic

Went looking for what real client config data this project had NOT yet read, rather than extending
an existing placeholder (parts 48/49's pattern). Swept every subdirectory of
`modassets/assets/` for text-based config files (`.txt`/`.json`/`.cfg`/`.csv`/`.dat`) in
directories never previously examined: `auction/`, `currency/`, `shop/`, `trade/`, `party/`,
`stalkerclans/`, `stalkerguide/`, `chat/`, `_jurisdiction/`, `handcuffs/`. Found two real hits:
`auction/fonts/dictionary.txt` (confirmed, on inspection, to be just a font glyph charset for UI
text rendering -- not game content, not pursued) and `party/weapon_levels.cfg` (12 bytes, one
line, `25000=NEWBIE` -- a single real key-value pair, too thin on its own to build anything
reliable from; noted here rather than silently dropped, not pursued further this part). Also
found `config/cluster` (STALZONE install's `config/` folder, 2 bytes, `"EU"`) -- a real file,
but a server-region/cluster selection code, i.e. launcher/matchmaking-adjacent rather than game
content, so out of scope per rule 2 and not pursued.

The one real, in-scope, followed-up find: `modassets/assets/stalker/min_food.txt` -- the third
real plain-text file in that exact directory (alongside `weight.txt`/`ItemWeightTable`, part 21,
and `detectors.txt`/`DetectorConfig`, part 22, and `moving.txt`/`MovementConfig`, part 23),
confirmed by direct byte inspection (`od -A x -t x1z`) to be exactly 2 bytes, `31 30` (ASCII
`"10"`), no trailing newline or CR. This project has now read every real plain-text config file
in `modassets/assets/stalker/`. New class `survival/MinFoodConfig.java` (new package --
genuinely the first survival/hunger-adjacent code in this codebase) loads the single bare
integer and exposes exactly one accessor, `minFood()`.

**What is and is not evidenced, stated carefully per rule 1**: the file, its exact byte content,
and its location in the real client install are directly confirmed. What the value `10` actually
MEANS is NOT evidenced -- searched real `config/keybindings` and `config/options.json` for any
food/hunger-related key (none found), and searched every real path anywhere under
`modassets/assets/` matching `food`/`hunger`/`thirst`/`water` for a second corroborating data
file (none found -- the results were exclusively models/textures/sounds, e.g. `food_bread.ol`,
`water_collector.mcsb`, plus one promising-looking real directory,
`stalker/hungerBuffs/`, which on inspection contains only a single UI icon sprite sheet,
`kit.mic`, not a data file). So this project can say with confidence that STALZONE has SOME real
hunger/food survival mechanic with a minimum-food threshold of `10`, but cannot say what unit
that's in (a 0-100 percentage? a raw summed food-item value? something else?), what happens
below it, or whether any decay/consumption rate exists anywhere in client data -- none of that
is invented. Unlike `loot/` (part 48) and `mobs/MobHealthTable` (part 49), there is no stated
project goal ("full control over spawn rates or damage values") that needs an invented hunger
simulation here, so `MinFoodConfig` is deliberately NOT wired into `GameLoop` this part --
it stays a standalone, honestly-scoped real-data reader, same category as `ClientPrefs`/
`ClientOptions` exposing exactly what a real file contains and no more.

**Main work — confirmed by build+run in the cloud container (100 source files, 0 compile
errors), md5-verified byte-exact against device copies for all 4 changed/new files, and
smoke-tested against the real file**: `survival/MinFoodConfig.java` +
`survival/MinFoodConfigDumpTool.java` (CLI, `dump`-style single-value print) +
`survival/MinFoodConfigTest.java` (5 new tests: exact real-content fixture, whitespace
tolerance, two malformed-input rejection paths, `toString()`). Test suite grew from 92 to 97.
`MinFoodConfigDumpTool` run directly against a staged, md5-verified copy of the real
`min_food.txt` (md5 `d3d9446802a44259755d38e6d163e820` on both device and cloud copies) printed
`minFood() = 10`, confirming the parser end to end against the real file byte-for-byte, not just
a fixture.

**Scope note**: no wire-protocol/network work touched. The `party/weapon_levels.cfg` and
`config/cluster` findings above are recorded rather than silently dropped (rule 1 discipline)
but were deliberately NOT built into classes this part -- one is too thin (a single key-value
pair) to build anything reliable from, the other is launcher/matchmaking-adjacent and out of
scope per rule 2. Either could be revisited if more corroborating real data ever surfaces.

## Part 51 (2026-09-03) -- item_tiers.txt, advices_en/ru.txt, and logged_trader_item_groups.txt: three more real, previously-unread plain-text files, found by a systematic sweep of every real text config file in the client

Continued part 50's approach (hunt for unread real data rather than extend an existing
placeholder), but made it systematic this time: built a complete inventory of every real
plain-text config file (`.txt`/`.json`/`.cfg`/`.csv`/`.dat`) anywhere under
`modassets/assets/` (29 real files total), then cross-checked that list against what this
project has already read to find genuine gaps, rather than spot-checking individual
directories by guesswork.

**Real, out-of-scope-or-too-thin, noted but not built (rule 1 discipline -- recorded, not
silently dropped)**: `auction/fonts/dictionary.txt` and `gloomycore/fonts/dict.txt`/
`dict-ranges.txt` (font glyph charsets for UI text rendering, not game content);
`gloomycore/egs_dlc_ids.txt`/`steam_dlc_ids.txt` (storefront DLC ids, launcher/store-adjacent,
rule 2 out of scope); `gloomycore/mcmtlList.txt` and `probuilder/smoothing.txt` (engine/asset-
tooling config, not gameplay data); `gloomycore/sky/world0/readme.txt` (an asset-pipeline
readme); `customitems/empty.txt` (confirmed genuinely empty, 0 bytes); `bundle/
item_groups_barter.txt` (confirmed genuinely empty, 0 bytes, despite the promising name);
`bundle/levels/br_location.txt` (12 bytes, real content `"battleroyale"` -- real, standalone
evidence of a Battle Royale game mode/location codename, but a single bare string with no
natural home in any existing class, so recorded here rather than given a one-off class);
`customnpcs/sorting_map.txt` (41 lines of comma-separated real item-id groups -- already
examined and cross-validated by this project as far back as part 21/22/23, referenced in
`items/ItemGroups`'s own class doc, but never given its own dedicated typed reader; still
true after this part, not revisited); `party/weapon_levels.cfg` (12 bytes, one real
key-value pair, `25000=NEWBIE` -- too thin alone, first noted part 50).

**Three real finds pursued into classes, confirmed by build+run in the cloud container (109
source files, 0 compile errors), md5-verified byte-exact against device copies for all 10
changed/new files, and smoke-tested against the real files themselves (not just fixtures)**:

1. `gloomycore/item_tiers.txt` -- confirmed by direct inspection to be exactly 4 CRLF-
   separated real names, no trailing newline: `novice`, `stalker`, `veteran`, `master`, in
   that file order. New class `items/ItemTiers.java`. What is/isn't evidenced stated
   carefully: the file, content, and order are real; the ordering reads naturally as an
   ascending progression (novice first, master last), consistent with STALKER-genre rank
   vocabulary, but no other real file this project has examined (item_groups.txt,
   itemnames.dat, weapons.json, weight.txt) tags any specific item id with one of these 4
   names, so this class does not assert a meaning beyond the file's own literal content.

2. `gloomycore/advices_en.txt` + `advices_ru.txt` -- real, player-facing loading-tip text,
   CRLF-terminated, blank-line-separated paragraphs, no trailing newline. New class
   `tips/AdviceList.java` (new package -- first UI-tip-text code in this codebase). English
   copy: 6 tips (ordinary gameplay tips, e.g. "To view the active quest, press «K».", plus a
   real `"STALCRAFT"` mention in a community-hub tip -- direct, independent corroboration of
   this project's existing evidence that STALZONE was previously/is-also known by that name).
   Russian copy: 84 tips -- a much LARGER and mostly DIFFERENT set (not just a translation of
   the English 6), almost entirely real account-security/support notices attributed to
   "Техническая поддержка" (Technical Support) and "Администрация" (Administration),
   repeatedly naming the real domains `support.exbo.ru` and `forum.exbo.ru` -- fresh, direct,
   independent real-world evidence corroborating this project's already-established EXBO
   support-infrastructure findings from unrelated earlier parts. Content-catalog only, same
   category as `dialogue/SubtitleRegistry` (part 34): no trigger/rotation logic invented,
   since none is evidenced anywhere in client data for which tip shows when.

3. `bundle/logged_trader_item_groups.txt` -- 10 real CRLF-separated category names, no
   trailing newline: `barter`, `cases`, `toys`, `weapons`, `armor`, `skins`, `backpacks`,
   `weapon_modules`, `signal_stuff`, `guitar`. New class `economy/TraderItemGroups.java`.
   VERIFIED (not assumed) against the real 41-category `item_groups.txt` (part 23) while
   writing this class: all 10 names are present there -- a caught-and-corrected mistake
   during this part's own work, since the first drafted class doc guessed (without checking)
   that only 8 of 10 overlapped and that `toys`/`guitar` were novel; running the actual
   cross-check found all 10 present, so the doc was rewritten to state the verified fact
   instead of the unchecked guess -- exactly the kind of self-caught rule-1 error this
   project's discipline exists to prevent, recorded here rather than quietly fixed and
   forgotten.

**Process note**: this part also caught and fixed two real bugs from parts 49/50's own doc
edits -- a text-splicing mistake in both `server-emulator/README.md` and the project's
`recon_findings.md` memory, where a new section/bullet got inserted mid-sentence into an
earlier one (anchoring a Python string-replace on a substring that was itself the START of
the earlier section's own final line, rather than the true boundary between sections),
leaving an orphaned fragment stranded later in the document. Both were caught by manual
re-reading rather than assumed correct from a successful diff/hash check alone, and repaired
with the same care (exact-length-preserving reorder, then independent boundary re-verification)
before this part's own new content was added on top.

**Scope note**: no wire-protocol/network work touched; this entire part is real-data cataloguing,
same rule 1 discipline as parts 44-50 -- see the repeated "what is/isn't evidenced" disclosures
above.

## Part 52 (2026-09-03) -- cross-session diff analysis: the client's own "_old" backup files, read with this project's EXISTING tooling, confirm unseenHandbookEntries and waypoints.cfg both shrink as well as grow

No new classes this part -- pure observation using already-built tooling (`ClientDataDumpTool`,
`VendorArchiveDumpTool`) against a freshly-noticed real data source: STALZONE's real `config/`
folder keeps a `<name>_old` backup of several files (`cluster_old`, `options.json_old`,
`prefs_old`, `vendor_archive.dat_old`, `waypoints.cfg_old`) written before the current copy's
last save, apparently as a save-crash-recovery mechanism -- consistent with the `_old` files'
mtimes being spread across this session's own timeframe (today and yesterday), i.e. real,
recent client activity, not stale historical artifacts. Diffed current vs. `_old` for every
pair that exists (`quests.cfg` has no `_old` counterpart on this install):

- `cluster`/`cluster_old`: byte-identical (`"EU"` both) -- no change, and still out of scope
  per rule 2 either way.
- `options.json`/`options.json_old`: byte-identical md5 (`c751cdeb...`) -- no change.
- `vendor_archive.dat`/`_old`: differ by 46 bytes at the raw compressed-file level, but running
  the EXISTING `VendorArchiveDumpTool` against both shows byte-for-byte identical parsed
  content -- same 2910 purchase records, same 3 characters, same currencies, same trader ids,
  same per-character purchase/trader counts on both. The 46-byte difference is consistent with
  zstd-compression-level noise (e.g. a slightly different compressed representation of
  identical decompressed data), not a real content change -- noted so a future part doesn't
  re-investigate the same dead end.
- **`config/prefs`/`prefs_old` (real content DOES differ)**: running the existing `ClientPrefs`
  reader against both shows every field byte-identical EXCEPT `unseenHandbookEntries`, which
  had 3 real entries in `prefs_old` (`handbook://articles/stats/obshie-svojstva-
  predmetov.md`, `handbook://articles/base/reputaciya.md`,
  `handbook://articles/arthunt/zarazheniya.md`) and is now an empty list in the current
  `prefs`. This is fresh, direct, real evidence refining this project's own prior
  `ClientPrefs` finding (part 45): `unseenHandbookEntries` is not just a real field this
  project can read, it is confirmed here to behave exactly as its name implies -- a to-read
  list of handbook article paths that SHRINKS (entries are removed) as they're presumably
  viewed in-client, not a monotonically-growing log.
- **`config/waypoints.cfg`/`waypoints.cfg_old` (real content DOES differ, most notable finding
  this part)**: `waypoints.cfg_old` (153 bytes) contains exactly ONE real death-marker waypoint
  entry (`{"name":"","time":1788379561286,"pos":{"x":-5282.2896,"y":63.273945,
  "z":1915.0941},"color":-1,"can_position_float":false,"icon_index":0,"type":"death"}`);
  the current `waypoints.cfg` (2 bytes) is `[]` -- confirmed via the existing `WaypointList`
  reader, not just raw text inspection: 1 waypoint parsed from the old copy (matching that
  JSON exactly, including the real coordinate/timestamp), 0 from the current copy. This
  MEANINGFULLY REFINES part 42's finding that `waypoints.cfg` is "actively written by the
  running client" -- part 42 only observed the file GROWING between two live reads; this part
  is the first confirmed real evidence that the file can also SHRINK BACK TO EMPTY, i.e. it is
  read-write/clearable client state, not an append-only log. This project has no evidence for
  WHAT specifically clears it (a UI "clear death markers" action, an automatic expiry, or
  something else) -- that remains unconfirmed and is not guessed at here, per rule 1.

**Scope note**: no wire-protocol/network work touched; no new code this part -- purely applying
already-built, already-tested tooling to a real data source (cross-session `_old` backups) this
project had not previously thought to compare. No file changes to sync/md5-verify since no
source files were modified.

## Part 53 (2026-09-03) -- 46 -> 67 real zones, and a real barter-item icon set that independently corroborates the mg/pd/td zone-code mystery (plus zarya armor, a hedged "sanitar"/Orderlies read, and BarterItemCatalog.java)

**Scope note up front**: no wire-protocol/network work touched (rule 3 untouched); no EXENS/anti-cheat/launcher internals examined (rule 2); every finding below is disclosed with the same evidence discipline as every prior part (rule 1) -- real file/log evidence is stated as such, and anything not directly evidenced (a name matching a real folder, a plausible faction-name equivalence) is explicitly flagged as unconfirmed rather than asserted.

### Trigger: 10 newly-available client-log sessions

Ten client-log sessions not analyzed as of part 52 (`client-14.log` through `client-23.log`, spanning 2026-09-02/03) became available. A `map_cache/5.0/` re-check plus a direct on-disk sweep of `modassets/assets/` (same method as part 26/51) turned up real new content on two separate fronts.

### Finding 1: the real zone roster grew again, 46 -> 67

Every one of the 21 new zone folders was independently confirmed to carry real, non-empty `.mdat` region-cache data (2-9 files each, not empty placeholder folders) before being added to `MilestoneMap.ALL_KNOWN_MAPS` -- the same verification standard used in parts 15/26. Zero zones were removed since part 26. The 21 new names:

```
bar_sever, battlefield_kolizei_open, battlefield_sovetpunk_gungame,
dungeon_base_sanitar_angar, dungeon_base_sanitar_fight, dungeon_base_sanitar_outside,
dungeon_base_sanitar_staff, dungeon_marin, dungeon_polygon_7_pve, dungeon_radar,
dungeon_radar_city, dungeon_stels, overpass, signals_dungeon_chimera,
signals_dungeon_dosug, signals_dungeon_final_boss, signals_dungeon_merc_02,
signals_dungeon_polygon_anomaly, signals_dungeon_polygon_motyga,
signals_dungeon_sklad_frimena, signals_dungeon_upir
```

Notable items in that list, each stated with exactly the evidence behind it:

- **`bar_sever`** directly resolves part 28's previously-unplaced settlement-roster name of the same spelling to a real, independently-cached zone. That name had been sitting in this project's notes since part 28 with no map_cache folder to back it; now it has one.
- **Four `dungeon_base_sanitar_*` zones** (`_angar`, `_fight`, `_outside`, `_staff`) appear alongside a widespread real "sanitar" armor-skin variant found this same sweep (see Finding 3 below) -- both are real and on-disk, but the connection between "a zone family named sanitar" and "an armor skin named sanitar" is only that they share a word, not a confirmed narrative link.
- **Eight `signals_dungeon_*` zones** mirror existing dungeon names (`chimera`, `sklad_frimena`, `upir`) under a new `signals_` prefix, plus a new one with no existing counterpart, `signals_dungeon_final_boss`. This lines up plausibly with the real "signal" barter-item category found in Finding 2 below (a "signal hunt" variant of an existing dungeon reads as a reasonable guess) -- but no file or log line states that connection directly, so it stays a plausible reading, not a fact.
- **Two new named PvP arenas**: `battlefield_kolizei_open`, `battlefield_sovetpunk_gungame` -- both real zone names, first named evidence for a "Kolizei" (Colosseum) location and a "Sovetpunk" (Soviet-punk) aesthetic/mode name, neither previously seen in any file or log this project has read.
- **Two new "Radar" callback zones**: `dungeon_radar`, `dungeon_radar_city` -- a real zone-name pair (base + "_city" variant), matching the existing pattern of zone families having a plain and a variant version (e.g. `dungeon_bolota_tutorial`/`_2`, `bar`/`bar_sever`).

`world/MilestoneMap.java` was updated in place: its part-53 Javadoc block documents all of the above, and `ALL_KNOWN_MAPS` now holds the full 67-entry array, in the real filesystem's own case-sensitive sort order (not manually re-sorted, consistent with this project's existing convention for that array).

### Finding 2: a real barter-item icon set corroborates the mg/pd/td zone-code mystery

A direct sweep of `modassets/assets/` (not log-driven, the same directory-sweep method as part 51) turned up a previously-unread real folder: `modassets/assets/customitems/textures/items/barter/`, holding 101 real `.ol` files.

Most of these follow a real, consistent naming pattern: `<zone>.<category>[_add].ol` -- e.g. `mg.node.ol`, `pd.signal_add.ol`, `bar.npc.ol`. This is directly, independently significant to a years-open thread in this project: the "mg"/"pd"/"td" real-but-unidentified `map_cache/5.0/` zone folder names (first flagged unresolved in part 26, revisited without resolution in parts 35/36/37). Those exact three tokens -- `mg`, `pd`, `td` -- appear here as barter-item zone prefixes, in the SAME reference list as already-confirmed real zones: `armsklad`, `bar`, `bolota`, `kordon`, `les`, `svalka`. This doesn't resolve what "mg"/"pd"/"td" stand for (their full expanded names still appear nowhere this project has found), but it places them in the identical evidence class as known-real zones rather than leaving them as isolated, possibly-coincidental folder names.

Real category tokens found across the zone-scoped entries: `mining`, `mob`, `npc`, `node`, `signal`, `airdrop`, `boss`, `capture`, `fallout`. Most zones have an `_add` variant of one or more categories -- a real, disclosed, unexplained naming suffix (this project exposes it as a distinct category token rather than guessing at what "_add" means).

Three zone-shaped names appear in this barter-item list that are new to this project entirely and are NOT real `map_cache/5.0/` folder names (checked directly, part 53): `limansk`, `sever`, `blizzard` (the last with only a single real category, `npc`). Unlike mg/pd/td, these three do not have the map_cache corroboration, so their status as actual playable zones -- versus e.g. cut, renamed, or reused-elsewhere content -- is explicitly NOT confirmed here, only that the names themselves are real and on-disk.

4 real filenames are standalone, not zone-scoped: `battlefields.ol`, `ffa_token.ol`, `fractional_coupon.ol`, `triumph_coin.ol` -- read plausibly as generic PvP-mode/event currencies, though this project does not assert that reading, only that they don't fit the `<zone>.<category>.ol` pattern. 11 more are bare internal numeric item ids (e.g. `28703.ol`) with no zone/category encoded in the filename.

**New class**: `economy/BarterItemCatalog.java` -- loads the barter directory, classifies each `.ol` file into zone-scoped / standalone / numeric-id via the stem's dot structure, and exposes `zoneNames()`, `categoriesFor(zone)`, `standaloneItemNames()`, `numericIdItemNames()`, `zoneCount()`, `totalFileCount()`. Rejects a stem with a second `.` (would break the assumed one-dot pattern) and an empty directory, both with `IOException`. `economy/BarterItemCatalogDumpTool.java` is its CLI. Verified two ways: 8 new unit tests against a 9-file fixture drawn from real filenames (`mg.node.ol`, `mg.signal.ol`, `pd.mining.ol`, `pd.mining_add.ol`, `bar.npc.ol`, `td.mob.ol`, `limansk.boss.ol`, `ffa_token.ol`, `28703.ol`), plus the DumpTool run against a reconstructed real 101-file listing (fetched directly from the device via `find ... -printf "%f\n" | sort`, md5-verified against the original before use, after an earlier hand-transcription attempt introduced a fabricated `td.mob.ol`-adjacent typo that was caught before it reached any committed file -- see "Self-correction" below). Compiled clean at 112 total sources; full suite 117/117 passing.

### Finding 3: real `zarya` armor set, and a hedged `sanitar`/"Orderlies" read

The same sweep found a real armor model set literally named `zarya` (`stalker:models/armor/zarya/...`, including a `sanitar`/medic sub-variant of that set). This is the first evidence tying the part-34 "zarya" quest-faction name to actual in-game equipment -- part 34 had only a bare faction-name string with nothing wearable attached to it; now there's a real, named armor line.

Separately, a widespread real "sanitar" armor-skin variant was found across at least 8 unrelated armor bases (`bover`, `kz3b`, `mule`, `saturn`, `skat10`, `smog_old`, `voshod_old`, and the `zarya` set above), plus dedicated NPC animation/game-object assets and the 4 `dungeon_base_sanitar_*` zones from Finding 1. This is disclosed as **plausibly, but NOT confirmed**, the same in-game faction EXBO's own public wiki calls "Orderlies" (first noted in part 36): "sanitar" is Russian for "orderly", a real and reasonable semantic match, but no STALZONE file or log line this project has found directly equates the two names. This stays a hedged reading, exactly as part 36 left it, now with considerably more supporting (but still not confirming) real evidence.

### rubezh: still unresolved

Part 34's unconfirmed quest-arc name `rubezh` was re-checked against all 10 newest sessions. It still never appears in any log. Unchanged from part 52 -- disclosed as real-but-unobserved-firing, not retired as a dead lead.

### Self-correction (rule 1 in practice)

While reconstructing the real 101-file barter list in a scratch directory for smoke-testing `BarterItemCatalog`/its DumpTool against real data, an initial hand-typed reconstruction of the real filename list included one fabricated filename that isn't actually on disk under that exact zone/category combination -- the file count came out one over what the real listing has. This was caught by re-fetching the real device listing directly via `find ... -printf "%f\n" | sort`, md5-comparing it against a Python-reconstructed set built by copying the tool's own machine-generated output string verbatim (not retyped from memory), and only proceeding once the two md5s matched. (Note: `BarterItemCatalogTest.java`'s fixture uses `td.mob.ol` deliberately as a small synthetic-but-plausible fixture entry built from real vocabulary -- `mob` is itself a confirmed real category token, just not necessarily paired with `td` on the real disk listing -- and is unrelated to the fabricated-filename error described here, which occurred in the separate 101-file real-data reconstruction, not in the unit test fixture.) A second, similar near-miss -- an early doc draft naming only "limansk and sever" as the new zone-shaped barter prefixes, omitting the third real one, `blizzard` -- was caught the same way, by checking the doc claim against the DumpTool's actual real-data output rather than trusting the first draft. Both are the same class of error this project has hit and caught before (e.g. part 51's `TraderItemGroups` doc fix): a manual transcription step introducing an error that machine-generated ground truth then catches.

### Files changed this part

- `world/MilestoneMap.java` -- extended with the part-53 Javadoc block and the full 67-entry `ALL_KNOWN_MAPS` array.
- `economy/BarterItemCatalog.java` -- new.
- `economy/BarterItemCatalogDumpTool.java` -- new.
- `economy/BarterItemCatalogTest.java` -- new, 8 tests.
- `testing/AllTests.java` -- extended with the new test registration.

All 5 files synced cloud <-> device and md5-verified byte-identical. Compiled clean at 112 total sources; full suite 117/117 passing (up from 109 as of part 51/52).


## Part 54 (2026-09-03) -- a first sweep of `stalker/models/armor/`: 192 real folders, real Twins/Nina/Reichstag/Murmur/Zivcas evidence, and two self-caught Javadoc corrections against a live real-data check

**Trigger**: `client-23.log` (220,549,028 bytes) checked again by timestamp/size and confirmed still the same live session used for part 53's analysis -- no new session exists yet, so this part is a direct on-disk sweep of a real asset folder this project had not read before, `modassets/assets/stalker/models/armor/`, rather than a new log-driven lead.

### Finding 1: the folder itself, and six real cross-references to previously log-only or partially-evidenced content

A direct real directory listing (`find`) confirmed 192 real top-level folders holding 2,240 real files total, mostly `.mcsb` meshes and `.ol` diffuse/normal/emissive textures -- the same mesh/texture format this project already identified in part 15, now confirmed present at real scale under `models/armor` specifically. A new class, `world/props/ArmorModelCatalog.java`, catalogs this: `foldersContaining(String)` is a deliberately generic, case-insensitive substring search over every real nested filename, so narrative claims below are checked programmatically against the real data rather than hand-counted -- the discipline this project adopted after part 53's self-caught fabricated-filename and omitted-zone-name errors.

Six real cross-references came out of the sweep: `armor/twins/` (real `left_twin`/`right_twin` body meshes plus body and shared-head texture pairs) is the first real character-model evidence for the "Twins" (bliznetsi) boss encounter, previously known only via 5 FMOD bank names (part 35). `armor/nina/` (a real mesh, a body texture pair, and a separate `face_diff.ol`) corroborates part 34's `nina_2`-`nina_6` subtitle files with an actual in-game model. `armor/reichstag/` holds two real, symmetric unit rosters -- `reich_*` and `rkka_*` (the real historical Soviet Red Army acronym, Raboche-Krestyanskaya Krasnaya Armiya) -- corroborating the `reichstag2026` FMOD bank (part 35) and the real `hasSeenReichstagHowToPlay` save flag (part 47) with actual opposing-army models. 8 real `murmur_*` folders plus a plain `murmur` base give the first real in-game armor evidence for EXBO's public "Monolit -> Murmur" faction rename (part 36), previously only evidenced by an external patch-notes article. `zivcas`/`zivcas_commander`/`zivcas_fighter`/`zivcas_heavy` plus a separate `dr_zivcas` extend part 34's "Zivcas special forces" faction name with a real 4-role roster. And `zombie_bm` plausibly, but NOT confirmedly, ties to the `bm2024` seasonal-event FMOD bank (part 35) via the shared "bm" token -- no file or log line directly equates the two, so this stays a hedged reading, same standard as the sanitar/Orderlies hypothesis below.

### Finding 2: the real "sanitar" variant is now an exact, programmatically-verified 9 bases, correcting part 53's hand-counted "8+"

`foldersContaining("sanitar")` against the real device listing returns exactly: `bover`, `kz3b`, `mule`, `saturn`, `seva`, `skat10`, `smog_old`, `voshod_old`, `zarya`. `seva` is newly named here -- part 53's hand-assembled "8+" did not name it explicitly. The real structure is inconsistent and disclosed as such rather than papered over: most bases only add a same-level `<base>_sanitar_diff.ol`/`_nrm.ol` texture pair over the existing mesh; `kz3b` additionally has its own dedicated `kz3b_sanitar.mcsb` mesh; `skat10`'s variant sits inside its own `motif_sanitar/` subfolder; and `zarya` -- checked in the most depth of the nine -- turns out to be the richest: it has BOTH the same-level texture pair AND its own `zarya/sanitar/` subfolder holding four distinct dedicated meshes (`zarya_sanitar`, `zarya_balon_sanitar`, `zarya_gas_sanitar`, `turist_sanitar`) plus its own texture pair. This still only corroborates, not confirms, part 36's hedged "sanitar" = EXBO's public "Orderlies" faction reading -- no direct file/log equation exists.

### Finding 3: other real, disclosed-as-unconfirmed finds in `armor/zarya/` and the `dr_` prefix pair

`armor/zarya/` contains `lipa_mask.mcsb`/`lipa_mask_diff.ol`/`lipa_mask_nrm.ol` -- a real, literal mask model tied to the "Lipa" tutorial character (parts 32/34/35), a cross-reference this project had not made before -- plus a separate `lipa_diff.ol`/`lipa_nrm.ol` pair without the "_mask" suffix, an additional real file not previously catalogued. The same folder has a real `outlaw_diff.ol`/`outlaw_nrm.ol` texture pair, a named skin variant new to this project with unconfirmed meaning. A real `dr_zivcas`/`dr_tank` pair share a `dr_` prefix (plausibly "doctor"/medic, not confirmed by any file or log found) and a matching `_hw22_diff.ol`/`_hw22_emi.ol`/`_hw22_nrm.ol` texture variant on both. `dr_zivcas` additionally carries three real `.mic`-extension files -- `dr_zivkas_hw22_diff.mic`, `dr_zivkas_hw22_emiss.mic`, `dr_zivkas_hw22_normal.mic` -- whose names read "zivkas" (with a k) rather than "zivcas" (with a c) everywhere else in the folder. This is disclosed as a real, on-disk spelling inconsistency in the game's own asset files, not a transcription error introduced by this project, and it is also the first `.mic` file extension this project has catalogued (previously only `.mcsb` mesh and `.ol` texture extensions were seen under `models/armor`). The folder list also carries many classic S.T.A.L.K.E.R.-trilogy suit names (seva, ozk, exo, berill, kz1-kz4, skat, etc.) -- the same "reuses/reimagines the original game's roster" pattern already established for zone names (part 15) and mob names (part 10), now extended to armor.

### Self-correction recorded on purpose (rule 1)

The first Javadoc draft for `ArmorModelCatalog.java` was written from an initial folder skim before a full live real-data check, and it contained two inaccuracies caught and fixed only once the class's own logic was cross-checked against a fresh `find` listing of the real device directory: (1) the `reichstag/` roster was first described as "6-8 roles" from a quick read; the real listing shows 9 real `.mcsb` meshes per prefix (capitan, corporal, engineer, gunner, hands_1, hands_2, hands_sniper, sniper, stormtrooper) -- fixed to the exact real count before this was committed. (2) `zombie_bm` was first described as just "`ao4`/`ao5`/`ao6` gas-mask suits"; the real listing shows 8 distinct zombie character lines (`ao4`, `ao5`, `ao6`, `centurion`, `ghost`, `goplit`, `legion`, `mule`), a materially larger and different scope than first drafted -- fixed to the real, complete list. Both corrections were made before any file was pushed to the device, using the same "verify the doc claim against the tool's actual real-data output" discipline already used in part 53 for the omitted "blizzard" zone name. The `zarya`/`sanitar` structure was also corrected in the same pass: an early draft credited `zarya` with only the same-level texture-suffix pair (the pattern most of the other 8 sanitar bases follow), missing its additional dedicated `zarya/sanitar/` subfolder entirely until the live listing surfaced it.

### Files changed this part

- `world/props/ArmorModelCatalog.java` -- new.
- `world/props/ArmorModelCatalogDumpTool.java` -- new.
- `world/props/ArmorModelCatalogTest.java` -- new, 8 tests.
- `testing/AllTests.java` -- extended with the new test registration.

All 4 files synced cloud <-> device and md5-verified byte-identical. Compiled clean at 115 total sources; full suite 125/125 passing (up from 117 as of part 53). Real-data smoke test against the live device directory confirmed every predicted count exactly: 192 folders, 2,240 files, all 9 sanitar bases, 8 `murmur_*` + plain, 4 `zivcas_*` + `dr_zivcas`, and the twins/nina/reichstag file listings -- the same live check is what caught and fixed the two Javadoc inaccuracies described above before they reached a committed file.

## Part 55 (2026-09-03) -- a first sweep of `stalker/models/artefacts/`, plus 8 small standalone `models/` folders

**Trigger.** Continuing the standing authorization to keep sweeping uncatalogued content areas after part 54's `ArmorModelCatalog` work was finished and verified. `stalker/models/` was listed directly on the device and, besides `armor/` (part 54) and the `customitems/models/blocks/vehicles/` tree (`VehiclePropCatalog`, earlier part), turned up several never-catalogued subfolders: `artefacts` (553 files, 98 dirs by initial count -- corrected below), `backpacks` (385 files, 71 dirs, left for a future part), `blizzard`, `cloth`, `food` (already covered by part 50's `min_food.txt` work, skipped here), `gift`, `halloween`, `hunter`, `items`, `shamanfight`, `undead`, and `dropped_tooltip`. `artefacts/` was chosen as this part's main target -- the single most iconic STALKER content type, and structurally close in scale to part 54's armor sweep.

**Finding 1: real 17-folder, 536-file `artefacts/` tree, in two structural eras.** Direct `find` confirmed 17 real top-level folders. Of these, 6 (`electra`, `jarka`, `karusel`, `kisel`, `puh`, `voronka`) are flat "classic" family folders with meshes and textures side by side, some textures explicitly reused across named artefacts in the same folder (`electra/lighting.ol`, `electra/plasmaball.ol`). A newer `n_art/` ("new artefacts") folder reorganizes those same 6 family names into 68 real per-artefact leaf folders across 12 real family subfolders (confirmed by direct listing: 12 family subdirs, 68 leaf dirs at depth 2), each leaf folder holding its own dedicated diffuse/normal/specular(/emissive) texture set instead of sharing one. The other 6 real `n_art/` families (`autumn25`, `biohim_summer25`, `collab_art`, `inside`, `ny26`, `trampoline`) have no flat-era counterpart at all -- purely `n_art`-era additions. The remaining 10 top-level folders (`barter`, `clan`, `helewin`, `ivent`, `newmodels`, `newyear`, `other`, `puzir`, `snow_storm`, `trump`) exist only in the flat, older style.

**Finding 2: a real 553-vs-536 discrepancy caught by the live device check, exactly the discipline this project keeps relying on.** The first pass's `find . -type f | wc -l` on the artefacts root read 553, and the first `ArtefactModelCatalog` Javadoc draft repeated that number as the class's own total. But `ArtefactModelCatalog.load()` (deliberately built as a folder-only walk, matching `ArmorModelCatalog`'s design) only visits the 17 named subfolders -- it does not see files sitting loose directly in the `artefacts/` root itself. Running the compiled DumpTool against the real device directory returned `totalFileCount() == 536`, not 553. A per-folder `find` sum (536, matching exactly) plus a direct `find -maxdepth 1 -type f` on the artefacts root confirmed the missing 17: `1623.ol`, `1623_emi.ol`, `dist.ol`, `dist2.ol`, `dist2k.ol`, `dist3.ol`, `dist4.ol`, `dist5.ol`, `krisstal.mcsb`, `kristal.ol`, `kristal_emi.ol`, `sopli.ol`, `sopli2.ol`, `sopli3.ol`, `sopli4.ol`, `sopli5.ol`, `sopli6.ol` -- real files that simply sit outside any subfolder. The Javadoc was corrected before commit to state the class's own real total (536) and separately disclose the 17 real loose files it does not walk, rather than silently reconciling the two numbers or padding the class to cover a case `ArmorModelCatalog` never needed. `krisstal.mcsb`'s own textures are spelled `kristal.ol`/`kristal_emi.ol` (one s, not two) -- a real on-disk spelling mismatch, the same class of inconsistency as `dr_zivkas`/`dr_zivcas` from part 54's armor sweep. `dist.ol` through `dist5.ol` plus `dist2k.ol` (6 files) and `sopli.ol` through `sopli6.ol` (6 files, "sopli" is Russian slang for "snot/mucus") are both mesh-less texture groups with no owning artefact -- read as shared distortion/heat-haze and organic/slime effect sets respectively, unconfirmed by any file or log.

**Finding 3: a real, dated seasonal-content naming convention, new for this project.** `n_art/autumn25`, `n_art/biohim_summer25`, and `n_art/ny26` are real, literally year-suffixed folder names (Autumn '25, Summer '25, New Year '26) -- the first time this project has found a directly dated naming convention inside a file or folder name itself, distinct from the dated FMOD bank names found earlier (`reichstag2026`, `bm2024`, part 35) which come from a different data source entirely (audio banks, not the model tree). `n_art/collab_art/` (real leaf folders `hedgehog`, `jele`, `spheretank`, `tracks`) is real, direct evidence of some collaboration/crossover content -- the literal substring "collab" is on disk -- but no partner or brand name appears anywhere in this tree, so who or what it collaborates with stays unconfirmed.

**Finding 4: a real, cross-referenced Halloween-adjacent cluster spanning two trees.** This part's sweep of the small standalone `models/` folders (see Finding 6) turned up a `models/halloween/` folder with a shared `goner_diff.ol`/`goner_emi.ol`/`goner_nrm.ol` texture set used by three real size-variant meshes (`big.mcsb`, `medium.mcsb`, `small.mcsb`) -- plausibly a Halloween creature or prop, not catalogued by any class. Independently, the `artefacts/` tree itself has a real `helewin/` folder (`pup.ol`, `pupdif.ol`, `pupdif_emi.ol`, `ty_kva.mcsb`) -- "helewin" reads as a transliteration of "Halloween", and "ty_kva" plausibly as "tykva" (Russian for "pumpkin"), though this project has no file or log that spells either reading out, so both stay hedged. A third, independent find -- `n_art/trampoline/art_pumpkin.mcsb` -- carries the literal English word "pumpkin" in its filename. Three differently-named folders in two separate real trees, converging on the same seasonal theme, is real corroborating evidence of a Halloween-adjacent event bundle. It is explicitly NOT evidence that these three ship together, share a date, or share a name in any player-facing string -- no such string has been found, and this stays disclosed as a pattern rather than a confirmed single event.

**Finding 5: a recurring real "proto"-prefixed naming habit, and other disclosed oddities.** The substring "proto" turns up independently in four unrelated folders: `electra/protoart.mcsb`, `jarka/proto.ol`, `n_art/trampoline/art_protocibulya.mcsb`, and `trump/art_protomedusa.mcsb` -- verified programmatically via `ArtefactModelCatalog.foldersContaining("proto")` against the real device data, which returned exactly `[electra, jarka, n_art, trump]`. Read here as a recurring internal "prototype before final" convention, not confirmed by any comment or log. Real dev-leftover files sit directly in the shipped tree: `jarka/test.ol`, `karusel/test.mcsb`. `karusel/` also has a real, disclosed naming duplication -- both `art_ostov.mcsb` (no texture files of its own) and a separately-named `ostov.mcsb` WITH its own `ostov_diff.ol`/`ostov_nrm.ol`/`ostov_spek.ol` exist side by side; whether these are the same in-game artefact or two different ones is unknown. In `barter/`, two of its eight real `art_`-prefixed meshes -- `art_tesla.mcsb` and `art_toksikoz.mcsb` -- have no matching same-name texture files anywhere in that folder, unlike their six siblings which each have a full texture set. `clan/` holds a real `clan`/`clan2` pair read as two skin tiers of one clan-related artefact, with `clan2` notably missing its own normal map in the real listing.

**Finding 6: 8 small standalone `models/` folders inspected directly, not given catalog classes (too small/miscellaneous, but real and disclosed).** `blizzard/` (4 files: a real `frozen` prop mesh plus diff/nrm/spek textures -- a distinct real folder from the still-unconfirmed `blizzard` map-zone-name lead from part 15; this is a "frozen object" prop, not necessarily a map zone). `cloth/` (1 file: `cloth_hp.mcvd`, an apparent orphan with no siblings). `gift/` (3 files: `gift_box.mcsb` plus two diffuse variants, likely a holiday reward-crate prop). `halloween/` (6 files, described in Finding 4). `hunter/hook/` (6 files: a real "hook" model, PLUS two literal Cyrillic-suffixed duplicate files, `hook_diff — копия.ol` and `hook_spek — копия.ol` -- "копия" is Russian for "copy" -- a genuine on-disk dev leftover in the game's own shipped files, not a transcription artifact introduced by this project). `undead/hook/` (4 files: a parallel real "hook" model under a different faction folder, no Cyrillic duplicates here -- both Hunter and Undead factions have their own "hook" prop, unconfirmed shared meaning). `shamanfight/` (10 files across `column/`, `flames/`, `phantom_proj/`, `phantom_zone/`, `shield/` -- `shield/` alone has a `.mcsb`, a `.mcvd`, a `.ol` texture, AND a `perlin.mic` noise texture, the first `.mic` file this project has seen used for a shader-noise role rather than a diffuse/normal/emissive texture -- the first real model evidence of a "Shaman" boss encounter with a phantom-projectile and shield mechanic). `dropped_tooltip/` (8 files: a `stick.mcsb` mesh plus 6 real rank-suffixed texture variants -- `stick.ol`, `stick_LEGEND.ol`, `stick_MASTER.ol`, `stick_NEWBIE.ol`, `stick_PICKLOCK.ol`, `stick_STALKER.ol`, `stick_VETERAN.ol` -- the first real, file-name-level evidence of a player rank/tier ladder in this project; `PICKLOCK` reads as an unexplained outlier among five otherwise general-sounding progression tiers, unconfirmed meaning).

**Self-correction recorded on purpose (rule 1).** The first `ArtefactModelCatalog` Javadoc draft claimed the class's own `totalFileCount()` would read 553, copying the raw root `find` count without accounting for the class's folder-only walk. Running the compiled DumpTool against the real device directory returned 536, not 553 -- caught immediately by the same "verify the live total before committing a narrative number" discipline established in part 54 (and, more broadly, the read-back-and-diff verification discipline this project uses for large writes generally). The Javadoc was corrected to state 536 as the class's real, walked total and to separately disclose the 17 real loose root files as a known, explicit gap rather than silently padding the class or fudging the number. This is recorded here, not smoothed over, consistent with how part 54's reichstag-role-count and zombie_bm-scope corrections were logged.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/ArtefactModelCatalog.java` (new), `ArtefactModelCatalogDumpTool.java` (new), `ArtefactModelCatalogTest.java` (new, 9 tests using an 18-file real-filename fixture spanning both structural eras), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 55 bullet, test count 125 -> 134), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 118 total sources; 134/134 tests passing (up from 125 as of part 54). Real-data smoke test against the live device directory confirmed every predicted count exactly once corrected: 17 folders, 536 walked files, 68 `n_art` leaf folders across 12 families, and the 4-folder `proto` cross-reference (`electra`, `jarka`, `n_art`, `trump`) -- the same live check is what caught and fixed the 553-vs-536 total-count error described above before it reached a committed file.

## Part 56 (2026-09-03) -- a first sweep of `stalker/models/backpacks/`

**Trigger.** Continuing the standing authorization to keep sweeping uncatalogued content areas after part 55's `ArtefactModelCatalog` work was finished and verified. Part 55's own trigger note had already flagged `backpacks` (385 files, 71 dirs by initial estimate) as "left for a future part" during the same `stalker/models/` directory listing that led to the artefacts sweep. This part picks that up directly, following the same `ArmorModelCatalog`/`ArtefactModelCatalog` generic-catalog template.

**Finding 1: a real 65-folder, 385-file `backpacks/` tree with zero loose root files.** Direct `find` confirmed 65 real top-level folders (the initial 71-dir estimate from part 55's listing was off by 6, superseded here by the direct count) holding 385 real files -- 154 `.mcsb` meshes and 231 `.ol` diffuse/normal/specular/emissive textures, only two extensions in the whole tree. Unlike part 55's `artefacts/` find, a root-level `find -maxdepth 1 -type f` returned nothing: every single file in this tree sits inside one of the 65 named subfolders, so `BackpackModelCatalog.totalFileCount()` (a folder-only walk, same design as `ArmorModelCatalog`/`ArtefactModelCatalog`) needs no disclosed root-vs-walked gap the way part 55's `ArtefactModelCatalog` did.

**Finding 2: a real 30/33/2 family split, caught and corrected after an initial 26/37 hand-count undercounted both families and omitted two real folders.** The 65 folders split cleanly into `bag_`-prefixed (worn/carried bags) and `cont_`-prefixed ("container", bulkier non-worn storage props), plus 2 folders outside either family (`clan/`, `corpse_bags/`). The first Javadoc draft, working from a hand-read of the folder listing, claimed 26 `bag_` and 37 `cont_` folders. Running `ls | grep -c '^bag_'` and `ls | grep -c '^cont_'` directly against the real device directory returned 30 and 33 -- not 26 and 37 -- and a diff against the hand-typed name lists showed the hand count had silently dropped two real folders from the `cont_` enumeration entirely: `cont_cocoon` and `cont_kolotun`. Both are real, on-disk, confirmed by the same `ls` output. The Javadoc was corrected to the live, tool-verified 30/33/2 split (30 + 33 + 2 = 65, matching `totalFolderCount()` exactly) and both omitted names were added to the enumeration, with the correction itself left visible in the class doc rather than silently fixed -- the same disclosure discipline as part 55's 553-vs-536 correction, and the same root cause: an early hand-count that skipped live verification until the DumpTool was actually run.

**Finding 3: a real, widespread worn/ground mesh-state convention, not seen in the armor or artefact sweeps.** 53 of the 65 folders (confirmed via `foldersContaining("_ground")` against the real device directory, not estimated) carry a separate `_ground` mesh alongside the normal worn/carried mesh -- e.g. `cont_kega/kega.mcsb` plus `cont_kega/kega_ground.mcsb`, the model as it appears dropped in the world. Textures are not duplicated per state; only the mesh gets a dedicated ground variant. This is a real structural convention specific to carryable-prop content that neither `ArmorModelCatalog` (part 54) nor `ArtefactModelCatalog` (part 55) needed, since worn armor and picked-up artefacts don't have an equivalent "as seen lying on the ground" mesh state in those trees.

**Finding 4: a real, reusable `style_<season>` subfolder convention, and a first "26"-dated season name.** `cont_bear/style_autumn25/`, `cont_hive/style_autumn25/`, and `cont_pathfinder/style_autumn26/` (confirmed via `foldersContaining("style_autumn25")` -> `[cont_bear, cont_hive]` and `foldersContaining("style_autumn26")` -> `[cont_pathfinder]` against the real device directory) each hold a dedicated seasonal texture/mesh variant of their parent container. A direct `find` on `cont_hive/` specifically (flagged as unresolved after the earlier evidence-gathering pass) confirmed the seasonal files live ONLY inside `style_autumn25/` -- `cont_hive/style_autumn25/hive_autumn25.mcsb`, `hive_autumn25_diff.ol`, `hive_autumn25_nrm.ol`, plus folder-specific `animation_atlas.ol`/`animation_atlas_emi.ol`/`glass_nrm.ol` -- with no redundant copies sitting in `cont_hive/`'s own root alongside the base `hive.mcsb` set; the two file groups are structurally distinct, not a duplicate. `style_autumn26` is real, on-disk, literally year-suffixed evidence of Autumn '26 content -- the first "26"-dated SEASON name this project has found anywhere (part 55's dated folders topped out at `ny26`, New Year '26); whether it has shipped or is upcoming relative to any specific date is not evidenced by the file alone. `bag_dmz26/` is a second, independent real 2026-dated find in this same sweep -- a DMZ-mode bag with real `bake`/`frost` finish variants (each its own diff/emi/nrm) plus a crossover texture pair, `bag_trizip_dmz26_diff.ol`/`_nrm.ol`, tying it to the separately-cataloged `bag_trizip/` folder.

**Finding 5: real "kept the old version" content, in three distinct forms found in one tree.** Sibling `_old` folders: `bag_npa_old/` has an identical file set to `bag_npa/` (`npa.mcsb`, `npa_diff.ol`, `npa_ground.mcsb`, `npa_nrm.ol`) plus one real extra file, `npa_spek.ol`, that the current folder lacks -- confirmed by a direct `ls` diff of both real folders. `bag_mbss_old/` is a byte-for-byte identical filename set to `bag_mbss/`, with no content difference detectable from names alone. New to this project: nested `old/` SUBFOLDERS inside an otherwise-current folder -- `cont_kzs1/old/` (real files `kzs1_diff.ol`, `kzs1_nrm.ol`, `kzs1_spek.ol`, `kzs1d_diff.ol`) and `cont_bidon/old/` (real files `kzs3.mcsb`, `kzs3_diff.ol`, `kzs3_emi.ol`, `kzs3_ground.mcsb`, `kzs3_nrm.ol`, `kzs3_spek.ol`, `kzs3d_diff.ol`) -- confirmed by direct `find` on both real folders. `cont_bidon/old/` notably holds `kzs3`-named files, not `bidon`-named ones, sitting alongside the real, separately-cataloged `cont_kzs3/` folder's own current textures -- a real, disclosed cross-folder retention quirk, not a naming error introduced here. Three different real retention conventions for old assets, none previously seen together in one folder before this sweep.

**Finding 6: real cross-references extending part 54's equipment findings, and several disclosed-as-unconfirmed oddities.** `bag_flamethrower/` carries `flamethrower.mcsb`, `flamethrower_nrm.ol`, `flamethrower_reich_diff.ol`, and `flamethrower_rkka_diff.ol` -- real evidence tying part 54's Reichstag/RKKA WWII-event armor roster to a matching backpack skin, with no plain `flamethrower_diff.ol` and no `_ground` mesh, an outlier among this tree's usual file pattern. `cont_zivcas/` and `cont_murmur/` extend the established Zivcas (parts 34/53/54) and Murmur (parts 36/54) faction cross-references from armor into containers. Other real, disclosed-as-unconfirmed finds, all confirmed by direct `ls`/`find` against the live device directory: a same-tree bee-themed cluster (`cont_apiary/`, `cont_hive/`, `cont_honeycomb/`), unconfirmed as a deliberate single set beyond the shared theme; `cont_hellboy/hellboydonate_diff.ol`, sitting alongside the folder's normal `hellboy.mcsb`/`hellboy_diff.ol`/`hellboy_ground.mcsb`/`hellboy_nrm.ol` set -- the first evidence in this project of a monetization/donation-tied cosmetic skin variant; `bag_tv/`, confirmed to hold only `tv.mcsb` and no texture file of any kind, the sole folder in this tree with that gap; `bag_sports/` and `bag_sports_quest/`, two differently-named folders whose internal file names are identical (`sports.mcsb`, `sports_diff.ol`, `sports_ground.mcsb`, `sports_nrm.ol`) -- the same duplication pattern as `bag_mbss_old`, read here as quest-specific reuse rather than an old/new revision; `bag_trader/` (one shared mesh, `trader.mcsb`, plus 10 real numbered diffuse skins `trader0_diff.ol`-`trader9_diff.ol`, a `trader_ground.mcsb`, `trader_nrm.ol`, and `trader_spek.ol`) sitting alongside 11 SEPARATE, fully-independent `bag_trader1/` through `bag_trader11/` folders (each confirmed to have its own dedicated mesh+texture set, e.g. `bag_trader1/trader1.mcsb`, `trader1_diff.ol`, `trader1_ground.mcsb`, `trader1_nrm.ol`) -- a real naming duplication across two different "trader" bag systems, the same class of oddity as part 55's `karusel/art_ostov` vs `ostov`, unknown if related in-game; `clan/` (real files `clan_supply.mcsb`, `clan_tent_medic.mcsb`/`_diff.ol`/`_nrm.ol`/`_spek.ol`, `clan_tent_supply.mcsb`/`_diff.ol`/`_nrm.ol`/`_spek.ol`), corroborating the clans system already inferred from keybindings and prefs data (parts 42/45); `corpse_bags/` (real `big.mcsb`, `medium.mcsb`, `small.mcsb`, no textures of their own), plausibly a death/loot-drop bag prop, unconfirmed by any log; and a recurring `u`/`d` suffix letter across several container names (`bidonu`, `kzs1u`/`kzs1d`, `kzs3u`, `kzs4d`, `kzs5d`, `honeycombu`, `cocoonu`, `bear4d`/`bear5u`/`bear6d`, `kegaue`), read here as a plausible camo or finish variant marker, NOT confirmed by any file or log this project has found -- the same hedge level as part 53's barter-icon zone-code reading before corroboration.

**Self-correction recorded on purpose (rule 1).** The first `BackpackModelCatalog` Javadoc draft hand-counted the `bag_`/`cont_` family split as 26/37, both numbers wrong, and silently dropped `cont_cocoon` and `cont_kolotun` from the enumerated `cont_` name list entirely. Running `ls "$DIR" | grep -c '^bag_'` and `grep -c '^cont_'` directly against the real device directory returned 30 and 33 -- caught by the same "verify the live total before committing a narrative number" discipline established in part 54 and applied again in part 55 (553-vs-536). The Javadoc was corrected to the tool-verified 30/33/2 split before any of it was committed to this writeup or pushed to the device, with the correction itself left visible in the class doc rather than smoothed over, consistent with how parts 54 and 55 logged their own corrections. This part's evidence-gathering also flagged and then resolved one internal ambiguity before it could ship: whether `cont_hive`'s autumn25 files sat redundantly in both the folder root and its `style_autumn25/` subfolder. A direct `find` on the real `cont_hive/` tree (Finding 4) confirmed no redundancy -- the seasonal files exist only inside `style_autumn25/`, distinct from the base set -- so no correction was needed there, but the check was made before the claim was finalized rather than after.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/BackpackModelCatalog.java` (new), `BackpackModelCatalogDumpTool.java` (new), `BackpackModelCatalogTest.java` (new, 11 tests using a 31-file real-filename fixture covering the `_old` sibling gap, the bare-mesh `bag_tv` gap, the nested `style_<season>` subfolder, and the nested `old/` subfolder), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 56 bullet, test count 134 -> 145), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 121 total sources; 145/145 tests passing (up from 134 as of part 55). Real-data smoke test against the live device directory confirmed every predicted count exactly once corrected: 65 folders, 385 files, the 30/33/2 family split, the 53-folder `_ground` spread, the two `style_autumn25` folders and the one `style_autumn26` folder -- the same live check is what caught and fixed the 26-vs-30/37-vs-33 family-count error and the two omitted folder names described above before they reached a committed file.

## Part 57 (2026-09-03) -- a first sweep of `stalker/models/items/` (armorplate, food, meds)

**Trigger.** Continuing the standing authorization to sweep uncatalogued content areas after part 56's `BackpackModelCatalog` work was finished and verified. `items/` had already surfaced in part 55's initial `stalker/models/` listing (133 files, 10 dirs by rough estimate) but was not picked up then. This part follows the same `ArmorModelCatalog`/`ArtefactModelCatalog`/`BackpackModelCatalog` generic-catalog template.

**Finding 1: a small, 3-folder, 130-walked-file tree with a real, disclosed 3-file root gap.** Direct `find` confirmed only 3 real top-level folders -- `armorplate`, `food`, `meds` -- far smaller than the 65-folder `backpacks/` or 192-folder `armor/` trees. A root-level `find -maxdepth 1 -type f` on `items/` itself turned up 3 real loose files outside any subfolder: `habar.mcsb`, `habar_diff.ol`, `habar_nrm.ol` ("habar" is Russian slang for "loot"/"swag"). `ItemModelCatalog` (folder-only walk, same design as the three prior catalogs) does not reach these, so `totalFileCount()` reports 130, not 133 -- the same class of disclosed root-vs-walked gap part 55's `ArtefactModelCatalog` found (17 loose files there), just much smaller here.

**Finding 2: `armorplate/` (15 files) -- a real texture-sharing convention, most meshes with no texture of their own.** 6 real mesh variants: `common`, `durable`, `meh`, `radiator`, `solid`, `stell` (plausibly a typo for "steel", unconfirmed). Only `meh` and `radiator` carry their own dedicated diff/nrm/spek sets; `common`, `durable`, `solid`, and `stell` have no texture files anywhere in the folder. Three generic, folder-root-level texture files -- `armorplate_diff.ol`, `armorplate2_diff.ol`, `armorplate_nrm.ol` -- sit alongside the six meshes with no single matching mesh name, plausibly serving as shared/default textures for the four texture-less meshes; not confirmed by any file or log.

**Finding 3: `food/` (4 files) -- the thinnest folder in this tree.** A single real item, `galet` (Russian "галета", a hardtack/cracker), with a full diff/nrm/spek texture set. Nothing further to disclose.

**Finding 4: `meds/` (111 files, by far the richest folder here) -- a real mesh-less-texture gap, and a real shared-mesh-many-skins convention not seen in the armor, artefact, or backpack sweeps.** The folder's own flat root (35 files) holds 6 real named meds with texture files but no matching mesh anywhere in this entire `items/` tree: `astrikcin`, `desperol`, `psiblock`, `sas`, `stark`, `stomping` -- the inverse of part 55's texture-less-mesh gap (there, meshes with no textures; here, textures with no mesh). `meds/inside_lech/` (30 files) has exactly ONE mesh, `inside_lech.mcsb` (with its own full diff/emi/nrm/spek set), reused across roughly a dozen differently-named real med items via texture-only variants: `amulet`, `cyan`, `grace`, `insight`, `mazz`, `pilul`, `red`, `salt`, `sedative`, `stimulator`, `treat`, `yellow` -- plus `nootrop`, which uniquely among this group also has its own dedicated `nootrop.mcsb` mesh, alongside `nootrop2`/`nootrop3` texture-only variants. "lech" is Russian for "cure"/"treatment". A smaller second instance of the same shared-folder pattern: `meds/inside_grass_extract/` (9 files) has one full-set mesh, `inside_grass_extract`, sharing its folder with two more real texture-only meds, `ozverin` and `schizoyorsh` (each diff+emi only).

**Finding 5: `meds/nmeds/` (32 files, including the nested `animated_apt/` folder) -- a real, disclosed naming-convention shift, numbered tiers, and a cosmetic "prestige" tier.** A real naming-convention shift, confirmed via direct listing and not a transcription artifact: capitalized `_Diffuse`/`_Normal`/`_Specular` texture suffixes instead of the lowercase `_diff`/`_nrm`/`_spek` used everywhere else in this tree, and in every other model tree this project has catalogued to date (armor, artefacts, backpacks). Real numbered-tier variants: `antirad` (radiation med) has 3 real numbered diffuse variants (`antirad_191`/`_292`/`_393_Diffuse.ol`); `pciblock_neiron` likewise has 3 (`_11`/`_22`/`_33_Diffuse.ol`). `aptechka` ("first aid kit") has a base diff/nrm/spek set PLUS 5 real numbered color variants (`aptechka_color1` through `color5.ol`) PLUS a real `aptechka_color_prestige.ol` variant -- real evidence tentatively tying a cosmetic "prestige" tier to the STALCRAFT prestige/rank system already known from this project's earlier wire-traffic and API work (part 11); NOT confirmed as the same mechanic by any file or log here. `nmeds/psiblock.mcsb` has no texture of its own anywhere in `nmeds/`; its only real candidates are the differently-named, differently-cased `meds/psiblock_diff.ol`/`psiblock_spek.ol` one directory up -- a real, disclosed cross-folder, cross-naming-convention pairing, NOT confirmed as the texture set this mesh actually uses in-game. `nmeds/animated_apt/aptechka.mcsb` is a second, independently-named "aptechka" mesh (an "animated" variant), separate from `nmeds/aptechka.mcsb` itself.

**Finding 6: `meds/reichstag26/` (4 files) -- a third real Reichstag/RKKA cross-reference, extending equipment into consumables.** `aptechkareih` (BaseColor/Normal/Specular) plus `reichstag_medbag.mcsb` -- a THIRD real Reichstag/RKKA WWII-event cross-reference found by this project (armor, part 54; backpacks, part 56; now consumables), extending the real `reichstag2026` FMOD bank (part 35) and the real `hasSeenReichstagHowToPlay` save flag (part 47).

**Finding 7: `meds/memoria/` (1 file) -- the sole bare-mesh-no-texture gap in this tree.** `memoria.mcsb`, no texture file of any kind -- the same class of gap as part 56's `bag_tv/` and part 55's texture-less artefact meshes. "memoria" (Latin: "memory"), meaning unconfirmed by any file or log this project has found.

**Self-correction / verification note (rule 1).** Unlike parts 54-56, this part's Javadoc drafting happened alongside direct `find`/`ls` evidence-gathering rather than before it, so no narrative-vs-live-count mismatch was introduced in the first place -- every specific number in the class doc (3 folders, 130 files, the 3-file root gap, the armorplate/food/meds breakdown, the meds/nmeds/reichstag26/memoria subfolder counts) was taken directly from `find`/`ls` output before being written, then independently re-confirmed by running the compiled `ItemModelCatalogDumpTool` against the real device directory (3 folders, 130 files matched exactly) plus targeted `foldersContaining` checks for `prestige`, `reichstag`, and `memoria` (each correctly resolving to `[meds]`). Recorded here for consistency with parts 54-56's self-correction entries, even though this part had no narrative number to correct.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/ItemModelCatalog.java` (new), `ItemModelCatalogDumpTool.java` (new), `ItemModelCatalogTest.java` (new, 10 tests using a 22-file real-filename fixture covering the armorplate texture-sharing gap, the mesh-less psiblock texture, and the nmeds capitalized-suffix convention), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 57 bullet, test count 145 -> 155), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 124 total sources; 155/155 tests passing (up from 145 as of part 56). Real-data smoke test against the live device directory confirmed every predicted count exactly: 3 folders, 130 walked files, and the `prestige`/`reichstag`/`memoria` substring checks each correctly isolating `meds`.

## Part 58 (2026-09-03) -- a first sweep of `modassets/assets/currency/`: a real dated seasonal-currency calendar, a Twitch-integration currency, and a self-caught Javadoc correction

**Trigger.** With `stalker/models/` essentially exhausted (armor, artefacts, backpacks, and items all now have dedicated catalog classes; 8 small standalone `models/` folders were directly inspected in part 55; `food` was deprioritized as already covered by part 50's `min_food.txt` work), this part surveyed the remaining uncatalogued top-level folders under `modassets/assets/` for the next sweep target. `weapons/` (18,813 files), `_localized/` (8,796 files), `pda/` (2,018 files), `highpoly/` (1,197 files), `gloomycore/` (863 files), and `effects/` (891 files) were all considered too large for a single-part sweep matching this project's established per-part scope, and were set aside for possible dedicated multi-part treatment later. `currency/` (98 files, 2 top-level folders) was selected instead: small enough for a clean single-part sweep with no disclosed root-vs-walked gap, yet directly extending part 27's `VendorArchive` finding (3 currencies observed in actual trading-history save data: `money`, `sc_birthday_21`, `season_pass_ticket_3`) into a much larger real currency/token roster.

**Finding 1: 2 real top-level folders, 98 real files, no root-vs-walked gap.** `models/` (8 files) and `textures/` (90 files, split between 4 files at `textures/` root and 86 in `textures/items/`) account for all 98 real files -- unlike parts 55 and 57, no file sits loose outside either folder. Confirmed by direct `ls`/`find` against the real device directory before the class doc was finalized.

**Finding 2: `models/` holds 6 real Russian ruble banknote-denomination props, plus a single dedicated physical token.** `10rub.mcsb`, `50rub.mcsb`, `100rub.mcsb`, `500rub.mcsb`, `1000rub.mcsb`, `5000rub.mcsb` -- literal real-world currency values modeled as physical pickup props -- plus a generic `money.ol` texture and `token_electroshtorm2026.mcsb` ("Electroshtorm" = "Electric Storm"), with its own full diff/nrm/spek texture set at `textures/` root. The `2026` in the name is a real, on-disk-confirmed "26"-dated find, joining part 56's `style_autumn26`/`bag_dmz26`.

**Finding 3 (self-correction, rule 1): `textures/items/` is NOT uniformly icon-only.** The first Javadoc draft claimed all 86 `textures/items/` files were pure UI icon textures with no dedicated 3D model. A live device check (`ls` on both `models/` and `textures/items/`, cross-referenced by name) found this was wrong: 7 of the 86 icons DO pair with a real model -- the 6 `money_10`/`money_50`/`money_100`/`money_500`/`money_1000`/`money_5000` icons name-match the 6 ruble-denomination `.mcsb` models, and `token_electroshtorm2026` (the icon) name-matches its own dedicated model. The Javadoc was corrected in place before anything was finalized, per this project's "verify before committing a narrative number" discipline -- the same discipline that caught real errors in parts 53, 55, and 56. The other 79 icons remain the only real asset for their currency, so the broader "mostly icon-only" characterization still holds, just not universally.

**Finding 4: a real, systematic, dated seasonal-currency calendar spanning 2024-2026 -- the most complete dated-content series this project has found.** `autumn_24`, `autumn_25` (plus `autumn_25_episode`/`_1`/`_2`/`_3` sub-variants), `autumn_26`, `spring_24`, `spring_25`, `spring_26`, `summer_24`, `summer_25`, `summer_26`, `winter_25` (plus `winter_26_episode`), `winter_26`. A direct `ls`/`grep` check confirmed `winter_24` is real and genuinely absent -- a disclosed gap in an otherwise complete quarterly series -- while `winter_25`/`winter_26` are both confirmed present. `spring_26`/`summer_26`/`autumn_26`/`winter_26` are real, on-disk icon names for 2026 content; their ship status relative to any specific date is not evidenced by the file alone.

**Finding 5: a second, parallel dated series specifically for the season pass, plus 5 one-off event battlepass currencies.** `season_battlepass_points` (base/undated) plus 9 real dated variants (`_autumn24`, `_autumn25`, `_spring24`, `_spring25`, `_spring26`, `_summer24`, `_summer25`, `_winter25`, `_winter26`) -- real evidence that `season_pass_ticket_3` (part 27's only directly-observed battle-pass currency) is one of a much larger seasonal family, not a standalone currency. Structurally separate: 5 real named one-off event battle-pass currencies (`battlepass_points_hw22`, `_halloween23`, `_ravent23`, `_blizzard23`, `_newyear24`), each apparently minting its own currency rather than sharing the seasonal token.

**Finding 6: real DMZ-mode-specific currencies, extending parts 26/53's DMZ zone/asset-name evidence into an actual progression economy.** `dmz_coin`, `dmz26_token`, `popularnostDMZ` ("popularnost" is Russian for "popularity"), `respectDMZ` -- real, and distinct from the base game's own `respect`/`loyal`/`levels` currencies found in this same folder.

**Finding 7: a second and third real Reichstag/RKKA cross-reference, a fifth across five content areas.** `reichstag_silver`, `reichstag_xp_free` -- confirmed present via direct `find`. Extends the WWII-event content this project has now found in armor (part 54), backpacks (part 56), and meds (part 57) into a dedicated currency.

**Finding 8: a "shards" currency family with dated variants, plus the project's first evidence of a Twitch-integration currency.** `shards` (base), `shards21`, `shards22` (year-dated), and `twitch_shards` -- the first real evidence in this project of a Twitch drops/streaming-integration currency. `coins21` is a separate, independently year-dated icon -- the earliest real dated content this project has found anywhere, predating every other dated file or folder by at least 3 years.

**Finding 9: `token_prestige` independently corroborates part 57's hedged `aptechka_color_prestige` finding.** A standalone real prestige-currency icon -- two now-independent real assets point toward a cosmetic/rank "prestige" tier, still not confirmed as literally the same system by any file or log.

**Finding 10: other real named tokens, including a `token_triumph26` tying to the "Triumph" endpoint from part 11's earliest wire-traffic work.** `token_hunter`, `token_fighter`, `token_science`, `token_innovative`, `token_functional`, `token_summer25_functional`, `token_snowflake`, `token_wrapper`, `token_triumph26`. `token_event` additionally has its own `token_event.mic` file at `textures/` root -- the same real use of a `.mic` extension for a shader/effect role this project first saw in part 55's `shamanfight/shield/perlin.mic`. The "Triumph" cross-reference to part 11 is asserted as likely in the class doc based on the shared name, but has not been independently re-verified against the part 11 source material this segment -- flagged as an open item for a future part.

**Finding 11: a long tail of real, disclosed-as-unconfirmed named currencies/materials.** `coupon`, `ticket`, `subsidies`, `cone`, `charge`, `redenergy`, `module_ingibitor`, `onyx`/`onyx_burnt`, `frozenbones`, `fireballs`/`fireballs2`, `kotleta` (Russian for "cutlet"), `sleeves`, `ssop_help`, `stalbucks` (literally "Stalker Bucks", plausibly a premium currency name), `respect`, `loyal`, `levels`, `craft_details`/`craft_details_hd`, `details`, `new_parts`, `encaged_currency`, `pg_currency`/`pg_na_currency` (abbreviation meaning unconfirmed). No further evidence found for any of these; disclosed rather than speculated on.

**Self-correction (recorded on purpose, rule 1).** See Finding 3 above: the first Javadoc draft's blanket "no dedicated 3D model" claim for all 86 `textures/items/` icons was disproved by a live device check before anything was finalized, and corrected in the class doc, the README bullet, and this entry rather than silently fixed -- matching the precedent set by parts 53, 55, and 56's corrections. A dedicated unit test (`foldersContaining("money_10")` / `foldersContaining("10rub")`) was added specifically to keep the corrected claim covered going forward.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/CurrencyCatalog.java` (new), `CurrencyCatalogDumpTool.java` (new), `CurrencyCatalogTest.java` (new, 10 tests using a 31-file real-filename fixture covering the ruble model/icon pairing, the reichstag cross-reference, DMZ currencies, and a dated seasonal variant), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 58 bullet, test count 155 -> 166), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 127 total sources; 166/166 tests passing (up from 155 as of part 57). Real-data smoke test against the live device directory confirmed the class's own totals exactly (2 folders, 98 files) and every targeted substring check (`reichstag`, `prestige`, `dmz`, `battlepass`, `shards`, `twitch`, `token`), plus a direct `winter_24`-absence / `winter_25`-`winter_26`-presence check -- all matched, except the one icon/model-pairing claim in Finding 3, which was corrected before being finalized rather than shipped wrong.

## Part 59 (2026-09-03) -- a first sweep of `modassets/assets/stalkerguide/`: real narrative/cutscene content, first evidence of the Duty faction, and a Shaman/Morg cross-reference to part 55

**Trigger.** With `stalker/models/` and `modassets/assets/currency/` both now catalogued, this part followed up on the two smaller candidates flagged at the end of part 58's survey -- `_jurisdiction/korea/` (region-locked content overrides) and `stalkerguide/` (157 files, containing `sound/glitch_monitor`, `stream/video`, `textures/gui`, `video`). `stalkerguide/` was selected: its subfolder names strongly suggested an in-game tutorial/help system with bundled audio/video, which -- if real -- would be squarely in-scope under this project's standing focus on game content and possible cut content (rule 2), and clearly small enough for a single-part sweep. `_jurisdiction/korea/` was deferred, flagged for a moment's consideration of whether region-locked compliance overrides stay within the "game content, not anti-cheat/compliance internals" boundary before it is picked up.

**Finding 1: 4 real top-level folders, 157 real files, no root-vs-walked gap -- and this is real STORY content, not a help-menu asset.** `sound`, `stream`, `textures`, `video` account for all 157 real files, confirmed by direct `find`/`ls` before the class doc was drafted. Despite the folder's name ("guide"), the actual file names are narrative cutscenes -- named characters, branching outcomes, and faction-arc videos -- not UI tooltips or anti-cheat/account-system assets.

**Finding 2: `video/` (99 files) is almost entirely `_720`/`_1080` dual-resolution `.mp4` pairs, with 4 real single-resolution exceptions -- including a fifth Reichstag/RKKA cross-reference.** `honey_badger`, `memagent`, `reichstag_atmosphere`, and `vozduhan` ("vozdukh" is Russian for "air") ship as a single file with no resolution suffix. `reichstag_atmosphere` is a fifth real Reichstag/RKKA WWII-event cross-reference in this project, after armor (part 54), backpacks (part 56), meds (part 57), and currency (part 58) -- now confirmed as a dedicated atmospheric cutscene, not just an item name.

**Finding 3: real named faction/story-arc start/end video pairs, live-confirmed, including the first evidence anywhere in this project of the "Duty" faction.** `duty_start`/`duty_end`, `zarya_start`/`zarya_end`, `zavet_start`/`zavet_end`, `merc_start`/`merc_end` all exist as real dual-resolution `.mp4` pairs. `duty_start`/`duty_end` is the first real evidence of "Duty" -- a faction name carried over from the base S.T.A.L.K.E.R. series -- appearing anywhere in this project's assets, logs, or configs to date. `zarya_start`/`zarya_end` corroborates the `zarya` armor set found in part 53's barter-item sweep, confirming Zarya has its own dedicated story arc rather than being purely cosmetic. `zavet_start`/`zavet_end` ("zavet" is Russian for "covenant"/"testament") is a newly-named story arc with no prior appearance in this project. `merc_start`/`merc_end` corroborates the `merc`-prefixed trader ids and zone names already found in parts 26, 27, and 53.

**Finding 4: the `video/` <-> `stream/video/` audio pairing is real but NOT total in either direction.** A live filename cross-check found 38 names shared between the 99 `video/` cutscenes and the 40 `stream/video/` audio tracks. Two real audio tracks -- `frontline_intro`, `inside` -- exist in `stream/video/` with no matching video file anywhere in `video/`. Complementarily, 14 real videos -- the 8 start/end-pair files and 4 single-resolution files from Findings 2-3, plus `cinemat` and `floating_away_2` -- exist in `video/` with no separate audio track of their own in `stream/video/`, plausibly because their audio is muxed directly into the `.mp4` (not evidenced by the file listing alone, disclosed as such).

**Finding 5: a real "Shaman" character/boss directly corroborated across two independent parts of this project.** `shaman_death`, `shaman_flies`, `morg_shaman_fight`, `morg_shaman_meet` are all real cutscene names. The `morg_shaman_*` pair directly corroborates part 55's `shamanfight` artefact-model folder (`shamanfight/shield/perlin.mic`), confirming "Shaman" is a real in-fiction character/boss encounter that the artefact-folder name was already hinting at, not just an arbitrary asset-folder label.

**Finding 6: a real location "Morg" (plausibly "morgue") and a real location/character "Lipa" ("linden tree" in Russian), each corroborated by multiple independent cutscene names.** "Morg" has 5 real cutscenes of its own (`morg`, `morg_death`, `morg_fires_roof`, plus the two shaman ones in Finding 5). "Lipa" has 4 real cutscenes (`lipa_1`, `lipa_2`, `lipa_3`, `lipa_lodka` -- "lodka" is Russian for "boat") plus a fifth, `tutorial_lipa_tonnel` ("tonnel" = "tunnel"), tying Lipa into the tutorial sequence in Finding 7. A real character "Vlasov" has two branching outcomes, `vlasov_death` and `vlasov_escape`.

**Finding 7: a real 4-part tutorial sequence, plus 3 numbered generic-story cutscenes as a separate family.** `tutorial_intro`, `tutorial_lipa_tonnel`, `tutorial_ejection`, `tutorial_outro` are all confirmed present as real dual-resolution video pairs with matching `stream/video/` audio tracks. Separately, `sstory1`, `sstory2`, `sstory3` form their own numbered family with no further evidence of what distinguishes them narratively.

**Finding 8: `sound/` (11 files) holds real elevator effects plus a 7-variant "glitched monitor" sound family.** 3 real elevator sound effects at `sound/` root (`elevator_bell_synth.ogg`, `elevator_close.ogg`, `elevator_open.ogg`) pair plausibly with `stream/elevator_ride.ogg` at `stream/` root, suggesting a real elevator sequence somewhere in this guide/tutorial flow (unconfirmed which specific moment). One generic `hint.ogg`. A real `glitch_monitor/` subfolder holds 7 numbered variants (`glitch_monitor_1.ogg` through `_7.ogg`) -- a real randomized "glitched monitor/screen" audio effect, unconfirmed as to which UI moment triggers it.

**Finding 9: `textures/gui/` (3 files) are real `.mic` banner textures, extending this project's `.mic`-extension pattern to a third context.** `advbanner.mic`, `banner.mic`, `palka.mic` ("palka" is Russian for "stick"/"pole") use the same shader/effect-role file extension this project first saw in part 55's `shamanfight/shield/perlin.mic` and again in part 58's `token_event.mic` -- now confirmed in a GUI-banner role as well.

**Finding 10: other real cutscene names, disclosed with no further evidence of their in-fiction context.** `ball`, `ball2`, `Boat` (capitalized, unlike every other file in this tree), `broken`, `club`, `grenade_throw`, `heli_crash`, `helicopter`, `knifestab`, `meet`, `oracle`, `phits`, `ppayalnik`, `pshoker`, `pshot`, `shepot` (Russian for "whisper"), `slayd` (plausibly "slide"), `floating_away`/`floating_away_2`, `cinemat` (plausibly a generic "cinematic" placeholder name).

**Self-correction / verification note.** No narrative-vs-live-count mismatch was introduced this part: every specific number in the class doc (4 folders, 157 files, the 99/44/3/11 per-folder split, the 38-name video/audio overlap, the 2 audio-only orphans, and the 14 video-only entries) was gathered directly from live `find`/`ls`/`comm` output on the real device directory before the Javadoc was drafted, then independently re-confirmed by running the compiled `StalkerGuideCatalogDumpTool` against the real directory (4 folders, 157 files matched exactly) plus 13 targeted `foldersContaining` substring checks (`reichstag`, `duty`, `zarya`, `zavet`, `merc`, `vlasov`, `shaman`, `morg`, `lipa`, `tutorial`, `sstory`, `glitch_monitor`, `palka`) -- every one resolved to exactly the folder(s) predicted. Recorded here for consistency with parts 54-58's self-correction entries, even though this part had no narrative number to correct.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/StalkerGuideCatalog.java` (new), `StalkerGuideCatalogDumpTool.java` (new), `StalkerGuideCatalogTest.java` (new, 10 tests using a 41-file real-filename fixture covering the faction start/end video-without-audio gap, the frontline_intro audio-without-video gap, and the Shaman/Morg cross-reference), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 59 bullet, test count 166 -> 176), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 130 total sources; 176/176 tests passing (up from 166 as of part 58). Real-data smoke test against the live device directory confirmed every predicted count exactly: 4 folders, 157 files, and all 13 targeted substring checks each resolving to the folder(s) predicted.

## Part 60 (2026-09-03) -- a first sweep of `modassets/assets/_jurisdiction/korea/`: a real regional content-override system, not an anti-cheat asset, with a partial cutscene substitution set, a narrow localization override, and several grim/cosmetic item finds

**Trigger.** With parts 58 and 59 closing out `currency/` and `stalkerguide/`, `_jurisdiction/korea/` was the remaining small candidate flagged at the end of part 58's survey. Before starting, it was checked against rule 2 (this project's standing focus on game content and possible cut content, explicitly NOT EXBO anti-cheat/launcher internals): a regional content-override folder holding alternate cutscenes, localized strings, and cosmetic item variants is squarely game content -- asset variance by region, not launcher or anti-cheat machinery -- so it was judged in-scope and selected.

**Finding 1: exactly one jurisdiction exists in this build, and it holds 5 real top-level folders, 60 real files, no root-vs-walked gap.** `_jurisdiction/` itself was confirmed by direct listing to contain only one region folder, `korea` -- no evidence of any other region in this build. Within it, `customitems` (8 files), `stalcraft` (1 file), `stalker` (3 files), `stalkerguide` (41 files), `weapons` (7 files) account for all 60 real files, confirmed by direct `find`/`ls` before the class doc was drafted.

**Finding 2: `stalkerguide/` (41 files) is a real PARTIAL override of part 59's base `stalkerguide/` cutscene catalog, with a disclosed 11-pair/7-single resolution asymmetry.** Every one of its 18 unique cutscene base names (`cinemat`, `grenade_throw`, `heli_crash`, `lipa_1`/`_2`/`_3`, `morg_death`, `morg_fires_roof`, `morg_shaman_fight`, `morg_shaman_meet`, `phits`, `ppayalnik`, `pshoker`, `pshot`, `shaman_death`, `shaman_flies`, `vlasov_death`, `vlasov_escape`) is a real name already found in the base 99-file `stalkerguide/video/` roster from part 59 -- confirming this is a substitution set for exactly these 18 story beats, not new Korea-exclusive narrative content. Only 11 of the 18 names ship as a `_720`/`_1080` pair here (`cinemat`, `heli_crash`, all 3 `lipa_*`, `phits`, `ppayalnik`, `pshoker`, `pshot`, both `vlasov_*`); the other 7 (`grenade_throw`, `morg_death`, `morg_fires_roof`, `morg_shaman_fight`, `morg_shaman_meet`, `shaman_death`, `shaman_flies`) ship as a real `_1080`-only single, dropping the `_720` variant the base game has for every one of these names.

**Finding 3: the audio side uses a real, structurally different internal path shape than the base game, plus one real unmatched anomaly -- a third independent "prestige" cross-reference.** `sound/stream/*.ogg` (11 files, matching 11 of the 18 video names) replaces base stalkerguide's separate `sound/` (with `glitch_monitor/`) and `stream/` (with `stream/video/`) split -- a real structural difference, not just a content difference. `stream/video/prestige.ogg` is the only file under this jurisdiction's `stream/video/`, and "prestige" matches none of the 18 real video names here or any name in part 59's base 99-file `video/` roster -- a third independent real "prestige" cross-reference in this project, after part 57's `aptechka_color_prestige` and part 58's `token_prestige`, still unconfirmed as the same system.

**Finding 4: `stalker/lang/` (2 files) is a real, narrow localization override, read directly, revealing the first named character "Holepunch" in this project.** `ko.lang` and `en.lang` (both real plain-text files, read via `cat`) define exactly 2 real numeric item ids: `16120` ("Bag of Holepunch's Hair" in Korean and English; en description: `A bag filled with Holepunch's "patchy" hair. Stored in a sack for easier carrying.`) and `16429` ("A Bag of Hair"), both real quest/trophy items. `16120`'s description is the first real evidence in this project of a named character "Holepunch" (an NPC nickname in the STALKER-series naming tradition), tied to a grim hair-collection trophy item. This finding is disclosed alongside `customitems/textures/items/quest_cutted_ear.ol` (Finding 5) as a second real body-part/trophy quest-item name in this same jurisdiction override, though no direct evidence links the two into a single quest. Whether this 2-item `.lang` override is a ratings-related text edit versus an unrelated Korea-specific localization addition is NOT evidenced by the file content alone; this project's own broader `_localized/` folder (flagged but not yet swept as of part 58) was not consulted for a base-text comparison this part.

**Finding 5: `customitems/` (8 files) holds real decal/poster overrides, a recurring "stalboy" dated item family, and a grim trophy-item texture pair.** Real decal/poster overrides: `propaganda_posters/regular/horizontal/friendship.ol`, `plakat/dec_mirnas.ol` ("plakat" is Russian for "poster"). A real recurring "stalboy" postcard/mascot quest item with 2 real dated texture variants (`stalboy_hw23_diff.ol`, `stalboy_summer24_diff.ol`), joining this project's established dated-content-series pattern from parts 55/56/58. A real `otkritka_diff.ol` ("otkritka" is Russian for "postcard"). And the real `quest_cutted_ear.ol`/`quest_cutted_ear_3.ol` pair -- a grim trophy-item texture (a severed ear), a second real body-part/trophy quest-item name in this jurisdiction override alongside the hair-bag items in Finding 4, plausibly (not confirmed) a ratings-sensitive asset in this region.

**Finding 6: `weapons/` (7 files) holds a real "lighter" bolt-throwable item, a dated Halloween weapon skin, and a cosmetic weapon-charm accessory pair.** A real "lighter" bolt-throwable item (`models/bolt/lighter/art/art_diff.ol`/`art_spek.ol` plus `textures/items/bolts/lighter.ol`), a real `mr153hell.ol` weapon skin, a real Halloween-2023 dated weapon skin (`styles/m1014b_halloween23.ol`), and a real cosmetic weapon-charm accessory pair -- `accessories/stalcraft_store/dakimakura.ol` and `dakimakura_naked.ol` -- disclosed factually as a real two-variant asset pair with no further claim about their in-game content, since the file names alone do not evidence what visually distinguishes the two textures.

**Finding 7: a real cosmetic sticker duplicated across two different subsystems.** `customisation/stickers/na/cowgirl.ol` appears under BOTH `stalcraft/textures/items/` (the sole file in `stalcraft/`) and `stalker/textures/gui/` (one of `stalker/`'s 3 files) -- the same real sticker name duplicated across two different subsystems in this one jurisdiction override, unconfirmed whether the two files are byte-identical or distinct per-subsystem variants.

**Self-correction / verification note.** No narrative-vs-live-count mismatch was introduced this part: every specific number in the class doc (5 folders, 60 files, the 8/1/3/41/7 per-folder split, the 18-name/11-pair/7-single stalkerguide breakdown, and every named cross-reference) was gathered directly from live `find`/`ls`/`cat` output on the real device directory before the Javadoc was drafted, then independently re-confirmed by running the compiled `KoreaJurisdictionCatalogDumpTool` against the real directory (5 folders, 60 files matched exactly) plus 12 targeted `foldersContaining` substring checks (`prestige`, `cutted_ear`, `stalboy`, `dakimakura`, `cowgirl`, `lighter`, `holepunch`, `mr153hell`, `m1014b_halloween23`, `otkritka`, `plakat`, `friendship`) -- every one resolved to exactly the folder(s) predicted, including `holepunch` correctly resolving to zero folders (since `foldersContaining` only searches file NAMES, not file contents, and "Holepunch" appears only inside `ko.lang`/`en.lang`'s text, which was verified separately via direct `cat`, not via this class's API -- this distinction is called out explicitly in the class Javadoc and the test class's own doc comment to avoid a future false expectation). Recorded here for consistency with parts 54-59's self-correction entries, even though this part had no narrative number to correct.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/KoreaJurisdictionCatalog.java` (new), `KoreaJurisdictionCatalogDumpTool.java` (new), `KoreaJurisdictionCatalogTest.java` (new, 12 tests using a 60-file real-filename fixture covering the 11-pair/7-single video asymmetry, the `prestige` anomaly, the `quest_cutted_ear` pair, the dated `stalboy` variants, the cross-subsystem `cowgirl` duplicate, and the `dakimakura` pair), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 60 bullet, test count 176 -> 188), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 133 total sources; 188/188 tests passing (up from 176 as of part 59). Real-data smoke test against the live device directory confirmed every predicted count exactly: 5 folders, 60 files, the 8/1/3/41/7 per-folder split, the 18-name/11-pair/7-single stalkerguide breakdown (independently re-verified via a fresh `find` listing), and all 12 targeted substring checks each resolving to the folder(s) predicted.

## Part 61 (2026-09-04) -- a first sweep of `modassets/assets/effects/particles/`: this project's richest cross-referencing content area yet, with a self-caught Javadoc correction on a real file duplicate

**Trigger.** Continuing self-directed under the standing "go on until you need my input" authorization, after completing part 60's full pipeline. Two remaining un-swept content areas were surveyed: `gloomycore/` (863 files, judged a messy grab-bag mixing engine internals with game content, deferred) and `effects/` (891 files total, of which `particles/` is 831). `effects/particles/` was selected as the clean, in-scope, richly evidenced target -- it ties together more open threads from prior parts than any other single folder swept so far.

**Finding 1: 48 real top-level folders, 829 real walked files, plus 2 more loose in the root -- the same disclosed root-vs-walked gap parts 55/57 found elsewhere.** A live top-level directory listing of `modassets/assets/effects/particles/` confirmed exactly 48 real folders. `EffectsParticleCatalog`'s folder-only walk (the same generic, no-hardcoding design as `ArmorModelCatalog`/`ArtefactModelCatalog`/`BackpackModelCatalog`/`ItemModelCatalog`) reaches 829 real files across those 48 folders. Two more real files -- `1.efkmat` and `ground_line_default.efkmat` -- sit loose directly in the `particles/` root, outside any subfolder, and are therefore genuinely invisible to this class's folder-only walk (confirmed by a dedicated regression test).

**Finding 2: `prestige/` (10 files) is the richest evidence yet for this project's recurring "prestige" cross-reference, and the first with real ability/mechanic names attached.** This is the fourth independent hit on the still-unconfirmed "prestige" thread, after part 57's `aptechka_color_prestige`, part 58's `token_prestige`, and part 60's `stream/video/prestige.ogg`. Unlike the first three, these are real ABILITY or MECHANIC names, not just a label on an unrelated asset: `charger_enabled`/`charger_enabled_distortion`, `scanner_installation_zone`/`scanner_zone`, `scout_stun`/`scout_stun_post`, `shield_depth_pulse`, `support_guard`, `tank_explosion_sphere`/`tank_explosion_zone`. The real "scout"/"tank"/"support" tokens read as class or loadout-role names, plausibly (not confirmed by any other evidence in this project) a role-based extraction-mode ability system gated behind this "prestige" mechanic.

**Finding 3 (self-correction, recorded per rule 1): the `laserpointer.efkmat` "isolated lead" was wrong -- it is a real duplicate, not unreleased content.** A real, separate `prestige_prototype_effects/` folder holds exactly 1 file, `laserpointer.efkmat`, using the literal word "prototype" in its folder name. The first Javadoc draft read this as a plausible cut/unreleased-content lead, since no other evidence in this project named what the laser pointer belonged to. Per this project's standing "verify before committing a narrative number" discipline, a live `foldersContaining("laserpointer")` check was run as part of the routine targeted-substring verification pass -- and it returned TWO folders, not one: `[prestige_prototype_effects, weapons]`. A direct `find` confirmed the second file: `weapons/stratg_exp/laserpointer.efkmat`, a real, identically-named file. The Javadoc was rewritten before finalizing to disclose this as a real duplicate instead -- a normal weapon-attachment laser-sight effect that also has a same-named copy filed under the "prestige prototype" folder, not necessarily unreleased content of its own. Whether the two files are byte-identical, and why the ordinary weapons-tree effect has a second copy here, is NOT evidenced by the file listing alone and remains open. This is the same class of catch as parts 53/55/58's self-corrections, and is now covered by a dedicated regression test (`EffectsParticleCatalogTest`'s "REGRESSION (part 61 self-correction)" case) to prevent it silently regressing.

**Finding 4: `shamanfight_fx/` (12 files) and `morgfight_fx/` (2 files) are real arena VFX for the Shaman/Morg boss encounter, corroborating parts 55 and 59.** Part 55 found the `shamanfight` artefact folder; part 59 found the `morg_shaman_fight`/`morg_shaman_meet` cutscenes. This part adds actual in-arena effects: a real `arena_boss_bg`, six real colored `shamanfight_field_zone_<color>` effects (`blue`, `green`, `orange`, `pink`, `red`, `yellow`), plus atmosphere (`clouds`, `clouds2`, `dust_particles`, `glow1`, `floor_t`). `morgfight_fx/` holds `morg_celling.efkmodel` -- a real on-disk spelling of "ceiling", the same class of in-game spelling quirk this project has disclosed before -- and `morg_floor.efkmodel`.

**Finding 5: `electrostorm_mutants/` (7 files) and `electrostorm_npc/` (2 files) give this project its first real evidence of specific mutant names tied to the "electrostorm" event.** The event was previously known only as a currency/token (part 58's `token_electroshtorm2026`). This part adds real spawner effects for `boar`, `burelom` ("windfall"/fallen-timber, Russian), `chimera`, `flesh`, and `krovosos` ("bloodsucker", Russian -- the classic S.T.A.L.K.E.R.-series mutant). `electrostorm_npc/` holds a real NPC `shield` effect for the same event.

**Finding 6: `operations/` (41 files) gives this project its first real evidence that "Electra" is an actual boss encounter, not just an artefact family.** Part 55 found the classic `electra/` artefact-model family. This part adds real `boss_electra` and `electra_pillar` effects, confirming a genuine boss encounter by that name. The same folder holds real raid/operation-mode content: `cargo`, `gas_ui_mat`, `ground_borders`, `heal_cross`, `operation_ship`, `operations_dome_shield`.

**Finding 7: `anomalies/` (70 files) directly corroborates four of part 55's classic artefact family names as real anomaly types, including a genuine spelling variant.** `electra_n` (also see Finding 6), `kisel`, `voronka_n`, and `carousel_n` are all real anomaly names here -- the last a real English spelling sitting next to part 55's transliterated `karusel`, a genuine cross-tree spelling variant, the same class of on-disk inconsistency this project has disclosed before (part 54's `dr_zivkas`/`dr_zivcas`, part 55's `krisstal`/`kristal`). Other real anomaly names: `batut_n` ("trampoline", Russian), `circus`, `gubilnik`, `lighter_n` (plausibly related to part 60's Korea-jurisdiction "lighter" bolt item, unconfirmed), `oboch_anomaly`, `portal`/`portal_inside`, `proto` (this project's recurring "proto"-prefix naming habit, parts 55/58), `psiop`, `slizen` ("slug", Russian -- plausibly a mutant, unconfirmed), and `tesla`.

**Finding 8: `artefacts/` (36 files) holds the first "26"-dated artefact name, plus the widest dated-seasonal-content spread this project has found in one sweep.** `buran26` extends the dated-content pattern from parts 55/56/58/60 -- which previously topped out at "26"-suffixed seasons and currencies -- into artefacts themselves. Other real names: `jele` (corroborating part 55's `n_art/collab_art/jele` leaf folder), `linza` ("lens", Russian), `petlya` ("loop"/"noose", Russian). Separately, a real, wide dated-seasonal-content pattern recurs across many independent subtrees in this one sweep: `hw23/` (Halloween '23, 3 files), `leto23/` ("summer" '23, 5 numbered `wall_*` effects), `buran23/` ("blizzard" '23, 2 files -- a real, differently-scoped "buran" than `artefacts/buran26`), `voron2024/` ("raven" '24, 9 files), real Battle-Pass-'24-prefixed melee/weapon skins (`melee/bp24_antiterror`, `melee/bp24_baton`, `weapons/bp24_deagle`), `winter25/` (1 file), and `caseopen/`'s real `case_autumn`, `case_ny25_inside`, and `efk_autumn24_tickets_big` -- together the widest real date-range (2023-2026) and the largest number of independent dated subtrees this project has found in a single sweep.

**Finding 9: `iznanka_iv/` (125 files, the single largest folder in this tree) and `ves_iv/` (22 files) share a real `_iv` suffix, with one confirmed Halloween tie.** "Iznanka" is Russian for "the other side"/"inside-out". It holds a real `HW_portal.efkmat` (tying it to a Halloween event), plus `barrier`, `bulat`, and `cubes_mat` materials; the real `_iv` suffix (Roman numeral four) plausibly marks a fourth iteration of a recurring event, unconfirmed. A second, smaller real folder shares the same `_iv` suffix -- `ves_iv/` (22 files): a real ice-themed arena (`ice_arena`, 5 numbered `ice_block_Collection_<n>` effects) -- disclosed as a real, possibly-related recurring numbered-event naming pattern, NOT confirmed as the same series as `iznanka_iv`.

**Finding 10: `caseopen/` (15 files) is real monetization VFX for the loot-case-opening mechanic, corroborating part 58.** Real effects: `case_larec_dust` ("larec" is Russian for "casket"/"small box"), `case_light_dust`/`_inside`, `case_light_open`/`_v2` -- corroborating part 58's currency/monetization findings with real UI/VFX evidence for the case-opening mechanic itself.

**Finding 11: other real, disclosed finds.** `sj_boss/` (1 file, `pfx_stonedebris1_boss.ol`) and `cybergiant/` (1 file, `testmat.efkmat`) are two more real boss/mutant names with only a single, minimal VFX file each so far -- plausibly early-development or lightly-used content, unconfirmed. `monolit_antena/` (3 files: `monolit.efkmodel`, `monolit_antena.efkmat`, `Noise120.mic`) is real VFX for the "Monolit" faction (parts 36/54's disclosed, still-hedged "Monolit -> Murmur" public-wiki rename reading) -- the first VFX-level evidence for that faction in this project. `scaries/` (4 files) holds a real `crow`, real `lenin_eye`/`lenin_eye_floor` effects (evocative Soviet-era imagery, unconfirmed in-game context), and `scaries_grdbasefire4_fwd`. `sermistor/` (11 files: a real `arena`, an `el_radius` effect, three numbered `tubes_<n>`, and a spark effect) is an unresolved name, plausibly a boss/hazard arena, not confirmed by any other evidence in this project. `placeholder/` (3 files, `zone1`-`zone3`) is a real, disclosed dev placeholder. `tutorial_eff/` (3 files: `ground_spawner`, `smoke_room_spawn`, `wall_Spawn1`) corroborates part 59's tutorial cutscene sequence with real gameplay VFX. `events/reihstag_tanks` is a sixth real Reichstag/RKKA WWII-event cross-reference in this project (after armor/backpacks/meds/currency/stalkerguide), spelled "reihstag" here -- a real, disclosed cross-tree spelling variant of "reichstag" used everywhere else. `grenades/` (15 files) holds real special grenade types: `black_hole`, `efk_raidal_grenade`, `low_gravity`, `n_aoe`. `melee/` (15 files) also holds a real named knife, `knife_crowbeak`, alongside its dated Halloween-'23 katana effects noted above.

**Finding 12: a real, disclosed naming-case inconsistency.** `Texture/` (141 files, the second-largest folder) and `NewVFX/` (4 files) are capitalized, unlike all 46 other real top-level folder names in this tree, which are lowercase -- the same class of on-disk inconsistency already disclosed in parts 54/55.

**Self-correction (recorded on purpose, rule 1).** The one real narrative error caught this part is Finding 3 above: the first Javadoc draft framed `prestige_prototype_effects/laserpointer.efkmat` as an isolated, unexplained cut/unreleased-content lead. A live `foldersContaining("laserpointer")` check -- run as routine part of the targeted-substring verification pass, not prompted by any external doubt -- returned two folders instead of the expected one, and a direct `find` confirmed a second, identically-named real file under `weapons/stratg_exp/`. The Javadoc was corrected before finalizing to disclose the real duplicate instead of the incorrect "isolated lead" framing, and a dedicated regression test (`EffectsParticleCatalogTest`) now locks in the corrected 2-folder result so this cannot silently regress. No other narrative number in the class doc required correction: the 48-folder/829-file total, all per-folder file counts cited above, and every other targeted substring check were verified live against the real device directory before finalizing and matched exactly.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/EffectsParticleCatalog.java` (new), `EffectsParticleCatalogDumpTool.java` (new), `EffectsParticleCatalogTest.java` (new, 14 tests using a real-filename fixture covering the prestige ability names, the corrected `laserpointer.efkmat` duplicate as a dedicated regression test, the shamanfight/electrostorm/anomalies/artefacts cross-references, a dated-seasonal name, and the loose-root-file gap), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 61 bullet, test count 188 -> 202), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 136 total sources; 202/202 tests passing (up from 188 as of part 60). Real-data smoke test against the live device directory confirmed the 48-folder/829-file total exactly, and roughly 40 targeted substring checks each resolved to the folder(s) predicted, except the one real laserpointer discrepancy caught, disclosed, and corrected as described above.

## Part 62 (2026-09-04) -- closing out `modassets/assets/effects/`: day/night and weather rendering, a real PNV texture set, and a second self-caught file-duplicate correction

**Trigger.** Continuing self-directed under the standing "go on until you need my input" authorization. Part 61's own class doc for `EffectsParticleCatalog` explicitly flagged three sibling folders under `effects/` as unswept: `models/` (11 files), `shaders/` (1 file), `textures/` (48 files) -- "NOT covered by this sweep and remain open for a future part." This part closes that loop with a new class, `EffectsAssetCatalog`, rooted one level up at `modassets/assets/effects/` itself so its folder-only walk naturally covers all 4 real top-level folders there (`models`, `particles`, `shaders`, `textures`), re-confirming `particles/`'s already-catalogued 831 files unchanged while adding real coverage of the other three (60 files).

**Finding 1: 4 real top-level folders, 891 total real files, no root-level gap at the `effects/` level itself.** A live top-level directory listing confirmed exactly 4 real folders directly under `effects/` -- unlike `particles/`'s own root (which has 2 loose files outside any subfolder, per part 61), `effects/` itself has no loose files sitting directly in it. `models` (11), `particles` (831, matching part 61's number exactly), `shaders` (1), `textures` (48) sum to 891, matching this project's own part-61 scoping note ("effects/ 891 files total, of which particles/ is 831").

**Finding 2: `models/` (11 files) holds real anomaly/weather-tied effect geometry.** `gravitation_distort.ol` and `portal_dist1.ol` directly corroborate part 61's `anomalies/` findings (gravity/funnel-type anomalies, and the real `portal`/`portal_inside` anomaly names) with actual distortion-effect models rather than just anomaly-folder labels. `teleport_bubble.mcsb` is a real teleport-effect model; no other "teleport" evidence has surfaced anywhere else in this project so far, so this is disclosed as a fresh, unconfirmed lead. `fxcloudroundtile.ol`/`fxfogheavy.ol`/`fxgroundwhisps.ol`/`fxpuffstile.ol` are real atmospheric/weather-tile effect models. `capsule_flat.mcsb`/`capsule_symmetric.mcsb`/`unit_sphere.mcsb` are real generic collision/bounding primitives, plausibly reusable scaling bases for other effects rather than named content of their own. `model_list.pack` is a real packed manifest/index file, not model geometry itself.

**Finding 3 (self-correction, recorded per rule 1, same class as part 61's laserpointer catch): `gravitation_distort.ol` is a real cross-folder duplicate.** The first Javadoc draft, based on the initial `models/` listing alone, described `gravitation_distort.ol` as living only under `models/`. Per this project's standing "verify before committing a narrative number" discipline, a live `foldersContaining("gravitation_distort")` check -- run as routine part of the targeted-substring verification pass -- returned TWO folders instead of the expected one: `[models, particles]`. A direct `find` confirmed the second file: `particles/Texture/gravitation_distort.ol` -- the same capitalized `Texture/` folder part 61 already flagged for its real naming-case inconsistency (141 files, one of only 2 capitalized top-level folders in the whole `particles/` tree). The Javadoc was corrected before finalizing to disclose this as a real cross-folder duplicate rather than the incorrect single-folder claim, and this is now covered by a dedicated regression test (`EffectsAssetCatalogTest`'s "REGRESSION (part 62 self-correction)" case) so it cannot silently regress. Whether the two files are byte-identical is unchecked and remains open, same as part 61's unresolved laserpointer question.

**Finding 4: `shaders/` holds exactly one real file, disclosed plainly as engine machinery, not content.** `light/ft_culling.comp` is a real compute shader, plausibly for forward-tiled lighting culling. Unlike everything else catalogued this part, this is pure rendering-engine technical content rather than narrative game content -- it's included for completeness of the `effects/` picture, but flagged explicitly as the one file in this sweep that isn't a "finding" in the usual sense.

**Finding 5: `textures/` (48 files) is dominated by real dynamic-weather and day/night rendering sets, plus one striking new named-event lead.** `rain/` (6 files) holds real `rain`/`snow`/`ash_1`/`ash_2` weather textures, plus two real `PsiEjection_particles` files -- a real weather-particle name that combines this project's recurring "psi" theme (part 61's `psiop` anomaly name) with "ejection." This is plausibly, though not confirmed by any other evidence in this project, the classic S.T.A.L.K.E.R.-series anomalous-emission event ("vybros" in the original Russian, sometimes localized as "emission") appearing here under a literal English name rather than a transliteration. `snow/` (8 files) is a real ice/snow texture set; `water/` (4 files) is real water-surface textures. A full real day/night rendering set -- `sun`/`sun.ol`/`sun_envmap`/`sun_glare`/`sun_gradient`/`sun_occlusion`/`sunglare` -- sits alongside `moon_phases.mic`, the first evidence in this project's swept trees of a day/night or lunar-cycle rendering system. `bleeding.mic` is a real HUD/status screen-effect texture for a bleeding or low-health state. Six real `pnv_offset`/`pnv_offset0-4.ol` files plus `nvd_lamp.ol` are a real PNV (passive night-vision device) glow/static texture set -- not otherwise catalogued elsewhere in this project's swept trees so far. The remaining files (`brdf.ol`, `dummy_nmap.ol`, three `envmap_*` files, `fx_noise.ol`, `horizon_mask.ol`, `kubemas.ol`, two `lens_*` files, `missing_mipmaps.ol`, `noise_stencil.ol`) are real generic rendering-utility textures, not narrative content.

**Self-correction (recorded on purpose, rule 1).** The one real narrative error caught this part is Finding 3 above: the first Javadoc draft claimed `gravitation_distort.ol` lived only under `models/`. A live `foldersContaining` check -- routine, not prompted by outside doubt -- found it also under `particles/Texture/`, and the Javadoc was corrected before finalizing, with a dedicated regression test added. This is the second time in two consecutive parts (61, 62) that this exact verification step has caught a real single-folder assumption that should have been a two-folder disclosure -- both times involving the `Texture/` (part 61) or a `Texture/`-adjacent (this part) folder, suggesting that capitalized folder is worth extra scrutiny in any future sweep that touches it again. No other narrative number in this part's class doc required correction: the 4-folder/891-file total and every other targeted substring check were verified live against the real device directory before finalizing and matched exactly.

**Files changed this part:** `server-emulator/src/main/java/net/stalzone/emulator/world/props/EffectsAssetCatalog.java` (new), `EffectsAssetCatalogDumpTool.java` (new), `EffectsAssetCatalogTest.java` (new, 12 tests using a real-filename fixture -- the complete real 11-file `models/` folder, the complete real 1-file `shaders/` folder, the complete real 48-file `textures/` folder, and a small representative `particles/` subset including a dedicated regression test for the corrected `gravitation_distort.ol` duplicate), `server-emulator/src/main/java/net/stalzone/emulator/testing/AllTests.java` (registered the new test suite), `server-emulator/README.md` (new part 62 bullet, test count 202 -> 214), `docs/RESEARCH_LOG.md` (this entry). Compiled clean at 139 total sources; 214/214 tests passing (up from 202 as of part 61). Real-data smoke test against the live device directory confirmed the 4-folder/891-file total exactly, and every targeted substring check resolved to the folder(s) predicted, except the one real gravitation_distort discrepancy caught, disclosed, and corrected as described above.
