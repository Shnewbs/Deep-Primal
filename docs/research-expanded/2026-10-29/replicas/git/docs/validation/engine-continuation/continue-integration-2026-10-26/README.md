# Expanded replica — docs/validation/engine-continuation/continue-integration-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/continue-integration-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/continue-integration-2026-10-26/README.md |
| Preserved source SHA-256 | `5e383ff5032fb874863c6c05650611287637a5d54f8459dcf256f5e3a1c993a3` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The dev23 integration that moved formerly broken Continue additions into normal production source sets and the existing mod lifecycle without a prototype exclusion. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that that checkpoint still scaled UI with spatial upscaling and supplied query meshes without player edits or materials; later dev32 claims must be dated separately. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a capability chronology separating query services, authoritative world generation and renderer integration, with host lifecycle and module-boundary ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compile the normal source sets, verify lifecycle attachment and private fixture exclusion, and compare later scene-only upscaling evidence without relabeling this earlier implementation as current. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Continue integration and branch merge - alpha.29-dev.23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Continue integration and branch merge - alpha.29-dev.23

The Continue additions in main failed compilation and mixed platform code into the
standalone core. They are now part of normal production source sets, connected to
the existing mod lifecycle and configurations. There is no excluded prototype
source set. See [ENGINE_CORE_SUMMARY.md](../../../../ENGINE_CORE_SUMMARY.md).

## Source preservation

- Main code baseline: `905e550174e32f9d25d4e01e4e398bb6712288b2`.
- Publication parent: `c6b7ac4f5d78836ca7e5752a25fc40955e6128c6`, which adds only the newer
  audit diagnostics for the old broken code; all 28 files are retained. Its provisional
  partial score is 4.2/10, with the last complete baseline still 5.0/10. This repair
  does not assign a new grade.
- Merged audit branch: `audit/20261025T2349-7aa3ae3`,
  `7ece93ef3d3ba441736bb678061d61c13bb47f9e`.
- All 52 audit files retain their original Git subtree
  `5955fd0ac657395cf3f3095377977513efb6e3a1`.
- The owner's local correction `c283bd5` is equivalent to the later main rename;
  its original checkout was not modified. Work used a separate writable checkout.
- Newer coordination validator/history from main is retained. No schedules,
  frozen V1-V7 terrain, channel-5 opt-in, approved art or existing world-water
  accounting were replaced. The historical independent grade remains 5.0/10.

## Implemented behavior

The existing mod owns one query service per engine dimension, reusing its saved
terrain planner and vertical range. Core queries provide bounded/coalesced CPU
materialization, real exposed voxel faces, terrain raycasts and a closed integer
water query grid. Shutdown cancels pending queries; external executors retain
caller ownership. Caches enforce byte/entry caps and dimension-safe identity.
Metrics use the existing dashboard; validation uses server ticks.

Duplicate terrain/hydrology/hazard declarations are consolidated with the existing
registered implementations. Tests move out of production. Vulkan has a real
linear presentation pass and opt-in whole-frame spatial scaling, defaulting to
native resolution. Invalid placeholder shaders are removed. FSR, DLSS and frame
generation requests fall back to native because no vendor provider is installed.

## Build environment and evidence

Java 25.0.4.1, Gradle 9.2.1, Minecraft 26.1.2, NeoForge 26.1.2.122, Windows.
Normal user command: `gradlew.bat build -PvulkanPlatform=windows`.

This restricted session could not create Gradle/NeoForm loopback sockets. The
verification uses an isolated copied dependency cache, Gradle's in-process CLI,
command-line javac, a workspace temporary directory, and the already generated
matching Minecraft patched artifacts. `createMinecraftArtifacts` is excluded;
this is not a fresh online dependency-bootstrap test. All changed product code is
compiled, and normal core/module/resource/cache gates run. The local init script
changes compiler/process setup only; it does not exclude sources or assertions.

`checks.json`, `build-output.txt`, `package.json` and `changed-source-sha256.json`
record completed results. Archive checks cover outer/nested CRCs, nested module
metadata, Windows-only native payloads, fixture exclusion, required integrated
classes and byte identity against every project module's compiler outputs.
Themes retains its independent version 0.1.0-alpha.8.

## Limits

This is a development build, not a stable release or a new engine grade. No new
Minecraft gameplay/save/reopen, OpenGL/Vulkan GPU, owner-hardware, performance/FPS
or long-soak acceptance is claimed. Spatial scaling also scales the UI. CPU query
meshes are not Minecraft block models and do not include player edits, textures,
vegetation or deposits. Existing host generation/rendering/water systems remain
responsible for those behaviors. Vendor upscalers, frame generation and the draft's
unsupported earthquake/volcano/landslide switches are not implemented features.


<!-- END PRESERVED SOURCE -->
