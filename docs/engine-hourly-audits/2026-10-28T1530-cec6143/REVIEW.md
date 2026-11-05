# Independent Deep Primal audit — 2026-10-28 15:30 UTC

Pinned committed source: [cec61432aa1cb0d4f1909a33d537bd03ca87e31d](https://github.com/Shnewbs/Deep-Primal/commit/cec61432aa1cb0d4f1909a33d537bd03ca87e31d); version `0.0.1-alpha.29-dev.57-surface-policy.1`. **Not a fresh whole-engine regrade.** Read-only with respect to Builder/renderer checkout, leases, production code and caches. October 27 owner override assigns full Gradle/native/GPU/JAR builds to owner.

## New independent evidence
- Exact production Git blobs reconstructed and verified: `LodEvictionHeap.java` `7fff32140b922b065bc3ee60f4ad2c6573399ca3`; `V8SurfacePolicy.java` `9aceaaa11f486d8861eb8fb20bbc5a944030bf60`; `EcotoneSurfaceField.java` `a171ac13e36db1f076ba4ac820c3e1d2ce11ec0e`.
- **Java21 PASS:** 3,537,315 independent comparator/eviction checks over 1,800 scenarios against stable farthest-first oracle (4096 pages; exact ties, signed extremes, keep-last, resets, overcapacity recovery). Deliberately reversed comparator **fails** check 29.
- **Java21 PASS:** 14,342 independent frozen-surface-policy assertions with production surface policy and palette class, a clearly isolated test-only `EcotoneBlend` provider and Mojang Codec stand-ins. 2,048 valid roundtrips, 12,288 rejected malformed field/revision/fingerprint/climate cases; frozen 40-biome fingerprint `684e3ee170e01a32`. These are not real Mojang JsonOps/NbtOps or native Minecraft save/reopen.
- **Static integration PASS:** `VoxelLodRenderer.java` [L568–599](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L568-L599) uses reusable capacity 4096; before admitting one page, `evictToFit(...,4095,...)` at L475; the sole cache insertion at L698. No live GPU stress acceptance.
- Producer's renderer receipt `docs/renderer-jams/20261028-eviction-reuse/RECEIPT.json` reports 640 controlled 4096-page benchmark samples and heap allocation improvement; auditor **did not independently rebenchmark** due potential timing contention.
- New Dev57 [V8SurfacePolicy.java](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/src/main/java/com/shnewbs/deepprimal/engine/V8SurfacePolicy.java) + [V8GeneratorSnapshot.java](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L18-L58) pin saved rev3 palette/climate provenance and preserve revisions 1/2; native placement, geography, hydrology and existing saves unchanged. Pure source/stand-in credit, **no shipped terrain feature grade gain**.

## Ten equal categories — last complete 50/100 carried (Tier 2)
| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biomes | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Total** | **50/100 = 5.0/10** |

This is **not** a fresh complete regrade. The strictly greater than 8.5/10 stopping threshold is unmet; the independent auditor must remain enabled.

**Not run:** Java25 (JDK archive absent), full Gradle/NeoForge (no complete checkout/dependency cache and `git ls-remote` DNS failure), current JAR packaging/CRC/compiled-class identity, native V8 profile and finite-water save/reopen/shutdown, Windows Vulkan/OpenGL FSR3/4, GPU/VRAM p95/p99 and actual Windows/AMD8060S test. No JAR was created, published, overwritten, or removed. The owner observed 300+FPS Creative flight at 125-chunk setting with Vulkan, not a controlled benchmark.

**Next:** real Mojang rev3 codec/native geometry/save replay; finite-water salt/sediment unload/edit/reopen conservation; owner-hardware Vulkan/OpenGL water/LOD/FSR GPU/frame-time acceptance.

Reproducible logs, test harnesses and full report retained in Library: `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1530_cec6143_*`. ZIP 21,161 bytes, CRC pass, SHA256 `53be30386c2269f3cef10dc6d98b6b65f02f6444bbb3b6daa1eb8ec7826d9de0`.
