# Expanded replica — docs/renderer-jams/20261028-fsr-native-aa-guard/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-fsr-native-aa-guard/README.md) |
| Source identity | git: docs/renderer-jams/20261028-fsr-native-aa-guard/README.md |
| Preserved source SHA-256 | `562202cdbd33232813975583c1ec4711d8c9fbf057cf595995d3637d5da0c947` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs an obsolete upscaling-policy oracle without enabling unsupported temporal FSR providers, and limits FSR 1 quality cycling to spatial modes. Legacy FSR 1 Native AA stages Quality while leaving saved config unchanged until Apply; serialized ordinals and existing kernels remain intact.

## Research expansion — gaps and details to deepen

- The focused policy proof needs native menu/state and provider-failure integration. Saved selection, staged selection, available implementation, effective mode, and first successful dispatch must remain distinct as new provider enums are added.

## Research expansion — proposed contracts

- Add a capability-state table and explicit migration behavior for unavailable providers and legacy qualities. Native AA remains temporal-only; FSR 1 stays spatial. Preserve Apply/Revert, reopen, and serialized compatibility independently of runtime fallback.

## Research expansion — acceptance checks

- Retain all 139 policy assertions and obsolete-oracle negative control. Exercise actual menu cycling, mode changes, Apply/Revert, legacy save/reopen, resize, and failed provider initialization on Vulkan/OpenGL with FSR 1 image checks; no temporal activation is inferred.

## Research expansion — source anchors

- Renderer jam RND-20261028-1355-FSR-QUALITY-GUARD

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
