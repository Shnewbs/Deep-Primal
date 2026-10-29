# Expanded replica — docs/renderer-jams/20261029-visible-snapshot-reuse/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-visible-snapshot-reuse/README.md) |
| Source identity | git: docs/renderer-jams/20261029-visible-snapshot-reuse/README.md |
| Preserved source SHA-256 | `d74d04baf7c30ac4e4cdd8860398f4cad11ded405b0fe0081a198c0f5fa4a06a` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Introduces a render-thread reusable visibility collector while preserving detached immutable List.copyOf publication and ordered duplicate entries. Finally cleanup clears references, reset drops backing storage, and frames exceeding 32,768 records discard the collector. The README records 69,984 focused Java21 assertions and a synthetic thread-allocation comparison.

## Research expansion — gaps and details to deepen

- The outlier record threshold is a retention policy, not an exact retained-byte measurement. Native extraction/publication ownership, immutable snapshot lifetime, collector capacity growth, and alternating large-frame allocation behavior remain qualification questions rather than already-proven client improvements.

## Research expansion — proposed contracts

- Define the collector as exclusively borrowed during extraction, with no nested use or published mutable aliases. Track collected count, retained capacity, discarded outliers, immutable-copy allocation, and world generation separately; preserve original publication failures when cleanup or diagnostics fail.

## Research expansion — acceptance checks

- Exercise actual extraction and render-state failure, retained earlier snapshots, threshold and oversized frames, and world reset. Measure native client allocation, retained heap/GC and complete-frame tails with unchanged visible content, plus Vulkan/OpenGL water images; the 45,435,432-byte fixture reduction is not FPS or VRAM evidence.

## Research expansion — source anchors

- Renderer jam 23 — reusable visibility extraction scratch
- Outlier retention bound: a collected frame exceeding 32,768 parts clears and

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 23 — reusable visibility extraction scratch

Jam ID: `renderer-20261029T0300-visible-snapshot-reuse`. Owner builds full Minecraft/Gradle/GPU.

The frame extraction path formerly created an ArrayList for every LOD-visible frame
and then an immutable List.copyOf for rendering. A single renderer-owned transient
array collector is now reused; List.copyOf remains the only published data and
finally always clears the collector. World reset drops the retained backing array.
No changes to culling, selection, mesh data, draw ordering, water transparency,
GPU API or simulation.

Focused source-excluded check (system Java21, tested before commit):

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  LodVisibleSnapshot.java LodVisibleSnapshotChecks.java
java -ea -Xverify:all -cp classes \
  com.shnewbs.deepprimal.lod.client.LodVisibleSnapshotChecks
```

10,000 randomized frames (0–512 visible items, duplicates, stable detached
immutable snapshots) plus null rejection, nested extraction, failed publication,
reference clearing and reset. The paired warmed 10,000-frame/256-item Java21
ThreadMXBean allocation probe saw old 66,480,000 B vs reusable 21,044,568 B,
a 45,435,432 B reduction in this isolated fixture (not a client frame-time,
GPU VRAM, FPS or native allocation measurement).

Open: owner Java25 Gradle/NeoForge, native Vulkan/OpenGL and transparent-water
pixels, same-scene p50/p95/p99 CPU/GPU frame, VRAM and allocation profiles,
resize/save/reopen/reload/shutdown, genuine temporal FSR/DLSS integration.

Outlier retention bound: a collected frame exceeding 32,768 parts clears and
releases the reusable backing array; the next frame starts with a new collector.
Focused regression now passes 69,984 assertions per strict Java21 run.


<!-- END PRESERVED SOURCE -->
