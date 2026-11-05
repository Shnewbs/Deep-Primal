# Deep Primal independent audit — 2026-10-28 14:34 UTC

**Pinned source:** [`6e98343f3f70266ca8ff6474c0dae08f509685bf`](https://github.com/Shnewbs/Deep-Primal/commit/6e98343f3f70266ca8ff6474c0dae08f509685bf), version `0.0.1-alpha.29-dev.56-ecotone-surface.1`. No new production edits by this auditor. Current owner override dated October 27 assigns full Java25/Gradle/Minecraft/GPU builds to owner; **no JAR was built or published**.

## Independent findings

1. **Previous FSR test failure repaired.** New UpscalingPolicyChecks source explicitly expects FSR_31/FSR_41 preferences to remain unsupported until real temporal providers exist. Exact committed [UpscalingPolicy.java](https://github.com/Shnewbs/Deep-Primal/blob/6e98343f3f70266ca8ff6474c0dae08f509685bf/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L23-L62) Git blob `8cb56a92010413a2cb9e7e13d6764c99c063f96b` was reconstructed, compiled with javac21 --release 21 -Xlint:all -Werror, and independently passed 3,035 assertions. Native AA is restricted to DLSS/FSR3/FSR4 preference menus, not actual active providers. Renderer self-report also lists 139 passing assertions; independent UI click/Apply/Revert native tests NOT run.
2. **V8 preview is deterministic, not registered worldgen.** New exact [EcotoneSurfaceField.java](https://github.com/Shnewbs/Deep-Primal/blob/6e98343f3f70266ca8ff6474c0dae08f509685bf/engine-core/src/main/java/com/shnewbs/deepprimal/core/biome/EcotoneSurfaceField.java#L18-L159) Git blob `a171ac13e36db1f076ba4ac820c3e1d2ce11ec0e` compiled and passed 594,041 independent assertions against a *test-only EcotoneBlend weights stand-in* (weighted fractions, seed-replay, negative/extreme signed coordinates, smooth adjacent fields, parallel replay, rejection and defensive palette). The actual EcotoneBlend producer and full native V8 snapshot codec were NOT compiled with this independent harness; [V8GeneratorSnapshot.Prepared](https://github.com/Shnewbs/Deep-Primal/blob/6e98343f3f70266ca8ff6474c0dae08f509685bf/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L186-L213) uses an explicit preview method. Palette is still an unpersisted experimental constant, so old saved V8-r2 outputs must not be silently changed by native activation.
3. **Previously accepted V8 source-only regression re-run.** 13,173,472 assertions passed across base height (874642), material preflight (1235414), callback prewrite rollback (16397), and section bridge (11047019). Test sources and unchanged production files were drawn from the previous pinned independent evidence; compilation uses API doubles and a prior V8GeneratorSnapshot fixture. The current modified V8GeneratorSnapshot was NOT part of these four test compilations, nor was native Minecraft.
4. **Performance remains unaccepted.** Owner observed >300 FPS while flying in Creative at 125-chunk distance with Vulkan; useful anecdotal hardware signal, not a captured controlled p50/p95/p99 load-to-ready/GPU benchmark and not an independent grade change. Finite-water unload/reopen conservation and native saved-world checks remain pending.

## Ten-category rubric (carried baseline; NOT a fresh full regrade)

| Category | /10 |
| --- | ---: |
| Terrain realism / variety | 5 |
| Biome coherence / blending | 4 |
| Hydrology / caves | 6 |
| Features / structures | 4 |
| Performance / threading | 3 |
| Determinism / stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing / documentation | 7 |
| Polish / tooling | 4 |
| **Total; average; tier** | **50/100; 5.0/10; Tier 2** |

Last full accepted review: [2026-10-24 ten-category source-and-native evidence](https://github.com/Shnewbs/Deep-Primal/blob/6e98343f3f70266ca8ff6474c0dae08f509685bf/docs/engine-review-2026-10-24/REVIEW.md). Strict target **greater than 8.5/10** not demonstrated. Actual production edits since the last independent audit were made by the Builder and renderer, not by this auditor: V8 ecotone preview/snapshot call, repaired FSR oracle/Native AA menu guarding, and documentation/version changes. No staged R57/R60/R61 promotion.

## Gates NOT run

- Java25 not available in this execution workspace; owner-provided archive was not mounted; installed Java 21.0.12.1 only.
- No complete Gradle/NeoForge module build, JAR, nested archive CRC, packaged/source class comparison, native save/reopen/quit, Windows/AMD8060S Vulkan/OpenGL, real Mojang JSON/NBT, FSR3/4 temporal GPU provider or frame-time/VRAM bench.
- No CurseForge/GitHub Releases publication; no old JAR relabeling or retention cleanup (no newly qualified JAR).

**Next priorities:** native V8 blend-profile versioning/palette and saved material placement/heightmap, long-soak water quantity/salt/sediment conservation and native recovery, owner-controlled Vulkan/GL water/LOD/FSR UI and GPU frame-time validation. Auditor work was isolated, read-only toward production, and did not touch the Builder/renderer lease/workspaces.
