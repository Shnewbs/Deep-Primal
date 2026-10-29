# Expanded replica — docs/engine-hourly-audits/2026-10-28T1726-182cdce/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T1726-182cdce/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T1726-182cdce/REVIEW.md |
| Preserved source SHA-256 | `5441672a91d7095dd3712c554f44c4c9cddc6722dd3ce271ba3639b319908ea8` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent Dev59 engine audit — 2026-10-28 17:26 UTC”. Its original opening states: “Pinned source: 182cdce92eaed2eb371af4066b43b5dc45f61908; version: 0.0.1-alpha.29-dev.59-ecotone-query.1 (pinned gradle.properties). Read-only auditor; no production source, Builder checkout/lease/cache, renderer checkout or work-state file changed. New authoring/report below is audit documentation only. The auditor did not issue a new development JAR because the owner's 2026-10-27 override assigns full Gradle/Minecraft/GPU builds to the owner.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityChunkMaterializer.java`, `VoxelLodRenderer.java`, `Long.MAX_VALUE`, `V8EcotoneColumnPlan.java`.

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

- Independent Dev59 engine audit — 2026-10-28 17:26 UTC
- New verified work (code changes belong to Builder and renderer, NOT auditor)
- Outstanding defects/gates and next owner priorities

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent Dev59 engine audit — 2026-10-28 17:26 UTC

**Pinned source:** [`182cdce92eaed2eb371af4066b43b5dc45f61908`](https://github.com/Shnewbs/Deep-Primal/commit/182cdce92eaed2eb371af4066b43b5dc45f61908); **version:** `0.0.1-alpha.29-dev.59-ecotone-query.1` (pinned `gradle.properties`). Read-only auditor; no production source, Builder checkout/lease/cache, renderer checkout or work-state file changed. New authoring/report below is audit documentation only. The auditor did not issue a new development JAR because the owner's 2026-10-27 override assigns full Gradle/Minecraft/GPU builds to the owner.

## New verified work (code changes belong to Builder and renderer, NOT auditor)
- **Builder Dev59:** [`DensityChunkMaterializer.java` L186–285](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L186-L285) introduces explicit opt-in pre-generation `planSavedEcotone` and reuses its density/cover plan for `fillSavedEcotone`. Snow-covered columns no longer resolve an unrelated non-snow material when cached snow is nonnull (L204–224). It remains unregistered as the native saved-world generator. [Builder receipt](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/docs/validation/engine-continuation/v8-ecotone-prewrite-query-2026-10-28/README.md) reports 11/11 focused Java21 double suites / 14,177,063 checks and a dev58 negative control. These are **Builder-reported**, not the auditor's independently repeated totals.
- **Renderer:** [`VoxelLodRenderer.java` L618–751](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L618-L751) clears aborted partial GPU upload ownership, accounts accepted buffer bytes before fallible telemetry, and transfers completed-page ownership before closing the prior page. Native GPU testing remains absent.

## Independent pinned-source checks
- Exact production `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuUploadFailure.java` (Git blob `ca79e0b1e8bb69b861077f63872e6fcbd6244841`) was strict-compiled under Java 21 with an **independent** test class. **1,000,011 assertions passed**: deterministic random nonnegative byte ledgers, release-once, original error/cleanup suppression identity, malformed accounting, and `Long.MAX_VALUE` boundaries. Removing the release operation in an isolated mutant failed at independent assertion 2.
- Exact production [`V8EcotoneColumnPlan.java`](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/src/main/java/com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java) (Git blob `89b9e298b7d246fa1ba40990138c7a62c0fd1a7c`) was strict-compiled against **clearly labeled, auditor-authored API doubles**. **3,599,039 assertions passed** across 3,000 deterministic randomized 16×16 chunks, signed edge coordinates, absolute callback coordinates, height/land-domain exclusions, soil depth and boundary checks. Removing the absolute X offset in an isolated mutant failed at independent assertion 519. One initial negative-boundary test expected the wrong Java exception class and was corrected in test-only code; the production file was untouched.
- **Independent total:** 4,599,050 passing assertions; both negative controls rejected. Commands: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. Linux OpenJDK/Javac 21.0.12.1. The source archive's 16 entries passed ZIP CRC and SHA256 `26f52f63bd86895082098db5045c2180cadcd7f3ba6a9e472125a5ec610c8677`. Library path: `/Deep Primal/Independent Audits/DeepPrimal_Dev59_IndependentAudit_20261028T1726_182cdce_SourceEvidence.zip`.
- **Static source review only:** [`planSavedEcotone` L269–285](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L269-L285) checks saved surface revision, saved world height, chunk alignment, then density and ecotone planning before native writes. The 586-line materializer was **not** compiled in this independent harness; the current test does **not** establish real native prewrite/query/materialized parity. Existing Dev59 builder source-level integration is reported separately above.

## Equal-weight ten-category rubric — historical full grade CARRIED, not a fresh complete regrade
The last accepted full [review](../../engine-review-2026-10-24/REVIEW.md) is **50/100, 5.0/10, Tier 2**. This audit verified two narrow source-level areas, not enough to establish a new whole-engine score. Sources below are pinned to `182cdce92eae` unless explicitly historical.

| Category | /10 carried | Relevant evidence and current unproven gates |
| --- | ---: | --- |
| Terrain realism and variety | 5 | [Dev59 V8 pre-generation API](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L269-L285), opt-in; no native relief/artistic variety and overhang acceptance |
| Biome blending and coherence | 4 | [V8 ecotone column planner](https://github.com/Shnewbs/Deep-Primal/blob/182cdce92eaed2eb371af4066b43b5dc45f61908/src/main/java/com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java#L52-L89) tested with doubles; no saved-authoritative climate provider or native world transects |
| Hydrology and cave quality | 6 | Historical V7 connected drainage and finite-water tests; no new native conservation, cave morphology or unloaded/restart acceptance |
| Features and structure placement | 4 | Cover/vegetation preview is read-only; no native structure spacing/exclusion and feature ownership acceptance |
| Performance and threading | 3 | Historical full review measured ~14–15 ms warm fill vs 5 ms target; current V8/LOD modifications have no comparable native benchmark. Owner's >300 FPS creative flight is observational |
| Determinism and stability | 7 | Historical frozen V1–V7 digests; exact-source randomized signed V8 column tests passed, but native replay/reopen and cross-machine identity unrun |
| Architecture and code quality | 6 | Separated core/LOD/graphics/Vulkan modules; V8 is explicit-call-only and does not yet provide registered generator or stable lifecycle |
| Extensibility and data-driven design | 4 | Versioned prepared snapshot/cover plan; full dynamic terrain/structure lifecycle and external API remain incomplete |
| Testing, benchmarks, documentation | 7 | New 4,599,050 independent focused Java21 assertions and mutation controls; Java25, full Minecraft and GPU acceptance unrun |
| Polish: configuration, errors, tooling | 4 | Safe LOD upload error cleanup source; user-facing temporal FSR 3/4 are preferences without functional providers; Windows acceptance unrun |
| **Total, historical carried** | **50/100** | **Average 5.0/10; Tier 2. No new grade awarded.** |

## Outstanding defects/gates and next owner priorities
1. **Native V8 lifecycle:** bind an immutable, saved-authoritative absolute-coordinate climate/domain producer; test before-generation base-column, materialized blocks, heightmaps and save/reopen on real NeoForge using signed chunk seams. Existing work remains opt-in. Preserve V1–V7 output, saved profiles and player edits.
2. **Finite water:** single-owner loaded/unloaded boundary transfer, mass/salt/sediment conservation, real waterfalls and long-soak/restart. Never grant hydrology score credit for surface-color prototypes.
3. **Renderer fault/latency:** force a partial GPU upload failure and close failure on Windows/AMD8060S Vulkan and OpenGL; collect p50/p95/p99 frame times, VRAM, LOD visible coverage, FSR1/temporal provider status and water alpha/edge readbacks. Source rollback tests are not GPU success.
4. **Full package:** owner-operated Java25/Gradle9.2.1/NeoForge, complete Themes/notices/Windows Vulkan+OpenGL nested JAR CRC/source-class identity, fixture exclusion and real native save/reopen/shutdown. These were **not run** by auditor; no JAR built, accepted, published, or deleted.
5. **Coordination:** pinned root `docs/ENGINE-WORK-STATE.json` remains checkpointed at 12:04 UTC and references older dev53 source. The scoped terrain state is released at 17:13 UTC and identifies committed Dev59. Neither was modified. No active timed GPU reservation was observed in these scoped receipts. Re-check other owners on future cycles.

The strictly greater-than-8.5 target is **not** met; continue the existing independent audit. No source conflicts were forced and no claims of continuous work between runs are made.


<!-- END PRESERVED SOURCE -->
