# Traffic Capture Guide (draft v1)

Goal: get real, evidence-grade samples of (a) the launcher's HTTPS API calls and (b) the raw game-server socket traffic, so protocol claims in RESEARCH_LOG.md can move from HYPOTHESIS to CONFIRMED.

**Read the risk note at the bottom before doing anything while logged into the live/official servers.**

## Part A — HTTPS launcher/API traffic (lower risk)

The launcher and account/session APIs (`launcher.stalcraft.net`, `backend.stalcraftx.ru`, etc. — see RESEARCH_LOG.md) are almost certainly plain HTTPS/JSON, same as any web API. Recommended tool: **mitmproxy** (free, cross-platform).

1. Install mitmproxy on the same Windows machine the game runs on: https://mitmproxy.org/ (or `pip install mitmproxy`).
2. Run `mitmweb` (gives a browser UI at http://127.0.0.1:8081 to watch requests live) and note the proxy port (default 127.0.0.1:8080).
3. Point the *launcher/game process* at that proxy. Two options:
   - System-wide: Windows Settings → Network → Proxy → manual proxy 127.0.0.1:8080. (Captures everything on the machine, noisier but simplest.)
   - Per-process (cleaner, if the launcher respects them): set `HTTP_PROXY`/`HTTPS_PROXY` environment variables before launching the game.
4. Install mitmproxy's root CA so HTTPS can be decrypted: visit http://mitm.it while the proxy is active and follow the Windows instructions, or the game may pin its own certificate and simply refuse to connect through the proxy — if every connection to `launcher.stalcraft.net`/`backend.stalcraftx.ru` fails once the proxy is on, that itself is useful evidence (cert pinning present) and we fall back to Part B for that traffic, or a jailbroken/patched client detour later.
5. Log in and play normally for a few minutes (login, character select, entering a zone, moving around, maybe a couple of inventory/vendor actions since we'll want those schemas eventually too).
6. In mitmweb, export the flows: Save → a `.flow` file (or File → Export → cURL/HAR per request). Send me the exported file(s) — I'll parse them without needing them run through Claude's own network tools (which can't see your machine's traffic).

## Part B — Raw game-socket traffic (the actual movement/gameplay protocol)

This is very likely a single persistent TCP connection to one of the IPs from `backend.stalcraftx.ru/address_list` (a "tunnel"), using Netty framing (see RESEARCH_LOG.md). HTTPS proxies don't see this — need a packet capture.

1. Simplest: **Wireshark** (https://www.wireshark.org/) on the same machine.
2. Start a capture on your active network adapter *before* launching the game.
3. Use a display filter once you know (or suspect) the server IP/port, e.g. `ip.addr == <server ip> && tcp`. If the IP isn't known yet, first capture everything for ~10 seconds around login, then in Wireshark do Statistics → Conversations → sort by bytes to spot the one long-lived high-traffic TCP connection distinct from the HTTPS launcher calls — that's almost certainly the game socket.
4. Play for a few minutes doing simple, repeatable actions one at a time with a short pause between each (e.g.: stand still 5s, walk forward 5s, stop, turn around, jump once, open inventory, close it). Repeatable, isolated actions make it much easier to correlate specific packets to specific actions afterward.
5. Stop the capture, save as `.pcapng`, send it over. Note roughly *when* (capture-relative time) you did each action so we can correlate.

## Part C — Client-side log correlation (free, no extra tooling)

While capturing, also grab a fresh copy of `logs/netcode.log` and `logs/client-N.log` (the most recent one) right after your play session — timestamps there can be lined up against the pcap to help identify which packets correspond to which logged client-side events.

## Risk note — read before capturing

- Part A (HTTPS proxy) and Part B (passive packet sniff) are both **passive** — they don't modify the client process or inject anything, so they carry meaningfully lower anti-cheat detection risk than the class-dump-agent approach mentioned in RESEARCH_LOG.md (which *does* touch the running JVM and is not recommended yet).
- That said, no external tool's risk profile against a specific anti-cheat (EXENS here) is something I can verify for you — if the client is logged into the official STALZONE servers, capture at your own risk, ideally with an account you're comfortable being flagged. If you'd rather not risk your main account, using a spare/alt account for capture sessions is the safer path.
- If mitmproxy's CA install fails or every request breaks under the proxy, stop — don't try to bypass certificate pinning by patching the client; that crosses into modifying the client binary, which is a different (and much riskier) can of worms than passive capture.
