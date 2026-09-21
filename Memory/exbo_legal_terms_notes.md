# EXBO License Agreement / Privacy Policy — notes relevant to this project

Source: `gl_licence.pdf` (31 pages) and `gl_privacy.pdf` (11 pages), added to the project folder
by the user on 2026-09-17, described by the user as "the latest policies I've had to agree on by
EXBO." Read in full (converted to text and reviewed) this same session. Recorded here per this
project's standing rule 2 (EXBO, the real-world publisher/developer, is explicitly in scope) and
because this content is directly relevant to the whole project's premise, not just one part.

**Not legal advice.** This is a factual summary of clause text for future-session reference, not a
legal opinion on what the user may or may not do. The user should read the full documents
themselves and consult a lawyer if they want an actual legal assessment.

## Directly relevant clauses (License Agreement)

- **3.1(c)** — the User is not permitted to "access the source code of the Game in any way,
  including, but not limited to, reverse engineering, disassembling, or the like."
- **4.1.6** — the User shall not "Connect to the Game without authorization, including
  unauthorized servers that emulate or attempt to emulate the operation of the Game, as well as
  connect using third-party programs or tools not specifically authorized by EXBO Company."
  This clause's wording ("servers that emulate... the operation of the Game") maps directly onto
  this project's own stated goal ("MAKE a server emulator to run the game STALZONE singleplayer
  or as private server").
- **4.1.16** — the User shall not "Decompile, extract technology, research, decrypt, perform
  reverse engineering of data, bypass data security systems, conduct hack/attempt to hack into
  software components of the Game or its services, and/or intercept data traffic to or from the
  server." Also: "it is prohibited to modify, alter, decompile, decrypt, sell, distribute modified
  Game content in whole or in part (or the means or materials to perform the specified actions),
  use bugs, modify program code, or change the functionality of the Game Client." This directly
  covers this project's wire-protocol capture/decryption work (already gated behind explicit
  per-instance user go-ahead under standing rule 3) and, more broadly, most of this project's
  reverse-engineering methodology.
- **4.1.18** — prohibits distributing the Game or copies of it, for commercial or non-commercial
  purposes, without EXBO's written permission — relevant if any packaged output of this project
  (client files, extracted assets, compiled emulator bundles referencing extracted data) were ever
  shared publicly rather than kept local. Standing rule 6 (no GitHub push without explicit request)
  already limits this in practice, but this clause is the underlying reason that rule matters.
- **4.1.19** — prohibits distributing information about "game bugs, vulnerabilities, mods, cheats,
  exploits, bots, systems, etc." publicly (YouTube, Twitch, etc.) — relevant if any research
  findings from this project were ever shared outside a private/local context.

## A carved-out exception for some modding (License Agreement 4.1.1)

4.1.1 prohibits replacing/modifying game files, cheats, bots, hacks, "mods," or unauthorized
software that modifies or interferes with the game experience — **but with an explicit exception**:
"the various ways to modify files described in the article at
https://support.exbo.games/en/help-center/articles/60/62/386 and the 'mods' folder, located in
the folder with the Game." This means EXBO has an official, sanctioned modding path (a support
article plus a real `mods/` folder shipped with the game install). This project's own "modding
support (similar to what SPT team has done)" goal may be able to align with this official path
specifically — worth reading that support article in a future part if/when modding support work
begins, to understand what EXBO already explicitly permits versus what would fall outside it.

## An official API exists too (License Agreement 3.2)

"The User is entitled to create derivative works using the official STALZONE API, where such
works shall comply with the STALZONE API Rules and this Agreement and shall not interfere with
the Game, its infrastructure, or user safety." A real, separate "STALZONE API Rules" document is
referenced but was not itself supplied/reviewed this part — another lead worth following up if
this project's modding/server-integration goals could be served by an officially sanctioned API
surface instead of (or alongside) an unauthorized emulator.

## Privacy Policy — Anti-Cheat and Enforcement Data (section 3)

EXBO states it processes "behavioural data and gameplay activity, device and system signals,
in-game interactions and logs, reports submitted by other users, and indicators of potential
violations" specifically "to detect, investigate and prevent cheating, exploitation, fraud, abuse
and other prohibited conduct," with enforcement actions up to account termination. This is a
direct, textual confirmation of the account-ban risk this project's standing rule 3 already treats
as a given for wire-protocol work — not new information in kind, but now backed by the actual
policy text rather than inference. Per rule 2, the anti-cheat SYSTEM itself (EXENS/launcher
internals) remains explicitly out of this project's scope; this note is only about the stated
existence and purpose of that monitoring, not an investigation of how it works.

## Bottom line for this project going forward

Nothing here is new in terms of the KIND of risk (ban risk for the account was already the
explicit premise of standing rule 3) — but 4.1.6 in particular gives that risk very specific,
literal textual backing for the project's core "server emulator" premise, not just for the
wire-protocol-decryption sub-activity rule 3 already singles out. Worth the user's own periodic
awareness that this project's entire premise sits outside what the license agreement permits, per
its own text, independent of how any particular part's technique (static asset reading vs. live
wire-protocol capture) is scoped. This project's own standing rules already reflect a
risk-accepting stance (rule 3's opt-in gate, rule 6's local-only default) that seems consistent
with, not contradicted by, what these documents say — but this note exists so a future session
starting fresh has the actual clause text on hand rather than relying on inference alone.
