# Expanded replica — docs/renderer-jams/20261026-lod-scratch/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261026-lod-scratch/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261026-lod-scratch/BACKLOG.md |
| Preserved source SHA-256 | `77f5e548fd77e5391bca2aa606f4d5ea329743184ae085e92b1ef0fe2237c865` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Reuses a build-local builder and largest scratch mask across three LOD levels, with copied output arrays/lists and no global or cross-job cache. Retained-output and parallel tests protect aliasing; reduced allocations coexist with some slower CPU medians. Use native allocation and queue-age evidence to decide whether further scratch retention is justified.

## Research expansion — gaps and details to deepen

- Per-build reuse should be connected to total concurrent-worker scratch and cancellation lifetime. A lower allocation count can still retain a large mask per worker or move cost into copying and publication.

## Research expansion — proposed contracts

- Specify maximum scratch capacity, owner lifetime, output immutability, and release on cancellation. Budget active workers times worst-case scratch separately from published meshes and record copied-output cost alongside allocation reduction.

## Research expansion — acceptance checks

- Retain retained-output alias, parallel, maximum-height, and cancellation checks. On the actual artifact, measure peak live heap, GC pauses, build/publication age, and complete-frame tails for realistic worker counts before claiming a gain.

## Research expansion — source anchors

- Renderer backlog after jam4

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer backlog after jam4

Done: exact part bounds; opt-in bounded CPU diagnostics; bit-exact opaque/air work reduction; build-local Builder/mask reuse across three detail levels. Measured allocation reduction30–49% on five CPU fixtures; returned mesh/GPU bytes unchanged. Frozen geometry, aliases, retained output,64parallel jobs, maximal4096height and cancellation regressions pass.

Next: actual named-device camera/travel/edit/full-frame/culling/telemetry lifecycle; investigate mixed CPU timings (three component medians regressed~3%, no FPS gain established). Complete worker rebuild/allocation, descriptor/draw, GPU timing and actual RAM/VRAM instrumentation. Evaluate further mesh reuse/scheduling and hierarchy/coverage/fence-safe storage with real scenes.

No unseen-terrain delivery contract or stream yet; coordinate generator/network prerequisites within renderer scope. Independent full-detail/vertical/LOD admission stays separate from simulation. No raised-max-only credit. Actual FSR/DLSS providers/licensing/inputs/dispatch/lifetime/device gates remain open; frame generation is later and distinct.

Renderer self-audit3.67/10 (+0.17):4,6,4,0,4,4. External engine rubric unchanged. CPU timing work is completed/nonexclusive; publish completion immediately after future probes so other jobs need not infer a timing reservation from file ownership. Dedicated renderer schedule remains enabled; preserve other owners' work.


<!-- END PRESERVED SOURCE -->
