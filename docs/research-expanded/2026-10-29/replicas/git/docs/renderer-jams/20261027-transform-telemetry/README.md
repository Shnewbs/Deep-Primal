# Expanded replica — docs/renderer-jams/20261027-transform-telemetry/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-transform-telemetry/README.md) |
| Source identity | git: docs/renderer-jams/20261027-transform-telemetry/README.md |
| Preserved source SHA-256 | `3b9d1a974b55cf9448a5806212d501527cdd52d518d521fb90b8ba061ca8f039` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds transform factory/cache-hit/oversized/eviction counters, five successful-request size buckets, and current/peak retained-element gauges. Snapshots are materialized on demand and published through the existing diagnostics; reset and factory-failure semantics are preserved.

## Research expansion — gaps and details to deepen

- The metrics count successful requests and factories, not heap bytes, GPU allocations, or hardware draws. Their snapshot needs frame/session context and a denominator for invalid or failed requests so dashboard comparisons remain meaningful.

## Research expansion — proposed contracts

- Add typed units, observation interval, world generation, successful and rejected request counts, and optional measured heap correlation. Keep the five fixed buckets and allocation-free hot-path intent while exposing histogram overflow and saturation clearly.

## Research expansion — acceptance checks

- Verify bucket boundaries, cache hits, oversized bypass, invalid factories, saturation, and reset. Collect target-native visible-size histograms with telemetry-on/off overhead and actual heap/GC/frame measurements before retuning the 32,768-element limit.

## Research expansion — source anchors

- Renderer jam 20261027-transform-telemetry — source/validation checkpoint
- What changed

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261027-transform-telemetry — source/validation checkpoint

Dedicated rendering workstream; independent engine builder, auditor, water, and Vulkan modules untouched.

## What changed

- `ReusableTransformArrays` collects allocation/cache-hit/oversized-bypass/eviction counts and a fixed five-bucket **successful visible transform-array draw-count** histogram. It also reports current and peak retained Java element counts, last draw count, and observed peak. No per-request collection/map allocation; the immutable snapshot is materialized on demand.
- `VoxelLodRenderer.transformStats()` provides the cache snapshot without touching worldgen/water, and `LodClient.diagnostics()` publishes named counters through the existing local diagnostic/dashboard map (sampled on the client frame thread roughly once per second).
- All counts reset with the cache on world detach/session reset. Invalid array factories still fail before retention changes and before a successful-request count is recorded.
- The existing four slots and 32,768-element cache ceiling, exact draw count, GPU uniform submission, FSR1/DLSS availability, LOD mesh/culling, transparency and saved geography are unchanged.

## Exact source identities

- `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java`: blob `e84f12bdcf4dffc17421faa8fd34f24bb30226bd`
- `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java`: blob `8cec5fbee84d2f82611544439d6346c9509d3267`
- `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java`: blob `8f2d2525f98a416213cacb5f9f8ef922e7a8351d`
- `TransformTelemetryChecks.java` (not a production class): blob `96c98afd482e3ab3367ff207eabb1d0c09c5ca52`

## Focused validation actually executed (Linux, JDK 21.0.12.1)

The owner-provided Temurin JDK25 path/archive does **not** exist in this execution container; system `java` and `javac` report 21.0.12.1. DNS lookup for GitHub from the container failed. GitHub source/commits use the connected GitHub API and guarded ref updates. The Java helper and test fixture Git blobs exactly match locally compiled files.

```sh
javac --release 21 -d classes ReusableTransformArrays.java TransformTelemetryChecks.java TransformRetentionBudgetChecks.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.TransformTelemetryChecks
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.TransformRetentionBudgetChecks
```

- `TRANSFORM_TELEMETRY_PASS checks=724271 randomized=120000 buckets=5 retentionBudget=31 factoryCalls=2 warmFrames=10000 measuredGpuFps=false`.
- Prior bounded-cache regression: `TRANSFORM_RETENTION_BUDGET_PASS checks=625529 trackedElementLimit=31 oracleOperations=150000 warmBaselineArrays=20000 warmCandidateArrays=2 gpuOrHeapTimingClaim=false`.
- Private mutation omitting successful-hit accounting: compilation succeeds, fixture exits 1 at `AssertionError: hit/miss/oversized counters drift 39` (expected).
- Histogram buckets: [0,512], [513,2048], [2049,8192], [8193,32768], [32769,+infinity]. Counters are **requests and factories**, not heap bytes, GPU allocation, GPU draw counts or time. The 32,768 retained elements are a proxy, not a measured memory byte ceiling.

## Not run

Full Java25/Gradle/NeoForge/client compilation, Windows build, JVM object-allocation profiler, actual cold/warm travel camera/edit measurements, Vulkan/OpenGL images, water transparency and resource reload/world-switch/save/reopen/shutdown, GPU frame p50/p95/p99 and AMD/NVIDIA hardware timings, JAR/package CRC and native class identity. Owner explicitly handles full builds until further notice. No fresh JAR/release is claimed.

## Next

Run a fixed scene/camera/resolution matrix on the owner's AMD Vulkan/OpenGL machine, collect the new real visible-count bins/factory calls/retention alongside p50/p95/p99 CPU/GPU/frame tails, and retune the 32,768-element cache only if the results justify it. Then validate bounded upload staging and temporal projection/motion/depth prerequisites for RTX DLSS. Existing upscaling is not relabeled as DLSS.


<!-- END PRESERVED SOURCE -->
