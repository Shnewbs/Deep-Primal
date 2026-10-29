# Expanded replica — Independent Audits/DeepPrimal_Audit_20261028T1530_cec6143_REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_a949a6031650819184db5fb8ef8f361f&account_id=personal) |
| Source identity | library: Independent Audits/DeepPrimal_Audit_20261028T1530_cec6143_REVIEW.md |
| Preserved source SHA-256 | `31d4fb73507fd43d587f18351473c98e04847c0667502f858071f3db3fc9161f` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent audit — 2026-10-28 15:30 UTC”. Its original opening states: “Pinned main source: cec61432aa1cb0d4f1909a33d537bd03ca87e31d (pinned before code reads). Version: 0.0.1-alpha.29-dev.57-surface-policy.1, neo_version=26.1.2.122. Role: independent auditor, read-only with respect to production source, Builder checkout/lease, cache and outputs. Owner's Oct 27 override assigns full builds/JAR/GPU acceptance to owner. No development JAR, stable release or CurseForge publication was attempted; no old JARs deleted.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `V8GeneratorSnapshot.java`, `LodEvictionHeap.java`, `VoxelLodRenderer.java`, `V8SurfacePolicy.java`.

## Research expansion — gaps and details to deepen

- Extend the recorded generation change with saved-revision, chunk-order, boundary, and authoritative-water compatibility checks.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Freeze existing generator/channel/material recipes; new terrain behavior requires an explicit saved revision and complete dependency identity.
- Distinguish surface appearance, regional drainage plans, ocean connectivity, and live finite-water ownership in the acceptance record.

## Research expansion — acceptance checks

- Generate the same signed-coordinate corpus in reversed and randomized chunk orders, then compare overlap, seams, and saved-profile replay.
- Reopen with creation defaults changed and verify old geography, player edits, water quantity, and material identities remain governed by the saved recipe.

## Research expansion — source anchors

- Deep Primal independent audit — 2026-10-28 15:30 UTC
- Changed committed source since prior independent audit
- Independent execution / receipts

Related design: [terrain water](../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent audit — 2026-10-28 15:30 UTC

**Pinned main source:** `cec61432aa1cb0d4f1909a33d537bd03ca87e31d` (pinned before code reads).
**Version:** `0.0.1-alpha.29-dev.57-surface-policy.1`, `neo_version=26.1.2.122`.
**Role:** independent auditor, read-only with respect to production source, Builder checkout/lease, cache and outputs. Owner's Oct 27 override assigns full builds/JAR/GPU acceptance to owner. No development JAR, stable release or CurseForge publication was attempted; no old JARs deleted.

## Changed committed source since prior independent audit

GitHub comparison from prior audited `6e98343f3f70266ca8ff6474c0dae08f509685bf` to this source: seven commits, including:

- [`src/main/java/com/shnewbs/deepprimal/engine/V8SurfacePolicy.java`](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/src/main/java/com/shnewbs/deepprimal/engine/V8SurfacePolicy.java) — saved 40-biome palette/climate provenance; strict 6-field envelope and frozen fingerprint, Git blob `9aceaaa11f486d8861eb8fb20bbc5a944030bf60`.
- [`V8GeneratorSnapshot.java` lines 18–25, 47–58, 103–125, 144–215, 257–272](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java) — explicit revision-3, 8-field snapshot while preserving legacy revision-1/2 constructors/wire formats; blob `fecb8c4850730d0c515169c4a467207f3aaf1230`. Actual Mojang codec and game native placement unverified.
- [`LodEvictionHeap.java`](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodEvictionHeap.java) — reusable primitive-array eviction selector, blob `7fff32140b922b065bc3ee60f4ad2c6573399ca3`.
- [`VoxelLodRenderer.java` lines 568–599 and 696–717](https://github.com/Shnewbs/Deep-Primal/blob/cec61432aa1cb0d4f1909a33d537bd03ca87e31d/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L568-L599) — allocates one 4096-slot heap per world session; pre-upload eviction to at most 4095 pages, then cache insertion, with reset cleanup on world switch; blob `9e912435f85caa3238c7733d3823e2fbf4e355da`.

New source remains **opt-in/policy only** for V8 and must not earn deployed terrain or biome gameplay credit. Renderer heap optimization is not yet an owner-Windows measured frame-time/VRAM performance result.

## Independent execution / receipts

- **PASS Java21 source identity:** `git hash-object` of exact `LodEvictionHeap.java` = `7fff32140b922b065bc3ee60f4ad2c6573399ca3`, exact `V8SurfacePolicy.java` = `9aceaaa11f486d8861eb8fb20bbc5a944030bf60`, exact `EcotoneSurfaceField.java` = `a171ac13e36db1f076ba4ac820c3e1d2ce11ec0e`. All match the pinned GitHub blobs. The EcotoneBlend and Mojang APIs are **clearly isolated test doubles**, not copied as production classes.
- **PASS Java21 `javac --release 21 -proc:none -Xlint:all -Werror`, `java -ea -Xverify:all`:** 3,537,315 independent heap-oracle assertions across 1,800 seeded scenarios, max 4,096 residents, protected-page behavior, signed coordinate extremes, stable ties, reset/reuse, overcapacity rejection and post-rejection recovery. Input was an ordered snapshot; source map ownership and GPU buffers were not exercised. A mutated comparator reverses the intended order and fails independently at check 29.
- **PASS Java21 focused V8 surface provenance:** 14,342 independent assertions using the exact committed `V8SurfacePolicy` and `EcotoneSurfaceField` production source with a test-only `EcotoneBlend` provider and Mojang Codec/DynamicOps stand-ins. Frozen 40-biome SHA-independent FNV fingerprint is `684e3ee170e01a32`; 2,048 valid codec roundtrips plus 12,288 invalid-field/revision/fingerprint/climate cases all behave as designed. Actual Mojang `JsonOps` and `NbtOps` were not available.
- **PASS production integration source inspection:** Renderer `evictToFit` uses capacity 4096; before a new pending page, it limits entries to 4095, and `cache.put` completes one admission; no other `cache.put` observed in the pinned renderer. This is a source-level invariant only, not a proof against all runtime exceptions or external mutations.
- **Previous producer evidence, NOT independently remeasured:** renderer receipt `docs/renderer-jams/20261028-eviction-reuse/RECEIPT.json` reports 509,243 new Java21 and 251,940 legacy checks, one CPU benchmark with 640 4,096-page samples, old/new p50 65,420/53,777 ns and 66,265/55,879 ns, old/new per-thread allocation 86,112/0 bytes. These are not GPU timings and were not rerun to avoid active timing contention. Builder `docs/validation/engine-continuation/v8-frozen-surface-policy-2026-10-28/README.md` reports 14,122,671 stand-in assertions; not independently rerun in full.

## Ten-category carried grade, NOT a fresh full-engine regrade

| Category | /10 | Current evidence/gate |
| --- | ---: | --- |
| Terrain realism/variety | 5 | Explicit V8 prototype and saved policy; no native registered 3D density terrain acceptance. |
| Biome coherence/blending | 4 | Ecotone preview/frozen palette strict checks; still no accepted native world cover placement or replayed saved worlds. |
| Hydrology and caves | 6 | Existing connected drainage core; finite water/salt/sediment loaded/unloaded/save/reopen owner gates incomplete. |
| Features and structures | 4 | Existing tree/deposit candidates, no generalized structure spacing/exclusion runtime. |
| Performance and threading | 3 | CPU LOD helper optimizes an isolated selector; terrain <5ms p95 and owner GPU/tick p95/p99 unaccepted. |
| Determinism and stability | 7 | Prior same-host replay plus new versioned policy; long-running native replay and cross-system parity incomplete. |
| Architecture | 6 | Module boundaries, still coupled materialization and complex cache lifecycle. |
| Extensibility/data-driven | 4 | Saved policy is additive; no complete gameplay-loaded worldgen graph/feature API. |
| Testing, benchmarks, docs | 7 | Exact-blob focused Java tests and receipts; missing modern native/full-package/hardware matrix. |
| Polish/config/tooling | 4 | Settings and safe FSR3/4 unavailable states; live water, GUI and authoring acceptance unresolved. |
| **Overall, Tier 2** | **50/100 = 5.0/10** | **Historical complete baseline carried forward.** |

Strict stopping criterion is **fresh, complete >85/100**, not met. Do not promote a regrade from the focused checks alone.

## Not run / blockers

- Java25 JDK not available at specified prior-workspace paths, and owner archive not mounted; Library title search found no `OpenJDK25U-jdk_x64_linux_hotspot_25.0.5_8.tar.gz`. Host `/usr/bin/java`/`javac` are Java21. Java25 source/language-level/toolchain gates **not run**.
- Shell `git ls-remote` fails DNS (`Could not resolve host: github.com`); no complete verified pinned checkout, wrapper/dependency cache or Gradle executable available. No `./gradlew`/NeoForge or end-to-end package build attempted under owner Oct 27 override.
- No new JAR bytes, outer/nested archive CRCs, Windows Vulkan/OpenGL payload/merged Themes class identity, native Minecraft V8 profile save/reopen/lighting, water/salt/sediment conservation, renderer/GPU p95/p99/VRAM or owner's Windows AMD8060S replay/smoke checks.
- User-observed 300+ FPS flying Creative with Vulkan and 125 chunk-distance setting is useful anecdotal field data, **not** controlled loaded-chunk counts, GPU frame-time or chunk-generation benchmark.

**Immediate priorities:** true Mojang rev3 saved-profile codec + native save/reopen and V1–V7 non-regression; finite-water quantity/constituent conservation unload/restart/edits; GPU cache-pressure/LOD eviction + Vulkan/OpenGL water and FSR UI quality on owner AMD Windows hardware. Do not pause the existing builder for an audit report.


<!-- END PRESERVED SOURCE -->
