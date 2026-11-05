# Read-only renderer jam self-audit — 20261027-pending-accounting-integration

**Reviewed source:** [b53d66c0c326de8f5259d42a49598cdd79004d92](https://github.com/Shnewbs/Deep-Primal/commit/b53d66c0c326de8f5259d42a49598cdd79004d92), exact final code blob e035637f91b1800d06b0c47c7676f35db8ddc118. Audit after production edit and focused JDK25 tests; no code changes during scoring. This is the author's renderer-only self-audit, **not an external engine regrade**.

| Equal-weight renderer category | Previous | Current | Pinned evidence and exclusions |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | 4 | [DeliveredChunkScan retarget](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L30-L45) unchanged; native camera/travel/unexplored terrain not tested |
| Mesh generation/reuse/scheduling | 6 | 6 | [LodMesher](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L1-L65) unchanged; no new mesh output or build/worker timing |
| GPU culling/submission/memory | 4 | 4 | [PendingGpu.bytes](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L86-L106) replaces allocating stream with equivalent indexing; source prefix/suffix exact, [admission points](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L604-L667) unchanged; no GPU residency, driver readback or p99 proof |
| Actual FSR/DLSS | 5 | 5 | Inherited FSR1 EASU/RCAS prior [dev32 evidence](https://github.com/Shnewbs/Deep-Primal/blob/b53d66c0c326de8f5259d42a49598cdd79004d92/docs/validation/upscaling-2026-10-27/README.md), **not rerun**; optional DLSS NGX cannot run in game until temporal inputs/dispatch/RTX hardware, not credited as completed DLSS |
| Visual/backend/settings compatibility | 5 | 5 | No shader/mesh/transparency/control changes; no fresh native Vulkan/GL resize/reload, AMD/RTX acceptance |
| Validation and measured performance | 4 | 4 | JDK25 standalone 10,063 exact-algorithm equivalence cases, eight 200k-call ABBA-like accounting microprobes, matched checksums/allocated bytes. **Not actual loaded production class**; no full Gradle/root/full module or named-GPU benchmark |
| **Mean** | **4.67/10** | **4.67/10** | **Δ 0.00** (28/60); conservative until qualified package/native measurements |

The narrow CPU microprobe is not an FPS improvement claim. Signed-long summing, null/ordering and element membership are not changed; private three-level collections are only mutated on the owning client event flow. The JDK25 component probe is not the separate existing 10,063 real-class [staged admission-gate work](https://github.com/Shnewbs/Deep-Primal/tree/b53d66c0c326de8f5259d42a49598cdd79004d92/docs/renderer-jams/20261027-admission-gate), which stays staged until final source/native qualification. No one has independently validated Windows execution. No Java25 **full** build, build artifact CRC/identity, packaged lifecycle, OpenGL/Vulkan pixels, graphics driver, AMD8060S, RTX/DLSS or upload-delay tail numbers were run.

**Prior independent whole-engine grade remains 50/100 = 5.0/10, Tier 2/5, carried forward.** The renderer grade 4.67/10 is not that overall grade and is far below the >8.5 targeted renderer acceptance. New JARs/releases: **0**.

Next: full checkout/toolchain-dependent packaging first, then real-client compared latency/memory and staged admission test integration.
