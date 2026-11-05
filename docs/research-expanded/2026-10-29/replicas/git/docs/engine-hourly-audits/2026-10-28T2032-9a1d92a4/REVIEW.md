# Expanded replica — docs/engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T2032-9a1d92a4/REVIEW.md |
| Preserved source SHA-256 | `1c46e8963474f90d6a67ee43052552e2640820bf912c94850f3a25a275a746d1` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev69 independent audit reports exact-blob checks, Java25 execution with --release 21, 12 marine-connectivity checks and nine immutable-snapshot checks. It preserves a deliberately failing deep, low-continentalness enclosed-basin witness against the production marine helper with API doubles. The result is a conditional heuristic counterexample, not a native-world reproduction. The broader Builder suites were inspected rather than independently rerun; historical Dev68 assertions remain historical. This expansion has read the report but has not rerun its tests.

## Research expansion — gaps and details to deepen

- A deep marine-looking core is a candidate signal, not a certificate of reachability to an ocean. The report exposes this distinction, but the finding needs a durable chain through subsequent bounded repair, source-mask policy, and future authoritative topology.
- Toolchain runtime, target bytecode release, exact production class, and API-double coverage should be separate receipt fields; Java25 execution with --release 21 must not imply complete Java25 native integration.

## Research expansion — proposed contracts

- Give the deep enclosed-basin witness a stable finding ID with geometry, signed coordinates, admitted graph values, exact helper hash, expected UNKNOWN/CLOSED classification, and the intentionally failing assertion.
- Track Dev70 and later source policies as dated successor evidence. Closing the bounded witness must not automatically close arbitrarily large basins, sampled narrow channels, native terrain generation, or physical water conservation.
- Require an explicit marine-authority provenance field whenever classification feeds flood roots, water chemistry, biome selection, or presentation. Heuristic and certified authority remain distinct states.

## Research expansion — acceptance checks

- Replay the bounded deep-basin witness on both signed seams and enlarge it beyond the search budget; verify unresolved traversal remains explicit instead of silently becoming proof of an ocean.
- Pair every closed-basin negative control with a genuinely connected open channel, a narrow sampled inlet, a ridge, and cache eviction; then repeat the selected cases through the real watershed/native boundary.

## Research expansion — source anchors

- Dev69 independent Java25 audit, 2026-10-28 20:32 PDT

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev69 independent Java25 audit, 2026-10-28 20:32 PDT

Pinned committed source: `9a1d92a44d7d3864d35e0c3ac288158a37c33015` (`0.0.1-alpha.29-dev.69-stitched-ocean.1`). Auditor source edits: **none**. Independently recovered and verified Temurin Java/Javac 25.0.5; owner-provided archive SHA256 `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`. Isolated Linux audit resources only; Builder untouched.

**Exact pinned Git blobs** compiled with Java25 `--release 21 -Xlint:all -Werror`: [V8OceanConnectivity.java](https://github.com/Shnewbs/Deep-Primal/blob/9a1d92a44d7d3864d35e0c3ac288158a37c33015/src/main/java/com/shnewbs/deepprimal/engine/V8OceanConnectivity.java) (`d858191e34b4b86a9dfd3f48136f151dea91f5a8`) and [LodVisibleSnapshot.java](https://github.com/Shnewbs/Deep-Primal/blob/9a1d92a44d7d3864d35e0c3ac288158a37c33015/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodVisibleSnapshot.java) (`eefd98ddc7ab8f1227291de5d8f7261e4165f3c1`), exact `git hash-object` matches. Minimal engine/Minecraft-free API doubles; **not real WatershedRegion, NeoForge or native rendering**.

Independent checks: **12/12** V8 marine seam/closed basin/connected open channel/ridge/cache checks and **9/9** LOD immutable snapshot/cleanup/oversize checks passed. **One independently reproduced conditional defect** with the exact V8 production class: a dry-ring-enclosed inland basin (x=7900..9500,z=1700..2700), sampled height=20 and continentalness=-0.4 throughout the depression, incorrectly yields `isMarineAt(8192,2048)=true`. The failing negative assertion exits 1 intentionally; see retained evidence. This is a synthetic admitted graph case and a heuristic false-positive counterexample, **not proven to occur in native V8 worlds**. Current deep marine core condition uses `height <= sea-24` and `continentalness <= -0.25`; neither guarantees reachability to an external ocean. Dev69 corrects the Dev68 shallow-land-continentalness regional-edge witness in independent positive cases but does not implement authoritative global outlet/discharge. Builder's committed 19-suite/21,012,646-check claim was inspected, not independently replayed. Historical Dev68 4,798 + 45,895 exact-source assertions reran under JDK25; they are **not** Dev69 tests.

Rubric scores **carried from last complete accepted review**, not freshly regraded: terrain 5, biomes 4, hydrology/caves 6, features 4, performance/threading 3, determinism 7, architecture 6, extensibility 4, testing/docs 7, polish/tooling 4. **50/100 = 5.0/10, Tier 2**. Strict threshold >8.5/10 unmet.

No JAR built or released per latest **October 27 owner-operated Gradle/Minecraft/GPU build override**, despite older instructions requesting an auditor-produced JAR. Local Git CLI cannot clone private repository: `Could not resolve host: github.com`. Thus no full Java25 Gradle9.2.1/NeoForge, archived/nested CRC and compiled-package identity, private fixture checks, real WatershedRegion, Mojang codec/save/reopen, water quantity/chemistry conservation, owner Windows AMD8060S Vulkan/OpenGL, native visual/frametime/benchmark acceptance. JAR retention remains unchanged; no stable release or new qualified binary claimed. Frozen V1-V7 files unchanged in compared Dev68-to-Dev69 commits, not native replay-verified.

Source evidence: `DeepPrimal_Dev69_IndependentAudit_20261028T2032_9a1d92a4_Java25Evidence.zip`, 13,438 bytes, SHA256 `f58fd11d6776026c1c53fbf76d4f3e119a3fa3f7eb1094fdb2a2c88a092fde0f`, 14 verified ZIP entries (local output). Next priorities: replace marine-core heuristic with true saved outlet topology and cross-region priority-flood parent/discharge; test deep closed basins and narrow inlets on both signed 8192 seams; native save/reopen/edits/finite water; then owner GPU/whole-frame tests. Auditor does not stop Builder.


<!-- END PRESERVED SOURCE -->
