# Independent Deep Primal engine audit — 2026-10-28 00:29 UTC

**Pinned main at audit start:** `bcce8355c7f8288e2749253578398e68c107bcda` — version `0.0.1-alpha.29-dev.40-waterwritescope.1`.
**Scope:** committed code inspection plus a separately compiled Java 21 *model*; no production source edits, builder coordination gate or native Minecraft invocation. This is **not a fresh full regrade**.

## Finding: constituent admission still relies on a native return in seven NON-transfer paths

The dev40 builder correctly replaced global chemistry event suppression with a scoped, one-event `WaterWriteScope`, addressing the preceding [auditor report](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/docs/engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md). The helper's own [builder tests](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/coreChecks/java/com/shnewbs/deepprimal/engine/WaterWriteScopeChecks.java) are reported as 480,028 passing checks in [dev40 evidence](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/docs/validation/engine-continuation/water-cell-write-scope-2026-10-27/README.md). Those were **not rerun by this auditor**.

A different residual vulnerability remains: [`HydrologyRuntime.write` L70–81](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L70-L81) returns the native `setBlock` result with a finally-closed scope; it does **not** establish that the cell still contains the expected block state *after synchronous callbacks*. Seven public environmental admission paths trust that return, then debit/credit reservoirs and/or overwrite the chemistry ledger **without a second loaded-state ownership check**:

| Method | Pinned code | Risk if synchronous callback changes the cell after its requested write |
| --- | --- | --- |
| `restoreOcean` | [L129–142](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L129-L142) | Debits ocean reserve and persists solutes under a replaced cell |
| `oceanSink` | [L144–162](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L144-L162) | Credits ocean despite lost/refilled source ownership |
| `place` | [L164–171](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L164-L171) | Reports placement and writes a parcel if the native cell was removed |
| `pickup` | [L172–181](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L172-L181) | Returns the parcel even if water was restored before the call returned |
| `emit` | [L182–198](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L182-L198) | Debits groundwater and saves a parcel under a replaced cell |
| `infiltrate` | [L199–213](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L199-L213) | Credits aquifer despite a foreign edit of the source |
| `meltIce` | [L215–223](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L215-L223) | Persists a water parcel where ice was replaced again |

Contrast [`WaterRuntime.transfer` L245–295](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L245-L295), which has targeted postwrite barriers introduced in dev39. That protection does not automatically cover the seven `HydrologyRuntime` entry points. Actual relevant source blob: `898649a479972c77c3c46470447197261107235a`.

**Read-only source check:** inspected each of these seven committed method bodies at pinned SHA: all have a native write followed by a ledger/reservoir update, and none checks the final loaded native state between those operations. This is a structural finding about source code, **not** proof that a specific native callback is reachable or that persisted water loss occurred.

## Focused independent model evidence

The self-contained `NonTransferCallbackProbe.java` models the production operation order and a synchronous foreign write after the requested native transition, including the external chemistry invalidation path. Commands:

```sh
javac --release 21 -Xlint:all -d classes NonTransferCallbackProbe.java
java -Xverify:all -cp classes NonTransferCallbackProbe
```

**PASS 16/16 modeled assertions**: normal placement/pickup controls and three callback cases. A modeled `pickup` returns 4096 units plus 211 salt/89 sediment while the block remains full; modeled `place` writes 4096 units and solutes in saved chemistry while the block is actually air; modeled `emit` reports success with a water parcel under stone. The mismatch is **conditional** on such a callback. Probe SHA256 `461b7f0e67c5485d5853458372b17c646b8268eb775ac7c0d82d740dc8f4bada`, Java result SHA256 `eaa086947b12f791114264f1788469119a5696b203641aef9ba7a2143ef45ccd`. Evidence ZIP `DeepPrimal_Audit_20261028T0029_bcce835_NonTransfer_SourceEvidence.zip`: 3,425 bytes, SHA256 `a96e7562702e038945667667752320a8ad7182f16eb977b5c50530d26c258ed6`; all four ZIP-entry CRCs passed. This model is **not a production-class Java25 or Minecraft test**.

## Rubric (historical grade carried, NOT a new complete assessment)

| Equally weighted category | Carried /10 | Missing gate or current evidence |
| --- | ---: | --- |
| Terrain realism and variety | 5 | Broad V8/3D density + real landscape comparison unverified |
| Biome blending and coherence | 4 | Weighted/ecotone feature placement remains incomplete |
| Hydrology and cave quality | 6 | New callback admission finding; native conservation and caves unaccepted |
| Feature/structure placement | 4 | General spacing/exclusion pipeline missing |
| Performance and threading | 3 | Historical warm fill ~14.45–14.99 ms, vs 5 ms target; no fresh named-host benchmark |
| Determinism and stability | 7 | Cross-host replay, long soak and recovery outstanding |
| Architecture and code quality | 6 | Separate modules; native constituent transaction guards incomplete |
| Extensibility/data-driven design | 4 | General editable terrain/feature graph/API missing |
| Testing, benchmarks and docs | 7 | Focused model only, native timing/build/multiplayer absent |
| Polish, config and tooling | 4 | Owner-visible graphics/water/LOD acceptance pending |
| **Total — Tier 2** | **50/100 = 5.0/10** | **Historical only; strictly >8.5 target NOT MET** |

[Original full rubric and baseline](https://github.com/Shnewbs/Deep-Primal/blob/bcce8355c7f8288e2749253578398e68c107bcda/docs/engine-review-2026-10-24/REVIEW.md). No score changes are awarded from a modeled callback, unused modules, staged geography or unverified hardware behavior.

## Build, separation and next steps

- **Passed here:** repository pinned read, seven-method read-only structural comparison, Java21 compilation and 16 modeled assertions; model evidence ZIP CRCs and SHA256. No production code changed.
- **Not run:** Java25 (workspace has Java21 only; owner-supplied JDK25 archive absent), Gradle, Minecraft/NeoForge class compilation and callback injection, packaged JAR or nested CRC, save/reopen, native server/client, Linux GPU, Windows/AMD8060S, OpenGL/Vulkan waterfall pixels and p50/p95/p99. Shell GitHub clone failed DNS; authenticated GitHub access supplied exact pinned files. Under owner's October 27 override, complete builds/GPU acceptance are owner-operated; **no new JAR or development release is claimed**.
- **Isolation:** no builder lease, checkout/cache/output edits, process interruptions, timed workloads, staged file promotions, renderer changes or JAR retention deletion. Audit files are separate under this per-audit path; the builder is free to continue. Newer main commits after the pinned SHA (if any) are **not** included in this finding.
- **Next builder priority:** add conservative post-native final-state checking to `place`, `pickup`, `emit`, `infiltrate`, `restoreOcean`, `oceanSink` and `meltIce` without overwriting foreign edits; exercise callbacks both before and after native state notification, false returns and exceptions. Follow with an exact-volume/constituent save/reopen and unloaded-cell single-owner journal test. An ownership exception alone is not proof of conservation. Continue river/waterfall temporal pixels and memory/frame-time gates on both APIs.

Independent auditor stopping condition **not satisfied**. The builder is not paused or gated by this report.
