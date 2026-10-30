# Renderer jam 53 — renewal-map hot-path probe

This renderer-only jam keeps the bounded LOD disk-index contract unchanged while removing a duplicate HashMap probe on recovery renewal.

`LodDiskIndexService.recoveryQueued` now uses `putIfAbsent` and only enters the overflow fail-closed path when a new owner token would exceed the 8,192-key bound. Existing scan-generation semantics, bounded queue application, retry cadence, worker retirement, and transparent-water/render ordering are unchanged.

Scope excludes terrain generation, water simulation/chemistry, GUI redesign, ports, standalone-game work, and any other scheduler.

No qualified JAR or GPU acceptance is claimed under the owner override.
