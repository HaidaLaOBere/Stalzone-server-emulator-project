# STALZONE Server Emulator — Project Plan (draft v1, 2026-08-30)

## Vision
A self-hosted server emulator for STALZONE (target build: 2026-07-29, AppID 1818450) so people can play solo or run fully-controlled private servers — spawn rates, damage values, economy, etc. all adjustable — without the live game's grind. Long term: a management GUI and modding support, in the spirit of SPT (Single Player Tarkov) for Escape from Tarkov.

## Decided
- Server implementation language: **Java/Kotlin** (matches the client's own JVM/Netty stack).
- Evidence policy: see `RESEARCH_LOG.md` — nothing goes into the emulator as "confirmed protocol" without a decompiled-code, capture, test, or cited-source basis.
- First playable milestone: **a single static zone, walk-around only** — real client connects, authenticates against our server, spawns into one map, position sync works both ways. No combat, AI, loot, or inventory yet.

## Phases

**Phase 0 — Research infrastructure (in progress)**
- Recon of the local client build: done a first pass (see RESEARCH_LOG.md) — JVM/Netty/JWT/Protobuf identified, classes.jar found to use a custom hash-blob packing format that blocks direct decompilation.
- Traffic capture guide written (`CAPTURE_GUIDE.md`) — next action is on you: capture a login + walk-around session (HTTPS via mitmproxy, raw socket via Wireshark) and send the exports back.
- Still open: how to get readable bytecode out of classes.jar (leading option: a live class-dump Java agent — see RESEARCH_LOG.md's risk note before we build/use it), understanding `client_class_index.txt`'s exact format, checking whether the Tier-1 community repos already solved the classes.jar packing question.

**Phase 1 — Protocol skeleton**
- From captures + decompiled classes: identify the login/auth handshake, the session token format (JWT claims), and the Netty framing (length-prefix? opcode byte? protobuf message wrapper?).
- Stand up a minimal Java/Netty server that can complete the handshake and get the real client to a "connected, in character select" state — even with fake/static data.

**Phase 2 — First zone, movement**
- Implement whatever packet(s) carry player position/rotation/velocity.
- Spawn the client into one real map with a static world (no other entities), confirm bidirectional movement sync and basic zone boundaries.

**Phase 3 — Core gameplay systems**
- Inventory, items (cross-reference `EXBO-Studio/stalzone-database` for item data), NPCs/mobs, combat, loot, economy — each gated behind its own evidence (capture the real behavior first, then implement).

**Phase 4 — Configurability & tooling**
- Externalize the things the user explicitly wants control over: spawn rates, damage values, economy multipliers, etc. into config files.
- Start the management GUI (local web dashboard is the likely default choice; revisit once the server skeleton exists).

**Phase 5 — Modding support**
- Design a mod-loading layer once the core server shape is stable (mirroring how SPT exposes hooks) — too early to spec in detail now.

## Resource tiers (from the user's research)
- Tier 1: `Art3mLapa/unofficial-stalzone-api`, `onejeuu/sc-file`, `onejeuu/sc-mapmerge`, `EXBO-Studio/stalzone-database`, `EXBO-Studio/stalzone-api`, `EXBO-Community/stalcraft-jvm-optimization`, EXBO forum network-protocol thread.
- Tier 2: `kayex/stalcraft`, `Oidaho/stalzone`, EXBO-Studio org, EXBO-Community org, StalCube (historical preservation, repo unverified).

## Repo layout (started)
- `docs/` — RESEARCH_LOG.md (findings, confirmed vs hypothesis), CAPTURE_GUIDE.md, PROJECT_PLAN.md (this file).
- `server-emulator/` — will hold the actual Java/Kotlin server project once Phase 1 starts.
