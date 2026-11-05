# Renderer jam 56 — lazy bounded shed allocation

This focused renderer jam reduces client-thread allocation in the bounded persisted-LOD refresh handoff.

Production change:
- `LodDiskIndexService.PendingApply` no longer allocates an empty `HashSet` for every scan apply.
- The shed set is created only if the queue is actually full and a discovered page must be evicted.
- Existing 256-entry-per-turn, 8192-key cap, renewal protection, retry cadence, cursor retirement, mesh/water ordering, Vulkan/OpenGL paths, and FSR/DLSS behavior are unchanged.

Scope excludes terrain generation, water simulation/chemistry, survival/content, GUI redesign, ports, standalone game work, and unrelated schedules.
