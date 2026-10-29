# Deep Primal renderer jam 42 — read-only author self-audit

**Exact scored implementation:** [`0a500a49ba1c4481b1162eea3783cb10de8c3862`](https://github.com/Shnewbs/Deep-Primal/commit/0a500a49ba1c4481b1162eea3783cb10de8c3862); parent claim commit `c0ec3b281ae2013222de481a457ebd96c8cfc2db`, original main `7f26b4927f2fb45258eb2820e25add02a49cb31e`. Jam ID `renderer-20261029T2158-lazy-uniform-page-union`. Version still **0.0.1-alpha.29-dev.87-plant-support.1** (source-only renderer change, not a new build). This is the **renderer author's read-only self-audit** after all product changes; no code was edited during scoring. The independent engine auditor did not perform this review.

## Findings consumed and code delivered

Jam40's A/B tests found high CPU publication overhead for two-back masks on unique/uniform bounds. Jam41 added split publication CPU telemetry but did not prove the GPU break-even. The latest independent [Dev87 audit](../../engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md) still leaves native graphics unaccepted and the separate external engine grade at historical 5.0/10. Read current main, root/no AGENTS.md, engine/water/LOD/upscaling documents, renderer backlog and released prior renderer claims. Scoped `CLAIM.json` fenced only the production AABB union helper and this jam's documentation; unrelated engine writer resumed on different files during our tests.

- [`LodPublishedBounds.java` L77–119](https://github.com/Shnewbs/Deep-Primal/blob/0a500a49ba1c4481b1162eea3783cb10de8c3862/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L77-L119), Git blob `776763a1cdd47471aa28af0b15ac07cd1c84b17f`: all-equal resident parts return the original validated first AABB without doing six unnecessary per-part conservative union reductions. On the first different AABB, the helper replays the prior equal-bound prefix **once, in original order**, then resumes ordinary min/max calculations. This retains bitwise signed-zero bounds, negative-coordinate coverage and the existing uniform identity marker. Invalid AABBs still disable coarse-page rejection; adjacent/two-back masks and their A/B options are unchanged.
- The prior exact [baseline implementation](LodPublishedBoundsBaseline.java), Git blob `f1f726fc268058281201f2071ec9b2fcecc315ad`, is preserved for parity.
- [`LodLazyUnionParityChecks.java`](LodLazyUnionParityChecks.java), Git blob `f074865825f5e43388977f2ad3a2b53483514069`, checks 8,000 deterministic randomized multilevel pages across 0/1/63/64/65/128/257 parts, signed zero, invalid NaN/Inf/reversed boxes, ±30M positions, shared and independently allocated equal values, alternating A/B, mixed and unique pages. Compares exact 64-bit union coordinates, first-part identity marker, both alias masks, and broadphase/two-back A/B flags.

Only these three files changed in the production/test source commit; renderer shader/transparent-water, GPU ownership, native distance, world edits, water chemistry and upscaler provider source are untouched.

## Exact checks

System Linux x86_64, `java`/`javac` OpenJDK **21.0.12.1**. JDK25 and owner upload archive were absent; independent direct `git ls-remote` failed `Could not resolve host: github.com`, so GitHub connector pinned reads and expected-parent main ref writes were used.

```sh
javac --release 21 -Xlint:all -Werror -d classes stubs/AABB.java stubs/TerrainGpuMesh.java stubs/LodPageFrustumBounds.java stubs/LodPartFrustumRuns.java src/LodPublishedBounds.java src/LodPublishedBoundsBaseline.java tests/LodLazyUnionParityChecks.java tests/LodTwoBackABChecks.java tests/LodLazyUnionBench.java
for b in true false; do for t in true false; do
  java -ea -Xverify:all -cp classes -Ddeepprimal.lod.pageFrustumBroadphase=$b -Ddeepprimal.lod.twoBackFrustumReuse=$t com.shnewbs.deepprimal.lod.client.LodLazyUnionParityChecks
  java -ea -Xverify:all -cp classes -Ddeepprimal.lod.pageFrustumBroadphase=$b -Ddeepprimal.lod.twoBackFrustumReuse=$t com.shnewbs.deepprimal.lod.client.LodTwoBackABChecks
done; done
```

**PASS:** 4 fresh bitwise parity runs = **312,290 assertions**; 4 prior two-back source regression runs = **11,473,778 assertions**; combined **11,786,068 passed**. Intentional `j=i` prefix omission mutation compiled cleanly but failed at union-coordinate bit assertion **34** (exit 1), as expected. Pinned source/flow verification **15/15** passed and remote source/test Git blobs matched compiled local bytes.

**Paired synthetic Java CPU publication measurements**, 251 samples/run, 64 page builds/sample, 4 LOD levels × 64 parts. p50 nanoseconds, two independently launched JVMs:

| Geometry | Old, run 1 → New | Old, run 2 → New | p50 reduction |
|---|---:|---:|---:|
| Uniform shared AABB instance | 120,653 → 64,717 | 119,481 → 64,317 | 46.4% / 46.2% |
| Uniform separately allocated equal AABBs | 188,706 → 121,964 | 183,268 → 121,835 | 35.4% / 33.5% |
| Alternating A/B | 145,480 → 119,812 | 142,326 → 119,080 | 17.6% / 16.3% |
| Mixed, divergence near end | 212,942 → 192,181 | 208,546 → 197,699 | 9.7% / 5.2% |
| All unique | 117,247 → 92,930 | 117,557 → 93,822 | 20.7% / 20.2% |

**Caution:** mixed-late p99 regressed (484,793→490,622 ns and 444,663→494,668 ns); unique p99 also worsened in one fixture run. These are isolated CPU metadata results, not Minecraft FPS, completed GPU frames or driver timings.

## Six equal-weight renderer-only grades

| Category | /10 | Pinned evidence / missing gate |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.80 carried** | Exact [frozen AABB publication and masks](https://github.com/Shnewbs/Deep-Primal/blob/0a500a49ba1c4481b1162eea3783cb10de8c3862/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L43-L116), no new native useful-distance coverage |
| Mesh generation/reuse/scheduling | **6.95 carried** | No mesher, worker or GPU upload change |
| GPU culling/submission/memory | **4.55 carried** | CPU conservative-union preparation only; no actual GPU indirect, VRAM or backend gain |
| Actual FSR/DLSS | **5.00 carried** | FSR1 spatial previously supported; temporal FSR and active RTX DLSS still not demonstrated |
| Visual/backend/settings | **5.30 carried** | Unchanged [translucent pipeline](https://github.com/Shnewbs/Deep-Primal/blob/0a500a49ba1c4481b1162eea3783cb10de8c3862/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L47-L57); water pixels and native lifecycle untested |
| Validation/measured performance | **6.55 (+0.05)** | [Exact bitwise parity](LodLazyUnionParityChecks.java), 4 A/B combinations, broken control and paired CPU timings, no native perf |
| **Average** | **5.86 (+0.01)** | 35.15/6=5.8583; jam41 5.85; independent whole-engine grade historical 5.0, unchanged |

**Unrun owner gates:** Java25/Gradle9.2.1/NeoForge, native Vulkan/OpenGL/FSR1 pixels, frame p50/p95/p99/VRAM and water blending, packaged save/reopen/resize/reload/world-switch/shutdown, temporal FSR, RTX DLSS, JAR packaging, fixture exclusion/outer-nested CRC/native bundle. No JAR, GitHub prerelease, Actions or CurseForge claimed under the owner's October27 build handoff.

**Next:** run same-scene native Vulkan/OpenGL with two-back ON/OFF, page-culling fixed/adaptive/disabled, FSR1 native/quality, waterfall alpha ordering and complete CPU/GPU frame+RAM/VRAM baselines. Evaluate mixed-late p99 and the real cost of fine frustum predicates before changing more GPU work.

**Evidence archive:** `DeepPrimal_Renderer_Jam42_UniformUnion_20261029_0a500a4_SourceEvidence.zip`, **24,414 bytes**, SHA256 `9805a1618a71d65b5bbf92f3eecbcbdbc1cbd60c2b427e5120a6e4dfa7121e03`, 22 ZIP entries, outer CRC and 21 individual SHA256 checks passed; uploaded to Library `/Deep Primal/Renderer Jams` (`libfile_b6b1b65decfc8191b7602e8f0bec5a1b`). Source/test/CPU evidence only, not a playable JAR.
