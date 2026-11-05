# Expanded replica — docs/renderer-jams/20261027-signed-distance/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-signed-distance/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-signed-distance/BACKLOG.md |
| Preserved source SHA-256 | `800ced9c20dde1518d54792fc3f6ae97ec5a7194f6bd9dfa6fdf429d6d57be86` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Widens coordinate subtraction before distance arithmetic in five renderer selection and handoff sites. It preserves practical-world behavior and prevents signed-int delta wrap, with large pure-Java coordinate coverage; squared double distance still has approximate ties at extremes. Prioritize exact-tie successors and camera-relative native fixtures before increasing distance promises.

## Research expansion — gaps and details to deepen

- Widened subtraction does not make every ranking exact or every camera transform stable. The expansion should distinguish legal coordinate range, exact tie ordering, neighbor-key arithmetic, and GPU-relative projection as separate contracts.

## Research expansion — proposed contracts

- Document checked coordinate construction, world/dimension key identity, arithmetic precision, and deterministic tie policy. Link later exact capture-distance and disk-ranking work rather than implying this initial helper resolves all extreme-coordinate behavior.

## Research expansion — acceptance checks

- Retain signed-extreme and practical-border oracle checks, then exercise neighboring keys, retarget, culling, and near/LOD handoff at large coordinates. Require native image stability and unchanged authoritative lookup; a million helper comparisons are not GPU evidence.

## Research expansion — source anchors

- Renderer backlog after signed-space admission jam

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after signed-space admission jam

1. Owner-run Java25 exact-SHA full Gradle/NeoForge compile plus native client/game and isolated artifacts; verify source/JAR identities and archive/native resources; no qualified JAR was produced by this renderer jam.
2. Real traveled negative/positive chunk-edge client: index discovery, finite queue/native overlap/handoff, loaded/unloaded cache, fragment materials and water visibility on both Vulkan and OpenGL, then named-hardware CPU/GPU frame p50/p95/p99, upload/cull/submit times and memory.
3. Bound GPU upload retries/error rollback and sparse long-travel LOD fair discovery (do not expand unserved terrain or full-detail tracker without network/controller budgets).
4. Temporal camera jitter + full scene motion/depth, valid NGX Vulkan extension/dispatch/resource barriers and real RTX lifecycle; preserve actual AMD FSR1 and native fallback; frame generation separate.

Open source commit: 52635aa9c774c61c20271102b7719d49985ef1e5; self-audit 28/60 (4.67), carried unchanged; engine independent 50/100 (5.0), carried unchanged. Both acceptance thresholds remain open. No build/release publication.


<!-- END PRESERVED SOURCE -->
