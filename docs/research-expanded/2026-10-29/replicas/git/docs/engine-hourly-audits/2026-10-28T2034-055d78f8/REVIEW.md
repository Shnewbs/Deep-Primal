# Expanded replica — docs/engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md |
| Preserved source SHA-256 | `65fd15b665018cc7701f837e92791b3d925b25e6b12e5d80bfce3115eb86cf8a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Dev62 independent audit — 2026-10-28T20:34Z”. Its original opening states: “Pinned source: 055d78f8535d5380ac99e2dd0fc43da49ae5ba89, version 0.0.1-alpha.29-dev.62-coastal-proof.1. No production files or builder/renderer resources changed.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityGraph.java`, `DensityVolumeBounds.java`, `TerrainShapeV8.java`, `LodMeshPartSplitter.java`.

## Research expansion — gaps and details to deepen

- Make ocean/outlet connectivity a regional topology contract; a local low edge cannot by itself establish a connection to the ocean.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define canonical basin/outlet identity and cross-region obligations, with explicit UNKNOWN coverage instead of guessing an ocean classification.
- Save the topology revision and distinguish proposed worldgen water placement from owned runtime quantity and chemistry.
- Retain the signed-side x=8190/8192/8194, z=2048 enclosed-basin witness as one explicit regression, then add a multi-region oracle whose ocean outlets are defined independently of local region edges.
- Read the later Dev69 ocean-seam receipt as a source-only successor: opt-in policy v2 inside saved-profile r5, unchanged legacy v1, and bounded sampled marine admission. Its API-double checks do not close real priority-flood outlet/discharge, native codec/reopen, or conservation gates.

## Research expansion — acceptance checks

- Build enclosed low basins spanning positive and negative region borders and compare classifications under different neighbor arrival orders.
- Replay the source record's seam or sea-plane witnesses against real WatershedRegion and native generation; keep API-double outcomes separately labeled.

## Research expansion — source anchors

- Dev62 independent audit — 2026-10-28T20:34Z
- Independently verified
- Grade

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev62 independent audit — 2026-10-28T20:34Z

**Pinned source:** `055d78f8535d5380ac99e2dd0fc43da49ae5ba89`, version `0.0.1-alpha.29-dev.62-coastal-proof.1`. No production files or builder/renderer resources changed.

## Independently verified

Reconstructed the committed sources from approved retained Dev61 evidence plus the Dev62 committed changes. Exact Git blob identities:
- `DensityGraph.java`: `aec303603a8ec85b598c4797ef20ac7c040dcf2d`
- `DensityVolumeBounds.java`: `6962711c12bf01b1e0f956275f09781d2716e19a`
- `TerrainShapeV8.java`: `726edf77ed103c16a0f94c2a6dd45257763a4319`
- `LodMeshPartSplitter.java`: `e9ab67d591b5628e61d7b2576d90adced763f1aa`

Isolated Java21 strict compile `--release 21 -Xlint:all -Werror`, runtime `-ea -Xverify:all`:
- **Coast V4:** 135,580 independent checks, 22,800 sampled columns, 18,693 protected coasts bit-identical; **zero V1/V4 sea-plane classification flips**, including both Dev61 witnesses (seed 0 at -2006,-2282; seed 922337203685 at 9459,9273). V1/V2/V3 saved graph definitions match the frozen prior source. 2,280 sampled interval classifications were all UNCERTAIN; no preflight speed claim.
- **Opt-in LOD split:** 9,638,305 independent checks; 420 randomized scenarios (373 valid, 47 malformed), 6,254 fragments, exact vertex/normal/color/index/bounds replay, cap and cancellation. The full pinned `LodMeshPartSplitter.java` was compiled with isolated test API doubles, not native Minecraft.
- Both signed-distance and inverted split-plan mutants compiled but failed the independent regression, as intended.

**Total newly independent checks: 9,773,885.** Producer-reported 20,929,784 V4 and 14,154,029 LOD self-checks were reviewed but not counted as independent evidence.

## Grade

Historical **50/100, 5.0/10, Tier 2** carries forward, not a new complete regrade. Terrain 5; biome 4; hydrology/caves 6; features/structures 4; performance/threading 3; determinism 7; architecture 6; extensibility 4; tests/docs 7; polish/tooling 4. Evidence/rationale: [baseline rubric](../../engine-review-2026-10-24/REVIEW.md). The strict >8.5 target is still unmet.

## Open gates and owner policy

The V4 operator only preserves a sampled numeric sea-plane side; it is not an authoritative ocean mask, routed hydrology, finite-water accounting, native generator, save/reopen or Windows GPU acceptance. The LOD optimization is optional/off by default and has no accepted hardware FPS evidence. Root work-state still records Dev53 while scoped terrain work-state records Dev62; reconcile only through the Builder's ownership protocol.

The October 27 owner override places full Java25/Gradle/Minecraft/GPU and development JAR production with the owner. No JAR was attempted, qualified or published. Java25 and its restored archive are absent in this isolated environment, and DNS prevents cloning the complete private checkout. Full Gradle, outer/nested archive CRC and class identity, packaged Windows Vulkan/OpenGL, Themes/notice merge, native V8 saved-world, water ledger and AMD8060S acceptance were not run. No other task, lease or timed performance reservation was interrupted. Revisit actual native ocean masks, water unload/reopen conservation, and Vulkan/LOD frame-time p99 before a regrade.

The independent auditor remains active; its report is observational, not a Builder stop gate.

<!-- END PRESERVED SOURCE -->
