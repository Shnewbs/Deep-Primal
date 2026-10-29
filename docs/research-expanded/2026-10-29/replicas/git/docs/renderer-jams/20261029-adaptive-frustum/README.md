# Expanded replica — docs/renderer-jams/20261029-adaptive-frustum/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-adaptive-frustum/README.md) |
| Source identity | git: docs/renderer-jams/20261029-adaptive-frustum/README.md |
| Preserved source SHA-256 | `89b4a9c7bf39d7c06268ceec952a5da5398920ef3f827484a2778b33c575b816` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Defines an opt-in adaptive coarse-page gate: after a successful sample of at least 32 eligible pages avoids no more fine tests than coarse tests, bypass three extractions, then sample again. Ordered fine-part culling remains active, failures do not train the policy, and the coarse-off override wins.

## Research expansion — gaps and details to deepen

- AABB-test counts are a cost proxy, not measured CPU or GPU time. The usage guide needs an explicit policy-generation boundary when scene resolution, projection, resource state, or later pose-aware behavior changes the workload being sampled.

## Research expansion — proposed contracts

- Add a developer experiment manifest recording fixed/adaptive/off state, completed-sample identity, eligible-page count, avoided checks, bypass age, and policy reset reason. Link the later pose and angle successors while keeping adaptive default-off and separating these startup flags from saved graphics preferences.

## Research expansion — acceptance checks

- Run the documented three-way restart-based comparison with identical all-visible, mostly-offscreen, and moving-camera scenes. Include failed extraction and world reset, verify ordered fine-path parity, and record CPU/GPU tails and water pixels; retain mixed adverse tails rather than promoting the small synthetic median gain.

## Research expansion — source anchors

- Renderer jam 27 — experimental adaptive page-frustum sampling
- Developer comparisons (restart client between runs)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 27 — experimental adaptive page-frustum sampling

Scope: CPU-only LOD visibility selection. No geometry, water, pipeline, Vulkan, OpenGL, resource ownership or FSR/DLSS changes. Default behavior remains the jam-26 fixed coarse broadphase pending native evidence.

## Developer comparisons (restart client between runs)

- Default: page-frustum broadphase enabled; adaptive sampling **disabled**.
- `-Ddeepprimal.lod.pageFrustumAutoSkip=true`: opt-in adaptive mode. After a fully completed extraction samples at least 32 eligible multi-part pages, if page-level tests avoided no more fine AABB tests than the number of coarse AABB tests, it bypasses the next 3 extractions and samples again. It always performs the original ordered per-part culling during bypass; world reset clears history.
- `-Ddeepprimal.lod.pageFrustumAutoSkip=false`: force the unchanged full coarse path.
- `-Ddeepprimal.lod.pageFrustumBroadphase=false`: disable the coarse path entirely, irrespective of adaptive mode.
- `-Ddeepprimal.lod.telemetry=true`: report coarse attempted/visible/rejected, avoided fine tests, sampled-last-extract and adaptive bypassed pages. Counts are from completed extracts; zero GPU occlusion/inference claims.

The heuristic uses counts of AABB tests, **not actual GPU or frame performance**. A camera turn while bypassing remains visually correct because fine tests always run, though the benefit of coarse rejection may be deferred by at most three completed extractions. A failed extract cannot train the heuristic. An opt-in flag is necessary because paired isolated component CPU gains are small and p95/p99 outcomes are mixed.

## Exact source-focused checks

From the repository root, with Java21 or a verified JDK25 and isolated classes:

```sh
ROOT=docs/renderer-jams/20261029-adaptive-frustum
mkdir -p /tmp/deepprimal-renderer-jam27-classes
javac --release 21 -Xlint:all -Werror -d /tmp/deepprimal-renderer-jam27-classes \
  docs/renderer-jams/20261029-page-frustum-broadphase/stubs/net/minecraft/world/phys/AABB.java \
  docs/renderer-jams/20261029-page-frustum-broadphase/stubs/com/shnewbs/deepprimal/graphics/TerrainGpuMesh.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveGate.java \
  "$ROOT/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumAdaptiveChecks.java"
for value in false true; do
  java -ea -Xverify:all "-Ddeepprimal.lod.pageFrustumAutoSkip=$value" \
    -cp /tmp/deepprimal-renderer-jam27-classes \
    com.shnewbs.deepprimal.lod.client.LodPageFrustumAdaptiveChecks
done
```

Use exactly the same seed, camera, FOV, warm/cold cache, full-detail/vertical/LOD distance and water for native fixed-coarse/adaptive/no-coarse comparisons on both Vulkan and OpenGL. Record CPU/GPU frame p50/p95/p99, draw counts, VRAM, culling ratios and transparent-water pixels. No JAR or qualified GPU acceptance is claimed here.


<!-- END PRESERVED SOURCE -->
