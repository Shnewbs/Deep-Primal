# Renderer jam RND-20261028-1355-FSR-QUALITY-GUARD

Scope: renderer/upscaling policy test oracle and native-AA menu quality staging only. The owner builds the full Java25/Gradle/Minecraft/GPU JAR. No terrain, finite-water, texture or other automation files are edited.

Consumed independently pinned audit: [2026-10-28T1330](../../engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md), which reproduced an obsolete UpscalingPolicyChecks expectation: newly selectable but unsupported FSR_31 and FSR_41 were incorrectly expected to activate with FSR 1/linear provider booleans. The same audit found that FSR 1 must not present temporal Native AA as a usable quality choice.

Changes: fix existing CoreChecks oracle without enabling an absent provider; test NONE/SPATIAL/FSR1/DLSS capability combinations, including the absence of temporal FSR providers even when all existing booleans are true. Define temporal-only Native AA quality-cycle policy. In the graphics screen, keep FSR 1's quality cycle within Ultra/Quality/Balanced/Performance/Custom; when a saved legacy or staged mode switch selects FSR1 + NATIVE_AA, stage QUALITY without editing the saved config until Apply. Existing FSR1 render-scale/FSR1 kernel and saved serialized enum ordinals remain unchanged. Optional DLSS and temporal FSR remain unavailable. Frame generation remains separate.

Focused Java 21 reproduction in an isolated output root (no Gradle/hosted Actions and no other actor's caches):

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  UpscalingPolicy.java UpscalingPolicyChecks.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.core.UpscalingPolicyChecks
```

Result: UPSCALING_POLICY_PASS, **139 assertions** (all provider modes, serialization order, FSR1 Native-AA menu limit, legacy staging, dimensions and finite bounds). The historical defective oracle, when executed on the unchanged policy, failed on FSR_31 as expected (exit 1). Exact edited policy and check blobs were cross-checked against the isolated compiled source. UI native compilation/pixels/menu-click acceptance, Java25, full Gradle/Minecraft/GPU and JAR packaging are **NOT RUN** under the owner-build arrangement (JDK25/archive absent from this workspace). No FPS, native GPU validation, temporal FSR or DLSS activation is claimed.

Follow-up: owner native upscaling menu quality cycling, Apply/Revert, legacy save/reopen and FSR1 pixels on OpenGL/Vulkan, then matched GPU p50/p95/p99 and actual temporal providers. See the later sibling REVIEW.md for scored pinned source and test evidence.
