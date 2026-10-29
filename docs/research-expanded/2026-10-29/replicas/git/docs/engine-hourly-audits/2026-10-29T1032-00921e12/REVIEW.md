# Expanded replica — docs/engine-hourly-audits/2026-10-29T1032-00921e12/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-29T1032-00921e12/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-29T1032-00921e12/REVIEW.md |
| Preserved source SHA-256 | `258abff95e2158655a1b9c38a34d33b149d269cba47ecdef2eb0ac398eb69e56` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev76 audit reports exact-blob Java25 execution of the region-port bridge and outlet solver with labeled boundary API doubles. It validates signed border deduplication, unknown frontiers, no fabricated runoff and three rejected mutations. Its new counterexample admits equal-ground neighboring observations with flood/stage 90 versus 900 and emits unchanged topology. Dev77 subsequently adds fail-closed observation checks, but that successor does not itself establish global routing reconciliation or native V8 activation.

## Research expansion — gaps and details to deepen

- Shared coordinates and matching ground do not imply matching hydrologic observations. The boundary contract must identify which stage, flood, catchment, classification and receiver fields must agree and what a mismatch means.
- Rejecting contradictory seams is necessary validation, but it is not a solver that repairs those seams. Zero-runoff topology exports also remain distinct from a complete discharge graph.

## Research expansion — proposed contracts

- Give every boundary snapshot an immutable source/region/recipe generation and a typed seam status. Record both disagreeing observations and a bounded diagnostic reason before refusing activation.
- Link the 90-versus-900 witness to the Dev77 shared-observation checks, preserving the original failure and requiring independent successor replay before promoting that finding.
- Specify a separate future global reconciliation step with authentic marine anchors, complete interior receiver paths and unique upstream discharge ownership; do not synthesize runoff from repeated cumulative boundary catchment.

## Research expansion — acceptance checks

- Vary one seam field at a time, reverse input order, include negative region boundaries and concurrent snapshot arrival, and verify deterministic rejection with the old accepted topology retained.
- Compile the full production WatershedRegion dependency graph and replay real neighboring snapshots through the bridge before native activation; then validate saved-policy provenance and finite-water lifecycle independently.

## Research expansion — source anchors

- Deep Primal — independent Dev76 audit (2026-10-29 10:32Z)
- Source developments
- Independent checks

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — independent Dev76 audit (2026-10-29 10:32Z)

**Pinned source:** `00921e12323bb759a22b557b0d07b8758ee1d753` — `0.0.1-alpha.29-dev.76-native-outlet-ports.1`. This is a read-only independent audit of the Builder's committed snapshot, not a release or production edit.

## Source developments

The Builder added [`WatershedRegion.boundarySnapshot()`](https://github.com/Shnewbs/Deep-Primal/blob/00921e12323bb759a22b557b0d07b8758ee1d753/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L1275-L1321), which exports real perimeter ground/flood/stage/catchment/receiver observations (explicit opt-in only), and [`WatershedRegionPortBridge.java`](https://github.com/Shnewbs/Deep-Primal/blob/00921e12323bb759a22b557b0d07b8758ee1d753/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java), which deduplicates signed region borders and retains unknown frontiers. No native V8 release or V1–V7 geography change was claimed. The renderer independently updated its default-off LOD motion-angle fast path; its native performance was not tested in this audit.

## Independent checks

- Owner's Library Temurin **25.0.5+8** toolchain restored and checked with `java -version`, `javac -version`. SHA256 of JDK archive `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`.
- Five extracted Java production/test/API-double files matched **exact pinned Git blob SHAs**, including bridge `9032f460195d506097da215ad0fcd1bf879a87f6` and outlet solver `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`.
- Isolated `javac --release 25 -Xlint:all -Werror` passed; **two Java25 runs passed 3,901 assertions each**. Two 8,192-block neighboring synthetic regions yielded **1,791 ports, 1,792 links, 257 shared coordinates, 1,536 unresolved frontiers**. Negative signs, ordering, concurrency, finite input admission, marine anchors, no fabricated runoff, and height mismatch checks passed.
- **3/3 independent source mutations rejected** (height parity guard loss, missing-frontier suppression, fabricated runoff). 
- **New independent gap reproduction:** the same boundary node can report `plannedStage` and `floodedElevation` 90 in one snapshot and 900 in its neighbor with identical ground. The bridge accepts the pair and produces **unchanged ports/links**. [`Sample.merge` L45–60](https://github.com/Shnewbs/Deep-Primal/blob/00921e12323bb759a22b557b0d07b8758ee1d753/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java#L45-L60) checks only ground and ocean; [port emission L128–155](https://github.com/Shnewbs/Deep-Primal/blob/00921e12323bb759a22b557b0d07b8758ee1d753/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegionPortBridge.java#L128-L155) emits zero runoff without using stage/flood/receiver. **Source-level contract gap, not a demonstrated native water bug.** Close before native activation.

**Unrun gates:** full production `WatershedRegion` compilation with dependencies, Gradle 9.2.1/NeoForge, class/package identity and ZIP CRC of a new JAR, native saved-world/water unload/reopen, Windows AMD8060S Vulkan/OpenGL and GPU performance. Git CLI cannot resolve GitHub, so a complete isolated current checkout was unavailable. The October 27 owner override reserves full builds to the owner. **No new development JAR was built, promoted or uploaded.**

## Ten-category grade (unchanged accepted baseline, not a fresh overall regrade)

| Category | Score / 10 |
| --- | ---: |
| Terrain realism | 5 |
| Biome blending | 4 |
| Hydrology and caves | 6 |
| Features and structures | 4 |
| Performance and threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Overall (Tier 2)** | **50/100, 5.0/10** |

The prior complete [rubric](https://github.com/Shnewbs/Deep-Primal/blob/00921e12323bb759a22b557b0d07b8758ee1d753/docs/engine-review-2026-10-24/REVIEW.md) remains authoritative. Target **strictly >8.5/10** is not met.

## Next priorities

1. Fail-closed hydrology seam reconciliation for stage, flood and receiver; true interior paths and unique upstream discharge accounting.
2. Authoritative global ocean outlet certificates, lake spill, signed boundary / profile / edit preservation, native conservation through unload and restart.
3. Owner full Java25/Gradle/NeoForge and Windows AMD8060S Vulkan/OpenGL acceptance, including matched adverse-scene LOD and water/frame-time benchmarks.

**Evidence archive:** `DeepPrimal_Dev76_IndependentAudit_20261029T1032_00921e12_Java25Evidence.zip`, Library `/Deep Primal/Independent Audits`, **18,661 bytes**, SHA256 `a8ee36954b00e11b201f9dfdcb20bdecd3fcb6af22e9ae2c385ace16e9f91de5` (20 entries, outer CRC and member hashes passed). Source-only, no Builder files touched.

<!-- END PRESERVED SOURCE -->
