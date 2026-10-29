# Expanded replica — docs/renderer-jams/20261028-fsr-native-aa-guard/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-fsr-native-aa-guard/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-fsr-native-aa-guard/REVIEW.md |
| Preserved source SHA-256 | `5d0502a820b16e6f14491615c90b15737c276c73bf090a26fb7e6c216856febe` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs an obsolete upscaling-policy oracle without enabling unsupported temporal FSR providers, and limits FSR 1 quality cycling to spatial modes. Legacy FSR 1 Native AA stages Quality while leaving saved config unchanged until Apply; serialized ordinals and existing kernels remain intact. The self-audit scopes its improvement to policy/menu correctness, with native UI and GPU evidence unrun.

## Research expansion — gaps and details to deepen

- The focused policy proof needs native menu/state and provider-failure integration. Saved selection, staged selection, available implementation, effective mode, and first successful dispatch must remain distinct as new provider enums are added.

## Research expansion — proposed contracts

- Add a capability-state table and explicit migration behavior for unavailable providers and legacy qualities. Native AA remains temporal-only; FSR 1 stays spatial. Preserve Apply/Revert, reopen, and serialized compatibility independently of runtime fallback.

## Research expansion — acceptance checks

- Retain all 139 policy assertions and obsolete-oracle negative control. Exercise actual menu cycling, mode changes, Apply/Revert, legacy save/reopen, resize, and failed provider initialization on Vulkan/OpenGL with FSR 1 image checks; no temporal activation is inferred.

## Research expansion — source anchors

- Renderer jam RND-20261028-1355-FSR-QUALITY-GUARD — read-only self-audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam RND-20261028-1355-FSR-QUALITY-GUARD — read-only self-audit

**Scored production snapshot:** [`baacdebc80d4a26cff893a30a962a144ef43b519`](https://github.com/Shnewbs/Deep-Primal/commit/baacdebc80d4a26cff893a30a962a144ef43b519), parent [`6068458949cfde3b57b7842b0ab33f9eccd0d495`](https://github.com/Shnewbs/Deep-Primal/commit/6068458949cfde3b57b7842b0ab33f9eccd0d495). Started at main `9922b9d3733986848a31b65f3c911b928c0b3bc7`. This is a renderer **self-audit**, NOT the independent whole-engine auditor, and does not change that auditor's **5.0/10 historical** score. No production edits occurred during this scored review.

**Consumed actual independent audit:** [2026-10-28T1330](../../engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md) independently compiled the old FSR policy/checks and reproduced exit 1: `AssertionError: Spatial/FSR capability never implies DLSS support` after FSR_31/FSR_41 preferences were added. Requested fix: correct the oracle, not the correct native fallback; also avoid advertising FSR 1 as temporal Native AA.

**Actual changed files and pinned Git blob identities:**
- [`UpscalingPolicy.java#L24-L60`](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L24-L60): `8cb56a92010413a2cb9e7e13d6764c99c063f96b` — explicit temporal-only Native AA UI policy, FSR1 legacy requested-quality staging and bounded menu range. FSR1 shader/runtime or unsupported-provider policy was not promoted.
- [`UpscalingPolicyChecks.java#L11-L55`](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/src/coreChecks/java/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java#L11-L55): `d3060c7b10726cdac9f8783aa5bb83286d7b92c1` — valid capability oracle for six enum modes; NONE/FSR1/DLSS provider combinations, explicit never-active FSR3/4, saved enum ordinals, menu staging; [cycle checks L88-L96](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/src/coreChecks/java/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java#L88-L96).
- [`PrimalGraphicsScreen.java#L836-L846`](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/PrimalGraphicsScreen.java#L836-L846): `3a1703e219de77a5b6bfba1cb386b58ef1ad7c88` — legacy FSR1+Native AA staged to Quality while saved baseline remains untouched until Apply; [bounded Native-AA quality cycle and mode switch L1028-L1038](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/PrimalGraphicsScreen.java#L1028-L1038). GUI/native integration unrun.
- [`README.md`](README.md): `3f7a786aec2ea965de50ffdbe4a986fa7b288f02` — scoped evidence/handoff. Claim and later review/receipt/backlog updates are documentation-only.

**Executed validation, exact commands (Linux, isolated disposable directory):**
```sh
javac -version  # javac 21.0.12.1
javac --release 21 -Xlint:all -Werror -d /mnt/data/renderer-20261028-fsr-nativeaa-check/classes \
  /mnt/data/renderer-20261028-fsr-nativeaa-check/src/com/shnewbs/deepprimal/core/UpscalingPolicy.java \
  /mnt/data/renderer-20261028-fsr-nativeaa-check/src/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java
java -ea -Xverify:all \
  -cp /mnt/data/renderer-20261028-fsr-nativeaa-check/classes \
  com.shnewbs.deepprimal.core.UpscalingPolicyChecks
git hash-object /mnt/data/renderer-20261028-fsr-nativeaa-check/src/com/shnewbs/deepprimal/core/UpscalingPolicy.java \
  /mnt/data/renderer-20261028-fsr-nativeaa-check/src/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java
```

**PASS:** strict javac exit 0; Java verified runtime exit 0, `UPSCALING_POLICY_PASS assertions=139 ... nativeAaTemporalOnly=true legacyStageCorrection=true`; local compiled source hashes exactly match the two pinned Git blobs listed above. **Expected negative:** isolated `OldCapabilityOracle.java` compiled with strict Java21, then exited 1 with `AssertionError: Spatial/FSR capability never implies DLSS support: FSR_31`, confirming the independent finding. **Source-only UI inspection:** exact committed UI read back and three injection points inspected; **not a compiled native screen test**.

**Not run / cannot claim:** JDK25 (toolchain and owner-provided archive not mounted in this workspace), full Gradle/NeoForge/module compile, actual staged Apply/Revert/save-reopen UI, Vulkan/OpenGL pixels/resize/resource reload/world switch/shutdown, FSR1 visual comparison after this UI change, DLSS on NVIDIA RTX, FSR3/4 on compatible AMD hardware, any CPU/GPU frame-time p50/p95/p99, draw calls, VRAM/RAM, JAR/package/CRC/native payload or GitHub release. Owner performs full builds. Source-only tests do not prove gameplay or performance. No FPS improvement asserted.

| Six equally weighted renderer categories | Score /10 | Pinned evidence and remaining limits |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.50 carried** | Unchanged prior [bounded nearest-page implementation](../20261028-disk-nearest/REVIEW.md). No native saved-world reopen, teleport, edit, unloaded terrain coverage or unseen-authoritative delivery in this jam. |
| Mesh generation, reuse and scheduling | **6.50 carried** | Prior [eviction heap LOD ordering](../20261028-eviction-heap/REVIEW.md), no new mesh or scheduler change or GPU timing. |
| GPU culling/submission/memory | **4.00 carried** | No GPU submission changes, no measured VRAM, real indirect/MDI path or qualified GPU profile. |
| Actual FSR/DLSS implementation | **5.00 carried** | Existing [FSR1/OpenGL/Vulkan functional receipts](../../validation/upscaling-2026-10-27/README.md); [policy L24–L33](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L24-L33) leaves FSR3/4 unavailable and DLSS pending. No new temporal integration. |
| Visual/backend/settings compatibility | **5.25 (+0.25)** | [UI staged legacy correction](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/PrimalGraphicsScreen.java#L836-L846) and [FSR1 menu cycle guard](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/PrimalGraphicsScreen.java#L1028-L1038). Saved ordinal/persisted baseline retained. Native UI/FSR/water transparency pixel acceptance unrun. |
| Validation and measured performance | **4.75 (+0.25)** | Exact [repaired oracle](https://github.com/Shnewbs/Deep-Primal/blob/baacdebc80d4a26cff893a30a962a144ef43b519/src/coreChecks/java/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java#L11-L55), 139 strict Java21 assertions and expected-failing old oracle. No Java25/full build, native/RTX, frame-time, GPU memory measurements. |
| **Renderer average** | **32.00 / 60 = 5.33/10 (+0.08)** | Previous renderer self-audit 5.25; target >8.5 not attained. Engine independent grade 5.0 carried separately, not regraded. |

**Unresolved defects and next repair:** check exact native upscaling menu over switching/Apply/Revert/legacy saved FSR1 NativeAA, small UI, reload and world handoff on both OpenGL/Vulkan without altering transparent water; obtain owner GPU p50/p95/p99, upload/fence/draw/VRAM in camera travel, teleport and saved-world reopen before GPU batching and temporal provider claims. Renderer ownership claim is released at this run boundary. No concurrent engine/water files changed.


<!-- END PRESERVED SOURCE -->
