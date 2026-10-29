# Expanded replica — docs/validation/engine-continuation/v8-ocean-interior-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-ocean-interior-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-ocean-interior-2026-10-29/README.md |
| Preserved source SHA-256 | `66677d5ccdb60669bcb2afe879b0167fbd234f8273030b23a7677f5e56664baf` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This Dev73 receipt adds opt-in routing policy v6 with an ocean-domain predicate applied to every traversed below-sea lattice cell, preventing graph-denied wet interiors from inheriting marine staging. It also reports replacing a thread-affine shared density sampler with a bounded four-worker sampler cache after a parallel source-model exception. Prior v1–v5 identities and legacy traversal remain preserved. Executed evidence comprises Java21/API-double suites, split-batch completion records, and rejected mutations. The changed real WatershedRegion dependency was not compiled, and authored-domain correctness, native reopening, global hydraulics, and GPU acceptance remain open.

## Research expansion — gaps and details to deepen

- Actual limitation: interior masking prevents one propagation error only when the authored domain is correct. It does not independently distinguish a marine basin from a wrongly negative enclosed lake.
- Elaboration: per-worker sampler residency needs explicit thread ownership, eviction lifetime, and world/profile invalidation rules. A four-entry cap must not hand an evicted mutable sampler to a different worker.
- Actual integration gap: fixtures execute the routed adapter with a watershed double, so the real every-interior-cell predicate and legacy null-constructor parity remain unqualified.
- Evidence nuance: two completed batches and rerun remainder suites are intentionally not a single uninterrupted run. Their identities and failed/timed-out attempts must remain separate.

## Research expansion — proposed contracts

- Specify boundary seed admission and interior traversal eligibility independently, both stamped by the same saved domain/profile. Reject a path as soon as a required interior cell is denied or unavailable.
- Give each sampler one worker owner and immutable graph identity; use safe reconstruction after cache eviction, reject cross-world reuse, and keep in-flight ownership alive until its query completes.
- Add a production WatershedRegion gate proving that the optional interior predicate is evaluated at every relevant traversal step while null preserves prior profiles.
- Carry per-batch source, fixture, completion, and attempt identity into the receipt index. Aggregate assertions only across identified completed runs and retain the timeout as a development attempt.
- Keep global marine certification and finite-water authority separate from graph-domain admission, even after the interior check passes.

## Research expansion — acceptance checks

- Build a marine boundary surrounding a graph-denied wet interior and verify the real planner does not propagate sea staging through it.
- Run more than four worker identities with repeated eviction, parallel queries, world changes, and graph changes; reject any thread-affinity violation or stale sampler reuse.
- Use deliberately incorrect masks to demonstrate that interior closure is not advertised as proof of global ocean topology.
- Compare null/v1–v5 legacy outputs with opt-in v6 under genuine codec reopen and actual production dependency compilation.
- Verify every reported batch has terminal status and source identity before counting its assertions toward a claim.

## Research expansion — source anchors

- Production changes (opt-in saved V8 routing v6)
- Executed source-only checks
- Missing acceptance gates and next implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev73 — explicitly closed saved-ocean interiors

Source version: `0.0.1-alpha.29-dev.73-closed-ocean-interior.1`. Owner-operated full builds remain in effect; **no JAR, native gameplay, release, GPU or grade** is asserted by this source checkpoint.

## Latest external audit consumed

`docs/engine-hourly-audits/2026-10-29T0634-84dda6f/REVIEW.md`, pinned `84dda6fb293647482b560c3a13cb47a98707e8d0`, confirms that dev72's graph-authored `ocean_domain` is provenance, **not** a globally proven marine/outlet topology. Last full external score remains **50/100 = 5.0/10**. Its source-only Java25 renderer tests were not engine/native validation.

## Production changes (opt-in saved V8 routing v6)

- `V8RoutedWaterSource` adds `authorClosedOceanDomain(revision4)`, a new domain-separated `closedOceanDomainFingerprint`, and reopening only a matching revision-5 saved profile. The earlier v1–v5 routing paths and their serialized identities are preserved. A v6 source samples graph-authored signed `ocean_domain` at each absolute 32-block lattice position, checks finite height/mask values, admits boundary seeds only inside the negative ocean domain and **never propagates ocean into graph-denied wet interiors**.
- The actual production `WatershedRegion` receives an optional second ocean predicate in an overloaded constructor. The original constructor delegates with `null`, preserving prior V1–V7 and saved V8 v1–v5 flood traversal. The v6 predicate runs on every traversed below-sea lattice cell, not just the outer edge. This protects correctly-authored inland basins from false sea staging.
- `V8OceanConnectivity` previously captured one thread-owned `DensityGraph.Sampler` in its constructor. Parallel queries from later worker threads reproduced `IllegalStateException: Density sampler changed job thread`. The source now caches at most **four** per-worker graph samplers, reconstructing a sampler on worker/cache eviction, with no random state or versioned graph changes.
- No V1–V7 geography, prior V8 authoring policy, material/texture files, finite-water quantity, chemical stores, player edits, GPU/LOD/rendering files or old-save migration was changed.

This does **not** generate globally correct ocean topology: a wrongly authored negative `ocean_domain` can still flood a closed lake. Cross-region receivers, lake spill, discharge and native chunk-border behavior remain distinct unfinished gates.

## Executed source-only checks

OpenJDK/javac **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`. **19/19** focused source/API-double Java21 suites passed across two completed batches (**21,013,331** fixture assertions/checks, counted from individual receipts), including 4,904 new routed checks and 737 marine-connectivity checks. The same model-compiled saved profile is reopened; positive/negative signed coordinates, reversed order, parallel worker replay, no old-v5 automatic migration, invalid seed/profile rejection, mask-edit fingerprints and bounded four-worker sampler residency are exercised. The separate legacy `tools/test_ecotone_blend.py` runner was not available in the partial source-only working set and was not run.

Three deliberately incorrect implementations compiled and were **rejected**: reversing the ocean mask, bypassing the new interior predicate, and reinstating a single shared thread-affine sampler. The last fails the newly added parallel replay with the reproduced `Density sampler changed job thread` exception. These rejects are evidence of regression coverage, not passing implementations. An earlier combined test invocation timed out after fourteen suites; its remaining five suites and the core ecotone-package suite were explicitly rerun successfully, so no uncompleted batch is labeled a single full run.

Exact published/tested Java Git blob identities:
- `V8RoutedWaterSource.java`: `2a19635c511403baf6eea40e60f084aa7abc146c`
- `V8OceanConnectivity.java`: `14d58488fb7591d8a977858d7d09eea4f79b123b`
- `V8RoutedWaterSourceChecks.java`: `dff4ecbe847124bb57cf81516b9c3fde75e5fe5c`
- `WatershedRegion.java`: `3066eac9ecae5fc5979bf1c3b3b663b06ee56fc0`
- `gradle.properties`: `fa420af03c38239b393aa8b315e6e65c80257dfa`

## Missing acceptance gates and next implementation

The source-only suite compiled the routed production adapter against a `WatershedRegion` API double, **not** the actual changed production watershed dependency graph. Mojang codecs/save/reopen were modeled, not native. Complete pinned checkout/root `tools/validate_engine_work_state.py`, Java25/Gradle9.2.1/NeoForge, real Minecraft, old/new-generation borders, geometry/ecotone visuals, native finite-water/constituent conservation, Vulkan/OpenGL and hardware performance were not exercised. The root work-state remains at Dev53, unchanged. Owner handles full builds and publication; last owner-confirmed playable remains dev53.

Next: derive a proven globally connected marine/outlet graph for the saved `ocean_domain`, connect true cross-region priority-flood receivers/river discharge and lake spills, and run the real production watershed native checks. Continue weighted surface/vegetation and versioned landform blending alongside finite-water restart/loaded-unloaded reconciliation. No independent grade is changed; completion still needs a fresh complete grade **strictly >8.5/10** plus all water gates.


<!-- END PRESERVED SOURCE -->
