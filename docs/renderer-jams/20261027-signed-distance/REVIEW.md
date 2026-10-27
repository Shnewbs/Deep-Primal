# Renderer jam self-audit — 20261027-signed-distance

**Exact pushed source:** [52635aa9c774c61c20271102b7719d49985ef1e5](https://github.com/Shnewbs/Deep-Primal/commit/52635aa9c774c61c20271102b7719d49985ef1e5), product blobs `486be1cdb9f4ecfb85013379ec435cc48776a9ef` (VoxelLodRenderer) and `a40a0cd81cab1cddace4a17b6db24f5838351857` (LodChunkDistance). Rebased the independently staged [5717acaa](https://github.com/Shnewbs/Deep-Primal/commit/5717acaa7c356401e6396995089d36c12a77024b) onto fresh main through guarded nonforce updates; parent included the engine builder's completed dev35 checkout/lease release. No unrelated code/asset/state changed. This is a read-only **renderer-author self-audit**, not an independent engine review.

## Implemented and proved

Previously the persisted LOD key sort subtracted signed `int` coordinates before widening, so `Integer.MIN_VALUE - Integer.MAX_VALUE` became `1`, and some remote disk pages looked nearest. Disk read admission and camera culling used the same unsafe int `Math.abs` subtraction. This could admit/retain/render far persisted pages as if nearby at signed extremes. Newly added [LodChunkDistance](../../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodChunkDistance.java) promotes operands to `long` *before* subtraction. Squared disk-key priority uses double arithmetic to avoid overflow beyond signed-long square range.

Five call sites are guarded in [VoxelLodRenderer](../../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java): index sort line291, range check line418, camera LOD radius line493, native handoff line500 and visible span line520. The visible-span cast is safe only after the preceding radius check (maximum512). The sortable double is approximate at extremely large distances but finite, and practical world-border squared distances remain exactly represented. Render distance limits themselves are unchanged.

Fresh **Eclipse Temurin Java25.0.5+8**:
- `javac -g -d out LodChunkDistance.java checks/LodChunkDistanceChecks.java` exited0; `java -Xverify:all ... LodChunkDistanceChecks` **PASS**: `LOD_SIGNED_DISTANCE_PASS exactOracle=100008 fullIntRange=true finiteSquare=true nearRange=true negativeSeams=true`.
- An additional ordinary-world-border parity fixture uses 1,000,000 seeded input pairs in +/-1,875,000 chunk range and checks exact equality with historical behavior. **PASS**: `LOD_NORMAL_RANGE_PARITY_PASS comparisons=1000000 worldBorderChunks=1875000`. The only behavior difference is at potential overflow/edge ranges.
- A private negative control replaced long-before-subtract with old int-before-long; compilation succeeded but its fixture correctly **failed** at the opposite signed-int extremes. Test-only negative-control source is outside production.
- Git blob SHA1 hashes of compiled helper and fixture match the frozen pushed Git blobs `a40a0cd8` and `8d95e911`. Exact production code was fetched back and call-site markers verified after commit; no product code edited during scoring. An initial stricter floating oracle failed on normal intermediate rounding (one-time double conversion vs two arithmetic rounds); the corrected fixture uses 2 ulps and the final full check was rerun.

No changed GPU resource, culling frustum, tessellation, LOD geometry/material/water ordering, shader, Vulkan/OpenGL, FSR1/NGX, saved geography/art/edits, worldgen, water chemistry/quantities, settings, version cap or network contract. This is correctness, **not measured FPS gain or expanded render distance**.

## Six equally weighted renderer scores

| Category | Previous /10 | Current /10 | Evidence and limitations |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | 4 | New signed disk/camera admission [LodChunkDistance](../../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodChunkDistance.java) and one million parity cases; full native distance/travel, actual client visibility and unknown-world coverage unrun |
| Mesh generation/reuse/scheduling | 6 | 6 | [LodMesher](../../../../engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java) unchanged; no new GPU/CPU meshing speedup measured |
| GPU culling/submission/memory | 4 | 4 | Safe camera coarse key admission [VoxelLodRenderer L493](../../../../lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java); no real driver memory/draw/culling throughput benchmark |
| FSR/DLSS | 5 | 5 | Existing FSR1 GPU EASU/RCAS carries prior evidence [upscaling](../../UPSCALING.md); NVIDIA NGX remains unavailable in-game without full temporal inputs, dispatch and RTX acceptance |
| Visual/backend/settings compatibility | 5 | 5 | Existing module/configs unaffected; no fresh Vulkan/OpenGL scene pixels, AMD or Windows client runs |
| Validation and measured performance | 4 | 4 | Exact pinned helper/fixture, 100008 exhaustive seeded oracle cases plus one-million normal-range parity and expected negative rejection; no full Gradle/NeoForge or named-hardware p50/p95/p99 |
| **Average** | **4.67** | **4.67** | **28/60, Δ0.00 — conservative carry-forward, not a full renderer regrade** |

Whole-engine **50/100 (5.0/10)** is the last accepted external grade, unchanged. The target strictly above 8.5 is not met.

## Gates left for the owner's builds

The **owner will perform full builds** under the October27 override. **Not run:** Java25 full Gradle/NeoForge root+module compilation and qualification, final JAR and nested CRC/identity/native payload/fixture exclusion, native Minecraft save/reopen/shutdown and actual-world camera travel/culling, GPU Vulkan/OpenGL pixels, Windows AMD8060S, RTX DLSS, FSR reset/reload and p50/p95/p99 CPU/GPU frame benchmarks. **New JARs/releases: zero.** No older binary was relabeled.

Next: owner-run exact-SHA build and client world-border/signed LOD admission tests; resume measured GPU upload/culling/LOD streaming and preserve the independent engine/water workstream. The renderer scope is checkpointed and writer lease released.
