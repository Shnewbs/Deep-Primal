# Dev171 — Independent Deep Primal audit (2026-11-01 23:30 UTC)

**Baseline:** `eac7a372b24351f6ec6270d4ee8899e69eb4d8aa` (Dev169 audit). **Reviewed engineering head:** `56c163f48519d3ecaa1d481d6cb2f751646428b2`. **New commits:** two, five changed paths. No GitHub Actions.

## Change and ownership review

[Compare Dev169..Dev170](https://github.com/Shnewbs/Deep-Primal/compare/eac7a372b24351f6ec6270d4ee8899e69eb4d8aa...56c163f48519d3ecaa1d481d6cb2f751646428b2) contains a completed writer claim, `WaterTransport.surface` NaN UV admission, a registered regression test and validation documentation. Claim status is `completed` and active_until has elapsed. No changes to native water storage, terrain generation, renderer backend, LOD, performance scheduling or persistence in this delta. Reviewed the call path in `src/main/java/com/shnewbs/deepprimal/client/render/SmoothWaterRenderer.java`; this is source-only, not a GPU acceptance test.

## Independent reproduction and tests

Fetched exact production `WaterTransport.java` (Git blob `cfc0993fe503fbbe02356b42fbe0f1a0afc0fe23`) and exact `WaterSurfaceUvAdmissionChecks.java` (Git blob `f650391c32b2c02627c0e9218f550e708ee9cd4b`). Both hashes verified locally with `git hash-object`. Reconstructed `WaterTransport.java` from the previously verified Dev169 source and applied the exact Dev170 delta; `TerrainNoise` is a minimal dependency double with the exact production `lerp(a,b,t) = a + (b-a)*t`. Reused exact `WaterSurfaceConvexAdmissionChecks.java` from the prior Dev169 evidence archive.

Strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed:
- Committed UV regression: **1,000,481** assertions.
- Committed convex regression: **1,933,122** assertions.
- Additional independent seeded oracle: **4,208,770** assertions (finite UV bitwise parity, NaN U/V/both midpoint normalization, infinity clamping, extreme finite corners, water conservation and snapshot admission).
- **7,142,373 passing isolated assertions** total.
- Baseline `WaterTransport` from Dev169 (Git blob `49d5709cd209fd901501fb0f0afd5c098b62d829`) **fails** the independent oracle with `AssertionError: NaN U` as expected. This verifies regression sensitivity.

**No additional nonconflicting production defect confirmed.** No production edits made. The test double and source-excerpt harness do not validate full native Minecraft, GPU or JDK25 behavior.

## Compatibility, runtime and packaging gates

Current repository `gradle.properties` specifies `neo_version=26.1.2.122`, `build.gradle` Java25, ModDevGradle 2.0.148, wrapper Gradle 9.2.1 and JarJar modules. Official [NeoForge 26.3 branch](https://github.com/neoforged/NeoForge/blob/26.3.x/gradle.properties) also specifies Java25, and official [Maven directory](https://maven.neoforged.net/releases/net/neoforged/neoforge/) lists stable 26.3.0.99 as of this review. No Deep Primal 26.3/26.4 build or binary compatibility claim.

Local runtime is Java21 only, no Gradle executable or complete private checkout, no resolved dependencies; `git ls-remote` cannot resolve github.com. Connected GitHub read/write works but does not provide a local clone. **No full Java25 NeoForge build, playable JAR or JAR SHA256 was produced.** No previous audit JAR deleted. No GitHub Actions invoked. Native water conservation/save-reopen, terrain seams, Vulkan/OpenGL pixels, LOD coverage and FPS/VRAM remain unaccepted.

## Grades and next gates

- **8.5/10 provisional scoped malformed-water-UV correctness**: exact source, registered regression, 7.14M passing isolated assertions, baseline negative control.
- **5.0/10 historical whole-engine baseline**, not freshly regraded; see [October 24 ten-category review](https://github.com/Shnewbs/Deep-Primal/blob/f141c0b4d058abb311cb088e4f17a575685a2939/docs/engine-review-2026-10-24/REVIEW.md). Historical renderer category average 5.0/10 in [Oct 28 review](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/docs/renderer-jams/20261028-disk-retry/REVIEW.md); not retested.

**Priority:** full Java25 repository checkout and dependency resolution, `check`/`coreChecks`/JarJar and runtime package identity, native water save/reopen and cliff temporal pixels, terrain seam hashes and Vulkan/LOD frame p50/p95/p99. Produce a replacement JAR with SHA256 before retiring any older JAR.

Local audit evidence: `DeepPrimal_Dev171_IndependentAudit_20261101T2330_Evidence.zip`; SHA256 `6a3c0c46551df3def22979073085ac05b6bd3932d52ce300b30eefe6c3f9e5cf`; ZIP CRC verified. Binary evidence is not committed to GitHub.