# Expanded replica — docs/renderer-jams/20261029-frustum-ab-gate/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-frustum-ab-gate/README.md) |
| Source identity | git: docs/renderer-jams/20261029-frustum-ab-gate/README.md |
| Preserved source SHA-256 | `14b1b354a74632c507863d756a7594d3c0ddcedd00df6693f5cf939dced190b2` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Provides a startup-only broadphase opt-out and diagnostics for coarse attempted, visible, rejected, and avoided fine tests. Only case-insensitive literal false disables the default-on path; client restart is required. Completed telemetry extracts satisfy tested equals visible plus rejected, with zero coarse counters when disabled.

## Research expansion — gaps and details to deepen

- The useful comparison requires more than matching the switch: extraction population, cache state, active scene extent, and later adaptive flags must be recorded. Disabled coarse tests can still retain cached bounds, so this A/B does not isolate every bounds allocation or construction cost.

## Research expansion — proposed contracts

- Add a capture manifest for effective startup options, frame/extraction identity, scene target, coarse-bound cache state, and counter population. Keep the switch separate from saved preferences and expose unavailable or incomplete samples rather than interpreting stale counters as current work.

## Research expansion — acceptance checks

- Restart between default, false, and FALSE variants; verify unchanged ordered visibility, native handoff, and completed-extract counter identities. Include all-visible adverse scenes, telemetry-on/off cost, full lifecycle, and matched Vulkan/OpenGL frame and water-image captures.

## Research expansion — source anchors

- Renderer jam 26 — page-frustum broadphase A/B and accounted checks
- Source-focused verification

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 26 — page-frustum broadphase A/B and accounted checks

**Scoped renderer-only source increment.** The existing conservative multi-part
LOD page-frustum optimization remains enabled by default. For matched native
Vulkan/OpenGL comparisons, launch the Minecraft client with
`-Ddeepprimal.lod.pageFrustumBroadphase=false` to restore the original fine
part-only frustum path, or omit it (default-on). Only the case-insensitive literal
`false` disables it. **Restart the client** between A/B variants; this is a
JVM-startup developer switch, not a saved world/video preference or a GUI toggle.

With `-Ddeepprimal.lod.telemetry=true`, diagnostics expose:

- `lodPageBroadphaseEnabled`: actual startup selector state
- `lodCoarseTestedPages`: page-level frustum tests on the last completed extraction
- `lodCoarseVisiblePages`: page-level tests that required normal fine tests
- `lodCoarseRejectedPages`: page-level tests rejecting the entire page
- `lodCoarseSkippedPartTests`: fine mesh tests skipped by coarse rejects

For completed telemetry-enabled extracts, tested = visible + rejected.
With the A/B flag off, all four coarse counters are zero; the original ordered
fine-part tests and native-chunk handoff remain in place. Coarse bounds can still
be cached but are not tested. No shader, mesh, material ordering, water simulation,
upscaling, render-distance maximum, GPU submission, or driver capability changed.

## Source-focused verification

The committed production helper and option regression build using the
production-excluded Java API doubles from
`../20261029-page-frustum-broadphase/stubs`. Example from the
`docs/renderer-jams/20261029-frustum-ab-gate` directory:

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  ../20261029-page-frustum-broadphase/stubs/net/minecraft/world/phys/AABB.java \
  ../20261029-page-frustum-broadphase/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java \
  ../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java \
  tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumOptionChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp classes com.shnewbs.deepprimal.lod.client.LodPageFrustumOptionChecks
```

These isolated tests prove bounded source-level visibility parity and the
startup switch, not native pixel correctness, actual GPU frame times or FPS.
Owner-run matching needs the same hardware, world seed, camera travel, resolution,
full-detail/vertical/LOD distances, warm/cold cache, and enabled water. Collect
CPU/GPU frame-time p50/p95/p99, coarse/fine culling counts, draw/descriptor counts,
VRAM and transparent-water pixels on both backends. An all-visible camera view
is necessary to expose the extra broadphase-test cost.


<!-- END PRESERVED SOURCE -->
