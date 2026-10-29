# Expanded replica — docs/renderer-jams/20261027-transform-array-reuse/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-transform-array-reuse/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-transform-array-reuse/REVIEW.md |
| Preserved source SHA-256 | `e55c4cb4a4d77c2107d48be8a374df2d060c9daf13985656573cfe850e70ed45` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Caches four exact-length transform arrays with FIFO reuse, preserving synchronous uniform submission and clearing references on world reset. It introduces no GPU resource cache and validates array identity/reuse through focused assertions. Its 320,014 identity assertions are not an allocation-profiler or native GPU result.

## Research expansion — gaps and details to deepen

- Four retained slots did not bound retained element count at this historical pin; the subsequent retention-budget repair addresses that issue. The remaining integration question is whether the synchronous consumer copies data before the array may be reused.

## Research expansion — proposed contracts

- Link the retention-budget successor and specify array ownership through writeTransforms return, maximum retained elements, oversized bypass, and reset. Separate factory count reduction from measured heap allocation or GPU uniform memory.

## Research expansion — acceptance checks

- Retain exact-length, reuse, eviction, and clear tests; verify the actual uniform consumer lifetime and native frame behavior. Measure visible-size distribution, retained heap, GC, and CPU tails under alternating large shapes before changing cache policy.

## Research expansion — source anchors

- Deep Primal renderer — exact transform array reuse, scoped self-audit
- Implemented code

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal renderer — exact transform array reuse, scoped self-audit

**Pinned production commit:** [100e91f1](https://github.com/Shnewbs/Deep-Primal/commit/100e91f1b51c4821b5a65cfa45afe3512ef94e4e). Parent 2cefe064; initial main 826ba816; independent renderer lease 30b583cd. Pinned existing version: 0.0.1-alpha.29-dev.35-heightadmission.1; no version bump, new JAR or release. Read-only renderer-author self-audit of the finished code, NOT the independent engine audit.

## Implemented code

- lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java lines 15–45: capped four-slot reusable exact-length Java array cache. Linear primitive-size key lookups avoid boxed per-frame keys; misses allocate, oldest slot evicts, clear drops retained references. No GPU buffer or backend object is retained.
- lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java lines 80–100: persistent transform factory and cache, replacing allocation of a fresh transform array and every transform object on each changing visible part count. Lines 700–718 acquire exactly parts.size() and preserve dynamic uniform writes and camera offset updates. Lines 781–790 clear pool during world reset/logout/shutdown.
- docs/renderer-jams/20261027-transform-array-reuse/ReusableTransformArraysChecks.java lines 1–74: stand-alone fixture excluded from normal mod payload; compiled/tested with exact Git blob identities.
- Prior signed-distance renderer audit (docs/renderer-jams/20261027-signed-distance/REVIEW.md) was consumed. No worldgen, finite water, transparency, GPU pipeline, FSR1/NGX, settings, distance caps, native buffers, profile or V1–V7 geography were modified.

## Fresh evidence and exact identities

Host: isolated Linux, Eclipse Temurin 25.0.5+8. Production blobs: VoxelLodRenderer cd5fe020967aebd2f14993a6641f65fcb13ae682; ReusableTransformArrays 7ffb0a5fcde30a0501ed9e97290ed256b3f2671e; docs fixture 8a026503990603bd3483533b976455f73d2f1e6e. The two dependency-free files compiled locally with javac --release 25 -g; Java ran with -Xverify:all; local Git-blob hashes match the committed products. The full client renderer was source-inspected but not native-compiled.

**PASS 320,014 assertions:** REUSABLE_TRANSFORM_ARRAYS_PASS checks=320014 baselineTransformArrays=20000 candidateTransformArrays=2 allocationDelta=19998 gpuTimingClaim=false. First 100,000 four-count iterations retain array and element identity; the alternating-count fixture invokes the old single-pool allocation model 20,000 times but the new four-slot factory twice. Also checks exact lengths, FIFO eviction, clear/reset, null/wrong-size/negative input. This is a controlled Java factory-call count, NOT a measured GPU frame/FPS, JVM allocation profiler, native host, or driver memory result.

**Expected negative rejection:** private helper copy changed the hit check to an always-miss comparison. javac passed, java -Xverify:all exited 1 at AssertionError 'reused transform array identity'. Candidate-positive suite exited 0.

**NOT RUN:** complete Java25/Gradle 9.2.1/NeoForge compilation, whole CoreChecks, packaged native and JAR CRC/source/class identity, Windows AMD8060S/RTX, GPU uniform resource lifetimes, OpenGL/Vulkan pixel/readback and waterfall transparency, game save/reopen/world-switch/resize/shutdown, real CPU/GPU p50/p95/p99 and VRAM. Full builds belong to owner under October 27 policy. Thus no new JAR exists.

## Six equal renderer scores, conservative carry-forward

| Category | Previous | Current | Justification |
|---|---:|---:|---|
| Distance/LOD correctness and useful coverage | 4 | 4 | Prior signed-space fix retained; no fresh traveled native coverage. |
| Mesh generation, reuse and scheduling | 6 | 6 | Existing mesher unchanged; only Java submission transforms reused. |
| GPU culling/submission/memory | 4 | 4 | Cache in VoxelLodRenderer lines 80–100 and 700–718 with fixture; no actual GPU/heap/driver benchmarks. |
| Actual AMD FSR and NVIDIA DLSS | 5 | 5 | Existing FSR1 world integration; DLSS NGX temporal inputs/RTX execution still unverified. |
| Visual/backend/settings compatibility | 5 | 5 | Uniform call semantics preserved; no real Vulkan/OpenGL/water images. |
| Validation and measured performance | 4 | 4 | Exact source JDK25 component and mutation rejection; full native/hardware gates absent. |
| **Total / average** | **28/60; 4.67/10** | **28/60; 4.67/10** | **Change 0.00; not an independent whole-engine regrade.** |

Whole-engine last complete external grade: 50/100, unchanged. Renderer target strictly greater than 8.5 remains unmet.

## Open risks and next

Cached arrays retain up to four large draw-count shapes (bounded slots, not bounded bytes). Measure heap/residency versus fewer allocations on real AMD/Vulkan and OpenGL scenes and verify DynamicUniforms.writeTransforms consumes the exact provided array before next frame. Then profile cold/warm travel, upload/cull/submit/frame tails, water ordering and world switch; shrink/disable cache if retained memory outweighs gains. Continue bounded mesh uploads and temporal camera depth/motion/jitter prerequisites without misrepresenting DLSS or frames generated.


<!-- END PRESERVED SOURCE -->
