# Why this folder exists

The real, long-lived project memory for this project (STALZONE server-emulator reverse-engineering)
was originally stored through the `mcp__remote-devices__project_memory_read`/`project_memory_write`
MCP tool: `recon_findings.md` + `recon_findings_cont02.md` through `cont70.md` + `MEMORY.md`,
covering Parts 1 through 114.

Partway through Part 115 (mid-September 2026), that tool started reporting "Project memory is not
available in this session" — on every retry since, across multiple sessions, including after a full
MCP server disconnect/reconnect cycle and a `RefreshMcpTools` call. The cause is unknown from inside
any session (no detail beyond the flat unavailable message) and does not appear to be a
connectivity issue, since the device link itself and every other `mcp__remote-devices__*` tool work
normally throughout.

Four parts' worth of findings (115-118) were written to stopgap files
(`docs/pending_memory_update_part115.md` through `part118.md`) while waiting for the tool to come
back. The user then created this `Memory/` folder directly in the project directory and asked that
memory be kept here instead, as a real plain-file fallback rather than a temporary stopgap — so this
folder is now the ACTIVE store for project memory going forward, not another waiting-room.

**Migrated so far:** the four stopgap files' content now lives in this folder as
`recon_findings_cont71.md` through `cont74.md` (see `MEMORY.md` for the index). The original
stopgap files in `docs/` have been moved to `_to_delete_docs/` (Claude cannot delete files directly
on this device — the user can delete that folder whenever convenient).

**What is NOT here:** `recon_findings.md` and `recon_findings_cont02.md`-`cont70.md` (Parts 1-114)
still exist only inside the MCP project-memory store, which remains unreachable. Nothing from that
period has been reconstructed here — doing so from imperfect recollection would risk introducing
errors into a record whose whole value is accuracy (per this project's standing rule 1: never turn
an assumption into a specification). If the MCP tool becomes reachable again in some future session,
that old content should be read out via `project_memory_read` and either left in place (with this
folder's cont71+ files copied in to merge the two stores) or migrated here in full, whichever the
user prefers at that time — this is a decision for that future session, not one made now.

**Going forward:** every future part's findings get written directly to a new file here
(`recon_findings_cont75.md`, `cont76.md`, ...), with `MEMORY.md` updated to list it, exactly the
same way the MCP tool's `project_memory_write` was used before the outage. No more
`pending_memory_update_*` stopgap files should be created in `docs/` unless this folder itself
becomes unreachable for some reason.
