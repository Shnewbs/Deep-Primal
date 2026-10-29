# Expanded replica — docs/engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T2332-a14721c/REVIEW.md |
| Preserved source SHA-256 | `92451a09fe1df44db1586e9cd23b3e15d336fe1c5040f0f92bc03df30c410a62` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent Deep Primal engine audit — 2026-10-27 23:32 UTC”. Its original opening states: “Exact inspected source: a14721c72625ea15ad9cb9168a6140c3f6882005, dev39 version 0.0.1-alpha.29-dev.39-waterpostwrite.1; code change in c969ace0dc1c236fcb371e86d121b478fbf7688c. Read-only code inspection; no engine files, builder state, renderer files, build resources or schedules changed. This is not a fresh complete regrade.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `ServerLevel`.

## Research expansion — gaps and details to deepen

- Carry the water finding through the real native callback boundary while preserving quantity, constituent identity, and exactly-once ownership.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Bind write scopes to world, level, immutable cell coordinates, expected state, and revision; unrelated callbacks must retain their own invalidation and accounting.
- Recheck final state after synchronous callbacks before committing water/salt/sediment ledgers; define bounded compensation and explicit failure when restoration is impossible.

## Research expansion — acceptance checks

- Exercise refused-but-applied and accepted-but-replaced writes, same-volume chemistry changes, unrelated-cell mutation, cross-level callbacks, and unload during commit.
- Compare joint loaded/unloaded/reservoir quantity and composition before and after edit, save, restart, and reopen using the actual host runtime.

## Research expansion — source anchors

- Independent Deep Primal engine audit — 2026-10-27 23:32 UTC
- Execution/limitations

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent Deep Primal engine audit — 2026-10-27 23:32 UTC

**Exact inspected source:** `a14721c72625ea15ad9cb9168a6140c3f6882005`, dev39 version `0.0.1-alpha.29-dev.39-waterpostwrite.1`; code change in `c969ace0dc1c236fcb371e86d121b478fbf7688c`. Read-only code inspection; no engine files, builder state, renderer files, build resources or schedules changed. This **is not a fresh complete regrade**.

## New conditional defect: global write-depth swallows unrelated native cell mutations

- [HydrologyRuntime.java lines 19, 72–90](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L72-L90) maintains a **static process-wide** `internalWrites` count around a native `setBlock`. `externalChange` returns without checking the owning level/position whenever the count is nonzero.
- [PressureChunkMutationMixin.java lines 13–19](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/src/main/java/com/shnewbs/deepprimal/mixin/PressureChunkMutationMixin.java#L13-L19) forwards block writes through [WaterRuntime.changed lines 85–91](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L85-L91), then to this global guard.
- [HydrologyRuntime.contents lines 92–97](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L92-L97) reuses a persisted chemistry parcel if current water **quantity alone** matches it.

If a *synchronous callback* during an internal write to A deletes or replaces **unrelated** cell C, the event for C is globally ignored. Later placing fresh water of the original unit count at C can therefore reuse C's stale salt/sediment parcel. A callback into another `ServerLevel` is also covered by the same global flag. This is **conditional on actual callback execution**; no native NeoForge data-loss reproduction is claimed. Dev39's new source+receiver postwrite barrier is present and protects *those two cells* in its modeled scope, but not unrelated C.

**Independent source-matched Java 21 model:** `javac --release 21 -Xlint:all -d classes CrossCellChemistrySuppressionProbe.java` and `java -Xverify:all -cp classes CrossCellChemistrySuppressionProbe` passed **11 assertions**: ordinary external deletion clears composition; unrelated same-level and different-level callback edits during another write retain it; equal-volume refill inherits stale salt; own-write suppression and unwind controls work; changed volume avoids reuse but retains the stale map entry. Probe SHA256 `ac19fb80c2f30285e8b2673909715510add5d9a2fde6b19b5f6b68ff62f9f5cf`. This is **not production class compilation, packaged behavior or proof of real Minecraft callback ordering**.

**Builder repair/gates:** use a bounded per-level/per-position scoped expected-transition context instead of a global write-depth exemption. Cover nested calls, same-cell callbacks, unrelated-cell edits, second-level edits, unloaded/edit state and exact salt/sediment/volume accounting before and after save/reopen. Do not force-load or overwrite newer player edits. Native event-injection and long-soak tests must confirm real behavior.

## Ten-category rubric — historical scores carried, not newly graded

| Category | Historical /10 | Current unaccepted evidence |
|---|---:|---|
| Terrain realism | 5 | V8 general density/overhang visuals |
| Biome blending/coherence | 4 | Weighted ecotones |
| Hydrology and caves | 6 | Callback ownership, conservation/save, connected waterfall |
| Features/structures | 4 | Engine-wide spacing/exclusion pipeline |
| Performance/threading | 3 | Warm fill historically 14.45–14.99 ms vs 5 ms target; fresh host tails absent |
| Determinism/stability | 7 | Cross-machine replay and soak/edit |
| Architecture/code quality | 6 | Scoped native write ownership and current module build |
| Extensibility/data-driven | 4 | Complete stage/profile/API |
| Tests/benchmarks/docs | 7 | Native callbacks/Java25/host measurements |
| Polish/config/tooling | 4 | Owner-visible water/LOD/GPU |
| **Total — Tier 2** | **50/100 = 5.0/10** | Strictly **>8.5** and all water gates **NOT MET** |

[Original rubric](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/docs/engine-review-2026-10-24/REVIEW.md); [most recent prior independent audit](https://github.com/Shnewbs/Deep-Primal/blob/a14721c72625ea15ad9cb9168a6140c3f6882005/docs/engine-hourly-audits/2026-10-27T2231-227f53b/REVIEW.md). That dev38 report's receiver-write flaw was addressed at code/policy level in dev39; no new native qualification or external score increase.

## Execution/limitations

Production HydrologyRuntime blob `29072f52cef0ba70a199f7a8d83e3d4d4d61ca7f`. Pinned main, handoff/work-state, latest completed audit, original rubric, mixin and dev39 evidence inspected. Java21 model **11/11**; global suppression structural inspection. Java25/archive absent from the current workspace; shell GitHub clone failed DNS; authenticated GitHub connector supplied immutable pinned source. Under the owner's October 27 build override, **no Java25 Gradle, full Minecraft build, JAR, packaged CRC, client/server native, save/reopen, Windows/AMD, OpenGL/Vulkan, p50/p95/p99 or release performed**. No old binary promoted. Audit-only report committed via expected-parent guarded main ref if successful. Existing builder/auditor/renderer schedules and leases left untouched.

**Next:** builder should validate a per-cell internal-write suppression fix with native callbacks and restart-safe chemistry; next independent audit should recheck committed repair and owner-provided native receipts. Historical 50/100 must not be inflated or treated as a newly verified score.


<!-- END PRESERVED SOURCE -->
