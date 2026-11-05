# Renderer jam 63 — publish discard fast path

Parent main: dfecaafcfae223efbfa6a3de65198365d6106e9c

## Scope
Renderer-only bounded LOD disk-recovery publish bookkeeping. No terrain-generation redesign, water simulation, GUI redesign, ports, FSR/DLSS provider changes, or unrelated work.

## Implementation
LodDiskNearestScan.Cursor.publish() now keeps the normal discard result as List.of() and lazily allocates the mutable shed list only when a recovery key is actually absent from the authoritative retained set. This avoids an empty ArrayList on successful scans with no discarded recovery pages, while preserving immutable completion payloads.

Implementation commit: 3e006784887363c0184be1f45b4ae09928eeadd9
