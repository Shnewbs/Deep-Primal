# Expanded replica — docs/validation/engine-continuation/v8-ecotone-surface-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-ecotone-surface-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-ecotone-surface-2026-10-28/README.md |
| Preserved source SHA-256 | `e7f3bab23b26924a1f8042699ac357c137a4d3c4ee584818e23294d30d31865a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev56 saved-width ecotone preview using a 40-biome cover palette and deterministic absolute-coordinate patch fields while rejecting river and ocean domains. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the PREVIEW_R1 palette was unpersisted at this stage, so saved revision2 widths could not safely acquire those native outputs. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a preview-versus-runtime provenance boundary requiring a new saved policy before adopting palette or spatial-field changes. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Verify negative-coordinate floor operations and parallel sampling identity, reject legacy soft preview and hydrological domains, and keep new cover output unpromoted until its palette fingerprint is saved. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- V8 saved-policy-bound ecotone surface preview — 2026-10-28

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# V8 saved-policy-bound ecotone surface preview — 2026-10-28

## Source and audit identity
- Intended source: `0.0.1-alpha.29-dev.56-ecotone-surface.1`.
- Parent checkpoint: dev55 (the owner-reported playable JAR is still dev53).
- Last *completed* independent audit read: `docs/engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md`, pinned source `643ac7903f19b87626ee25f810bfb6f42cf7e0c9`. Independently accepted baseline **50/100 (5.0/10)**, not regraded here. Its UpscalingPolicyChecks FSR_31/FSR_41 oracle finding is renderer-owned, not modified by this builder.
- Prior source checkpoint for climate weights: `0f0121b5f8e4feda5ae65623ccec6cdecbfea30e`; persisted blend policy added in dev55.

## Implemented (explicit-call-only, not native placement)
- `EcotoneSurfaceField`: immutable revisioned 40-biome preview cover palette and seeded, quintic-interpolated absolute-coordinate patch fields (48- and 160-block cells). All normalized biome weights contribute to grass/podzol/bare distribution, snow fraction, vegetation density and soil factor; no dominant-biome switch or per-call RNG.
- `V8GeneratorSnapshot.Prepared.previewEcotoneSurface` consumes the *saved* V8 revision-2 policy widths and seed. Legacy revision-1 snapshots reject explicit soft preview and retain their existing six-field wire format and hard generator path.
- Negative signed coordinates use floorDiv/floorMod; chunk-local iteration order and parallel calls do not affect results. Hydrological RIVER/OCEAN domain is rejected to preserve authoritative water geometry and conservative physical water/chemistry.
- No new blocks, artwork or material registrations. `PREVIEW_R1` is an experimental **unpersisted cover palette** and MUST be pinned in a newer saved revision before any native activation. The already saved revision-2 widths must not silently acquire new native outputs.

## Focused local test receipts (NOT native acceptance)
- Host Java: OpenJDK **21.0.12.1** (`javac --release 21`). Owner-provided Java25 archive and standalone JDK25 not present in this isolated workspace.
- New Java source+V8 snapshot+tests compiled using *test-only Minecraft/Mojang/ecotone API doubles* from the earlier dev55 source-evidence fixture. The real `EcotoneBlend` producer was not recompiled locally, though its `sample/Weights/Climate/Domain` API was checked against current main.
- `EcotoneSurfaceFieldChecks`: **297,752** assertions passed, covering weighted fractions, deterministic spatial patches, signed coordinates, adjacent continuity, reverse/parallel replay, legacy V8 rejection, revision-2 policy call, and modeled codec reopen.
- `V8BlendPolicyChecks`: **53,316** assertions passed, including **4,096** version-2 modeled codec roundtrips.
- `V8SnapshotRootFieldChecks`: **14,392** assertions passed, including **4,122** invalid root envelopes rejected.
- An attempted unrelated `V8GeneratorSnapshotChecks` local compile failed because the limited fixture did not provide the native materializer class. Its full test was **not** claimed as passing; no Gradle/native producer was run.
- No Windows AMD8060S, real Mojang JSON/NBT, actual save/reopen, NeoForge, full Java25/Gradle, OpenGL, Vulkan, wall-clock benchmark or production JAR validation was performed.

## Explicit remaining gates
1. Preserve profile-specific, persisted **palette** and climate field provenance before consuming this preview inside native V8 surface generation. Avoid retroactively rewriting saved revision-1/2 outputs.
2. Reconcile true native soil/snow/vegetation placement with approved block states, final heightmaps, callback rollback and signed chunk seams; test reversed/parallel chunk writes, saved-policy reopen, edits and old/new border joins.
3. Implement versioned landform/relief blending **before hydrology**, preserving cliffs, geology, canyons and river/lake connections. Native multi-seed visual transects and CPU/memory measurements remain open.
4. Water gates remain separate: conservative loaded/unloaded ownership, chemistry and metered source accounting, long soak, and temporally connected Vulkan/OpenGL waterfall/rock-contact surfaces. Foreground water ownership was not verified live; this cycle did not edit water-owned files.
5. Renderer owns the audited FSR test-oracle repair. The independent auditor owns the next ten-category grade.

No full build/JAR/release was attempted under the owner's 2026-10-27 owner-operated build instruction.


<!-- END PRESERVED SOURCE -->
