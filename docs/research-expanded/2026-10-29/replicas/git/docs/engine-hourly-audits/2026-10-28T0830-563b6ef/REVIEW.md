# Expanded replica — docs/engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md |
| Preserved source SHA-256 | `512f95c679494e08c001ecb70ee56188009d720a323ffb3ae512a8e7c9ad3332` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent engine audit — October 28, 2026, 08:30 UTC”. Its original opening states: “Pinned audited source: 563b6ef23586f579ff2b972d7404b4f0a9054002, version 0.0.1-alpha.29-dev.49-v8snapshot.1. Dev49 implementation was committed at ffe9bf42f27b0d3b295cc97403f784a05aff7c01; the pinned head includes its completed source-only work-state checkpoint. This is a separate read-only auditor review: production, Builder and renderer code/checkouts/caches/leases remained unchanged. No active exclusive performance reservation appeared in the pinned work state. The latest 07:31 UTC external audit was consumed by the Builder.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `RecordCodecBuilder`, `DensityGraphCodec`, `GroundSurfaceProfile`.

## Research expansion — gaps and details to deepen

- Convert the persisted-LOD finding into a recovery state machine that remains correct for stationary cameras, empty results, retries, and stale completion.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Track request epoch, recovery epoch, cursor ownership, retry reason, and next eligible retry independently of camera movement.
- Document mutable-versus-immutable result ownership and bounded incremental publication; old scans cannot consume a newer recovery request.

## Research expansion — acceptance checks

- Inject an I/O error followed by recovery without camera movement, and create new persisted data after a successful empty scan.
- Complete an old scan after a reset or newer request; verify no duplicate admission, lost wakeup, wrong-world publication, or unbounded retry spin.

## Research expansion — source anchors

- Deep Primal independent engine audit — October 28, 2026, 08:30 UTC
- Current scoped findings
- Build policy, scope and handoff

Related design: [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent engine audit — October 28, 2026, 08:30 UTC

**Pinned audited source:** [563b6ef23586f579ff2b972d7404b4f0a9054002](https://github.com/Shnewbs/Deep-Primal/commit/563b6ef23586f579ff2b972d7404b4f0a9054002), version `0.0.1-alpha.29-dev.49-v8snapshot.1`. Dev49 implementation was committed at `ffe9bf42f27b0d3b295cc97403f784a05aff7c01`; the pinned head includes its completed source-only work-state checkpoint. This is a separate read-only auditor review: production, Builder and renderer code/checkouts/caches/leases remained unchanged. No active exclusive performance reservation appeared in the pinned work state. The latest 07:31 UTC external audit was consumed by the Builder.

## Current scoped findings

**V8 snapshot root-envelope strictness: source-level risk, real Mojang codec still untested.** The [new serializer L33–50](https://github.com/Shnewbs/Deep-Primal/blob/563b6ef23586f579ff2b972d7404b4f0a9054002/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L33-L50) calls `RecordCodecBuilder` for the six required fields, without explicit rejection of unexpected top-level keys, unlike the strict `DensityGraphCodec` and `GroundSurfaceProfile` decoders. An independent `javac --release 25` test using the exact production V8 source and the project's Mojang codec stand-in accepted an otherwise valid record with an unexpected misspelled `heigth` field; omitting the correctly spelled required `height` failed. Four scoped assertions passed as an *observation of an omission*, not a native codec acceptance or world-corruption claim. Add a bounded top-level allowlist if strictness is intended, and test with actual Mojang JSON/NBT before registering V8.

**Persisted LOD page rediscovery: previously documented gap, independently reproduced.** [LodDiskIndexService L83–134](https://github.com/Shnewbs/Deep-Primal/blob/563b6ef23586f579ff2b972d7404b4f0a9054002/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L83-L134) retries missing/failed and successful-empty directory scans, but clears retries after a successful nonempty scan. Using the exact committed service/scanner and a real temporary directory, a saved `100_0.plod` page loaded; a subsequently created nearer `0_0.plod` stayed undiscovered through **600 stationary-camera ticks**, then was found immediately after reset. The new Java25 diagnostic passed 1,815 observations while confirming this failure mode. It was **already documented as open** in [the renderer's recovery-epochs jam](https://github.com/Shnewbs/Deep-Primal/blob/563b6ef23586f579ff2b972d7404b4f0a9054002/docs/renderer-jams/20261028-recovery-epochs/README.md); this is an independent confirmation, not a newly found defect or native Minecraft loss. The older post-snapshot stale-renewal discard is now repaired in the tested service.

## Independent focused Java25 checks

Isolated Linux x86_64 / AMD EPYC 9V74 audit directory. Temurin **25.0.5+8** reused read-only from a previous auditor-owned toolchain; independent classes/outputs compiled with `javac --release 25 -Xlint:all -Werror`, executed with `java -ea -Xverify:all`. Git object hashes of 13 copied production files (nine dependency-free V8 core + two native-shaped V8 adapters + LOD service + scanner) match this pinned GitHub snapshot.

| Check | Passing assertions / observations |
| --- | ---: |
| Dev49 V8 saved snapshot (API doubles) | 594,901 |
| V8 base-height bridge (API doubles) | 874,642 |
| V8 planned ground/materials (API doubles) | 1,235,414 |
| V8 section/materialization bridge (API doubles) | 11,047,019 |
| New 512-seed signed-long replay / 6,144 noise samples (API doubles) | 9,216 |
| New unexpected-field stand-in diagnostic | 4 |
| Existing LOD recovery/service/nearest/incremental/retry (real filesystem/JDK) | 22,853 |
| New successful-nonempty directory refresh diagnostic | 1,815 |
| **Total** | **13,785,864** |

All final isolated compilations and test processes exited zero. The fuzz fixture's first draft had an unreachable graph node and failed before its subject test; the test-only graph was corrected and final run passed, with that rejected attempt noted in evidence. Permission-denial testing was skipped under a privileged Linux process. V8 and saved-codec checks use **API doubles**; these are not actual native Minecraft/Mojang JSON/NBT, a gameplay benchmark, Windows/AMD8060S, or Vulkan/OpenGL GPU validation.

## Ten-category grade (carried, not a fresh complete regrade)

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome blending | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Overall** | **50/100 — average 5.0/10 — Tier 2** |

These are the evidence-supported [October 24 full rubric](https://github.com/Shnewbs/Deep-Primal/blob/563b6ef23586f579ff2b972d7404b4f0a9054002/docs/engine-review-2026-10-24/REVIEW.md) scores **carried forward**, not a new score. Experimental V8 paths do not earn deployed-gameplay/performance points. The required independent average is **strictly greater than 8.5/10**; it is not demonstrated.

## Build policy, scope and handoff

Under the owner's **October 27 latest override**, complete Gradle/Minecraft/GPU/JAR builds belong to the owner. Thus **no dev49 JAR was produced** and no full Gradle, native chunk creation/save/reopen/shutdown, actual Mojang codec, nested archive CRC/source-package identities, Windows/AMD8060S, Vulkan/GL image or FPS/performance acceptance was run. No hosted Actions, GitHub/CurseForge release or old-JAR deletion was attempted. Last independently recorded qualified Windows bundle was dev31 from `a1583a508b0d67130d387bc018defd5758acd496`, **not** the audited dev49 commit. Frozen V1–V7 generation, saved player edits, finite liquid quantities and module boundaries were untouched.

Priorities: native V8 snapshot JSON/NBT/unknown-key checks and saved/reopen profile/heightmap/lighting/legacy parity; native water loaded/unloaded conservation/chemistry; renderer-owned bounded nonempty directory discovery and matched visible-world Vulkan/GL LOD/water quality. Do not gate or stop Builder progress for this audit.


<!-- END PRESERVED SOURCE -->
