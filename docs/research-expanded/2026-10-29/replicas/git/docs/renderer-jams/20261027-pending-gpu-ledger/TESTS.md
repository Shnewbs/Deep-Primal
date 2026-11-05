# Expanded replica — docs/renderer-jams/20261027-pending-gpu-ledger/TESTS.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-pending-gpu-ledger/TESTS.md) |
| Source identity | git: docs/renderer-jams/20261027-pending-gpu-ledger/TESTS.md |
| Preserved source SHA-256 | `cc1358000c2f9c7f5cd041235c757b5a7ee2a62c797693d960f9ece24c14eb09` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Replaces repeated pending-buffer summation with an O(1) committed byte ledger. Append validates null, negative size, and overflow before mutation; bytes advance only after successful list insertion, relying on immutable accepted GPU buffer sizes.

## Research expansion — gaps and details to deepen

- The local ledger should connect to exactly-once ownership transfer through published resident and retired states. Correct pending totals alone do not prevent a replacement from being double-charged or a GPU-referenced resource from being declared free too early.

## Research expansion — proposed contracts

- Specify immutable allocation identity and byte size, transfer events, rollback, and completion-aware release. Maintain separate reserved, pending, resident, and retired totals with conservation checks across successful append, failed append, cache replacement, reset, and close faults.

## Research expansion — acceptance checks

- Retain the 418,317-assertion randomized/overflow coverage, then execute actual backend ownership transitions with partial failures. After completion, require each allocation to have exactly one accounting owner or a verified released state; component timing is not a frame result.

## Research expansion — source anchors

- Dedicated renderer jam — constant-time accepted GPU byte accounting
- Reproduce the exact focused tests (Java 21 compatible)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dedicated renderer jam — constant-time accepted GPU byte accounting

**Production code pin:** `598c860d57687b37b2c24f7f329569354dd4b113`. This is scoped to the LOD upload path and preserves the existing resident GPU cap, per-frame admission, mesh content, water materials and all engine/water systems. The total records successfully **accepted actual GPU bytes** rather than recomputing the sum on every frame.

## Reproduce the exact focused tests (Java 21 compatible)

From the repository root:

```bash
OUT="$(mktemp -d)"
javac --release 21 -Xlint:all -d "$OUT" \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/PendingGpuBytes.java \
  docs/renderer-jams/20261027-pending-gpu-ledger/PendingGpuBytesChecks.java
java -ea -Xverify:all -cp "$OUT" com.shnewbs.deepprimal.lod.client.PendingGpuBytesChecks
# PENDING_GPU_BYTES_PASS checks=418317 acceptedUploads=393216 randomizedPages=512 perPageParts=768
javac --release 21 -Xlint:all -d "$OUT" \
  lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/PendingGpuBytes.java \
  docs/renderer-jams/20261027-pending-gpu-ledger/PendingGpuBytesMicrobench.java
java -Xverify:all -cp "$OUT" com.shnewbs.deepprimal.lod.client.PendingGpuBytesMicrobench
```

Exact checked blobs: production helper `bd3a1f251133b23eb616f6f13d7865baafbf99c2`; checks `5a312cd78eb1fb8b9248dd4b86743a63e8efdded`; microbench `27715806fc6d60b27ce9c5a08350c0a9d6a6e0e7`. The modified renderer source blob is `79970c0491579b20efe4b05934d7b4535c5d9b7d`.

## Executed results (separate Linux workspace)

Java and javac **21.0.12.1**; owner-provided JDK25 archive was **not present** here; no local full private project checkout, Gradle cache, native Minecraft or GPU session was available. Real GitHub code commit and source blob readback succeeded through authorized GitHub access.

- Java check: **418,317 passed assertions**, 393,216 accepted parts across 512 randomized three-LOD-level page sequences; failed list append leaves byte total unchanged; signed-long negative/overflow and clean replacement covered.
- Mutant: changing `return committed;` to `return 0;` compiles, but fails the exact test with `AssertionError: accepted and zero sizes: 0 != 42` (expected rejection).
- CPU component-only ABBA-style observations (8,000 samples per mode, 30,000 warm-ups; not a game frame):
  - 512 parts: old traversal p50/p95/p99 **260/271/380 ns**; new total **60/61/71 ns**.
  - 4,096 parts: old traversal p50/p95/p99 **2,915/2,945/3,515 ns**; new total **30/30/31 ns**.
  - Same accepted-byte checksum per mode. JVM compilation/JIT and host noise influence nanoscale observations; these are not production GPU/upload/FPS speedup claims.
- Source integration static readback: `PendingGpu.bytes()` calls `accepted.bytes()`; the only successful `TerrainGpuMesh.upload` append passes `gpu.byteSize` to `accepted.append`; discard, budget and completed-entry paths still read `p.bytes()`.

**Not executed:** JDK25 compile, complete Gradle/module checks, native/game/window/GPU/backend/LOD-water scenes, packaged JAR CRCs, real GPU memory or measured frame time. The October 27 owner-build override explicitly puts full Gradle/Minecraft/GPU checks in the owner's hands; do not claim a new JAR or release.

## Next renderer-only gates

Run owner-side Vulkan then OpenGL real camera/travel/edit/water-LOD handoff captures with real GPU/CPU p50/p95/p99, mesh-size distributions, maximum partial pages, upload stalls and device resize/reload. If oversized indivisible GPU parts cause frame spikes, implement **bounded part splitting** with identical material/UV/lighting/transparent-water semantics and hard residency; do not raise full-detail distance solely by slider.


<!-- END PRESERVED SOURCE -->
