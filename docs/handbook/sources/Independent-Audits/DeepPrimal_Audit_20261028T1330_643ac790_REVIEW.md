# Deep Primal Dev55 independent audit — 2026-10-28T1330Z

**Exact audited committed snapshot:** [`643ac7903f19b87626ee25f810bfb6f42cf7e0c9`](https://github.com/Shnewbs/Deep-Primal/commit/643ac7903f19b87626ee25f810bfb6f42cf7e0c9), pinned at audit start. Version `0.0.1-alpha.29-dev.55-blendpolicy.1` from `gradle.properties`. Auditor did not change production files, builder checkout, leases or caches.

**Audit status:** Four focused V8 Java21 suites PASS; newly reproduced production-source upscaling core regression FAIL; full Java25/Gradle/NeoForge/native/Windows/Vulkan/AMD/GPU and any new JAR NOT RUN under explicit October 27 owner-build override. Historical grade **50/100 (5.0/10), Tier 2**, carried unchanged, **not a fresh complete regrade**. Goal strictly greater than 8.5 remains open.

## New confirmed regression — mandatory core test fails

- [UpscalingPolicy.java L8-32](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L8-L32) adds distinct `FSR_31` and `FSR_41` user preferences and correctly refuses uninstalled temporal runtime providers even when FSR1 is available.
- [UpscalingPolicyChecks.java L11-17](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/src/coreChecks/java/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java#L11-L17) loops through *all* mode enum values and erroneously expects each mode except `DLSS` to be supported with only SPATIAL and FSR1 capabilities. The new modes violate that expectation. [CoreChecks.java L9](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L9) invokes the stale assertion.
- Both `.java` sources were reconstructed byte-exact and verified against Git blob SHA (`ae7beffd40cb988c7e449d6330bfe3d8f4655bbf` and `e5b89272270a0041111366bb9fc73f24fb2cf7af`), compiled on Java21 with `-Xlint:all -Werror`, executed with `-ea -Xverify:all`; **exit 1**, `AssertionError: Spatial/FSR capability never implies DLSS support`. See `logs/upscaling-policy-exact-source-failure.log`.
- **Repair recommendation for the Builder/graphics owner:** update the independent expected result in the test to treat FSR 3.1 and 4.1.1 as unavailable when only FSR1 is installed. Keep production refusal intact. Expand tests to verify persistence, denied activation, Native AA, sharpen settings, and model fallbacks. This is a test regression, not evidence that unavailable temporal rendering was activated.
- Secondary user-facing clarity issue: [Quality.NATIVE_AA and `renderScale`](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L11-L47) allow a Native AA preference with FSR 1, but FSR 1 is spatial reconstruction, not temporal anti-aliasing. [Upscaling selector](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/PrimalGraphicsScreen.java#L760-L795) should disable temporal-only Native AA where no temporal backend exists.

## Independent executed checks on pinned unchanged V8 source

Compared the Git blob identity of all 10 prior approved V8 density/materializer source files and the independent LOD capture priority file against the new immutable commit; **11/11 match**. Extracted previously approved exact source and test fixtures into a **new isolated audit folder**; compiled Java21 and ran four representative suites. As before, Minecraft APIs are doubles and no native game is run.

| Suite | Java21 assertions | Result |
|---|---:|---|
| Density base height | 874,642 | PASS |
| Native section bridge API doubles | 11,047,019 | PASS |
| Planned ground and heightmap API doubles | 1,235,414 | PASS |
| Callback recovery independent fault injection | 16,397 | PASS |
| **Total** | **13,173,472** | **PASS** |

The changed V8 profile v2 `blend` codec, new ecotone weights, and renderer eviction heap were inspected but **their full focused suites were not independently rerun** in this audit. The builder reports 7,333,288 ecotone assertions and the renderer author reports 251,940 eviction assertions; those are prior producer evidence, not fresh auditor assertions. Opt-in ecotone components are not enabled native generation, and the eviction CPU microbenchmark is not in-game FPS or GPU VRAM evidence. The user's 300+ FPS while creatively flying at a 125-chunk render-distance setting is encouraging but uncontrolled user-observed performance, not a benchmark for a new grade.

## Grade (ten categories equally weighted; carried only)

| Terrain | Biomes | Hydrology/caves | Features | Performance | Determinism | Architecture | Extensibility | Tests/docs | Polish | Total |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 | 4 | 6 | 4 | 3 | 7 | 6 | 4 | 7 | 4 | **50/100** |

All scoring rationales and baseline pinned tests remain in [`docs/engine-review-2026-10-24/REVIEW.md`](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/docs/engine-review-2026-10-24/REVIEW.md). No gameplay credit for prototype-only V8 ecotone or staged upscaling SDKs.

## Platform/build gates and priority

JDK25 and owner-provided JDK25 archive **not present** in this isolated Linux session. System `java` and `javac` 21.0.12.1 used. Git clone/Gradle remote dependencies cannot be obtained because `github.com` DNS resolution fails in the container; connector read of private committed GitHub sources succeeded. The owner explicitly handles full builds and JAR delivery. No fresh development JAR, nested archive CRC, native saved/reopened Minecraft world, Native AA, real Mojang codec, Windows/AMD8060S Vulkan/OpenGL GPU readback, frame-time p50/p95/p99 or long-duration water conservation was executed. **No artifact is claimed or sent as a successful JAR.** No old JAR was reused, renamed, deleted or newly uploaded; the three newest user-facing JAR retention invariant was not mutated.

Priority 1: fix the upscaling CoreChecks expectation and add capability/mode persistence and Native AA availability coverage. Priority 2: native V8 saved-profile/ecotone material transitions, old V1-V7 identity and chunk-abandonment callback rollback. Priority 3: finite water/chemistry save/reopen/unload conservation, and Windows Vulkan/GL camera-flight/render-distance frame times and renderer eviction VRAM/GC measurement. Keep independent auditor active until a complete fresh score **strictly >8.5**.
