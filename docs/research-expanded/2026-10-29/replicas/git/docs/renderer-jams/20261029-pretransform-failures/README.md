# Expanded replica — docs/renderer-jams/20261029-pretransform-failures/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-pretransform-failures/README.md) |
| Source identity | git: docs/renderer-jams/20261029-pretransform-failures/README.md |
| Preserved source SHA-256 | `9f2344e9e99670aa0c7cc9e4760519be493627fc17c3b86f5468ebef4febc6e6` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends opt-in submission telemetry to target/camera, transform reservation, offset, and uniform-write failures before draw recording. These become preparation-only failed observations with zero returned draw work, while existing draw-stage errors remain on their single finally path.

## Research expansion — gaps and details to deepen

- The new coverage still excludes no-visible-LOD frames and culling, and diagnostic failure during unwinding can obscure the original error. The probe should join a complete frame population without converting unobserved work into zero duration.

## Research expansion — proposed contracts

- Add nonthrowing or guarded error-path observation, original-exception preservation, explicit skipped/empty outcomes, and frame/epoch correlation. Keep telemetry-off clock avoidance and prevent duplicate observations across preparation and draw-finally paths.

## Research expansion — acceptance checks

- Retain 30,000 mixed cases and exact-source boundary checks, then inject real preparation, recording, close, and diagnostic failures. Verify one observation per relevant pass, original error identity, and complete-frame/GPU correlation on the target artifact.

## Research expansion — source anchors

- Renderer jam 22 — failed LOD pre-draw CPU telemetry

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 22 — failed LOD pre-draw CPU telemetry

LOD submission histograms previously ignored target/camera acquisition,
transform buffer reservation and uniform-writing exceptions. These failures
now enter the opt-in CPU timing history as preparation-only failed passes.
No draw calls or records are credited, and the original exception is rethrown.
Draw-pass errors continue on their existing finally path without duplication.

The new lodSubmissionPreDrawFailures diagnostic distinguishes pre-draw failures
from errors during command recording. Only when the system property
deepprimal.lod.telemetry=true is set does the catch handler read CPU clocks.

Isolated Java21 source regression (not native Minecraft or GPU validation):

    find src -name '*.java' | sort > sources.txt
    javac --release 21 -Xlint:all -Werror -d classes @sources.txt
    java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPretransformFailureChecks

Tests cover 30,000 mixed successful, pre-draw failure, and partial API
submission cases, precise rolling percentiles, counters and world reset.
Source-only integration checks verify the renderer call boundary and telemetry
opt-in. No geometry, water surface, shader, LOD distance, FSR/DLSS, Vulkan
backend, terrain, or unrelated workstream changed.

Java25 Gradle, native Vulkan/OpenGL, water pixels, VRAM/FPS and packaged
Minecraft client/JAR are owner-operated and explicitly unrun in this jam.


<!-- END PRESERVED SOURCE -->
