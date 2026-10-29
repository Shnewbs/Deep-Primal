# Expanded replica — docs/validation/engine-continuation/v8-saved-profile-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-saved-profile-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-saved-profile-2026-10-28/README.md |
| Preserved source SHA-256 | `27819395f47e16ffe5bef08a890a5f17d9bf90c6daea3f9387bec0b2c8973401` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev49 immutable explicit V8 snapshots saving the full density and ground definitions, signed 64-bit seed and vertical span with privately constructed prepared state. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that test-only codec round trips are not Minecraft saved-world round trips, and a strict value schema alone did not yet reject unknown root keys. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a world-generation snapshot contract tying all prepared components to one validated immutable envelope and clearly versioned authoring route. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject mismatched graph, channel, seed and height combinations, replay exact voxels from serialized definitions and then verify real Mojang codecs and native save/reopen identity. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev49 — V8 immutable saved-profile envelope (source-only)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev49 — V8 immutable saved-profile envelope (source-only)

**Version:** `0.0.1-alpha.29-dev.49-v8snapshot.1`. Consumed external audit `docs/engine-hourly-audits/2026-10-28T0731-5a23f28/REVIEW.md`, pinned `5a23f285d4680f9537b8baf1639b75036e7392ef`. The newly identified stale persisted-LOD discard is renderer-owned, so this engine cycle changes no renderer/LOD code. Last full independently accepted grade remains **50/100 (5.0/10)**.

## Source changes

- `V8GeneratorSnapshot` is an immutable *explicit-only* full V8 snapshot with a strict `Codec` envelope: `version`, decimal-string `seed` (entire signed 64-bit range), `min_y`, `height`, `density` graph definition, and `ground` surface definition. Invalid version, graph, channel, seed and vertical bounds are rejected. `Prepared` has a private constructor to prevent combining unrelated world profile/graph data, and can be compiled once per saved generator instead of every chunk.
- `DensityChunkMaterializer.fillSavedSnapshot` accepts that prepared profile and enforces saved minY and height **before any block or heightmap write**, then delegates to the existing exact planned soil/stone path. No V8 chunk-generator registration, automatic world migration or V1–V7 geography changes.
- `gradle.properties` is bumped to the uniquely named dev49 source version; no binary was built.

## Focused tests (not native Minecraft)

Eclipse Temurin **25.0.5+8**, `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all`, with isolated test-only Minecraft and Mojang-codec API doubles: `V8_SAVED_PROFILE_PASS` **594,901 assertions / 116,736 voxel parity / 384 seed replay comparisons**, plus retained planned ground **1,235,414**, base height **874,642**, and section bridge **11,047,019** assertions. Aggregate **13,751,976**. Five deliberately broken source variants were separately compiled and rejected (height mismatch bypass, seed truncation, missing material channel, forged prepared owner, incorrect compiled seed). Existing production density classes were copied unchanged from the validated dev48 inputs; changed source SHA-1 blobs are recorded in the cycle evidence bundle.

**Not run/accepted:** actual Mojang JSON/NBT codec, native 26.1.2 generator/save/reopen, real heightmap/lighting/block placement, frozen V1–V7 world replay, full Java25 Gradle, Vulkan/OpenGL, owner Windows AMD8060S, finite-water ledger/chemistry and loaded/unloaded/multiplayer soak, JAR or release. The owner retains all full builds under the October 27 override. Codec stand-in roundtrip is **not** a saved Minecraft world roundtrip.

Next owner gate: actual V8 snapshot serializer and native heightmap/lighting/reopen parity. Builder next: reconcile V8 registered profile lifecycle after native qualification and finite-water unloaded-owner ledger. No completion/regrade claim.


<!-- END PRESERVED SOURCE -->
