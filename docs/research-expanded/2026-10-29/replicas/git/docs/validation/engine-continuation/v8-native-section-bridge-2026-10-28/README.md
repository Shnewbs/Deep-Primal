# Expanded replica — docs/validation/engine-continuation/v8-native-section-bridge-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-native-section-bridge-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-native-section-bridge-2026-10-28/README.md |
| Preserved source SHA-256 | `739145c0afb6d17cfdbd14c61cfc91854a781d31cdff8e2e92a448fcb6a537cb` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev46 explicit native-section materialization consuming exact planned occupancy in signed-Y sections, writing air and bedrock and priming both final generation heightmaps. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the method was not called by EngineChunkGenerator, codecs or presets, and conservative full-cell writes were not a demonstrated speedup. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an adapter activation table separating compiled methods, explicit callers, registry wiring and qualified saved-world behavior. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Materialize into stale-filled chunks across negative and partial sections, require air to clear old blocks and compare both heightmaps before enabling a versioned generator. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev46 — Opt-in V8 native section materializer

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev46 — Opt-in V8 native section materializer

Version: `0.0.1-alpha.29-dev.46-v8native.1`. Source-only integration, not a qualified Minecraft build or release.

## Independent audit consumed and coordination

Read `docs/engine-hourly-audits/2026-10-28T0432-09a57c1/REVIEW.md`, independently pinned at `09a57c1896eb1429b2af83223d4a3d6bd5ad8724`. Last *complete* external score remains **50/100 (5.0/10), Tier 2**, not freshly regraded. Dev45's section projection proved native palette packing only; the source audit found that production materialization did not consume it. The builder claimed the bounded main lease, checked Git/automation and other-chat evidence, preserved concurrent renderer `lod-engine` and unrelated water work, and did not touch shared build output.

## Code implemented

`DensityChunkMaterializer.fillPlanned(DensityGraph, ChunkAccess, int)` now provides an explicit opt-in bridge: it samples exact/planned V8 occupancy, consumes `DensitySectionLayout.project` by signed-Y 16-cubed section, writes every covered X/Z/Y cell in canonical native palette order (including air and bedrock floor), primes both WORLD_SURFACE_WG and OCEAN_FLOOR_WG after writes, and returns density-call statistics. Its existing `fill` and `baseColumn` remain unchanged. Neither `EngineChunkGenerator` nor CODEC/presets call this method, so existing V1–V7 gameplay and saved geography are not modified and V8 is not enabled.

All-air sections deliberately still write air when this path is called: skipping these writes can leave stale blocks on reused test chunks. Section indexing is deterministic across negative-Y and partial section boundaries. This is a conservative functional bridge, **not a demonstrated speedup** or accepted native level writer.

## Tests executed and explicitly excluded

Verified the pre-edit adapter was Git blob `c7e2389356432ebcf765fdab24efbb707008d295`, and all nine dependency-free product graph/section sources match current Git blob hashes. The exact new adapter was compiled with those sources against ten test-only API stand-ins (not Mojang/NeoForge implementation classes). Linux Java **21.0.12.1** and copied, independently verified Temurin **25.0.5+8** each pass `javac --release {21,25} -Xlint:all -Werror` plus `java -ea -Xverify:all`, with identical results:

`DENSITY_NATIVE_BRIDGE_PASS assertions=11047019 nativePositions=1376512 baseColumnChecks=1376512 scenarios=34 certifiedCells=1236480 apiDoubles=true nativeMinecraft=false`

Cases cover top-exclusive density/heightmaps, full 2048-high all-air/all-solid, signed/partial section Y, cave/roof/floating/noise real graph, each classification budget, base-column/native material identity, bedrock even for density air, clearing preexisting blocks, exact once-per-cell writes and Y/Z/X order, and invalid-input no mutation. Five compiled faulty variants were rejected at runtime: X/Z transposition, dropped solids, skipping air clears, stale heightmap priming, and signed local-Y offset. See compact results in `checks.json` and the owner-accessible dev46 source/test evidence ZIP.

**Unrun:** Full Java25 Gradle 9.2.1/NeoForge modules, actual Minecraft chunk write/save/reopen/lighting, selected JAR, nested archive/source checks, native GPU Vulkan/OpenGL, Windows AMD8060S, multiplayer/long-soak and matched chunk p50/p95/p99 allocation/performance. No GitHub Actions or build/release invoked, per owner's October 27 full-build override. Java25 test-facade compilation is not native API compatibility proof.

**Next:** Owner's actual Java25 build and selected NeoForge V8 section/heightmap/base-column/native block/save-profile/reopen parity; only then consider registered V8 codec/preset. Keep loaded/unloaded finite-water ledger, native callback chemistry, seasonal/lake/ocean exchange, connected cliff temporal GPU and named-hardware acceptance open. Preserve frozen V1–V7 and all owner art. Builder completion still requires a *new* independent >8.5/10 grade plus all water-engine gates.


<!-- END PRESERVED SOURCE -->
