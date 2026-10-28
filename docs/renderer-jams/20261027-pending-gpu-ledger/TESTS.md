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
