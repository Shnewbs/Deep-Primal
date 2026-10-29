# Renderer jam 32 — read-only self-audit of pinned source

**Jam ID:** `renderer-20261029T1151-projection-cache`. **Baseline main:** `df5ffad407e57b0eb3e338f363847e66427df2b3`. **Scoped claim:** [67cda582](https://github.com/Shnewbs/Deep-Primal/commit/67cda582f939e76921bc14a51f327c02d3af1c08). **Claim-parent source commit reviewed:** [85a48595](https://github.com/Shnewbs/Deep-Primal/commit/85a48595e0ab172c8779f4f2a95a92d822c08ce8). Version `0.0.1-alpha.29-dev.77-seam-contract.1` unchanged. This is an **internal renderer jam self-audit, not an independent engine audit**. Production changes were frozen before the read-only scored inspection. [Jam 31](../20261029-scene-pixel-lod/REVIEW.md) scored **5.74/10**.

## Renderer-only change and evidence

- [`LodProjectionSelectorCache.java` lines 1–34](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodProjectionSelectorCache.java#L1-L34), Git blob `1f4453540843e99ea8dd4180acbbb301e9d4abfb`: render-thread selector instance and trigonometric focal reused only when pixel height, raw FOV bits and raw error bits agree; reset clears it.
- [`VoxelLodRenderer.java` L82, L556–560, L1000](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L550-L562), Git blob `e4aa0fa88a9edb49c3048af96ee8fcecfc22ac5e`: uses cache in the existing per-extract error-selector path, resets on world switch, preserves scaled world-target/native fallback.
- [`LodProjectionCacheChecks.java` L1–80](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/docs/renderer-jams/20261029-projection-cache/tests/com/shnewbs/deepprimal/lod/client/LodProjectionCacheChecks.java#L1-L80), Git blob `74594cbbc06a92c24426b7e83a9ddb39a8f17797`: seeded native/FSR pixel domain, FOV/error, signed-zero, exceptional values and selector parity; production-excluded.
- Unchanged [`LodErrorProjection.java`](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodErrorProjection.java) blob `895c4beef16e9d251cf8fe1f0b200f35a280e782`; [`LodScreenErrorSelector.java`](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodScreenErrorSelector.java) blob `f550847a571bbf56b243b2bb2b77ecc740b98e8d`. No changed meshes, culling order, GPU draw calls, water blend, geometry, Vulkan/OpenGL module, upscaler providers, engine/water code, settings, or build output.
- Consumed [prior jam31 audit](../20261029-scene-pixel-lod/REVIEW.md) (correct scene-pixel projection, no native performance evidence), and [independent Dev77 audit](../../engine-hourly-audits/2026-10-29T1132-1f926907/REVIEW.md) (Java25 pixel-domain arithmetic accepted; full renderer compile/GPU not run).

## Reproducible tests and measured component results

Host: isolated Linux OpenJDK/javac **21.0.12.1**; exact committed helper/test and unchanged selector/projector verified by Git blob hashes. Commands for the three source helpers plus production-excluded test:

```sh
find src tests -name '*.java' | sort > sources.list
javac --release 21 -Xlint:all -Werror -d classes @sources.list
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodProjectionCacheChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodProjectionCacheChecks
```

Two runs **PASS 750,010 assertions each; 1,500,020 total**. A strict-compiled negative mutant omitting the target-height cache key failed **assertion 10,001** as expected. **11/11** read-only pinned integration checks passed (exact source/test/unchanged blob IDs, placement, world reset, FSR target read, translucent state, exactly three scoped changed paths). GitHub main updated using a guarded expected-parent ref; no force push.

The synthetic Java allocation/timing fixture used seven 100,000-operation paired samples in each of two runs, with a volatile reference sink to prevent baseline allocation elimination. It measured baseline **4.203 ms vs cached 0.222 ms p50** in run 1 and **4.978 ms vs 0.278 ms p50** in run 2, per 100k selector acquisitions. Total test-thread allocations per seven samples: baseline **44,800,000 bytes**, cached **448 bytes**, in both runs. **Not gameplay CPU/GPU frame time, not end-to-end FPS/VRAM, not proof of FSR/DLSS support**; microbench timing is JIT/host dependent and omits Minecraft render workload.

Evidence ZIP `DeepPrimal_Renderer_ProjectionSelectorCache_20261029_85a4859_SourceEvidence.zip`: 8,859 bytes, SHA256 `8ba0675a684625cb8c6c8f9f7680bf187368cf35f26b214a52abb8bb46122e8f`, 13 entries, CRC and SHA-256 manifest verified; Library `/Deep Primal/Renderer Jams/` upload succeeded. Archive is source/tests/CPU evidence, **not an executable JAR**.

## Six equal-weight renderer grades /10

| Category | Grade | Pinned basis / open gate |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.60 carried** | [Scene-target pixels preserved](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L545-L565); no new native distance coverage or handoff acceptance |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Existing source work](../20261028-split-preflight-once/REVIEW.md), unchanged; mesh rebuild, GPU upload and game p99 unmeasured |
| GPU culling/submission/memory | **4.55 carried** | [Indexed-draw path](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L863-L920) unchanged; no true GPU indirect draw or VRAM proof |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | [Actual FSR1 only](../../UPSCALING.md); temporal FSR and DLSS production dispatch unaccepted |
| Visual/backend/settings compatibility | **5.30 carried** | [World-target and reset cache key](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodProjectionSelectorCache.java#L12-L34) preserves selection; native Vulkan/GL/water pixel tests unrun |
| Validation and measured performance | **6.10 (+0.05)** | [Committed deterministic parity](https://github.com/Shnewbs/Deep-Primal/blob/85a48595e0ab172c8779f4f2a95a92d822c08ce8/docs/renderer-jams/20261029-projection-cache/tests/com/shnewbs/deepprimal/lod/client/LodProjectionCacheChecks.java), negative mutant, 11 source checks, paired selector-only allocation results; no host GPU FPS |
| **Equal-weight mean** | **5.75 (+0.01)** | 34.50/6 = 5.75 versus prior 5.74, **not** the whole-engine grade |

## Unrun gates, limitations and next repair

JDK 25 archive and directory are not mounted here. Local `java` and `javac` are 21.0.12.1; Git CLI DNS cannot resolve GitHub, but source/commits use connected GitHub. Owner authorization keeps **complete Java25/Gradle 9.2.1/NeoForge, JAR/native packaging, outer/nested CRC and GPU benchmarking** outside this jam. **No** Minecraft Vulkan/OpenGL or Windows AMD/NVIDIA render execution; no actual GPU pixels, transparent-water verification, reload/resize/save/reopen/world-switch/shutdown, p50/p95/p99 hardware frame timings, GPU memory, temporal AMD FSR3/4, RTX DLSS, or frame generation validation.

**Next:** owner-run native Vulkan/OpenGL same-scene p50/p95/p99 and LOD pixel/water checks with FSR1 quality/native toggles; then profile draw/descriptor/VRAM and optimize the actually measured submission bottleneck. This cache is a small per-extract CPU saving, not a replacement for that evidence. The independent engine grade remains at its separate carried 5.0/10.
