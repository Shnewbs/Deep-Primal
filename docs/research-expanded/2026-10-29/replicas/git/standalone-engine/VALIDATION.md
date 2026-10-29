# Expanded replica — standalone-engine/VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/VALIDATION.md) |
| Source identity | git: standalone-engine/VALIDATION.md |
| Preserved source SHA-256 | `11049e98c43a18e85617f6d3fca3a07842a5256b925b09b7acbeebf8690065f7` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

Primal Engine alpha1 preview validation of a separate Minecraft-hosted terrain and integer-fluid core with package and native server/client checks. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the independent core boundary is not a standalone executable or complete Deep Primal replacement, and old worlds are explicitly not migrated. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a historical prototype capability map separating reusable core ideas from host storage, native rendering, unfinished gameplay and current integrated modules. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Verify the archived artifact's class and nested-library identities, use disposable worlds for historical reproduction and require new host contracts before treating the preview as a standalone migration baseline. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Primal Engine 0.0.1-alpha.1 — executed validation

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `packaging-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/packaging-audit.json) |
| `validation/native-client.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/validation/native-client.txt) |
| `validation/native-fresh.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/validation/native-fresh.txt) |
| `validation/native-reopen.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/validation/native-reopen.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Primal Engine 0.0.1-alpha.1 — executed validation

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This is an engineering preview of the independent engine path. It is not the completed Deep Primal replacement or a visual-quality release.

## Build and core

The standalone `build` and `nativeChecksClasses` tasks pass. Source boundary checks reject Minecraft/NeoForge imports from the terrain/transport core and reject dependencies on the legacy Deep Primal package or its noise generator. The new generator directly fills chunk columns and owns its biome selection.

Executed core checks cover 500,000 integer fluid transfers with capacity/conservation invariants, 4,225 deterministic geography comparisons, an additional 50,000 jittered channel probes, acyclic/downhill continent drainage and 10,000 common-edge mesh checks. For seed 20261020, the continental grid survey found 1,231 land and 2,994 ocean points; the channel probes found 109 river samples. These are coverage counts, not a quality score or a hardware performance result.

LOD storage checks round-trip a 1,024-block-tall synthetic chunk at signed coordinates, preserving a raised player-build interval, a tree interval and the intervening air. They reject a stale revision and a truncated compressed file. That simple synthetic fixture compressed to 664 bytes, with a 7,492-byte data estimate; this is not an estimate for arbitrary real-world LOD memory and excludes JVM/container overhead.

## Packaged native server

A packaged server loaded `Primal Engine` without the legacy Deep Primal modules. The normal Overworld preset selected the custom generator. Nine loaded chunks had the expected solid floor. A sealed native basin retained sixteen units during gravity/pooling. Importing a partial vanilla water state added exactly eight units through the new conversion adapter, without running native spreading. Full bucket extraction removed the cell and returned our item; a partial cell could not yield a full bucket.

An actual loaded chunk was captured in increments of at most 2,048 block reads. Its disk snapshot retained the engine fluid's seven-unit state. A subsequent packaged-server launch reopened both the world fluid state (units=7, flow=3) and the LOD snapshot successfully. Evidence: [fresh server](validation/native-fresh.txt), [reopen](validation/native-reopen.txt).

The fixtures run in a disposable world and are not included in the distributed JAR. These tests do not certify piston displacement, waterlogged shapes, pressure, every bucket/mod integration or arbitrary chunk-boundary changes.

## Packaged native client

The packaged OpenGL client passed on Mesa llvmpipe software rendering, with the legacy mod absent. The final run exercised 6,121 custom-meshed fluid cells and 384 curved-cell paths, captured an inland scene and a coast plus an explicitly constructed test basin, then shut down successfully. Evidence: [client](validation/native-client.txt). Native water meshing is skipped for our registered fluid through NeoForge's custom-fluid rendering interface; platform chunk buffers and water materials remain in use.

The first client run found a mixin-package bootstrap error; it was fixed and the successful runs were repeated. A test-camera issue was also corrected so the final captures show loaded scenes rather than a loading screen. The source tree contains no unresolved bootstrap failure from those attempts.

Visual inspection shows broad, simple terrain terraces, sparse placeholder trees and rudimentary coasts. Those scenes are not TFC parity or final polish. Surf, sophisticated erosion/deposition and a complete hydraulic surface model are absent. The separate LOD module currently provides data capture/storage primitives; it does not yet draw distant terrain or update/synchronize it automatically. No DH comparison has run, and no superiority or hardware-FPS claim is made.

## Packaging and limits

The JAR is 56,028 bytes, SHA-256 `efdd92291a1b29d309a5c9d9274363469a1728ee736822854e47ad4bbac77e04`. It contains one nested LOD-core library and 24 project classes in total, with no duplicated project classes, platform classes, development fixtures or legacy Deep Primal code. See [packaging audit](packaging-audit.json).

Use a new disposable world and replace the older Deep Primal worldgen JAR while testing. The normal preset belongs to this independent generator; old worlds are not migrated. The Nether and End remain native. The full gameplay, settings, renderer/LOD and environmental replacement remains unfinished, and this preview has not been published to CurseForge.


<!-- END PRESERVED SOURCE -->
