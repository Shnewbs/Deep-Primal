# Renderer Jam 64 — bounded LOD diagnostics snapshot

This jam adds a read-only diagnostics snapshot to LodDiskIndexService for reproducible renderer baseline capture. It reports queue/renewal admission, scan progress, retry/backoff, cursor completion/missing-directory state, retiring-worker state, and whether bounded apply work is pending. It does not start filesystem work, mutate queue state, or change rendering behavior.

Scope exclusions: no terrain-generation redesign, water simulation, GUI redesign, ports, standalone engine, or FSR/DLSS implementation changes.
