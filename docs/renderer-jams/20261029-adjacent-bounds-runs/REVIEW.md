# Renderer jam 35 — adjacent fine-part AABB verdict reuse (read-only author self-audit)

**Jam ID:** `renderer-20261029T1505-adjacent-bounds-runs`. **Initial main:** `4a7d40da930962c99bacbccd2a4e4631370dbce3`; **scoped claim:** [`87db3950f8a15b6b41d603735f2905f19cb6593c`](https://github.com/Shnewbs/Deep-Primal/commit/87db3950f8a15b6b41d603735f2905f19cb6593c); **first implementation:** [`840fb18a9c87cb14a15c5846283423dd4d4c831f`](https://github.com/Shnewbs/Deep-Primal/commit/840fb18a9c87cb14a15c5846283423dd4d4c831f); **final exact reviewed implementation + source-only test:** [`7241700af3eaf4da30b8787c07b3c42e08abd25b`](https://github.com/Shnewbs/Deep-Primal/commit/7241700af3eaf4da30b8787c07b3c42e08abd25b); version (unchanged engine-owned `gradle.properties`) `0.0.1-alpha.29-dev.81-route-dag.1`.

This is a **read-only renderer author's self-audit** of the final immutable candidate, **not** the independent whole-engine audit, and not GPU/native acceptance. Initial per-frame six-coordinate adjacency checks caused a synthetic slowdown; the author repaired this *before* the reviewed final candidate by precomputing compact bitmasks at GPU page publication. No production code was edited during this scored review.

## Prior audit consumed and exact source increment

The [renderer jam34 self-audit](../20261029-uniform-page-visibility/REVIEW.md) improved positive, fully uniform page visibility but left native fixed/adaptive/off GPU performance, water pixels and mixed-bound CPU overhead open. The latest [independent Dev80 audit](../../engine-hourly-audits/2026-10-29T0735-f71c48ad/REVIEW.md) concerns a separate engine DAG and does not establish renderer GPU acceptance; its findings and engine-only lease were left untouched.

- [New helper `LodPartFrustumRuns.java` L18–55](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPartFrustumRuns.java#L18-L55), Git blob `ef11507332a2ffec58da2dd8803915603b0744ed`: one-pass detection of **adjacent equal finite ordered AABBs** at page commit, encoded as bounded `long[]` bits for each detail. The first part and nonmatching, malformed, NaN or infinite bounds always require an actual fine test. Empty run maps retain a shared empty array. Bit addressing covers boundaries between 64 entries.
- [`VoxelLodRenderer.java` L606–652](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L606-L652): reuse only a previous fine-frustum result when its prevalidated adjacent bit is set; preserve original per-part order. Coarse union rejection and full-page uniform positive reuse remain intact. The explicit broadphase-off mode retains its original fine-test-per-part A/B baseline. The same renderer [precomputes masks at accepted page upload L845–860](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L845-L860), outside the per-camera hot path; no geometry, Vulkan/OpenGL shader, GPU mesh allocation, draw submission or water transparency changes.
- [Render-state publication L664–676](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L664-L676) now precedes updating the optional adaptive coarse-frustum heuristic, preventing it from learning from a failed publication. No adjustment to the heuristic's coarse-only savings formula.
- [`LodClient.java` L105–121](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L105-L121) reports positive/negative *fine-part checks avoided* separately, not GPU queries or FPS. Counters [reset L1012–1021](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L1012-L1021). All renderer changes stay in three `lod-engine` files; new production-excluded [smoke test](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/docs/renderer-jams/20261029-adjacent-bounds-runs/tests/com/shnewbs/deepprimal/lod/client/LodAdjacentRunSmoke.java) and this jam's docs are the only additional files. No terrain, finite-water, graphics-settings, Vulkan adapter, Gradle config, authored textures, engine or other scheduler altered.

**Explicit contract:** resident-page part AABB coordinates must remain unchanged after publication for the immutable mask to remain valid. Production assigns `gpu.bounds` during upload before cache publication, but `TerrainGpuMesh.bounds` is not declared `final`; real reload/world-switch/mutation testing is still necessary. A negative control mutating the adjacency bit address failed the committed smoke at assertion 6, as expected.

## Exact tests and observed metrics

Isolated **Linux x86_64 OpenJDK/javac 21.0.12.1** using production-exact helper blob `ef115...`, production-exact smoke blob `ae5e440e053500bdc6f93a0c9587a1f1f7750783` and **test-only** Minecraft AABB/TerrainGpuMesh doubles. The source of the full Minecraft mod was not compiled.

```sh
find src stubs tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d out @sources.txt
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodAdjacentRunSmoke
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodPartFrustumRunsChecks
```

**PASS** strict compiler, two smoke runs (40,015 assertions each), two extended runs (90,021 each): **260,072 passing assertions**. Randomized mixed/uniform/unique/invalid boxes, ordered visible-index equivalence, null/nonfinite/reversed bounds and 64-bit mask boundaries exercised. An intentionally wrong mask-index variant compiled but failed at smoke check 6. Separate read-only exact GitHub candidate check: **25/25** expected source integrations and blob identities matched. No full Java25/Gradle/native verification was executed.

**Paired isolated synthetic Java CPU fixture**, each observation = 256 page selections, 151 alternating before/after sample pairs per run; 64-part mixed page with eight adjacent groups:

| Run | p50 before → after | p95 before → after | p99 before → after |
| --- | --- | --- | --- |
| 1 | 42,754 → 25,088 ns | 100,399 → 76,193 ns | 218,985 → 181,399 ns |
| 2 | 37,145 → 19,388 ns | 115,481 → 59,709 ns | 249,962 → 129,182 ns |

All-unique controls p50 were effectively unchanged: 26,209 → 26,239 ns and 24,065 → 24,026 ns. The all-unique p99 was inconsistent (run 2 136,593 → 230,452 ns), so **no universal tail-latency, actual Minecraft FPS, GPU throughput or hardware improvement claim**. The original pre-bitmask approach measured slower and was superseded before audit.

## Six equally weighted renderer grades /10

| Renderer category | Score | Pinned evidence and missing gate |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.70** | +0.05 for exact fine-part visibility parity including signed/malformed/mixed bounds, [helper L18–55](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPartFrustumRuns.java#L18-L55), [render selection L606–652](https://github.com/Shnewbs/Deep-Primal/blob/7241700af3eaf4da30b8787c07b3c42e08abd25b/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L606-L652); **no expanded/renderable distance or native coverage**. |
| Mesh generation/reuse/scheduling | **6.95** | **Carried unchanged** from jam34; no mesher/worker rebuild or allocation proof this jam. |
| GPU culling/submission/memory | **4.55** | **Carried unchanged**; this is CPU frustum predicate reuse, not GPU occlusion, indirect drawing or measured VRAM. Draw API path unchanged. |
| Actual FSR/DLSS implementation | **5.00** | **Carried unchanged**: existing FSR1 spatial only; no activated FSR temporal or hardware NVIDIA DLSS super-resolution evaluate this jam. |
| Visual/backend/settings compatibility | **5.30** | **Carried unchanged**. Order preserved in source; Vulkan/OpenGL, water transparency pixels, FSR1 resize/reload not checked natively. |
| Validation and measured performance | **6.25** | +0.05 for strict 260,072 Java21 assertions, negative control, 25/25 pinned checks and paired *CPU-only* p50; all-unique p99 unstable and no GPU data. |
| **Arithmetic mean** | **5.79/10** | `(6.70+6.95+4.55+5.00+5.30+6.25)/6=5.7917`; displayed **+0.01** vs jam34 **5.78**. Target above 8.5 remains unmet. |

The independent whole-engine **5.0/10** remains carried as historical, neither regraded nor modified by this renderer jam.

## Delivery and remaining acceptance

The verified evidence file is `DeepPrimal_Renderer_AdjacentBoundsRuns_20261029_7241700_SourceEvidence.zip`, **12,340 bytes**, **SHA-256 `283e155b0668a5dddd3e804e449e9748d05b42ffbd486c81b38bda78856a62be`**, **15 ZIP entries**, CRC and per-member SHA-256 verified. Successfully uploaded to Library `/Deep Primal/Renderer Jams/DeepPrimal_Renderer_AdjacentBoundsRuns_20261029_7241700_SourceEvidence.zip`. It contains source-exact helper/smoke, test doubles, extended test, benchmarks, logs, negative mutant, manifest and receipt, **not** production compiled classes or a game JAR.

**Not run:** JDK25 (owner archive unavailable locally), full Java25 Gradle9.2.1/NeoForge, packaged game and SHA/native payload/JAR qualification, Vulkan/OpenGL owner hardware, water/FSR1 pixels, resize/reload/world switch/save/reopen/shutdown, actual GPU/CPU whole-frame p50/p95/p99 and VRAM/draw counts, frame-generation, temporal AMD FSR and supported NVIDIA RTX DLSS dispatch. Under October 27 owner override, full builds and hardware tests belong to owner; no qualified JAR or GitHub/CurseForge release asserted.

**Next:** owner same-seed native fixed/adaptive/off comparisons with both mixed-bound and all-uniform parts, FSR1/native scene heights, transparent-water ordering and bounds immutability after reload/world switch; collect CPU/GPU frame p50/p95/p99, RAM/VRAM, streaming/upload/draw counts. Only then select a GPU mesh/submission bottleneck or vendor temporal upscaler follow-up. Renderer ownership claim released at run boundary; no unrelated work modified.
